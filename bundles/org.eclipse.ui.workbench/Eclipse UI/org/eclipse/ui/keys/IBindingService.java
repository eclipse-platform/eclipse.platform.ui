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
package org.eclipse.ui.keys;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.bindings.Scheme;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.ui.IService;

/**
 * <p>
 * Provides services related to the binding architecture (e.g., keyboard
 * shortcuts) within the workbench. This service can be used to access the
 * currently active bindings, as well as the current state of the binding
 * architecture.
 * </p>
 * <p>
 * <em>EXPERIMENTAL</em>. The commands architecture is currently under
 * development for Eclipse 3.1. This class -- its existence, its name and its
 * methods -- are in flux. Do not use this class yet.
 * </p>
 * 
 * @since 3.1
 */
public interface IBindingService extends IService {

	/**
	 * Gets the active bindings for a given command identifier.
	 * 
	 * @param commandId
	 *            The identifier of the command for which the active bindings
	 *            should be found; must not be <code>null</code>.
	 * @return The collection of all active bindings for the given command. This
	 *         collection may be empty, but it is never <code>null</code>.
	 */
	public Collection getActiveBindingsFor(String commandId);

	/**
	 * Returns the currently active scheme.
	 * 
	 * @return The currently active scheme. This value may (in certain rare
	 *         circumstances) be <code>null</code>.
	 */
	public Scheme getActiveScheme();

	/**
	 * Returns the current set of bindings.
	 * 
	 * @return The current set of bindings (<code>Binding</code>). This is
	 *         an unmodifiable collection.
	 */
	public Set getBindings();

	/**
	 * Returns the default scheme identifier for the currently running
	 * application.
	 * 
	 * @return The default scheme identifier (<code>String</code>); never
	 *         <code>null</code>, but may be empty or point to an undefined
	 *         scheme.
	 */
	public String getDefaultSchemeId();

	/**
	 * Returns the collection of the identifiers for all of the defined schemes
	 * in the workbench.
	 * 
	 * @return The collection of scheme identifiers (<code>String</code>)
	 *         that are defined; never <code>null</code>, but may be empty.
	 */
	public Collection getDefinedSchemeIds();

	/**
	 * Returns the currently active locale.
	 * 
	 * @return The current locale.
	 */
	public String getLocale();

	/**
	 * Returns all of the possible bindings that start with the given trigger
	 * (but are not equal to the given trigger).
	 * 
	 * @param trigger
	 *            The prefix to look for; must not be <code>null</code>.
	 * @return A map of triggers (<code>TriggerSequence</code>) to command
	 *         identifier (<code>String</code>). This map may be empty, but
	 *         it is never <code>null</code>.
	 */
	public Map getPartialMatches(TriggerSequence trigger);

	/**
	 * Returns the command identifier for the active binding matching this
	 * trigger, if any.
	 * 
	 * @param trigger
	 *            The trigger to match; may be <code>null</code>.
	 * @return The command identifier that matches, if any; <code>null</code>
	 *         otherwise.
	 */
	public String getPerfectMatch(TriggerSequence trigger);

	/**
	 * Returns the currently active platform.
	 * 
	 * @return The current platform.
	 */
	public String getPlatform();

	/**
	 * Retrieves the scheme with the given identifier. If no such scheme exists,
	 * then an undefined scheme with the given id is created.
	 * 
	 * @param schemeId
	 *            The identifier to find; must not be <code>null</code>.
	 * @return A scheme with the given identifier, either defined or undefined.
	 */
	public Scheme getScheme(String schemeId);

	/**
	 * Returns whether the given trigger sequence is a partial match for the
	 * given sequence.
	 * 
	 * @param trigger
	 *            The sequence which should be the prefix for some binding;
	 *            should not be <code>null</code>.
	 * @return <code>true</code> if the trigger can be found in the active
	 *         bindings; <code>false</code> otherwise.
	 */
	public boolean isPartialMatch(TriggerSequence trigger);

	/**
	 * Returns whether the given trigger sequence is a perfect match for the
	 * given sequence.
	 * 
	 * @param trigger
	 *            The sequence which should match exactly; should not be
	 *            <code>null</code>.
	 * @return <code>true</code> if the trigger can be found in the active
	 *         bindings; <code>false</code> otherwise.
	 */
	public boolean isPerfectMatch(TriggerSequence trigger);

	/**
	 * <p>
	 * Writes the given active scheme and bindings to the preference store. Only
	 * the bindings that are of the <code>Binding.USER</code> type will be
	 * written; the others will be ignored. This should only be used by
	 * applications trying to persist user preferences. If you are trying to
	 * change the active scheme as an RCP application, then you should be using
	 * the <code>plugin_customization.ini</code> file. If you are trying to
	 * switch between groups of bindings dynamically, you should be using
	 * contexts.
	 * </p>
	 * <p>
	 * This method also updates the active scheme and bindings in the system to
	 * match those written to the preference store.
	 * </p>
	 * 
	 * @param activeScheme
	 *            The scheme which should be persisted; may be <code>null</code>.
	 * @param bindings
	 *            The bindings which should be persisted; may be
	 *            <code>null</code>.
	 * @throws IOException
	 *             If something goes wrong while writing to the preference
	 *             store.
	 * @see org.eclipse.ui.IWorkbenchPreferenceConstants
	 * @see org.eclipse.ui.contexts.IContextService
	 */
	public void savePreferences(Scheme activeScheme, Set bindings)
			throws IOException;
}
