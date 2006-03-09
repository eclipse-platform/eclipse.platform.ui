/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.mappings;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.ICVSUIConstants;
import org.eclipse.team.internal.core.subscribers.DiffChangeSet;
import org.eclipse.team.internal.ui.mapping.ResourceModelLabelProvider;

public class ChangeSetLabelProvider extends ResourceModelLabelProvider {

	private Image changeSetImage;

	protected String getDelegateText(Object element) {
		if (element instanceof DiffChangeSet) {
			DiffChangeSet set = (DiffChangeSet) element;
			return set.getName();
		}
		return super.getDelegateText(element);
	}
	
	protected Image getDelegateImage(Object element) {
		if (element instanceof DiffChangeSet) {
			return getChangeSetImage();
		}
		return super.getDelegateImage(element);
	}

	private Image getChangeSetImage() {
		if (changeSetImage == null) {
			ImageDescriptor imageDescriptor = CVSUIPlugin.getPlugin().getImageDescriptor(ICVSUIConstants.IMG_CHANGELOG);
			if (imageDescriptor != null)
				changeSetImage = imageDescriptor.createImage();
		}
		return changeSetImage;
	}
	
	public void dispose() {
		if (changeSetImage != null) {
			changeSetImage.dispose();
		}
		super.dispose();
	}

}
