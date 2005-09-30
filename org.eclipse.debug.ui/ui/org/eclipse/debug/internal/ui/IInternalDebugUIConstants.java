/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui;

import org.eclipse.debug.internal.ui.views.memory.MemoryBlocksTreeViewPane;
import org.eclipse.debug.internal.ui.views.memory.RenderingViewPane;
import org.eclipse.debug.ui.IDebugUIConstants;

 
public interface IInternalDebugUIConstants {
    
    public static final String DIALOGSTORE_LASTEXTJAR= "org.eclipse.debug.ui.lastextjar"; //$NON-NLS-1$
        
    //Folders
    public static final String ID_NAVIGATOR_FOLDER_VIEW= "org.eclipse.debug.internal.ui.NavigatorFolderView"; //$NON-NLS-1$
    public static final String ID_TOOLS_FOLDER_VIEW= "org.eclipse.debug.internal.ui.ToolsFolderView"; //$NON-NLS-1$
    public static final String ID_CONSOLE_FOLDER_VIEW= "org.eclipse.debug.internal.ui.ConsoleFolderView"; //$NON-NLS-1$
    public static final String ID_OUTLINE_FOLDER_VIEW= "org.eclipse.debug.internal.ui.OutlineFolderView"; //$NON-NLS-1$
    
    // annotation types for instruction pointers
    public static final String ANN_INSTR_POINTER_CURRENT = "org.eclipse.debug.ui.currentIP"; //$NON-NLS-1$
    public static final String ANN_INSTR_POINTER_SECONDARY = "org.eclipse.debug.ui.secondaryIP"; //$NON-NLS-1$
    
    // tool images
    public static final String IMG_LCL_COLLAPSE_ALL = "IMG_LCL_COLLAPSE_ALL"; //$NON-NLS-1$
    public static final String IMG_LCL_TERMINATE = "IMG_LCL_TERMINATE"; //$NON-NLS-1$
    public static final String IMG_LCL_SHOW_LOGICAL_STRUCTURE = "IMG_LCL_SHOW_LOGICAL_STRUCTURE"; //$NON-NLS-1$
    public static final String IMG_LCL_RUN_TO_LINE= "IMG_LCL_RUN_TO_LINE"; //$NON-NLS-1$
    public static final String IMG_SRC_LOOKUP_MENU = "IMG_SRC_LOOKUP_MENU"; //$NON-NLS-1$
    public static final String IMG_LCL_MONITOR_EXPRESSION= "IMG_LCL_MONITOR_EXPRESSION"; //$NON-NLS-1$
    public static final String IMG_LCL_REMOVE_MEMORY= "IMG_LCL_REMOVE_MEMORY"; //$NON-NLS-1$
    public static final String IMG_LCL_RESET_MEMORY= "IMG_LCL_RESET_MEMORY";//$NON-NLS-1$
    public static final String IMG_LCL_COPY_VIEW_TO_CLIPBOARD = "IMG_LCL_COPY_VIEW_TO_CLIPBOARD"; //$NON-NLS-1$
    public static final String IMG_LCL_PRINT_TOP_VIEW_TAB = "IMG_LCL_PRINT_TOP_VIEW_TAB"; //$NON-NLS-1$
    
    // disabled local tool images
    public static final String IMG_DLCL_LOCK= "IMG_DLCL_LOCK"; //$NON-NLS-1$
    public static final String IMG_DLCL_DETAIL_PANE= "IMG_DLCL_DETAIL_PANE"; //$NON-NLS-1$
    public static final String IMG_DLCL_CHANGE_VARIABLE_VALUE= "IMG_DLCL_CHANGE_VARIABLE_VALUE"; //$NON-NLS-1$
    public static final String IMG_DLCL_TYPE_NAMES= "IMG_DLCL_TYPE_NAMES"; //$NON-NLS-1$
    public static final String IMG_DLCL_SHOW_LOGICAL_STRUCTURE= "IMG_DLCL_SHOW_LOGICAL_STRUCTURE"; //$NON-NLS-1$
    public static final String IMG_DLCL_DETAIL_PANE_UNDER= "IMG_DLCL_DETAIL_PANE_UNDER"; //$NON-NLS-1$
    public static final String IMG_DLCL_DETAIL_PANE_RIGHT= "IMG_DLCL_DETAIL_PANE_RIGHT"; //$NON-NLS-1$
    public static final String IMG_DLCL_DETAIL_PANE_HIDE= "IMG_DLCL_DETAIL_PANE_HIDE"; //$NON-NLS-1$
    public static final String IMG_DLCL_COLLAPSE_ALL = "IMG_DLCL_COLLAPSE_ALL"; //$NON-NLS-1$
    public static final String IMG_DLCL_TERMINATE = "IMG_DLCL_TERMINATE"; //$NON-NLS-1$
    public static final String IMG_DLCL_REMOVE_ALL = "IMG_DLCL_REMOVE_ALL"; //$NON-NLS-1$
    public static final String IMG_DLCL_REMOVE = "IMG_DLCL_REMOVE"; //$NON-NLS-1$
    public static final String IMG_DLCL_RUN_TO_LINE= "IMG_DLCL_RUN_TO_LINE"; //$NON-NLS-1$
    public static final String IMG_SRC_LOOKUP_MENU_DLCL = "IMG_SRC_LOOKUP_MENU_DISABLED"; //$NON-NLS-1$
    public static final String IMG_DLCL_MONITOR_EXPRESSION= "IMG_DLCL_MONITOR_EXPRESSION"; //$NON-NLS-1$
    public static final String IMG_DLCL_REMOVE_MEMORY= "IMG_DLCL_REMOVE_MEMORY"; //$NON-NLS-1$
    public static final String IMG_DLCL_RESET_MEMORY= "IMG_DLCL_RESET_MEMORY"; //$NON-NLS-1$
    public static final String IMG_DLCL_COPY_VIEW_TO_CLIPBOARD= "IMG_DLCL_COPY_VIEW_TO_CLIPBOARD"; //$NON-NLS-1$
    public static final String IMG_DLCL_PRINT_TOP_VIEW_TAB= "IMG_DLCL_PRINT_TOP_VIEW_TAB"; //$NON-NLS-1$
    
    // enabled local tool images    
    public static final String IMG_ELCL_LOCK= "IMG_ELCL_LOCK"; //$NON-NLS-1$
    public static final String IMG_ELCL_DETAIL_PANE= "IMG_ELCL_DETAIL_PANE"; //$NON-NLS-1$
    public static final String IMG_ELCL_CHANGE_VARIABLE_VALUE= "IMG_ELCL_CHANGE_VARIABLE_VALUE"; //$NON-NLS-1$
    public static final String IMG_ELCL_TYPE_NAMES= "IMG_ELCL_TYPE_NAMES"; //$NON-NLS-1$
    public static final String IMG_ELCL_SHOW_LOGICAL_STRUCTURE= "IMG_ELCL_SHOW_LOGICAL_STRUCTURE"; //$NON-NLS-1$
    public static final String IMG_ELCL_DETAIL_PANE_UNDER= "IMG_ELCL_DETAIL_PANE_UNDER"; //$NON-NLS-1$
    public static final String IMG_ELCL_DETAIL_PANE_RIGHT= "IMG_ELCL_DETAIL_PANE_RIGHT"; //$NON-NLS-1$
    public static final String IMG_ELCL_DETAIL_PANE_HIDE= "IMG_ELCL_DETAIL_PANE_HIDE"; //$NON-NLS-1$
    public static final String IMG_ELCL_COLLAPSE_ALL = "IMG_ELCL_COLLAPSE_ALL"; //$NON-NLS-1$
    public static final String IMG_ELCL_TERMINATE = "IMG_ELCL_TERMINATE"; //$NON-NLS-1$
    public static final String IMG_SRC_LOOKUP_MENU_ELCL = "IMG_SRC_LOOKUP_MENU_ENABLED"; //$NON-NLS-1$
    public static final String IMG_ELCL_MONITOR_EXPRESSION= "IMG_ELCL_MONITOR_EXPRESSION";  //$NON-NLS-1$
    public static final String IMG_ELCL_REMOVE_MEMORY= "IMG_ELCL_REMOVE_MEMORY";  //$NON-NLS-1$
    public static final String IMG_ELCL_RESET_MEMORY= "IMG_ELCL_RESET_MEMORY";  //$NON-NLS-1$
    public static final String IMG_ELCL_COPY_VIEW_TO_CLIPBOARD= "IMG_ELCL_COPY_VIEW_TO_CLIPBOARD";  //$NON-NLS-1$
    public static final String IMG_ELCL_PRINT_TOP_VIEW_TAB= "IMG_ELCL_PRINT_TOP_VIEW_TAB";  //$NON-NLS-1$
    public static final String IMG_ELCL_REMOVE_ALL = "IMG_ELCL_REMOVE_ALL"; //$NON-NLS-1$
    public static final String IMG_ELCL_REMOVE = "IMG_ELCL_REMOVE"; //$NON-NLS-1$
    public static final String IMG_ELCL_HIERARCHICAL = "IMG_ELCL_HIERARCHICAL"; //$NON-NLS-1$
    public static final String IMG_ELCL_HELP = "IMG_ELCL_HELP"; //$NON-NLS-1$
    
    // object images
    public static final String IMG_OBJS_INSTRUCTION_POINTER_TOP = "IMG_OBJS_INSTRUCTION_POINTER_TOP"; //$NON-NLS-1$
    public static final String IMG_OBJS_INSTRUCTION_POINTER = "IMG_OBJS_INSTRUCTION_POINTER"; //$NON-NLS-1$
    public static final String IMG_OBJS_COMMON_TAB = "IMG_OBJS_COMMON_TAB"; //$NON-NLS-1$
    public static final String IMG_OBJS_REFRESH_TAB = "IMG_OBJS_REFRESH_TAB"; //$NON-NLS-1$
    public static final String IMG_OBJS_PERSPECTIVE_TAB = "IMG_OBJS_PERSPECTIVE_TAB"; //$NON-NLS-1$
    public static final String IMG_OBJS_ARRAY_PARTITION = "IMG_OBJS_ARRAY_PARTITION"; //$NON-NLS-1$
    public static final String IMG_SRC_LOOKUP_TAB = "IMG_SRC_LOOKUP_TAB"; //$NON-NLS-1$
    public static final String IMG_ADD_SRC_LOC_WIZ = "IMG_ADD_SRC_LOCATION"; //$NON-NLS-1$
    public static final String IMG_EDIT_SRC_LOC_WIZ = "IMG_EDIT_SRC_LOCATION"; //$NON-NLS-1$
    public static final String IMG_OBJECT_MEMORY_CHANGED="IMG_OBJECT_MEMORY_CHANGED";  //$NON-NLS-1$
    public static final String IMG_OBJECT_MEMORY="IMG_OBJECT_MEMORY";  //$NON-NLS-1$
    public static final String IMG_OBJS_BREAKPOINT_TYPE="IMG_OBJ_BREAKPOINT_TYPE"; //$NON-NLS-1$
    
    // wizard images
    public static final String IMG_WIZBAN_IMPORT_BREAKPOINTS = "IMG_WIZBAN_IMPORT_BREAKPOINTS"; //$NON-NLS-1$
    public static final String IMG_WIZBAN_EXPORT_BREAKPOINTS = "IMG_WIZBAN_EXPORT_BREAKPOINTS"; //$NON-NLS-1$
    
    // internal preferenes
    /**
     * XML for perspective settings - see PerspectiveManager.
     * @since 3.0
     */
    public static final String PREF_LAUNCH_PERSPECTIVES = IDebugUIConstants.PLUGIN_ID + ".PREF_LAUNCH_PERSPECTIVES"; //$NON-NLS-1$
    
    /**
     * Preference for using step filters.
     * 
     * @since 3.0
     */
    public static final String PREF_USE_STEP_FILTERS = IDebugUIConstants.PLUGIN_ID + ".USE_STEP_FILTERS"; //$NON-NLS-1$ 
     
    /** Transparent overlay image identifier. */
    public static final String IMG_OVR_TRANSPARENT = "IMG_OVR_TRANSPARENT";  //$NON-NLS-1$
    
    /**
     * Editor Id for the "Source Not Found" editor
     */
    public static final String ID_SOURCE_NOT_FOUND_EDITOR = "org.eclipse.debug.ui.NoSourceFoundEditor"; //$NON-NLS-1$
    
    /**
     * Editor Id for the "Common Source Not Found" editor.
     * @since 3.0
     */
    public static final String ID_COMMON_SOURCE_NOT_FOUND_EDITOR="org.eclipse.debug.ui.sourcelookup.CommonSourceNotFoundEditor"; //$NON-NLS-1$
        
    /**
     * The name of the font to use for detail panes. This font is managed via
     * the workbench font preference page.
     * 
     * @since 2.1
     */ 
    public static final String DETAIL_PANE_FONT= "org.eclipse.debug.ui.DetailPaneFont"; //$NON-NLS-1$   

    /**
     * String preference controlling whether editors are saved before launching.
     * Valid values are either "always", "never", or "prompt".
     * If "always" or "never", launching will save editors (or not) automatically.
     * If "prompt", the user will be prompted each time.
     * 
     * @since 3.0
     */
    public static final String PREF_SAVE_DIRTY_EDITORS_BEFORE_LAUNCH= IDebugUIConstants.PLUGIN_ID + ".save_dirty_editors_before_launch"; //$NON-NLS-1$

    /**
     * Preference specifiying that all launches should be DEBUG_MODE if breakpoints exist in the workspace
     * @since 3.0
     */
    public static final String PREF_RELAUNCH_IN_DEBUG_MODE = IDebugUIConstants.PLUGIN_ID + ".relaunch_in_debug_mode"; //$NON-NLS-1$

    /**
     * Preference specifying that launches should continue when compile errors exist.
     * @since 3.0
     */
    public static final String PREF_CONTINUE_WITH_COMPILE_ERROR = IDebugUIConstants.PLUGIN_ID + ".cancel_launch_with_compile_errors"; //$NON-NLS-1$ 

    /**
     * Boolean preference controlling whether the debugger will force activate the debug
     * view when a breakpoint is hit.
     * 
     * @since 3.0
     */
    public static final String PREF_ACTIVATE_DEBUG_VIEW= IDebugUIConstants.PLUGIN_ID + ".activate_debug_view"; //$NON-NLS-1$

    /**
     * String preference controlling whether the debugger switching to
     * the associated perspective when launching.
     * Valid values are either "always", "never", or "prompt".
     * If "always" or "never", launching will switch perspectives (or not) automatically.
     * If "prompt", the user will be prompted each time.
     * 
     * @since 3.0
     */
    public static final String PREF_SWITCH_TO_PERSPECTIVE= IDebugUIConstants.PLUGIN_ID + ".switch_to_perspective"; //$NON-NLS-1$

    /**
     * String preference controlling whether the debugger switching to
     * the associated perspective when a launch suspends.
     * Valid values are either "always", "never", or "prompt".
     * If "always" or "never", suspension will switch perspectives (or not) automatically.
     * If "prompt", the user will be prompted each time.
     * 
     * @since 3.0
     */
    public static final String PREF_SWITCH_PERSPECTIVE_ON_SUSPEND= IDebugUIConstants.PLUGIN_ID + ".switch_perspective_on_suspend"; //$NON-NLS-1$

    /**
     * String preference controlling whether the debugger waits for a currently
     * pending (running or waiting) background build to complete before launching.
     * Valid values are either "always", "never", or "prompt".
     * If "always" or "never", launching will wait for builds to finish (or not) automatically.
     * If "prompt", the user will be prompted each time.
     * 
     * @since 3.0
     */
    public static final String PREF_WAIT_FOR_BUILD= IDebugUIConstants.PLUGIN_ID + ".wait_for_build"; //$NON-NLS-1$
    
    /**
     * Font for Memory View
     * 
     * @since 3.0
     */
    public final static String FONT_NAME = IDebugUIConstants.PLUGIN_ID + ".MemoryViewTableFont"; //$NON-NLS-1$
    
    public final static int ADD_UNIT_PER_LINE = 16; // number of addressable unit per line
    public final static int CHAR_PER_BYTE = 2;      // number of characters to represent one byte

    /**
     * Memory view identifier (value <code>"org.eclipse.debug.ui.MemoryView"</code>).
     * @since 3.0
     */
    public static String ID_MEMORY_VIEW = "org.eclipse.debug.ui.MemoryView";  //$NON-NLS-1$
        
    /**
     * Memory view's rendering view pane identifier (value <code>"org.eclipse.debug.ui.MemoryView.RenderingViewPane"</code>).
     * @since 3.1
     */
    public static String ID_RENDERING_VIEW_PANE = RenderingViewPane.RENDERING_VIEW_PANE_ID;
    
    /**
     * Memory view's rendering view pane identifier for the rendering view pane
     * on the left. (value <code>"org.eclipse.debug.ui.MemoryView.RenderingViewPane.1"</code>).
     * @since 3.1
     */
    public static String ID_RENDERING_VIEW_PANE_1 = ID_RENDERING_VIEW_PANE + ".1"; //$NON-NLS-1$
    
    /**
     * Memory view's rendering view pane identifier for the rendering view pane
     * on the right. (value <code>"org.eclipse.debug.ui.MemoryView.RenderingViewPane.2"</code>).
     * @since 3.1
     */
    public static String ID_RENDERING_VIEW_PANE_2 = ID_RENDERING_VIEW_PANE + ".2"; //$NON-NLS-1$
    
    /**
     * Memory view's memory block tree viewer's identifier. (value <code>"org.eclipse.debug.ui.MemoryView.MemoryBlocksTreeViewPane"</code>).
     * @since 3.1
     */
    public static String ID_MEMORY_BLOCK_TREE_VIEWER = MemoryBlocksTreeViewPane.PANE_ID;

    /** 
     * Memory view image identifier
     * 
     * @since 3.0
     * */
    public static final String IMG_CVIEW_MEMORY_VIEW= "IMG_CVIEW_MEMORY_VIEW";  //$NON-NLS-1$

    /**
     * Boolean preference controlling whether the debug view tracks
     * views as the user manually opens/closes them for the purpose
     * of not automatically opening/closing such views.
     * 
     * @since 3.0
     */
    public static final String PREF_TRACK_VIEWS= IDebugUIConstants.PLUGIN_ID + ".track_views"; //$NON-NLS-1$

    /**
     * Preference storing memento for the default breakpoint working set in
     * the breakpoints view.
     * 
     * @since 3.1
     */
    public static final String MEMENTO_BREAKPOINT_WORKING_SET_NAME = DebugUIPlugin.getUniqueIdentifier() + ".MEMENTO_BREAKPOINT_WORKING_SET_NAME"; //$NON-NLS-1$

    /**
     * Breakpoint working set identifier.
     * 
     * @since 3.1
     */
    public static final String ID_BREAKPOINT_WORKINGSET = "org.eclipse.debug.ui.breakpointWorkingSet"; //$NON-NLS-1$
    
    
    /**
     * Address at the beginning of a page in a table rendering.
     * @since 3.1
     */
    public static final String PROPERTY_PAGE_START_ADDRESS = "pageStart"; //$NON-NLS-1$

    
    /**
     * This constant is used as a "quick-fix" for the issue of breakpoint to working set
     * persistance when the state of a project changes.
     * 
     * @since 3.2
     */
    public static final String WORKING_SET_NAME = "workingset_name"; //$NON-NLS-1$
    
    /**
     * This constant is used as a "quick-fix" for the issue of breakpoint to working set
     * persistance when the state of a project changes.
     * 
     * @since 3.2
     */
    public static final String WORKING_SET_ID = "workingset_id";  //$NON-NLS-1$
}
