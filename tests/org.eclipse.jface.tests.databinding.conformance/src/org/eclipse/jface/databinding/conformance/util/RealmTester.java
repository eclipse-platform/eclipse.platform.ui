/*******************************************************************************
 * Copyright (c) 2006, 2014 Brad Reynolds and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brad Reynolds - initial API and implementation
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 444829
 ******************************************************************************/

package org.eclipse.jface.databinding.conformance.util;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.runtime.AssertionFailedException;
import org.junit.Assert;

/**
 * Aids in the testing of Realms.
 *
 * @since 3.2
 */
public class RealmTester {

	/**
	 * Sets the default realm without using Realm.runWithDefault() for testing
	 * purposes.
	 *
	 * @param realm
	 */
	public static void setDefault(Realm realm) {
		CurrentRealm.setDefault(realm);
	}

	/**
	 * Runs the provided <code>runnable</code> when the realm is both current
	 * and not current. It checks for AssertionFailedExceptions and if an
	 * exception occurs or doesn't occur as expected the test fails. The realm
	 * of an observable created before the method was invoked must be of type
	 * {@link CurrentRealm}. The default realm during the runnable invocation is
	 * set to an instance of {@link CurrentRealm} when the runnable is invoked.
	 *
	 * @param runnable
	 */
	public static void exerciseCurrent(Runnable runnable) {
		CurrentRealm previousRealm = (CurrentRealm) Realm.getDefault();
		CurrentRealm realm = new CurrentRealm();
		setDefault(realm);

		try {
			realm.setCurrent(true);
			if (previousRealm != null) {
				previousRealm.setCurrent(true);
			}

			try {
				runnable.run();
			} catch (AssertionFailedException e) {
				Assert.fail("Correct realm, exception should not have been thrown");
			}

			realm.setCurrent(false);
			if (previousRealm != null) {
				previousRealm.setCurrent(false);
			}

			try {
				runnable.run();
				Assert.fail("Incorrect realm, exception should have been thrown");
			} catch (AssertionFailedException e) {
			}
		} finally {
			setDefault(previousRealm);
		}
	}

	/**
	 * Runs the provided <code>runnable</code> when the realm is both current
	 * and not current. It checks for AssertionFailedExceptions and if an
	 * exception occurs or doesn't occur as expected the test fails.
	 *
	 * @param runnable
	 * @param realm
	 */
	public static void exerciseCurrent(Runnable runnable, CurrentRealm realm) {
		realm.setCurrent(true);

		try {
			runnable.run();
		} catch (AssertionFailedException e) {
			Assert.fail("Correct realm, exception should not have been thrown");
		}

		realm.setCurrent(false);

		try {
			runnable.run();
			Assert.fail("Incorrect realm, exception should have been thrown");
		} catch (AssertionFailedException e) {
		}
	}
}
