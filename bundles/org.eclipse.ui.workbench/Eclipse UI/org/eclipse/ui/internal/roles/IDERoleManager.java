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
package org.eclipse.ui.internal.roles;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * The IDERoleManager is the class that implements the IDE specific behaviour
 * of the RoleManager. This is experimental API and will be subject to
 * refactoring at any time.
 */
public class IDERoleManager extends RoleManager {

	private IResourceChangeListener listener;

	/**
	 * Create a new instance of the receiver. */
	public IDERoleManager() {
		super();
	}

	/*
	 * (non-Javadoc) @see
	 * org.eclipse.ui.internal.roles.RoleManager#connectToPlatform()
	 */
	protected void connectToPlatform() {

		listener = getChangeListener();
		WorkbenchPlugin.getPluginWorkspace().addResourceChangeListener(
			listener);

	}

	/**
	 * Get a change listener for listening to resource changes.
	 * 
	 * @return
	 */
	private IResourceChangeListener getChangeListener() {
		return new IResourceChangeListener() {
			/*
			 * (non-Javadoc) @see
			 * org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org.eclipse.core.resources.IResourceChangeEvent)
			 */
			public void resourceChanged(IResourceChangeEvent event) {

				IResourceDelta mainDelta = event.getDelta();

				if (mainDelta == null)
					return;
				//Has the root changed?
				if (mainDelta.getKind() == IResourceDelta.CHANGED
					&& mainDelta.getResource().getType() == IResource.ROOT) {

					try {
						IResourceDelta[] children =
							mainDelta.getAffectedChildren();
						for (int i = 0; i < children.length; i++) {
							IResourceDelta delta = children[i];
							if (delta.getResource().getType()
								== IResource.PROJECT
								&& delta.getKind() == IResourceDelta.ADDED) {
								IProject project =
									(IProject) delta.getResource();
								String[] ids =
									project.getDescription().getNatureIds();
								for (int j = 0; j < ids.length; j++) {
									enableActivities(ids[j]);
								}
							}
						}

					} catch (CoreException exception) {
						//Do nothing if there is a CoreException
					}
				}

			}
		};
	}

	/*
	 * (non-Javadoc) @see
	 * org.eclipse.ui.internal.roles.RoleManager#shutdownManager()
	 */
	public void shutdownManager() {
		super.shutdownManager();
		if (listener != null) {
			WorkbenchPlugin.getPluginWorkspace().removeResourceChangeListener(
				listener);
		}
	}

}
