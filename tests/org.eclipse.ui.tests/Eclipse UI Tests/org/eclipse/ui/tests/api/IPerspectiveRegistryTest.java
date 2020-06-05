/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
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
