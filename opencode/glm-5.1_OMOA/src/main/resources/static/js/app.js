const App = {
    currentPage: 'login',

    init() {
        document.getElementById('login-btn').addEventListener('click', () => this.handleLogin());
        document.getElementById('logout-btn').addEventListener('click', () => Auth.logout());
        document.getElementById('upload-btn').addEventListener('click', () => this.handleUpload());
        document.getElementById('fetch-btn').addEventListener('click', () => this.handleFetch());
        document.getElementById('add-threshold-btn').addEventListener('click', () => this.handleAddThreshold());
        document.getElementById('trigger-check-btn').addEventListener('click', () => this.handleTriggerCheck());

        document.getElementById('cert-search').addEventListener('input', () => this.filterCertificates());
        document.getElementById('cert-status-filter').addEventListener('change', () => this.filterCertificates());

        document.querySelectorAll('.nav-link').forEach(link => {
            link.addEventListener('click', (e) => {
                e.preventDefault();
                this.navigate(e.target.dataset.page);
            });
        });

        if (Auth.isAuthenticated()) {
            this.onAuthSuccess();
        } else {
            this.showLogin();
        }
    },

    showLogin() {
        this.showPage('login');
        document.getElementById('navbar').style.display = 'none';
    },

    onAuthSuccess() {
        document.getElementById('navbar').style.display = 'flex';
        const user = Auth.getUser();
        document.getElementById('nav-user').style.display = 'flex';
        document.getElementById('user-name').textContent = user.username + ' (' + user.group + ')';
        this.navigate('certificates');
        this.loadCertificates();
    },

    handleLogin() {
        const token = document.getElementById('login-token').value.trim();
        if (!token) {
            this.showError('login-error', 'Please enter a JWT token');
            return;
        }

        try {
            const parts = token.split('.');
            const payload = JSON.parse(atob(parts[1]));
            Auth.setAuth(token, {
                username: payload.preferred_username || payload.sub,
                group: payload.group || 'default',
                role: payload.role || 'CERT_VIEWER'
            });
            this.onAuthSuccess();
        } catch (e) {
            this.showError('login-error', 'Invalid JWT token format');
        }
    },

    async navigate(page) {
        this.currentPage = page;
        this.showPage(page);

        document.querySelectorAll('.nav-link').forEach(link => {
            link.classList.toggle('active', link.dataset.page === page);
        });

        if (page === 'certificates') await this.loadCertificates();
        if (page === 'thresholds') await this.loadThresholds();
        if (page === 'alerts') await this.loadAlerts();
    },

    showPage(page) {
        document.querySelectorAll('.page').forEach(p => p.style.display = 'none');
        const el = document.getElementById('page-' + page);
        if (el) el.style.display = 'block';
    },

    switchTab(tab) {
        document.querySelectorAll('.tab-btn').forEach(b => b.classList.toggle('active', b.dataset.tab === tab));
        document.querySelectorAll('.tab-content').forEach(c => c.classList.remove('active'));
        const content = document.getElementById('tab-' + tab);
        if (content) {
            content.classList.add('active');
            content.style.display = 'block';
        }
        document.querySelectorAll('.tab-content:not(.active)').forEach(c => c.style.display = 'none');
    },

    async loadCertificates() {
        const container = document.getElementById('certificates-list');
        container.innerHTML = '<div class="loading">Loading certificates...</div>';
        try {
            const certs = await API.getCertificates();
            this._certificates = certs;
            this.renderCertificates(certs);
        } catch (e) {
            container.innerHTML = '<div class="empty-state">Failed to load certificates: ' + e.message + '</div>';
        }
    },

    renderCertificates(certs) {
        const container = document.getElementById('certificates-list');
        if (!certs.length) {
            container.innerHTML = '<p class="empty-state">No certificates found. Add one to get started.</p>';
            return;
        }
        container.innerHTML = certs.map(c => this.certCard(c)).join('');
    },

    filterCertificates() {
        if (!this._certificates) return;
        const search = document.getElementById('cert-search').value.toLowerCase();
        const status = document.getElementById('cert-status-filter').value;
        let filtered = this._certificates;
        if (search) filtered = filtered.filter(c =>
            c.subject.toLowerCase().includes(search) || c.issuer.toLowerCase().includes(search) || c.serialNumber.toLowerCase().includes(search)
        );
        if (status) filtered = filtered.filter(c => c.status === status);
        this.renderCertificates(filtered);
    },

    certCard(c) {
        const daysColor = c.status === 'EXPIRED' ? 'var(--danger)' :
                          c.status === 'CRITICAL' ? 'var(--danger)' :
                          c.status === 'WARNING' ? 'var(--warning)' : 'var(--success)';
        return `<div class="cert-card">
            <div><span class="status-badge status-${c.status}">${c.status}</span></div>
            <div>
                <div class="cert-subject">${this.escapeHtml(c.subject)}</div>
                <div class="cert-meta">
                    <span>Issuer: ${this.escapeHtml(c.issuer)}</span>
                    <span>Group: ${c.groupName}</span>
                    <span>Added by: ${c.addedBy}</span>
                    ${c.sourceUrl ? '<span>Source: ' + this.escapeHtml(c.sourceUrl) + '</span>' : ''}
                    ${c.originalFilename ? '<span>File: ' + this.escapeHtml(c.originalFilename) + '</span>' : ''}
                </div>
                <div class="cert-meta">
                    <span>Valid: ${this.formatDate(c.notBefore)} - ${this.formatDate(c.notAfter)}</span>
                    <span>Serial: ${c.serialNumber}</span>
                </div>
            </div>
            <div style="text-align:right">
                <div class="cert-days" style="color:${daysColor}">${c.daysUntilExpiry}<small>days left</small></div>
                <button type="button" class="btn btn-sm btn-danger" onclick="App.deleteCert(${c.id})">Delete</button>
            </div>
        </div>`;
    },

    async deleteCert(id) {
        if (!confirm('Delete this certificate?')) return;
        try {
            await API.deleteCertificate(id);
            this.toast('Certificate deleted', 'success');
            this.loadCertificates();
        } catch (e) {
            this.toast('Delete failed: ' + e.message, 'error');
        }
    },

    async handleUpload() {
        const fileInput = document.getElementById('cert-file');
        if (!fileInput.files.length) {
            this.toast('Please select a certificate file', 'error');
            return;
        }
        const btn = document.getElementById('upload-btn');
        btn.disabled = true;
        btn.textContent = 'Uploading...';
        try {
            const result = await API.uploadCertificate(fileInput.files[0]);
            document.getElementById('add-result').style.display = 'block';
            document.getElementById('add-result').innerHTML =
                '<h4>Certificate Added Successfully</h4>' + this.certCard(result);
            this.toast('Certificate uploaded', 'success');
        } catch (e) {
            this.toast('Upload failed: ' + e.message, 'error');
        } finally {
            btn.disabled = false;
            btn.textContent = 'Upload Certificate';
        }
    },

    async handleFetch() {
        const url = document.getElementById('cert-url').value.trim();
        if (!url) {
            this.toast('Please enter a URL', 'error');
            return;
        }
        const btn = document.getElementById('fetch-btn');
        btn.disabled = true;
        btn.textContent = 'Fetching...';
        try {
            const result = await API.fetchCertificate(url);
            document.getElementById('add-result').style.display = 'block';
            document.getElementById('add-result').innerHTML =
                '<h4>Certificate Fetched Successfully</h4>' + this.certCard(result);
            this.toast('Certificate fetched', 'success');
        } catch (e) {
            this.toast('Fetch failed: ' + e.message, 'error');
        } finally {
            btn.disabled = false;
            btn.textContent = 'Fetch Certificate';
        }
    },

    async loadThresholds() {
        const container = document.getElementById('thresholds-list');
        container.innerHTML = '<div class="loading">Loading thresholds...</div>';
        try {
            const thresholds = await API.getThresholds();
            this.renderThresholds(thresholds);
        } catch (e) {
            container.innerHTML = '<div class="empty-state">Failed to load thresholds</div>';
        }
    },

    renderThresholds(thresholds) {
        const container = document.getElementById('thresholds-list');
        if (!thresholds.length) {
            container.innerHTML = '<p class="empty-state">No thresholds configured.</p>';
            return;
        }
        container.innerHTML = thresholds.map(t => `
            <div class="threshold-card">
                <div class="threshold-info">
                    <span class="threshold-days">${t.days}d</span>
                    <span class="threshold-name">${this.escapeHtml(t.name)}</span>
                    <span class="status-badge ${t.active ? 'status-VALID' : 'status-EXPIRED'}">${t.active ? 'Active' : 'Inactive'}</span>
                </div>
                <button type="button" class="btn btn-sm btn-danger" onclick="App.deleteThreshold(${t.id})">Delete</button>
            </div>
        `).join('');
    },

    async handleAddThreshold() {
        const name = document.getElementById('threshold-name').value.trim();
        const days = parseInt(document.getElementById('threshold-days').value);
        const active = document.getElementById('threshold-active').value === 'true';
        if (!name) { this.toast('Name is required', 'error'); return; }
        if (!days || days < 1) { this.toast('Days must be at least 1', 'error'); return; }

        try {
            await API.createThreshold({ name, days, active });
            this.toast('Threshold created', 'success');
            this.loadThresholds();
        } catch (e) {
            this.toast('Failed: ' + e.message, 'error');
        }
    },

    async deleteThreshold(id) {
        if (!confirm('Delete this threshold?')) return;
        try {
            await API.deleteThreshold(id);
            this.toast('Threshold deleted', 'success');
            this.loadThresholds();
        } catch (e) {
            this.toast('Failed: ' + e.message, 'error');
        }
    },

    async loadAlerts() {
        const container = document.getElementById('alerts-list');
        container.innerHTML = '<div class="loading">Loading alerts...</div>';
        try {
            const alerts = await API.getAlerts();
            this.renderAlerts(alerts);
        } catch (e) {
            container.innerHTML = '<div class="empty-state">Failed to load alerts</div>';
        }
    },

    renderAlerts(alerts) {
        const container = document.getElementById('alerts-list');
        if (!alerts.length) {
            container.innerHTML = '<p class="empty-state">No alerts yet.</p>';
            return;
        }
        container.innerHTML = alerts.map(a => `
            <div class="alert-card">
                <div class="alert-type ${a.type}">${a.type.replace('_', ' ')}</div>
                <div><strong>${this.escapeHtml(a.certificate.subject)}</strong></div>
                <div class="cert-meta">
                    <span>${a.daysRemaining} days remaining</span>
                    <span>Threshold: ${a.thresholdDays}d</span>
                    <span>${this.formatDate(a.notifiedAt)}</span>
                </div>
                <div style="margin-top:0.5rem;color:var(--text-muted);font-size:0.85rem">${this.escapeHtml(a.message)}</div>
            </div>
        `).join('');
    },

    async handleTriggerCheck() {
        const btn = document.getElementById('trigger-check-btn');
        btn.disabled = true;
        btn.textContent = 'Checking...';
        try {
            await API.triggerCheck();
            this.toast('Expiration check triggered', 'success');
            this.loadAlerts();
        } catch (e) {
            this.toast('Failed: ' + e.message, 'error');
        } finally {
            btn.disabled = false;
            btn.textContent = 'Trigger Check Now';
        }
    },

    showError(id, msg) {
        const el = document.getElementById(id);
        el.textContent = msg;
        el.style.display = 'block';
    },

    toast(msg, type) {
        const container = document.getElementById('toast-container');
        const toast = document.createElement('div');
        toast.className = 'toast toast-' + type;
        toast.textContent = msg;
        container.appendChild(toast);
        setTimeout(() => toast.remove(), 4000);
    },

    escapeHtml(str) {
        if (!str) return '';
        const div = document.createElement('div');
        div.textContent = str;
        return div.innerHTML;
    },

    formatDate(dateStr) {
        if (!dateStr) return 'N/A';
        return new Date(dateStr).toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' });
    }
};

document.addEventListener('DOMContentLoaded', () => App.init());