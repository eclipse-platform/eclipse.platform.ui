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
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.forms.*;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.part.EditorPart;

/**
 * A base class that all pages that should be added to FormEditor
 * must subclass. Form page has a managed form. Subclasses should
 * override method 'createFormContent(ManagedForm)' to fill the
 * form with content. Note that page itself can be loaded
 * lazily (when first open). Consequently, the call to create
 * the form content can come after the editor has been opened
 * for a while (in fact, it is possible to open and close the
 * editor and never create the form because no attempt was
 * made to show the page).
 * 
 * @since 3.0
 */
public class FormPage extends EditorPart implements IFormPage {
	private FormEditor editor;
	private PageForm mform;
	private int index;
	private String id;
	private String title;
	
	public FormPage(FormEditor editor, String id, String title) {
		this(id, title);
		initialize(editor);
	}

/**
 * The constructor.
 * @param id a unique page identifier
 * @param title a user-friendly page title
 */	
	public FormPage(String id, String title) {
		this.id = id;
		this.title = title;
	}

/**
 * Initializes the form page.
 * @see IEditorPart#init
 */	
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
		if (active) {
			// We are switching to this page - refresh it
			// if needed.
			mform.refresh();
		}
	}
	
	public boolean isActive() {
		return this.equals(editor.getActivePageInstance());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent) {
		ScrolledForm form = editor.getToolkit().createScrolledForm(parent);
		mform = new PageForm(this, form);
		BusyIndicator.showWhile(parent.getDisplay(), new Runnable() {
			public void run() {
				createFormContent(mform);
			}
		});
	}
	
/**
 * Subclasses should override this method to create content
 * in the form hosted in this page.
 * @param managedForm the form hosted in this page.
 */
	protected void createFormContent(IManagedForm managedForm) {
	}
	
	public Control getPartControl() {
		return mform!=null?mform.getForm():null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#dispose()
	 */
	public void dispose() {
		if (mform!=null) mform.dispose();
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
		if (mform!=null) mform.setFocus();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISaveablePart#doSave(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void doSave(IProgressMonitor monitor) {
		if (mform!=null) mform.commit(true);
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
		return mform!=null?mform.isDirty():false;
	}

	public void setIndex(int index) {
		this.index = index;
	}
	
	public int getIndex() {
		return index;
	}
	public boolean isEditor() {
		return false;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.editor.IFormPage#focusOn(java.lang.Object)
	 */
	public boolean selectReveal(Object object) {
		if (mform!=null)
			return mform.setInput(object);
		return false;
	}
}