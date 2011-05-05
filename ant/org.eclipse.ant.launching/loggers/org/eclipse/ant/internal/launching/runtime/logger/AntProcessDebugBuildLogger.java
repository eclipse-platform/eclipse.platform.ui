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
package org.eclipse.ant.internal.launching.runtime.logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.Location;
import org.eclipse.ant.internal.launching.debug.AntDebugState;
import org.eclipse.ant.internal.launching.debug.IAntDebugController;
import org.eclipse.ant.internal.launching.debug.IDebugBuildLogger;
import org.eclipse.ant.internal.launching.debug.model.AntDebugTarget;
import org.eclipse.ant.internal.launching.debug.model.AntThread;
import org.eclipse.ant.internal.launching.launchConfigurations.AntProcess;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.ILineBreakpoint;
import org.eclipse.debug.core.model.IProcess;

public class AntProcessDebugBuildLogger extends AntProcessBuildLogger implements IAntDebugController, IDebugBuildLogger {
	
	private AntDebugState fDebugState= null;
	
	private List fBreakpoints= null;
    
	private AntDebugTarget fAntDebugTarget;
	private boolean fResumed= false;
	
	/* (non-Javadoc)
	 * @see org.apache.tools.ant.BuildListener#buildStarted(org.apache.tools.ant.BuildEvent)
	 */
	public void buildStarted(BuildEvent event) {
		fDebugState= new AntDebugState(this);
		super.buildStarted(event);
		IProcess process= getAntProcess(fProcessId);
		ILaunch launch= process.getLaunch();
		fAntDebugTarget= new AntDebugTarget(launch, process, this);
		launch.addDebugTarget(fAntDebugTarget);
        
        fAntDebugTarget.buildStarted();
        fDebugState.buildStarted();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.launching.runtime.logger.AntProcessBuildLogger#buildFinished(org.apache.tools.ant.BuildEvent)
	 */
	public void buildFinished(BuildEvent event) {
		super.buildFinished(event);
		cleanup();
	}
	
	/**
	 * Cleans up all held memory.
	 * <br><br>
	 * Called from {@link #buildFinished(BuildEvent)} and {@link #terminate()}
	 * @since 1.0.1
	 */
	void cleanup() {
		if(fAntDebugTarget != null) {
			IProcess process= getAntProcess(fProcessId);
			if(process != null) {
				ILaunch launch= process.getLaunch();
				launch.removeDebugTarget(fAntDebugTarget);
			}
		}
		if(fDebugState != null) {
			fDebugState.buildFinished();
		}
		if(fBreakpoints != null) {
			fBreakpoints.clear();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.apache.tools.ant.BuildListener#taskFinished(org.apache.tools.ant.BuildEvent)
	 */
	public void taskFinished(BuildEvent event) {
		super.taskFinished(event);
		fDebugState.taskFinished();
	}
	
	/* (non-Javadoc)
	 * @see org.apache.tools.ant.BuildListener#taskStarted(org.apache.tools.ant.BuildEvent)
	 */
	public void taskStarted(BuildEvent event) {
        super.taskStarted(event);
		fDebugState.taskStarted(event);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.antsupport.logger.util.IDebugBuildLogger#waitIfSuspended()
	 */
	public synchronized void waitIfSuspended() {
		fResumed= false;
		IBreakpoint breakpoint= breakpointAtLineNumber(fDebugState.getBreakpointLocation());
		if (breakpoint != null) {
			 fAntDebugTarget.breakpointHit(breakpoint);
			 try {
				 while (!fResumed) {
					 wait(500);
					 checkCancelled();
				 }
			 } catch (InterruptedException e) {
			 }
		} else if (fDebugState.getCurrentTask() != null) {
			int detail= -1;
	        boolean shouldSuspend= true;
	        if (fDebugState.isStepIntoSuspend()) {
	            detail= DebugEvent.STEP_END;
	            fDebugState.setStepIntoSuspend(false);               
	        } else if ((fDebugState.getLastTaskFinished() != null && fDebugState.getLastTaskFinished() == fDebugState.getStepOverTask()) || fDebugState.shouldSuspend()) {
				detail= DebugEvent.STEP_END;
				fDebugState.setShouldSuspend(false);
				fDebugState.setStepOverTask(null);
	        } else if (fDebugState.isClientSuspend()) {
	            detail= DebugEvent.CLIENT_REQUEST;
	            fDebugState.setClientSuspend(false);
	        } else {
	            shouldSuspend= false;
	        }
	        if (shouldSuspend) {
                fAntDebugTarget.suspended(detail);
	            try {
	            	while (!fResumed) {
	            		wait(500);
	            		checkCancelled();
	            	}
	            } catch (InterruptedException e) {
	            }
	        }
	    }
	}

	private void checkCancelled() {
		AntProcess process= getAntProcess(fProcessId);
		if (process != null && process.isCanceled()) {
			throw new OperationCanceledException(RuntimeMessages.AntProcessDebugBuildLogger_1);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.debug.IAntDebugController#resume()
	 */
	public synchronized void resume() {
		fResumed= true;
        notifyAll();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.debug.IAntDebugController#suspend()
	 */
	public synchronized void suspend() {
		fDebugState.setClientSuspend(true);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.debug.IAntDebugController#stepInto()
	 */
	public synchronized void stepInto() {
		fDebugState.setStepIntoSuspend(true);
		fResumed= true;
		notifyAll();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.launching.debug.IAntDebugController#terminate()
	 */
	public void terminate() {
		cleanup();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.debug.IAntDebugController#stepOver()
	 */
	public synchronized void stepOver() {
		fResumed= true;
		fDebugState.stepOver();
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
			if (fBreakpoints != null) {
				fBreakpoints.remove(breakpoint);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.debug.IAntDebugController#getProperties()
	 */
	public void getProperties() {
		if (fAntDebugTarget == null || !fAntDebugTarget.isSuspended()) {
			return;
		}
	    StringBuffer propertiesRepresentation= new StringBuffer();
		fDebugState.marshallProperties(propertiesRepresentation, false);
		if (fAntDebugTarget.getThreads().length > 0) {
			((AntThread) fAntDebugTarget.getThreads()[0]).newProperties(propertiesRepresentation.toString());
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.debug.IAntDebugController#getStackFrames()
	 */
	public void getStackFrames() {
		StringBuffer stackRepresentation= new StringBuffer();
		fDebugState.marshalStack(stackRepresentation);
		((AntThread) fAntDebugTarget.getThreads()[0]).buildStack(stackRepresentation.toString());
	}
    
    private IBreakpoint breakpointAtLineNumber(Location location) {
        if (fBreakpoints == null || location == null || location == Location.UNKNOWN_LOCATION) {
            return null;
        }
        int lineNumber= fDebugState.getLineNumber(location);
        File locationFile= new File(fDebugState.getFileName(location));
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
		fDebugState.targetStarted(event);
		waitIfSuspended();
		super.targetStarted(event);
    }
	
	/* (non-Javadoc)
	 * @see org.apache.tools.ant.BuildListener#targetFinished(org.apache.tools.ant.BuildEvent)
	 */
	public void targetFinished(BuildEvent event) {
		super.targetFinished(event);
		if(fDebugState != null) {
			fDebugState.setTargetExecuting(null);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.debug.IAntDebugController#unescapeString(java.lang.StringBuffer)
	 */
	public StringBuffer unescapeString(StringBuffer propertyValue) {
		return propertyValue;
	}
}
