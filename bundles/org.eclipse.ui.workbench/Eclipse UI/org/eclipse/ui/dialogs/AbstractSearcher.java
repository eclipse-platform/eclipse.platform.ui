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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
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
 * AbstractSearcher represents set of solutions to searching different elements fg. resources or classes. 
 * This elements are presents in AbstractSearchDialog.
 * 
 * <strong>EXPERIMENTAL</strong> This class or interface has been added as part
 * of a work in progress. This API may change at any given time. Please do not
 * use this API without consulting with the Platform/UI team.
 * 
 * @since 3.3
 */
public abstract class AbstractSearcher {
	
	/**
	 * Filter param to set searching pattern.
	 * It's set by <code> setFilterParam(int param, Object value) <code> method where value is a string pattern.
	 */
	public static final int PATTERN = 1;

	private SearcherHistory searcherHistory;
	
	private UIJob refreshJob;

	private SearcherModel searcherModel;
	
	private AbstractSearchJob searchJob;
	
	private int searchJobTicket;

	private List lastCompletedResult;
	
	private SearchFilter filter;
	
	private SearchFilter lastComplitedFilter = null;
	
	/**
	 * 
	 */
	public AbstractSearcher() {
		this.searcherHistory = new SearcherHistory();
		this.searcherModel = new SearcherModel();
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
	public void setFilterParam(int param, Object value){
		if (param == PATTERN) {
			setSearchPattern((String) value);
		}
	}

	/**
	 * Validate and return status of the object
	 * 
	 * @param item
	 * @return status of the item
	 */
	public abstract IStatus validateElement(Object item);
	
	/**
	 * Remove selected items form history set.
	 * @param items
	 */
	public void removeSelectedItems(List items) {
		searcherModel.removeElements(items);
	}
	
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
		searcherHistory.load(memento);
	}
	/**
	 * Save a state of history of selected elements to memento
	 * 
	 * @param memento
	 */
	public void saveState(IMemento memento) {
		searcherHistory.save(memento);
	}
	
	/**
	 * Get matched elements
	 * 
	 * @return searched elements
	 */
	public Object[] getElements(){
		return searcherModel.getElements();
	}

	/**
	 * Get progress message
	 * 
	 * @return progress message
	 */
	public String getProgressMessage(){
		return searcherModel.getProgressMessage();
	}
	
	private void setSearchPattern(String text) {
		stop();
		if (text.length() == 0) { 
			filter = null;
			reset();
		} else {
			filter = createFilter(text);
			scheduleSearchJob();
		}
	}
	/**
	 * Create instance of filter. It could be override to change behaviours of filtering.
	 * 
	 * @param text
	 * @return filter to matching elements
	 */
	protected abstract SearchFilter createFilter(String text);
	
	/**
	 * Reset searcher. It could be override to change it's behaviour.
	 *
	 */
	protected void reset() {
		searcherModel.reset();
	}
	
	/**
	 * Schedule search job. Depend on filter it decide which job will be schedule.
	 * If last searching done(last complited filter is not null) and new filter is subfilter of last one 
	 * it schedule job schearching in cache.
	 * If it is first searching or new filter isn't subfilter of last one it schedule full seach. 
	 *
	 */
	protected void scheduleSearchJob() {
		searchJobTicket++;
		if (lastComplitedFilter != null && lastComplitedFilter.isSubFilter(filter)) {
			searchJob = new CachedResultSearchJob(searchJobTicket, lastCompletedResult, searcherModel, filter);
		} else {
			lastComplitedFilter = null;
			lastCompletedResult = null;
			searchJob = new SearchJob(searchJobTicket, searcherModel, this, filter);
		}
		searchJob.schedule();
	}
	
	/**
	 * Data model for searcher. It collects matched elements. It is resist to concurrent access.
	 * It conatains one synchronized sorted set for collecting and sorting data elements using comparator.
	 * Comparator is return by getElementComparator() method implemented in searcher.
	 */
	protected class SearcherModel {
		
		private SortedSet elements;
		
		private String progressMessage = ""; //$NON-NLS-1$
		
		/**
		 * Create model using synchronized sorted set.
		 */
		public SearcherModel(){
			this.elements = Collections.synchronizedSortedSet(new TreeSet(getElementsComparator()));
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
			SortedSet sortedElements = new TreeSet(getHistoryComparator());
			sortedElements.addAll(Arrays.asList(elements.toArray()));
			return sortedElements.toArray();
		}
		
		/**
		 * Get searched elements
		 * @return searched elements
		 */
		public List getElementsList(){
			SortedSet sortedElements = new TreeSet(getHistoryComparator());
			sortedElements.addAll(Arrays.asList(elements.toArray()));
			return new ArrayList(sortedElements);
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
		
		/**
		 * Remove elements form model
		 * @param items
		 */
		public void removeElements(List items){
			for (Iterator iter = items.iterator(); iter.hasNext();) {
				AbstractSearchItem item = (AbstractSearchItem) iter.next();
				item.unmarkHistory();
				searcherHistory.removeKey(item);
				if (lastCompletedResult != null)
					lastCompletedResult.remove(item);
			}
			refresh();
		}

	}
	
	/**
	 * Returns comparator to sort elements inside model.
	 * 
	 */
	protected abstract Comparator getElementsComparator();
	
	/**
	 * Get hitory comparator
	 * 
	 * @return decorated comparator 
	 */
	private Comparator getHistoryComparator() {
		return new Comparator() {
		
			public int compare(Object o1, Object o2) {
				AbstractSearchItem searchItem1 = ((AbstractSearchItem) o1);
				AbstractSearchItem searchItem2 = ((AbstractSearchItem) o2);
				
				if ((searchItem1.isHistory() && searchItem2
						.isHistory())
						|| (!searchItem1.isHistory() && !searchItem2
								.isHistory()))
					return getElementsComparator().compare(o1, o2);
				
				if (searchItem1.isHistory())
					return -1;
				if (searchItem2.isHistory())
					return +1;

				return 0;
			}
		
		};
	}
	
	/**
	 * SearcherProgressMonitor to monitoring progress of searching process.
	 * It updates progress message and refresh dialog after concrete part of work.
	 * 
	 * @since 3.3
	 *
	 */
	protected static class SearcherProgressMonitor extends ProgressMonitorWrapper {

		private SearcherModel model;
		private String name;
		private int totalWork;
		private double worked;
		private boolean done;
			
		/**
		 * @param monitor
		 * @param model
		 */
		public SearcherProgressMonitor(IProgressMonitor monitor, SearcherModel model) {
			super(monitor);
			this.model = model;
		}
		
		public void setTaskName(String name) {
			super.setTaskName(name);
			this.name= name;
		}
		
		public void beginTask(String name, int totalWork) {
			super.beginTask(name, totalWork);
			if (this.name == null)
				this.name= name;
			this.totalWork= totalWork;
		}
		
		public void worked(int work) {
			super.worked(work);
			internalWorked(work);
		}
		
		public void done() {
			done= true;
			model.setProgressMessage(""); //$NON-NLS-1$
			model.refresh();
			super.done();
		}
		
		/**
		 * 
		 */
		public void internalWorked(double work) {
			worked= worked + work;
			if ((((int)(((worked - work)  * 10) / totalWork)) < ((int)((worked  * 10) / totalWork))) || (((int)((worked  * 10) / totalWork)) == 0))
				model.setProgressMessage(getMessage());
		}
		
		private String getMessage() {
			if (done) {
				return ""; //$NON-NLS-1$
			} else if (totalWork == 0) {
				return name;
			} else {
				return MessageFormat.format( "{0} ({1}%)" //$NON-NLS-1$
						, new Object[] { name, new Integer((int)((worked * 100) / totalWork)) });
			}
		}
	}
	
	/**
	 * Abstract Job 
	 * 
	 * @since 3.3
	 *
	 */
	private abstract class AbstractJob extends Job {
		
		protected SearcherModel model;
		
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
	
	}
	
	/**
	 * Abstract job for searching elements. 
	 * It is a pattern job for searching elements in cache and full searching.
	 * 
	 * @since 3.3
	 *
	 */
	protected abstract class AbstractSearchJob extends AbstractJob {
		
		protected int ticket;
		
		protected SearchFilter searchFilter;
		
		protected AbstractSearchJob(int ticket, SearcherModel model, SearchFilter searchFilter) {
			super(WorkbenchMessages.AbstractSearcher_job_label, model);
			this.ticket = ticket;
			this.searchFilter= searchFilter;
		}
		
		/**
		 * Stop job 
		 */
		public void stop() {
			cancel();
		}
		
		protected IStatus doRun(SearcherProgressMonitor monitor) {
			try {
				internalRun(monitor);
			} catch (CoreException e) {
				searchJobFailed(ticket, e);
				return new Status(IStatus.ERROR, WorkbenchPlugin.PI_WORKBENCH, IStatus.ERROR, WorkbenchMessages.AbstractSearcher_job_error, e);
			//} catch (InterruptedException e) {
		//		return canceled(e, true);
			} catch (OperationCanceledException e) {
				return canceled(e);
			}
			searchJobDone(ticket);
			return ok();	
		}
		
		/**
		 * Search elements using filter
		 * 
		 * @param filteredHistory set of history elements
		 * @param monitor for monitoring progress
		 * @throws CoreException
		 */
		protected abstract void searchResults(Set filteredHistory, SearcherProgressMonitor monitor) throws CoreException ;
		
		/**
		 * Get filtered history
		 * 
		 * @return lit of filtered history elements
		 */
		protected abstract List getFilteredHistory();
		
		/**
		 * Main method for jobs.
		 * 
		 * @param monitor
		 * @throws CoreException
		 */
		private void internalRun(SearcherProgressMonitor monitor) throws CoreException {
			
			if (monitor.isCanceled())
				throw new OperationCanceledException();
			
			model.reset();
			
			List elements = getFilteredHistory();
			
			if (searchJobTicket != ticket) return ;
			
			model.setHistory(elements.toArray());
			
			searchResults(new HashSet(elements), monitor);
			
			model.refresh();
		
			if (monitor.isCanceled())
				throw new OperationCanceledException();			

		}
		
		private IStatus canceled(Exception e) {
			searchJobCanceled(ticket);
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
	
	private void searchJobCanceled(int ticket) {
		if (ticket == searchJobTicket)
			searchJob= null;
	}
	
	protected void stop() {
		if (searchJob != null) {
			searchJob.stop();
			searchJob= null;
		}
	}
	
	/**
	 * Search matches element stored in cache. 
	 * 
	 * @since 3.3
	 * 
	 */
	private class CachedResultSearchJob extends AbstractSearchJob {
		private List lastResult;

		/**
		 * @param ticket of job
		 * @param lastResult is list of last complited searched results (cache)
		 * @param model to collect cearched elements
		 * @param searchFilter for filtering elements
		 */
		public CachedResultSearchJob(int ticket, List lastResult, SearcherModel model, SearchFilter searchFilter) {
			super(ticket, model, searchFilter);
			this.lastResult = lastResult;
		}

		protected void searchResults(Set filteredHistory, SearcherProgressMonitor monitor) {
			
			for (Iterator iter = lastResult.iterator(); iter.hasNext();) {
				AbstractSearchItem searchitem = (AbstractSearchItem) iter.next();
				if (filteredHistory.contains(searchitem))
					continue;
				if (searchFilter == getFilter() && searchFilter.matchesElement(searchitem))
					if (searchJobTicket == ticket) model.addElement(searchitem);
			}
			
		}

		protected List getFilteredHistory() {
		//	Collection values = getHistoryElements();
			List result = new ArrayList();
		/*	for (Iterator iter = values.iterator(); iter.hasNext();) {
				AbstractSearchItem searchitem = (AbstractSearchItem) iter.next();
				
				if (searchFilter == getFilter() && searchFilter.matchesElement(searchitem))
					if (searchFilter.isConsistentElement(searchitem))
						result.add(searchitem);
					else
						removeHistoryElement(searchitem);
			}*/
			return result;
		}
	}
	
	/**
	 * Search matched elements in indicated set and in history.
	 * During searching it refresh progres Monitor.
	 * 
	 * @since 3.3
	 * 
	 */
	private class SearchJob extends AbstractSearchJob {

		private AbstractSearcher searcher;

		/**
		 * @param ticket of job
		 * @param model to collect cearched elements
		 * @param searcher 
		 * @param searchFilter for filtering elements
		 */
		public SearchJob(int ticket, SearcherModel model, AbstractSearcher searcher, SearchFilter searchFilter) {
			super(ticket, model, searchFilter);
			this.searcher = searcher;
		}

		public void stop() {
			super.stop();
		}

		protected List getFilteredHistory() {
			Collection values = getHistoryElements();
			List result = new ArrayList();
			for (Iterator iter = values.iterator(); iter.hasNext();) {
				AbstractSearchItem searchItem = (AbstractSearchItem) iter.next();
				
				if (searchFilter == getFilter() && searchFilter.matchesElement(searchItem))
					if (searchFilter.isConsistentElement(searchItem))
						result.add(searchItem);
					else
						removeHistoryElement(searchItem);
			}
			return result;
		}

		protected void searchResults(Set filteredHistory, SearcherProgressMonitor monitor) throws CoreException {
			
			SearcherProgressMonitor progress = new SearcherProgressMonitor(monitor, model);
			
			searchElements(model, progress, searchFilter);

			searcher.rememberResult(ticket, this.model.getElementsList());
		}
	}
	
	/**
	 * Search elements mtehod used by SearchJob. 
	 * It get elements filtered elements and add it to model. 
	 * During this operation searchPorgress and dialog is refreshing.
	 * 
	 * @param model to collect elements
	 * @param searchProgress to monitoring progress
	 * @param filter to filtering elements
	 * @throws CoreException
	 */
	protected abstract void searchElements(SearcherModel model, SearcherProgressMonitor searchProgress , SearchFilter filter) throws CoreException;
	
	private void rememberResult(int ticket, final List result) {
		if (ticket == searchJobTicket) {
			if (lastCompletedResult == null) {
				lastComplitedFilter = filter;
				lastCompletedResult = result;
			}
		}
	}
	
	/**
	 * History stores a list of key, object pairs. The list is bounded at size
	 * MAX_HISTORY_SIZE. If the list exceeds this size the eldest element is removed
	 * from the list. An element can be added/renewed with a call to <code>accessed(Object)</code>. 
	 * 
	 * The history can be stored to/loaded from an xml file.
	 * @since 3.3
	 *
	 */
	private class SearcherHistory {

		private static final String DEFAULT_ROOT_NODE_NAME= "historyRootNode"; //$NON-NLS-1$
		private static final String DEFAULT_INFO_NODE_NAME= "infoNode"; //$NON-NLS-1$
		private static final int MAX_HISTORY_SIZE= 60;

		private final Map history;
		private final String rootNodeName;
		private final String infoNodeName;
			
		private SearcherHistory(String rootNodeName, String infoNodeName) {
			history = Collections.synchronizedMap(new LinkedHashMap(80, 0.75f, true) {
				private static final long serialVersionUID= 1L;
				protected boolean removeEldestEntry(Map.Entry eldest) {
					return size() > MAX_HISTORY_SIZE;
				}
			});
			this.rootNodeName= rootNodeName;
			this.infoNodeName= infoNodeName;
		}
		
		/**
		 * 
		 */
		public SearcherHistory() {
			this(DEFAULT_ROOT_NODE_NAME, DEFAULT_INFO_NODE_NAME);
		}

		/**
		 * @param object
		 */
		public synchronized void accessed(Object object) {
			history.put(object, object);
		}
		/**
		 * @param object
		 * @return true if history caontains object
		 * 			false in other way
		 */
		public synchronized boolean contains(Object object) {
			return history.containsKey(object);
		}
		/**
		 * @param key
		 * @return true if history contains key object
		 * 			false in other way
		 */
		public synchronized boolean containsKey(Object key) {
			return history.containsKey(key);
		}
		
		/**
		 * @return true if history is empty
		 */
		public synchronized boolean isEmpty() {
			return history.isEmpty();
		}
		
		/**
		 * @param object to remove form the history
		 * @return removed object
		 */
		public synchronized Object remove(Object object) {
			Object removed = history.remove(object);
			return removed;
		}
		
		/**
		 * @param key to remove form the history 
		 * @return removed key
		 */
		public synchronized Object removeKey(Object key) {
			Object removed= history.remove(key);
			return removed;
		}
		
		/**
		 * Load history elements from memento.
		 * 
		 * @param memento 
		 */
		public void load(IMemento memento) {
			
			XMLMemento historyMemento = (XMLMemento) memento.getChild(rootNodeName);
			
			if (historyMemento == null) return;
			
			IMemento[] mementoElements = historyMemento.getChildren(infoNodeName);
			for (int i= 0; i < mementoElements.length; ++i) {
				IMemento mementoElement = mementoElements[i];
				Object object= createFromElement(mementoElement);
				if (object != null)
					history.put(object, object);
			}
		}
		
		/**
		 * Save history elements to memento
		 * 
		 * @param memento
		 */
		public void save(IMemento memento) {
			
			IMemento historyMemento = memento.createChild(rootNodeName);
			
			Iterator values= getValues().iterator();
			while (values.hasNext()) {
				Object object= values.next();
				IMemento elementMemento= historyMemento.createChild(infoNodeName);
				setAttributes(object, elementMemento);
			}
			
		}
		
		protected Set getKeys() {
			return history.keySet();
		}
		
		/**
		 * @return collection of history elements
		 */
		public synchronized Collection getValues() {
			return Collections.synchronizedCollection(history.values());
		}

	}
	
	public Collection getHistoryElements() {
		return searcherHistory.getValues();
	}
	
	private Object removeHistoryElement(AbstractSearchItem searchItem){
		return searcherHistory.removeKey(searchItem);
	}
	
	/**
	 * @param key
	 * @return true if history contains key
	 * 			false in other way
	 */
	public boolean isHistoryContainsKey(Object key){
		return searcherHistory.containsKey(key);
	}
	
	protected void accessedHistory(AbstractSearchItem searchItem){
		searcherHistory.accessed(searchItem);
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
	 * Filters elements using searchPatter for comparation name of resources with pattern. 
	 * 
	 * @since 3.3
	 *
	 */
	protected abstract class SearchFilter {

		private String text;

		private SearchPattern nameMatcher;
		
		/**
		 * @param text of the pattern
		 */
		public SearchFilter(String text) {
			this.text = text;
			nameMatcher = new SearchPattern(text);
		}

		/**
		 * @return text of the pattern
		 */
		public String getText() {
			return text;
		}

		/**
		 * Check if <code>SearchFilter filter</code> is sub-filter of this.
		 * In basic version it depends on pattern. 
		 * It will be override to change behaviour of Searcher.
		 * 
		 * @param filter
		 * @return true if filter is sub-filter of this
		 * 			false if filter isn't sub-filter
		 */
		public boolean isSubFilter(SearchFilter filter) {
			if (filter != null && filter.getNamePattern().startsWith(this.text)) {
				return true;
			} 
			return false;
		}

		/**
		 * @return true if text is camelCase pattern
		 * 			false if text don't implement camelCase cases
		 */
		public boolean isCamelCasePattern() {
			return nameMatcher.getMatchKind() == SearchPattern.R_CAMELCASE_MATCH;
		}
		
		/**
		 * Set new pattern
		 * @param namePattern
		 */
		public void setNamePattern(String namePattern){
			nameMatcher = new SearchPattern(namePattern);
		}

		/**
		 * @return pattern for this filter
		 */
		public String getNamePattern() {
			return nameMatcher.getPattern();
		}

		/**
		 * @return search flag
		 */
		public int getSearchFlags() {
			return nameMatcher.getMatchKind();
		}
		
		protected boolean matchesName(String name) {
			return nameMatcher.matches(name);
		}
		
		/**
		 * @param element
		 * @return true if element matches wit
		 * 			false 
		 */
		public abstract boolean matchesElement(Object element) ;
		
		/**
		 * Check consistency of elements. 
		 * Element is inconsitent if is changed element or remove 
		 * 
		 * @param searchitem
		 * @return true if element is consistent
		 * 			false if element is inconsitent
		 */
		public abstract boolean isConsistentElement(Object searchitem);
		
	}
	
	/**
	 * Get filter
	 * @return current filter
	 */
	public SearchFilter getFilter(){
		return this.filter;
	}
	/**
	 * Set new filter
	 * @param searchFilter 
	 */
	public void setFilter(SearchFilter searchFilter){
		this.filter = searchFilter;
	}
	
	public Object removeHistoryElement(Object element) {
		return this.searcherHistory.remove(element);
	}

}
