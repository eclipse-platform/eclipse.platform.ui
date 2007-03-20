/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brad Reynolds - bug 164653, 147515
 *******************************************************************************/

package org.eclipse.jface.tests.databinding.observable.list;

import java.util.Arrays;
import java.util.Collections;

import junit.framework.TestCase;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.IListChangeListener;
import org.eclipse.core.databinding.observable.list.ListChangeEvent;
import org.eclipse.core.databinding.observable.list.ListDiffEntry;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.tests.databinding.observable.ThreadRealm;
import org.eclipse.jface.tests.databinding.util.RealmTester;
import org.eclipse.jface.tests.databinding.util.RealmTester.CurrentRealm;
import org.eclipse.swt.widgets.Display;

/**
 * @since 3.2
 */
public class WritableListTest extends TestCase {
	protected void tearDown() throws Exception {
		RealmTester.setDefault(null);
	}

	public void testClear() {
		ThreadRealm realm = new ThreadRealm();
		realm.init(Thread.currentThread());

		WritableList writableList = new WritableList(realm);
		writableList.add("hello");
		writableList.add("world");
		assertEquals(2, writableList.size());
		writableList.clear();
		assertEquals(0, writableList.size());
	}
    
    public void testRemoveAllChangeEvent() throws Exception {
        CurrentRealm realm = new CurrentRealm(true);
        
        WritableList list = new WritableList(realm);
        String element = "element";
        
        list.add(element);
        
        class ListChangeListener implements IListChangeListener {
            int count;
            ListChangeEvent event;
            
            /* (non-Javadoc)
             * @see org.eclipse.core.databinding.observable.list.IListChangeListener#handleListChange(org.eclipse.core.databinding.observable.list.ListChangeEvent)
             */
            public void handleListChange(ListChangeEvent event) {
                count++;
                this.event = event;
            }
        }
        
        ListChangeListener listener = new ListChangeListener();
        list.addListChangeListener(listener);
        assertEquals(0, listener.count);
        
        list.removeAll(Arrays.asList(new String[] {element}));
        assertEquals(1, listener.count);
        
        assertEquals(1, listener.event.diff.getDifferences().length);
        ListDiffEntry diffEntry = listener.event.diff.getDifferences()[0];
        assertFalse("addition", diffEntry.isAddition());
        assertEquals("element", element, diffEntry.getElement());
        assertEquals("position", 0, diffEntry.getPosition());
        assertFalse(list.contains(element));
    }

	public void testSetRealmChecks() throws Exception {
		RealmTester.exerciseCurrent(new Runnable() {
			public void run() {
				WritableList list = new WritableList();
				list.add("");
				list.set(0, "");
			}
		});
	}

	public void testAddRealmChecks() throws Exception {
		RealmTester.exerciseCurrent(new Runnable() {
			public void run() {
				WritableList list = new WritableList();
				list.add("");
			}
		});
	}

	public void testAddByIndexRealmChecks() throws Exception {
		RealmTester.exerciseCurrent(new Runnable() {
			public void run() {
				WritableList list = new WritableList();
				list.add(0, "");
			}
		});
	}

	public void testAddAllRealmChecks() throws Exception {
		RealmTester.exerciseCurrent(new Runnable() {
			public void run() {
				WritableList list = new WritableList();
				list.addAll(Collections.EMPTY_LIST);
			}
		});
	}

	public void testAddAllByIndexRealmChecks() throws Exception {
		RealmTester.exerciseCurrent(new Runnable() {
			public void run() {
				WritableList list = new WritableList();
				list.addAll(0, Collections.EMPTY_LIST);
			}
		});
	}

	public void testRemoveRealmChecks() throws Exception {
		RealmTester.setDefault(new CurrentRealm(true));
		final WritableList list = new WritableList();
		list.add("");
		list.add("");

		RealmTester.exerciseCurrent(new Runnable() {
			public void run() {
				list.remove("");
			}
		});
		RealmTester.setDefault(null);
	}

	public void testRemoveByIndexRealmChecks() throws Exception {
		RealmTester.setDefault(new CurrentRealm(true));
		final WritableList list = new WritableList();
		list.add("");
		list.add("");

		RealmTester.exerciseCurrent(new Runnable() {
			public void run() {
				list.remove(list.size() - 1);
			}
		});

		RealmTester.setDefault(null);
	}

	public void testRemoveAllRealmChecks() throws Exception {
		RealmTester.exerciseCurrent(new Runnable() {
			public void run() {
				WritableList list = new WritableList();
				list.removeAll(Collections.EMPTY_LIST);
			}
		});
	}

	public void testRetainAllRealmChecks() throws Exception {
		RealmTester.exerciseCurrent(new Runnable() {
			public void run() {
				WritableList list = new WritableList();
				list.retainAll(Collections.EMPTY_LIST);
			}
		});
	}

	public void testClearRealmChecks() throws Exception {
		RealmTester.exerciseCurrent(new Runnable() {
			public void run() {
				WritableList list = new WritableList();
				list.clear();
			}
		});
	}

	public void testNullElementType() throws Exception {
		RealmTester.setDefault(SWTObservables.getRealm(Display.getDefault()));
		WritableList writableList = new WritableList();
		assertNull(writableList.getElementType());

		writableList = new WritableList(Realm.getDefault());
		assertNull(writableList.getElementType());
	}

	public void testWithElementType() throws Exception {
		RealmTester.setDefault(SWTObservables.getRealm(Display.getDefault()));

		Object elementType = String.class;
		WritableList list = WritableList.withElementType(elementType);
		assertNotNull(list);
		assertEquals(Realm.getDefault(), list.getRealm());
		assertEquals(elementType, list.getElementType());
	}
}
