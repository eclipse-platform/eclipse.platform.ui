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
package org.eclipse.ui.internal;

import org.eclipse.ui.*;

/**
 * Implements a pane for a MultiEditor.
 */
public class MultiEditorOuterPane extends EditorPane {
	/**
	 * Constructor for MultiEditorOuterPane.
	 */
	public MultiEditorOuterPane(IEditorReference ref, WorkbenchPage page, EditorWorkbook workbook) {
		super(ref, page, workbook);
	}
	/*
	 * @see EditorPane
	 */
	protected void requestActivation() {
		//Outer editor is never activated.
	}
}
