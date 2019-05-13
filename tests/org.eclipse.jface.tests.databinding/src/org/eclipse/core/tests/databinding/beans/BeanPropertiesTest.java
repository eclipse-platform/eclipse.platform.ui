/*******************************************************************************
 * Copyright (c) 2010, 2016 Matthew Hall and others.
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
 *******************************************************************************/
package org.eclipse.core.tests.databinding.beans;

import static org.junit.Assert.assertTrue;

import org.eclipse.core.databinding.beans.typed.BeanProperties;
import org.eclipse.core.databinding.beans.IBeanObservable;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.tests.internal.databinding.beans.Bean;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;
import org.junit.Before;
import org.junit.Test;

public class BeanPropertiesTest extends AbstractDefaultRealmTestCase {
	private Bean bean;

	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();

		bean = new Bean();
	}

	@Test
	public void testValue_ValueFactory_ProducesIBeanObservable() {
		IObservable observable = BeanProperties.value(Bean.class, "value")
				.valueFactory().createObservable(bean);
		assertTrue(observable instanceof IBeanObservable);
	}

	@Test
	public void testSet_SetFactory_ProducesIBeanObservable() {
		IObservable observable = BeanProperties.set(Bean.class, "set")
				.setFactory().createObservable(bean);
		assertTrue(observable instanceof IBeanObservable);
	}

	@Test
	public void testList_ListFactory_ProducesIBeanObservable() {
		IObservable observable = BeanProperties.list(Bean.class, "list")
				.listFactory().createObservable(bean);
		assertTrue(observable instanceof IBeanObservable);
	}

	@Test
	public void testMap_MapFactory_ProducesIBeanObservable() {
		IObservable observable = BeanProperties.map(Bean.class, "map")
				.mapFactory().createObservable(bean);
		assertTrue(observable instanceof IBeanObservable);
	}
}
