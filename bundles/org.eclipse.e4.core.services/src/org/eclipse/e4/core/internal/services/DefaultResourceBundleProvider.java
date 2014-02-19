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
package org.eclipse.e4.core.internal.services;

import java.util.ResourceBundle;
import org.eclipse.e4.core.services.translation.ResourceBundleProvider;
import org.eclipse.osgi.service.localization.BundleLocalization;
import org.osgi.framework.Bundle;

/**
 * Default implementation of {@link ResourceBundleProvider} that simply delegates to the
 * {@link BundleLocalization} for retrieving the {@link ResourceBundle}.
 */
public class DefaultResourceBundleProvider implements ResourceBundleProvider {

	private BundleLocalization localization;

	@Override
	public ResourceBundle getResourceBundle(Bundle bundle, String locale) {
		if (localization != null)
			return localization.getLocalization(bundle, locale);

		return null;
	}

	/**
	 * Method called by DS to set the {@link BundleLocalization} to this
	 * {@link ResourceBundleProvider}.
	 * 
	 * @param localization
	 *            The {@link BundleLocalization} that should be set to this
	 *            {@link ResourceBundleProvider}
	 */
	public void setBundleLocalization(BundleLocalization localization) {
		this.localization = localization;
	}

	/**
	 * Method called by DS to unset the {@link BundleLocalization} from this
	 * {@link ResourceBundleProvider}.
	 * 
	 * @param localization
	 *            The {@link BundleLocalization} to remove from this {@link ResourceBundleProvider}.
	 *            If the given {@link BundleLocalization} is not the same that is already set,
	 *            nothing will happen.
	 */
	public void unsetBundleLocalization(BundleLocalization localization) {
		if (this.localization == localization) {
			this.localization = null;
		}
	}
}
