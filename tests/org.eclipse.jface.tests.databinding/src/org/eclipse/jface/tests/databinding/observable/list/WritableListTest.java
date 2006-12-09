/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brad Reynolds - bug 164653
 ******************************************************************************/

package org.eclipse.jface.tests.databinding.observable.list;

import java.util.Collections;

import junit.framework.TestCase;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.jface.tests.databinding.observable.ThreadRealm;
import org.eclipse.jface.tests.databinding.util.RealmTester;
import org.eclipse.jface.tests.databinding.util.RealmTester.CurrentRealm;

/**
 * @since 3.2
 */
public class WritableListTest extends TestCase {
	protected void tearDown() throws Exception {
		Realm.setDefault(null);
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
		Realm.setDefault(new CurrentRealm(true));
		final WritableList list = new WritableList();
		list.add("");
		list.add("");
		
		RealmTester.exerciseCurrent(new Runnable() {
			public void run() {
				list.remove("");
			}			
		});
		Realm.setDefault(null);
	}
	
	public void testRemoveByIndexRealmChecks() throws Exception {
		Realm.setDefault(new CurrentRealm(true));
		final WritableList list = new WritableList();
		list.add("");
		list.add("");
		
		RealmTester.exerciseCurrent(new Runnable() {
			public void run() {
				list.remove(list.size() - 1);
			}			
		});
		
		Realm.setDefault(null);
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
}
