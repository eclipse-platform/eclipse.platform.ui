/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.model;

 
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFolder;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.ICVSUIConstants;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * This class represents an IProject resource in a repository.  The
 * children of a RemoteModule are its versions. A RemoteModule is
 * a child of the VersionsCategory.
 */
public class RemoteVersionModule extends CVSModelElement implements IAdaptable {
	CVSTag tag;
	List projects = new ArrayList();
	GroupedByVersionCategory parent;
	
	/**
	 * RemoteProject constructor.
	 */
	public RemoteVersionModule(CVSTag tag, GroupedByVersionCategory parent) {
		this.tag = tag;
		this.parent = parent;
	}
	
	/**
	 * Returns an object which is an instance of the given class
	 * associated with this object. Returns <code>null</code> if
	 * no such object can be found.
	 */
	public Object getAdapter(Class adapter) {
		if (adapter == IWorkbenchAdapter.class) return this;
		return null;
	}
	
	public void addProject(ICVSRemoteFolder project) {
		projects.add(project);
	}
	
	/**
	 * Returns the children of this object.  When this object
	 * is displayed in a tree, the returned objects will be this
	 * element's children.  Returns an empty enumeration if this
	 * object has no children. The children of the RemoteModule
	 * are the versions for that module.
	 */
	public Object[] fetchChildren(Object o, IProgressMonitor monitor) {
		return (ICVSRemoteFolder[]) projects.toArray(new ICVSRemoteFolder[projects.size()]);
	}
	
	/**
	 * Returns an image to be used for displaying an object in the desktop.
	 * @param object The object to get an image for.
	 * @param owner The viewer that the image will be used in.  The image will
	 * be disposed when this viewer is closed.  If the owner is null, a new 
	 * image is returned, and the caller is responsible for disposing it.
	 */
	public ImageDescriptor getImageDescriptor(Object object) {
		return CVSUIPlugin.getPlugin().getImageDescriptor(ICVSUIConstants.IMG_PROJECT_VERSION);
	}
	
	/**
	 * Returns the name of this element.  This will typically
	 * be used to assign a label to this object when displayed
	 * in the UI.
	 */
	public String getLabel(Object o) {
		return tag.getName();
	}
	
	/**
	 * Returns the logical parent of the given object in its tree.
	 */
	public Object getParent(Object o) {
		return parent;
	}

	
	/** (Non-javadoc)
	 * For debugging purposes only.
	 */
	public String toString() {
		return "RemoteVersionModule(" + tag.getName() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
	}
}
