/*******************************************************************************
 * Copyright (c) 2007, 2009 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 210115)
 *     Matthew Hall - bugs 249526, 251424
 ******************************************************************************/

package org.eclipse.core.tests.databinding.observable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.eclipse.core.databinding.observable.AbstractObservable;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.ObservableTracker;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.util.ILogger;
import org.eclipse.core.databinding.util.Policy;
import org.eclipse.core.internal.databinding.IdentitySet;
import org.eclipse.core.runtime.AssertionFailedException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.databinding.conformance.util.CurrentRealm;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;

public class ObservableTrackerTest extends AbstractDefaultRealmTestCase {
	public void testRunAndMonitor_GetterCalled() throws Exception {
		final IObservable observable = new ObservableStub();
		IObservable[] result = ObservableTracker.runAndMonitor(new Runnable() {
			public void run() {
				ObservableTracker.getterCalled(observable);
			}
		}, null, null);
		assertEquals(1, result.length);
		assertSame(observable, result[0]);
	}

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
			IObservable observable = new ObservableStub(new CurrentRealm(false));

			ObservableTracker.getterCalled(observable);

			fail("expected AssertionFailedException");
		} catch (AssertionFailedException expected) {
		}
	}

	public void testRunAndCollect() throws Exception {
		final IObservable[] created = new IObservable[1];
		IObservable[] collected = ObservableTracker
				.runAndCollect(new Runnable() {
					public void run() {
						created[0] = new ObservableStub();
					}
				});
		assertEquals(1, collected.length);
		assertSame(created[0], collected[0]);
	}

	public void testRunAndIgnore_RunAndMonitor() throws Exception {
		final IObservable observable = new ObservableStub();
		IObservable[] result = ObservableTracker.runAndMonitor(new Runnable() {
			public void run() {
				ObservableTracker.runAndIgnore(new Runnable() {
					public void run() {
						ObservableTracker.getterCalled(observable);
					}
				});
			}
		}, null, null);
		assertEquals(0, result.length);
	}

	public void testRunAndIgnore_RunAndCollect() throws Exception {
		IObservable[] result = ObservableTracker.runAndCollect(new Runnable() {
			public void run() {
				ObservableTracker.runAndIgnore(new Runnable() {
					public void run() {
						new ObservableStub();
					}
				});
			}
		});
		assertEquals(0, result.length);
	}

	public void testSetIgnore_RunAndMonitor() throws Exception {
		final IObservable observable = new ObservableStub();
		IObservable[] result = ObservableTracker.runAndMonitor(new Runnable() {
			public void run() {
				ObservableTracker.setIgnore(true);
				ObservableTracker.getterCalled(observable);
				ObservableTracker.setIgnore(false);
			}
		}, null, null);
		assertEquals(0, result.length);
	}

	public void testSetIgnore_RunAndCollect() throws Exception {
		IObservable[] result = ObservableTracker.runAndCollect(new Runnable() {
			public void run() {
				ObservableTracker.setIgnore(true);
				new ObservableStub();
				ObservableTracker.setIgnore(false);
			}
		});
		assertEquals(0, result.length);
	}

	public void testSetIgnore_Nested_RunAndCollect() throws Exception {
		final List list = new ArrayList();

		Set collected = new IdentitySet(Arrays.asList(ObservableTracker
				.runAndCollect(new Runnable() {
					public void run() {
						list.add(new ObservableStub()); // list[0] collected
						ObservableTracker.setIgnore(true);
						list.add(new ObservableStub()); // list[1] ignored
						ObservableTracker.setIgnore(true);
						list.add(new ObservableStub()); // list[2] ignored
						ObservableTracker.setIgnore(false);
						list.add(new ObservableStub()); // list[3] ignored
						ObservableTracker.setIgnore(false);
						list.add(new ObservableStub()); // list[4] collected
					}
				})));

		// Have to compare result in identity set because ObservableTracker may
		// not return them in the same order they were collected
		Set expected = new IdentitySet();
		expected.add(list.get(0));
		expected.add(list.get(4));
		assertEquals(expected, collected);
	}

	public void testSetIgnore_Nested_RunAndMonitor() throws Exception {
		final IObservable[] observables = { new ObservableStub(),
				new ObservableStub(), new ObservableStub(),
				new ObservableStub(), new ObservableStub() };

		Set result = new IdentitySet(Arrays.asList(ObservableTracker
				.runAndMonitor(new Runnable() {
					public void run() {
						ObservableTracker.getterCalled(observables[0]); // monitored
						ObservableTracker.setIgnore(true);
						ObservableTracker.getterCalled(observables[1]); // ignored
						ObservableTracker.setIgnore(true);
						ObservableTracker.getterCalled(observables[2]); // ignored
						ObservableTracker.setIgnore(false);
						ObservableTracker.getterCalled(observables[3]); // ignored
						ObservableTracker.setIgnore(false);
						ObservableTracker.getterCalled(observables[4]); // monitored
					}
				}, null, null)));

		// Have to compare result in identity set because ObservableTracker may
		// not return them in the same order they were monitored
		Set expected = new IdentitySet();
		expected.add(observables[0]);
		expected.add(observables[4]);
		assertEquals(expected, result);
	}

	public void testSetIgnore_RunAndMonitor_UnmatchedIgnore_LogsError() {
		final List log = new ArrayList();
		Policy.setLog(new ILogger() {
			public void log(IStatus status) {
				log.add(status);
			}
		});

		ObservableTracker.runAndMonitor(new Runnable() {
			public void run() {
				ObservableTracker.setIgnore(true);
				// do not call call setIgnore(false)
			}
		}, null, null);

		assertEquals(1, log.size());
		IStatus status = (IStatus) log.get(0);
		assertEquals(IStatus.ERROR, status.getSeverity());
		assertTrue(status.getMessage().indexOf("setIgnore") != -1);
	}

	public void testSetIgnore_RunAndCollect_UnmatchedIgnore_LogsError() {
		final List log = new ArrayList();
		Policy.setLog(new ILogger() {
			public void log(IStatus status) {
				log.add(status);
			}
		});

		ObservableTracker.runAndCollect(new Runnable() {
			public void run() {
				ObservableTracker.setIgnore(true);
				// do not call call setIgnore(false)
			}
		});

		assertEquals(1, log.size());
		IStatus status = (IStatus) log.get(0);
		assertEquals(IStatus.ERROR, status.getSeverity());
		assertTrue(status.getMessage().indexOf("setIgnore") != -1);
	}

	public void testSetIgnore_UnmatchedUnignore() {
		try {
			ObservableTracker.setIgnore(false);
			fail("Expected IllegalStateException");
		} catch (IllegalStateException expected) {
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
}
