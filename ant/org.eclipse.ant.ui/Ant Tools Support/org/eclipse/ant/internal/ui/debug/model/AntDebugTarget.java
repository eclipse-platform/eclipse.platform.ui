/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ant.internal.ui.debug.model;

import org.eclipse.ant.internal.ui.debug.IAntDebugConstants;
import org.eclipse.ant.internal.ui.debug.IAntDebugController;
import org.eclipse.core.internal.variables.StringVariableManager;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.ILineBreakpoint;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.ui.externaltools.internal.model.IExternalToolConstants;

/**
 * Ant Debug Target
 */
public class AntDebugTarget extends AntDebugElement implements IDebugTarget {
	
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
		fTarget = this;
		fProcess = process;
		
		fController= controller;
		
		fThread = new AntThread(this);
		fThreads = new IThread[] {fThread};
		
		DebugPlugin.getDefault().getBreakpointManager().addBreakpointListener(this);
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
				fName= getLaunch().getLaunchConfiguration().getAttribute(IExternalToolConstants.ATTR_LOCATION, DebugModelMessages.getString("AntDebugTarget.0")); //$NON-NLS-1$
				fName= StringVariableManager.getDefault().performStringSubstitution(fName);
			} catch (CoreException e) {
				fName = DebugModelMessages.getString("AntDebugTarget.0"); //$NON-NLS-1$
			}
		}
		return fName;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IDebugTarget#supportsBreakpoint(org.eclipse.debug.core.model.IBreakpoint)
	 */
	public boolean supportsBreakpoint(IBreakpoint breakpoint) {
		if (breakpoint.getModelIdentifier().equals(IAntDebugConstants.ID_ANT_DEBUG_MODEL)) {
			try {
				String buildFilePath = getLaunch().getLaunchConfiguration().getAttribute(IExternalToolConstants.ATTR_LOCATION, (String)null);
				buildFilePath= StringVariableManager.getDefault().performStringSubstitution(buildFilePath);
				if (buildFilePath != null) {
					IMarker marker = breakpoint.getMarker();
					if (marker != null) {
						//need to consider all breakpoints as no way to tell which set
					    //or buildfiles will be executed (ant task)
					    return true;
					}
				}
			} catch (CoreException e) {
			}			
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
		return !isTerminated() && isSuspended();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#canSuspend()
	 */
	public boolean canSuspend() {
		return !isTerminated() && !isSuspended();
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
	}
	
	/**
	 * Notification the target has suspended for the given reason
	 * 
	 * @param detail reason for the suspend
	 */
	protected void suspended(int detail) {
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
		fController.handleBreakpoint(breakpoint, true);
	}

    /* (non-Javadoc)
	 * @see org.eclipse.debug.core.IBreakpointListener#breakpointRemoved(org.eclipse.debug.core.model.IBreakpoint, org.eclipse.core.resources.IMarkerDelta)
	 */
	public void breakpointRemoved(IBreakpoint breakpoint, IMarkerDelta delta) {
		fController.handleBreakpoint(breakpoint, false);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IBreakpointListener#breakpointChanged(org.eclipse.debug.core.model.IBreakpoint, org.eclipse.core.resources.IMarkerDelta)
	 */
	public void breakpointChanged(IBreakpoint breakpoint, IMarkerDelta delta) {
		if (supportsBreakpoint(breakpoint)) {
			try {
				if (breakpoint.isEnabled()) {
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
	 * manager.
	 */
	private void installDeferredBreakpoints() {
		IBreakpoint[] breakpoints = DebugPlugin.getDefault().getBreakpointManager().getBreakpoints(IAntDebugConstants.ID_ANT_DEBUG_MODEL);
		for (int i = 0; i < breakpoints.length; i++) {
			breakpointAdded(breakpoints[i]);
		}
	}
	
	/**
	 * Called when this debug target terminates.
	 */
	protected void terminated() {
		fThreads= new IThread[0];
		fTerminated = true;
		fSuspended = false;
		DebugPlugin.getDefault().getBreakpointManager().removeBreakpointListener(this);
		if (!getProcess().isTerminated()) {
		    try {
		        getProcess().terminate();
		    } catch (DebugException e) {       
		    }
		}
		fireTerminateEvent();
	}
	
	/**
	 * Single step the Ant build.
	 * 
	 * @throws DebugException if the request fails
	 */
	protected void stepOver() throws DebugException {
	    fSuspended= false;
		fController.stepOver();
	}
	
	/**
	 * Step-into the Ant build.
	 * 
	 * @throws DebugException if the request fails
	 */
	protected void stepInto() throws DebugException {
	    fSuspended= false;
	    fController.stepInto();
	}
	
	/**
	 * Notification a breakpoint was encountered. Determine
	 * which breakpoint was hit and fire a suspend event.
	 * 
	 * @param event debug event
	 */
	protected void breakpointHit(String event) {
		// determine which breakpoint was hit, and set the thread's breakpoint
		String[] datum= event.split(DebugMessageIds.MESSAGE_DELIMITER);
		String fileName= datum[1];
		int lineNumber = Integer.parseInt(datum[2]);
		IBreakpoint[] breakpoints = DebugPlugin.getDefault().getBreakpointManager().getBreakpoints(IAntDebugConstants.ID_ANT_DEBUG_MODEL);
		for (int i = 0; i < breakpoints.length; i++) {
			IBreakpoint breakpoint = breakpoints[i];
			if (supportsBreakpoint(breakpoint)) {
				if (breakpoint instanceof ILineBreakpoint) {
					ILineBreakpoint lineBreakpoint = (ILineBreakpoint) breakpoint;
					try {
						if (lineBreakpoint.getLineNumber() == lineNumber && 
								fileName.equals(breakpoint.getMarker().getResource().getLocation().toOSString())) {
							fThread.setBreakpoints(new IBreakpoint[]{breakpoint});
							break;
						}
					} catch (CoreException e) {
					}
				}
			}
		}
		suspended(DebugEvent.BREAKPOINT);
	}	
	
	protected void getStackFrames() {
		fController.getStackFrames();
	}
	
	protected void getProperties() {
		fController.getProperties();
	}
}