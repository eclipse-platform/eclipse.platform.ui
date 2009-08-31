/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Serge Beauchamp (Freescale Semiconductor) - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.ide.misc;

import org.eclipse.core.filesystem.IFileInfoFilter;

import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.resources.IFileInfoFilterFactory;
import org.eclipse.core.resources.IProject;
import org.eclipse.ui.internal.ide.StringMatcher;

/**
 * A file info filter that uses a simple string matcher to match on file name.
 * @since 3.6
 */
public class StringMatcherFilter implements IFileInfoFilterFactory {

	/**
	 * Creates a new factory for this filter type.
	 */
	public StringMatcherFilter() {
	}

	class FilterType implements IFileInfoFilter {

		StringMatcher matcher;
		/**
		 * @param arguments
		 */
		public FilterType(String arguments) {
			matcher = new StringMatcher(arguments, true, false);
		}
		/* (non-Javadoc)
		 * @see org.eclipse.core.resources.IFilterType#matches(org.eclipse.core.filesystem.IFileInfo)
		 */
		public boolean matches(IFileInfo fileInfo) {
			return matcher.match(fileInfo.getName());
		}
		
	}
	
	public IFileInfoFilter instantiate(IProject project, String arguments) {
		return new FilterType(arguments);
	}

}
