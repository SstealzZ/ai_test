const Auth = {
    TOKEN_KEY: 'certalert_token',
    USER_KEY: 'certalert_user',

    getToken() {
        return localStorage.getItem(this.TOKEN_KEY);
    },

    getUser() {
        const raw = localStorage.getItem(this.USER_KEY);
        return raw ? JSON.parse(raw) : null;
    },

    setAuth(token, user) {
        localStorage.setItem(this.TOKEN_KEY, token);
        localStorage.setItem(this.USER_KEY, JSON.stringify(user));
    },

    logout() {
        localStorage.removeItem(this.TOKEN_KEY);
        localStorage.removeItem(this.USER_KEY);
        App.showLogin();
    },

    isAuthenticated() {
        return !!this.getToken();
    },

    async devLogin(username) {
        try {
            const res = await fetch('/api/auth/dev-token', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username })
            });
            if (!res.ok) {
                const err = await res.json();
                App.toast('Login failed: ' + (err.detail || err.message || 'Unknown error'), 'error');
                return;
            }
            const data = await res.json();
            this.setAuth(data.token, { username: data.username, group: data.group, role: data.role });
            App.onAuthSuccess();
        } catch (e) {
            App.toast('Login failed: ' + e.message, 'error');
        }
    }
};