/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 *
 * This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ant.internal.core.ant;

import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Path;

public class InternalProject2 extends Project {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.tools.ant.Project#createClassLoader(org.apache.tools.ant.types.Path)
	 */
	@Override
	public AntClassLoader createClassLoader(Path path) {
		AntClassLoader loader = super.createClassLoader(path);
		if (path == null) {
			// use the "fake" Eclipse runtime classpath for Ant
			loader.setClassPath(Path.systemClasspath);
		}

		return loader;
	}
}