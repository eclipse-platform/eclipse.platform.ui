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
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.forms.events.*;
import org.eclipse.ui.forms.widgets.*;
/**
 * Section part implements IFormPart interface based on the Section widget. It
 * can either wrap the widget or create one itself.
 * 
 * @see Section
 */
public class SectionPart implements IFormPart {
	private IManagedForm managedForm;
	private Section section;
	private boolean dirty = false;
	private boolean stale = true;
	/**
	 * Creates a new section part based on the provided section.
	 * 
	 * @param section
	 *            the section to use
	 */
	public SectionPart(Section section) {
		this.section = section;
		hookListeners();
	}
	/**
	 * Creates a new section part inside the provided parent and using the
	 * provided toolkit. The section part will create the section widget.
	 * 
	 * @param parent
	 *            the parent
	 * @param toolkit
	 *            the toolkit to use
	 * @param style
	 *            the section widget style
	 */
	public SectionPart(Composite parent, FormToolkit toolkit, int style) {
		this(toolkit.createSection(parent, style));
	}
	protected void hookListeners() {
		if ((section.getExpansionStyle() & Section.TWISTIE) != 0
				|| (section.getExpansionStyle() & Section.TREE_NODE) != 0) {
			section.addExpansionListener(new ExpansionAdapter() {
				public void expansionStateChanging(ExpansionEvent e) {
					SectionPart.this.expansionStateChanging(e.getState());
				}
				public void expansionStateChanged(ExpansionEvent e) {
					SectionPart.this.expansionStateChanged(e.getState());
				}
			});
		}
	}
	/**
	 * Returns the section widget used in this part.
	 * 
	 * @return the section widget
	 */
	public Section getSection() {
		return section;
	}
	/**
	 * The section is about to expand or collapse.
	 * 
	 * @param expanding
	 *            <code>true</code> for expansion, <code>false</code> for
	 *            collapse.
	 */
	protected void expansionStateChanging(boolean expanding) {
	}
	/**
	 * The section has expanded or collapsed.
	 * 
	 * @param expanded
	 *            <code>true</code> for expansion, <code>false</code> for
	 *            collapse.
	 */
	protected void expansionStateChanged(boolean expanded) {
		managedForm.getForm().reflow(false);
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.forms.IFormPart#initialize(org.eclipse.ui.forms.IManagedForm)
	 */
	public void initialize(IManagedForm form) {
		this.managedForm = form;
	}
	/**
	 * Returns the form that manages this part.
	 * 
	 * @return the managed form
	 */
	public IManagedForm getForm() {
		return managedForm;
	}
	/**
	 * Disposes the section part. Subclasses should override to release any
	 * system resources.
	 */
	public void dispose() {
	}
	/**
	 * Commits the section part. Subclasses should call 'super' when
	 * overriding.
	 * 
	 * @param onSave
	 *            <code>true</code> if the request to commit has arrived as a
	 *            result of the 'save' action.
	 */
	public void commit(boolean onSave) {
		dirty = false;
	}
	/**
	 * Sets the overal form input. Subclases may elect to override the method
	 * and adjust according to the form input.
	 * 
	 * @param input
	 *            the form input object
	 */
	public void setFormInput(Object input) {
	}
	/**
	 * Instructs the section to grab keyboard focus. The default implementation
	 * will transfer focus to the section client. Subclasses may override and
	 * transfer focus to some widget in the client.
	 */
	public void setFocus() {
		Control client = section.getClient();
		if (client != null)
			client.setFocus();
	}
	/**
	 * Refreshes the section after becoming stale (falling behind data in the
	 * model). Subclasses must call 'super' when overriding this method.
	 */
	public void refresh() {
		stale = false;
	}
	/**
	 * Marks the section dirty. Subclasses should call this method as a result
	 * of user interaction with the widgets in the section.
	 */
	public void markDirty() {
		dirty = true;
		managedForm.dirtyStateChanged();
	}
	/**
	 * Tests whether the section is dirty i.e. its widgets have state that is
	 * newer than the data in the model.
	 * 
	 * @return <code>true</code> if the section is dirty, <code>false</code>
	 *         otherwise.
	 */
	public boolean isDirty() {
		return dirty;
	}
	/**
	 * Tests whether the section is stale i.e. its widgets have state that is
	 * older than the data in the model.
	 * 
	 * @return <code>true</code> if the section is stale, <code>false</code>
	 *         otherwise.
	 */
	public boolean isStale() {
		return stale;
	}
	/**
	 * Marks the section stale. Subclasses should call this method as a result
	 * of model notification that indicates that the content of the section is
	 * no longer in sync with the model.
	 */
	public void markStale() {
		stale = true;
		managedForm.staleStateChanged();
	}
}