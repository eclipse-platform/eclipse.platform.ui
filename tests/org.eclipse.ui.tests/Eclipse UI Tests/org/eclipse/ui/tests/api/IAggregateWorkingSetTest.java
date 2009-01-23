/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.api;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.IAggregateWorkingSet;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.tests.harness.util.ArrayUtil;
import org.eclipse.ui.tests.harness.util.UITestCase;

public class IAggregateWorkingSetTest extends UITestCase {

	final static String WORKING_SET_NAME = "testws";
	final static String AGGREGATE_WORKING_SET_NAME_ = "testaggregatews";

	IWorkspace fWorkspace;

	IWorkingSet[] components;
	IAggregateWorkingSet fWorkingSet;

	public IAggregateWorkingSetTest(String testName) {
		super(testName);
	}

	protected void doSetUp() throws Exception {
		super.doSetUp();
		IWorkingSetManager workingSetManager = fWorkbench
		.getWorkingSetManager();

		fWorkspace = ResourcesPlugin.getWorkspace();
		components = new IWorkingSet[4];
		for (int i = 0; i < 4; i++) {
			components[i] = workingSetManager.createWorkingSet(WORKING_SET_NAME
					+ i, new IAdaptable[] {});
			workingSetManager.addWorkingSet(components[i]);
		}
		fWorkingSet = (IAggregateWorkingSet) workingSetManager
		.createAggregateWorkingSet(AGGREGATE_WORKING_SET_NAME_,
				AGGREGATE_WORKING_SET_NAME_, components);

		workingSetManager.addWorkingSet(fWorkingSet);
	}
	protected void doTearDown() throws Exception {
		IWorkingSetManager workingSetManager = fWorkbench.getWorkingSetManager();
		workingSetManager.removeWorkingSet(fWorkingSet);
		for (int i = 0; i < components.length; i++) {
			workingSetManager.removeWorkingSet(components[i]);
		}    	
		super.doTearDown();
	}

	public void testSaveWSet() throws Throwable {
		//<possible client code>
		IWorkingSetManager workingSetManager = fWorkbench
		.getWorkingSetManager();
		IWorkingSet set=workingSetManager.getWorkingSet(AGGREGATE_WORKING_SET_NAME_);
		if(set.isAggregateWorkingSet()){
			IWorkingSet[] sets=((IAggregateWorkingSet)set).getComponents();
			if(sets.length>=1){
				sets[0]=null; //client fails to pay enough attention to specs or unknowingly does this
			}
		}
		//</possible client code>    	
		//error makes it look like it comes from workingsets api, with no clue about the actual culprit 
		IMemento memento=XMLMemento.createWriteRoot(IWorkbenchConstants.TAG_WORKING_SET);
		set.saveState(memento);
	}
	public void testGetElemets() throws Throwable {
		//<possible client code>
		IWorkingSetManager workingSetManager = fWorkbench
		.getWorkingSetManager();
		IWorkingSet set=workingSetManager.getWorkingSet(AGGREGATE_WORKING_SET_NAME_);
		if(set.isAggregateWorkingSet()){
			IWorkingSet[] sets=((IAggregateWorkingSet)set).getComponents();
			if(sets.length>1){
				//code 2 fails to pay enough attention to specs or unknowingly does this
				sets[0]=workingSetManager.createWorkingSet(WORKING_SET_NAME, new IAdaptable[] { fWorkspace.getRoot() }); 
				//code 1 part  removes a workingset
				workingSetManager.removeWorkingSet(sets[1]);
			}
		}
		//</possible client code>    	
		
		//unexpected 
		assertTrue(ArrayUtil.equals(
				new IAdaptable[] {},
				fWorkingSet.getElements()));
	}
}
