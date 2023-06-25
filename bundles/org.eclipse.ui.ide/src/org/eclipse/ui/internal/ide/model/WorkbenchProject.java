/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.ui.internal.ide.model;

import java.util.HashMap;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.ui.IProjectActionFilter;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.ide.IDEInternalWorkbenchImages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;

/**
 * An IWorkbenchAdapter that represents IProject.
 */
public class WorkbenchProject extends WorkbenchResource implements IProjectActionFilter {

	HashMap<String, ImageDescriptor> imageCache = new HashMap<>(11);

	/**
	 *	Answer the appropriate base image to use for the passed resource, optionally
	 *	considering the passed open status as well iff appropriate for the type of
	 *	passed resource
	 */
	@Override
	protected ImageDescriptor getBaseImage(IResource resource) {
		IProject project = (IProject) resource;
		boolean isOpen = project.isOpen();
		String baseKey = isOpen ? IDE.SharedImages.IMG_OBJ_PROJECT
				: IDE.SharedImages.IMG_OBJ_PROJECT_CLOSED;
		if (isOpen) {
			try {
				for (String imageKey : project.getDescription().getNatureIds()) {
					// Have to use a cache because OverlayIcon does not define
					// its own equality criteria,
					// so WorkbenchLabelProvider would always create a new image
					// otherwise.
					ImageDescriptor overlayImage = imageCache.get(imageKey);
					if (overlayImage != null) {
						return overlayImage;
					}
					ImageDescriptor natureImage = IDEWorkbenchPlugin
							.getDefault().getProjectImageRegistry()
							.getNatureImage(imageKey);
					if (natureImage != null) {
						ImageDescriptor baseImage = IDEInternalWorkbenchImages.getImageDescriptor(baseKey);
						overlayImage = new DecorationOverlayIcon(baseImage, natureImage, IDecoration.TOP_RIGHT);
						imageCache.put(imageKey, overlayImage);
						return overlayImage;
					}
				}
			} catch (CoreException e) {
			}
		}
		return IDEInternalWorkbenchImages.getImageDescriptor(baseKey);
	}

	/**
	 * Returns the children of this container.
	 */
	@Override
	public Object[] getChildren(Object o) {
		IProject project = (IProject) o;
		if (project.isOpen()) {
			try {
				return project.members();
			} catch (CoreException e) {
				//don't get the children if there are problems with the project
			}
		}
		return NO_CHILDREN;
	}

	/**
	 * Returns whether the specific attribute matches the state of the target
	 * object.
	 *
	 * @param target the target object
	 * @param name the attribute name
	 * @param value the attriute value
	 * @return <code>true</code> if the attribute matches; <code>false</code> otherwise
	 */
	@Override
	public boolean testAttribute(Object target, String name, String value) {
		if (!(target instanceof IProject)) {
			return false;
		}
		IProject proj = (IProject) target;
		if (name.equals(NATURE)) {
			try {
				return proj.isAccessible() && proj.hasNature(value);
			} catch (CoreException e) {
				return false;
			}
		} else if (name.equals(OPEN)) {
			value = value.toLowerCase();
			return (proj.isOpen() == value.equals("true"));//$NON-NLS-1$
		}
		return super.testAttribute(target, name, value);
	}
}
