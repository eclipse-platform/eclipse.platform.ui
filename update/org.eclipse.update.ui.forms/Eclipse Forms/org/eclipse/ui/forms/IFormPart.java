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
 * Classes that take part
 */
public interface IFormPart {
	/**
	 * Initializes the part.
	 * 
	 * @param form
	 */
	void initialize(IManagedForm form);
	/**
	 * Disposes the part allowing it to release allocated resources.
	 */
	void dispose();
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
	 * Refreshes the part completely from the information freshly obtained from
	 * the model.
	 */
	void refresh();
}