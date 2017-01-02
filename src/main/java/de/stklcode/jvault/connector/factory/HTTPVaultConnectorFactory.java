/*
 * Copyright 2016-2017 Stefan Kalscheuer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.stklcode.jvault.connector.factory;

import de.stklcode.jvault.connector.HTTPVaultConnector;
import de.stklcode.jvault.connector.exception.TlsException;
import de.stklcode.jvault.connector.exception.VaultConnectorException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

/**
 * Vault Connector Factory implementation for HTTP Vault connectors.
 *
 * @author Stefan Kalscheuer
 * @since 0.1
 */
public class HTTPVaultConnectorFactory extends VaultConnectorFactory {
    public static final String DEFAULT_HOST = "127.0.0.1";
    public static final Integer DEFAULT_PORT = 8200;
    public static final boolean DEFAULT_TLS = true;
    public static final String DEFAULT_PREFIX = "/v1/";

    private String host;
    private Integer port;
    private boolean tls;
    private String prefix;
    private SSLContext sslContext;

    /**
     * Default empty constructor.
     * Initializes factory with default values.
     */
    public HTTPVaultConnectorFactory() {
        host = DEFAULT_HOST;
        port = DEFAULT_PORT;
        tls = DEFAULT_TLS;
        prefix = DEFAULT_PREFIX;
    }

    /**
     * Set hostname (default: 127.0.0.1)
     *
     * @param host Hostname or IP address
     * @return self
     */
    public HTTPVaultConnectorFactory withHost(String host) {
        this.host = host;
        return this;
    }

    /**
     * Set port (default: 8200)
     *
     * @param port Vault TCP port
     * @return self
     */
    public HTTPVaultConnectorFactory withPort(Integer port) {
        this.port = port;
        return this;
    }

    /**
     * Set TLS usage (default: TRUE)
     *
     * @param useTLS use TLS or not
     * @return self
     */
    public HTTPVaultConnectorFactory withTLS(boolean useTLS) {
        this.tls = useTLS;
        return this;
    }

    /**
     * Convenience Method for TLS usage (enabled by default)
     *
     * @return self
     */
    public HTTPVaultConnectorFactory withTLS() {
        return withTLS(true);
    }

    /**
     * Convenience Method for NOT using TLS
     *
     * @return self
     */
    public HTTPVaultConnectorFactory withoutTLS() {
        return withTLS(false);
    }

    /**
     * Set API prefix. Default is "/v1/" and changes should not be necessary for current state of development.
     *
     * @param prefix Vault API prefix (default: "/v1/"
     * @return self
     */
    public HTTPVaultConnectorFactory withPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    /**
     * Add a trusted CA certifiate for HTTPS connections.
     *
     * @param cert path to certificate file
     * @return self
     * @throws VaultConnectorException on error
     * @since 0.4.0
     */
    public HTTPVaultConnectorFactory withTrustedCA(Path cert) throws VaultConnectorException {
        if (cert != null)
            return withSslContext(createSslContext(cert));
        return this;
    }

    /**
     * Add a custom SSL context.
     * Overwrites certificates set by {@link #withTrustedCA}.
     *
     * @param sslContext the SSL context
     * @return self
     * @since 0.4.0
     */
    public HTTPVaultConnectorFactory withSslContext(SSLContext sslContext) {
        this.sslContext = sslContext;
        return this;
    }

    @Override
    public HTTPVaultConnector build() {
        return new HTTPVaultConnector(host, tls, port, prefix, sslContext);
    }

    /**
     * Create SSL Context trusting only provided certificate.
     *
     * @param trustedCert Path to trusted CA certificate
     * @return SSL context
     * @throws TlsException on errors
     * @since 0.4.0
     */
    private SSLContext createSslContext(Path trustedCert) throws TlsException {
        try {
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, createTrustManager(trustedCert), new SecureRandom());
            return context;
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new TlsException("Unable to intialize SSLContext", e);
        }
    }

    /**
     * Create a custom TrustManager for given CA certificate file.
     *
     * @param trustedCert Path to trusted CA certificate
     * @return TrustManger
     * @throws TlsException on error
     * @since 0.4.0
     */
    private TrustManager[] createTrustManager(Path trustedCert) throws TlsException {
        try {
            /* Create Keystore with trusted certificate */
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, null);
            keyStore.setCertificateEntry("trustedCert", certificateFromFile(trustedCert));
            /* Initialize TrustManager */
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(keyStore);
            return tmf.getTrustManagers();
        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
            throw new TlsException("Unable to initialize TrustManager", e);
        }
    }

    /**
     * Read given certificate file to X.509 certificate.
     *
     * @param certFile Path to certificate file
     * @return X.509 Certificate object
     * @throws TlsException on error
     * @since 0.4.0
     */
    private X509Certificate certificateFromFile(Path certFile) throws TlsException {
        try (InputStream is = Files.newInputStream(certFile)) {
            return (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(is);
        } catch (IOException | CertificateException e) {
            throw new TlsException("Unable to read certificate.", e);
        }
    }
}
