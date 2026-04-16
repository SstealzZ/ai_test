async function api(path, options = {}) {
    const response = await fetch(path, {
        headers: {
            ...(options.body ? {'Content-Type': 'application/json'} : {})
        },
        ...options
    });

    if (!response.ok) {
        const text = await response.text();
        throw new Error(`HTTP ${response.status}: ${text}`);
    }

    if (response.status === 204) {
        return null;
    }
    return response.json();
}

function show(id, value) {
    document.getElementById(id).textContent = JSON.stringify(value, null, 2);
}

document.getElementById('refreshMeBtn').addEventListener('click', async () => {
    try {
        const me = await api('/api/me');
        show('meBox', me);
    } catch (e) {
        show('meBox', {error: e.message});
    }
});

document.getElementById('loadThresholdBtn').addEventListener('click', async () => {
    try {
        const data = await api('/api/settings/threshold');
        show('thresholdBox', data);
        document.getElementById('thresholdInput').value = data.thresholdDays;
    } catch (e) {
        show('thresholdBox', {error: e.message});
    }
});

document.getElementById('saveThresholdBtn').addEventListener('click', async () => {
    try {
        const thresholdDays = Number(document.getElementById('thresholdInput').value);
        const data = await api('/api/settings/threshold', {
            method: 'PUT',
            body: JSON.stringify({thresholdDays})
        });
        show('thresholdBox', data);
    } catch (e) {
        show('thresholdBox', {error: e.message});
    }
});

document.getElementById('uploadBtn').addEventListener('click', async () => {
    const fileInput = document.getElementById('certFileInput');
    if (!fileInput.files || fileInput.files.length === 0) {
        alert('Select a certificate file first.');
        return;
    }

    const formData = new FormData();
    formData.append('file', fileInput.files[0]);

    try {
        const response = await fetch('/api/certificates/upload', {method: 'POST', body: formData});
        const payload = await response.json();
        if (!response.ok) {
            throw new Error(JSON.stringify(payload));
        }
        alert(`Added certificate: ${payload.id}`);
    } catch (e) {
        alert(`Upload failed: ${e.message}`);
    }
});

document.getElementById('urlBtn').addEventListener('click', async () => {
    const url = document.getElementById('urlInput').value;
    try {
        const data = await api('/api/certificates/from-url', {
            method: 'POST',
            body: JSON.stringify({url})
        });
        alert(`Added certificate: ${data.id}`);
    } catch (e) {
        alert(`URL import failed: ${e.message}`);
    }
});

document.getElementById('loadCertsBtn').addEventListener('click', async () => {
    try {
        const data = await api('/api/certificates');
        show('certsBox', data);
    } catch (e) {
        show('certsBox', {error: e.message});
    }
});

document.getElementById('loadAlertsBtn').addEventListener('click', async () => {
    try {
        const data = await api('/api/alerts');
        show('alertsBox', data);
    } catch (e) {
        show('alertsBox', {error: e.message});
    }
});
