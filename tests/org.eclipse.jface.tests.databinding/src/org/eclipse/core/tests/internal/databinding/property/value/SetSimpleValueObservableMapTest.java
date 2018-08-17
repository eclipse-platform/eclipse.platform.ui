/*******************************************************************************
 * Copyright (c) 2010 Ovidio Mallo and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Ovidio Mallo - initial API and implementation (bug 299619)
 ******************************************************************************/

package org.eclipse.core.tests.internal.databinding.property.value;

import static org.junit.Assert.assertEquals;

import org.eclipse.core.databinding.observable.set.WritableSet;
import org.eclipse.core.internal.databinding.property.value.SelfValueProperty;
import org.eclipse.core.internal.databinding.property.value.SetSimpleValueObservableMap;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;
import org.junit.Test;

public class SetSimpleValueObservableMapTest extends
		AbstractDefaultRealmTestCase {

	@Test
	public void testGetKeyValueType() {
		WritableSet masterSet = WritableSet.withElementType(String.class);
		SelfValueProperty detailProperty = new SelfValueProperty(Object.class);

		SetSimpleValueObservableMap detailMap = new SetSimpleValueObservableMap(
				masterSet, detailProperty);

		assertEquals(masterSet.getElementType(), detailMap.getKeyType());
		assertEquals(detailProperty.getValueType(), detailMap.getValueType());
	}
}
