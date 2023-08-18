/*******************************************************************************
 * Copyright (c) 2023 Advantest Europe GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 				Raghunandana Murthappa
 *******************************************************************************/

package org.eclipse.jface.tests.viewers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.widgets.Composite;

public class BaseLimitBasedViewerTest extends ViewerTestCase {

	List<DataModel> rootModel;
	protected static final int VIEWER_LIMIT = 4;
	protected static final int DEFAULT_ELEMENTS_COUNT = 40;

	public BaseLimitBasedViewerTest(String name) {
		super(name);
	}

	@Override
	protected StructuredViewer createViewer(Composite parent) {
		return null;
	}

	protected static List<DataModel> createModel(final int maxCount) {
		List<DataModel> rootModel = new ArrayList<>();
		for (int i = 0; i < maxCount; i++) {
			if (i % 2 == 0) {
				DataModel rootLevel = new DataModel(Integer.valueOf(i));
				for (int j = 0; j < maxCount; j++) {
					if (j % 2 == 0) {
						DataModel level1 = new DataModel(Integer.valueOf(j));
						level1.parent = rootLevel;
						for (int k = 0; k < maxCount; k++) {
							if (k % 2 == 0) {
								DataModel level2 = new DataModel(Integer.valueOf(k));
								level2.parent = level1;
								level1.addChild(level2);
							}

						}
						rootLevel.addChild(level1);
					}

				}
				rootModel.add(rootLevel);
			}
		}
		return rootModel;
	}

	protected static class DataModel {
		public Integer id;
		public List<DataModel> children;
		public DataModel parent;

		public DataModel(Integer id) {
			this.id = id;
			children = new ArrayList<>();
		}

		public void addChild(DataModel child) {
			children.add(child);
		}

		@Override
		public String toString() {
			return "Item " + id;
		}
	}

	protected static class TestComparator extends ViewerComparator {
		@Override
		public int compare(Viewer viewer, Object e1, Object e2) {
			if (e1 instanceof DataModel mod1 && e2 instanceof DataModel mod2) {
				return mod1.id.compareTo(mod2.id);
			}
			return super.compare(viewer, e1, e2);
		}
	}

	public static class TestViewerFilter extends ViewerFilter {
		@Override
		public boolean select(Viewer viewer, Object parent, Object element) {
			return ((DataModel) element).id.intValue() > 10;
		}
	}

}
