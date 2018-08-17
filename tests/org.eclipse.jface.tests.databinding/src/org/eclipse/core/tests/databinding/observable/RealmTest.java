/*******************************************************************************
 * Copyright (c) 2006, 2018 Brad Reynolds and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Brad Reynolds - initial API and implementation
 ******************************************************************************/

package org.eclipse.core.tests.databinding.observable;

import static org.junit.Assert.assertEquals;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.jface.databinding.conformance.util.CurrentRealm;
import org.eclipse.jface.databinding.conformance.util.RealmTester;
import org.junit.Test;

/**
 * @since 3.2
 */
public class RealmTest {
	@Test
	public void testSetDefaultWithRunnable() throws Exception {
		Realm oldRealm = new CurrentRealm(true);
		final Realm newRealm = new CurrentRealm(true);

		RealmTester.setDefault(oldRealm);
		Realm.runWithDefault(newRealm, () -> assertEquals("new realm should be default", newRealm, Realm.getDefault()));

		assertEquals("old realm should have been restored", oldRealm, Realm.getDefault());
	}
}
