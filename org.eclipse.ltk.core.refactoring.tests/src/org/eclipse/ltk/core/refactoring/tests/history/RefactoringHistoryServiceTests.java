/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.core.refactoring.tests.history;

import java.util.Collections;

import junit.framework.TestCase;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.ltk.core.refactoring.CheckConditionsOperation;
import org.eclipse.ltk.core.refactoring.PerformRefactoringOperation;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptorProxy;
import org.eclipse.ltk.core.refactoring.history.RefactoringHistory;

import org.eclipse.ltk.internal.core.refactoring.history.RefactoringHistoryService;

import org.eclipse.ltk.core.refactoring.tests.util.SimpleTestProject;

public class RefactoringHistoryServiceTests extends TestCase {

	private static final int BREAKING_NUMBER= 20;

	private static final int NONE_NUMBER= 100;

	private static final int STAMP_FACTOR= 10000000;

	private static final int STRUCTURAL_NUMBER= 10;

	private SimpleTestProject fProject;

	private void assertDescendingSortOrder(RefactoringDescriptorProxy[] proxies) {
		for (int index= 0; index < proxies.length - 1; index++)
			assertTrue("", proxies[index].getTimeStamp() > proxies[index + 1].getTimeStamp());
	}

	private void executeRefactoring(int index, int flags) throws CoreException {
		RefactoringHistoryService service= RefactoringHistoryService.getInstance();
		try {
			service.setOverrideTimeStamp((index + 1) * RefactoringHistoryServiceTests.STAMP_FACTOR);
			MockRefactoring refactoring= new MockRefactoring(fProject.getProject().getName(), "A mock description number " + index, "A mock comment number " + index, Collections.EMPTY_MAP, flags);
			PerformRefactoringOperation operation= new PerformRefactoringOperation(refactoring, CheckConditionsOperation.ALL_CONDITIONS);
			ResourcesPlugin.getWorkspace().run(operation, null);
		} finally {
			service.setOverrideTimeStamp(-1);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	protected void setUp() throws Exception {
		super.setUp();
		fProject= new SimpleTestProject();
		RefactoringHistoryService service= RefactoringHistoryService.getInstance();
		service.setSharedRefactoringHistory(fProject.getProject(), true, null);
		IFolder folder= fProject.getProject().getFolder(RefactoringHistoryService.NAME_HISTORY_FOLDER);
		assertTrue("Refactoring history folder should not exist.", !folder.exists());
		for (int index= 0; index < RefactoringHistoryServiceTests.NONE_NUMBER; index++)
			executeRefactoring(index, RefactoringDescriptor.NONE);
		for (int index= 0; index < RefactoringHistoryServiceTests.BREAKING_NUMBER; index++)
			executeRefactoring(index + RefactoringHistoryServiceTests.NONE_NUMBER, RefactoringDescriptor.BREAKING_CHANGE);
		for (int index= 0; index < RefactoringHistoryServiceTests.STRUCTURAL_NUMBER; index++)
			executeRefactoring(index + RefactoringHistoryServiceTests.NONE_NUMBER + RefactoringHistoryServiceTests.BREAKING_NUMBER, RefactoringDescriptor.STRUCTURAL_CHANGE);
		RefactoringHistory history= service.getProjectHistory(fProject.getProject(), null);
		assertEquals(NONE_NUMBER + BREAKING_NUMBER + STRUCTURAL_NUMBER, history.getDescriptors().length);
	}

	/**
	 * {@inheritDoc}
	 */
	protected void tearDown() throws Exception {
		fProject.delete();
		super.tearDown();
	}

	public void testReadProjectHistory0() throws Exception {
		RefactoringHistory history= RefactoringHistoryService.getInstance().getProjectHistory(fProject.getProject(), null);
	}

	public void testSortOrder0() throws Exception {
		RefactoringHistory history= RefactoringHistoryService.getInstance().getProjectHistory(fProject.getProject(), null);
		assertTrue("Refactoring history must not be empty", !history.isEmpty());
		RefactoringDescriptorProxy[] proxies= history.getDescriptors();
		assertEquals("Refactoring history has wrong size", NONE_NUMBER + BREAKING_NUMBER + STRUCTURAL_NUMBER, proxies.length);
		assertDescendingSortOrder(proxies);
	}

	public void testSortOrder1() throws Exception {
		RefactoringHistory history= RefactoringHistoryService.getInstance().getProjectHistory(fProject.getProject(), STAMP_FACTOR, STAMP_FACTOR * 5, RefactoringDescriptor.NONE, null);
		assertTrue("Refactoring history must not be empty", !history.isEmpty());
		RefactoringDescriptorProxy[] proxies= history.getDescriptors();
		assertEquals("Refactoring history has wrong size", 5, proxies.length);
		assertDescendingSortOrder(proxies);
	}

	public void testSortOrder2() throws Exception {
		RefactoringHistory history= RefactoringHistoryService.getInstance().getProjectHistory(fProject.getProject(), STAMP_FACTOR * 3, STAMP_FACTOR * 5, RefactoringDescriptor.NONE, null);
		assertTrue("Refactoring history must not be empty", !history.isEmpty());
		RefactoringDescriptorProxy[] proxies= history.getDescriptors();
		assertEquals("Refactoring history has wrong size", 3, proxies.length);
		assertDescendingSortOrder(proxies);
	}

	public void testSortOrder3() throws Exception {
		RefactoringHistory history= RefactoringHistoryService.getInstance().getProjectHistory(fProject.getProject(), STAMP_FACTOR * (NONE_NUMBER + 1), STAMP_FACTOR * (NONE_NUMBER + 4), RefactoringDescriptor.BREAKING_CHANGE, null);
		assertTrue("Refactoring history must not be empty", !history.isEmpty());
		RefactoringDescriptorProxy[] proxies= history.getDescriptors();
		assertEquals("Refactoring history has wrong size", 4, proxies.length);
		assertDescendingSortOrder(proxies);
	}

	public void testSortOrder4() throws Exception {
		RefactoringHistory history= RefactoringHistoryService.getInstance().getProjectHistory(fProject.getProject(), STAMP_FACTOR * (NONE_NUMBER + 1), STAMP_FACTOR * (NONE_NUMBER + 18), RefactoringDescriptor.NONE, null);
		assertTrue("Refactoring history must not be empty", !history.isEmpty());
		RefactoringDescriptorProxy[] proxies= history.getDescriptors();
		assertEquals("Refactoring history has wrong size", 18, proxies.length);
		assertDescendingSortOrder(proxies);
	}
}