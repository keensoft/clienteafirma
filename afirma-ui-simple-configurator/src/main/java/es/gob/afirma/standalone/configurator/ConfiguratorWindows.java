package es.gob.afirma.standalone.configurator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.logging.Logger;

import es.gob.afirma.core.misc.AOUtil;
import es.gob.afirma.standalone.configurator.CertUtil.CertPack;
/** Configura la instalaci&oacute;n en Windows para la correcta ejecuci&oacute;n de AutoFirma. */
final class ConfiguratorWindows implements Configurator {

	static final Logger LOGGER = Logger.getLogger("es.gob.afirma"); //$NON-NLS-1$

	private static final String KS_FILENAME = "autofirma.pfx"; //$NON-NLS-1$
	private static final String FILE_AUTOFIRMA_CERTIFICATE = "AutoFirma_ROOT.cer"; //$NON-NLS-1$
	private static final String KS_PASSWORD = "654321"; //$NON-NLS-1$

	/** Nombre del usuario por defecto en Windows. Este usuario es el que se usa como base para
	 * crear nuevos usuarios y no se deber&iacute;a tocar. */
	private static String DEFAULT_WINDOWS_USER_NAME = "Default"; //$NON-NLS-1$

	private static final String CHROME_CONFIG_FILE = "AppData/Local/Google/Chrome/User Data/Local State"; //$NON-NLS-1$

	@Override
	public void configure(final Console window) throws IOException, GeneralSecurityException {

		window.print(Messages.getString("ConfiguratorWindows.2")); //$NON-NLS-1$

		final File appDir = ConfiguratorUtil.getApplicationDirectory();

		window.print(Messages.getString("ConfiguratorWindows.3") + appDir.getAbsolutePath()); //$NON-NLS-1$

		if (!checkSSLKeyStoreGenerated(appDir)) {
			window.print(Messages.getString("ConfiguratorWindows.5")); //$NON-NLS-1$
			final CertPack certPack = CertUtil.getCertPackForLocalhostSsl(
				ConfiguratorUtil.CERT_ALIAS,
				KS_PASSWORD
			);

			window.print(Messages.getString("ConfiguratorWindows.11")); //$NON-NLS-1$

			//Generacion del certificado pfx
			ConfiguratorUtil.installFile(
				certPack.getPkcs12(),
				new File(appDir, KS_FILENAME)
			);

			//Generacion del certificado raiz .cer
			ConfiguratorUtil.installFile(
					certPack.getCaCertificate().getEncoded(),
					new File(appDir, FILE_AUTOFIRMA_CERTIFICATE));

			try {
				ConfiguratorFirefoxWindows.installCACertOnMozillaKeyStores(appDir, window);
			}
			catch(final MozillaProfileNotFoundException e) {
				window.print(Messages.getString("ConfiguratorWindows.12") + ": " + e); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		else {
			window.print(Messages.getString("ConfiguratorWindows.14")); //$NON-NLS-1$
		}

		// Insertamos el protocolo afirma en el fichero de configuracion de Google Chrome

		configureChrome(window, true);

		window.print(Messages.getString("ConfiguratorWindows.8")); //$NON-NLS-1$
	}

	/** Comprueba si ya existe un almac&eacute;n de certificados generado.
	 * @param appDir Directorio de la aplicaci&oacute;n.
	 * @return {@code true} si ya existe un almacen de certificados SSL, {@code false} en caso contrario. */
	private static boolean checkSSLKeyStoreGenerated(final File appDir) {
		return new File(appDir, KS_FILENAME).exists();
	}

	@Override
	public void uninstall() {

		LOGGER.info("Desinstalamos el certificado raiz del almacen de Firefox"); //$NON-NLS-1$
		ConfiguratorFirefoxWindows.uninstallRootCAMozillaKeyStore(ConfiguratorUtil.getApplicationDirectory());

		// Insertamos el protocolo afirma en el fichero de configuracion de Google Chrome
		configureChrome(null, false);

		// No es necesario eliminar nada mas porque el proceso de desinstalacion de Windows
		// eliminara el directorio de aplicacion con todo su contenido
	}

	/**
	 * Configura el protocolo "afirma" en Chrome para todos los usuarios de Windows.
	 * @param window Consola de salida.
	 * @param installing Indica si se debe configurar ({@code true}) o desconfigurar
	 * ({@code false}) el protocolo "afirma" en Chrome.
	 */
	private static void configureChrome(final Console window, boolean installing) {

		if (window != null && installing) {
			window.print(Messages.getString("ConfiguratorWindows.16")); //$NON-NLS-1$
		}

		final File usersDir = new File(System.getProperty("user.home")).getParentFile(); //$NON-NLS-1$
		for (final File userDir : usersDir.listFiles()) {
			if (userDir.isDirectory() && !DEFAULT_WINDOWS_USER_NAME.equalsIgnoreCase(userDir.getName())) {
				try {
					final File chromeConfigFile = new File(userDir, CHROME_CONFIG_FILE);
					if (chromeConfigFile.isFile() && chromeConfigFile.canWrite()) {
						String config;
						try (final FileInputStream fis = new FileInputStream(chromeConfigFile)) {
							config = new String(AOUtil.getDataFromInputStream(fis), StandardCharsets.UTF_8);
						}

						config = config.replace("\"afirma\":false,", ""); //$NON-NLS-1$ //$NON-NLS-2$
						if (installing) {
							config = config.replace("\"protocol_handler\":{\"excluded_schemes\":{", //$NON-NLS-1$
									"\"protocol_handler\":{\"excluded_schemes\":{\"afirma\":false,"); //$NON-NLS-1$
						}
						try (final FileOutputStream fos = new FileOutputStream(chromeConfigFile)) {
							fos.write(config.getBytes(StandardCharsets.UTF_8));
						}
					}

				}
				catch (final Exception e) {
					if (window != null) {
						window.print(String.format(Messages.getString("ConfiguratorWindows.15"), userDir.getName())); //$NON-NLS-1$
					}
					LOGGER.warning("No se pudo configurar Chrome para el usuario " + userDir + ": " + e); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
		}
	}
}
