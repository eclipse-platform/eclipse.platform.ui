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

import org.eclipse.core.databinding.observable.AbstractObservable;
import org.eclipse.core.databinding.observable.DecoratingObservable;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.jface.databinding.conformance.util.DisposeEventTracker;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;

/**
 * @since 3.2
 * 
 */
public class DecoratingObservableTest extends AbstractDefaultRealmTestCase {
	private IObservable decorated;
	private DecoratingObservable decorator;

	protected void setUp() throws Exception {
		super.setUp();
		decorated = new ObservableStub(Realm.getDefault());
		decorator = new DecoratingObservable(decorated, false);
	}

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

		public boolean isStale() {
			return false;
		}
	}
}
