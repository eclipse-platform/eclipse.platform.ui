/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.forms.editor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.ManagedForm;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.internal.forms.widgets.FormUtil;

/**
 * A variation of {@link FormEditor}, this editor has a stable header that does
 * not change when pages are switched. Pages that are added to this editor
 * should not have the title or image set.
 *
 * @since 3.3
 */
public abstract class SharedHeaderFormEditor extends FormEditor {
	private HeaderForm headerForm;

	private boolean wasHeaderActive= true;
	private Listener activationListener= null;

	private static class HeaderForm extends ManagedForm {
		public HeaderForm(FormEditor editor, ScrolledForm form) {
			super(editor.getToolkit(), form);
			setContainer(editor);
			if (editor.getEditorInput() != null) {
				setInput(editor.getEditorInput());
			}
		}

		private FormEditor getEditor() {
			return (FormEditor) getContainer();
		}

		@Override
		public void dirtyStateChanged() {
			getEditor().editorDirtyStateChanged();
		}

		@Override
		public void staleStateChanged() {
			refresh();
		}
	}

	/**
	 * The default constructor.
	 */

	public SharedHeaderFormEditor() {
	}

	/**
	 * Overrides <code>super</code> to create a form in which to host the tab
	 * folder. This form will be responsible for creating a common form header.
	 * Child pages should not have a header of their own.
	 *
	 * @param parent
	 *            the page container parent
	 *
	 * @see org.eclipse.ui.part.MultiPageEditorPart#createPageContainer(org.eclipse.swt.widgets.Composite)
	 */

	@Override
	protected Composite createPageContainer(Composite parent) {
		parent = super.createPageContainer(parent);
		parent.setLayout(new FillLayout());
		ScrolledForm scform = getToolkit().createScrolledForm(parent);
		scform.getForm().setData(FormUtil.IGNORE_BODY, Boolean.TRUE);
		headerForm = new HeaderForm(this, scform);
		createHeaderContents(headerForm);
		return headerForm.getForm().getBody();
	}

	/**
	 * Returns the form that owns the shared header.
	 *
	 * @return the shared header
	 */

	public IManagedForm getHeaderForm() {
		return headerForm;
	}

	@Override
	protected void createPages() {
		super.createPages();

		// preempt MultiPageEditorPart#createPartControl(Composite)
		if (getActivePage() == -1) {
			// create page control and initialize page, keep focus on header by calling super implementation
			super.setActivePage(0);
		}
	}

	@Override
	protected void setActivePage(int pageIndex) {
		// programmatic focus change
		wasHeaderActive= false;
		super.setActivePage(pageIndex);
	}

	@Override
	public void setFocus() {
		installActivationListener();
		if (wasHeaderActive) {
			((ManagedForm) getHeaderForm()).setFocus();
		} else {
			int index= getActivePage();
			if (index == -1) {
				((ManagedForm) getHeaderForm()).setFocus();
			} else {
				super.setFocus();
			}
		}
	}

	private void installActivationListener() {
		if (activationListener == null) {
			activationListener = event -> {
				boolean wasHeaderActive = event.widget != getContainer();

				int activePage = getActivePage();
				if (SharedHeaderFormEditor.this.wasHeaderActive != wasHeaderActive && activePage != -1
						&& pages.get(activePage) instanceof IEditorPart) {

					if (wasHeaderActive) {
						deactivateSite(true, true);
					} else {
						activateSite();
					}
				}

				SharedHeaderFormEditor.this.wasHeaderActive = wasHeaderActive;
			};
			getContainer().addListener(SWT.Activate, activationListener);
			getHeaderForm().getForm().getForm().getHead().addListener(SWT.Activate, activationListener);
		}
	}

	@Override
	public void dispose() {
		if (headerForm != null) {
			headerForm.dispose();
			headerForm = null;
		}
		super.dispose();
	}

	@Override
	public boolean isDirty() {
		if (headerForm != null && headerForm.isDirty()) {
			return true;
		}
		return super.isDirty();
	}

	@Override
	protected void commitPages(boolean onSave) {
		if (headerForm != null && headerForm.isDirty()) {
			headerForm.commit(onSave);
		}
		super.commitPages(onSave);
	}

	/**
	 * Subclasses should extend this method to configure the form that owns the
	 * shared header. If the header form will contain controls that can change
	 * the state of the editor, they should be wrapped in an IFormPart so that
	 * they can participate in the life cycle event management.
	 *
	 * @param headerForm
	 *            the form that owns the shared header
	 * @see IFormPart
	 */
	protected void createHeaderContents(IManagedForm headerForm) {
	}
}