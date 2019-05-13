/*******************************************************************************
 * Copyright (c) 2009 Matthew Hall and others.
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

import org.eclipse.core.databinding.observable.AbstractObservable;
import org.eclipse.core.databinding.observable.DecoratingObservable;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.jface.databinding.conformance.util.DisposeEventTracker;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;
import org.junit.Before;
import org.junit.Test;

/**
 * @since 3.2
 *
 */
public class DecoratingObservableTest extends AbstractDefaultRealmTestCase {
	private IObservable decorated;
	private DecoratingObservable decorator;

	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();
		decorated = new ObservableStub(Realm.getDefault());
		decorator = new DecoratingObservable(decorated, false);
	}

	@Test
	public void testDisposeDecorated_DisposesDecorator() {
		DisposeEventTracker tracker = DisposeEventTracker.observe(decorator);
		assertFalse(decorator.isDisposed());
		decorated.dispose();
		assertEquals(1, tracker.count);
		assertTrue(decorator.isDisposed());
	}

	static class ObservableStub extends AbstractObservable {
		public ObservableStub(Realm realm) {
			super(realm);
		}

		@Override
		public boolean isStale() {
			return false;
		}
	}
}
