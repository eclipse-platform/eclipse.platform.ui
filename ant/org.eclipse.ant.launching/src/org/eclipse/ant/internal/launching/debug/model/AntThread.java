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
	
	static final IBreakpoint[] NO_BREAKPOINTS = new IBreakpoint[0];
	
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
    
    private Object fPropertiesLock= new Object();
	
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
			if (fFrames.size() == 0) {
				getStackFrames0();
			}
		} 
		
		return (IStackFrame[]) fFrames.toArray(new IStackFrame[fFrames.size()]);
	}
	
	/**
	 * Retrieves the current stack frames in the thread
	 * possibly waiting until the frames are populated
     * 
	 */
	private void getStackFrames0() throws DebugException {
        synchronized (fFrames) {
    		getAntDebugTarget().getStackFrames();
            if (fFrames.size() > 0) {
                //frames set..no need to wait
                return;
            }
            int attempts= 0;
    		try {
                while (fFrames.size() == 0 && !isTerminated()) {
                    fFrames.wait(50);
                    if (attempts == 20 && fFrames.size() == 0 && !isTerminated()) {
                        throwDebugException(DebugModelMessages.AntThread_3);
                    }
                    attempts++;
                }
    		} catch (InterruptedException e) {
    		}
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
	public synchronized IStackFrame getTopStackFrame() throws DebugException {
		if (isSuspended()) {
			if (fFrames.size() == 0) {
				getStackFrames0();
			}
			if (fFrames.size() > 0) {
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
			return NO_BREAKPOINTS;
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
	
	/**
	 * Call-back when the target is resumed
	 * @since 1.0
	 */
	void resumedByTarget() {
		aboutToResume(DebugEvent.CLIENT_REQUEST, false);
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
        setPropertiesValid(false);
	    setStepping(stepping);
	    setBreakpoints(null);
		fireResumeEvent(detail);
    }

    private void setPropertiesValid(boolean valid) {
        if (fUserProperties != null) {
            fUserProperties.setValid(valid);
            fSystemProperties.setValid(valid);
            fRuntimeProperties.setValid(valid);
        }
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

    public void buildStack(String data) { 
        synchronized (fFrames) {
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
            fFrames.notifyAll();
        }
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
    
    public void newProperties(String data) {
        synchronized (fPropertiesLock) {
            try {
                String[] datum= data.split(DebugMessageIds.MESSAGE_DELIMITER);
                if (fUserProperties == null) {
                    initializePropertyGroups();
                }

                List userProperties= ((AntPropertiesValue)fUserProperties.getLastValue()).getProperties();
                List systemProperties= ((AntPropertiesValue)fSystemProperties.getLastValue()).getProperties();
                List runtimeProperties= ((AntPropertiesValue)fRuntimeProperties.getLastValue()).getProperties();
                //0 PROPERTIES message
                //1 propertyName length
                //2 propertyName
                //3 propertyValue length
                //3 propertyValue
                //4 propertyType
                //5 ...
                if (datum.length > 1) { //new properties
                    StringBuffer propertyName;
                    StringBuffer propertyValue;
                    int propertyNameLength;
                    int propertyValueLength;
                    for (int i = 1; i < datum.length; i++) {
                        propertyNameLength= Integer.parseInt(datum[i]);
                        propertyName= new StringBuffer(datum[++i]);
                        while (propertyName.length() != propertyNameLength) {
                            propertyName.append(DebugMessageIds.MESSAGE_DELIMITER);
                            propertyName.append(datum[++i]);
                        }

                        propertyName= getAntDebugTarget().getAntDebugController().unescapeString(propertyName);

                        propertyValueLength= Integer.parseInt(datum[++i]);
                        if (propertyValueLength == 0 && i + 1 == datum.length) { //bug 81299
                            propertyValue= new StringBuffer(""); //$NON-NLS-1$
                        } else {
                            propertyValue= new StringBuffer(datum[++i]);
                        }
                        while (propertyValue.length() != propertyValueLength) {
                            propertyValue.append(DebugMessageIds.MESSAGE_DELIMITER);
                            propertyValue.append(datum[++i]);
                        }

                        propertyValue= getAntDebugTarget().getAntDebugController().unescapeString(propertyValue);

                        int propertyType= Integer.parseInt(datum[++i]);
                        addProperty(userProperties, systemProperties, runtimeProperties, propertyName.toString(), propertyValue.toString(), propertyType);
                    }
                }
            } finally {
                fRefreshProperties= false;
                setPropertiesValid(true);
                //wake up the call from getVariables
                fPropertiesLock.notifyAll();
            }
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
        AntDebugTarget target= getAntDebugTarget();
		fUserProperties= new AntProperties(target, DebugModelMessages.AntThread_0);
		fUserProperties.setValue(new AntPropertiesValue(target));
		fSystemProperties= new AntProperties(target, DebugModelMessages.AntThread_1);
		fSystemProperties.setValue(new AntPropertiesValue(target));
		fRuntimeProperties= new AntProperties(target, DebugModelMessages.AntThread_2);
		fRuntimeProperties.setValue(new AntPropertiesValue(target));
	}
    
    protected IVariable[] getVariables() throws DebugException {
        synchronized (fPropertiesLock) {
            if (fRefreshProperties) {
                getAntDebugTarget().getProperties();
                if (fRefreshProperties) { 
                    //properties have not been set; need to wait
                    try {
                        int attempts= 0;
                        while (fRefreshProperties && !isTerminated()) {
                            fPropertiesLock.wait(50);
                            if (attempts == 20 && fRefreshProperties && !isTerminated()) {
                                throwDebugException(DebugModelMessages.AntThread_4);
                            }
                            attempts++;
                        }
                    } catch (InterruptedException ie) {
                    }
                }
            }
            if (fSystemProperties == null) {
                return new IVariable[0];
            }
            return new IVariable[]{fSystemProperties, fUserProperties, fRuntimeProperties};
        }
    }
}