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
	public final String IMG_CONFLICT_OVR = "ovr/confchg_ov.gif"; //$NON-NLS-1$
	public final String IMG_ERROR_OVR = "ovr/error_co.gif"; //$NON-NLS-1$
	public final String IMG_WARNING_OVR = "ovr/warning_co.gif"; //$NON-NLS-1$
	public final String IMG_HOURGLASS_OVR = "ovr/hourglass_ov.gif"; //$NON-NLS-1$
	
	public final String IMG_COLLAPSE_ALL = "clcl16/collapseall.gif"; //$NON-NLS-1$
	public final String IMG_COLLAPSE_ALL_ENABLED = "elcl16/collapseall.gif"; //$NON-NLS-1$
	
	public final String IMG_SYNC_VIEW = "eview16/synch_synch.gif"; //$NON-NLS-1$
	public final String IMG_COMPARE_VIEW = "eview16/compare_view.gif"; //$NON-NLS-1$

	 // local toolbars (colour)
	 public final String IMG_DLG_SYNC_INCOMING = "elcl16/incom_synch.gif"; //$NON-NLS-1$
	 public final String IMG_DLG_SYNC_OUTGOING = "elcl16/outgo_synch.gif"; //$NON-NLS-1$
	 public final String IMG_DLG_SYNC_CONFLICTING = "elcl16/conflict_synch.gif"; //$NON-NLS-1$
	 public final String IMG_REFRESH = "elcl16/refresh.gif"; //$NON-NLS-1$
	 public final String IMG_IGNORE_WHITESPACE = "elcl16/ignorews_edit.gif";	 //$NON-NLS-1$
	 public final String IMG_CONTENTS = "elcl16/contents.gif"; //$NON-NLS-1$
	
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
	 public final String IMG_SYNC_MODE_CATCHUP = "elcl16/catchup_rls.gif"; //$NON-NLS-1$
	 public final String IMG_SYNC_MODE_RELEASE = "elcl16/release_rls.gif"; //$NON-NLS-1$
	 public final String IMG_SYNC_MODE_FREE = "elcl16/catchuprelease_rls.gif"; //$NON-NLS-1$
	
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
	 public final String IMG_KEY_LOCK = "wizban/keylock.gif"; //$NON-NLS-1$
	
	 //objects
	 public final String IMG_SITE_ELEMENT = "elcl16/site_element.gif"; //$NON-NLS-1$
	 public final String IMG_CHANGE_FILTER = "elcl16/change_filter.gif"; //$NON-NLS-1$	 
	 public final String IMG_COMPRESSED_FOLDER = "obj/compressed_folder_obj.gif"; //$NON-NLS-1$
	 public final String IMG_WARNING = "ovr/warning_co.gif"; //$NON-NLS-1$
	 public final String IMG_HIERARCHICAL = "elcl16/hierarchicalLayout.gif"; //$NON-NLS-1$
}

