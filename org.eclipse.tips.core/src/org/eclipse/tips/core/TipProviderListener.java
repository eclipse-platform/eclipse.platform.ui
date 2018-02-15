/*******************************************************************************
 * Copyright (c) 2018 Remain Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     wim.jongman@remainsoftware.com - initial API and implementation
 *******************************************************************************/
package org.eclipse.tips.core;

/**
 * Provides notifications on TipProvider events.
 *
 */
@FunctionalInterface
public interface TipProviderListener {

	/**
	 * Event ready. The TipProvider is ready to serve tips.
	 */
	public static final int EVENT_READY = 1;

	/**
	 * When the subject is ready to serve tips (e.g. fetched new tips from
	 * somewhere) it will The provider is ready to serve tips. The default
	 * implementation does nothing, subclasses may override.
	 *
	 * @param provider
	 *            the provider.
	 */
	public void providerReady(TipProvider provider);
}