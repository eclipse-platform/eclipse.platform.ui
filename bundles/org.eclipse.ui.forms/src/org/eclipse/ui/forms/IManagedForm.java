/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.forms;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.forms.widgets.*;

/**
 * Managed form wraps a form widget and adds life cycle methods for form parts.
 * A form part is a portion of the form that participates in form life cycle
 * events.
 * <p>
 * There is no 1/1 mapping between widgets and form parts. A widget like
 * Section can be a part by itself, but a number of widgets can gather around
 * one form part.
 *<p>
 * Although many Eclipse Forms classes expect IManagedForm interface,
 * implementing it is not required. Instead, ManagedForm should
 * either be used as-is or subclassed.
 * @see ManagedForm
 * @since 3.0
 */
public interface IManagedForm {
	/**
	 * Returns the toolkit used by this form.
	 * 
	 * @return the toolkit
	 */
	public FormToolkit getToolkit();
	/**
	 * Returns the form widget managed by this form.
	 * 
	 * @return the form widget
	 */
	public ScrolledForm getForm();
	/**
	 * Reflows the form as a result of the layout change.
	 * 
	 * @param changed
	 *            if <code>true</code>, discard cached layout information
	 */
	public void reflow(boolean changed);
	/**
	 * A part can use this method to notify other parts that implement
	 * IPartSelectionListener about selection changes.
	 * 
	 * @param part
	 *            the part that broadcasts the selection
	 * @param selection
	 *            the selection in the part
	 */
	public void fireSelectionChanged(IFormPart part, ISelection selection);
	/**
	 * Returns all the parts currently managed by this form.
	 * 
	 * @return the managed parts
	 */
	IFormPart[] getParts();
	/**
	 * Adds the new part to the form.
	 * 
	 * @param part
	 *            the part to add
	 */
	void addPart(IFormPart part);
	/**
	 * Removes the part from the form.
	 * 
	 * @param part
	 *            the part to remove
	 */
	void removePart(IFormPart part);
	/**
	 * Sets the input of this page to the provided object.
	 * 
	 * @param input
	 *            the new page input
	 * @return <code>true</code> if the form contains this object,
	 * <code>false</code> otherwise.
	 */
	boolean setInput(Object input);
	/**
	 * Returns the current page input.
	 * 
	 * @return page input object or <code>null</code> if not applicable.
	 */
	Object getInput();
	/**
	 * Tests if form is dirty. A managed form is dirty if at least one managed
	 * part is dirty.
	 * 
	 * @return <code>true</code> if at least one managed part is dirty,
	 *         <code>false</code> otherwise.
	 */
	boolean isDirty();
	/**
	 * Notifies the form that the dirty state of one of its parts has changed.
	 * The global dirty state of the form can be obtained by calling 'isDirty'.
	 * 
	 * @see #isDirty
	 */
	void dirtyStateChanged();
	/**
	 * Commits the dirty form. All pending changes in the widgets are flushed
	 * into the model.
	 * 
	 * @param onSave
	 */
	void commit(boolean onSave);
	/**
	 * Tests if form is stale. A managed form is stale if at least one managed
	 * part is stale. This can happen when the underlying model changes,
	 * resulting in the presentation of the part being out of sync with the
	 * model and needing refreshing.
	 * 
	 * @return <code>true</code> if the form is stale, <code>false</code>
	 *         otherwise.
	 */
	boolean isStale();
	/**
	 * Notifies the form that the stale state of one of its parts has changed.
	 * The global stale state of the form can be obtained by calling 'isStale'.
	 */
	void staleStateChanged();
	/**
	 * Refreshes the form by refreshing every part that is stale.
	 */
	void refresh();
	/**
	 * Sets the container that owns this form. Depending on the
	 * context, the container may be wizard, editor page,
	 * editor etc.
	 * @param container the container of this form
	 */
	void setContainer(Object container);
	/**
	 * Returns the container of this form.
	 * @return the form container
	 */
	Object getContainer();
}
