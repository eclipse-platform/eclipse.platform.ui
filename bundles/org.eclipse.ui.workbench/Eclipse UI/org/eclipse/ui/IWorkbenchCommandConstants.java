/*******************************************************************************
 * Copyright (c) 2009-2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc R. Hoffmann <hoffmann@mountainminds.com> 
 *         - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui;

/**
 * Constants for all commands defined by the Eclipse workbench.
 * 
 * @since 3.5
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IWorkbenchCommandConstants {

    // File Category:

    /**
     * Id for command "New" in category "File"
     * (value is <code>"org.eclipse.ui.newWizard"</code>).
     */
    public static final String FILE_NEW = "org.eclipse.ui.newWizard"; //$NON-NLS-1$

	/**
	 * Id for parameter "New Wizard" in command "New" in category "File" (value
	 * is <code>"newWizardId"</code>). Optional.
	 * 
	 * @since 3.6
	 */
	public static final String FILE_NEW_PARM_WIZARDID = "newWizardId"; //$NON-NLS-1$

	/**
	 * Id for command "Close" in category "File" (value is
	 * <code>"org.eclipse.ui.file.close"</code>).
	 */
    public static final String FILE_CLOSE = "org.eclipse.ui.file.close"; //$NON-NLS-1$

    /**
     * Id for command "Close All" in category "File"
     * (value is <code>"org.eclipse.ui.file.closeAll"</code>).
     */
    public static final String FILE_CLOSE_ALL = "org.eclipse.ui.file.closeAll"; //$NON-NLS-1$

    /**
     * Id for command "Import" in category "File"
     * (value is <code>"org.eclipse.ui.file.import"</code>).
     */
    public static final String FILE_IMPORT = "org.eclipse.ui.file.import"; //$NON-NLS-1$

	/**
	 * Id for parameter "Import Wizard" in command "Import" in category "File"
	 * (value is <code>"importWizardId"</code>). Optional.
	 * 
	 * @since 3.6
	 */
	public static final String FILE_IMPORT_PARM_WIZARDID = "importWizardId"; //$NON-NLS-1$

	/**
	 * Id for command "Export" in category "File" (value is
	 * <code>"org.eclipse.ui.file.export"</code>).
	 */
    public static final String FILE_EXPORT = "org.eclipse.ui.file.export"; //$NON-NLS-1$

	/**
	 * Id for parameter "Export Wizard" in command "Export" in category "File"
	 * (value is <code>"exportWizardId"</code>). Optional.
	 * 
	 * @since 3.6
	 */
	public static final String FILE_EXPORT_PARM_WIZARDID = "exportWizardId"; //$NON-NLS-1$

    /**
     * Id for command "Save" in category "File"
     * (value is <code>"org.eclipse.ui.file.save"</code>).
     */
    public static final String FILE_SAVE = "org.eclipse.ui.file.save"; //$NON-NLS-1$

    /**
     * Id for command "Save As" in category "File"
     * (value is <code>"org.eclipse.ui.file.saveAs"</code>).
     */
    public static final String FILE_SAVE_AS = "org.eclipse.ui.file.saveAs"; //$NON-NLS-1$

    /**
     * Id for command "Save All" in category "File"
     * (value is <code>"org.eclipse.ui.file.saveAll"</code>).
     */
    public static final String FILE_SAVE_ALL = "org.eclipse.ui.file.saveAll"; //$NON-NLS-1$

    /**
     * Id for command "Print" in category "File"
     * (value is <code>"org.eclipse.ui.file.print"</code>).
     */
    public static final String FILE_PRINT = "org.eclipse.ui.file.print"; //$NON-NLS-1$

    /**
     * Id for command "Revert" in category "File"
     * (value is <code>"org.eclipse.ui.file.revert"</code>).
     */
    public static final String FILE_REVERT = "org.eclipse.ui.file.revert"; //$NON-NLS-1$

    /**
     * Id for command "Restart" in category "File"
     * (value is <code>"org.eclipse.ui.file.restartWorkbench"</code>).
     */
    public static final String FILE_RESTART = "org.eclipse.ui.file.restartWorkbench"; //$NON-NLS-1$

    /**
     * Id for command "Refresh" in category "File"
     * (value is <code>"org.eclipse.ui.file.refresh"</code>).
     */
    public static final String FILE_REFRESH = "org.eclipse.ui.file.refresh"; //$NON-NLS-1$

    /**
     * Id for command "Properties" in category "File"
     * (value is <code>"org.eclipse.ui.file.properties"</code>).
     */
    public static final String FILE_PROPERTIES = "org.eclipse.ui.file.properties"; //$NON-NLS-1$

    /**
     * Id for command "Exit" in category "File"
     * (value is <code>"org.eclipse.ui.file.exit"</code>).
     */
    public static final String FILE_EXIT = "org.eclipse.ui.file.exit"; //$NON-NLS-1$

    /**
     * Id for command "Move" in category "File"
     * (value is <code>"org.eclipse.ui.edit.move"</code>).
     */
    public static final String FILE_MOVE = "org.eclipse.ui.edit.move"; //$NON-NLS-1$

    /**
     * Id for command "Rename" in category "File"
     * (value is <code>"org.eclipse.ui.edit.rename"</code>).
     */
    public static final String FILE_RENAME = "org.eclipse.ui.edit.rename"; //$NON-NLS-1$

    /**
     * Id for command "Close Others" in category "File"
     * (value is <code>"org.eclipse.ui.file.closeOthers"</code>).
     */
    public static final String FILE_CLOSE_OTHERS = "org.eclipse.ui.file.closeOthers"; //$NON-NLS-1$

    // Edit Category:

    /**
     * Id for command "Undo" in category "Edit"
     * (value is <code>"org.eclipse.ui.edit.undo"</code>).
     */
    public static final String EDIT_UNDO = "org.eclipse.ui.edit.undo"; //$NON-NLS-1$

    /**
     * Id for command "Redo" in category "Edit"
     * (value is <code>"org.eclipse.ui.edit.redo"</code>).
     */
    public static final String EDIT_REDO = "org.eclipse.ui.edit.redo"; //$NON-NLS-1$

    /**
     * Id for command "Cut" in category "Edit"
     * (value is <code>"org.eclipse.ui.edit.cut"</code>).
     */
    public static final String EDIT_CUT = "org.eclipse.ui.edit.cut"; //$NON-NLS-1$

    /**
     * Id for command "Copy" in category "Edit"
     * (value is <code>"org.eclipse.ui.edit.copy"</code>).
     */
    public static final String EDIT_COPY = "org.eclipse.ui.edit.copy"; //$NON-NLS-1$

    /**
     * Id for command "Paste" in category "Edit"
     * (value is <code>"org.eclipse.ui.edit.paste"</code>).
     */
    public static final String EDIT_PASTE = "org.eclipse.ui.edit.paste"; //$NON-NLS-1$

    /**
     * Id for command "Delete" in category "Edit"
     * (value is <code>"org.eclipse.ui.edit.delete"</code>).
     */
    public static final String EDIT_DELETE = "org.eclipse.ui.edit.delete"; //$NON-NLS-1$

    /**
     * Id for command "Content Assist" in category "Edit"
     * (value is <code>"org.eclipse.ui.edit.text.contentAssist.proposals"</code>).
     */
    public static final String EDIT_CONTENT_ASSIST = "org.eclipse.ui.edit.text.contentAssist.proposals"; //$NON-NLS-1$

    /**
     * Id for command "Context Information" in category "Edit"
     * (value is <code>"org.eclipse.ui.edit.text.contentAssist.contextInformation"</code>).
     */
    public static final String EDIT_CONTEXT_INFORMATION = "org.eclipse.ui.edit.text.contentAssist.contextInformation"; //$NON-NLS-1$

    /**
     * Id for command "Select All" in category "Edit"
     * (value is <code>"org.eclipse.ui.edit.selectAll"</code>).
     */
    public static final String EDIT_SELECT_ALL = "org.eclipse.ui.edit.selectAll"; //$NON-NLS-1$

    /**
     * Id for command "Find and Replace" in category "Edit"
     * (value is <code>"org.eclipse.ui.edit.findReplace"</code>).
     */
    public static final String EDIT_FIND_AND_REPLACE = "org.eclipse.ui.edit.findReplace"; //$NON-NLS-1$

    /**
     * Id for command "Add Task" in category "Edit".
     * (value is <code>"org.eclipse.ui.edit.addTask"</code>).
     */
    public static final String EDIT_ADD_TASK = "org.eclipse.ui.edit.addTask"; //$NON-NLS-1$

    /**
     * Id for command "Add Bookmark" in category "Edit"
     * (value is <code>"org.eclipse.ui.edit.addBookmark"</code>).
     */
    public static final String EDIT_ADD_BOOKMARK = "org.eclipse.ui.edit.addBookmark"; //$NON-NLS-1$

    // Navigate Category:

    /**
     * Id for command "Go Into" in category "Navigate"
     * (value is <code>"org.eclipse.ui.navigate.goInto"</code>).
     */
    public static final String NAVIGATE_GO_INTO = "org.eclipse.ui.navigate.goInto"; //$NON-NLS-1$

    /**
     * Id for command "Back" in category "Navigate"
     * (value is <code>"org.eclipse.ui.navigate.back"</code>).
     */
    public static final String NAVIGATE_BACK = "org.eclipse.ui.navigate.back"; //$NON-NLS-1$

    /**
     * Id for command "Forward" in category "Navigate"
     * (value is <code>"org.eclipse.ui.navigate.forward"</code>).
     */
    public static final String NAVIGATE_FORWARD = "org.eclipse.ui.navigate.forward"; //$NON-NLS-1$

    /**
     * Id for command "Up" in category "Navigate"
     * (value is <code>"org.eclipse.ui.navigate.up"</code>).
     */
    public static final String NAVIGATE_UP = "org.eclipse.ui.navigate.up"; //$NON-NLS-1$

    /**
     * Id for command "Next" in category "Navigate"
     * (value is <code>"org.eclipse.ui.navigate.next"</code>).
     */
    public static final String NAVIGATE_NEXT = "org.eclipse.ui.navigate.next"; //$NON-NLS-1$

    /**
     * Id for command "Backward History" in category "Navigate"
     * (value is <code>"org.eclipse.ui.navigate.backwardHistory"</code>).
     */
    public static final String NAVIGATE_BACKWARD_HISTORY = "org.eclipse.ui.navigate.backwardHistory"; //$NON-NLS-1$

    /**
     * Id for command "Forward History" in category "Navigate"
     * (value is <code>"org.eclipse.ui.navigate.forwardHistory"</code>).
     */
    public static final String NAVIGATE_FORWARD_HISTORY = "org.eclipse.ui.navigate.forwardHistory"; //$NON-NLS-1$

    /**
     * Id for command "Previous" in category "Navigate"
     * (value is <code>"org.eclipse.ui.navigate.previous"</code>).
     */
    public static final String NAVIGATE_PREVIOUS = "org.eclipse.ui.navigate.previous"; //$NON-NLS-1$

    /**
     * Id for command "Toggle Link with Editor " in category "Navigate"
     * (value is <code>"org.eclipse.ui.navigate.linkWithEditor"</code>).
     */
    public static final String NAVIGATE_TOGGLE_LINK_WITH_EDITOR = "org.eclipse.ui.navigate.linkWithEditor"; //$NON-NLS-1$

    /**
     * Id for command "Next Page" in category "Navigate"
     * (value is <code>"org.eclipse.ui.part.nextPage"</code>).
     */
    public static final String NAVIGATE_NEXT_PAGE = "org.eclipse.ui.part.nextPage"; //$NON-NLS-1$

    /**
     * Id for command "Previous Page" in category "Navigate"
     * (value is <code>"org.eclipse.ui.part.previousPage"</code>).
     */
    public static final String NAVIGATE_PREVIOUS_PAGE = "org.eclipse.ui.part.previousPage"; //$NON-NLS-1$

    /**
     * Id for command "Collapse All" in category "Navigate"
     * (value is <code>"org.eclipse.ui.navigate.collapseAll"</code>).
     */
    public static final String NAVIGATE_COLLAPSE_ALL = "org.eclipse.ui.navigate.collapseAll"; //$NON-NLS-1$

	/**
	 * Id for command "Expand All" in category "Navigate" (value is
	 * <code>"org.eclipse.ui.navigate.expandAll"</code>).
	 * 
	 * @since 3.6
	 */
	public static final String NAVIGATE_EXPAND_ALL = "org.eclipse.ui.navigate.expandAll"; //$NON-NLS-1$

    /**
     * Id for command "Show In" in category "Navigate"
     * (value is <code>"org.eclipse.ui.navigate.showIn"</code>).
     */
    public static final String NAVIGATE_SHOW_IN = "org.eclipse.ui.navigate.showIn"; //$NON-NLS-1$

	/**
	 * Id for parameter "Target Id" in command "Show In" in category "Navigate"
	 * (value is <code>"org.eclipse.ui.navigate.showIn.targetId"</code>).
	 * Required.
	 * 
	 * @since 3.6
	 */
	public static final String NAVIGATE_SHOW_IN_PARM_TARGET = "org.eclipse.ui.navigate.showIn.targetId"; //$NON-NLS-1$

	/**
	 * Id for command "Show In" in category "Navigate" (value is
	 * <code>"org.eclipse.ui.navigate.showInQuickMenu"</code>).
	 */
    public static final String NAVIGATE_SHOW_IN_QUICK_MENU = "org.eclipse.ui.navigate.showInQuickMenu"; //$NON-NLS-1$

    // project category
    
    /**
     * Id for command "Build All" in category "Project".
     * (value is <code>"org.eclipse.ui.project.buildAll"</code>).
     */
    public static final String PROJECT_BUILD_ALL = "org.eclipse.ui.project.buildAll"; //$NON-NLS-1$

    /**
     * Id for command "Build Project" in category "Project".
     * (value is <code>"org.eclipse.ui.project.buildProject"</code>).
     */
    public static final String PROJECT_BUILD_PROJECT = "org.eclipse.ui.project.buildProject"; //$NON-NLS-1$

    /**
     * Id for command "Close Project" in category "Project".
     * (value is <code>"org.eclipse.ui.project.closeProject"</code>).
     */
    public static final String PROJECT_CLOSE_PROJECT = "org.eclipse.ui.project.closeProject"; //$NON-NLS-1$

    /**
     * Id for command "Close Unrelated Projects" in category "Project".
     * (value is <code>"org.eclipse.ui.project.closeUnrelatedProjects"</code>).
     */
    public static final String PROJECT_CLOSE_UNRELATED_PROJECTS = "org.eclipse.ui.project.closeUnrelatedProjects"; //$NON-NLS-1$

    /**
     * Id for command "Open Project" in category "Project".
     * (value is <code>"org.eclipse.ui.project.openProject"</code>).
     */
    public static final String PROJECT_OPEN_PROJECT = "org.eclipse.ui.project.openProject"; //$NON-NLS-1$

    // Window Category:

    /**
     * Id for command "New Window" in category "Window"
     * (value is <code>"org.eclipse.ui.window.newWindow"</code>).
     */
    public static final String WINDOW_NEW_WINDOW = "org.eclipse.ui.window.newWindow"; //$NON-NLS-1$

    /**
     * Id for command "New Editor" in category "Window"
     * (value is <code>"org.eclipse.ui.window.newEditor"</code>).
     */
    public static final String WINDOW_NEW_EDITOR = "org.eclipse.ui.window.newEditor"; //$NON-NLS-1$

    /**
     * Id for command "Show View Menu" in category "Window"
     * (value is <code>"org.eclipse.ui.window.showViewMenu"</code>).
     */
    public static final String WINDOW_SHOW_VIEW_MENU = "org.eclipse.ui.window.showViewMenu"; //$NON-NLS-1$

    /**
     * Id for command "Activate Editor" in category "Window"
     * (value is <code>"org.eclipse.ui.window.activateEditor"</code>).
     */
    public static final String WINDOW_ACTIVATE_EDITOR = "org.eclipse.ui.window.activateEditor"; //$NON-NLS-1$

    /**
     * Id for command "Maximize Active View or Editor" in category "Window"
     * (value is <code>"org.eclipse.ui.window.maximizePart"</code>).
     */
    public static final String WINDOW_MAXIMIZE_ACTIVE_VIEW_OR_EDITOR = "org.eclipse.ui.window.maximizePart"; //$NON-NLS-1$

    /**
     * Id for command "Minimize Active View or Editor" in category "Window"
     * (value is <code>"org.eclipse.ui.window.minimizePart"</code>).
     */
    public static final String WINDOW_MINIMIZE_ACTIVE_VIEW_OR_EDITOR = "org.eclipse.ui.window.minimizePart"; //$NON-NLS-1$

    /**
     * Id for command "Next Editor" in category "Window"
     * (value is <code>"org.eclipse.ui.window.nextEditor"</code>).
     */
    public static final String WINDOW_NEXT_EDITOR = "org.eclipse.ui.window.nextEditor"; //$NON-NLS-1$

    /**
     * Id for command "Previous Editor" in category "Window"
     * (value is <code>"org.eclipse.ui.window.previousEditor"</code>).
     */
    public static final String WINDOW_PREVIOUS_EDITOR = "org.eclipse.ui.window.previousEditor"; //$NON-NLS-1$

    /**
     * Id for command "Next View" in category "Window"
     * (value is <code>"org.eclipse.ui.window.nextView"</code>).
     */
    public static final String WINDOW_NEXT_VIEW = "org.eclipse.ui.window.nextView"; //$NON-NLS-1$

    /**
     * Id for command "Previous View" in category "Window"
     * (value is <code>"org.eclipse.ui.window.previousView"</code>).
     */
    public static final String WINDOW_PREVIOUS_VIEW = "org.eclipse.ui.window.previousView"; //$NON-NLS-1$

    /**
     * Id for command "Next Perspective" in category "Window"
     * (value is <code>"org.eclipse.ui.window.nextPerspective"</code>).
     */
    public static final String WINDOW_NEXT_PERSPECTIVE = "org.eclipse.ui.window.nextPerspective"; //$NON-NLS-1$

    /**
     * Id for command "Previous Perspective" in category "Window"
     * (value is <code>"org.eclipse.ui.window.previousPerspective"</code>).
     */
    public static final String WINDOW_PREVIOUS_PERSPECTIVE = "org.eclipse.ui.window.previousPerspective"; //$NON-NLS-1$

    /**
     * Id for command "Close All Perspectives" in category "Window"
     * (value is <code>"org.eclipse.ui.window.closeAllPerspectives"</code>).
     */
    public static final String WINDOW_CLOSE_ALL_PERSPECTIVES = "org.eclipse.ui.window.closeAllPerspectives"; //$NON-NLS-1$

    /**
     * Id for command "Close Perspective" in category "Window"
     * (value is <code>"org.eclipse.ui.window.closePerspective"</code>).
     */
    public static final String WINDOW_CLOSE_PERSPECTIVE = "org.eclipse.ui.window.closePerspective"; //$NON-NLS-1$

	/**
	 * Id for parameter "Perspective Id" in command "Close Perspective" in
	 * category "Window" (value is
	 * <code>"org.eclipse.ui.window.closePerspective.perspectiveId"</code>).
	 * Optional.
	 * 
	 * @since 3.6
	 */
	public static final String WINDOW_CLOSE_PERSPECTIVE_PARM_ID = "org.eclipse.ui.window.closePerspective.perspectiveId"; //$NON-NLS-1$

	/**
	 * Id for command "Close Part" in category "Window" (value is
	 * <code>"org.eclipse.ui.file.closePart"</code>).
	 */
    public static final String WINDOW_CLOSE_PART = "org.eclipse.ui.file.closePart"; //$NON-NLS-1$

    /**
     * Id for command "Customize Perspective" in category "Window"
     * (value is <code>"org.eclipse.ui.window.customizePerspective"</code>).
     */
    public static final String WINDOW_CUSTOMIZE_PERSPECTIVE = "org.eclipse.ui.window.customizePerspective"; //$NON-NLS-1$

    /**
     * Id for command "Pin Editor" in category "Window"
     * (value is <code>"org.eclipse.ui.window.pinEditor"</code>).
     */
    public static final String WINDOW_PIN_EDITOR = "org.eclipse.ui.window.pinEditor"; //$NON-NLS-1$

    /**
     * Id for command "Preferences" in category "Window"
     * (value is <code>"org.eclipse.ui.window.preferences"</code>).
     */
    public static final String WINDOW_PREFERENCES = "org.eclipse.ui.window.preferences"; //$NON-NLS-1$

	/**
	 * Id for parameter "Preference Page Id" in command "Preferences" in
	 * category "Window" (value is <code>"preferencePageId"</code>). Optional.
	 * 
	 * @since 3.6
	 */
	public static final String WINDOW_PREFERENCES_PARM_PAGEID = "preferencePageId"; //$NON-NLS-1$

	/**
	 * Id for command "Reset Perspective" in category "Window" (value is
	 * <code>"org.eclipse.ui.window.resetPerspective"</code>).
	 */
    public static final String WINDOW_RESET_PERSPECTIVE = "org.eclipse.ui.window.resetPerspective"; //$NON-NLS-1$

    /**
     * Id for command "Save Perspective As" in category "Window"
     * (value is <code>"org.eclipse.ui.window.savePerspective"</code>).
     */
    public static final String WINDOW_SAVE_PERSPECTIVE_AS = "org.eclipse.ui.window.savePerspective"; //$NON-NLS-1$

    /**
     * Id for command "Show Key Assist" in category "Window"
     * (value is <code>"org.eclipse.ui.window.showKeyAssist"</code>).
     */
    public static final String WINDOW_SHOW_KEY_ASSIST = "org.eclipse.ui.window.showKeyAssist"; //$NON-NLS-1$

	/**
	 * Id for command "Lock Toolbar" in category "Window" (value is
	 * <code>"org.eclipse.ui.window.lockToolbar"</code>).
	 * 
	 * @since 3.7
	 */
	public static final String WINDOW_LOCK_TOOLBAR = "org.eclipse.ui.window.lockToolBar"; //$NON-NLS-1$

    // Help Category:

    /**
     * Id for command "Help Contents" in category "Help"
     * (value is <code>"org.eclipse.ui.help.helpContents"</code>).
     */
    public static final String HELP_HELP_CONTENTS = "org.eclipse.ui.help.helpContents"; //$NON-NLS-1$

    /**
     * Id for command "Help Search" in category "Help"
     * (value is <code>"org.eclipse.ui.help.helpSearch"</code>).
     */
    public static final String HELP_HELP_SEARCH = "org.eclipse.ui.help.helpSearch"; //$NON-NLS-1$

    /**
     * Id for command "Dynamic Help" in category "Help"
     * (value is <code>"org.eclipse.ui.help.dynamicHelp"</code>).
     */
    public static final String HELP_DYNAMIC_HELP = "org.eclipse.ui.help.dynamicHelp"; //$NON-NLS-1$

    /**
     * Id for command "Welcome" in category "Help"
     * (value is <code>"org.eclipse.ui.help.quickStartAction"</code>).
     */
    public static final String HELP_WELCOME = "org.eclipse.ui.help.quickStartAction"; //$NON-NLS-1$

    /**
     * Id for command "Tips and Tricks" in category "Help"
     * (value is <code>"org.eclipse.ui.help.tipsAndTricksAction"</code>).
     */
    public static final String HELP_TIPS_AND_TRICKS = "org.eclipse.ui.help.tipsAndTricksAction"; //$NON-NLS-1$

    /**
     * Id for command "About" in category "Help"
     * (value is <code>"org.eclipse.ui.help.aboutAction"</code>).
     */
    public static final String HELP_ABOUT = "org.eclipse.ui.help.aboutAction"; //$NON-NLS-1$

    // Views Category:

    /**
     * Id for command "Show View" in category "Views"
     * (value is <code>"org.eclipse.ui.views.showView"</code>).
     */
    public static final String VIEWS_SHOW_VIEW = "org.eclipse.ui.views.showView"; //$NON-NLS-1$

	/**
	 * Id for parameter "View Id" in command "Show View" in category "Views"
	 * (value is <code>"org.eclipse.ui.views.showView.viewId"</code>).
	 * 
	 * @since 3.6
	 */
	public static final String VIEWS_SHOW_VIEW_PARM_ID = "org.eclipse.ui.views.showView.viewId"; //$NON-NLS-1$

	/**
	 * Id for parameter "Secondary Id" in command "Show View" in category "Views"
	 * (value is <code>"org.eclipse.ui.views.showView.secondaryId"</code>).
	 * 
	 * @since 3.7
	 */
	public static final String VIEWS_SHOW_VIEW_SECONDARY_ID = "org.eclipse.ui.views.showView.secondaryId"; //$NON-NLS-1$

	/**
	 * Id for parameter "As Fastview" in command "Show View" in category "Views"
	 * (value is <code>"org.eclipse.ui.views.showView.makeFast"</code>).
	 * Optional.
	 * 
	 * @since 3.6
	 */
	public static final String VIEWS_SHOW_VIEW_PARM_FASTVIEW = "org.eclipse.ui.views.showView.makeFast"; //$NON-NLS-1$

	// Perspectives Category:

    /**
     * Id for command "Show Perspective" in category "Perspectives"
     * (value is <code>"org.eclipse.ui.perspectives.showPerspective"</code>).
     */
    public static final String PERSPECTIVES_SHOW_PERSPECTIVE = "org.eclipse.ui.perspectives.showPerspective"; //$NON-NLS-1$

	/**
	 * Id for parameter "Perspective Id" in command "Show Perspective" in
	 * category "Perspectives" (value is
	 * <code>"org.eclipse.ui.perspectives.showPerspective.perspectiveId"</code>
	 * ).
	 * 
	 * @since 3.6
	 */
	public static final String PERSPECTIVES_SHOW_PERSPECTIVE_PARM_ID = "org.eclipse.ui.perspectives.showPerspective.perspectiveId"; //$NON-NLS-1$

	/**
	 * Id for parameter "In New Window" in command "Show Perspective" in
	 * category "Perspectives" (value is
	 * <code>"org.eclipse.ui.perspectives.showPerspective.newWindow"</code>).
	 * Optional.
	 * 
	 * @since 3.6
	 */
	public static final String PERSPECTIVES_SHOW_PERSPECTIVE_PARM_NEWWINDOW = "org.eclipse.ui.perspectives.showPerspective.newWindow"; //$NON-NLS-1$

}
