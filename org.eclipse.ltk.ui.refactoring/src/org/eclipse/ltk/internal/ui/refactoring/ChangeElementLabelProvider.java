/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.ui.refactoring;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.LabelProvider;

class ChangeElementLabelProvider extends LabelProvider implements IFontProvider {

	private Map<ImageDescriptor, Image> fDescriptorImageMap= new HashMap<>();

	public ChangeElementLabelProvider() {
	}

	@Override
	public Image getImage(Object object) {
		return manageImageDescriptor(((PreviewNode)object).getImageDescriptor());
	}

	@Override
	public String getText(Object object) {
		String text= ((PreviewNode)object).getText();
		if (isDerived(object)) {
			return Messages.format(RefactoringUIMessages.ChangeElementLabelProvider_derived, text);
		} else {
			return text;
		}
	}

	@Override
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

	@Override
	public void dispose() {
		for (Image image : fDescriptorImageMap.values()) {
			image.dispose();
		}
		super.dispose();
	}

	private Image manageImageDescriptor(ImageDescriptor descriptor) {
		Image image= fDescriptorImageMap.get(descriptor);
		if (image == null) {
			image= descriptor.createImage();
			fDescriptorImageMap.put(descriptor, image);
		}
		return image;
	}
}
