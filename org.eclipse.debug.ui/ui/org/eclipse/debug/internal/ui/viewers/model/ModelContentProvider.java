/*******************************************************************************
 * Copyright (c) 2006, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Pawel Piech - Wind River - Bug 205335: ModelContentProvider does not cancel stale updates when switching viewer input
 *     Wind River Systems - Fix for viewer state save/restore [188704] 
 *     Pawel Piech (Wind River) - added support for a virtual tree model viewer (Bug 242489)
 *     Dorin Ciuca - Top index fix (Bug 324100)
 *******************************************************************************/
package org.eclipse.debug.internal.ui.viewers.model;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementCompareRequest;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementMementoProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementMementoRequest;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelChangedListener;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDeltaVisitor;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelProxy;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelProxyFactory;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelProxyFactory2;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IStateUpdateListener;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdateListener;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ModelDelta;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.widgets.Display;
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

    private ITreeModelContentProviderTarget fViewer;

    /**
     * Mask used to filter delta updates coming from the model.
     */
    private int fModelDeltaMask = ~0;

    /**
     * Map tree paths to model proxy responsible for element
     * 
     * Used to install different model proxy instances for one element depending
     * on the tree path.
     */
    private Map fTreeModelProxies = new HashMap(); // tree model proxy by
                                                   // element tree path

    /**
     * Map element to model proxy responsible for it.
     * 
     * Used to install a single model proxy which is responsible for all
     * instances of an element in the model tree.
     */
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
     * Viewer update listeners
     */
    private ListenerList fUpdateListeners = new ListenerList();

    /**
     * State update listeners
     */
    private ListenerList fStateUpdateListeners = new ListenerList();

    /**
     * Map of updates in progress: element path -> list of requests
     */
    protected Map fRequestsInProgress = new HashMap();

    /**
     * Map of dependent requests waiting for parent requests to complete:
     * element path -> list of requests
     */
    protected Map fWaitingRequests = new HashMap();

    /**
     * Map of viewer states keyed by viewer input mementos
     */
    private Map fViewerStates = Collections.synchronizedMap(new LRUMap(20));

    /**
     * Pending viewer state to be restored
     */
    protected ModelDelta fPendingState = null;

    /**
     * Flag indicating that the content provider is performing
     * state restore operations.  
     */
    private boolean fInStateRestore = false; 
    
    protected interface IPendingRevealDelta extends IViewerUpdateListener {
    	/**
    	 * 
    	 * @return delta that should be revealed
    	 */
    	ModelDelta getDelta();
    	
    	/**
    	 * Dispose the pending operation
    	 */
    	void dispose();
    }
    
    /**
     * Postpone restoring REVEAL element until the current updates are complete.
     * See bug 324100
     */
    protected IPendingRevealDelta fPendingSetTopItem = null;
    
    private static class CompareRequestKey {
        CompareRequestKey(TreePath path, IModelDelta delta) {
            fPath = path;
            fDelta = delta;
        }

        TreePath fPath;

        IModelDelta fDelta;

        public boolean equals(Object obj) {
            if (obj instanceof CompareRequestKey) {
                CompareRequestKey key = (CompareRequestKey) obj;
                return key.fDelta.equals(fDelta) && key.fPath.equals(fPath);
            }
            return false;
        }

        public int hashCode() {
            return fDelta.hashCode() + fPath.hashCode();
        }
    }

    private Map fCompareRequestsInProgress = new LinkedHashMap();

    /**
     * Set of IMementoManager's that are currently saving state
     */
    private Set fPendingStateSaves = new HashSet();

    /**
     * Used to queue a viewer input for state restore
     */
    private Object fQueuedRestore = null;

    /**
     * Dummy marker element used in the state delta. The marker indicates that a
     * given element in the pending state delta has been removed. It replaces
     * the original element so that it may optionally be garbage collected.
     */
    private final static String ELEMENT_REMOVED = "ELEMENT_REMOVED"; //$NON-NLS-1$

    /**
     * LRU cache for viewer states
     */
    class LRUMap extends LinkedHashMap {
        private static final long serialVersionUID = 1L;

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
     * Additional state update type constants
     */
    static final int STATE_SAVE_SEQUENCE_BEGINS = 4;

    static final int STATE_SAVE_SEQUENCE_COMPLETE = 5;

    static final int STATE_RESTORE_SEQUENCE_BEGINS = 6;

    static final int STATE_RESTORE_SEQUENCE_COMPLETE = 7;

    /**
     * Constant for an empty tree path.
     */
    protected static final TreePath EMPTY_TREE_PATH = new TreePath(new Object[] {});

    // debug flags
    public static String DEBUG_PRESENTATION_ID = null;

    public static boolean DEBUG_CONTENT_PROVIDER = false;

    public static boolean DEBUG_UPDATE_SEQUENCE = false;

    public static boolean DEBUG_STATE_SAVE_RESTORE = false;

    public static boolean DEBUG_DELTAS = false;

    public static boolean DEBUG_TEST_PRESENTATION_ID(IPresentationContext context) {
        if (context == null) {
            return true;
        }
        return DEBUG_PRESENTATION_ID == null || DEBUG_PRESENTATION_ID.equals(context.getId());
    }
    
    static {
        DEBUG_PRESENTATION_ID = Platform.getDebugOption("org.eclipse.debug.ui/debug/viewers/presentationId"); //$NON-NLS-1$
        if (!DebugUIPlugin.DEBUG || "".equals(DEBUG_PRESENTATION_ID)) { //$NON-NLS-1$
            DEBUG_PRESENTATION_ID = null;
        }
        DEBUG_CONTENT_PROVIDER = DebugUIPlugin.DEBUG && "true".equals( //$NON-NLS-1$
            Platform.getDebugOption("org.eclipse.debug.ui/debug/viewers/contentProvider")); //$NON-NLS-1$
        DEBUG_UPDATE_SEQUENCE = DebugUIPlugin.DEBUG && "true".equals( //$NON-NLS-1$
            Platform.getDebugOption("org.eclipse.debug.ui/debug/viewers/updateSequence")); //$NON-NLS-1$
        DEBUG_STATE_SAVE_RESTORE = DebugUIPlugin.DEBUG && "true".equals( //$NON-NLS-1$
            Platform.getDebugOption("org.eclipse.debug.ui/debug/viewers/stateSaveRestore")); //$NON-NLS-1$
        DEBUG_DELTAS = DebugUIPlugin.DEBUG && "true".equals( //$NON-NLS-1$
            Platform.getDebugOption("org.eclipse.debug.ui/debug/viewers/deltas")); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.IContentProvider#dispose()
     */
    public void dispose() {
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
            fWaitingRequests.clear();
        }

        synchronized(this) {
            for (Iterator itr = fPendingStateSaves.iterator(); itr.hasNext(); ) {
                ((IMementoManager)itr.next()).cancel();
            }
            
            fModelListeners.clear();
            fUpdateListeners.clear();
            fStateUpdateListeners.clear();
            disposeAllModelProxies();
            fViewer = null;
        }
    }

    public synchronized boolean isDisposed() {
        return fViewer == null;
    }

    public synchronized void inputAboutToChange(ITreeModelContentProviderTarget viewer, Object oldInput, Object newInput) {
        if (newInput != oldInput && oldInput != null) {
            for (Iterator itr = fCompareRequestsInProgress.values().iterator(); itr.hasNext();) {
                ((ElementCompareRequest) itr.next()).cancel();
                itr.remove();
            }
            saveViewerState(oldInput);
        }
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface
     * .viewers.Viewer, java.lang.Object, java.lang.Object)
     */
    public synchronized void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        fViewer = (ITreeModelContentProviderTarget) viewer;
        if (newInput != oldInput) {
            cancelSubtreeUpdates(TreePath.EMPTY);
            disposeAllModelProxies();
            cancelSubtreeUpdates(TreePath.EMPTY);
            fTransform.clear();
            if (newInput != null) {
                installModelProxy(newInput, TreePath.EMPTY);
                restoreViewerState(newInput);
            }
        }
    }

    /**
     * Restores the viewer state unless a save is taking place. If a save is
     * taking place, the restore is queued.
     * 
     * @param input
     *            viewer input
     */
    protected synchronized void restoreViewerState(final Object input) {
        fPendingState = null;
        if (isSavingState()) {
            fQueuedRestore = input;
        } else {
            startRestoreViewerState(input);
        }
    }

    /**
     * Restores viewer state for the given input
     * 
     * @param input
     *            viewer input
     */
    private synchronized void startRestoreViewerState(final Object input) {
        fPendingState = null;
        final IElementMementoProvider defaultProvider = ViewerAdapterService.getMementoProvider(input);
        if (defaultProvider != null) {
            // build a model delta representing expansion and selection state
            final ModelDelta delta = new ModelDelta(input, IModelDelta.NO_CHANGE);
            final XMLMemento inputMemento = XMLMemento.createWriteRoot("VIEWER_INPUT_MEMENTO"); //$NON-NLS-1$
            final IMementoManager manager = new IMementoManager() {

                private IElementMementoRequest fRequest;

                /*
                 * (non-Javadoc)
                 * 
                 * @see
                 * org.eclipse.debug.internal.ui.viewers.model.provisional.viewers
                 * .
                 * IMementoManager#requestComplete(org.eclipse.debug.internal.ui
                 * .viewers.model.provisional.IElementMementoRequest)
                 */
                public void requestComplete(IElementMementoRequest request) {
                    notifyStateUpdate(input, UPDATE_COMPLETE, request);

                    if (!request.isCanceled() && (request.getStatus() == null || request.getStatus().isOK())) {
                        XMLMemento keyMemento = (XMLMemento) delta.getElement();
                        StringWriter writer = new StringWriter();
                        try {
                            keyMemento.save(writer);
                            final String keyMementoString = writer.toString();
                            ModelDelta stateDelta = (ModelDelta) fViewerStates.get(keyMementoString);
                            if (stateDelta != null) {
                                if (DEBUG_STATE_SAVE_RESTORE && DEBUG_TEST_PRESENTATION_ID(getPresentationContext()))  {
                                    System.out.println("STATE RESTORE INPUT COMARE ENDED : " + fRequest + " - MATCHING STATE FOUND"); //$NON-NLS-1$ //$NON-NLS-2$
                                }

                                // begin restoration
                                UIJob job = new UIJob("restore state") { //$NON-NLS-1$
                                    public IStatus runInUIThread(IProgressMonitor monitor) {
                                        if (!isDisposed() && input.equals(getViewer().getInput())) {
                                            ModelDelta stateDelta2 = (ModelDelta) fViewerStates.remove(keyMementoString);
                                            if (stateDelta2 != null) {
                                                if (DEBUG_STATE_SAVE_RESTORE && DEBUG_TEST_PRESENTATION_ID(getPresentationContext()))  {
                                                    System.out.println("STATE RESTORE BEGINS"); //$NON-NLS-1$
                                                    System.out.println("\tRESTORE: " + stateDelta2.toString()); //$NON-NLS-1$
                                                    notifyStateUpdate(input, STATE_RESTORE_SEQUENCE_BEGINS, null);
                                                }
                                                stateDelta2.setElement(input);
                                                fPendingState = stateDelta2;
                                                doInitialRestore(fPendingState);
                                            }
                                        } else {
                                            if (DEBUG_STATE_SAVE_RESTORE && DEBUG_TEST_PRESENTATION_ID(getPresentationContext()))  {
                                                System.out.println("STATE RESTORE CANCELED."); //$NON-NLS-1$
                                            }
                                        }
                                        return Status.OK_STATUS;
                                    }

                                };
                                job.setSystem(true);
                                job.schedule();
                            } else {
                                if (DEBUG_STATE_SAVE_RESTORE && DEBUG_TEST_PRESENTATION_ID(getPresentationContext()))  {
                                    System.out.println("STATE RESTORE INPUT COMARE ENDED : " + fRequest + " - NO MATCHING STATE"); //$NON-NLS-1$ //$NON-NLS-2$
                                }
                            }
                        } catch (IOException e) {
                            DebugUIPlugin.log(e);
                        }
                    } else {
                        notifyStateUpdate(input, STATE_RESTORE_SEQUENCE_BEGINS, null);
                    }
                }

                /*
                 * (non-Javadoc)
                 * 
                 * @see
                 * org.eclipse.debug.internal.ui.viewers.model.provisional.viewers
                 * .IMementoManager#processReqeusts()
                 */
                public void processReqeusts() {
                    notifyStateUpdate(input, STATE_RESTORE_SEQUENCE_BEGINS, null);
                    if (DEBUG_STATE_SAVE_RESTORE && DEBUG_TEST_PRESENTATION_ID(getPresentationContext())) {
                        System.out.println("STATE RESTORE INPUT COMARE BEGIN : " + fRequest); //$NON-NLS-1$
                    }
                    notifyStateUpdate(input, UPDATE_BEGINS, fRequest);
                    defaultProvider.encodeElements(new IElementMementoRequest[] { fRequest });
                }

                /*
                 * (non-Javadoc)
                 * 
                 * @see
                 * org.eclipse.debug.internal.ui.viewers.model.provisional.viewers
                 * .
                 * IMementoManager#addRequest(org.eclipse.debug.internal.ui.viewers
                 * .model.provisional.IElementMementoRequest)
                 */
                public void addRequest(IElementMementoRequest req) {
                    fRequest = req;
                }
                
                public void cancel() {
                    // not used
                }

            };
            manager.addRequest(new ElementMementoRequest(ModelContentProvider.this, getViewer().getInput(), manager,
                getPresentationContext(), delta.getElement(), getViewerTreePath(delta), inputMemento, delta));
            manager.processReqeusts();
        } else {
            if (DEBUG_STATE_SAVE_RESTORE && DEBUG_TEST_PRESENTATION_ID(getPresentationContext()))  {
                System.out.println("STATE RESTORE: No input memento provider"); //$NON-NLS-1$
            }            
        }
    }

    /**
     * Restore selection/expansion based on items already in the viewer
     */
    protected abstract void doInitialRestore(ModelDelta delta);

    /**
     * @param delta
     */
    abstract void restorePendingStateNode(final ModelDelta delta, boolean knowsHasChildren, boolean knowsChildCount,
        boolean checkChildrenRealized);

    public void cancelRestore(final TreePath path, final int flags) {
        if (fInStateRestore) {
        	// If we are currently processing pending state already, ignore 
        	// cancelRestore requests.  These requests may be triggered in the viewer
        	// by changes to the tree state (Bug 295585).
        	return;
        }

        if ((flags & IModelDelta.REVEAL) != 0 && fPendingSetTopItem != null) {
            fPendingSetTopItem.dispose();
            return;
        }

        // Nothing else to do 
        if (fPendingState == null) {
            return;
        }
        
        if ((flags & (IModelDelta.SELECT | IModelDelta.REVEAL)) != 0) {
        	// If we're canceling reveal and this is waiting for updates to complete
        	// then just cancel it and return
        	
            // If we're canceling select or reveal, cancel it for all of pending deltas
            final int mask = flags & (IModelDelta.SELECT | IModelDelta.REVEAL);
            fPendingState.accept(new IModelDeltaVisitor() {
                public boolean visit(IModelDelta delta, int depth) {
                    int deltaFlags = delta.getFlags();
                    int newFlags = deltaFlags & ~mask;
                    if (DEBUG_STATE_SAVE_RESTORE && DEBUG_TEST_PRESENTATION_ID(getPresentationContext())) {
                        if (deltaFlags != newFlags) {
                            System.out.println("\tCANCEL: " + delta.getElement() + "(" + Integer.toHexString(deltaFlags & mask) + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        }
                    }
                    ((ModelDelta)delta).setFlags(newFlags);
                    return true;
                }
            });
        }
        if ((flags & ~(IModelDelta.SELECT | IModelDelta.REVEAL)) != 0) {
            final int mask = flags & ~(IModelDelta.SELECT | IModelDelta.REVEAL);
            // For other flags (EXPAND/COLLAPSE), cancel only from the matching path.
            fPendingState.accept(new IModelDeltaVisitor() {
                public boolean visit(IModelDelta delta, int depth) {
                    if (depth < path.getSegmentCount()) {
                        // Descend until we reach a matching depth.
                        TreePath deltaPath = getViewerTreePath(delta);
                        if (path.startsWith(deltaPath, null)) {
                            return true;
                        } else {
                            return false;
                        }
                    }
                    else if (depth == path.getSegmentCount()) {
                        TreePath deltaPath = getViewerTreePath(delta);
                        if (deltaPath.equals(path)) {
                            int deltaFlags = delta.getFlags();
                            int newFlags = deltaFlags & ~mask;
                            if (DEBUG_STATE_SAVE_RESTORE && DEBUG_TEST_PRESENTATION_ID(getPresentationContext())) {
                                if (deltaFlags != newFlags) {
                                    System.out.println("\tCANCEL: " + delta.getElement() + "(" + Integer.toHexString(deltaFlags & mask) + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                }
                            }
                            ((ModelDelta)delta).setFlags(newFlags);
                            if ((flags & IModelDelta.EXPAND) != 0) {
                                // Descend delta to clear the EXPAND flags of a canceled expand
                                return true;
                            }
                        } 
                        return false;
                    } else {
                        // We're clearing out flags of a matching sub-tree
                        // assert (flags & IModelDelta.EXPAND) != 0;
                        
                        if (DEBUG_STATE_SAVE_RESTORE && DEBUG_TEST_PRESENTATION_ID(getPresentationContext())) {
                            if (delta.getFlags() != IModelDelta.NO_CHANGE) {
                                System.out.println("\tCANCEL: " + delta.getElement() + "(" + Integer.toHexString(delta.getFlags()) + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                            }
                        }
                        ((ModelDelta)delta).setFlags(IModelDelta.NO_CHANGE);
                        return true;
                    }
                }
            });
        }
    }
    
    protected void appendToPendingStateDelta(final TreePath path) {
        if (DEBUG_STATE_SAVE_RESTORE && DEBUG_TEST_PRESENTATION_ID(getPresentationContext()))  {
            System.out.println("STATE APPEND BEGIN: " + path.getLastSegment()); //$NON-NLS-1$
        }

        // build a model delta representing expansion and selection state
        final ModelDelta appendDeltaRoot = new ModelDelta(getViewer().getInput(), IModelDelta.NO_CHANGE);
        ModelDelta delta = appendDeltaRoot;
        for (int i = 0; i < path.getSegmentCount(); i++) {
            delta = delta.addNode(path.getSegment(i), IModelDelta.NO_CHANGE);
        }

        if (!fViewer.saveElementState(path, delta, IModelDelta.COLLAPSE | IModelDelta.EXPAND | IModelDelta.SELECT)) {
            // Path to save the state was not found or there was no 
            // (expansion) state to save!  Abort.
            if (DEBUG_STATE_SAVE_RESTORE && DEBUG_TEST_PRESENTATION_ID(getPresentationContext())) {
                System.out.println("STATE APPEND CANCEL: Element " + path.getLastSegment() + " not found."); //$NON-NLS-1$ //$NON-NLS-2$
            }
            return;
        }

        // Append a marker CONTENT flag to all the delta nodes that contain the
        // EXPAND node. These
        // markers are used by the restore logic to know when a delta node can
        // be removed.
        delta.accept(new IModelDeltaVisitor() {
            public boolean visit(IModelDelta d, int depth) {
                if ((d.getFlags() & IModelDelta.EXPAND) != 0) {
                    ((ModelDelta) d).setFlags(d.getFlags() | IModelDelta.CONTENT);
                }
                return true;
            }
        });

        if (DEBUG_STATE_SAVE_RESTORE && DEBUG_TEST_PRESENTATION_ID(getPresentationContext())) {
            System.out.println("\tAPPEND DELTA: " + appendDeltaRoot); //$NON-NLS-1$
        }

        if (fPendingState != null) {
            // If the restore for the current input was never completed,
            // preserve
            // that restore along with the restore that was completed.
            if (DEBUG_STATE_SAVE_RESTORE && DEBUG_TEST_PRESENTATION_ID(getPresentationContext())) {
                System.out.println("\tAPPEND OUTSTANDING RESTORE: " + fPendingState); //$NON-NLS-1$
            }

            // If the append delta is generated for a sub-tree, copy the pending dela 
            // attributes into the pending delta.
            if (path.getSegmentCount() > 0) {
                fPendingState.accept( new IModelDeltaVisitor() {
                    public boolean visit(IModelDelta pendingDeltaNode, int depth) {
                        TreePath pendingDeltaPath = getViewerTreePath(pendingDeltaNode);
                        if (path.startsWith(pendingDeltaPath, null)) 
                        {
                            ModelDelta appendDelta = findDeltaForPath(appendDeltaRoot, pendingDeltaPath);
                            appendDelta.setFlags(pendingDeltaNode.getFlags());
                            appendDelta.setChildCount(pendingDeltaNode.getChildCount());
                            appendDelta.setIndex(pendingDeltaNode.getIndex());
                            return true;
                        }
                        return false;
                    }
                });
            }

            // Copy the pending state into the new appended state.
            fPendingState.accept( new IModelDeltaVisitor() {
                public boolean visit(IModelDelta pendingDeltaNode, int depth) {
                    // Skip the top element
                    if (pendingDeltaNode.getParentDelta() == null) {
                        return true;
                    }

                    // Find the node in the save delta which is the parent
                    // of to the current pending delta node.
                    // If the parent node cannot be found, it means that
                    // most likely the user collapsed the parent node before
                    // the children were ever expanded.
                    // If the pending state node already exists in the parent
                    // node, it is already processed and we can skip it.
                    // If the pending state node does not contain any flags,
                    // we can also skip it.
                    ModelDelta saveDeltaNode = findSubDeltaParent(appendDeltaRoot, pendingDeltaNode);
                    if (saveDeltaNode != null && 
                        !isDeltaInParent(pendingDeltaNode, saveDeltaNode) &&
                        pendingDeltaNode.getFlags() != IModelDelta.NO_CHANGE) 
                    {
                        saveDeltaNode.setChildCount(pendingDeltaNode.getParentDelta().getChildCount());
                        copyIntoDelta(pendingDeltaNode, saveDeltaNode);
                    } else {
                        if (DEBUG_STATE_SAVE_RESTORE && DEBUG_TEST_PRESENTATION_ID(getPresentationContext())) {
                            System.out.println("\tSKIPPED: " + pendingDeltaNode.getElement()); //$NON-NLS-1$
                        }
                    }

                    // If the pending delta node has a memento element, its
                    // children should also be mementos therefore the copy
                    // delta operation should have added all the children
                    // of this pending delta node into the save delta.
                    if (pendingDeltaNode.getElement() instanceof IMemento) {
                        return false;
                    } else {
                        return pendingDeltaNode.getChildCount() > 0;
                    }
                }
                
            });
        }

        if (appendDeltaRoot.getChildDeltas().length > 0) {
            // Set the new delta root as the pending state delta.
            if (fPendingState == null) {
                notifyStateUpdate(appendDeltaRoot.getElement(), STATE_RESTORE_SEQUENCE_BEGINS, null);
            }
            fPendingState = appendDeltaRoot;
            if (DEBUG_STATE_SAVE_RESTORE && DEBUG_TEST_PRESENTATION_ID(getPresentationContext())) {
                System.out.println("STATE APPEND COMPLETE " + fPendingState); //$NON-NLS-1$
            }
        } else {
            if (DEBUG_STATE_SAVE_RESTORE && DEBUG_TEST_PRESENTATION_ID(getPresentationContext())) {
                System.out.println("STATE APPEND CANCELED: No Data"); //$NON-NLS-1$
            }
        }
    }

    /**
     * Perform any restoration required for the given tree path.
     * 
     * @param path
     */
    protected synchronized void restorePendingStateOnUpdate(final TreePath path, final int modelIndex, final boolean knowsHasChildren,
        final boolean knowsChildCount, final boolean checkChildrenRealized) {
        if (fPendingState == null) {
            return;
        }

        IModelDeltaVisitor visitor = new IModelDeltaVisitor() {
            public boolean visit(final IModelDelta delta, int depth) {

                Object element = delta.getElement();
                Object potentialMatch = depth != 0 ? path.getSegment(depth - 1) : getViewer().getInput();
                // Only process if the depth in the delta matches the tree path.
                if (depth == path.getSegmentCount()) {
                    if (element instanceof IMemento) {
                        IElementMementoProvider provider = ViewerAdapterService.getMementoProvider(potentialMatch);
                        if (provider == null) {
                            provider = ViewerAdapterService.getMementoProvider(getViewer().getInput());
                        }
                        if (provider != null) {
                            CompareRequestKey key = new CompareRequestKey(path, delta);
                            ElementCompareRequest existingRequest = (ElementCompareRequest) fCompareRequestsInProgress
                                .get(key);
                            if (existingRequest != null) {
                                // Check all the running compare updates for a
                                // matching tree path.
                                // If found, just update the flags.
                                existingRequest.setKnowsHasChildren(knowsHasChildren);
                                existingRequest.setKnowsChildCount(knowsChildCount);
                                existingRequest.setCheckChildrenRealized(checkChildrenRealized);
                            } else {
                                // Start a new compare request
                                ElementCompareRequest compareRequest = new ElementCompareRequest(
                                    ModelContentProvider.this, getViewer().getInput(), potentialMatch, path,
                                    (IMemento) element, (ModelDelta) delta, modelIndex, knowsHasChildren,
                                    knowsChildCount, checkChildrenRealized);
                                fCompareRequestsInProgress.put(key, compareRequest);
                                if (DEBUG_STATE_SAVE_RESTORE && DEBUG_TEST_PRESENTATION_ID(getPresentationContext())) {
                                    System.out.println("\tSTATE BEGIN: " + compareRequest); //$NON-NLS-1$
                                }
                                notifyStateUpdate(element, UPDATE_BEGINS, compareRequest);
                                provider.compareElements(new IElementCompareRequest[] { compareRequest });
                            }
                        }
                    } else if (element.equals(potentialMatch)) {
                        // Element comparison already succeeded, and it matches
                        // our element.
                        // Call restore with delta to process the delta flags.
                        restorePendingStateNode((ModelDelta) delta, knowsHasChildren, knowsChildCount, checkChildrenRealized);
                    }
                    return false;
                }
                // Only follow the paths that match the delta.
                return element.equals(potentialMatch);
            }
        };

        try {
        	fInStateRestore = true;
        	fPendingState.accept(visitor);
        } 
        finally {
        	fInStateRestore = false;
        }
        checkIfRestoreComplete();
    }

    void compareFinished(ElementCompareRequest request, ModelDelta delta) {
        notifyStateUpdate(request.getViewerInput(), UPDATE_COMPLETE, request);
        if (DEBUG_STATE_SAVE_RESTORE && DEBUG_TEST_PRESENTATION_ID(getPresentationContext())) {
            System.out.println("\tSTATE END: " + request + " = " + false); //$NON-NLS-1$ //$NON-NLS-2$
        }

        fCompareRequestsInProgress.remove(new CompareRequestKey(request.getElementPath(), delta));
        if (!request.isCanceled()) {
            if (request.isEqual()) {
                delta.setElement(request.getElement());
                restorePendingStateNode(delta, request.knowsHasChildren(), request.knowChildCount(), request.checkChildrenRealized());
            } else if (request.getModelIndex() != -1) {
                // Comparison failed.
                // Check if the delta has a reveal flag, and if its index 
                // matches the index of the element that it was compared 
                // against. If this is the case, strip the reveal flag from 
                // the delta as it is most likely not applicable anymore.
                if ((delta.getFlags() & IModelDelta.REVEAL) != 0 && delta.getIndex() == request.getModelIndex()) {
                    delta.setFlags(delta.getFlags() & ~IModelDelta.REVEAL);
                }
            }
        }
        checkIfRestoreComplete();
    }

    /**
     * Saves the viewer's state for the previous input. * @param oldInput
     */
    protected void saveViewerState(Object input) {
        IElementMementoProvider stateProvider = ViewerAdapterService.getMementoProvider(input);
        if (stateProvider != null) {
            if (DEBUG_STATE_SAVE_RESTORE && DEBUG_TEST_PRESENTATION_ID(getPresentationContext())) {
                System.out.println("STATE SAVE BEGIN: " + input); //$NON-NLS-1$
            }

            // build a model delta representing expansion and selection state
            final ModelDelta saveDeltaRoot = new ModelDelta(input, IModelDelta.NO_CHANGE);
            buildViewerState(saveDeltaRoot);
            if (DEBUG_STATE_SAVE_RESTORE && DEBUG_TEST_PRESENTATION_ID(getPresentationContext())) {
                System.out.println("\tSAVE DELTA FROM VIEW:\n" + saveDeltaRoot); //$NON-NLS-1$
            }

            // check if pending restore reveal
            if (fPendingSetTopItem != null) {
            	// set back the pending reveal flag
            	ModelDelta revealDelta = fPendingSetTopItem.getDelta();
            	revealDelta.setFlags(revealDelta.getFlags() | IModelDelta.REVEAL);
            	
            	fPendingSetTopItem.dispose();
            	
            	ModelDelta saveDeltaNode = findSubDeltaParent(saveDeltaRoot, revealDelta);
            	if (saveDeltaNode != null) {
            		clearRevealFlag(saveDeltaRoot);
            		boolean childFounded = false;
            		for (int i = 0; i < saveDeltaNode.getChildDeltas().length; i++) {
            			ModelDelta child = (ModelDelta)saveDeltaNode.getChildDeltas()[i]; 
            			if (deltasEqual(child, revealDelta)) {
            				child.setFlags(child.getFlags() | IModelDelta.REVEAL);
            				childFounded = true;
            				break;
            			}
            		}
            		
            		// the node should be added if not found
            		if (!childFounded) {
            			saveDeltaNode.setChildCount(revealDelta.getParentDelta().getChildCount());
                        copyIntoDelta(revealDelta, saveDeltaNode);
            		}
                } else {
                    if (DEBUG_STATE_SAVE_RESTORE && DEBUG_TEST_PRESENTATION_ID(getPresentationContext())) {
                        System.out.println("\tSKIPPED: " + revealDelta.getElement()); //$NON-NLS-1$
                    }
            	}
            }
            
            if (fPendingState != null) {
                // If the restore for the current input was never completed,
                // preserve
                // that restore along with the restore that was completed.
                if (DEBUG_STATE_SAVE_RESTORE && DEBUG_TEST_PRESENTATION_ID(getPresentationContext())) {
                    System.out.println("\tSAVE OUTSTANDING RESTORE: " + fPendingState); //$NON-NLS-1$
                }

                IModelDeltaVisitor pendingStateVisitor = new IModelDeltaVisitor() {
                    public boolean visit(IModelDelta pendingDeltaNode, int depth) {
                        // Ignore the top element.
                        if (pendingDeltaNode.getParentDelta() == null) {
                            return true;
                        }

                        // Find the node in the save delta which is the parent
                        // of to the current pending delta node.
                        // If the parent node cannot be found, it means that
                        // most likely the user collapsed the parent node before
                        // the children were ever expanded.
                        // If the pending state node already exists in the
                        // parent
                        // node, it is already processed and we can skip it.
                        // If the pending state node does not contain any flags,
                        // we can also skip it.
                        ModelDelta saveDeltaNode = findSubDeltaParent(saveDeltaRoot, pendingDeltaNode);
                        if (saveDeltaNode != null && !isDeltaInParent(pendingDeltaNode, saveDeltaNode)
                            && pendingDeltaNode.getFlags() != IModelDelta.NO_CHANGE) {
                            // There should be only one delta element with
                            // the REVEAL flag in the entire save delta. The
                            // reveal flag in the pending delta trumps the one
                            // in the save delta because most likely the restore
                            // operation did not yet complete the reveal
                            // operation.
                            if ((pendingDeltaNode.getFlags() & IModelDelta.REVEAL) != 0) {
                                clearRevealFlag(saveDeltaRoot);
                            }
                            saveDeltaNode.setChildCount(pendingDeltaNode.getParentDelta().getChildCount());
                            copyIntoDelta(pendingDeltaNode, saveDeltaNode);
                        } else {
                            if (DEBUG_STATE_SAVE_RESTORE && DEBUG_TEST_PRESENTATION_ID(getPresentationContext())) {
                                System.out.println("\tSKIPPED: " + pendingDeltaNode.getElement()); //$NON-NLS-1$
                            }
                        }

                        // If the pending delta node has a memento element, its
                        // children should also be mementos therefore the copy
                        // delta operation should have added all the children
                        // of this pending delta node into the save delta.
                        if (pendingDeltaNode.getElement() instanceof IMemento) {
                            return false;
                        } else {
                            return pendingDeltaNode.getChildCount() > 0;
                        }
                    }
                };
                fPendingState.accept(pendingStateVisitor);
            }

            if (saveDeltaRoot.getChildDeltas().length > 0) {
                // encode delta with mementos in place of elements, in non-UI
                // thread
                encodeDelta(saveDeltaRoot, stateProvider);
            } else {
                if (DEBUG_STATE_SAVE_RESTORE && DEBUG_TEST_PRESENTATION_ID(getPresentationContext())) {
                    System.out.println("STATE SAVE CANCELED, NO DATA"); //$NON-NLS-1$
                }
            }
        }
    }

    private void clearRevealFlag(ModelDelta saveRootDelta) {
        IModelDeltaVisitor clearDeltaVisitor = new IModelDeltaVisitor() {
            public boolean visit(IModelDelta delta, int depth) {
                if ((delta.getFlags() & IModelDelta.REVEAL) != 0) {
                    ((ModelDelta) delta).setFlags(delta.getFlags() & ~IModelDelta.REVEAL);
                }
                return true;
            }
        };
        saveRootDelta.accept(clearDeltaVisitor);
    }

    private ModelDelta findSubDeltaParent(ModelDelta destinationDeltaRoot, IModelDelta subDelta) {
        // Create a path of elements to the sub-delta.
        LinkedList deltaPath = new LinkedList();
        IModelDelta delta = subDelta;
        while (delta.getParentDelta() != null) {
            delta = delta.getParentDelta();
            deltaPath.addFirst(delta);
        }

        // For each element in the path of the sub-delta, find the corresponding
        // element in the destination delta
        Iterator itr = deltaPath.iterator();
        // Skip the root element
        itr.next();
        ModelDelta saveDelta = destinationDeltaRoot;
        outer: while (itr.hasNext()) {
            IModelDelta itrDelta = (IModelDelta) itr.next();
            for (int i = 0; i < saveDelta.getChildDeltas().length; i++) {
                if (deltasEqual(saveDelta.getChildDeltas()[i], itrDelta)) {
                    saveDelta = (ModelDelta) saveDelta.getChildDeltas()[i];
                    continue outer;
                }
            }
            return null;
        }
        return saveDelta;
    }

    private ModelDelta findDeltaForPath(ModelDelta root, TreePath path) {
        ModelDelta delta = root;
        for (int i = 0; i < path.getSegmentCount(); i++) {
            delta = delta.getChildDelta(path.getSegment(i));
            if (delta == null) {
                return null;
            }
        }
        return delta;
    }

    private boolean deltasEqual(IModelDelta d1, IModelDelta d2) {
        // Note: don't compare the child count, because it is
        // incorrect for nodes which have not been expanded yet.
        return d1.getElement().equals(d2.getElement()) && d1.getIndex() == d2.getIndex();
    }

    private boolean isDeltaInParent(IModelDelta delta, ModelDelta destParent) {
        for (int i = 0; i < destParent.getChildDeltas().length; i++) {
            if (deltasEqual(destParent.getChildDeltas()[i], delta)) {
                return true;
            }
        }
        return false;
    }

    private void copyIntoDelta(IModelDelta delta, ModelDelta destParent) {
        ModelDelta newDelta = destParent.addNode(delta.getElement(), delta.getIndex(), delta.getFlags(), delta
            .getChildCount());
        for (int i = 0; i < delta.getChildDeltas().length; i++) {
            copyIntoDelta(delta.getChildDeltas()[i], newDelta);
        }
    }

    /**
     * Encodes delta elements into mementos using the given provider.
     * 
     * @param delta
     * @param stateProvider
     */
    protected void encodeDelta(final ModelDelta rootDelta, final IElementMementoProvider defaultProvider) {
        final Object input = rootDelta.getElement();
        final XMLMemento inputMemento = XMLMemento.createWriteRoot("VIEWER_INPUT_MEMENTO"); //$NON-NLS-1$
        final XMLMemento childrenMemento = XMLMemento.createWriteRoot("CHILDREN_MEMENTO"); //$NON-NLS-1$
        final IMementoManager manager = new IMementoManager() {

            /**
             * list of memento fRequests
             */
            private List fRequests = new ArrayList();

            /**
             * Flag indicating whether the encoding of delta has been canceled.
             */
            private boolean fCanceled = false;

            /*
             * (non-Javadoc)
             * 
             * @see
             * org.eclipse.debug.internal.ui.viewers.model.provisional.viewers
             * .IMementoManager
             * #requestComplete(org.eclipse.debug.internal.ui.viewers
             * .model.provisional.IElementMementoRequest)
             */
            public void requestComplete(IElementMementoRequest request) {
                notifyStateUpdate(input, UPDATE_COMPLETE, request);
                if (DEBUG_STATE_SAVE_RESTORE && DEBUG_TEST_PRESENTATION_ID(getPresentationContext())) {
                    System.out.println("\tSTATE END: " + request); //$NON-NLS-1$
                }

                if (!request.isCanceled() && (request.getStatus() == null || request.getStatus().isOK())) {
                    boolean requestsComplted = false; 
                    synchronized(this) {
                        if (!fCanceled) { 
                            fRequests.remove(request);
                            requestsComplted = fRequests.isEmpty();
                        }
                    }
                    if (requestsComplted) {
                        XMLMemento keyMemento = (XMLMemento) rootDelta.getElement();
                        StringWriter writer = new StringWriter();
                        try {
                            keyMemento.save(writer);
                            fViewerStates.put(writer.toString(), rootDelta);
                        } catch (IOException e) {
                            DebugUIPlugin.log(e);
                        }
                        if (DEBUG_STATE_SAVE_RESTORE && DEBUG_TEST_PRESENTATION_ID(getPresentationContext())) {
                            System.out.println("STATE SAVE COMPLETED: " + rootDelta); //$NON-NLS-1$
                        }
                        stateSaveComplete(input, this);
                    }
                } else {
                    cancel();
                }
            }

            public void cancel() {
                synchronized (this) {
                    if (fCanceled) {
                        return;
                    }
                                
                    fCanceled = true;
                    Iterator iterator = fRequests.iterator();
                    while (iterator.hasNext()) {
                        IElementMementoRequest req = (IElementMementoRequest) iterator.next();
                        req.cancel();
                    }
                    fRequests.clear();
                    if (DEBUG_STATE_SAVE_RESTORE && DEBUG_TEST_PRESENTATION_ID(getPresentationContext())) {
                        System.out.println("STATE SAVE ABORTED: " + rootDelta.getElement()); //$NON-NLS-1$
                    }
                }
                stateSaveComplete(input, this);
            }
            
            /*
             * (non-Javadoc)
             * 
             * @see
             * org.eclipse.debug.internal.ui.viewers.model.provisional.viewers
             * .IMementoManager#processReqeusts()
             */
            public synchronized void processReqeusts() {
                Map providers = new HashMap();
                Iterator iterator = fRequests.iterator();
                while (iterator.hasNext()) {
                    IElementMementoRequest request = (IElementMementoRequest) iterator.next();
                    notifyStateUpdate(input, UPDATE_BEGINS, request);
                    IElementMementoProvider provider = ViewerAdapterService.getMementoProvider(request.getElement());
                    if (provider == null) {
                        provider = defaultProvider;
                    }
                    List reqs = (List) providers.get(provider);
                    if (reqs == null) {
                        reqs = new ArrayList();
                        providers.put(provider, reqs);
                    }
                    reqs.add(request);
                }
                iterator = providers.entrySet().iterator();
                while (iterator.hasNext()) {
                    Entry entry = (Entry) iterator.next();
                    IElementMementoProvider provider = (IElementMementoProvider) entry.getKey();
                    List reqs = (List) entry.getValue();
                    provider.encodeElements((IElementMementoRequest[]) reqs.toArray(new IElementMementoRequest[reqs
                        .size()]));
                }
            }

            /*
             * (non-Javadoc)
             * 
             * @see
             * org.eclipse.debug.internal.ui.viewers.model.provisional.viewers
             * .IMementoManager
             * #addRequest(org.eclipse.debug.internal.ui.viewers.
             * model.provisional.IElementMementoRequest)
             */
            public synchronized void addRequest(IElementMementoRequest request) {
                fRequests.add(request);
            }

        };
        IModelDeltaVisitor visitor = new IModelDeltaVisitor() {
            public boolean visit(IModelDelta delta, int depth) {
                // Add the CONTENT flag to all nodes with an EXPAND flag. 
                // During restoring, this flag is used as a marker indicating 
                // whether all the content of a given element has been 
                // retrieved.
                if ((delta.getFlags() | IModelDelta.EXPAND) != 0) {
                    ((ModelDelta)delta).setFlags(delta.getFlags() | IModelDelta.CONTENT);
                }
                
                // This is the root element, save the root element memento in 'inputMemento'.
                if (delta.getParentDelta() == null) {
                    manager.addRequest(new ElementMementoRequest(ModelContentProvider.this, input, manager,
                        getPresentationContext(), delta.getElement(), getViewerTreePath(delta), inputMemento,
                        (ModelDelta) delta));
                } else {
                    // If this is another node element, save the memento to a children memento.
                    if (!(delta.getElement() instanceof XMLMemento)) {
                        manager.addRequest(new ElementMementoRequest(ModelContentProvider.this, input, manager,
                            getPresentationContext(), delta.getElement(), getViewerTreePath(delta), childrenMemento
                                .createChild("CHILD_ELEMENT"), (ModelDelta) delta)); //$NON-NLS-1$
                    }
                }
                return true;
            }
        };
        rootDelta.accept(visitor);
        stateSaveStarted(input, manager);
        manager.processReqeusts();
    }

    /**
     * Called when a state save is starting.
     * 
     * @param manager
     */
    private synchronized void stateSaveStarted(Object input, IMementoManager manager) {
        notifyStateUpdate(input, STATE_SAVE_SEQUENCE_BEGINS, null);
        fPendingStateSaves.add(manager);
    }

    /**
     * Called when a state save is complete.
     * 
     * @param manager
     */
    private synchronized void stateSaveComplete(Object input, IMementoManager manager) {
        notifyStateUpdate(input, STATE_SAVE_SEQUENCE_COMPLETE, null);
        fPendingStateSaves.remove(manager);
        if (fQueuedRestore != null) {
            Object temp = fQueuedRestore;
            fQueuedRestore = null;
            restoreViewerState(temp);
        }
    }

    /**
     * Returns whether any state saving is in progress.
     * 
     * @return whether any state saving is in progress
     */
    private synchronized boolean isSavingState() {
        return !fPendingStateSaves.isEmpty();
    }

    /**
     * Builds a delta with the given root delta for expansion/selection state.
     * 
     * @param delta
     *            root delta
     */
    protected abstract void buildViewerState(ModelDelta delta);

    /**
     * Uninstalls the model proxy installed for the given element, if any.
     * 
     * @param element
     */
    protected synchronized void disposeModelProxy(TreePath path) {
        IModelProxy proxy = (IModelProxy) fTreeModelProxies.remove(path);
        if (proxy != null) {
            proxy.dispose();
        }
        proxy = (IModelProxy) fModelProxies.remove(path.getLastSegment());
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

        updatePolicies = fTreeModelProxies.values().iterator();
        while (updatePolicies.hasNext()) {
            IModelProxy proxy = (IModelProxy) updatePolicies.next();
            proxy.dispose();
        }
        fTreeModelProxies.clear();
    }

    protected synchronized IModelProxy[] getModelProxies() {
        IModelProxy[] proxies = new IModelProxy[fTreeModelProxies.size() + fModelProxies.size()];
        fTreeModelProxies.values().toArray(proxies);
        System.arraycopy(fModelProxies.values().toArray(), 0, proxies, fModelProxies.size(), fModelProxies.size());
        return proxies;
    }

    protected synchronized IModelProxy getElementProxy(TreePath path) {
        while (path != null) {
            IModelProxy proxy = (IModelProxy) fTreeModelProxies.get(path);
            if (proxy != null) {
                return proxy;
            }

            Object element = path.getSegmentCount() == 0 ? getViewer().getInput() : path.getLastSegment();
            proxy = (IModelProxy) fModelProxies.get(element);
            if (proxy != null) {
                return proxy;
            }

            path = path.getParentPath();
        }
        return null;
    }

    /**
     * Installs the model proxy for the given element into this content provider
     * if not already installed.
     * 
     * @param element
     *            element to install an update policy for
     */
    protected synchronized void installModelProxy(Object input, TreePath path) {
        if (!fTreeModelProxies.containsKey(path) && !fModelProxies.containsKey(path.getLastSegment())) {
            Object element = path.getSegmentCount() != 0 ? path.getLastSegment() : input;
            IModelProxy proxy = null;
            IModelProxyFactory2 modelProxyFactory2 = ViewerAdapterService.getModelProxyFactory2(element);
            if (modelProxyFactory2 != null) {
                proxy = modelProxyFactory2.createTreeModelProxy(input, path, getPresentationContext());
                if (proxy != null) {
                    fTreeModelProxies.put(path, proxy);
                }
            }
            if (proxy == null) {
                IModelProxyFactory modelProxyFactory = ViewerAdapterService.getModelProxyFactory(element);
                if (modelProxyFactory != null) {
                    proxy = modelProxyFactory.createModelProxy(element, getPresentationContext());
                    if (proxy != null) {
                        fModelProxies.put(element, proxy);
                    }
                }
            }

            if (proxy != null) {
                final IModelProxy finalProxy = proxy;
                if (proxy != null) {
                    Job job = new Job("Model Proxy installed notification job") {//$NON-NLS-1$
                        protected IStatus run(IProgressMonitor monitor) {
                            if (!monitor.isCanceled()) {
                                IPresentationContext context = null;
                                Viewer viewer = null;
                                synchronized (ModelContentProvider.this) {
                                    if (!isDisposed()) {
                                        context = getPresentationContext();
                                        viewer = (Viewer) getViewer();
                                    }
                                }
                                if (context != null && !finalProxy.isDisposed()) {
                                    finalProxy.init(context);
                                    finalProxy.addModelChangedListener(ModelContentProvider.this);
                                    finalProxy.installed(viewer);
                                }
                            }
                            return Status.OK_STATUS;
                        }

                        /*
                         * (non-Javadoc)
                         * 
                         * @see org.eclipse.core.runtime.jobs.Job#shouldRun()
                         */
                        public boolean shouldRun() {
                            return !isDisposed();
                        }
                    };
                    job.setSystem(true);
                    job.schedule();
                }
            }
        }
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
     * @see
     * org.eclipse.debug.internal.ui.viewers.provisional.IModelChangedListener
     * #modelChanged
     * (org.eclipse.debug.internal.ui.viewers.provisional.IModelDelta)
     */
    public void modelChanged(final IModelDelta delta, final IModelProxy proxy) {
        Display display = null;

        // Check if the viewer is still available, i.e. if the content provider
        // is not disposed.
        synchronized(this) {
            if (fViewer != null && !proxy.isDisposed()) {
                display = fViewer.getDisplay();
            }
        }
        if (display != null) {
            // If we're in display thread, process the delta immediately to 
            // avoid "skid" in processing events.
            if (Thread.currentThread().equals(display.getThread())) {
                doModelChanged(delta, proxy);
            }
            else {
                WorkbenchJob job = new WorkbenchJob(fViewer.getDisplay(), "process model delta") { //$NON-NLS-1$
                    public IStatus runInUIThread(IProgressMonitor monitor) {
                        doModelChanged(delta, proxy);
                        return Status.OK_STATUS;
                    }
                };
                job.setSystem(true);
                job.schedule();
            }
        }
    }

    private void doModelChanged(IModelDelta delta, IModelProxy proxy) {
        if (!proxy.isDisposed()) {
            if (DEBUG_DELTAS && DEBUG_TEST_PRESENTATION_ID(getPresentationContext())) {
                DebugUIPlugin.debug("RECEIVED DELTA: " + delta.toString()); //$NON-NLS-1$
            }

            updateModel(delta, getModelDeltaMask());

            // Call model listeners after updating the viewer model.
            Object[] listeners = fModelListeners.getListeners();
            for (int i = 0; i < listeners.length; i++) {
                ((IModelChangedListener) listeners[i]).modelChanged(delta, proxy);
            }
        }
    }
    
    /**
     * @see ITreeModelContentProvider#setModelDeltaMask(int)
     */
    public void setModelDeltaMask(int mask) {
        fModelDeltaMask = mask;
    }

    /**
     * @see ITreeModelContentProvider#getModelDeltaMask()
     */
    public int getModelDeltaMask() {
        return fModelDeltaMask;
    }

    public void updateModel(IModelDelta delta, int mask) {
        IModelDelta[] deltaArray = new IModelDelta[] { delta };
        updateNodes(deltaArray, mask & (IModelDelta.REMOVED | IModelDelta.UNINSTALL));
        updateNodes(deltaArray, mask & ITreeModelContentProvider.UPDATE_MODEL_DELTA_FLAGS
            & ~(IModelDelta.REMOVED | IModelDelta.UNINSTALL));
        updateNodes(deltaArray, mask & ITreeModelContentProvider.CONTROL_MODEL_DELTA_FLAGS);
        
        checkIfRestoreComplete();
    }

    /**
     * Updates the viewer with the following deltas.
     * 
     * @param nodes
     *            Model deltas to be processed.
     * @param override
     *            If true, it overrides the mode which suppresses processing of
     *            SELECT, REVEAL, EXPAND, COLLAPSE flags of {@link IModelDelta}.
     */
    protected void updateNodes(IModelDelta[] nodes, int mask) {
        for (int i = 0; i < nodes.length; i++) {
            IModelDelta node = nodes[i];
            int flags = node.getFlags() & mask;

            if ((flags & IModelDelta.ADDED) != 0) {
                handleAdd(node);
            }
            if ((flags & IModelDelta.REMOVED) != 0) {
                handleRemove(node);
            }
            if ((flags & IModelDelta.CONTENT) != 0) {
                handleContent(node);
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
            if ((flags & IModelDelta.EXPAND) != 0) {
                handleExpand(node);
            }
            if ((flags & IModelDelta.COLLAPSE) != 0) {
                handleCollapse(node);
            }
            if ((flags & IModelDelta.SELECT) != 0) {
                handleSelect(node);
            }
            if ((flags & IModelDelta.REVEAL) != 0) {
                handleReveal(node);
            }
            updateNodes(node.getChildDeltas(), mask);
        }
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
        installModelProxy(getViewer().getInput(), getFullTreePath(delta));
    }

    protected void handleUninstall(IModelDelta delta) {
        disposeModelProxy(getFullTreePath(delta));
    }

    /**
     * Returns a tree path for the node including the root element.
     * 
     * @param node
     *            model delta
     * @return corresponding tree path
     */
    protected TreePath getFullTreePath(IModelDelta node) {
        ArrayList list = new ArrayList();
        while (node.getParentDelta() != null) {
            list.add(0, node.getElement());
            node = node.getParentDelta();
        }
        return new TreePath(list.toArray());
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
    protected ITreeModelContentProviderTarget getViewer() {
        return fViewer;
    }

    /**
     * Translates and returns the given child index from the viewer coordinate
     * space to the model coordinate space.
     * 
     * @param parentPath
     *            path to parent element
     * @param index
     *            index of child element in viewer (filtered) space
     * @return index of child element in model (raw) space
     */
    public/* protected */int viewToModelIndex(TreePath parentPath, int index) {
        return fTransform.viewToModelIndex(parentPath, index);
    }

    /**
     * Translates and returns the given child count from the viewer coordinate
     * space to the model coordinate space.
     * 
     * @param parentPath
     *            path to parent element
     * @param count
     *            number of child elements in viewer (filtered) space
     * @return number of child elements in model (raw) space
     */
    public/* protected */int viewToModelCount(TreePath parentPath, int count) {
        return fTransform.viewToModelCount(parentPath, count);
    }

    /**
     * Translates and returns the given child index from the model coordinate
     * space to the viewer coordinate space.
     * 
     * @param parentPath
     *            path to parent element
     * @param index
     *            index of child element in model (raw) space
     * @return index of child element in viewer (filtered) space or -1 if
     *         filtered
     */
    public int modelToViewIndex(TreePath parentPath, int index) {
        return fTransform.modelToViewIndex(parentPath, index);
    }

    /**
     * Translates and returns the given child count from the model coordinate
     * space to the viewer coordinate space.
     * 
     * @param parentPath
     *            path to parent element
     * @param count
     *            child count element in model (raw) space
     * @return child count in viewer (filtered) space
     */
    public int modelToViewChildCount(TreePath parentPath, int count) {
        return fTransform.modelToViewCount(parentPath, count);
    }

    /**
     * Notes that the child at the specified index of the given parent element
     * has been filtered from the viewer. Returns whether the child at the given
     * index was already filtered.
     * 
     * @param parentPath
     *            path to parent element
     * @param index
     *            index of child element to be filtered
     * @param element
     *            the filtered element
     * @return whether the child was already filtered
     */
    protected boolean addFilteredIndex(TreePath parentPath, int index, Object element) {
        return fTransform.addFilteredIndex(parentPath, index, element);
    }

    /**
     * Notes that the element at the given index has been removed from its
     * parent and filtered indexes should be updated accordingly.
     * 
     * @param parentPath
     *            path to parent element
     * @param index
     *            index of element that was removed
     */
    protected void removeElementFromFilters(TreePath parentPath, int index) {
        fTransform.removeElementFromFilters(parentPath, index);
    }

    /**
     * Removes the given element from filtered elements of the given parent
     * element. Return true if the element was removed, otherwise false.
     * 
     * @param parentPath
     *            path to parent element
     * @param element
     *            element to remove
     * @return whether the element was removed
     */
    protected boolean removeElementFromFilters(TreePath parentPath, Object element) {
        return fTransform.removeElementFromFilters(parentPath, element);
    }

    /**
     * The child count for a parent has been computed. Ensure any filtered items
     * above the given count are cleared.
     * 
     * @param parentPath
     *            path to parent element
     * @param childCount
     *            number of children
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
    public boolean shouldFilter(Object parentElementOrTreePath, Object element) {
        ViewerFilter[] filters = fViewer.getFilters();
        if (filters.length > 0) {
            for (int j = 0; j < filters.length; j++) {
                if (!(filters[j].select((Viewer) fViewer, parentElementOrTreePath, element))) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns whether the given index of the specified parent was previously
     * filtered.
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
    public void unmapPath(TreePath path) {
        // System.out.println("Unmap " + path.getLastSegment());
        fTransform.clear(path);
        cancelSubtreeUpdates(path);
    }

    /**
     * Returns filtered children or <code>null</code>
     * 
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

    protected synchronized void checkIfRestoreComplete() {
        if (fPendingState == null) {
            return;
        }
        
        /**
         * Used to determine when restoration delta has been processed
         */
        class CheckState implements IModelDeltaVisitor {
            private boolean complete = true;

            /*
             * (non-Javadoc)
             * 
             * @see
             * org.eclipse.debug.internal.ui.viewers.provisional.IModelDeltaVisitor
             * #visit(org.eclipse.debug.internal.ui.viewers.provisional.IModelDelta,
             * int)
             */
            public boolean visit(IModelDelta delta, int depth) {
                // Filster out the CONTENT flags from the delta flags, the content
                // flag is only used as a marker indicating that all the sub-elements
                // of a given delta have been retrieved.  
                int flags = (delta.getFlags() & ~IModelDelta.CONTENT);
                
                if (flags != IModelDelta.NO_CHANGE) {
                    IModelDelta parentDelta = delta.getParentDelta();
                    // Remove the delta if :
                    // - The parent delta has no more flags on it (the content flag is removed as well), 
                    // which means that parent element's children have been completely exposed.
                    // - There are no more pending updates for the element.
                    // - If element is a memento, there are no state requests pending.
                    if (parentDelta != null && parentDelta.getFlags() == IModelDelta.NO_CHANGE) {
                        TreePath deltaPath = getViewerTreePath(delta);
                        if ( !areElementUpdatesPending(deltaPath) &&
                             (!(delta.getElement() instanceof IMemento) || !areMementoUpdatesPending(delta)) ) 
                        {
                            removeDelta(delta);
                            return false;
                        }
                    }

                    if (flags != IModelDelta.REVEAL || (delta.getElement() instanceof IMemento)) {
                        complete = false;
                        return false;
                    }
                }
                return true;
            }

            public boolean isComplete() {
                return complete;
            }

            private boolean areElementUpdatesPending(TreePath path) {
                synchronized (fRequestsInProgress) {
                    TreePath parentPath = path.getParentPath();
                    List requests = (List) fWaitingRequests.get(path);
                    if (requests != null) {
                        for (int i = 0; i < requests.size(); i++) {
                            ViewerUpdateMonitor update = (ViewerUpdateMonitor) requests.get(i);
                            if (update instanceof ChildrenUpdate) {
                                return true;
                            }
                        }
                    }
                    requests = (List) fWaitingRequests.get(parentPath);
                    if (requests != null) {
                        for (int i = 0; i < requests.size(); i++) {
                            ViewerUpdateMonitor update = (ViewerUpdateMonitor) requests.get(i);
                            if (update.containsUpdate(path)) {
                                return true;
                            }
                        }
                    }
                    requests = (List) fRequestsInProgress.get(path);
                    if (requests != null) {
                        for (int i = 0; i < requests.size(); i++) {
                            ViewerUpdateMonitor update = (ViewerUpdateMonitor) requests.get(i);
                            if (update instanceof ChildrenUpdate) {
                                return true;
                            }
                        }
                    }
                    requests = (List) fRequestsInProgress.get(parentPath);
                    if (requests != null) {
                        for (int i = 0; i < requests.size(); i++) {
                            ViewerUpdateMonitor update = (ViewerUpdateMonitor) requests.get(i);
                            if (update.getElement().equals(path.getLastSegment())) {
                                return true;
                            }
                        }
                    }
                }
                return false;
            }

            private boolean areMementoUpdatesPending(IModelDelta delta) {
                for (Iterator itr = fCompareRequestsInProgress.keySet().iterator(); itr.hasNext();) {
                    CompareRequestKey key = (CompareRequestKey) itr.next();
                    if (delta.getElement().equals(key.fDelta.getElement())) {
                        return true;
                    }
                }
                return false;
            }

            private void removeDelta(IModelDelta delta) {
                if (DEBUG_STATE_SAVE_RESTORE && DEBUG_TEST_PRESENTATION_ID(getPresentationContext())) {
                    System.out.println("\tRESTORE REMOVED: " + delta.getElement()); //$NON-NLS-1$
                }

                delta.accept(new IModelDeltaVisitor() {
                    public boolean visit(IModelDelta _visitorDelta, int depth) {
                        ModelDelta visitorDelta = (ModelDelta) _visitorDelta;
                        visitorDelta.setElement(ELEMENT_REMOVED);
                        visitorDelta.setFlags(IModelDelta.NO_CHANGE);
                        return true;
                    }
                });

            }
        }

        CheckState state = new CheckState();
        fPendingState.accept(state);
        if (state.isComplete()) {
            // notify restore complete if REVEAL was restored also, otherwise
            // postpone until then. 
            if (fPendingSetTopItem == null) {
                if (DEBUG_STATE_SAVE_RESTORE && DEBUG_TEST_PRESENTATION_ID(getPresentationContext())) {
                    System.out.println("STATE RESTORE COMPELTE: " + fPendingState); //$NON-NLS-1$
                }

                notifyStateUpdate(fPendingState.getElement(), STATE_RESTORE_SEQUENCE_COMPLETE, null);
            }
            
            fPendingState = null;            
        }
    }

    public void addViewerUpdateListener(IViewerUpdateListener listener) {
        fUpdateListeners.add(listener);
    }

    public void removeViewerUpdateListener(IViewerUpdateListener listener) {
        fUpdateListeners.remove(listener);
    }

    /**
     * Notification an update request has started
     * 
     * @param update
     */
    void updateStarted(ViewerUpdateMonitor update) {
        boolean begin = false;
        synchronized (fRequestsInProgress) {
            begin = fRequestsInProgress.isEmpty();
            List requests = (List) fRequestsInProgress.get(update.getSchedulingPath());
            if (requests == null) {
                requests = new ArrayList();
                fRequestsInProgress.put(update.getSchedulingPath(), requests);
            }
            requests.add(update);
        }
        if (begin) {
            if (DEBUG_UPDATE_SEQUENCE && DEBUG_TEST_PRESENTATION_ID(getPresentationContext())) {
                System.out.println("MODEL SEQUENCE BEGINS"); //$NON-NLS-1$
            }
            notifyUpdate(UPDATE_SEQUENCE_BEGINS, null);
        }
        if (DEBUG_UPDATE_SEQUENCE && DEBUG_TEST_PRESENTATION_ID(getPresentationContext())) {
            System.out.println("\tBEGIN - " + update); //$NON-NLS-1$
        }
        notifyUpdate(UPDATE_BEGINS, update);
    }

    /**
     * Notification an update request has completed
     * 
     * @param update
     */
    void updateComplete(final ViewerUpdateMonitor update) {
        notifyUpdate(UPDATE_COMPLETE, update);
        if (DEBUG_UPDATE_SEQUENCE && DEBUG_TEST_PRESENTATION_ID(getPresentationContext())) {
            System.out.println("\tEND - " + update); //$NON-NLS-1$
        }

        new UIJob("Update complete") { //$NON-NLS-1$
            { setSystem(true); }
            
            public IStatus runInUIThread(IProgressMonitor monitor) {
                boolean end = false;
                synchronized (fRequestsInProgress) {
                    List requests = (List) fRequestsInProgress.get(update.getSchedulingPath());
                    if (requests != null) {
                        requests.remove(update);
                        trigger(update);
                        if (requests.isEmpty()) {
                            fRequestsInProgress.remove(update.getSchedulingPath());
                        }
                    }
                    end = fRequestsInProgress.isEmpty();
                }
                if (end) {
                    if (DEBUG_UPDATE_SEQUENCE && DEBUG_TEST_PRESENTATION_ID(getPresentationContext())) {
                        System.out.println("MODEL SEQUENCE ENDS"); //$NON-NLS-1$
                    }
                    notifyUpdate(UPDATE_SEQUENCE_COMPLETE, null);
                }
                return Status.OK_STATUS;
            }
        }.schedule();
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

    public void addStateUpdateListener(IStateUpdateListener listener) {
        fStateUpdateListeners.add(listener);
    }

    public void removeStateUpdateListener(IStateUpdateListener listener) {
        fStateUpdateListeners.remove(listener);
    }

    protected void notifyStateUpdate(final Object input, final int type, final IViewerUpdate update) {
        if (!fStateUpdateListeners.isEmpty()) {
            Object[] listeners = fStateUpdateListeners.getListeners();
            for (int i = 0; i < listeners.length; i++) {
                final IStateUpdateListener listener = (IStateUpdateListener) listeners[i];
                SafeRunner.run(new ISafeRunnable() {
                    public void run() throws Exception {
                        switch (type) {
                        case STATE_SAVE_SEQUENCE_BEGINS:
                            listener.stateSaveUpdatesBegin(input);
                            break;
                        case STATE_SAVE_SEQUENCE_COMPLETE:
                            listener.stateSaveUpdatesComplete(input);
                            break;
                        case STATE_RESTORE_SEQUENCE_BEGINS:
                            listener.stateRestoreUpdatesBegin(input);
                            break;
                        case STATE_RESTORE_SEQUENCE_COMPLETE:
                            listener.stateRestoreUpdatesComplete(input);
                            break;
                        case UPDATE_BEGINS:
                            listener.stateUpdateStarted(input, update);
                            break;
                        case UPDATE_COMPLETE:
                            listener.stateUpdateComplete(input, update);
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
                        ((IRequest) reqIter.next()).cancel();
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
        for (Iterator itr = fCompareRequestsInProgress.keySet().iterator(); itr.hasNext();) {
            CompareRequestKey key = (CompareRequestKey) itr.next();
            if (key.fPath.startsWith(path, null)) {
                ElementCompareRequest compareRequest = (ElementCompareRequest) fCompareRequestsInProgress.get(key);
                compareRequest.cancel();
                itr.remove();
            }
        }
    }

    /**
     * Returns whether this given request should be run, or should wait for
     * parent update to complete.
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

    protected boolean getElementChildrenRealized(TreePath path) {
        synchronized (fRequestsInProgress) {
            List requests = (List) fWaitingRequests.get(path);
            if (requests != null) {
                for (int i = 0; i < requests.size(); i++) {
                    if (requests.get(i) instanceof ChildrenUpdate) {
                        return false;
                    }
                }
            }
            requests = (List) fRequestsInProgress.get(path);
            if (requests != null) {
                int numChildrenUpdateRequests = 0;
                for (int i = 0; i < requests.size(); i++) {
                    if (requests.get(i) instanceof ChildrenUpdate) {
                        if (++numChildrenUpdateRequests > 1) {
                            return false;
                        }
                    }
                }
            }
        }

        return getViewer().getElementChildrenRealized(path);
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
        if (next != null) {
	        waiting.remove(next);
	        if (waiting.isEmpty()) {
	            fWaitingRequests.remove(key);
	        }
	        next.start();
        }
    }

    /**
     * Registers the given listener for model delta notification.
     * 
     * @param listener
     *            model delta listener
     */
    public void addModelChangedListener(IModelChangedListener listener) {
        fModelListeners.add(listener);
    }

    /**
     * Unregisters the given listener from model delta notification.
     * 
     * @param listener
     *            model delta listener
     */
    public void removeModelChangedListener(IModelChangedListener listener) {
        fModelListeners.remove(listener);
    }

    /**
     * Returns the element corresponding to the given tree path.
     * 
     * @param path
     *            tree path
     * @return model element
     */
    protected Object getElement(TreePath path) {
        if (path.getSegmentCount() > 0) {
            return path.getLastSegment();
        }
        return getViewer().getInput();
    }

    /**
     * Reschedule any children updates in progress for the given parent that
     * have a start index greater than the given index. An element has been
     * removed at this index, invalidating updates in progress.
     * 
     * @param parentPath
     *            view tree path to parent element
     * @param modelIndex
     *            index at which an element was removed
     */
    protected void rescheduleUpdates(TreePath parentPath, int modelIndex) {
        synchronized (fRequestsInProgress) {
            List requests = (List) fRequestsInProgress.get(parentPath);
            List reCreate = null;
            if (requests != null) {
                Iterator iterator = requests.iterator();
                while (iterator.hasNext()) {
                    IViewerUpdate update = (IViewerUpdate) iterator.next();
                    if (update instanceof IChildrenUpdate) {
                        IChildrenUpdate childrenUpdate = (IChildrenUpdate) update;
                        if (childrenUpdate.getOffset() > modelIndex) {
                            childrenUpdate.cancel();
                            if (reCreate == null) {
                                reCreate = new ArrayList();
                            }
                            reCreate.add(childrenUpdate);
                            if (DEBUG_CONTENT_PROVIDER && DEBUG_TEST_PRESENTATION_ID(getPresentationContext())) {
                                System.out.println("canceled update in progress handling REMOVE: " + childrenUpdate); //$NON-NLS-1$
                            }
                        }
                    }
                }
            }
            requests = (List) fWaitingRequests.get(parentPath);
            if (requests != null) {
                Iterator iterator = requests.iterator();
                while (iterator.hasNext()) {
                    IViewerUpdate update = (IViewerUpdate) iterator.next();
                    if (update instanceof IChildrenUpdate) {
                        IChildrenUpdate childrenUpdate = (IChildrenUpdate) update;
                        if (childrenUpdate.getOffset() > modelIndex) {
                            ((ChildrenUpdate) childrenUpdate).setOffset(childrenUpdate.getOffset() - 1);
                            if (DEBUG_CONTENT_PROVIDER && DEBUG_TEST_PRESENTATION_ID(getPresentationContext())) {
                                System.out.println("modified waiting update handling REMOVE: " + childrenUpdate); //$NON-NLS-1$
                            }
                        }
                    }
                }
            }
            // re-schedule canceled updates at new position.
            // have to do this last else the requests would be waiting and
            // get modified.
            if (reCreate != null) {
                Iterator iterator = reCreate.iterator();
                while (iterator.hasNext()) {
                    IChildrenUpdate childrenUpdate = (IChildrenUpdate) iterator.next();
                    int start = childrenUpdate.getOffset() - 1;
                    int end = start + childrenUpdate.getLength();
                    for (int i = start; i < end; i++) {
                        ((TreeModelContentProvider) this).doUpdateElement(parentPath, i);
                    }
                }
            }
        }
    }
}
