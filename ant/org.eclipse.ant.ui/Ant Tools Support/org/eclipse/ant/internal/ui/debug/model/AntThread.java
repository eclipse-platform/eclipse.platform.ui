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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IVariable;

/**
 * A Ant build thread.
 */
public class AntThread extends AntDebugElement implements IThread {
	
	/**
	 * Breakpoints this thread is suspended at or <code>null</code>
	 * if none.
	 */
	private IBreakpoint[] fBreakpoints;
	
	/**
	 * The stackframes associated with this thread
	 */
	private List fFrames;
	
	/**
	 * Whether this thread is stepping
	 */
	private boolean fStepping = false;
	
	/**
	 * The properties associated with this thread
	 */
	private List fProperties;
	
	private boolean fRefreshProperties= true;
	
	/**
	 * Constructs a new thread for the given target
	 * 
	 * @param target the Ant Build
	 */
	public AntThread(AntDebugTarget target) {
		super(target);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IThread#getStackFrames()
	 */
	public synchronized IStackFrame[] getStackFrames() throws DebugException {
		if (isSuspended()) {
			if (fFrames == null || fFrames.size() == 0) {
				getStackFrames0();
				fireChangeEvent(DebugEvent.CONTENT);
			}
		} 
		
		return (IStackFrame[]) fFrames.toArray(new IStackFrame[fFrames.size()]);
	}
	
	/**
	 * Returns the current stack frames in the thread
	 * possibly waiting until the frames are populating
     * 
	 * @return the current stack frames in the thread
	 * @throws DebugException if unable to perform the request
	 */
	private void getStackFrames0() {
		fTarget.getStackFrames();
        if (fFrames != null && fFrames.size() > 0) {
            //frames set..no need to wait
            return;
        }
		try {
		    wait();
		} catch (InterruptedException e) {
		}
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
		if (frames != null && frames.length > 0) {
			return frames[0];
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IThread#getName()
	 */
	public String getName() {
		return "Thread [Ant Build]"; //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IThread#getBreakpoints()
	 */
	public IBreakpoint[] getBreakpoints() {
		if (fBreakpoints == null) {
			return new IBreakpoint[0];
		}
		return fBreakpoints;
	}
	
	/**
	 * Sets the breakpoints this thread is suspended at, or <code>null</code>
	 * if none.
	 * 
	 * @param breakpoints the breakpoints this thread is suspended at, or <code>null</code>
	 * if none
	 */
	protected void setBreakpoints(IBreakpoint[] breakpoints) {
		fBreakpoints = breakpoints;
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
		return getDebugTarget().isSuspended();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#resume()
	 */
	public synchronized void resume() throws DebugException {
		aboutToResume(DebugEvent.CLIENT_REQUEST, false);
		getDebugTarget().resume();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#suspend()
	 */
	public synchronized void suspend() throws DebugException {
		getDebugTarget().suspend();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStep#canStepInto()
	 */
	public boolean canStepInto() {
	    return isSuspended();
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
	public synchronized void stepInto() throws DebugException {
	    aboutToResume(DebugEvent.STEP_INTO, true);
		((AntDebugTarget)getDebugTarget()).stepInto();
	}
	
	private void aboutToResume(int detail, boolean stepping) {
	    fRefreshProperties= true;
        fFrames.clear();
	    setStepping(stepping);
	    setBreakpoints(null);
		fireResumeEvent(detail);
    }

    /* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStep#stepOver()
	 */
	public synchronized void stepOver() throws DebugException {
	    aboutToResume(DebugEvent.STEP_OVER, true);
		((AntDebugTarget)getDebugTarget()).stepOver();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStep#stepReturn()
	 */
	public synchronized void stepReturn() throws DebugException {
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
		fFrames= null;
		getDebugTarget().terminate();
	}
	
	/**
	 * Sets whether this thread is stepping
	 * 
	 * @param stepping whether stepping
	 */
	protected void setStepping(boolean stepping) {
		fStepping = stepping;
	}

    public synchronized void buildStack(String data) {
		String[] strings= data.split(DebugMessageIds.MESSAGE_DELIMITER);
		//0 STACK message
		//1 targetName
		//2 taskName
		//3 filePath
		//4 lineNumber
		//5 ...
		fFrames= new ArrayList();
		String name;
		String filePath;
		int lineNumber;
		AntStackFrame frame;
		int stackFrameId= 0;
		for (int i = 1; i < strings.length; i++) {
		    name= strings[i] + ": " + strings[++i]; //$NON-NLS-1$
			filePath= strings[++i];
			lineNumber= Integer.parseInt(strings[++i]);
			frame= new AntStackFrame(this, stackFrameId++, name, filePath, lineNumber);
			fFrames.add(frame);
		}
		//wake up the call from getStackFrames
		notifyAll();
    }
    
    public synchronized void newProperties(String data) {
	    try {
	    	String[] datum= data.split(DebugMessageIds.MESSAGE_DELIMITER);
	    	if (fProperties == null) {
	    		fProperties= new ArrayList(datum.length);
	    	}
	    	//0 PROPERTIES message
	    	//1 propertyName length
	    	//2 propertyName
	    	//3 propertyValue length
	    	//3 propertyValue
	    	//4 ...
	    	if (datum.length > 1) { //new properties
	    		String propertyName;
	    		String propertyValue;
	    		AntProperty property;
	    		int propertyNameLength;
	    		int propertyValueLength;
	    		for (int i = 1; i < datum.length; i++) {
	    			propertyNameLength= Integer.parseInt(datum[i]);
	    			propertyName= datum[++i];
	    			while (propertyName.length() != propertyNameLength) {
	    				propertyName+= DebugMessageIds.MESSAGE_DELIMITER + datum[++i];
	    			}
	    			propertyValueLength= Integer.parseInt(datum[++i]);
	    			propertyValue= datum[++i];
	    			while (propertyValue.length() != propertyValueLength) {
	    				propertyValue+= DebugMessageIds.MESSAGE_DELIMITER + datum[++i];
	    			}
	    			
	    			property= new AntProperty((AntDebugTarget) getDebugTarget(), propertyName, propertyValue);
	    			fProperties.add(property);
	    		}
	    	}
	    } finally {
            fRefreshProperties= false;
	        //wake up the call from getVariables
	    	notifyAll();
	    }
	}
    
    protected synchronized IVariable[] getVariables() {
        if (fRefreshProperties) {
            fTarget.getProperties();
            if (fRefreshProperties) { 
            //properties have not been set; need to wait
                try {
                    wait();
                } catch (InterruptedException ie) {
                }
            }
        }
        
        return (IVariable[])fProperties.toArray(new IVariable[fProperties.size()]);
      }
}