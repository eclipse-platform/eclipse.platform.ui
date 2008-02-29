/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui;

import java.util.Map;

import org.eclipse.core.commands.util.Tracing;
import org.eclipse.ui.internal.misc.Policy;
import org.eclipse.ui.services.IServiceLocator;

/**
 * <p>
 * An implementation of <code>ISourceProvider</code> that provides listener
 * support. Subclasses need only call <code>fireSourceChanged</code> whenever
 * appropriate.
 * </p>
 * 
 * @since 3.1
 */
public abstract class AbstractSourceProvider implements ISourceProvider {

	/**
	 * Whether source providers should print out debugging information to the
	 * console when events arrive.
	 * 
	 * @since 3.2
	 */
	protected static boolean DEBUG = Policy.DEBUG_SOURCES;

	/**
	 * The listeners to this source provider. This value is never
	 * <code>null</code>. {@link #listenerCount} should be consulted to get
	 * the real length.
	 */
	private ISourceProviderListener[] listeners = new ISourceProviderListener[7];

	/**
	 * The number of listeners in the array.
	 */
	private int listenerCount = 0;

	public final void addSourceProviderListener(
			final ISourceProviderListener listener) {
		if (listener == null) {
			throw new NullPointerException("The listener cannot be null"); //$NON-NLS-1$
		}

		if (listenerCount == listeners.length) {
			final ISourceProviderListener[] growArray = new ISourceProviderListener[listeners.length + 4];
			System.arraycopy(listeners, 0, growArray, 0, listeners.length);
			listeners = growArray;
		}
		listeners[listenerCount++] = listener;
	}

	/**
	 * Notifies all listeners that a single source has changed.
	 * 
	 * @param sourcePriority
	 *            The source priority that has changed.
	 * @param sourceName
	 *            The name of the source that has changed; must not be
	 *            <code>null</code>.
	 * @param sourceValue
	 *            The new value for the source; may be <code>null</code>.
	 */
	protected final void fireSourceChanged(final int sourcePriority,
			final String sourceName, final Object sourceValue) {
		for (int i = 0; i < listenerCount; i++) {
			final ISourceProviderListener listener = listeners[i];
			listener.sourceChanged(sourcePriority, sourceName, sourceValue);
		}
	}

	/**
	 * Notifies all listeners that multiple sources have changed.
	 * 
	 * @param sourcePriority
	 *            The source priority that has changed.
	 * @param sourceValuesByName
	 *            The map of source names (<code>String</code>) to source
	 *            values (<code>Object</code>) that have changed; must not
	 *            be <code>null</code>. The names must not be
	 *            <code>null</code>, but the values may be <code>null</code>.
	 */
	protected final void fireSourceChanged(final int sourcePriority,
			final Map sourceValuesByName) {
		for (int i = 0; i < listenerCount; i++) {
			final ISourceProviderListener listener = listeners[i];
			listener.sourceChanged(sourcePriority, sourceValuesByName);
		}
	}

	/**
	 * Logs a debugging message in an appropriate manner. If the message is
	 * <code>null</code> or the <code>DEBUG</code> is <code>false</code>,
	 * then this method does nothing.
	 * 
	 * @param message
	 *            The debugging message to log; if <code>null</code>, then
	 *            nothing is logged.
	 * @since 3.2
	 */
	protected final void logDebuggingInfo(final String message) {
		if (DEBUG && (message != null)) {
			Tracing.printTrace("SOURCES", message); //$NON-NLS-1$
		}
	}

	public final void removeSourceProviderListener(
			final ISourceProviderListener listener) {
		if (listener == null) {
			throw new NullPointerException("The listener cannot be null"); //$NON-NLS-1$
		}

		int emptyIndex = -1;
		for (int i = 0; i < listenerCount; i++) {
			if (listeners[i] == listener) {
				listeners[i] = null;
				emptyIndex = i;
			}
		}

		if (emptyIndex != -1) {
			// Compact the array.
			for (int i = emptyIndex + 1; i < listenerCount; i++) {
				listeners[i - 1] = listeners[i];
			}
			listenerCount--;
		}
	}

	/**
	 * This method is called when the source provider is instantiated by
	 * <code>org.eclipse.ui.services</code>. Clients may override this method
	 * to perform initialization.
	 * 
	 * @param locator
	 *            The global service locator. It can be used to retrieve
	 *            services like the IContextService
	 * @since 3.4
	 */
	public void initialize(final IServiceLocator locator) {
	}
}
