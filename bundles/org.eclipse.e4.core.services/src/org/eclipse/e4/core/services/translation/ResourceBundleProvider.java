/*******************************************************************************
 * Copyright (c) 2014 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@gmail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.core.services.translation;

import java.util.ResourceBundle;
import org.eclipse.osgi.service.localization.BundleLocalization;
import org.osgi.framework.Bundle;

/**
 * The interface of the service that gets {@link ResourceBundle} objects from a given bundle with a
 * given locale.
 * <p>
 * This service abstracts out the reference to the {@link BundleLocalization} and enables the
 * possibility to load {@link ResourceBundle}s from different locations than the default OSGi
 * context.
 * </p>
 *
 * @since 1.2
 */
public interface ResourceBundleProvider {

	/**
	 * Returns a <code>ResourceBundle</code> object for the given bundle and locale.
	 *
	 * @param bundle
	 *            the bundle to get localization for
	 * @param locale
	 *            the name of the locale to get, or <code>null</code> if the default locale is to be
	 *            used
	 *
	 * @return A <code>ResourceBundle</code> object for the given bundle and locale, or
	 *         <code>null</code> is returned if no ResourceBundle object can be loaded.
	 */
	ResourceBundle getResourceBundle(Bundle bundle, String locale);
}
