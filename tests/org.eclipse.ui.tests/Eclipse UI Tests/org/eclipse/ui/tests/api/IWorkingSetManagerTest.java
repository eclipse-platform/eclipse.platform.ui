/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
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

public class IWorkingSetManagerTest extends UITestCase {
    final static String WORKING_SET_NAME_1 = "ws1";

    final static String WORKING_SET_NAME_2 = "ws2";
    
    final static String WORKING_SET_NAME_3 = "ws3";

    IWorkingSetManager fWorkingSetManager;

    IWorkspace fWorkspace;

    IWorkingSet fWorkingSet;

    String fChangeProperty;

    Object fChangeNewValue;

    Object fChangeOldValue;

    class TestPropertyChangeListener implements IPropertyChangeListener {
        @Override
		public void propertyChange(PropertyChangeEvent event) {
            fChangeProperty = event.getProperty();
            fChangeNewValue = event.getNewValue();
            fChangeOldValue = event.getOldValue();
        }
    }

    public IWorkingSetManagerTest(String testName) {
        super(testName);
    }

    @Override
	protected void doSetUp() throws Exception {
        super.doSetUp();
        fWorkingSetManager = fWorkbench.getWorkingSetManager();
        fWorkspace = ResourcesPlugin.getWorkspace();
        fWorkingSet = fWorkingSetManager.createWorkingSet(WORKING_SET_NAME_1,
                new IAdaptable[] { fWorkspace.getRoot() });

        IWorkingSet[] workingSets = fWorkingSetManager.getWorkingSets();
        for (int i = 0; i < workingSets.length; i++) {
            fWorkingSetManager.removeWorkingSet(workingSets[i]);
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
    	
    	for (int j = 0; j < setIds.length; j++) {
    		newSets = WorkingSetConfigurationBlock.filter(sets, new String [] {setIds[j]});	
    		assertEquals(3, newSets.length);
    		assertEquals(setIds[j], newSets[0].getId());
    		assertEquals(setIds[j], newSets[1].getId());
    		assertEquals(setIds[j], newSets[2].getId());
		}
    	
    }

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
		fWorkingSet.setLabel(WORKING_SET_NAME_3); // set the label first to
													// something other than the
													// new name. This will allow
													// us to test for the name
													// property apart from the
													// label property
		assertEquals(IWorkingSetManager.CHANGE_WORKING_SET_LABEL_CHANGE,
				fChangeProperty);
		assertEquals(null, fChangeOldValue);
		assertEquals(fWorkingSet, fChangeNewValue);
		fWorkingSet.setName(WORKING_SET_NAME_2);
		assertEquals(IWorkingSetManager.CHANGE_WORKING_SET_NAME_CHANGE,
				fChangeProperty);
		assertEquals(null, fChangeOldValue);
		assertEquals(fWorkingSet, fChangeNewValue);

        resetChangeData();
        fWorkingSet.setElements(new IAdaptable[] {});
        assertEquals(IWorkingSetManager.CHANGE_WORKING_SET_CONTENT_CHANGE,
                fChangeProperty);
        assertEquals(null, fChangeOldValue);
        assertEquals(fWorkingSet, fChangeNewValue);
    }

    public void testAddRecentWorkingSet() throws Throwable {
        fWorkingSetManager.addRecentWorkingSet(fWorkingSet);
        fWorkingSetManager.addWorkingSet(fWorkingSet);
        assertTrue(ArrayUtil.equals(new IWorkingSet[] { fWorkingSet },
                fWorkingSetManager.getRecentWorkingSets()));

        IWorkingSet workingSet2 = fWorkingSetManager.createWorkingSet(
                WORKING_SET_NAME_2, new IAdaptable[] { fWorkspace.getRoot() });
        fWorkingSetManager.addRecentWorkingSet(workingSet2);
        fWorkingSetManager.addWorkingSet(workingSet2);
        assertTrue(ArrayUtil.equals(new IWorkingSet[] { workingSet2,
                fWorkingSet }, fWorkingSetManager.getRecentWorkingSets()));
    }

    public void testAddWorkingSet() throws Throwable {
        fWorkingSetManager.addWorkingSet(fWorkingSet);
        assertTrue(ArrayUtil.equals(new IWorkingSet[] { fWorkingSet },
                fWorkingSetManager.getWorkingSets()));

        boolean exceptionThrown = false;
        try {
            fWorkingSetManager.addWorkingSet(fWorkingSet);
        } catch (RuntimeException exception) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
        assertTrue(ArrayUtil.equals(new IWorkingSet[] { fWorkingSet },
                fWorkingSetManager.getWorkingSets()));
    }

    public void testCreateWorkingSet() throws Throwable {
        IWorkingSet workingSet2 = fWorkingSetManager.createWorkingSet(
                WORKING_SET_NAME_2, new IAdaptable[] { fWorkspace.getRoot() });
        assertEquals(WORKING_SET_NAME_2, workingSet2.getName());
        assertTrue(ArrayUtil.equals(new IAdaptable[] { fWorkspace.getRoot() },
                workingSet2.getElements()));

        workingSet2 = fWorkingSetManager.createWorkingSet("",
                new IAdaptable[] {});
        assertEquals("", workingSet2.getName());
        assertTrue(ArrayUtil.equals(new IAdaptable[] {}, workingSet2
                .getElements()));
    }

    public void testCreateWorkingSetFromMemento() throws Throwable {
        IWorkingSet workingSet2 = fWorkingSetManager.createWorkingSet(
                WORKING_SET_NAME_2, new IAdaptable[] { fWorkspace.getRoot() });
        IMemento memento = XMLMemento.createWriteRoot("savedState"); //$NON-NLS-1$
        workingSet2.saveState(memento);
        IWorkingSet restoredWorkingSet2 = fWorkingSetManager
                .createWorkingSet(memento);
        assertEquals(WORKING_SET_NAME_2, restoredWorkingSet2.getName());
        assertTrue(ArrayUtil.equals(new IAdaptable[] { fWorkspace.getRoot() },
                restoredWorkingSet2.getElements()));
    }

    public void testCreateWorkingSetSelectionDialog() throws Throwable {
        IWorkbenchWindow window = openTestWindow();
        IWorkingSetSelectionDialog dialog = fWorkingSetManager
                .createWorkingSetSelectionDialog(window.getShell(), true);

        assertNotNull(dialog);
    }

    public void testGetRecentWorkingSets() throws Throwable {
        assertEquals(0, fWorkingSetManager.getRecentWorkingSets().length);

        fWorkingSetManager.addRecentWorkingSet(fWorkingSet);
        fWorkingSetManager.addWorkingSet(fWorkingSet);
        assertTrue(ArrayUtil.equals(new IWorkingSet[] { fWorkingSet },
                fWorkingSetManager.getRecentWorkingSets()));

        IWorkingSet workingSet2 = fWorkingSetManager.createWorkingSet(
                WORKING_SET_NAME_2, new IAdaptable[] { fWorkspace.getRoot() });
        fWorkingSetManager.addRecentWorkingSet(workingSet2);
        fWorkingSetManager.addWorkingSet(workingSet2);
        assertTrue(ArrayUtil.equals(new IWorkingSet[] { workingSet2,
                fWorkingSet }, fWorkingSetManager.getRecentWorkingSets()));

        fWorkingSetManager.removeWorkingSet(workingSet2);
        assertTrue(ArrayUtil.equals(new IWorkingSet[] { fWorkingSet },
                fWorkingSetManager.getRecentWorkingSets()));
    }
    
    public void testRecentWorkingSetsLength() throws Throwable {
        int oldMRULength =  fWorkingSetManager.getRecentWorkingSetsLength();
        try {
	        fWorkingSetManager.setRecentWorkingSetsLength(10);
	        
	        IWorkingSet[] workingSets = new IWorkingSet[10];
	        for (int i = 0 ; i < 10; i++) {
	            IWorkingSet workingSet = fWorkingSetManager.createWorkingSet(
	                    "ws_" + Integer.toString(i + 1), new IAdaptable[] { fWorkspace.getRoot() });
	            fWorkingSetManager.addRecentWorkingSet(workingSet);
	            fWorkingSetManager.addWorkingSet(workingSet);
	            workingSets[9 - i] = workingSet;
	        }
	        assertTrue(ArrayUtil.equals(workingSets, fWorkingSetManager.getRecentWorkingSets()));
	        
	        fWorkingSetManager.setRecentWorkingSetsLength(7);
	        IWorkingSet[] workingSets7 = new IWorkingSet[7];
	        System.arraycopy(workingSets, 0, workingSets7, 0, 7);
	        assertTrue(ArrayUtil.equals(workingSets7, fWorkingSetManager.getRecentWorkingSets()));
	        
	        fWorkingSetManager.setRecentWorkingSetsLength(9);
	        IWorkingSet[] workingSets9 = new IWorkingSet[9];
	        System.arraycopy(workingSets, 0, workingSets9, 2, 7);
	        
	        for (int i = 7 ; i < 9; i++) {
	            IWorkingSet workingSet = fWorkingSetManager.createWorkingSet(
	                    "ws_addded_" + Integer.toString(i + 1), new IAdaptable[] { fWorkspace.getRoot() });
	            fWorkingSetManager.addRecentWorkingSet(workingSet);
	            fWorkingSetManager.addWorkingSet(workingSet);
	            workingSets9[8 - i] = workingSet;
	        }
	        
	        assertTrue(ArrayUtil.equals(workingSets9, fWorkingSetManager.getRecentWorkingSets()));
        } finally {
        	if (oldMRULength > 0)
        		fWorkingSetManager.setRecentWorkingSetsLength(oldMRULength);
        }
    }

    public void testGetWorkingSet() throws Throwable {
        assertNull(fWorkingSetManager.getWorkingSet(WORKING_SET_NAME_1));

        fWorkingSetManager.addWorkingSet(fWorkingSet);
        assertNotNull(fWorkingSetManager.getWorkingSet(fWorkingSet.getName()));

        assertNull(fWorkingSetManager.getWorkingSet(""));

        assertNull(fWorkingSetManager.getWorkingSet(null));
    }

    public void testGetWorkingSets() throws Throwable {
		assertTrue(ArrayUtil.equals(new IWorkingSet[] {}, fWorkingSetManager
				.getWorkingSets()));

		fWorkingSetManager.addWorkingSet(fWorkingSet);
		assertTrue(ArrayUtil.equals(new IWorkingSet[] { fWorkingSet },
				fWorkingSetManager.getWorkingSets()));

		try {
			fWorkingSetManager.addWorkingSet(fWorkingSet);
			fail("Added the same set twice");
		} catch (RuntimeException exception) {
		}
		assertTrue(ArrayUtil.equals(new IWorkingSet[] { fWorkingSet },
				fWorkingSetManager.getWorkingSets()));

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

    public void testRemovePropertyChangeListener() throws Throwable {
        IPropertyChangeListener listener = new TestPropertyChangeListener();

        fWorkingSetManager.removePropertyChangeListener(listener);

        fWorkingSetManager.addPropertyChangeListener(listener);
        fWorkingSetManager.removePropertyChangeListener(listener);

        resetChangeData();
        fWorkingSet.setName(WORKING_SET_NAME_1);
        assertEquals("", fChangeProperty);
    }

    public void testRemoveWorkingSet() throws Throwable {
        fWorkingSetManager.removeWorkingSet(fWorkingSet);
        assertTrue(ArrayUtil.equals(new IWorkingSet[] {}, fWorkingSetManager
                .getWorkingSets()));

        fWorkingSetManager.addWorkingSet(fWorkingSet);
        IWorkingSet workingSet2 = fWorkingSetManager.createWorkingSet(
                WORKING_SET_NAME_2, new IAdaptable[] { fWorkspace.getRoot() });
        fWorkingSetManager.addWorkingSet(workingSet2);
        fWorkingSetManager.removeWorkingSet(fWorkingSet);
        assertTrue(ArrayUtil.equals(new IWorkingSet[] { workingSet2 },
                fWorkingSetManager.getWorkingSets()));
    }
    
    public void testRemoveWorkingSetAfterRename() throws Throwable {
    	/* get workingSetManager */
    	IWorkingSetManager workingSetManager = 
    		fWorkbench.getWorkingSetManager();

    	workingSetManager.addWorkingSet(fWorkingSet);
    	String origName=fWorkingSet.getName();

    	/* check that workingSetManager contains "fWorkingSet"*/
    	assertTrue(ArrayUtil.equals(
    			new IWorkingSet[] {  fWorkingSet },
    			workingSetManager.getWorkingSets()));

    	fWorkingSet.setName(" ");
    	assertEquals(" ", fWorkingSet.getName());

    	/* remove "fWorkingSet" from working set manager */
    	workingSetManager.removeWorkingSet(fWorkingSet);

    	/* check that "fWorkingSet" was removed  after rename*/
    	if(!ArrayUtil.equals(new IWorkingSet[] {},
    			workingSetManager.getWorkingSets())){
    		/*Test Failure, report after restoring state*/
    		fWorkingSet.setName(origName);
    		workingSetManager.removeWorkingSet(fWorkingSet);
    		fail("expected that fWorkingSet has been removed");
    	}

    }
    /**
     * Tests to ensure that a misbehaving listener does not bring down the manager.
     * 
     * @throws Throwable
     */
    public void testListenerSafety() throws Throwable {
		final boolean[] result = new boolean[1];
		// add a bogus listener that dies unexpectedly
		IPropertyChangeListener badListener = new IPropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent event) {
				throw new RuntimeException();

			}
		};
		IPropertyChangeListener goodListener = new IPropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent event) {
				result[0] = true;

			}
		};
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

	/**
	 * @param setIds
	 * @param i
	 * @return
	 */
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
			public Object getAdapter(Class adapter) {
				return null;
			}
			
			@Override
			public String toString() {
				return getId();
			}
		};
			
	}
}
