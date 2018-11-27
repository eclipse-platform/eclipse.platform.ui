/*******************************************************************************
 *  Copyright (c) 2000, 2012 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.resources.regression;

import java.io.ByteArrayInputStream;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.tests.resources.ResourceTest;

public class Bug_006708 extends ResourceTest {
	/**
	 * Constructor for Bug_6708.
	 */
	public Bug_006708() {
		super();
	}

	/**
	 * Constructor for Bug_6708.
	 * @param name
	 */
	public Bug_006708(String name) {
		super(name);
	}

	public static Test suite() {
		return new TestSuite(Bug_006708.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProject p1 = workspace.getRoot().getProject("p1");
		if (p1.exists()) {
			p1.delete(true, null);
		}
		IProject p2 = workspace.getRoot().getProject("p2");
		if (p2.exists()) {
			p2.delete(true, null);
		}
	}

	public void testBug() throws CoreException {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();

		IProject sourceProj = workspace.getRoot().getProject("P1");
		assertFalse("Project P1 exists already", sourceProj.exists());
		sourceProj.create(null);
		sourceProj.open(null);
		IFile source = sourceProj.getFile("Source.txt");
		source.create(new ByteArrayInputStream("abcdef".getBytes()), false, null);

		IProject destProj = workspace.getRoot().getProject("P2");
		assertFalse("Project P2 exists already", destProj.exists());
		destProj.create(null);
		destProj.open(null);
		IFile dest = destProj.getFile("Dest.txt");

		source.copy(dest.getFullPath(), false, null);
		dest.setContents(new ByteArrayInputStream("ghijkl".getBytes()), false, true, null);
	}
}
