package org.eclipse.team.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
/**
 * Central location for constants used by the Team user interface.
 */
public interface UIConstants {
	// plugin id
	public final String PLUGIN_ID = "org.eclipse.team.ui"; //$NON-NLS-1$
	
	// extension points
	public final String PT_CONFIGURATION ="configurationWizards"; //$NON-NLS-1$
	public final String PT_DECORATORS = "decorators"; //$NON-NLS-1$

	// image paths
	public final String ICON_PATH_FULL = "icons/full/"; //$NON-NLS-1$
	public final String ICON_PATH_BASIC = "icons/basic/"; //$NON-NLS-1$

	//Local toolbars (colour)
	public final String IMG_DLG_SYNC_INCOMING = "clcl16/incom_synch.gif"; //$NON-NLS-1$
	public final String IMG_DLG_SYNC_OUTGOING = "clcl16/outgo_synch.gif"; //$NON-NLS-1$
	public final String IMG_DLG_SYNC_CONFLICTING = "clcl16/conflict_synch.gif"; //$NON-NLS-1$
	public final String IMG_REFRESH = "clcl16/refresh.gif"; //$NON-NLS-1$
	public final String IMG_IGNORE_WHITESPACE = "clcl16/ignorews_edit.gif";	 //$NON-NLS-1$

	//sync view modes
	public final String IMG_SYNC_MODE_CATCHUP = "clcl16/catchup_rls.gif"; //$NON-NLS-1$
	public final String IMG_SYNC_MODE_RELEASE = "clcl16/release_rls.gif"; //$NON-NLS-1$
	public final String IMG_SYNC_MODE_FREE = "clcl16/catchuprelease_rls.gif"; //$NON-NLS-1$
	
	// Wizard banners
	public final String IMG_WIZBAN_SHARE = "wizban/newconnect_wizban.gif"; //$NON-NLS-1$
	
}
