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
import java.util.Comparator;
import java.util.List;

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
 * <strong>EXPERIMENTAL</strong> This class or interface has been added as part
 * of a work in progress. This API may change at any given time. Please do not
 * use this API without consulting with the Platform/UI team.
 * 
 * ResourceSearcher is an implementation of AbstractSearcher to searching
 * resources in workspace.
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

	private static Collator collator = Collator.getInstance();

	private IContainer container;

	private int typeMask;
	
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
		ResourceSearchItem resourceSearchItem = (ResourceSearchItem) item;
		accessedHistory(resourceSearchItem);
		return resourceSearchItem.getResource();
	}

	/* 
	 * 
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.dialogs.AbstractSearcher#setFilterParam(int,
	 *      java.lang.Object)
	 */
	public void setFilterParam(int param, Object value) {
		
		super.setFilterParam(param, value);
		
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
	
	private void setSearchDerived(boolean isDerived) {

		SearchFilter searchFilter = getFilter();
		
		if (searchFilter!=null) {
			reset();
			setFilter(createFilter(searchFilter.getNamePattern(), this.container, isDerived, this.typeMask));
			scheduleSearchJob();
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
	/*protected boolean getShowDerived() {
		return showDerived;
	}*/

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
	 * During visit resources they update progress monitor and add matched resources to model.
	 * 
	 * @since 3.3
	 */
	private class ResourceProxyVisitor implements IResourceProxyVisitor {

		private SearcherModel model;

		private IProgressMonitor progressMonitor;

		private List projects;
		
		private ResourceFilter resourceFilter;
		
		private boolean isDone = false;

		/**
		 * @param model 
		 * @param progressMonitor 
		 * @param filter 
		 * @throws CoreException
		 */
		public ResourceProxyVisitor(SearcherModel model, IProgressMonitor progressMonitor, SearchFilter filter)
				throws CoreException {
			super();
			this.model = model;
			this.progressMonitor = progressMonitor;
			this.resourceFilter = (ResourceFilter) filter;
			this.projects = new ArrayList(Arrays.asList(container.members()));

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.resources.IResourceProxyVisitor#visit(org.eclipse.core.resources.IResourceProxy)
		 */
		public boolean visit(IResourceProxy proxy) {
			
			if (isDone) return false;
			
			if (getFilter() != this.resourceFilter) { 
				progressMonitor.done();
				isDone = true;
			}
			
			if (!(this.resourceFilter.isShowDerived()) && proxy.isDerived()) {
				return false;
			}
			
			IResource res = proxy.requestResource();
			ResourceSearchItem searchItem = new ResourceSearchItem(res);
			
			if (this.projects.remove((res.getProject())) || this.projects.remove((res))) {
				progressMonitor.worked(1);
			}
			
			int type = proxy.getType();
			if ((typeMask & type) != 0) {
				if (resourceFilter == getFilter() && resourceFilter.matchesElement(searchItem)) {
					if (validateSearchedResource(res)) {
						if (!isHistoryContainsKey(new ResourceSearchItem(res)))
							model.addElement(searchItem);
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
	 * Filters resources using pattern and showDerived flag.
	 * It override SearchFilter.
	 * 
	 * @since 3.3
	 *
	 */
	private class ResourceFilter extends SearchFilter{

		private boolean showDerived = false;
		private IContainer container;
		private int typeMask;
		
		/**
		 * @param text
		 * @param showDerived flag which determine showing derived elements
		 */
		public ResourceFilter(String text, IContainer container, boolean showDerived, int typeMask) {
			super(text);
			this.container = container;
			this.showDerived = showDerived;
			this.typeMask = typeMask;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.dialogs.AbstractSearcher.SearchFilter#isConsistentElement(java.lang.Object)
		 */
		public boolean isConsistentElement(Object searchitem) {
			ResourceSearchItem resourceSearchItem = (ResourceSearchItem) searchitem;
			IResource resource = resourceSearchItem.getResource();
			if (this.container.findMember(resource.getFullPath()) != null)
				return true;
			return false;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.dialogs.AbstractSearcher.SearchFilter#matchesElement(java.lang.Object)
		 */
		public boolean matchesElement(Object element) {
			ResourceSearchItem searchItem = (ResourceSearchItem) element;
			IResource resource = searchItem.getResource();
			if ((!this.showDerived && resource.isDerived()) || ((this.typeMask & resource.getType()) == 0))
				return false;
			return matchesName(resource.getName());
		}

		/**
		 * Set showDerived flag
		 * @param showDerived
		 */
		public void setDerived(boolean showDerived){
			this.showDerived = showDerived;
		}
		
		/**
		 * @return true if show derived flag is true 
		 * 			false in other way
		 */
		public boolean isShowDerived(){
			return showDerived;
		}
		
		public boolean isSubFilter(SearchFilter filter) {
			if (!super.isSubFilter(filter)) return false;
			if (filter instanceof ResourceFilter)
				if (this.showDerived == ((ResourceFilter) filter).isShowDerived())
					return true;
			return false;
		}
	
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.AbstractSearcher#createFromElement(org.eclipse.ui.IMemento)
	 */
	protected Object createFromElement(IMemento element) {
		IResource resource = null;
		ResourceFactory resourceFactory = new ResourceFactory();
		resource = (IResource) resourceFactory.createElement(element);
		return new ResourceSearchItem(resource, true);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.AbstractSearcher#setAttributes(java.lang.Object, org.eclipse.ui.IMemento)
	 */
	protected void setAttributes(Object object, IMemento element) {
		IResource resource = ((ResourceSearchItem) object).getResource();
		ResourceFactory resourceFactory = new ResourceFactory(resource);
		resourceFactory.saveState(element);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.AbstractSearcher#createFilter(java.lang.String)
	 */
	protected SearchFilter createFilter(String text) {
		return createFilter(text, container, false, typeMask);
	}
	
	protected SearchFilter createFilter(String text, IContainer container, boolean showDerived, int typeMask) {
		return new ResourceFilter(text, container, showDerived, typeMask);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.AbstractSearcher#searchElements(org.eclipse.ui.dialogs.AbstractSearcher.SearcherModel, org.eclipse.ui.dialogs.AbstractSearcher.SearcherProgressMonitor, org.eclipse.ui.dialogs.AbstractSearcher.SearchFilter)
	 */
	protected void searchElements(SearcherModel model, SearcherProgressMonitor searchProgress, SearchFilter filter) throws CoreException {
		
		if (searchProgress != null)
			searchProgress.beginTask("", container.members().length); //$NON-NLS-1$

		container.accept(new ResourceProxyVisitor(model, searchProgress, filter), IResource.NONE);

		if (searchProgress != null)
			searchProgress.done();
		
	}
}
	
