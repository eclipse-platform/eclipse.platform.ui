/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.internal.workbench;

import org.eclipse.e4.core.services.translation.MessageFactory;

import java.util.Locale;
import javax.inject.Inject;
import org.eclipse.e4.core.services.translation.ITranslationService;
import org.eclipse.e4.ui.services.ETranslationService;

/**
 *
 */
public class TranslationServiceImpl implements ETranslationService {
	@Inject
	private ITranslationService service;

	private String locale = Locale.getDefault().toString();

	public <M> M createInstance(Class<M> messages) throws InstantiationException,
			IllegalAccessException {
		return createInstance(locale, messages);
	}

	public <M> M createInstance(String locale, Class<M> messages) throws InstantiationException,
			IllegalAccessException {
		return MessageFactory.createInstance(locale, messages);
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	public String getLocale() {
		return locale;
	}

	public String translate(String providerId, String key) {
		return service.translate(key, providerId, key);
	}

}
