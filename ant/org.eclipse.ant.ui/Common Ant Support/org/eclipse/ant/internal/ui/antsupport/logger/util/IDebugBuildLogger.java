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

public interface IDebugBuildLogger {

    public abstract void waitIfSuspended();

    public abstract Task getLastTaskFinished();

    public abstract void setLastTaskFinished(Task lastTaskFinished);

    public abstract Task getCurrentTask();

    public abstract void setCurrentTask(Task currentTask);

    public abstract Map getInitialProperties();

    public abstract void setInitialProperties(Map initialProperties);

    public abstract Task getStepOverTask();

    public abstract void setStepOverTask(Task stepOverTask);

    public abstract boolean considerTargetBreakpoints();

    public abstract void setConsiderTargetBreakpoints(boolean considerTargetBreakpoints);

    public abstract void setTasks(Stack tasks);

    public abstract Stack getTasks();

    public abstract void setShouldSuspend(boolean fShouldSuspend);

    public abstract boolean shouldSuspend();

    public abstract Map getTargetToBuildSequence();

    public abstract void setTargetToBuildSequence(Map sequence);

    public abstract void setTargetToExecute(Target target);

    public abstract void setTargetExecuting(Target target);

    public abstract Target getTargetToExecute();

}