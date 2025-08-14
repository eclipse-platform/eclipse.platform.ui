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
 *     Tomasz Zarna <tomasz.zarna@tasktop.com> - Bug 37183
 *******************************************************************************/
package org.eclipse.ui.tests.api;

import static org.eclipse.ui.PlatformUI.getWorkbench;
import static org.eclipse.ui.tests.harness.util.UITestUtil.openTestWindow;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThrows;

import java.util.Arrays;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.dialogs.IWorkingSetSelectionDialog;
import org.eclipse.ui.dialogs.WorkingSetConfigurationBlock;
import org.eclipse.ui.tests.harness.util.ArrayUtil;
import org.eclipse.ui.tests.harness.util.UITestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class IWorkingSetManagerTest extends UITestCase {
	static final String WORKING_SET_NAME_1 = "ws1";

	static final String WORKING_SET_NAME_2 = "ws2";

	static final String WORKING_SET_NAME_3 = "ws3";

	IWorkingSetManager fWorkingSetManager;

	IWorkspace fWorkspace;

	IWorkingSet fWorkingSet;

	String fChangeProperty;

	IWorkingSet fChangeNewValue;

	IWorkingSet fChangeOldValue;

	class TestPropertyChangeListener implements IPropertyChangeListener {
		@Override
		public void propertyChange(PropertyChangeEvent event) {
			fChangeProperty = event.getProperty();
			fChangeNewValue = (IWorkingSet) event.getNewValue();
			fChangeOldValue = (IWorkingSet) event.getOldValue();
		}
	}

	public IWorkingSetManagerTest() {
		super(IWorkingSetManagerTest.class.getSimpleName());
	}

	@Override
	protected void doSetUp() throws Exception {
		super.doSetUp();
		fWorkingSetManager = getWorkbench().getWorkingSetManager();
		fWorkspace = ResourcesPlugin.getWorkspace();
		fWorkingSet = fWorkingSetManager.createWorkingSet(WORKING_SET_NAME_1,
				new IAdaptable[] { fWorkspace.getRoot() });

		IWorkingSet[] workingSets = fWorkingSetManager.getWorkingSets();
		for (IWorkingSet workingSet : workingSets) {
			fWorkingSetManager.removeWorkingSet(workingSet);
		}
	}

	void resetChangeData() {
		fChangeProperty = "";
		fChangeNewValue = null;
		fChangeOldValue = null;
	}

	/**
	 * Tests the utility method found on the WorkingSetConfigurationBlock.
	 */
	@Test
	public void testConfigBlockFilter() {
		final String [] setIds = new String[] {"5", "2", "4", "1", "3" };

		IWorkingSet [] sets = new IWorkingSet[setIds.length * 3];
		for (int i = 0; i < setIds.length; i++) {
			sets[i * 3] = createSet(setIds, i);
			sets[i * 3 + 1] = createSet(setIds, i);
			sets[i * 3 + 2] = createSet(setIds, i);
		}
		IWorkingSet [] newSets = WorkingSetConfigurationBlock.filter(sets, setIds);
		assertEquals(sets.length, newSets.length);

		for (String setId : setIds) {
			newSets = WorkingSetConfigurationBlock.filter(sets, new String [] {setId});
			assertEquals(3, newSets.length);
			assertEquals(setId, newSets[0].getId());
			assertEquals(setId, newSets[1].getId());
			assertEquals(setId, newSets[2].getId());
		}

	}

	@Test
	public void testAddPropertyChangeListener() throws Throwable {
		IPropertyChangeListener listener = new TestPropertyChangeListener();
		fWorkingSetManager.addPropertyChangeListener(listener);

		resetChangeData();
		fWorkingSetManager.removeWorkingSet(fWorkingSet);
		assertEquals("", fChangeProperty);

		resetChangeData();
		fWorkingSetManager.addWorkingSet(fWorkingSet);
		assertEquals(IWorkingSetManager.CHANGE_WORKING_SET_ADD, fChangeProperty);
		assertEquals(null, fChangeOldValue);
		assertEquals(fWorkingSet, fChangeNewValue);

		resetChangeData();
		fWorkingSetManager.removeWorkingSet(fWorkingSet);
		assertEquals(IWorkingSetManager.CHANGE_WORKING_SET_REMOVE,
				fChangeProperty);
		assertEquals(fWorkingSet, fChangeOldValue);
		assertEquals(null, fChangeNewValue);

		resetChangeData();
		// Set the label first to something other than the new name.
		// This will allow us to test for the name property apart from the label
		// property
		fWorkingSet.setLabel(WORKING_SET_NAME_3);
		assertEquals(IWorkingSetManager.CHANGE_WORKING_SET_LABEL_CHANGE,
				fChangeProperty);
		assertEquals(WORKING_SET_NAME_1, fChangeOldValue.getLabel());
		assertEquals(fWorkingSet, fChangeNewValue);
		fWorkingSet.setName(WORKING_SET_NAME_2);
		assertEquals(IWorkingSetManager.CHANGE_WORKING_SET_NAME_CHANGE,
				fChangeProperty);
		assertEquals(WORKING_SET_NAME_1, fChangeOldValue.getName());
		assertEquals(fWorkingSet, fChangeNewValue);

		resetChangeData();
		fWorkingSet.setElements(new IAdaptable[] {});
		assertEquals(IWorkingSetManager.CHANGE_WORKING_SET_CONTENT_CHANGE,
				fChangeProperty);
		assertEquals(1, fChangeOldValue.getElements().length);
		assertEquals(fWorkingSet, fChangeNewValue);
	}

	@Test
	public void testAddRecentWorkingSet() throws Throwable {
		fWorkingSetManager.addRecentWorkingSet(fWorkingSet);
		fWorkingSetManager.addWorkingSet(fWorkingSet);
		assertArrayEquals(new IWorkingSet[] { fWorkingSet }, fWorkingSetManager.getRecentWorkingSets());

		IWorkingSet workingSet2 = fWorkingSetManager.createWorkingSet(
				WORKING_SET_NAME_2, new IAdaptable[] { fWorkspace.getRoot() });
		fWorkingSetManager.addRecentWorkingSet(workingSet2);
		fWorkingSetManager.addWorkingSet(workingSet2);
		assertArrayEquals(new IWorkingSet[] { workingSet2, fWorkingSet }, fWorkingSetManager.getRecentWorkingSets());
	}

	@Test
	public void testAddWorkingSet() throws Throwable {
		fWorkingSetManager.addWorkingSet(fWorkingSet);
		assertArrayEquals(new IWorkingSet[] { fWorkingSet }, fWorkingSetManager.getWorkingSets());

		boolean exceptionThrown = false;
		try {
			fWorkingSetManager.addWorkingSet(fWorkingSet);
		} catch (RuntimeException exception) {
			exceptionThrown = true;
		}
		assertTrue(exceptionThrown);
		assertArrayEquals(new IWorkingSet[] { fWorkingSet }, fWorkingSetManager.getWorkingSets());
	}

	@Test
	public void testCreateWorkingSet() throws Throwable {
		IWorkingSet workingSet2 = fWorkingSetManager.createWorkingSet(
				WORKING_SET_NAME_2, new IAdaptable[] { fWorkspace.getRoot() });
		assertEquals(WORKING_SET_NAME_2, workingSet2.getName());
		assertArrayEquals(new IAdaptable[] { fWorkspace.getRoot() }, workingSet2.getElements());

		workingSet2 = fWorkingSetManager.createWorkingSet("",
				new IAdaptable[] {});
		assertEquals("", workingSet2.getName());
		assertArrayEquals(new IAdaptable[] {}, workingSet2.getElements());
	}

	@Test
	public void testCreateWorkingSetFromMemento() throws Throwable {
		IWorkingSet workingSet2 = fWorkingSetManager.createWorkingSet(
				WORKING_SET_NAME_2, new IAdaptable[] { fWorkspace.getRoot() });
		IMemento memento = XMLMemento.createWriteRoot("savedState"); //$NON-NLS-1$
		workingSet2.saveState(memento);
		IWorkingSet restoredWorkingSet2 = fWorkingSetManager
				.createWorkingSet(memento);
		assertEquals(WORKING_SET_NAME_2, restoredWorkingSet2.getName());
		assertArrayEquals(new IAdaptable[] { fWorkspace.getRoot() }, restoredWorkingSet2.getElements());
	}

	@Test
	public void testCreateWorkingSetSelectionDialog() throws Throwable {
		IWorkbenchWindow window = openTestWindow();
		IWorkingSetSelectionDialog dialog = fWorkingSetManager
				.createWorkingSetSelectionDialog(window.getShell(), true);

		assertNotNull(dialog);
	}

	@Test
	public void testGetRecentWorkingSets() throws Throwable {
		assertEquals(0, fWorkingSetManager.getRecentWorkingSets().length);

		fWorkingSetManager.addRecentWorkingSet(fWorkingSet);
		fWorkingSetManager.addWorkingSet(fWorkingSet);
		assertArrayEquals(new IWorkingSet[] { fWorkingSet }, fWorkingSetManager.getRecentWorkingSets());

		IWorkingSet workingSet2 = fWorkingSetManager.createWorkingSet(
				WORKING_SET_NAME_2, new IAdaptable[] { fWorkspace.getRoot() });
		fWorkingSetManager.addRecentWorkingSet(workingSet2);
		fWorkingSetManager.addWorkingSet(workingSet2);
		assertArrayEquals(new IWorkingSet[] { workingSet2, fWorkingSet }, fWorkingSetManager.getRecentWorkingSets());

		fWorkingSetManager.removeWorkingSet(workingSet2);
		assertArrayEquals(new IWorkingSet[] { fWorkingSet }, fWorkingSetManager.getRecentWorkingSets());
	}

	@Test
	public void testRecentWorkingSetsLength() throws Throwable {
		int oldMRULength =  fWorkingSetManager.getRecentWorkingSetsLength();
		try {
			fWorkingSetManager.setRecentWorkingSetsLength(10);

			IWorkingSet[] workingSets = new IWorkingSet[10];
			for (int i = 0 ; i < 10; i++) {
				IWorkingSet workingSet = fWorkingSetManager.createWorkingSet(
						"ws_" + (i + 1), new IAdaptable[] { fWorkspace.getRoot() });
				fWorkingSetManager.addRecentWorkingSet(workingSet);
				fWorkingSetManager.addWorkingSet(workingSet);
				workingSets[9 - i] = workingSet;
			}
			assertArrayEquals(workingSets, fWorkingSetManager.getRecentWorkingSets());

			fWorkingSetManager.setRecentWorkingSetsLength(7);
			IWorkingSet[] workingSets7 = new IWorkingSet[7];
			System.arraycopy(workingSets, 0, workingSets7, 0, 7);
			assertArrayEquals(workingSets7, fWorkingSetManager.getRecentWorkingSets());

			fWorkingSetManager.setRecentWorkingSetsLength(9);
			IWorkingSet[] workingSets9 = new IWorkingSet[9];
			System.arraycopy(workingSets, 0, workingSets9, 2, 7);

			for (int i = 7 ; i < 9; i++) {
				IWorkingSet workingSet = fWorkingSetManager.createWorkingSet(
						"ws_addded_" + (i + 1), new IAdaptable[] { fWorkspace.getRoot() });
				fWorkingSetManager.addRecentWorkingSet(workingSet);
				fWorkingSetManager.addWorkingSet(workingSet);
				workingSets9[8 - i] = workingSet;
			}

			assertArrayEquals(workingSets9, fWorkingSetManager.getRecentWorkingSets());
		} finally {
			if (oldMRULength > 0) {
				fWorkingSetManager.setRecentWorkingSetsLength(oldMRULength);
			}
		}
	}

	@Test
	public void testGetWorkingSet() throws Throwable {
		assertNull(fWorkingSetManager.getWorkingSet(WORKING_SET_NAME_1));

		fWorkingSetManager.addWorkingSet(fWorkingSet);
		assertNotNull(fWorkingSetManager.getWorkingSet(fWorkingSet.getName()));

		assertNull(fWorkingSetManager.getWorkingSet(""));

		assertNull(fWorkingSetManager.getWorkingSet(null));
	}

	@Test
	public void testGetWorkingSets() throws Throwable {
		assertArrayEquals(new IWorkingSet[] {}, fWorkingSetManager.getWorkingSets());

		fWorkingSetManager.addWorkingSet(fWorkingSet);
		assertArrayEquals(new IWorkingSet[] { fWorkingSet }, fWorkingSetManager.getWorkingSets());

		assertThrows("added same set twice", RuntimeException.class,
				() -> fWorkingSetManager.addWorkingSet(fWorkingSet));
		assertArrayEquals(new IWorkingSet[] { fWorkingSet }, fWorkingSetManager.getWorkingSets());

		IWorkingSet workingSet2 = fWorkingSetManager.createWorkingSet(
				WORKING_SET_NAME_2, new IAdaptable[] { fWorkspace.getRoot() });
		fWorkingSetManager.addWorkingSet(workingSet2);
		assertTrue(ArrayUtil.contains(fWorkingSetManager.getWorkingSets(),
				workingSet2));
		assertTrue(ArrayUtil.contains(fWorkingSetManager.getWorkingSets(),
				fWorkingSet));

		IWorkingSet workingSet3 = fWorkingSetManager.createWorkingSet(
				WORKING_SET_NAME_2, new IAdaptable[] { fWorkspace.getRoot() });
		workingSet3.setName("ws0");
		workingSet3.setLabel(WORKING_SET_NAME_2); // reset the label - it
													// would be set to ws0 by
													// the above call.
		fWorkingSetManager.addWorkingSet(workingSet3);

		// asserts the order is correct - the name of set three should push it
		// above set two even though their labels are the same
		IWorkingSet[] sets = fWorkingSetManager.getWorkingSets();
		assertEquals(fWorkingSet, sets[0]);
		assertEquals(workingSet2, sets[2]);
		assertEquals(workingSet3, sets[1]);

		IWorkingSet workingSet3a = fWorkingSetManager.createWorkingSet(
				WORKING_SET_NAME_2 + "\u200b", new IAdaptable[] { fWorkspace.getRoot() });
		workingSet3.setLabel(WORKING_SET_NAME_2); // reset the label - it

		fWorkingSetManager.addWorkingSet(workingSet3a);
		assertFalse(workingSet3a.equals(workingSet3));

		sets = fWorkingSetManager.getWorkingSets();
		assertEquals(4, sets.length);

	}

	@Test
	public void testRemovePropertyChangeListener() throws Throwable {
		IPropertyChangeListener listener = new TestPropertyChangeListener();

		fWorkingSetManager.removePropertyChangeListener(listener);

		fWorkingSetManager.addPropertyChangeListener(listener);
		fWorkingSetManager.removePropertyChangeListener(listener);

		resetChangeData();
		fWorkingSet.setName(WORKING_SET_NAME_1);
		assertEquals("", fChangeProperty);
	}

	@Test
	public void testRemoveWorkingSet() throws Throwable {
		fWorkingSetManager.removeWorkingSet(fWorkingSet);
		assertArrayEquals(new IWorkingSet[] {}, fWorkingSetManager.getWorkingSets());

		fWorkingSetManager.addWorkingSet(fWorkingSet);
		IWorkingSet workingSet2 = fWorkingSetManager.createWorkingSet(
				WORKING_SET_NAME_2, new IAdaptable[] { fWorkspace.getRoot() });
		fWorkingSetManager.addWorkingSet(workingSet2);
		fWorkingSetManager.removeWorkingSet(fWorkingSet);
		assertArrayEquals(new IWorkingSet[] { workingSet2 }, fWorkingSetManager.getWorkingSets());
	}

	@Test
	public void testRemoveWorkingSetAfterRename() throws Throwable {
		/* get workingSetManager */
		IWorkingSetManager workingSetManager = getWorkbench().getWorkingSetManager();

		workingSetManager.addWorkingSet(fWorkingSet);
		String origName=fWorkingSet.getName();

		/* check that workingSetManager contains "fWorkingSet"*/
		assertArrayEquals(new IWorkingSet[] { fWorkingSet }, workingSetManager.getWorkingSets());

		fWorkingSet.setName(" ");
		assertEquals(" ", fWorkingSet.getName());

		/* remove "fWorkingSet" from working set manager */
		workingSetManager.removeWorkingSet(fWorkingSet);

		/* check that "fWorkingSet" was removed  after rename*/
		if (!Arrays.equals(new IWorkingSet[] {},
				workingSetManager.getWorkingSets())){
			/*Test Failure, report after restoring state*/
			fWorkingSet.setName(origName);
			workingSetManager.removeWorkingSet(fWorkingSet);
			fail("expected that fWorkingSet has been removed");
		}

	}
	/**
	 * Tests to ensure that a misbehaving listener does not bring down the manager.
	 */
	@Test
	public void testListenerSafety() throws Throwable {
		final boolean[] result = new boolean[1];
		// add a bogus listener that dies unexpectedly
		IPropertyChangeListener badListener = event -> {
			throw new TestException();

		};
		IPropertyChangeListener goodListener = event -> result[0] = true;
		fWorkingSetManager.addPropertyChangeListener(badListener);
		fWorkingSetManager.addPropertyChangeListener(goodListener);
		try {
			IWorkingSet set = fWorkingSetManager.createWorkingSet("foo",
					new IAdaptable[0]);
			fWorkingSetManager.addWorkingSet(set);

			assertTrue("Good listener wasn't invoked", result[0]);
		} finally {
			fWorkingSetManager.removePropertyChangeListener(badListener);
			fWorkingSetManager.removePropertyChangeListener(goodListener);
		}
	}

	private IWorkingSet createSet(final String[] setIds, final int i) {
		return new IWorkingSet() {

			@Override
			public IAdaptable[] adaptElements(IAdaptable[] objects) {
				return null;
			}

			@Override
			public IAdaptable[] getElements() {
				return null;
			}

			@Override
			public String getId() {
				return setIds[i] + "";
			}

			@Override
			public ImageDescriptor getImage() {
				return null;
			}

			@Override
			public ImageDescriptor getImageDescriptor() {
				return null;
			}

			@Override
			public String getLabel() {
				return null;
			}

			@Override
			public String getName() {
				return null;
			}

			@Override
			public boolean isAggregateWorkingSet() {
				return false;
			}

			@Override
			public boolean isEditable() {
				return true;
			}

			@Override
			public boolean isEmpty() {
				return false;
			}

			@Override
			public boolean isSelfUpdating() {
				return false;
			}

			@Override
			public boolean isVisible() {
				return true;
			}

			@Override
			public void setElements(IAdaptable[] elements) {
			}

			@Override
			public void setId(String id) {
			}

			@Override
			public void setLabel(String label) {
			}

			@Override
			public void setName(String name) {
			}

			@Override
			public String getFactoryId() {
				return null;
			}

			@Override
			public void saveState(IMemento memento) {
			}

			@Override
			public <T> T getAdapter(Class<T> adapter) {
				return null;
			}

			@Override
			public String toString() {
				return getId();
			}
		};

	}
}
