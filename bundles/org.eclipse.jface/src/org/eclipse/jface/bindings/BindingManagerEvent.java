/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.bindings;

/**
 * An instance of this class describes changes to an instance of
 * <code>BindingManager</code>.
 * <p>
 * This class is not intended to be extended by clients.
 * </p>
 * <p>
 * <em>EXPERIMENTAL</em>. The commands architecture is currently under
 * development for Eclipse 3.1. This class -- its existence, its name and its
 * methods -- are in flux. Do not use this class yet.
 * </p>
 * 
 * @since 3.1
 * @see IBindingManagerListener#bindingManagerChanged(BindingManagerEvent)
 */
public final class BindingManagerEvent {

	/**
	 * Whether the active bindings have changed.
	 */
	private final boolean activeBindingsChanged;

	/**
	 * Whether the active scheme has changed.
	 */
	private final boolean activeSchemeChanged;

	/**
	 * Whether the locale has changed.
	 */
	private final boolean localeChanged;

	/**
	 * Whether the platform has changed.
	 */
	private final boolean platformChanged;

	/**
	 * The binding manager that has changed; this value is never
	 * <code>null</code>.
	 */
	private final BindingManager manager;

	/**
	 * The scheme that became defined or undefined. This value may be
	 * <code>null</code> if no scheme changed its defined state.
	 */
	private final Scheme scheme;

	/**
	 * Whether the given scheme became defined.
	 */
	private final boolean schemeDefined;

	/**
	 * Whether the given scheme became undefined.
	 */
	private final boolean schemeUndefined;

	/**
	 * Creates a new instance of this class.
	 * 
	 * @param manager
	 *            the instance of manager that changed; must not be
	 *            <code>null</code>.
	 * @param activeBindingsChanged
	 *            Whether the active bindings have changed.
	 * @param activeSchemeChanged
	 *            true, iff the active scheme changed.
	 * @param scheme
	 *            The scheme that became defined or undefined; <code>null</code>
	 *            if no scheme changed state.
	 * @param schemeDefined
	 *            <code>true</code> if the given scheme became defined;
	 *            <code>false</code> otherwise.
	 * @param schemeUndefined
	 *            <code>true</code> if the given scheme became undefined;
	 *            <code>false</code> otherwise.
	 * @param localeChanged
	 *            <code>true</code> iff the active locale changed
	 * @param platformChanged
	 *            <code>true</code> iff the active platform changed
	 */
	public BindingManagerEvent(final BindingManager manager,
			final boolean activeBindingsChanged,
			final boolean activeSchemeChanged, final Scheme scheme,
			final boolean schemeDefined, final boolean schemeUndefined,
			final boolean localeChanged, final boolean platformChanged) {
		if (manager == null)
			throw new NullPointerException(
					"A binding manager event needs a binding manager"); //$NON-NLS-1$

		if (schemeDefined || schemeUndefined) {
			if (scheme == null) {
				throw new NullPointerException(
						"If a scheme changed defined state, then there should be a scheme identifier"); //$NON-NLS-1$
			}
		} else if (scheme != null) {
			throw new IllegalArgumentException(
					"The scheme has not changed defined state"); //$NON-NLS-1$
		}

		this.manager = manager;
		this.activeBindingsChanged = activeBindingsChanged;
		this.activeSchemeChanged = activeSchemeChanged;
		this.scheme = scheme;
		this.schemeDefined = schemeDefined;
		this.schemeUndefined = schemeUndefined;
		this.localeChanged = localeChanged;
		this.platformChanged = platformChanged;
	}

	/**
	 * Returns the instance of the manager that changed.
	 * 
	 * @return the instance of the manager that changed. Guaranteed not to be
	 *         <code>null</code>.
	 */
	public final BindingManager getManager() {
		return manager;
	}

	/**
	 * Returns the scheme that changed.
	 * 
	 * @return The changed scheme
	 */
	public final Scheme getScheme() {
		return scheme;
	}

	/**
	 * Returns whether or not the active scheme changed.
	 * 
	 * @return true, iff the active scheme property changed.
	 */
	public final boolean hasActiveSchemeChanged() {
		return activeSchemeChanged;
	}

	/**
	 * Returns whether the locale has changed
	 * 
	 * @return <code>true</code> if the locale changed; <code>false</code>
	 *         otherwise.
	 */
	public boolean hasLocaleChanged() {
		return localeChanged;
	}

	/**
	 * Returns whether the platform has changed
	 * 
	 * @return <code>true</code> if the platform changed; <code>false</code>
	 *         otherwise.
	 */
	public boolean hasPlatformChanged() {
		return platformChanged;
	}

	/**
	 * Returns whether the active bindings have changed.
	 * 
	 * @return <code>true</code> if the active bindings have changed;
	 *         <code>false</code> otherwise.
	 */
	public final boolean haveActiveBindingsChanged() {
		return activeBindingsChanged;
	}

	/**
	 * Returns whether or not the scheme became defined
	 * 
	 * @return <code>true</code> if the scheme became defined.
	 */
	public final boolean isSchemeDefined() {
		return schemeDefined;
	}

	/**
	 * Returns whether or not the scheme became undefined
	 * 
	 * @return <code>true</code> if the scheme became undefined.
	 */
	public final boolean isSchemeUndefined() {
		return schemeUndefined;
	}
}
