/*******************************************************************************
 * Copyright (c) 2006, 2017 IBM Corporation and others.
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

package org.eclipse.ui.tests.dynamicplugins;

import static org.eclipse.ui.tests.harness.util.UITestUtil.processEvents;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkingSet;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.intro.IIntroPart;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.osgi.framework.Bundle;

/**
 * @since 3.3
 */
@RunWith(JUnit4.class)
public class WorkingSetTests extends DynamicTestCase {

	public WorkingSetTests() {
		super(WorkingSetTests.class.getSimpleName());
	}

	@Override
	protected String getMarkerClass() {
		return "org.eclipse.ui.dynamic.DynamicWorkingSetElementAdapter";
	}

	@Test
	@Ignore
	@Override
	public void testClass() throws Exception {
		super.testClass();
		// commented out for now - it's causing grief
	}

	@Override
	protected String getExtensionId() {
		return "newWorkingSet1.testDynamicWorkingSetAddition1";
	}

	@Override
	protected String getExtensionPoint() {
		return IWorkbenchRegistryConstants.PL_WORKINGSETS;
	}

	@Override
	protected String getInstallLocation() {
		return "data/org.eclipse.newWorkingSet1";
	}

	/**
	 * Tests to ensure that the working set updater is unloaded after the bundle
	 * is unloaded.
	 */
	@Test
	public void testWorkingSetUpdater() throws ClassNotFoundException {
		final PropertyChangeEvent[] events = new PropertyChangeEvent[1];
		IWorkingSetManager workingSetManager = PlatformUI.getWorkbench()
				.getWorkingSetManager();
		WorkingSet workingSet = (WorkingSet) workingSetManager
				.createWorkingSet("dynamicSet3", new IAdaptable[0]);
		workingSet.setId("org.eclipse.newWorkingSet1.WorkingSet1");
		workingSetManager.addWorkingSet(workingSet);

		IPropertyChangeListener propertyChangeListener = new IPropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent event) {
				synchronized (this) {
					events[0] = event;
				}
			}
		};

		workingSetManager.addPropertyChangeListener(propertyChangeListener);
		try {
			getBundle();
			getBundle().loadClass(getMarkerClass()); // ensure the class is
														// loaded
			assertEquals("Bundle not active after classloading", Bundle.ACTIVE,
					getBundle().getState());
			PropertyChangeEvent event = events[0];
			int i = 0;
			while (event == null && i <= 10) {
				try {
					processEvents();
					Thread.sleep(250);
					i++;
				} catch (InterruptedException e) {
				}
				synchronized (propertyChangeListener) {
					event = events[0];
				}
			}
			assertNotNull(event);
			assertEquals(
					IWorkingSetManager.CHANGE_WORKING_SET_UPDATER_INSTALLED,
					event.getProperty());
			assertEquals(null, event.getOldValue());
			assertNotNull(event.getNewValue());
			assertTrue(event.getNewValue().getClass().getName().equals(
					"org.eclipse.ui.dynamic.DynamicWorkingSetUpdater"));
			events[0] = null;
			i = 0;
			removeBundle();
			event = events[0];
			while (event == null && i <= 10) {
				try {
					processEvents();
					Thread.sleep(250);
					i++;
				} catch (InterruptedException e) {
				}
				synchronized (propertyChangeListener) {
					event = events[0];
				}
			}
			assertNotNull(event);
			assertEquals(
					IWorkingSetManager.CHANGE_WORKING_SET_UPDATER_UNINSTALLED,
					event.getProperty());
			assertEquals(null, event.getNewValue());
			assertNotNull(event.getOldValue());
			assertTrue(event.getOldValue().getClass().getName().equals(
					"org.eclipse.ui.dynamic.DynamicWorkingSetUpdater"));
		} finally {
			workingSetManager
					.removePropertyChangeListener(propertyChangeListener);
		}
	}

	/**
	 * Tests to ensure that if you've loaded a working set extension that uses
	 * the basic element adapter then it will be invoked even before the bundle
	 * is activated.
	 */
	@Test
	public void testWorkingSetWithBasicElementAdapter() {
		IWorkingSetManager workingSetManager = PlatformUI.getWorkbench()
				.getWorkingSetManager();
		IAdaptable adaptable = new IAdaptable() {

			@SuppressWarnings("unchecked")
			@Override
			public <T> T getAdapter(Class<T> adapter) {
				if (adapter == IResource.class) {
					return (T) ResourcesPlugin.getWorkspace().getRoot();
				}
				return null;
			}
		};
		WorkingSet workingSet = (WorkingSet) workingSetManager
				.createWorkingSet("dynamicSet2", new IAdaptable[0]);
		workingSet.setId("org.eclipse.newWorkingSet1.WorkingSet2");
		workingSetManager.addWorkingSet(workingSet);
		IAdaptable[] elementsToAdapt = new IAdaptable[] { adaptable };
		IAdaptable[] adaptedElements = workingSet
				.adaptElements(elementsToAdapt);
		// make sure the identity is used
		assertEquals(elementsToAdapt, adaptedElements);
		getBundle();
		// ensure that the bundle is not already active
		assertFalse("Bundle is active too early", Bundle.ACTIVE == getBundle()
				.getState());
		adaptedElements = workingSet.adaptElements(elementsToAdapt);
		assertEquals(1, adaptedElements.length);
		// ensure that the adapter above has been invoked
		assertEquals(ResourcesPlugin.getWorkspace().getRoot(),
				adaptedElements[0]);
		// ensure that the bundle is still not active
		assertFalse("Bundle is active after adapt",
				Bundle.ACTIVE == getBundle().getState());

		removeBundle();
		adaptedElements = workingSet.adaptElements(elementsToAdapt);
		// make sure the identity is used again
		assertEquals(elementsToAdapt, adaptedElements);
	}

	/**
	 * Tests to ensure that if you've loaded a working set extension that uses
	 * the it's own element adapter then it will be not cause bundle activation
	 * early.
	 */
	@Test
	public void testWorkingSetWithCustomElementAdapter()
			throws ClassNotFoundException {
		IWorkingSetManager workingSetManager = PlatformUI.getWorkbench()
				.getWorkingSetManager();
		IAdaptable adaptable = new IAdaptable() {

			@SuppressWarnings("unchecked")
			@Override
			public <T> T getAdapter(Class<T> adapter) {
				if (adapter == IResource.class) {
					return (T) ResourcesPlugin.getWorkspace().getRoot();
				}
				return null;
			}
		};
		WorkingSet workingSet = (WorkingSet) workingSetManager
				.createWorkingSet("dynamicSet1", new IAdaptable[0]);
		workingSet.setId("org.eclipse.newWorkingSet1.WorkingSet1");
		workingSetManager.addWorkingSet(workingSet);
		IAdaptable[] elementsToAdapt = new IAdaptable[] { adaptable };
		IAdaptable[] adaptedElements = workingSet
				.adaptElements(elementsToAdapt);
		// make sure the identity is used
		assertEquals(elementsToAdapt, adaptedElements);
		getBundle();
		// ensure that the bundle is not already active
		assertFalse("Bundle is active too early", Bundle.ACTIVE == getBundle()
				.getState());
		adaptedElements = workingSet.adaptElements(elementsToAdapt);
		// ensure that the identity is still returned, confirming that the
		// bundle has not been activated
		assertEquals(elementsToAdapt, adaptedElements);
		// activate the bundle by touching a class
		getBundle().loadClass(getMarkerClass());
		assertEquals("Bundle not active after classloading", Bundle.ACTIVE,
				getBundle().getState());
		// try the adapt again. The custom element adapter should be used
		adaptedElements = workingSet.adaptElements(elementsToAdapt);
		assertEquals(1, adaptedElements.length);
		assertTrue(adaptedElements[0] instanceof IIntroPart);

		removeBundle();
		adaptedElements = workingSet.adaptElements(elementsToAdapt);
		// make sure the identity is used again
		assertEquals(elementsToAdapt, adaptedElements);
	}
}
