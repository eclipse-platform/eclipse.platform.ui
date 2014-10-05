/*******************************************************************************
 * Copyright (c) 2006, 2007 Brad Reynolds and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brad Reynolds - initial API and implementation
 ******************************************************************************/

package org.eclipse.core.tests.databinding.observable;

import junit.framework.TestCase;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.jface.databinding.conformance.util.CurrentRealm;
import org.eclipse.jface.databinding.conformance.util.RealmTester;

/**
 * @since 3.2
 */
public class RealmTest extends TestCase {
	public void testSetDefaultWithRunnable() throws Exception {
		Realm oldRealm = new CurrentRealm(true);
		final Realm newRealm = new CurrentRealm(true);
		
		RealmTester.setDefault(oldRealm);
		Realm.runWithDefault(newRealm, new Runnable() {
			@Override
			public void run() {
				assertEquals("new realm should be default", newRealm, Realm.getDefault());
			}
		});
		
		assertEquals("old realm should have been restored", oldRealm, Realm.getDefault());
	}
}
