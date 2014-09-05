/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.tests.statushandlers;

import junit.framework.TestCase;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.ui.internal.statushandlers.IStatusDialogConstants;
import org.eclipse.ui.internal.statushandlers.WorkbenchStatusDialogManagerImpl;
import org.eclipse.ui.progress.IProgressConstants;
import org.eclipse.ui.statushandlers.StatusAdapter;

/**
 * @since 3.5
 *
 */
public class WorkbenchStatusDialogManagerImplTest extends TestCase {

	WorkbenchStatusDialogManagerImpl mgr;
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		mgr = new WorkbenchStatusDialogManagerImpl(0xFFFFFF, null);
		mgr.setProperty(IStatusDialogConstants.ANIMATION, Boolean.FALSE);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		if(mgr != null && mgr.getShell() != null){
			mgr.getShell().dispose();
		}
	}
	
	public void testDefaultTitle() {
		assertEquals(JFaceResources.getString("Problem_Occurred"), mgr
				.getProperty(IStatusDialogConstants.TITLE));
	}
	
	public void testOKStatusAcceptanceWhenOKStatusNotEnabled(){
		mgr.setProperty(IStatusDialogConstants.HANDLE_OK_STATUSES, Boolean.FALSE);
		assertEquals(false, mgr.shouldAccept(new StatusAdapter(Status.OK_STATUS)));
		
		assertEquals(true, mgr.shouldAccept(new StatusAdapter(Status.CANCEL_STATUS)));
	}
	
	public void testOKStatusAcceptanceWhenOKStatusEnabled(){
		mgr.setProperty(IStatusDialogConstants.HANDLE_OK_STATUSES, Boolean.FALSE);
		assertFalse(mgr.shouldAccept(new StatusAdapter(Status.OK_STATUS)));
		
		assertTrue(mgr.shouldAccept(new StatusAdapter(Status.CANCEL_STATUS)));
	}
	
	/*try not to accept cancel */
	public void testCheckMasking(){
		mgr.setProperty(IStatusDialogConstants.MASK, new Integer(0));
		assertFalse(mgr.shouldAccept(new StatusAdapter(Status.CANCEL_STATUS)));
	}
	
	public void testCheckRecognizingImmediatePrompting1(){
		//no property
		StatusAdapter sa = new StatusAdapter(new Status(IStatus.ERROR, "org.eclipse.ui.tests", "message"));
		assertTrue(mgr.shouldPrompt(sa));
	}
	
	public void testCheckRecognizingImmediatePrompting2(){
		//property set to false
		StatusAdapter sa = new StatusAdapter(new Status(IStatus.ERROR, "org.eclipse.ui.tests", "message"));
		sa.setProperty(IProgressConstants.NO_IMMEDIATE_ERROR_PROMPT_PROPERTY, Boolean.FALSE);
		assertTrue(mgr.shouldPrompt(sa));
	}
	
	public void testCheckRecognizingNonImmediatePrompting(){
		StatusAdapter sa = new StatusAdapter(new Status(IStatus.ERROR, "org.eclipse.ui.tests", "message"));
		sa.setProperty(IProgressConstants.NO_IMMEDIATE_ERROR_PROMPT_PROPERTY, Boolean.TRUE);
		assertFalse(mgr.shouldPrompt(sa));
	}
	
}
