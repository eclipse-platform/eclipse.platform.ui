/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
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

import org.eclipse.swt.graphics.Image;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.LabelProvider;

class ChangeElementLabelProvider extends LabelProvider {

	private Map fDescriptorImageMap= new HashMap();

	public ChangeElementLabelProvider() {
	}
		
	public Image getImage(Object object) {
		return manageImageDescriptor(((PreviewNode)object).getImageDescriptor());
	}
	
	public String getText(Object object) {
		return ((PreviewNode)object).getText();
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