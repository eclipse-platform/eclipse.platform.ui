/*******************************************************************************
 * Copyright (c) 2008, 2009 Freescale Semiconductor and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Serge Beauchamp (Freescale Semiconductor) - [252996] initial API and implementation
 *     IBM Corporation - ongoing implementation
 *******************************************************************************/
package org.eclipse.ui.internal.ide.misc;

import org.eclipse.core.filesystem.IFileInfoFilter;
import org.eclipse.core.resources.IFilterDescriptor;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceFilter;

/**
 * Resource Filter Type allowing serializing sub filters as the arguments
 */
public class CompoundResourceFilter {

	protected IFileInfoFilter instantiate(IProject project,
			IResourceFilter filter) {
		IFilterDescriptor desc = project.getWorkspace().getFilterDescriptor(
				filter.getId());
		if (desc != null)
			return desc.getFactory()
					.instantiate(project, filter.getArguments());
		return null;
	}

	protected abstract class FileInfoFilter implements IFileInfoFilter {
		protected IFileInfoFilter[] filterTypes;
		protected IResourceFilter[] filters;

		public FileInfoFilter(IProject project, IResourceFilter[] filters) {
			this.filters = filters;
			filterTypes = new IFileInfoFilter[filters!=null ? filters.length : 0];
			for (int i = 0; i < filterTypes.length; i++)
				filterTypes[i] = instantiate(project, filters[i]);
		}
	}
}