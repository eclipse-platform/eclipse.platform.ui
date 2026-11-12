/*******************************************************************************
 * Copyright (c) 2026 Vector Informatik GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Vector Informatik GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.search.tests.filesearch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.eclipse.swt.widgets.Display;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.Job;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.jface.text.ITextSelection;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import org.eclipse.search.internal.ui.text.EditorOpener;
import org.eclipse.search.tests.GotoMarkerOnlyEditor;
import org.eclipse.search.tests.GotoMarkerOnlyEditor.GotoCall;
import org.eclipse.search.tests.ResourceHelper;
import org.eclipse.search.tests.SearchTestUtil;
import org.eclipse.search.tests.TextEditorWrapper;
import org.eclipse.search.ui.NewSearchUI;

/**
 * Tests that {@link EditorOpener} does not block the UI thread while another thread holds the
 * workspace lock.
 */
public class EditorOpenerTest {

	private static final String PROJECT_NAME= "EditorOpenerTest"; //$NON-NLS-1$

	private static final long MAX_UI_WAIT_MS= 3000;

	private static final long LOCK_RELEASE_MS= 10_000;

	private static final long JOB_TIMEOUT_MS= 20_000;

	private IWorkbenchPage fPage;

	private IProject fProject;

	private IFile fTrigger;

	private final List<IStatus> fLoggedSearchErrors= new CopyOnWriteArrayList<>();

	private final ILogListener fLogListener= (status, _) -> {
		if (status.getSeverity() == IStatus.ERROR && NewSearchUI.PLUGIN_ID.equals(status.getPlugin())) {
			fLoggedSearchErrors.add(status);
		}
	};

	@BeforeEach
	public void setUp() throws Exception {
		SearchTestUtil.ensureWelcomePageClosed();
		fPage= PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		fProject= ResourceHelper.createProject(PROJECT_NAME);
		fTrigger= createFile("trigger.txt"); //$NON-NLS-1$
		Platform.addLogListener(fLogListener);
	}

	@AfterEach
	public void tearDown() throws Exception {
		Platform.removeLogListener(fLogListener);
		fPage.closeAllEditors(false);
		waitForMarkerJobs();
		ResourceHelper.deleteProject(PROJECT_NAME);
	}

	@Test
	public void openMatchDoesNotWaitForWorkspaceLock() throws Exception {
		IFile file= createFile("match.gotomarker"); //$NON-NLS-1$
		EditorOpener opener= new EditorOpener();

		IEditorPart editor;
		try (WorkspaceLockHolder _= new WorkspaceLockHolder(fTrigger)) {
			long start= System.nanoTime();
			editor= opener.openAndSelect(fPage, file, 0, 1, true);
			assertUiThreadDidNotWait(start);
		}

		GotoMarkerOnlyEditor gotoEditor= assertInstanceOf(GotoMarkerOnlyEditor.class, editor);
		waitForMarkerJobs();
		assertEquals(List.of(new GotoCall(0, 1, true)), gotoEditor.getGotoCalls());
		assertNoSearchMarkerLeft(file);
	}

	@Test
	public void wrappedTextEditorSelectsWithoutMarker() throws Exception {
		IFile file= createFile("match.wrappedtext"); //$NON-NLS-1$
		EditorOpener opener= new EditorOpener();

		IEditorPart editor;
		try (WorkspaceLockHolder _= new WorkspaceLockHolder(fTrigger)) {
			long start= System.nanoTime();
			editor= opener.openAndSelect(fPage, file, 2, 3, true);
			assertUiThreadDidNotWait(start);
			assertEquals(0, Job.getJobManager().find(EditorOpener.MARKER_JOB_FAMILY).length, "no marker job expected"); //$NON-NLS-1$
		}

		TextEditorWrapper wrapper= assertInstanceOf(TextEditorWrapper.class, editor);
		ITextSelection selection= (ITextSelection) wrapper.getTextEditor().getSelectionProvider().getSelection();
		assertEquals(2, selection.getOffset());
		assertEquals(3, selection.getLength());
		assertNoSearchMarkerLeft(file);
	}

	@Test
	public void onlyLatestClickMovesCaret() throws Exception {
		IFile file= createFile("match.gotomarker"); //$NON-NLS-1$
		EditorOpener opener= new EditorOpener();

		IEditorPart editor;
		try (WorkspaceLockHolder _= new WorkspaceLockHolder(fTrigger)) {
			long start= System.nanoTime();
			opener.openAndSelect(fPage, file, 0, 1, true);
			editor= opener.openAndSelect(fPage, file, 5, 2, true);
			assertUiThreadDidNotWait(start);
		}

		GotoMarkerOnlyEditor gotoEditor= assertInstanceOf(GotoMarkerOnlyEditor.class, editor);
		waitForMarkerJobs();
		assertEquals(List.of(new GotoCall(5, 7, true)), gotoEditor.getGotoCalls());
		assertNoSearchMarkerLeft(file);
	}

	@Test
	public void editorClosedBeforeMarkerIsReady() throws Exception {
		IFile file= createFile("match.gotomarker"); //$NON-NLS-1$
		EditorOpener opener= new EditorOpener();

		IEditorPart editor;
		try (WorkspaceLockHolder _= new WorkspaceLockHolder(fTrigger)) {
			long start= System.nanoTime();
			editor= opener.openAndSelect(fPage, file, 0, 1, true);
			assertUiThreadDidNotWait(start);
			assertTrue(fPage.closeEditor(editor, false));
		}

		GotoMarkerOnlyEditor gotoEditor= assertInstanceOf(GotoMarkerOnlyEditor.class, editor);
		waitForMarkerJobs();
		assertEquals(List.of(), gotoEditor.getGotoCalls());
		assertNoSearchMarkerLeft(file);
		assertEquals(List.of(), fLoggedSearchErrors);
	}

	@Test
	public void textEditorClickCancelsPendingMarker() throws Exception {
		IFile markerFile= createFile("match.gotomarker"); //$NON-NLS-1$
		IFile textFile= createFile("match.wrappedtext"); //$NON-NLS-1$
		EditorOpener opener= new EditorOpener();

		IEditorPart markerEditor;
		IEditorPart textEditor;
		try (WorkspaceLockHolder _= new WorkspaceLockHolder(fTrigger)) {
			long start= System.nanoTime();
			markerEditor= opener.openAndSelect(fPage, markerFile, 0, 1, true);
			textEditor= opener.openAndSelect(fPage, textFile, 2, 3, true);
			assertUiThreadDidNotWait(start);
		}

		waitForMarkerJobs();
		GotoMarkerOnlyEditor gotoEditor= assertInstanceOf(GotoMarkerOnlyEditor.class, markerEditor);
		assertEquals(List.of(), gotoEditor.getGotoCalls());
		assertNoSearchMarkerLeft(markerFile);
		TextEditorWrapper wrapper= assertInstanceOf(TextEditorWrapper.class, textEditor);
		ITextSelection selection= (ITextSelection) wrapper.getTextEditor().getSelectionProvider().getSelection();
		assertEquals(2, selection.getOffset());
		assertEquals(3, selection.getLength());
	}

	private IFile createFile(String name) throws CoreException {
		IFile file= fProject.getFile(name);
		file.create(new ByteArrayInputStream("0123456789".getBytes(StandardCharsets.UTF_8)), true, null); //$NON-NLS-1$
		return file;
	}

	private static void assertUiThreadDidNotWait(long startNanos) {
		long ms= TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
		assertTrue(ms < MAX_UI_WAIT_MS, "UI thread waited " + ms + " ms for the workspace lock"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private static void assertNoSearchMarkerLeft(IFile file) throws CoreException {
		assertEquals(0, file.findMarkers(NewSearchUI.SEARCH_MARKER, true, IResource.DEPTH_ZERO).length,
				"search markers left on " + file); //$NON-NLS-1$
	}

	/**
	 * Processes UI events until no marker job of {@link EditorOpener} is scheduled or running and
	 * no UI runnable that schedules one is pending.
	 */
	private static void waitForMarkerJobs() throws InterruptedException {
		long deadline= System.currentTimeMillis() + JOB_TIMEOUT_MS;
		while (true) {
			processUiEvents();
			if (Job.getJobManager().find(EditorOpener.MARKER_JOB_FAMILY).length == 0) {
				// a finished create job has already posted its UI runnable, which may schedule the delete job
				processUiEvents();
				if (Job.getJobManager().find(EditorOpener.MARKER_JOB_FAMILY).length == 0) {
					return;
				}
			}
			assertTrue(System.currentTimeMillis() < deadline, "timeout waiting for the marker jobs"); //$NON-NLS-1$
			Thread.sleep(10);
		}
	}

	private static void processUiEvents() {
		Display display= Display.getCurrent();
		while (display.readAndDispatch()) {
			// process all pending events
		}
	}

	/**
	 * Holds the workspace lock like a slow resource change listener does: a background job changes
	 * the trigger file, and a {@code POST_CHANGE} listener blocks while the workspace notifies
	 * about it, until the holder is closed or at most {@link #LOCK_RELEASE_MS} passed. A UI thread
	 * that waits for the lock fails the test instead of hanging it.
	 */
	private static final class WorkspaceLockHolder implements AutoCloseable {
		private final CountDownLatch fHolding= new CountDownLatch(1);

		private final CountDownLatch fRelease= new CountDownLatch(1);

		private final AtomicBoolean fBlocked= new AtomicBoolean();

		private final Timer fWatchdog= new Timer(true);

		private final IResourceChangeListener fListener;

		private final Job fJob;

		WorkspaceLockHolder(IFile trigger) throws InterruptedException {
			fListener= event -> {
				IResourceDelta delta= event.getDelta();
				if (delta != null && delta.findMember(trigger.getFullPath()) != null && fBlocked.compareAndSet(false, true)) {
					fHolding.countDown();
					try {
						fRelease.await(2 * LOCK_RELEASE_MS, TimeUnit.MILLISECONDS);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
				}
			};
			ResourcesPlugin.getWorkspace().addResourceChangeListener(fListener, IResourceChangeEvent.POST_CHANGE);
			fJob= Job.create("touch trigger file", (ICoreRunnable) trigger::touch); //$NON-NLS-1$
			fJob.setSystem(true);
			fJob.schedule();
			boolean holding= fHolding.await(5, TimeUnit.SECONDS);
			if (!holding) {
				fRelease.countDown();
				ResourcesPlugin.getWorkspace().removeResourceChangeListener(fListener);
			}
			assertTrue(holding, "workspace lock not acquired"); //$NON-NLS-1$
			fWatchdog.schedule(new TimerTask() {
				@Override
				public void run() {
					fRelease.countDown();
				}
			}, LOCK_RELEASE_MS);
		}

		@Override
		public void close() throws InterruptedException {
			fRelease.countDown();
			fWatchdog.cancel();
			long deadline= System.currentTimeMillis() + JOB_TIMEOUT_MS;
			while (fJob.getState() != Job.NONE && System.currentTimeMillis() < deadline) {
				processUiEvents();
				Thread.sleep(10);
			}
			ResourcesPlugin.getWorkspace().removeResourceChangeListener(fListener);
		}
	}
}
