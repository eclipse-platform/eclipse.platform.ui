/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.commands;

import org.eclipse.core.commands.IHandlerState;
import org.eclipse.core.commands.IHandlerStateListener;
import org.eclipse.core.commands.util.ListenerList;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.commands.IPersistableHandlerState;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * <p>
 * A proxy for handler state that has been defined in XML. This delays the class
 * loading until the state is really asked for information. Asking a proxy for
 * anything (except disposing, and adding and removing listeners) will cause the
 * proxy to instantiate the proxied handler.
 * </p>
 * <p>
 * Loading the proxied state will automatically cause it to load its value from
 * the preference store. Disposing of the state will cause it to persist its
 * value.
 * </p>
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.2
 */
public final class HandlerStateProxy implements IPersistableHandlerState {

	/**
	 * The configuration element from which the state can be created. This value
	 * will exist until the element is converted into a real class -- at which
	 * point this value will be set to <code>null</code>.
	 */
	private IConfigurationElement configurationElement;

	/**
	 * The listeners to this proxy. This list is non-<code>null</code> iff
	 * the real state has not been loaded and there are listeners attached.
	 */
	private ListenerList listeners = null;

	/**
	 * The key in the preference store to locate the persisted state.
	 */
	private String preferenceKey;

	/**
	 * The preference store containing the persisted state, if any.
	 */
	private IPreferenceStore preferenceStore;

	/**
	 * The real state. This value is <code>null</code> until the proxy is
	 * forced to load the real state. At this point, the configuration element
	 * is converted, nulled out, and this state gains a reference.
	 */
	private IHandlerState state = null;

	/**
	 * The name of the configuration element attribute which contains the
	 * information necessary to instantiate the real state.
	 */
	private final String stateAttributeName;

	/**
	 * Constructs a new instance of <code>HandlerState</code> with all the
	 * information it needs to create the real state later.
	 * 
	 * @param configurationElement
	 *            The configuration element from which the real class can be
	 *            loaded at run-time; must not be <code>null</code>.
	 * @param stateAttributeName
	 *            The name of the attribute or element containing the state
	 *            executable extension; must not be <code>null</code>.
	 * @param preferenceStore
	 *            The preference store to which any persistent data should be
	 *            written, and from which it should be loaded; may be
	 *            <code>null</code>.
	 * @param preferenceKey
	 *            The key at which the persistent data is located within the
	 *            preference store.
	 */
	public HandlerStateProxy(final IConfigurationElement configurationElement,
			final String stateAttributeName,
			final IPreferenceStore preferenceStore, final String preferenceKey) {

		if (configurationElement == null) {
			throw new NullPointerException(
					"The configuration element backing a state proxy cannot be null"); //$NON-NLS-1$
		}

		if (stateAttributeName == null) {
			throw new NullPointerException(
					"The attribute containing the state class must be known"); //$NON-NLS-1$
		}

		this.configurationElement = configurationElement;
		this.stateAttributeName = stateAttributeName;
		this.preferenceKey = preferenceKey;
		this.preferenceStore = preferenceStore;
	}

	public final void addListener(final IHandlerStateListener listener) {
		if (state == null) {
			if (listeners == null) {
				listeners = new ListenerList(1);
			}
			listeners.add(listener);
		} else {
			state.addListener(listener);
		}
	}

	public final void dispose() {
		if (state != null) {
			state.dispose();
			if (state instanceof IPersistableHandlerState) {
				final IPersistableHandlerState persistableState = (IPersistableHandlerState) state;
				if (persistableState.isPersisted() && preferenceStore != null
						&& preferenceKey != null) {
					persistableState.save(preferenceStore, preferenceKey);
				}
			}
		}
	}

	public final Object getValue() {
		if (loadState()) {
			return state.getValue();
		}

		return null;
	}

	public final boolean isPersisted() {
		if (loadState() && state instanceof IPersistableHandlerState) {
			return ((IPersistableHandlerState) state).isPersisted();
		}

		return false;
	}

	public final void load(final IPreferenceStore store,
			final String preferenceKey) {
		if (loadState() && state instanceof IPersistableHandlerState) {
			((IPersistableHandlerState) state).load(store, preferenceKey);
		}
	}

	/**
	 * Loads the state, if possible. If the state is loaded, then the member
	 * variables are updated accordingly and the state is told to load its value
	 * from the preference store.
	 * 
	 * @return <code>true</code> if the state is now non-null;
	 *         <code>false</code> otherwise.
	 */
	private final boolean loadState() {
		if (state == null) {
			try {
				state = (IHandlerState) configurationElement
						.createExecutableExtension(stateAttributeName);
				configurationElement = null;

				// Try to load the persistent state, if possible.
				if (state instanceof IPersistableHandlerState) {
					final IPersistableHandlerState persistableState = (IPersistableHandlerState) state;
					if (persistableState.isPersisted()
							&& preferenceStore != null && preferenceKey != null) {
						persistableState.load(preferenceStore, preferenceKey);
					}
				}

				// Transfer the local listeners to the real state.
				if (listeners != null) {
					final Object[] listenerArray = listeners.getListeners();
					for (int i = 0; i < listenerArray.length; i++) {
						state
								.addListener((IHandlerStateListener) listenerArray[i]);
					}
					listeners = null;
				}

				return true;

			} catch (final ClassCastException e) {
				final String message = "The proxied state was the wrong class"; //$NON-NLS-1$
				final IStatus status = new Status(IStatus.ERROR,
						WorkbenchPlugin.PI_WORKBENCH, 0, message, e);
				WorkbenchPlugin.log(message, status);
				return false;

			} catch (final CoreException e) {
				final String message = "The proxied state for '" + configurationElement.getAttribute(stateAttributeName) //$NON-NLS-1$
						+ "' could not be loaded"; //$NON-NLS-1$
				IStatus status = new Status(IStatus.ERROR,
						WorkbenchPlugin.PI_WORKBENCH, 0, message, e);
				WorkbenchPlugin.log(message, status);
				return false;
			}
		}

		return true;
	}

	public final void removeListener(final IHandlerStateListener listener) {
		if (state == null) {
			if (listeners != null) {
				listeners.remove(listener);
				if (listeners.isEmpty()) {
					listeners = null;
				}
			}
		} else {
			state.removeListener(listener);
		}
	}

	public final void save(final IPreferenceStore store,
			final String preferenceKey) {
		if (loadState() && state instanceof IPersistableHandlerState) {
			((IPersistableHandlerState) state).load(store, preferenceKey);
		}
	}

	public final void setPersisted(final boolean persisted) {
		if (loadState() && state instanceof IPersistableHandlerState) {
			((IPersistableHandlerState) state).setPersisted(persisted);
		}
	}

	public final void setValue(final Object value) {
		if (loadState()) {
			state.setValue(value);
		}
	}

	public final String toString() {
		if (state == null) {
			return configurationElement.getAttribute(stateAttributeName);
		}

		return state.toString();
	}
}
