/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM Corporation - Initial implementation
**********************************************************************/

package org.eclipse.jface.window;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.NullProgressMonitor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.operation.ModalContext;
import org.eclipse.jface.resource.JFaceResources;

/**
 * An application window is a high-level "main window", with built-in
 * support for an optional menu bar with standard menus, an optional toolbar,
 * and an optional status line.
 * <p>
 * Creating an application window involves the following steps:
 * <ul>
 *   <li>creating an instance of <code>ApplicationWindow</code>
 *   </li>
 *   <li>assigning the window to a window manager (optional)
 *   </li>
 *   <li>opening the window by calling <code>open</code>
 *   </li>
 * </ul>
 * Only on the last step, when the window is told to open, are
 * the window's shell and widget tree created. When the window is
 * closed, the shell and widget tree are disposed of and are no longer
 * referenced, and the window is automatically removed from its window
 * manager. Like all windows, an application window may be reopened.
 * </p>
 * <p>
 * An application window is also a suitable context in which to perform 
 * long-running operations (that is, it implements <code>IRunnableContext</code>).
 * </p>
 */
public class ApplicationWindow extends Window implements IRunnableContext {

	/**
	 * Menu bar manager, or <code>null</code> if none (default).
	 *
	 * @see #addMenuBar
	 */
	private MenuManager menuBarManager = null;

	/**
	 * Tool bar manager, or <code>null</code> if none (default).
	 *
	 * @see #addToolBar
	 */
	private ToolBarManager toolBarManager = null;

	/**
	 * Status line manager, or <code>null</code> if none (default).
	 *
	 * @see #addStatusLine
	 */
	private StatusLineManager statusLineManager = null;
	
	/**
	 * A flag indicating that an operation is running.
	 */
	private boolean operationInProgress = false;
	
	/**
	 * Internal application window layout class.
	 * This vertical layout supports a tool bar area (fixed size),
	 * a separator line, the content area (variable size), and a 
	 * status line (fixed size).
	 */
	/*package*/ class ApplicationWindowLayout extends Layout {
	
		static final int VGAP = 2;
			
		protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
			if (wHint != SWT.DEFAULT && hHint != SWT.DEFAULT)
				return new Point(wHint, hHint);
				
			Point result= new Point(0, 0);
			Control[] ws= composite.getChildren();
			for (int i= 0; i < ws.length; i++) {
				Control w= ws[i];
				
				boolean hide= false;
				if (getToolBarControl() == w) {
					if (!toolBarChildrenExist()) {
						hide= true;
						result.y+= 23;	// REVISIT
					} 
				} else if (statusLineManager != null && statusLineManager.getControl() == w) {
				} else if (i > 0) { /* we assume this window is contents */
					hide= false;
				}
				
				if (! hide) {
					Point e= w.computeSize(wHint, hHint, flushCache);
					result.x= Math.max(result.x, e.x);
					result.y+= e.y + VGAP;
				}
			}
			
			if (wHint != SWT.DEFAULT)
				result.x= wHint;
			if (hHint != SWT.DEFAULT)
				result.y= hHint;
			return result;
		}

		protected void layout(Composite composite, boolean flushCache) {
			Rectangle clientArea= composite.getClientArea();
	
			Control[] ws= composite.getChildren();
			
			for (int i= 0; i < ws.length; i++) {
				Control w= ws[i];
				
				if (i == 0) { // Separator
					Point e= w.computeSize(SWT.DEFAULT, SWT.DEFAULT, flushCache);
					w.setBounds(clientArea.x, clientArea.y, clientArea.width, e.y);
					clientArea.y+= e.y;
					clientArea.height-= e.y;
				} else if (getToolBarControl() == w) { 
					if (toolBarChildrenExist()) {
						Point e= w.computeSize(SWT.DEFAULT, SWT.DEFAULT, flushCache);
						w.setBounds(clientArea.x, clientArea.y, clientArea.width, e.y);
						clientArea.y+= e.y + VGAP;
						clientArea.height-= e.y + VGAP;
					} 
				} else if (statusLineManager != null && statusLineManager.getControl() == w) {
					Point e= w.computeSize(SWT.DEFAULT, SWT.DEFAULT, flushCache);
					w.setBounds(clientArea.x, clientArea.y+clientArea.height-e.y, clientArea.width, e.y);
					clientArea.height-= e.y + VGAP;
				} else {
					w.setBounds(clientArea.x, clientArea.y + VGAP, clientArea.width, clientArea.height - VGAP);
				}
			}
		}
	}
	
/**
 * Create an application window instance, whose shell will be created under the
 * given parent shell.
 * Note that the window will have no visual representation (no widgets)
 * until it is told to open. By default, <code>open</code> does not block.
 *
 * @param parentShell the parent shell, or <code>null</code> to create a top-level shell
 */
public ApplicationWindow(Shell parentShell) {
	super(parentShell);
}
/**
 * Configures this window to have a menu bar.
 * Does nothing if it already has one.
 * This method must be called before this window's shell is created.
 */
protected void addMenuBar() {
	if ((getShell() == null) && (menuBarManager == null)) {
		menuBarManager = createMenuManager();
	}
}
/**
 * Configures this window to have a status line.
 * Does nothing if it already has one.
 * This method must be called before this window's shell is created.
 */
protected void addStatusLine() {
	if ((getShell() == null) && (statusLineManager == null)) {
		statusLineManager = createStatusLineManager();
	}
}
/**
 * Configures this window to have a tool bar.
 * Does nothing if it already has one.
 * This method must be called before this window's shell is created.
 */
protected void addToolBar(int style) {
	if ((getShell() == null) && (toolBarManager == null)) {
		toolBarManager = createToolBarManager(style);
	}
}
/* (non-Javadoc)
 * Method declared on Window.
 */ 
protected void handleShellCloseEvent() {
	if(operationInProgress)
		return;
	super.handleShellCloseEvent();
}
/* (non-Javadoc)
 * Method declared on Window.
 */
public boolean close() {
	if(operationInProgress)
		return false;
		
	if (super.close()) {
		menuBarManager = null;
		toolBarManager = null;
		statusLineManager = null;
		return true;
	}
	return false;
}
/* (non-Javadoc)
 * Method declared on Window.
 * Sets the ApplicationWindows's content layout.
 * This vertical layout supports a fixed size Toolbar area, a separator line,
 * the variable size content area,
 * and a fixed size status line.
 */
protected void configureShell(Shell shell) {

	super.configureShell(shell);
	
	if (menuBarManager != null) {
		menuBarManager.updateAll(true);
		shell.setMenuBar(menuBarManager.createMenuBar(shell));
	}

	// we need a special layout
	shell.setLayout(new ApplicationWindowLayout());

	if (! "carbon".equals(SWT.getPlatform()))
		 new Label(shell, SWT.SEPARATOR | SWT.HORIZONTAL);

	createToolBarControl(shell);

	if (statusLineManager != null) {
		statusLineManager.createControl(shell);
	}
}
/**
 * Returns a new menu manager for the window.
 * <p>
 * Subclasses may override this method to customize the menu manager.
 * </p>
 * @return a menu manager
 */
protected MenuManager createMenuManager() {
	return new MenuManager();
}
/**
 * Returns a new status line manager for the window.
 * <p>
 * Subclasses may override this method to customize the status line manager.
 * </p>
 * @return a status line manager
 */
protected StatusLineManager createStatusLineManager() {
	return new StatusLineManager();
}
/**
 * Returns a new tool bar manager for the window.
 * <p>
 * Subclasses may override this method to customize the tool bar manager.
 * </p>
 * @return a tool bar manager
 */
protected ToolBarManager createToolBarManager(int style) {
	return new ToolBarManager(style);
}
/**
 * Creates the control for the tool bar manager.
 * <p>
 * Subclasses may override this method to customize the tool bar manager.
 * </p>
 * @return a Control
 */
protected Control createToolBarControl(Shell shell) {
	if (toolBarManager instanceof ToolBarManager) {
		return ((ToolBarManager)toolBarManager).createControl(shell);
	} 
	return null;
}
/**
 * Returns the default font used for this window.
 * <p>
 * The default implementation of this framework method
 * obtains the symbolic name of the font from the
 * <code>getSymbolicFontName</code> framework method
 * and retrieves this font from JFace's font
 * registry using <code>JFaceResources.getFont</code>.
 * Subclasses may override to use a different registry,
 * etc.
 * </p>
 *
 * @return the default font, or <code>null</code> if none
 */
protected Font getFont() {
	return JFaceResources.getFont(getSymbolicFontName());
}
/**
 * Returns the menu bar manager for this window (if it has one).
 *
 * @return the menu bar manager, or <code>null</code> if
 *   this window does not have a menu bar
 * @see #addMenuBar
 */
public MenuManager getMenuBarManager() {
	return menuBarManager;
}
/**
 * Returns the status line manager for this window (if it has one).
 *
 * @return the status line manager, or <code>null</code> if
 *   this window does not have a status line
 * @see #addStatusLine
 */
protected StatusLineManager getStatusLineManager() {
	return statusLineManager;
}

/**
 * Returns the symbolic font name of the font to be
 * used to display text in this window.
 * This is not recommended and is included for backwards
 * compatability.
 * It is recommended to use the default font provided by
 * SWT (that is, do not set the font).
 * 
 * @return the symbolic font name
 */
public String getSymbolicFontName() {
	return JFaceResources.TEXT_FONT;
}
/**
 * Returns the tool bar manager for this window (if it has one).
 *
 * @return the tool bar manager, or <code>null</code> if
 *   this window does not have a tool bar
 * @see #addToolBar
 */
public ToolBarManager getToolBarManager() {
	return toolBarManager;
}
/**
 * Returns the control for the window's toolbar.
 * <p>
 * Subclasses may override this method to customize the tool bar manager.
 * </p>
 * @return a Control
 */
protected Control getToolBarControl() {
	if (toolBarManager == null) return null;
	if (toolBarManager instanceof ToolBarManager) {
		return ((ToolBarManager)toolBarManager).getControl();
	}
	return null;
}
/* (non-Javadoc)
 * Method declared on IRunnableContext.
 */
public void run(final boolean fork, boolean cancelable, final IRunnableWithProgress runnable) throws InvocationTargetException, InterruptedException {
	try {
		operationInProgress = true;
		final StatusLineManager mgr = getStatusLineManager();
		if (mgr == null) {
			runnable.run(new NullProgressMonitor());
			return;
		}
		boolean cancelWasEnabled = mgr.isCancelEnabled();
	
		final Control contents = getContents();
		final Display display = contents.getDisplay();
		Shell shell = getShell();
		boolean contentsWasEnabled = contents.isEnabled();
		MenuManager manager = getMenuBarManager();
		Menu menuBar = null;
		if (manager != null) {
			menuBar = manager.getMenu();
			manager = null;
		}
		boolean menuBarWasEnabled = false;
		if (menuBar != null)
			menuBarWasEnabled = menuBar.isEnabled();
	
		Control toolbarControl = getToolBarControl();
		boolean toolbarWasEnabled = false;
		if (toolbarControl != null) 
			toolbarWasEnabled = toolbarControl.isEnabled();
	
		// Disable the rest of the shells on the current display
		Shell[] shells = display.getShells();
		boolean[] enabled = new boolean[shells.length];
		for (int i = 0; i < shells.length; i++) {
			Shell current = shells[i];
			if (current == shell) continue;
			if (current != null && !current.isDisposed()) {
				enabled[i] = current.isEnabled();
				current.setEnabled(false);
			}
		}
	
		Control currentFocus = display.getFocusControl();
		try {
			contents.setEnabled(false);
			if (menuBar != null) menuBar.setEnabled(false);
			if (toolbarControl != null) toolbarControl.setEnabled(false);
			mgr.setCancelEnabled(cancelable);
			final Exception[] holder = new Exception[1];
			BusyIndicator.showWhile(display, new Runnable() {
				public void run() {
					try {
						ModalContext.run(runnable, fork, mgr.getProgressMonitor(), display);
					} catch (InvocationTargetException ite) {
						holder[0] = ite;
					} catch (InterruptedException ie) {
						holder[0] = ie;
					}
				}});
	
			if (holder[0] != null) {
				if (holder[0] instanceof InvocationTargetException) {
					throw (InvocationTargetException) holder[0];
				} else if (holder[0] instanceof InterruptedException) {
					throw (InterruptedException) holder[0];
				}
			}
		} finally {
			operationInProgress = false;
			// Enable the rest of the shells on the current display
			for (int i = 0; i < shells.length; i++) {
				Shell current = shells[i];
				if (current == shell) continue;
				if (current != null && !current.isDisposed()) {
					current.setEnabled(enabled[i]);
				}
			}
			if (!contents.isDisposed())
				contents.setEnabled(contentsWasEnabled);
			if (menuBar != null && !menuBar.isDisposed())
				menuBar.setEnabled(menuBarWasEnabled);
			if (toolbarControl != null && !toolbarControl.isDisposed())
				toolbarControl.setEnabled(toolbarWasEnabled);
			mgr.setCancelEnabled(cancelWasEnabled);
			if (currentFocus != null && !currentFocus.isDisposed()) currentFocus.setFocus();
		}
	} finally {
		operationInProgress = false;
	}
}
/**
 * Sets or clears the message displayed in this window's status
 * line (if it has one). This method has no effect if the
 * window does not have a status line.
 *
 * @param message the status message, or <code>null</code> to clear it
 */
public void setStatus(String message) {
	if (statusLineManager != null) {
		statusLineManager.setMessage(message);
	}
}
/**
 * Returns whether or not children exist for the Application Window's
 * toolbar control.
 * <p>
 * @return boolean true if children exist, false otherwise
 */
protected boolean toolBarChildrenExist() {
	Control toolControl = getToolBarControl();
	if (toolControl instanceof ToolBar) {
		return ((ToolBar)toolControl).getItemCount() > 0;
	}
	return false;
}

}
