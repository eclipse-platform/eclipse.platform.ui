/*******************************************************************************
 * Copyright (c) 2000, 2024 IBM Corporation and others.
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
package org.eclipse.ui.tests.api;

import static org.eclipse.ui.PlatformUI.getWorkbench;
import static org.junit.Assert.assertArrayEquals;

import java.util.Arrays;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.internal.WorkingSet;
import org.eclipse.ui.tests.harness.util.FileUtil;
import org.eclipse.ui.tests.harness.util.UITestCase;
import org.eclipse.ui.tests.menus.ObjectContributionClasses.IA;
import org.eclipse.ui.tests.menus.ObjectContributionClasses.ICommon;
import org.eclipse.ui.tests.menus.ObjectContributionClasses.IModelElement;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class IWorkingSetTest extends UITestCase {
	static final String WORKING_SET_NAME_1 = "ws1";

	static final String WORKING_SET_NAME_2 = "ws2";

	IWorkspace fWorkspace;

	IWorkingSet fWorkingSet;

	public IWorkingSetTest() {
		super(IWorkingSetTest.class.getSimpleName());
	}

	@Override
	protected void doSetUp() throws Exception {
		super.doSetUp();
		IWorkingSetManager workingSetManager = getWorkbench().getWorkingSetManager();

		fWorkspace = ResourcesPlugin.getWorkspace();
		fWorkingSet = workingSetManager.createWorkingSet(WORKING_SET_NAME_1,
				new IAdaptable[] { fWorkspace.getRoot() });

		workingSetManager.addWorkingSet(fWorkingSet);
	}
	@Override
	protected void doTearDown() throws Exception {
		IWorkingSetManager workingSetManager = getWorkbench().getWorkingSetManager();
		workingSetManager.removeWorkingSet(fWorkingSet);
		super.doTearDown();
	}
	@Test
	public void testGetElements() throws Throwable {
		assertEquals(fWorkspace.getRoot(), fWorkingSet.getElements()[0]);
	}

	@Test
	public void testGetId() throws Throwable {
		assertEquals(null, fWorkingSet.getId());
		fWorkingSet.setId("bogusId");
		assertEquals("bogusId", fWorkingSet.getId());
		fWorkingSet.setId(null);
		assertEquals(null, fWorkingSet.getId());
	}

	@Test
	public void testGetName() throws Throwable {
		assertEquals(WORKING_SET_NAME_1, fWorkingSet.getName());
	}

	@Test
	public void testSetElements() throws Throwable {
		boolean exceptionThrown = false;

		try {
			fWorkingSet.setElements(null);
		} catch (RuntimeException exception) {
			exceptionThrown = true;
		}
		assertTrue(exceptionThrown);

		IProject p1 = FileUtil.createProject("TP1");
		IFile f1 = FileUtil.createFile("f1.txt", p1);
		IAdaptable[] elements = new IAdaptable[] { f1, p1 };
		fWorkingSet.setElements(elements);
		assertArrayEquals(elements, fWorkingSet.getElements());

		fWorkingSet.setElements(new IAdaptable[] { f1 });
		assertEquals(f1, fWorkingSet.getElements()[0]);

		fWorkingSet.setElements(new IAdaptable[] {});
		assertEquals(0, fWorkingSet.getElements().length);
	}

	@Test
	public void testSetId() throws Throwable {
		assertEquals(null, fWorkingSet.getId());
		fWorkingSet.setId("bogusId");
		assertEquals("bogusId", fWorkingSet.getId());
		fWorkingSet.setId(null);
		assertEquals(null, fWorkingSet.getId());
	}

	@Test
	public void testSetName() throws Throwable {
		boolean exceptionThrown = false;

		try {
			fWorkingSet.setName(null);
		} catch (RuntimeException exception) {
			exceptionThrown = true;
		}
		assertTrue(exceptionThrown);

		fWorkingSet.setName(WORKING_SET_NAME_2);
		assertEquals(WORKING_SET_NAME_2, fWorkingSet.getName());

		exceptionThrown = false;
		try {
			String name = fWorkingSet.getName();
			// set same name
			fWorkingSet.setName(name);
		} catch (RuntimeException exception) {
			exceptionThrown = true;
		}
		assertFalse("Failed to setName when new name is same as old name",
				exceptionThrown);

		fWorkingSet.setName("");
		assertEquals("", fWorkingSet.getName());

		fWorkingSet.setName(" ");
		assertEquals(" ", fWorkingSet.getName());
	}

	@Test
	public void testNoDuplicateWorkingSetName() throws Throwable {
		/* get workingSetManager */
		IWorkingSetManager workingSetManager = getWorkbench().getWorkingSetManager();

		/*
		 * check that initially workingSetManager contains "fWorkingSet"
		 */
		assertArrayEquals(new IWorkingSet[] { fWorkingSet }, workingSetManager.getWorkingSets());

		IWorkingSet wSet = workingSetManager.createWorkingSet(
				WORKING_SET_NAME_2, new IAdaptable[] {});
		workingSetManager.addWorkingSet(wSet);

		/* check that workingSetManager contains "fWorkingSet" and wSet */
		assertTrue(Arrays.equals(new IWorkingSet[] { fWorkingSet, wSet },
				workingSetManager.getWorkingSets())
				|| Arrays.equals(new IWorkingSet[] { wSet, fWorkingSet },
						workingSetManager.getWorkingSets()));

		String sameName = fWorkingSet.getName();
		boolean exceptionThrown = false;

		try {
			wSet.setName(sameName);
			/* Test failed,set original name for restoring state */
			wSet.setName(WORKING_SET_NAME_2);
		} catch (RuntimeException exception) {
			exceptionThrown = true;
		}
		assertTrue(exceptionThrown);

		/* restore state */
		workingSetManager.removeWorkingSet(wSet);
	}

	@Test
	public void testNoDuplicateWorkingSetNamesDifferentLabels()
			throws Throwable {
		/* get workingSetManager */
		IWorkingSetManager workingSetManager = getWorkbench().getWorkingSetManager();
		/*
		 * check that initially workingSetManager contains "fWorkingSet"
		 */
		assertArrayEquals(new IWorkingSet[] { fWorkingSet }, workingSetManager.getWorkingSets());

		String sameName = fWorkingSet.getName();
		IWorkingSet wSet = workingSetManager.createWorkingSet(sameName,
				new IAdaptable[] {});
		wSet.setLabel(WORKING_SET_NAME_2);

		/*
		 * Expected to throw an error as the wSet has the same name as
		 * fWorkingSet
		 */
		boolean exceptionThrown = false;
		try {
			workingSetManager.addWorkingSet(wSet);
			/* Test failed, restore state */
			workingSetManager.removeWorkingSet(wSet);
		} catch (RuntimeException exception) {
			exceptionThrown = true;
		}
		assertTrue(exceptionThrown);
	}

	@Test
	public void testIsEmpty() {
		fWorkingSet.setElements(new IAdaptable[] {});
		assertTrue(fWorkingSet.isEmpty());
		fWorkingSet.setElements(new IAdaptable[] { new IAdaptable() {
			@Override
			public <T> T getAdapter(Class<T> adapter) {
				return null;
			}
		} });
		assertFalse(fWorkingSet.isEmpty());
	}


	@Test
	public void testApplicableTo_ResourceWorkingSet() {
		fWorkingSet.setId("org.eclipse.ui.resourceWorkingSetPage");
		assertEquals("org.eclipse.ui.resourceWorkingSetPage", fWorkingSet
				.getId());
		IAdaptable[] adapted = fWorkingSet.adaptElements(new IAdaptable[] {ResourcesPlugin.getWorkspace()
				.getRoot()});
		assertEquals(1, adapted.length);
		assertTrue(adapted[0] instanceof IWorkspaceRoot);
	}

	@Test
	public void testApplicableTo_DirectComparison() {

		fWorkingSet.setId("org.eclipse.ui.tests.api.MockWorkingSet");
		Foo myFoo = new Foo();
		IAdaptable[] adapted = fWorkingSet.adaptElements(new IAdaptable[] {myFoo});
		assertEquals(1, adapted.length);
		assertTrue(adapted[0] instanceof Foo);
	}

	@Test
	public void testApplicableTo_Inheritance() {
		fWorkingSet.setId("org.eclipse.ui.tests.api.MockWorkingSet");
		Bar myBar = new Bar();
		IAdaptable[] adapted = fWorkingSet.adaptElements(new IAdaptable[] {myBar});
		assertEquals(1, adapted.length);
		assertTrue(adapted[0] instanceof Bar);
	}

	@Test
	public void testApplicableTo_Adapter1() {
		fWorkingSet.setId("org.eclipse.ui.tests.api.MockWorkingSet");
		ToFoo tc = new ToFoo();
		IAdaptable[] adapted = fWorkingSet.adaptElements(new IAdaptable[] {tc});
		assertEquals(1, adapted.length);
		assertTrue(adapted[0] instanceof Foo);
	}

	@Test
	public void testApplicableTo_AdapterManager1() {
		fWorkingSet.setId("org.eclipse.ui.tests.api.MockWorkingSet");
		IAImpl ia = new IAImpl();
		IAdaptable[] adapted = fWorkingSet.adaptElements(new IAdaptable[] {ia});
		assertEquals(1, adapted.length);
		assertTrue(adapted[0] instanceof ICommon);
	}

	/**
	 * Tests that adaptable=false is working.  ModelElement has a registered adapter to IResource that should not be used.
	 */
	@Test
	public void testApplicableTo_AdapterManager2() {
		fWorkingSet.setId("org.eclipse.ui.tests.api.MockWorkingSet");
		ModelElement element = new ModelElement();
		assertTrue(fWorkingSet.adaptElements(new IAdaptable[] {element}).length == 0);
	}

	/**
	 * Tests to verify that we don't fall down in the event that the factory
	 * throws an exception while restoring a working set.
	 */
	@Test
	public void testBadFactory_Restore() {
		fWorkingSet
				.setElements(new IAdaptable[] { new BadElementFactory.BadElementInstance() });
		IMemento m = XMLMemento.createWriteRoot("ws");
		fWorkingSet.saveState(m);
		BadElementFactory.shouldFailOnCreateElement = true;
		IWorkingSet copy = new WorkingSet(fWorkingSet.getName(), fWorkingSet.getId(), m) {};
		assertFalse(BadElementFactory.elementCreationAttemptedWhileShouldFail);
		IAdaptable[] elements = copy.getElements();
		assertTrue(BadElementFactory.elementCreationAttemptedWhileShouldFail);
		assertEquals("Element array should be empty", 0, elements.length);
	}

	/**
	 * Tests to verify that we don't fall down in the event that the persistable
	 * throws an exception while saving a working set.
	 */
	@Test
	public void testBadFactory_Save() {
		fWorkingSet
				.setElements(new IAdaptable[] { new BadElementFactory.BadElementInstance() });
		IMemento m = XMLMemento.createWriteRoot("ws");
		BadElementFactory.BadElementInstance.shouldSaveFail = true;
		assertFalse(BadElementFactory.BadElementInstance.saveAttemptedWhileShouldFail);
		fWorkingSet.saveState(m);
		assertTrue(BadElementFactory.BadElementInstance.saveAttemptedWhileShouldFail);
	}

	public static class Foo implements IAdaptable {

		@Override
		public <T> T getAdapter(Class<T> adapter) {
			return null;
		}
	}

	public static class Bar extends Foo {

	}

	public class ToFoo implements IAdaptable {

		@SuppressWarnings("unchecked")
		@Override
		public <T> T getAdapter(Class<T> adapter) {
			if (adapter == Foo.class) {
				return (T) new Foo() {
				};
			}
			return null;
		}

	}

	public static class IAImpl implements IA, IAdaptable {

		@Override
		public <T> T getAdapter(Class<T> adapter) {
			return null;
		}
	}

	public static class ModelElement implements IModelElement, IAdaptable {

		@Override
		public <T> T getAdapter(Class<T> adapter) {
			return null;
		}

	}
}
