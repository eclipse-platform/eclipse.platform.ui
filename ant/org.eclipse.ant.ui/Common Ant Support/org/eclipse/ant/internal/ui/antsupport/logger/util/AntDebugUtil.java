/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ant.internal.ui.antsupport.logger.util;

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
        
        for (int i = tasks.size() - 1; i >= 0 ; i--) {
            Task task= (Task) tasks.get(i);
            appendToStack(stackRepresentation, task.getOwningTarget().getName(), task.getTaskName(), task.getLocation());
        }   
        //target dependancy stack 
         if (targetToExecute != null) {
         	Vector buildSequence= (Vector) targetToBuildSequence.get(targetToExecute);
         	int startIndex= buildSequence.indexOf(targetExecuting) + 1;
         	int dependancyStackDepth= buildSequence.indexOf(targetToExecute);
         	
         	Target stackTarget;
         	for (int i = startIndex; i <= dependancyStackDepth; i++) {
         		stackTarget= (Target) buildSequence.get(i);
         		appendToStack(stackRepresentation, stackTarget.getName(), "", stackTarget.getLocation()); //$NON-NLS-1$
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
}
