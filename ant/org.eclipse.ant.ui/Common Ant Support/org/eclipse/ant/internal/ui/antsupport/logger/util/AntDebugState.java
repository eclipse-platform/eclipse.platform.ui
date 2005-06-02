/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ant.internal.ui.antsupport.logger.util;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;
import java.util.Vector;

import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.Location;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.MacroInstance;

public class AntDebugState implements IDebugBuildLogger {
	private IDebugBuildLogger fLogger;
	private Stack fTasks= new Stack();
	private Task fCurrentTask;
	private Task fStepOverTask;
	private Task fStepIntoTask;
	private Task fLastTaskFinished;
    
	//properties set before execution
    private Map fInitialProperties= null;
	private Map fProperties= null;
    
    private Map fTargetToBuildSequence= null;
    private Map fProjectToTargetNames= null;
    private Stack fTargetsToExecute= new Stack();
    private Stack fTargetsExecuting= new Stack();
	
	private boolean fConsiderTargetBreakpoints= false;
	private boolean fShouldSuspend;
	private boolean fClientSuspend= false;
	private boolean fStepIntoSuspend= false;
	private boolean fIsAfterTaskEvent= false;
	
	public AntDebugState(IDebugBuildLogger logger) {
		fLogger= logger;
	}

	public void waitIfSuspended() {
		fLogger.waitIfSuspended();
	}

	public Task getLastTaskFinished() {
		return fLastTaskFinished;
	}

	private void setLastTaskFinished(Task lastTaskFinished) {
		fLastTaskFinished= lastTaskFinished;

	}

	public Task getCurrentTask() {
		return fCurrentTask;
	}

	public void setCurrentTask(Task currentTask) {
		fCurrentTask= currentTask;

	}

	private Map getInitialProperties() {
		return fInitialProperties;
	}

	public Task getStepOverTask() {
		return fStepOverTask;
	}

	public void setStepOverTask(Task stepOverTask) {
		fStepOverTask= stepOverTask;

	}

	private boolean considerTargetBreakpoints() {
		return fConsiderTargetBreakpoints;
	}

	private void setConsiderTargetBreakpoints(boolean considerTargetBreakpoints) {
		fConsiderTargetBreakpoints= considerTargetBreakpoints;
	}

	private Stack getTasks() {
		return fTasks;
	}

	public void setShouldSuspend(boolean shouldSuspend) {
		fShouldSuspend= shouldSuspend;
	}

	public boolean shouldSuspend() {
		return fShouldSuspend;
	}

	private Map getTargetToBuildSequence() {
		return fTargetToBuildSequence;
	}

	private void setTargetToBuildSequence(Map sequence) {
		fTargetToBuildSequence= sequence;
	}

	public void setTargetToExecute(Target target) {
        if (target == null) {
            fTargetsToExecute.pop();
        } else {
            fTargetsToExecute.push(target);
        }
	}

	public void setTargetExecuting(Target target) {
        if (target == null) {
            fTargetsExecuting.pop();
        } else {
            fTargetsExecuting.push(target);
        }
	}

	private Target getTargetToExecute() {
	    if (fTargetsToExecute.isEmpty()) {
            return null;
        }
		return (Target) fTargetsToExecute.peek();
	}
	
	private Target getTargetExecuting() {
        if (fTargetsExecuting.isEmpty()) {
            return null;
        }
		return (Target) fTargetsExecuting.peek();
	}

	public boolean isStepIntoSuspend() {
		return isAfterTaskEvent() && fStepIntoSuspend;
	}

	public void setStepIntoSuspend(boolean stepIntoSuspend) {
		fStepIntoSuspend = stepIntoSuspend;
	}

	public boolean isClientSuspend() {
		return fClientSuspend;
	}

	public void setClientSuspend(boolean clientSuspend) {
		fClientSuspend = clientSuspend;
	}

	public Task getStepIntoTask() {
		return fStepIntoTask;
	}

	public void setStepIntoTask(Task stepIntoTask) {
		fStepIntoTask = stepIntoTask;
	}
	
	public void resume() {
		fLogger.notifyAll();
	}

	public Map getProperties() {
		return fProperties;
	}
	
	public Location getBreakpointLocation() {
		if (isAfterTaskEvent() && getCurrentTask() != null) {
			return getCurrentTask().getLocation();
		}
		if (considerTargetBreakpoints()) {
	        Target targetExecuting= getTargetExecuting();
	        if (targetExecuting != null) {
                return getLocation(targetExecuting);      
            }
		}
		return null;
	}

	private boolean isAfterTaskEvent() {
		return fIsAfterTaskEvent;
	}

	private void setAfterTaskEvent(boolean isAfterTaskEvent) {
		fIsAfterTaskEvent = isAfterTaskEvent;
	}
	
	public void taskStarted(BuildEvent event) {
		setAfterTaskEvent(true);
		if (getInitialProperties() == null) {//implicit or top level target does not fire targetStarted()
			fInitialProperties= event.getProject().getProperties();
		}
		
		setCurrentTask(event.getTask());
		setConsiderTargetBreakpoints(false);
		getTasks().push(getCurrentTask());
		waitIfSuspended();
	}
	

    public void taskFinished() {
    	Task lastTask= (Task)getTasks().pop();
        setLastTaskFinished(lastTask);
        setCurrentTask(null);
        String taskName= lastTask.getTaskName();
       
        if (getStepOverTask() != null) {
        	if ("antcall".equals(taskName) || "ant".equals(taskName)) { //$NON-NLS-1$ //$NON-NLS-2$
        		setShouldSuspend(true);
        	} else if (lastTask.getRuntimeConfigurableWrapper().getProxy() instanceof MacroInstance) {
        		setShouldSuspend(true);
        	}
        }
        waitIfSuspended();
    }

    public void stepOver() {
       setStepOverTask(getCurrentTask());
        if (getCurrentTask() == null) {
            //stepping over target breakpoint
           setShouldSuspend(true);
        }
        resume();
    }

    public void targetStarted(BuildEvent event) {
		setAfterTaskEvent(false);
        Project eventProject = event.getProject();
        if (getInitialProperties() == null) {
            fInitialProperties= eventProject.getProperties();
        }
        if (fProjectToTargetNames.get(eventProject) == null) {
            Object ref= eventProject.getReference("eclipse.ant.targetVector"); //$NON-NLS-1$
            if (ref != null) {
                fProjectToTargetNames.put(eventProject, ref);
                setTargetToExecute(initializeBuildSequenceInformation(event, getTargetToBuildSequence()));
            }
        }
        
        setTargetExecuting(event.getTarget());
        if (event.getTarget().equals(getTargetToExecute())) {
            //the dependancies of the target to execute have been met
            //prepare for the next target
            Vector targets= (Vector) fProjectToTargetNames.get(eventProject);
            if (!targets.isEmpty()) {
                setTargetToExecute((Target) eventProject.getTargets().get(targets.remove(0)));
            } else {
                setTargetToExecute(null);
            }
        }
        setConsiderTargetBreakpoints(true);
    }

	public int getLineNumber(Location location) {
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

	public static Location getLocation(Target target) {
	    try {//succeeds with Ant newer than 1.6.2
	        return target.getLocation();
	    } catch (NoSuchMethodError e) {
	        return Location.UNKNOWN_LOCATION;
	    }
	}

	public String getFileName(Location location) {
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
	        //bug 84403
	            //if (locationString.startsWith("file:")) { //$NON-NLS-1$
	              //  return FileUtils.newFileUtils().fromURI(locationString);
	            //}
	            //remove file:
	            return locationString.substring(5, index);
	        }
	        return null;
	    }
	}

	private void appendToStack(StringBuffer stackRepresentation, String targetName, String taskName, Location location) {
	    stackRepresentation.append(targetName);
	    stackRepresentation.append(DebugMessageIds.MESSAGE_DELIMITER);
	    stackRepresentation.append(taskName);
	    stackRepresentation.append(DebugMessageIds.MESSAGE_DELIMITER);
	    
	    stackRepresentation.append(getFileName(location));
	    stackRepresentation.append(DebugMessageIds.MESSAGE_DELIMITER);
	    stackRepresentation.append(getLineNumber(location));
	    stackRepresentation.append(DebugMessageIds.MESSAGE_DELIMITER);
	}

	public void marshalStack(StringBuffer stackRepresentation) {
		Stack tasks= getTasks();
		
	    stackRepresentation.append(DebugMessageIds.STACK);
	    stackRepresentation.append(DebugMessageIds.MESSAGE_DELIMITER);
	    
		Target targetToExecute= getTargetToExecute();
		Target targetExecuting= getTargetExecuting();
        Project projectExecuting= targetExecuting.getProject();
        
		if (!isAfterTaskEvent()) {
			appendToStack(stackRepresentation, targetExecuting.getName(), "", getLocation(targetExecuting)); //$NON-NLS-1$
		} 
		for (int i = tasks.size() - 1; i >= 0 ; i--) {
			Task task= (Task) tasks.get(i);
            if (task.getProject() == projectExecuting) {
                appendToStack(stackRepresentation, task.getOwningTarget().getName(), task.getTaskName(), task.getLocation());
            } else {
                //sub build target dependancies
                String targetName= task.getOwningTarget().getName();
                if (targetName != null && targetName.length() != 0) { //skip for implicit target
                    Iterator itr= fTargetsToExecute.iterator();
                    while (itr.hasNext()) {
                        Target target = (Target) itr.next();
                        if (target.getProject() != projectExecuting) {
                            continue;
                        }
                        marshalTargetDependancyStack(stackRepresentation, target, targetExecuting);
                    }
                }
                projectExecuting= task.getProject();
                targetExecuting= task.getOwningTarget();
                appendToStack(stackRepresentation, targetExecuting.getName(), task.getTaskName(), task.getLocation());
            }
		}

	    //target dependancy stack 
	    marshalTargetDependancyStack(stackRepresentation, targetToExecute, targetExecuting);
	}

    private void marshalTargetDependancyStack(StringBuffer stackRepresentation, Target targetToExecute, Target targetExecuting) {
        if (targetToExecute != null) {
	     	Vector buildSequence= (Vector) getTargetToBuildSequence().get(targetToExecute);
	     	int startIndex= buildSequence.indexOf(targetExecuting) + 1;
	     	int dependancyStackDepth= buildSequence.indexOf(targetToExecute);
	     	
	     	Target stackTarget;
	     	for (int i = startIndex; i <= dependancyStackDepth; i++) {
	     		stackTarget= (Target) buildSequence.get(i);
	            if (stackTarget.dependsOn(targetExecuting.getName())) {
	     		    appendToStack(stackRepresentation, stackTarget.getName(), "", getLocation(stackTarget)); //$NON-NLS-1$
	            }
	     	}
	     }
    }

	public void marshallProperties(StringBuffer propertiesRepresentation, boolean escapeLineSep) {
		if (getTasks().isEmpty()) {
			return;
		}
	    propertiesRepresentation.append(DebugMessageIds.PROPERTIES);
	    propertiesRepresentation.append(DebugMessageIds.MESSAGE_DELIMITER);
		
		Project project= ((Task)getTasks().peek()).getProject();
		Map lastProperties= getProperties(); 
		
	    Map currentProperties= project.getProperties();
	    if (lastProperties != null && currentProperties.size() == lastProperties.size()) {
	        //no new properties
	        return;
	    }
	    
		Map initialProperties= getInitialProperties();
	    Map currentUserProperties= project.getUserProperties();
	    Iterator iter= currentProperties.keySet().iterator();
	    String propertyName;
		String originalPropertyName;
	    String propertyValue;
	    while (iter.hasNext()) {
	        propertyName = (String) iter.next();
			originalPropertyName= propertyName;
	        if (lastProperties == null || lastProperties.get(propertyName) == null) { //new property
				if (escapeLineSep) {
					propertyName= escapeLineSeparator(propertyName);
				}
	            propertiesRepresentation.append(propertyName.length());
	            propertiesRepresentation.append(DebugMessageIds.MESSAGE_DELIMITER);
	            propertiesRepresentation.append(propertyName);
	            propertiesRepresentation.append(DebugMessageIds.MESSAGE_DELIMITER);
	            propertyValue= (String) currentProperties.get(originalPropertyName);
				if (escapeLineSep) {
					propertyValue= escapeLineSeparator(propertyValue);
				}
	            propertiesRepresentation.append(propertyValue.length());
	            propertiesRepresentation.append(DebugMessageIds.MESSAGE_DELIMITER);
				propertiesRepresentation.append(propertyValue);
	            propertiesRepresentation.append(DebugMessageIds.MESSAGE_DELIMITER);
	            propertiesRepresentation.append(getPropertyType(initialProperties, currentUserProperties, originalPropertyName));
	            propertiesRepresentation.append(DebugMessageIds.MESSAGE_DELIMITER);
	        }
	    }
	    
	    propertiesRepresentation.deleteCharAt(propertiesRepresentation.length() - 1);
		fProperties= currentProperties;
	}

	private int getPropertyType(Map initialProperties, Map currentUserProperties, String propertyName) {
		if (initialProperties.get(propertyName) != null) { //properties set before the start of the build
		    if (currentUserProperties.get(propertyName) == null) {
		        return DebugMessageIds.PROPERTY_SYSTEM;
		    } 
			return DebugMessageIds.PROPERTY_USER;
		} else if (currentUserProperties.get(propertyName) == null){
		    return DebugMessageIds.PROPERTY_RUNTIME;
		} else {
		    return DebugMessageIds.PROPERTY_USER;
		}
	}

	private String escapeLineSeparator(String stringToEscape) {
		if (!(stringToEscape.indexOf('\r') != -1 || stringToEscape.indexOf('\n') != -1 || stringToEscape.indexOf("\\r") != -1 || stringToEscape.indexOf("\\n") != -1)) { //$NON-NLS-1$ //$NON-NLS-2$
			return stringToEscape;
		}
		StringBuffer escapedValue= new StringBuffer(stringToEscape);		
		for (int i= 0; i < escapedValue.length(); i++) {
			switch (escapedValue.charAt(i)) {
			case '\r':
				escapedValue.replace(i, i+1, "\\r"); //$NON-NLS-1$
				i++;
				break;
			case '\n':
				escapedValue.replace(i, i+1, "\\n"); //$NON-NLS-1$
				i++;
				break;
			case '\\':
				if (escapedValue.charAt(i + 1) == 'r' || escapedValue.charAt(i + 1) == 'n') {
					escapedValue.replace(i, i+1, "\\\\"); //$NON-NLS-1$
					i++;
				}
				break;
			default:
				break;
			}
		}

		return escapedValue.toString();
	}

	private Target initializeBuildSequenceInformation(BuildEvent event, Map targetToBuildSequence) {
	    Project antProject= event.getProject();
	    Vector targets= (Vector) antProject.getReference("eclipse.ant.targetVector"); //$NON-NLS-1$
        if (targets == null) {
            return null;
        }
	    Iterator itr= targets.iterator();
	    Hashtable allTargets= antProject.getTargets();
	    String targetName;
	    Vector sortedTargets;
	    while (itr.hasNext()) {
	        targetName= (String) itr.next();
	        sortedTargets= antProject.topoSort(targetName, allTargets);
	        targetToBuildSequence.put(allTargets.get(targetName), sortedTargets);
	    }
	    //the target to execute
	    return (Target) allTargets.get(targets.remove(0));
	}
    
    public void buildStarted() {
        setTargetToBuildSequence(new HashMap());
        fProjectToTargetNames= new HashMap();
    }
}
