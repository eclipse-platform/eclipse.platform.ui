package org.eclipse.ui.views.properties;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

/**
 * This interface documents the property constants used by the resource
 * property source.
 */
public interface IResourcePropertyConstants {
	/** 
	 * The <code>IResource</code> property key for name.
	 */
	public static final String	P_LABEL_RES = "name";

	/** 
	 * The <code>IResource</code> property key for path.
	 */
	public static final String	P_PATH_RES = "org.eclipse.ui.path";

	/** 
	 * The <code>IResource</code> property key for display path.
	 */
	public static final String  P_DISPLAYPATH_RES = "path";

	/** 
	 * The <code>IResource</code> property key for read-only.
	 */
	public static final String	P_EDITABLE_RES = "org.eclipse.ui.editable";

	/** 
	 * The <code>IResource</code> property key for display read-only.
	 */
	public static final String  P_DISPLAYEDITABLE_RES = "editable";

	/**
	 * The <code>IResource</code> category for the base values
	 */
	public static final String P_FILE_SYSTEM_CATEGORY = "Info";

	/** 
	 * The <code>IResource</code> property key for path.
	 */
	public static final String	P_SIZE_RES = "org.eclipse.ui.size";
	
	/**
	 * The <code>IResource</code> property key for displaying size
	 */
	public static final String P_DISPLAY_SIZE = "size";

	/** 
	 * The <code>IResource</code> property key for path.
	 */
	public static final String	P_LAST_MODIFIED_RES = "org.eclipse.ui.lastmodified";

	/**
	 * The <code>IResource</code> category for last modified
	 */
	public static final String P_DISPLAY_LAST_MODIFIED = "last modified";
	
}
