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

import java.util.Map;
import java.util.Stack;

import org.apache.tools.ant.Target;
import org.apache.tools.ant.Task;

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
    private Target fTargetToExecute= null;
    private Target fTargetExecuting= null;
	
	private boolean fConsiderTargetBreakpoints= false;
	private boolean fShouldSuspend;
	private boolean fClientSuspend= false;
	private boolean fStepIntoSuspend= false;
	
	public AntDebugState(IDebugBuildLogger logger) {
		fLogger= logger;
	}

	public void waitIfSuspended() {
		fLogger.waitIfSuspended();
	}

	public Task getLastTaskFinished() {
		return fLastTaskFinished;
	}

	public void setLastTaskFinished(Task lastTaskFinished) {
		fLastTaskFinished= lastTaskFinished;

	}

	public Task getCurrentTask() {
		return fCurrentTask;
	}

	public void setCurrentTask(Task currentTask) {
		fCurrentTask= currentTask;

	}

	public Map getInitialProperties() {
		return fInitialProperties;
	}

	public void setInitialProperties(Map initialProperties) {
		fInitialProperties= initialProperties;
	}

	public Task getStepOverTask() {
		return fStepOverTask;
	}

	public void setStepOverTask(Task stepOverTask) {
		fStepOverTask= stepOverTask;

	}

	public boolean considerTargetBreakpoints() {
		return fConsiderTargetBreakpoints;
	}

	public void setConsiderTargetBreakpoints(boolean considerTargetBreakpoints) {
		fConsiderTargetBreakpoints= considerTargetBreakpoints;
	}

	public void setTasks(Stack tasks) {
		fTasks= tasks;
	}

	public Stack getTasks() {
		return fTasks;
	}

	public void setShouldSuspend(boolean shouldSuspend) {
		fShouldSuspend= shouldSuspend;
	}

	public boolean shouldSuspend() {
		return fShouldSuspend;
	}

	public Map getTargetToBuildSequence() {
		return fTargetToBuildSequence;
	}

	public void setTargetToBuildSequence(Map sequence) {
		fTargetToBuildSequence= sequence;
	}

	public void setTargetToExecute(Target target) {
		fTargetToExecute= target;
	}

	public void setTargetExecuting(Target target) {
		fTargetExecuting= target;
	}

	public Target getTargetToExecute() {
		return fTargetToExecute;
	}
	
	public Target getTargetExecuting() {
		return fTargetExecuting;
	}

	public boolean isStepIntoSuspend() {
		return fStepIntoSuspend;
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

	public void updateProperties() {
		fProperties= ((Task)getTasks().peek()).getProject().getProperties();
	}
}
