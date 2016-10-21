/*******************************************************************************
 * Copyright (c) 2016 Andrey Loskutov.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrey Loskutov <loskutov@gmx.de> - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.editors.tests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.eclipse.swt.widgets.Display;

import org.eclipse.core.internal.localstore.FileSystemResourceManager;
import org.eclipse.core.internal.resources.File;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IProgressMonitorWithBlocking;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;

import org.eclipse.core.filebuffers.tests.ResourceHelper;

import org.eclipse.jface.action.IStatusLineManager;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.dialogs.EventLoopProgressMonitor;

import org.eclipse.ui.editors.text.FileDocumentProvider;

/**
 * Test checking UI deadlock on modifying the FileDocumentProvider's underlined
 * file.
 *
 * @since 3.10
 */
public class FileDocumentProviderTest {

	private File file;
	private AtomicBoolean stoppedByTest;
	private AtomicBoolean stopLockingFlag;
	private LockJob lockJob;
	private FileDocumentProviderMock fileProvider;
	private FileSystemResourceManager fsManager;
	private IEditorPart editor;
	private IWorkbenchPage page;

	@Before
	public void setUp() throws Exception {
		IFolder folder = ResourceHelper.createFolder("FileDocumentProviderTestProject/test");
		file = (File) ResourceHelper.createFile(folder, "file.txt", "");
		assertTrue(file.exists());
		fsManager = file.getLocalManager();
		assertTrue(fsManager.fastIsSynchronized(file));
		stopLockingFlag = new AtomicBoolean(false);
		stoppedByTest = new AtomicBoolean(false);
		fileProvider = new FileDocumentProviderMock();
		lockJob = new LockJob("Locking workspace", file, stopLockingFlag, stoppedByTest);

		// We need the editor only to get the default editor status line manager
		IWorkbench workbench = PlatformUI.getWorkbench();
		page = workbench.getActiveWorkbenchWindow().getActivePage();
		editor = IDE.openEditor(page, file);
		TestUtil.runEventLoop();

		IStatusLineManager statusLineManager = editor.getEditorSite().getActionBars().getStatusLineManager();
		// This is default monitor which almost all editors are using
		IProgressMonitor progressMonitor = statusLineManager.getProgressMonitor();
		assertNotNull(progressMonitor);
		assertFalse(progressMonitor instanceof NullProgressMonitor);
		assertFalse(progressMonitor instanceof EventLoopProgressMonitor);
		assertTrue(progressMonitor instanceof IProgressMonitorWithBlocking);

		// Because this monitor is not EventLoopProgressMonitor, it will not
		// process UI events while waiting on workspace lock
		fileProvider.setProgressMonitor(progressMonitor);

		TestUtil.waitForJobs(500, 5000);
		Job[] jobs = Job.getJobManager().find(null);
		String jobsList = Arrays.toString(jobs);
		System.out.println("Still running jobs: " + jobsList);
		if (!Job.getJobManager().isIdle()) {
			jobs = Job.getJobManager().find(null);
			for (Job job : jobs) {
				System.out.println("Going to cancel: " + job.getName() + " / " + job);
				job.cancel();
			}
		}
	}

	@After
	public void tearDown() throws Exception {
		stopLockingFlag.set(true);
		lockJob.cancel();
		if (editor != null) {
			page.closeEditor(editor, false);
		}
		ResourceHelper.deleteProject(file.getProject().getName());
		TestUtil.runEventLoop();
		TestUtil.cleanUp();
	}

	@Test
	public void testRefreshFileWhileWorkspaceIsLocked1() throws Exception {
		// See https://bugs.eclipse.org/bugs/show_bug.cgi?id=482354
		assertNotNull("Test must run in UI thread", Display.getCurrent());

		// Start workspace job which will lock workspace operations on file via
		// rule
		lockJob.schedule();

		// touch the file of the editor
		makeSureResourceIsOutOfDate();

		// Put an UI event in the queue which will stop the workspace lock job
		// after a delay so that we can verify the UI events are still
		// dispatched after the call to refreshFile() below
		Display.getCurrent().timerExec(500, new Runnable() {
			@Override
			public void run() {
				stopLockingFlag.set(true);
				System.out.println("UI event dispatched, lock removed");
			}
		});

		// Original code will lock UI thread here because it will try to acquire
		// resource lock and no one will process UI events anymore
		fileProvider.refreshFile(file);

		System.out.println("Busy wait terminated, UI thread is operable again!");
		assertFalse("Test deadlocked while waiting on resource lock", stoppedByTest.get());
		assertTrue(stopLockingFlag.get());
	}

	@Test
	public void testRefreshFileWhileWorkspaceIsLocked2() throws Exception {
		// See https://bugs.eclipse.org/bugs/show_bug.cgi?id=482354
		assertNotNull("Test must run in UI thread", Display.getCurrent());

		// Start workspace job which will lock workspace operations on file via
		// rule
		lockJob.schedule();

		// touch the file of the editor
		makeSureResourceIsOutOfDate();

		// Put an UI event in the queue which will stop the workspace lock job
		// after a delay so that we can verify the UI events are still
		// dispatched after the call to refreshFile() below
		Display.getCurrent().timerExec(500, new Runnable() {
			@Override
			public void run() {
				stopLockingFlag.set(true);
				System.out.println("UI event dispatched, lock removed");
			}
		});

		// Original code will lock UI thread here because it will try to acquire
		// resource lock and no one will process UI events anymore
		fileProvider.refreshFile(file, fileProvider.getProgressMonitor());

		System.out.println("Busy wait terminated, UI thread is operable again!");

		assertFalse("Test deadlocked while waiting on resource lock", stoppedByTest.get());
		assertTrue(stopLockingFlag.get());
	}

	/*
	 * Set current time stamp via java.nio to make sure
	 * org.eclipse.core.internal.resources.File.refreshLocal(int,
	 * IProgressMonitor) will call super.refreshLocal(IResource.DEPTH_ZERO,
	 * monitor) and so lock the UI by trying to access resource locked by the
	 * job
	 */
	private void makeSureResourceIsOutOfDate() throws Exception {
		int count = 0;
		Files.setLastModifiedTime(file.getLocation().toFile().toPath(),
				FileTime.fromMillis(System.currentTimeMillis()));
		// Give the file system a chance to have a *different* timestamp
		Thread.sleep(100);
		while (fsManager.fastIsSynchronized(file) && count < 1000) {
			Files.setLastModifiedTime(file.getLocation().toFile().toPath(),
					FileTime.fromMillis(System.currentTimeMillis()));
			Thread.sleep(10);
			count++;
		}
		System.out.println("Managed to update file after " + count + " attempts");
		assertFalse(fsManager.fastIsSynchronized(file));
	}

	static void logError(String message, Exception ex) {
		String PLUGIN_ID = "org.eclipse.jface.text"; //$NON-NLS-1$
		ILog log = Platform.getLog(Platform.getBundle(PLUGIN_ID));
		log.log(new Status(IStatus.ERROR, PLUGIN_ID, IStatus.OK, message, ex));
	}
}

class FileDocumentProviderMock extends FileDocumentProvider {

	/**
	 * Overridden to make public accessible for the test
	 */
	@Override
	public void refreshFile(IFile file) throws CoreException {
		System.out.println("Will try to refresh file now: " + file);
		super.refreshFile(file);
	}

	/**
	 * Overridden to make public accessible for the test
	 */
	@Override
	public void refreshFile(IFile file, IProgressMonitor m) throws CoreException {
		System.out.println("Will try to refresh file (with monitor: " + m + " ) now: " + file);
		super.refreshFile(file, m);
	}
}

/** Emulates what SVN plugin jobs are doing */
class LockJob extends WorkspaceJob {

	private final IResource resource;
	private AtomicBoolean stopFlag;
	private AtomicBoolean stoppedByTest;

	public LockJob(String name, IResource resource, AtomicBoolean stopFlag, AtomicBoolean stoppedByTest) {
		super(name);
		this.stopFlag = stopFlag;
		this.stoppedByTest = stoppedByTest;
		setUser(true);
		setSystem(true);
		this.resource = resource;
	}

	public IStatus run2(IProgressMonitor monitor) {
		long startTime = System.currentTimeMillis();
		// Wait maximum 5 minutes
		int maxWaitTime = 5 * 60 * 1000;
		long stopTime = startTime + maxWaitTime;

		System.out.println("Starting the busy wait while holding lock on: " + resource.getFullPath());
		try {
			while (!stopFlag.get()) {
				try {
					if (System.currentTimeMillis() > stopTime) {
						FileDocumentProviderTest.logError("Tiemout occured while waiting on test to finish",
								new IllegalStateException());
						stoppedByTest.set(true);
						return Status.CANCEL_STATUS;
					}
					Thread.sleep(100);
				} catch (InterruptedException e) {
					stoppedByTest.set(true);
					FileDocumentProviderTest.logError("Lock job was interrupted while waiting on test to finish", e);
					e.printStackTrace();
					return Status.CANCEL_STATUS;
				}
			}
		} finally {
			System.out.println("Lock task terminated");
		}
		return Status.OK_STATUS;
	}

	@Override
	public boolean belongsTo(Object family) {
		return super.belongsTo(family) || LockJob.class == family;
	}

	@Override
	public String toString() {
		return super.toString() + " on " + resource;
	}

	@Override
	public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
		IWorkspaceRunnable workspaceRunnable = new IWorkspaceRunnable() {
			@Override
			public void run(IProgressMonitor pm) throws CoreException {
				try {
					run2(pm);
				} catch (Exception e) {
					// Re-throw as OperationCanceledException, which will be
					// caught and re-thrown as InterruptedException below.
					throw new OperationCanceledException(e.getMessage());
				}
				// CoreException and OperationCanceledException are propagated
			}
		};
		ResourcesPlugin.getWorkspace().run(workspaceRunnable,
				resource, IResource.NONE, monitor);

		return monitor.isCanceled() ? Status.CANCEL_STATUS : Status.OK_STATUS;
	}

}
