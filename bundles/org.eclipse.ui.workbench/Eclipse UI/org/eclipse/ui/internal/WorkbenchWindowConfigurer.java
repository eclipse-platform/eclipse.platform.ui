/*******************************************************************************
 * Copyright (c) 2003, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.CoolBarManager;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.internal.provisional.action.ICoolBarManager2;
import org.eclipse.jface.internal.provisional.action.IToolBarContributionItem;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.TextProcessor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.internal.StartupThreading.StartupRunnable;
import org.eclipse.ui.internal.provisional.application.IActionBarConfigurer2;
import org.eclipse.ui.internal.provisional.presentations.IActionBarPresentationFactory;
import org.eclipse.ui.internal.util.PrefUtil;
import org.eclipse.ui.presentations.AbstractPresentationFactory;
import org.eclipse.ui.presentations.WorkbenchPresentationFactory;

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
     * Whether the workbench window should show the fast view bars.
     */
    private boolean showFastViewBars = false;

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
     * The presentation factory.  Lazily initialized in getPresentationFactory
     * if not already assigned in setPresentationFactory.
     */
    private AbstractPresentationFactory presentationFactory = null;

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
        
        /* (non-Javadoc)
         * @see org.eclipse.ui.application.IActionBarConfigurer#getWindowConfigurer()
         */
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

        /* (non-Javadoc)
         * @see org.eclipse.ui.application.IActionBarConfigurer
         */
        public IStatusLineManager getStatusLineManager() {
            if (proxy != null) {
                return proxy.getStatusLineManager();
            }
            return window.getStatusLineManager();
        }

        /* (non-Javadoc)
         * @see org.eclipse.ui.application.IActionBarConfigurer
         */
        public IMenuManager getMenuManager() {
            if (proxy != null) {
                return proxy.getMenuManager();
            }
            return window.getMenuManager();
        }

        /* (non-Javadoc)
         * @see org.eclipse.ui.internal.AbstractActionBarConfigurer
         */
        public ICoolBarManager getCoolBarManager() {
            if (proxy != null) {
                return proxy.getCoolBarManager();
            }
            return window.getCoolBarManager2();
        }

        /* (non-Javadoc)
         * @see org.eclipse.ui.application.IActionBarConfigurer
         */
        public void registerGlobalAction(IAction action) {
            if (proxy != null) {
                proxy.registerGlobalAction(action);
            }
            window.registerGlobalAction(action);
        }

		private IActionBarPresentationFactory getActionBarPresentationFactory() {
			WorkbenchWindow window = (WorkbenchWindow)getWindowConfigurer().getWindow();
			return window.getActionBarPresentationFactory();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.application.IActionBarConfigurer#createToolBarManager()
		 */
		public IToolBarManager createToolBarManager() {
			if (proxy != null) {
				return proxy.createToolBarManager();
			}
			return getActionBarPresentationFactory().createToolBarManager();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.application.IActionBarConfigurer#createToolBarContributionItem(org.eclipse.jface.action.IToolBarManager, java.lang.String)
		 */
		public IToolBarContributionItem createToolBarContributionItem(IToolBarManager toolBarManager, String id) {
			if (proxy != null) {
				return proxy.createToolBarContributionItem(toolBarManager, id);
			}
			return getActionBarPresentationFactory().createToolBarContributionItem(toolBarManager, id);
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

    /**
     * Returns the title as set by <code>setTitle</code>, without consulting the shell.
     * 
     * @return the window title as set, or <code>null</code> if not set
     */
    /* package */String basicGetTitle() {
        return windowTitle;
    }

    /* (non-javadoc)
     * @see org.eclipse.ui.application.IWorkbenchWindowConfigurer#getTitle
     */
    public String getTitle() {
        Shell shell = window.getShell();
        if (shell != null) {
            // update the cached title
            windowTitle = shell.getText();
        }
        return windowTitle;
    }

    /* (non-javadoc)
     * @see org.eclipse.ui.application.IWorkbenchWindowConfigurer#setTitle
     */
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
        WorkbenchWindow win = (WorkbenchWindow) getWindow();
        Shell shell = win.getShell();
        if (shell != null) {
            boolean showing = shell.getMenuBar() != null;
            if (show != showing) {
                if (show) {
                    shell.setMenuBar(win.getMenuBarManager().getMenu());
                } else {
                    shell.setMenuBar(null);
                }
            }
        }
    }

    /* (non-javadoc)
     * @see org.eclipse.ui.application.IWorkbenchWindowConfigurer#getShowToolBar
     */
    public boolean getShowCoolBar() {
        return showToolBar;
    }

    /* (non-javadoc)
     * @see org.eclipse.ui.application.IWorkbenchWindowConfigurer
     */
    public void setShowCoolBar(boolean show) {
        showToolBar = show;
        window.setCoolBarVisible(show);
        // @issue need to be able to reconfigure after window's controls created
    }

    /* (non-javadoc)
     * @see org.eclipse.ui.application.IWorkbenchWindowConfigurer
     */
    public boolean getShowFastViewBars() {
        return showFastViewBars;
    }

    /* (non-javadoc)
     * @see org.eclipse.ui.application.IWorkbenchWindowConfigurer
     */
    public void setShowFastViewBars(boolean show) {
        showFastViewBars = show;
        window.setFastViewBarVisible(show);
        // @issue need to be able to reconfigure after window's controls created
    }

    /* (non-javadoc)
     * @see org.eclipse.ui.application.IWorkbenchWindowConfigurer
     */
    public boolean getShowPerspectiveBar() {
        return showPerspectiveBar;
    }

    /* (non-javadoc)
     * @see org.eclipse.ui.application.IWorkbenchWindowConfigurer
     */
    public void setShowPerspectiveBar(boolean show) {
        showPerspectiveBar = show;
        window.setPerspectiveBarVisible(show);
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

    /* (non-Javadoc)
     * @see org.eclipse.ui.application.IWorkbenchWindowConfigurer
     */
    public boolean getShowProgressIndicator() {
        return showProgressIndicator;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.application.IWorkbenchWindowConfigurer
     */
    public void setShowProgressIndicator(boolean show) {
        showProgressIndicator = show;
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
                DropTarget dropTarget = ((EditorSashContainer) page
                        .getEditorPresentation().getLayoutPart())
                        .getDropTarget();
                if (dropTarget != null) {
                    dropTarget.setTransfer(transfers);
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.application.IWorkbenchWindowConfigurer
     */
    public void configureEditorAreaDropListener(
            DropTargetListener dropTargetListener) {
        if (dropTargetListener != null) {
            this.dropTargetListener = dropTargetListener;
            IWorkbenchPage[] pages = window.getPages();
            for (int i = 0; i < pages.length; i++) {
                WorkbenchPage page = (WorkbenchPage) pages[i];
                DropTarget dropTarget = ((EditorSashContainer) page
                        .getEditorPresentation().getLayoutPart())
                        .getDropTarget();
                if (dropTarget != null) {
                    dropTarget.addDropListener(this.dropTargetListener);
                }
            }
        }
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
    /* package */boolean containsCoolItem(String id) {
        // trigger lazy initialization
        getActionBarConfigurer();
        return actionBarConfigurer.containsCoolItem(id);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.application.IWorkbenchWindowConfigurer
     */
    public int getShellStyle() {
        return shellStyle;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.application.IWorkbenchWindowConfigurer
     */
    public void setShellStyle(int shellStyle) {
        this.shellStyle = shellStyle;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.application.IWorkbenchWindowConfigurer
     */
    public Point getInitialSize() {
        return initialSize;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.application.IWorkbenchWindowConfigurer
     */
    public void setInitialSize(Point size) {
        initialSize = size;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.application.IWorkbenchWindowConfigurer
     */
    public AbstractPresentationFactory getPresentationFactory() {
        if (presentationFactory == null) {
            presentationFactory = createDefaultPresentationFactory();
        }
        return presentationFactory;
    }

    /**
     * Creates the default presentation factory by looking up the presentation
     * factory extension with the id specified by the presentation factory preference.
     * If the preference is null or if no matching extension is found, a
     * factory default presentation factory is used.
     */
    private AbstractPresentationFactory createDefaultPresentationFactory() {
        final String factoryId = ((Workbench) window.getWorkbench())
                .getPresentationId();

        if (factoryId != null && factoryId.length() > 0) {
            final AbstractPresentationFactory [] factory = new AbstractPresentationFactory[1];
            StartupThreading.runWithoutExceptions(new StartupRunnable() {

				public void runWithException() throws Throwable {
					factory[0] = WorkbenchPlugin.getDefault()
							.getPresentationFactory(factoryId);
				}
			});
            
            if (factory[0] != null) {
                return factory[0];
            }
        }
        // presentation ID must be a bogus value, reset it to the default
        PrefUtil.getAPIPreferenceStore().setValue(
				IWorkbenchPreferenceConstants.PRESENTATION_FACTORY_ID,
				IWorkbenchConstants.DEFAULT_PRESENTATION_ID);
        return new WorkbenchPresentationFactory();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.application.IWorkbenchWindowConfigurer
     */
    public void setPresentationFactory(AbstractPresentationFactory factory) {
        if (factory == null) {
            throw new IllegalArgumentException();
        }
        presentationFactory = factory;
    }

    /**
     * Creates the default window contents.
     * 
     * @param shell the shell
     */
    public void createDefaultContents(Shell shell) {
        window.createDefaultContents(shell);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.application.IWorkbenchWindowConfigurer
     */
    public Menu createMenuBar() {
        return window.getMenuManager().createMenuBar(window.getShell());
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.application.IWorkbenchWindowConfigurer
     */
    public Control createCoolBarControl(Composite parent) {
    	ICoolBarManager coolBarManager = window.getCoolBarManager2();
    	if (coolBarManager != null) {
        	if (coolBarManager instanceof ICoolBarManager2) {
				return ((ICoolBarManager2) coolBarManager).createControl2(parent);
			}
        	if (coolBarManager instanceof CoolBarManager) {
				return ((CoolBarManager) coolBarManager).createControl(parent);
			}
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.application.IWorkbenchWindowConfigurer
     */
    public Control createStatusLineControl(Composite parent) {
        return window.getStatusLineManager().createControl(parent);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.application.IWorkbenchWindowConfigurer
     */
    public Control createPageComposite(Composite parent) {
        return window.createPageComposite(parent);
    }
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.application.IWorkbenchWindowConfigurer#saveState(org.eclipse.ui.IMemento)
	 */
	public IStatus saveState(IMemento memento) {
		return window.saveState(memento);
	}

}
