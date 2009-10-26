/*******************************************************************************
 * Copyright (c) 2008, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Serge Beauchamp (Freescale Semiconductor) - initial API and implementation
 *     IBM Corporation - ongoing development
 *******************************************************************************/
package org.eclipse.core.internal.resources;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileInfoFilter;
import org.eclipse.core.resources.IFileInfoFilterFactory;
import org.eclipse.core.resources.IProject;

/**
 * A Filter provider for Java Regular expression supported by 
 * java.util.regex.Pattern.
 */
public class RegexFilterFactory implements IFileInfoFilterFactory {

	static class RegexFilterType implements IFileInfoFilter {
		Pattern pattern = null;

		public RegexFilterType(IProject project, String arguments) {
			if (arguments != null)
				pattern = Pattern.compile(arguments);
		}

		public boolean matches(IFileInfo fileInfo) {
			if (pattern == null)
				return false;
			Matcher m = pattern.matcher(fileInfo.getName());
			return m.matches();
		}
	}

	public IFileInfoFilter instantiate(IProject project, Object arguments) {
		return new RegexFilterType(project, (String) arguments);
	}
}
