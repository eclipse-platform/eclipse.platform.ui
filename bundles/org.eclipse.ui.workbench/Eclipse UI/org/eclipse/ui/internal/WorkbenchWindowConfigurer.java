/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.application.IWorkbenchPreferences;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;

/**
 * Internal class providing special access for configuring workbench windows.
 * <p>
 * Note that these objects are only available to the main application
 * (the plug-in that creates and owns the workbench).
 * </p>
 * <p>
 * This class is not intended to be instantiated or subclassed by clients.
 * </p>
 * 
 * @since 3.0
 */
public final class WorkbenchWindowConfigurer implements IWorkbenchWindowConfigurer {
	
	/**
	 * The workbench window associated with this configurer.
	 */
	private WorkbenchWindow window;

	/**
	 * Whether the workbench window should show the shortcut bar.
	 */
	private boolean showShortcutBar = true;
	
	/**
	 * Whether the workbench window should show the status line.
	 */
	private boolean showStatusLine = true;
	
	/**
	 * Whether the workbench window should show the main tool bar.
	 */
	private boolean showToolBar = true;
	
	/**
	 * Whether the workbench window should show the main menu bar.
	 */
	private boolean showMenuBar = true;
	
	/**
	 * Whether the workbench window should have a title bar.
	 */
	private boolean showTitleBar = true;

	/**
	 * Table to hold arbitrary key-data settings (key type: <code>String</code>,
	 * value type: <code>Object</code>).
	 * @see #setData
	 */
	private Map extraData = new HashMap(1);

	/**
	 * Holds onto the cool item ids added by the application.
	 */
	private ArrayList coolItemIds = new ArrayList(4);

	/**
	 * Holds the list drag and drop <code>Transfer</code> for the
	 * editor area
	 */
	private ArrayList transferTypes = new ArrayList(3);

	/**
	 * The <code>DropTargetListener</code> implementation for handling a
	 * drop into the editor area.
	 */
	private DropTargetListener dropTargetListener = null;
	 
	/**
	 * Object for configuring this workbench window's action bars. 
	 * Lazily initialized to an instance unique to this window.
	 */
	private IActionBarConfigurer actionBarConfigurer = null;

	/**
	 * Creates a new workbench configurer.
	 * <p>
	 * This method is declared package-private. Clients obtain instances
	 * via {@link WorkbenchAdviser#getWindowConfigurer 
	 * WorkbenchAdviser.getWindowConfigurer}
	 * </p>
	 * 
	 * @param window the workbench window that this object configures
	 * @see WorkbenchAdviser#getWindowConfigurer
	 */
	WorkbenchWindowConfigurer(WorkbenchWindow window) {
		if (window == null) {
			throw new IllegalArgumentException();
		}
		this.window = window;
	}

	/**
	 * Allows the configurer to initialize its state that
	 * depends on a Display existing.
	 */
	/* package */ void init() {
		IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();
		showMenuBar = store.getBoolean(IWorkbenchPreferences.SHOULD_SHOW_MENU_BAR);
		showShortcutBar = store.getBoolean(IWorkbenchPreferences.SHOULD_SHOW_SHORTCUT_BAR);
		showStatusLine = store.getBoolean(IWorkbenchPreferences.SHOULD_SHOW_STATUS_LINE);
		showTitleBar = store.getBoolean(IWorkbenchPreferences.SHOULD_SHOW_TITLE_BAR);
		showToolBar = store.getBoolean(IWorkbenchPreferences.SHOULD_SHOW_TOOL_BAR);
	}
	
	/* (non-javadoc)
	 * @see org.eclipse.ui.application.IWorkbenchWindowConfigurer#getWindow
	 */
	public IWorkbenchWindow getWindow() {
		return window;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.application.IWorkbenchWindowConfigurer#getWorkbenchConfigurer()
	 */
	public IWorkbenchConfigurer getWorkbenchConfigurer() {
		return Workbench.getInstance().getWorkbenchConfigurer();
	}
	
	/* (non-javadoc)
	 * @see org.eclipse.ui.application.IWorkbenchWindowConfigurer#getTitle
	 */
	public String getTitle() {
		Shell shell =  window.getShell();
		if (shell != null) {
			return shell.getText();
		} else {
			// @issue need to be able to configure title before window's controls created
			return ""; //$NON-NLS-1$
		}
	}
	
	/* (non-javadoc)
	 * @see org.eclipse.ui.application.IWorkbenchWindowConfigurer#setTitle
	 */
	public void setTitle(String title) {
		if (title == null) {
			throw new IllegalArgumentException();
		}
		Shell shell =  window.getShell();
		if (shell != null) {
			shell.setText(title);
		} else {
			// @issue need to be able to configure title before window's controls created
		}
	}
	
	/* (non-javadoc)
	 * @see org.eclipse.ui.application.IWorkbenchWindowConfigurer#getShowTitleBar
	 */
	public boolean getShowTitleBar() {
		return showTitleBar;
	}

	/* (non-javadoc)
	 * @see org.eclipse.ui.application.IWorkbenchWindowConfigurer#setShowTitleBar
	 */
	public void setShowTitleBar(boolean show) {
		showTitleBar = show;
		// @issue need to be able to reconfigure after window's controls created
	}

	/* (non-javadoc)
	 * @see org.eclipse.ui.application.IWorkbenchWindowConfigurer#getShowMenuBar
	 */
	public boolean getShowMenuBar() {
		return showMenuBar;
	}

	/* (non-javadoc)
	 * @see org.eclipse.ui.application.IWorkbenchWindowConfigurer#setShowMenuBar
	 */
	public void setShowMenuBar(boolean show) {
		showMenuBar = show;
		// @issue need to be able to reconfigure after window's controls created
	}

	/* (non-javadoc)
	 * @see org.eclipse.ui.application.IWorkbenchWindowConfigurer#getShowToolBar
	 */
	public boolean getShowToolBar() {
		return showToolBar;
	}

	/* (non-javadoc)
	 * @see org.eclipse.ui.application.IWorkbenchWindowConfigurer#setShowToolBar
	 */
	public void setShowToolBar(boolean show) {
		showToolBar = show;
		// @issue need to be able to reconfigure after window's controls created
	}

	/* (non-javadoc)
	 * @see org.eclipse.ui.application.IWorkbenchWindowConfigurer#getShowShortcutBar
	 */
	public boolean getShowShortcutBar() {
		return showShortcutBar;
	}

	/* (non-javadoc)
	 * @see org.eclipse.ui.application.IWorkbenchWindowConfigurer#setShowShortcutBar
	 */
	public void setShowShortcutBar(boolean show) {
		showShortcutBar = show;
		// @issue need to be able to reconfigure after window's controls created
	}

	/* (non-javadoc)
	 * @see org.eclipse.ui.application.IWorkbenchWindowConfigurer#getShowStatusLine
	 */
	public boolean getShowStatusLine() {
		return showStatusLine;
	}

	/* (non-javadoc)
	 * @see org.eclipse.ui.application.IWorkbenchWindowConfigurer#setShowStatusLine
	 */
	public void setShowStatusLine(boolean show) {
		showStatusLine = show;
		// @issue need to be able to reconfigure after window's controls created
	}
	
	/* (non-javadoc)
	 * @see org.eclipse.ui.application.IWorkbenchWindowConfigurer#getData
	 */
	public Object getData(String key) {
		if (key == null) {
			throw new IllegalArgumentException();
		}
		return extraData.get(key);
	}

	/* (non-javadoc)
	 * @see org.eclipse.ui.application.IWorkbenchWindowConfigurer#setData
	 */
	public void setData(String key, Object data) {
		if (key == null) {
			throw new IllegalArgumentException();
		}
		if (data != null) {
			extraData.put(key, data);
		} else {
			extraData.remove(key);
		}
	}

	/**
	 * Adds a tool bar item with the given id to the tool bar of this workbench
	 * window. The new tool bar item is added after any existing ones.
	 * 
	 * @param id the id assigned to this tool bar
	 * @return the tool bar manager for the new tool bar item
	 */
	/* package */ IToolBarManager addToolBar(String id) {
		if (id == null || id.length() < 1) {
			throw new IllegalArgumentException();
		}
		coolItemIds.add(id);
		CoolBarManager cBarMgr = window.getCoolBarManager();
		CoolBarContributionItem cBarItem = new CoolBarContributionItem(cBarMgr, id);
		cBarMgr.add(cBarItem);
		cBarItem.setVisible(true);
		return cBarItem.getToolBarManager();
	}

	/**
	 * Removes the tool bar item with the given id from the tool bar of this
	 * workbench window. Ignored if there is no tool bar item with the given id. 
	 * 
	 * @param id the tool bar id
	 */
	/* package */ void removeToolBar(String id) {
		if (id == null || id.length() < 1) {
			throw new IllegalArgumentException();
		}
		coolItemIds.remove(id);
		CoolBarManager cBarMgr = window.getCoolBarManager();
		cBarMgr.remove(id);
	}

	/**
	 * Returns the tool bar manager for the tool bar item with the given id
	 * to the tool bar of this workbench window. The new tool bar item is added
	 * after any existing ones.
	 * 
	 * @param id the id of the tool bar item
	 * @return the tool bar manager for the tool bar item with the given id
	 */
	/* package */ IToolBarManager getToolBar(String id) {
		if (id == null || id.length() < 1) {
			throw new IllegalArgumentException();
		}
		CoolBarManager cBarMgr = window.getCoolBarManager();
		CoolBarContributionItem cBarItem = (CoolBarContributionItem) cBarMgr.find(id);
		if (cBarItem != null) {
			return cBarItem.getToolBarManager();
		} else {
			return null;
		}
	}

	/**
	 * Adds the special editor tool bar group to the tool bar of this workbench
	 * window. The new tool bar item is added after any existing ones. The id
	 * of editor tool bar item is always 
	 * {@link EDITOR_TOOLBAR_ID EDITOR_TOOLBAR_ID}, and consists of a canned
	 * arrangement of buttons pre-bound to editor-specific commands.
	 * 
	 * @return the tool bar manager for the new tool bar item
	 * @issue where is EDITOR_TOOLBAR_ID defined?
	 */
	/* package */ void addEditorToolbarGroup() {
		// @issue need to provide implementation for this
	}

	/**
	 * Adds a group to the tool bar of this workbench window. The new group is 
	 * added after any existing contributions to the tool bar.
	 *
	 * @param toolBarMgr the tool bar manager to add the group to 
	 * @param id the unique group identifier
	 * @param asSeparator whether the group should have a seperator
	 */
	/* package */ void addToolBarGroup(IToolBarManager toolBarMgr, String id, boolean asSeparator) {
		if (id == null || id.length() < 1) {
			throw new IllegalArgumentException();
		}
		if (!(toolBarMgr instanceof CoolItemToolBarManager)) {
			throw new IllegalArgumentException();
		}
		((CoolItemToolBarManager) toolBarMgr).addBaseGroup(id, asSeparator);
	}

	/**
	 * Adds a menu item to the tool bar of this workbench menu.
	 * 
	 * @param menuItem the action contribution item to add to the menu
	 */
	/* package */ void addToToolBarMenu(ActionContributionItem menuItem) {
		if (menuItem == null) {
			throw new IllegalArgumentException();
		}
		CoolBarManager cBarMgr = window.getCoolBarManager();
		cBarMgr.addToMenu(menuItem);
	}
	
	/**
	 * Returns the list of cool item ids added by the application.
	 */
	/* package */ ArrayList getCoolItemIds() {
		return coolItemIds;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.application.IWorkbenchWindowConfigurer#addEditorAreaTransfer
	 */
	public void addEditorAreaTransfer(Transfer tranfer) {
		if (tranfer != null && !transferTypes.contains(tranfer)) {
			transferTypes.add(tranfer);
			Transfer[] transfers = new Transfer[transferTypes.size()];
			transferTypes.toArray(transfers);
			IWorkbenchPage[] pages = window.getPages();
			for (int i = 0; i < pages.length; i++) {
				WorkbenchPage page = (WorkbenchPage) pages[i];
				DropTarget dropTarget = ((EditorArea) page.getEditorPresentation().getLayoutPart()).getDropTarget();
				if (dropTarget != null) {
					dropTarget.setTransfer(transfers);
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.application.IWorkbenchWindowConfigurer
	 */
	public void configureEditorAreaDropListener(DropTargetListener dropTargetListener) {
		if (dropTargetListener != null) {
			this.dropTargetListener = dropTargetListener;
			IWorkbenchPage[] pages = window.getPages();
			for (int i = 0; i < pages.length; i++) {
				WorkbenchPage page = (WorkbenchPage) pages[i];
				DropTarget dropTarget = ((EditorArea) page.getEditorPresentation().getLayoutPart()).getDropTarget();
				if (dropTarget != null) {
					dropTarget.addDropListener(this.dropTargetListener);
				}
			}
		}
	}
	
	/**
	 * Returns the array of <code>Transfer</code> added by the application
	 */
	/* package */ Transfer[] getTransfers() {
		Transfer[] transfers = new Transfer[transferTypes.size()];
		transferTypes.toArray(transfers);
		return transfers;
	}

	/**
	 * Return the drop listener provided by the application.
	 */	
	/* package */ DropTargetListener getDropTargetListener() {
		return dropTargetListener;
	}

	/* (non-javadoc)
	 * @see org.eclipse.ui.application.IWorkbenchWindowConfigurer
	 */
	public IActionBarConfigurer getActionBarConfigurer() {
		if (actionBarConfigurer == null) {
			// lazily initialize
			actionBarConfigurer = new IActionBarConfigurer() {

				public IMenuManager getMenuManager() {
					return window.getMenuManager();
				}

				public IToolBarManager addToolBar(String id) {
					return WorkbenchWindowConfigurer.this.addToolBar(id);
				}

				public void removeToolBar(String id) {
					WorkbenchWindowConfigurer.this.removeToolBar(id);
				}

				public IToolBarManager getToolBar(String id) {
					return WorkbenchWindowConfigurer.this.getToolBar(id);
				}

				public void addToolBarGroup(IToolBarManager toolBarMgr, String id, boolean asSeparator) {
					WorkbenchWindowConfigurer.this.addToolBarGroup(toolBarMgr, id, asSeparator);
				}

				public void registerGlobalAction(IAction action) {
					window.registerGlobalAction(action);
				}

				public void addToToolBarMenu(ActionContributionItem menuItem) {
					WorkbenchWindowConfigurer.this.addToToolBarMenu(menuItem);
				}

				public void addEditorToolBarGroup() {
					WorkbenchWindowConfigurer.this.addEditorToolbarGroup();
				}

				public IStatusLineManager getStatusLineManager() {
					return window.getStatusLineManager();
				}
			};
		}
		return actionBarConfigurer;
	}
}
