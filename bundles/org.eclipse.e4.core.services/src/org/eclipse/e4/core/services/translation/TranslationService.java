/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.core.services.translation;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Provides localization service.
 */
abstract public class TranslationService {

	/**
	 * The name of the context variable with locale information
	 */
	static public final String LOCALE = "org.eclipse.e4.core.locale"; //$NON-NLS-1$

	@Inject
	@Named(LOCALE)
	protected String locale;

	@Inject
	public TranslationService() {
		// placeholder
	}

	/**
	 * Translates the key from the contributor. If translation can not be found, the original key
	 * should be returned.
	 * <p>
	 * This method is expected to be overridden by the implementors.
	 * </p>
	 * 
	 * @param key
	 *            the key
	 * @param contributorURI
	 *            URI of the contributor
	 * @return localized value, or the original key if the translation can not be done
	 */
	public String translate(String key, String contributorURI) {
		return key;
	}
}
