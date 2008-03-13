/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.examples.contributions.model;

import java.util.Collection;

import org.eclipse.jface.util.IPropertyChangeListener;

/**
 * An example service to manage People.
 * 
 * @since 3.4
 */
public interface IPersonService {
	/**
	 * Fired when a new person is created.
	 */
	public static final String PROP_ADD = "add"; //$NON-NLS-1$

	/**
	 * Fired when a Person is updated by the service.
	 */
	public static final String PROP_CHANGE = "change"; //$NON-NLS-1$

	/**
	 * The collection of people.
	 * 
	 * @return an unmodifiable Collection. For looking, not touching. Will not
	 *         be <code>null</code>.
	 */
	public Collection getPeople();

	/**
	 * Return the person identified by <code>id</code>.
	 * 
	 * @param id
	 *            a valid ID
	 * @return a copy of the person, or <code>null</code> if not found.
	 */
	public Person getPerson(int id);

	/**
	 * Update the person in this service. If this person does not exist in the
	 * service it does nothing.
	 * 
	 * @param person
	 *            the person to update. Must not be <code>null</code>.
	 */
	public void updatePerson(Person person);

	/**
	 * Create a person object for this id. Does nothing if a person already
	 * exists for this id. The returned person has default values for most
	 * attributes, and the person's ID is set to <code>id</code>.
	 * 
	 * @param id
	 *            the id for the person.
	 * @return a copy of the person, or <code>null</code> if a person already
	 *         exists.
	 */
	public Person createPerson(int id);

	/**
	 * Listen for changes to people managed by this service.
	 * <p>
	 * Note: this services cleans up listeners when it is disposed.
	 * </p>
	 * 
	 * @param listener
	 *            the property change listener. Has no effect if an identical
	 *            listener is already registered. Must not be <code>null</code>
	 * @see #PROP_ADD
	 * @see #PROP_CHANGE
	 * @see IPersonService#removePersonChangeListener(IPropertyChangeListener)
	 */
	public void addPersonChangeListener(IPropertyChangeListener listener);

	/**
	 * Remove the change listener.
	 * 
	 * @param listener
	 *            the property change listener. Has no effect if it is not
	 *            already registered. Must not be <code>null</code>.
	 */
	public void removePersonChangeListener(IPropertyChangeListener listener);

	/**
	 * Log this person into the system. This is in effect until anoteher person
	 * is logged in.
	 * 
	 * @param person
	 *            the person to log in. May be <code>null</code>.
	 */
	public void login(Person person);
}
