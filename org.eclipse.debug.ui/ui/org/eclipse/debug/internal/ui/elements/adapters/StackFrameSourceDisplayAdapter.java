/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
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
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.internal.ui.InstructionPointerManager;
import org.eclipse.debug.internal.ui.sourcelookup.SourceLookupResult;
import org.eclipse.debug.internal.ui.views.launch.DecorationManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.contexts.ISourceDisplayAdapter;
import org.eclipse.debug.ui.sourcelookup.ISourceLookupResult;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.progress.UIJob;

/**
 * @since 3.2
 */
public class StackFrameSourceDisplayAdapter implements ISourceDisplayAdapter {

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
						case DebugEvent.RESUME:
							if (!event.isEvaluation()) {
								clearSourceSelection(event.getSource());
							}
							break;
					}
				}
			}
		});
	}
	
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
		public SourceLookupJob(IStackFrame frame, ISourceLocator locator, IWorkbenchPage page) {
			super("Debug Source Lookup"); 
			setPriority(Job.INTERACTIVE);
			setSystem(true);
			fTarget = frame;
			fLocator = locator;
			fPage = page;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
		 */
		protected IStatus run(IProgressMonitor monitor) {
			if (!monitor.isCanceled()) {
				ISourceLookupResult result = null;
				result = DebugUITools.lookupSource(fTarget, fLocator);
				synchronized (StackFrameSourceDisplayAdapter.this) {
					fPrevResult = (SourceLookupResult)result;
					fPrevFrame = fTarget;
				}
				if (!monitor.isCanceled()) {
					SourceDisplayJob job = new SourceDisplayJob(result, fPage);
					job.schedule();
				}
			}
			return Status.OK_STATUS;
		}
		
	}
	
	class SourceDisplayJob extends UIJob {
		
		private ISourceLookupResult fResult;
		private IWorkbenchPage fPage;

		/**
		 * Constructs a new source display job
		 */
		public SourceDisplayJob(ISourceLookupResult result, IWorkbenchPage page) {
			super("Debug Source Display"); 
			setSystem(true);
			setPriority(Job.INTERACTIVE);
			fResult = result;
			fPage = page;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.progress.UIJob#runInUIThread(org.eclipse.core.runtime.IProgressMonitor)
		 */
		public IStatus runInUIThread(IProgressMonitor monitor) {
			if (!monitor.isCanceled()) {
				DebugUITools.displaySource(fResult, fPage);
			}
			return Status.OK_STATUS;
		}
		
	}	

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.contexts.ISourceDisplayAdapter#displaySource(java.lang.Object, org.eclipse.ui.IWorkbenchPage)
	 */
	public synchronized void displaySource(Object context, IWorkbenchPage page) {
		IStackFrame frame = (IStackFrame)context;
		if (frame.equals(fPrevFrame)) {
			fPrevResult.updateArtifact(context);
			(new SourceDisplayJob(fPrevResult, page)).schedule();
		} else {
			(new SourceLookupJob(frame, frame.getLaunch().getSourceLocator(), page)).schedule();
		}
		
	}
	
	/**
	 * Deselects any source decorations associated with the given thread or
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
	
}
