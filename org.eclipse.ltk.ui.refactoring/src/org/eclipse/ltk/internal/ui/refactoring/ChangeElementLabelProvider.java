/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.ui.refactoring;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.LabelProvider;

class ChangeElementLabelProvider extends LabelProvider implements IFontProvider {

	private Map fDescriptorImageMap= new HashMap();

	public ChangeElementLabelProvider() {
	}

	public Image getImage(Object object) {
		return manageImageDescriptor(((PreviewNode)object).getImageDescriptor());
	}

	public String getText(Object object) {
		String text= ((PreviewNode)object).getText();
		if (isDerived(object)) {
			return Messages.format(RefactoringUIMessages.ChangeElementLabelProvider_derived, text);
		} else {
			return text;
		}
	}

	public Font getFont(Object element) {
		if (isDerived(element)) {
			return JFaceResources.getFontRegistry().getItalic(JFaceResources.DIALOG_FONT);
		} else {
			return null;
		}
	}

	private boolean isDerived(Object element) {
		PreviewNode node= (PreviewNode)element;
		return node.hasDerived();
	}

	public void dispose() {
		for (Iterator iter= fDescriptorImageMap.values().iterator(); iter.hasNext(); ) {
			Image image= (Image)iter.next();
			image.dispose();
		}
		super.dispose();
	}

	private Image manageImageDescriptor(ImageDescriptor descriptor) {
		Image image= (Image)fDescriptorImageMap.get(descriptor);
		if (image == null) {
			image= descriptor.createImage();
			fDescriptorImageMap.put(descriptor, image);
		}
		return image;
	}
}
