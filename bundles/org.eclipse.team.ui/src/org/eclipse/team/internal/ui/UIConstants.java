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
	public final String PLUGIN_ID = "org.eclipse.team.ui";
	
	// extension points
	public final String PT_CONFIGURATION ="configurationWizards";
	public final String PT_DECORATORS = "decorators";

	// image paths
	public final String ICON_PATH_FULL = "icons/full/";
	public final String ICON_PATH_BASIC = "icons/basic/";

	//Local toolbars (colour)
	public final String IMG_DLG_SYNC_INCOMING = "clcl16/incom_synch.gif";
	public final String IMG_DLG_SYNC_OUTGOING = "clcl16/outgo_synch.gif";
	public final String IMG_DLG_SYNC_CONFLICTING = "clcl16/conflict_synch.gif";
	public final String IMG_REFRESH = "clcl16/refresh.gif";
	public final String IMG_IGNORE_WHITESPACE = "clcl16/ignorews_edit.gif";	

	//sync view modes
	public final String IMG_SYNC_MODE_CATCHUP = "clcl16/catchup_rls.gif";
	public final String IMG_SYNC_MODE_RELEASE = "clcl16/release_rls.gif";
	public final String IMG_SYNC_MODE_FREE = "clcl16/catchuprelease_rls.gif";
}
