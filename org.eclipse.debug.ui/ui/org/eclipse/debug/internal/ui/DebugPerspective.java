package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.ui.*;

/**
 * The debug perspective
 */
public class DebugPerspective implements IPerspectiveFactory {
	
	/**
	 * @see IPerspectiveFactory
	 */
	public void createInitialLayout(IPageLayout layout) {
		
		IFolderLayout consoleFolder = layout.createFolder(IInternalDebugUIConstants.ID_CONSOLE_FOLDER_VIEW, IPageLayout.BOTTOM, (float)0.75, layout.getEditorArea());
		consoleFolder.addView(IDebugUIConstants.ID_CONSOLE_VIEW);
		consoleFolder.addView(IPageLayout.ID_TASK_LIST);
		
		IFolderLayout navFolder= layout.createFolder(IInternalDebugUIConstants.ID_NAVIGATOR_FOLDER_VIEW, IPageLayout.TOP, (float) 0.5, layout.getEditorArea());
		navFolder.addView(IDebugUIConstants.ID_DEBUG_VIEW);
		navFolder.addView(IDebugUIConstants.ID_PROCESS_VIEW);
		navFolder.addView(IPageLayout.ID_RES_NAV);
		
		
		IFolderLayout folder= layout.createFolder(IInternalDebugUIConstants.ID_TOOLS_FOLDER_VIEW, IPageLayout.RIGHT, (float) 0.5, IInternalDebugUIConstants.ID_NAVIGATOR_FOLDER_VIEW);
		folder.addView(IDebugUIConstants.ID_VARIABLE_VIEW);		
		folder.addView(IDebugUIConstants.ID_BREAKPOINT_VIEW);
		folder.addView(IDebugUIConstants.ID_INSPECTOR_VIEW);
		
		layout.addView(IPageLayout.ID_OUTLINE, IPageLayout.RIGHT, (float) 0.75, layout.getEditorArea());
		
		layout.addActionSet(IDebugUIConstants.DEBUG_ACTION_SET);
		
		setContentsOfShowViewMenu(layout);
	}
	
	/** 
	 * Sets the intial contents of the "Show View" menu.
	 */
	protected void setContentsOfShowViewMenu(IPageLayout layout) {
		layout.addShowViewShortcut(IDebugUIConstants.ID_DEBUG_VIEW);
		layout.addShowViewShortcut(IDebugUIConstants.ID_PROCESS_VIEW);
		layout.addShowViewShortcut(IDebugUIConstants.ID_BREAKPOINT_VIEW);
		layout.addShowViewShortcut(IDebugUIConstants.ID_INSPECTOR_VIEW);
		layout.addShowViewShortcut(IDebugUIConstants.ID_VARIABLE_VIEW);
		layout.addShowViewShortcut(IDebugUIConstants.ID_CONSOLE_VIEW);
		layout.addShowViewShortcut(IPageLayout.ID_TASK_LIST);
	}
}
