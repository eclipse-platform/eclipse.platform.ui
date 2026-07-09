/*******************************************************************************
 * Copyright (c) 2000, 2026 IBM Corporation and others.
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
package org.eclipse.ui.tests.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspectiveStack;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.tests.harness.util.ArrayUtil;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class IPerspectiveRegistryTest {

	private IPerspectiveRegistry fReg;

	@Before
	public void setUp() {
		fReg = PlatformUI.getWorkbench().getPerspectiveRegistry();
	}

	@Test
	public void testFindPerspectiveWithId() {
		IPerspectiveDescriptor pers = (IPerspectiveDescriptor) ArrayUtil
				.pickRandom(fReg.getPerspectives());

		IPerspectiveDescriptor suspect = fReg.findPerspectiveWithId(pers
				.getId());
		assertNotNull(suspect);
		assertEquals(pers, suspect);

		suspect = fReg.findPerspectiveWithId(IConstants.FakeID);
		assertNull(suspect);
	}

	@Ignore
	@Test
	public void testFindPerspectiveWithLabel() {
		IPerspectiveDescriptor pers = (IPerspectiveDescriptor) ArrayUtil.pickRandom(fReg.getPerspectives());

		IPerspectiveDescriptor suspect = fReg.findPerspectiveWithLabel(pers.getLabel());
		assertNotNull(suspect);
		assertEquals(pers, suspect);

		suspect = fReg.findPerspectiveWithLabel(IConstants.FakeLabel);
		assertNull(suspect);
	}

	@Test
	public void testGetDefaultPerspective() {
		String id = fReg.getDefaultPerspective();
		assertNotNull(id);

		IPerspectiveDescriptor suspect = fReg.findPerspectiveWithId(id);
		assertNotNull(suspect);
	}

	@Test
	public void testSetDefaultPerspective() {
		IPerspectiveDescriptor pers = (IPerspectiveDescriptor) ArrayUtil
				.pickRandom(fReg.getPerspectives());
		fReg.setDefaultPerspective(pers.getId());

		assertEquals(pers.getId(), fReg.getDefaultPerspective());
	}

	@Test
	public void testGetPerspectives() throws Throwable {
		IPerspectiveDescriptor[] pers = fReg.getPerspectives();
		assertNotNull(pers);

		for (IPerspectiveDescriptor per : pers) {
			assertNotNull(per);
		}
	}

	/**
	 * Perspectives contributed directly into the model (not via the extension
	 * point) must still be registered so they are listed and can be opened by id.
	 */
	@Test
	public void testModelPerspective() {
		WorkbenchWindow window = (WorkbenchWindow) PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		EModelService modelService = window.getService(EModelService.class);

		List<MPerspectiveStack> stacks = modelService.findElements(window.getModel(), null, MPerspectiveStack.class);
		assertFalse("expected a perspective stack in the active window", stacks.isEmpty());
		MPerspectiveStack stack = stacks.get(0);

		String id = "org.eclipse.ui.tests.modelContributedPerspective";
		String label = "Model Contributed Perspective";
		MPerspective perspective = modelService.createModelElement(MPerspective.class);
		perspective.setElementId(id);
		perspective.setLabel(label);
		perspective.setToBeRendered(false);
		stack.getChildren().add(perspective);

		try {
			// It must appear in the list backing the "Open Perspective" dialog.
			assertTrue("the model-contributed perspective should be listed in the registry",
					Arrays.stream(fReg.getPerspectives()).anyMatch(d -> id.equals(d.getId())));

			// It must be resolvable by id so that opening it actually works.
			IPerspectiveDescriptor descriptor = fReg.findPerspectiveWithId(id);
			assertNotNull("model-contributed perspective must be resolvable by id", descriptor);
			assertEquals(id, descriptor.getId());
			assertEquals(label, descriptor.getLabel());
		} finally {
			stack.getChildren().remove(perspective);
			IPerspectiveDescriptor descriptor = fReg.findPerspectiveWithId(id);
			if (descriptor != null) {
				fReg.deletePerspective(descriptor);
			}
		}
	}

	@Test
	@Ignore
	public void XXXtestDeleteClonedPerspective() {
		IWorkbenchPage page = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage();
		String perspId = page.getPerspective().getId() + ".1";
		IPerspectiveDescriptor desc = fReg.clonePerspective(perspId, perspId, page.getPerspective());
		page.setPerspective(desc);

		assertNotNull(fReg.findPerspectiveWithId(perspId));

		page.closePerspective(desc, false, false);
		fReg.deletePerspective(desc);

		assertNull(fReg.findPerspectiveWithId(perspId));
	}
}
