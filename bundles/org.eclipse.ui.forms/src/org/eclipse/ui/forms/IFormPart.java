/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.forms;

/**
 * 
 * @see IManagedForm
 */
public interface IFormPart {
	/**
	 * Initializes the part.
	 * 
	 * @param form the managed form that manages the part
	 */
	void initialize(IManagedForm form);
	/**
	 * Disposes the part allowing it to release allocated resources.
	 */
	void dispose();
	/**
	 * Returns true if the part has been modified with respect to
	 * the data loaded from the model.
	 * @return
	 */
	boolean isDirty();
	/**
	 * If part is displaying information loaded from a model, this method
	 * instructs it to commit the new (modified) data back into the model.
	 * 
	 * @param onSave
	 *            indicates if commit is called during 'save' operation or for
	 *            some other reason (for example, if form is contained in a
	 *            wizard or a multi-page editor and the user is about to leave
	 *            the page).
	 */
	void commit(boolean onSave);

	/**
	 * Notifies the part that an object has been set as overall form's input.
	 * The part can elect to react by revealing or selecting the object, or do
	 * nothing if not applicable.
	 */
	void setFormInput(Object input);
	/**
	 * Instructs form part to transfer focus to the widget that should has
	 * focus in that part. The method can do nothing (if it has no widgets
	 * capable of accepting focus).
	 */
	void setFocus();
	/**
	 * Tests whether the form part is stale and needs refreshing.
	 * Parts can receive notification from models that will make
	 * their content stale, but may need to delay refreshing
	 * to improve performance (for example, there is no
	 * need to immediately refresh a part on a form that is
	 * current on a hidden page).
	 * <p>It is important to differentiate 'stale' and 'dirty'
	 * states. Part is 'dirty' if user interacted with
	 * its editable widgets and changed the values. In contrast,
	 * part is 'stale' when the data it presents in the widgets
	 * has been changed in the model without direct user
	 * interaction.
	 * @return <code>true</code> if the part needs refreshing, 
	 * <code>false</code> otherwise.
	 */
	boolean isStale();
	/**
	 * Marks the part stale. Stale parts are refreshed the next
	 * time the form is made visible. If the form is already
	 * visible, it will be refreshed immediately.
	 */
	void markStale();
	/**
	 * Refreshes the part completely from the information freshly 
	 * obtained from the model. The method will not be called
	 * if the part is not stale. Otherwise, the part is
	 * responsible for clearing the 'stale' flag after refreshing
	 * itself.
	 */
	void refresh();
}