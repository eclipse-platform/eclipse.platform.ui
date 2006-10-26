/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.elements.adapters;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.internal.ui.InstructionPointerManager;
import org.eclipse.debug.internal.ui.sourcelookup.SourceLookupResult;
import org.eclipse.debug.internal.ui.views.launch.DecorationManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.sourcelookup.ISourceDisplay;
import org.eclipse.debug.ui.sourcelookup.ISourceLookupResult;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.progress.UIJob;

/**
 * @since 3.2
 */
public class StackFrameSourceDisplayAdapter implements ISourceDisplay {

	private IStackFrame fPrevFrame;
	private SourceLookupResult fPrevResult;
	
	/**
	 * Constructs singleton source display adapter for stack frames.
	 */
	public StackFrameSourceDisplayAdapter() {
		DebugPlugin.getDefault().addDebugEventListener(new IDebugEventSetListener() {
			public void handleDebugEvents(DebugEvent[] events) {
				for (int i = 0; i < events.length; i++) {
					DebugEvent event = events[i];
					switch (event.getKind()) {
						case DebugEvent.TERMINATE:
							clearCachedModel(event.getSource());
							// fall through
						case DebugEvent.RESUME:
							if (!event.isEvaluation()) {
								clearSourceSelection(event.getSource());
							}
							break;
						case DebugEvent.CHANGE:
							if (event.getSource() instanceof IStackFrame) {
								if (event.getDetail() == DebugEvent.CONTENT) {
									// force source lookup if a stack frame fires a content change event
									clearCachedModel(event.getSource());
								}
							}
							break;
					}
				}
			}
		});
	}
	
	
	private SourceLookupJob fSourceLookupJob = new SourceLookupJob();
	/**
	 * A job to perform source lookup on the currently selected stack frame.
	 */
	class SourceLookupJob extends Job {
		
		private IStackFrame fTarget;
		private ISourceLocator fLocator;
		private IWorkbenchPage fPage;

		/**
		 * Constructs a new source lookup job.
		 */
		public SourceLookupJob() {
			super("Debug Source Lookup");  //$NON-NLS-1$
			setPriority(Job.INTERACTIVE);
			setSystem(true);	
		}
		
		public void setLookupInfo(IStackFrame frame, ISourceLocator locator, IWorkbenchPage page) {
			fTarget = frame;
			fLocator = locator;
			fPage = page;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
		 */
		protected IStatus run(IProgressMonitor monitor) {
			if (!monitor.isCanceled()) {
				IStackFrame lookupFrame = fTarget;
				ISourceLocator lookupLocator = fLocator;
				
				if (lookupFrame != null && lookupLocator != null && !lookupFrame.isTerminated()) {
					ISourceLookupResult result = null;
					result = DebugUITools.lookupSource(lookupFrame, lookupLocator);
					synchronized (StackFrameSourceDisplayAdapter.this) {
						fPrevResult = (SourceLookupResult)result;
						fPrevFrame = lookupFrame;
					}
					if (!monitor.isCanceled() && fPage != null && !lookupFrame.isTerminated()) {
						fSourceDisplayJob.setDisplayInfo(result, fPage);
						fSourceDisplayJob.schedule();
					}
				}
				setLookupInfo(null, null, null);
			}
			return Status.OK_STATUS;
		}
		
	}
	
	private SourceDisplayJob fSourceDisplayJob = new SourceDisplayJob();
	class SourceDisplayJob extends UIJob {
		
		private ISourceLookupResult fResult;
		private IWorkbenchPage fPage;

		public SourceDisplayJob() {
			super("Debug Source Display");  //$NON-NLS-1$
			setSystem(true);
			setPriority(Job.INTERACTIVE);
		}
		
		/**
		 * Constructs a new source display job
		 */
		public synchronized void setDisplayInfo(ISourceLookupResult result, IWorkbenchPage page) {
			fResult = result;
			fPage = page;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.progress.UIJob#runInUIThread(org.eclipse.core.runtime.IProgressMonitor)
		 */
		public IStatus runInUIThread(IProgressMonitor monitor) {
			ISourceLookupResult result = null;
			IWorkbenchPage page = null;
			synchronized (this) {
				result = fResult;
				page = fPage;
				setDisplayInfo(null, null);
			}
			if (!monitor.isCanceled() && result != null && page != null) {
				DebugUITools.displaySource(result, page);
				// termination may have occurred while displaying source
				if (monitor.isCanceled()) {
					Object artifact = result.getArtifact();
					if (artifact instanceof IStackFrame) {
						clearSourceSelection(((IStackFrame)artifact).getThread());
					}
				}
			}
			
			return Status.OK_STATUS;
		}
		
	}	

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.contexts.ISourceDisplayAdapter#displaySource(java.lang.Object, org.eclipse.ui.IWorkbenchPage, boolean)
	 */
	public synchronized void displaySource(Object context, IWorkbenchPage page, boolean force) {
		IStackFrame frame = (IStackFrame)context;
		if (!force && frame.equals(fPrevFrame)) {
			fPrevResult.updateArtifact(context);
			fSourceDisplayJob.setDisplayInfo(fPrevResult, page);
			fSourceDisplayJob.schedule();
		} else {
			fSourceLookupJob.setLookupInfo(frame, frame.getLaunch().getSourceLocator(), page);
			fSourceLookupJob.schedule();
		}
	}
	
	/**
	 * Clears any source decorations associated with the given thread or
	 * debug target.
	 * 
	 * @param source thread or debug target
	 */
	private void clearSourceSelection(Object source) {		
		if (source instanceof IThread) {
			IThread thread = (IThread)source;
			DecorationManager.removeDecorations(thread);
			InstructionPointerManager.getDefault().removeAnnotations(thread);
		} else if (source instanceof IDebugTarget) {
			IDebugTarget target = (IDebugTarget)source;
			DecorationManager.removeDecorations(target);
			InstructionPointerManager.getDefault().removeAnnotations(target);
		}
	}	
	
	/**
	 * Clear any cached results associated with the given object.
	 * 
	 * @param source
	 */
	private synchronized void clearCachedModel(Object source) {
		if (fPrevFrame != null) {
			IDebugTarget target = null;
			if (source instanceof IDebugElement) {
				target = ((IDebugElement)source).getDebugTarget();
			}
			if (fPrevFrame.getDebugTarget().equals(target)) {
				fPrevFrame = null;
				fPrevResult = null;
				fSourceDisplayJob.cancel();
			}
		}
	}
	
}
