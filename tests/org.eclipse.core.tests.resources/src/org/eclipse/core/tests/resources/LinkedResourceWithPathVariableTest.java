/**********************************************************************
 * IBM - Initial API and implementation
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * IBM - Initial API and implementation
 * Contributors:
 * IBM - Initial API and implementation
 *********************************************************************/

package org.eclipse.core.tests.resources;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.resources.IPathVariableManager;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;

/**
 * This class extends <code>LinkedResourceTest</code> in order to use
 * randomly generated locations that are always variable-based.
 * TODO: add tests specific to linking resources using path variables (then
 * removing the variable, change the variable value, etc)
 */
public class LinkedResourceWithPathVariableTest extends LinkedResourceTest {
	
	private final static String VARIABLE_NAME = "ROOT"; 
	
    public LinkedResourceWithPathVariableTest() {
		super();
	}
	public LinkedResourceWithPathVariableTest(String name) {
		super(name);
	}
	public static Test suite() {
		return new TestSuite(LinkedResourceWithPathVariableTest.class);
	}
	protected void setUp() throws Exception {
		IPath base = Platform.getLocation().removeLastSegments(1);
		getWorkspace().getPathVariableManager().setValue(VARIABLE_NAME, base);
		super.setUp();
	}
	protected void tearDown() throws Exception {
		getWorkspace().getPathVariableManager().setValue(VARIABLE_NAME, null);
		super.tearDown();
	}
	/**
	 * @see org.eclipse.core.tests.harness.EclipseWorkspaceTest#getRandomLocation()
	 */
	public IPath getRandomLocation() {
		IPathVariableManager pathVars = getWorkspace().getPathVariableManager();
		//low order bits are current time, high order bits are static counter
		IPath parent = new Path(VARIABLE_NAME);
		final long mask = 0x00000000FFFFFFFFL;
		long segment = (((long)++nextLocationCounter) << 32) | (System.currentTimeMillis() & mask);
		IPath path = parent.append(Long.toString(segment));
		while (pathVars.resolvePath(path).toFile().exists()) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
			}
			segment = (((long)++nextLocationCounter) << 32) | (System.currentTimeMillis() & mask);
			path = parent.append(Long.toString(segment));
		}
		return path;
	}
	/**
	 * @see LinkedResourceTest#resolvePath(org.eclipse.core.runtime.IPath)
	 */
	protected IPath resolvePath(IPath path) {
		return getWorkspace().getPathVariableManager().resolvePath(path);
	}
}
