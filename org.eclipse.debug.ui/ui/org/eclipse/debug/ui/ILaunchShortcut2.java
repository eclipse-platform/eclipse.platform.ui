/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.ui;

import org.eclipse.core.resources.IResource;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;


/**
 * <p>
 * This extension to a standard launch shortcut ({@link ILaunchShortcut}) allows 
 * providers of launch shortcuts to specify how selections should be 
 * launched; i.e. provide a context launching participant which is called 
 * when we cannot derive the correct resource and/or launch configuration
 * to launch.
 * </p>
 * <br>
 * <p>
 * The new methods of this interface are used in a particular ordering.
 * <ol> 
 * <li>
 * When launching begins and we have no resource (no <code>IResource</code> adapter)
 * we ask participants for the launchable resource; calling the <code>getLaunchableResource(..)</code>
 * method of each implementation of this interface (participant).
 * </li>
 * <li>
 * With (or without) a resource all of the applicable launch configurations are collected from the launch 
 * configuration manager and from participants (calling the getLaunchConfigurations(..) method).
 * </li>
 * </ol>
 * </p>
 * <p>
 * <br>
 * Clients contributing a launch shortcut are intended to implement this interface.
 * </p>
 * <p>
 * <br>
 * @see org.eclipse.debug.internal.ui.contextlaunching.ContextRunner
 * @see org.eclipse.debug.internal.ui.contextlaunching.LaunchingResourceManager
 * </p>
 * @since 3.4
 */
public interface ILaunchShortcut2 extends ILaunchShortcut {

	/**
	 * Given the specified <code>ISelection</code> this method returns an array of 
	 * <code>ILaunchConfiguration</code>s that apply to the current selection, 
	 * i.e. all of the launch configurations that could be used to launch the given 
	 * selection.
	 * @param selection the current selection
	 * @return an array of <code>ILaunchConfiguration</code>s that could be 
	 * used to launch the given selection, or an empty array, never <code>null</code>
	 */
	public ILaunchConfiguration[] getLaunchConfigurations(ISelection selection);
	
	/**
	 * Given the specified <code>IEditorPart</code> this method returns an array of 
	 * <code>ILaunchConfiguration</code>s that apply to the current editor part, 
	 * i.e. all of the launch configurations that could be used to launch the given 
	 * editor part/editor input.
	 * @param editorpart the current selection
	 * @return an array of <code>ILaunchConfiguration</code>s that could be 
	 * used to launch the given editor part/editor input, or an empty array, never <code>null</code>
	 */
	public ILaunchConfiguration[] getLaunchConfigurations(IEditorPart editorpart);
	
	/**
	 * Given the specified <code>ISelection</code> this method returns an
	 * <code>IResource</code> that directly maps to the current selection.
	 * This mapping is then leveraged by the context launching framework
	 * to try and launch the resource. 
	 * @param selection the current selection
	 * @return an <code>IResource</code> that would be used during context
	 * sensitive launching or <code>null</code> if one is not to be provided or does not exist.
	 */
	public IResource getLaunchableResource(ISelection selection);
	
	/**
	 * Given the specified <code>IEditorPart</code> this method returns an
	 * <code>IResource</code> that directly maps to the current editor part/editor input.
	 * This mapping is then leveraged by the context launching framework
	 * to try and launch the resource.  
	 * @param editorpart the current editor part
	 * @return an <code>IResource</code> that would be used during context
	 * sensitive launching or <code>null</code> if one is not to be provided or does not exist.
	 */
	public IResource getLaunchableResource(IEditorPart editorpart);
}
