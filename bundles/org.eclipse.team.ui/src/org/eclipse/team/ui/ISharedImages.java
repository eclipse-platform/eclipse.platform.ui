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
package org.eclipse.team.ui;



/**
 * Images that are available for providers to re-use. They include
 * common overlays and wizard images. A provider can use their own
 * custom images, these shared images are only available for 
 * convenience.
 */
public interface ISharedImages {
	public final String IMG_DIRTY_OVR = "ovr/dirty_ov.gif"; //$NON-NLS-1$
	public final String IMG_CHECKEDIN_OVR = "ovr/version_controlled.gif"; //$NON-NLS-1$
	public final String IMG_CHECKEDOUT_OVR = "ovr/checkedout_ov.gif"; //$NON-NLS-1$
}

