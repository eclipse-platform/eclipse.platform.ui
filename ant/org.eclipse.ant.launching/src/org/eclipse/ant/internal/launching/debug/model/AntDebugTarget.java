/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ant.internal.launching.debug.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ant.internal.launching.debug.IAntDebugConstants;
import org.eclipse.ant.internal.launching.debug.IAntDebugController;
import org.eclipse.core.externaltools.internal.IExternalToolConstants;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.IBreakpointManagerListener;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.ILineBreakpoint;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IThread;

/**
 * Ant Debug Target
 */
public class AntDebugTarget extends AntDebugElement implements IDebugTarget, IDebugEventSetListener, IBreakpointManagerListener {
	
	// associated system process (Ant Build)
	private IProcess fProcess;
	
	// containing launch object
	private ILaunch fLaunch;
	
	// Build file name
	private String fName;

	// suspend state
	private boolean fSuspended= false;
	
	// terminated state
	private boolean fTerminated= false;
	
	// threads
	private AntThread fThread;
	private IThread[] fThreads;
	
	private IAntDebugController fController;
    
    private List fRunToLineBreakpoints;

	/**
	 * Constructs a new debug target in the given launch for the 
	 * associated Ant build process.
	 * 
	 * @param launch containing launch
	 * @param process Ant build process
	 * @param controller the controller to communicate to the Ant build
	 */
	public AntDebugTarget(ILaunch launch, IProcess process, IAntDebugController controller) {
		super(null);
		fLaunch = launch;
		fProcess = process;
		
		fController= controller;
		
		fThread = new AntThread(this);
		fThreads = new IThread[] {fThread};
        
        DebugPlugin.getDefault().getBreakpointManager().addBreakpointManagerListener(this);
		DebugPlugin.getDefault().getBreakpointManager().addBreakpointListener(this);
        DebugPlugin.getDefault().addDebugEventListener(this);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IDebugTarget#getProcess()
	 */
	public IProcess getProcess() {
		return fProcess;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IDebugTarget#getThreads()
	 */
	public IThread[] getThreads() {
		return fThreads;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IDebugTarget#hasThreads()
	 */
	public boolean hasThreads() throws DebugException {
		return !fTerminated && fThreads.length > 0;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IDebugTarget#getName()
	 */
	public String getName() throws DebugException {
		if (fName == null) {
			try {
				fName= getLaunch().getLaunchConfiguration().getAttribute(IExternalToolConstants.ATTR_LOCATION, DebugModelMessages.AntDebugTarget_0);
				fName= VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(fName);
			} catch (CoreException e) {
				fName = DebugModelMessages.AntDebugTarget_0;
			}
		}
		return fName;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IDebugTarget#supportsBreakpoint(org.eclipse.debug.core.model.IBreakpoint)
	 */
	public boolean supportsBreakpoint(IBreakpoint breakpoint) {
		if (breakpoint.getModelIdentifier().equals(IAntDebugConstants.ID_ANT_DEBUG_MODEL)) {
		    //need to consider all breakpoints as no way to tell which set
		    //of buildfiles will be executed (ant task)
		    return true;
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IDebugElement#getDebugTarget()
	 */
	public IDebugTarget getDebugTarget() {
		return this;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IDebugElement#getLaunch()
	 */
	public ILaunch getLaunch() {
		return fLaunch;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ITerminate#canTerminate()
	 */
	public boolean canTerminate() {
		return !fTerminated;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ITerminate#isTerminated()
	 */
	public boolean isTerminated() {
		return fTerminated;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ITerminate#terminate()
	 */
	public void terminate() throws DebugException {
	    terminated();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#canResume()
	 */
	public boolean canResume() {
		return !fTerminated && fSuspended;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#canSuspend()
	 */
	public boolean canSuspend() {
		return !fTerminated && !fSuspended;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#isSuspended()
	 */
	public boolean isSuspended() {
		return fSuspended;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#resume()
	 */
	public void resume() throws DebugException {
		fSuspended= false;
		fController.resume();
		if(fThread.isSuspended()) {
			fThread.resumedByTarget();
		}
		fireResumeEvent(DebugEvent.CLIENT_REQUEST);
	}
	
	/**
	 * Notification the target has suspended for the given reason
	 * 
	 * @param detail reason for the suspend
	 */
	public void suspended(int detail) {
		fSuspended = true;
		fThread.setStepping(false);
		fThread.fireSuspendEvent(detail);
	}	
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#suspend()
	 */
	public void suspend() throws DebugException {
		fController.suspend();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IBreakpointListener#breakpointAdded(org.eclipse.debug.core.model.IBreakpoint)
	 */
	public void breakpointAdded(IBreakpoint breakpoint) {
		if(!fTerminated) {
			fController.handleBreakpoint(breakpoint, true);
	        if (breakpoint instanceof AntLineBreakpoint) {
	            if (((AntLineBreakpoint) breakpoint).isRunToLine()) {
	                if (fRunToLineBreakpoints == null) {
	                    fRunToLineBreakpoints= new ArrayList();
	                }
	                fRunToLineBreakpoints.add(breakpoint);
	            }
	        }
		}
	}

    /* (non-Javadoc)
	 * @see org.eclipse.debug.core.IBreakpointListener#breakpointRemoved(org.eclipse.debug.core.model.IBreakpoint, org.eclipse.core.resources.IMarkerDelta)
	 */
	public void breakpointRemoved(IBreakpoint breakpoint, IMarkerDelta delta) {
		if(!fTerminated) {
			fController.handleBreakpoint(breakpoint, false);
	        if (fRunToLineBreakpoints != null) {
	            if (fRunToLineBreakpoints.remove(breakpoint) && fRunToLineBreakpoints.isEmpty()) {
	                fRunToLineBreakpoints= null;
	            }
	        }
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IBreakpointListener#breakpointChanged(org.eclipse.debug.core.model.IBreakpoint, org.eclipse.core.resources.IMarkerDelta)
	 */
	public void breakpointChanged(IBreakpoint breakpoint, IMarkerDelta delta) {
		if (supportsBreakpoint(breakpoint)) {
			try {
				if (breakpoint.isEnabled() && DebugPlugin.getDefault().getBreakpointManager().isEnabled()) {
					breakpointAdded(breakpoint);
				} else {
					breakpointRemoved(breakpoint, null);
				}
			} catch (CoreException e) {
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IDisconnect#canDisconnect()
	 */
	public boolean canDisconnect() {
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IDisconnect#disconnect()
	 */
	public void disconnect() throws DebugException {
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IDisconnect#isDisconnected()
	 */
	public boolean isDisconnected() {
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlockRetrieval#supportsStorageRetrieval()
	 */
	public boolean supportsStorageRetrieval() {
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlockRetrieval#getMemoryBlock(long, long)
	 */
	public IMemoryBlock getMemoryBlock(long startAddress, long length) throws DebugException {
		return null;
	}

	/**
	 * Notification we have connected to the Ant build logger and it has started.
	 * Resume the build.
	 */
	public void buildStarted() {
		fireCreationEvent();
		installDeferredBreakpoints();
		try {
			resume();
		} catch (DebugException e) {
		}
	}
	
	/**
	 * Install breakpoints that are already registered with the breakpoint
	 * manager if the breakpoint manager is enabled and the breakpoint is enabled.
	 */
	private void installDeferredBreakpoints() {
		IBreakpointManager manager= DebugPlugin.getDefault().getBreakpointManager();
		if (!manager.isEnabled()) {
			return;
		}
		IBreakpoint[] breakpoints = manager.getBreakpoints(IAntDebugConstants.ID_ANT_DEBUG_MODEL);
		for (int i = 0; i < breakpoints.length; i++) {
            IBreakpoint breakpoint= breakpoints[i];
            try {
                if (breakpoint.isEnabled()) {
                    breakpointAdded(breakpoints[i]);
                }
            } catch (CoreException e) {
            }
		}
	}
	
	/**
	 * Called when this debug target terminates.
	 */
	public synchronized void terminated() {
		if(!fTerminated) {
			fThreads= new IThread[0];
			fTerminated = true;
			fSuspended = false;
			fController.terminate();
			if (DebugPlugin.getDefault() != null) {
				DebugPlugin.getDefault().getBreakpointManager().removeBreakpointListener(this);
				DebugPlugin.getDefault().removeDebugEventListener(this);
				DebugPlugin.getDefault().getBreakpointManager().removeBreakpointManagerListener(this);
			}
			if (!getProcess().isTerminated()) {
			    try {
	                fProcess.terminate();
			    } catch (DebugException e) {       
			    }
			}
			if (DebugPlugin.getDefault() != null) {
				fireTerminateEvent();
			}
		}
	}
	
	/**
	 * Single step the Ant build.
	 * 
	 * @throws DebugException if the request fails
	 */
	public void stepOver() {
	    fSuspended= false;
		fController.stepOver();
		fireResumeEvent(DebugEvent.CLIENT_REQUEST);
	}
	
	/**
	 * Step-into the Ant build.
	 * 
	 * @throws DebugException if the request fails
	 */
	public void stepInto() {
	    fSuspended= false;
	    fController.stepInto();
	    fireResumeEvent(DebugEvent.CLIENT_REQUEST);
	}
	
	/**
	 * Notification a breakpoint was encountered. Determine
	 * which breakpoint was hit and fire a suspend event.
	 * 
	 * @param event debug event
	 */
	public void breakpointHit(String event) {
		// determine which breakpoint was hit, and set the thread's breakpoint
		String[] datum= event.split(DebugMessageIds.MESSAGE_DELIMITER);
		String fileName= datum[1];
		int lineNumber = Integer.parseInt(datum[2]);
		IBreakpoint[] breakpoints = DebugPlugin.getDefault().getBreakpointManager().getBreakpoints(IAntDebugConstants.ID_ANT_DEBUG_MODEL);
        boolean found= false;
		for (int i = 0; i < breakpoints.length; i++) {
           ILineBreakpoint lineBreakpoint = (ILineBreakpoint)breakpoints[i];
           if (setThreadBreakpoint(lineBreakpoint, lineNumber, fileName)) {
               found= true;
               break;
           }
		}
        if (!found && fRunToLineBreakpoints != null) {
            Iterator iter= fRunToLineBreakpoints.iterator();
            while (iter.hasNext()) {
                ILineBreakpoint lineBreakpoint = (ILineBreakpoint) iter.next();
                if (setThreadBreakpoint(lineBreakpoint, lineNumber, fileName)) {
                    break;
                }
            }
        }
		suspended(DebugEvent.BREAKPOINT);
	}	
    
    private boolean setThreadBreakpoint(ILineBreakpoint lineBreakpoint, int lineNumber, String fileName) {
        try {
            if (lineBreakpoint.getLineNumber() == lineNumber && 
                    fileName.equals(lineBreakpoint.getMarker().getResource().getLocation().toOSString())) {
                fThread.setBreakpoints(new IBreakpoint[]{lineBreakpoint});
                return true;
            }
        } catch (CoreException e) {
        }
        return false;
    }
	
    public void breakpointHit (IBreakpoint breakpoint) {
        fThread.setBreakpoints(new IBreakpoint[]{breakpoint});
        suspended(DebugEvent.BREAKPOINT);
    }
    
	public void getStackFrames() {
		if(isSuspended()) {
			fController.getStackFrames();
		}
	}
	
	public void getProperties() {
		if(!fTerminated) {
			fController.getProperties();
		}
	}

    /* (non-Javadoc)
     * @see org.eclipse.debug.core.IDebugEventSetListener#handleDebugEvents(org.eclipse.debug.core.DebugEvent[])
     */
    public void handleDebugEvents(DebugEvent[] events) {
        for (int i = 0; i < events.length; i++) {
            DebugEvent event = events[i];
            if (event.getKind() == DebugEvent.TERMINATE && event.getSource().equals(fProcess)) {
                terminated();
            }
        }
    }
    
    /**
     * When the breakpoint manager disables, remove all registered breakpoints
     * requests from the VM. When it enables, reinstall them.
     *
     * @see org.eclipse.debug.core.IBreakpointManagerListener#breakpointManagerEnablementChanged(boolean)
     */
    public void breakpointManagerEnablementChanged(boolean enabled) {
        IBreakpoint[] breakpoints = DebugPlugin.getDefault().getBreakpointManager().getBreakpoints(IAntDebugConstants.ID_ANT_DEBUG_MODEL);
        for (int i = 0; i < breakpoints.length; i++) {
            IBreakpoint breakpoint = breakpoints[i];
            if (enabled) {
                breakpointAdded(breakpoint);
            } else {
                breakpointRemoved(breakpoint, null);
            }
        }
    }

	public IAntDebugController getAntDebugController() {
		return fController;
	}   
}
