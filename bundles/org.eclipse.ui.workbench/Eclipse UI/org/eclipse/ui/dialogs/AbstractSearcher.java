/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.dialogs;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.ProgressMonitorWrapper;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.progress.UIJob;

import com.ibm.icu.text.MessageFormat;

/**
 * AbstractSearcher 
 * 
 * @since 3.3
 *
 */
public abstract class AbstractSearcher {
	
	/**
	 * Filter param to set searching pattern.
	 * It's set by <code> setFilterParam(int param, Object value) <code> method where value is a string pattern.
	 */
	public static final int PATTERN = 1;

	protected SearcherHistory history;
	private UIJob refreshJob;

	protected SearcherModel model;
	
	protected AbstractSearchJob searchJob;
	protected int searchJobTicket;
	
	/**
	 * 
	 */
	public AbstractSearcher() {
	}

	/**
	 * Get descritoions for concrete object
	 * 
	 * @param item
	 * @return item description
	 */
	public abstract Object getDetails(Object item);

	/**
	 * Get object respondent to item, which is return by dialog
	 * 
	 * @param item
	 * @return respondent object
	 */
	public abstract Object getObjectToReturn(Object item);

	/**
	 * Set filter param fg. for pattern <code> setFilterParam(AbstractSearcher.PATTERN, "*") <code>, its return all matched resources
	 * 
	 * @param param
	 * @param value
	 */
	public abstract void setFilterParam(int param, Object value);

	/**
	 * Validate and return status of the object
	 * 
	 * @param item
	 * @return status of the item
	 */
	public abstract IStatus validateElement(Object item);
	
	/**
	 * Set refresh job
	 * 
	 * @param refreshJob
	 */
	public void setRefreshJob(UIJob refreshJob){
		this.refreshJob = refreshJob;
	}
	/**
	 * Restore a state of history of selected elements from memento
	 * 
	 * @param memento
	 */
	public void restoreState(IMemento memento) {
		history.load(memento);
	}
	/**
	 * Save a state of history of selected elements to memento
	 * 
	 * @param memento
	 */
	public void saveState(IMemento memento) {
		history.save(memento);
	}
	
	/**
	 * Get matched elements
	 * 
	 * @return searched elements
	 */
	public Object[] getElements(){
		return model.getElements();
	}

	/**
	 * Get progress message
	 * 
	 * @return progress message
	 */
	public String getProgressMessage(){
		return model.getProgressMessage();
	}
	
	/**
	 * SearcherModel is a data model for searcher.
	 * It's collects data about matched elements and about history(selected elements). 
	 */
	protected class SearcherModel {
		
		private SortedSet elements;
		
		private String progressMessage = ""; //$NON-NLS-1$
		
		public SearcherModel(){
			this.elements = Collections.synchronizedSortedSet(new TreeSet(getComparator()));
	//		this.historyElements = Collections.synchronizedSortedSet(new TreeSet(getComparator()));
		}
		
		protected void addElements(Object[] items) {
			this.elements.addAll(Arrays.asList(items));
			refresh();
		}
	
		/**
		 * Get searched elements
		 * @return searched elements
		 */
		public Object[] getElements(){
			return elements.toArray();
		}

		/**
		 * Get progress message
		 * @return progress message
		 */
		public String getProgressMessage(){
			return progressMessage;
		}
		
		/**
		 * Set elements
		 * @param items
		 */
		public void setElements(Object[] items){
			elements.clear();
			elements.addAll(Arrays.asList(items));
			refresh();
		}
		
		/**
		 * Set history (selected elements)
		 * @param items
		 */
		public void setHistory(Object[] items){
			elements.addAll(Arrays.asList(items));
			refresh();
		}
		
		/**
		 * Clear elemnts and history
		 */
		public void reset(){
			this.elements.clear();
	//		this.historyElements.clear();
			refresh();
		}
		
		/**
		 * Add Element to elements collection
		 * @param item
		 */
		public void addElement(Object item){
			this.elements.add(item);
		}
		
		/**
		 * Schedule refresh job on the dialog
		 */
		public void refresh(){
			refreshJob.schedule();
		}
		
		/**
		 * Set progress message
		 * @param progressMessage
		 */
		public void setProgressMessage(String progressMessage){
		 	this.progressMessage = progressMessage;
		 	refresh();
		}

	}
	
	/**
	 * Get comparator to sort elements
	 * @param comparator
	 */
	protected abstract Comparator getComparator();
	
	/**
	 * SearcherProgressMonitor to monitoring progress of searching process.
	 * It's update progress message and refresh dialog after concrete part of work.
	 * @since 3.3
	 *
	 */
	protected static class SearcherProgressMonitor extends ProgressMonitorWrapper {

		private SearcherModel model;
		private String fName;
		private int fTotalWork;
		private double fWorked;
		private boolean fDone;
			
		/**
		 * @param monitor
		 * @param searchEngineModel
		 */
		public SearcherProgressMonitor(IProgressMonitor monitor, SearcherModel model) {
			super(monitor);
			this.model = model;
		}
		public void setTaskName(String name) {
			super.setTaskName(name);
			fName= name;
		}
		
		public void beginTask(String name, int totalWork) {
			super.beginTask(name, totalWork);
			if (fName == null)
				fName= name;
			fTotalWork= totalWork;
		}
		
		public void worked(int work) {
			super.worked(work);
			internalWorked(work);
		}
		
		public void done() {
			fDone= true;
			model.setProgressMessage(""); //$NON-NLS-1$
			model.refresh();
			super.done();
		}
		
		public void internalWorked(double work) {
			fWorked= fWorked + work;
			if ((((int)(((fWorked - work)  * 10) / fTotalWork)) < ((int)((fWorked  * 10) / fTotalWork))) || (((int)((fWorked  * 10) / fTotalWork)) == 0))
				model.setProgressMessage(getMessage());
		}
		
		private String getMessage() {
			if (fDone) {
				return ""; //$NON-NLS-1$
			} else if (fTotalWork == 0) {
				return fName;
			} else {
				return MessageFormat.format( "{0} ({1}%)" //$NON-NLS-1$
						, new Object[] { fName, new Integer((int)((fWorked * 100) / fTotalWork)) });
			}
		}
	}
	
	/**
	 * AbstractJob 
	 * @since 3.3
	 *
	 */
	private static abstract class AbstractJob extends Job {
		
		protected SearcherModel model;
		
		private static int fSearchJobTicket;
		
		/**
		 * @param name
		 */
		protected AbstractJob(String name, SearcherModel model) {
			super(name);
			this.model = model;
			setSystem(true);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
		 */
		protected final IStatus run(IProgressMonitor parent) {
			SearcherProgressMonitor monitor= new SearcherProgressMonitor(parent, model);
			return doRun(monitor);
		}

		protected abstract IStatus doRun(SearcherProgressMonitor monitor);
		
		protected void setSearchResult(int ticket, final Object[] results) {
			if (ticket == fSearchJobTicket)
				model.setElements(results); 
		}
	}
	
	/**
	 * AbstractSearchJob for searching elements.
	 * @since 3.2
	 *
	 */
	protected abstract class AbstractSearchJob extends AbstractJob {
		
		protected int fTicket;
		
		protected SearcherHistory history;
		
		protected AbstractSearchJob(int ticket, SearcherModel model, SearcherHistory history) {
			super(WorkbenchMessages.AbstractSearcher_job_label, model);
			fTicket= ticket;
			this.history= history;
		}
		
		public void stop() {
			cancel();
		}
		
		protected IStatus doRun(SearcherProgressMonitor monitor) {
			try {
				internalRun(monitor);
			} catch (CoreException e) {
				searchJobFailed(fTicket, e);
				return new Status(IStatus.ERROR, WorkbenchPlugin.PI_WORKBENCH, IStatus.ERROR, WorkbenchMessages.AbstractSearcher_job_error, e);
			} catch (InterruptedException e) {
				return canceled(e, true);
			} catch (OperationCanceledException e) {
				return canceled(e, false);
			}
			searchJobDone(fTicket);
			return ok();	
		}
		
		protected abstract void searchResults(Set filteredHistory, SearcherProgressMonitor monitor) throws CoreException ;
		
		protected abstract List getFilteredHistory();
		
		private void internalRun(SearcherProgressMonitor monitor) throws CoreException, InterruptedException {
			
			if (monitor.isCanceled())
				throw new OperationCanceledException();
			
			model.reset();
			
			List elements = getFilteredHistory();
			
			model.setHistory(elements.toArray());
			
			searchResults(new HashSet(elements), monitor);
		
			if (monitor.isCanceled())
				throw new OperationCanceledException();			

		}
		
		private IStatus canceled(Exception e, boolean removePendingItems) {
			searchJobCanceled(fTicket, removePendingItems);
			return new Status(IStatus.CANCEL, WorkbenchPlugin.PI_WORKBENCH, IStatus.CANCEL, WorkbenchMessages.AbstractSearcher_job_cancel, e);
		}
		private IStatus ok() {
			return new Status(IStatus.OK, WorkbenchPlugin.PI_WORKBENCH, IStatus.OK, "", null); //$NON-NLS-1$
		}
	}

	private void searchJobDone(int ticket) {
		if (ticket == searchJobTicket)
				searchJob= null;
	}
	
	private synchronized void searchJobFailed(int ticket, CoreException e) {
		searchJobDone(ticket);
		WorkbenchPlugin.log(e);
	}
	
	private void searchJobCanceled(int ticket, final boolean removePendingItems) {
		if (ticket == searchJobTicket)
			searchJob= null;
	}
	
	protected void stop(boolean stopSyncJob, boolean dispose) {
		if (searchJob != null) {
			searchJob.stop();
			searchJob= null;
		}
	}
	
	/**
	 * SearcherHistory abstract representaion of history(selected elements).
	 * It's store and resore his state.
	 * @since 3.3
	 *
	 */
	protected static abstract class SearcherHistory {

		private static final String DEFAULT_ROOT_NODE_NAME= "historyRootNode"; //$NON-NLS-1$
		private static final String DEFAULT_INFO_NODE_NAME= "infoNode"; //$NON-NLS-1$
		private static final int MAX_HISTORY_SIZE= 60;

		private final Map fHistory;
		private final Hashtable fPositions;
		private final String fRootNodeName;
		private final String fInfoNodeName;
			
		public SearcherHistory(String rootNodeName, String infoNodeName) {
			fHistory= new LinkedHashMap(80, 0.75f, true) {
				private static final long serialVersionUID= 1L;
				protected boolean removeEldestEntry(Map.Entry eldest) {
					return size() > MAX_HISTORY_SIZE;
				}
			};
			fRootNodeName= rootNodeName;
			fInfoNodeName= infoNodeName;
			fPositions= new Hashtable(MAX_HISTORY_SIZE);
		}
		
		public SearcherHistory() {
			this(DEFAULT_ROOT_NODE_NAME, DEFAULT_INFO_NODE_NAME);
		}
		
		public synchronized void accessed(Object object) {
			fHistory.put(getKey(object), object);
			rebuildPositions();
		}
		
		public synchronized boolean contains(Object object) {
			return fHistory.containsKey(getKey(object));
		}
		
		public synchronized boolean containsKey(Object key) {
			return fHistory.containsKey(key);
		}
		
		public synchronized boolean isEmpty() {
			return fHistory.isEmpty();
		}
		
		public synchronized Object remove(Object object) {
			Object removed= fHistory.remove(getKey(object));
			rebuildPositions();
			return removed;
		}
		
		public synchronized Object removeKey(Object key) {
			Object removed= fHistory.remove(key);
			rebuildPositions();
			return removed;
		}
		
		/**
		 * Normalized position in history of object denoted by key.
		 * The position is a value between zero and one where zero
		 * means not contained in history and one means newest element
		 * in history. The lower the value the older the element.
		 * 
		 * @param key The key of the object to inspect
		 * @return value in [0.0, 1.0] the lower the older the element
		 */
		public synchronized float getNormalizedPosition(Object key) {
			if (!containsKey(key)) 
				return 0.0f;

			int pos= ((Integer)fPositions.get(key)).intValue() + 1;
			
			return (float)pos / (float)fHistory.size();
		}
		
		/**
		 * Absolute position of object denoted by key in the
		 * history or -1 if !containsKey(key). The higher the
		 * newer.
		 * 
		 * @param key The key of the object to inspect
		 * @return value between 0 and MAX_HISTORY_SIZE - 1, or -1
		 */
		public synchronized int getPosition(Object key) {
			if (!containsKey(key))
				return -1;
			
			return ((Integer)fPositions.get(key)).intValue();
		}

		public void load(IMemento memento) {
			
			XMLMemento historyMemento = (XMLMemento) memento.getChild(fRootNodeName);
			
			if (historyMemento == null) return;
			
			IMemento[] mementoElements = historyMemento.getChildren(fInfoNodeName);
			for (int i= 0; i < mementoElements.length; ++i) {
				IMemento mementoElement = mementoElements[i];
				Object object= createFromElement(mementoElement);
				if (object != null)
					fHistory.put(getKey(object), object);
			}
			rebuildPositions();
		}
		
		public void save(IMemento memento) {
			
			IMemento historyMemento = memento.createChild(fRootNodeName);
			
			Iterator values= getValues().iterator();
			while (values.hasNext()) {
				Object object= values.next();
				IMemento elementMemento= historyMemento.createChild(fInfoNodeName);
				setAttributes(object, elementMemento);
			}
			
		}
		
		protected Set getKeys() {
			return fHistory.keySet();
		}
		
		protected Collection getValues() {
			return fHistory.values();
		}
		
		/**
		 * Store <code>Object</code> in <code>Element</code>
		 * 
		 * @param object The object to store
		 * @param element The Element to store to
		 */
		protected abstract void setAttributes(Object object, IMemento element);
		
		/**
		 * Return a new instance of an Object given <code>element</code>
		 * 
		 * @param element The element containing required information to create the Object
		 */
		protected abstract Object createFromElement(IMemento element);
		
		/**
		 * Get key for object
		 * 
		 * @param object The object to calculate a key for, not null
		 * @return The key for object, not null
		 */
		protected abstract Object getKey(Object object);
		
		private void rebuildPositions() {
			fPositions.clear();
			Collection values= fHistory.values();
			int pos=0;
			for (Iterator iter= values.iterator(); iter.hasNext();) {
				Object element= iter.next();
				fPositions.put(getKey(element), new Integer(pos));
				pos++;
			}
		}

	}

}
