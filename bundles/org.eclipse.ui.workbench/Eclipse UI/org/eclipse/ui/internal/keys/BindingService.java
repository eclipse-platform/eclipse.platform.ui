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

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.bindings.BindingManager;
import org.eclipse.jface.bindings.Scheme;
import org.eclipse.jface.bindings.TriggerSequence;
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

	public final Collection getActiveBindingsFor(final String commandId) {
		return bindingManager.getActiveBindingsFor(commandId);
	}

	public Scheme getActiveScheme() {
		return bindingManager.getActiveScheme();
	}

	public Set getBindings() {
		return bindingManager.getBindings();
	}

	public Collection getDefinedSchemeIds() {
		return bindingManager.getDefinedSchemeIds();
	}

	public String getLocale() {
		return bindingManager.getLocale();
	}

	public Map getPartialMatches(final TriggerSequence trigger) {
		return bindingManager.getPartialMatches(trigger);
	}

	public String getPerfectMatch(final TriggerSequence trigger) {
		return bindingManager.getPerfectMatch(trigger);
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

	public final boolean isPartialMatch(final TriggerSequence sequence) {
		return bindingManager.isPartialMatch(sequence);
	}

	public final boolean isPerfectMatch(final TriggerSequence sequence) {
		return bindingManager.isPerfectMatch(sequence);
	}
}
