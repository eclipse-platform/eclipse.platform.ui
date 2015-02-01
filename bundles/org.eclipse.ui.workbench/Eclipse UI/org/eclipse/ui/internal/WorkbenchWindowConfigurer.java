/*******************************************************************************
 * Copyright (c) 2003, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 441184, 440136
 *     Denis Zygann <d.zygann@web.de> - Bug 457390
 *******************************************************************************/
package org.eclipse.ui.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.internal.provisional.action.IToolBarContributionItem;
import org.eclipse.jface.internal.provisional.action.ToolBarContributionItem2;
import org.eclipse.jface.internal.provisional.action.ToolBarManager2;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.TextProcessor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.internal.provisional.application.IActionBarConfigurer2;

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
public final class WorkbenchWindowConfigurer implements
        IWorkbenchWindowConfigurer {

    /**
     * The workbench window associated with this configurer.
     */
    private WorkbenchWindow window;

    /**
     * The shell style bits to use when the window's shell is being created.
     */
    private int shellStyle = SWT.SHELL_TRIM | Window.getDefaultOrientation();

    /**
     * The window title to set when the window's shell has been created.
     */
    private String windowTitle;

    /**
     * Whether the workbench window should show the perspective bar
     */
    private boolean showPerspectiveBar = false;

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
     * Whether the workbench window should have a progress indicator.
     */
    private boolean showProgressIndicator = false;

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
     * The initial size to use for the shell.
     */
    private Point initialSize = new Point(1024, 768);

    /**
     * Action bar configurer that changes this workbench window.
     * This implementation keeps track of of cool bar items
     */
    class WindowActionBarConfigurer implements IActionBarConfigurer2 {

        private IActionBarConfigurer2 proxy;

        /**
         * Sets the proxy to use, or <code>null</code> for none.
         *
         * @param proxy the proxy
         */
        public void setProxy(IActionBarConfigurer2 proxy) {
            this.proxy = proxy;
        }

        @Override
		public IWorkbenchWindowConfigurer getWindowConfigurer() {
            return window.getWindowConfigurer();
        }

        /**
         * Returns whether the given id is for a cool item.
         *
         * @param the item id
         * @return <code>true</code> if it is a cool item,
         * and <code>false</code> otherwise
         */
        /* package */boolean containsCoolItem(String id) {
            ICoolBarManager cbManager = getCoolBarManager();
            if (cbManager == null) {
				return false;
			}
            IContributionItem cbItem = cbManager.find(id);
            if (cbItem == null) {
				return false;
			}
            //@ issue: maybe we should check if cbItem is visible?
            return true;
        }

        @Override
		public IStatusLineManager getStatusLineManager() {
            if (proxy != null) {
                return proxy.getStatusLineManager();
            }
			return window.getStatusLineManager();
        }

        @Override
		public IMenuManager getMenuManager() {
            if (proxy != null) {
                return proxy.getMenuManager();
            }
			return window.getMenuManager();
        }

        @Override
		public ICoolBarManager getCoolBarManager() {
            if (proxy != null) {
                return proxy.getCoolBarManager();
            }
			return window.getCoolBarManager2();
        }

        @Override
		public void registerGlobalAction(IAction action) {
            if (proxy != null) {
                proxy.registerGlobalAction(action);
            }
            window.registerGlobalAction(action);
        }

		@Override
		public IToolBarManager createToolBarManager() {
			if (proxy != null) {
				return proxy.createToolBarManager();
			}
			return new ToolBarManager2(SWT.WRAP | SWT.FLAT | SWT.RIGHT);
		}

		@Override
		public IToolBarContributionItem createToolBarContributionItem(IToolBarManager toolBarManager, String id) {
			if (proxy != null) {
				return proxy.createToolBarContributionItem(toolBarManager, id);
			}
			return new ToolBarContributionItem2(toolBarManager, id);
		}
    }

    /**
     * Creates a new workbench window configurer.
     * <p>
     * This method is declared package-private. Clients obtain instances
     * via {@link WorkbenchAdvisor#getWindowConfigurer
     * WorkbenchAdvisor.getWindowConfigurer}
     * </p>
     *
     * @param window the workbench window that this object configures
     * @see WorkbenchAdvisor#getWindowConfigurer
     */
    WorkbenchWindowConfigurer(WorkbenchWindow window) {
        if (window == null) {
            throw new IllegalArgumentException();
        }
        this.window = window;
        windowTitle = WorkbenchPlugin.getDefault().getProductName();
        if (windowTitle == null) {
            windowTitle = ""; //$NON-NLS-1$
        }
    }

    @Override
	public IWorkbenchWindow getWindow() {
        return window;
    }

    @Override
	public IWorkbenchConfigurer getWorkbenchConfigurer() {
        return Workbench.getInstance().getWorkbenchConfigurer();
    }

    /**
     * Returns the title as set by <code>setTitle</code>, without consulting the shell.
     *
     * @return the window title as set, or <code>null</code> if not set
     */
    /* package */String basicGetTitle() {
        return windowTitle;
    }

    @Override
	public String getTitle() {
        Shell shell = window.getShell();
        if (shell != null) {
            // update the cached title
            windowTitle = shell.getText();
        }
        return windowTitle;
    }

    @Override
	public void setTitle(String title) {
        if (title == null) {
            throw new IllegalArgumentException();
        }
        windowTitle = title;
        Shell shell = window.getShell();
        if (shell != null && !shell.isDisposed()) {
            shell.setText(TextProcessor.process(title, WorkbenchWindow.TEXT_DELIMITERS));
        }
    }

    @Override
	public boolean getShowMenuBar() {
        return showMenuBar;
    }

    @Override
	public void setShowMenuBar(boolean show) {
        showMenuBar = show;
        WorkbenchWindow win = (WorkbenchWindow) getWindow();
        Shell shell = win.getShell();
        if (shell != null) {
            boolean showing = shell.getMenuBar() != null;
            if (show != showing) {
                if (show) {
					shell.setMenuBar(null);
                } else {
                    shell.setMenuBar(null);
                }
            }
        }
    }

    @Override
	public boolean getShowCoolBar() {
        return showToolBar;
    }

    @Override
	public void setShowCoolBar(boolean show) {
        showToolBar = show;
        // @issue need to be able to reconfigure after window's controls created
    }

    @Override
    public boolean getShowFastViewBars() {
        // not supported anymore
        return false;
    }

    @Override
    public void setShowFastViewBars(boolean show) {
        // not supported anymore
    }

    @Override
	public boolean getShowPerspectiveBar() {
        return showPerspectiveBar;
    }

    @Override
	public void setShowPerspectiveBar(boolean show) {
        showPerspectiveBar = show;
        // @issue need to be able to reconfigure after window's controls created
    }

    @Override
	public boolean getShowStatusLine() {
        return showStatusLine;
    }

    @Override
	public void setShowStatusLine(boolean show) {
        showStatusLine = show;
        window.setStatusLineVisible(show);
        // @issue need to be able to reconfigure after window's controls created
    }

    @Override
	public boolean getShowProgressIndicator() {
        return showProgressIndicator;
    }

    @Override
	public void setShowProgressIndicator(boolean show) {
        showProgressIndicator = show;
        // @issue need to be able to reconfigure after window's controls created
    }

    @Override
	public Object getData(String key) {
        if (key == null) {
            throw new IllegalArgumentException();
        }
        return extraData.get(key);
    }

    @Override
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

    @Override
	public void addEditorAreaTransfer(Transfer tranfer) {
		if (tranfer != null && !transferTypes.contains(tranfer)) {
			transferTypes.add(tranfer);
		}
    }

    @Override
	public void configureEditorAreaDropListener(
            DropTargetListener dropTargetListener) {
		this.dropTargetListener = dropTargetListener;
    }

    /**
     * Returns the array of <code>Transfer</code> added by the application
     */
    /* package */Transfer[] getTransfers() {
        Transfer[] transfers = new Transfer[transferTypes.size()];
        transferTypes.toArray(transfers);
        return transfers;
    }

    /**
     * Returns the drop listener provided by the application.
     */
    /* package */DropTargetListener getDropTargetListener() {
        return dropTargetListener;
    }

    @Override
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
    /* package */boolean containsCoolItem(String id) {
        // trigger lazy initialization
        getActionBarConfigurer();
        return actionBarConfigurer.containsCoolItem(id);
    }

    @Override
	public int getShellStyle() {
        return shellStyle;
    }

    @Override
	public void setShellStyle(int shellStyle) {
        this.shellStyle = shellStyle;
    }

    @Override
	public Point getInitialSize() {
        return initialSize;
    }

    @Override
	public void setInitialSize(Point size) {
        initialSize = size;
    }

    /**
     * Creates the default window contents.
     *
     * @param shell the shell
     */
    public void createDefaultContents(Shell shell) {

    }

    @Override
	public Menu createMenuBar() {
		return null;
    }

    @Override
	public Control createCoolBarControl(Composite parent) {

        return null;
    }

    @Override
	public Control createStatusLineControl(Composite parent) {
		return null;
    }

    @Override
	public Control createPageComposite(Composite parent) {
		return null;
    }

	@Override
	public IStatus saveState(IMemento memento) {
		return null;
	}

}
