/*******************************************************************************
 * Copyright (c) 2008, 2018 Freescale Semiconductor and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Serge Beauchamp (Freescale Semiconductor) - [252996] initial API and implementation
 *     IBM Corporation - ongoing implementation
 *******************************************************************************/
package org.eclipse.ui.internal.ide.misc;

import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.filtermatchers.AbstractFileInfoMatcher;
import org.eclipse.ui.internal.ide.StringMatcher;

/**
 * A file info filter that uses a simple string matcher to match on file name.
 */
public class StringFileInfoMatcher extends AbstractFileInfoMatcher {

	/**
	 */
	public static String ID = "org.eclipse.ui.ide.patternFilterMatcher"; //$NON-NLS-1$

	StringMatcher matcher = null;
	/**
	 * Creates a new factory for this filter type.
	 */
	public StringFileInfoMatcher() {
	}

	@Override
	public void initialize(IProject project, Object arguments) {
		if ((arguments instanceof String) && ((String) arguments).length() > 0)
			matcher = new StringMatcher((String) arguments, true, false);
	}

	@Override
	public boolean matches(IContainer parent, IFileInfo fileInfo) {
		if (matcher != null)
			return matcher.match(fileInfo.getName());
		return false;
	}
}
