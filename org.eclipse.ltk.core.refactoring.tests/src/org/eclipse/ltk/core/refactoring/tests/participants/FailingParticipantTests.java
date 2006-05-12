/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.core.refactoring.tests.participants;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.eclipse.core.runtime.NullProgressMonitor;

import org.eclipse.ltk.core.refactoring.Change;

public class FailingParticipantTests extends TestCase {

	private ElementRenameRefactoring fRefactoring;
	
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	public void test() throws Exception {
		fRefactoring= new ElementRenameRefactoring();
		fRefactoring.checkInitialConditions(new NullProgressMonitor());
		fRefactoring.checkFinalConditions(new NullProgressMonitor());
		
		boolean exception= false;
		try {
			fRefactoring.createChange(new NullProgressMonitor());
		} catch (FailingParticipant.Exception e) {
			exception= true;
		}
		Assert.assertTrue("No exception generated", exception);
		
		
		fRefactoring= new ElementRenameRefactoring();
		fRefactoring.checkInitialConditions(new NullProgressMonitor());
		fRefactoring.checkFinalConditions(new NullProgressMonitor());
		// this time create change must pass, but execution still fails
		Change change= fRefactoring.createChange(new NullProgressMonitor());
		exception= false;
		try {
			change.perform(new NullProgressMonitor());
		} catch (FailingParticipant2.Exception e) {
			exception= true;
		}
		Assert.assertTrue("No exception generated", exception);

		// this time everything must pass 
		fRefactoring= new ElementRenameRefactoring();
		fRefactoring.checkInitialConditions(new NullProgressMonitor());
		fRefactoring.checkFinalConditions(new NullProgressMonitor());
		change= fRefactoring.createChange(new NullProgressMonitor());
		change.perform(new NullProgressMonitor());
	}
}

