/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
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
 */
public interface IWorkbenchCommandConstants {

    // File Category:

    /**
     * Id for command "New" in category "File"
     * (value is <code>"org.eclipse.ui.newWizard"</code>).
     */
    public static final String FILE_NEW = "org.eclipse.ui.newWizard"; //$NON-NLS-1$

    /**
     * Id for command "Close" in category "File"
     * (value is <code>"org.eclipse.ui.file.close"</code>).
     */
    public static final String FILE_CLOSE = "org.eclipse.ui.file.close"; //$NON-NLS-1$

    /**
     * Id for command "Close All" in category "File"
     * (value is <code>"org.eclipse.ui.file.closeAll"</code>).
     */
    public static final String FILE_CLOSEALL = "org.eclipse.ui.file.closeAll"; //$NON-NLS-1$

    /**
     * Id for command "Import" in category "File"
     * (value is <code>"org.eclipse.ui.file.import"</code>).
     */
    public static final String FILE_IMPORT = "org.eclipse.ui.file.import"; //$NON-NLS-1$

    /**
     * Id for command "Export" in category "File"
     * (value is <code>"org.eclipse.ui.file.export"</code>).
     */
    public static final String FILE_EXPORT = "org.eclipse.ui.file.export"; //$NON-NLS-1$

    /**
     * Id for command "Save" in category "File"
     * (value is <code>"org.eclipse.ui.file.save"</code>).
     */
    public static final String FILE_SAVE = "org.eclipse.ui.file.save"; //$NON-NLS-1$

    /**
     * Id for command "Save As" in category "File"
     * (value is <code>"org.eclipse.ui.file.saveAs"</code>).
     */
    public static final String FILE_SAVEAS = "org.eclipse.ui.file.saveAs"; //$NON-NLS-1$

    /**
     * Id for command "Save All" in category "File"
     * (value is <code>"org.eclipse.ui.file.saveAll"</code>).
     */
    public static final String FILE_SAVEALL = "org.eclipse.ui.file.saveAll"; //$NON-NLS-1$

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
    public static final String FILE_CLOSEOTHERS = "org.eclipse.ui.file.closeOthers"; //$NON-NLS-1$

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
    public static final String EDIT_CONTENTASSIST = "org.eclipse.ui.edit.text.contentAssist.proposals"; //$NON-NLS-1$

    /**
     * Id for command "Context Information" in category "Edit"
     * (value is <code>"org.eclipse.ui.edit.text.contentAssist.contextInformation"</code>).
     */
    public static final String EDIT_CONTEXTINFORMATION = "org.eclipse.ui.edit.text.contentAssist.contextInformation"; //$NON-NLS-1$

    /**
     * Id for command "Select All" in category "Edit"
     * (value is <code>"org.eclipse.ui.edit.selectAll"</code>).
     */
    public static final String EDIT_SELECTALL = "org.eclipse.ui.edit.selectAll"; //$NON-NLS-1$

    /**
     * Id for command "Find and Replace" in category "Edit"
     * (value is <code>"org.eclipse.ui.edit.findReplace"</code>).
     */
    public static final String EDIT_FINDANDREPLACE = "org.eclipse.ui.edit.findReplace"; //$NON-NLS-1$

    /**
     * Id for command "Add Task" in category "Edit".
     * (value is <code>"org.eclipse.ui.edit.addTask"</code>).
     */
    public static final String EDIT_ADDTASK = "org.eclipse.ui.edit.addTask"; //$NON-NLS-1$

    /**
     * Id for command "Add Bookmark" in category "Edit"
     * (value is <code>"org.eclipse.ui.edit.addBookmark"</code>).
     */
    public static final String EDIT_ADDBOOKMARK = "org.eclipse.ui.edit.addBookmark"; //$NON-NLS-1$

    // Navigate Category:

    /**
     * Id for command "Go Into" in category "Navigate"
     * (value is <code>"org.eclipse.ui.navigate.goInto"</code>).
     */
    public static final String NAVIGATE_GOINTO = "org.eclipse.ui.navigate.goInto"; //$NON-NLS-1$

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
    public static final String NAVIGATE_BACKWARDHISTORY = "org.eclipse.ui.navigate.backwardHistory"; //$NON-NLS-1$

    /**
     * Id for command "Forward History" in category "Navigate"
     * (value is <code>"org.eclipse.ui.navigate.forwardHistory"</code>).
     */
    public static final String NAVIGATE_FORWARDHISTORY = "org.eclipse.ui.navigate.forwardHistory"; //$NON-NLS-1$

    /**
     * Id for command "Previous" in category "Navigate"
     * (value is <code>"org.eclipse.ui.navigate.previous"</code>).
     */
    public static final String NAVIGATE_PREVIOUS = "org.eclipse.ui.navigate.previous"; //$NON-NLS-1$

    /**
     * Id for command "Toggle Link with Editor " in category "Navigate"
     * (value is <code>"org.eclipse.ui.navigate.linkWithEditor"</code>).
     */
    public static final String NAVIGATE_TOGGLELINKWITHEDITOR = "org.eclipse.ui.navigate.linkWithEditor"; //$NON-NLS-1$

    /**
     * Id for command "Next Page" in category "Navigate"
     * (value is <code>"org.eclipse.ui.part.nextPage"</code>).
     */
    public static final String NAVIGATE_NEXTPAGE = "org.eclipse.ui.part.nextPage"; //$NON-NLS-1$

    /**
     * Id for command "Previous Page" in category "Navigate"
     * (value is <code>"org.eclipse.ui.part.previousPage"</code>).
     */
    public static final String NAVIGATE_PREVIOUSPAGE = "org.eclipse.ui.part.previousPage"; //$NON-NLS-1$

    /**
     * Id for command "Collapse All" in category "Navigate"
     * (value is <code>"org.eclipse.ui.navigate.collapseAll"</code>).
     */
    public static final String NAVIGATE_COLLAPSEALL = "org.eclipse.ui.navigate.collapseAll"; //$NON-NLS-1$

    /**
     * Id for command "Show In" in category "Navigate"
     * (value is <code>"org.eclipse.ui.navigate.showIn"</code>).
     */
    public static final String NAVIGATE_SHOWIN = "org.eclipse.ui.navigate.showIn"; //$NON-NLS-1$

    /**
     * Id for command "Show In" in category "Navigate"
     * (value is <code>"org.eclipse.ui.navigate.showInQuickMenu"</code>).
     */
    public static final String NAVIGATE_SHOWINQUICKMENU = "org.eclipse.ui.navigate.showInQuickMenu"; //$NON-NLS-1$

    // project category
    
    /**
     * Id for command "Build All" in category "Project".
     * (value is <code>"org.eclipse.ui.project.buildAll"</code>).
     */
    public static final String PROJECT_BUILDALL = "org.eclipse.ui.project.buildAll"; //$NON-NLS-1$

    /**
     * Id for command "Build Project" in category "Project".
     * (value is <code>"org.eclipse.ui.project.buildProject"</code>).
     */
    public static final String PROJECT_BUILDPROJECT = "org.eclipse.ui.project.buildProject"; //$NON-NLS-1$

    /**
     * Id for command "Close Project" in category "Project".
     * (value is <code>"org.eclipse.ui.project.closeProject"</code>).
     */
    public static final String PROJECT_CLOSEPROJECT = "org.eclipse.ui.project.closeProject"; //$NON-NLS-1$

    /**
     * Id for command "Close Unrelated Projects" in category "Project".
     * (value is <code>"org.eclipse.ui.project.closeUnrelatedProjects"</code>).
     */
    public static final String PROJECT_CLOSEUNRELATEDPROJECTS = "org.eclipse.ui.project.closeUnrelatedProjects"; //$NON-NLS-1$

    /**
     * Id for command "Open Project" in category "Project".
     * (value is <code>"org.eclipse.ui.project.openProject"</code>).
     */
    public static final String PROJECT_OPENPROJECT = "org.eclipse.ui.project.openProject"; //$NON-NLS-1$

    // Window Category:

    /**
     * Id for command "New Window" in category "Window"
     * (value is <code>"org.eclipse.ui.window.newWindow"</code>).
     */
    public static final String WINDOW_NEWWINDOW = "org.eclipse.ui.window.newWindow"; //$NON-NLS-1$

    /**
     * Id for command "New Editor" in category "Window"
     * (value is <code>"org.eclipse.ui.window.newEditor"</code>).
     */
    public static final String WINDOW_NEWEDITOR = "org.eclipse.ui.window.newEditor"; //$NON-NLS-1$

    /**
     * Id for command "Show View Menu" in category "Window"
     * (value is <code>"org.eclipse.ui.window.showViewMenu"</code>).
     */
    public static final String WINDOW_SHOWVIEWMENU = "org.eclipse.ui.window.showViewMenu"; //$NON-NLS-1$

    /**
     * Id for command "Activate Editor" in category "Window"
     * (value is <code>"org.eclipse.ui.window.activateEditor"</code>).
     */
    public static final String WINDOW_ACTIVATEEDITOR = "org.eclipse.ui.window.activateEditor"; //$NON-NLS-1$

    /**
     * Id for command "Maximize Active View or Editor" in category "Window"
     * (value is <code>"org.eclipse.ui.window.maximizePart"</code>).
     */
    public static final String WINDOW_MAXIMIZEACTIVEVIEWOREDITOR = "org.eclipse.ui.window.maximizePart"; //$NON-NLS-1$

    /**
     * Id for command "Minimize Active View or Editor" in category "Window"
     * (value is <code>"org.eclipse.ui.window.minimizePart"</code>).
     */
    public static final String WINDOW_MINIMIZEACTIVEVIEWOREDITOR = "org.eclipse.ui.window.minimizePart"; //$NON-NLS-1$

    /**
     * Id for command "Next Editor" in category "Window"
     * (value is <code>"org.eclipse.ui.window.nextEditor"</code>).
     */
    public static final String WINDOW_NEXTEDITOR = "org.eclipse.ui.window.nextEditor"; //$NON-NLS-1$

    /**
     * Id for command "Previous Editor" in category "Window"
     * (value is <code>"org.eclipse.ui.window.previousEditor"</code>).
     */
    public static final String WINDOW_PREVIOUSEDITOR = "org.eclipse.ui.window.previousEditor"; //$NON-NLS-1$

    /**
     * Id for command "Next View" in category "Window"
     * (value is <code>"org.eclipse.ui.window.nextView"</code>).
     */
    public static final String WINDOW_NEXTVIEW = "org.eclipse.ui.window.nextView"; //$NON-NLS-1$

    /**
     * Id for command "Previous View" in category "Window"
     * (value is <code>"org.eclipse.ui.window.previousView"</code>).
     */
    public static final String WINDOW_PREVIOUSVIEW = "org.eclipse.ui.window.previousView"; //$NON-NLS-1$

    /**
     * Id for command "Next Perspective" in category "Window"
     * (value is <code>"org.eclipse.ui.window.nextPerspective"</code>).
     */
    public static final String WINDOW_NEXTPERSPECTIVE = "org.eclipse.ui.window.nextPerspective"; //$NON-NLS-1$

    /**
     * Id for command "Previous Perspective" in category "Window"
     * (value is <code>"org.eclipse.ui.window.previousPerspective"</code>).
     */
    public static final String WINDOW_PREVIOUSPERSPECTIVE = "org.eclipse.ui.window.previousPerspective"; //$NON-NLS-1$

    /**
     * Id for command "Close All Perspectives" in category "Window"
     * (value is <code>"org.eclipse.ui.window.closeAllPerspectives"</code>).
     */
    public static final String WINDOW_CLOSEALLPERSPECTIVES = "org.eclipse.ui.window.closeAllPerspectives"; //$NON-NLS-1$

    /**
     * Id for command "Close Perspective" in category "Window"
     * (value is <code>"org.eclipse.ui.window.closePerspective"</code>).
     */
    public static final String WINDOW_CLOSEPERSPECTIVE = "org.eclipse.ui.window.closePerspective"; //$NON-NLS-1$

    /**
     * Id for command "Close Part" in category "Window"
     * (value is <code>"org.eclipse.ui.file.closePart"</code>).
     */
    public static final String WINDOW_CLOSEPART = "org.eclipse.ui.file.closePart"; //$NON-NLS-1$

    /**
     * Id for command "Customize Perspective" in category "Window"
     * (value is <code>"org.eclipse.ui.window.customizePerspective"</code>).
     */
    public static final String WINDOW_CUSTOMIZEPERSPECTIVE = "org.eclipse.ui.window.customizePerspective"; //$NON-NLS-1$

    /**
     * Id for command "Pin Editor" in category "Window"
     * (value is <code>"org.eclipse.ui.window.pinEditor"</code>).
     */
    public static final String WINDOW_PINEDITOR = "org.eclipse.ui.window.pinEditor"; //$NON-NLS-1$

    /**
     * Id for command "Preferences" in category "Window"
     * (value is <code>"org.eclipse.ui.window.preferences"</code>).
     */
    public static final String WINDOW_PREFERENCES = "org.eclipse.ui.window.preferences"; //$NON-NLS-1$

    /**
     * Id for command "Reset Perspective" in category "Window"
     * (value is <code>"org.eclipse.ui.window.resetPerspective"</code>).
     */
    public static final String WINDOW_RESETPERSPECTIVE = "org.eclipse.ui.window.resetPerspective"; //$NON-NLS-1$

    /**
     * Id for command "Save Perspective As" in category "Window"
     * (value is <code>"org.eclipse.ui.window.savePerspective"</code>).
     */
    public static final String WINDOW_SAVEPERSPECTIVEAS = "org.eclipse.ui.window.savePerspective"; //$NON-NLS-1$

    /**
     * Id for command "Show Key Assist" in category "Window"
     * (value is <code>"org.eclipse.ui.window.showKeyAssist"</code>).
     */
    public static final String WINDOW_SHOWKEYASSIST = "org.eclipse.ui.window.showKeyAssist"; //$NON-NLS-1$

    // Help Category:

    /**
     * Id for command "Help Contents" in category "Help"
     * (value is <code>"org.eclipse.ui.help.helpContents"</code>).
     */
    public static final String HELP_HELPCONTENTS = "org.eclipse.ui.help.helpContents"; //$NON-NLS-1$

    /**
     * Id for command "Help Search" in category "Help"
     * (value is <code>"org.eclipse.ui.help.helpSearch"</code>).
     */
    public static final String HELP_HELPSEARCH = "org.eclipse.ui.help.helpSearch"; //$NON-NLS-1$

    /**
     * Id for command "Dynamic Help" in category "Help"
     * (value is <code>"org.eclipse.ui.help.dynamicHelp"</code>).
     */
    public static final String HELP_DYNAMICHELP = "org.eclipse.ui.help.dynamicHelp"; //$NON-NLS-1$

    /**
     * Id for command "Welcome" in category "Help"
     * (value is <code>"org.eclipse.ui.help.quickStartAction"</code>).
     */
    public static final String HELP_WELCOME = "org.eclipse.ui.help.quickStartAction"; //$NON-NLS-1$

    /**
     * Id for command "Tips and Tricks" in category "Help"
     * (value is <code>"org.eclipse.ui.help.tipsAndTricksAction"</code>).
     */
    public static final String HELP_TIPSANDTRICKS = "org.eclipse.ui.help.tipsAndTricksAction"; //$NON-NLS-1$

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
    public static final String VIEWS_SHOWVIEW = "org.eclipse.ui.views.showView"; //$NON-NLS-1$

    // Perspectives Category:

    /**
     * Id for command "Show Perspective" in category "Perspectives"
     * (value is <code>"org.eclipse.ui.perspectives.showPerspective"</code>).
     */
    public static final String PERSPECTIVES_SHOWPERSPECTIVE = "org.eclipse.ui.perspectives.showPerspective"; //$NON-NLS-1$
	
}
