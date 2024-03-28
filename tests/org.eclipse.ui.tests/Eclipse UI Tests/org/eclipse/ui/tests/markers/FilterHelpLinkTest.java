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

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IPageLayout;
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
	public void helpDoesNotShowWithNoHelpConfig() throws Exception {
		MarkerSupportView view = (MarkerSupportView) PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getActivePage().showView(IPageLayout.ID_PROBLEM_VIEW);

		Composite composite = showFilterDialog(view);
		assertFalse(JFaceResources.getString("helpToolTip"),
				isHelpAvailable(composite, JFaceResources.getString("helpToolTip")));
	}

	@Test
	public void helpShowsWithHelpConfig() throws Exception {
		MarkerSupportView view = (MarkerSupportView) PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getActivePage().showView(FilterHelpTestView.ID);

		Composite composite = showFilterDialog(view);
		assertTrue(JFaceResources.getString("helpToolTip"),
				isHelpAvailable(composite, JFaceResources.getString("helpToolTip")));
	}

	Composite showFilterDialog(MarkerSupportView view) throws Exception {

		MarkerContentGenerator generator = getMarkerContentGenerator(view);

		Composite[] bbComposite = new Composite[1];
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		dialog = new FiltersConfigurationDialog(shell, generator) {
			@Override
			protected Control createButtonBar(Composite parent) {
				bbComposite[0] = (Composite) super.createButtonBar(parent);
				return bbComposite[0];
			}
		};

		dialog.setBlockOnOpen(false);
		dialog.open();

		return bbComposite[0];
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
					if (helpTooltip.equals(ti.getToolTipText())) {
						return true;
					}
				}
			}
		}
		return false;
	}
}
