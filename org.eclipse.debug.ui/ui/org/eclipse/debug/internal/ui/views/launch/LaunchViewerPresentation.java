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
package org.eclipse.debug.internal.ui.views.launch;

import org.eclipse.debug.internal.ui.DelegatingModelPresentation;
import org.eclipse.swt.graphics.Image;

/**
 * Presentation/label provider for the launch viewer.
 * It try to compute the labels lazily.
 */
public class LaunchViewerPresentation extends DelegatingModelPresentation {

	private LaunchViewer fViewer;
	
	public LaunchViewerPresentation(LaunchViewer viewer) {
		fViewer= viewer;
	}
	
	/*
	 * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
	 */
	public Image getImage(Object item) {
		// ask for the image in a lazy way
		return getLazyImage(item, fViewer);
	}

	/*
	 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
	 */
	public String getText(Object item) {
		// ask for the text in a lazy way
		return getLazyText(item, fViewer);
	}

}
