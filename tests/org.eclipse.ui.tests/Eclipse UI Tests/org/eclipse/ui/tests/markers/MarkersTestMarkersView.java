/*******************************************************************************
 * Copyright (c) 2007, 2018 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.tests.markers;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.internal.views.markers.ExtendedMarkersView;
import org.eclipse.ui.views.markers.MarkerSupportView;
import org.eclipse.ui.views.markers.internal.MarkerSupportRegistry;

/**
 * MarkersTestMarkersView is the test suite version of the markers view.
 *
 * @since 3.4
 */
public class MarkersTestMarkersView extends MarkerSupportView {

	private Tree tree;

	/**
	 * Create a new instance of the receiver.
	 */
	public MarkersTestMarkersView() {
		super(MarkerSupportRegistry.PROBLEMS_GENERATOR);
	}

	/**
	 * Get the current markers for the receiver.
	 */
	public IMarker[] getCurrentMarkers() {
		Method method;
		try {
			method = ExtendedMarkersView.class.getDeclaredMethod("getAllMarkers");
			method.setAccessible(true);
		} catch (SecurityException | NoSuchMethodException e) {
			e.printStackTrace();
			return new IMarker[0];
		}
		try {
			return (IMarker[]) method.invoke(this);
		} catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
		}
		return new IMarker[0];
	}

	/**
	 * Add a listener for the end of the update.
	 */
	public void addUpdateFinishListener(IJobChangeListener listener) {
		getUpdateJobForListener().addJobChangeListener(listener);

	}

	/**
	 * Return the updateJob.
	 */
	private Job getUpdateJobForListener() {
		Field field;
		try {
			field = ExtendedMarkersView.class.getDeclaredField("updateJob");
			field.setAccessible(true);
			return (Job) field.get(this);
		} catch (IllegalAccessException | NoSuchFieldException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Remove a listener for the end of the update.
	 */
	public void removeUpdateFinishListener(IJobChangeListener listener) {
		getUpdateJobForListener().addJobChangeListener(listener);

	}

	/**
	 * Set the width of all of the columns.
	 */
	public void setColumnWidths(int width) {
		TreeColumn[] treeColumns = tree.getColumns();
		for (TreeColumn treeColumn : treeColumns) {
			treeColumn.setWidth(width);
		}

	}

	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		tree = getTreeWidget();
	}

	public boolean checkColumnSizes(int size) {
		TreeColumn[] treeColumns = tree.getColumns();

		//Do not check the last column as Linux will resize it to fit the whole table
		for (int j = 0; j < treeColumns.length - 1; j++) {
			if(treeColumns[j].getWidth() == size) {
				continue;
			}
			return false;
		}
		return true;
	}

	private Tree getTreeWidget() {
		TreeViewer viewer;
		try {
			Method m = ExtendedMarkersView.class.getDeclaredMethod("getViewer");
			m.setAccessible(true);
			viewer = (TreeViewer) m.invoke(this);
			return viewer.getTree();
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			e.printStackTrace();
		}
		return null;
	}
}
