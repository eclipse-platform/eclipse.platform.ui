/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.actions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;

/**
 * An adapter to support breakpoint creation/deletion for an active part.
 * The debug platform provides retargettable actions for toggling line breakpoints,
 * method breakpoints, and watchpoints. Debugger implementations provide debug model
 * specific targets for the global actions by contribuing an adapter of this type
 * to perform the actual breakpoint creation/removal.
 * <p>
 * When a part is activated, a retargettable action asks the part
 * for its 'toggle breakpoint' adapter. If one exists, that adapter is
 * delegated to to perform 'toggle breakpoint' operations when the user
 * invokes an associated action. This allows the platform to provide one
 * command and keybinding for each 'toggle breakpoint' function that can be
 * shared by many debuggers.
 * </p> 
 * <p>
 * EXPERIMENTAL
 * </p>
 * <p>
 * Clients are intended to implement this interface and provide instances as
 * an adapter on applicable parts (for example, editors) that support breakpoint
 * toggling.
 * </p>
 * @since 3.0
 */
public interface IToggleBreakpointsTarget {
	
	/**
	 * Creates new line breakpoints or removes existing breakpoints.
	 * The selection varies depending on the given part. For example,
	 * a text selection is provided for text editors, and a structured
	 * selection is provided for tree views, and may be a multi-selection.
	 * 
	 * @param part the part on which the action has been invoked  
	 * @param selection selection on which line breakpoints should be toggled
	 * @throws CoreException if unable to perform the action 
	 */
	public void toggleLineBreakpoints(IWorkbenchPart part, ISelection selection) throws CoreException;
	
	/**
	 * Returns whether line breakpoints can be toggled on the given selection.
	 * The selection varies depending on the given part. For example,
	 * a text selection is provided for text editors, and a structured
	 * selection is provided for tree views, and may be a multi-selection.
	 * 
	 * @param part the part on which the action has been invoked
	 * @param selection selection on which line breakpoints may be toggled
	 * @return whether line breakpoints can be toggled on the given selection
	 */
	public boolean canToggleLineBreakpoints(IWorkbenchPart part, ISelection selection);
	
}
