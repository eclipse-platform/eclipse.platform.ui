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
 * @author dejan
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public abstract class FormEditor extends MultiPageEditorPart {
	private FormToolkit toolkit;
	
	public FormEditor() {
	}

/**
 * Creates the common toolkit for this editor. Subclasses should
 * override this method to create pages but must call 'super'
 * before attempting to use the toolkit.
 */
	protected void createPages() {
		toolkit = new FormToolkit(getContainer().getDisplay());
	}

	public void dispose() {
		super.dispose();
		toolkit.dispose();
	}
	
	public FormToolkit getToolkit() {
		return toolkit;
	}
}
