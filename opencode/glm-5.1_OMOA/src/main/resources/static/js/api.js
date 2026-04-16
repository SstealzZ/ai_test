const API = {
    BASE_URL: '/api',

    headers() {
        const h = { 'Content-Type': 'application/json' };
        const token = Auth.getToken();
        if (token) h['Authorization'] = 'Bearer ' + token;
        return h;
    },

    async request(method, path, body, isFormData) {
        const opts = {
            method,
            headers: isFormData ? { 'Authorization': 'Bearer ' + Auth.getToken() } : this.headers()
        };
        if (body) opts.body = isFormData ? body : JSON.stringify(body);

        const res = await fetch(this.BASE_URL + path, opts);
        if (res.status === 204) return null;
        const data = await res.json();

        if (!res.ok) {
            throw { status: res.status, message: data.detail || data.message || data.error || 'Request failed' };
        }
        return data;
    },

    getCertificates() { return this.request('GET', '/certificates'); },
    getExpiringCertificates() { return this.request('GET', '/certificates/expiring'); },
    getCertificate(id) { return this.request('GET', '/certificates/' + id); },
    uploadCertificate(file) {
        const fd = new FormData();
        fd.append('file', file);
        return this.request('POST', '/certificates/upload', fd, true);
    },
    fetchCertificate(url) { return this.request('POST', '/certificates/fetch', { url }); },
    deleteCertificate(id) { return this.request('DELETE', '/certificates/' + id); },

    getThresholds() { return this.request('GET', '/thresholds'); },
    getActiveThresholds() { return this.request('GET', '/thresholds/active'); },
    createThreshold(data) { return this.request('POST', '/thresholds', data); },
    updateThreshold(id, data) { return this.request('PUT', '/thresholds/' + id, data); },
    deleteThreshold(id) { return this.request('DELETE', '/thresholds/' + id); },

    getAlerts() { return this.request('GET', '/alerts'); },
    triggerCheck() { return this.request('POST', '/alerts/check'); }
};