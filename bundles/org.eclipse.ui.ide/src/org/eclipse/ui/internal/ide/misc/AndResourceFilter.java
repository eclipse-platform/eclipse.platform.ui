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

import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileInfoFilter;
import org.eclipse.core.resources.IFileInfoFilterFactory;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceFilter;

/**
 * A Resource Filter Type Factory for supporting the AND logical preposition
 */
public class AndResourceFilter extends CompoundResourceFilter implements
		IFileInfoFilterFactory {

	class AndFileInfoFilter extends FileInfoFilter {
		public AndFileInfoFilter(IProject project, IResourceFilter[] filters) {
			super(project, filters);
		}

		public boolean matches(IFileInfo fileInfo) {
			for (int i = 0; i < filterTypes.length; i++) {
				if (!filterTypes[i].matches(fileInfo))
					return false;
			}
			return true;
		}
	}

	public IFileInfoFilter instantiate(IProject project, String arguments) {
		return new AndFileInfoFilter(project, unserialize(project, arguments));
	}
}
