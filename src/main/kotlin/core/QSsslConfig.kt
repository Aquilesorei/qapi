package core


/**
 * Represents the configuration for a keystore used for SSL/TLS connections.
 *
 * To generate a self-signed keystore using `keytool`:
 * ```bash
 * keytool -genkeypair -alias myalias -keyalg RSA -keystore mykeystore.jks
 * ```
 * Replace `myalias` with a suitable alias and `mykeystore.jks` with your desired keystore filename.
 * You will be prompted to enter passwords for the keystore and the key.
 */
data class QSslConfig(
    /**
     * The file path to the Java KeyStore (JKS) file.
     */
    val keyStorePath: String? = null,

    /**
     * The password used to access the keystore.
     */
    val keyStorePassword: String? = null,

    /**
     * The password used to protect the private key within the keystore.
     */
    val keyPassword: String? = null,
    /**
     * The keystore type.
     */
    val  keyStoreType  : String? = "JKS"
)