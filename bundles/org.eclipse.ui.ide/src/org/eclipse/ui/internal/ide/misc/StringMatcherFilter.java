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

import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.resources.IFilterType;
import org.eclipse.core.resources.IFilterTypeFactory;
import org.eclipse.core.resources.IProject;
import org.eclipse.ui.internal.ide.StringMatcher;

public class StringMatcherFilter implements IFilterTypeFactory {

	public StringMatcherFilter() {
	}

	class FilterType implements IFilterType {

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
	
	public IFilterType instantiate(IProject project, String arguments) {
		return new FilterType(arguments);
	}

}
