/*******************************************************************************
 * Copyright (c) 2003, 2019 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 440136
 *     Denis Zygann <d.zygann@web.de> - Bug 457390
 *******************************************************************************/
package org.eclipse.ui.application;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * Interface providing special access for configuring workbench windows.
 * <p>
 * Window configurer objects are in 1-1 correspondence with the workbench
 * windows they configure. Clients may use <code>get/setData</code> to associate
 * arbitrary state with the window configurer object.
 * </p>
 * <p>
 * Note that these objects are only available to the main application (the
 * plug-in that creates and owns the workbench).
 * </p>
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 *
 * @see IWorkbenchConfigurer#getWindowConfigurer
 * @see WorkbenchAdvisor#preWindowOpen
 * @since 3.0
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IWorkbenchWindowConfigurer {
	/**
	 * Returns the underlying workbench window.
	 *
	 * @return the workbench window
	 */
	IWorkbenchWindow getWindow();

	/**
	 * Returns the workbench configurer.
	 *
	 * @return the workbench configurer
	 */
	IWorkbenchConfigurer getWorkbenchConfigurer();

	/**
	 * Returns the action bar configurer for this workbench window.
	 *
	 * @return the action bar configurer
	 */
	IActionBarConfigurer getActionBarConfigurer();

	/**
	 * Returns the title of the underlying workbench window.
	 *
	 * @return the window title
	 */
	String getTitle();

	/**
	 * Sets the title of the underlying workbench window.
	 *
	 * @param title the window title
	 */
	void setTitle(String title);

	/**
	 * Returns whether the underlying workbench window has a menu bar.
	 * <p>
	 * The initial value is <code>true</code>.
	 * </p>
	 *
	 * @return <code>true</code> for a menu bar, and <code>false</code> for no menu
	 *         bar
	 */
	boolean getShowMenuBar();

	/**
	 * Sets whether the underlying workbench window has a menu bar.
	 *
	 * @param show <code>true</code> for a menu bar, and <code>false</code> for no
	 *             menu bar
	 */
	void setShowMenuBar(boolean show);

	/**
	 * Returns whether the underlying workbench window has a cool bar.
	 * <p>
	 * The initial value is <code>true</code>.
	 * </p>
	 *
	 * @return <code>true</code> for a cool bar, and <code>false</code> for no cool
	 *         bar
	 */
	boolean getShowCoolBar();

	/**
	 * Sets whether the underlying workbench window has a cool bar.
	 *
	 * @param show <code>true</code> for a cool bar, and <code>false</code> for no
	 *             cool bar
	 */
	void setShowCoolBar(boolean show);

	/**
	 * Returns whether the underlying workbench window has a status line.
	 * <p>
	 * The initial value is <code>true</code>.
	 * </p>
	 *
	 * @return <code>true</code> for a status line, and <code>false</code> for no
	 *         status line
	 */
	boolean getShowStatusLine();

	/**
	 * Sets whether the underlying workbench window has a status line.
	 *
	 * @param show <code>true</code> for a status line, and <code>false</code> for
	 *             no status line
	 */
	void setShowStatusLine(boolean show);

	/**
	 * Returns whether the underlying workbench window has a perspective bar (the
	 * perspective bar provides buttons to quickly switch between perspectives).
	 * <p>
	 * The initial value is <code>false</code>.
	 * </p>
	 *
	 * @return <code>true</code> for a perspective bar, and <code>false</code> for
	 *         no perspective bar
	 */
	boolean getShowPerspectiveBar();

	/**
	 * Sets whether the underlying workbench window has a perspective bar (the
	 * perspective bar provides buttons to quickly switch between perspectives).
	 *
	 * @param show <code>true</code> for a perspective bar, and <code>false</code>
	 *             for no perspective bar
	 */
	void setShowPerspectiveBar(boolean show);

	/**
	 * No longer used by the platform
	 *
	 * @return <code>true</code> for fast view bars, and <code>false</code> for no
	 *         fast view bars
	 * @noreference This method is not intended to be referenced by clients.
	 *
	 *              This method is planned to be deleted, see
	 *              https://bugs.eclipse.org/bugs/show_bug.cgi?id=485835
	 * @deprecated discontinued support for fast views
	 */
	@Deprecated
	boolean getShowFastViewBars();

	/**
	 * No longer used by the platform
	 *
	 * @param enable <code>true</code> for fast view bars, and <code>false</code>
	 *               for no fast view bars
	 * @noreference This method is not intended to be referenced by clients.
	 *
	 *              This method is planned to be deleted, see
	 *              https://bugs.eclipse.org/bugs/show_bug.cgi?id=485835
	 *
	 * @deprecated discontinued support for fast views
	 */
	@Deprecated
	void setShowFastViewBars(boolean enable);

	/**
	 * Returns whether the underlying workbench window has a progress indicator.
	 * <p>
	 * The initial value is <code>false</code>.
	 * </p>
	 *
	 * @return <code>true</code> for a progress indicator, and <code>false</code>
	 *         for no progress indicator
	 */
	boolean getShowProgressIndicator();

	/**
	 * Sets whether the underlying workbench window has a progress indicator.
	 *
	 * @param show <code>true</code> for a progress indicator, and
	 *             <code>false</code> for no progress indicator
	 */
	void setShowProgressIndicator(boolean show);

	/**
	 * Returns the style bits to use for the window's shell when it is created. The
	 * default is <code>SWT.SHELL_TRIM</code>.
	 *
	 * @return the shell style bits
	 */
	int getShellStyle();

	/**
	 * Sets the style bits to use for the window's shell when it is created. This
	 * method has no effect after the shell is created. That is, it must be called
	 * within the <code>preWindowOpen</code> callback on
	 * <code>WorkbenchAdvisor</code>.
	 * <p>
	 * For more details on the applicable shell style bits, see the documentation
	 * for {@link org.eclipse.swt.widgets.Shell}.
	 * </p>
	 *
	 * @param shellStyle the shell style bits
	 */
	void setShellStyle(int shellStyle);

	/**
	 * Returns the size to use for the window's shell when it is created.
	 *
	 * @return the initial size to use for the shell
	 */
	Point getInitialSize();

	/**
	 * Sets the size to use for the window's shell when it is created. This method
	 * has no effect after the shell is created. That is, it must be called within
	 * the <code>preWindowOpen</code> callback on <code>WorkbenchAdvisor</code>.
	 *
	 * @param initialSize the initial size to use for the shell
	 */
	void setInitialSize(Point initialSize);

	/**
	 * Returns the data associated with this workbench window at the given key.
	 *
	 * @param key the key
	 * @return the data, or <code>null</code> if there is no data at the given key
	 */
	Object getData(String key);

	/**
	 * Sets the data associated with this workbench window at the given key.
	 *
	 * @param key  the key
	 * @param data the data, or <code>null</code> to delete existing data
	 */
	void setData(String key, Object data);

	/**
	 * Adds the given drag and drop <code>Transfer</code> type to the ones supported
	 * for drag and drop on the editor area of this workbench window.
	 * <p>
	 * The workbench advisor would ordinarily call this method from the
	 * <code>preWindowOpen</code> callback. A newly-created workbench window
	 * supports no drag and drop transfer types. Adding
	 * <code>EditorInputTransfer.getInstance()</code> enables
	 * <code>IEditorInput</code>s to be transferred.
	 * </p>
	 * <p>
	 * Note that drag and drop to the editor area requires adding one or more
	 * transfer types (using <code>addEditorAreaTransfer</code>) and configuring a
	 * drop target listener (with <code>configureEditorAreaDropListener</code>)
	 * capable of handling any of those transfer types.
	 * </p>
	 *
	 * @param transfer a drag and drop transfer object
	 * @see #configureEditorAreaDropListener
	 * @see org.eclipse.ui.part.EditorInputTransfer
	 */
	void addEditorAreaTransfer(Transfer transfer);

	/**
	 * Configures the drop target listener for the editor area of this workbench
	 * window.
	 * <p>
	 * The workbench advisor ordinarily calls this method from the
	 * <code>preWindowOpen</code> callback. A newly-created workbench window has no
	 * configured drop target listener for its editor area.
	 * </p>
	 * <p>
	 * Note that drag and drop to the editor area requires adding one or more
	 * transfer types (using <code>addEditorAreaTransfer</code>) and configuring a
	 * drop target listener (with <code>configureEditorAreaDropListener</code>)
	 * capable of handling any of those transfer types.
	 * </p>
	 *
	 * @param dropTargetListener the drop target listener that will handle requests
	 *                           to drop an object on to the editor area of this
	 *                           window
	 *
	 * @see #addEditorAreaTransfer
	 */
	void configureEditorAreaDropListener(DropTargetListener dropTargetListener);

	/**
	 * No longer used by the platform
	 *
	 * @return the menu bar, suitable for setting in the shell extended by clients.
	 * @noreference This method is not intended to be referenced by clients.
	 *
	 *              This method is planned to be deleted, see (
	 *              https://bugs.eclipse.org/bugs/show_bug.cgi?id=485835
	 * @deprecated This method is no longer used. Applications now define workbench
	 *             window contents in their application model.
	 */
	@Deprecated
	Menu createMenuBar();

	/**
	 * No longer used by the platform
	 *
	 * @param parent the parent composite
	 * @return the cool bar control, suitable for laying out in the parent
	 * @noreference This method is not intended to be referenced by clients.
	 *
	 *              This method is planned to be deleted, see
	 *              https://bugs.eclipse.org/bugs/show_bug.cgi?id=485835
	 * @deprecated This method is no longer used. Applications now define workbench
	 *             window contents in their application model.
	 */
	@Deprecated
	Control createCoolBarControl(Composite parent);

	/**
	 * No longer used by the platform
	 *
	 * @param parent the parent composite
	 * @return the status line control, suitable for laying out in the parent
	 * @noreference This method is not intended to be referenced by clients.
	 *
	 *              This method is planned to be deleted, see
	 *              https://bugs.eclipse.org/bugs/show_bug.cgi?id=485835
	 * @deprecated This method is no longer used. Applications now define workbench
	 *             window contents in their application model.
	 */
	@Deprecated
	Control createStatusLineControl(Composite parent);

	/**
	 * No longer used by the platform
	 *
	 * @param parent the parent composite
	 * @return the page composite, suitable for laying out in the parent
	 *
	 * @noreference This method is not intended to be referenced by clients.
	 *
	 *              This method is planned to be deleted, see
	 *              https://bugs.eclipse.org/bugs/show_bug.cgi?id=485835
	 * @deprecated This method is no longer used. Applications now define workbench
	 *             window contents in their application model.
	 */
	@Deprecated
	Control createPageComposite(Composite parent);

	/**
	 * Saves the current state of the window using the specified memento.
	 *
	 * @param memento the memento in which to save the window's state
	 * @return a status object indicating whether the save was successful
	 * @see IWorkbenchConfigurer#restoreWorkbenchWindow(IMemento)
	 * @since 3.1
	 */
	IStatus saveState(IMemento memento);
}
