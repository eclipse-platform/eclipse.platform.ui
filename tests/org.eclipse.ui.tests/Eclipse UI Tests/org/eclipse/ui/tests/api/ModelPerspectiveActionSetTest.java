/*******************************************************************************
 * Copyright (c) 2026 Aleksandar Kurtakov and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Aleksandar Kurtakov - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspectiveStack;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.e4.compatibility.ModeledPageLayout;
import org.eclipse.ui.internal.registry.IActionSetDescriptor;
import org.eclipse.ui.internal.registry.PerspectiveRegistry;
import org.junit.Test;

/**
 * Setting up the page of a window applies the <code>perspectiveExtensions</code>
 * contributions to the perspectives of that window. The scratch layout used to
 * read them is tagged with every initially visible action set, which must not be
 * taken over.
 */
public class ModelPerspectiveActionSetTest {

	private static final String PERSPECTIVE_ID = "org.eclipse.ui.tests.api.modelPerspectiveActionSets";
	/** Initially visible and contributed to {@link #PERSPECTIVE_ID} in plugin.xml. */
	private static final String CONTRIBUTED_ACTION_SET_ID = "org.eclipse.ui.tests.api.initiallyVisibleActionSet";

	@Test
	public void testInitiallyVisibleActionSetsAreNotActivated() {
		// without such an action set nothing could leak and the test would pass for the
		// wrong reason
		assertTrue("the workbench must offer initially visible action sets besides the contributed one",
				Arrays.stream(WorkbenchPlugin.getDefault().getActionSetRegistry().getActionSets())
						.filter(IActionSetDescriptor::isInitiallyVisible)
						.anyMatch(actionSet -> !CONTRIBUTED_ACTION_SET_ID.equals(actionSet.getId())));

		Workbench workbench = (Workbench) PlatformUI.getWorkbench();
		EModelService modelService = workbench.getService(EModelService.class);
		MApplication application = workbench.getApplication();
		PerspectiveRegistry registry = (PerspectiveRegistry) workbench.getPerspectiveRegistry();

		MPerspective perspective = modelService.createModelElement(MPerspective.class);
		perspective.setElementId(PERSPECTIVE_ID);
		perspective.setLabel("Model Perspective Action Sets");

		MPerspectiveStack perspectiveStack = modelService.createModelElement(MPerspectiveStack.class);
		perspectiveStack.getChildren().add(perspective);
		perspectiveStack.setSelectedElement(perspective);

		MTrimmedWindow window = modelService.createModelElement(MTrimmedWindow.class);
		window.getChildren().add(perspectiveStack);

		// the perspective has to be known to the registry before the page of the window
		// is set up, just like one restored from the persisted model on startup
		registry.createDescriptor(perspective);
		IPerspectiveDescriptor descriptor = registry.findPerspectiveWithId(PERSPECTIVE_ID);
		assertNotNull("the model contributed perspective must be registered", descriptor);

		IWorkbenchWindow workbenchWindow = null;
		try {
			workbenchWindow = workbench.openWorkbenchWindow(workbench.getDefaultPageInput(), descriptor, window, true);
			assertEquals("the page must have been set up over the model contributed perspective", PERSPECTIVE_ID,
					workbenchWindow.getActivePage().getPerspective().getId());

			// the contributed action set is initially visible as well, so it also covers
			// that such a contribution is not mistaken for one of the seeded tags
			List<String> actionSetTags = perspective.getTags().stream()
					.filter(tag -> tag.startsWith(ModeledPageLayout.ACTION_SET_TAG)).toList();
			assertEquals("only the action set contributed by the perspective extension may be activated",
					List.of(ModeledPageLayout.ACTION_SET_TAG + CONTRIBUTED_ACTION_SET_ID), actionSetTags);
		} finally {
			if (workbenchWindow != null) {
				workbenchWindow.close();
			}
			application.getChildren().remove(window);
			registry.deletePerspective(descriptor);
		}
	}

}
