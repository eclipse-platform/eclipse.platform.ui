/*******************************************************************************
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which accompanies this distribution,
 * and is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.ui.tests.markers;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.ui.internal.views.markers.MarkerContentGenerator;
import org.eclipse.ui.internal.views.markers.MarkerFieldFilterGroup;
import org.eclipse.ui.views.markers.MarkerSupportView;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@code MarkerFieldFilterGroup.selectByScope()} to verify that
 * scope-based filtering works correctly, especially the fix where
 * selection-dependent scopes fall back to ON_ANY when no resources are selected
 * (preventing the "vanishing markers" bug).
 */
public class MarkerFieldFilterGroupTest {

	private static final String PROBLEM_VIEW_ID = "org.eclipse.ui.views.ProblemView";

	private IProject project;
	private IMarker marker;

	@Before
	public void setUp() throws Exception {
		project = ResourcesPlugin.getWorkspace().getRoot().getProject("MarkerFilterTest");
		if (!project.exists()) {
			project.create(null);
		}
		if (!project.isOpen()) {
			project.open(null);
		}
		marker = project.createMarker(IMarker.PROBLEM);
		marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
		marker.setAttribute(IMarker.MESSAGE, "Test error marker");
	}

	@After
	public void tearDown() throws Exception {
		if (project != null && project.exists()) {
			project.delete(true, null);
		}
	}

	@Test
	public void selectByScope_ON_ANY_alwaysReturnsTrue() throws Exception {
		MarkerFieldFilterGroup group = createFilterGroup();
		group.setScope(MarkerFieldFilterGroup.ON_ANY);
		Object entry = createMarkerEntry(marker);

		assertTrue("ON_ANY should select marker with empty resources",
				invokeSelectByScope(group, entry, new IResource[0]));
		assertTrue("ON_ANY should select marker with resources",
				invokeSelectByScope(group, entry, new IResource[] { project }));
	}

	@Test
	public void selectByScope_ON_SELECTED_ONLY_emptyResourcesFallsBackToShowAll() throws Exception {
		MarkerFieldFilterGroup group = createFilterGroup();
		group.setScope(MarkerFieldFilterGroup.ON_SELECTED_ONLY);
		Object entry = createMarkerEntry(marker);

		assertTrue("ON_SELECTED_ONLY with empty resources should fall back to showing all",
				invokeSelectByScope(group, entry, new IResource[0]));
	}

	@Test
	public void selectByScope_ON_SELECTED_AND_CHILDREN_emptyResourcesFallsBackToShowAll() throws Exception {
		MarkerFieldFilterGroup group = createFilterGroup();
		group.setScope(MarkerFieldFilterGroup.ON_SELECTED_AND_CHILDREN);
		Object entry = createMarkerEntry(marker);

		assertTrue("ON_SELECTED_AND_CHILDREN with empty resources should fall back to showing all",
				invokeSelectByScope(group, entry, new IResource[0]));
	}

	@Test
	public void selectByScope_ON_ANY_IN_SAME_CONTAINER_emptyResourcesFallsBackToShowAll() throws Exception {
		MarkerFieldFilterGroup group = createFilterGroup();
		group.setScope(MarkerFieldFilterGroup.ON_ANY_IN_SAME_CONTAINER);
		Object entry = createMarkerEntry(marker);

		assertTrue("ON_ANY_IN_SAME_CONTAINER with empty resources should fall back to showing all",
				invokeSelectByScope(group, entry, new IResource[0]));
	}

	@Test
	public void selectByScope_ON_SELECTED_ONLY_matchesSelectedResource() throws Exception {
		MarkerFieldFilterGroup group = createFilterGroup();
		group.setScope(MarkerFieldFilterGroup.ON_SELECTED_ONLY);
		Object entry = createMarkerEntry(marker);

		assertTrue("ON_SELECTED_ONLY should match marker on selected resource",
				invokeSelectByScope(group, entry, new IResource[] { project }));
	}

	@Test
	public void selectByScope_ON_SELECTED_ONLY_doesNotMatchDifferentResource() throws Exception {
		MarkerFieldFilterGroup group = createFilterGroup();
		group.setScope(MarkerFieldFilterGroup.ON_SELECTED_ONLY);
		Object entry = createMarkerEntry(marker);

		IProject otherProject = ResourcesPlugin.getWorkspace().getRoot().getProject("OtherProject");
		try {
			if (!otherProject.exists()) {
				otherProject.create(null);
			}
			if (!otherProject.isOpen()) {
				otherProject.open(null);
			}
			assertFalse("ON_SELECTED_ONLY should not match marker on different resource",
					invokeSelectByScope(group, entry, new IResource[] { otherProject }));
		} finally {
			if (otherProject.exists()) {
				otherProject.delete(true, null);
			}
		}
	}

	@Test
	public void selectByScope_ON_SELECTED_AND_CHILDREN_matchesChildResource() throws Exception {
		MarkerFieldFilterGroup group = createFilterGroup();
		group.setScope(MarkerFieldFilterGroup.ON_SELECTED_AND_CHILDREN);
		Object entry = createMarkerEntry(marker);

		assertTrue("ON_SELECTED_AND_CHILDREN should match marker on child of selected resource",
				invokeSelectByScope(group, entry, new IResource[] { project }));
	}

	@Test
	public void selectByScope_ON_ANY_IN_SAME_CONTAINER_matchesSameProject() throws Exception {
		MarkerFieldFilterGroup group = createFilterGroup();
		group.setScope(MarkerFieldFilterGroup.ON_ANY_IN_SAME_CONTAINER);
		Object entry = createMarkerEntry(marker);

		assertTrue("ON_ANY_IN_SAME_CONTAINER should match marker in same project",
				invokeSelectByScope(group, entry, new IResource[] { project }));
	}

	private MarkerFieldFilterGroup createFilterGroup() {
		try {
			MarkerContentGenerator generator = MarkerSupportViewTest
					.getMarkerContentGenerator(
							(MarkerSupportView) org.eclipse.ui.PlatformUI.getWorkbench()
									.getActiveWorkbenchWindow().getActivePage()
									.showView(PROBLEM_VIEW_ID));
			return new MarkerFieldFilterGroup(null, generator);
		} catch (Exception e) {
			throw new RuntimeException("Failed to create filter group for testing", e);
		}
	}

	/**
	 * Creates a MarkerEntry via reflection since it's an internal class.
	 */
	private Object createMarkerEntry(IMarker m) throws Exception {
		Class<?> markerEntryClass = Class.forName("org.eclipse.ui.internal.views.markers.MarkerEntry");
		Constructor<?> constructor = markerEntryClass.getConstructor(IMarker.class);
		constructor.setAccessible(true);
		return constructor.newInstance(m);
	}

	/**
	 * Invokes selectByScope via reflection to work with the MarkerEntry object.
	 */
	private boolean invokeSelectByScope(MarkerFieldFilterGroup group, Object entry, IResource[] resources)
			throws Exception {
		Class<?> markerEntryClass = Class.forName("org.eclipse.ui.internal.views.markers.MarkerEntry");
		Method method = MarkerFieldFilterGroup.class.getMethod("selectByScope", markerEntryClass, IResource[].class);
		method.setAccessible(true);
		return (boolean) method.invoke(group, entry, resources);
	}
}
