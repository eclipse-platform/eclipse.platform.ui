/*******************************************************************************
 * Copyright (c) 2013 Tom Schindl <tom.schindl@bestsolution.at> and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.core.services.nls;

import java.util.Locale;
import java.util.ResourceBundle;
import org.eclipse.osgi.service.localization.BundleLocalization;

/**
 * Service that is responsible for creating and managing message class instances.
 */
public interface IMessageFactoryService {

	/**
	 * Returns an instance of the of a given messages class for the given {@link Locale}. If
	 * configured it caches the created instances and return the already created instances.
	 * Otherwise a new instance will be created.
	 * 
	 * @param locale
	 *            The {@link Locale} for which the message class instance is requested.
	 * @param messages
	 *            The type of the message class whose instance is requested.
	 * @param localization
	 *            The service that is needed to retrieve {@link ResourceBundle} objects from a
	 *            bundle with a given locale.
	 * @return An instance of the given messages class and {@link Locale}.
	 */
	public <M> M getMessageInstance(final Locale locale, final Class<M> messages,
			BundleLocalization localization);
}
