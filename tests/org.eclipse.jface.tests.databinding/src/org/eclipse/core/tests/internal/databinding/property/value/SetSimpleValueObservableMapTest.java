/*******************************************************************************
 * Copyright (c) 2010 Ovidio Mallo and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ovidio Mallo - initial API and implementation (bug 299619)
 ******************************************************************************/

package org.eclipse.core.tests.internal.databinding.property.value;

import org.eclipse.core.databinding.observable.set.WritableSet;
import org.eclipse.core.internal.databinding.property.value.SelfValueProperty;
import org.eclipse.core.internal.databinding.property.value.SetSimpleValueObservableMap;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;

public class SetSimpleValueObservableMapTest extends
		AbstractDefaultRealmTestCase {

	public void testGetKeyValueType() {
		WritableSet masterSet = WritableSet.withElementType(String.class);
		SelfValueProperty detailProperty = new SelfValueProperty(Object.class);

		SetSimpleValueObservableMap detailMap = new SetSimpleValueObservableMap(
				masterSet, detailProperty);

		assertEquals(masterSet.getElementType(), detailMap.getKeyType());
		assertEquals(detailProperty.getValueType(), detailMap.getValueType());
	}
}
