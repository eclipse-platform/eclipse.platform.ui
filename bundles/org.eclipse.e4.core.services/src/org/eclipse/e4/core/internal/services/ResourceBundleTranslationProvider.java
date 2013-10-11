/*******************************************************************************
 * Copyright (c) 2012 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@gmail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.core.internal.services;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Wrapper class for accessing translations out of a {@link ResourceBundle}.
 * 
 * @author Dirk Fauth
 *
 */
public class ResourceBundleTranslationProvider {

	/**
	 * The {@link ResourceBundle} to use for translations.
	 */
	private ResourceBundle resourceBundle;
	
	/**
	 * 
	 * @param resourceBundle The {@link ResourceBundle} to use for translations.
	 * 			Can be <code>null</code>, which will lead to simply return the key
	 * 			modified by prefixing and suffixing it with "!" when calling translate(String).
	 */
	public ResourceBundleTranslationProvider(ResourceBundle resourceBundle) {
		this.setResourceBundle(resourceBundle);
	}

	/**
	 * Tries to retrieve the translation value for the given key out of the {@link ResourceBundle}
	 * set to this {@link ResourceBundleTranslationProvider}. If there is no {@link ResourceBundle}
	 * set or there is no translation found for the given key, the key itself prefixed and suffixed
	 * with "!" will be returned to indicate that there is no translation found.
	 * <p>This implementation also supports the usage of dot separation for property keys. As in Java
	 * variables can not be separated with a dot, the underscore needs to be used for separation of
	 * the variable. This will be replaced automatically to a dot, if there is no translation found
	 * with an underscore as separator.
	 * </p>
	 * @param key The key of the requested translation property.
	 * @return The translation for the given key or the key itself prefixed and suffixed
	 * 			with "!" to indicate that there is no translation available for the
	 * 			given key.
	 */
	public String translate(String key) {
		String result = ""; //$NON-NLS-1$
		try {
			if (this.resourceBundle == null) {
				result = "!" + key + "!"; //$NON-NLS-1$ //$NON-NLS-2$
			}
			else {
				result = this.resourceBundle.getString(key);
			}
		} catch (MissingResourceException e) {
			if (key.contains("_")) { //$NON-NLS-1$
				result = translate(key.replace('_', '.'));
			} else {
				result = "!" + key + "!"; //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		return result;
	}

	/**
	 * @return The {@link ResourceBundle} that is used for translations.
	 */
	public ResourceBundle getResourceBundle() {
		return resourceBundle;
	}

	/**
	 * @param resourceBundle The {@link ResourceBundle} to use for translations.
	 */
	public void setResourceBundle(ResourceBundle resourceBundle) {
		this.resourceBundle = resourceBundle;
	}
}
