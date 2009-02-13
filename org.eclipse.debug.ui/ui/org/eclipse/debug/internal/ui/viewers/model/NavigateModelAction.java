/*******************************************************************************

 * Copyright (c) 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.viewers.model;

import java.util.Arrays;

import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelNavigateProxy;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelNavigateUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelProxy;
import org.eclipse.debug.internal.ui.viewers.model.provisional.TreeModelViewer;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreePath;

/**
 * Action used to navigate the model in the treemodel viewer.  This action uses
 * the {@link IModelNavigateProxy} to select the next or previous element in the 
 * model.  The model determines which elements should be navigated to (e.g. next
 * suspended thread).   
 * 
 * @since 3.5
 */
public class NavigateModelAction extends Action implements ISelectionChangedListener {
    
    final private TreeModelViewer fViewer;
    final private boolean fReverse;

    public NavigateModelAction(TreeModelViewer viewer, boolean reverse) {
        fViewer = viewer;
        fReverse = reverse;
        fViewer.addSelectionChangedListener(this);
        update();
    }

    public void dispose() {
    	fViewer.removeSelectionChangedListener(this);
    }

    public void selectionChanged(SelectionChangedEvent event) {
    	update();
    }

    private void update() {
    	// Update the action enablement.  Action should be disabled if there 
    	// are no model proxies that support navigation.
    	boolean enabled = false;
        ModelContentProvider contentProvider = (ModelContentProvider)fViewer.getContentProvider();
        final IModelProxy[] proxies = contentProvider.getModelProxies();
        for (int i = 0; i < proxies.length; i++) {
        	if (proxies[i] instanceof IModelNavigateProxy) {
        		enabled = true;
        	}
        }
        setEnabled(enabled);    	
    }

    
    /**
     * Update class for collecting the model delta to navigate the viewer to.
     */
    abstract private class TraversalUpdate extends ViewerUpdateMonitor implements IModelNavigateUpdate {
    	
        final IModelNavigateProxy fModelTraversalProxy;
        IModelDelta fDelta; 
        
        TraversalUpdate(IModelNavigateProxy proxy, TreePath path) {
            super((ModelContentProvider)fViewer.getContentProvider(), 
                  fViewer.getInput(), 
                  path, 
                  path.getSegmentCount() == 0 ? fViewer.getInput() : path.getLastSegment(), 
                  null, 
                  fViewer.getPresentationContext());
            fModelTraversalProxy = proxy;
        }

        public void setNextElementDelta(IModelDelta delta) {
            fDelta = delta;
        }

        public boolean isReverse() {
            return fReverse;
        }
        
        void startRequest() {
            fModelTraversalProxy.update(this);
        }
        
        boolean coalesce(ViewerUpdateMonitor update) {
            return false;
        }

        int getPriority() {
            return 0;
        }

        TreePath getSchedulingPath() {
            return null;
        }
    }
    
    public void run() {
        // Get the model proxy of the currently selected element.
        setEnabled(false);
        TreePath path = null;
        final ISelection currentContext = fViewer.getSelection();
        if (currentContext instanceof ITreeSelection) {
            TreePath[] paths = ((ITreeSelection)currentContext).getPaths();
            if (paths.length > 0 && paths[0].getSegmentCount() > 0) {
                path = paths[0];
            }
        }
        if (path == null) {
            path = TreePath.EMPTY; 
        }
        ModelContentProvider contentProvider = (ModelContentProvider)fViewer.getContentProvider();
        IModelProxy selectionProxy = contentProvider.getElementProxy(path);
        
        // Calculate the index of the current proxy.
        final IModelProxy[] proxies = contentProvider.getModelProxies();
        int _idx = Arrays.asList(proxies).indexOf(selectionProxy);
        final int selectionProxyIdx = _idx == -1 ? 0 : _idx;
        final int nextProxyIdx = getNextProxyIdx(proxies, selectionProxyIdx);
        
        // Try traversing in the current model.
        if (selectionProxy instanceof IModelNavigateProxy && !selectionProxy.isDisposed()) {
            new TraversalUpdate((IModelNavigateProxy)selectionProxy, path) {
                protected void performUpdate() {
                    if (fDelta != null) {
                        fViewer.updateViewer(fDelta);
                        setEnabled(true);
                    } else if (proxies.length > 0) {
                        updateNextProxy(proxies, nextProxyIdx, nextProxyIdx, true);
                    } else {
                        setEnabled(true);
                    }
                }
            }.start();
        } else if (proxies.length > 0) {
            updateNextProxy(proxies, nextProxyIdx, nextProxyIdx, true);
        }
    }
    
    private int getNextProxyIdx(final IModelProxy[] proxies, int currentProxyIdx) {
        int _nextProxyIdx = currentProxyIdx;
        if (fReverse) {
            _nextProxyIdx--;
            if (_nextProxyIdx < 0) {
                _nextProxyIdx = proxies.length - 1;
            }
        } else {
            _nextProxyIdx++;
            if (_nextProxyIdx >= proxies.length) {
                _nextProxyIdx = 0;
            }
        }
        return _nextProxyIdx;
    }
    
    private void updateNextProxy(final IModelProxy[] proxies, final int currentProxyIdx, final int endProxyIdx, boolean start) {
        IModelProxy proxy = proxies[currentProxyIdx];
        
        if (!start && currentProxyIdx == endProxyIdx) {
            // We're finished trying to find the next context
            setEnabled(true);
        } else if (proxy instanceof IModelNavigateProxy && !proxy.isDisposed()) { 
            new TraversalUpdate((IModelNavigateProxy)proxy, TreePath.EMPTY) {
                protected void performUpdate() {
                    if (fDelta != null) {
                        fViewer.updateViewer(fDelta);
                        setEnabled(true);
                    } else {
                        updateNextProxy(proxies, getNextProxyIdx(proxies, currentProxyIdx), endProxyIdx, false);
                    } 
                }
            }.start();
        } else {
            updateNextProxy(proxies, getNextProxyIdx(proxies, currentProxyIdx), endProxyIdx, false);
        }
        
    }
}
