package com.demo.tlsalert.service;

import com.demo.tlsalert.api.dto.CertificateResponse;
import com.demo.tlsalert.common.BadRequestException;
import com.demo.tlsalert.domain.SourceType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.Locale;

@Service
public class CertificateIngestionService {

    private final CertificateParserService certificateParserService;
    private final CertificateService certificateService;

    public CertificateIngestionService(CertificateParserService certificateParserService, CertificateService certificateService) {
        this.certificateParserService = certificateParserService;
        this.certificateService = certificateService;
    }

    public CertificateResponse addFromFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Certificate file is required");
        }

        String filename = file.getOriginalFilename() == null ? "uploaded" : file.getOriginalFilename();
        String lower = filename.toLowerCase(Locale.ROOT);
        if (!(lower.endsWith(".cer") || lower.endsWith(".crt") || lower.endsWith(".pem"))) {
            throw new BadRequestException("Only .cer, .crt or .pem files are accepted");
        }

        try {
            X509Certificate certificate = certificateParserService.parseFirstX509(file.getBytes());
            return certificateService.createFromX509(certificate, SourceType.FILE, filename);
        } catch (BadRequestException ex) {
            throw ex;
        } catch (IOException ex) {
            throw new BadRequestException("Unable to process uploaded certificate: " + ex.getMessage());
        }
    }

    public CertificateResponse addFromUrl(String inputUrl) {
        URI uri = normalizeUrl(inputUrl);
        String host = uri.getHost();
        int port = uri.getPort() > 0 ? uri.getPort() : 443;
        if (host == null || host.isBlank()) {
            throw new BadRequestException("Invalid URL host");
        }
        ensurePublicHost(host);

        try {
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, new TrustManager[]{new PermissiveTrustManager()}, null);

            SSLSocketFactory factory = context.getSocketFactory();
            try (Socket plainSocket = new Socket()) {
                plainSocket.connect(new InetSocketAddress(host, port), 5_000);
                try (SSLSocket socket = (SSLSocket) factory.createSocket(plainSocket, host, port, true)) {
                socket.setSoTimeout(10_000);
                socket.startHandshake();
                java.security.cert.Certificate[] peerCertificates = socket.getSession().getPeerCertificates();
                if (peerCertificates.length == 0 || !(peerCertificates[0] instanceof X509Certificate certificate)) {
                    throw new BadRequestException("No X.509 certificate found from remote host");
                }
                return certificateService.createFromX509(certificate, SourceType.URL, uri.toString());
                }
            }
        } catch (GeneralSecurityException | java.io.IOException ex) {
            throw new BadRequestException("Unable to fetch certificate from URL: " + ex.getMessage());
        }
    }

    private URI normalizeUrl(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new BadRequestException("URL is required");
        }
        String value = raw.trim();
        if (!value.startsWith("https://") && !value.startsWith("http://")) {
            value = "https://" + value;
        }
        try {
            URI uri = new URI(value);
            if (uri.getScheme() == null || uri.getHost() == null) {
                throw new BadRequestException("Malformed URL");
            }
            if (!"https".equalsIgnoreCase(uri.getScheme())) {
                throw new BadRequestException("Only https URLs are allowed");
            }
            return uri;
        } catch (URISyntaxException ex) {
            throw new BadRequestException("Malformed URL");
        }
    }

    private void ensurePublicHost(String host) {
        try {
            InetAddress[] resolved = InetAddress.getAllByName(host);
            if (resolved.length == 0) {
                throw new BadRequestException("Unable to resolve host");
            }
            for (InetAddress address : resolved) {
                if (address.isAnyLocalAddress()
                    || address.isLoopbackAddress()
                    || address.isLinkLocalAddress()
                    || address.isSiteLocalAddress()
                    || address.isMulticastAddress()) {
                    throw new BadRequestException("Private or local network addresses are not allowed");
                }
            }
        } catch (UnknownHostException ex) {
            throw new BadRequestException("Unable to resolve host");
        }
    }

    private static class PermissiveTrustManager implements X509TrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }
}
