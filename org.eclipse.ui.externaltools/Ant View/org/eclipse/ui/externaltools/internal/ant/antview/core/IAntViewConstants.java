/*
 * Copyright (c) 2002, Roscoe Rush. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public License
 * Version 0.5 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.eclipse.org/
 *
 */
package org.eclipse.ui.externaltools.internal.ant.antview.core;

public interface IAntViewConstants {
    public static final String RESOURCE_BASE_NAME = "org.eclipse.ui.externaltools.internal.ant.antview.core.ResourceStrings";

    public static final String IMAGE_DIR               = "icons/full/clcl16/";	
	public static final String IMAGE_PROJECT           = "ant_project.gif";
	public static final String IMAGE_PROJECT_ERROR     = "ant_project_err.gif";
	public static final String IMAGE_TARGET_SELECTED   = "ant_tsk_check.gif";
	public static final String IMAGE_TARGET_DESELECTED = "ant_tsk_arrow.gif";
	public static final String IMAGE_RUN               = "ant_run.gif";
	public static final String IMAGE_REMOVE            = "remove_co.gif";
	public static final String IMAGE_CLEAR             = "clear_co.gif";
	public static final String IMAGE_REFRESH           = "refresh_nav.gif";
	public static final String IMAGE_ELEMENT           = "element.gif";
	public static final String IMAGE_ELEMENTS          = "elements.gif";
	public static final String IMAGE_DEFAULT           = "elements.gif";
	public static final String IMAGE_DEFAULT_ERROR     = "elements.gif";
	public static final String IMAGE_ERROR             = "error.gif";

	public static final String PREF_ANT_DISPLAY        = "org.eclipse.ui.externaltools.antview.preference.AntDisplayLevel";
	public static final String PREF_PROJECT_DISPLAY    = "org.eclipse.ui.externaltools.antview.preference.ProjectDisplay";
	public static final String PREF_TARGET_DISPLAY     = "org.eclipse.ui.externaltools.antview.preference.TargetDisplay";
	public static final String PREF_TARGET_FILTER      = "org.eclipse.ui.externaltools.antview.preference.TargetFilter";
	public static final String PREF_ANT_BUILD_FILE     = "org.eclipse.ui.externaltools.antview.preference.AntBuildFile";
	public static final String PREF_TARGET_VECTOR      = "org.eclipse.ui.externaltools.antview.preference.TargetVector";
	
	public static final String ANT_DISPLAYLVL_ERROR    = "ERROR";
    public static final String ANT_DISPLAYLVL_WARN     = "WARN";
    public static final String ANT_DISPLAYLVL_INFO     = "INFO";
    public static final String ANT_DISPLAYLVL_VERBOSE  = "VERBOSE";
    public static final String ANT_DISPLAYLVL_DEBUG    = "DEBUG";

    public static final String PROJECT_DISPLAY_NAMEATTR = "Name.Attribute";
    public static final String PROJECT_DISPLAY_DIRLOC   = "Directory.Location";
    public static final String PROJECT_DISPLAY_BOTH     = "Both";   	    
    
    public static final String TARGET_DISPLAY_NAMEATTR  = "Name Attribute";
    public static final String TARGET_DISPLAY_DESCATTR  = "Description Attribute";
    public static final String TARGET_DISPLAY_BOTH      = "Both";
    
    public static final String TARGET_FILTER_NONE       = "None";
    public static final String TARGET_FILTER_DESCATTR   = "Description Attribute";
   
}

