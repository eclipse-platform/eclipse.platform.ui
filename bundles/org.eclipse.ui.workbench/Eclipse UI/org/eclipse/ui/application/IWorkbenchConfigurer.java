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
package org.eclipse.ui.application;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.window.WindowManager;
import org.eclipse.ui.AboutInfo;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.WorkbenchException;

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
 * @see WorkbenchAdviser#initialize
 * @since 3.0
 */
public interface IWorkbenchConfigurer {
	
	/**
	 * Returns the underlying workbench.
	 * 
	 * @return the workbench
	 */
	public IWorkbench getWorkbench();

	/**
	 * Returns the configuration information found in the
	 * <code>about.ini</code> file for the primary feature.
	 * Fails if the <code>about.ini</code> file cannot be opened
	 * and parsed correctly.
	 * 
	 * @return the configuration information for the primary feature
	 * @exception WorkbenchException if the information cannot be retrieved
	 * @issue spec should be less specific about where about info comes from
	 * @issue there does not need to be a special method for the primary feature
	 */
	public AboutInfo getPrimaryFeatureAboutInfo() throws WorkbenchException;
	
	/**
	 * Returns the configuration information found in the
	 * <code>about.ini</code> file for all installed features.
	 * Fails if the <code>about.ini</code> file cannot be opened
	 * and parsed correctly.
	 * 
	 * @return the configuration information for all features
	 * @exception WorkbenchException if the information cannot be retrieved
	 * @issue spec should be less specific about where about info comes from
	 */
	public AboutInfo[] getAllFeaturesAboutInfo() throws WorkbenchException;
	
	/**
	 * Returns the configuration information found in the
	 * <code>about.ini</code> file for all new installed features
	 * since the last time the workbench was started. Fails if the
	 * <code>about.ini</code> file cannot be opened and parsed
	 * correctly.
	 * 
	 * @return the configuration information for new installed features
	 * @exception WorkbenchException if the information cannot be retrieved
	 * @issue spec should be less specific about where about info comes from
	 * @issue there does not need to be a special method for the primary feature; there just needs to be a way to find out id of primary feature and obtain about info for that id
	 */
	public AboutInfo[] getNewFeaturesAboutInfo() throws WorkbenchException;
	
	/**
	 * Returns whether the workbench state should be saved on close and 
	 * restored on subsequence open.
	 * 
	 * @return <code>true</code> to save and restore workbench state, or
	 * 	<code>false</code> to forget current workbench state on close.
	 */
	public boolean getSaveAndRestore();

	/**
	 * Sets whether the workbench state should be saved on close and 
	 * restored on subsequence open.
	 * 
	 * @param enabled <code>true</code> to save and restore workbench state, or
	 * 	<code>false</code> to forget current workbench state on close.
	 */	
	public void setSaveAndRestore(boolean enabled);
	
	/**
	 * Returns the workbench window manager.
	 *
	 * @return the workbench window manager
	 */
	public WindowManager getWorkbenchWindowManager();
	
	/**
	 * Declares a workbench image.
	 * <p>
	 * The workbench remembers the given image descriptor under the given name,
	 * and makes the image available to plug-ins via
	 * {@link org.eclipse.ui.ISharedImages IWorkbench.getSharedImages()}.
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
	public void declareImage(String symbolicName, ImageDescriptor descriptor, boolean shared);

	/**
	 * Forces the workbench to close due to an emergency. This method should
	 * only be called when the workbench is in dire straights and cannot
	 * continue, and cannot even risk a normal workbench close (think "out of
	 * memory" or "unable to create shell"). When this method is called, an
	 * abbreviated workbench shutdown sequence is performed (less critical
	 * steps may be skipped). The workbench adviser is still called; however,
	 * it must not attempt to communicate with the user. While an emergency
	 * close is in progress, <code>emergencyClosing</code> returns
	 * <code>true</code>. Workbench adviser methods should always check this
	 * flag before communicating with the user.
	 * 
	 * @see #emergencyClosing
	 */
	public void emergencyClose();

	/**
	 * Returns whether the workbench is being closed due to an emergency.
	 * When this method returns <code>true</code>, the workbench is in dire
	 * straights and cannot continue. Indeed, things are so bad that we cannot
	 * even risk a normal workbench close. Workbench adviser methods should
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
	public IWorkbenchWindowConfigurer getWindowConfigurer(IWorkbenchWindow window);

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
}
