const { app, BrowserWindow, ipcMain, dialog, Menu } = require('electron');
const path = require('path');
const fsSync = require('fs');
const fs = require('fs/promises');

const windowIconPath = path.join(__dirname, 'build', 'icon.png');
const http = require('http');
const express = require('express');

let mainWindow;
let lanServer = null;
let lanToken = '';
let lanPort = 3847;
let lanBaseUrl = '';

function settingsFilePath() {
  return path.join(app.getPath('userData'), 'settings.json');
}

const defaultSettings = {
  outDir: null,
  extractZips: false,
  deleteZipsAfterExtract: false,
  lanAllowAutoDownload: false,
  plTitle: 'My list',
  plAuthor: 'BeastSaber'
};

function readSettingsSync() {
  try {
    const raw = fsSync.readFileSync(settingsFilePath(), 'utf8');
    return { ...defaultSettings, ...JSON.parse(raw) };
  } catch {
    return { ...defaultSettings };
  }
}

function writeSettingsSync(patch) {
  const next = { ...readSettingsSync(), ...patch };
  const dir = path.dirname(settingsFilePath());
  if (!fsSync.existsSync(dir)) fsSync.mkdirSync(dir, { recursive: true });
  fsSync.writeFileSync(settingsFilePath(), JSON.stringify(next, null, 2), 'utf8');
}

ipcMain.handle('settings-get', () => readSettingsSync());
ipcMain.handle('settings-set', (_e, patch) => {
  writeSettingsSync(patch);
  return true;
});

function createWindow() {
  mainWindow = new BrowserWindow({
    width: 1040,
    height: 800,
    minWidth: 720,
    minHeight: 560,
    autoHideMenuBar: true,
    icon: fsSync.existsSync(windowIconPath) ? windowIconPath : undefined,
    webPreferences: {
      preload: path.join(__dirname, 'preload.js'),
      contextIsolation: true,
      nodeIntegration: false
    }
  });
  mainWindow.setMenuBarVisibility(false);
  mainWindow.loadFile(path.join(__dirname, 'renderer', 'index.html'));
}

function parseExport(data) {
  if (typeof data === 'string') data = JSON.parse(data);
  if (!data || data.format !== 'bsaber-map-list' || !Array.isArray(data.maps)) {
    throw new Error('Invalid file: expected format bsaber-map-list with maps[]');
  }
  return data;
}

ipcMain.handle('select-folder', async () => {
  const r = await dialog.showOpenDialog(mainWindow, {
    properties: ['openDirectory']
  });
  if (r.canceled || !r.filePaths[0]) return null;
  return r.filePaths[0];
});

ipcMain.handle('import-json-path', async () => {
  const r = await dialog.showOpenDialog(mainWindow, {
    properties: ['openFile'],
    filters: [{ name: 'JSON', extensions: ['json'] }]
  });
  if (r.canceled || !r.filePaths[0]) return null;
  const text = await fs.readFile(r.filePaths[0], 'utf8');
  return parseExport(text);
});

ipcMain.handle('load-json-text', async (_e, text) => parseExport(text));

/** Generate LAN pairing QR in main process (reliable vs require() in preload when packaged). */
ipcMain.handle('qr-data-url', async (_e, text) => {
  const QRCode = require('qrcode');
  return QRCode.toDataURL(String(text), { margin: 1, width: 220, type: 'png' });
});


async function extractMapZip(zipPath, outDir, baseFolderName) {
  const extract = require('extract-zip');
  const extractDir = path.join(outDir, baseFolderName);
  await fs.mkdir(extractDir, { recursive: true });
  const absDir = path.resolve(extractDir);
  await extract(zipPath, { dir: absDir });
  return extractDir;
}

ipcMain.handle('download-maps', async (_e, payload) => {
  const {
    outDir,
    maps,
    concurrency = 4,
    extractZips = false,
    deleteZipsAfterExtract = false
  } = payload;
  if (!outDir || !Array.isArray(maps)) throw new Error('Missing outDir or maps');
  const results = [];
  const safe = (s) => String(s).replace(/[<>:"/\\|?*]+/g, '_').slice(0, 120);

  for (let i = 0; i < maps.length; i += concurrency) {
    const batch = maps.slice(i, i + concurrency);
    const part = await Promise.all(
      batch.map(async (m) => {
        const name = `${safe(m.key)} - ${safe(m.songName)}.zip`;
        const dest = path.join(outDir, name);
        const baseFolderName = path.basename(dest, '.zip');
        try {
          const res = await fetch(m.downloadURL);
          if (!res.ok) throw new Error(`HTTP ${res.status}`);
          const buf = Buffer.from(await res.arrayBuffer());
          await fs.writeFile(dest, buf);
          const row = { key: m.key, ok: true, path: dest };
          if (extractZips) {
            try {
              row.extractedTo = await extractMapZip(dest, outDir, baseFolderName);
              if (deleteZipsAfterExtract) {
                await fs.unlink(dest);
                row.zipDeleted = true;
              }
            } catch (ex) {
              row.extractError = String(ex.message || ex);
            }
          }
          return row;
        } catch (e) {
          return { key: m.key, ok: false, error: String(e.message || e) };
        }
      })
    );
    results.push(...part);
    if (mainWindow) {
      mainWindow.webContents.send('lan-event', {
        type: 'download-progress',
        done: Math.min(i + batch.length, maps.length),
        total: maps.length
      });
    }
  }
  return results;
});

ipcMain.handle('write-bplist', async (_e, payload) => {
  const { outDir, maps, title, author } = payload;
  const playlist = {
    playlistTitle: title || 'BeastSaber export',
    playlistAuthor: author || 'BeastSaber PC',
    image: '',
    songs: maps.map((m) => ({
      key: m.key,
      hash: m.hash,
      songName: m.songName,
      levelAuthorName: m.levelAuthorName || ''
    }))
  };
  const dest = path.join(outDir, 'playlist.bplist');
  await fs.writeFile(dest, JSON.stringify(playlist, null, 2), 'utf8');
  return dest;
});

function randomToken() {
  return require('crypto').randomBytes(12).toString('hex');
}

function getLanIps() {
  const nets = require('os').networkInterfaces();
  const ips = [];
  for (const name of Object.keys(nets)) {
    for (const net of nets[name] || []) {
      const fam = net.family;
      const isV4 = fam === 'IPv4' || fam === 4;
      if (isV4 && !net.internal) ips.push(net.address);
    }
  }
  return ips;
}

function getLanNetworkBaseUrl() {
  const ips = getLanIps();
  const primary = ips[0] || '127.0.0.1';
  return `http://${primary}:${lanPort}`;
}

ipcMain.handle('lan-start', async () => {
  try {
    if (lanServer) {
      const ips = getLanIps();
      return {
        running: true,
        port: lanPort,
        token: lanToken,
        baseUrl: lanBaseUrl || getLanNetworkBaseUrl(),
        url: `http://127.0.0.1:${lanPort}`,
        ips
      };
    }
    lanToken = randomToken();
    const exp = express();
    exp.use(express.json({ limit: '25mb' }));

    exp.post('/import', (req, res) => {
      const token = req.query.token || req.headers['x-beastsaber-token'];
      if (!token || token !== lanToken) {
        res.status(401).json({ error: 'Unauthorized' });
        return;
      }
      try {
        const data = parseExport(req.body);
        const autoDownload =
          req.query.autoDownload === '1' ||
          req.query.autoDownload === 'true' ||
          req.query.autoDownload === 'yes';
        if (mainWindow) {
          mainWindow.webContents.send('lan-event', { type: 'import', data, autoDownload });
        }
        res.json({ ok: true, maps: data.maps.length });
      } catch (e) {
        res.status(400).json({ error: String(e.message || e) });
      }
    });

    exp.get('/health', (_req, res) => {
      res.json({ ok: true, app: 'beastsaber-pc' });
    });

    const server = http.createServer(exp);
    await new Promise((resolve, reject) => {
      server.on('error', reject);
      server.listen(lanPort, '0.0.0.0', () => resolve());
    });
    lanServer = server;

    const ips = getLanIps();
    const base = getLanNetworkBaseUrl();
    lanBaseUrl = base;

    if (mainWindow) {
      mainWindow.webContents.send('lan-event', {
        type: 'started',
        port: lanPort,
        token: lanToken,
        baseUrl: base
      });
    }

    return { running: true, port: lanPort, token: lanToken, baseUrl: base, ips };
  } catch (e) {
    const msg =
      e && e.code === 'EADDRINUSE'
        ? `Port ${lanPort} is already in use. Close the other app or change the port in the PC app source.`
        : String(e.message || e);
    throw new Error(msg);
  }
});

ipcMain.handle('lan-stop', async () => {
  if (lanServer) {
    await new Promise((resolve) => lanServer.close(() => resolve()));
    lanServer = null;
    lanBaseUrl = '';
  }
  if (mainWindow) {
    mainWindow.webContents.send('lan-event', { type: 'stopped' });
  }
  return { running: false };
});

app.whenReady().then(() => {
  Menu.setApplicationMenu(null);
  createWindow();
  app.on('activate', () => {
    if (BrowserWindow.getAllWindows().length === 0) createWindow();
  });
});

app.on('window-all-closed', () => {
  if (lanServer) {
    lanServer.close();
    lanServer = null;
  }
  if (process.platform !== 'darwin') app.quit();
});
