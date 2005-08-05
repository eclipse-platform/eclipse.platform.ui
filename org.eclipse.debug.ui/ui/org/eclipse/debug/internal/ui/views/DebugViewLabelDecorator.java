/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.Vector;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * A label decorator which computes text for debug elements
 * in the background and updates them asynchronously.
 */
public class DebugViewLabelDecorator extends LabelProvider implements ILabelDecorator, IDebugEventSetListener {

	/**
	 * The presentation used to compute text.
	 */
	private IDebugModelPresentation fPresentation;
	/**
	 * The label provider notified when text is computed.
	 */
	private DebugViewDecoratingLabelProvider fLabelProvider;
	/**
	 * The job which will be executed next. All new label requests
	 * are appended to this job.
	 */
	protected LabelJob fNextJob= null;
	/**
	 * A collection of threads that were resumed while their stack
	 * frames' labels were being computed. By maintaining this collection,
	 * the label decorator can refresh stack frame labels for these
	 * threads when they suspend again.
	 */
	private Set resumedThreads= new HashSet();
	/**
	 * The stack frame whose label is currently being computed
	 * or <code>null</code> if no stack frame label is being computed.
	 */
	private IStackFrame fCurrentStackFrame= null;
	/**
	 * An object to be used as a lock for thread-safe access to the
	 * current frame.
	 */
	private Object fCurrentStackFrameLock= new Object();
	
	/**
	 * Creates a new label decorator which will query the
	 * given model presentation for text in the background.
	 * @param presentation
	 */
	public DebugViewLabelDecorator(IDebugModelPresentation presentation) {
		fPresentation= presentation;
		DebugPlugin.getDefault().addDebugEventListener(this);
	}
	
	/**
	 * Sets the label provider which will be notified when a
	 * label has been computed in the background.
	 * 
	 * @param labelProvider the label provider to notify when text
	 *  is computed
	 */
	public void setLabelProvider(DebugViewDecoratingLabelProvider labelProvider) {
		fLabelProvider= labelProvider;
	}
	
	/**
	 * @see org.eclipse.jface.viewers.ILabelDecorator#decorateImage(org.eclipse.swt.graphics.Image, java.lang.Object)
	 */
	public Image decorateImage(Image image, Object element) {
		return image;
	}

	/**
	 * @see org.eclipse.jface.viewers.ILabelDecorator#decorateText(java.lang.String, java.lang.Object)
	 */
	public String decorateText(String text, final Object element) {
		computeText(element);
		return text;
	}
	
	/**
	 * Queues up computation of text for the given element.
	 *  
	 * @param element
	 */
	public void computeText(Object element) {
		synchronized(this) {
			if (fNextJob == null) {
				fNextJob= new LabelJob(DebugUIViewsMessages.DebugViewLabelDecorator_0, fPresentation); 
			}
			fNextJob.computeText(element);
		}
	}
	
	/**
	 * Labels have been computed for the given elements. Fire notification
	 * asynchronously.
	 * 
	 * @param computedElements the elements whose labels have been
	 *  computed.
	 */
	public void labelsComputed(final Object[] computedElements) {
		DebugUIPlugin.getStandardDisplay().asyncExec(new Runnable() {
			public void run() {
				fLabelProvider.labelsComputed(computedElements);
			}
		});
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IDebugEventSetListener#handleDebugEvents(org.eclipse.debug.core.DebugEvent[])
	 */
	public void handleDebugEvents(DebugEvent[] events) {
		for (int i = 0; i < events.length; i++) {
			DebugEvent event= events[i];
			if (event.getKind() == DebugEvent.SUSPEND) {
				handleSuspendEvent(event);
			} else if (event.getKind() == DebugEvent.TERMINATE) {
				handleTerminateEvent(event);
			} else if (event.getKind() == DebugEvent.RESUME) {
				handleResumeEvent(event);
			}
		}
	}

	/**
	 * When a thread resumes for an evaluation or step while computing
	 * labels for one of that thread's stack frames, add the thread to the
	 * collection of "resumed threads". This allows any stack frames whose
	 * label computation was interrupted when the thread was resumed
	 * to be cleaned up later.
	 * 
	 * @param event the resume event
	 */
	private void handleResumeEvent(DebugEvent event) {
		if (event.getSource() instanceof IThread && (event.isEvaluation() || event.isStepStart())) {
			IThread thread= (IThread) event.getSource();
			IStackFrame frame;
			synchronized (fCurrentStackFrameLock) {
				if (fCurrentStackFrame == null) {
					return;
				}
				frame= fCurrentStackFrame;
			}
			if (thread == frame.getThread()) {
				resumedThreads.add(thread);
			}
		}
	}

	/**
	 * When a thread suspends after an evaluation or step, recompute labels
	 * for its stack frames. This ensures that any stack frames whose
	 * label computation was interrupted when the thread was resumed
	 * will be cleaned up.
	 * 
	 * @param event the suspend event
	 */
	private void handleSuspendEvent(DebugEvent event) {
		Object source= event.getSource();
		synchronized (resumedThreads) {
			if (!resumedThreads.remove(source)) {
				return;
			}
		}
		if (!event.isEvaluation() && (event.getDetail() & DebugEvent.STEP_END) == 0) {
			return;
		}
		IThread thread= (IThread) source;
		try {
			IStackFrame[] frames= thread.getStackFrames();
			for (int i = 0; i < frames.length; i++) {
				computeText(frames[i]);
			}
		} catch (DebugException e) {
		}
	}

	/**
	 * When a terminate event is received for a debug target, remove
	 * any of its threads from the resumed threads collection. This not only
	 * prevents unnecessary stack frame label computations, it is a
	 * backstop for cleaning up threads in the collection.
	 * 
	 * @param event the terminate event
	 */
	private void handleTerminateEvent(DebugEvent event) {
		Object source= event.getSource();
		if (source instanceof IDebugTarget) {
			List copiedThreads= new ArrayList(resumedThreads);
			ListIterator iterator = copiedThreads.listIterator();
			while (iterator.hasNext()) {
				IThread thread = (IThread) iterator.next();
				if (thread.getDebugTarget() == source) {
					iterator.remove();
				}
			}
			synchronized(resumedThreads) {
				resumedThreads.retainAll(copiedThreads);
			}
		}
	}
	
	/**
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
	 */
	public void dispose() {
		super.dispose();
		DebugPlugin.getDefault().removeDebugEventListener(this);
	}
	
	/**
	 * A job which computes text for a queue of elements. The job's label
	 * decorator is notified when text has been computed for some number
	 * of elements.
	 */
	protected class LabelJob extends Job implements ISchedulingRule {
		private Vector fElementQueue= new Vector();
		private IDebugModelPresentation fJobPresentation;
		
		/**
		 * Creates a new job with the given name which will use the given
		 * presentation to compute labels in the background
		 * @param name the job's name
		 * @param presentation the presentation to use for label
		 *  computation
		 */
		public LabelJob(String name, IDebugModelPresentation presentation) {
			super(name);
			fJobPresentation= presentation;
			// TODO: why was this rule needed?
			//setRule(this);
			setSystem(true);
		}
		
		/**
		 * Queues up the given element to have its text computed.
		 * @param element the element whose text should be computed
		 *  in this background job
		 */
		public void computeText(Object element) {
			if (!fElementQueue.contains(element)) {
				if (element instanceof IStackFrame) {
					fElementQueue.add(element);
				} else {
					// Add non-stack frame elements (debug target, thread, etc.)
					// to the beginning of the queue so they're computed first.
					fElementQueue.add(0, element);
				}
			}
			schedule();
		}

		/**
		 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
		 */
		public IStatus run(IProgressMonitor monitor) {
			while (!fElementQueue.isEmpty() && !monitor.isCanceled()) {
				int blockSize= 10;
				if (fElementQueue.size() < blockSize) {
					blockSize= fElementQueue.size();
				}
				final List computedElements= new ArrayList();
				for (int i= 0; i < blockSize; i++) {
					Object element= fElementQueue.remove(0);
					if (element == null) {
						break;
					}
					if (element instanceof IStackFrame) {
						synchronized (fCurrentStackFrameLock) {
							fCurrentStackFrame= (IStackFrame) element;
						}
						// If a stack frame's thread has been resumed, make sure it is added to the collection
						// of resumed threads. There's a (small) chance of a race condition here if
						// the thread manages to resume after we check its suspended status and then
						// suspend before we check the status for the next frame.
						IThread thread= fCurrentStackFrame.getThread();
						synchronized(resumedThreads) {
							if (!thread.isTerminated() && !thread.isSuspended()) {
								resumedThreads.add(thread);
								// no need to compute label for "running" stack frame
								continue;
							}
						}
					}
					fLabelProvider.textComputed(element, fJobPresentation.getText(element));
					synchronized (fCurrentStackFrameLock) {
						fCurrentStackFrame= null;
					}
					computedElements.add(element);
				}
				labelsComputed(computedElements.toArray());
			}
			monitor.done();
			return Status.OK_STATUS;
		}

		/*
		 * @see org.eclipse.core.runtime.jobs.ISchedulingRule#contains(org.eclipse.core.runtime.jobs.ISchedulingRule)
		 */
		public boolean contains(ISchedulingRule rule) {
			return (rule instanceof LabelJob) && fJobPresentation == ((LabelJob)rule).fJobPresentation;
		}

		/*
		 * @see org.eclipse.core.runtime.jobs.ISchedulingRule#isConflicting(org.eclipse.core.runtime.jobs.ISchedulingRule)
		 */
		public boolean isConflicting(ISchedulingRule rule) {
			return (rule instanceof LabelJob) && fJobPresentation == ((LabelJob)rule).fJobPresentation;
		}
	}
}
