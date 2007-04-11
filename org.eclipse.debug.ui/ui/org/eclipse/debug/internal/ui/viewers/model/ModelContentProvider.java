/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.viewers.model;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.IRequest;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementCompareRequest;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementContentProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementMementoProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementMementoRequest;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelChangedListener;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDeltaVisitor;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelProxy;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelProxyFactory;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdateListener;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ModelDelta;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.ui.progress.WorkbenchJob;

/**
 * Content provider for a virtual viewer.
 * 
 * @since 3.3
 */
abstract class ModelContentProvider implements IContentProvider, IModelChangedListener {

	private Viewer fViewer;

	private Map fModelProxies = new HashMap(); // model proxy by element
	
	/**
	 * Map of nodes that have been filtered from the viewer.
	 */
	private FilterTransform fTransform = new FilterTransform();
	
	/**
	 * Model listeners
	 */
	private ListenerList fModelListeners = new ListenerList();
	
	/**
	 * Update listeners
	 */
	private ListenerList fUpdateListeners = new ListenerList();
	
	/**
	 * Map of updates in progress: element path -> list of requests
	 */
	private Map fRequestsInProgress = new HashMap();
	
	/**
	 * Map of dependent requests waiting for parent requests to complete:
	 *  element path -> list of requests
	 */
	private Map fWaitingRequests = new HashMap();
	
	/**
	 * Map of viewer states keyed by viewer input mementos
	 */
	private Map fViewerStates = new LRUMap(20);
	
	/**
	 * Pending viewer state to be restored
	 */
	private ModelDelta fPendingState = null;
	
	/**
	 * Used to determine when restoration delta has been processed
	 */
	class CheckState implements IModelDeltaVisitor {
		private boolean complete = true;
		private IModelDelta topDelta = null;
		/* (non-Javadoc)
		 * @see org.eclipse.debug.internal.ui.viewers.provisional.IModelDeltaVisitor#visit(org.eclipse.debug.internal.ui.viewers.provisional.IModelDelta, int)
		 */
		public boolean visit(IModelDelta delta, int depth) {
			if (delta.getFlags() != IModelDelta.NO_CHANGE) {
				if (delta.getFlags() == IModelDelta.REVEAL && !(delta.getElement() instanceof IMemento)) {
					topDelta = delta;
				} else {
					complete = false;
					return false;
				}
			}
			return true;
		}
		
		public boolean isComplete() {
			return complete;
		}
		
		public IModelDelta getTopItemDelta() {
			return topDelta;
		}
	}
	
	/**
	 * LRU cache for viewer states 
	 */
	class LRUMap extends LinkedHashMap {
		private static final long serialVersionUID= 1L;
		private int fMaxSize;
		LRUMap(int maxSize) {
			super();
			fMaxSize = maxSize;
		}
		protected boolean removeEldestEntry(Entry eldest) {
			return size() > fMaxSize;
		}	
	}
	
	/**
	 * Update type constants
	 */
	static final int UPDATE_SEQUENCE_BEGINS = 0;
	static final int UPDATE_SEQUENCE_COMPLETE = 1;
	static final int UPDATE_BEGINS = 2;
	static final int UPDATE_COMPLETE = 3;
	
	/**
	 * Constant for an empty tree path.
	 */
	protected static final TreePath EMPTY_TREE_PATH = new TreePath(new Object[]{});
	
	// debug flags
	public static boolean DEBUG_CONTENT_PROVIDER = false;
	public static boolean DEBUG_UPDATE_SEQUENCE = false;
	
	static {
		DEBUG_CONTENT_PROVIDER = DebugUIPlugin.DEBUG && "true".equals( //$NON-NLS-1$
		 Platform.getDebugOption("org.eclipse.debug.ui/debug/viewers/contentProvider")); //$NON-NLS-1$
		DEBUG_UPDATE_SEQUENCE = DebugUIPlugin.DEBUG && "true".equals( //$NON-NLS-1$
		 Platform.getDebugOption("org.eclipse.debug.ui/debug/viewers/updateSequence")); //$NON-NLS-1$
	} 	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	public synchronized void dispose() {
		// cancel pending updates
		synchronized (fRequestsInProgress) {
			Iterator iterator = fRequestsInProgress.values().iterator();
			while (iterator.hasNext()) {
				List requests = (List) iterator.next();
				Iterator reqIter = requests.iterator();
				while (reqIter.hasNext()) {
					((IRequest) reqIter.next()).cancel();
				}
			}
		}
		fModelListeners.clear();
		fUpdateListeners.clear();
		disposeAllModelProxies();
		fViewer = null;
	}
	
	public synchronized boolean isDisposed() {
		return fViewer == null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
	 *      java.lang.Object, java.lang.Object)
	 */
	public synchronized void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		fViewer = viewer;
		if (oldInput != null) {
			saveViewerState(oldInput);
		}
		if (newInput != oldInput) {
			disposeAllModelProxies();
			fTransform.clear();
			if (newInput != null) {
				installModelProxy(newInput);
				restoreViewerState(newInput);
			}
		}
	}

	/**
	 * Restores viewer state for the new input
	 * 
	 * @param newInput
	 */
	protected synchronized void restoreViewerState(final Object input) {
		fPendingState = null;
		final IElementMementoProvider defaultProvider = getViewerStateAdapter(input);
		if (defaultProvider != null) {
			// build a model delta representing expansion and selection state
			final ModelDelta delta = new ModelDelta(input, IModelDelta.NO_CHANGE);
			final XMLMemento inputMemento = XMLMemento.createWriteRoot("VIEWER_INPUT_MEMENTO"); //$NON-NLS-1$
			final IMementoManager manager = new IMementoManager() {
			
				private IElementMementoRequest fRequest;
				
				/* (non-Javadoc)
				 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.viewers.IMementoManager#requestComplete(org.eclipse.debug.internal.ui.viewers.model.provisional.IElementMementoRequest)
				 */
				public synchronized void requestComplete(IElementMementoRequest request) {
					if (!request.isCanceled() && (request.getStatus() == null || request.getStatus().isOK())) {
						XMLMemento keyMemento = (XMLMemento) delta.getElement();
						StringWriter writer = new StringWriter();
						try {
							keyMemento.save(writer);
							final ModelDelta stateDelta = (ModelDelta) fViewerStates.remove(writer.toString());
							if (stateDelta != null) {
								if (DEBUG_CONTENT_PROVIDER) {
									System.out.println("RESTORE: " + stateDelta.toString()); //$NON-NLS-1$
								}
								stateDelta.setElement(input);
								// begin restoration
								UIJob job = new UIJob("restore state") { //$NON-NLS-1$
									public IStatus runInUIThread(IProgressMonitor monitor) {
										if (input.equals(getViewer().getInput())) {
											fPendingState = stateDelta;
											doInitialRestore();
										}
										return Status.OK_STATUS;
									}
								
								};
								job.setSystem(true);
								job.schedule();
							}
						} catch (IOException e) {
							// TODO log
						}
					}
				}
			
				/* (non-Javadoc)
				 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.viewers.IMementoManager#processReqeusts()
				 */
				public void processReqeusts() {
					defaultProvider.encodeElements(new IElementMementoRequest[]{fRequest});
				}
			
				/* (non-Javadoc)
				 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.viewers.IMementoManager#addRequest(org.eclipse.debug.internal.ui.viewers.model.provisional.IElementMementoRequest)
				 */
				public synchronized void addRequest(IElementMementoRequest req) {
					fRequest = req;
				}
			
			};
			manager.addRequest(new ElementMementoRequest(ModelContentProvider.this, manager, getPresentationContext(),
									delta.getElement(), getViewerTreePath(delta), inputMemento, delta));
			manager.processReqeusts();
		}
	}
	
	/**
	 * Restore selection/expansion based on items already in the viewer
	 */
	abstract protected void doInitialRestore();
	
	/**
	 * @param delta
	 */
	abstract void doRestore(final ModelDelta delta);
	
	/**
	 * Perform any restoration required for the given tree path.
	 * 
	 * @param path
	 */
	protected synchronized void doRestore(final TreePath path) {
		if (fPendingState == null) { 
			return;
		}
		final IElementMementoProvider defaultProvider = getViewerStateAdapter(getViewer().getInput());
		IModelDeltaVisitor visitor = new IModelDeltaVisitor() {
			public boolean visit(IModelDelta delta, int depth) {
				if (delta.getParentDelta() == null) {
					return true;
				}
				Object element = delta.getElement();
				Object potentialMatch = path.getSegment(depth - 1);
				if (element instanceof IMemento) {
					IElementMementoProvider provider = getViewerStateAdapter(element);
					if (provider == null) {
						provider = defaultProvider;
					}
					if (provider != null) {
						provider.compareElements(new IElementCompareRequest[]{
								new ElementCompareRequest(ModelContentProvider.this,
										potentialMatch, path, (IMemento) element, (ModelDelta)delta)});
					}
				} else {
					if (element.equals(potentialMatch)) {
						// already processed - visit children
						return path.getSegmentCount() > depth;
					}
				}
				return false;
			}
		};
		fPendingState.accept(visitor);
	}

	/**
	 * Saves the viewer's state for the previous input.
	 * 
	 * @param oldInput
	 */
	protected void saveViewerState(Object input) {
		IElementMementoProvider stateProvider = getViewerStateAdapter(input);
		if (stateProvider != null) {
			// build a model delta representing expansion and selection state
			ModelDelta delta = new ModelDelta(input, IModelDelta.NO_CHANGE);
			buildViewerState(delta);
			if (delta.getChildDeltas().length > 0) {
				// encode delta with mementos in place of elements, in non-UI thread
				encodeDelta(delta, stateProvider);
			}
		}
	}

	/**
	 * Encodes delta elements into mementos using the given provider.
	 *  
	 * @param delta
	 * @param stateProvider
	 */
	protected void encodeDelta(final ModelDelta rootDelta, final IElementMementoProvider defaultProvider) {
		final XMLMemento inputMemento = XMLMemento.createWriteRoot("VIEWER_INPUT_MEMENTO"); //$NON-NLS-1$
		final XMLMemento childrenMemento = XMLMemento.createWriteRoot("CHILDREN_MEMENTO"); //$NON-NLS-1$
		final IMementoManager manager = new IMementoManager() {
		
			/**
			 * Maps element memento provider to list of memento requests
			 */
			private Map requestMap = new HashMap();
			private Set allRequests = new HashSet();
			private boolean abort = false; 
			
			/* (non-Javadoc)
			 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.viewers.IMementoManager#requestComplete(org.eclipse.debug.internal.ui.viewers.model.provisional.IElementMementoRequest)
			 */
			public synchronized void requestComplete(IElementMementoRequest request) {
				if (!abort) {
					if (!request.isCanceled() && (request.getStatus() == null || request.getStatus().isOK())) {
						allRequests.remove(request);
						if (allRequests.isEmpty()) {
							requestMap.clear();
							XMLMemento keyMemento = (XMLMemento) rootDelta.getElement();
							StringWriter writer = new StringWriter();
							try {
								keyMemento.save(writer);
								fViewerStates.put(writer.toString(), rootDelta);
							} catch (IOException e) {
								// TODO log
							}
						}
					} else {
						abort = true;
						Iterator iterator = allRequests.iterator();
						while (iterator.hasNext()) {
							IElementMementoRequest req = (IElementMementoRequest) iterator.next();
							req.cancel();
						}
						requestMap.clear();
						allRequests.clear();
					}
				}
			}
		
			/* (non-Javadoc)
			 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.viewers.IMementoManager#processReqeusts()
			 */
			public void processReqeusts() {
				Iterator iterator = requestMap.entrySet().iterator();
				while (iterator.hasNext()) {
					Entry entry = (Entry) iterator.next();
					IElementMementoProvider provider = (IElementMementoProvider) entry.getKey();
					List list = (List) entry.getValue();
					provider.encodeElements((IElementMementoRequest[]) list.toArray(new IElementMementoRequest[list.size()]));
				}
			}
		
			/* (non-Javadoc)
			 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.viewers.IMementoManager#addRequest(org.eclipse.debug.internal.ui.viewers.model.provisional.IElementMementoRequest)
			 */
			public synchronized void addRequest(IElementMementoRequest request) {
				if (allRequests.add(request)) {
					IElementMementoProvider provider = getViewerStateAdapter(request.getElement());
					if (provider == null) {
						provider = defaultProvider;
					}
					List list = (List)requestMap.get(provider);
					if (list == null) {
						list = new ArrayList();
						requestMap.put(provider, list);
					}
					list.add(request);
				}
			}
		
		};
		IModelDeltaVisitor visitor = new IModelDeltaVisitor() {
			public boolean visit(IModelDelta delta, int depth) {
				if (delta.getParentDelta() == null) {
					manager.addRequest(
						new ElementMementoRequest(ModelContentProvider.this, manager, getPresentationContext(),
								delta.getElement(), getViewerTreePath(delta), inputMemento, (ModelDelta)delta));
				} else {
					manager.addRequest(
						new ElementMementoRequest(ModelContentProvider.this, manager, getPresentationContext(),
								delta.getElement(), getViewerTreePath(delta), childrenMemento.createChild("CHILD_ELEMENT"), (ModelDelta)delta)); //$NON-NLS-1$
				}
				return true;
			}
		};
		rootDelta.accept(visitor);
		manager.processReqeusts();
	}

	/**
	 * Builds a delta with the given root delta for expansion/selection state.
	 * 
	 * @param delta root delta
	 */
	protected abstract void buildViewerState(ModelDelta delta);

	/**
	 * Uninstalls the model proxy installed for the given element, if any.
	 * 
	 * @param element
	 */
	protected synchronized void disposeModelProxy(Object element) {
		IModelProxy proxy = (IModelProxy) fModelProxies.remove(element);
		if (proxy != null) {
			proxy.dispose();
		}
	}

	/**
	 * Uninstalls each model proxy
	 */
	protected synchronized void disposeAllModelProxies() {
		Iterator updatePolicies = fModelProxies.values().iterator();
		while (updatePolicies.hasNext()) {
			IModelProxy proxy = (IModelProxy) updatePolicies.next();
			proxy.dispose();
		}
		fModelProxies.clear();
	}

	/**
	 * Installs the model proxy for the given element into this content provider
	 * if not already installed.
	 * 
	 * @param element
	 *            element to install an update policy for
	 */
	protected synchronized void installModelProxy(Object element) {
		if (!fModelProxies.containsKey(element)) {
			IModelProxyFactory modelProxyFactory = getModelProxyFactoryAdapter(element);
			if (modelProxyFactory != null) {
				final IModelProxy proxy = modelProxyFactory.createModelProxy(
						element, getPresentationContext());
				if (proxy != null) {
					fModelProxies.put(element, proxy);
					Job job = new Job("Model Proxy installed notification job") {//$NON-NLS-1$
						protected IStatus run(IProgressMonitor monitor) {
							if (!monitor.isCanceled()) {
								proxy.init(getPresentationContext());
								Object[] mcls = fModelListeners.getListeners();
								for (int i = 0; i < mcls.length; i++) {
									proxy.addModelChangedListener((IModelChangedListener) mcls[i]);
								}
								proxy
										.addModelChangedListener(ModelContentProvider.this);
								proxy.installed(getViewer());
							}
							return Status.OK_STATUS;
						}
					};
					job.setSystem(true);
					job.schedule();
				}
			}
		}
	}

	/**
	 * Returns the model proxy factory for the given element or
	 * <code>null</code> if none.
	 * 
	 * @param element
	 *            element to retrieve adapter for
	 * @return model proxy factory adapter or <code>null</code>
	 */
	protected IModelProxyFactory getModelProxyFactoryAdapter(Object element) {
		IModelProxyFactory adapter = null;
		if (element instanceof IModelProxyFactory) {
			adapter = (IModelProxyFactory) element;
		} else if (element instanceof IAdaptable) {
			IAdaptable adaptable = (IAdaptable) element;
			adapter = (IModelProxyFactory) adaptable.getAdapter(IModelProxyFactory.class);
		}
		return adapter;
	}
	
	/**
	 * Returns the viewer state adapter for the given element or
	 * <code>null</code> if none.
	 * 
	 * @param element
	 *            element to retrieve adapter for
	 * @return viewer state adapter or <code>null</code>
	 */
	protected IElementMementoProvider getViewerStateAdapter(Object element) {
		IElementMementoProvider adapter = null;
		if (element instanceof IElementMementoProvider) {
			adapter = (IElementMementoProvider) element;
		} else if (element instanceof IAdaptable) {
			IAdaptable adaptable = (IAdaptable) element;
			adapter = (IElementMementoProvider) adaptable.getAdapter(IElementMementoProvider.class);
		}
		return adapter;
	}	

	/**
	 * Returns the presentation context for this content provider.
	 * 
	 * @return presentation context
	 */
	protected abstract IPresentationContext getPresentationContext();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.internal.ui.viewers.provisional.IModelChangedListener#modelChanged(org.eclipse.debug.internal.ui.viewers.provisional.IModelDelta)
	 */
	public void modelChanged(final IModelDelta delta, final IModelProxy proxy) {
		WorkbenchJob job = new WorkbenchJob("process model delta") { //$NON-NLS-1$
			public IStatus runInUIThread(IProgressMonitor monitor) {
				if (!proxy.isDisposed()) {
					updateNodes(new IModelDelta[] { delta });
				}
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		job.schedule();
		
	}

	protected void updateNodes(IModelDelta[] nodes) {
		for (int i = 0; i < nodes.length; i++) {
			IModelDelta node = nodes[i];
			int flags = node.getFlags();

			if ((flags & IModelDelta.ADDED) != 0) {
				handleAdd(node);
			}
			if ((flags & IModelDelta.REMOVED) != 0) {
				handleRemove(node);
			}
			if ((flags & IModelDelta.CONTENT) != 0) {
				handleContent(node);
			}
			if ((flags & IModelDelta.EXPAND) != 0) {
				handleExpand(node);
			}
			if ((flags & IModelDelta.COLLAPSE) != 0) {
				handleCollapse(node);
			}
			if ((flags & IModelDelta.SELECT) != 0) {
				handleSelect(node);
			}
			if ((flags & IModelDelta.STATE) != 0) {
				handleState(node);
			}
			if ((flags & IModelDelta.INSERTED) != 0) {
				handleInsert(node);
			}
			if ((flags & IModelDelta.REPLACED) != 0) {
				handleReplace(node);
			}
			if ((flags & IModelDelta.INSTALL) != 0) {
				handleInstall(node);
			}
			if ((flags & IModelDelta.UNINSTALL) != 0) {
				handleUninstall(node);
			}
			if ((flags & IModelDelta.REVEAL) != 0) {
				handleReveal(node);
			}
			updateNodes(node.getChildDeltas());
		}
	}
	
    /**
     * Returns the content adapter for the given element or
     * <code>null</code> if none.
     * 
     * @param element
     *            element to retrieve adapter for
     * @return content adapter or <code>null</code>
     */
    protected IElementContentProvider getContentAdapter(Object element) {        
        IElementContentProvider adapter = null;
        if (element instanceof IElementContentProvider) {
            adapter = (IElementContentProvider) element;
        } else if (element instanceof IAdaptable) {
            IAdaptable adaptable = (IAdaptable) element;
            adapter = (IElementContentProvider) adaptable.getAdapter(IElementContentProvider.class);
        }
        return adapter;
    }		

	protected abstract void handleState(IModelDelta delta);

	protected abstract void handleSelect(IModelDelta delta);

	protected abstract void handleExpand(IModelDelta delta);
	
	protected abstract void handleCollapse(IModelDelta delta);

	protected abstract void handleContent(IModelDelta delta);

	protected abstract void handleRemove(IModelDelta delta);

	protected abstract void handleAdd(IModelDelta delta);

	protected abstract void handleInsert(IModelDelta delta);

	protected abstract void handleReplace(IModelDelta delta);
	
	protected abstract void handleReveal(IModelDelta delta);
	
	protected void handleInstall(IModelDelta delta) {
		installModelProxy(delta.getElement());
	}
	
	protected void handleUninstall(IModelDelta delta) {
		disposeModelProxy(delta.getElement());
	}	

	/**
	 * Returns a tree path for the node, *not* including the root element.
	 * 
	 * @param node
	 *            model delta
	 * @return corresponding tree path
	 */
	protected TreePath getViewerTreePath(IModelDelta node) {
		ArrayList list = new ArrayList();
		IModelDelta parentDelta = node.getParentDelta();
		while (parentDelta != null) {
			list.add(0, node.getElement());
			node = parentDelta;
			parentDelta = node.getParentDelta();
		}
		return new TreePath(list.toArray());
	}

	/**
	 * Returns the viewer this content provider is working for.
	 * 
	 * @return viewer
	 */
	protected Viewer getViewer() {
		return fViewer;
	}
	
	/**
	 * Translates and returns the given child index from the viewer coordinate
	 * space to the model coordinate space.
	 *  
	 * @param parentPath path to parent element
	 * @param index index of child element in viewer (filtered) space
	 * @return index of child element in model (raw) space
	 */
	public /* protected */ int viewToModelIndex(TreePath parentPath, int index) {
		return fTransform.viewToModelIndex(parentPath, index);
	}
	
	/**
	 * Translates and returns the given child count from the viewer coordinate
	 * space to the model coordinate space.
	 *  
	 * @param parentPath path to parent element
	 * @param count number of child elements in viewer (filtered) space
	 * @return number of child elements in model (raw) space
	 */
	public /* protected */ int viewToModelCount(TreePath parentPath, int count) {
		return fTransform.viewToModelCount(parentPath, count);
	}	
	
	/**
	 * Translates and returns the given child index from the model coordinate
	 * space to the viewer coordinate space.
	 *  
	 * @param parentPath path to parent element
	 * @param index index of child element in model (raw) space
	 * @return index of child element in viewer (filtered) space
	 */
	protected int modelToViewIndex(TreePath parentPath, int index) {
		return fTransform.modelToViewIndex(parentPath, index);
	}	
	
	/**
	 * Translates and returns the given child count from the model coordinate
	 * space to the viewer coordinate space.
	 *  
	 * @param parentPath path to parent element
	 * @param count child count element in model (raw) space
	 * @return child count in viewer (filtered) space
	 */
	protected int modelToViewChildCount(TreePath parentPath, int count) {
		return fTransform.modelToViewCount(parentPath, count);
	}	
	
	/**
	 * Notes that the child at the specified index of the given parent element
	 * has been filtered from the viewer. Returns whether the child at the given
	 * index was already filtered.
	 * 
	 * @param parentPath path to parent element
	 * @param index index of child element to be filtered
	 * @param element the filtered element
	 * @return whether the child was already filtered
	 */
	protected boolean addFilteredIndex(TreePath parentPath, int index, Object element) {
		return fTransform.addFilteredIndex(parentPath, index, element);
	}
	
	/**
	 * Notes that the element at the given index has been removed from its parent
	 * and filtered indexes should be updated accordingly.
	 * 
	 * @param parentPath path to parent element
	 * @param index index of element that was removed
	 */
	protected void removeElementFromFilters(TreePath parentPath, int index) {
		fTransform.removeElementFromFilters(parentPath, index);
	}
	
	/**
	 * Removes the given element from filtered elements of the given parent
	 * element. Return true if the element was removed, otherwise false.
	 * 
	 * @param parentPath path to parent element
	 * @param element element to remove
	 * @return whether the element was removed
	 */
	protected boolean removeElementFromFilters(TreePath parentPath, Object element) {
		return fTransform.removeElementFromFilters(parentPath, element);
	}	
	
	/**
	 * The child count for a parent has been computed. Ensure any filtered items
	 * above the given count are cleared.
	 * 
	 * @param parentPath path to parent element
	 * @param childCount number of children
	 */
	protected void setModelChildCount(TreePath parentPath, int childCount) {
		fTransform.setModelChildCount(parentPath, childCount);
	}
	
	/**
	 * Returns whether the given element is filtered.
	 * 
	 * @param parentElementOrTreePath
	 *            the parent element or path
	 * @param element
	 *            the child element
	 * @return whether to filter the element
	 */
	protected boolean shouldFilter(Object parentElementOrTreePath, Object element) {
		ViewerFilter[] filters = ((StructuredViewer)fViewer).getFilters();
		if (filters.length > 0) {
			for (int j = 0; j < filters.length; j++) {
				if (!(filters[j].select(fViewer, parentElementOrTreePath, element))) {
					return true;
				}
			}
		}
		return false;
	}	
	
	/**
	 * Returns whether the given index of the specified parent was previously filtered.
	 * 
	 * @param parentPath
	 * @param index
	 * @return whether the element at the given index was filtered
	 */
	protected boolean isFiltered(TreePath parentPath, int index) {
		return fTransform.isFiltered(parentPath, index);
	}
	
	/**
	 * Notification the given element is being unmapped.
	 * 
	 * @param path
	 */
	protected void unmapPath(TreePath path) {
		//System.out.println("Unmap " + path.getLastSegment());
		fTransform.clear(path);
		cancelSubtreeUpdates(path);
	}

	/**
	 * Returns filtered children or <code>null</code>
	 * @param parent
	 * @return filtered children or <code>null</code>
	 */
	protected int[] getFilteredChildren(TreePath parent) {
		return fTransform.getFilteredChildren(parent);
	}
	
	protected void clearFilteredChild(TreePath parent, int modelIndex) {
		fTransform.clear(parent, modelIndex);
	}
	
	protected void clearFilters(TreePath parent) {
		fTransform.clear(parent);
	}

	protected synchronized IModelDelta checkIfRestoreComplete() {
		if (fPendingState == null) {
			return null;
		}
		CheckState state = new CheckState();
		fPendingState.accept(state);
		if (state.isComplete()) {
			fPendingState = null;
			if (DEBUG_CONTENT_PROVIDER) {
				System.out.println("RESTORE COMPELTE"); //$NON-NLS-1$
			}
			return state.getTopItemDelta();
		}
		return null;
	}
	
	void addViewerUpdateListener(IViewerUpdateListener listener) {
		fUpdateListeners.add(listener);
	}
	
	void removeViewerUpdateListener(IViewerUpdateListener listener) {
		fUpdateListeners.remove(listener);
	}
	
	/**
	 * Notification an update request has started
	 * 
	 * @param update
	 */
	void updateStarted(IViewerUpdate update) {
		boolean begin = false;
		synchronized (fRequestsInProgress) {
			begin = fRequestsInProgress.isEmpty();
			List requests = (List) fRequestsInProgress.get(update.getElementPath());
			if (requests == null) {
				requests = new ArrayList();
				fRequestsInProgress.put(update.getElementPath(), requests);
			}
			requests.add(update);
		}
		if (begin) {
			if (DEBUG_UPDATE_SEQUENCE) {
				System.out.println("MODEL SEQUENCE BEGINS"); //$NON-NLS-1$
			}
			notifyUpdate(UPDATE_SEQUENCE_BEGINS, null);
		}
		if (DEBUG_UPDATE_SEQUENCE) {
			System.out.println("\tBEGIN - " + update); //$NON-NLS-1$
		}
		notifyUpdate(UPDATE_BEGINS, update);
	}
	
	/**
	 * Notification an update request has completed
	 * 
	 * @param update
	 */
	void updateComplete(IViewerUpdate update) {
		boolean end = false;
		synchronized (fRequestsInProgress) {
			List requests = (List) fRequestsInProgress.get(update.getElementPath());
			if (requests != null) {
				requests.remove(update);
				trigger((ViewerUpdateMonitor) update);
				if (requests.isEmpty()) {
					fRequestsInProgress.remove(update.getElementPath());
				}
			}
			end = fRequestsInProgress.isEmpty();
		}
		notifyUpdate(UPDATE_COMPLETE, update);
		if (DEBUG_UPDATE_SEQUENCE) {
			System.out.println("\tEND - " + update); //$NON-NLS-1$
		}
		if (end) {
			if (DEBUG_UPDATE_SEQUENCE) {
				System.out.println("MODEL SEQUENCE ENDS"); //$NON-NLS-1$
			}
			notifyUpdate(UPDATE_SEQUENCE_COMPLETE, null);
		}
	}
	
	protected void notifyUpdate(final int type, final IViewerUpdate update) {
		if (!fUpdateListeners.isEmpty()) {
			Object[] listeners = fUpdateListeners.getListeners();
			for (int i = 0; i < listeners.length; i++) {
				final IViewerUpdateListener listener = (IViewerUpdateListener) listeners[i];
				SafeRunner.run(new ISafeRunnable() {
					public void run() throws Exception {
						switch (type) {
							case UPDATE_SEQUENCE_BEGINS:
								listener.viewerUpdatesBegin();
								break;
							case UPDATE_SEQUENCE_COMPLETE:
								listener.viewerUpdatesComplete();
								break;
							case UPDATE_BEGINS:
								listener.updateStarted(update);
								break;
							case UPDATE_COMPLETE:
								listener.updateComplete(update);
								break;
						}
					}
					public void handleException(Throwable exception) {
						DebugUIPlugin.log(exception);
					}
				});
			}
		}
	}	
	
	protected void cancelSubtreeUpdates(TreePath path) {
		synchronized (fRequestsInProgress) {
			Iterator iterator = fRequestsInProgress.entrySet().iterator();
			while (iterator.hasNext()) {
				Entry entry = (Entry) iterator.next();
				TreePath entryPath = (TreePath) entry.getKey();
				if (entryPath.startsWith(path, null)) {
					List requests = (List) entry.getValue();
					Iterator reqIter = requests.iterator();
					while (reqIter.hasNext()) {
						((IRequest)reqIter.next()).cancel();
					}
				}
			}
			List purge = new ArrayList(); 
			iterator = fWaitingRequests.keySet().iterator();
			while (iterator.hasNext()) {
				TreePath entryPath = (TreePath) iterator.next();
				if (entryPath.startsWith(path, null)) {
					purge.add(entryPath);
				}
			}
			iterator = purge.iterator();
			while (iterator.hasNext()) {
				fWaitingRequests.remove(iterator.next());
			}
		}
	}
	
	/**
	 * Returns whether this given request should be run, or should wait for parent
	 * update to complete.
	 * 
	 * @param update
	 * @return whether to start the given request
	 */
	void schedule(ViewerUpdateMonitor update) {
		synchronized (fRequestsInProgress) {
			TreePath schedulingPath = update.getSchedulingPath();
			List requests = (List) fWaitingRequests.get(schedulingPath);
			if (requests == null) {
				// no waiting requests
				TreePath parentPath = schedulingPath;
				while (fRequestsInProgress.get(parentPath) == null) {
					parentPath = parentPath.getParentPath();
					if (parentPath == null) {
						// no running requests: start request
						update.start();
						return;
					}
				}
				// request running on parent, add to waiting list
				requests = new ArrayList();
				requests.add(update);
				fWaitingRequests.put(schedulingPath, requests);
			} else {
				// there are waiting requests: coalesce with existing request?
				Iterator reqIter = requests.iterator();
				while (reqIter.hasNext()) {
					ViewerUpdateMonitor waiting = (ViewerUpdateMonitor) reqIter.next();
					if (waiting.coalesce(update)) {
						// coalesced with existing request, done
						return;
					}
				}
				// add to list of waiting requests
				requests.add(update);
				return;
			}
		}
	}
	
	/**
	 * Triggers waiting requests based on the given request that just completed.
	 * 
	 * TODO: should we cancel child updates if a request has been canceled?
	 * 
	 * @param request
	 */
	void trigger(ViewerUpdateMonitor request) {
		if (fWaitingRequests.isEmpty()) {
			return;
		}
		TreePath schedulingPath = request.getSchedulingPath();
		List waiting = (List) fWaitingRequests.get(schedulingPath);
		if (waiting == null) {
			// no waiting, update the entry with the shortest path
			int length = Integer.MAX_VALUE;
			Iterator entries = fWaitingRequests.entrySet().iterator();
			Entry candidate = null;
			while (entries.hasNext()) {
				Entry entry = (Entry) entries.next();
				TreePath key = (TreePath) entry.getKey();
				if (key.getSegmentCount() < length) {
					candidate = entry;
					length = key.getSegmentCount();
				}
			}
			if (candidate != null) {
				startHighestPriorityRequest((TreePath) candidate.getKey(), (List) candidate.getValue());
			}
		} else {
			// start the highest priority request
			startHighestPriorityRequest(schedulingPath, waiting);
		}
	}

	/**
	 * @param key
	 * @param waiting
	 */
	private void startHighestPriorityRequest(TreePath key, List waiting) {
		int priority = 4;
		ViewerUpdateMonitor next = null;
		Iterator requests = waiting.iterator();
		while (requests.hasNext()) {
			ViewerUpdateMonitor vu = (ViewerUpdateMonitor) requests.next();
			if (vu.getPriority() < priority) {
				next = vu;
				priority = next.getPriority();
			}
		}
		waiting.remove(next);
		if (waiting.isEmpty()) {
			fWaitingRequests.remove(key);
		}
		next.start();
	}
	
	/**
	 * Registers the given listener for model delta notification.
	 * 
	 * @param listener model delta listener
	 */
	void addModelChangedListener(IModelChangedListener listener) {
		fModelListeners.add(listener); 
		Iterator proxies = fModelProxies.values().iterator();
		while (proxies.hasNext()) {
			IModelProxy proxy = (IModelProxy) proxies.next();
			proxy.addModelChangedListener(listener);
		}
	}
	
	/**
	 * Unregisters the given listener from model delta notification.
	 * 
	 * @param listener model delta listener
	 */
	void removeModelChangedListener(IModelChangedListener listener) {
		fModelListeners.remove(listener);
		Iterator proxies = fModelProxies.values().iterator();
		while (proxies.hasNext()) {
			IModelProxy proxy = (IModelProxy) proxies.next();
			proxy.removeModelChangedListener(listener);
		}
	}
	
	/**
	 * Returns the element corresponding to the given tree path.
	 * 
	 * @param path tree path
	 * @return model element
	 */
	protected Object getElement(TreePath path) {
		if (path.getSegmentCount() > 0) {
			return path.getLastSegment();
		}
		return getViewer().getInput();
	}
}
