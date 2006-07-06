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
import org.eclipse.core.runtime.preferences.IEclipsePreferences;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.ltk.core.refactoring.CheckConditionsOperation;
import org.eclipse.ltk.core.refactoring.PerformRefactoringOperation;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptorProxy;
import org.eclipse.ltk.core.refactoring.history.IRefactoringExecutionListener;
import org.eclipse.ltk.core.refactoring.history.IRefactoringHistoryListener;
import org.eclipse.ltk.core.refactoring.history.IRefactoringHistoryService;
import org.eclipse.ltk.core.refactoring.history.RefactoringExecutionEvent;
import org.eclipse.ltk.core.refactoring.history.RefactoringHistory;
import org.eclipse.ltk.core.refactoring.history.RefactoringHistoryEvent;

import org.eclipse.ltk.internal.core.refactoring.RefactoringPreferenceConstants;
import org.eclipse.ltk.internal.core.refactoring.history.RefactoringHistoryService;

import org.eclipse.ltk.core.refactoring.tests.util.SimpleTestProject;

import org.osgi.service.prefs.BackingStoreException;

public class RefactoringHistoryServiceTests extends TestCase {

	private static final class RefactoringExecutionListener implements IRefactoringExecutionListener {

		private RefactoringExecutionEvent fLastEvent= null;

		public void assertEventDescriptor(RefactoringDescriptorProxy expected) throws Exception {
			assertNotNull("No refactoring history event has been recorded", fLastEvent);
			assertEquals("Wrong refactoring descriptor proxy in refactoring history event:", expected, fLastEvent.getDescriptor());
			RefactoringDescriptor expectedDescriptor= expected.requestDescriptor(null);
			RefactoringDescriptor actualDescriptor= fLastEvent.getDescriptor().requestDescriptor(null);
			assertNotNull("Could not resolve expected refactoring descriptor", expectedDescriptor);
			assertNotNull("Could not resolve actual refactoring descriptor", actualDescriptor);
			assertEquals("Resolved refactoring descriptors are not equal:", expectedDescriptor, actualDescriptor);
		}

		public void assertEventSource(IRefactoringHistoryService expected) throws Exception {
			assertNotNull("No refactoring history event has been recorded", fLastEvent);
			assertFalse("Wrong refactoring history service in refactoring history event:", expected == fLastEvent.getHistoryService());
		}

		public void assertEventType(int expected) throws Exception {
			assertNotNull("No refactoring history event has been recorded", fLastEvent);
			assertEquals("Wrong refactoring history event type:", expected, fLastEvent.getEventType());
		}

		public void connect() {
			fLastEvent= null;
			RefactoringHistoryService.getInstance().addExecutionListener(this);
		}

		public void disconnect() {
			RefactoringHistoryService.getInstance().removeExecutionListener(this);
			fLastEvent= null;
		}

		/**
		 * {@inheritDoc}
		 */
		public void executionNotification(RefactoringExecutionEvent event) {
			fLastEvent= event;
		}
	}

	private static final class RefactoringHistoryListener implements IRefactoringHistoryListener {

		private RefactoringHistoryEvent fLastEvent= null;

		public void assertEventDescriptor(RefactoringDescriptorProxy expected) throws Exception {
			assertNotNull("No refactoring history event has been recorded", fLastEvent);
			assertEquals("Wrong refactoring descriptor proxy in refactoring history event:", expected, fLastEvent.getDescriptor());
			RefactoringDescriptor expectedDescriptor= expected.requestDescriptor(null);
			RefactoringDescriptor actualDescriptor= fLastEvent.getDescriptor().requestDescriptor(null);
			assertNotNull("Could not resolve expected refactoring descriptor", expectedDescriptor);
			assertNotNull("Could not resolve actual refactoring descriptor", actualDescriptor);
			assertEquals("Resolved refactoring descriptors are not equal:", expectedDescriptor, actualDescriptor);
		}

		public void assertEventSource(IRefactoringHistoryService expected) throws Exception {
			assertNotNull("No refactoring history event has been recorded", fLastEvent);
			assertFalse("Wrong refactoring history service in refactoring history event:", expected == fLastEvent.getHistoryService());
		}

		public void assertEventType(int expected) throws Exception {
			assertNotNull("No refactoring history event has been recorded", fLastEvent);
			assertEquals("Wrong refactoring history event type:", expected, fLastEvent.getEventType());
		}

		public void connect() {
			fLastEvent= null;
			RefactoringHistoryService.getInstance().addHistoryListener(this);
		}

		public void disconnect() {
			RefactoringHistoryService.getInstance().removeHistoryListener(this);
			fLastEvent= null;
		}

		/**
		 * {@inheritDoc}
		 */
		public void historyNotification(RefactoringHistoryEvent event) {
			fLastEvent= event;
		}
	}

	private static final int BREAKING_NUMBER= 20;

	private static final int COMMON_NUMBER= 20;

	private static final int CUSTOM_FLAG= 1 << 10;

	private static final int CUSTOM_NUMBER= 5;

	private static final int NONE_NUMBER= 100;

	private static final int STAMP_FACTOR= 10000000;

	private static final int STRUCTURAL_NUMBER= 10;

	private static final int TOTAL_PROJECT_NUMBER= NONE_NUMBER + BREAKING_NUMBER + STRUCTURAL_NUMBER + CUSTOM_NUMBER;

	private static final int TOTALZ_HISTORY_NUMBER= TOTAL_PROJECT_NUMBER + COMMON_NUMBER;

	private SimpleTestProject fProject;

	private void assertDescendingSortOrder(RefactoringDescriptorProxy[] proxies) {
		for (int index= 0; index < proxies.length - 1; index++)
			assertTrue("", proxies[index].getTimeStamp() > proxies[index + 1].getTimeStamp());
	}

	private void executeRefactoring(String project, int index, int flags) throws CoreException {
		RefactoringHistoryService service= RefactoringHistoryService.getInstance();
		try {
			service.setOverrideTimeStamp((index + 1) * RefactoringHistoryServiceTests.STAMP_FACTOR);
			MockRefactoring refactoring= new MockRefactoring(project, "A mock description number " + index, "A mock comment number " + index, Collections.EMPTY_MAP, flags);
			PerformRefactoringOperation operation= new PerformRefactoringOperation(refactoring, CheckConditionsOperation.ALL_CONDITIONS);
			ResourcesPlugin.getWorkspace().run(operation, null);
		} finally {
			service.setOverrideTimeStamp(-1);
		}
	}

	private void setSharedRefactoringHistory(boolean shared) throws BackingStoreException, CoreException {
		final IEclipsePreferences preferences= new ProjectScope(fProject.getProject()).getNode(RefactoringCore.ID_PLUGIN);
		preferences.put(RefactoringPreferenceConstants.PREFERENCE_SHARED_REFACTORING_HISTORY, Boolean.toString(shared));
		preferences.flush();
		RefactoringHistoryService.getInstance().setSharedRefactoringHistory(fProject.getProject(), true, null);
	}

	/**
	 * {@inheritDoc}
	 */
	protected void setUp() throws Exception {
		super.setUp();
		final RefactoringHistoryService service= RefactoringHistoryService.getInstance();
		service.connect();
		fProject= new SimpleTestProject();
		setSharedRefactoringHistory(true);
		assertTrue("Refactoring history should be shared", service.hasSharedRefactoringHistory(fProject.getProject()));
		IFolder folder= fProject.getProject().getFolder(RefactoringHistoryService.NAME_HISTORY_FOLDER);
		assertTrue("Refactoring history folder should not exist.", !folder.exists());
		setUpTestProjectRefactorings();
		assertTrue("Refactoring history folder should exist", folder.exists());
	}

	private void setUpTestProjectRefactorings() throws CoreException {
		final String name= fProject.getProject().getName();
		for (int index= 0; index < RefactoringHistoryServiceTests.NONE_NUMBER; index++)
			executeRefactoring(name, index, RefactoringDescriptor.NONE);
		for (int index= 0; index < RefactoringHistoryServiceTests.BREAKING_NUMBER; index++)
			executeRefactoring(name, index + RefactoringHistoryServiceTests.NONE_NUMBER, RefactoringDescriptor.BREAKING_CHANGE);
		for (int index= 0; index < RefactoringHistoryServiceTests.STRUCTURAL_NUMBER; index++)
			executeRefactoring(name, index + RefactoringHistoryServiceTests.NONE_NUMBER + RefactoringHistoryServiceTests.BREAKING_NUMBER, RefactoringDescriptor.STRUCTURAL_CHANGE);
		for (int index= 0; index < RefactoringHistoryServiceTests.CUSTOM_NUMBER; index++)
			executeRefactoring(name, index + RefactoringHistoryServiceTests.NONE_NUMBER + RefactoringHistoryServiceTests.BREAKING_NUMBER + RefactoringHistoryServiceTests.STRUCTURAL_NUMBER, CUSTOM_FLAG);
		RefactoringHistory history= RefactoringHistoryService.getInstance().getProjectHistory(fProject.getProject(), null);
		assertEquals(RefactoringHistoryServiceTests.TOTAL_PROJECT_NUMBER, history.getDescriptors().length);
	}

	private void setUpWorkspaceRefactorings() throws CoreException {
		for (int index= 0; index < RefactoringHistoryServiceTests.COMMON_NUMBER; index++)
			executeRefactoring(null, index + TOTAL_PROJECT_NUMBER, RefactoringDescriptor.BREAKING_CHANGE);
	}

	/**
	 * {@inheritDoc}
	 */
	protected void tearDown() throws Exception {
		final RefactoringHistoryService service= RefactoringHistoryService.getInstance();
		service.deleteRefactoringHistory(fProject.getProject(), null);
		RefactoringHistory history= service.getWorkspaceHistory(null);
		service.deleteRefactoringDescriptors(history.getDescriptors(), null);
		history= service.getWorkspaceHistory(null);
		assertTrue("Refactoring history must be empty", history.isEmpty());
		service.disconnect();
		fProject.delete();
		super.tearDown();
	}

	public void testPushDescriptor0() throws Exception {
		RefactoringHistory history= RefactoringHistoryService.getInstance().getProjectHistory(fProject.getProject(), null);

	}

	public void testReadProjectHistory0() throws Exception {
		RefactoringHistory history= RefactoringHistoryService.getInstance().getProjectHistory(fProject.getProject(), null);
		assertTrue("Refactoring history must not be empty", !history.isEmpty());
		RefactoringDescriptorProxy[] proxies= history.getDescriptors();
		assertEquals("Refactoring history has wrong size", RefactoringHistoryServiceTests.TOTAL_PROJECT_NUMBER, proxies.length);
	}

	public void testReadProjectHistory1() throws Exception {
		RefactoringHistory history= RefactoringHistoryService.getInstance().getProjectHistory(fProject.getProject(), 0, Long.MAX_VALUE, RefactoringDescriptor.NONE, null);
		assertTrue("Refactoring history must not be empty", !history.isEmpty());
		RefactoringDescriptorProxy[] proxies= history.getDescriptors();
		assertEquals("Refactoring history has wrong size", RefactoringHistoryServiceTests.TOTAL_PROJECT_NUMBER, proxies.length);
	}

	public void testReadProjectHistory2() throws Exception {
		RefactoringHistory history= RefactoringHistoryService.getInstance().getProjectHistory(fProject.getProject(), 0, Long.MAX_VALUE, RefactoringDescriptor.BREAKING_CHANGE, null);
		assertTrue("Refactoring history must not be empty", !history.isEmpty());
		RefactoringDescriptorProxy[] proxies= history.getDescriptors();
		assertEquals("Refactoring history has wrong size", BREAKING_NUMBER, proxies.length);
	}

	public void testReadProjectHistory3() throws Exception {
		RefactoringHistory history= RefactoringHistoryService.getInstance().getProjectHistory(fProject.getProject(), 0, Long.MAX_VALUE, RefactoringDescriptor.STRUCTURAL_CHANGE, null);
		assertTrue("Refactoring history must not be empty", !history.isEmpty());
		RefactoringDescriptorProxy[] proxies= history.getDescriptors();
		assertEquals("Refactoring history has wrong size", STRUCTURAL_NUMBER, proxies.length);
	}

	public void testReadProjectHistory4() throws Exception {
		RefactoringHistory history= RefactoringHistoryService.getInstance().getProjectHistory(fProject.getProject(), 0, Long.MAX_VALUE, RefactoringDescriptor.MULTI_CHANGE, null);
		assertTrue("Refactoring history should  be empty", history.isEmpty());
		RefactoringDescriptorProxy[] proxies= history.getDescriptors();
		assertEquals("Refactoring history has wrong size", 0, proxies.length);
	}

	public void testReadProjectHistory5() throws Exception {
		RefactoringHistory history= RefactoringHistoryService.getInstance().getProjectHistory(fProject.getProject(), 0, Long.MAX_VALUE, CUSTOM_FLAG, null);
		assertTrue("Refactoring history must not be empty", !history.isEmpty());
		RefactoringDescriptorProxy[] proxies= history.getDescriptors();
		assertEquals("Refactoring history has wrong size", CUSTOM_NUMBER, proxies.length);
	}

	public void testReadProjectHistory6() throws Exception {
		RefactoringHistory history= RefactoringHistoryService.getInstance().getProjectHistory(fProject.getProject(), 0, STAMP_FACTOR, CUSTOM_FLAG, null);
		assertTrue("Refactoring history should  be empty", history.isEmpty());
		RefactoringDescriptorProxy[] proxies= history.getDescriptors();
		assertEquals("Refactoring history has wrong size", 0, proxies.length);
	}

	public void testReadRefactoringHistory0() throws Exception {
		setUpWorkspaceRefactorings();
		RefactoringHistory history= RefactoringHistoryService.getInstance().getWorkspaceHistory(null);
		assertTrue("Refactoring history must not be empty", !history.isEmpty());
		RefactoringDescriptorProxy[] proxies= history.getDescriptors();
		assertEquals("Refactoring history has wrong size", RefactoringHistoryServiceTests.TOTALZ_HISTORY_NUMBER, proxies.length);
	}

	public void testReadRefactoringHistory1() throws Exception {
		setUpWorkspaceRefactorings();
		RefactoringHistory history= RefactoringHistoryService.getInstance().getWorkspaceHistory(0, Long.MAX_VALUE, null);
		assertTrue("Refactoring history must not be empty", !history.isEmpty());
		RefactoringDescriptorProxy[] proxies= history.getDescriptors();
		assertEquals("Refactoring history has wrong size", RefactoringHistoryServiceTests.TOTALZ_HISTORY_NUMBER, proxies.length);
	}

	public void testReadWorkspaceHistory0() throws Exception {
		RefactoringHistory history= RefactoringHistoryService.getInstance().getWorkspaceHistory(0, STAMP_FACTOR, null);
		assertTrue("Refactoring history should  be empty", !history.isEmpty());
		RefactoringDescriptorProxy[] proxies= history.getDescriptors();
		assertEquals("Refactoring history has wrong size", 1, proxies.length);
	}

	public void testReadWorkspaceHistory1() throws Exception {
		RefactoringHistory history= RefactoringHistoryService.getInstance().getWorkspaceHistory(0, Long.MAX_VALUE, null);
		assertTrue("Refactoring history should  be empty", !history.isEmpty());
		RefactoringDescriptorProxy[] proxies= history.getDescriptors();
		assertEquals("Refactoring history has wrong size", TOTAL_PROJECT_NUMBER, proxies.length);
	}

	public void testReadWorkspaceHistory2() throws Exception {
		RefactoringHistory history= RefactoringHistoryService.getInstance().getWorkspaceHistory(STAMP_FACTOR, STAMP_FACTOR * 5, null);
		assertTrue("Refactoring history should  be empty", !history.isEmpty());
		RefactoringDescriptorProxy[] proxies= history.getDescriptors();
		assertEquals("Refactoring history has wrong size", 5, proxies.length);
	}

	public void testReadWorkspaceHistory3() throws Exception {
		RefactoringHistory history= RefactoringHistoryService.getInstance().getWorkspaceHistory(STAMP_FACTOR * 3, STAMP_FACTOR * 5, null);
		assertTrue("Refactoring history should  be empty", !history.isEmpty());
		RefactoringDescriptorProxy[] proxies= history.getDescriptors();
		assertEquals("Refactoring history has wrong size", 3, proxies.length);
	}

	public void testSortOrder0() throws Exception {
		RefactoringHistory history= RefactoringHistoryService.getInstance().getProjectHistory(fProject.getProject(), null);
		assertTrue("Refactoring history must not be empty", !history.isEmpty());
		RefactoringDescriptorProxy[] proxies= history.getDescriptors();
		assertEquals("Refactoring history has wrong size", RefactoringHistoryServiceTests.TOTAL_PROJECT_NUMBER, proxies.length);
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

	public void testSortOrder5() throws Exception {
		RefactoringHistory history= RefactoringHistoryService.getInstance().getProjectHistory(fProject.getProject(), 0, Long.MAX_VALUE, CUSTOM_FLAG, null);
		assertTrue("Refactoring history must not be empty", !history.isEmpty());
		RefactoringDescriptorProxy[] proxies= history.getDescriptors();
		assertEquals("Refactoring history has wrong size", CUSTOM_NUMBER, proxies.length);
		assertDescendingSortOrder(proxies);
	}

	public void testSortOrder6() throws Exception {
		RefactoringHistory history= RefactoringHistoryService.getInstance().getWorkspaceHistory(0, Long.MAX_VALUE, null);
		assertTrue("Refactoring history must not be empty", !history.isEmpty());
		RefactoringDescriptorProxy[] proxies= history.getDescriptors();
		assertEquals("Refactoring history has wrong size", TOTAL_PROJECT_NUMBER, proxies.length);
		assertDescendingSortOrder(proxies);
	}

	public void testSortOrder7() throws Exception {
		RefactoringHistory history= RefactoringHistoryService.getInstance().getWorkspaceHistory(STAMP_FACTOR * 3, STAMP_FACTOR * 5, null);
		assertTrue("Refactoring history must not be empty", !history.isEmpty());
		RefactoringDescriptorProxy[] proxies= history.getDescriptors();
		assertEquals("Refactoring history has wrong size", 3, proxies.length);
		assertDescendingSortOrder(proxies);
	}

}