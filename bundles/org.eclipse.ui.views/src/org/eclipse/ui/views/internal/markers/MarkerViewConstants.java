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

package org.eclipse.ui.views.internal.markers;

import org.eclipse.core.resources.IMarker;


class MarkerViewConstants {
	private MarkerViewConstants() {} //Prevent instantiation
	
	public static final String[] ROOT_TYPES = {IMarker.MARKER};
	public static final String RESOURCE = "resource"; //$NON-NLS-1$
	public static final String FOLDER = "folder"; //$NON-NLS-1$
	public static final String CREATION_TIME = "creationTime"; //$NON-NLS-1$

}
