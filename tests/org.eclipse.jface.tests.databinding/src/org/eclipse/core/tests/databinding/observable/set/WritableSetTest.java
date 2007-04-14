/*******************************************************************************
 * Copyright (c) 2007 Brad Reynolds and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brad Reynolds - initial API and implementation
 ******************************************************************************/
package org.eclipse.core.tests.databinding.observable.set;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.set.WritableSet;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;

/**
 * @since 3.3
 */
public class WritableSetTest extends AbstractDefaultRealmTestCase {
	public void testWithElementType() throws Exception {
		Object elementType = String.class;
		WritableSet set = WritableSet.withElementType(elementType);
		assertNotNull(set);
		assertEquals(Realm.getDefault(), set.getRealm());
		assertEquals(elementType, set.getElementType());
	}
}
