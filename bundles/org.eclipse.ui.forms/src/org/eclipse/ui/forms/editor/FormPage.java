/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
 * A base class that all pages that should be added to FormEditor must subclass.
 * Form page has an instance of PageForm that extends managed form. Subclasses
 * should override method 'createFormContent(ManagedForm)' to fill the form with
 * content. Note that page itself can be loaded lazily (on first open).
 * Consequently, the call to create the form content can come after the editor
 * has been opened for a while (in fact, it is possible to open and close the
 * editor and never create the form because no attempt has been made to show the
 * page).
 * 
 * @since 3.0
 */
public class FormPage extends EditorPart implements IFormPage {
	private FormEditor editor;
	private PageForm mform;
	private int index;
	private String id;
	
	private static class PageForm extends ManagedForm {
		public PageForm(FormPage page, ScrolledForm form) {
			super(page.getEditor().getToolkit(), form);
			setContainer(page);
		}
		
		public FormPage getPage() {
			return (FormPage)getContainer();
		}
		public void dirtyStateChanged() {
			getPage().getEditor().editorDirtyStateChanged();
		}
		public void staleStateChanged() {
			if (getPage().isActive())
				refresh();
		}
	}
	/**
	 * A constructor that creates the page and initializes it with the editor.
	 * 
	 * @param editor
	 *            the parent editor
	 * @param id
	 *            the unique identifier
	 * @param title
	 *            the page title
	 */
	public FormPage(FormEditor editor, String id, String title) {
		this(id, title);
		initialize(editor);
	}
	/**
	 * The constructor. The parent editor need to be passed in the
	 * <code>initialize</code> method if this constructor is used.
	 * 
	 * @param id
	 *            a unique page identifier
	 * @param title
	 *            a user-friendly page title
	 */
	public FormPage(String id, String title) {
		this.id = id;
		setPartName(title);
	}
	/**
	 * Initializes the form page.
	 * 
	 * @see IEditorPart#init
	 */
	public void init(IEditorSite site, IEditorInput input) {
		setSite(site);
		setInput(input);
	}
	/**
	 * Primes the form page with the parent editor instance.
	 * 
	 * @param editor
	 *            the parent editor
	 */
	public void initialize(FormEditor editor) {
		this.editor = editor;
	}
	/**
	 * Returns the parent editor.
	 * 
	 * @return parent editor instance
	 */
	public FormEditor getEditor() {
		return editor;
	}
	/**
	 * Returns the managed form owned by this page.
	 * 
	 * @return the managed form
	 */
	public IManagedForm getManagedForm() {
		return mform;
	}
	/**
	 * Implements the required method by refreshing the form when set active.
	 * Subclasses must call super when overriding this method.
	 */
	public void setActive(boolean active) {
		if (active) {
			// We are switching to this page - refresh it
			// if needed.
			if (mform != null)
				mform.refresh();
		}
	}
	/**
	 * Tests if the page is active by asking the parent editor if this page is
	 * the currently active page.
	 * 
	 * @return <code>true</code> if the page is currently active,
	 *         <code>false</code> otherwise.
	 */
	public boolean isActive() {
		return this.equals(editor.getActivePageInstance());
	}
	/**
	 * Creates the part control by creating the managed form using the parent
	 * editor's toolkit. Subclasses should override
	 * <code>createFormContent(IManagedForm)</code> to populate the form with
	 * content.
	 * 
	 * @param parent
	 *            the page parent composite
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
	 * Subclasses should override this method to create content in the form
	 * hosted in this page.
	 * 
	 * @param managedForm
	 *            the form hosted in this page.
	 */
	protected void createFormContent(IManagedForm managedForm) {
	}
	/**
	 * Returns the form page control.
	 * 
	 * @return managed form's control
	 */
	public Control getPartControl() {
		return mform != null ? mform.getForm() : null;
	}
	/**
	 * Disposes the managed form.
	 */
	public void dispose() {
		if (mform != null)
			mform.dispose();
	}
	/**
	 * Returns the unique identifier that can be used to reference this page.
	 * 
	 * @return the unique page identifier
	 */
	public String getId() {
		return id;
	}
	/**
	 * Returns <code>null</code>- form page has no title image. Subclasses
	 * may override.
	 * 
	 * @return <code>null</code>
	 */
	public Image getTitleImage() {
		return null;
	}
	/**
	 * Sets the focus by delegating to the managed form.
	 */
	public void setFocus() {
		if (mform != null)
			mform.setFocus();
	}
	/**
	 * @see org.eclipse.ui.ISaveablePart#doSave(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void doSave(IProgressMonitor monitor) {
		if (mform != null)
			mform.commit(true);
	}
	/**
	 * @see org.eclipse.ui.ISaveablePart#doSaveAs()
	 */
	public void doSaveAs() {
	}
	/**
	 * @see org.eclipse.ui.ISaveablePart#isSaveAsAllowed()
	 */
	public boolean isSaveAsAllowed() {
		return false;
	}
	/**
	 * Implemented by testing if the managed form is dirty.
	 * 
	 * @return <code>true</code> if the managed form is dirty,
	 *         <code>false</code> otherwise.
	 * 
	 * @see org.eclipse.ui.ISaveablePart#isDirty()
	 */
	public boolean isDirty() {
		return mform != null ? mform.isDirty() : false;
	}
	/**
	 * Preserves the page index.
	 * 
	 * @param index
	 *            the assigned page index
	 */
	public void setIndex(int index) {
		this.index = index;
	}
	/**
	 * Returns the saved page index.
	 * 
	 * @return the page index
	 */
	public int getIndex() {
		return index;
	}
	/**
	 * Form pages are not editors.
	 * 
	 * @return <code>false</code>
	 */
	public boolean isEditor() {
		return false;
	}
	/**
	 * Attempts to select and reveal the given object by passing the request to
	 * the managed form.
	 * 
	 * @param object
	 *            the object to select and reveal in the page if possible.
	 * @return <code>true</code> if the page has been successfully selected
	 *         and revealed by one of the managed form parts, <code>false</code>
	 *         otherwise.
	 */
	public boolean selectReveal(Object object) {
		if (mform != null)
			return mform.setInput(object);
		return false;
	}
	/**
	 * By default, editor will be allowed to flip the page.
	 * @return <code>true</code>
	 */
	public boolean canLeaveThePage() {
		return true;
	}
}
