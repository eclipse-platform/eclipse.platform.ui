/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.tests.ccvs.ui.benchmark;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.zip.ZipException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.tests.ccvs.core.EclipseTest;

/**
 * Benchmark test superclass
 */
public abstract class BenchmarkTest extends EclipseTest {

	protected BenchmarkTest() {
	}

	protected BenchmarkTest(String name) {
		super(name);
	}

	protected IProject createUniqueProject(File zipFile) throws TeamException, CoreException, ZipException, IOException, InterruptedException, InvocationTargetException {
		// create a project with no contents
		IProject project = getUniqueTestProject(getName());
		Util.importZip(project, zipFile);
		return project;
	}
	
}
