/*******************************************************************************
 * Copyright (c) 2009, 2018 Matthew Hall and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 283428)
 ******************************************************************************/

package org.eclipse.jface.tests.databinding.viewers;

import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.HashSet;

import org.eclipse.core.databinding.beans.typed.BeanProperties;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.set.WritableSet;
import org.eclipse.core.databinding.property.list.IListProperty;
import org.eclipse.core.databinding.property.value.IValueProperty;
import org.eclipse.core.databinding.util.ILogger;
import org.eclipse.core.databinding.util.Policy;
import org.eclipse.core.tests.internal.databinding.beans.Bean;
import org.eclipse.jface.databinding.viewers.ViewerSupport;
import org.eclipse.jface.tests.databinding.AbstractSWTTestCase;
import org.eclipse.jface.util.ISafeRunnableRunner;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.AbstractTableViewer;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ViewerSupportTest extends AbstractSWTTestCase {
	private ILogger oldLog;
	private ISafeRunnableRunner oldRunner;

	private AbstractTableViewer structuredViewer;
	private AbstractTreeViewer treeViewer;

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();

		oldLog = Policy.getLog();
		Policy.setLog(status -> {
			assertNotNull("Unexpected status: " + status, status.getException());
			throw new RuntimeException(status.getException());
		});

		oldRunner = SafeRunnable.getRunner();
		SafeRunnable.setRunner(code -> {
			try {
				code.run();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
	}

	@Override
	@After
	public void tearDown() throws Exception {
		if (structuredViewer != null)
			structuredViewer.getControl().dispose();
		if (treeViewer != null)
			treeViewer.getControl().dispose();

		Policy.setLog(oldLog);

		SafeRunnable.setRunner(oldRunner);

		super.tearDown();
	}

	private StructuredViewer getStructuredViewer() {
		if (structuredViewer == null) {
			structuredViewer = new TableViewer(getShell());
		}
		return structuredViewer;
	}

	private AbstractTreeViewer getTreeViewer() {
		if (treeViewer == null)
			treeViewer = new TreeViewer(getShell());
		return treeViewer;
	}

	@Test
	public void testBindList_Twice() {
		StructuredViewer viewer = getStructuredViewer();
		IObservableList<Bean> input0 = WritableList.withElementType(Bean.class);
		IObservableList<Bean> input1 = WritableList.withElementType(Bean.class);
		input0.add(new Bean("element0"));
		input1.add(new Bean("element1"));
		IValueProperty<Bean, String> labelProp = BeanProperties.value(Bean.class, "value");
		ViewerSupport.bind(viewer, input0, labelProp);
		ViewerSupport.bind(viewer, input1, labelProp);
	}

	@Test
	public void testBindSet_Twice() {
		StructuredViewer viewer = getStructuredViewer();
		IObservableSet<Bean> input0 = WritableSet.withElementType(Bean.class);
		IObservableSet<Bean> input1 = WritableSet.withElementType(Bean.class);
		input0.add(new Bean("element0"));
		input1.add(new Bean("element1"));
		IValueProperty<Bean, String> labelProp = BeanProperties.value(Bean.class, "value");
		ViewerSupport.bind(viewer, input0, labelProp);
		ViewerSupport.bind(viewer, input1, labelProp);
	}

	@Test
	public void testBindListTree_Twice() {
		AbstractTreeViewer viewer = getTreeViewer();
		Bean input0 = new Bean(Arrays.asList(new Bean("elem0"), new Bean("elem1"), new Bean("elem2")));
		Bean input1 = new Bean(Arrays.asList(new Bean("elem3"), new Bean("elem4"), new Bean("elem5")));

		// TODO j: It is weird to be forced to cast the values like this
		@SuppressWarnings({ "unchecked", "rawtypes" })
		IValueProperty<Object, String> labelProp = BeanProperties.value((Class) Bean.class, "value");
		@SuppressWarnings({ "unchecked", "rawtypes" })
		IListProperty<Object, Object> childrenProp = BeanProperties.list((Class) Bean.class, "list");
		// TODO j: We could add cast methods to the
//		IValueProperty<Object, Object> labelProp = BeanProperties.value(Bean.class, "value").castSource(Object.class);
//		IListProperty<Object, Object> childrenProp = BeanProperties.list(Bean.class, "list").castSource(Object.class);
		ViewerSupport.bind(viewer, input0, childrenProp, labelProp);
		ViewerSupport.bind(viewer, input1, childrenProp, labelProp);
	}

	@Test
	public void testBindSetTree_Twice() {
		AbstractTreeViewer viewer = getTreeViewer();
		Bean input0 = new Bean(new HashSet<>(Arrays.asList(new Bean("elem0"), new Bean("elem1"), new Bean("elem2"))));
		Bean input1 = new Bean(new HashSet<>(Arrays.asList(new Bean("elem3"), new Bean("elem4"), new Bean("elem5"))));
		// TODO j: It is weird to be forced to cast the values like this
		@SuppressWarnings({ "unchecked", "rawtypes" })
		IValueProperty<Object, String> labelProp = BeanProperties.value((Class) Bean.class, "value");
		@SuppressWarnings({ "unchecked", "rawtypes" })
		IListProperty<Object, Object> childrenProp = BeanProperties.list((Class) Bean.class, "list");
		// TODO j: We could add cast methods to the
//		IValueProperty<Object, Object> labelProp = BeanProperties.value(Bean.class, "value").castSource(Object.class);
//		IListProperty<Object, Object> childrenProp = BeanProperties.list(Bean.class, "list").castSource(Object.class);

		ViewerSupport.bind(viewer, input0, childrenProp, labelProp);
		ViewerSupport.bind(viewer, input1, childrenProp, labelProp);
	}
}
