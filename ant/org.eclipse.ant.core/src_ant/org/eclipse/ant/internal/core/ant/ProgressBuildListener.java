/**********************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.ant.internal.core.ant;

import java.util.*;
import java.util.Hashtable;
import java.util.Iterator;

import org.apache.tools.ant.*;
import org.apache.tools.ant.taskdefs.Ant;
import org.apache.tools.ant.taskdefs.CallTarget;
import org.eclipse.ant.core.AntCorePlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
/**
 * Reports progress and checks for cancelation of a script execution.
 */
public class ProgressBuildListener implements BuildListener {

	protected Hashtable projects;
	protected Project mainProject;
	protected Project parentProject;

	// cointains the progress monitor instances for the various
	// projects in a chain
	protected class ProjectMonitors {
		// this field is null for the main project
		Target mainTarget;
		IProgressMonitor mainMonitor;
		IProgressMonitor targetMonitor;
		IProgressMonitor taskMonitor;
	}

public ProgressBuildListener(Project project, Vector targetNames, IProgressMonitor monitor) {
	projects = new Hashtable();
	this.mainProject = project;
	ProjectMonitors monitors = new ProjectMonitors();
	monitors.mainMonitor = Policy.monitorFor(monitor);
	projects.put(this.mainProject, monitors);
	Target[] targets = new Target[targetNames.size()];
	for (int i = 0; i < targetNames.size(); i++) {
		String targetName = (String) targetNames.get(i);
		targets[i] = (Target) this.mainProject.getTargets().get(targetName);
	}
	int work = computeWork(this.mainProject, targets);
	monitors.mainMonitor.beginTask("", work); //$NON-NLS-1$
}

public void buildStarted(BuildEvent event) {
	checkCanceled();
}

protected int computeWork(Project project, Target[] targets) {
	int result = 0;
	for (int i = 0; i < targets.length; i++)
		result = result + countTarget(targets[i]);
	return result;
}

protected int countTarget(Target target) {
	int result = 1;
	Project project = target.getProject();
	for(Enumeration dependencies = target.getDependencies(); dependencies.hasMoreElements();) {
		String targetName = (String) dependencies.nextElement();
		Target dependency = (Target) project.getTargets().get(targetName);
		if (dependency != null)
			result = result + countTarget(dependency);
	}
	// we have to handle antcall tasks as well
	Task[] tasks = target.getTasks();
	for (int i = 0; i < tasks.length; i++) {
		if (tasks[i] instanceof CallTarget) {
			// As we do not have access to the information (at least in Ant 1.4.1)
			// describing what target is executed by this antcall task, we assume
			// a scenario where it depends on all targets of the project but itself.
			result = result + (project.getTargets().size()-1);
		}
	}
	return result;
}

public void buildFinished(BuildEvent event) {
	ProjectMonitors monitors = (ProjectMonitors) projects.get(mainProject);
	monitors.mainMonitor.done();
}

public void targetStarted(BuildEvent event) {
	checkCanceled();
	Project currentProject = event.getProject();
	if (currentProject == null)
		return;
	Target target = event.getTarget();
	ProjectMonitors monitors = (ProjectMonitors) projects.get(currentProject);

	// if monitors is null we are in a new script
	if (monitors == null)
		monitors = createMonitors(currentProject, target);

	monitors.targetMonitor = Policy.subMonitorFor(monitors.mainMonitor, 1);
	int work = (target != null) ? target.getTasks().length : 100;
	monitors.targetMonitor.beginTask("", work); //$NON-NLS-1$
}

protected ProjectMonitors createMonitors(Project currentProject, Target target) {
	ProjectMonitors monitors = new ProjectMonitors();
	// remember the target so we can remove this monitors object later
	monitors.mainTarget = target;
	int work = computeWork(currentProject, new Target[]{ target });
	ProjectMonitors parentMonitors = null;
	if (parentProject == null) {
		parentMonitors = (ProjectMonitors) projects.get(mainProject);
		monitors.mainMonitor = Policy.subMonitorFor(parentMonitors.mainMonitor, 1);
	} else {
		parentMonitors = (ProjectMonitors) projects.get(parentProject);
		parentProject = null;
		monitors.mainMonitor = Policy.subMonitorFor(parentMonitors.taskMonitor, 1);
	}
	monitors.mainMonitor.beginTask("", work); //$NON-NLS-1$
	projects.put(currentProject, monitors);
	return monitors;
}

public void targetFinished(BuildEvent event) {
	checkCanceled();
	Project currentProject = event.getProject();
	if (currentProject == null)
		return;
	ProjectMonitors monitors = (ProjectMonitors) projects.get(currentProject);
	if (monitors == null)
		return;
	monitors.targetMonitor.done();
	// if this is not the main project test if we are done with this project
	if ((currentProject != mainProject) && (monitors.mainTarget == event.getTarget())) {
		monitors.mainMonitor.done();
		projects.remove(currentProject);
	}
}

public void taskStarted(BuildEvent event) {
	checkCanceled();
	Project currentProject = event.getProject();
	if (currentProject == null)
		return;
	currentProject.getReferences().remove(AntCorePlugin.ECLIPSE_PROGRESS_MONITOR);
	ProjectMonitors monitors = (ProjectMonitors) projects.get(currentProject);
	if (monitors == null)
		return;
	Task task = event.getTask();
	if (task == null)
		return;
	monitors.taskMonitor = Policy.subMonitorFor(monitors.targetMonitor, 1);
	monitors.taskMonitor.beginTask("", 1); //$NON-NLS-1$
	// If this script is calling another one, track the project chain.
	if (task instanceof Ant)
		parentProject = currentProject;
	else
		currentProject.addReference(AntCorePlugin.ECLIPSE_PROGRESS_MONITOR, monitors.taskMonitor);
}

public void taskFinished(BuildEvent event) {
	checkCanceled();
	Project project = event.getProject();
	if (project == null)
		return;
	project.getReferences().remove(AntCorePlugin.ECLIPSE_PROGRESS_MONITOR);
	ProjectMonitors monitors = (ProjectMonitors) projects.get(project);
	if (monitors == null)
		return;
	monitors.taskMonitor.done();
}

public void messageLogged(BuildEvent event) {
}

protected void checkCanceled() {
	ProjectMonitors monitors = (ProjectMonitors) projects.get(mainProject);
	if (monitors.mainMonitor.isCanceled())
		throw new OperationCanceledException(Policy.bind("exception.canceled")); //$NON-NLS-1$
}
}