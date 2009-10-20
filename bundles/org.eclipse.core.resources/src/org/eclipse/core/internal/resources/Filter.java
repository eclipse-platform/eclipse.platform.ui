/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Serge Beauchamp (Freescale Semiconductor) - initial API and implementation
 *     IBM Corporation - ongoing implementation
 *******************************************************************************/
package org.eclipse.core.internal.resources;

import org.eclipse.core.resources.IFilterDescriptor;

import org.eclipse.core.filesystem.IFileInfoFilter;


import org.eclipse.core.internal.utils.Messages;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.*;
import org.eclipse.osgi.util.NLS;


import java.util.*;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.resources.IResourceFilter;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;

/**
 * Class that instantiate IResourceFilter's  that are stored in the project description.
 */

public class Filter implements IResourceFilter {

	FilterDescription description;
	IProject project;
	IFileInfoFilter provider = null;
	
	public Filter(IProject project, FilterDescription  description) {
		this.description = description;
		this.project = project;
	}
	
	public boolean match(IFileInfo fileInfo) throws CoreException {
		if (provider == null) {
			IFilterDescriptor filterDescriptor = project.getWorkspace().getFilterDescriptor(getId());
			if (filterDescriptor != null)
				provider = filterDescriptor.getFactory().instantiate(project, getArguments());
			if (provider == null) {
				String message = NLS.bind(Messages.filters_missingFilterType, getId());
				throw new CoreException(new Status(IStatus.ERROR, ResourcesPlugin.PI_RESOURCES, Platform.PLUGIN_ERROR, message, new Error()));
			}
		}
		if (provider != null)
			return provider.matches(fileInfo);
		return false;
	}

	public boolean isFirst() {
		IFilterDescriptor descriptor = project.getWorkspace().getFilterDescriptor(getId());
		if (descriptor != null)
			return descriptor.isFirstOrdering();
		return false;
	}

	public Object getArguments() {
		return description.getArguments();
	}

	public String getId() {
		return description.getFilterID();
	}

	public IPath getPath() {
		return description.getProjectRelativePath();
	}

	public IProject getProject() {
		return project;
	}

	public int getType() {
		return description.getType();
	}
	
	public boolean isIncludeOnly() {
		return (getType() & INCLUDE_ONLY) != 0;
	}

	public boolean appliesTo(IFileInfo info) {
		if (info.isDirectory())
			return (getType() & FOLDERS) != 0;
		return (getType() & FILES) != 0;
	}

	public static IFileInfo[] filter(IProject project, LinkedList/*Filter*/ includeFilters, LinkedList/*Filter*/ excludeFilters,  IFileInfo[] list) throws CoreException {
		IFileInfo[] result = filterIncludes(project, includeFilters, list);
		return filterExcludes(project, excludeFilters, result);
	}
	
	public static IFileInfo[] filterIncludes(IProject project, LinkedList/*Filter*/ filters,  IFileInfo[] list) throws CoreException {
		if (filters.size() > 0) {
			IFileInfo[] result = new IFileInfo[list.length];
			int outputIndex = 0;
	
			for (int i = 0; i < list.length; i++) {
				IFileInfo info = list[i];
				Iterator objIt = filters.iterator();
				boolean filtersWereApplicable = false;
				while (objIt.hasNext()) {
					Filter filter = (Filter) objIt.next();
					if (filter.appliesTo(info)) {
						filtersWereApplicable = true;
						if (filter.match(info)) {
							result[outputIndex++] = info;
							break;
						}
					}
				}
				if (!filtersWereApplicable)
					result[outputIndex++] = info;
			}
			if (outputIndex != result.length) {
				IFileInfo[] tmp = new IFileInfo[outputIndex];
				System.arraycopy(result, 0, tmp, 0, outputIndex);
				result = tmp;
			}
			return result;
		}
		return list;
	}
	
	public static IFileInfo[] filterExcludes(IProject project, LinkedList/*Filter*/ filters,  IFileInfo[] list) throws CoreException {
		if (filters.size() > 0) {
			IFileInfo[] result = new IFileInfo[list.length];
			int outputIndex = 0;
	
			for (int i = 0; i < list.length; i++) {
				IFileInfo info = list[i];
				Iterator objIt = filters.iterator();
				boolean shouldBeExcluded = false;
				while (objIt.hasNext()) {
					Filter filter = (Filter) objIt.next();
					if (filter.appliesTo(info)) {
						if (filter.match(info)) {
							shouldBeExcluded = true;
							break;
						}
					}
				}
				if (!shouldBeExcluded)
					result[outputIndex++] = info;
			}
			if (outputIndex != result.length) {
				IFileInfo[] tmp = new IFileInfo[outputIndex];
				System.arraycopy(result, 0, tmp, 0, outputIndex);
				result = tmp;
			}
			return result;
		}
		return list;
	}
}
