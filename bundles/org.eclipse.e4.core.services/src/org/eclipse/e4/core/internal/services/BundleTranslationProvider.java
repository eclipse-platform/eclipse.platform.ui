/*******************************************************************************
 * Copyright (c) 2011, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Dirk Fauth <dirk.fauth@googlemail.com> - Bug 428427
 ******************************************************************************/
package org.eclipse.e4.core.internal.services;

import java.util.ResourceBundle;
import javax.inject.Inject;
import org.eclipse.e4.core.services.translation.ResourceBundleProvider;
import org.eclipse.e4.core.services.translation.TranslationService;
import org.osgi.service.log.LogService;

public class BundleTranslationProvider extends TranslationService {

	@Inject
	ResourceBundleProvider provider;

	@Override
	public String translate(String key, String contributorURI) {
		if (provider == null)
			return key;

		try {
			ResourceBundle resourceBundle = ResourceBundleHelper.getResourceBundleForUri(
					contributorURI, ResourceBundleHelper.toLocale(locale), provider);
			return getResourceString(key, resourceBundle);
		} catch (Exception e) {
			// an error occurred on trying to retrieve the translation for the given key
			// for improved fault tolerance we will log the error and return the key
			LogService logService = ServicesActivator.getDefault().getLogService();
			if (logService != null)
				logService.log(LogService.LOG_ERROR,
						"Error retrieving the translation for key=" + key //$NON-NLS-1$
								+ " and contributorURI=" + contributorURI, e); //$NON-NLS-1$

			return key;
		}
	}

}
