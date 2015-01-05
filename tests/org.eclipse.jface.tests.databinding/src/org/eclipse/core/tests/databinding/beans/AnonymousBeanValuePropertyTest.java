/*******************************************************************************
 * Copyright (c) 2009 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation
 ******************************************************************************/

package org.eclipse.core.tests.databinding.beans;

import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.databinding.property.value.IValueProperty;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;

/**
 * @since 3.2
 *
 */
public class AnonymousBeanValuePropertyTest extends
		AbstractDefaultRealmTestCase {
	public void testObserveDetailHavingNullValueType_UseExplicitValueType() {
		IObservableValue master = WritableValue.withValueType(null);
		IValueProperty prop = BeanProperties.value("value", String.class);

		IObservableValue detail = prop.observeDetail(master);

		assertEquals(String.class, detail.getValueType());
	}
}
