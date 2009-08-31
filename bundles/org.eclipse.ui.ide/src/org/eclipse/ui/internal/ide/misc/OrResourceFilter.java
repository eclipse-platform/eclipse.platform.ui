/*******************************************************************************
 * Copyright (c) 2008, 2009 Freescale Semiconductor and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Serge Beauchamp (Freescale Semiconductor) - initial API and implementation
 *     IBM Corporation - ongoing development
 *******************************************************************************/
package org.eclipse.ui.internal.ide.misc;


import org.eclipse.core.filesystem.IFileInfoFilter;

import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.resources.IResourceFilter;
import org.eclipse.core.resources.IFilterTypeFactory;
import org.eclipse.core.resources.IProject;

/** 
 * A Resource Filter Type Factory for supporting the OR logical preposition
 * @since 3.6
 */
public class OrResourceFilter extends CompoundResourceFilter implements IFilterTypeFactory {

	class OrFilterType extends FilterType {
		public OrFilterType(IProject project, IResourceFilter[] filters) {
			super (project, filters);
		}
		public boolean matches(IFileInfo fileInfo) {
			if (filterTypes.length > 0) {
				for (int i = 0; i < filterTypes.length; i++) {
					if (filterTypes[i].matches(fileInfo))
						return true;
				}
				return false;
			}
			return true;
		}
	}
	public IFileInfoFilter instantiate(IProject project, String arguments) {
		return new OrFilterType(project, unserialize(project, arguments));
	}
}
