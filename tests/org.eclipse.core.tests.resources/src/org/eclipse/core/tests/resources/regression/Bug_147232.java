/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.resources.regression;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.tests.internal.builders.AbstractBuilderTest;
import org.eclipse.core.tests.internal.builders.ClearMarkersBuilder;

/**
 * Tests duplicate resource change events caused by a builder that makes
 * no changes.
 */
public class Bug_147232 extends AbstractBuilderTest implements IResourceChangeListener {
	/**
	 * Records the number of times we have seen the file creation delta
	 */
	int deltaSeenCount;

	IFile file;
	IProject project;

	public static Test suite() {
		return new TestSuite(Bug_147232.class);
	}

	public Bug_147232(String name) {
		super(name);
	}

	public void resourceChanged(IResourceChangeEvent event) {
		//we are only concerned with seeing duplicate post change events
		if (event.getType() != IResourceChangeEvent.POST_CHANGE)
			return;
		//record occurrence of the file creation delta if we find it
		IResourceDelta delta = event.getDelta().findMember(file.getFullPath());
		if (delta != null && delta.getKind() == IResourceDelta.ADDED)
			deltaSeenCount++;
	}

	protected void setUp() throws Exception {
		super.setUp();
		// make the builder wait after running to all a POST_CHANGE event to occur before POST_BUILD
		ClearMarkersBuilder.pauseAfterBuild = true;
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		getWorkspace().removeResourceChangeListener(this);
		ClearMarkersBuilder.pauseAfterBuild = false;
	}

	public void testBug() {
		project = getWorkspace().getRoot().getProject("Bug_147232");
		file = project.getFile("file.txt");
		getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE | IResourceChangeEvent.PRE_BUILD | IResourceChangeEvent.POST_BUILD);
		try {
			setAutoBuilding(false);
			project.create(getMonitor());
			project.open(getMonitor());
			addBuilder(project, ClearMarkersBuilder.BUILDER_NAME);
			setAutoBuilding(true);
			waitForBuild();
		} catch (CoreException e) {
			fail("0.99", e);
		}
		//create a file in the project to trigger a build
		try {
			create(file, true);
		} catch (CoreException e) {
			fail("1.99", e);
		}
		waitForBuild();
		assertEquals("2.0", 1, deltaSeenCount);
	}

}
