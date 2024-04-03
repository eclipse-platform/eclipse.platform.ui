/*******************************************************************************
 * Copyright (c) 2022 Enda O'Brien and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which accompanies this distribution,
 * and is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors: IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.tests.markers;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.views.markers.ExtendedMarkersView;
import org.eclipse.ui.internal.views.markers.FiltersConfigurationDialog;
import org.eclipse.ui.internal.views.markers.MarkerContentGenerator;
import org.eclipse.ui.tests.FilterHelpTestView;
import org.eclipse.ui.views.markers.MarkerSupportView;
import org.junit.After;
import org.junit.Test;

public class FilterHelpLinkTest {
	FiltersConfigurationDialog dialog;

	@After
	public void cleanup() {
		if (dialog != null) {
			dialog.close();
		}
	}

	@Test
	public void helpDoesNotShowDyDefault() throws Exception {
		FilterHelpTestView view = (FilterHelpTestView) PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getActivePage().showView(FilterHelpTestView.ID);

		openFiltersDialog(view);
		assertFalse(JFaceResources.getString("helpToolTip"),
				isHelpAvailable(view.getButtonBar(), JFaceResources.getString("helpToolTip")));
	}

	@Test
	public void helpShowsWithHelpConfig() throws Exception {
		FilterHelpTestView.setShowHelp(true);
		FilterHelpTestView view = (FilterHelpTestView) PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getActivePage().showView(FilterHelpTestView.ID);

		openFiltersDialog(view);

		assertTrue(JFaceResources.getString("helpToolTip"),
				isHelpAvailable(view.getButtonBar(), JFaceResources.getString("helpToolTip")));
		FilterHelpTestView.setShowHelp(false);
	}

	void openFiltersDialog(MarkerSupportView view) {
		Method openFiltersDialog;
		try {
			openFiltersDialog = ExtendedMarkersView.class.getDeclaredMethod("openFiltersDialog");
			openFiltersDialog.setAccessible(true);
			openFiltersDialog.invoke(view);
		} catch (NoSuchMethodException | SecurityException | IllegalArgumentException | IllegalAccessException
				| InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	public static MarkerContentGenerator getMarkerContentGenerator(MarkerSupportView view) {
		MarkerContentGenerator generator = null;
		try {
			Field fieldGenerator = ExtendedMarkersView.class.getDeclaredField("generator");
			fieldGenerator.setAccessible(true);
			generator = (MarkerContentGenerator) fieldGenerator.get(view);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
		}
		return generator;
	}

	boolean isHelpAvailable(Composite composite, String helpTooltip) {
		Control[] children = composite.getChildren();
		for (Control ctrl : children) {
			if (ctrl instanceof ToolBar) {
				ToolBar tb = (ToolBar) ctrl;
				ToolItem[] toolItems = tb.getItems();
				for (ToolItem ti : toolItems) {
					System.out.println(ti.getToolTipText());
					if (helpTooltip.equals(ti.getToolTipText())) {
						return true;
					}
				}
			}
		}
		return false;
	}
}
