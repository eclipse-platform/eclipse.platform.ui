/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.ui.mapping;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.mapping.ModelProvider;
import org.eclipse.core.resources.mapping.ResourceTraversal;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.*;
import org.eclipse.team.core.delta.*;
import org.eclipse.team.core.mapping.IResourceMappingScope;
import org.eclipse.team.core.mapping.ISynchronizationContext;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.ui.TeamUI;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.navigator.ICommonContentProvider;
import org.eclipse.ui.navigator.IExtensionStateModel;

/**
 * Abstract team aware content provider that delegates to another content provider
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/Team team.
 * </p>
 * 
 * @since 3.2
 */
public abstract class SynchronizationContentProvider implements ICommonContentProvider, IDeltaChangeListener, IPropertyChangeListener {

	private IResourceMappingScope scope;
	private ISynchronizationContext context;
	private Viewer viewer;
	private IExtensionStateModel stateModel;

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(Object parent) {
		if (parent instanceof IResourceMappingScope) {
			IResourceMappingScope rms = (IResourceMappingScope) parent;
			if (rms.getMappings(getModelProviderId()).length > 0) {
				return new Object[] { getModelProvider() };
			}
			return new Object[0];
		} else if (parent instanceof ISynchronizationContext) {
			ISynchronizationContext sc = (ISynchronizationContext) parent;
			if (sc.getScope().getMappings(getModelProviderId()).length > 0) {
				if (filter(parent, getDelegateContentProvider().getChildren(getModelRoot())).length > 0)
					return new Object[] { getModelProvider() };
			}
			return new Object[0];
		}
		if (parent == getModelProvider()) {
			return filter(parent, getDelegateContentProvider().getChildren(getModelRoot()));
		}
		return filter(parent, getDelegateContentProvider().getElements(parent));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
	 */
	public Object getParent(Object element) {
		if (element instanceof ModelProvider)
			return null;
		if (element == getModelRoot())
			return null;
		Object parent = getDelegateContentProvider().getParent(element);
		if (parent == getModelRoot())
			return getModelProvider();
		return parent;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
	 */
	public boolean hasChildren(Object element) {
		if (element instanceof ModelProvider) {
			element = getModelRoot();
		}
		return getDelegateContentProvider().hasChildren(element) && filter(element, getChildren(element)).length > 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof IResourceMappingScope) {
			IResourceMappingScope rms = (IResourceMappingScope) inputElement;
			if (rms.getMappings(getModelProviderId()).length > 0) {
				return new Object[] { getModelProvider() };
			}
			return new Object[0];
		} else if (inputElement instanceof ISynchronizationContext) {
			ISynchronizationContext sc = (ISynchronizationContext) inputElement;
			if (sc.getScope().getMappings(getModelProviderId()).length > 0) {
				if (filter(getModelRoot(), getDelegateContentProvider().getChildren(getModelRoot())).length > 0)
					return new Object[] { getModelProvider() };
			}
			return new Object[0];
		}
		if (inputElement == getModelProvider()) {
			return filter(inputElement, getDelegateContentProvider().getChildren(getModelRoot()));
		}
		return filter(inputElement, getDelegateContentProvider().getElements(inputElement));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	public void dispose() {
		stateModel.removePropertyChangeListener(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.viewer = viewer;
		getDelegateContentProvider().inputChanged(viewer, oldInput, newInput);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.internal.extensions.ICommonContentProvider#init(org.eclipse.ui.navigator.IExtensionStateModel, org.eclipse.ui.IMemento)
	 */
	public void init(IExtensionStateModel aStateModel, IMemento aMemento) {
		stateModel = aStateModel;
		stateModel.addPropertyChangeListener(this);
		scope = (IResourceMappingScope)aStateModel.getProperty(TeamUI.RESOURCE_MAPPING_SCOPE);
		context = (ISynchronizationContext)aStateModel.getProperty(TeamUI.SYNCHRONIZATION_CONTEXT);
		ITreeContentProvider provider = getDelegateContentProvider();
		if (provider instanceof ICommonContentProvider) {
			((ICommonContentProvider) provider).init(aStateModel, aMemento);	
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		// TODO: this could happen at the root as well
		if (event.getProperty().equals(ISynchronizePageConfiguration.P_MODE)) {
			refresh();
		}
	}
	
	/**
	 * Return whether elements with the given synchronization kind (as define in
	 * the {@link SyncInfo} class) should be included in the contents. This
	 * method is invoked by the {@link #getChildrenInContext(Object, Object[]) }
	 * method to filter the list of children returned when {@link #getChildren(Object) }
	 * is called. It accessing the <code>ISynchronizePageConfiguration.P_MODE</code>
	 * property on the state model provided by the view to determine what kinds
	 * should be included.
	 * 
	 * @param direction the synchronization kind as described in the {@link SyncInfo}
	 *            class
	 * @return whether elements with the given synchronization kind should be
	 *         included in the contents
	 */
	protected boolean includeDirection(int direction) {
		int mode = stateModel.getIntProperty(ISynchronizePageConfiguration.P_MODE);
		switch (mode) {
		case ISynchronizePageConfiguration.BOTH_MODE:
			return true;
		case ISynchronizePageConfiguration.CONFLICTING_MODE:
			return direction == IThreeWayDelta.CONFLICTING;
		case ISynchronizePageConfiguration.INCOMING_MODE:
			return direction == IThreeWayDelta.CONFLICTING || direction == IThreeWayDelta.INCOMING;
		case ISynchronizePageConfiguration.OUTGOING_MODE:
			return direction == IThreeWayDelta.CONFLICTING || direction == IThreeWayDelta.OUTGOING;
		default:
			break;
		}
		return true;
	}
	
	/**
	 * Return the synchronization context associated with the view to which
	 * this content provider applies. A <code>null</code> is returned if
	 * no context is available.
	 * @return the synchronization context or <code>null</code>
	 */
	protected ISynchronizationContext getContext() {
		return context;
	}

	/**
	 * Return the resource mapping scope associated with the view to which
	 * this content provider applies. A <code>null</code> is returned if
	 * no scope is available.
	 * @return the resource mapping scope or <code>null</code>
	 */
	protected IResourceMappingScope getScope() {
		return scope;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.IMementoAware#restoreState(org.eclipse.ui.IMemento)
	 */
	public void restoreState(IMemento aMemento) {
		ITreeContentProvider provider = getDelegateContentProvider();
		if (provider instanceof ICommonContentProvider) {
			((ICommonContentProvider) provider).restoreState(aMemento);	
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.IMementoAware#saveState(org.eclipse.ui.IMemento)
	 */
	public void saveState(IMemento aMemento) {
		ITreeContentProvider provider = getDelegateContentProvider();
		if (provider instanceof ICommonContentProvider) {
			((ICommonContentProvider) provider).saveState(aMemento);	
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.core.delta.ISyncDeltaChangeListener#syncDeltaTreeChanged(org.eclipse.team.core.delta.ISyncDeltaChangeEvent, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void deltaChanged(IDeltaChangeEvent event, IProgressMonitor monitor) {
		refresh();
	}

	/**
	 * Refresh the subtree associated with this model.
	 */
	protected void refresh() {
		Utils.syncExec(new Runnable() {
			public void run() {
				TreeViewer treeViewer = ((TreeViewer)getViewer());
				treeViewer.refresh(getModelProvider());
			}
		
		}, getViewer().getControl());
	}

	/**
	 * Return the model content provider that the team aware content
	 * provider delegates to.
	 * @return the model content provider
	 */
	protected abstract ITreeContentProvider getDelegateContentProvider();
	
	/**
	 * Filter the obtained children of the given parent so that only the
	 * desired elements are shown. By default the {@link #getChildrenInScope(Object, Object[]) }
	 * and {@link #getChildrenInContext(Object, Object[]) } methods are used
	 * to filter the set of elements returned from the delegate provider.
	 * Subclass may override.
	 * @param parentElement the parent element
	 * @param children the children
	 * @return the filtered children
	 */
	protected Object[] filter(Object parentElement, Object[] children) {
		children = getChildrenInScope(parentElement, children);
		children = getChildrenInContext(parentElement, children);
		return children;
	}
	
	/**
	 * Return the model provider for this content provider.
	 * @return the model provider for this content provider
	 */
	protected final ModelProvider getModelProvider() {
		try {
			return ModelProvider.getModelProviderDescriptor(getModelProviderId()).getModelProvider();
		} catch (CoreException e) {
			// TODO: this is a bit harsh. can we do something less destructive
			throw new IllegalStateException();
		}
	}
	
	/**
	 * Return the id of model provider for this content provider.
	 * @return the model provider for this content provider
	 */
	protected abstract String getModelProviderId();
	
	/**
	 * Return the object that acts as the model root. It is used when getting the children
	 * for a model provider.
	 * @return the object that acts as the model root
	 */
	protected abstract Object getModelRoot();

	/**
	 * Return the viewer to which the content provider is associated.
	 * @return the viewer to which the content provider is associated
	 */
	protected final Viewer getViewer() {
		return viewer;
	}
	
	/**
	 * Return the subset of the given children that are in the
	 * scope of the content provider or are parents
	 * of elements that are in scope. If the content provider
	 * is not scope (i.e. <code>getScope() == null</code>),
	 * all the children are returned.
	 * @param parent the parent of the given children
	 * @param children all the children of the parent that are in scope.
	 * @return the subset of the given children that are in the
	 * scope of the content provider
	 */
	protected Object[] getChildrenInScope(Object parent, Object[] children) {
		IResourceMappingScope scope = getScope();
		if (scope == null)
			return children;
		List result = new ArrayList();
		for (int i = 0; i < children.length; i++) {
			Object object = children[i];
			if (isInScope(parent, object)) {
				result.add(object);
			}
		}
		return result.toArray(new Object[result.size()]);
	}
	
	/**
	 * Return the subset of children that are of interest from the given context.
	 * If there is no context, all the children are returned.
	 * @param parent the parent of the children
	 * @param children the children
	 * @return the subset of children that are of interest from the given context
	 */
	protected Object[] getChildrenInContext(Object parentElemnt, Object[] children) {
		ISynchronizationContext context = getContext();
		if (context == null)
			return children;
		List result = new ArrayList();
		for (int i = 0; i < children.length; i++) {
			Object object = children[i];
			ResourceTraversal[] traversals = getTraversals(object);
			IDelta[] deltas = context.getDeltas(traversals);
			if (deltas.length > 0) {
				boolean include = false;
				for (int j = 0; j < deltas.length; j++) {
					IDelta delta = deltas[j];
					if (delta instanceof IThreeWayDelta) {
						IThreeWayDelta twd = (IThreeWayDelta) delta;
						if (includeDirection(twd.getDirection())) {
							include = true;
							break;
						}
					}
				}
				if (include)
					result.add(object);
			}
		}
		// TODO: may need to get phantoms as well
		return result.toArray(new Object[result.size()]);
	}

	/**
	 * Return the traversals for the given model object. The traversals
	 * should be obtained from the scope.
	 * @param object the model object
	 * @return the traversals for the given object in the scope of this content provider
	 */
	protected abstract ResourceTraversal[] getTraversals(Object object);

	/**
	 * Return whether the given object is within the scope of this
	 * content provider. The object is in scope if it is part of
	 * a resource mapping in the scope or is the parent of resources
	 * covered by one or more resource mappings in the scope.
	 * @param parent the parent of the object
	 * @param object the object
	 * @return whether the given object is within the scope of this
	 * content provider
	 */
	protected abstract boolean isInScope(Object parent, Object object);
}
