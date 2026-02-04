/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
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
package org.eclipse.ltk.core.refactoring.tests.history;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

public class RefactoringHistoryServiceTests {
	private static final class RefactoringExecutionListener implements IRefactoringExecutionListener {
		private RefactoringExecutionEvent fLastEvent= null;

		public void assertEventDescriptor(RefactoringDescriptorProxyAdapter expected) throws Exception {
			assertNotNull(fLastEvent, "No refactoring history event has been recorded");
			RefactoringDescriptor expectedDescriptor= expected.requestDescriptor(null);
			assertNotNull(expectedDescriptor, "Could not resolve expected refactoring descriptor");
			expectedDescriptor.setTimeStamp(fLastEvent.getDescriptor().getTimeStamp());
			assertEquals(expected, fLastEvent.getDescriptor(), "Wrong refactoring descriptor proxy in refactoring history event:");
			RefactoringDescriptor actualDescriptor= fLastEvent.getDescriptor().requestDescriptor(null);
			assertNotNull(actualDescriptor, "Could not resolve actual refactoring descriptor");
			assertEquals(expectedDescriptor, actualDescriptor, "Resolved refactoring descriptors are not equal:");
		}

		public void assertEventSource(IRefactoringHistoryService expected) throws Exception {
			assertNotNull(fLastEvent, "No refactoring history event has been recorded");
			assertSame(expected, fLastEvent.getHistoryService(), "Wrong refactoring history service in refactoring history event:");
		}

		public void assertEventType(int expected) throws Exception {
			assertNotNull(fLastEvent, "No refactoring history event has been recorded");
			assertEquals(expected, fLastEvent.getEventType(), "Wrong refactoring history event type:");
		}

		public void connect() {
			fLastEvent= null;
			RefactoringHistoryService.getInstance().addExecutionListener(this);
		}

		public void disconnect() {
			RefactoringHistoryService.getInstance().removeExecutionListener(this);
			fLastEvent= null;
		}

		@SuppressWarnings("incomplete-switch")
		@Override
		public void executionNotification(RefactoringExecutionEvent event) {
			int previous= fLastEvent != null ? fLastEvent.getEventType() : -1;
			switch (event.getEventType()) {
				case RefactoringExecutionEvent.PERFORMED:
					assertEquals(RefactoringExecutionEvent.ABOUT_TO_PERFORM, previous, "Previous event should be ABOUT_TO_PERFORM");
					break;
				case RefactoringExecutionEvent.REDONE:
					assertEquals(RefactoringExecutionEvent.ABOUT_TO_REDO, previous, "Previous event should be ABOUT_TO_REDO");
					break;
				case RefactoringExecutionEvent.UNDONE:
					assertEquals(RefactoringExecutionEvent.ABOUT_TO_UNDO, previous, "Previous event should be ABOUT_TO_UNDO");
					break;
			}
			fLastEvent= event;
		}
	}

	private static final class RefactoringHistoryListener implements IRefactoringHistoryListener {

		private RefactoringHistoryEvent fLastEvent= null;

		public void assertEventDescriptor(RefactoringDescriptorProxyAdapter expected) throws Exception {
			assertNotNull(fLastEvent, "No refactoring history event has been recorded");
			RefactoringDescriptor expectedDescriptor= expected.requestDescriptor(null);
			assertNotNull(expectedDescriptor, "Could not resolve expected refactoring descriptor");
			expectedDescriptor.setTimeStamp(fLastEvent.getDescriptor().getTimeStamp());
			assertEquals(expected, fLastEvent.getDescriptor(), "Wrong refactoring descriptor proxy in refactoring history event:");
			RefactoringDescriptor actualDescriptor= fLastEvent.getDescriptor().requestDescriptor(null);
			assertNotNull(actualDescriptor, "Could not resolve actual refactoring descriptor");
			assertEquals(expectedDescriptor, actualDescriptor, "Resolved refactoring descriptors are not equal:");
		}

		public void assertEventSource(IRefactoringHistoryService expected) throws Exception {
			assertNotNull(fLastEvent, "No refactoring history event has been recorded");
			assertSame(expected, fLastEvent.getHistoryService(), "Wrong refactoring history service in refactoring history event:");
		}

		public void assertEventType(int expected) throws Exception {
			assertNotNull(fLastEvent, "No refactoring history event has been recorded");
			assertEquals(expected, fLastEvent.getEventType(), "Wrong refactoring history event type:");
		}

		public void connect() {
			fLastEvent= null;
			RefactoringHistoryService.getInstance().addHistoryListener(this);
		}

		public void disconnect() {
			RefactoringHistoryService.getInstance().removeHistoryListener(this);
			fLastEvent= null;
		}

		@Override
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
			assertTrue(proxies[index].getTimeStamp() > proxies[index + 1].getTimeStamp(), "");
	}

	private RefactoringDescriptor executeRefactoring(String project, int index, int flags) throws CoreException {
		RefactoringHistoryService service= RefactoringHistoryService.getInstance();
		try {
			service.setOverrideTimeStamp((index + 1) * RefactoringHistoryServiceTests.STAMP_FACTOR);
			MockRefactoring refactoring= new MockRefactoring(project, "A mock description number " + index, "A mock comment number " + index, Collections.<String, String> emptyMap(), flags);
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

	@BeforeEach
	public void setUp() throws Exception {
		final RefactoringHistoryService service= RefactoringHistoryService.getInstance();
		service.connect();
		fProject= new SimpleTestProject();
		RefactoringHistory history= service.getWorkspaceHistory(null);
		service.deleteRefactoringDescriptors(history.getDescriptors(), null);
		setSharedRefactoringHistory(true);
		assertTrue(RefactoringHistoryService.hasSharedRefactoringHistory(fProject.getProject()), "Refactoring history should be shared");
		IFolder folder= fProject.getProject().getFolder(RefactoringHistoryService.NAME_HISTORY_FOLDER);
		assertFalse(folder.exists(), "Refactoring history folder should not exist.");
		service.deleteRefactoringHistory(fProject.getProject(), null);
		setUpTestProjectRefactorings();
		assertTrue(folder.exists(), "Refactoring history folder should exist");
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

	@AfterEach
	public void tearDown() throws Exception {
		final RefactoringHistoryService service= RefactoringHistoryService.getInstance();
		service.deleteRefactoringHistory(fProject.getProject(), null);
		RefactoringHistory history= service.getWorkspaceHistory(null);
		service.deleteRefactoringDescriptors(history.getDescriptors(), null);
		history= service.getWorkspaceHistory(null);
		assertTrue(history.isEmpty(), "Refactoring history must be empty");
		service.disconnect();
		fProject.delete();
	}

	@Test
	public void testDeleteProjectHistory0() throws Exception {
		setUpWorkspaceRefactorings();
		final IProject project= fProject.getProject();
		final RefactoringHistoryService service= RefactoringHistoryService.getInstance();
		service.deleteRefactoringHistory(project, null);
		RefactoringHistory projectHistory= service.getProjectHistory(project, null);
		assertEquals(COMMON_NUMBER, projectHistory.getDescriptors().length, "Refactoring history has wrong size:");
		RefactoringHistory workspaceHistory= service.getWorkspaceHistory(null);
		final RefactoringDescriptorProxy[] descriptors= workspaceHistory.getDescriptors();
		assertEquals(COMMON_NUMBER, descriptors.length, "Refactoring history has wrong size:");
		for (RefactoringDescriptorProxy descriptor : descriptors) {
			assertNull(descriptor.getProject(), "Workspace refactoring should have no project attribute set:\n\n" + descriptor.toString());
		}
	}

	@Test
	public void testDeleteProjectHistory1() throws Exception {
		setUpWorkspaceRefactorings();
		final IProject project= fProject.getProject();
		final RefactoringHistoryService service= RefactoringHistoryService.getInstance();
		RefactoringHistory workspaceHistory= service.getWorkspaceHistory(null);
		Set<RefactoringDescriptorProxy> set= new HashSet<>();
		for (RefactoringDescriptorProxy descriptor : workspaceHistory.getDescriptors()) {
			if (descriptor.getProject() == null) {
				set.add(descriptor);
			}
		}
		service.deleteRefactoringDescriptors(set.toArray(new RefactoringDescriptorProxy[set.size()]), null);
		workspaceHistory= service.getWorkspaceHistory(null);
		RefactoringHistory projectHistory= service.getProjectHistory(project, null);
		assertEquals(projectHistory, workspaceHistory, "Refactoring history should be the same:");
		service.deleteRefactoringHistory(project, null);
		projectHistory= service.getProjectHistory(project, null);
		assertTrue(projectHistory.isEmpty(), "Refactoring history should be empty");
		workspaceHistory= service.getWorkspaceHistory(null);
		assertTrue(workspaceHistory.isEmpty(), "Refactoring history should be empty");
	}

	@Test
	public void testDeleteRefactoringDescriptors0() throws Exception {
		final IProject project= fProject.getProject();
		final RefactoringHistoryService service= RefactoringHistoryService.getInstance();
		RefactoringHistory projectHistory= service.getProjectHistory(project, null);
		assertFalse(projectHistory.isEmpty(), "Refactoring history should not be empty");
		service.deleteRefactoringDescriptors(projectHistory.getDescriptors(), null);
		projectHistory= service.getProjectHistory(project, null);
		projectHistory= service.getProjectHistory(project, null);
		assertTrue(projectHistory.isEmpty(), "Refactoring history should be empty");
		RefactoringHistory workspaceHistory= service.getWorkspaceHistory(null);
		assertTrue(workspaceHistory.isEmpty(), "Refactoring history should be empty");
	}

	@Test
	public void testDeleteRefactoringDescriptors1() throws Exception {
		final IProject project= fProject.getProject();
		final RefactoringHistoryService service= RefactoringHistoryService.getInstance();
		RefactoringHistory workspaceHistory= service.getWorkspaceHistory(null);
		RefactoringHistory projectHistory= service.getProjectHistory(project, 0, Long.MAX_VALUE, RefactoringDescriptor.BREAKING_CHANGE, null);
		assertFalse(projectHistory.isEmpty(), "Refactoring history should not be empty");
		service.deleteRefactoringDescriptors(projectHistory.getDescriptors(), null);
		RefactoringHistory afterHistory= service.getWorkspaceHistory(null);
		assertEquals(afterHistory.getDescriptors().length + BREAKING_NUMBER, workspaceHistory.getDescriptors().length, "");
	}

	@Test
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
			assertNotSame(previousProjectHistory, nextProjectHistory, "Refactoring history should not be the same:");
			assertNotSame(previousWorkspaceHistory, nextWorkspaceHistory, "Refactoring history should not be the same:");
			assertEquals(previousProjectHistory.getDescriptors().length + 1, nextProjectHistory.getDescriptors().length, "Length of refactoring history should be one more:");
			            assertEquals(previousWorkspaceHistory.getDescriptors().length + 1, nextWorkspaceHistory.getDescriptors().length, "Length of refactoring history should be one more:");
			            assertEquals(previousProjectHistory, nextProjectHistory.removeAll(new RefactoringHistoryImplementation(new RefactoringDescriptorProxyAdapter[] { new RefactoringDescriptorProxyAdapter(descriptor)})), "Refactoring history should be the same:");
			            assertEquals(previousWorkspaceHistory, nextWorkspaceHistory.removeAll(new RefactoringHistoryImplementation(new RefactoringDescriptorProxyAdapter[] { new RefactoringDescriptorProxyAdapter(descriptor)})), "Refactoring history should be the same:");
			            RefactoringCore.getUndoManager().performUndo(null, null);			historyListener.assertEventDescriptor(new RefactoringDescriptorProxyAdapter(descriptor));
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
			assertNotSame(previousProjectHistory, nextProjectHistory, "Refactoring history should not be the same:");
			assertNotSame(previousWorkspaceHistory, nextWorkspaceHistory, "Refactoring history should not be the same:");
			assertEquals(previousProjectHistory.getDescriptors().length + 1, nextProjectHistory.getDescriptors().length, "Length of refactoring history should be one more:");
			assertEquals(previousWorkspaceHistory.getDescriptors().length + 1, nextWorkspaceHistory.getDescriptors().length, "Length of refactoring history should be one more:");
			assertEquals(previousProjectHistory, nextProjectHistory.removeAll(new RefactoringHistoryImplementation(new RefactoringDescriptorProxyAdapter[] { new RefactoringDescriptorProxyAdapter(descriptor)})), "Refactoring history should be the same");
			assertEquals(previousWorkspaceHistory, nextWorkspaceHistory.removeAll(new RefactoringHistoryImplementation(new RefactoringDescriptorProxyAdapter[] { new RefactoringDescriptorProxyAdapter(descriptor)})), "Refactoring history should be the same");
		} finally {
			historyListener.disconnect();
			executionListener.disconnect();
		}
	}

	@Test
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
			assertNotSame(previousProjectHistory, nextProjectHistory, "Refactoring history should not be the same:");
			assertNotSame(previousWorkspaceHistory, nextWorkspaceHistory, "Refactoring history should not be the same:");
			assertEquals(previousProjectHistory.getDescriptors().length + 2, nextProjectHistory.getDescriptors().length, "Length of refactoring history should be one more:");
			assertEquals(previousWorkspaceHistory.getDescriptors().length + 2, nextWorkspaceHistory.getDescriptors().length, "Length of refactoring history should be one more:");
			assertEquals(previousProjectHistory, nextProjectHistory.removeAll(new RefactoringHistoryImplementation(new RefactoringDescriptorProxyAdapter[] { new RefactoringDescriptorProxyAdapter(firstDescriptor), new RefactoringDescriptorProxyAdapter(secondDescriptor)})), "Refactoring history should be the same:");
			assertEquals(previousWorkspaceHistory, nextWorkspaceHistory.removeAll(new RefactoringHistoryImplementation(new RefactoringDescriptorProxyAdapter[] { new RefactoringDescriptorProxyAdapter(firstDescriptor), new RefactoringDescriptorProxyAdapter(secondDescriptor)})), "Refactoring history should be the same:");
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
			assertNotSame(previousProjectHistory, nextProjectHistory, "Refactoring history should not be the same:");
			assertNotSame(previousWorkspaceHistory, nextWorkspaceHistory, "Refactoring history should not be the same:");
			assertEquals(previousProjectHistory.getDescriptors().length + 2, nextProjectHistory.getDescriptors().length, "Length of refactoring history should be one more:");
			assertEquals(previousWorkspaceHistory.getDescriptors().length + 2, nextWorkspaceHistory.getDescriptors().length, "Length of refactoring history should be one more:");
			assertEquals(previousProjectHistory, nextProjectHistory.removeAll(new RefactoringHistoryImplementation(new RefactoringDescriptorProxyAdapter[] { new RefactoringDescriptorProxyAdapter(firstDescriptor), new RefactoringDescriptorProxyAdapter(secondDescriptor)})), "Refactoring history should be the same");
			assertEquals(previousWorkspaceHistory, nextWorkspaceHistory.removeAll(new RefactoringHistoryImplementation(new RefactoringDescriptorProxyAdapter[] { new RefactoringDescriptorProxyAdapter(firstDescriptor), new RefactoringDescriptorProxyAdapter(secondDescriptor)})), "Refactoring history should be the same");
		} finally {
			historyListener.disconnect();
			executionListener.disconnect();
		}
	}

	@Test
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
			assertNotSame(previousProjectHistory, nextProjectHistory, "Refactoring history should not be the same:");
			assertNotSame(previousWorkspaceHistory, nextWorkspaceHistory, "Refactoring history should not be the same:");
			assertEquals(previousProjectHistory.getDescriptors().length + 1, nextProjectHistory.getDescriptors().length, "Length of refactoring history should be one more:");
			assertEquals(previousWorkspaceHistory.getDescriptors().length + 1, nextWorkspaceHistory.getDescriptors().length, "Length of refactoring history should be one more:");
			assertEquals(previousProjectHistory, nextProjectHistory.removeAll(new RefactoringHistoryImplementation(new RefactoringDescriptorProxyAdapter[] { new RefactoringDescriptorProxyAdapter(descriptor)})), "Refactoring history should be the same:");
			assertEquals(previousWorkspaceHistory, nextWorkspaceHistory.removeAll(new RefactoringHistoryImplementation(new RefactoringDescriptorProxyAdapter[] { new RefactoringDescriptorProxyAdapter(descriptor)})), "Refactoring history should be the same:");
		} finally {
			historyListener.disconnect();
			executionListener.disconnect();
		}
	}

	@Test
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
			assertNotSame(previousProjectHistory, nextProjectHistory, "Refactoring history should not be the same:");
			assertNotSame(previousWorkspaceHistory, nextWorkspaceHistory, "Refactoring history should not be the same:");
			assertEquals(previousProjectHistory.getDescriptors().length + 1, nextProjectHistory.getDescriptors().length, "Length of refactoring history should be one more:");
			assertEquals(previousWorkspaceHistory.getDescriptors().length + 1, nextWorkspaceHistory.getDescriptors().length, "Length of refactoring history should be one more:");
			assertEquals(previousProjectHistory, nextProjectHistory.removeAll(new RefactoringHistoryImplementation(new RefactoringDescriptorProxyAdapter[] { new RefactoringDescriptorProxyAdapter(descriptor)})), "Refactoring history should be the same:");
			assertEquals(previousWorkspaceHistory, nextWorkspaceHistory.removeAll(new RefactoringHistoryImplementation(new RefactoringDescriptorProxyAdapter[] { new RefactoringDescriptorProxyAdapter(descriptor)})), "Refactoring history should be the same:");
		} finally {
			historyListener.disconnect();
			executionListener.disconnect();
		}
	}

	@Test
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
			assertNotSame(previousProjectHistory, nextProjectHistory, "Refactoring history should not be the same:");
			assertNotSame(previousWorkspaceHistory, nextWorkspaceHistory, "Refactoring history should not be the same:");
			assertEquals(previousProjectHistory.getDescriptors().length + 1, nextProjectHistory.getDescriptors().length, "Length of refactoring history should be one more:");
			assertEquals(previousWorkspaceHistory.getDescriptors().length + 1, nextWorkspaceHistory.getDescriptors().length, "Length of refactoring history should be one more:");
			assertEquals(previousProjectHistory, nextProjectHistory.removeAll(new RefactoringHistoryImplementation(new RefactoringDescriptorProxyAdapter[] { new RefactoringDescriptorProxyAdapter(descriptor)})), "Refactoring history should be the same:");
			assertEquals(previousWorkspaceHistory, nextWorkspaceHistory.removeAll(new RefactoringHistoryImplementation(new RefactoringDescriptorProxyAdapter[] { new RefactoringDescriptorProxyAdapter(descriptor)})), "Refactoring history should be the same:");
		} finally {
			historyListener.disconnect();
			executionListener.disconnect();
		}
	}

	@Test
	public void testReadProjectHistory0() throws Exception {
		RefactoringHistory history= RefactoringHistoryService.getInstance().getProjectHistory(fProject.getProject(), null);
		assertFalse(history.isEmpty(), "Refactoring history must not be empty");
		RefactoringDescriptorProxy[] proxies= history.getDescriptors();
		assertEquals(RefactoringHistoryServiceTests.TOTAL_PROJECT_NUMBER, proxies.length, "Refactoring history has wrong size");
	}

	@Test
	public void testReadProjectHistory1() throws Exception {
		RefactoringHistory history= RefactoringHistoryService.getInstance().getProjectHistory(fProject.getProject(), 0, Long.MAX_VALUE, RefactoringDescriptor.NONE, null);
		assertFalse(history.isEmpty(), "Refactoring history must not be empty");
		RefactoringDescriptorProxy[] proxies= history.getDescriptors();
		assertEquals(RefactoringHistoryServiceTests.TOTAL_PROJECT_NUMBER, proxies.length, "Refactoring history has wrong size");
	}

	@Test
	public void testReadProjectHistory2() throws Exception {
		RefactoringHistory history= RefactoringHistoryService.getInstance().getProjectHistory(fProject.getProject(), 0, Long.MAX_VALUE, RefactoringDescriptor.BREAKING_CHANGE, null);
		assertFalse(history.isEmpty(), "Refactoring history must not be empty");
		RefactoringDescriptorProxy[] proxies= history.getDescriptors();
		assertEquals(BREAKING_NUMBER, proxies.length, "Refactoring history has wrong size");
	}

	@Test
	public void testReadProjectHistory3() throws Exception {
		RefactoringHistory history= RefactoringHistoryService.getInstance().getProjectHistory(fProject.getProject(), 0, Long.MAX_VALUE, RefactoringDescriptor.STRUCTURAL_CHANGE, null);
		assertFalse(history.isEmpty(), "Refactoring history must not be empty");
		RefactoringDescriptorProxy[] proxies= history.getDescriptors();
		assertEquals(STRUCTURAL_NUMBER, proxies.length, "Refactoring history has wrong size");
	}

	@Test
	public void testReadProjectHistory4() throws Exception {
		RefactoringHistory history= RefactoringHistoryService.getInstance().getProjectHistory(fProject.getProject(), 0, Long.MAX_VALUE, RefactoringDescriptor.MULTI_CHANGE, null);
		assertTrue(history.isEmpty(), "Refactoring history should  be empty");
		RefactoringDescriptorProxy[] proxies= history.getDescriptors();
		assertEquals(0, proxies.length, "Refactoring history has wrong size");
	}

	@Test
	public void testReadProjectHistory5() throws Exception {
		RefactoringHistory history= RefactoringHistoryService.getInstance().getProjectHistory(fProject.getProject(), 0, Long.MAX_VALUE, CUSTOM_FLAG, null);
		assertFalse(history.isEmpty(), "Refactoring history must not be empty");
		RefactoringDescriptorProxy[] proxies= history.getDescriptors();
		assertEquals(CUSTOM_NUMBER, proxies.length, "Refactoring history has wrong size");
	}

	@Test
	public void testReadProjectHistory6() throws Exception {
		RefactoringHistory history= RefactoringHistoryService.getInstance().getProjectHistory(fProject.getProject(), 0, STAMP_FACTOR, CUSTOM_FLAG, null);
		assertTrue(history.isEmpty(), "Refactoring history should  be empty");
		RefactoringDescriptorProxy[] proxies= history.getDescriptors();
		assertEquals(0, proxies.length, "Refactoring history has wrong size");
	}

	@Test
	public void testReadRefactoringHistory0() throws Exception {
		setUpWorkspaceRefactorings();
		RefactoringHistory history= RefactoringHistoryService.getInstance().getWorkspaceHistory(null);
		assertFalse(history.isEmpty(), "Refactoring history must not be empty");
		RefactoringDescriptorProxy[] proxies= history.getDescriptors();
		assertEquals(RefactoringHistoryServiceTests.TOTALZ_HISTORY_NUMBER, proxies.length, "Refactoring history has wrong size");
	}

	@Test
	public void testReadRefactoringHistory1() throws Exception {
		setUpWorkspaceRefactorings();
		RefactoringHistory history= RefactoringHistoryService.getInstance().getWorkspaceHistory(0, Long.MAX_VALUE, null);
		assertFalse(history.isEmpty(), "Refactoring history must not be empty");
		RefactoringDescriptorProxy[] proxies= history.getDescriptors();
		assertEquals(RefactoringHistoryServiceTests.TOTALZ_HISTORY_NUMBER, proxies.length, "Refactoring history has wrong size");
	}

	@Test
	public void testReadWorkspaceHistory0() throws Exception {
		RefactoringHistory history= RefactoringHistoryService.getInstance().getWorkspaceHistory(0, STAMP_FACTOR, null);
		assertFalse(history.isEmpty(), "Refactoring history should  be empty");
		RefactoringDescriptorProxy[] proxies= history.getDescriptors();
		assertEquals(1, proxies.length, "Refactoring history has wrong size");
	}

	@Test
	public void testReadWorkspaceHistory1() throws Exception {
		RefactoringHistory history= RefactoringHistoryService.getInstance().getWorkspaceHistory(0, Long.MAX_VALUE, null);
		assertFalse(history.isEmpty(), "Refactoring history should  be empty");
		RefactoringDescriptorProxy[] proxies= history.getDescriptors();
		assertEquals(TOTAL_PROJECT_NUMBER, proxies.length, "Refactoring history has wrong size");
	}

	@Test
	public void testReadWorkspaceHistory2() throws Exception {
		RefactoringHistory history= RefactoringHistoryService.getInstance().getWorkspaceHistory(STAMP_FACTOR, STAMP_FACTOR * 5, null);
		assertFalse(history.isEmpty(), "Refactoring history should  be empty");
		RefactoringDescriptorProxy[] proxies= history.getDescriptors();
		assertEquals(5, proxies.length, "Refactoring history has wrong size");
	}

	@Test
	public void testReadWorkspaceHistory3() throws Exception {
		RefactoringHistory history= RefactoringHistoryService.getInstance().getWorkspaceHistory(STAMP_FACTOR * 3, STAMP_FACTOR * 5, null);
		assertFalse(history.isEmpty(), "Refactoring history should  be empty");
		RefactoringDescriptorProxy[] proxies= history.getDescriptors();
		assertEquals(3, proxies.length, "Refactoring history has wrong size");
	}

	@Test
	public void testSharing0() throws Exception {
		final IProject project= fProject.getProject();
		final RefactoringHistoryService service= RefactoringHistoryService.getInstance();
		RefactoringHistory previousHistory= service.getProjectHistory(project, null);
		assertTrue(RefactoringHistoryService.hasSharedRefactoringHistory(project), "Refactoring history should be shared");
		IFolder folder= fProject.getProject().getFolder(RefactoringHistoryService.NAME_HISTORY_FOLDER);
		assertTrue(folder.exists(), "Refactoring history folder should exist.");
		setSharedRefactoringHistory(false);
		RefactoringHistory nextHistory= service.getProjectHistory(project, null);
		assertEquals(previousHistory, nextHistory, "Refactoring history should be the same:");
		assertFalse(RefactoringHistoryService.hasSharedRefactoringHistory(project), "Refactoring history should not be shared");
		assertFalse(folder.exists(), "Refactoring history folder should not exist.");
	}

	@Test
	public void testSharing1() throws Exception {
		final IProject project= fProject.getProject();
		final RefactoringHistoryService service= RefactoringHistoryService.getInstance();
		RefactoringHistory previousHistory= service.getProjectHistory(project, null);
		assertTrue(RefactoringHistoryService.hasSharedRefactoringHistory(project), "Refactoring history should be shared");
		IFolder folder= fProject.getProject().getFolder(RefactoringHistoryService.NAME_HISTORY_FOLDER);
		assertTrue(folder.exists(), "Refactoring history folder should exist.");
		setSharedRefactoringHistory(false);
		RefactoringHistory nextHistory= service.getProjectHistory(project, null);
		assertEquals(previousHistory, nextHistory, "Refactoring history should be the same:");
		assertFalse(RefactoringHistoryService.hasSharedRefactoringHistory(project), "Refactoring history should not be shared");
		assertFalse(folder.exists(), "Refactoring history folder should not exist.");
		setSharedRefactoringHistory(true);
		RefactoringHistory lastHistory= service.getProjectHistory(project, null);
		assertEquals(previousHistory, lastHistory, "Refactoring history should be the same:");
		assertEquals(nextHistory, lastHistory, "Refactoring history should be the same:");
		assertTrue(RefactoringHistoryService.hasSharedRefactoringHistory(project), "Refactoring history should be shared");
		assertTrue(folder.exists(), "Refactoring history folder should exist.");
	}

	@Test
	public void testSortOrder0() throws Exception {
		RefactoringHistory history= RefactoringHistoryService.getInstance().getProjectHistory(fProject.getProject(), null);
		assertFalse(history.isEmpty(), "Refactoring history must not be empty");
		RefactoringDescriptorProxy[] proxies= history.getDescriptors();
		assertEquals(RefactoringHistoryServiceTests.TOTAL_PROJECT_NUMBER, proxies.length, "Refactoring history has wrong size");
		assertDescendingSortOrder(proxies);
	}

	@Test
	public void testSortOrder1() throws Exception {
		RefactoringHistory history= RefactoringHistoryService.getInstance().getProjectHistory(fProject.getProject(), STAMP_FACTOR, STAMP_FACTOR * 5, RefactoringDescriptor.NONE, null);
		assertFalse(history.isEmpty(), "Refactoring history must not be empty");
		RefactoringDescriptorProxy[] proxies= history.getDescriptors();
		assertEquals(5, proxies.length, "Refactoring history has wrong size");
		assertDescendingSortOrder(proxies);
	}

	@Test
	public void testSortOrder2() throws Exception {
		RefactoringHistory history= RefactoringHistoryService.getInstance().getProjectHistory(fProject.getProject(), STAMP_FACTOR * 3, STAMP_FACTOR * 5, RefactoringDescriptor.NONE, null);
		assertFalse(history.isEmpty(), "Refactoring history must not be empty");
		RefactoringDescriptorProxy[] proxies= history.getDescriptors();
		assertEquals(3, proxies.length, "Refactoring history has wrong size");
		assertDescendingSortOrder(proxies);
	}

	@Test
	public void testSortOrder3() throws Exception {
		RefactoringHistory history= RefactoringHistoryService.getInstance().getProjectHistory(fProject.getProject(), STAMP_FACTOR * (NONE_NUMBER + 1), STAMP_FACTOR * (NONE_NUMBER + 4), RefactoringDescriptor.BREAKING_CHANGE, null);
		assertFalse(history.isEmpty(), "Refactoring history must not be empty");
		RefactoringDescriptorProxy[] proxies= history.getDescriptors();
		assertEquals(4, proxies.length, "Refactoring history has wrong size");
		assertDescendingSortOrder(proxies);
	}

	@Test
	public void testSortOrder4() throws Exception {
		RefactoringHistory history= RefactoringHistoryService.getInstance().getProjectHistory(fProject.getProject(), STAMP_FACTOR * (NONE_NUMBER + 1), STAMP_FACTOR * (NONE_NUMBER + 18), RefactoringDescriptor.NONE, null);
		assertFalse(history.isEmpty(), "Refactoring history must not be empty");
		RefactoringDescriptorProxy[] proxies= history.getDescriptors();
		assertEquals(18, proxies.length, "Refactoring history has wrong size");
		assertDescendingSortOrder(proxies);
	}

	@Test
	public void testSortOrder5() throws Exception {
		RefactoringHistory history= RefactoringHistoryService.getInstance().getProjectHistory(fProject.getProject(), 0, Long.MAX_VALUE, CUSTOM_FLAG, null);
		assertFalse(history.isEmpty(), "Refactoring history must not be empty");
		RefactoringDescriptorProxy[] proxies= history.getDescriptors();
		assertEquals(CUSTOM_NUMBER, proxies.length, "Refactoring history has wrong size");
		assertDescendingSortOrder(proxies);
	}

	@Test
	public void testSortOrder6() throws Exception {
		RefactoringHistory history= RefactoringHistoryService.getInstance().getWorkspaceHistory(0, Long.MAX_VALUE, null);
		assertFalse(history.isEmpty(), "Refactoring history must not be empty");
		RefactoringDescriptorProxy[] proxies= history.getDescriptors();
		assertEquals(TOTAL_PROJECT_NUMBER, proxies.length, "Refactoring history has wrong size");
		assertDescendingSortOrder(proxies);
	}

	@Test
	public void testSortOrder7() throws Exception {
		RefactoringHistory history= RefactoringHistoryService.getInstance().getWorkspaceHistory(STAMP_FACTOR * 3, STAMP_FACTOR * 5, null);
		assertFalse(history.isEmpty(), "Refactoring history must not be empty");
		RefactoringDescriptorProxy[] proxies= history.getDescriptors();
		assertEquals(3, proxies.length, "Refactoring history has wrong size");
		assertDescendingSortOrder(proxies);
	}

}
