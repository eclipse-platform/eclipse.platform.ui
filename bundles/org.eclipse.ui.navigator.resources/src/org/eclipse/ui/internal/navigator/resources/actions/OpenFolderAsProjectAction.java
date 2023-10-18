/*******************************************************************************
 * Copyright (c) 2014, 2015, 2023 Red Hat Inc. and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Mickael Istria (Red Hat Inc.) - initial API and implementation
 *     Nikifor Fedorov (ArSysOp) - Import more than one project at once (eclipse.platform#226)
 ******************************************************************************/
package org.eclipse.ui.internal.navigator.resources.actions;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.OperationHistoryFactory;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobFunction;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.core.runtime.jobs.JobGroup;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE.SharedImages;
import org.eclipse.ui.ide.undo.CreateProjectOperation;
import org.eclipse.ui.internal.navigator.resources.plugin.WorkbenchNavigatorMessages;
import org.eclipse.ui.navigator.CommonViewer;

/**
 * @since 3.3
 *
 */
public class OpenFolderAsProjectAction extends Action {

	private final Collection<IFolder> folders;
	private final CommonViewer viewer;

	/**
	 * @param folder
	 * @param viewer
	 */
	public OpenFolderAsProjectAction(Collection<IFolder> folder, CommonViewer viewer) {
		super(folder.size() > 1 //
				? WorkbenchNavigatorMessages.OpenProjectAction_OpenExistingProjects //
				: WorkbenchNavigatorMessages.OpenProjectAction_OpenExistingProject);
		this.folders = folder;
		this.viewer = viewer;
		setDescription(WorkbenchNavigatorMessages.OpenProjectAction_OpenExistingProject_desc);
		setImageDescriptor(
				PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(SharedImages.IMG_OBJ_PROJECT));
	}

	/**
	 * @param folder
	 * @param viewer
	 */
	public OpenFolderAsProjectAction(IFolder folder, CommonViewer viewer) {
		this(Collections.singleton(folder), viewer);
	}

	@Override
	public void run() {
		List<IProject> imported = new LinkedList<IProject>();
		JobGroup group = new JobGroup(WorkbenchNavigatorMessages.OpenProjectAction_multiple, 0, folders.size());
		Job.getJobManager().addJobChangeListener(new GroupFinishedListener(group,
				() -> reflectChanges(imported, folders.stream().map(IFolder::getParent).distinct().toList())));
		for (IFolder folder : folders) {
			Job job = Job.create(NLS.bind(WorkbenchNavigatorMessages.OpenProjectAction_opening, folder.getName()),
					new ActualImport(folder, imported::add));
			job.setPriority(Job.INTERACTIVE);
			job.setJobGroup(group);
			job.schedule();
		}
	}

	private void reflectChanges(List<IProject> imported, List<IContainer> refreshed) {
		viewer.getTree().getDisplay().asyncExec(() -> {
			refreshed.forEach(viewer::refresh);
			viewer.setSelection(new StructuredSelection(imported));
		});
	}

	private static final class GroupFinishedListener extends JobChangeAdapter {

		private final JobGroup target;
		private final Runnable finalize;

		public GroupFinishedListener(JobGroup target, Runnable finalize) {
			this.target = target;
			this.finalize = finalize;
		}

		@Override
		public void done(IJobChangeEvent event) {
			if (!target.equals(event.getJob().getJobGroup())) {
				return;
			}
			if (event.getJobGroupResult() == null) {
				return;
			}
			Job.getJobManager().removeJobChangeListener(this);
			finalize.run();
		}

	}

	private static final class ActualImport implements IJobFunction {

		private final IFolder folder;
		private final Consumer<IProject> reflect;

		private ActualImport(IFolder folder, Consumer<IProject> reflect) {
			this.folder = folder;
			this.reflect = reflect;
		}

		@Override
		public IStatus run(IProgressMonitor monitor) {
			IProject parentProject = folder.getProject();
			Set<IWorkingSet> parentWorkingSets = new HashSet<>();
			IWorkingSetManager workingSetManager = PlatformUI.getWorkbench().getWorkingSetManager();
			for (IWorkingSet workingSet : workingSetManager.getWorkingSets()) {
				for (IAdaptable element : workingSet.getElements()) {
					if (parentProject.equals(Adapters.adapt(element, IProject.class))) {
						parentWorkingSets.add(workingSet);
						break;
					}
				}
			}
			try {
				IProjectDescription desc = ResourcesPlugin.getWorkspace()
						.loadProjectDescription(folder.getLocation().append(IProjectDescription.DESCRIPTION_FILE_NAME));
				desc.setLocation(folder.getLocation());
				CreateProjectOperation operation = new CreateProjectOperation(desc, desc.getName());
				IStatus status = OperationHistoryFactory.getOperationHistory().execute(operation, null, null);
				if (status.isOK()) {
					IProject newProject = (IProject) operation.getAffectedObjects()[0];
					workingSetManager.addToWorkingSets(newProject,
							parentWorkingSets.toArray(new IWorkingSet[parentWorkingSets.size()]));
					reflect.accept(newProject);
					return Status.OK_STATUS;
				}
				return status;
			} catch (ExecutionException ex) {
				return Status.error(ex.getMessage(), ex);
			} catch (CoreException ex) {
				return ex.getStatus();
			}
		}

	}
}
