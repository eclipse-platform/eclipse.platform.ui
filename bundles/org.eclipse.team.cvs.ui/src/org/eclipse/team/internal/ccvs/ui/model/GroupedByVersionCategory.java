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

 
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.resources.RemoteFolder;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.ICVSUIConstants;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.ui.model.IWorkbenchAdapter;

public class GroupedByVersionCategory extends CVSModelElement implements IAdaptable {
	private ICVSRepositoryLocation repository;
	
	/**
	 * ProjectVersionsCategory constructor.
	 */
	public GroupedByVersionCategory(ICVSRepositoryLocation repo) {
		super();
		this.repository = repo;
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
	
	/**
	 * Returns the children of this object.  When this object
	 * is displayed in a tree, the returned objects will be this
	 * element's children.  Returns an empty enumeration if this
	 * object has no children.
	 * 
	 * XXX This method looks wrong to me somehow
	 */
	public Object[] fetchChildren(Object o, IProgressMonitor monitor) {
		//String -> CTags[]
		Map mappings = CVSUIPlugin.getPlugin().getRepositoryManager().getKnownProjectsAndVersions(repository);
		Map remoteVersionModules = new HashMap();
		for (Iterator it = mappings.keySet().iterator(); it.hasNext();) {
			String project = (String) it.next();
			CVSTag[] versions = (CVSTag[])((HashSet)mappings.get(project)).toArray(new CVSTag[0]);
			for (int i = 0; i < versions.length; i++) {
				CVSTag tag = versions[i];
				RemoteVersionModule module = (RemoteVersionModule)remoteVersionModules.get(tag);
				if(module==null) {
					module = new RemoteVersionModule(tag, this);
					remoteVersionModules.put(tag, module);
				}
				module.addProject(new RemoteFolder(null, repository, project, tag));				
			}
		}
		return (RemoteVersionModule[])remoteVersionModules.values().toArray(new RemoteVersionModule[0]);				
	}

	/**
	 * Returns an image descriptor to be used for displaying an object in the workbench.
	 * Returns null if there is no appropriate image.
	 *
	 * @param object The object to get an image descriptor for.
	 */
	public ImageDescriptor getImageDescriptor(Object object) {
		return CVSUIPlugin.getPlugin().getImageDescriptor(ICVSUIConstants.IMG_VERSIONS_CATEGORY);
	}

	/**
	 * Returns the name of this element.  This will typically
	 * be used to assign a label to this object when displayed
	 * in the UI.  Returns an empty string if there is no appropriate
	 * name for this object.
	 *
	 * @param object The object to get a label for.
	 */
	public String getLabel(Object o) {
		return Policy.bind("GroupedByVersionCategory.Versions_1"); //$NON-NLS-1$
	}

	/**
	 * Returns the logical parent of the given object in its tree.
	 * Returns null if there is no parent, or if this object doesn't
	 * belong to a tree.
	 *
	 * @param object The object to get the parent for.
	 */
	public Object getParent(Object o) {
		return repository;
	}
	
	/**
	 * Return the repository the given element belongs to.
	 */
	public ICVSRepositoryLocation getRepository(Object o) {
		return repository;
	}
}
