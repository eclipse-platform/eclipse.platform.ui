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

import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IRegisterGroup;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IVariable;

/**
 * Ant stack frame.
 */
public class AntStackFrame extends AntDebugElement implements IStackFrame {
	
	private AntThread fThread;
	private String fName;
	private int fLineNumber;
	private String fFileName;
	private String fFilePath;
	private int fId;
	private List fProperties;
	
	/**
	 * Constructs a stack frame in the given thread with the given id.
	 * 
	 * @param thread
	 * @param id stack frame id (0 is the bottom of the stack)
	 */
	public AntStackFrame(AntThread thread, int id, String name, String filePath, int lineNumber) {
		super((AntDebugTarget) thread.getDebugTarget());
		fId = id;
		fThread = thread;
		fLineNumber= lineNumber;
		fName= name;
		fFilePath= filePath;
		fFileName= new Path(fFilePath).lastSegment();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStackFrame#getThread()
	 */
	public IThread getThread() {
		return fThread;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStackFrame#getVariables()
	 */
	public IVariable[] getVariables() throws DebugException {
	    return new IVariable[0];
	  // List properties= getVariables0();
	  // return (IVariable[])properties.toArray(new IVariable[properties.size()]);
	}

	private synchronized List getVariables0() throws DebugException {
      sendRequest(DebugMessageIds.PROPERTIES);
      try {
           wait();
      } catch (InterruptedException ie) {
      }
      return fProperties;
    }

    /* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStackFrame#hasVariables()
	 */
	public boolean hasVariables() {
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStackFrame#getLineNumber()
	 */
	public int getLineNumber() {
		return fLineNumber;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStackFrame#getCharStart()
	 */
	public int getCharStart() {
		return -1;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStackFrame#getCharEnd()
	 */
	public int getCharEnd() {
		return -1;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStackFrame#getName()
	 */
	public String getName() {
		return fName;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStackFrame#getRegisterGroups()
	 */
	public IRegisterGroup[] getRegisterGroups() {
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStackFrame#hasRegisterGroups()
	 */
	public boolean hasRegisterGroups() {
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStep#canStepInto()
	 */
	public boolean canStepInto() {
		return getThread().canStepInto();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStep#canStepOver()
	 */
	public boolean canStepOver() {
		return getThread().canStepOver();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStep#canStepReturn()
	 */
	public boolean canStepReturn() {
		return getThread().canStepReturn();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStep#isStepping()
	 */
	public boolean isStepping() {
		return getThread().isStepping();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStep#stepInto()
	 */
	public void stepInto() throws DebugException {
		getThread().stepInto();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStep#stepOver()
	 */
	public void stepOver() throws DebugException {
		getThread().stepOver();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStep#stepReturn()
	 */
	public void stepReturn() throws DebugException {
		getThread().stepReturn();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#canResume()
	 */
	public boolean canResume() {
		return getThread().canResume();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#canSuspend()
	 */
	public boolean canSuspend() {
		return getThread().canSuspend();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#isSuspended()
	 */
	public boolean isSuspended() {
		return getThread().isSuspended();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#resume()
	 */
	public void resume() throws DebugException {
		getThread().resume();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#suspend()
	 */
	public void suspend() throws DebugException {
		getThread().suspend();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ITerminate#canTerminate()
	 */
	public boolean canTerminate() {
		return getThread().canTerminate();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ITerminate#isTerminated()
	 */
	public boolean isTerminated() {
		return getThread().isTerminated();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ITerminate#terminate()
	 */
	public void terminate() throws DebugException {
		getThread().terminate();
	}
	
	/**
	 * Returns the name of the buildfile this stack frame is associated
	 * with.
	 * 
	 * @return the name of the buildfile this stack frame is associated
	 * with
	 */
	public String getSourceName() {
		return fFileName;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (obj instanceof AntStackFrame) {
			AntStackFrame sf = (AntStackFrame)obj;
//			if (getSourceName() != null) {
//				return getSourceName().equals(sf.getSourceName()) &&
//				sf.getLineNumber() == getLineNumber() &&
//				sf.fId == fId;
//			}
			return sf.fId == fId;
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		if (getSourceName() != null) {
			//return getSourceName().hashCode() + fId;
		}
		return fId;
	}
	
	/**
	 * Returns this stack frame's unique identifier within its thread
	 * 
	 * @return this stack frame's unique identifier within its thread
	 */
	protected int getIdentifier() {
		return fId;
	}

	protected synchronized void newProperties(String data) {
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
	    			propertyName = datum[++i];
	    			while (propertyName.length() != propertyNameLength) {
	    				propertyName+=DebugMessageIds.MESSAGE_DELIMITER + datum[++i];
	    			}
	    			propertyValueLength= Integer.parseInt(datum[++i]);
	    			propertyValue= datum[++i];
	    			while (propertyValue.length() != propertyValueLength) {
	    				propertyValue+=DebugMessageIds.MESSAGE_DELIMITER + datum[++i];
	    			}
	    			
	    			property= new AntProperty(this, propertyName, propertyValue);
	    			fProperties.add(property);
	    		}
	    	}
	    } finally {
	    	synchronized (this) {
	    		notifyAll();
	    	}
	    }
	}
}