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
	private WindowActionBarConfigurer actionBarConfigurer = null;

	/**
	 * Action bar configurer that changes this workbench window.
	 * This implementation keeps track of of cool bar items
	 */
	class WindowActionBarConfigurer extends AbstractActionBarConfigurer {

		/**
		 * Holds onto the cool item ids added by the application.
		 */
		private ArrayList coolItemIds = new ArrayList(4);
	
		/**
		 * Returns whether the given id is for a cool item.
		 * 
		 * @param the item id
		 * @return <code>true</code> if it is a cool item,
		 * and <code>false</code> otherwise
		 */
		/* package */ boolean containsCoolItem(String id) {
			return coolItemIds.contains(id);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.application.IActionBarConfigurer
		 */
		public IStatusLineManager getStatusLineManager() {
			return window.getStatusLineManager();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.application.IActionBarConfigurer
		 */
		public IMenuManager getMenuManager() {
			return window.getMenuManager();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.internal.AbstractActionBarConfigurer
		 */
		public CoolBarManager getCoolBarManager() {
			return window.getCoolBarManager();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.application.IActionBarConfigurer
		 */
		public void addEditorToolBarGroup() {
			// @issue missing implementation
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.ui.application.IActionBarConfigurer
		 */
		public void registerGlobalAction(IAction action) {
			window.registerGlobalAction(action);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.application.IActionBarConfigurer
		 */
		public IToolBarManager addToolBar(String id) {
			IToolBarManager manager = super.addToolBar(id);
			coolItemIds.add(id);
			return manager;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.application.IActionBarConfigurer
		 */
		public void removeToolBar(String id) {
			super.removeToolBar(id);
			coolItemIds.remove(id);
		}
	}
	
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
	 * Returns the drop listener provided by the application.
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
			actionBarConfigurer = new WindowActionBarConfigurer();
		}
		return actionBarConfigurer;
	}
	
	/**
	 * Returns whether the given id is for a cool item.
	 * 
	 * @param the item id
	 * @return <code>true</code> if it is a cool item,
	 * and <code>false</code> otherwise
	 */
	/* package */ boolean containsCoolItem(String id) {
		// trigger lazy initialization
		getActionBarConfigurer();
		return actionBarConfigurer.containsCoolItem(id);
	}
}
