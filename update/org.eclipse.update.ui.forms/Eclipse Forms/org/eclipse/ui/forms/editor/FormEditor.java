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

import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.part.MultiPageEditorPart;

/**
 * This class forms a base of multi-page form editors that typically use one or
 * more pages with forms and one page for raw source of the editor input.
 * 
 * @since 3.0
 */

public abstract class FormEditor extends MultiPageEditorPart {
	private FormToolkit toolkit;

	/**
	 * The constructor.
	 */

	public FormEditor() {
	}

	/**
	 * Creates the common toolkit for this editor. Subclasses should override
	 * this method to create pages but must call 'super' before attempting to
	 * use the toolkit.
	 */
	protected void createPages() {
		toolkit = new FormToolkit(getContainer().getDisplay());
	}

	/**
	 * Disposes the toolkit after disposing the editor itself.
	 */
	public void dispose() {
		super.dispose();
		toolkit.dispose();
	}

	/**
	 * Returns the toolkit owned by this editor.
	 * 
	 * @return the toolkit object
	 */
	public FormToolkit getToolkit() {
		return toolkit;
	}
}