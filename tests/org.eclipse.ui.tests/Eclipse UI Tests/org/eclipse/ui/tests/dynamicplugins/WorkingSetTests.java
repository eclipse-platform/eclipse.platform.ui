/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.tests.dynamicplugins;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkingSet;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.intro.IIntroPart;
import org.osgi.framework.Bundle;

/**
 * @since 3.3
 */
public class WorkingSetTests extends DynamicTestCase {

	/**
	 * @param testName
	 */
	public WorkingSetTests(String testName) {
		super(testName);
	}

     /* (non-Javadoc)
     * @see org.eclipse.ui.tests.dynamicplugins.DynamicTestCase#getMarkerClass()
     */
    protected String getMarkerClass() {
    	return "org.eclipse.ui.dynamic.DynamicWorkingSetElementAdapter";
    }
    
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.tests.dynamicplugins.DynamicTestCase#getExtensionId()
	 */
	protected String getExtensionId() {
		return "newWorkingSet1.testDynamicWorkingSetAddition1";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.tests.dynamicplugins.DynamicTestCase#getExtensionPoint()
	 */
	protected String getExtensionPoint() {
		return IWorkbenchRegistryConstants.PL_WORKINGSETS;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.tests.dynamicplugins.DynamicTestCase#getInstallLocation()
	 */
	protected String getInstallLocation() {
		return "data/org.eclipse.newWorkingSet1";
	}

	/**
	 * Tests to ensure that if you've loaded a working set extension that uses
	 * the basic element adapter then it will be invoked even before the bundle
	 * is activated.
	 */
	public void testWorkingSetWithBasicElementAdapter() {
		IWorkingSetManager workingSetManager = PlatformUI.getWorkbench()
				.getWorkingSetManager();
		IAdaptable adaptable = new IAdaptable() {

			public Object getAdapter(Class adapter) {
				if (adapter == IResource.class)
					return ResourcesPlugin.getWorkspace().getRoot();
				return null;
			}
		};
		WorkingSet workingSet = (WorkingSet) workingSetManager.createWorkingSet(
				"dynamicSet2", new IAdaptable[0]);
		workingSet.setId("org.eclipse.newWorkingSet1.WorkingSet2");
		workingSetManager.addWorkingSet(workingSet);
		IAdaptable[] elementsToAdapt = new IAdaptable[] { adaptable };
		IAdaptable[] adaptedElements = workingSet.adaptElements(elementsToAdapt);
		// make sure the identity is used
		assertEquals(elementsToAdapt, adaptedElements);
		getBundle();
		// ensure that the bundle is not already active
		assertFalse("Bundle is active too early", Bundle.ACTIVE == getBundle().getState());
		adaptedElements = workingSet.adaptElements(elementsToAdapt);
		assertEquals(1, adaptedElements.length);
		// ensure that the adapter above has been invoked
		assertEquals(ResourcesPlugin.getWorkspace().getRoot(), adaptedElements[0]);
		// ensure that the bundle is still not active
		assertFalse("Bundle is active after adapt", Bundle.ACTIVE == getBundle().getState());
		
		removeBundle();
		adaptedElements = workingSet.adaptElements(elementsToAdapt);
		// make sure the identity is used again
		assertEquals(elementsToAdapt, adaptedElements);
	}

	
	/**
	 * Tests to ensure that if you've loaded a working set extension that uses
	 * the it's own element adapter then it will be not cause bundle activation early.
	 */
	public void testWorkingSetWithCustomElementAdapter() throws ClassNotFoundException {
		IWorkingSetManager workingSetManager = PlatformUI.getWorkbench()
				.getWorkingSetManager();
		IAdaptable adaptable = new IAdaptable() {

			public Object getAdapter(Class adapter) {
				if (adapter == IResource.class)
					return ResourcesPlugin.getWorkspace().getRoot();
				return null;
			}
		};
		WorkingSet workingSet = (WorkingSet) workingSetManager.createWorkingSet(
				"dynamicSet1", new IAdaptable[0]);
		workingSet.setId("org.eclipse.newWorkingSet1.WorkingSet1");
		workingSetManager.addWorkingSet(workingSet);
		IAdaptable[] elementsToAdapt = new IAdaptable[] { adaptable };
		IAdaptable[] adaptedElements = workingSet.adaptElements(elementsToAdapt);
		// make sure the identity is used
		assertEquals(elementsToAdapt, adaptedElements);
		getBundle();
		// ensure that the bundle is not already active
		assertFalse("Bundle is active too early", Bundle.ACTIVE == getBundle().getState());
		adaptedElements = workingSet.adaptElements(elementsToAdapt);
		//ensure that the identity is still returned, confirming that the bundle has not been activated
		assertEquals(elementsToAdapt, adaptedElements);
		//activate the bundle by touching a class 
		getBundle().loadClass(getMarkerClass());
		assertEquals("Bundle not active after classloading", Bundle.ACTIVE, getBundle().getState());
		//try the adapt again.  The custom element adapter should be used
		adaptedElements = workingSet.adaptElements(elementsToAdapt);
		assertEquals(1, adaptedElements.length);
		assertTrue(adaptedElements[0] instanceof IIntroPart);
		
		removeBundle();
		adaptedElements = workingSet.adaptElements(elementsToAdapt);
		// make sure the identity is used again
		assertEquals(elementsToAdapt, adaptedElements);
	}
}
