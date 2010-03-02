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
package org.eclipse.ui.internal.keys;

import org.eclipse.jface.bindings.BindingManager;
import org.eclipse.jface.bindings.Scheme;
import org.eclipse.ui.commands.IKeyConfiguration;
import org.eclipse.ui.commands.IKeyConfigurationListener;
import org.eclipse.ui.commands.NotDefinedException;

/**
 * A wrapper around the new {@link Scheme} class, providing supported for the
 * old {@link IKeyConfiguration} interface.
 * 
 * @since 3.1
 */
public final class SchemeLegacyWrapper implements IKeyConfiguration {

	/**
	 * The binding manager managing this scheme. This value is never
	 * <code>null</code>.
	 */
	private final BindingManager bindingManager;

	/**
	 * The wrapped scheme; never <code>null</code>
	 */
	private final Scheme scheme;

	/**
	 * Constructs a new instance of <code>SchemeWrapper</code>.
	 * 
	 * @param scheme
	 *            The scheme to be wrapped; must not be <code>null</code>.
	 * @param bindingManager
	 *            The binding manager for this scheme; must not be
	 *            <code>null</code>.
	 */
	public SchemeLegacyWrapper(final Scheme scheme,
			final BindingManager bindingManager) {
		if (scheme == null) {
			throw new NullPointerException("Cannot wrap a null scheme"); //$NON-NLS-1$
		}

		if (bindingManager == null) {
			throw new NullPointerException(
					"Cannot wrap a scheme without a binding manager"); //$NON-NLS-1$
		}

		this.scheme = scheme;
		this.bindingManager = bindingManager;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.commands.IKeyConfiguration#addKeyConfigurationListener(org.eclipse.ui.commands.IKeyConfigurationListener)
	 */
	public void addKeyConfigurationListener(
			IKeyConfigurationListener keyConfigurationListener) {
		scheme.addSchemeListener(new LegacySchemeListenerWrapper(
				keyConfigurationListener, bindingManager));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object o) {
		return scheme.compareTo(o);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.commands.IKeyConfiguration#getDescription()
	 */
	public String getDescription() throws NotDefinedException {
		try {
			return scheme.getDescription();
		} catch (final org.eclipse.core.commands.common.NotDefinedException e) {
			throw new NotDefinedException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.commands.IKeyConfiguration#getId()
	 */
	public String getId() {
		return scheme.getId();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.commands.IKeyConfiguration#getName()
	 */
	public String getName() throws NotDefinedException {
		try {
			return scheme.getName();
		} catch (final org.eclipse.core.commands.common.NotDefinedException e) {
			throw new NotDefinedException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.commands.IKeyConfiguration#getParentId()
	 */
	public String getParentId() throws NotDefinedException {
		try {
			return scheme.getParentId();
		} catch (final org.eclipse.core.commands.common.NotDefinedException e) {
			throw new NotDefinedException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.commands.IKeyConfiguration#isActive()
	 */
	public boolean isActive() {
		return scheme.getId().equals(bindingManager.getActiveScheme().getId());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.commands.IKeyConfiguration#isDefined()
	 */
	public boolean isDefined() {
		return scheme.isDefined();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.commands.IKeyConfiguration#removeKeyConfigurationListener(org.eclipse.ui.commands.IKeyConfigurationListener)
	 */
	public void removeKeyConfigurationListener(
			IKeyConfigurationListener keyConfigurationListener) {
		scheme.removeSchemeListener(new LegacySchemeListenerWrapper(
				keyConfigurationListener, bindingManager));

	}

}
