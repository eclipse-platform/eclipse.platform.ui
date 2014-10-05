/*******************************************************************************
 * Copyright (c) 2009 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 283428)
 ******************************************************************************/

package org.eclipse.jface.tests.databinding.viewers;

import java.util.Arrays;
import java.util.HashSet;

import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.set.WritableSet;
import org.eclipse.core.databinding.property.list.IListProperty;
import org.eclipse.core.databinding.property.set.ISetProperty;
import org.eclipse.core.databinding.property.value.IValueProperty;
import org.eclipse.core.databinding.util.ILogger;
import org.eclipse.core.databinding.util.Policy;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
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

public class ViewerSupportTest extends AbstractSWTTestCase {
	private ILogger oldLog;
	private ISafeRunnableRunner oldRunner;

	private AbstractTableViewer structuredViewer;
	private AbstractTreeViewer treeViewer;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		oldLog = Policy.getLog();
		Policy.setLog(new ILogger() {
			@Override
			public void log(IStatus status) {
				if (status.getException() != null)
					throw new RuntimeException(status.getException());
				fail("Unexpected status: " + status);
			}
		});

		oldRunner = SafeRunnable.getRunner();
		SafeRunnable.setRunner(new ISafeRunnableRunner() {
			@Override
			public void run(ISafeRunnable code) {
				try {
					code.run();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		});
	}

	@Override
	protected void tearDown() throws Exception {
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

	public void testBindList_Twice() {
		StructuredViewer viewer = getStructuredViewer();
		IObservableList input0 = WritableList.withElementType(Bean.class);
		IObservableList input1 = WritableList.withElementType(Bean.class);
		input0.add(new Bean("element0"));
		input1.add(new Bean("element1"));
		IValueProperty labelProp = BeanProperties.value(Bean.class, "value");
		ViewerSupport.bind(viewer, input0, labelProp);
		ViewerSupport.bind(viewer, input1, labelProp);
	}

	public void testBindSet_Twice() {
		StructuredViewer viewer = getStructuredViewer();
		IObservableSet input0 = WritableSet.withElementType(Bean.class);
		IObservableSet input1 = WritableSet.withElementType(Bean.class);
		input0.add(new Bean("element0"));
		input1.add(new Bean("element1"));
		IValueProperty labelProp = BeanProperties.value(Bean.class, "value");
		ViewerSupport.bind(viewer, input0, labelProp);
		ViewerSupport.bind(viewer, input1, labelProp);
	}

	public void testBindListTree_Twice() {
		AbstractTreeViewer viewer = getTreeViewer();
		Bean input0 = new Bean(Arrays.asList(new Bean[] { new Bean("elem0"),
				new Bean("elem1"), new Bean("elem2") }));
		Bean input1 = new Bean(Arrays.asList(new Bean[] { new Bean("elem3"),
				new Bean("elem4"), new Bean("elem5") }));
		IValueProperty labelProp = BeanProperties.value(Bean.class, "value");
		IListProperty childrenProp = BeanProperties.list(Bean.class, "list");
		ViewerSupport.bind(viewer, input0, childrenProp, labelProp);
		ViewerSupport.bind(viewer, input1, childrenProp, labelProp);
	}

	public void testBindSetTree_Twice() {
		AbstractTreeViewer viewer = getTreeViewer();
		Bean input0 = new Bean(new HashSet(Arrays.asList(new Bean[] {
				new Bean("elem0"), new Bean("elem1"), new Bean("elem2") })));
		Bean input1 = new Bean(new HashSet(Arrays.asList(new Bean[] {
				new Bean("elem3"), new Bean("elem4"), new Bean("elem5") })));
		IValueProperty labelProp = BeanProperties.value(Bean.class, "value");
		ISetProperty childrenProp = BeanProperties.set(Bean.class, "set");
		ViewerSupport.bind(viewer, input0, childrenProp, labelProp);
		ViewerSupport.bind(viewer, input1, childrenProp, labelProp);
	}
}
