const { contextBridge, ipcRenderer } = require('electron');

contextBridge.exposeInMainWorld('bs', {
  selectFolder: () => ipcRenderer.invoke('select-folder'),
  importJsonPath: () => ipcRenderer.invoke('import-json-path'),
  loadJsonText: (text) => ipcRenderer.invoke('load-json-text', text),
  downloadMaps: (payload) => ipcRenderer.invoke('download-maps', payload),
  writeBplist: (payload) => ipcRenderer.invoke('write-bplist', payload),
  lanStart: () => ipcRenderer.invoke('lan-start'),
  lanStop: () => ipcRenderer.invoke('lan-stop'),
  relayStart: (relayUrl) => ipcRenderer.invoke('relay-start', relayUrl),
  relayStop: () => ipcRenderer.invoke('relay-stop'),
  onLanEvent: (cb) => {
    ipcRenderer.on('lan-event', (_e, data) => cb(data));
  },
  qrDataUrl: (text) => ipcRenderer.invoke('qr-data-url', text),
  getSettings: () => ipcRenderer.invoke('settings-get'),
  setSettings: (patch) => ipcRenderer.invoke('settings-set', patch)
});
