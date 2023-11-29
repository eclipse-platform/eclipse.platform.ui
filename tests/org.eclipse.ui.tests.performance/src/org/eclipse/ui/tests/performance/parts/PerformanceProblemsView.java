/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
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
 ******************************************************************************/

package org.eclipse.ui.tests.performance.parts;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.internal.views.markers.ExtendedMarkersView;
import org.eclipse.ui.internal.views.markers.ProblemsView;

/**
 * PerformanceProblemsView is a problems view created
 * for the performance tests.
 * @since 3.2
 */
public class PerformanceProblemsView extends ProblemsView {

	public Tree getTreeWidget(){
		TreeViewer viewer;
		try {
			Method m = ExtendedMarkersView.class.getDeclaredMethod("getViewer");
			m.setAccessible(true);
			viewer = (TreeViewer) m.invoke(this);
			return viewer.getTree();
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
		return null;
	}
}
