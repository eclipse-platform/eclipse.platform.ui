/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ant.internal.ui.antsupport.logger;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.Vector;

import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.Location;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.Task;
import org.eclipse.ant.internal.ui.antsupport.logger.util.AntDebugUtil;
import org.eclipse.ant.internal.ui.debug.IAntDebugController;
import org.eclipse.ant.internal.ui.debug.model.AntDebugTarget;
import org.eclipse.ant.internal.ui.debug.model.AntThread;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.ILineBreakpoint;
import org.eclipse.debug.core.model.IProcess;

public class AntProcessDebugBuildLogger extends AntProcessBuildLogger implements IAntDebugController {
	
	private boolean fStepIntoSuspend= false;
	private boolean fClientSuspend= false;
	private boolean fShouldSuspend= false;
	
	private Stack fTasks= new Stack();
	private Task fCurrentTask;
	private Task fStepOverTask;
	private Task fLastTaskFinished;
	
	private List fBreakpoints= null;
    
	//properties set before execution
    private Map fInitialProperties= null;
	private Map fProperties= null;
    
    private Map fTargetToBuildSequence= null;
    private Target fTargetToExecute= null;
    private Target fTargetExecuting= null;
	private boolean fConsiderTargetBreakpoints= false;
	
	private AntDebugTarget fAntDebugTarget;
	
	/* (non-Javadoc)
	 * @see org.apache.tools.ant.BuildListener#buildStarted(org.apache.tools.ant.BuildEvent)
	 */
	public void buildStarted(BuildEvent event) {
		super.buildStarted(event);
		IProcess process= getAntProcess(fProcessId);
		ILaunch launch= process.getLaunch();
		fAntDebugTarget= new AntDebugTarget(launch, process, this);
		launch.addDebugTarget(fAntDebugTarget);
        
        fAntDebugTarget.buildStarted();
	}

    private void initializeBuildSequenceInformation(BuildEvent event) {
        fTargetToBuildSequence= new HashMap();
        fTargetToExecute= AntDebugUtil.initializeBuildSequenceInformation(event, fTargetToBuildSequence);
    }
	
	/* (non-Javadoc)
	 * @see org.apache.tools.ant.BuildListener#taskFinished(org.apache.tools.ant.BuildEvent)
	 */
	public void taskFinished(BuildEvent event) {
		super.taskFinished(event);
		fLastTaskFinished= (Task)fTasks.pop();
		fCurrentTask= null;
		waitIfSuspended();
	}
	
	/* (non-Javadoc)
	 * @see org.apache.tools.ant.BuildListener#taskStarted(org.apache.tools.ant.BuildEvent)
	 */
	public void taskStarted(BuildEvent event) {
        if (fInitialProperties == null) {//implicit or top level target does not fire targetStarted()
            fInitialProperties= event.getProject().getProperties();
        }
		super.taskStarted(event);
		fConsiderTargetBreakpoints= false;
		fCurrentTask= event.getTask();
		fTasks.push(fCurrentTask);
		waitIfSuspended();
	}
	
	private synchronized void waitIfSuspended() {
		IBreakpoint breakpoint= breakpointAtLineNumber(getBreakpointLocation());
		if (breakpoint != null) {
			 fAntDebugTarget.breakpointHit(breakpoint);
			 try {
				 wait();
			 } catch (InterruptedException e) {
			 }
		} else if (fCurrentTask != null) {
			int detail= -1;
	        boolean shouldSuspend= true;
	        if (fStepIntoSuspend) {
	            detail= DebugEvent.STEP_END;
	            fStepIntoSuspend= false;               
	        } else if ((fLastTaskFinished != null && fLastTaskFinished == fStepOverTask) || fShouldSuspend) {
				detail= DebugEvent.STEP_END;
				fShouldSuspend= false;
				fStepOverTask= null;
	        } else if (fClientSuspend) {
	            detail= DebugEvent.CLIENT_REQUEST;
	            fClientSuspend= false;
	        } else {
	            shouldSuspend= false;
	        }
	        if (shouldSuspend) {
                fAntDebugTarget.suspended(detail);
	            try {
	                wait();
	            } catch (InterruptedException e) {
	            }
	        }
	    }
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.debug.IAntDebugController#resume()
	 */
	public synchronized void resume() {
        notifyAll();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.debug.IAntDebugController#suspend()
	 */
	public synchronized void suspend() {
		fClientSuspend= true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.debug.IAntDebugController#stepInto()
	 */
	public synchronized void stepInto() {
		fStepIntoSuspend= true;
		notifyAll();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.debug.IAntDebugController#stepOver()
	 */
	public synchronized void stepOver() {
		fStepOverTask= fCurrentTask;
		if (fCurrentTask == null) {
			//stepping over target breakpoint
			fShouldSuspend= true;
		}
		notifyAll();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.debug.IAntDebugController#handleBreakpoint(org.eclipse.debug.core.model.IBreakpoint, boolean)
	 */
	public void handleBreakpoint(IBreakpoint breakpoint, boolean added) {
		if (added) {
			if (fBreakpoints == null) {
				fBreakpoints= new ArrayList();
			}
			if (!fBreakpoints.contains(breakpoint)) {
				fBreakpoints.add(breakpoint);
			}
		} else {
			fBreakpoints.remove(breakpoint);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.debug.IAntDebugController#getProperties()
	 */
	public void getProperties() {
		if (!fAntDebugTarget.isSuspended()) {
			return;
		}
	    StringBuffer propertiesRepresentation= new StringBuffer();
	    if (!fTasks.isEmpty()) {
	        AntDebugUtil.marshallProperties(propertiesRepresentation, ((Task)fTasks.peek()).getProject(), fInitialProperties, fProperties, true);
	        fProperties= ((Task)fTasks.peek()).getProject().getProperties();
	    }
		if (fAntDebugTarget.getThreads().length > 0) {
			((AntThread) fAntDebugTarget.getThreads()[0]).newProperties(propertiesRepresentation.toString());
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.debug.IAntDebugController#getStackFrames()
	 */
	public void getStackFrames() {
		StringBuffer stackRepresentation= new StringBuffer();
		AntDebugUtil.marshalStack(stackRepresentation, fTasks, fTargetToExecute, fTargetExecuting, fTargetToBuildSequence);
		((AntThread) fAntDebugTarget.getThreads()[0]).buildStack(stackRepresentation.toString());
	}
    
    private IBreakpoint breakpointAtLineNumber(Location location) {
        if (fBreakpoints == null || location == null || location == Location.UNKNOWN_LOCATION) {
            return null;
        }
        int lineNumber= AntDebugUtil.getLineNumber(location);
        File locationFile= new File(AntDebugUtil.getFileName(location));
        for (int i = 0; i < fBreakpoints.size(); i++) {
            ILineBreakpoint breakpoint = (ILineBreakpoint) fBreakpoints.get(i);
            int breakpointLineNumber;
            try {
            	if (!breakpoint.isEnabled()) {
                	continue;
                }
            	breakpointLineNumber = breakpoint.getLineNumber();
            } catch (CoreException e) {
               return null;
            }
            IFile resource= (IFile) breakpoint.getMarker().getResource();
            if (breakpointLineNumber == lineNumber && resource.getLocation().toFile().equals(locationFile)) {
                return breakpoint;
            }
        }
        return null;
    }
    
    /* (non-Javadoc)
     * @see org.apache.tools.ant.BuildListener#targetStarted(org.apache.tools.ant.BuildEvent)
     */
    public void targetStarted(BuildEvent event) {
        if (fInitialProperties == null) {
            fInitialProperties= event.getProject().getProperties();
        }
        if (fTargetToBuildSequence == null) {
        	initializeBuildSequenceInformation(event);
        }
       
        fTargetExecuting= event.getTarget();
        if (event.getTarget().equals(fTargetToExecute)) {
            //the dependancies of the target to execute have been met
            //prepare for the next target
            Vector targets= (Vector) event.getProject().getReference("eclipse.ant.targetVector"); //$NON-NLS-1$
            if (!targets.isEmpty()) {
                fTargetToExecute= (Target) event.getProject().getTargets().get(targets.remove(0));
            } else {
                fTargetToExecute= null;
            }
        }
		fConsiderTargetBreakpoints= true;
		waitIfSuspended();
		super.targetStarted(event);
    }
	
	/* (non-Javadoc)
	 * @see org.apache.tools.ant.BuildListener#targetFinished(org.apache.tools.ant.BuildEvent)
	 */
	public void targetFinished(BuildEvent event) {
		super.targetFinished(event);
		fTargetExecuting= null;
	}	
	
	private Location getBreakpointLocation() {
		if (fCurrentTask != null) {
			return fCurrentTask.getLocation();
		}
		if (fConsiderTargetBreakpoints && fTargetExecuting != null) {
			return AntDebugUtil.getLocation(fTargetExecuting);
		}
		return null;
	}
}
