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
 *     Andrey Loskutov (loskutov@gmx.de) - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.views;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * <p>
 * Configure a {@link ColumnViewer} to show limited items per parent before
 * showing an ExpandableNode. Limit used is read from preference
 * {@link IWorkbenchPreferenceConstants#LARGE_VIEW_LIMIT}. Client must call this
 * before {@link Viewer#setInput(Object)}
 * </p>
 *
 * @since 3.130
 */
public class WorkbenchViewerSetup {

	static Map<DisposeListener, ColumnViewer> registeredViewers = new ConcurrentHashMap<>();

	static final IPropertyChangeListener propertyChangeListener = event -> {
		if (!IWorkbenchPreferenceConstants.LARGE_VIEW_LIMIT.equals(event.getProperty())) {
			return;
		}
		int itemsLimit = getItemsLimit();
		registeredViewers.values().forEach(v -> {
			v.setDisplayIncrementally(itemsLimit);
			Object input = v.getInput();
			if (input != null) {
				v.setInput(null);
				v.setInput(input);
			} else {
				v.refresh();
			}
		});
	};

	static {
		getPreferenceStore().addPropertyChangeListener(propertyChangeListener);
	}

	/**
	 * Returns the current viewer limit set in the {@code General} preference page.
	 *
	 * @return {@link IWorkbenchPreferenceConstants#LARGE_VIEW_LIMIT}
	 */
	public static int getItemsLimit() {
		return getPreferenceStore().getInt(IWorkbenchPreferenceConstants.LARGE_VIEW_LIMIT);
	}

	private static IPreferenceStore getPreferenceStore() {
		return WorkbenchPlugin.getDefault().getPreferenceStore();
	}

	/**
	 * Configure a {@link ColumnViewer} to show limited items per parent before
	 * showing an ExpandableNode. Limit used is read from preference
	 * {@link IWorkbenchPreferenceConstants#LARGE_VIEW_LIMIT}. Client must call this
	 * before {@link Viewer#setInput(Object)}
	 * <p>
	 * User can change the viewer limit on preference page any time in the lifetime
	 * of the viewer. This setup takes care of refreshing the viewer with the new
	 * limit set.
	 * </p>
	 *
	 * @param viewer {@link ColumnViewer} which has to configured for showing
	 *               limited items.
	 */
	public static void setupViewer(ColumnViewer viewer) {
		viewer.setDisplayIncrementally(getItemsLimit());
		Control control = viewer.getControl();
		if (control != null) {
			control.addDisposeListener(new DisposeListener(viewer));
		}
	}

	private static class DisposeListener implements org.eclipse.swt.events.DisposeListener {

		public DisposeListener(ColumnViewer viewer) {
			registeredViewers.put(this, viewer);
		}

		@Override
		public void widgetDisposed(DisposeEvent e) {
			registeredViewers.remove(this);
		}

	}
}
