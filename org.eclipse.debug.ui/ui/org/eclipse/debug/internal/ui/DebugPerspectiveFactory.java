/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui;


import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.console.IConsoleConstants;

/**
 * The debug perspective factory.
 */
public class DebugPerspectiveFactory implements IPerspectiveFactory {
	
	/**
	 * @see IPerspectiveFactory#createInitialLayout(IPageLayout)
	 */
	public void createInitialLayout(IPageLayout layout) {
		
		IFolderLayout consoleFolder = layout.createFolder(IInternalDebugUIConstants.ID_CONSOLE_FOLDER_VIEW, IPageLayout.BOTTOM, (float)0.75, layout.getEditorArea());
		consoleFolder.addView(IConsoleConstants.ID_CONSOLE_VIEW);
		consoleFolder.addView(IPageLayout.ID_TASK_LIST);
		consoleFolder.addPlaceholder(IPageLayout.ID_BOOKMARKS);
		consoleFolder.addPlaceholder(IPageLayout.ID_PROP_SHEET);
		
		IFolderLayout navFolder= layout.createFolder(IInternalDebugUIConstants.ID_NAVIGATOR_FOLDER_VIEW, IPageLayout.TOP, (float) 0.45, layout.getEditorArea());
		navFolder.addView(IDebugUIConstants.ID_DEBUG_VIEW);
		navFolder.addPlaceholder(IPageLayout.ID_RES_NAV);
		
		IFolderLayout toolsFolder= layout.createFolder(IInternalDebugUIConstants.ID_TOOLS_FOLDER_VIEW, IPageLayout.RIGHT, (float) 0.50, IInternalDebugUIConstants.ID_NAVIGATOR_FOLDER_VIEW);
		toolsFolder.addView(IDebugUIConstants.ID_VARIABLE_VIEW);
		toolsFolder.addView(IDebugUIConstants.ID_BREAKPOINT_VIEW);
		toolsFolder.addPlaceholder(IDebugUIConstants.ID_EXPRESSION_VIEW);
		toolsFolder.addPlaceholder(IDebugUIConstants.ID_REGISTER_VIEW);
		
		IFolderLayout outlineFolder= layout.createFolder(IInternalDebugUIConstants.ID_OUTLINE_FOLDER_VIEW, IPageLayout.RIGHT, (float) 0.75, layout.getEditorArea());
		outlineFolder.addView(IPageLayout.ID_OUTLINE);
		
		layout.addActionSet(IDebugUIConstants.LAUNCH_ACTION_SET);
		layout.addActionSet(IDebugUIConstants.DEBUG_ACTION_SET);
		
		setContentsOfShowViewMenu(layout);
	}
	
	/** 
	 * Sets the initial contents of the "Show View" menu.
	 */
	protected void setContentsOfShowViewMenu(IPageLayout layout) {
		layout.addShowViewShortcut(IDebugUIConstants.ID_DEBUG_VIEW);
		layout.addShowViewShortcut(IDebugUIConstants.ID_VARIABLE_VIEW);
		layout.addShowViewShortcut(IDebugUIConstants.ID_BREAKPOINT_VIEW);
		layout.addShowViewShortcut(IDebugUIConstants.ID_EXPRESSION_VIEW);
		layout.addShowViewShortcut(IPageLayout.ID_OUTLINE);
		layout.addShowViewShortcut(IConsoleConstants.ID_CONSOLE_VIEW);
		layout.addShowViewShortcut(IPageLayout.ID_TASK_LIST);
	}
}
