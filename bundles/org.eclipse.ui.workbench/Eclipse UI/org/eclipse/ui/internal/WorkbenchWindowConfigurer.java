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

import org.eclipse.ui.IWorkbenchWindow;
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
	 * Table to hold arbitrary key-data settings (key type: <code>String</code>,
	 * value type: <code>Object</code>).
	 * @see #setData
	 */
	private Map extraData = new HashMap(1);
	
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
	WorkbenchWindowConfigurer(IWorkbenchWindow window) {
		if (window == null || !(window instanceof WorkbenchWindow)) {
			throw new IllegalArgumentException();
		}
		this.window = (WorkbenchWindow) window;
	}

	/* (non-javadoc)
	 * @see org.eclipse.ui.application.IWorkbenchWindowConfigurer#getWindow
	 */
	public IWorkbenchWindow getWindow() {
		return window;
	}
	
	/* (non-javadoc)
	 * @see org.eclipse.ui.application.IWorkbenchWindowConfigurer#getTitle
	 */
	public String getTitle() {
		String title = window.getShell().getText();
		return title;
	}
	
	/* (non-javadoc)
	 * @see org.eclipse.ui.application.IWorkbenchWindowConfigurer#setTitle
	 */
	public void setTitle(String title) {
		if (title == null) {
			throw new IllegalArgumentException();
		}
		window.getShell().setText(title);
	}
	
	/* (non-javadoc)
	 * @see org.eclipse.ui.application.IWorkbenchWindowConfigurer#getShowTitleBar
	 */
	public boolean getShowTitleBar() {
		return window.getShowTitleBar();
	}

	/* (non-javadoc)
	 * @see org.eclipse.ui.application.IWorkbenchWindowConfigurer#setShowTitleBar
	 */
	public void setShowTitleBar(boolean show) {
		window.setShowTitleBar(show);
	}

	/* (non-javadoc)
	 * @see org.eclipse.ui.application.IWorkbenchWindowConfigurer#getShowMenuBar
	 */
	public boolean getShowMenuBar() {
		return window.getShowMenuBar();
	}

	/* (non-javadoc)
	 * @see org.eclipse.ui.application.IWorkbenchWindowConfigurer#setShowMenuBar
	 */
	public void setShowMenuBar(boolean show) {
		window.setShowMenuBar(show);
	}

	/* (non-javadoc)
	 * @see org.eclipse.ui.application.IWorkbenchWindowConfigurer#getShowToolBar
	 */
	public boolean getShowToolBar() {
		return window.getShowToolBar();
	}

	/* (non-javadoc)
	 * @see org.eclipse.ui.application.IWorkbenchWindowConfigurer#setShowToolBar
	 */
	public void setShowToolBar(boolean show) {
		window.setShowToolBar(show);
	}

	/* (non-javadoc)
	 * @see org.eclipse.ui.application.IWorkbenchWindowConfigurer#getShowShortcutBar
	 */
	public boolean getShowShortcutBar() {
		return window.getShowShortcutBar();
	}

	/* (non-javadoc)
	 * @see org.eclipse.ui.application.IWorkbenchWindowConfigurer#setShowShortcutBar
	 */
	public void setShowShortcutBar(boolean show) {
		window.setShowShortcutBar(show);
	}

	/* (non-javadoc)
	 * @see org.eclipse.ui.application.IWorkbenchWindowConfigurer#getShowStatusLine
	 */
	public boolean getShowStatusLine() {
		return window.getShowStatusLine();
	}

	/* (non-javadoc)
	 * @see org.eclipse.ui.application.IWorkbenchWindowConfigurer#setShowStatusLine
	 */
	public void setShowStatusLine(boolean show) {
		window.setShowStatusLine(show);
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
}

