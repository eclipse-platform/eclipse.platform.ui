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

import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.IColorDecorator;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IFontDecorator;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

/**
 * Decorating label provider that supports multiple decorators that are font and color decorators.
 * 
 * @since 3.0
 */
public class DecoratingColorLabelProvider extends DecoratingLabelProvider implements IColorProvider, IFontProvider {

	static class MultiLabelDecorator extends LabelProvider implements ILabelDecorator, IFontDecorator, IColorDecorator {
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

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IFontDecorator#decorateFont(java.lang.Object)
		 */
		public Font decorateFont(Object element) {
			for (int i = 0; i < decorators.length; i++) {
				ILabelDecorator decorator = decorators[i];
				if(decorator instanceof IFontDecorator) {
					return ((IFontDecorator)decorator).decorateFont(element);
				}
			}
			return null;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IColorDecorator#decorateForeground(java.lang.Object)
		 */
		public Color decorateForeground(Object element) {
			for (int i = 0; i < decorators.length; i++) {
				ILabelDecorator decorator = decorators[i];
				if(decorator instanceof IColorDecorator) {
					return ((IColorDecorator)decorator).decorateForeground(element);
				}
			}
			return null;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IColorDecorator#decorateBackground(java.lang.Object)
		 */
		public Color decorateBackground(Object element) {
			for (int i = 0; i < decorators.length; i++) {
				ILabelDecorator decorator = decorators[i];
				if(decorator instanceof IColorDecorator) {
					return ((IColorDecorator)decorator).decorateBackground(element);
				}
			}
			return null;
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
