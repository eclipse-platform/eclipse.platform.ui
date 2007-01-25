/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.core.model;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;

/**
 * Implementation of common function for debug elements.
 * <p>
 * Clients may subclass this class.
 * </p>
 * @since 3.1
 */
public abstract class DebugElement extends PlatformObject implements IDebugElement {
    
    private IDebugTarget fTarget;

    /**
     * Constructs a debug element referring to an artifact in the given
     * debug target.
     * 
     * @param target debug target containing this element
     */
    public DebugElement(IDebugTarget target) {
        fTarget = target;
    }

    /* (non-Javadoc)
     * 
     * Debug target implementation should override this method.
     * 
     * @see org.eclipse.debug.core.model.IDebugElement#getDebugTarget()
     */
    public IDebugTarget getDebugTarget() {
        return fTarget;
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.core.model.IDebugElement#getLaunch()
     */
    public ILaunch getLaunch() {
        return getDebugTarget().getLaunch();
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    public Object getAdapter(Class adapter) {
		if (adapter == IDebugElement.class) {
			return this;
		}
		
		// a debug target may not implement IStepFilters
		if (adapter == IStepFilters.class) {
			if (getDebugTarget() instanceof IStepFilters)
				return getDebugTarget();
		}
		if (adapter == IDebugTarget.class) {
			return getDebugTarget();
		}
		if (adapter == ILaunch.class) {
		    return getLaunch();
		}
		if (adapter == IProcess.class) {
		    return getDebugTarget().getProcess();
		}
		//CONTEXTLAUNCHING
		if(adapter == ILaunchConfiguration.class) {
			return getLaunch().getLaunchConfiguration();
		}
		return super.getAdapter(adapter);
    }

	/**
	 * Fires a debug event.
	 * 
	 * @param event debug event to fire
	 */
	public void fireEvent(DebugEvent event) {
		DebugPlugin.getDefault().fireDebugEventSet(new DebugEvent[] {event});
	}    

	/**
	 * Fires a change event for this debug element
	 * with the specified detail code.
	 * 
	 * @param detail detail code for the change event,
	 *  such as <code>DebugEvent.STATE</code> or <code>DebugEvent.CONTENT</code>
	 */
	public void fireChangeEvent(int detail) {
		fireEvent(new DebugEvent(this, DebugEvent.CHANGE, detail));
	}
	
	/**
	 * Fires a creation event for this debug element.
	 */
    public void fireCreationEvent() {
		fireEvent(new DebugEvent(this, DebugEvent.CREATE));
	}	
	
	/**
	 * Fires a resume for this debug element with
	 * the specified detail code.
	 * 
	 * @param detail detail code for the resume event, such 
	 *  as <code>DebugEvent.STEP_OVER</code>
	 */
    public void fireResumeEvent(int detail) {
		fireEvent(new DebugEvent(this, DebugEvent.RESUME, detail));
	}
	
	/**
	 * Fires a suspend event for this debug element with
	 * the specified detail code.
	 * 
	 * @param detail detail code for the suspend event, such
	 *  as <code>DebugEvent.BREAKPOINT</code>
	 */
    public void fireSuspendEvent(int detail) {
		fireEvent(new DebugEvent(this, DebugEvent.SUSPEND, detail));
	}	
	
	/**
	 * Fires a terminate event for this debug element.
	 */
    public void fireTerminateEvent() {
		fireEvent(new DebugEvent(this, DebugEvent.TERMINATE));
	}	
	
	/**
	 * Throws a debug exception with a status code of <code>TARGET_REQUEST_FAILED</code>.
	 * 
	 * @param message exception message
	 * @param e underlying exception or <code>null</code>
	 * @throws DebugException
	 */
	protected void requestFailed(String message, Throwable e) throws DebugException {
		throw new DebugException(new Status(IStatus.ERROR, DebugPlugin.getUniqueIdentifier(), 
				DebugException.TARGET_REQUEST_FAILED, message, e));
	}
	
	/**
	 * Throws a debug exception with a status code of <code>NOT_SUPPORTED</code>.
	 * 
	 * @param message exception message
	 * @param e underlying exception or <code>null</code>
	 * @throws DebugException
	 */
	protected void notSupported(String message, Throwable e) throws DebugException {
		throw new DebugException(new Status(IStatus.ERROR, DebugPlugin.getUniqueIdentifier(), 
				DebugException.NOT_SUPPORTED, message, e));
	}
}
