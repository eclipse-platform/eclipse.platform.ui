/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.launch;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
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
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.views.DebugUIViewsMessages;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.swt.graphics.Image;

/**
 * A label decorator which computes text for debug elements
 * in the background and updates them asynchronously.
 */
public class LaunchViewLabelDecorator extends LabelProvider implements ILabelDecorator, IDebugEventSetListener {

	/**
	 * The presentation used to compute text.
	 */
	private IDebugModelPresentation fPresentation;
	/**
	 * The label provider notified when text is computed.
	 */
	private LaunchViewDecoratingLabelProvider fLabelProvider;
	/**
	 * The job which will be executed next. All new label requests
	 * are appended to this job.
	 */
	protected LabelJob fNextJob= null;
	
	/**
	 * Creates a new label decorator which will query the
	 * given model presentation for text in the background.
	 * @param presentation
	 */
	public LaunchViewLabelDecorator(IDebugModelPresentation presentation) {
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
	public void setLabelProvider(LaunchViewDecoratingLabelProvider labelProvider) {
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
		if (!(element instanceof IDebugElement)) {
			return fPresentation.getText(element);
		}
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
				fNextJob= new LabelJob(DebugUIViewsMessages.getString("LaunchViewLabelDecorator.0"), fPresentation); //$NON-NLS-1$
			}
		}
		fNextJob.computeText(element);
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
				fireLabelProviderChanged(new LabelProviderChangedEvent(LaunchViewLabelDecorator.this, computedElements));
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
			}
		}
	}
		
	/**
	 * When a thread suspends after an evaluation, recompute labels
	 * for its stack frames. This ensures that any stack frames whose
	 * label computation was interrupted when the thread was resumed
	 * will be cleaned up eventually.
	 * @param event the suspend event
	 */
	public void handleSuspendEvent(DebugEvent event) {
		Object source= event.getSource();
		if (!(source instanceof IThread) || !event.isEvaluation()) {
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
		private IDebugModelPresentation fPresentation;
		
		/**
		 * Creates a new job with the given name which will use the given
		 * presentation to compute labels in the background
		 * @param name the job's name
		 * @param presentation the presentation to use for label
		 *  computation
		 */
		public LabelJob(String name, IDebugModelPresentation presentation) {
			super(name);
			fPresentation= presentation;
			setRule(this);
		}
		
		/**
		 * Queues up the given element to have its text computed.
		 * @param element the element whose text should be computed
		 *  in this background job
		 */
		public void computeText(Object element) {
			if (!fElementQueue.contains(element)) {
				fElementQueue.add(element);
			}
			schedule();
		}

		/**
		 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
		 */
		public IStatus run(IProgressMonitor monitor) {
			synchronized(this) {
				fNextJob= null;
			}
			int numElements= fElementQueue.size();
			monitor.beginTask(MessageFormat.format(DebugUIViewsMessages.getString("LaunchViewLabelDecorator.1"), new String[] { Integer.toString(numElements) }), numElements); //$NON-NLS-1$
			while (!fElementQueue.isEmpty()) {
				StringBuffer message= new StringBuffer(MessageFormat.format(DebugUIViewsMessages.getString("LaunchViewLabelDecorator.1"), new String[] { Integer.toString(fElementQueue.size()) })); //$NON-NLS-1$
				if (fNextJob != null) {
					message.append(MessageFormat.format(DebugUIViewsMessages.getString("LaunchViewLabelDecorator.2"), new String[] { Integer.toString(fNextJob.fElementQueue.size()) })); //$NON-NLS-1$
				}
				monitor.setTaskName(message.toString());
				int blockSize= 10;
				if (fElementQueue.size() < blockSize) {
					blockSize= fElementQueue.size();
				}
				final List computedElements= new ArrayList();
				for (int i= 0; i < blockSize; i++) {
					Object element= fElementQueue.remove(0);
					if (element != null) {
						fLabelProvider.textComputed(element, fPresentation.getText(element));
						computedElements.add(element);
					}
				}
				labelsComputed(computedElements.toArray());
				monitor.worked(computedElements.size());
			}
			monitor.done();
			return Status.OK_STATUS;
		}

		/*
		 * @see org.eclipse.core.runtime.jobs.ISchedulingRule#contains(org.eclipse.core.runtime.jobs.ISchedulingRule)
		 */
		public boolean contains(ISchedulingRule rule) {
			return (rule instanceof LabelJob) && fPresentation == ((LabelJob)rule).fPresentation;
		}

		/*
		 * @see org.eclipse.core.runtime.jobs.ISchedulingRule#isConflicting(org.eclipse.core.runtime.jobs.ISchedulingRule)
		 */
		public boolean isConflicting(ISchedulingRule rule) {
			return (rule instanceof LabelJob) && fPresentation == ((LabelJob)rule).fPresentation;
		}
	};

}
