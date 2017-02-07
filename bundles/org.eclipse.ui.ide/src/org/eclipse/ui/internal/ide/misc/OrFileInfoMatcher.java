/*******************************************************************************
 * Copyright (c) 2008, 2014 Freescale Semiconductor and others.
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
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.filtermatchers.AbstractFileInfoMatcher;
import org.eclipse.core.resources.filtermatchers.CompoundFileInfoMatcher;
import org.eclipse.core.runtime.CoreException;

/**
 * A Resource Filter Type Factory for supporting the OR logical preposition
 */
public class OrFileInfoMatcher extends CompoundFileInfoMatcher {

	@Override
	public boolean matches(IContainer parent, IFileInfo fileInfo) throws CoreException {
		if (matchers.length > 0) {
			for (AbstractFileInfoMatcher matcher : matchers) {
				if (matcher.matches(parent, fileInfo))
					return true;
			}
			return false;
		}
		return true;
	}
}
