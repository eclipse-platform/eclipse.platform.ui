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

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorReference;

class EditorsLabelProvider extends LabelProvider {

	public EditorsLabelProvider() {
	}

	public String getText(Object element) {
		String text = null;

		if (element instanceof EditorPane) {
			IEditorReference editorReference =
				((EditorPane) element).getEditorReference();
			String title = editorReference.getTitle().trim();
			text = title;
			
			if (editorReference.isDirty())
				text = "*" + text; //$NON-NLS-1$
			
			String titleTooltip = editorReference.getTitleToolTip().trim();

			if (titleTooltip.endsWith(title))
				titleTooltip =
					titleTooltip
						.substring(0, titleTooltip.lastIndexOf(title))
						.trim();

			if (titleTooltip.length() >= 1)
				text += " - " + titleTooltip; //$NON-NLS-1$
			
			return text;
		}

		return text;
	}

	public Image getImage(Object element) {
		if (element instanceof EditorPane)
			return ((EditorPane) element).getEditorReference().getTitleImage();

		return null;
	}
}
