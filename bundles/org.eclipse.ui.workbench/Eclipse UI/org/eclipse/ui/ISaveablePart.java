/************************************************************************
Copyright (c) 2003 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM - Initial implementation
************************************************************************/

package org.eclipse.ui;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Workbench parts implement or adapt to this interface to participate
 * in the enablement and execution of the <code>Save</code> and
 * <code>Save As</code> actions.
 * <p>
 * NOTE: This is experimental API, which may be changed or removed at any point in time.
 * This API should not be called, overridden or otherwise used in production code.
 * </p>
 * 
 * @since 2.1
 * @see org.eclipse.ui.IEditorPart  
 */
public interface ISaveablePart {

	/**
	 * The property id for <code>isDirty</code>.
	 */
	public static final int PROP_DIRTY = 0x101;

	/**
	 * Saves the contents of this part.
	 * <p>
	 * If the save is successful, the part should fire a property changed event 
	 * reflecting the new dirty state (<code>PROP_DIRTY</code> property).
	 * </p>
	 * <p>
	 * If the save is cancelled through user action, or for any other reason, the
	 * part should invoke <code>setCancelled</code> on the <code>IProgressMonitor</code>
	 * to inform the caller.
	 * </p>
	 * <p>
	 * This method is long-running; progress and cancellation are provided
	 * by the given progress monitor. 
	 * </p>
	 *
	 * @param monitor the progress monitor
	 */
	public void doSave(IProgressMonitor monitor);
	
	/**
	 * Saves the contents of this part to another object.
	 * <p>
	 * Implementors are expected to open a "Save As" dialog where the user will
	 * be able to select a new name for the contents. After the selection is made,
	 * the contents should be saved to that new name.  During this operation a
	 * <code>IProgressMonitor</code> should be used to indicate progress.
	 * </p>
	 * <p>
	 * If the save is successful, the part fires a property changed event 
	 * reflecting the new dirty state (<code>PROP_DIRTY</code> property).
	 * </p>
	 */
	public void doSaveAs();

	/**
	 * Returns whether the contents of this part have changed since the last save
	 * operation. If this value changes the part must fire a property listener 
	 * event with <code>PROP_DIRTY</code>.
	 * <p>
	 *
	 * @return <code>true</code> if the contents have been modified and need
	 *   saving, and <code>false</code> if they have not changed since the last
	 *   save
	 */
	public boolean isDirty();

	/**
	 * Returns whether the "Save As" operation is supported by this part.
	 *
	 * @return <code>true</code> if "Save As" is supported, and <code>false</code>
	 *  if not supported
	 */
	public boolean isSaveAsAllowed();

	/**
	 * Returns whether the contents of this part should be saved when the part
	 * is closed.
	 *
	 * @return <code>true</code> if the contents of the part should be saved on
	 *   close, and <code>false</code> if the contents are expendable
	 */
	public boolean isSaveOnCloseNeeded();
}
