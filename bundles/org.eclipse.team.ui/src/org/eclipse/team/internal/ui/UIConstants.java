package org.eclipse.team.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
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
	public final String PT_TARGETCONFIG ="targetConfigWizards"; //$NON-NLS-1$
	public final String PT_DECORATORS = "decorators"; //$NON-NLS-1$

	// image paths
	public final String ICON_PATH = "icons/full/"; //$NON-NLS-1$

	// local toolbars (colour)
	public final String IMG_DLG_SYNC_INCOMING = "clcl16/incom_synch.gif"; //$NON-NLS-1$
	public final String IMG_DLG_SYNC_OUTGOING = "clcl16/outgo_synch.gif"; //$NON-NLS-1$
	public final String IMG_DLG_SYNC_CONFLICTING = "clcl16/conflict_synch.gif"; //$NON-NLS-1$
	public final String IMG_REFRESH = "clcl16/refresh.gif"; //$NON-NLS-1$
	public final String IMG_IGNORE_WHITESPACE = "clcl16/ignorews_edit.gif";	 //$NON-NLS-1$
	public final String IMG_CONTENTS = "clcl16/contents.gif"; //$NON-NLS-1$
	
	// sync view modes
	public final String IMG_SYNC_MODE_CATCHUP = "clcl16/catchup_rls.gif"; //$NON-NLS-1$
	public final String IMG_SYNC_MODE_RELEASE = "clcl16/release_rls.gif"; //$NON-NLS-1$
	public final String IMG_SYNC_MODE_FREE = "clcl16/catchuprelease_rls.gif"; //$NON-NLS-1$
	
	// wizard banners
	public final String IMG_WIZBAN_SHARE = "wizban/share_wizban.gif"; //$NON-NLS-1$
	public final String IMG_WIZBAN_NEW_CONNECTION = "wizban/newconnect_wizban.gif"; //$NON-NLS-1$	
	public final String IMG_PROJECTSET_IMPORT_BANNER = "wizban/import_projectset_wizban.gif"; //$NON-NLS-1$
	public final String IMG_PROJECTSET_EXPORT_BANNER = "wizban/export_projectset_wizban.gif"; //$NON-NLS-1$
	
	// preferences
	public final String PREF_ALWAYS_IN_INCOMING_OUTGOING = "pref_always_in_incoming_outgoing"; //$NON-NLS-1$

	//objects
	public final String IMG_SITE_ELEMENT = "clcl16/site_element.gif"; //$NON-NLS-1$

}
