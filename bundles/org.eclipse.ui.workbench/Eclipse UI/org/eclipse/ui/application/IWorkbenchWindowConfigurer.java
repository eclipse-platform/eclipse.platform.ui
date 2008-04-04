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
import org.eclipse.ui.presentations.AbstractPresentationFactory;

/**
 * Interface providing special access for configuring workbench windows.
 * <p>
 * Window configurer objects are in 1-1 correspondence with the workbench
 * windows they configure. Clients may use <code>get/setData</code> to
 * associate arbitrary state with the window configurer object.
 * </p>
 * <p>
 * Note that these objects are only available to the main application
 * (the plug-in that creates and owns the workbench).
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
    public IWorkbenchWindow getWindow();

    /**
     * Returns the workbench configurer.
     * 
     * @return the workbench configurer
     */
    public IWorkbenchConfigurer getWorkbenchConfigurer();

    /**
     * Returns the action bar configurer for this workbench
     * window.
     * 
     * @return the action bar configurer
     */
    public IActionBarConfigurer getActionBarConfigurer();

    /**
     * Returns the title of the underlying workbench window.
     * 
     * @return the window title
     */
    public String getTitle();

    /**
     * Sets the title of the underlying workbench window.
     * 
     * @param title the window title
     */
    public void setTitle(String title);

    /**
     * Returns whether the underlying workbench window has a menu bar.
     * <p>
     * The initial value is <code>true</code>.
     * </p>
     * 
     * @return <code>true</code> for a menu bar, and <code>false</code>
     * for no menu bar
     */
    public boolean getShowMenuBar();

    /**
     * Sets whether the underlying workbench window has a menu bar.
     * 
     * @param show <code>true</code> for a menu bar, and <code>false</code>
     * for no menu bar
     */
    public void setShowMenuBar(boolean show);

    /**
     * Returns whether the underlying workbench window has a cool bar.
     * <p>
     * The initial value is <code>true</code>.
     * </p>
     * 
     * @return <code>true</code> for a cool bar, and <code>false</code>
     * for no cool bar
     */
    public boolean getShowCoolBar();

    /**
     * Sets whether the underlying workbench window has a cool bar.
     * 
     * @param show <code>true</code> for a cool bar, and <code>false</code>
     * for no cool bar
     */
    public void setShowCoolBar(boolean show);

    /**
     * Returns whether the underlying workbench window has a status line.
     * <p>
     * The initial value is <code>true</code>.
     * </p>
     * 
     * @return <code>true</code> for a status line, and <code>false</code>
     * for no status line
     */
    public boolean getShowStatusLine();

    /**
     * Sets whether the underlying workbench window has a status line.
     * 
     * @param show <code>true</code> for a status line, and <code>false</code>
     * for no status line
     */
    public void setShowStatusLine(boolean show);

    /**
     * Returns whether the underlying workbench window has a perspective bar (the
     * perspective bar provides buttons to quickly switch between perspectives).
     * <p>
     * The initial value is <code>false</code>.
     * </p>
     * 
     * @return <code>true</code> for a perspective bar, and <code>false</code>
     * for no perspective bar
     */
    public boolean getShowPerspectiveBar();

    /**
     * Sets whether the underlying workbench window has a perspective bar (the 
     * perspective bar provides buttons to quickly switch between perspectives).
     * 
     * @param show <code>true</code> for a perspective bar, and
     * <code>false</code> for no perspective bar
     */
    public void setShowPerspectiveBar(boolean show);

    /**
     * Returns whether the underlying workbench window has fast view bars.
     * <p>
     * The initial value is <code>false</code>.
     * </p>
     * 
     * @return <code>true</code> for fast view bars, and 
     * <code>false</code> for no fast view bars
     */
    public boolean getShowFastViewBars();

    /**
     * Sets whether the underlying workbench window has fast view bars. 
     * 
     * @param enable <code>true</code> for fast view bars, and 
     * <code>false</code> for no fast view bars
     */
    public void setShowFastViewBars(boolean enable);

    /**
     * Returns whether the underlying workbench window has a progress indicator.
     * <p>
     * The initial value is <code>false</code>.
     * </p>
     * 
     * @return <code>true</code> for a progress indicator, and <code>false</code>
     * for no progress indicator
     */
    public boolean getShowProgressIndicator();

    /**
     * Sets whether the underlying workbench window has a progress indicator.
     * 
     * @param show <code>true</code> for a progress indicator, and <code>false</code>
     * for no progress indicator
     */
    public void setShowProgressIndicator(boolean show);

    /**
     * Returns the style bits to use for the window's shell when it is created.
     * The default is <code>SWT.SHELL_TRIM</code>.
     *
     * @return the shell style bits
     */
    public int getShellStyle();

    /**
     * Sets the style bits to use for the window's shell when it is created.
     * This method has no effect after the shell is created.
     * That is, it must be called within the <code>preWindowOpen</code>
     * callback on <code>WorkbenchAdvisor</code>.
     * <p>
     * For more details on the applicable shell style bits, see the
     * documentation for {@link org.eclipse.swt.widgets.Shell}.
     * </p>
     *
     * @param shellStyle the shell style bits
     */
    public void setShellStyle(int shellStyle);

    /**
     * Returns the size to use for the window's shell when it is created.
     *
     * @return the initial size to use for the shell
     */
    public Point getInitialSize();

    /**
     * Sets the size to use for the window's shell when it is created.
     * This method has no effect after the shell is created.
     * That is, it must be called within the <code>preWindowOpen</code>
     * callback on <code>WorkbenchAdvisor</code>.
     *
     * @param initialSize the initial size to use for the shell
     */
    public void setInitialSize(Point initialSize);

    /**
     * Returns the data associated with this workbench window at the given key.
     * 
     * @param key the key
     * @return the data, or <code>null</code> if there is no data at the given
     * key
     */
    public Object getData(String key);

    /**
     * Sets the data associated with this workbench window at the given key.
     * 
     * @param key the key
     * @param data the data, or <code>null</code> to delete existing data
     */
    public void setData(String key, Object data);

    /**
     * Adds the given drag and drop <code>Transfer</code> type to the ones
     * supported for drag and drop on the editor area of this workbench window.
     * <p>
     * The workbench advisor would ordinarily call this method from the
     * <code>preWindowOpen</code> callback.
     * A newly-created workbench window supports no drag and drop transfer
     * types. Adding <code>EditorInputTransfer.getInstance()</code>
     * enables <code>IEditorInput</code>s to be transferred. 
     * </p>
     * <p>
     * Note that drag and drop to the editor area requires adding one or more
     * transfer types (using <code>addEditorAreaTransfer</code>) and 
     * configuring a drop target listener
     * (with <code>configureEditorAreaDropListener</code>)
     * capable of handling any of those transfer types.
     * </p>
     * 
     * @param transfer a drag and drop transfer object
     * @see #configureEditorAreaDropListener
     * @see org.eclipse.ui.part.EditorInputTransfer
     */
    public void addEditorAreaTransfer(Transfer transfer);

    /**
     * Configures the drop target listener for the editor area of this workbench window.
     * <p>
     * The workbench advisor ordinarily calls this method from the
     * <code>preWindowOpen</code> callback.
     * A newly-created workbench window has no configured drop target listener for its
     * editor area.
     * </p>
     * <p>
     * Note that drag and drop to the editor area requires adding one or more
     * transfer types (using <code>addEditorAreaTransfer</code>) and 
     * configuring a drop target listener
     * (with <code>configureEditorAreaDropListener</code>)
     * capable of handling any of those transfer types.
     * </p>
     * 
     * @param dropTargetListener the drop target listener that will handle
     * requests to drop an object on to the editor area of this window
     * 
     * @see #addEditorAreaTransfer
     */
    public void configureEditorAreaDropListener(
            DropTargetListener dropTargetListener);

    /**
     * Returns the presentation factory for this window.  The window consults its presentation
     * factory for the presentation aspects of views, editors, status lines, and other components
     * of the window.
     * <p>
     * If no presentation factory has been set, a default one is returned.
     * </p>
     * 
     * @return the presentation factory used for this window
     * 
     * @deprecated the presentation factory is now obtained via extension point
     *   and a preference on org.eclipse.ui specifying which one to use;
     *   see IWorkbenchPreferenceConstants.PRESENTATION_FACTORY_ID
     */
    public AbstractPresentationFactory getPresentationFactory();

    /**
     * Sets the presentation factory.  The window consults its presentation
     * factory for the presentation aspects of views, editors, status lines, and other components
     * of the window.  
     * <p>
     * This must be called before the window's controls are created, for example
     * in <code>preWindowOpen</code>.
     * </p>
     * 
     * @param factory the presentation factory to use for this window
     * 
     * @deprecated the presentation factory is now obtained via extension point
     *   and a preference on org.eclipse.ui specifying which one to use;
     *   see IWorkbenchPreferenceConstants.PRESENTATION_FACTORY_ID
     */
    public void setPresentationFactory(AbstractPresentationFactory factory);

    /**
     * Creates the menu bar for the window's shell.
     * <p>
     * This should only be called if the advisor is defining custom window contents
     * in <code>createWindowContents</code>, and may only be called once.
     * The caller must set it in the shell using <code>Shell.setMenuBar(Menu)</code>
     * but must not make add, remove or change items in the result.
     * The menu bar is populated by the window's menu manager.  
     * The application can add to the menu manager in the advisor's 
     * <code>fillActionBars</code> method instead.
     * </p>
     * 
     * @return the menu bar, suitable for setting in the shell
     */
    public Menu createMenuBar();

    /**
     * Creates the cool bar control. 
     * <p>
     * This should only be called if the advisor is defining custom window contents
     * in <code>createWindowContents</code>, and may only be called once.
     * The caller must lay out the cool bar appropriately within the parent,
     * but must not add, remove or change items in the result (hence the
     * return type of <code>Control</code>).
     * The cool bar is populated by the window's cool bar manager.  
     * The application can add to the cool bar manager in the advisor's 
     * <code>fillActionBars</code> method instead.
     * </p>
     * 
     * @param parent the parent composite
     * @return the cool bar control, suitable for laying out in the parent 
     */
    public Control createCoolBarControl(Composite parent);

    /**
     * Creates the status line control. 
     * <p>
     * This should only be called if the advisor is defining custom window contents
     * in <code>createWindowContents</code>, and may only be called once.
     * The caller must lay out the status line appropriately within the parent,
     * but must not add, remove or change items in the result (hence the
     * return type of <code>Control</code>).
     * The status line is populated by the window's status line manager.  
     * The application can add to the status line manager in the advisor's 
     * <code>fillActionBars</code> method instead.
     * </p>
     * 
     * @param parent the parent composite
     * @return the status line control, suitable for laying out in the parent 
     */
    public Control createStatusLineControl(Composite parent);

    /**
     * Creates the page composite, in which the window's pages, and their
     * views and editors, appear.
     * <p>
     * This should only be called if the advisor is defining custom window contents
     * in <code>createWindowContents</code>, and may only be called once.
     * The caller must lay out the page composite appropriately within the parent,
     * but must not add, remove or change items in the result (hence the
     * return type of <code>Control</code>).
     * The page composite is populated by the workbench.  
     * </p>
     * 
     * @param parent the parent composite
     * @return the page composite, suitable for laying out in the parent 
     */
    public Control createPageComposite(Composite parent);
	
	/**
	 * Saves the current state of the window using the specified memento.
	 * 
	 * @param memento the memento in which to save the window's state
	 * @return a status object indicating whether the save was successful
     * @see IWorkbenchConfigurer#restoreWorkbenchWindow(IMemento)
	 * @since 3.1
	 */
	public IStatus saveState(IMemento memento);
}
