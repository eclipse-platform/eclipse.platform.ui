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
package org.eclipse.ant.internal.ui.antsupport.logger;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.Vector;

import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.Location;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.Task;
import org.eclipse.ant.internal.ui.debug.IAntDebugController;
import org.eclipse.ant.internal.ui.debug.model.AntDebugTarget;
import org.eclipse.ant.internal.ui.debug.model.AntThread;
import org.eclipse.ant.internal.ui.debug.model.DebugMessageIds;
import org.eclipse.ant.internal.ui.launchConfigurations.AntProcess;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.ILineBreakpoint;
import org.eclipse.debug.core.model.IProcess;

public class AntProcessDebugBuildLogger extends AntProcessBuildLogger implements IAntDebugController {
	
	private boolean fStepOverSuspend= false;
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
	
	private AntDebugTarget fAntDebugTarget;
	
	/* (non-Javadoc)
	 * @see org.apache.tools.ant.BuildListener#buildStarted(org.apache.tools.ant.BuildEvent)
	 */
	public void buildStarted(BuildEvent event) {
		super.buildStarted(event);
		IProcess process= getAntProcess(event.getProject().getUserProperty(AntProcess.ATTR_ANT_PROCESS_ID));
		ILaunch launch= process.getLaunch();
		fAntDebugTarget= new AntDebugTarget(launch, process, this);
		launch.addDebugTarget(fAntDebugTarget);
        
        fAntDebugTarget.buildStarted();
	}

    private void initializeBuildSequenceInformation(BuildEvent event) {
        Project antProject= event.getProject();
        Vector targets= (Vector) antProject.getReference("eclipse.ant.targetVector"); //$NON-NLS-1$
        fTargetToBuildSequence= new HashMap(targets.size());
        Iterator itr= targets.iterator();
        Hashtable allTargets= antProject.getTargets();
        String targetName;
        Vector sortedTargets;
        while (itr.hasNext()) {
            targetName= (String) itr.next();
            sortedTargets= antProject.topoSort(targetName, allTargets);
            fTargetToBuildSequence.put(allTargets.get(targetName), sortedTargets);
        }
        fTargetToExecute= (Target) allTargets.get(targets.remove(0));
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
            initializeBuildSequenceInformation(event);
            fInitialProperties= event.getProject().getProperties();
        }
		super.taskStarted(event);
		fCurrentTask= event.getTask();
		fTasks.push(fCurrentTask);
		waitIfSuspended();
	}
	
	private synchronized void waitIfSuspended() {
	    if (fCurrentTask != null) {
            int detail= -1;
	        boolean shouldSuspend= true;
	        IBreakpoint breakpoint= breakpointAtLineNumber(fCurrentTask.getLocation());
	        if (breakpoint != null) {
                detail= -2;
	            fAntDebugTarget.breakpointHit(breakpoint);
	        } else if (fStepIntoSuspend) {
	            detail= DebugEvent.STEP_END;
	            fStepIntoSuspend= false;               
	        } else if (fStepOverSuspend) {
	            if (fLastTaskFinished == fStepOverTask) {
	                detail= DebugEvent.STEP_END;
	                fStepOverSuspend= false;
	                fStepOverTask= null;
	            } else {
	                shouldSuspend= false;
	            }
	        } else if (fClientSuspend) {
	            detail= DebugEvent.CLIENT_REQUEST;
	            fClientSuspend= false;
	        } else {
	            shouldSuspend= false;
	        }
	        if (shouldSuspend) {
                if (detail != -2) { //not already notified of hitting breakpoint
                    fAntDebugTarget.suspended(detail);
                }
	            try {
	                wait();
	            } catch (InterruptedException e) {
	            }
	        }
	    } else if (fShouldSuspend) {
	        try {
	            fShouldSuspend= false;
	            wait();
	        } catch (InterruptedException e) {
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
		fStepOverSuspend= true;
		fStepOverTask= fCurrentTask;
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
		  StringBuffer propertiesRepresentation= new StringBuffer();
		  propertiesRepresentation.append(DebugMessageIds.PROPERTIES);
		  propertiesRepresentation.append(DebugMessageIds.MESSAGE_DELIMITER);
		  Map currentProperties= null;
		  if (!fTasks.isEmpty()) {
		      currentProperties= ((Task)fTasks.peek()).getProject().getProperties();
		      if (fProperties != null && currentProperties.size() == fProperties.size()) {
		  		//no new properties
		  		((AntThread) fAntDebugTarget.getThreads()[0]).newProperties("no"); //$NON-NLS-1$
		  		return;
		  	}
		  	
              Map currentUserProperties= ((Task)fTasks.peek()).getProject().getUserProperties();
		  	Iterator iter= currentProperties.keySet().iterator();
		  	String propertyName;
		  	String propertyValue;
		  	while (iter.hasNext()) {
		  		propertyName = (String) iter.next();
		  		if (fProperties == null || fProperties.get(propertyName) == null) { //new property
                  propertiesRepresentation.append(propertyName.length());
                    propertiesRepresentation.append(DebugMessageIds.MESSAGE_DELIMITER);
                    propertiesRepresentation.append(propertyName);
                    propertiesRepresentation.append(DebugMessageIds.MESSAGE_DELIMITER);
                    propertyValue= (String) currentProperties.get(propertyName);
                    propertiesRepresentation.append(propertyValue.length());
                    propertiesRepresentation.append(DebugMessageIds.MESSAGE_DELIMITER);
                    propertiesRepresentation.append(propertyValue);
                    propertiesRepresentation.append(DebugMessageIds.MESSAGE_DELIMITER);
                    if (fInitialProperties.get(propertyName) != null) { //properties set before the start of the build
                        if (currentUserProperties.get(propertyName) == null) {
                            propertiesRepresentation.append(DebugMessageIds.PROPERTY_SYSTEM);
                        } else {
                            propertiesRepresentation.append(DebugMessageIds.PROPERTY_USER);
                        }
                    } else if (currentUserProperties.get(propertyName) == null){
                        propertiesRepresentation.append(DebugMessageIds.PROPERTY_RUNTIME);
                    } else {
                        propertiesRepresentation.append(DebugMessageIds.PROPERTY_USER);
                    }
                    propertiesRepresentation.append(DebugMessageIds.MESSAGE_DELIMITER);
                }
            }
        }
		  propertiesRepresentation.deleteCharAt(propertiesRepresentation.length() - 1);
		  fProperties= currentProperties;
		  ((AntThread) fAntDebugTarget.getThreads()[0]).newProperties(propertiesRepresentation.toString());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.debug.IAntDebugController#getStackFrames()
	 */
	public void getStackFrames() {
		StringBuffer stackRepresentation= new StringBuffer();
		stackRepresentation.append(DebugMessageIds.STACK);
		stackRepresentation.append(DebugMessageIds.MESSAGE_DELIMITER);
		
        //task stack
		for (int i = fTasks.size() - 1; i >= 0 ; i--) {
			Task task = (Task) fTasks.get(i);
			stackRepresentation.append(task.getOwningTarget().getName());
			stackRepresentation.append(DebugMessageIds.MESSAGE_DELIMITER);
			stackRepresentation.append(task.getTaskName());
			stackRepresentation.append(DebugMessageIds.MESSAGE_DELIMITER);
			
			Location location= task.getLocation();
			stackRepresentation.append(getFileName(location));
			stackRepresentation.append(DebugMessageIds.MESSAGE_DELIMITER);
			stackRepresentation.append(getLineNumber(location));
			stackRepresentation.append(DebugMessageIds.MESSAGE_DELIMITER);
		}	
        
        //target dependancy stack 
		 if (fTargetToExecute != null) {
		     Vector buildSequence= (Vector) fTargetToBuildSequence.get(fTargetToExecute);
             int startIndex= buildSequence.indexOf(fTargetExecuting) + 1;
             int dependancyStackDepth= buildSequence.indexOf(fTargetToExecute);
           
             Target stackTarget;
             Location location;
             for (int i = startIndex; i <= dependancyStackDepth; i++) {
                stackTarget= (Target) buildSequence.get(i);
                
                stackRepresentation.append(stackTarget.getName());
                stackRepresentation.append(DebugMessageIds.MESSAGE_DELIMITER);
                stackRepresentation.append(""); //$NON-NLS-1$
                stackRepresentation.append(DebugMessageIds.MESSAGE_DELIMITER);
                
                location= stackTarget.getLocation();
                stackRepresentation.append(getFileName(location));
                stackRepresentation.append(DebugMessageIds.MESSAGE_DELIMITER);
                //TODO until targets have locations set properly 
                //stackRepresentation.append(getLineNumber(location));
                stackRepresentation.append(-1);
                stackRepresentation.append(DebugMessageIds.MESSAGE_DELIMITER);
            }
         }
		 ((AntThread) fAntDebugTarget.getThreads()[0]).buildStack(stackRepresentation.toString());
	}
    
    private IBreakpoint breakpointAtLineNumber(Location location) {
        if (fBreakpoints == null) {
            return null;
        }
        int lineNumber= getLineNumber(location);
        File locationFile= new File(getFileName(location));
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
    
    private int getLineNumber(Location location) {
		try { //succeeds with Ant newer than 1.6
			return location.getLineNumber();
		} catch (NoSuchMethodError e) {
			//Ant before 1.6
			String locationString= location.toString();
			if (locationString.length() == 0) {
				return 0;
			}
			//filename: lineNumber: ("c:\buildfile.xml: 12: ")
			int lastIndex= locationString.lastIndexOf(':');
			int index =locationString.lastIndexOf(':', lastIndex - 1);
			if (index != -1) {
				try {
					return Integer.parseInt(locationString.substring(index+1, lastIndex));
				} catch (NumberFormatException nfe) {
					return 0;
				}
			}
			return 0;
		}
	}
	
	private String getFileName(Location location) {
		try {//succeeds with Ant newer than 1.6
			return location.getFileName();
		} catch (NoSuchMethodError e) {
			//Ant before 1.6
			String locationString= location.toString();
			if (locationString.length() == 0) {
				return null;
			}
			//filename: lineNumber: ("c:\buildfile.xml: 12: ")			
			int lastIndex= locationString.lastIndexOf(':');
			int index =locationString.lastIndexOf(':', lastIndex-1);
			if (index == -1) {
				index= lastIndex; //only the filename is known
			}
			if (index != -1) {
				//remove file:
				return locationString.substring(5, index);
			}
			return null;
		}
	}
    
    /* (non-Javadoc)
     * @see org.apache.tools.ant.BuildListener#targetStarted(org.apache.tools.ant.BuildEvent)
     */
    public void targetStarted(BuildEvent event) {
        if (fInitialProperties == null) {
            initializeBuildSequenceInformation(event);
            fInitialProperties= event.getProject().getProperties();
        }
        super.targetStarted(event);
        fTargetExecuting= event.getTarget();
        if (event.getTarget().getName().equals(fTargetToExecute)) {
            //the dependancies of the target to execute have been met
            //prepare for the next target
            Vector targets= (Vector) event.getProject().getReference("eclipse.ant.targetVector"); //$NON-NLS-1$
            if (!targets.isEmpty()) {
                fTargetToExecute= (Target) event.getProject().getTargets().get(targets.remove(0));
            }
        }
    }
}