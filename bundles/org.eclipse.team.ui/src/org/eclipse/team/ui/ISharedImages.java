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
	//	image paths
	 public final String ICON_PATH = "icons/full/"; //$NON-NLS-1$

	public final String IMG_DIRTY_OVR = "ovr/dirty_ov.gif"; //$NON-NLS-1$
	public final String IMG_CHECKEDIN_OVR = "ovr/version_controlled.gif"; //$NON-NLS-1$
	public final String IMG_CHECKEDOUT_OVR = "ovr/checkedout_ov.gif"; //$NON-NLS-1$
	
	public final String IMG_COLLAPSE_ALL = "clcl16/collapseall.gif"; //$NON-NLS-1$
	public final String IMG_COLLAPSE_ALL_ENABLED = "elcl16/collapseall.gif"; //$NON-NLS-1$
	
	public final String IMG_SYNC_VIEW = "cview16/synch_synch.gif"; //$NON-NLS-1$

	 // local toolbars (colour)
	 public final String IMG_DLG_SYNC_INCOMING = "clcl16/incom_synch.gif"; //$NON-NLS-1$
	 public final String IMG_DLG_SYNC_OUTGOING = "clcl16/outgo_synch.gif"; //$NON-NLS-1$
	 public final String IMG_DLG_SYNC_CONFLICTING = "clcl16/conflict_synch.gif"; //$NON-NLS-1$
	 public final String IMG_REFRESH = "clcl16/refresh.gif"; //$NON-NLS-1$
	 public final String IMG_IGNORE_WHITESPACE = "clcl16/ignorews_edit.gif";	 //$NON-NLS-1$
	 public final String IMG_CONTENTS = "clcl16/contents.gif"; //$NON-NLS-1$
	
	 // local toolbars (disabled)
	 public final String IMG_DLG_SYNC_INCOMING_DISABLED = "dlcl16/incom_synch.gif"; //$NON-NLS-1$
	 public final String IMG_DLG_SYNC_OUTGOING_DISABLED = "dlcl16/outgo_synch.gif"; //$NON-NLS-1$
	 public final String IMG_DLG_SYNC_CONFLICTING_DISABLED = "dlcl16/conflict_synch.gif"; //$NON-NLS-1$
	 public final String IMG_REFRESH_DISABLED = "dlcl16/refresh.gif"; //$NON-NLS-1$
	 public final String IMG_IGNORE_WHITESPACE_DISABLED = "dlcl16/ignorews_edit.gif";	 //$NON-NLS-1$
	 public final String IMG_CONTENTS_DISABLED = "dlcl16/contents.gif"; //$NON-NLS-1$
	
	 // local toolbars (enabled)
	 public final String IMG_DLG_SYNC_INCOMING_ENABLED = "elcl16/incom_synch.gif"; //$NON-NLS-1$
	 public final String IMG_DLG_SYNC_OUTGOING_ENABLED = "elcl16/outgo_synch.gif"; //$NON-NLS-1$
	 public final String IMG_DLG_SYNC_CONFLICTING_ENABLED = "elcl16/conflict_synch.gif"; //$NON-NLS-1$
	 public final String IMG_REFRESH_ENABLED = "elcl16/refresh.gif"; //$NON-NLS-1$
	 public final String IMG_IGNORE_WHITESPACE_ENABLED = "elcl16/ignorews_edit.gif";	 //$NON-NLS-1$
	 public final String IMG_CONTENTS_ENABLED = "elcl16/contents.gif"; //$NON-NLS-1$
	
	 // sync view modes
	 public final String IMG_SYNC_MODE_CATCHUP = "clcl16/catchup_rls.gif"; //$NON-NLS-1$
	 public final String IMG_SYNC_MODE_RELEASE = "clcl16/release_rls.gif"; //$NON-NLS-1$
	 public final String IMG_SYNC_MODE_FREE = "clcl16/catchuprelease_rls.gif"; //$NON-NLS-1$
	
	 // sync view modes (disabled)
	 public final String IMG_SYNC_MODE_CATCHUP_DISABLED = "dlcl16/catchup_rls.gif"; //$NON-NLS-1$
	 public final String IMG_SYNC_MODE_RELEASE_DISABLED = "dlcl16/release_rls.gif"; //$NON-NLS-1$
	 public final String IMG_SYNC_MODE_FREE_DISABLED = "dlcl16/catchuprelease_rls.gif"; //$NON-NLS-1$
	
	 // sync view modes (enabled)
	 public final String IMG_SYNC_MODE_CATCHUP_ENABLED = "elcl16/catchup_rls.gif"; //$NON-NLS-1$
	 public final String IMG_SYNC_MODE_RELEASE_ENABLED = "elcl16/release_rls.gif"; //$NON-NLS-1$
	 public final String IMG_SYNC_MODE_FREE_ENABLED = "elcl16/catchuprelease_rls.gif"; //$NON-NLS-1$
	
	 // wizard banners
	 public final String IMG_WIZBAN_SHARE = "wizban/share_wizban.gif"; //$NON-NLS-1$
	 public final String IMG_PROJECTSET_IMPORT_BANNER = "wizban/import_projectset_wizban.gif"; //$NON-NLS-1$
	 public final String IMG_PROJECTSET_EXPORT_BANNER = "wizban/export_projectset_wizban.gif"; //$NON-NLS-1$
	
	 //objects
	 public final String IMG_SITE_ELEMENT = "clcl16/site_element.gif"; //$NON-NLS-1$
	 public final String IMG_CHANGE_FILTER = "clcl16/change_filter.gif"; //$NON-NLS-1$
	 
	 // TODO: I just picked an image. We should get a custom image
	 public final String IMG_COMPRESSED_FOLDER = "obj/compressed_folder_obj.gif"; //$NON-NLS-1$
}

