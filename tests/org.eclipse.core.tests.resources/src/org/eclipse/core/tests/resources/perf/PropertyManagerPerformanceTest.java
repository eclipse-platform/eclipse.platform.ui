/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.resources.perf;

import java.util.*;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.internal.resources.Workspace;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.tests.harness.PerformanceTestRunner;
import org.eclipse.core.tests.resources.ResourceTest;

public class PropertyManagerPerformanceTest extends ResourceTest {

	public static String getPropertyValue(int size) {
		StringBuffer value = new StringBuffer(size);
		for (int i = 0; i < size; i++)
			value.append((char) (Math.random() * Character.MAX_VALUE));
		return value.toString();
	}

	public static Test suite() {
		//			TestSuite suite = new TestSuite();
		//			suite.addTest(new PropertyManagerTest("testProperties"));
		//			return suite;
		return new TestSuite(PropertyManagerPerformanceTest.class);
	}

	public PropertyManagerPerformanceTest(String name) {
		super(name);
	}

	/**
	 * Creates a tree of resources. 
	 */
	private List createTree(IFolder base, int filesPerFolder) {
		IFolder[] folders = new IFolder[5];
		folders[0] = base.getFolder("folder1");
		folders[1] = base.getFolder("folder2");
		folders[2] = folders[0].getFolder("folder3");
		folders[3] = folders[2].getFolder("folder4");
		folders[4] = folders[3].getFolder("folder5");
		List resources = new ArrayList(filesPerFolder * folders.length);
		for (int i = 0; i < folders.length; i++)
			resources.add(folders[i]);
		ensureExistsInWorkspace(folders, true);
		for (int i = 0; i < folders.length; i++) {
			for (int j = 0; j < filesPerFolder; j++) {
				IFile file = folders[i].getFile("file" + j);
				ensureExistsInWorkspace(file, getRandomContents());
				resources.add(file);
			}
		}
		return resources;
	}

	private void testGetProperty(int filesPerFolder, final int properties, int measurements, int repetitions) {
		IProject proj1 = getWorkspace().getRoot().getProject("proj1");
		final IFolder folder1 = proj1.getFolder("folder1");
		final List allResources = createTree(folder1, filesPerFolder);
		for (Iterator i = allResources.iterator(); i.hasNext();) {
			IResource resource = (IResource) i.next();
			for (int j = 0; j < properties; j++)
				try {
					resource.setPersistentProperty(new QualifiedName(PI_RESOURCES_TESTS, "prop" + j), getPropertyValue(200));
				} catch (CoreException ce) {
					fail("0.2", ce);
				}
		}

		new PerformanceTestRunner() {
			protected void test() {
				for (int j = 0; j < properties; j++)
					for (Iterator i = allResources.iterator(); i.hasNext();) {
						IResource resource = (IResource) i.next();
						try {
							assertNotNull(resource.getPersistentProperty(new QualifiedName(PI_RESOURCES_TESTS, "prop" + j)));
						} catch (CoreException ce) {
							fail("0.2", ce);
						}
					}
			}
		}.run(this, measurements, repetitions);
		try {
			((Workspace) getWorkspace()).getPropertyManager().deleteProperties(folder1, IResource.DEPTH_INFINITE);
		} catch (CoreException e) {
			fail("0.1", e);
		}

	}

	public void testGetProperty100x4() {
		testGetProperty(100, 4, 10, 2);
	}

	public void testGetProperty20x20() {
		testGetProperty(20, 20, 10, 2);
	}

	public void testGetProperty4x100() {
		testGetProperty(4, 100, 10, 1);
	}

	private void testSetProperty(int filesPerFolder, int properties, int measurements, int repetitions) {
		IProject proj1 = getWorkspace().getRoot().getProject("proj1");
		final IFolder folder1 = proj1.getFolder("folder1");
		final List allResources = createTree(folder1, filesPerFolder);
		new PerformanceTestRunner() {

			protected void tearDown() {
				try {
					((Workspace) getWorkspace()).getPropertyManager().deleteProperties(folder1, IResource.DEPTH_INFINITE);
				} catch (CoreException e) {
					fail("0.1", e);
				}
			}

			protected void test() {
				for (Iterator i = allResources.iterator(); i.hasNext();) {
					IResource resource = (IResource) i.next();
					try {
						resource.setPersistentProperty(new QualifiedName(PI_RESOURCES_TESTS, "prop" + ((int) Math.random() * 50)), getPropertyValue(200));
					} catch (CoreException ce) {
						fail("0.2", ce);
					}
				}
			}
		}.run(this, measurements, repetitions);
	}

	public void testSetProperty100x4() {
		testSetProperty(100, 4, 10, 1);
	}

	public void testSetProperty20x20() {
		testSetProperty(20, 20, 10, 4);
	}

	public void testSetProperty4x100() {
		testSetProperty(4, 100, 10, 20);
	}
}
