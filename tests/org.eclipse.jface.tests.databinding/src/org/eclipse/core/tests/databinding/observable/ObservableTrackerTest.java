/*******************************************************************************
 * Copyright (c) 2007 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 210115)
 ******************************************************************************/

package org.eclipse.core.tests.databinding.observable;

import org.eclipse.core.databinding.observable.*;
import org.eclipse.core.runtime.AssertionFailedException;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;

public class ObservableTrackerTest extends AbstractDefaultRealmTestCase {
	public void testGetterCalled_ObservableDisposed() throws Exception {
		try {
			IObservable observable = new ObservableStub();
			observable.dispose();

			ObservableTracker.getterCalled(observable);

			fail("expected AssertionFailedException");
		} catch (AssertionFailedException expected) {
		}
	}

	public void testGetterCalled_ObservableRealmNotCurrent() throws Exception {
		try {
			IObservable observable = new ObservableStub(new NotCurrentRealm());

			ObservableTracker.getterCalled(observable);

			fail("expected AssertionFailedException");
		} catch (AssertionFailedException expected) {
		}
	}

	public static class ObservableStub extends AbstractObservable {
		public ObservableStub() {
			this(Realm.getDefault());
		}

		public ObservableStub(Realm realm) {
			super(realm);
		}

		public boolean isStale() {
			return false;
		}
	}

	public static class NotCurrentRealm extends Realm {
		public boolean isCurrent() {
			return false;
		}
	}
}
