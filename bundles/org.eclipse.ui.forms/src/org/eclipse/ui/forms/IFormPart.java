/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.forms;

/**
 * Classes that implement this interface can be added to the managed form and
 * take part in the form life cycle. The part is initialized with the form and
 * will be asked to accept focus. The part can receive form input and can elect
 * to do something according to it (for example, select an object that matches
 * the input).
 * <p>
 * The form part has two 'out of sync' states in respect to the model(s) that
 * feed the form: <b>dirty</b> and <b>stale</b>. When a part is dirty, it
 * means that the user interacted with it and now its widgets contain state that
 * is newer than the model. In order to sync up with the model, 'commit' needs
 * to be called. In contrast, the model can change 'under' the form (as a result
 * of some actions outside the form), resulting in data in the model being
 * 'newer' than the content presented in the form. A 'stale' form part is
 * brought in sync with the model by calling 'refresh'. The part is responsible
 * for notifying the form when one of these states change in the part. The form
 * reserves the right to handle this notification in the most appropriate way
 * for the situation (for example, if the form is in a page of the multi-page
 * editor, it may do nothing for stale parts if the page is currently not
 * showing).
 * <p>
 * When the form is disposed, each registered part is disposed as well. Parts
 * are responsible for releasing any system resources they created and for
 * removing themselves as listeners from all event providers.
 * 
 * @see IManagedForm
 * @since 3.0
 * 
 */
public interface IFormPart {
	/**
	 * Initializes the part.
	 * 
	 * @param form
	 *            the managed form that manages the part
	 */
	void initialize(IManagedForm form);

	/**
	 * Disposes the part allowing it to release allocated resources.
	 */
	void dispose();

	/**
	 * Returns true if the part has been modified with respect to the data
	 * loaded from the model.
	 * 
	 * @return true if the part has been modified with respect to the data
	 *         loaded from the model
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
	 * 
	 * @return <code>true</code> if the part has selected and revealed the
	 *         input object, <code>false</code> otherwise.
	 */
	boolean setFormInput(Object input);

	/**
	 * Instructs form part to transfer focus to the widget that should has focus
	 * in that part. The method can do nothing (if it has no widgets capable of
	 * accepting focus).
	 */
	void setFocus();

	/**
	 * Tests whether the form part is stale and needs refreshing. Parts can
	 * receive notification from models that will make their content stale, but
	 * may need to delay refreshing to improve performance (for example, there
	 * is no need to immediately refresh a part on a form that is current on a
	 * hidden page).
	 * <p>
	 * It is important to differentiate 'stale' and 'dirty' states. Part is
	 * 'dirty' if user interacted with its editable widgets and changed the
	 * values. In contrast, part is 'stale' when the data it presents in the
	 * widgets has been changed in the model without direct user interaction.
	 * 
	 * @return <code>true</code> if the part needs refreshing,
	 *         <code>false</code> otherwise.
	 */
	boolean isStale();

	/**
	 * Refreshes the part completely from the information freshly obtained from
	 * the model. The method will not be called if the part is not stale.
	 * Otherwise, the part is responsible for clearing the 'stale' flag after
	 * refreshing itself.
	 */
	void refresh();
}
