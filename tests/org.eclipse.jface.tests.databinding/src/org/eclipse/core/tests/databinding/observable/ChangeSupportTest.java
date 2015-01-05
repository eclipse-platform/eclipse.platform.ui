/*******************************************************************************
 * Copyright (c) 2009 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 255734)
 ******************************************************************************/

package org.eclipse.core.tests.databinding.observable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.databinding.observable.ChangeSupport;
import org.eclipse.core.databinding.observable.DisposeEvent;
import org.eclipse.core.databinding.observable.IDisposeListener;
import org.eclipse.core.databinding.observable.IStaleListener;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.StaleEvent;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;

/**
 * @since 3.2
 *
 */
public class ChangeSupportTest extends AbstractDefaultRealmTestCase {
	private ChangeSupportStub changeSupport;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		changeSupport = new ChangeSupportStub(Realm.getDefault());
	}

	public void testAddDisposeListener_HasListenersFalse() {
		IDisposeListener disposeListener = new IDisposeListener() {
			@Override
			public void handleDispose(DisposeEvent staleEvent) {
			}
		};
		IStaleListener staleListener = new IStaleListener() {
			@Override
			public void handleStale(StaleEvent staleEvent) {
			}
		};

		assertFalse(changeSupport.hasListeners());

		changeSupport.addDisposeListener(disposeListener);
		assertFalse(changeSupport.hasListeners());
		assertEquals(Collections.EMPTY_LIST, changeSupport.log);

		changeSupport.addStaleListener(staleListener);
		assertTrue(changeSupport.hasListeners());
		assertEquals(Collections.singletonList(ADD_FIRST), changeSupport.log);

		changeSupport.removeDisposeListener(disposeListener);
		assertTrue(changeSupport.hasListeners());
		assertEquals(Collections.singletonList(ADD_FIRST), changeSupport.log);

		changeSupport.removeStaleListener(staleListener);
		assertFalse(changeSupport.hasListeners());
		assertEquals(Arrays.asList(new Object[] { ADD_FIRST, REMOVE_LAST }),
				changeSupport.log);
	}

	private static final String ADD_FIRST = "firstListenerAdded";
	private static final String REMOVE_LAST = "lastListenerRemoved";

	private static class ChangeSupportStub extends ChangeSupport {
		List log = new ArrayList();

		ChangeSupportStub(Realm realm) {
			super(realm);
		}

		@Override
		protected void firstListenerAdded() {
			log.add(ADD_FIRST);
		}

		@Override
		protected void lastListenerRemoved() {
			log.add(REMOVE_LAST);
		}

		@Override
		protected boolean hasListeners() {
			return super.hasListeners();
		}
	}
}