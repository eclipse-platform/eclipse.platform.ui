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
 * An Ant build thread.
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
	private List fFrames= new ArrayList(1);
	
	/**
	 * The stackframes to be reused on suspension
	 */
	private List fOldFrames;
	
	/**
	 * Whether this thread is stepping
	 */
	private boolean fStepping = false;
	
	private boolean fRefreshProperties= true;
	
	/**
	 * The user properties associated with this thread
	 */
	private AntProperties fUserProperties;
	
	/**
	 * The system properties associated with this thread
	 */
	private AntProperties fSystemProperties;
	
	/**
	 * The properties set during the build associated with this thread
	 */
	private AntProperties fRuntimeProperties;
	
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
	public synchronized IStackFrame[] getStackFrames() {
		if (isSuspended()) {
			if (fFrames.size() == 0) {
				getStackFrames0();
			}
		} 
		
		return (IStackFrame[]) fFrames.toArray(new IStackFrame[fFrames.size()]);
	}
	
	/**
	 * Returns the current stack frames in the thread
	 * possibly waiting until the frames are populated
     * 
	 * @return the current stack frames in the thread
	 */
	private synchronized void getStackFrames0() {
		fTarget.getStackFrames();
        if (fFrames.size() > 0) {
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
	public synchronized IStackFrame getTopStackFrame() {
		if (isSuspended()) {
			if (fFrames.size() == 0) {
				getStackFrames0();
				return (IStackFrame)fFrames.get(0);
			}
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
	    fOldFrames= new ArrayList(fFrames);
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
		fFrames.clear();
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
		if (fOldFrames != null && (strings.length - 1)/ 4 != fOldFrames.size()) {
			fOldFrames= null; //stack size changed..do not preserve
		}
		StringBuffer name;
		String filePath;
		int lineNumber;
		int stackFrameId= 0;
        String taskName;
		for (int i = 1; i < strings.length; i++) {
			if (strings[i].length() > 0) {
                name= new StringBuffer(strings[i]);
                taskName= strings[++i];
                if (taskName.length() > 0) {
                     name.append(": "); //$NON-NLS-1$
                     name.append(taskName);
                }
			} else {
				name= new StringBuffer(strings[++i]);
			}
			filePath= strings[++i];
			lineNumber= Integer.parseInt(strings[++i]);
			addFrame(stackFrameId++, name.toString(), filePath, lineNumber);
		}
		//wake up the call from getStackFrames
		notifyAll();
    }
    
    private void addFrame(int stackFrameId, String name, String filePath, int lineNumber) {
    	AntStackFrame frame= getOldFrame();
    	
    	if (frame == null || !frame.getFilePath().equals(filePath)) {
    		frame= new AntStackFrame(this, stackFrameId, name, filePath, lineNumber);
    	} else {
    		frame.setFilePath(filePath);
    		frame.setId(stackFrameId);
    		frame.setLineNumber(lineNumber);
    		frame.setName(name);
    	}
		fFrames.add(frame);
    }
    
    private AntStackFrame getOldFrame() {
    	if (fOldFrames == null) {
    		return null;
    	}
    	AntStackFrame frame= (AntStackFrame) fOldFrames.remove(0);
    	if (fOldFrames.isEmpty()) {
    		fOldFrames= null;
    	}
    	return frame;
    }
    
    public synchronized void newProperties(String data) {
	    try {
	    	String[] datum= data.split(DebugMessageIds.MESSAGE_DELIMITER);
	    	if (fUserProperties == null) {
	    		initializePropertyGroups();
	    	}
	    	
	    	List userProperties= ((AntPropertiesValue)fUserProperties.getValue()).getProperties();
	    	List systemProperties= ((AntPropertiesValue)fSystemProperties.getValue()).getProperties();
	    	List runtimeProperties= ((AntPropertiesValue)fRuntimeProperties.getValue()).getProperties();
	    	//0 PROPERTIES message
	    	//1 propertyName length
	    	//2 propertyName
	    	//3 propertyValue length
	    	//3 propertyValue
	    	//4 propertyType
	    	//5 ...
	    	if (datum.length > 1) { //new properties
	    		String propertyName;
	    		String propertyValue;
	    		int propertyNameLength;
	    		int propertyValueLength;
	    		for (int i = 1; i < datum.length; i++) {
	    			propertyNameLength= Integer.parseInt(datum[i]);
	    			propertyName= datum[++i];
	    			while (propertyName.length() != propertyNameLength) {
	    				propertyName+= DebugMessageIds.MESSAGE_DELIMITER + datum[++i];
	    			}
	    			propertyValueLength= Integer.parseInt(datum[++i]);
	    			if (propertyValueLength == 0 && i + 1 == datum.length) { //bug 81299
	    				propertyValue= ""; //$NON-NLS-1$
	    			} else {
	    				propertyValue= datum[++i];
	    			}
	    			while (propertyValue.length() != propertyValueLength) {
	    				propertyValue+= DebugMessageIds.MESSAGE_DELIMITER + datum[++i];
	    			}
	    			
	    			int propertyType= Integer.parseInt(datum[++i]);
	    			addProperty(userProperties, systemProperties, runtimeProperties, propertyName, propertyValue, propertyType);
	    		}
	    	}
	    } finally {
            fRefreshProperties= false;
	        //wake up the call from getVariables
	    	notifyAll();
	    }
	}

	private void addProperty(List userProperties, List systemProperties, List runtimeProperties, String propertyName, String propertyValue, int propertyType) {
		AntProperty property= new AntProperty((AntDebugTarget) getDebugTarget(), propertyName, propertyValue);
		switch (propertyType) {
			case DebugMessageIds.PROPERTY_SYSTEM:
				systemProperties.add(property);
				break;
			case DebugMessageIds.PROPERTY_USER:
				userProperties.add(property);
				break;
			case DebugMessageIds.PROPERTY_RUNTIME:
				runtimeProperties.add(property);
				break;
		}
	}

	private void initializePropertyGroups() {
		fUserProperties= new AntProperties(fTarget, DebugModelMessages.getString("AntThread.0")); //$NON-NLS-1$
		fUserProperties.setValue(new AntPropertiesValue(fTarget));
		fSystemProperties= new AntProperties(fTarget, DebugModelMessages.getString("AntThread.1")); //$NON-NLS-1$
		fSystemProperties.setValue(new AntPropertiesValue(fTarget));
		fRuntimeProperties= new AntProperties(fTarget, DebugModelMessages.getString("AntThread.2")); //$NON-NLS-1$
		fRuntimeProperties.setValue(new AntPropertiesValue(fTarget));
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
        return new IVariable[]{fSystemProperties, fUserProperties, fRuntimeProperties};
      }
}