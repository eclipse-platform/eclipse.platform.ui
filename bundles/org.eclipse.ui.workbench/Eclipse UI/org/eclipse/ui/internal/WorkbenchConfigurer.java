/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.window.WindowManager;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;

/**
 * Internal class providing special access for configuring the workbench.
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
public final class WorkbenchConfigurer implements IWorkbenchConfigurer {

    /**
     * Table to hold arbitrary key-data settings (key type: <code>String</code>,
     * value type: <code>Object</code>).
     * @see #setData
     */
    private Map extraData = new HashMap();

    /**
     * Indicates whether workbench state should be saved on close and 
     * restored on subsequent open.
     */
    private boolean saveAndRestore = false;

    /**
     * Indicates whether the workbench is being force to close. During
     * an emergency close, no interaction with the user should be done.
     */
    private boolean isEmergencyClosing = false;

    /**
     * Indicates the behaviour when the last window is closed.
     * If <code>true</code>, the workbench will exit (saving the last window's state, 
     * if configured to do so).
     * If <code>false</code> the window will be closed, leaving the workbench running.
     * 
     * @since 3.1
     */
	private boolean exitOnLastWindowClose = true;

    /**
     * Creates a new workbench configurer.
     * <p>
     * This method is declared package-private. Clients are passed an instance
     * only via {@link WorkbenchAdvisor#initialize WorkbenchAdvisor.initialize}
     * </p>
     */
    /* package */WorkbenchConfigurer() {
        super();
    }

    /* (non-javadoc)
     * @see org.eclipse.ui.application.IWorkbenchConfigurer#getWorkbench
     */
    public IWorkbench getWorkbench() {
        return PlatformUI.getWorkbench();
    }

    /* (non-javadoc)
     * @see org.eclipse.ui.application.IWorkbenchConfigurer#getWorkbenchWindowManager
     */
    public WindowManager getWorkbenchWindowManager() {
        // return the global workbench window manager
        return ((Workbench) getWorkbench()).getWindowManager();
    }

    /* (non-javadoc)
     * @see org.eclipse.ui.application.IWorkbenchConfigurer#declareImage
     */
    public void declareImage(String symbolicName, ImageDescriptor descriptor,
            boolean shared) {
        if (symbolicName == null || descriptor == null) {
            throw new IllegalArgumentException();
        }
        WorkbenchImages.declareImage(symbolicName, descriptor, shared);
    }

    /* (non-javadoc)
     * @see org.eclipse.ui.application.IWorkbenchConfigurer#getWindowConfigurer
     */
    public IWorkbenchWindowConfigurer getWindowConfigurer(
            IWorkbenchWindow window) {
        if (window == null) {
            throw new IllegalArgumentException();
        }
        return ((WorkbenchWindow) window).getWindowConfigurer();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.application.IWorkbenchConfigurer#getSaveAndRestore()
     */
    public boolean getSaveAndRestore() {
        return saveAndRestore;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.application.IWorkbenchConfigurer#setSaveAndRestore(boolean)
     */
    public void setSaveAndRestore(boolean enabled) {
        saveAndRestore = enabled;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.application.IWorkbenchConfigurer#getData
     */
    public Object getData(String key) {
        if (key == null) {
            throw new IllegalArgumentException();
        }
        return extraData.get(key);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.application.IWorkbenchConfigurer#setData(String, Object)
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
     * @see org.eclipse.ui.application.IWorkbenchConfigurer#emergencyClose()
     */
    public void emergencyClose() {
        if (!isEmergencyClosing) {
            isEmergencyClosing = true;
            if (Workbench.getInstance() != null
                    && !Workbench.getInstance().isClosing()) {
                Workbench.getInstance().close(
                        PlatformUI.RETURN_EMERGENCY_CLOSE, true);
            }
        }

    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.application.IWorkbenchConfigurer#emergencyClosing()
     */
    public boolean emergencyClosing() {
        return isEmergencyClosing;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.application.IWorkbenchConfigurer#restoreState()
     */
    public IStatus restoreState() {
        return ((Workbench) getWorkbench()).restoreState();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.application.IWorkbenchConfigurer#openFirstTimeWindow()
     */
    public void openFirstTimeWindow() {
        ((Workbench) getWorkbench()).openFirstTimeWindow();
    }

	/* (non-Javadoc)
	 * @see org.eclipse.ui.application.IWorkbenchConfigurer#restoreWorkbenchWindow(org.eclipse.ui.IMemento)
	 */
	public IWorkbenchWindowConfigurer restoreWorkbenchWindow(IMemento memento) throws WorkbenchException {
		return getWindowConfigurer(((Workbench) getWorkbench()).restoreWorkbenchWindow(memento));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.application.IWorkbenchConfigurer#getExitOnLastWindowClose()
	 */
	public boolean getExitOnLastWindowClose() {
		return exitOnLastWindowClose;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.application.IWorkbenchConfigurer#setExitOnLastWindowClose(boolean)
	 */
	public void setExitOnLastWindowClose(boolean enabled) {
		exitOnLastWindowClose = enabled;
	}
}
