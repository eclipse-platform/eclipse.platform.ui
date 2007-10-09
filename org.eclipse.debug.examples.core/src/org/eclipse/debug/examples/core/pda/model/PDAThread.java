/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Bjorn Freeman-Benson - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.examples.core.pda.model;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IVariable;

/**
 * A PDA thread. A PDA VM is single threaded.
 */
public class PDAThread extends PDADebugElement implements IThread, IPDAEventListener {
	
	/**
	 * Breakpoint this thread is suspended at or <code>null</code>
	 * if none.
	 */
	private IBreakpoint fBreakpoint;
	
	/**
	 * Whether this thread is stepping
	 */
	private boolean fStepping = false;
	
	/**
	 * Wether this thread is suspended
	 */
	private boolean fSuspended = false;

	/**
	 * Most recent error event or <code>null</code>
	 */
	private String fErrorEvent;
	
	/**
	 * Table mapping stack frames to current variables
	 */
	private Map fVariables = new HashMap();
	
	/**
	 * Constructs a new thread for the given target
	 * 
	 * @param target VM
	 */
	public PDAThread(PDADebugTarget target) {
		super(target);
		getPDADebugTarget().addEventListener(this);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IThread#getStackFrames()
	 */
	public IStackFrame[] getStackFrames() throws DebugException {
		if (isSuspended()) {
			String framesData = sendRequest("stack");
			if (framesData != null) {
				String[] frames = framesData.split("#");
				IStackFrame[] theFrames = new IStackFrame[frames.length];
				for (int i = 0; i < frames.length; i++) {
					String data = frames[i];
					theFrames[frames.length - i - 1] = new PDAStackFrame(this, data, i);
				}
				return theFrames;
			}
		}
		return new IStackFrame[0];
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IThread#hasStackFrames()
	 */
	public boolean hasStackFrames() throws DebugException {
		return isSuspended();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IThread#getPriority()
	 */
	public int getPriority() throws DebugException {
		return 0;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IThread#getTopStackFrame()
	 */
	public IStackFrame getTopStackFrame() throws DebugException {
		IStackFrame[] frames = getStackFrames();
		if (frames.length > 0) {
			return frames[0];
		}
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IThread#getName()
	 */
	public String getName() {
		return "Main thread";
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IThread#getBreakpoints()
	 */
	public IBreakpoint[] getBreakpoints() {
		if (fBreakpoint == null) {
			return new IBreakpoint[0];
		}
		return new IBreakpoint[]{fBreakpoint};
	}
	
	/**
	 * Notifies this thread it has been suspended by the given breakpoint.
	 * 
	 * @param breakpoint breakpoint
	 */
	public void suspendedBy(IBreakpoint breakpoint) {
		fBreakpoint = breakpoint;
		suspended(DebugEvent.BREAKPOINT);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#canResume()
	 */
	public boolean canResume() {
		return isSuspended();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#canSuspend()
	 */
	public boolean canSuspend() {
		return !isSuspended();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#isSuspended()
	 */
	public boolean isSuspended() {
		return fSuspended && !isTerminated();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#resume()
	 */
	public void resume() throws DebugException {
		//#ifdef ex2
//#		// TODO: Exercise 2 - send resume request to interpreter		
		//#else
		sendRequest("resume");
		//#endif
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#suspend()
	 */
	public void suspend() throws DebugException {
		//#ifdef ex2
//#		// TODO: Exercise 2 - send suspend request to interpreter		
		//#else
	    sendRequest("suspend");
		//#endif
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStep#canStepInto()
	 */
	public boolean canStepInto() {
		return false;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStep#canStepOver()
	 */
	public boolean canStepOver() {
		return isSuspended();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStep#canStepReturn()
	 */
	public boolean canStepReturn() {
		return false;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStep#isStepping()
	 */
	public boolean isStepping() {
		return fStepping;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStep#stepInto()
	 */
	public void stepInto() throws DebugException {
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStep#stepOver()
	 */
	public void stepOver() throws DebugException {
		sendRequest("step");
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStep#stepReturn()
	 */
	public void stepReturn() throws DebugException {
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ITerminate#canTerminate()
	 */
	public boolean canTerminate() {
		return !isTerminated();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ITerminate#isTerminated()
	 */
	public boolean isTerminated() {
		return getDebugTarget().isTerminated();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ITerminate#terminate()
	 */
	public void terminate() throws DebugException {
		//#ifdef ex2
//#		// TODO: Exercise 2 - send termination request to interpreter		
		//#else
		sendRequest("exit");
		//#endif
	}
	
	/**
	 * Sets whether this thread is stepping
	 * 
	 * @param stepping whether stepping
	 */
	private void setStepping(boolean stepping) {
		fStepping = stepping;
	}
	
	/**
	 * Sets whether this thread is suspended
	 * 
	 * @param suspended whether suspended
	 */
	private void setSuspended(boolean suspended) {
		fSuspended = suspended;
	}

	/**
	 * Sets the most recent error event encountered, or <code>null</code>
	 * to clear the most recent error
	 * 
	 * @param event one of 'unimpinstr' or 'nosuchlabel' or <code>null</code>
	 */
	private void setError(String event) {
		fErrorEvent = event;
	}

	/**
	 * Returns the most revent error event encountered since the last
	 * suspend, or <code>null</code> if none.
	 * 
	 * @return the most revent error event encountered since the last
	 * suspend, or <code>null</code> if none
	 */
	public Object getError() {
		return fErrorEvent;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.examples.core.pda.model.IPDAEventListener#handleEvent(java.lang.String)
	 */
	public void handleEvent(String event) {
		// clear previous state
		fBreakpoint = null;
		setStepping(false);
		
		// handle events
		if (event.startsWith("resumed")) {
			setSuspended(false);
			if (event.endsWith("step")) {
				setStepping(true);
				resumed(DebugEvent.STEP_OVER);
			//#ifdef ex2
//#			}
//#			// TODO: Exercise 2 - handle/fire "client" resume event
			//#else	
			} else if (event.endsWith("client")) {
				resumed(DebugEvent.CLIENT_REQUEST);
			}
			//#endif
			//#ifdef ex5
//#			// TODO: Exercise 5 - handle start of drop event
			//#else
			else if (event.endsWith("drop")) {
				resumed(DebugEvent.STEP_RETURN);
			}
			//#endif
		} else if (event.startsWith("suspended")) {
			setSuspended(true);
			//#ifdef ex2
//#			// TODO: Exercise 2 - handle/fire "client" suspend event
//#			if (event.endsWith("step")) {
//#				suspended(DebugEvent.STEP_END);
//#			} else if (event.startsWith("suspended event") && getError() != null) {
//#				exceptionHit();
//#			}
			//#else
			if (event.endsWith("client")) {
				suspended(DebugEvent.CLIENT_REQUEST);
			} else if (event.endsWith("step")) {
				suspended(DebugEvent.STEP_END);
			} else if (event.startsWith("suspended event") && getError() != null) {
				exceptionHit();
			} 
			//#endif
			//#ifdef ex5
//#			// TODO: Exercise 5 - handle end of drop event
			//#else
			else if (event.endsWith("drop")) {
				suspended(DebugEvent.STEP_END);
			}
			//#endif
		} else if (event.equals("started")) {
			fireCreationEvent();
		} else {
			setError(event);
		}
		
	}
	
	/**
	 * Notification the target has resumed for the given reason.
	 * Clears any error condition that was last encountered and
	 * fires a resume event, and clears all cached variables
	 * for stack frames.
	 * 
	 * @param detail reason for the resume
	 */
	private void resumed(int detail) {
		setError(null);
		synchronized (fVariables) {
			fVariables.clear();
		}
		fireResumeEvent(detail);
	}
	
	/**
	 * Notification the target has suspended for the given reason
	 * 
	 * @param detail reason for the suspend
	 */
	private void suspended(int detail) {
		fireSuspendEvent(detail);
	}

	/**
     * Notification an error was encountered. Fires a breakpoint
     * suspend event.
     */
    private void exceptionHit() {
    	suspended(DebugEvent.BREAKPOINT);
    }  
	
	/**
	 * Sets the current variables for the given stack frame. Called
	 * by PDA stack frame when it is created.
	 * 
	 * @param frame
	 * @param variables
	 */
	protected void setVariables(IStackFrame frame, IVariable[] variables) {
		synchronized (fVariables) {
			fVariables.put(frame, variables);
		}
	}
	
	/**
	 * Returns the current variables for the given stack frame, or
	 * <code>null</code> if none.
	 * 
	 * @param frame stack frame
	 * @return variables or <code>null</code>
	 */
	protected IVariable[] getVariables(IStackFrame frame) {
		synchronized (fVariables) {
			IVariable[] variables = (IVariable[]) fVariables.get(frame);
			if (variables == null) {
				return new IVariable[0];
			}
			return variables;
		}
	}
	
	/**
	 * Pops the top frame off the callstack.
	 *
	 * @throws DebugException
	 */
	public void pop() throws DebugException {
		//#ifdef ex5
//#		// TODO: Exercise 5 - send drop request		
		//#else
		sendRequest("drop");
		//#endif
	}
	
	/**
	 * Returns whether this thread can pop the top stack frame.
	 *
	 * @return whether this thread can pop the top stack frame
	 */
	public boolean canPop() {
		//#ifdef ex5
//#		// TODO: Exercise 5 - allow pop if there is more than 1 frame on the stack		
		//#else
		try {
			return getStackFrames().length > 1;
		} catch (DebugException e) {
		}
		//#endif
		return false;
	}
}
