package org.eclipse.ant.internal.core.ant;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Ant;
import org.apache.tools.ant.taskdefs.CallTarget;
import org.eclipse.ant.core.AntCorePlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;

/**
 * Reports progress and checks for cancelation of a script execution.
 */
public class ProgressBuildListener implements BuildListener {

	protected Map fProjects;
	protected Project fMainProject;
	protected Project fParentProject;

	/**
	 *  Contains the progress monitor instances for the various	 *	projects in a chain.
	 */
	protected class ProjectMonitors {
		/**
		 *  This field is null for the main project		 */
		private Target fMainTarget;
		private IProgressMonitor fMainMonitor;
		private IProgressMonitor fTargetMonitor;
		private IProgressMonitor fTaskMonitor;
		
		protected IProgressMonitor getMainMonitor() {
			return fMainMonitor;
		}

		protected Target getMainTarget() {
			return fMainTarget;
		}

		protected IProgressMonitor getTargetMonitor() {
			return fTargetMonitor;
		}

		protected IProgressMonitor getTaskMonitor() {
			return fTaskMonitor;
		}

		protected void setMainMonitor(IProgressMonitor mainMonitor) {
			fMainMonitor = mainMonitor;
		}

		protected void setMainTarget(Target mainTarget) {
			fMainTarget = mainTarget;
		}

		protected void setTargetMonitor(IProgressMonitor targetMonitor) {
			fTargetMonitor = targetMonitor;
		}

		protected void setTaskMonitor(IProgressMonitor taskMonitor) {
			fTaskMonitor = taskMonitor;
		}

	}

	public ProgressBuildListener(Project project, List targetNames, IProgressMonitor monitor) {
		fProjects = new HashMap();
		fMainProject = project;
		ProjectMonitors monitors = new ProjectMonitors();
		if (monitor == null) {
			monitor= new NullProgressMonitor();
		}
		monitors.setMainMonitor(monitor);
		fProjects.put(fMainProject, monitors);
		Target[] targets = new Target[targetNames.size()];
		for (int i = 0; i < targetNames.size(); i++) {
			String targetName = (String) targetNames.get(i);
			targets[i] = (Target) fMainProject.getTargets().get(targetName);
		}
		int work = computeWork(fMainProject, targets);
		monitors.getMainMonitor().beginTask("", work);  //$NON-NLS-1$
	}

	public void buildStarted(BuildEvent event) {
		checkCanceled();
	}

	protected int computeWork(Project project, Target[] targets) {
		int result = 0;
		for (int i = 0; i < targets.length; i++) {
			result = result + countTarget(targets[i]);
		}
		return result;
	}

	protected int countTarget(Target target) {
		if (target == null) {
			return 0;
		}
		int result = 1;
		Project project = target.getProject();
		for (Enumeration dependencies = target.getDependencies(); dependencies.hasMoreElements();) {
			String targetName = (String) dependencies.nextElement();
			Target dependency = (Target) project.getTargets().get(targetName);
			if (dependency != null) {
				result = result + countTarget(dependency);
			}
		}
		// we have to handle antcall tasks as well
		Task[] tasks = target.getTasks();
		for (int i = 0; i < tasks.length; i++) {
			if (tasks[i] instanceof CallTarget) {
				// As we do not have access to the information (at least in Ant 1.4.1)
				// describing what target is executed by this antcall task, we assume
				// a scenario where it depends on all targets of the project but itself.
				result = result + (project.getTargets().size() - 1);
			}
		}
		return result;
	}

	public void buildFinished(BuildEvent event) {
		ProjectMonitors monitors = (ProjectMonitors) fProjects.get(fMainProject);
		monitors.getMainMonitor().done();
	}

	public void targetStarted(BuildEvent event) {
		checkCanceled();
		Project currentProject = event.getProject();
		if (currentProject == null) {
			return;
		}
		Target target = event.getTarget();
		ProjectMonitors monitors = (ProjectMonitors) fProjects.get(currentProject);

		// if monitors is null we are in a new script
		if (monitors == null) {
			monitors = createMonitors(currentProject, target);
		}

		monitors.setTargetMonitor(subMonitorFor(monitors.getMainMonitor(), 1));
		int work = (target != null) ? target.getTasks().length : 100;
		monitors.getTargetMonitor().beginTask("", work);  //$NON-NLS-1$
	}

	protected ProjectMonitors createMonitors(Project currentProject, Target target) {
		ProjectMonitors monitors = new ProjectMonitors();
		// remember the target so we can remove this monitors object later
		monitors.setMainTarget(target);
		int work = computeWork(currentProject, new Target[] { target });
		ProjectMonitors parentMonitors = null;
		if (fParentProject == null) {
			parentMonitors = (ProjectMonitors) fProjects.get(fMainProject);
			monitors.setMainMonitor(subMonitorFor(parentMonitors.getMainMonitor(), 1));
		} else {
			parentMonitors = (ProjectMonitors) fProjects.get(fParentProject);
			fParentProject = null;
			monitors.setMainMonitor(subMonitorFor(parentMonitors.getTaskMonitor(), 1));
		}
		monitors.getMainMonitor().beginTask("", work);  //$NON-NLS-1$
		fProjects.put(currentProject, monitors);
		return monitors;
	}

	public void targetFinished(BuildEvent event) {
		checkCanceled();
		Project currentProject = event.getProject();
		if (currentProject == null) {
			return;
		}
		ProjectMonitors monitors = (ProjectMonitors) fProjects.get(currentProject);
		if (monitors == null) {
			return;
		}
		monitors.getTargetMonitor().done();
		// if this is not the main project test if we are done with this project
		if ((currentProject != fMainProject) && (monitors.getMainTarget() == event.getTarget())) {
			monitors.getMainMonitor().done();
			fProjects.remove(currentProject);
		}
	}

	public void taskStarted(BuildEvent event) {
		checkCanceled();
		Project currentProject = event.getProject();
		if (currentProject == null) {
			return;
		}
		currentProject.getReferences().remove(AntCorePlugin.ECLIPSE_PROGRESS_MONITOR);
		ProjectMonitors monitors = (ProjectMonitors) fProjects.get(currentProject);
		if (monitors == null) {
			return;
		}
		Task task = event.getTask();
		if (task == null) {
			return;
		}
		monitors.setTaskMonitor(subMonitorFor(monitors.getTargetMonitor(), 1));
		monitors.getTaskMonitor().beginTask("", 1);  //$NON-NLS-1$
		// If this script is calling another one, track the project chain.
		if (task instanceof Ant) {
			fParentProject = currentProject;
		} else {
			currentProject.addReference(AntCorePlugin.ECLIPSE_PROGRESS_MONITOR, monitors.getTaskMonitor());
		}
	}

	public void taskFinished(BuildEvent event) {
		checkCanceled();
		Project project = event.getProject();
		if (project == null) {
			return;
		}
		project.getReferences().remove(AntCorePlugin.ECLIPSE_PROGRESS_MONITOR);
		ProjectMonitors monitors = (ProjectMonitors) fProjects.get(project);
		if (monitors == null) {
			return;
		}
		monitors.getTaskMonitor().done();
	}

	public void messageLogged(BuildEvent event) {
	}

	protected void checkCanceled() {
		ProjectMonitors monitors = (ProjectMonitors) fProjects.get(fMainProject);
		if (monitors.getMainMonitor().isCanceled()) {
			throw new OperationCanceledException(InternalAntMessages.getString("ProgressBuildListener.Build_cancelled._5")); //$NON-NLS-1$
		}
	}

	protected IProgressMonitor subMonitorFor(IProgressMonitor monitor, int ticks) {
		if (monitor == null) {
			return new NullProgressMonitor();
		}
		if (monitor instanceof NullProgressMonitor) {
			return monitor;
		}
		return new SubProgressMonitor(monitor, ticks);
	}
}