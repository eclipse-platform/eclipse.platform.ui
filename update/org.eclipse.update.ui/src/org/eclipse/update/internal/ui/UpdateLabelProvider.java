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
package org.eclipse.update.internal.ui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.update.configuration.IConfiguredSite;
import org.eclipse.update.internal.ui.parts.SharedLabelProvider;

/**
 *  */

public class UpdateLabelProvider extends SharedLabelProvider {
	/**
	 * Constructor for UpdateLabelProvider.
	 */
	public UpdateLabelProvider() {
	}
	
	public String getText(Object obj) {
		return super.getText(obj);
	}
	
	public Image getImage(Object obj) {
		return super.getImage(obj);
	}
	
	public ImageDescriptor getLocalSiteDescriptor(IConfiguredSite csite) {
		if (csite.isProductSite())
			return UpdateUIImages.DESC_PSITE_OBJ;
		if (csite.isExtensionSite())
			return UpdateUIImages.DESC_ESITE_OBJ;
		return UpdateUIImages.DESC_LSITE_OBJ;
	}
	
	public Image getLocalSiteImage(IConfiguredSite csite) {
		ImageDescriptor desc = getLocalSiteDescriptor(csite);
		return get(desc);
	}
}
