/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.examples.presentation;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;


/**
 * @since 3.0
 */
public class PresentationImages {	
	public static final String CLOSE_VIEW = "close_view.gif";
	public static final String MIN_VIEW = "min_view.gif";
	public static final String MAX_VIEW = "max_view.gif";
	public static final String RESTORE_VIEW = "restore_view.gif";
	public static final String VIEW_MENU = "view_menu.gif";
	public static final String SHOW_TOOLBAR = "show_toolbar.gif";
	public static final String HIDE_TOOLBAR = "hide_toolbar.gif";
	
	private PresentationImages() {
	}
	
	public static Image getImage(String imageName) {
		return PresentationPlugin.getDefault().getImage(imageName);
	}
	
	public static ImageDescriptor getImageDescriptor(String imageName) {
		return PresentationPlugin.getDefault().getImageDescriptor(imageName);
	}
}
