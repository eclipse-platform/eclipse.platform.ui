package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.HashMap;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchListener;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.ISuspendResume;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.viewers.IBasicPropertyConstants;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * Provides the contents for a debug mode of the Launches viewer.
 */
public class DebugContentProvider extends BasicContentProvider implements IDebugEventListener, ILaunchListener, ITreeContentProvider {
	
	/**
	 * Stack frame counts keyed by thread.  Used to optimize thread refreshing.
	 */
	private HashMap fStackFrameCountByThread = new HashMap(5);
	
	public DebugContentProvider() {
		DebugPlugin plugin= DebugPlugin.getDefault();
		plugin.addDebugEventListener(this);
		plugin.getLaunchManager().addLaunchListener(this);
	}

	/**
	 * @see BasicContentProvider#doGetChildren(Object)
	 */
	protected Object[] doGetChildren(Object parent) {
		try {
			if (parent instanceof IDebugTarget) {
				return ((IDebugTarget)parent).getThreads();
			}
			if (parent instanceof IThread) {
				return ((IThread)parent).getStackFrames();
			}			
		} catch (DebugException e) {
			DebugUIPlugin.logError(e);
		}
		if (parent instanceof ILaunch) {
			return ((ILaunch)parent).getChildren();
		}
		if (parent instanceof ILaunchManager) {
			return ((ILaunchManager) parent).getLaunches();
		}
		return new Object[0];
	}

	/**
	 * Returns the <code>ILaunch</code>es for the <code>ILaunchManager</code>
	 * 
	 * @see IStructuredContentProvider#getElements(Object)
	 */
	public Object[] getElements(Object element) {
		if (element instanceof ILaunchManager) {
			return ((ILaunchManager) element).getLaunches();
		}
		return new Object[0];
	}

	/**
	 * @see ITreeContentProvider#getParent(Object)
	 */
	public Object getParent(Object item) {
		if (item instanceof IStackFrame) {
			return ((IStackFrame)item).getThread();
		}
		if (item instanceof IThread) {
			return ((IThread)item).getDebugTarget();
		}
		if (item instanceof IDebugTarget) {
			return ((IDebugElement)item).getLaunch();
		}
		if (item instanceof IProcess) {
			return ((IProcess)item).getLaunch();
		}
		if (item instanceof ILaunch) {
			return DebugPlugin.getDefault().getLaunchManager();
		}
		return null;
	}

	/**
	 * @see BasicContentProvider#doHandleDebug(Event)
	 */
	public void doHandleDebugEvent(DebugEvent event) {
		Object element= event.getSource();
		if (element instanceof IDebugElement && ((IDebugElement) element).getElementType() == IDebugElement.VARIABLE) {
			// the debug view does not show variables
			return;
		}
		switch (event.getKind()) {
			case DebugEvent.CREATE :
				insert(element);
				break;
			case DebugEvent.TERMINATE :
				if (element instanceof IThread) {
					clearSourceSelection((IThread)element);
					fStackFrameCountByThread.remove(element);
					remove(element);
				} else {
					clearSourceSelection(null);
					Object parent = getParent(element);
					refresh(parent);
				}
				updateButtons();
				break;
			case DebugEvent.RESUME :
				doHandleResumeEvent(event, element);
				break;
			case DebugEvent.SUSPEND :
				doHandleSuspendEvent(element);
				break;
			case DebugEvent.CHANGE :
				refresh(element);
				updateButtons();
				break;
		}
	}
		
	protected void doHandleResumeEvent(DebugEvent event, Object element) {
		if (element instanceof ISuspendResume) {
			if (((ISuspendResume)element).isSuspended()) {
				return;
			}
		}		
		clearSourceSelection(null);
		if (event.getDetail() != DebugEvent.STEP_START) {
			refresh(element);
			if (element instanceof IThread) {
				//select and reveal will update buttons
				//via selection changed callback
				selectAndReveal(element);
				resetStackFrameCount((IThread)element);
				return;
			}
		} else {
			IThread thread = null;
			if (element instanceof IThread) {
				thread = (IThread) element;
			} else if (element instanceof IStackFrame) {
				thread = ((IStackFrame)element).getThread();
			}
			if (thread != null) {								
				((LaunchesViewer)fViewer).updateStackFrameIcons(thread);
				resetStackFrameCount(thread);
			}
		}
		labelChanged(element);
		updateButtons();
	}
	
	protected void resetStackFrameCount(IThread thread) {
		fStackFrameCountByThread.put(thread, new Integer(0));
	}

	protected void doHandleSuspendEvent(Object element) {
		if (element instanceof IThread) {
			doHandleSuspendThreadEvent((IThread)element);
		}
		updateButtons();
	}
	
	// This method exists to provide some optimization for refreshing suspended
	// threads.  If the number of stack frames has changed from the last suspend, 
	// we do a full refresh on the thread.  Otherwise, we only do a surface-level
	// refresh on the thread, then refresh each of the children, which eliminates
	// flicker when do quick stepping (e.g., holding down the F6 key) within a method.
	protected void doHandleSuspendThreadEvent(IThread thread) {
		// if the thread has already resumed, do nothing
		if (!thread.isSuspended()) {
			return;
		}
		
		// Get the current stack frames from the thread
		IStackFrame[] stackFrames = null;
		try {
			stackFrames = thread.getStackFrames();
		} catch (DebugException de) {
			DebugUIPlugin.logError(de);
			return;
		}
		
		int currentStackFrameCount = stackFrames.length;
		
		// Retrieve the old and current counts of stack frames for this thread
		int oldStackFrameCount = 0;
		Integer oldStackFrameCountObject = (Integer)fStackFrameCountByThread.get(thread);
		if (oldStackFrameCountObject != null) {
			oldStackFrameCount = oldStackFrameCountObject.intValue();
		}
		
		// Compare old & current stack frame counts.  We need to refresh the thread
		// parent if there are fewer stack frame children
		boolean refreshNeeded = true;
		if (currentStackFrameCount == oldStackFrameCount) {
			refreshNeeded = false;
		}
		
		// Auto-expand the thread.  If we are also refreshing the thread,
		// then we don't need to worry about any children, since refreshing
		// the parent handles this
		((LaunchesViewer)fViewer).autoExpand(thread, refreshNeeded, refreshNeeded);
		if (refreshNeeded) {
			return;
		} else {
			labelChanged(thread);
		}
		
		// Auto-expand each stack frame child.  This has the effect of adding 
		// any new stack frames, and updating any existing stack frames
		if ((stackFrames != null) && (currentStackFrameCount > 0)) {
			for (int i = currentStackFrameCount - 1; i > 0; i--) {
				((LaunchesViewer)fViewer).autoExpand(stackFrames[i], true, false);				
			}
			// Treat the first stack frame differently, since we want to select it
			((LaunchesViewer)fViewer).autoExpand(stackFrames[0], true, true);				
		}	
		
		// Update the stack frame count for the thread
		oldStackFrameCountObject = new Integer(currentStackFrameCount);
		fStackFrameCountByThread.put(thread, oldStackFrameCountObject);
	}
	
		
	/**
	 * Helper method for inserting the given element - must be called in UI thread
	 */
	protected void insert(Object element) {
		final Object parent= getParent(element);
		// a parent can be null for a debug target or process that has not yet been associated
		// with a launch
		if (parent != null) {
			((LaunchesViewer) fViewer).add(parent, element);
		}
	}

	/**
	 * Helper method to remove the given element - must be called in UI thread
	 */
	private void remove(Object element) {
		((LaunchesViewer) fViewer).remove(element);
	}

	/**
	 * Helper method to update the label of the given element - must be called in UI thread
	 */
	protected void labelChanged(Object element) {
		((LaunchesViewer) fViewer).update(element, new String[] {IBasicPropertyConstants.P_TEXT});
	}

	/**
	 * Helper method to update the buttons of the viewer - must be called in UI thread
	 */
	protected void updateButtons() {
		((LaunchesViewer)fViewer).updateButtons();
	}

	/**
	 * Helper method to update the selection of the viewer - must be called in UI thread
	 */
	protected void updateMarkerForSelection() {
		((LaunchesViewer)fViewer).updateMarkerForSelection();
	}

	/**
	 * Helper method to select and reveal the given element - must be called in UI thread
	 */
	protected void selectAndReveal(Object element) {
		((LaunchesViewer) fViewer).setSelection(new StructuredSelection(element), true);
	}

	/**
	 * @see ITreeContentProvider#hasChildren(Object)
	 */
	public boolean hasChildren(Object item) {
		if (item instanceof IStackFrame) {
			return false;
			// No children shown for IStackFrames in this view (see the variables view instead).
		} else
			if (item instanceof IDebugElement) {
				return doGetChildren(item).length > 0;
			} else
				if (item instanceof ILaunch) {
					return true;
				} else
					if (item instanceof ILaunchManager) {
						return ((ILaunchManager) item).getLaunches().length > 0;
					}
		return false;
	}

	/**
	 * @see ILaunchListener#launchRegistered(ILaunch)
	 */
	public void launchDeregistered(final ILaunch launch) {
		Runnable r= new Runnable() {
			public void run() {
				if (!isDisposed()) {
					remove(launch);
					ILaunchManager lm= DebugPlugin.getDefault().getLaunchManager();
					IDebugTarget[] targets= lm.getDebugTargets();
					if (targets.length > 0) {
						IDebugTarget target= targets[targets.length - 1];
						try {
							IThread[] threads= target.getThreads();
							for (int i=0; i < threads.length; i++) {
								if (threads[i].isSuspended()) {
									((LaunchesViewer)fViewer).autoExpand(threads[i], false, true);
									return;
								}
							}						
						} catch (DebugException de) {
							DebugUIPlugin.logError(de);
						}
						
						((LaunchesViewer)fViewer).autoExpand(target.getLaunch(), false, true);
					}
					updateButtons();
				}
			}
		};

		asyncExec(r);
	}

	/**
	 * @see ILaunchListener
	 */
	public void launchRegistered(final ILaunch newLaunch) {
		Runnable r= new Runnable() {
			public void run() {
				if (!isDisposed() && newLaunch.getLaunchMode().equals(ILaunchManager.DEBUG_MODE)) {			
					if (DebugUIPlugin.getDefault().getPreferenceStore().getBoolean(IDebugUIConstants.PREF_AUTO_REMOVE_OLD_LAUNCHES)) {
						ILaunchManager lManager= DebugPlugin.getDefault().getLaunchManager();
						Object[] launches= lManager.getLaunches();
						for (int i= 0; i < launches.length; i++) {
							ILaunch launch= (ILaunch)launches[i];
							if (launch != newLaunch && launch.isTerminated()) {
								lManager.deregisterLaunch(launch);
							}
						}
					}
					insert(newLaunch);
					((LaunchesViewer)fViewer).autoExpand(newLaunch, false, true);
				}
			}
		};

		syncExec(r);
	}

	/**
	 * Unregisters this content provider from the debug model so that
	 * this object can be garbage-collected.
	 * 
	 * @see IContentProvider#dispose()
	 */
	public void dispose() {
		super.dispose();
		DebugPlugin plugin= DebugPlugin.getDefault();
		plugin.removeDebugEventListener(this);
		plugin.getLaunchManager().removeLaunchListener(this);
	}

	/**
	 * Clear the selection in the editor - must be called in UI thread
	 */
	private void clearSourceSelection(IThread thread) {
		if (fViewer != null) {
			if (thread != null) {
				IStructuredSelection selection= (IStructuredSelection)fViewer.getSelection();
				Object element= selection.getFirstElement();
				if (element instanceof IStackFrame) {
					IStackFrame stackFrame = (IStackFrame) element;
					if (!stackFrame.getThread().equals(thread)) {
						//do not clear the source selection
						//a thread has terminated that is not the
						//parent of the currently selected stack frame
						return;
					}
				}
			}
		
			((LaunchesViewer)fViewer).clearSourceSelection();
		}
	}
}

