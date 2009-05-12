/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.viewers.update;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ModelDelta;
import org.eclipse.debug.internal.ui.viewers.provisional.AbstractModelProxy;

/**
 * @since 3.2
 */
public class ThreadEventHandler extends DebugEventHandler {
	
	/**
	 * Queue of suspended threads to choose from when needing
	 * to select a thread when another is resumed. Threads
	 * are added in the order they suspend.
	 */
	private Set fThreadQueue = new LinkedHashSet();
	
	/** 
	 * Map of previous TOS per thread
	 */
	private Map fLastTopFrame = new HashMap();
	/**
	 * Constructs and event handler for a threads in the given viewer.
	 * 
	 * @param viewer
	 */
	public ThreadEventHandler(AbstractModelProxy proxy) {
		super(proxy);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.update.DebugEventHandler#dispose()
	 */
	public synchronized void dispose() {
		fLastTopFrame.clear();
		fThreadQueue.clear();
		super.dispose();
	}

	protected void handleSuspend(DebugEvent event) {
        IThread thread = (IThread) event.getSource();
		if (event.isEvaluation()) {
        	ModelDelta delta = buildRootDelta();
    		ModelDelta node = addPathToThread(delta, thread);
    		node = node.addNode(thread, IModelDelta.NO_CHANGE);
			try {
				IStackFrame frame = thread.getTopStackFrame();
                if (frame != null) {
                	int flag = IModelDelta.NO_CHANGE;
                	if (event.getDetail() == DebugEvent.EVALUATION) {
                		// explicit evaluations can change content
                		flag = flag | IModelDelta.CONTENT;
                	} else if (event.getDetail() == DebugEvent.EVALUATION_IMPLICIT) {
                		// implicit evaluations can change state
                		flag = flag | IModelDelta.STATE;
                	}
                    node.addNode(frame, flag);
                    fireDelta(delta);
                }
			} catch (DebugException e) {
			}
        } else {
        	queueSuspendedThread(event);
            int extras = IModelDelta.STATE;
            switch (event.getDetail()) {
            case DebugEvent.BREAKPOINT:
            	// on breakpoint also position thread to be top element
            	extras = IModelDelta.EXPAND | IModelDelta.REVEAL;
            	break;
            case DebugEvent.CLIENT_REQUEST:
            	extras = IModelDelta.EXPAND;
            	break;
            }
        	fireDeltaUpdatingSelectedFrame(thread, IModelDelta.NO_CHANGE | extras, event);
        }
	}
	
	private boolean isEqual(Object o1, Object o2) {
		if (o1 == o2) {
			return true;
		}
		if (o1 == null) {
			return false;
		}
		return o1.equals(o2);
	}

	protected void handleResume(DebugEvent event) {
		IThread thread = removeSuspendedThread(event);
		fireDeltaAndClearTopFrame(thread, IModelDelta.STATE | IModelDelta.CONTENT | IModelDelta.SELECT);
		thread = getNextSuspendedThread();
		if (thread != null) {
			fireDeltaUpdatingSelectedFrame(thread, IModelDelta.NO_CHANGE | IModelDelta.REVEAL, event);
		}
	}

	protected void handleCreate(DebugEvent event) {
		fireDeltaAndClearTopFrame((IThread) event.getSource(), IModelDelta.ADDED | IModelDelta.STATE);
	}

	protected void handleTerminate(DebugEvent event) {
		IThread thread = (IThread) event.getSource();
		IDebugTarget target = thread.getDebugTarget();
		// ignore thread termination if target is terminated/disconnected
		if (!(target.isTerminated() || target.isDisconnected())) {
			fireDeltaAndClearTopFrame(thread, IModelDelta.REMOVED);
		}
	}

	protected void handleChange(DebugEvent event) {
		if (event.getDetail() == DebugEvent.STATE) {
			fireDeltaUpdatingThread((IThread) event.getSource(), IModelDelta.STATE);
		} else {
			fireDeltaUpdatingThread((IThread) event.getSource(), IModelDelta.CONTENT);
		}
	}

	protected void handleLateSuspend(DebugEvent suspend, DebugEvent resume) {
		IThread thread = queueSuspendedThread(suspend);
		if (suspend.isEvaluation() && suspend.getDetail() == DebugEvent.EVALUATION_IMPLICIT) {
			// late implicit evaluation - update thread and frame
        	ModelDelta delta = buildRootDelta();
    		ModelDelta node = addPathToThread(delta, thread);
    		node = node.addNode(thread, IModelDelta.STATE);
			try {
				IStackFrame frame = thread.getTopStackFrame();
                if (frame != null) {
                    node.addNode(frame, IModelDelta.STATE);
                    fireDelta(delta);
                }
			} catch (DebugException e) {
			}
        } else {	
        	fireDeltaUpdatingSelectedFrame(thread, IModelDelta.STATE | IModelDelta.EXPAND, suspend);
        }
	}

	protected void handleSuspendTimeout(DebugEvent event) {
		IThread thread = removeSuspendedThread(event);
		// don't collapse thread when waiting for long step or evaluation to complete
		fireDeltaUpdatingThread(thread, IModelDelta.STATE);
	}
	
	protected ModelDelta buildRootDelta() {
		return new ModelDelta(getLaunchManager(), IModelDelta.NO_CHANGE);
	}

	/**
	 * Returns the launch manager.
	 * 
	 * @return the launch manager
	 */
	protected ILaunchManager getLaunchManager() {
		return DebugPlugin.getDefault().getLaunchManager();
	}
	
	/**
	 * Adds nodes into the delta up to but not including the given thread.
	 * 
	 * @param delta root delta for the view (includes viewer input)
	 * @param thread thread for which path is requested
	 * @return
	 */
	protected ModelDelta addPathToThread(ModelDelta delta, IThread thread) {
		ILaunch launch = thread.getLaunch();
		Object[] children = launch.getChildren();
		delta = delta.addNode(launch, indexOf(getLaunchManager().getLaunches(), launch), IModelDelta.NO_CHANGE, children.length);
		IDebugTarget debugTarget = thread.getDebugTarget();
		int numThreads = -1;
		try {
			numThreads = debugTarget.getThreads().length;
		} catch (DebugException e) {
		}
		return delta.addNode(debugTarget, indexOf(children, debugTarget), IModelDelta.NO_CHANGE, numThreads);
	}

	private void fireDeltaAndClearTopFrame(IThread thread, int flags) {
		ModelDelta delta = buildRootDelta();
		ModelDelta node = addPathToThread(delta, thread);
		node.addNode(thread, indexOf(thread), flags);
		synchronized (this) {
			fLastTopFrame.remove(thread);
		}
		fireDelta(delta);
	}
	
	private void fireDeltaUpdatingSelectedFrame(IThread thread, int flags, DebugEvent event) {
		ModelDelta delta = buildRootDelta();
		ModelDelta node = addPathToThread(delta, thread);
    	IStackFrame prev = null;
    	synchronized (this) {
    		 prev = (IStackFrame) fLastTopFrame.get(thread);
		}
    	IStackFrame frame = null;
		try {
			Object frameToSelect = event.getData();
			if (frameToSelect == null || !(frameToSelect instanceof IStackFrame)) {
				frame = thread.getTopStackFrame();
			} else {
				frame = (IStackFrame)frameToSelect;
			}
		} catch (DebugException e) {
		}
		int threadIndex = indexOf(thread);
		int childCount = childCount(thread);
    	if (isEqual(frame, prev)) {
    		if (frame == null) {
    			if (thread.isSuspended()) {
	    			// no frames, but suspended - update & select
	    			node = node.addNode(thread, threadIndex, flags | IModelDelta.STATE | IModelDelta.SELECT, childCount);
    			}
    		} else {
    			node = node.addNode(thread, threadIndex, flags, childCount);
    		}
    	} else {
    		if (event.getDetail() == DebugEvent.STEP_END) {
	    		if (prev == null) {
	    			// see bug 166602 - expand the thread if this is a step end with no previous top frame
	    			flags = flags | IModelDelta.EXPAND;
	    		} else if (frame == null) {
	    			// there was a previous frame and current is null on a step: transient state
	    			return;
	    		}
    		}
			node = node.addNode(thread, threadIndex, flags | IModelDelta.CONTENT, childCount);
    	}
    	if (frame != null) {
            node.addNode(frame, indexOf(frame), IModelDelta.STATE | IModelDelta.SELECT, childCount(frame));
        }
    	synchronized (this) {
    		if (!isDisposed()) {
    			fLastTopFrame.put(thread, frame);
    		}
		}
    	fireDelta(delta);
	}
	
	/**
	 * Returns the index of the given thread, relative to its parent in the view.
	 * 
	 * @param thread thread
	 * @return index of the thread, relative to its parent
	 */
	protected int indexOf(IThread thread) {
		try {
			return indexOf(thread.getDebugTarget().getThreads(), thread);
		} catch (DebugException e) {
		}
		return -1;
	}
	
	/**
	 * Returns the index of the given frame, relative to its parent in the view.
	 * 
	 * @param frame stack frame
	 * @return index of the frame, relative to its thread
	 */
	protected int indexOf(IStackFrame frame) {
		try {
			return indexOf(frame.getThread().getStackFrames(), frame);
		} catch (DebugException e) {
			return -1;
		}
	}
	
	/**
	 * Returns the number of children the given thread has in the view.
	 * 
	 * @param thread thread
	 * @return number of children
	 */
	protected int childCount(IThread thread) {
		try {
			return thread.getStackFrames().length;
		} catch (DebugException e) {
		}
		return -1;
	}
	
	/**
	 * Returns the number of children the given frame has in the view.
	 * 
	 * @param frame frame
	 * @return child count
	 */
	protected int childCount(IStackFrame frame) {
		return 0;
	}
	
	private void fireDeltaUpdatingThread(IThread thread, int flags) {
		ModelDelta delta = buildRootDelta();
		ModelDelta node = addPathToThread(delta, thread);
	    node = node.addNode(thread, flags);
    	fireDelta(delta);
	}	
	
	protected boolean handlesEvent(DebugEvent event) {
		return event.getSource() instanceof IThread;
	}
	
	protected synchronized IThread queueSuspendedThread(DebugEvent event) {
		IThread thread = (IThread) event.getSource();
		if (!isDisposed()) {
			fThreadQueue.add(thread);
		}
		return thread;
	}
	
	protected synchronized IThread removeSuspendedThread(DebugEvent event) {
		IThread thread = (IThread)event.getSource();
		fThreadQueue.remove(thread);
		return thread;
	}
	
	protected synchronized IThread queueSuspendedThread(IThread thread) {
		if (!isDisposed()) {
			fThreadQueue.add(thread);
		}
		return thread;
	}	
	
	protected synchronized void removeQueuedThread(IThread thread) {
		fThreadQueue.remove(thread);
	}
	
	protected synchronized IThread getNextSuspendedThread() {
		if (!fThreadQueue.isEmpty()) {
			return (IThread) fThreadQueue.iterator().next();
		}
		return null;
	}

}
