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
package org.eclipse.ui.forms.editor;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.forms.*;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.part.EditorPart;

/**
 *
 */
public class FormPage extends EditorPart implements IFormPage {
	private FormEditor editor;
	private ManagedForm mform;
	private boolean active;
	private int index;
	private String id;
	private String title;

	public FormPage(String id, String title) {
		this.id = id;
		this.title = title;
	}

	public void init(IEditorSite site, IEditorInput input) {
		setSite(site);
		setInput(input);
	}

	public void initialize(FormEditor editor) {
		this.editor = editor;
	}

	public FormEditor getEditor() {
		return editor;
	}

	public IManagedForm getManagedForm() {
		return mform;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.IFormPage#setActive(boolean)
	 */
	public void setActive(boolean active) {
		this.active = active;
	}
	
	public boolean isActive() {
		return active;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent) {
		Form form = editor.getToolkit().createForm(parent);
		mform = new ManagedForm(form);
	}
	
	public Control getPartControl() {
		return mform.getForm();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#dispose()
	 */
	public void dispose() {
		mform.dispose();
	}
	
	public String getId() {
		return id;
	}
	
	public String getTitle() {
		return title;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#getTitleImage()
	 */
	public Image getTitleImage() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#setFocus()
	 */
	public void setFocus() {
		mform.setFocus();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISaveablePart#doSave(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void doSave(IProgressMonitor monitor) {
		mform.commit(true);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISaveablePart#doSaveAs()
	 */	
	public void doSaveAs() {
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISaveablePart#isSaveAsAllowed()
	 */	
	public boolean isSaveAsAllowed() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISaveablePart#isDirty()
	 */
	public boolean isDirty() {
		return mform.isDirty();
	}

	public void setIndex(int index) {
		this.index = index;
	}
	
	public int getIndex() {
		return index;
	}
	public boolean isSource() {
		return false;
	}
}
