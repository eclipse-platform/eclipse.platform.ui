/*******************************************************************************
 * Copyright (c) 2009, 2018 Matthew Hall and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 255734)
 ******************************************************************************/

package org.eclipse.core.tests.databinding.observable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.databinding.observable.ChangeSupport;
import org.eclipse.core.databinding.observable.IDisposeListener;
import org.eclipse.core.databinding.observable.IStaleListener;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;
import org.junit.Before;
import org.junit.Test;

/**
 * @since 3.2
 *
 */
public class ChangeSupportTest extends AbstractDefaultRealmTestCase {
	private ChangeSupportStub changeSupport;

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();

		changeSupport = new ChangeSupportStub(Realm.getDefault());
	}

	@Test
	public void testAddDisposeListener_HasListenersFalse() {
		IDisposeListener disposeListener = staleEvent -> {
		};
		IStaleListener staleListener = staleEvent -> {
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
		assertEquals(Arrays.asList(new Object[] { ADD_FIRST, REMOVE_LAST }), changeSupport.log);
	}

	private static final String ADD_FIRST = "firstListenerAdded";
	private static final String REMOVE_LAST = "lastListenerRemoved";

	private static class ChangeSupportStub extends ChangeSupport {
		List<String> log = new ArrayList<String>();

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
