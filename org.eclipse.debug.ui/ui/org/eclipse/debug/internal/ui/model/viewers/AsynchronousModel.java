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
package org.eclipse.debug.internal.ui.model.viewers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.internal.ui.elements.adapters.AsynchronousDebugLabelAdapter;
import org.eclipse.debug.internal.ui.viewers.provisional.IAsynchronousContentAdapter;
import org.eclipse.debug.internal.ui.viewers.provisional.IAsynchronousLabelAdapter;
import org.eclipse.debug.internal.ui.viewers.provisional.IAsynchronousRequestMonitor;
import org.eclipse.debug.internal.ui.viewers.provisional.IChildrenRequestMonitor;
import org.eclipse.debug.internal.ui.viewers.provisional.ILabelRequestMonitor;
import org.eclipse.debug.internal.ui.viewers.provisional.IModelProxy;
import org.eclipse.debug.internal.ui.viewers.provisional.IModelProxyFactoryAdapter;
import org.eclipse.debug.internal.ui.viewers.provisional.IPresentationContext;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.widgets.Widget;

/**
 * Model for an asynchronous viewer
 * 
 * @since 3.2
 */
public abstract class AsynchronousModel {
	
	private ModelNode fRoot; // root node
	private Map fWidgetToNode = new HashMap(); // map of widgets to corresponding tree node
	private Map fElementToNodes = new HashMap(); // map of element to corresponding tree nodes (list)
	private Map fModelProxies = new HashMap(); // map of installed model proxies, by element
	private AsynchronousModelViewer fViewer; // viewer this model works for
    private boolean fDisposed = false; // whether disposed
	
	/**
	 * List of requests currently being performed.
	 */
	private List fPendingUpdates = new ArrayList();
	
	/**
	 * List of pending viewer udpates
	 */
	private List fViewerUpdates = new ArrayList();

	/**
	 * Constructs a new empty tree model
	 * 
	 * @param viewer associated viewer
	 */
	public AsynchronousModel(AsynchronousModelViewer viewer) {
		fViewer = viewer;

	}
	
	/**
	 * Initializes this model. Called once after creation.
	 * 
	 * @param root root element or <code>null</code>
	 * @param widget root widget/control
	 */
	public void init(Object root, Widget widget) {
		if (root != null) {
			fRoot = new ModelNode(null, root);
			fRoot.setWidget(widget);
			mapWidget(widget, fRoot);
			mapElement(root, fRoot);
		}		
	}
	
	protected AsynchronousModelViewer getViewer() {
		return fViewer;
	}
	
	/**
	 * Disposes this model
	 */
	public synchronized void dispose() {
        fDisposed = true;
        cancelPendingUpdates();
		disposeAllModelProxies();
		fPendingUpdates.clear();
		if (getRootNode() != null) {
			getViewer().nodeDisposed(getRootNode());
		}
	}
    
    /**
     * Returns whether this model has been disposed
     */
    public synchronized boolean isDisposed() {
        return fDisposed;
    }
    
    /**
     * Cancels all pending update requests.
     */
    protected synchronized void cancelPendingUpdates() {
        Iterator updates = fPendingUpdates.iterator();
        while (updates.hasNext()) {
            IAsynchronousRequestMonitor update = (IAsynchronousRequestMonitor) updates.next();
            updates.remove();
            update.setCanceled(true);
        }
        fPendingUpdates.clear();
    }    
	
	/**
	 * Installs the model proxy for the given element into this viewer
	 * if not already installed.
	 * 
	 * @param element element to install an update policy for
	 */
	public synchronized void installModelProxy(Object element) {
		if (!fModelProxies.containsKey(element)) {
			IModelProxyFactoryAdapter modelProxyFactory = getModelProxyFactoryAdapter(element);
			if (modelProxyFactory != null) {
				IModelProxy proxy = modelProxyFactory.createModelProxy(element, getPresentationContext());
				if (proxy != null) {
					proxy.init(getPresentationContext());
					fModelProxies.put(element, proxy);
					getViewer().modelProxyAdded(proxy);
					proxy.installed();
				}
			}
		}
	}	
	
	/**
	 * Uninstalls the model proxy installed for the given element, if any.
	 * 
	 * @param element
	 */
	protected synchronized void disposeModelProxy(Object element) {
		IModelProxy proxy = (IModelProxy) fModelProxies.remove(element);
		if (proxy != null) {
			getViewer().modelProxyRemoved(proxy);
			proxy.dispose();
		}
	}	
	
	/**
	 * Unintalls all model proxies installed for this model
	 */
	private void disposeAllModelProxies() {
	    synchronized(fModelProxies) {
	        Iterator updatePolicies = fModelProxies.values().iterator();
	        while (updatePolicies.hasNext()) {
	            IModelProxy proxy = (IModelProxy)updatePolicies.next();
	            getViewer().modelProxyRemoved(proxy);	            
	            proxy.dispose();
	        }
	        
	        fModelProxies.clear();
	    }
	}	
	
	/**
	 * Returns the presentation this model is installed in
	 * 
	 * @return
	 */
	protected IPresentationContext getPresentationContext() {
		return fViewer.getPresentationContext();
	}
	
	/**
	 * Returns the model proxy factory for the given element of <code>null</code> if none.
	 * 
	 * @param element element to retrieve adapters for
	 * @return model proxy factory adapter or <code>null</code>
	 */
	protected IModelProxyFactoryAdapter getModelProxyFactoryAdapter(Object element) {
		IModelProxyFactoryAdapter adapter = null;
		if (element instanceof IModelProxyFactoryAdapter) {
			adapter = (IModelProxyFactoryAdapter) element;
		} else if (element instanceof IAdaptable) {
			IAdaptable adaptable = (IAdaptable) element;
			adapter = (IModelProxyFactoryAdapter) adaptable.getAdapter(IModelProxyFactoryAdapter.class);
		}
		return adapter;
	}	
	
	/**
	 * Maps the given widget to the given node
	 * 
	 * @param widget
	 * @param node
	 */
	protected void mapWidget(Widget widget, ModelNode node) {
		Object element = node.getElement();
		widget.setData(element);
        node.setWidget(widget);
		fWidgetToNode.put(widget, node);
	}
	
	/**
	 * Maps the given element to the given node.
	 * 
	 * @param element
	 * @param node
	 */
	protected synchronized void mapElement(Object element, ModelNode node) {
		ModelNode[] nodes = getNodes(element);
        node.remap(element);
		if (nodes == null) {
			fElementToNodes.put(element, new ModelNode[] { node});
		} else {
			for (int i = 0; i < nodes.length; i++) {
				if (nodes[i] == node) {
					return;
				}
			}
			ModelNode[] old = nodes;
			ModelNode[] newNodes = new ModelNode[old.length + 1];
			System.arraycopy(old, 0, newNodes, 0, old.length);
			newNodes[old.length] = node;
			fElementToNodes.put(element, newNodes);
		}
        installModelProxy(element);
	}
    
    /**
     * Unmaps the given node from its element and widget.
     * 
     * @param node
     */
    protected synchronized void unmapNode(ModelNode node) {
        Object element = node.getElement();
        ModelNode[] nodes = (ModelNode[]) fElementToNodes.get(element);
        if (nodes == null) {
            return;
        }
        if (nodes.length == 1) {
            fElementToNodes.remove(element);
            disposeModelProxy(element);
        } else {
            for (int i = 0; i < nodes.length; i++) {
                ModelNode node2 = nodes[i];
                if (node2 == node) {
                    ModelNode[] newNodes= new ModelNode[nodes.length - 1];
                    System.arraycopy(nodes, 0, newNodes, 0, i);
                    if (i < newNodes.length) {
                        System.arraycopy(nodes, i + 1, newNodes, i, newNodes.length - i);
                    }
                    fElementToNodes.put(element, newNodes);
                }
            }
        }
    }
	
	/**
	 * Returns the nodes in this model for the given element or
     * <code>null</code> if none.
	 * 
	 * @param element model element
	 * @return associated nodes or <code>null</code>
	 */
	protected ModelNode[] getNodes(Object element) {
		return (ModelNode[]) fElementToNodes.get(element);
	}
	
	/**
	 * Returns the node mapped to the given widet, or <code>null</code>
	 * 
	 * @param widget
	 * @return
	 */
	protected ModelNode getNode(Widget widget) {
		return (ModelNode) fWidgetToNode.get(widget);
	}
	
	/**
	 * Returns the root node or <code>null</code>
	 * 
	 * @return the root node or <code>null</code>
	 */
	protected ModelNode getRootNode() {
		return fRoot;
	}
	
	/**
	 * Cancels any conflicting updates for children of the given item, and
	 * schedules the new update.
	 * 
	 * @param update the update to schedule
	 */
	protected void requestScheduled(IAsynchronousRequestMonitor update) {
		AsynchronousModelRequestMonitor absUpdate = (AsynchronousModelRequestMonitor) update;
		synchronized (fPendingUpdates) {
			Iterator updates = fPendingUpdates.listIterator();
			while (updates.hasNext()) {
				AsynchronousModelRequestMonitor pendingUpdate = (AsynchronousModelRequestMonitor) updates.next();
				if (absUpdate.contains(pendingUpdate)) {
					updates.remove();
					pendingUpdate.setCanceled(true);
				}
			}
			fPendingUpdates.add(update);
		}
	}	
	
	/**
	 * Removes the update from the pending updates list.
	 * 
	 * @param update
	 */
	protected void requestComplete(IAsynchronousRequestMonitor update) {
		synchronized (fPendingUpdates) {
			fPendingUpdates.remove(update);
		}
	}
	
	/**
	 * An viewer update has been scheduled due to the following update request.
	 * 
	 * @param update
	 */
	protected void viewerUpdateScheduled(IAsynchronousRequestMonitor update) {
		synchronized (fViewerUpdates) {
			fViewerUpdates.add(update);
		}
	}
	
	/**
	 * Returns the result of running the given elements through the
	 * viewers filters.
	 * 
	 * @param parent parent element
	 * @param elements the elements to filter
	 * @return only the elements which all filters accept
	 */
	protected Object[] filter(Object parent, Object[] elements) {
		ViewerFilter[] filters = getViewer().getFilters();
		if (filters != null) {
			ArrayList filtered = new ArrayList(elements.length);
			for (int i = 0; i < elements.length; i++) {
				boolean add = true;
				for (int j = 0; j < filters.length; j++) {
					add = filters[j].select(getViewer(), parent, elements[i]);
					if (!add)
						break;
				}
				if (add)
					filtered.add(elements[i]);
			}
			return filtered.toArray();
		}
		return elements;
	}
	
	/**
	 * Returns the model node the given widget is mapped to or <code>null</code>
	 * 
	 * @param widget
	 * @return
	 */
	public ModelNode getModelNode(Widget widget) {
		return (ModelNode) fWidgetToNode.get(widget);
	}
	
	/**
	 * Refreshes the given node.
	 * 
	 * @param node
	 */
	protected void updateLabel(ModelNode node) {
		Object element = node.getElement();
		IAsynchronousLabelAdapter adapter = getLabelAdapter(element);
		if (adapter != null) {
			ILabelRequestMonitor labelUpdate = new ModelLabelRequestMonitor(node, this);
			requestScheduled(labelUpdate);
			adapter.retrieveLabel(element, getPresentationContext(), labelUpdate);
		}		
	}
	
	/**
	 * Returns the label adapter for the given element or <code>null</code> if none.
	 * 
	 * @param element element to retrieve adapter for
	 * @return presentation adapter or <code>null</code>
	 */
	protected IAsynchronousLabelAdapter getLabelAdapter(Object element) {
		IAsynchronousLabelAdapter adapter = null;
		if (element instanceof IAsynchronousLabelAdapter) {
			adapter = (IAsynchronousLabelAdapter) element;
		} else if (element instanceof IAdaptable) {
			IAdaptable adaptable = (IAdaptable) element;
			adapter = (IAsynchronousLabelAdapter) adaptable.getAdapter(IAsynchronousLabelAdapter.class);
		}
		// if no adapter, use default (i.e. model presentation)
		if (adapter == null) {
			return new AsynchronousDebugLabelAdapter();
		}
		return adapter;
	}	
	
    /**
     * Returns the tree element adapter for the given element or
     * <code>null</code> if none.
     * 
     * @param element
     *            element to retrieve adapter for
     * @return presentation adapter or <code>null</code>
     */
    protected IAsynchronousContentAdapter getContentAdapter(Object element) {        
        IAsynchronousContentAdapter adapter = null;
        if (element instanceof IAsynchronousContentAdapter) {
            adapter = (IAsynchronousContentAdapter) element;
        } else if (element instanceof IAdaptable) {
            IAdaptable adaptable = (IAdaptable) element;
            adapter = (IAsynchronousContentAdapter) adaptable.getAdapter(IAsynchronousContentAdapter.class);
        }
        return adapter;
    }	
    
    /**
     * Updates the children of the given node.
     * 
     * @param parent
     *            node of which to update children
     */
    protected void updateChildren(ModelNode parent) {
        Object element = parent.getElement();
        IAsynchronousContentAdapter adapter = getContentAdapter(element);
        if (adapter != null) {
            IChildrenRequestMonitor update = new ModelChildrenRequestMonitor(parent, this);
            requestScheduled(update);
            adapter.retrieveChildren(element, getPresentationContext(), update);
        }
    }    
	
	/**
	 * Update this model's viewer preserving its selection.
	 * 
	 * @param update
	 */
	protected void preservingSelection(Runnable update) {
		getViewer().preservingSelection(update);
	}

	/**
	 * The viewer updated associated with a request is compelte.
	 * 
	 * @param monitor
	 */
	protected void viewerUpdateComplete(IAsynchronousRequestMonitor monitor) {
		synchronized (fViewerUpdates) {
			fViewerUpdates.remove(monitor);
		}
		getViewer().updateComplete(monitor);
	}
	
	/**
	 * An update request was cancelled
	 * 
	 * @param monitor
	 */
	protected void requestCanceled(AsynchronousModelRequestMonitor monitor) {
		synchronized (fPendingUpdates) {
			fPendingUpdates.remove(monitor);
		}
	}
	
	/**
	 * Whether any updates are still in progress in the model or against the viewer.
	 * 
	 * @return
	 */
	protected boolean hasPendingUpdates() {
		synchronized (fViewerUpdates) {
			return !fPendingUpdates.isEmpty() || !fViewerUpdates.isEmpty();
		}
	}
	
    /**
     * Asynchronous update for add/set children request.
     * 
     * @param parent
     * @param element
     */
	protected abstract void add(ModelNode parent, Object element);
	
	/**
	 * Notification from children request monitor
	 * 
	 * @param parentNode parent node
	 * @param kids list of model elements
	 */
	protected void setChildren(final ModelNode parentNode, List kids) {
		
        final Object[] children = filter(parentNode.getElement(), kids.toArray());
        final AsynchronousModelViewer viewer = getViewer();
        ViewerSorter sorter = viewer.getSorter();
        if (sorter != null) {
        	sorter.sort(viewer, children);
        }
        
        final ModelNode[] prevKids = parentNode.getChildrenNodes();
        if (prevKids == null) {
        	final ModelNode[] newKids = new ModelNode[children.length];
            synchronized (this) {
                if (isDisposed()) {
                    return;
                }
            	for (int i = 0; i < children.length; i++) {
    				ModelNode node = new ModelNode(parentNode, children[i]);
    				mapElement(children[i], node);
    				newKids[i] = node;
    			}
            	parentNode.setChildren(newKids);
            }
            preservingSelection(new Runnable() {
                public void run() {
                	if (newKids.length == 0) {
                		viewer.nodeChanged(parentNode);
                	} else {
//                  the tree could have asked for data before it was ready...
                		viewer.nodeChildrenSet(parentNode, newKids);
                	}
                }
            });

        } else {
            synchronized (this) {
                if (isDisposed()) {
                    return;
                }
    	        for (int i = 0; i < prevKids.length; i++) {
    				ModelNode kid = prevKids[i];
    				if (i >= children.length) {
    					kid.dispose();
    					unmapNode(kid);
    				} else {
    					ModelNode prevNode = prevKids[i];
    					Object nextChild = children[i];
    					if (!prevNode.getElement().equals(nextChild)) {
    						unmapNode(prevNode);
    					}
    					mapElement(nextChild, prevNode);
    				}
    			}
    			// create new children
    	        if (children.length > prevKids.length) {
    		        ModelNode[] replaceChildren = new ModelNode[children.length];
    		        System.arraycopy(prevKids, 0, replaceChildren, 0, prevKids.length);
    		        for (int i = prevKids.length; i < children.length; i ++) {
    		        	Object child = children[i];
    					ModelNode childNode = new ModelNode(parentNode, child);
    		        	mapElement(child, childNode);
    		        	replaceChildren[i] = childNode;
    		        }
    		        parentNode.setChildren(replaceChildren);
    	        }
                if (children.length < prevKids.length) {
                    ModelNode[] replaceChildren = new ModelNode[children.length];
                    System.arraycopy(prevKids, 0, replaceChildren, 0, children.length);
                    parentNode.setChildren(replaceChildren);
                }
                    
            }
            // update viewer outside the lock
            
            preservingSelection(new Runnable() {
                public void run() {
                    for (int i = 0; i < prevKids.length; i++) {
                        ModelNode kid = prevKids[i];
                        if (i >= children.length) {
                            viewer.nodeDisposed(kid);
                        } else {
                            viewer.nodeChanged(kid);
                        }
                    }
                    if (children.length == 0) {
                    	viewer.nodeChanged(parentNode);
                    } else {
                    	viewer.nodeChildrenChanged(parentNode);
                    }
                }
            });

        }
	}
}
