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
 *     Matthew Hall - initial API and implementation
 ******************************************************************************/

package org.eclipse.core.tests.databinding.beans;

import static org.junit.Assert.assertEquals;

import org.eclipse.core.databinding.beans.typed.PojoProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.databinding.property.value.IValueProperty;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;
import org.junit.Test;

/**
 * @since 3.2
 *
 */
public class AnonymousPojoValuePropertyTest extends
		AbstractDefaultRealmTestCase {
	@Test
	public void testObserveDetailHavingNullValueType_UseExplicitValueType() {
		IObservableValue<Object> master = WritableValue.withValueType(null);
		IValueProperty<Object, String> prop = PojoProperties.value("value", String.class);

		IObservableValue<String> detail = prop.observeDetail(master);

		assertEquals(String.class, detail.getValueType());
	}
}
