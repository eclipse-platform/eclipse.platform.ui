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

/**
 * Ant Debug utility class that provides support to the various Ant debug build loggers.
 */
public final class AntDebugUtil {
    
    public static int getLineNumber(Location location) {
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
    
    public static String getFileName(Location location) {
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
    
    private static void appendToStack(StringBuffer stackRepresentation, String targetName, String taskName, Location location) {
        stackRepresentation.append(targetName);
        stackRepresentation.append(DebugMessageIds.MESSAGE_DELIMITER);
        stackRepresentation.append(taskName);
        stackRepresentation.append(DebugMessageIds.MESSAGE_DELIMITER);
        
        stackRepresentation.append(AntDebugUtil.getFileName(location));
        stackRepresentation.append(DebugMessageIds.MESSAGE_DELIMITER);
        stackRepresentation.append(AntDebugUtil.getLineNumber(location));
        stackRepresentation.append(DebugMessageIds.MESSAGE_DELIMITER);
    }
    
    public static void marshalStack(StringBuffer stackRepresentation, Stack tasks, Target targetToExecute, Target targetExecuting, Map targetToBuildSequence) {
        stackRepresentation.append(DebugMessageIds.STACK);
        stackRepresentation.append(DebugMessageIds.MESSAGE_DELIMITER);
        
		if (tasks.isEmpty()) {
			appendToStack(stackRepresentation, targetExecuting.getName(), "", getLocation(targetExecuting)); //$NON-NLS-1$
		} else {
			for (int i = tasks.size() - 1; i >= 0 ; i--) {
				Task task= (Task) tasks.get(i);
				appendToStack(stackRepresentation, task.getOwningTarget().getName(), task.getTaskName(), task.getLocation());
			}
		}
        //target dependancy stack 
         if (targetToExecute != null) {
         	Vector buildSequence= (Vector) targetToBuildSequence.get(targetToExecute);
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
    
    public static void marshallProperties(StringBuffer propertiesRepresentation, Project project, Map initialProperties, Map lastProperties, boolean marshallLineSep) {
        propertiesRepresentation.append(DebugMessageIds.PROPERTIES);
        propertiesRepresentation.append(DebugMessageIds.MESSAGE_DELIMITER);
        Map currentProperties= currentProperties= project.getProperties();
        if (lastProperties != null && currentProperties.size() == lastProperties.size()) {
            //no new properties
            return;
        }
        
        Map currentUserProperties= project.getUserProperties();
        Iterator iter= currentProperties.keySet().iterator();
        String propertyName;
        String propertyValue;
        while (iter.hasNext()) {
            propertyName = (String) iter.next();
            if (!marshallLineSep && propertyName.equals("line.separator")) { //$NON-NLS-1$
            	continue;
            }
            if (lastProperties == null || lastProperties.get(propertyName) == null) { //new property
                propertiesRepresentation.append(propertyName.length());
                propertiesRepresentation.append(DebugMessageIds.MESSAGE_DELIMITER);
                propertiesRepresentation.append(propertyName);
                propertiesRepresentation.append(DebugMessageIds.MESSAGE_DELIMITER);
                propertyValue= (String) currentProperties.get(propertyName);
                propertiesRepresentation.append(propertyValue.length());
                propertiesRepresentation.append(DebugMessageIds.MESSAGE_DELIMITER);
                propertiesRepresentation.append(propertyValue);
                propertiesRepresentation.append(DebugMessageIds.MESSAGE_DELIMITER);
                if (initialProperties.get(propertyName) != null) { //properties set before the start of the build
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
        
        propertiesRepresentation.deleteCharAt(propertiesRepresentation.length() - 1);
    }
    
    public static Target initializeBuildSequenceInformation(BuildEvent event, Map targetToBuildSequence) {
        Project antProject= event.getProject();
        Vector targets= (Vector) antProject.getReference("eclipse.ant.targetVector"); //$NON-NLS-1$
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

    public static void taskStarted(BuildEvent event, IDebugBuildLogger logger) {
        if (logger.getInitialProperties() == null) {//implicit or top level target does not fire targetStarted()
            logger.setInitialProperties(event.getProject().getProperties());
        }
        
        logger.setCurrentTask(event.getTask());
        logger.setConsiderTargetBreakpoints(false);
        logger.getTasks().push(logger.getCurrentTask());
        logger.waitIfSuspended();
    }

    public static void taskFinished(IDebugBuildLogger logger) {
        logger.setLastTaskFinished((Task)logger.getTasks().pop());
        logger.setCurrentTask(null);
        String taskName= logger.getLastTaskFinished().getTaskName();
        if (logger.getStepOverTask() != null && ("antcall".equals(taskName) || "ant".equals(taskName))) { //$NON-NLS-1$ //$NON-NLS-2$
            logger.setShouldSuspend(true);
        }
        logger.waitIfSuspended();
    }

    public static void stepOver(IDebugBuildLogger logger) {
        logger.setStepOverTask(logger.getCurrentTask());
        if (logger.getCurrentTask() == null) {
            //stepping over target breakpoint
            logger.setShouldSuspend(true);
        }
        logger.notifyAll();
    }

    public static void targetStarted(BuildEvent event, IDebugBuildLogger logger) {
        if (logger.getInitialProperties() == null) {
            logger.setInitialProperties(event.getProject().getProperties());
        }
        if (logger.getTargetToBuildSequence() == null) {
            logger.setTargetToBuildSequence(new HashMap());
            logger.setTargetToExecute(AntDebugUtil.initializeBuildSequenceInformation(event, logger.getTargetToBuildSequence()));
        }
        
        logger.setTargetExecuting(event.getTarget());
        if (event.getTarget().equals(logger.getTargetToExecute())) {
            //the dependancies of the target to execute have been met
            //prepare for the next target
            Vector targets= (Vector) event.getProject().getReference("eclipse.ant.targetVector"); //$NON-NLS-1$
            if (!targets.isEmpty()) {
                logger.setTargetToExecute((Target) event.getProject().getTargets().get(targets.remove(0)));
            } else {
                logger.setTargetToExecute(null);
            }
        }
        logger.setConsiderTargetBreakpoints(true);
    }
}
