/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.navigator.resources;

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.navigator.INavigatorContentService;

/**
 * Provides constant for the view identifier for the standard instance of the
 * Common Navigator.
 * 
 * @since 3.3
 * 
 * @see CommonNavigator
 * @see INavigatorContentService
 * 
 */
public final class ProjectExplorer {

	/**
	 * Provides a constant for the standard instance of the Common Navigator.
	 * 
	 * @see PlatformUI#getWorkbench()
	 * @see IWorkbench#getActiveWorkbenchWindow()
	 * @see IWorkbenchWindow#getActivePage()
	 * 
	 * @see IWorkbenchPage#findView(String)
	 * @see IWorkbenchPage#findViewReference(String)
	 */
	public static final String VIEW_ID = "org.eclipse.ui.navigator.ProjectExplorer"; //$NON-NLS-1$

}
