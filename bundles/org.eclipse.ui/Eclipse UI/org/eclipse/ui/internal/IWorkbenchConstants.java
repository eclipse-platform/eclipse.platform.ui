package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.ui.PlatformUI;

/**
 * General constants used by the workbench.
 */
public interface IWorkbenchConstants {
	// Plug-in id to match the id in the plugin.xml file
	public static final String PLUGIN_ID = PlatformUI.PLUGIN_ID;

	// Workbench Extension Point Names
	public static final String PL_ACTION_SETS = "actionSets";
	public static final String PL_VIEW_ACTIONS = "viewActions";
	public static final String PL_EDITOR_ACTIONS = "editorActions";
	public static final String PL_PERSPECTIVES ="perspectives";
	public static final String PL_PERSPECTIVE_EXTENSIONS ="perspectiveExtensions";
	public static final String PL_PREFERENCES ="preferencePages";
	public static final String PL_PROPERTY_PAGES ="propertyPages";
	public static final String PL_EDITOR ="editors";
	public static final String PL_VIEWS ="views";
	public static final String PL_POPUP_MENU ="popupMenus";
	public static final String PL_IMPORT ="importWizards";
	public static final String PL_EXPORT ="exportWizards";
	public static final String PL_NEW ="newWizards";
	public static final String PL_ELEMENT_FACTORY ="elementFactories";
	public static final String PL_DROP_ACTIONS ="dropActions";
	public static final String PL_MARKER_IMAGE_PROVIDER ="markerImageProviders";
	
	//mappings for type/extension to an editor
	public final static String EDITOR_FILE_NAME = "editors.xml";
	public final static String RESOURCE_TYPE_FILE_NAME = "resourcetypes.xml";

	// Filename containing the workbench's preferences 
	public static final String PREFERENCE_BUNDLE_FILE_NAME = "workbench.ini";

	// Identifier for visible view parts. 
	public static final String WORKBENCH_VISIBLE_VIEW_ID = "Workbench.visibleViewID"; 

	// String to show in preference dialog as root node of workbench preferences
	public static final String WORKBENCH_PREFERENCE_CATEGORY_ID = PLUGIN_ID + ".preferencePages.Workbench";

	// Identifier of workbench info properties page
	public static final String WORKBENCH_PROPERTIES_PAGE_INFO = PLUGIN_ID + ".propertypages.info.file";
	
	// Default layout.
	public static final String DEFAULT_LAYOUT_ID = PLUGIN_ID + ".resourcePerspective";      

	// Various editor.
	public static final String DEFAULT_EDITOR_ID = PLUGIN_ID + ".DefaultTextEditor";
	public static final String OLE_EDITOR_ID = PLUGIN_ID + ".OleEditor";
	public static final String SYSTEM_EDITOR_ID = PLUGIN_ID + ".SystemEditor";

	// Default view category.
	public static final String DEFAULT_CATEGORY_ID = PLUGIN_ID;

	// Persistance tags.
	public static final String TAG_ID = "id";
	public static final String TAG_FOCUS = "focus";
	public static final String TAG_EDITOR = "editor";
	public static final String TAG_EDITORS = "editors";
	public static final String TAG_WORKBOOK = "workbook";
	public static final String TAG_ACTIVE_WORKBOOK = "activeWorkbook";
	public static final String TAG_AREA = "editorArea";
	public static final String TAG_AREA_VISIBLE = "editorAreaVisible";
	public static final String TAG_INPUT = "input";
	public static final String TAG_FACTORY_ID = "factoryID";
	public static final String TAG_TITLE = "title";
	public static final String TAG_X = "x";
	public static final String TAG_Y = "y";
	public static final String TAG_WIDTH = "width";
	public static final String TAG_HEIGHT = "height";
	public static final String TAG_FOLDER = "folder";
	public static final String TAG_INFO = "info";
	public static final String TAG_PART = "part";
	public static final String TAG_RELATIVE = "relative";
	public static final String TAG_RELATIONSHIP = "relationship";
	public static final String TAG_RATIO = "ratio";
	public static final String TAG_ACTIVE_PAGE_ID = "activePageID";
	public static final String TAG_PAGE = "page";
	public static final String TAG_LABEL = "label";
	public static final String TAG_CONTENT = "content";
	public static final String TAG_CLASS = "class";
	public static final String TAG_FILE = "file";
	public static final String TAG_DESCRIPTOR = "descriptor";
	public static final String TAG_MAIN_WINDOW = "mainWindow";
	public static final String TAG_DETACHED_WINDOW = "detachedWindow";
	public static final String TAG_HIDDEN_WINDOW = "hiddenWindow";
	public static final String TAG_WORKBENCH = "workbench";
	public static final String TAG_WINDOW = "window";
	public static final String TAG_VERSION = "version";
	public static final String TAG_PERSPECTIVES = "perspectives";
	public static final String TAG_PERSPECTIVE = "perspective";
	public static final String TAG_ACTIVE_PERSPECTIVE = "activePerspective";
	public static final String TAG_ACTIVE_PART = "activePart";
	public static final String TAG_ACTION_SET = "actionSet";
	public static final String TAG_SHOW_VIEW_ACTION = "show_view_action";
	public static final String TAG_NEW_WIZARD_ACTION = "new_wizard_action";
	public static final String TAG_PERSPECTIVE_ACTION = "perspective_action";
	public static final String TAG_VIEW = "view";
	public static final String TAG_LAYOUT = "layout";
	public static final String TAG_EXTENSION = "extension";
	public static final String TAG_NAME = "name";
	public static final String TAG_IMAGE = "image";
	public static final String TAG_LAUNCHER = "launcher";
	public static final String TAG_PLUGING = "plugin";
	public static final String TAG_INTERNAL = "internal";
	public static final String TAG_OPEN_IN_PLACE = "open_in_place";
	public static final String TAG_PROGRAM_NAME = "program_name";
	public static final String TAG_FAST_VIEWS = "fastViews";
}
