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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.window.WindowManager;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;

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
	 * The workbench associated with this configurer.
	 */
	private Workbench workbench;

	/**
	 * Creates a new workbench configurer.
	 * <p>
	 * This method is declared package-private. Clients are passed an instance
	 * only via {@link WorkbenchAdviser#initialize WorkbenchAdviser.initialize}
	 * </p>
	 * @return the workbench
	 * @see WorkbenchAdviser#getWorkbenchConfigurer
	 */
	WorkbenchConfigurer(IWorkbench workbench) {
		if (workbench == null || !(workbench instanceof Workbench)) {
			throw new IllegalArgumentException();
		}
		this.workbench = (Workbench) workbench;
	}

	/* (non-javadoc)
	 * @see org.eclipse.ui.application.IWorkbenchConfigurer#getWorkbench
	 */
	public IWorkbench getWorkbench() {
		return workbench;
	}

	/* (non-javadoc)
	 * @see org.eclipse.ui.application.IWorkbenchConfigurer#getWorkbenchWindowManager
	 */
	public WindowManager getWorkbenchWindowManager() {
		// return the global workbench window manager
		return workbench.getWindowManager();
	}	
	
	/* (non-javadoc)
	 * @see org.eclipse.ui.application.IWorkbenchConfigurer#getWorkbenchImageRegistry
	 */
	public ImageRegistry getWorkbenchImageRegistry() {
		// return the global workbench image registry
		return WorkbenchPlugin.getDefault().getImageRegistry();
	}	

	/* (non-javadoc)
	 * @see org.eclipse.ui.application.IWorkbenchConfigurer#getWindowConfigurer
	 */
	public IWorkbenchWindowConfigurer getWindowConfigurer(IWorkbenchWindow window) {
		if (window == null) {
			throw new IllegalArgumentException();
		}
		return ((WorkbenchWindow) window).getWindowConfigurer();
	}

	/**
	 * Returns the data associated with the workbench at the given key.
	 * 
	 * @param key the key
	 * @return the data, or <code>null</code> if there is no data at the given
	 * key
	 */
	public Object getData(String key) {
		if (key == null) {
			throw new IllegalArgumentException();
		}
		return extraData.get(key);
	}
	
	/**
	 * Sets the data associated with the workbench at the given key.
	 * 
	 * @param key the key
	 * @param data the data, or <code>null</code> to delete existing data
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
}
