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
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.ide.model.ResourceFactory;
import org.eclipse.ui.utils.ResourceSearchItem;

import com.ibm.icu.text.Collator;

/**
 * ResourceSearcher is an implementation of AbstractSearcher to searching
 * resources in workspace.
 * 
 * <strong>EXPERIMENTAL</strong> This class or interface has been added as part
 * of a work in progress. This API may change at any given time. Please do not
 * use this API without consulting with the Platform/UI team.
 * 
 * @since 3.3
 * @see AbstractSearcher
 */
public class ResourceSearcher extends AbstractSearcher {

	/**
	 * Filter param to change manner of searching. When its filter param is
	 * true, ResourceSearcher show all derived resources. It's set by
	 * <code> setFilterParam(int param, Object value) <code> method.
	 */
	public static final int DERIVED = 2;

	private List lastCompletedResult;

	private static Collator collator = Collator.getInstance();
	
	private ResourceFilter filter;
	
	private ResourceFilter lastComplitedFilter = null;

	private IContainer container;

	private int typeMask;

	private boolean showDerived = false;
	

	/**
	 * Creates a ResourceSearcher
	 * 
	 * @param container
	 *            which contains other resources
	 * @param typeMask
	 *            to filter type of serached elements
	 */
	public ResourceSearcher(IContainer container, int typeMask) {
		this.container = container;
		this.typeMask = typeMask;
		this.model = new SearcherModel();
		this.history = ResourceHistory.getInstance();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.dialogs.AbstractSearcher#getComparator()
	 */
	protected Comparator getElementsComparator() {
		return new ResourceComparator();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.dialogs.AbstractSearcher#getDetails(java.lang.Object)
	 */
	public Object getDetails(Object item) {
		return ((ResourceSearchItem) item).getResource().getParent();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.dialogs.AbstractSearcher#getObjectToReturn(java.lang.Object)
	 */
	public Object getObjectToReturn(Object item) {
		ResourceHistory history = ResourceHistory.getInstance();
		ResourceSearchItem resourceDecorator = (ResourceSearchItem) item;
		history.accessed(resourceDecorator.getResource());
		return resourceDecorator.getResource();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.dialogs.AbstractSearcher#setFilterParam(int,
	 *      java.lang.Object)
	 */
	public void setFilterParam(int param, Object value) {
		if (param == PATTERN) {
			setSearchPattern((String) value);
		}
		
		if (param == DERIVED) {
			setSearchDerived(((Boolean) value).booleanValue());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.dialogs.AbstractSearcher#validateElement(java.lang.Object)
	 */
	public IStatus validateElement(Object item) {
		return new Status(IStatus.OK, WorkbenchPlugin.PI_WORKBENCH, 0, "", null); //$NON-NLS-1$
	}

	/**
	 * Use this method to further filter resources. As resources are gathered,
	 * if a resource matches the current pattern string, this method will be
	 * called. If this method answers false, the resource will not be included
	 * in the list of matches and the resource's children will NOT be considered
	 * for matching.
	 */
	protected boolean validateSearchedResource(IResource resource) {
		return true;
	}

	private void setSearchPattern(String text) {
		stop(false, false);
		if (text.length() == 0) { //|| "*".equals(text)) { 
				filter = null;
				model.reset();
		} else {
			filter = new ResourceFilter(text);
			scheduleSearchJob();
		}
	}
	
	private void setSearchDerived(boolean isDerived) {
		showDerived = isDerived;
		if (isDerived)
			lastComplitedFilter = null;
		if (filter!=null)
			scheduleSearchJob();
	}

	/**
	 * Adjust the pattern string for matching.
	 */
	protected String adjustPattern(String textPattern) {
		String text = textPattern.trim();
		if (text.endsWith("<")) { //$NON-NLS-1$
			// the < character indicates an exact match search
			return text.substring(0, text.length() - 1);
		}
		if (!text.equals("") && !text.endsWith("*")) { //$NON-NLS-1$ //$NON-NLS-2$
			return text + "*"; //$NON-NLS-1$
		}
		return text;
	}

	private void scheduleSearchJob() {
		searchJobTicket++;
		if (lastComplitedFilter != null
				&& lastComplitedFilter.isSubFilter(filter.getNamePattern())) {
			searchJob = new ResourceCachedResultJob(searchJobTicket,
					lastCompletedResult, model, (ResourceHistory) history);
		} else {
			lastComplitedFilter = null;
			lastCompletedResult = null;
			searchJob = new ResourceSearchEngineJob(searchJobTicket, model,
					(ResourceHistory) history, this);
		}
		searchJob.schedule();
	}

	private void rememberResult(int ticket, final List result) {
		if (ticket == searchJobTicket) {
			if (lastCompletedResult == null) {
				lastComplitedFilter = filter;
				lastCompletedResult = result;
			}
		}
	}

	/**
	 * Returns whether derived resources should be shown in the list. The
	 * default is <code>false</code>.
	 * 
	 * @return <code>true</code> to show derived resources, <code>false</code>
	 *         to hide them
	 * @since 3.1
	 */
	protected boolean getShowDerived() {
		return showDerived;
	}

	/**
	 * ResourceCachedResultJob to search resources in cache.
	 * 
	 * @since 3.3
	 * 
	 */
	private class ResourceCachedResultJob extends AbstractSearchJob {
		private List lastResult;

		/**
		 * @param ticket
		 * @param lastResult
		 * @param model
		 * @param history
		 * @param mode
		 */
		public ResourceCachedResultJob(int ticket, List lastResult,
				SearcherModel model, ResourceHistory history) {
			super(ticket, model, history);
			this.lastResult = lastResult;
		}

		protected void searchResults(Set filteredHistory,
				SearcherProgressMonitor monitor) throws CoreException {
			model.reset();
			for (Iterator iter = lastResult.iterator(); iter.hasNext();) {
				ResourceSearchItem resource = (ResourceSearchItem) iter.next();
				if (!getShowDerived() && resource.getResource().isDerived())
					continue;
				if (filteredHistory.contains(resource.getResource()))
					continue;
				if (filter.matchesElement(resource.getResource()))
					model.addElement(resource);
			}
			model.refresh();

		}

		protected List getFilteredHistory() {
			List elements = new ArrayList();
			IResource[] matchingResources = ((ResourceHistory) history)
					.getMatchedElements(filter, container);

			if (matchingResources.length > 0) {
				for (int i = 0; i < matchingResources.length; i++) {
					IResource resource = matchingResources[i];
					if ((!getShowDerived() && resource.isDerived())
							|| ((typeMask & resource.getType()) == 0)) {
						continue;
					}
					elements.add(new ResourceSearchItem(resource, true));
				}
			}
			return elements;
		}
	}

	/**
	 * ResourceSearchEngineJob to search matched resources in indicated reources
	 * container and in resource history. It decorate results and mark
	 * duplicates. During searching this refresh progres Monitor
	 * 
	 * @since 3.3
	 * 
	 */
	private class ResourceSearchEngineJob extends AbstractSearchJob {

		private ResourceSearcher resourceSearcher;

		public ResourceSearchEngineJob(int ticket, SearcherModel model,
				ResourceHistory history, ResourceSearcher resourceSearcher) {
			super(ticket, model, history);
			this.resourceSearcher = resourceSearcher;
		}

		public void stop() {
			super.stop();
		}

		protected List getFilteredHistory() {
			List elements = new ArrayList();
			IResource[] matchingResources = ((ResourceHistory) history)
					.getMatchedElements(filter, container);

			if (matchingResources.length > 0) {
				for (int i = 0; i < matchingResources.length; i++) {
					IResource resource = matchingResources[i];
					if ((!getShowDerived() && resource.isDerived())
							|| ((typeMask & resource.getType()) == 0)) {
						continue;
					}
					elements.add(new ResourceSearchItem(resource, true));
				}
			}
			return elements;
		}

		protected void searchResults(Set filteredHistory,
				SearcherProgressMonitor monitor) throws CoreException {
			SearcherProgressMonitor progress = new SearcherProgressMonitor(
					monitor, model);

			if (progress != null)
				progress.beginTask("", container.members().length); //$NON-NLS-1$

			container.accept(new ResourceProxyVisitor(model, progress,
					(ResourceHistory) this.history, container, filter), IResource.NONE);

			if (progress != null)
				progress.done();

			ArrayList resources = new ArrayList(Arrays.asList(this.model
					.getElements()));
			resourceSearcher.rememberResult(fTicket, resources);
		}
	}

	/**
	 * ResourceComparator to caompare resources and mark duplicates.
	 * 
	 * @since 3.3
	 * 
	 */
	public static class ResourceComparator implements Comparator {

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(Object o1, Object o2) {
			ResourceSearchItem resourceDecorator1 = ((ResourceSearchItem) o1);
			ResourceSearchItem resourceDecorator2 = ((ResourceSearchItem) o2);
			IResource resource1 = resourceDecorator1.getResource();
			IResource resource2 = resourceDecorator2.getResource();
			String s1 = resource1.getName();
			String s2 = resource2.getName();
			int comparability = collator.compare(s1, s2);
			if (comparability == 0) {
				resourceDecorator1.markAsDuplicate();
				resourceDecorator2.markAsDuplicate();
				s1 = resource1.getFullPath().toString();
				s2 = resource2.getFullPath().toString();
				comparability = collator.compare(s1, s2);
			}

			return comparability;
		}
	}

	/**
	 * ResourceProxyVisitor to visit resource tree and get matched resources.
	 * During visit resources they update progress monitor.
	 * 
	 * @since 3.3
	 */
	private class ResourceProxyVisitor implements IResourceProxyVisitor {

		SearcherModel model;

		IProgressMonitor progressMonitor;

		ResourceHistory resourceHistory;

		IContainer container;

		List projects;
		
		ResourceFilter resourceFilter;
		
		boolean isDone = false;

		/**
		 * @param model
		 * @throws CoreException
		 */
		public ResourceProxyVisitor(SearcherModel model,
				IProgressMonitor progressMonitor,
				ResourceHistory resourceHistory, IContainer container, ResourceFilter filter)
				throws CoreException {
			super();
			this.model = model;
			this.progressMonitor = progressMonitor;
			this.resourceHistory = resourceHistory;
			this.container = container;
			this.resourceFilter = filter;
			this.projects = new ArrayList(Arrays.asList(container.members()));

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.resources.IResourceProxyVisitor#visit(org.eclipse.core.resources.IResourceProxy)
		 */
		public boolean visit(IResourceProxy proxy) throws CoreException {
			
			if (isDone) return false;
			
			if (filter != this.resourceFilter) { 
				progressMonitor.done();
				isDone = true;
			}
			
			if (!getShowDerived() && proxy.isDerived()) {
				return false;
			}
			
			IResource res = proxy.requestResource();
			
			if (this.projects.remove((res.getProject())) || this.projects.remove((res))) {
				progressMonitor.worked(1);
			}
			
			int type = proxy.getType();
			if ((typeMask & type) != 0) {
				if (filter == this.resourceFilter && resourceFilter.matchesElement(res)) {
					if (validateSearchedResource(res)) {
						if (!resourceHistory.contains(res))
							model.addElement(new ResourceSearchItem(res));
						return true;
					}
					return false;
				}
			}
			if (type == IResource.FILE) {
				return false;
			}
			return true;
		}

	}

	/**
	 * ResourceHistory store a history of selected resources.
	 * 
	 * @since 3.3
	 */
	private static class ResourceHistory extends SearcherHistory {

		private static final String NODE_ROOT = "resourceInfoHistroy"; //$NON-NLS-1$

		private static final String NODE_TYPE_INFO = "resourceInfo"; //$NON-NLS-1$

		private static ResourceHistory fgInstance;

		public static synchronized ResourceHistory getInstance() {
			if (fgInstance == null)
				fgInstance = new ResourceHistory();
			return fgInstance;
		}

		private ResourceHistory() {
			super(NODE_ROOT, NODE_TYPE_INFO);
		}

		public synchronized boolean contains(IResource resource) {
			return super.contains(resource);
		}

		public synchronized void accessed(IResource resource) {
			super.accessed(resource);
		}

		public synchronized IResource remove(IResource resource) {
			return (IResource) super.remove(resource);
		}

		public synchronized IResource[] getMatchedElements(
				ResourceFilter filter, IContainer container) {
			Collection values = getValues();
			List result = new ArrayList();
			for (Iterator iter = values.iterator(); iter.hasNext();) {
				IResource resource = (IResource) iter.next();
				if (filter == null || filter.matchesElement(resource))
					if (container.findMember(resource.getFullPath()) != null)
						result.add(resource);
					else
						remove(resource);
			}
			Collections.reverse(result);
			return (IResource[]) result.toArray(new IResource[result.size()]);

		}

		protected Object getKey(Object object) {
			return object;
		}

		protected Object createFromElement(IMemento memento) {
			IResource resource = null;
			ResourceFactory resourceFactory = new ResourceFactory();
			resource = (IResource) resourceFactory.createElement(memento);
			return resource;
		}

		protected void setAttributes(Object object, IMemento resourceElement) {
			IResource resource = (IResource) object;
			ResourceFactory resourceFactory = new ResourceFactory(resource);
			resourceFactory.saveState(resourceElement);
		}

	}
	
	/**
	 * 
	 * Filters resources using searchPatter for comparation name of resources with pattern. 
	 * 
	 * @since 3.3
	 *
	 */
	public class ResourceFilter {

		private String text;

		private SearchPattern nameMatcher;
		
		public ResourceFilter(String text) {
			this.text = text;
			nameMatcher = new SearchPattern(text, true);
		}

		public String getText() {
			return text;
		}

		public boolean isSubFilter(String text) {
			if (text != null && text.startsWith(this.text)) {
				return true;
			} 
			return false;
		}

		public boolean isCamcelCasePattern() {
			return nameMatcher.getMatchKind() == SearchPattern.R_CAMELCASE_MATCH;
		}

		public String getNamePattern() {
			return nameMatcher.getPattern();
		}

		public int getSearchFlags() {
			return nameMatcher.getMatchKind();
		}

		public boolean matchesElement(IResource resource) {
			return matchesName(resource);
		}

		private boolean matchesName(IResource resource) {
			return nameMatcher.matches(resource.getName());
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.AbstractSearcher#getDuplicatesComparator()
	 */
	protected Comparator getEndComparator() {
		return new Comparator() {
		
			public int compare(Object o1, Object o2) {
				ResourceSearchItem resourceDecorator1 = ((ResourceSearchItem) o1);
				ResourceSearchItem resourceDecorator2 = ((ResourceSearchItem) o2);
				IResource resource1 = resourceDecorator1.getResource();
				IResource resource2 = resourceDecorator2.getResource();
				String s1 = resource1.getName();
				String s2 = resource2.getName();
				int comparability = collator.compare(s1, s2);
				if (comparability == 0) {
					s1 = resource1.getFullPath().toString();
					s2 = resource2.getFullPath().toString();
					comparability = collator.compare(s1, s2);
				}

				if ((resourceDecorator1.isHistory() && resourceDecorator2
						.isHistory())
						|| (!resourceDecorator1.isHistory() && !resourceDecorator2
								.isHistory()))
					return comparability;
				if (resourceDecorator1.isHistory())
					return -1;
				if (resourceDecorator2.isHistory())
					return +1;

				return 0;
			}
		
		};
	}

}
