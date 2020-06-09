/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
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
 ******************************************************************************/

package org.eclipse.ui.tests.statushandlers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.ui.internal.statushandlers.IStatusDialogConstants;
import org.eclipse.ui.internal.statushandlers.WorkbenchStatusDialogManagerImpl;
import org.eclipse.ui.progress.IProgressConstants;
import org.eclipse.ui.statushandlers.StatusAdapter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @since 3.5
 *
 */
public class WorkbenchStatusDialogManagerImplTest {

	WorkbenchStatusDialogManagerImpl mgr;

	@Before
	public void setUp() throws Exception {
		mgr = new WorkbenchStatusDialogManagerImpl(0xFFFFFF, null);
		mgr.setProperty(IStatusDialogConstants.ANIMATION, Boolean.FALSE);
	}

	@After
	public void tearDown() throws Exception {
		if(mgr != null && mgr.getShell() != null){
			mgr.getShell().dispose();
		}
	}

	@Test
	public void testDefaultTitle() {
		assertEquals(JFaceResources.getString("Problem_Occurred"), mgr
				.getProperty(IStatusDialogConstants.TITLE));
	}

	@Test
	public void testOKStatusAcceptanceWhenOKStatusNotEnabled(){
		mgr.setProperty(IStatusDialogConstants.HANDLE_OK_STATUSES, Boolean.FALSE);
		assertEquals(false, mgr.shouldAccept(new StatusAdapter(Status.OK_STATUS)));

		assertEquals(true, mgr.shouldAccept(new StatusAdapter(Status.CANCEL_STATUS)));
	}

	@Test
	public void testOKStatusAcceptanceWhenOKStatusEnabled(){
		mgr.setProperty(IStatusDialogConstants.HANDLE_OK_STATUSES, Boolean.FALSE);
		assertFalse(mgr.shouldAccept(new StatusAdapter(Status.OK_STATUS)));

		assertTrue(mgr.shouldAccept(new StatusAdapter(Status.CANCEL_STATUS)));
	}

	/*try not to accept cancel */
	@Test
	public void testCheckMasking(){
		mgr.setProperty(IStatusDialogConstants.MASK, Integer.valueOf(0));
		assertFalse(mgr.shouldAccept(new StatusAdapter(Status.CANCEL_STATUS)));
	}

	@Test
	public void testCheckRecognizingImmediatePrompting1(){
		//no property
		StatusAdapter sa = new StatusAdapter(new Status(IStatus.ERROR, "org.eclipse.ui.tests", "message"));
		assertTrue(mgr.shouldPrompt(sa));
	}

	@Test
	public void testCheckRecognizingImmediatePrompting2(){
		//property set to false
		StatusAdapter sa = new StatusAdapter(new Status(IStatus.ERROR, "org.eclipse.ui.tests", "message"));
		sa.setProperty(IProgressConstants.NO_IMMEDIATE_ERROR_PROMPT_PROPERTY, Boolean.FALSE);
		assertTrue(mgr.shouldPrompt(sa));
	}

	@Test
	public void testCheckRecognizingNonImmediatePrompting(){
		StatusAdapter sa = new StatusAdapter(new Status(IStatus.ERROR, "org.eclipse.ui.tests", "message"));
		sa.setProperty(IProgressConstants.NO_IMMEDIATE_ERROR_PROMPT_PROPERTY, Boolean.TRUE);
		assertFalse(mgr.shouldPrompt(sa));
	}

}
