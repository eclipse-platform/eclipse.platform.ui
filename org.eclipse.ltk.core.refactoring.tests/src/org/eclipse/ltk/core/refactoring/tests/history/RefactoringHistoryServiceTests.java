/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
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
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.osgi.service.prefs.BackingStoreException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
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
import org.eclipse.ltk.core.refactoring.tests.util.SimpleTestProject;
import org.eclipse.ltk.internal.core.refactoring.RefactoringPreferenceConstants;
import org.eclipse.ltk.internal.core.refactoring.history.RefactoringDescriptorProxyAdapter;
import org.eclipse.ltk.internal.core.refactoring.history.RefactoringHistoryImplementation;
import org.eclipse.ltk.internal.core.refactoring.history.RefactoringHistoryService;

public class RefactoringHistoryServiceTests extends TestCase {

	private static final class RefactoringExecutionListener implements IRefactoringExecutionListener {

		private RefactoringExecutionEvent fLastEvent= null;

		public void assertEventDescriptor(RefactoringDescriptorProxyAdapter expected) throws Exception {
			assertNotNull("No refactoring history event has been recorded", fLastEvent);
			RefactoringDescriptor expectedDescriptor= expected.requestDescriptor(null);
			assertNotNull("Could not resolve expected refactoring descriptor", expectedDescriptor);
			expectedDescriptor.setTimeStamp(fLastEvent.getDescriptor().getTimeStamp());
			assertEquals("Wrong refactoring descriptor proxy in refactoring history event:", expected, fLastEvent.getDescriptor());
			RefactoringDescriptor actualDescriptor= fLastEvent.getDescriptor().requestDescriptor(null);
			assertNotNull("Could not resolve actual refactoring descriptor", actualDescriptor);
			assertEquals("Resolved refactoring descriptors are not equal:", expectedDescriptor, actualDescriptor);
		}

		public void assertEventSource(IRefactoringHistoryService expected) throws Exception {
			assertNotNull("No refactoring history event has been recorded", fLastEvent);
			assertTrue("Wrong refactoring history service in refactoring history event:", expected == fLastEvent.getHistoryService());
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
			int previous= fLastEvent != null ? fLastEvent.getEventType() : -1;
			switch (event.getEventType()) {
				case RefactoringExecutionEvent.PERFORMED:
					assertTrue("Previous event should be ABOUT_TO_PERFORM", previous == RefactoringExecutionEvent.ABOUT_TO_PERFORM);
					break;
				case RefactoringExecutionEvent.REDONE:
					assertTrue("Previous event should be ABOUT_TO_REDO", previous == RefactoringExecutionEvent.ABOUT_TO_REDO);
					break;
				case RefactoringExecutionEvent.UNDONE:
					assertTrue("Previous event should be ABOUT_TO_UNDO", previous == RefactoringExecutionEvent.ABOUT_TO_UNDO);
					break;
			}
			fLastEvent= event;
		}
	}

	private static final class RefactoringHistoryListener implements IRefactoringHistoryListener {

		private RefactoringHistoryEvent fLastEvent= null;

		public void assertEventDescriptor(RefactoringDescriptorProxyAdapter expected) throws Exception {
			assertNotNull("No refactoring history event has been recorded", fLastEvent);
			RefactoringDescriptor expectedDescriptor= expected.requestDescriptor(null);
			assertNotNull("Could not resolve expected refactoring descriptor", expectedDescriptor);
			expectedDescriptor.setTimeStamp(fLastEvent.getDescriptor().getTimeStamp());
			assertEquals("Wrong refactoring descriptor proxy in refactoring history event:", expected, fLastEvent.getDescriptor());
			RefactoringDescriptor actualDescriptor= fLastEvent.getDescriptor().requestDescriptor(null);
			assertNotNull("Could not resolve actual refactoring descriptor", actualDescriptor);
			assertEquals("Resolved refactoring descriptors are not equal:", expectedDescriptor, actualDescriptor);
		}

		public void assertEventSource(IRefactoringHistoryService expected) throws Exception {
			assertNotNull("No refactoring history event has been recorded", fLastEvent);
			assertTrue("Wrong refactoring history service in refactoring history event:", expected == fLastEvent.getHistoryService());
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

	private static final int NONE_NUMBER= 10;

	private static final int STAMP_FACTOR= 10000000;

	private static final int STRUCTURAL_NUMBER= 10;

	private static final int TOTAL_PROJECT_NUMBER= NONE_NUMBER + BREAKING_NUMBER + STRUCTURAL_NUMBER + CUSTOM_NUMBER;

	private static final int TOTALZ_HISTORY_NUMBER= TOTAL_PROJECT_NUMBER + COMMON_NUMBER;

	private SimpleTestProject fProject;

	private void assertDescendingSortOrder(RefactoringDescriptorProxy[] proxies) {
		for (int index= 0; index < proxies.length - 1; index++)
			assertTrue("", proxies[index].getTimeStamp() > proxies[index + 1].getTimeStamp());
	}

	private RefactoringDescriptor executeRefactoring(String project, int index, int flags) throws CoreException {
		RefactoringHistoryService service= RefactoringHistoryService.getInstance();
		try {
			service.setOverrideTimeStamp((index + 1) * RefactoringHistoryServiceTests.STAMP_FACTOR);
			MockRefactoring refactoring= new MockRefactoring(project, "A mock description number " + index, "A mock comment number " + index, Collections.EMPTY_MAP, flags);
			RefactoringDescriptor descriptor= refactoring.createRefactoringDescriptor();
			PerformRefactoringOperation operation= new PerformRefactoringOperation(refactoring, CheckConditionsOperation.ALL_CONDITIONS);
			ResourcesPlugin.getWorkspace().run(operation, null);
			return descriptor;
		} finally {
			service.setOverrideTimeStamp(-1);
		}
	}

	private void setSharedRefactoringHistory(boolean shared) throws BackingStoreException, CoreException {
		final IEclipsePreferences preferences= new ProjectScope(fProject.getProject()).getNode(RefactoringCore.ID_PLUGIN);
		preferences.put(RefactoringPreferenceConstants.PREFERENCE_SHARED_REFACTORING_HISTORY, Boolean.toString(shared));
		preferences.flush();
		RefactoringHistoryService.setSharedRefactoringHistory(fProject.getProject(), shared, null);
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
		assertTrue("Refactoring history should be shared", RefactoringHistoryService.hasSharedRefactoringHistory(fProject.getProject()));
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

	public void testDeleteProjectHistory0() throws Exception {
		setUpWorkspaceRefactorings();
		final IProject project= fProject.getProject();
		final RefactoringHistoryService service= RefactoringHistoryService.getInstance();
		service.deleteRefactoringHistory(project, null);
		RefactoringHistory projectHistory= service.getProjectHistory(project, null);
		assertTrue("Refactoring history has wrong size:", projectHistory.getDescriptors().length == COMMON_NUMBER);
		RefactoringHistory workspaceHistory= service.getWorkspaceHistory(null);
		final RefactoringDescriptorProxy[] descriptors= workspaceHistory.getDescriptors();
		assertEquals("Refactoring history has wrong size:", COMMON_NUMBER, descriptors.length);
		for (int index= 0; index < descriptors.length; index++)
			assertTrue("Workspace refactoring should have no project attribute set:\n\n" + descriptors[index].toString(), descriptors[index].getProject() == null);
	}

	public void testDeleteProjectHistory1() throws Exception {
		setUpWorkspaceRefactorings();
		final IProject project= fProject.getProject();
		final RefactoringHistoryService service= RefactoringHistoryService.getInstance();
		RefactoringHistory workspaceHistory= service.getWorkspaceHistory(null);
		RefactoringDescriptorProxy[] descriptors= workspaceHistory.getDescriptors();
		Set set= new HashSet();
		for (int index= 0; index < descriptors.length; index++) {
			if (descriptors[index].getProject() == null)
				set.add(descriptors[index]);
		}
		service.deleteRefactoringDescriptors((RefactoringDescriptorProxy[]) set.toArray(new RefactoringDescriptorProxy[set.size()]), null);
		workspaceHistory= service.getWorkspaceHistory(null);
		RefactoringHistory projectHistory= service.getProjectHistory(project, null);
		assertEquals("Refactoring history should be the same:", projectHistory, workspaceHistory);
		service.deleteRefactoringHistory(project, null);
		projectHistory= service.getProjectHistory(project, null);
		assertTrue("Refactoring history should be empty", projectHistory.isEmpty());
		workspaceHistory= service.getWorkspaceHistory(null);
		assertTrue("Refactoring history should be empty", workspaceHistory.isEmpty());
	}

	public void testDeleteRefactoringDescriptors0() throws Exception {
		final IProject project= fProject.getProject();
		final RefactoringHistoryService service= RefactoringHistoryService.getInstance();
		RefactoringHistory projectHistory= service.getProjectHistory(project, null);
		assertTrue("Refactoring history should not be empty", !projectHistory.isEmpty());
		service.deleteRefactoringDescriptors(projectHistory.getDescriptors(), null);
		projectHistory= service.getProjectHistory(project, null);
		projectHistory= service.getProjectHistory(project, null);
		assertTrue("Refactoring history should be empty", projectHistory.isEmpty());
		RefactoringHistory workspaceHistory= service.getWorkspaceHistory(null);
		assertTrue("Refactoring history should be empty", workspaceHistory.isEmpty());
	}

	public void testDeleteRefactoringDescriptors1() throws Exception {
		final IProject project= fProject.getProject();
		final RefactoringHistoryService service= RefactoringHistoryService.getInstance();
		RefactoringHistory workspaceHistory= service.getWorkspaceHistory(null);
		RefactoringHistory projectHistory= service.getProjectHistory(project, 0, Long.MAX_VALUE, RefactoringDescriptor.BREAKING_CHANGE, null);
		assertTrue("Refactoring history should not be empty", !projectHistory.isEmpty());
		service.deleteRefactoringDescriptors(projectHistory.getDescriptors(), null);
		RefactoringHistory afterHistory= service.getWorkspaceHistory(null);
		assertEquals("", afterHistory.getDescriptors().length + BREAKING_NUMBER, workspaceHistory.getDescriptors().length);
	}

	public void testPopDescriptor0() throws Exception {
		final RefactoringHistoryService service= RefactoringHistoryService.getInstance();
		RefactoringHistory previousWorkspaceHistory= service.getWorkspaceHistory(null);
		RefactoringHistory previousProjectHistory= service.getProjectHistory(fProject.getProject(), null);
		RefactoringHistoryListener historyListener= new RefactoringHistoryListener();
		RefactoringExecutionListener executionListener= new RefactoringExecutionListener();
		try {
			historyListener.connect();
			executionListener.connect();
			RefactoringDescriptor descriptor= executeRefactoring(fProject.getProject().getName(), 1000000, CUSTOM_FLAG);
			historyListener.assertEventDescriptor(new RefactoringDescriptorProxyAdapter(descriptor));
			historyListener.assertEventSource(service);
			historyListener.assertEventType(RefactoringHistoryEvent.PUSHED);
			executionListener.assertEventDescriptor(new RefactoringDescriptorProxyAdapter(descriptor));
			executionListener.assertEventSource(service);
			executionListener.assertEventType(RefactoringExecutionEvent.PERFORMED);
			RefactoringHistory nextWorkspaceHistory= service.getWorkspaceHistory(null);
			RefactoringHistory nextProjectHistory= service.getProjectHistory(fProject.getProject(), null);
			assertNotSame("Refactoring history should not be the same:", previousProjectHistory, nextProjectHistory);
			assertNotSame("Refactoring history should not be the same:", previousWorkspaceHistory, nextWorkspaceHistory);
			assertEquals("Length of refactoring history should be one more:", previousProjectHistory.getDescriptors().length + 1, nextProjectHistory.getDescriptors().length);
			assertEquals("Length of refactoring history should be one more:", previousWorkspaceHistory.getDescriptors().length + 1, nextWorkspaceHistory.getDescriptors().length);
			assertEquals("Refactoring history should be the same:", nextProjectHistory.removeAll(new RefactoringHistoryImplementation(new RefactoringDescriptorProxyAdapter[] { new RefactoringDescriptorProxyAdapter(descriptor)})), previousProjectHistory);
			assertEquals("Refactoring history should be the same:", nextWorkspaceHistory.removeAll(new RefactoringHistoryImplementation(new RefactoringDescriptorProxyAdapter[] { new RefactoringDescriptorProxyAdapter(descriptor)})), previousWorkspaceHistory);
			RefactoringCore.getUndoManager().performUndo(null, null);
			historyListener.assertEventDescriptor(new RefactoringDescriptorProxyAdapter(descriptor));
			historyListener.assertEventSource(service);
			historyListener.assertEventType(RefactoringHistoryEvent.POPPED);
			executionListener.assertEventDescriptor(new RefactoringDescriptorProxyAdapter(descriptor));
			executionListener.assertEventSource(service);
			executionListener.assertEventType(RefactoringExecutionEvent.UNDONE);
			RefactoringCore.getUndoManager().performRedo(null, null);
			historyListener.assertEventDescriptor(new RefactoringDescriptorProxyAdapter(descriptor));
			historyListener.assertEventSource(service);
			historyListener.assertEventType(RefactoringHistoryEvent.PUSHED);
			executionListener.assertEventDescriptor(new RefactoringDescriptorProxyAdapter(descriptor));
			executionListener.assertEventSource(service);
			executionListener.assertEventType(RefactoringExecutionEvent.REDONE);
			nextWorkspaceHistory= service.getWorkspaceHistory(null);
			nextProjectHistory= service.getProjectHistory(fProject.getProject(), null);
			assertNotSame("Refactoring history should not be the same:", previousProjectHistory, nextProjectHistory);
			assertNotSame("Refactoring history should not be the same:", previousWorkspaceHistory, nextWorkspaceHistory);
			assertEquals("Length of refactoring history should be one more:", previousProjectHistory.getDescriptors().length + 1, nextProjectHistory.getDescriptors().length);
			assertEquals("Length of refactoring history should be one more:", previousWorkspaceHistory.getDescriptors().length + 1, nextWorkspaceHistory.getDescriptors().length);
			assertEquals("Refactoring history should be the same", nextProjectHistory.removeAll(new RefactoringHistoryImplementation(new RefactoringDescriptorProxyAdapter[] { new RefactoringDescriptorProxyAdapter(descriptor)})), previousProjectHistory);
			assertEquals("Refactoring history should be the same", nextWorkspaceHistory.removeAll(new RefactoringHistoryImplementation(new RefactoringDescriptorProxyAdapter[] { new RefactoringDescriptorProxyAdapter(descriptor)})), previousWorkspaceHistory);
		} finally {
			historyListener.disconnect();
			executionListener.disconnect();
		}
	}

	public void testPopDescriptor1() throws Exception {
		final RefactoringHistoryService service= RefactoringHistoryService.getInstance();
		RefactoringHistory previousWorkspaceHistory= service.getWorkspaceHistory(null);
		RefactoringHistory previousProjectHistory= service.getProjectHistory(fProject.getProject(), null);
		RefactoringHistoryListener historyListener= new RefactoringHistoryListener();
		RefactoringExecutionListener executionListener= new RefactoringExecutionListener();
		try {
			historyListener.connect();
			executionListener.connect();
			RefactoringDescriptor firstDescriptor= executeRefactoring(fProject.getProject().getName(), 1000000, CUSTOM_FLAG);
			historyListener.assertEventDescriptor(new RefactoringDescriptorProxyAdapter(firstDescriptor));
			historyListener.assertEventSource(service);
			historyListener.assertEventType(RefactoringHistoryEvent.PUSHED);
			executionListener.assertEventDescriptor(new RefactoringDescriptorProxyAdapter(firstDescriptor));
			executionListener.assertEventSource(service);
			executionListener.assertEventType(RefactoringExecutionEvent.PERFORMED);
			RefactoringDescriptor secondDescriptor= executeRefactoring(fProject.getProject().getName(), 1000001, RefactoringDescriptor.BREAKING_CHANGE);
			historyListener.assertEventDescriptor(new RefactoringDescriptorProxyAdapter(secondDescriptor));
			historyListener.assertEventSource(service);
			historyListener.assertEventType(RefactoringHistoryEvent.PUSHED);
			executionListener.assertEventDescriptor(new RefactoringDescriptorProxyAdapter(secondDescriptor));
			executionListener.assertEventSource(service);
			executionListener.assertEventType(RefactoringExecutionEvent.PERFORMED);
			RefactoringHistory nextWorkspaceHistory= service.getWorkspaceHistory(null);
			RefactoringHistory nextProjectHistory= service.getProjectHistory(fProject.getProject(), null);
			assertNotSame("Refactoring history should not be the same:", previousProjectHistory, nextProjectHistory);
			assertNotSame("Refactoring history should not be the same:", previousWorkspaceHistory, nextWorkspaceHistory);
			assertEquals("Length of refactoring history should be one more:", previousProjectHistory.getDescriptors().length + 2, nextProjectHistory.getDescriptors().length);
			assertEquals("Length of refactoring history should be one more:", previousWorkspaceHistory.getDescriptors().length + 2, nextWorkspaceHistory.getDescriptors().length);
			assertEquals("Refactoring history should be the same:", nextProjectHistory.removeAll(new RefactoringHistoryImplementation(new RefactoringDescriptorProxyAdapter[] { new RefactoringDescriptorProxyAdapter(firstDescriptor), new RefactoringDescriptorProxyAdapter(secondDescriptor)})), previousProjectHistory);
			assertEquals("Refactoring history should be the same:", nextWorkspaceHistory.removeAll(new RefactoringHistoryImplementation(new RefactoringDescriptorProxyAdapter[] { new RefactoringDescriptorProxyAdapter(firstDescriptor), new RefactoringDescriptorProxyAdapter(secondDescriptor)})), previousWorkspaceHistory);
			RefactoringCore.getUndoManager().performUndo(null, null);
			historyListener.assertEventDescriptor(new RefactoringDescriptorProxyAdapter(secondDescriptor));
			historyListener.assertEventSource(service);
			historyListener.assertEventType(RefactoringHistoryEvent.POPPED);
			executionListener.assertEventDescriptor(new RefactoringDescriptorProxyAdapter(secondDescriptor));
			executionListener.assertEventSource(service);
			executionListener.assertEventType(RefactoringExecutionEvent.UNDONE);
			RefactoringCore.getUndoManager().performUndo(null, null);
			historyListener.assertEventDescriptor(new RefactoringDescriptorProxyAdapter(firstDescriptor));
			historyListener.assertEventSource(service);
			historyListener.assertEventType(RefactoringHistoryEvent.POPPED);
			executionListener.assertEventDescriptor(new RefactoringDescriptorProxyAdapter(firstDescriptor));
			executionListener.assertEventSource(service);
			executionListener.assertEventType(RefactoringExecutionEvent.UNDONE);
			RefactoringCore.getUndoManager().performRedo(null, null);
			historyListener.assertEventDescriptor(new RefactoringDescriptorProxyAdapter(firstDescriptor));
			historyListener.assertEventSource(service);
			historyListener.assertEventType(RefactoringHistoryEvent.PUSHED);
			executionListener.assertEventDescriptor(new RefactoringDescriptorProxyAdapter(firstDescriptor));
			executionListener.assertEventSource(service);
			executionListener.assertEventType(RefactoringExecutionEvent.REDONE);
			RefactoringCore.getUndoManager().performRedo(null, null);
			historyListener.assertEventDescriptor(new RefactoringDescriptorProxyAdapter(secondDescriptor));
			historyListener.assertEventSource(service);
			historyListener.assertEventType(RefactoringHistoryEvent.PUSHED);
			executionListener.assertEventDescriptor(new RefactoringDescriptorProxyAdapter(secondDescriptor));
			executionListener.assertEventSource(service);
			executionListener.assertEventType(RefactoringExecutionEvent.REDONE);
			nextWorkspaceHistory= service.getWorkspaceHistory(null);
			nextProjectHistory= service.getProjectHistory(fProject.getProject(), null);
			assertNotSame("Refactoring history should not be the same:", previousProjectHistory, nextProjectHistory);
			assertNotSame("Refactoring history should not be the same:", previousWorkspaceHistory, nextWorkspaceHistory);
			assertEquals("Length of refactoring history should be one more:", previousProjectHistory.getDescriptors().length + 2, nextProjectHistory.getDescriptors().length);
			assertEquals("Length of refactoring history should be one more:", previousWorkspaceHistory.getDescriptors().length + 2, nextWorkspaceHistory.getDescriptors().length);
			assertEquals("Refactoring history should be the same", nextProjectHistory.removeAll(new RefactoringHistoryImplementation(new RefactoringDescriptorProxyAdapter[] { new RefactoringDescriptorProxyAdapter(firstDescriptor), new RefactoringDescriptorProxyAdapter(secondDescriptor)})), previousProjectHistory);
			assertEquals("Refactoring history should be the same", nextWorkspaceHistory.removeAll(new RefactoringHistoryImplementation(new RefactoringDescriptorProxyAdapter[] { new RefactoringDescriptorProxyAdapter(firstDescriptor), new RefactoringDescriptorProxyAdapter(secondDescriptor)})), previousWorkspaceHistory);
		} finally {
			historyListener.disconnect();
			executionListener.disconnect();
		}
	}

	public void testPushDescriptor0() throws Exception {
		final RefactoringHistoryService service= RefactoringHistoryService.getInstance();
		RefactoringHistory previousWorkspaceHistory= service.getWorkspaceHistory(null);
		RefactoringHistory previousProjectHistory= service.getProjectHistory(fProject.getProject(), null);
		RefactoringHistoryListener historyListener= new RefactoringHistoryListener();
		RefactoringExecutionListener executionListener= new RefactoringExecutionListener();
		try {
			historyListener.connect();
			executionListener.connect();
			RefactoringDescriptor descriptor= executeRefactoring(fProject.getProject().getName(), 1000000, CUSTOM_FLAG);
			historyListener.assertEventDescriptor(new RefactoringDescriptorProxyAdapter(descriptor));
			historyListener.assertEventSource(service);
			historyListener.assertEventType(RefactoringHistoryEvent.PUSHED);
			executionListener.assertEventDescriptor(new RefactoringDescriptorProxyAdapter(descriptor));
			executionListener.assertEventSource(service);
			executionListener.assertEventType(RefactoringExecutionEvent.PERFORMED);
			RefactoringHistory nextWorkspaceHistory= service.getWorkspaceHistory(null);
			RefactoringHistory nextProjectHistory= service.getProjectHistory(fProject.getProject(), null);
			assertNotSame("Refactoring history should not be the same:", previousProjectHistory, nextProjectHistory);
			assertNotSame("Refactoring history should not be the same:", previousWorkspaceHistory, nextWorkspaceHistory);
			assertEquals("Length of refactoring history should be one more:", previousProjectHistory.getDescriptors().length + 1, nextProjectHistory.getDescriptors().length);
			assertEquals("Length of refactoring history should be one more:", previousWorkspaceHistory.getDescriptors().length + 1, nextWorkspaceHistory.getDescriptors().length);
			assertEquals("Refactoring history should be the same:", nextProjectHistory.removeAll(new RefactoringHistoryImplementation(new RefactoringDescriptorProxyAdapter[] { new RefactoringDescriptorProxyAdapter(descriptor)})), previousProjectHistory);
			assertEquals("Refactoring history should be the same:", nextWorkspaceHistory.removeAll(new RefactoringHistoryImplementation(new RefactoringDescriptorProxyAdapter[] { new RefactoringDescriptorProxyAdapter(descriptor)})), previousWorkspaceHistory);
		} finally {
			historyListener.disconnect();
			executionListener.disconnect();
		}
	}

	public void testPushDescriptor1() throws Exception {
		final RefactoringHistoryService service= RefactoringHistoryService.getInstance();
		RefactoringHistory previousWorkspaceHistory= service.getWorkspaceHistory(null);
		RefactoringHistory previousProjectHistory= service.getProjectHistory(fProject.getProject(), null);
		RefactoringHistoryListener historyListener= new RefactoringHistoryListener();
		RefactoringExecutionListener executionListener= new RefactoringExecutionListener();
		try {
			historyListener.connect();
			executionListener.connect();
			RefactoringDescriptor descriptor= executeRefactoring(fProject.getProject().getName(), 1000000, RefactoringDescriptor.MULTI_CHANGE | RefactoringDescriptor.BREAKING_CHANGE);
			historyListener.assertEventDescriptor(new RefactoringDescriptorProxyAdapter(descriptor));
			historyListener.assertEventSource(service);
			historyListener.assertEventType(RefactoringHistoryEvent.PUSHED);
			executionListener.assertEventDescriptor(new RefactoringDescriptorProxyAdapter(descriptor));
			executionListener.assertEventSource(service);
			executionListener.assertEventType(RefactoringExecutionEvent.PERFORMED);
			RefactoringHistory nextWorkspaceHistory= service.getWorkspaceHistory(null);
			RefactoringHistory nextProjectHistory= service.getProjectHistory(fProject.getProject(), null);
			assertNotSame("Refactoring history should not be the same:", previousProjectHistory, nextProjectHistory);
			assertNotSame("Refactoring history should not be the same:", previousWorkspaceHistory, nextWorkspaceHistory);
			assertEquals("Length of refactoring history should be one more:", previousProjectHistory.getDescriptors().length + 1, nextProjectHistory.getDescriptors().length);
			assertEquals("Length of refactoring history should be one more:", previousWorkspaceHistory.getDescriptors().length + 1, nextWorkspaceHistory.getDescriptors().length);
			assertEquals("Refactoring history should be the same:", nextProjectHistory.removeAll(new RefactoringHistoryImplementation(new RefactoringDescriptorProxyAdapter[] { new RefactoringDescriptorProxyAdapter(descriptor)})), previousProjectHistory);
			assertEquals("Refactoring history should be the same:", nextWorkspaceHistory.removeAll(new RefactoringHistoryImplementation(new RefactoringDescriptorProxyAdapter[] { new RefactoringDescriptorProxyAdapter(descriptor)})), previousWorkspaceHistory);
		} finally {
			historyListener.disconnect();
			executionListener.disconnect();
		}
	}

	public void testPushDescriptor2() throws Exception {
		setUpWorkspaceRefactorings();
		final RefactoringHistoryService service= RefactoringHistoryService.getInstance();
		RefactoringHistory previousWorkspaceHistory= service.getWorkspaceHistory(null);
		RefactoringHistory previousProjectHistory= service.getProjectHistory(fProject.getProject(), 0, Long.MAX_VALUE, RefactoringDescriptor.NONE, null);
		RefactoringHistoryListener historyListener= new RefactoringHistoryListener();
		RefactoringExecutionListener executionListener= new RefactoringExecutionListener();
		try {
			historyListener.connect();
			executionListener.connect();
			RefactoringDescriptor descriptor= executeRefactoring(fProject.getProject().getName(), 1000000, RefactoringDescriptor.MULTI_CHANGE | RefactoringDescriptor.BREAKING_CHANGE);
			historyListener.assertEventDescriptor(new RefactoringDescriptorProxyAdapter(descriptor));
			historyListener.assertEventSource(service);
			historyListener.assertEventType(RefactoringHistoryEvent.PUSHED);
			executionListener.assertEventDescriptor(new RefactoringDescriptorProxyAdapter(descriptor));
			executionListener.assertEventSource(service);
			executionListener.assertEventType(RefactoringExecutionEvent.PERFORMED);
			RefactoringHistory nextWorkspaceHistory= service.getWorkspaceHistory(null);
			RefactoringHistory nextProjectHistory= service.getProjectHistory(fProject.getProject(), null);
			assertNotSame("Refactoring history should not be the same:", previousProjectHistory, nextProjectHistory);
			assertNotSame("Refactoring history should not be the same:", previousWorkspaceHistory, nextWorkspaceHistory);
			assertEquals("Length of refactoring history should be one more:", previousProjectHistory.getDescriptors().length + 1, nextProjectHistory.getDescriptors().length);
			assertEquals("Length of refactoring history should be one more:", previousWorkspaceHistory.getDescriptors().length + 1, nextWorkspaceHistory.getDescriptors().length);
			assertEquals("Refactoring history should be the same:", nextProjectHistory.removeAll(new RefactoringHistoryImplementation(new RefactoringDescriptorProxyAdapter[] { new RefactoringDescriptorProxyAdapter(descriptor)})), previousProjectHistory);
			assertEquals("Refactoring history should be the same:", nextWorkspaceHistory.removeAll(new RefactoringHistoryImplementation(new RefactoringDescriptorProxyAdapter[] { new RefactoringDescriptorProxyAdapter(descriptor)})), previousWorkspaceHistory);
		} finally {
			historyListener.disconnect();
			executionListener.disconnect();
		}
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

	public void testSharing0() throws Exception {
		final IProject project= fProject.getProject();
		final RefactoringHistoryService service= RefactoringHistoryService.getInstance();
		RefactoringHistory previousHistory= service.getProjectHistory(project, null);
		assertTrue("Refactoring history should be shared", RefactoringHistoryService.hasSharedRefactoringHistory(project));
		IFolder folder= fProject.getProject().getFolder(RefactoringHistoryService.NAME_HISTORY_FOLDER);
		assertTrue("Refactoring history folder should exist.", folder.exists());
		setSharedRefactoringHistory(false);
		RefactoringHistory nextHistory= service.getProjectHistory(project, null);
		assertEquals("Refactoring history should be the same:", previousHistory, nextHistory);
		assertFalse("Refactoring history should not be shared", RefactoringHistoryService.hasSharedRefactoringHistory(project));
		assertFalse("Refactoring history folder should not exist.", folder.exists());
	}

	public void testSharing1() throws Exception {
		final IProject project= fProject.getProject();
		final RefactoringHistoryService service= RefactoringHistoryService.getInstance();
		RefactoringHistory previousHistory= service.getProjectHistory(project, null);
		assertTrue("Refactoring history should be shared", RefactoringHistoryService.hasSharedRefactoringHistory(project));
		IFolder folder= fProject.getProject().getFolder(RefactoringHistoryService.NAME_HISTORY_FOLDER);
		assertTrue("Refactoring history folder should exist.", folder.exists());
		setSharedRefactoringHistory(false);
		RefactoringHistory nextHistory= service.getProjectHistory(project, null);
		assertEquals("Refactoring history should be the same:", previousHistory, nextHistory);
		assertFalse("Refactoring history should not be shared", RefactoringHistoryService.hasSharedRefactoringHistory(project));
		assertFalse("Refactoring history folder should not exist.", folder.exists());
		setSharedRefactoringHistory(true);
		RefactoringHistory lastHistory= service.getProjectHistory(project, null);
		assertEquals("Refactoring history should be the same:", previousHistory, lastHistory);
		assertEquals("Refactoring history should be the same:", nextHistory, lastHistory);
		assertTrue("Refactoring history should be shared", RefactoringHistoryService.hasSharedRefactoringHistory(project));
		assertTrue("Refactoring history folder should exist.", folder.exists());
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