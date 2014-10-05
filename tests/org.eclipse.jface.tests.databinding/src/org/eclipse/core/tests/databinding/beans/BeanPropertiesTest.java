package org.eclipse.core.tests.databinding.beans;

import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.beans.IBeanObservable;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.tests.internal.databinding.beans.Bean;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;

public class BeanPropertiesTest extends AbstractDefaultRealmTestCase {
	private Bean bean;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		bean = new Bean();
	}

	public void testValue_ValueFactory_ProducesIBeanObservable() {
		IObservable observable = BeanProperties.value(Bean.class, "value")
				.valueFactory().createObservable(bean);
		assertTrue(observable instanceof IBeanObservable);
	}

	public void testSet_SetFactory_ProducesIBeanObservable() {
		IObservable observable = BeanProperties.set(Bean.class, "set")
				.setFactory().createObservable(bean);
		assertTrue(observable instanceof IBeanObservable);
	}

	public void testList_ListFactory_ProducesIBeanObservable() {
		IObservable observable = BeanProperties.list(Bean.class, "list")
				.listFactory().createObservable(bean);
		assertTrue(observable instanceof IBeanObservable);
	}

	public void testMap_MapFactory_ProducesIBeanObservable() {
		IObservable observable = BeanProperties.map(Bean.class, "map")
				.mapFactory().createObservable(bean);
		assertTrue(observable instanceof IBeanObservable);
	}
}
