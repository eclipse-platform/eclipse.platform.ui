/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.ui.synchronize.viewers;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.ViewerSorter;

/**
 * This class is reponsible for creating and maintaining model of
 * DiffNodes that can be shown in a viewer.
 * 
 * @since 3.0
 */
public abstract class SynchronizeModelProvider {

	protected class RootDiffNode extends UnchangedResourceModelElement {
		public RootDiffNode() {
			super(null, ResourcesPlugin.getWorkspace().getRoot());
		}
		public void fireChanges() {
			fireChange();
		}
		public boolean hasChildren() {
			// This is required to allow the sync framework to be used in wizards
			// where the input is not populated until after the compare input is
			// created
			// (i.e. the compare input will only create the diff viewer if the
			// input has children
			return true;
		}
	}
	
	/**
	 * Called to initialize this controller and returns the input created by this controller. 
	 * @param monitor
	 * @return
	 */
	public abstract SynchronizeModelElement prepareInput(IProgressMonitor monitor);
	
	/**
	 * Returns the input created by this controller or <code>null</code> if 
	 * {@link #prepareInput(IProgressMonitor)} hasn't been called on this object yet.
	 * @return
	 */
	public abstract SynchronizeModelElement getInput();
	
	public abstract void setViewer(StructuredViewer viewer);

	public abstract ViewerSorter getViewerSorter();

	public abstract void dispose();
}