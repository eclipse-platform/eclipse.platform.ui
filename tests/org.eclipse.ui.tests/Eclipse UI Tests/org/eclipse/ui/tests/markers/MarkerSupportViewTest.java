/*******************************************************************************
 * Copyright (c) 2022 Enda O'Brien and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which accompanies this distribution,
 * and is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors: Enda O'Brien - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.tests.markers;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.views.markers.ExtendedMarkersView;
import org.eclipse.ui.internal.views.markers.FiltersConfigurationDialog;
import org.eclipse.ui.internal.views.markers.MarkerContentGenerator;
import org.eclipse.ui.views.markers.MarkerField;
import org.eclipse.ui.views.markers.MarkerSupportView;
import org.eclipse.ui.views.markers.internal.ContentGeneratorDescriptor;
import org.eclipse.ui.views.markers.internal.MarkerGroup;
import org.eclipse.ui.views.markers.internal.MarkerMessages;
import org.eclipse.ui.views.markers.internal.MarkerSupportRegistry;
import org.eclipse.ui.views.markers.internal.MarkerType;
import org.junit.Test;

public class MarkerSupportViewTest {

	private static final String PROBLEM_VIEW_ID = "org.eclipse.ui.views.ProblemView";

	@Test
	public void canOverrideOpenSelectedMarkers() {
		Boolean[] canOverride = new Boolean[] { false };
		new MarkerSupportView("") {
			@Override
			protected void openSelectedMarkers() {
				canOverride[0] = true;
			}
		}.openSelectedMarkers();

		assertTrue(canOverride[0]);
	}

	@Test
	public void filterEnabled() throws Exception {
		MarkerSupportView view = (MarkerSupportView) PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getActivePage().showView(PROBLEM_VIEW_ID);

		FiltersConfigDialog dialog = new FiltersConfigDialog(getMarkerContentGenerator(view));

		dialog.setBlockOnOpen(false);
		dialog.open();

		// de-select the checkbox to enable filters
		dialog.selectCheckboxInDialog(false, MarkerMessages.ALL_Title);
		CheckboxTableViewer ctv = dialog.getCheckboxTableViewer();
		ctv.setAllChecked(true);

		dialog.okPressed();

		boolean isFilterEnabled = view.isFilterEnabled();
		assertTrue(isFilterEnabled);
	}

	@Test
	public void filterDisabled() throws Exception {
		MarkerSupportView view = (MarkerSupportView) PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getActivePage().showView(PROBLEM_VIEW_ID);

		FiltersConfigDialog dialog = new FiltersConfigDialog(getMarkerContentGenerator(view));

		dialog.setBlockOnOpen(false);
		dialog.open();

		// select the checkbox to disable filters
		dialog.selectCheckboxInDialog(true, MarkerMessages.ALL_Title);

		dialog.okPressed();

		boolean isFilterEnabled = view.isFilterEnabled();
		assertFalse(isFilterEnabled);
	}

	@Test
	public void limitEnabled() throws Exception {
		MarkerSupportView view = (MarkerSupportView) PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getActivePage().showView(PROBLEM_VIEW_ID);

		FiltersConfigDialog dialog = new FiltersConfigDialog(getMarkerContentGenerator(view));

		dialog.setBlockOnOpen(false);
		dialog.open();
		dialog.selectCheckboxInDialog(true, MarkerMessages.MarkerPreferences_MarkerLimits);
		dialog.okPressed();

		boolean isLimitEnabled = view.isMarkerLimitsEnabled();
		assertTrue(isLimitEnabled);
	}

	@Test
	public void limitDisabled() throws Exception {
		MarkerSupportView view = (MarkerSupportView) PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getActivePage().showView(PROBLEM_VIEW_ID);

		FiltersConfigDialog dialog = new FiltersConfigDialog(getMarkerContentGenerator(view));

		dialog.setBlockOnOpen(false);
		dialog.open();
		dialog.selectCheckboxInDialog(false, MarkerMessages.MarkerPreferences_MarkerLimits);
		dialog.okPressed();

		boolean isLimitEnabled = view.isMarkerLimitsEnabled();
		assertFalse(isLimitEnabled);
	}

	@Test
	public void markerContentGeneratorExtensionLoaded() throws Exception {
		MarkerSupportView view = (MarkerSupportView) PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getActivePage().showView(PROBLEM_VIEW_ID);

		MarkerContentGenerator generator = getMarkerContentGenerator(view);
		ContentGeneratorDescriptor descriptor = MarkerSupportRegistry.getInstance()
				.getContentGenDescriptor(generator.getId());

		assertNotNull(descriptor);

		MarkerField[] allFields = descriptor.getAllFields();
		List<String> allFieldNames = mapToNames(allFields);
		String fieldName1 = "Problem Key";
		String fieldName2 = "Problem Key V2";

		assertTrue(
				"Expected loading marker field '" + fieldName1
						+ "' from marker content generator extensions, but got only " + allFieldNames,
				allFieldNames.contains(fieldName1));
		assertTrue(
				"Expected recursively loading marker field '" + fieldName2
						+ "' from marker content generator extensions, but got only " + allFieldNames,
				allFieldNames.contains(fieldName2));

		MarkerField[] initiallyVisibleFields = descriptor.getInitialVisible();
		List<String> initiallyVisibleFieldNames = mapToNames(initiallyVisibleFields);

		assertTrue("Expected marker field '" + fieldName1
				+ "' from marker content generator extension being visible according to 'visible' attribute in the extension,"
				+ " but only the following marker fields are visible " + initiallyVisibleFieldNames,
				initiallyVisibleFieldNames.contains(fieldName1));
		assertFalse("Expected marker field '" + fieldName2
				+ "' from marker content generator extension being not visible according to 'visible' attribute in the extension,"
				+ " but the following marker fields are visible " + initiallyVisibleFieldNames,
				initiallyVisibleFieldNames.contains(fieldName2));

		String markerTypeId = "org.eclipse.ui.tests.markers.artificial.problem";
		MarkerType markerTypeFromExtension = descriptor.getType(markerTypeId);
		List<String> markerTypeIds = descriptor.getMarkerTypes().stream().map(MarkerType::getId).toList();

		assertNotNull("Marker type with id '" + markerTypeId + "' not loaded from marker content generator extension.",
				markerTypeFromExtension);
		assertTrue("Expected marker type id '" + markerTypeId + "' being in marker types list, but we have only "
				+ markerTypeIds, markerTypeIds.contains(markerTypeId));

		Collection<MarkerGroup> groups = descriptor.getMarkerGroups();
		List<String> groupIds = groups.stream().map(MarkerGroup::getId).toList();
		String groupId = "org.eclipse.ui.tests.test.extended";

		assertTrue("Expected loading group id '" + groupId
				+ "' from marker content generator extension, but got only the following group ids: " + groupIds,
				groupIds.contains(groupId));
	}

	private List<String> mapToNames(MarkerField[] markerFields) {
		if (markerFields == null || markerFields.length == 0) {
			return Collections.emptyList();
		}

		return Arrays.stream(markerFields).map(mf -> mf.getName()).toList();
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

	class FiltersConfigDialog extends FiltersConfigurationDialog {

		protected Composite _area;

		protected MarkerContentGenerator _generator;

		public FiltersConfigDialog(MarkerContentGenerator generator) {
			super(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), generator);

			_generator = generator;
		}

		@Override
		protected Control createDialogArea(Composite parent) {
			_area = (Composite) super.createDialogArea(parent);
			return _area;
		}

		public CheckboxTableViewer getCheckboxTableViewer() {
			CheckboxTableViewer ctb = null;
			try {
				Field fieldGenerator = FiltersConfigurationDialog.class.getDeclaredField("configsTable");
				fieldGenerator.setAccessible(true);
				ctb = (CheckboxTableViewer) fieldGenerator.get(this);
			} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			}
			return ctb;
		}

		public boolean getCheckboxSelectionInDialog(String buttonMessage) {
			return getCheckboxSelectionInDialog(_area, buttonMessage);
		}

		private boolean getCheckboxSelectionInDialog(Composite composite, String buttonMessage) {
			Control[] children = composite.getChildren();
			for (Control ctrl : children) {
				if (ctrl instanceof Button button) {
					if (button.getText().equals(buttonMessage)) {
						return button.getSelection();
					}
				} else if (ctrl instanceof Composite ee) {
					return getCheckboxSelectionInDialog(ee, buttonMessage);
				}
			}
			throw new RuntimeException();
		}

		public void selectCheckboxInDialog(boolean select, String buttonMessage) {
			selectCheckboxInDialog(select, _area, buttonMessage);
		}

		private void selectCheckboxInDialog(boolean select, Composite composite, String buttonMessage) {
			Control[] children = composite.getChildren();
			for (Control ctrl : children) {
				if (ctrl instanceof Button button) {
					if (button.getText().equals(buttonMessage)) {
						button.setSelection(select);
					}
				} else if (ctrl instanceof Composite ee) {
					selectCheckboxInDialog(select, ee, buttonMessage);
				}
			}
		}

		@Override
		public void okPressed() {
			super.okPressed();

			// Normally updateFilters is done in ExtendedMarkersView.openFiltersDialog()
			// but since its not available (internal) we need to do it here for the purpose
			// of the
			// test.

			Method getFilters;
			try {
				getFilters = FiltersConfigurationDialog.class.getDeclaredMethod("getFilters");
				getFilters.setAccessible(true);
				Object filtersGot = getFilters.invoke(this);

				Method andFilters = FiltersConfigurationDialog.class.getDeclaredMethod("andFilters");
				andFilters.setAccessible(true);
				Object filtersAnd = andFilters.invoke(this);

				Method updateFilters = MarkerContentGenerator.class.getDeclaredMethod("updateFilters", Collection.class,
						boolean.class);
				updateFilters.setAccessible(true);

				updateFilters.invoke(_generator, filtersGot, filtersAnd);

			} catch (Exception e) {
				e.printStackTrace();
			}

		}

	}
}
