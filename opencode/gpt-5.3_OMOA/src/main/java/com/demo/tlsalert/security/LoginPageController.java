package com.demo.tlsalert.security;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginPageController {

    @GetMapping(value = "/login", produces = MediaType.TEXT_HTML_VALUE)
    public String loginPage(
        @RequestParam(name = "error", required = false) String error,
        @RequestParam(name = "logout", required = false) String logout
    ) {
        String errorBanner = error != null
            ? "<p style='color:#b91c1c;background:#fee2e2;padding:8px;border-radius:6px;'>Invalid username or password</p>"
            : "";
        String logoutBanner = logout != null
            ? "<p style='color:#166534;background:#dcfce7;padding:8px;border-radius:6px;'>Logged out successfully</p>"
            : "";

        return """
            <!doctype html>
            <html lang='en'>
            <head>
              <meta charset='UTF-8'/>
              <meta name='viewport' content='width=device-width, initial-scale=1.0'/>
              <title>TLS Alert Demo - Login</title>
              <style>
                body { font-family: Arial, sans-serif; background:#f5f7fb; margin:0; padding:24px; }
                .wrap { max-width:900px; margin:0 auto; display:grid; grid-template-columns:1fr 1fr; gap:16px; }
                .card { background:#fff; border:1px solid #dbe3ef; border-radius:10px; padding:16px; }
                h1,h2 { margin-top:0; }
                label { display:block; margin:10px 0 6px; }
                input { width:100%; padding:10px; box-sizing:border-box; }
                button { margin-top:12px; padding:10px 14px; }
                table { width:100%; border-collapse: collapse; margin-top:8px; }
                th,td { border:1px solid #dbe3ef; text-align:left; padding:8px; font-size:14px; }
                .note { color:#475569; font-size:14px; }
              </style>
            </head>
            <body>
              <div class='wrap'>
                <section class='card'>
                  <h1>Login</h1>
                  __ERROR_BANNER__
                  __LOGOUT_BANNER__
                  <form method='post' action='/login'>
                    <label for='username'>Username</label>
                    <input id='username' name='username' type='text' required />
                    <label for='password'>Password</label>
                    <input id='password' name='password' type='password' required />
                    <button type='submit'>Sign in</button>
                  </form>
                  <p class='note'>After login, you can access the demo interface.</p>
                </section>
                <section class='card'>
                  <h2>Demo Users / Roles</h2>
                  <table>
                    <thead><tr><th>User</th><th>Password</th><th>Group</th><th>Authorities</th></tr></thead>
                    <tbody>
                      <tr><td>alice</td><td>alice123</td><td>GROUP_A</td><td>CERT_ADD, CERT_VIEW</td></tr>
                      <tr><td>bob</td><td>bob123</td><td>GROUP_A</td><td>CERT_VIEW</td></tr>
                      <tr><td>carol</td><td>carol123</td><td>GROUP_B</td><td>CERT_ADD, CERT_VIEW</td></tr>
                      <tr><td>dave</td><td>dave123</td><td>GROUP_B</td><td>CERT_VIEW</td></tr>
                    </tbody>
                  </table>
                </section>
              </div>
            </body>
            </html>
            """
            .replace("__ERROR_BANNER__", errorBanner)
            .replace("__LOGOUT_BANNER__", logoutBanner);
    }
}
