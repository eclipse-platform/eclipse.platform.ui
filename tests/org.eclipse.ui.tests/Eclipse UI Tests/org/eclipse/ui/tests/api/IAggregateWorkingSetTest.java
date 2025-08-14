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

import static org.eclipse.ui.tests.harness.util.UITestUtil.processEvents;
import static org.eclipse.ui.tests.harness.util.UITestUtil.waitForJobs;
import static org.junit.Assert.assertArrayEquals;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.ui.IAggregateWorkingSet;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.internal.AbstractWorkingSet;
import org.eclipse.ui.internal.AbstractWorkingSetManager;
import org.eclipse.ui.internal.AggregateWorkingSet;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.tests.harness.util.UITestCase;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class IAggregateWorkingSetTest extends UITestCase {

	static final String WORKING_SET_NAME = "testws";
	static final String AGGREGATE_WORKING_SET_NAME_ = "testaggregatews";
	static final String WSET_PAGE_ID="org.eclipse.ui.resourceWorkingSetPage";
	IWorkspace fWorkspace;
	IWorkbench fWorkbench;

	IWorkingSet[] components;
	List<IWorkingSet> backup;
	IAggregateWorkingSet fWorkingSet;

	public IAggregateWorkingSetTest() {
		super(IAggregateWorkingSetTest.class.getSimpleName());
	}

	@Override
	protected void doSetUp() throws Exception {
		super.doSetUp();
		IWorkingSetManager workingSetManager = PlatformUI.getWorkbench().getWorkingSetManager();
		backup = Arrays.asList(workingSetManager.getAllWorkingSets());

		fWorkspace = ResourcesPlugin.getWorkspace();
		fWorkbench = PlatformUI.getWorkbench();
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
	@Override
	protected void doTearDown() throws Exception {
		IWorkingSetManager workingSetManager = fWorkbench.getWorkingSetManager();
		workingSetManager.removeWorkingSet(fWorkingSet);
		for (IWorkingSet component : components) {
			workingSetManager.removeWorkingSet(component);
		}
		IWorkingSet[] sets = workingSetManager.getAllWorkingSets();
		for (IWorkingSet wset : sets) {
			if (!backup.contains(wset)) {
				workingSetManager.removeWorkingSet(wset);
			}
		}
		fWorkbench = null;
		super.doTearDown();
	}

	@Test
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
	@Test
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
		assertArrayEquals(new IAdaptable[] {}, fWorkingSet.getElements());
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
	 * <pre>
	 * &lt;workingSet name="testCycle" label="testCycle" aggregate="true"&gt;
	 * 	&lt;workingSet IMemento.internal.id="testCycle" /&gt;
	 * &lt;/workingSet&gt;
	 * </pre>
	 *
	 * All we have to do to emulate stack overflow is to create a working set based on this IMemento.
	 */
	@Test
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
		} finally {
			if (aggregateReloaded != null) {
				manager.removeWorkingSet(aggregateReloaded);
			}
		}
	}

	/**
	 * Tests cleanup of the cycle from an aggregate working set.
	 */
	@Test
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
			for (IWorkingSet aggregate2 : aggregates) {
				assertFalse("testCycle".equals(aggregate2.getName()));
			}
		} finally {
			if (aggregateReloaded != null) {
				manager.removeWorkingSet(aggregateReloaded);
			}
		}
	}

	/*
	 * Test related to Bug 217955.The initial fix made changes that caused
	 * save/restore to fail due to early restore and forward reference in
	 * memento of aggregates
	 */
	/* TODO test must be enabled after bug 479217 is fixed */
	@Test
	@Ignore("Bug 479217")
	public void XXXtestWorkingSetSaveRestoreAggregates() throws Throwable {
		IWorkingSetManager manager = fWorkbench.getWorkingSetManager();
		String nameA = "A";
		String nameB = "B";
		String nameC = "C";

		IWorkingSet wSetA = manager
				.createWorkingSet(nameA, new IAdaptable[] {});
		manager.addWorkingSet(wSetA);

		IAggregateWorkingSet wSetB = (IAggregateWorkingSet) manager
		.createAggregateWorkingSet(nameB, nameB, new IWorkingSet[] {});
		manager.addWorkingSet(wSetB);

		IAggregateWorkingSet wSetC = (IAggregateWorkingSet) manager
				.createAggregateWorkingSet(nameC, nameC, new IWorkingSet[0]);
		manager.addWorkingSet(wSetC);

		try {
			assertEquals("Failed to add workingset" + nameA, wSetA, manager
					.getWorkingSet(nameA));

			assertEquals("Failed to add workingset" + nameC, wSetC, manager
					.getWorkingSet(nameC));

			assertEquals("Failed to add workingset" + nameB, wSetB, manager
					.getWorkingSet(nameB));

			assertEquals(0, wSetB.getComponents().length);

			invokeMethod(AggregateWorkingSet.class, "setComponents", wSetB,
					new Object[] { new IWorkingSet[] {
							wSetA, wSetC } },
					new Class[] { new IWorkingSet[] {}.getClass() });

			assertArrayEquals(new IWorkingSet[] { wSetA, wSetC }, wSetB.getComponents());

			IMemento workingSets = saveAndRemoveWorkingSets(wSetA, wSetB, wSetC);
			processEvents();
			waitForJobs(500, 3000);

			assertNull(manager.getWorkingSet(nameA));
			assertNull(manager.getWorkingSet(nameB));
			assertNull(manager.getWorkingSet(nameC));

			restoreWorkingSetManager(workingSets);
			processEvents();
			waitForJobs(500, 3000);

			IWorkingSet restoredA = manager.getWorkingSet(nameA);
			assertNotNull("Unable to save/restore correctly", restoredA);

			IAggregateWorkingSet restoredB = (IAggregateWorkingSet) manager.getWorkingSet(nameB);

			IAggregateWorkingSet restoredC = (IAggregateWorkingSet) manager.getWorkingSet(nameC);

			assertNotNull("Unable to save/restore correctly", restoredC);
			assertNotNull("Unable to save/restore correctly", restoredB);

			assertArrayEquals(nameB + " has lost data in the process of save/restore: " + restoredB,
					wSetB.getComponents(), restoredB.getComponents());
		} finally {
			// restore
			IWorkingSet set = manager.getWorkingSet(nameA);
			if (set != null) {
				manager.removeWorkingSet(set);
			}
			set = manager.getWorkingSet(nameB);
			if (set != null) {
				manager.removeWorkingSet(set);
			}
			set = manager.getWorkingSet(nameC);
			if (set != null) {
				manager.removeWorkingSet(set);
			}
		}
	}

	/* test which passes as long as bug 479217 is not fixed */
	@Test
	public void testWorkingSetSaveNeverRestoresAggregate() throws Throwable {
		IWorkingSetManager manager = fWorkbench.getWorkingSetManager();
		String nameA = "A";
		String nameB = "B";
		String nameC = "C";

		IWorkingSet wSetA = manager.createWorkingSet(nameA, new IAdaptable[] {});
		manager.addWorkingSet(wSetA);

		IAggregateWorkingSet wSetB = (IAggregateWorkingSet) manager.createAggregateWorkingSet(nameB, nameB,
				new IWorkingSet[] {});
		manager.addWorkingSet(wSetB);

		IAggregateWorkingSet wSetC = (IAggregateWorkingSet) manager.createAggregateWorkingSet(nameC, nameC,
				new IWorkingSet[0]);
		manager.addWorkingSet(wSetC);

		try {
			assertEquals("Failed to add workingset" + nameA, wSetA, manager.getWorkingSet(nameA));

			assertEquals("Failed to add workingset" + nameC, wSetC, manager.getWorkingSet(nameC));

			assertEquals("Failed to add workingset" + nameB, wSetB, manager.getWorkingSet(nameB));

			assertEquals(0, wSetB.getComponents().length);

			invokeMethod(AggregateWorkingSet.class, "setComponents", wSetB,
					new Object[] { new IWorkingSet[] { wSetA, wSetC } },
					new Class[] { new IWorkingSet[] {}.getClass() });

			assertArrayEquals(new IWorkingSet[] { wSetA, wSetC }, wSetB.getComponents());

			IMemento workingSets = saveAndRemoveWorkingSets(wSetA, wSetB, wSetC);
			processEvents();
			waitForJobs(500, 3000);

			assertNull(manager.getWorkingSet(nameA));
			assertNull(manager.getWorkingSet(nameB));
			assertNull(manager.getWorkingSet(nameC));

			final AtomicReference<String> error = new AtomicReference<>();

			// Exploit the bug 479217 in
			// AbstractWorkingSetManager.restoreWorkingSetState():
			// every client which wants to see components of the working set
			// *before* the restoreWorkingSetState() is done, can silently (!!!)
			// damage the AggregateWorkingSet being restored
			IPropertyChangeListener badListener = event -> {
				if (event.getProperty() != IWorkingSetManager.CHANGE_WORKING_SET_ADD) {
					return;
				}
				// simply resolve the working set before the manager creates
				// another one
				Object ws = event.getNewValue();
				if (!(ws instanceof AggregateWorkingSet aws)) {
					return;
				}
				IMemento m;
				try {
					m = readField(AbstractWorkingSet.class, "workingSetMemento", IMemento.class, aws);
				} catch (Exception e) {
					error.set(e.getMessage());
					return;
				}
				IWorkingSet[] sets = aws.getComponents();
				if (m != null) {
					IMemento[] msets = m.getChildren(IWorkbenchConstants.TAG_WORKING_SET);
					if (msets.length != sets.length) {
						// KABOOM!
						error.set("Working set lost due the bad listener! " + "restored: " + Arrays.toString(sets)
								+ ", expected: " + Arrays.toString(msets));
					}
				} else if (nameB.equals(aws.getName()) && sets.length != 2) {
					// someone was faster
					error.set("Working set lost due the bad listener! " + "restored: " + Arrays.toString(sets));
				}
			};
			try {
				manager.addPropertyChangeListener(badListener);

				restoreWorkingSetManager(workingSets);
				processEvents();

				IWorkingSet restoredA = manager.getWorkingSet(nameA);
				assertNotNull("Unable to save/restore correctly", restoredA);

				IAggregateWorkingSet restoredB = (IAggregateWorkingSet) manager.getWorkingSet(nameB);
				assertNotNull("Unable to save/restore correctly", restoredB);

				IAggregateWorkingSet restoredC = (IAggregateWorkingSet) manager.getWorkingSet(nameC);
				assertNotNull("Unable to save/restore correctly", restoredC);

				IWorkingSet[] componenents1 = wSetB.getComponents();
				IWorkingSet[] componenents2 = restoredB.getComponents();
				assertEquals(2, componenents1.length);
				// this is the bug 479217: we should see 2 elements, and not 1!
				assertEquals(1, componenents2.length);
				// if the bug is fixed, the error must be null
				assertNotNull(error.get());
			} finally {
				manager.removePropertyChangeListener(badListener);
			}

		} finally {
			// restore
			IWorkingSet set = manager.getWorkingSet(nameA);
			if (set != null) {
				manager.removeWorkingSet(set);
			}
			set = manager.getWorkingSet(nameB);
			if (set != null) {
				manager.removeWorkingSet(set);
			}
			set = manager.getWorkingSet(nameC);
			if (set != null) {
				manager.removeWorkingSet(set);
			}
		}
	}

	private IMemento saveAndRemoveWorkingSets(IWorkingSet... sets) throws Exception {
		IMemento managerMemento = XMLMemento
				.createWriteRoot(IWorkbenchConstants.TAG_WORKING_SET_MANAGER);
		IWorkingSetManager manager = fWorkbench.getWorkingSetManager();
		for (IWorkingSet set : sets) {
			if(set.getId()==null){
				//set default id as set by factory
				set.setId(WSET_PAGE_ID);
			}
		}
		invokeMethod(AbstractWorkingSetManager.class, "saveWorkingSetState",
				manager, new Object[] { managerMemento },
				new Class[] { IMemento.class });
		invokeMethod(AbstractWorkingSetManager.class, "saveMruList", manager,
				new Object[] { managerMemento }, new Class[] { IMemento.class });
		for (IWorkingSet set : sets) {
			((AbstractWorkingSet) set).disconnect();
		}
		for (IWorkingSet set : sets) {
			manager.removeWorkingSet(set);
		}
		//manager.dispose(); //not needed, also cause problems
		return managerMemento;
	}

	private void restoreWorkingSetManager(IMemento managerMemento) throws Exception {
		IWorkingSetManager manager = fWorkbench.getWorkingSetManager();

		invokeMethod(AbstractWorkingSetManager.class, "restoreWorkingSetState",
				manager, new Object[] { managerMemento },
				new Class[] { IMemento.class });
		invokeMethod(AbstractWorkingSetManager.class, "restoreMruList",
				manager, new Object[] { managerMemento },
				new Class[] { IMemento.class });
	}

	private Object invokeMethod(Class<?> clazz, String methodName, Object instance, Object[] args,
			Class<?>[] argsClasses) throws Exception {
		Method method = clazz.getDeclaredMethod(methodName, argsClasses);
		method.setAccessible(true);
		return method.invoke(instance, args);
	}

	private <T> T readField(Class<?> clazz, String filedName, Class<T> type, Object instance)
			throws Exception {
		Field field = clazz.getDeclaredField(filedName);
		field.setAccessible(true);
		return type.cast(field.get(instance));
	}
}
