/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.application;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.window.WindowManager;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * Interface providing special access for configuring the workbench.
 * <p>
 * Note that these objects are only available to the main application
 * (the plug-in that creates and owns the workbench).
 * </p>
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * 
 * @see WorkbenchAdvisor#initialize
 * @since 3.0
 */
public interface IWorkbenchConfigurer {

    /**
     * Restore status code indicating that the saved state
     * could not be restored, but that startup should continue
     * with a reset state.
     * 
     * @see #restoreState
     */
    public static final int RESTORE_CODE_RESET = 1;

    /**
     * Restore status code indicating that the saved state
     * could not be restored, and that the application
     * must exit immediately without modifying any previously
     * saved workbench state.
     */
    public static final int RESTORE_CODE_EXIT = 2;

    /**
     * Returns the underlying workbench.
     * 
     * @return the workbench
     */
    public IWorkbench getWorkbench();

    /**
     * Returns whether the workbench state should be saved on close and 
     * restored on subsequent open.  
     * <p>
     * The initial value is <code>false</code>.
     * </p>
     * 
     * @return <code>true</code> to save and restore workbench state, or
     * 	<code>false</code> to forget current workbench state on close.
     */
    public boolean getSaveAndRestore();

    /**
     * Sets whether the workbench state should be saved on close and 
     * restored on subsequent open.
     * 
     * @param enabled <code>true</code> to save and restore workbench state, or
     * 	<code>false</code> to forget current workbench state on close.
     */
    public void setSaveAndRestore(boolean enabled);

    /**
     * Returns the workbench window manager.
     *
     * @return the workbench window manager
     * 
     * Note:IWorkbenchWindow is implemented using JFace's Window (and therefore uses WindowManager), 
     *   but this is an implementation detail
     */
    public WindowManager getWorkbenchWindowManager();

    /**
     * Declares a workbench image.
     * <p>
     * The workbench remembers the given image descriptor under the given name,
     * and makes the image available to plug-ins via
     * {@link IWorkbench#getSharedImages() IWorkbench.getSharedImages()}.
     * For "shared" images, the workbench remembers the image descriptor and
     * will manages the image object create from it; clients retrieve "shared"
     * images via
     * {@link org.eclipse.ui.ISharedImages#getImage ISharedImages.getImage()}.
     * For the other, "non-shared" images, the workbench remembers only the
     * image descriptor; clients retrieve the image descriptor via
     * {@link org.eclipse.ui.ISharedImages#getImageDescriptor
     * ISharedImages.getImageDescriptor()} and are entirely
     * responsible for managing the image objects they create from it.
     * (This is made confusing by the historical fact that the API interface
     *  is called "ISharedImages".)
     * </p>
     * 
     * @param symbolicName the symbolic name of the image
     * @param descriptor the image descriptor
     * @param shared <code>true</code> if this is a shared image, and
     * <code>false</code> if this is not a shared image
     * @see org.eclipse.ui.ISharedImages#getImage
     * @see org.eclipse.ui.ISharedImages#getImageDescriptor
     */
    public void declareImage(String symbolicName, ImageDescriptor descriptor,
            boolean shared);

    /**
     * Forces the workbench to close due to an emergency. This method should
     * only be called when the workbench is in dire straights and cannot
     * continue, and cannot even risk a normal workbench close (think "out of
     * memory" or "unable to create shell"). When this method is called, an
     * abbreviated workbench shutdown sequence is performed (less critical
     * steps may be skipped). The workbench advisor is still called; however,
     * it must not attempt to communicate with the user. While an emergency
     * close is in progress, <code>emergencyClosing</code> returns
     * <code>true</code>. Workbench advisor methods should always check this
     * flag before communicating with the user.
     * 
     * @see #emergencyClosing
     */
    public void emergencyClose();

    /**
     * Returns whether the workbench is being closed due to an emergency.
     * When this method returns <code>true</code>, the workbench is in dire
     * straights and cannot continue. Indeed, things are so bad that we cannot
     * even risk a normal workbench close. Workbench advisor methods should
     * always check this flag before attempting to communicate with the user.
     * 
     * @return <code>true</code> if the workbench is in the process of being
     * closed under emergency conditions, and <code>false</code> otherwise
     */
    public boolean emergencyClosing();

    /**
     * Returns an object that can be used to configure the given window.
     * 
     * @param window a workbench window
     * @return a workbench window configurer
     */
    public IWorkbenchWindowConfigurer getWindowConfigurer(
            IWorkbenchWindow window);

    /**
     * Returns the data associated with the workbench at the given key.
     * 
     * @param key the key
     * @return the data, or <code>null</code> if there is no data at the given
     * key
     */
    public Object getData(String key);

    /**
     * Sets the data associated with the workbench at the given key.
     * 
     * @param key the key
     * @param data the data, or <code>null</code> to delete existing data
     */
    public void setData(String key, Object data);

    /**
     * Restores the workbench state saved from the previous session, if any.
     * This includes any open windows and their open perspectives, open views
     * and editors, layout information, and any customizations to the open 
     * perspectives. 
     * <p>
     * This is typically called from the advisor's <code>openWindows()</code>
     * method.
     * </p>
     * 
     * @return a status object indicating whether the restore was successful
     * @see #RESTORE_CODE_RESET
     * @see #RESTORE_CODE_EXIT
     * @see WorkbenchAdvisor#openWindows
     */
    public IStatus restoreState();

    /**
     * Opens the first time window, using the default perspective and
     * default page input.
     * <p>
     * This is typically called from the advisor's <code>openWindows()</code>
     * method.
     * </p>
     * 
     * @see WorkbenchAdvisor#openWindows
     */
    public void openFirstTimeWindow();
}