/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
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

	/**
	 * Core of the problem: while Eclipse is running, name collisions among working sets
	 * don't matter. However, on save and restart names will be used to identify working
	 * sets, which could possibly lead to cycles in aggregate working sets.
	 * 
	 * Bottom line: if there are multiple aggregate working sets with the same name, expect
	 * trouble on restart.
	 * 
	 * To create a cycle we have to be creative:
	 * - create an aggregate1 with an ID = "testCycle"
	 * - create an aggregate2 with an ID = "testCycle" containing aggregate1
	 * - save it into IMemento
	 * 
	 * Now the IMememnto creates a self reference:
	 * 
	 * <workingSet name="testCycle" label="testCycle" aggregate="true">
	 * 	<workingSet IMemento.internal.id="testCycle" />
	 * </workingSet>
	 * 
	 * All we have to do to emulate stack overflow is to create a working set based on this IMemento.
	 * 
	 * @throws Throwable
	 */
	public void testWorkingSetCycle() throws Throwable {
		IWorkingSetManager manager = fWorkbench.getWorkingSetManager();
		
		// create an IMemento with a cycle in it
		IAggregateWorkingSet aggregate = (IAggregateWorkingSet) manager
		.createAggregateWorkingSet("testCycle","testCycle", new IWorkingSet[0]);
		
		IAggregateWorkingSet aggregate2 = (IAggregateWorkingSet) manager
		.createAggregateWorkingSet("testCycle","testCycle", new IWorkingSet[] {aggregate});
		
		IMemento memento=XMLMemento.createWriteRoot(IWorkbenchConstants.TAG_WORKING_SET);
		aggregate2.saveState(memento);
		
		// load the IMemento
		IAggregateWorkingSet aggregateReloaded = null;
		try {
			aggregateReloaded = (IAggregateWorkingSet) manager.createWorkingSet(memento);
			manager.addWorkingSet(aggregateReloaded);
			aggregateReloaded.getComponents();
		} catch (StackOverflowError e) {
			e.printStackTrace();
			fail("Stack overflow for self-referenced aggregate working set", e);
		} finally {
			if (aggregateReloaded != null)
				manager.removeWorkingSet(aggregateReloaded);
		}
	}
	
	/**
	 * Tests cleanup of the cycle from an aggregate working set.
	 * @throws Throwable
	 */
	public void testCycleCleanup() throws Throwable {
		IWorkingSetManager manager = fWorkbench.getWorkingSetManager();
		
		// create an IMemento with a cycle in it: { good, good, cycle, good, good }
		IAggregateWorkingSet aggregateSub0 = (IAggregateWorkingSet) manager
		.createAggregateWorkingSet("testCycle0","testCycle0", new IWorkingSet[0]);
		
		IAggregateWorkingSet aggregateSub1 = (IAggregateWorkingSet) manager
		.createAggregateWorkingSet("testCycle1","testCycle1", new IWorkingSet[0]);

		IAggregateWorkingSet aggregateSub2 = (IAggregateWorkingSet) manager
		.createAggregateWorkingSet("testCycle","testCycle", new IWorkingSet[0]); // cycle
		
		IAggregateWorkingSet aggregateSub3 = (IAggregateWorkingSet) manager
		.createAggregateWorkingSet("testCycle3","testCycle3", new IWorkingSet[0]);
		
		IAggregateWorkingSet aggregateSub4 = (IAggregateWorkingSet) manager
		.createAggregateWorkingSet("testCycle4","testCycle4", new IWorkingSet[0]);
		
		
		IAggregateWorkingSet aggregate = (IAggregateWorkingSet) manager
		.createAggregateWorkingSet("testCycle","testCycle", new IWorkingSet[] {aggregateSub0, 
				aggregateSub1, aggregateSub2, aggregateSub3, aggregateSub4});

		manager.addWorkingSet(aggregateSub0);
		manager.addWorkingSet(aggregateSub1);
		manager.addWorkingSet(aggregateSub3);
		manager.addWorkingSet(aggregateSub4);
		
		IMemento memento=XMLMemento.createWriteRoot(IWorkbenchConstants.TAG_WORKING_SET);
		aggregate.saveState(memento);
		
		// load the IMemento
		IAggregateWorkingSet aggregateReloaded = null;
		try {
			aggregateReloaded = (IAggregateWorkingSet) manager.createWorkingSet(memento);
			manager.addWorkingSet(aggregateReloaded);
			IWorkingSet[] aggregates = aggregateReloaded.getComponents();
			assertNotNull(aggregates);
			assertEquals(4, aggregates.length);
			for(int i = 0; i < aggregates.length; i++)
				assertFalse("testCycle".equals(aggregates[i].getName()));
		} catch (StackOverflowError e) {
			e.printStackTrace();
			fail("Stack overflow for self-referenced aggregate working set", e);
		} finally {
			if (aggregateReloaded != null)
				manager.removeWorkingSet(aggregateReloaded);
		}
	}
}
