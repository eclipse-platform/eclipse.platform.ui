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
package org.eclipse.ui.forms.editor;

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.Form;

/**
 * A variation of {@link FormEditor}, this editor has a stable header that does
 * not change when pages are switched. Pages that are added to this editor
 * should not have the title or image set.
 * 
 * @since 3.3
 */
public abstract class SingleHeaderFormEditor extends FormEditor {
	private Form headerForm;

	/**
	 * The default constructor.
	 */

	public SingleHeaderFormEditor() {
	}

	/**
	 * Overrides <code>super</code> to create a form in which to host the tab
	 * folder.
	 * 
	 * @param parent
	 *            the page container parent
	 * 
	 * @see org.eclipse.ui.part.MultiPageEditorPart#createPageContainer(org.eclipse.swt.widgets.Composite)
	 */

	protected Composite createPageContainer(Composite parent) {
		parent = super.createPageContainer(parent);
		parent.setLayout(new FillLayout());
		headerForm = getToolkit().createForm(parent);
		createHeaderContents(headerForm);
		return headerForm.getBody();
	}

	/**
	 * Returns the form that owns the shared header.
	 * 
	 * @return the shared header
	 */

	public Form getHeaderForm() {
		return headerForm;
	}

	/**
	 * Subclasses should extend this method to configure the form that owns the
	 * shared header.
	 * 
	 * @param headerForm
	 *            the form that owns the shared header
	 */

	protected void createHeaderContents(Form headerForm) {
	}
}