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
package org.eclipse.debug.internal.ui.model.elements;

import java.util.LinkedList;
import java.util.NoSuchElementException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementLabelProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.progress.UIJob;

/**
 * @since 3.3
 */
public abstract class ElementLabelProvider implements IElementLabelProvider {

	private Job fLabelJob = null;
	
	interface ILabelJob {
		/**
		 * Returns whether the update was queued.
		 * 
		 * @param update update
		 * @return whether the update was queued
		 */
		public boolean queue(ILabelUpdate update);
	}
	
	class LabelJob extends Job implements ILabelJob {
		
		private LabelUpdater fUpdater = new LabelUpdater();

		public LabelJob() {
			super("Label Job"); //$NON-NLS-1$
			setSystem(true);
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
		 */
		protected IStatus run(IProgressMonitor monitor) {
			fUpdater.run();
			return Status.OK_STATUS;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.elements.ElementContentProvider.ILabelJob#queue(org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate)
		 */
		public boolean queue(ILabelUpdate update) {
			return fUpdater.queue(update);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.jobs.Job#shouldRun()
		 */
		public boolean shouldRun() {
			return fUpdater.shouldRun();
		}
		
	}
	
	class UILabelJob extends UIJob implements ILabelJob {
		
		private LabelUpdater fUpdater = new LabelUpdater();

		public UILabelJob() {
			super("Label Job"); //$NON-NLS-1$
			setSystem(true);
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.ui.progress.UIJob#runInUIThread(org.eclipse.core.runtime.IProgressMonitor)
		 */
		public IStatus runInUIThread(IProgressMonitor monitor) {
			fUpdater.run();
			return Status.OK_STATUS;
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.elements.ElementContentProvider.ILabelJob#queue(org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate)
		 */
		public boolean queue(ILabelUpdate update) {
			return fUpdater.queue(update);
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.jobs.Job#shouldRun()
		 */
		public boolean shouldRun() {
			return fUpdater.shouldRun();
		}		
	}
	
	/**
	 * Queue of label updates
	 */
	class LabelUpdater implements Runnable {
		
		LinkedList fQueue = new LinkedList();
		
		public synchronized boolean queue(ILabelUpdate update) {
			if (fQueue == null) {
				return false;
			} else {
				fQueue.addLast(update);
				return true;
			}
		}

		/* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		public void run() {
			ILabelUpdate update = getNextUpdate();
			while (update != null) {	
				try {
					retrieveLabel(update);
				} catch (CoreException e) {
					update.setStatus(e.getStatus());
				}
				update.done();
				update = getNextUpdate();
			}
		}
		
		public synchronized ILabelUpdate getNextUpdate() {
			if (fQueue == null) {
				return null;
			}
			ILabelUpdate update = null;
			try {
				update = (ILabelUpdate) fQueue.removeFirst();
			} catch (NoSuchElementException e) {
				fQueue = null;
			}
			return update;
		}
		
		public boolean shouldRun() {
			return fQueue != null;
		}
	}
	
	/**
	 * Retrieves label attributes for the specified update.
	 * 
	 * @param update
	 */
	protected void retrieveLabel(ILabelUpdate update) throws CoreException {
		String[] columnIds = update.getColumnIds();
		IPresentationContext presentationContext = update.getPresentationContext();
		TreePath elementPath = update.getElement();
		int numColumns = 1;
		if (columnIds != null) {
			numColumns = columnIds.length;
		}
		for (int i = 0; i < numColumns; i++) {
			String columnId = null;
			if (columnIds != null) {
				columnId = columnIds[i];
			}
			update.setLabel(getLabel(elementPath, presentationContext, columnId), i);
			update.setImageDescriptor(getImageDescriptor(elementPath, presentationContext, columnId), i);
			update.setBackground(getBackground(elementPath, presentationContext, columnId), i);
			update.setForeground(getForeground(elementPath, presentationContext, columnId), i);
			update.setFontData(getFontData(elementPath, presentationContext, columnId), i);
		}
	}

	/**
	 * @param element
	 * @param presentationContext
	 * @param columnId
	 * @return font information or <code>null</code>
	 */
	protected FontData getFontData(TreePath elementPath, IPresentationContext presentationContext, String columnId) throws CoreException {
		return null;
	}

	/**
	 * @param element
	 * @param presentationContext
	 * @param columnId
	 * @return color or <code>null</code>
	 */
	protected RGB getForeground(TreePath elementPath, IPresentationContext presentationContext, String columnId) throws CoreException {
		return null;
	}

	/**
	 * @param element
	 * @param presentationContext
	 * @param columnId
	 * @return color or <code>null</code>
	 */
	protected RGB getBackground(TreePath elementPath, IPresentationContext presentationContext, String columnId) throws CoreException {
		return null;
	}

	/**
	 * @param element
	 * @param presentationContext
	 * @param columnId
	 * @return image descriptor or <code>null</code>
	 */
	protected ImageDescriptor getImageDescriptor(TreePath elementPath, IPresentationContext presentationContext, String columnId) throws CoreException {
		return null;
	}

	/**
	 * @param element
	 * @param presentationContext
	 * @param columnId
	 * @return label
	 */
	protected abstract String getLabel(TreePath elementPath, IPresentationContext presentationContext, String columnId) throws CoreException;	

    /* (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IElementLabelProvider#updateLabel(org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate)
     */
    public synchronized void update(ILabelUpdate update) {
		if (fLabelJob == null) {
			fLabelJob = newLabelJob(update);
		}
		if (!((ILabelJob)fLabelJob).queue(update)) {
			fLabelJob = newLabelJob(update);
			((ILabelJob)fLabelJob).queue(update);
		}
		// TODO: rule
		fLabelJob.schedule();
	}
    
    private Job newLabelJob(ILabelUpdate update) {
    	if (requiresUIJob(update)) {
			return new UILabelJob();
		} else {
			return new LabelJob();
		}
    }
    
    /** 
     * Returns whether a UI job should be used for updates versus a non-UI job.
     */
    protected boolean requiresUIJob(ILabelUpdate update) {
    	return false;
    }
	
}
