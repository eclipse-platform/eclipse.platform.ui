/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.synchronize;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;

/**
 * Decorating label provider that supports multiple decorators and color providers.
 * 
 * @since 3.0
 */
public class DecoratingColorLabelProvider extends DecoratingLabelProvider implements IColorProvider, IFontProvider {

	static class MultiLabelDecorator extends LabelProvider implements ILabelDecorator {
		private ILabelDecorator[] decorators;

		public MultiLabelDecorator(ILabelDecorator[] decorators) {
			this.decorators = decorators;
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ILabelDecorator#decorateImage(org.eclipse.swt.graphics.Image, java.lang.Object)
		 */
		public Image decorateImage(Image image, Object element) {
			for (int i = 0; i < decorators.length; i++) {
				ILabelDecorator decorator = decorators[i];
				Image newImage = decorator.decorateImage(image, element);
				if (newImage != null) {
					image = newImage;
				}
			}
			return image;
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ILabelDecorator#decorateText(java.lang.String, java.lang.Object)
		 */
		public String decorateText(String text, Object element) {
			for (int i = 0; i < decorators.length; i++) {
				ILabelDecorator decorator = decorators[i];
				String newText = decorator.decorateText(text, element);
				if (newText != null) {
					text = newText;
				}
			}
			return text;
		}
		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
		 */
		public void dispose() {
			for (int i = 0; i < decorators.length; i++) {
				ILabelDecorator d = decorators[i];
				d.dispose();
			}
		}
	}
	public DecoratingColorLabelProvider(ILabelProvider provider, ILabelDecorator[] decorators) {
		super(provider, new MultiLabelDecorator(decorators));
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IColorProvider#getForeground(java.lang.Object)
	 */
	public Color getForeground(Object element) {
		ILabelProvider p = getLabelProvider();
		if (p instanceof IColorProvider) {
			return ((IColorProvider) p).getForeground(element);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IColorProvider#getBackground(java.lang.Object)
	 */
	public Color getBackground(Object element) {
		ILabelProvider p = getLabelProvider();
		if (p instanceof IColorProvider) {
			return ((IColorProvider) p).getBackground(element);
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IFontProvider#getFont(java.lang.Object)
	 */
	public Font getFont(Object element) {
		ILabelProvider p = getLabelProvider();
		if (p instanceof IFontProvider) {
			return ((IFontProvider) p).getFont(element);
		}
		return null;
	}
}
