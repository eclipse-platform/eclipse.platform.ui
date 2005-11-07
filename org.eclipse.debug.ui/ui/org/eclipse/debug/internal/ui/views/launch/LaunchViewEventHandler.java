/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.launch;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.ILaunchesListener2;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.views.AbstractDebugEventHandler;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * Handles debug events, updating the launch view and viewer.
 */
public class LaunchViewEventHandler extends AbstractDebugEventHandler implements ILaunchesListener2 {
	/**
	 * The timer used to time step and evaluation events. The timer allows
	 * the UI to not refresh during fast evaluations and steps.
	 */
	private ThreadTimer fThreadTimer= new ThreadTimer();
	
	/**
	 * Cache of the last top stack frame
	 */
	private IStackFrame fLastStackFrame = null;
	
	/**
	 * Constructs an event handler for the given launch view.
	 * 
	 * @param view launch view
	 */
	public LaunchViewEventHandler(LaunchView view) {
		super(view);
		DebugPlugin plugin= DebugPlugin.getDefault();
		plugin.getLaunchManager().addLaunchListener(this);
	}
	

    /* (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.views.AbstractDebugEventHandler#filterEvents(org.eclipse.debug.core.DebugEvent[])
     */
    protected DebugEvent[] filterEvents(DebugEvent[] events) {
        if (events.length > 0) {
            DebugEvent event = events[0];
            Object source = event.getSource();
            ILaunch launch = null;
            if (source instanceof IDebugElement) {
                launch = ((IDebugElement)source).getLaunch();
            } else if (source instanceof IProcess) {
                launch = ((IProcess)source).getLaunch();
            }
            // we only need to consider the first event, as all events in an event set come
            // from the same program
            if (launch != null && DebugPlugin.getDefault().getLaunchManager().isRegistered(launch)) {
                return events;
            }
            return EMPTY_EVENT_SET;
        }
        return events;
    }
	
	/**
	 * Returns the parent for the given element.
	 * 
	 * @param element
	 * @return parent
	 */
	private Object getParent(Object element) {
		return ((ITreeContentProvider)getTreeViewer().getContentProvider()).getParent(element);
	}
    
	/**
	 * @see AbstractDebugEventHandler#doHandleDebugEvents(DebugEvent[])
	 */
	protected void doHandleDebugEvents(DebugEvent[] events, Object data) {
		fThreadTimer.handleDebugEvents(events);
		Object suspendee = null;
		for (int i = 0; i < events.length; i++) {
			DebugEvent event = events[i];
			Object source= event.getSource();
			switch (event.getKind()) {
				case DebugEvent.CREATE :
					if (source instanceof IThread) {
						insert(source);
					} else {
						Object parent = getParent(source);
						if (parent != null) {
							refresh(parent);
						}
						if (source instanceof IDebugTarget | source instanceof IProcess) {
							getLaunchView().autoExpand(source, true);
						}
					}
					break;
				case DebugEvent.TERMINATE :
					clearSourceSelection(source);
					if (source instanceof IThread) {
						fThreadTimer.getTimedOutThreads().remove(source);
						remove(source);
					} else {
					    Object parent = getParent(source);
						if (parent != null) {
						    refresh(parent);
						}
					}
					break;
				case DebugEvent.RESUME :
					doHandleResumeEvent(event, source, data);
					break;
				case DebugEvent.SUSPEND :
					if (suspendee == null || !suspendee.equals(source)) {
						doHandleSuspendEvent(source, event, data);
						suspendee = source;
					}
					break;
				case DebugEvent.CHANGE :
                    Object element = null;
                    IStructuredSelection selection = getLaunchViewer().getDeferredSelection();
                    if (selection == null) {
                        selection = (IStructuredSelection) getLaunchViewer().getSelection();
                    } 
                    
                    element = selection.getFirstElement();
                    IStackFrame lastFrame = null;
                    if (element instanceof IStackFrame) {
                        lastFrame = (IStackFrame) element;
                    }
					if (source instanceof IStackFrame) {
						if (source.equals(lastFrame)) {
							getLaunchView().setStackFrame(null);
							getLaunchView().autoExpand(lastFrame, true);
						}
					}
					if (event.getDetail() == DebugEvent.STATE) {
						labelChanged(source);
					} else {
						//structural change
						refresh(source);
					}
					if (lastFrame != null && source instanceof IThread) {
					    if (lastFrame.getThread().equals(source)) {
					        getLaunchView().autoExpand(lastFrame, true);
					    }
					}
					break;
			}
		}
	}
	
	/**
	 * Handles the given resume event with the given source.
	 */
	protected void doHandleResumeEvent(DebugEvent event, Object source, Object data) {
		if (!event.isEvaluation()) {
			clearSourceSelection(source);
		}
		if (event.isEvaluation() || event.isStepStart()) {
			// Do not update for step starts and evaluation
			// starts immediately. Instead, start the timer.
			IThread thread= getThread(source);
			if (thread != null) {
				fThreadTimer.startTimer(thread);
			}
			return;
		}
		refresh(source);
		if (source instanceof IThread) {
		    if (data instanceof IStackFrame) {
				selectAndReveal(data);
				return;
			}
            selectAndReveal(source);
		}
	}
	
	/**
	 * Updates the stack frame icons for a running thread.
	 * This is useful for the case where a thread is resumed
	 * temporarily  but the view should keep the stack frame 
	 * visible (for example, step start or evaluation start).
	 */
	protected void updateRunningThread(IThread thread) {
		labelChanged(thread);
		getLaunchViewer().updateStackFrameImages(thread);
		clearSourceSelection(thread);
	}

	protected void doHandleSuspendEvent(Object element, DebugEvent event, Object data) {
		IThread thread= getThread(element);
		if (thread != null) {
			fThreadTimer.stopTimer(thread);
		}
		
		boolean wasTimedOut= fThreadTimer.getTimedOutThreads().remove(thread);
		if (event.isEvaluation() && ((event.getDetail() & DebugEvent.EVALUATION_IMPLICIT) != 0)) {
			if (thread != null && !wasTimedOut) {
				// No refresh required for implicit evaluations that complete on time
				return;
			}
		}
		if (element instanceof IThread) {
			doHandleSuspendThreadEvent((IThread)element, event, wasTimedOut, data);
			return;
		}
		refresh(element);
	}
	
	/**
	 * Updates the given thread for the given suspend event.
	 */
	protected void doHandleSuspendThreadEvent(IThread thread, DebugEvent event, boolean wasTimedOut, Object data) {
		// if the thread has already resumed, do nothing
		if (!thread.isSuspended() || !isAvailable()) {
			return;
		}

		// do not update source selection for evaluation events
		boolean evaluationEvent = event.isEvaluation();
		
		// get the top frame
		IStackFrame frame = null;
		if (data instanceof IStackFrame) {
		    frame = (IStackFrame) data;
		}
	    
		// if the top frame is the same, only update labels and images, and re-select
		// the frame to display source
		if (frame != null && frame.equals(fLastStackFrame)) {
			if (wasTimedOut) {
				getLaunchViewer().updateStackFrameImages(thread);
			}
			getLaunchViewer().update(new Object[] {thread, frame}, null);
			if (!evaluationEvent) {
			    getLaunchViewer().deferExpansion(thread);
				getLaunchViewer().deferSelection(new StructuredSelection(frame));
			} else if (wasTimedOut) {
				getLaunchView().showEditorForCurrentSelection();
			}
			return;
		}
		
		if (frame == null) {
			// suspend event, but no frames in the thead
			fLastStackFrame = null;			
			refresh(thread);
			getLaunchView().autoExpand(thread, !evaluationEvent);
		} else {
		    fLastStackFrame = frame;
			// Auto-expand the thread. Only select the thread if this wasn't the end
			// of an evaluation
		    refresh(thread);
			getLaunchView().autoExpand(frame, !evaluationEvent);
		}
	}
	
	/**
	 * @see AbstractDebugEventHandler#updateForDebugEvents(DebugEvent[])
	 */
	protected void updateForDebugEvents(DebugEvent[] events, Object data) {
		super.updateForDebugEvents(events, data);
		if (isViewVisible()) {
			return;
		}
		doHandleDebugEvents(events, data);
	}
	
	/**
	 * De-registers this event handler from the debug model.
	 */
	public void dispose() {
		super.dispose();
		fThreadTimer.stop();
		DebugPlugin plugin= DebugPlugin.getDefault();
		plugin.getLaunchManager().removeLaunchListener(this);
	}

	/**
	 * Clear the selection in the editor - must be called in UI thread
	 */
	private void clearSourceSelection(Object source) {
		if (getViewer() != null) {
			getLaunchView().clearSourceSelection(source);
		}
	}	

	/**
	 * Returns this event handler's launch viewer
	 * 
	 * @return launch viewer
	 */
	protected LaunchViewer getLaunchViewer() {
		return (LaunchViewer)getViewer();
	}
	
	/**
	 * Returns this event handler's launch view
	 * 
	 * @return launch view
	 */
	protected LaunchView getLaunchView() {
		return (LaunchView)getView();
	}		
	
	private IThread getThread(Object element) {
		IThread thread = null;
		if (element instanceof IThread) {
			thread = (IThread) element;
		} else if (element instanceof IStackFrame) {
			thread = ((IStackFrame)element).getThread();
		}
		return thread;
	}
	
	class ThreadTimer {
		
		private Thread fThread;
		/**
		 * The time allotted before a thread will be updated
		 */
		private long TIMEOUT= 500;
		/**
		 * Time in milliseconds that the thread timer started
		 * running with no timers.
		 */
		private long timeEmpty= 0;
		/**
		 * The maximum time in milliseconds that the thread
		 * will continue running with no timers.
		 */
		private long MAX_TIME_EMPTY= 3000;
		private boolean fStopped= false;
		private Object fLock= new Object();
		
		/**
		 * Maps threads that are currently performing being timed
		 * to the allowed time by which they must finish. If this
		 * limit expires before the timer is stopped, the thread will
		 * be refreshed.
		 */
		HashMap fStopTimes= new HashMap();
		/**
		 * Collection of threads whose timers have expired.
		 */
		HashSet fTimedOutThreads= new HashSet();
		
		public Set getTimedOutThreads() {
			return fTimedOutThreads;
		}
		
		/**
		 * Handle debug events dispatched from launch view event handler.
		 * If there are no running targets, stop this timer.
		 */
		public void handleDebugEvents(DebugEvent[] events) {
			if (fStopped) {
				return;
			}
			DebugEvent event;
			for (int i= 0, numEvents= events.length; i < numEvents; i++) {
				event= events[i];
				if (event.getKind() == DebugEvent.TERMINATE && event.getSource() instanceof IDebugTarget) {
					ILaunch[] launches= DebugPlugin.getDefault().getLaunchManager().getLaunches();
					// If there are no more active DebugTargets, stop the thread.
					for (int j= 0; j < launches.length; j++) {
						IDebugTarget[] targets= launches[j].getDebugTargets();
						for (int k = 0; k < targets.length; k++) {
							IDebugTarget target = targets[k];
							if (target != null && !target.isDisconnected() && !target.isTerminated()) {
								return;
							}
						}
					}
					// To get here, there must be no running DebugTargets
					stop();
					return;
				}
			}
		}
			
		public void startTimer(IThread thread) {
			synchronized (fLock) {
				fStopTimes.put(thread, new Long(System.currentTimeMillis() + TIMEOUT));
				if (fThread == null) {
					startThread();
				}
			}
		}
		
		public void stop() {
			synchronized (fLock) {
				fStopped= true;
				fThread= null;
				fStopTimes.clear();
			}
		}
		
		public void stopTimer(IThread thread) {
			synchronized (fLock) {
				fStopTimes.remove(thread);
			}
		}
		
		private void startThread() {
			fThread= new Thread(new Runnable() {
				public void run() {
					fStopped= false;
					while (!fStopped) {
						checkTimers();
					}
					
				}
			}, "Thread timer"); //$NON-NLS-1$
			fThread.setDaemon(true);
			fThread.start();
		}
		
		private void checkTimers() {
			long timeToWait= TIMEOUT;
			Map.Entry[] entries;
			synchronized (fLock) {
				if (fStopTimes.size() == 0) {
					if (timeEmpty == 0) {
						timeEmpty= System.currentTimeMillis();
					} else 	if (System.currentTimeMillis() - timeEmpty > MAX_TIME_EMPTY) {
						stop();
						return;
					}
				} else {
					timeEmpty= 0;
				}
				entries= (Map.Entry[])fStopTimes.entrySet().toArray(new Map.Entry[0]);
			}
			long stopTime, currentTime= System.currentTimeMillis();
			Long entryValue;
			Map.Entry entry= null;
			for (int i= 0, numEntries= entries.length; i < numEntries; i++) {
				entry= entries[i];
				entryValue= (Long)entry.getValue();
				if (entryValue == null) {
					continue;
				}
				stopTime= entryValue.longValue();
				if (stopTime <= currentTime) {
					// The timer has expired for this thread.
					// Refresh the UI to show that the thread
					// is performing a long evaluation
					final IThread thread= (IThread)entry.getKey();
					fStopTimes.remove(thread);	
					getView().asyncExec(new Runnable() {
						public void run() {
							fTimedOutThreads.add(thread);
							updateRunningThread(thread);
						}
					});
				} else {
					timeToWait= Math.min(timeToWait, stopTime - currentTime);
				}
			}
			try {
				Thread.sleep(timeToWait);
			} catch (InterruptedException e) {
			}
		}
	}
	
	/**
	 * @see org.eclipse.debug.core.ILaunchesListener#launchesAdded(org.eclipse.debug.core.ILaunch)
	 */
	public void launchesAdded(final ILaunch[] launches) {
		Runnable r= new Runnable() {
			public void run() {
				if (isAvailable()) {
					if (launches.length == 1) {
						insert(launches[0]);
					} else {
						refresh();
					}
					for (int i = 0; i < launches.length; i++) {
						if (launches[i].hasChildren()) {
							getLaunchView().autoExpand(launches[i], false);
						}
					}					

				}
			}
		};

		getView().syncExec(r);		
	}

	/**
	 * @see org.eclipse.debug.core.ILaunchesListener#launchesChanged(org.eclipse.debug.core.ILaunch)
	 */
	public void launchesChanged(final ILaunch[] launches) {
		Runnable r= new Runnable() {
			public void run() {
				if (isAvailable()) {	
					if (launches.length == 1) {
						refresh(launches[0]);
					} else {
						refresh();
					}
					for (int i = 0; i < launches.length; i++) {
						if (launches[i].hasChildren()) {
							getLaunchView().autoExpand(launches[i], false);
						}						
					}
				}
			}
		};
		
		getView().asyncExec(r);				
	}

	/**
	 * @see org.eclipse.debug.core.ILaunchesListener#launchesRemoved(org.eclipse.debug.core.ILaunch)
	 */
	public void launchesRemoved(final ILaunch[] launches) {
		Runnable r= new Runnable() {
			public void run() {
				if (isAvailable()) {
					if (launches.length == 1) {
						remove(launches[0]);
					} else {
						refresh();
					}
					
					getLaunchView().cleanupLaunches(launches);
					
					// auto select the next suspended thread if no current selection
					if (getLaunchViewer().getSelection().isEmpty()) {
						// only change selection if the thing removed is of the same type as the things still there
						Set types = new HashSet();
						for (int i = 0; i < launches.length; i++) {
							ILaunch launch = launches[i];
							ILaunchConfiguration configuration = launch.getLaunchConfiguration();
							if (configuration != null) {
								try {
									types.add(configuration.getType());
								} catch (CoreException e) {
								}
							}
						}
						ILaunchManager lm= DebugPlugin.getDefault().getLaunchManager();
						IDebugTarget[] targets= lm.getDebugTargets();
						if (targets.length > 0) {
							IDebugTarget target= targets[targets.length - 1];
							ILaunchConfiguration configuration = target.getLaunch().getLaunchConfiguration();
							if (configuration != null) {
								try {
									if (types.contains(configuration.getType())) {
										IThread[] threads= target.getThreads();
										for (int i=0; i < threads.length; i++) {
											if (threads[i].isSuspended()) {
												IStackFrame topStackFrame = threads[i].getTopStackFrame();
												if (topStackFrame != null) {
												    getLaunchView().autoExpand(topStackFrame, true);
												}
												return;
											}
										}
									}
								} catch (CoreException e) {
									DebugUIPlugin.log(e);
								}
								getLaunchView().autoExpand(target.getLaunch(), true);
							}
						}
					}
				}
			}
		};

		getView().asyncExec(r);		
	}


	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchesListener2#launchesTerminated(org.eclipse.debug.core.ILaunch[])
	 */
	public void launchesTerminated(final ILaunch[] launches) {
		if (fLastStackFrame != null) {
			ILaunch launch= fLastStackFrame.getLaunch();
			for (int i = 0; i < launches.length; i++) {
				ILaunch terminatedLaunch = launches[i];
				if (terminatedLaunch.equals(launch)) {
					fLastStackFrame= null;
				}
			}
		}
		Runnable r= new Runnable() {
			public void run() {
				getLaunchView().cleanupLaunches(launches);
			}
		};
		getView().asyncExec(r);
	}

    /* (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.views.AbstractDebugEventHandler#doPreprocessEvents(org.eclipse.debug.core.DebugEvent[])
     */
    protected DebugEvent[] doPreprocessEvents(DebugEvent[] events) {
        for (int i = 0; i < events.length; i++) {
            DebugEvent event = events[i];
            Object source = event.getSource();
            switch (event.getKind()) {
            	case DebugEvent.SUSPEND:
            	    if (source instanceof IThread) {
            	        IThread thread = (IThread)source;
            		    try {
            		        IStackFrame frame = thread.getTopStackFrame();
            		        queueData(frame);
            		    } catch (DebugException e) {
            		    }
            	    }
            	    break;
            	case DebugEvent.RESUME:
            		if (source instanceof IThread && event.getDetail() == DebugEvent.CLIENT_REQUEST) {
            			// When a thread resumes, try to select another suspended thread
            			// in the same target.
            			try {
            			    IDebugTarget target = ((IThread) source).getDebugTarget();
            				IThread[] threads= target.getThreads();
            				for (int j = 0; j < threads.length; j++) {
            					IStackFrame frame = threads[j].getTopStackFrame();
            					if (frame != null) {
            						queueData(frame);
            						break;
            					}
            				}
            			} catch (DebugException e) {
            			}
            		}
            		break;
            }
        }
        return events;
    }
}
