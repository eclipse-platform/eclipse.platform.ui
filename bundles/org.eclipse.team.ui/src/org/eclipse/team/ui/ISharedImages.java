package org.eclipse.team.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.jface.resource.ImageDescriptor;

/**
 * Images that are available for providers to re-use. They include
 * common overlays and wizard images. A provider can use their own
 * custom images, these shared images are only available for 
 * convenience.
 */
public interface ISharedImages {
	public final String IMG_DIRTY_OVR = "ovr/dirty_ov.gif"; //$NON-NLS-1$
	public final String IMG_CHECKEDIN_OVR = "ovr/checkedin_ov.gif"; //$NON-NLS-1$
	public final String IMG_CHECKEDOUT_OVR = "ovr/checkedout_ov.gif"; //$NON-NLS-1$
	
	public final String IMG_SITE_VIEW = "cview16/site_view.gif"; //$NON-NLS-1$
	public final String IMG_SITE_ELEMENT = "clcl16/site_element.gif"; //$NON-NLS-1$
}

