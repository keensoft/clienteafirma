/*
 * Este fichero forma parte del Cliente @firma.
 * El Cliente @firma es un aplicativo de libre distribucion cuyo codigo fuente puede ser consultado
 * y descargado desde www.ctt.map.es.
 * Copyright 2009,2010,2011 Gobierno de Espana
 * Este fichero se distribuye bajo  bajo licencia GPL version 2  segun las
 * condiciones que figuran en el fichero 'licence' que se acompana. Si se distribuyera este
 * fichero individualmente, deben incluirse aqui las condiciones expresadas alli.
 */

package es.gob.afirma.applet;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import es.gob.afirma.core.misc.AOUtil;

/** Clase para la carga del acuerdo de licencia. */
final class LicenseManager {

    private static String LICENSE_FILE = "/resources/licenses"; //$NON-NLS-1$

    /** recupera el texto de la licencia del Cliente @firma. Si ocurre un error
     * lanza una excepci&oacute;n.
     * @return Texto de la licencia.
     * @throws IOException
     *         Cuando no se ha podido recuperar el texto de la licencia. */
    static String getLicenceText() throws IOException {

        InputStream is = Class.class.getResourceAsStream(LICENSE_FILE + "_" + Locale.getDefault()); //$NON-NLS-1$

        if (is == null) {
            is = Class.class.getResourceAsStream(LICENSE_FILE + "_" + Locale.getDefault().getLanguage()); //$NON-NLS-1$
        }

        if (is == null) {
            is = Class.class.getClass().getResourceAsStream(LICENSE_FILE);
        }

        try {
            return new String(AOUtil.getDataFromInputStream(is), "UTF-8"); //$NON-NLS-1$
        }
        catch (final Exception e) {
            throw new IOException("No se ha podido recuperar el texto de la licencia:" + e); //$NON-NLS-1$
        }
        finally {
            try {
                is.close();
            }
            catch (final Exception e) {
                // Ignoramos los errores en el cierre
            }
        }
    }
}
