/**********************************************************************
Copyright (c) 2003, 2004 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

package org.eclipse.ant.internal.ui.preferences;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.ant.internal.ui.AntUIPlugin;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.custom.BusyIndicator;

public class FileFilter extends ViewerFilter {

	/**
	 * Objects to filter
	 */
	protected List fFilter;
	
	/**
	 * Collection of property files and containers to display
	 */
	private Set fPropertyFiles;
	
	private String fExtension;

	/**
	 * Creates a new filter that filters the given 
	 * objects.
	 */
	public FileFilter(List objects, String extension) {
		fFilter = objects;
		fExtension= extension;
		init();
	}

	/**
	 * @see ViewerFilter#select(Viewer, Object, Object)
	 */
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		return fPropertyFiles.contains(element) && !fFilter.contains(element);
	}
	
	/**
	 * Search for all archives in the workspace.
	 */
	private void init() {
		BusyIndicator.showWhile(AntUIPlugin.getStandardDisplay(), new Runnable() {
			public void run() {
				fPropertyFiles = new HashSet();
				traverse(ResourcesPlugin.getWorkspace().getRoot(), fPropertyFiles);
			}
		});
	}

	/**
	 * Traverse the given container, adding property file to the given set.
	 * Returns whether any files were added
	 */
	private boolean traverse(IContainer container, Set set) {
		boolean added = false;
		try {
			IResource[] resources = container.members();
			for (int i = 0; i < resources.length; i++) {
				IResource resource = resources[i];
				if (resource instanceof IFile) {
					IFile file = (IFile) resource;
					String ext = file.getFileExtension();
					if (ext != null && ext.equalsIgnoreCase(fExtension)) {
						set.add(file);
						added = true;
					}
				} else if (resource instanceof IContainer) {
					if (traverse((IContainer) resource, set)) {
						set.add(resource);
						added = true;
					}
				}
			}
		} catch (CoreException e) {
		}
		return added;
	}
}