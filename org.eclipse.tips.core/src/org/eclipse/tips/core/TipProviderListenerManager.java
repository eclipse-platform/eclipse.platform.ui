/****************************************************************************
 * Copyright (c) 2017, 2018 Remain Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Wim Jongman <wim.jongman@remainsoftware.com> - initial API and implementation
 *****************************************************************************/
package org.eclipse.tips.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to manage provider listeners.
 */
public class TipProviderListenerManager {

	protected List<TipProviderListener> fListeners = new ArrayList<>();

	/**
	 * Notifies all listeners that an event occurred.
	 *
	 * @param event
	 *            the event
	 * @param provider
	 *            the provider
	 */
	public void notifyListeners(int event, TipProvider provider) {
		synchronized (fListeners) {
			for (TipProviderListener tipProviderListener : fListeners) {
				switch (event) {
				case TipProviderListener.EVENT_READY:
					tipProviderListener.providerReady(provider);
					break;
				}
			}
		}
	}

	/**
	 * Adds a listener to the list of listeners.
	 *
	 * @param pProviderListener
	 *            the listener to be notified
	 * @return this
	 */
	public TipProviderListenerManager addProviderListener(TipProviderListener pProviderListener) {
		fListeners.remove(pProviderListener);
		fListeners.add(pProviderListener);
		return this;
	}

	/**
	 * Removes a listener from the list of listeners.
	 *
	 * @param pProviderListener
	 *            the listener to remove
	 * @return this
	 */
	public TipProviderListenerManager removeProviderListener(TipProviderListener pProviderListener) {
		fListeners.remove(pProviderListener);
		return this;
	}
}