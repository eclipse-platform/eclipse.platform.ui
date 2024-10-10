/*******************************************************************************
 * Copyright (c) 2024 Enda O'Brien and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which accompanies this distribution,
 * and is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors: Enda O'Brien, Pilz Ireland - PR #144
 *******************************************************************************/
package org.eclipse.ui.tests.markers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.views.markers.ExtendedMarkersView;
import org.eclipse.ui.internal.views.markers.MarkerContentGenerator;
import org.eclipse.ui.tests.NoApplicationAttribTestView;
import org.eclipse.ui.tests.SubTypeOnlyTestView;
import org.eclipse.ui.tests.TypeAndSubTypeTestView;
import org.eclipse.ui.tests.TypeOnlyTestView;
import org.eclipse.ui.views.markers.MarkerSupportView;
import org.eclipse.ui.views.markers.internal.ContentGeneratorDescriptor;
import org.eclipse.ui.views.markers.internal.MarkerType;
import org.eclipse.ui.views.markers.internal.MarkerTypesModel;
import org.junit.Test;

public class MarkerTypeTests {

	static final String PROBLEM_MARKER = "org.eclipse.core.resources.problemmarker";

	@Test
	public void canIncludeTypeOnly() throws Exception {
		MarkerSupportView view = (MarkerSupportView) PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getActivePage().showView(TypeOnlyTestView.ID);

		MarkerContentGenerator generator = getMarkerContentGenerator(view);
		Collection<MarkerType> filterDialogTypes = getMarkerTypes(generator);

		assertEquals(1, filterDialogTypes.size());
		assertEquals(PROBLEM_MARKER, filterDialogTypes.stream().map(type -> type.getId()).findFirst().get());
	}

	@Test
	public void canIncludeTypeAndSubTypes() throws Exception {
		MarkerSupportView view = (MarkerSupportView) PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getActivePage().showView(TypeAndSubTypeTestView.ID);

		MarkerContentGenerator generator = getMarkerContentGenerator(view);
		Collection<MarkerType> filterDialogTypes = getMarkerTypes(generator);

		Collection<MarkerType> markerTypes = new HashSet<>();
		markerTypes.add(MarkerTypesModel.getInstance().getType(PROBLEM_MARKER));
		markerTypes.addAll(Arrays.asList(MarkerTypesModel.getInstance().getType(PROBLEM_MARKER).getAllSubTypes()));

		assertEquals(markerTypes.size(), filterDialogTypes.size());
		assertEquals(PROBLEM_MARKER, filterDialogTypes.stream().map(type -> type.getId())
				.filter(s -> s.equals(PROBLEM_MARKER)).findFirst().get());

		for (MarkerType type : markerTypes) {
			assertTrue(filterDialogTypes.contains(type));
		}
	}

	@Test
	public void canIncludeSubtypesOnly() throws Exception {
		MarkerSupportView view = (MarkerSupportView) PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getActivePage().showView(SubTypeOnlyTestView.ID);

		MarkerContentGenerator generator = getMarkerContentGenerator(view);
		Collection<MarkerType> filterDialogTypes = getMarkerTypes(generator);

		Collection<MarkerType> markerTypes = new HashSet<>();
		markerTypes.addAll(Arrays.asList(MarkerTypesModel.getInstance().getType(PROBLEM_MARKER).getAllSubTypes()));

		assertEquals(markerTypes.size(), filterDialogTypes.size());
		assertTrue(PROBLEM_MARKER, filterDialogTypes.stream().map(type -> type.getId())
				.filter(s -> s.equals(PROBLEM_MARKER)).findFirst().isEmpty());
		for (MarkerType type : markerTypes) {
			assertTrue(filterDialogTypes.contains(type));
		}
	}

	@Test
	public void typeAndSubTypesIsDefault() throws Exception {
		MarkerSupportView view = (MarkerSupportView) PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getActivePage().showView(NoApplicationAttribTestView.ID);

		MarkerContentGenerator generator = getMarkerContentGenerator(view);
		Collection<MarkerType> filterDialogTypes = getMarkerTypes(generator);

		Collection<MarkerType> markerTypes = new HashSet<>();
		markerTypes.add(MarkerTypesModel.getInstance().getType(PROBLEM_MARKER));
		markerTypes.addAll(Arrays.asList(MarkerTypesModel.getInstance().getType(PROBLEM_MARKER).getAllSubTypes()));

		assertEquals(markerTypes.size(), filterDialogTypes.size());
		assertEquals(PROBLEM_MARKER, filterDialogTypes.stream().map(type -> type.getId())
				.filter(s -> s.equals(PROBLEM_MARKER)).findFirst().get());

		for (MarkerType type : markerTypes) {
			assertTrue(filterDialogTypes.contains(type));
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

	@SuppressWarnings("unchecked")
	public static Collection<MarkerType> getMarkerTypes(MarkerContentGenerator generator) {
		Collection<MarkerType> selectedTypesCollection = null;
		try {
			Field generatorDescriptor = MarkerContentGenerator.class.getDeclaredField("generatorDescriptor");
			generatorDescriptor.setAccessible(true);

			ContentGeneratorDescriptor contentGeneratorDescriptor = (ContentGeneratorDescriptor) generatorDescriptor
					.get(generator);

			Field markerTypesField = ContentGeneratorDescriptor.class.getDeclaredField("markerTypes");
			markerTypesField.setAccessible(true);

			selectedTypesCollection = (Collection<MarkerType>) markerTypesField.get(contentGeneratorDescriptor);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
		}
		return selectedTypesCollection;
	}
}
