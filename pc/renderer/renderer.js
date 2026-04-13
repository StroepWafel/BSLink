let current = null;

let outDir = null;



const el = (id) => document.getElementById(id);



const STORAGE_LAN_ALLOW_AUTO = 'beastsaber.lanAllowAutoDownload';

const STORAGE_EXTRACT_ZIPS = 'beastsaber.extractZipsAfterDownload';

const STORAGE_DELETE_ZIPS = 'beastsaber.deleteZipsAfterExtract';



function collectSettings() {

  return {

    outDir,

    extractZips: el('chkExtractZips')?.checked === true,

    deleteZipsAfterExtract: el('chkDeleteZipsAfterExtract')?.checked === true,

    lanAllowAutoDownload: el('lanAllowAutoDownload')?.checked === true,

    plTitle: el('plTitle')?.value ?? 'My list',

    plAuthor: el('plAuthor')?.value ?? 'BeastSaber'

  };

}



function saveSettings() {

  if (!window.bs?.setSettings) return;

  window.bs.setSettings(collectSettings());

}



let saveDebounce;

function scheduleSaveSettings() {

  clearTimeout(saveDebounce);

  saveDebounce = setTimeout(saveSettings, 300);

}



/** One-time migration from older localStorage prefs into settings.json */

function migrateLocalStorageOnce() {

  if (localStorage.getItem('beastsaber.settingsMigrated') === '1') return null;

  const patch = {};

  if (localStorage.getItem(STORAGE_LAN_ALLOW_AUTO) === '1') patch.lanAllowAutoDownload = true;

  if (localStorage.getItem(STORAGE_EXTRACT_ZIPS) === '1') patch.extractZips = true;

  if (localStorage.getItem(STORAGE_DELETE_ZIPS) === '1') patch.deleteZipsAfterExtract = true;

  localStorage.setItem('beastsaber.settingsMigrated', '1');

  return Object.keys(patch).length ? patch : null;

}



function applySettingsToUi(s) {

  outDir = s.outDir || null;

  el('folderPath').textContent = outDir || 'Not set.';



  const ex = el('chkExtractZips');

  const del = el('chkDeleteZipsAfterExtract');

  if (ex) ex.checked = !!s.extractZips;

  if (del) {

    del.checked = !!s.deleteZipsAfterExtract;

    del.disabled = !ex?.checked;

  }



  const lan = el('lanAllowAutoDownload');

  if (lan) lan.checked = !!s.lanAllowAutoDownload;



  if (el('plTitle')) el('plTitle').value = s.plTitle || 'My list';

  if (el('plAuthor')) el('plAuthor').value = s.plAuthor || 'BeastSaber';



  refreshButtons();

}



function wirePersistListeners() {

  const ex = el('chkExtractZips');

  const del = el('chkDeleteZipsAfterExtract');

  if (ex) {

    ex.addEventListener('change', () => {

      if (!ex.checked && del) {

        del.checked = false;

    }

      if (del) del.disabled = !ex.checked;

      saveSettings();

    });

  }

  if (del) del.addEventListener('change', saveSettings);



  const lan = el('lanAllowAutoDownload');

  if (lan) lan.addEventListener('change', saveSettings);



  const pt = el('plTitle');

  const pa = el('plAuthor');

  if (pt) {
    pt.addEventListener('input', scheduleSaveSettings);
    pt.addEventListener('blur', saveSettings);
  }

  if (pa) {
    pa.addEventListener('input', scheduleSaveSettings);
    pa.addEventListener('blur', saveSettings);
  }

}



(async function bootstrap() {

  if (!window.bs?.getSettings) return;

  let s = await window.bs.getSettings();

  const mig = migrateLocalStorageOnce();

  if (mig) {

    await window.bs.setSettings({ ...s, ...mig });

    s = { ...s, ...mig };

  }

  applySettingsToUi(s);

  wirePersistListeners();

})();



function setSummary(data) {

  current = data;

  const n = data.maps?.length ?? 0;

  el('listSummary').textContent = n ? `Loaded ${n} map(s).` : 'No list loaded.';

  const ul = el('mapPreview');

  ul.innerHTML = '';

  (data.maps || []).slice(0, 50).forEach((m) => {

    const li = document.createElement('li');

    li.textContent = `${m.songName} — ${m.key}`;

    ul.appendChild(li);

  });

  if (n > 50) {

    const li = document.createElement('li');

    li.textContent = `… and ${n - 50} more`;

    ul.appendChild(li);

  }

  refreshButtons();

}



function refreshButtons() {

  const ok = current && current.maps && current.maps.length > 0;

  el('btnDownload').disabled = !ok || !outDir;

  el('btnBplist').disabled = !ok || !outDir;

}



/**

 * @param {{ lanInfoIntro?: string }} opts - If set (e.g. LAN auto-download), same extract/delete flags apply and result is shown in lanInfo.

 */

async function runDownload(opts = {}) {

  if (!current?.maps?.length || !outDir) return;

  const extractZips = el('chkExtractZips')?.checked === true;

  const deleteZipsAfterExtract = el('chkDeleteZipsAfterExtract')?.checked === true;

  el('dlStatus').textContent = extractZips ? 'Downloading and extracting…' : 'Downloading…';

  const res = await window.bs.downloadMaps({

    outDir,

    maps: current.maps,

    concurrency: 4,

    extractZips,

    deleteZipsAfterExtract

  });

  const failed = res.filter((r) => !r.ok);

  const extractFailed = res.filter((r) => r.ok && r.extractError);

  const extracted = res.filter((r) => r.ok && r.extractedTo).length;

  const zipsRemoved = res.filter((r) => r.ok && r.zipDeleted).length;

  let msg =

    failed.length === 0

      ? `Done. ${res.length} download(s).`

      : `Finished with ${failed.length} download error(s).`;

  if (extractZips && extracted > 0) {

    msg += ` Extracted ${extracted} folder(s).`;

  }

  if (deleteZipsAfterExtract && zipsRemoved > 0) {

    msg += ` Removed ${zipsRemoved} ZIP(s).`;

  }

  if (extractFailed.length > 0) {

    msg += ` ${extractFailed.length} extract error(s) — ZIPs kept on disk.`;

  }

  el('dlStatus').textContent = msg;

  if (opts.lanInfoIntro != null) {

    el('lanInfo').textContent = `${opts.lanInfoIntro}\n\n${msg}`;

  }

}



el('btnImport').onclick = async () => {

  const data = await window.bs.importJsonPath();

  if (data) setSummary(data);

};



el('btnFolder').onclick = async () => {

  const p = await window.bs.selectFolder();

  if (p) {

    outDir = p;

    el('folderPath').textContent = p;

    refreshButtons();

    saveSettings();

  }

};



el('btnDownload').onclick = () => runDownload();



el('btnBplist').onclick = async () => {

  el('bpStatus').textContent = 'Writing…';

  const dest = await window.bs.writeBplist({

    outDir,

    maps: current.maps,

    title: el('plTitle').value || 'My list',

    author: el('plAuthor').value || 'BeastSaber'

  });

  el('bpStatus').textContent = `Wrote ${dest}`;

};



async function showLanQr(info) {

  const wrap = el('lanQrWrap');

  const img = el('lanQr');

  const base = info?.baseUrl || info?.url;

  if (!base || !info?.token) {

    wrap.hidden = true;

    return;

  }

  const qrUrl = `${base.replace(/\/$/, '')}/?token=${encodeURIComponent(info.token)}`;

  try {

    if (!window.bs?.qrDataUrl) {

      throw new Error('QR bridge missing');

    }

    const dataUrl = await window.bs.qrDataUrl(qrUrl);

    if (!dataUrl || typeof dataUrl !== 'string') {

      throw new Error('QR generator returned empty');

    }

    img.src = dataUrl;

    wrap.hidden = false;

  } catch (e) {

    wrap.hidden = true;

    const prev = el('lanInfo').textContent || '';

    const hint = `\n\nQR could not be generated: ${e?.message || e}. URL for manual entry: ${qrUrl}`;

    if (!prev.includes('QR could not be generated')) {

      el('lanInfo').textContent = prev + hint;

    }

  }

}



el('btnLanStart').onclick = async () => {

  if (!window.bs?.lanStart) {

    el('lanInfo').textContent =

      'Internal error: app bridge missing. Reinstall or run from source (npm start in pc/).';

    return;

  }

  el('lanInfo').textContent = 'Starting…';

  try {

    const info = await window.bs.lanStart();

    el('btnLanStop').disabled = false;

    el('lanInfo').textContent = JSON.stringify(info, null, 2);

    await showLanQr(info);

  } catch (err) {

    el('btnLanStop').disabled = true;

    el('lanQrWrap').hidden = true;

    el('lanInfo').textContent = `Could not start receiver:\n${err?.message || err}`;

  }

};



el('btnLanStop').onclick = async () => {

  await window.bs.lanStop();

  el('btnLanStop').disabled = true;

  el('lanInfo').textContent = 'Stopped.';

  el('lanQrWrap').hidden = true;

};



window.bs.onLanEvent((ev) => {

  if (ev.type === 'import' && ev.data) {

    setSummary(ev.data);

    let msg = `Received list from phone. ${ev.data.maps?.length ?? 0} map(s).`;

    if (ev.autoDownload) {

      const allowOnPc = el('lanAllowAutoDownload')?.checked === true;

      if (outDir && allowOnPc) {

        const intro = `Received list from phone. ${ev.data.maps?.length ?? 0} map(s).`;

        el('lanInfo').textContent = `${intro}\nStarting download…`;

        // Same extract/delete checkboxes as manual download (section 3).

        runDownload({ lanInfoIntro: intro });

      } else if (!outDir) {

        msg +=

          '\nAuto-download skipped: choose a download folder first (step 2), then send again or download manually.';

        el('lanInfo').textContent = msg;

      } else if (!allowOnPc) {

        msg +=

          '\nAuto-download skipped: enable “Allow automatic downloads when the phone requests it” above (PC safety).';

        el('lanInfo').textContent = msg;

      }

    } else {

      el('lanInfo').textContent = msg;

    }

  }

  if (ev.type === 'started') {

    el('lanInfo').textContent =

      `POST JSON to:\n${ev.baseUrl}/import?token=${ev.token}\n\nToken: ${ev.token}`;

    showLanQr(ev);

  }

});


