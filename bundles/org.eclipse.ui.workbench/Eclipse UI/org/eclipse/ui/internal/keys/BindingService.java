/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.keys;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.jface.bindings.BindingManager;
import org.eclipse.jface.bindings.Scheme;
import org.eclipse.ui.keys.IBindingService;

/**
 * @since 3.1
 */
public class BindingService implements IBindingService {

	/**
	 * The binding manager that supports this service. This value is never
	 * <code>null</code>.
	 */
	private final BindingManager bindingManager;

	/**
	 * Constructs a new instance of <code>BindingService</code> using a JFace
	 * binding manager.
	 * 
	 * @param bindingManager
	 *            The binding manager to use; must not be <code>null</code>.
	 */
	public BindingService(final BindingManager bindingManager) {
		if (bindingManager == null) {
			throw new NullPointerException(
					"Cannot create a binding service with a null manager"); //$NON-NLS-1$
		}
		this.bindingManager = bindingManager;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.keys.IBindingService#getActiveBindingsDisregardingContext()
	 */
	public Collection getActiveBindingsDisregardingContext() {
		final Collection bindingCollections = bindingManager
				.getActiveBindingsDisregardingContext().values();
		final Collection mergedBindings = new ArrayList();
		final Iterator bindingCollectionItr = bindingCollections.iterator();
		while (bindingCollectionItr.hasNext()) {
			final Collection bindingCollection = (Collection) bindingCollectionItr
					.next();
			if ((bindingCollection != null) && (!bindingCollection.isEmpty())) {
				mergedBindings.addAll(bindingCollection);
			}
		}

		return mergedBindings;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.keys.IBindingService#getActiveScheme()
	 */
	public Scheme getActiveScheme() {
		return bindingManager.getActiveScheme();
	}

	public Set getBindings() {
		return bindingManager.getBindings();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.contexts.IBindingService#getDefinedSchemeIds()
	 */
	public Collection getDefinedSchemeIds() {
		return bindingManager.getDefinedSchemeIds();
	}

	public String getLocale() {
		return bindingManager.getLocale();
	}

	public String getPlatform() {
		return bindingManager.getPlatform();
	}

	public final Scheme getScheme(final String schemeId) {
		/*
		 * TODO Need to put in place protection against the context being
		 * changed.
		 */
		return bindingManager.getScheme(schemeId);
	}
}
