/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.filebuffers.manipulation;

import java.util.ArrayList;

import org.eclipse.core.internal.filebuffers.FileBuffersPlugin;
import org.eclipse.core.internal.filebuffers.Progress;
import org.eclipse.core.internal.filebuffers.TextFileBufferManager;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.MultiRule;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.IFileBuffer;
import org.eclipse.core.filebuffers.IFileBufferManager;
import org.eclipse.core.filebuffers.IFileBufferStatusCodes;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;

/**
 * A <code>FileBufferOperationRunner</code> executes
 * {@link org.eclipse.core.filebuffers.manipulation.IFileBufferOperation}.
 * The runner takes care of all aspects that are not operation specific.
 * <p>
 * This class is not intended to be subclassed. Clients instantiate this class.
 * <p>
 * Not yet for public use. API under construction.
 * 
 * @see org.eclipse.core.filebuffers.manipulation.IFileBufferOperation
 * @since 3.1
 */
public class FileBufferOperationRunner {	
	
	/** The validation context */
	private final Object fValidationContext;
	/** The file buffer manager */
	private final IFileBufferManager fFileBufferManager;
	
	/** The lock for waiting for completion of computation in the UI thread. */
	private final Object fCompletionLock= new Object();
	/** The flag indicating completion of computation in the UI thread. */
	private transient boolean fIsCompleted;
	/** The exception thrown during the computation in the UI thread. */
	private transient Throwable fThrowable;
	
	
	/**
	 * Creates a new file buffer operation runner.
	 * 
	 * @param fileBufferManager the file buffer manager
	 * @param validationContext the validationContext
	 */
	public FileBufferOperationRunner(IFileBufferManager fileBufferManager, Object validationContext) {
		fFileBufferManager= fileBufferManager;
		fValidationContext= validationContext;
	}
	
	/**
	 * Executes the given operation for all file buffers specified by the given locations.
	 * 
	 * @param locations the file buffer locations
	 * @param operation the operation to be performed
	 * @param monitor the progress monitor
	 * @throws CoreException in case of error
	 * @throws OperationCanceledException in case the execution get canceled
	 */
	public void execute(IPath[] locations, final IFileBufferOperation operation, IProgressMonitor monitor) throws CoreException, OperationCanceledException {
		final int size= locations.length;
		final IProgressMonitor progressMonitor= Progress.getMonitor(monitor);
		progressMonitor.beginTask(operation.getOperationName(), size * 200);
		try {
			
			
			SubProgressMonitor subMonitor= new SubProgressMonitor(progressMonitor, size * 10);
			IFileBuffer[] fileBuffers= createFileBuffers(locations, subMonitor);
			subMonitor.done();
			
			IFileBuffer[] fileBuffers2Save= findFileBuffersToSave(fileBuffers);
			subMonitor= new SubProgressMonitor(progressMonitor, size * 10);
			fFileBufferManager.validateState(fileBuffers2Save, subMonitor, fValidationContext);
			subMonitor.done();
			if (!isCommitable(fileBuffers2Save))
				throw new OperationCanceledException();
			
			IFileBuffer[] unsynchronizedFileBuffers= findUnsynchronizedFileBuffers(fileBuffers);
			performOperation(unsynchronizedFileBuffers, operation, progressMonitor) ;
			
			final IFileBuffer[] synchronizedFileBuffers= findSynchronizedFileBuffers(fileBuffers);
			fIsCompleted= false;
			fThrowable= null;
			synchronized (fCompletionLock) {
				
				executeInContext(new Runnable() {
					public void run() {
						synchronized(fCompletionLock) {
							try {
								Platform.run(new ISafeRunnable() {
									public void handleException(Throwable throwable) {
										fThrowable= throwable;
									}
									public void run() throws Exception {
										performOperation(synchronizedFileBuffers, operation, progressMonitor);
									}
								});
							} finally {
								fIsCompleted= true;
								fCompletionLock.notifyAll();
							}
						}
					}
				});
				
				while (!fIsCompleted) {
					try {
						fCompletionLock.wait(500);
					} catch (InterruptedException x) {
					}
				}
			}
			
			if (fThrowable != null) {
				if (fThrowable instanceof CoreException)
					throw (CoreException) fThrowable;
				throw new CoreException(new Status(IStatus.ERROR, FileBuffersPlugin.PLUGIN_ID, IFileBufferStatusCodes.CONTENT_CHANGE_FAILED, fThrowable.getLocalizedMessage(), fThrowable));
			}
			
			subMonitor= new SubProgressMonitor(progressMonitor, size * 80);
			commit(fileBuffers2Save, subMonitor);
			subMonitor.done();
			
		} finally {
			try {
				SubProgressMonitor subMonitor= new SubProgressMonitor(progressMonitor, size * 10);
				releaseFileBuffers(locations, subMonitor);
				subMonitor.done();
			} finally {
				progressMonitor.done();
			}
		}
	}
			
	private void performOperation(IFileBuffer fileBuffer, IFileBufferOperation operation, IProgressMonitor progressMonitor) throws CoreException, OperationCanceledException {
		
		ISchedulingRule rule= fileBuffer.computeCommitRule();
		IJobManager manager= Platform.getJobManager();
		manager.beginRule(rule, progressMonitor);
		try {
			
			String name= fileBuffer.getLocation().lastSegment();
			progressMonitor.beginTask(name, 100);
			try {
				SubProgressMonitor subMonitor= new SubProgressMonitor(progressMonitor, 100);
				operation.run(fileBuffer, subMonitor);
				subMonitor.done();
			} finally {
				progressMonitor.done();				
			}
			
		} finally {
			manager.endRule(rule);
		}
	}
	
	private void performOperation(IFileBuffer[] fileBuffers, IFileBufferOperation operation, IProgressMonitor progressMonitor) throws CoreException, OperationCanceledException {
		for (int i= 0; i < fileBuffers.length; i++) {
			if (progressMonitor.isCanceled())
				throw new OperationCanceledException();
			SubProgressMonitor subMonitor= new SubProgressMonitor(progressMonitor, 100);
			performOperation(fileBuffers[i], operation, subMonitor);
			subMonitor.done();
		}
	}
	
	private void executeInContext(Runnable runnable) {
		ITextFileBufferManager fileBufferManager= FileBuffers.getTextFileBufferManager();
		if (fileBufferManager instanceof TextFileBufferManager) {
			TextFileBufferManager manager= (TextFileBufferManager) fileBufferManager;
			manager.execute(runnable, true);
		} else {
			runnable.run();
		}
	}
			
	private IFileBuffer[] findUnsynchronizedFileBuffers(IFileBuffer[] fileBuffers) {
		ArrayList list= new ArrayList();
		for (int i= 0; i < fileBuffers.length; i++) {
			if (!fileBuffers[i].isSynchronizationContextRequested())
				list.add(fileBuffers[i]);
		}
		return (IFileBuffer[]) list.toArray(new IFileBuffer[list.size()]);
	}

	private IFileBuffer[] findSynchronizedFileBuffers(IFileBuffer[] fileBuffers) {
		ArrayList list= new ArrayList();
		for (int i= 0; i < fileBuffers.length; i++) {
			if (fileBuffers[i].isSynchronizationContextRequested())
				list.add(fileBuffers[i]);
		}
		return (IFileBuffer[]) list.toArray(new IFileBuffer[list.size()]);
	}

	private IFileBuffer[] createFileBuffers(IPath[] locations, IProgressMonitor progressMonitor) throws CoreException {
		progressMonitor.beginTask("connecting file buffers", locations.length);
		try {
			IFileBuffer[] fileBuffers= new ITextFileBuffer[locations.length];
			for (int i= 0; i < locations.length; i++) {
				IProgressMonitor subMonitor= new SubProgressMonitor(progressMonitor, 1);
				fFileBufferManager.connect(locations[i], subMonitor);
				subMonitor.done();
				fileBuffers[i]= fFileBufferManager.getFileBuffer(locations[i]);
			}
			return fileBuffers;
		
		} catch (CoreException x) {
			try {
				releaseFileBuffers(locations, Progress.getMonitor());
			} catch (CoreException e) {
			}
			throw x;
		} finally {
			progressMonitor.done();
		}
	}
	
	private void releaseFileBuffers(IPath[] locations, IProgressMonitor progressMonitor) throws CoreException {
		progressMonitor.beginTask("disconnecting file buffers", locations.length);
		try {
			final ITextFileBufferManager fileBufferManager= FileBuffers.getTextFileBufferManager();
			for (int i= 0; i < locations.length; i++) {
				SubProgressMonitor subMonitor= new SubProgressMonitor(progressMonitor, 1);
				fileBufferManager.disconnect(locations[i], subMonitor);
				subMonitor.done();
			}
		} finally {
			progressMonitor.done();
		}
	}
	
	private IFileBuffer[] findFileBuffersToSave(IFileBuffer[] fileBuffers) {
		ArrayList list= new ArrayList();
		for (int i= 0; i < fileBuffers.length; i++) {
			IFileBuffer buffer= fileBuffers[i];
			if (!buffer.isDirty())
				list.add(buffer);
		}
		return (IFileBuffer[]) list.toArray(new IFileBuffer[list.size()]);		
	}
					
	private boolean isCommitable(IFileBuffer[] fileBuffers) {
		for (int i= 0; i < fileBuffers.length; i++) {
			if (!fileBuffers[i].isCommitable())
				return false;
		}
		return true;
	}
	
	private ISchedulingRule computeCommitRule(IFileBuffer[] fileBuffers) {
		ArrayList list= new ArrayList();
		for (int i= 0; i < fileBuffers.length; i++) {
			ISchedulingRule rule= fileBuffers[i].computeCommitRule();
			if (rule != null)
				list.add(rule);
		}
		ISchedulingRule[] rules= new ISchedulingRule[list.size()];
		list.toArray(rules);
		return new MultiRule(rules);
	}
	
	private void commit(final IFileBuffer[] fileBuffers, IProgressMonitor progressMonitor) throws CoreException {
		IWorkspaceRunnable runnable= new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				monitor= Progress.getMonitor(monitor);
				monitor.beginTask("committing", fileBuffers.length);
				try {
					for (int i= 0; i < fileBuffers.length; i++) {
						IProgressMonitor subMonitor= new SubProgressMonitor(monitor, 1);
						fileBuffers[i].commit(subMonitor, true);
						subMonitor.done();
					}
				} finally {
					monitor.done();
				}
			}
		};
		ResourcesPlugin.getWorkspace().run(runnable, computeCommitRule(fileBuffers), IWorkspace.AVOID_UPDATE, progressMonitor);
	}	
}
