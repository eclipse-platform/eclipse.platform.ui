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

import org.eclipse.ui.forms.*;
import org.eclipse.ui.forms.widgets.ScrolledForm;

/**
 * This class extends managed form and specializes it for use
 * inside a multi-page form editor.
 * 
 * TODO (dejan) - spell out subclass contract
 * @see FormPage
 * @since 3.0
 */
public class PageForm extends ManagedForm {
	/**
	 * TODO (dejan) missing spec
	 * @param toolkit
	 * @param form
	 */
	public PageForm(FormPage page, ScrolledForm form) {
		super(page.getEditor().getToolkit(), form);
		setContainer(page);
	}
	
	public FormPage getPage() {
		return (FormPage)getContainer();
	}
/**
 *@see IManagedForm#dirtyStateChanged
 */	
	public void dirtyStateChanged() {
		getPage().getEditor().editorDirtyStateChanged();
	}
/**
 *@see IManagedForm#staleStateChanged
 */	
	public void staleStateChanged() {
		if (getPage().isActive())
			refresh();
	}
}