/***************************************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 **************************************************************************************************/
/*
 * Created on Sep 16, 2004
 * 
 * TODO To change the template for this generated file go to Window - Preferences - Java - Code
 * Style - Code Templates
 */
package org.eclipse.ui.navigator.internal.deferred;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.navigator.internal.NavigatorImages;

/**
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part of a work in
 * progress. There is a guarantee neither that this API will work nor that it will remain the same.
 * Please do not use this API without consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.2 
 *
 */
class ProxyPendingAdapter extends PendingUpdateAdapter {


	private static final ImageDescriptor PENDING_IMAGE = NavigatorImages.createManaged("clcl16/", "elipses.gif"); //$NON-NLS-1$ //$NON-NLS-2$
	private Image image;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.progress.PendingUpdateAdapter#getImageDescriptor(java.lang.Object)
	 */
	public ImageDescriptor getImageDescriptor(Object object) {
		return PENDING_IMAGE;
	}

	public Image getIcon() {
		if (image == null)
			image = PENDING_IMAGE.createImage();
		return image;
	}
}