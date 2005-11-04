/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.commands;

import org.eclipse.core.commands.IHandlerState;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * <p>
 * A piece of handler state that can be persisted using {@link IPreferenceStore}.
 * A piece of persistent handler state stores a flag (<code>persisted</code>)
 * as to whether it should do anything for
 * {@link IPersistableHandlerState#load(IPreferenceStore, String)} or
 * {@link IPersistableHandlerState#save(IPreferenceStore, String)} calls.
 * </p>
 * <p>
 * Clients may implement or extend this interface.
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
public interface IPersistableHandlerState extends IHandlerState {

	/**
	 * Whether this state should be persisted when a call to
	 * {@link IPersistableHandlerState#save(IPreferenceStore, String)} or read
	 * when a call to
	 * {@link IPersistableHandlerState#load(IPreferenceStore, String)} is made.
	 * 
	 * @return <code>true</code> if this state should really be persisted;
	 *         <code>false</code> otherwise.
	 */
	public boolean isPersisted();

	/**
	 * Loads this state from the preference store, given the location at which
	 * to look. This method must be symmetric with a call to
	 * {@link IPersistableHandlerState#save(IPreferenceStore, String)}.
	 * 
	 * @param store
	 *            The store from which to read; must not be <code>null</code>.
	 * @param preferenceKey
	 *            The key at which the state is stored; must not be
	 *            <code>null</code>.
	 */
	public void load(IPreferenceStore store, String preferenceKey);

	/**
	 * Saves this state to the preference store, given the location at which to
	 * write. This method must be symmetric with a call to
	 * {@link IPersistableHandlerState#load(IPreferenceStore, String)}.
	 * 
	 * @param store
	 *            The store to which the state should be written; must not be
	 *            <code>null</code>.
	 * @param preferenceKey
	 *            The key at which the state should be stored; must not be
	 *            <code>null</code>.
	 */
	public void save(IPreferenceStore store, String preferenceKey);

	/**
	 * Sets whether this state should be persisted.
	 * 
	 * @param persisted
	 *            <code>true</code> if the state should be persisted;
	 *            <code>false</code> otherwise.
	 */
	public void setPersisted(boolean persisted);
}
