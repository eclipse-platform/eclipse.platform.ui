/*******************************************************************************
 * Copyright (c) 2006, 2007 Brad Reynolds and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brad Reynolds - initial API and implementation
 *     Matthew Hall - bug 349038
 ******************************************************************************/

package org.eclipse.core.tests.databinding.observable.map;

import java.util.Set;

import junit.framework.TestCase;

import org.eclipse.core.databinding.observable.ChangeEvent;
import org.eclipse.core.databinding.observable.DisposeEvent;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.IDisposeListener;
import org.eclipse.core.databinding.observable.IStaleListener;
import org.eclipse.core.databinding.observable.StaleEvent;
import org.eclipse.core.databinding.observable.map.AbstractObservableMap;
import org.eclipse.core.databinding.observable.map.IMapChangeListener;
import org.eclipse.core.databinding.observable.map.MapChangeEvent;
import org.eclipse.core.databinding.observable.map.MapDiff;
import org.eclipse.jface.databinding.conformance.util.CurrentRealm;
import org.eclipse.jface.databinding.conformance.util.RealmTester;

/**
 * @since 3.2
 */
public class AbstractObservableMapTest extends TestCase {
	private AbstractObservableMapStub map;

	@Override
	protected void setUp() throws Exception {
		RealmTester.setDefault(new CurrentRealm(true));
		map = new AbstractObservableMapStub();
	}
	
	@Override
	protected void tearDown() throws Exception {
		RealmTester.setDefault(null);
	}
	
	public void testIsStaleRealmChecks() throws Exception {
		RealmTester.exerciseCurrent(new Runnable() {
			@Override
			public void run() {
				map.isStale();
			}			
		});
	}
	
	public void testSetStaleRealmChecks() throws Exception {
		RealmTester.exerciseCurrent(new Runnable() {
			@Override
			public void run() {
				map.setStale(true);
			}
		});
	}
	
	public void testFireStaleRealmChecks() throws Exception {
		RealmTester.exerciseCurrent(new Runnable() { 
			@Override
			public void run() {
				map.fireStale();
			}
		});
	}
	
	public void testFireChangeRealmChecks() throws Exception {
		RealmTester.exerciseCurrent(new Runnable() {
			@Override
			public void run() {
				map.fireChange();
			}
		});
	}
	
	public void testFireMapChangeRealmChecks() throws Exception {
		RealmTester.exerciseCurrent(new Runnable() {
			@Override
			public void run() {
				map.fireMapChange(null);
			}
		});
	}

	public void testAddListChangeListener_AfterDispose() {
		map.dispose();
		map.addMapChangeListener(new IMapChangeListener() {
			@Override
			public void handleMapChange(MapChangeEvent event) {
				// do nothing
			}
		});
	}

	public void testRemoveListChangeListener_AfterDispose() {
		map.dispose();
		map.removeMapChangeListener(new IMapChangeListener() {
			@Override
			public void handleMapChange(MapChangeEvent event) {
				// do nothing
			}
		});
	}

	public void testAddChangeListener_AfterDispose() {
		map.dispose();
		map.addChangeListener(new IChangeListener() {
			@Override
			public void handleChange(ChangeEvent event) {
				// do nothing
			}
		});
	}

	public void testRemoveChangeListener_AfterDispose() {
		map.dispose();
		map.removeChangeListener(new IChangeListener() {
			@Override
			public void handleChange(ChangeEvent event) {
				// do nothing
			}
		});
	}

	public void testAddStaleListener_AfterDispose() {
		map.dispose();
		map.addStaleListener(new IStaleListener() {
			@Override
			public void handleStale(StaleEvent staleEvent) {
				// do nothing
			}
		});
	}

	public void testRemoveStaleListener_AfterDispose() {
		map.dispose();
		map.removeStaleListener(new IStaleListener() {
			@Override
			public void handleStale(StaleEvent staleEvent) {
				// do nothing
			}
		});
	}

	public void testAddDisposeListener_AfterDispose() {
		map.dispose();
		map.addDisposeListener(new IDisposeListener() {
			@Override
			public void handleDispose(DisposeEvent event) {
				// do nothing
			}
		});
	}

	public void testRemoveDisposeListener_AfterDispose() {
		map.dispose();
		map.removeDisposeListener(new IDisposeListener() {
			@Override
			public void handleDispose(DisposeEvent event) {
				// do nothing
			}
		});
	}

	public void testHasListeners_AfterDispose() {
		map.dispose();
		map.hasListeners();
	}

	static class AbstractObservableMapStub extends AbstractObservableMap {
		@Override
		public Set entrySet() {
			return null;
		}
		
		@Override
		protected void fireChange() {
			super.fireChange();
		}
		
		@Override
		protected void fireMapChange(MapDiff diff) {
			super.fireMapChange(diff);
		}
		
		@Override
		protected void fireStale() {
			super.fireStale();
		}

		@Override
		protected synchronized boolean hasListeners() {
			return super.hasListeners();
		}
	}
}
