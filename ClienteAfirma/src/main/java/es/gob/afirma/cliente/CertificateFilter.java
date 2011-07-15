/*
 * Este fichero forma parte del Cliente @firma.
 * El Cliente @firma es un aplicativo de libre distribucion cuyo codigo fuente puede ser consultado
 * y descargado desde www.ctt.map.es.
 * Copyright 2009,2010,2011 Gobierno de Espana
 * Este fichero se distribuye bajo  bajo licencia GPL version 2  segun las
 * condiciones que figuran en el fichero 'licence' que se acompana. Si se distribuyera este
 * fichero individualmente, deben incluirse aqui las condiciones expresadas alli.
 */
package es.gob.afirma.cliente;

import java.security.cert.X509Certificate;

/** Filtro para certificados. Debe autocontener toda la l&oacute;gica que indique si un
 * certificado cumple o no las condiciones del filtro.
 * El establecimiento de los datos encesarios para las condiciones de filtrado queda fuera
 * del interfaz y debe ser espec&iacute;fico para cada implementaci&oacute;n.
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s */
public interface CertificateFilter {
    boolean matches(final X509Certificate cert);
}
