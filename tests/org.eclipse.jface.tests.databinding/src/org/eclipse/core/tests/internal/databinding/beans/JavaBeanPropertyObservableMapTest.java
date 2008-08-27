/*******************************************************************************
 * Copyright (c) 2008 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 240931)
 ******************************************************************************/

package org.eclipse.core.tests.internal.databinding.beans;

import java.beans.PropertyDescriptor;
import java.util.HashMap;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.map.IMapChangeListener;
import org.eclipse.core.databinding.observable.map.MapChangeEvent;
import org.eclipse.core.internal.databinding.beans.JavaBeanPropertyObservableMap;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;

/**
 * @since 3.2
 * 
 */
public class JavaBeanPropertyObservableMapTest extends
		AbstractDefaultRealmTestCase {
	private JavaBeanPropertyObservableMap map;

	private PropertyDescriptor propertyDescriptor;

	private Bean bean;

	private String propertyName;

	protected void setUp() throws Exception {
		super.setUp();

		propertyName = "map";
		propertyDescriptor = new PropertyDescriptor(propertyName, Bean.class);
		bean = new Bean(new HashMap());

		map = new JavaBeanPropertyObservableMap(Realm.getDefault(), bean,
				propertyDescriptor);
	}

	public void testFirstListenerAdded_AfterLastListenerRemoved()
			throws Exception {
		IMapChangeListener listener = new IMapChangeListener() {
			public void handleMapChange(MapChangeEvent event) {
				// noop
			}
		};
		map.addMapChangeListener(listener);
		map.removeMapChangeListener(listener);
		map.addMapChangeListener(listener);
	}

	public void testDispose_DoubleInvocation() throws Exception {
		map.dispose();
		map.dispose();
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(JavaBeanPropertyObservableMapTest.class
				.getName());
		suite.addTestSuite(JavaBeanPropertyObservableMapTest.class);
		return suite;
	}
}
