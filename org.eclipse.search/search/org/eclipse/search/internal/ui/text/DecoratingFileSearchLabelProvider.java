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
package org.eclipse.search.internal.ui.text;

import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.LabelDecorator;

import org.eclipse.ui.PlatformUI;

public class DecoratingFileSearchLabelProvider extends DecoratingLabelProvider implements IRichLabelProvider {

	public DecoratingFileSearchLabelProvider(FileLabelProvider provider) {
		super(provider, PlatformUI.getWorkbench().getDecoratorManager().getLabelDecorator());
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.ui.viewsupport.IRichLabelProvider#getRichTextLabel(Object)
	 */
	public ColoredString getRichTextLabel(Object element) {
		// get a rich label from the label decorator
		FileLabelProvider richLabelProvider= (FileLabelProvider) getLabelProvider();
		ColoredString richLabel= richLabelProvider.getRichTextLabel(element);
		if (richLabel != null) {
			String decorated= null;
			ILabelDecorator labelDecorator= getLabelDecorator();
			if (labelDecorator != null) {
				if (labelDecorator instanceof LabelDecorator) {
					decorated= ((LabelDecorator) labelDecorator).decorateText(richLabel.getString(), element, getDecorationContext());
				} else {
					decorated= labelDecorator.decorateText(richLabel.getString(), element);
				}
			}
			if (decorated != null) {
				return ColoredViewersManager.decorateColoredString(richLabel, decorated, ColoredViewersManager.DECORATIONS_STYLE);
			}
			return richLabel;
		}
		return null;
	}

}
