/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal;

import org.eclipse.swt.graphics.Image;

import org.eclipse.jface.viewers.LabelProvider;

/**
 * Label provider for editors.
 */
class EditorsLabelProvider extends LabelProvider {

	public EditorsLabelProvider() {
	}

	public String getText(Object element) {
		if (element instanceof EditorPane) {
			return ((EditorPane) element).getEditorReference().getTitle();
		}
		return null;
	}
	
	public Image getImage(Object element) {
		if (element instanceof EditorPane) {
			return ((EditorPane) element).getEditorReference().getTitleImage();
		}
		return null;
	}
}
