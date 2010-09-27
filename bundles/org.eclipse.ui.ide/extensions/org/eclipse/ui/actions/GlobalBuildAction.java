/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Anton Leherbauer (Wind River) -  [296800] UI build actions should not lock the workspace
 *******************************************************************************/
package org.eclipse.ui.actions;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.ide.IDEInternalWorkbenchImages;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.IIDEHelpContextIds;
import org.eclipse.ui.internal.ide.actions.BuildUtilities;
import org.eclipse.ui.progress.IProgressConstants2;
import org.eclipse.ui.statushandlers.StatusManager;

/**
 * Standard action for full and incremental builds of all projects within the
 * workspace.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * @noextend This class is not intended to be subclassed by clients.
 */
public class GlobalBuildAction extends Action implements
        ActionFactory.IWorkbenchAction {
    /**
     * The type of build performed by this action. Can be either
     * <code>IncrementalProjectBuilder.INCREMENTAL_BUILD</code> or
     * <code>IncrementalProjectBuilder.FULL_BUILD</code>.
     */
    private int buildType;

    /**
     * The workbench window; or <code>null</code> if this action has been
     * <code>dispose</code>d.
     */
    private IWorkbenchWindow workbenchWindow;

    /**
     * Creates a new action of the appropriate type. The action id is
     * <code>IWorkbenchActionConstants.BUILD</code> for incremental builds and
     * <code>IWorkbenchActionConstants.REBUILD_ALL</code> for full builds.
     * 
     * @param workbench
     *            the active workbench
     * @param shell
     *            the shell for any dialogs
     * @param type
     *            the type of build; one of
     *            <code>IncrementalProjectBuilder.INCREMENTAL_BUILD</code> or
     *            <code>IncrementalProjectBuilder.FULL_BUILD</code>
     * 
     * @deprecated use GlobalBuildAction(IWorkbenchWindow, type) instead
     */
    public GlobalBuildAction(IWorkbench workbench, Shell shell, int type) {
        // always use active window; ignore shell
        this(workbench.getActiveWorkbenchWindow(), type);
        Assert.isNotNull(shell);
    }

    /**
     * Creates a new action of the appropriate type. The action id is
     * <code>IWorkbenchActionConstants.BUILD</code> for incremental builds and
     * <code>IWorkbenchActionConstants.REBUILD_ALL</code> for full builds.
     * 
     * @param window
     *            the window in which this action appears
     * @param type
     *            the type of build; one of
     *            <code>IncrementalProjectBuilder.INCREMENTAL_BUILD</code> or
     *            <code>IncrementalProjectBuilder.FULL_BUILD</code>
     */
    public GlobalBuildAction(IWorkbenchWindow window, int type) {
        Assert.isNotNull(window);
        this.workbenchWindow = window;
        setBuildType(type);
    }

    /**
     * Sets the build type.
     * 
     * @param type
     *            the type of build; one of
     *            <code>IncrementalProjectBuilder.INCREMENTAL_BUILD</code> or
     *            <code>IncrementalProjectBuilder.FULL_BUILD</code>
     */
    private void setBuildType(int type) {
        // allow AUTO_BUILD as well for backwards compatibility, but treat it
        // the same as INCREMENTAL_BUILD
        switch (type) {
        case IncrementalProjectBuilder.INCREMENTAL_BUILD:
        case IncrementalProjectBuilder.AUTO_BUILD:
            setText(IDEWorkbenchMessages.GlobalBuildAction_text);
            setToolTipText(IDEWorkbenchMessages.GlobalBuildAction_toolTip);
            setId("build"); //$NON-NLS-1$
            workbenchWindow.getWorkbench().getHelpSystem().setHelp(this,
                    IIDEHelpContextIds.GLOBAL_INCREMENTAL_BUILD_ACTION);
            setImageDescriptor(IDEInternalWorkbenchImages
                    .getImageDescriptor(IDEInternalWorkbenchImages.IMG_ETOOL_BUILD_EXEC));
            setDisabledImageDescriptor(IDEInternalWorkbenchImages
                    .getImageDescriptor(IDEInternalWorkbenchImages.IMG_ETOOL_BUILD_EXEC_DISABLED));
            setActionDefinitionId(IWorkbenchCommandConstants.PROJECT_BUILD_ALL);
            break;
        case IncrementalProjectBuilder.FULL_BUILD:
            setText(IDEWorkbenchMessages.GlobalBuildAction_rebuildText);
            setToolTipText(IDEWorkbenchMessages.GlobalBuildAction_rebuildToolTip);
            setId("rebuildAll"); //$NON-NLS-1$
            workbenchWindow.getWorkbench().getHelpSystem().setHelp(this,
                    IIDEHelpContextIds.GLOBAL_FULL_BUILD_ACTION);
            setActionDefinitionId("org.eclipse.ui.project.rebuildAll"); //$NON-NLS-1$
            break;
        default:
            throw new IllegalArgumentException("Invalid build type"); //$NON-NLS-1$
        }
        this.buildType = type;
    }

    /**
     * Returns the shell to use.
     */
    private Shell getShell() {
        return workbenchWindow.getShell();
    }

    /**
     * Returns the operation name to use
     */
    private String getOperationMessage() {
        if (buildType == IncrementalProjectBuilder.INCREMENTAL_BUILD) {
			return IDEWorkbenchMessages.GlobalBuildAction_buildOperationTitle;
		}
        return IDEWorkbenchMessages.GlobalBuildAction_rebuildAllOperationTitle;
    }

    /**
     * Builds all projects within the workspace. Does not save any open editors.
     */
    public void doBuild() {
        doBuildOperation();
    }

    /**
     * Invokes a build on all projects within the workspace. Reports any errors
     * with the build to the user.
     */
    /* package */void doBuildOperation() {
        Job buildJob = new Job(IDEWorkbenchMessages.GlobalBuildAction_jobTitle) {
            /*
             * (non-Javadoc)
             * 
             * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
             */
            protected IStatus run(IProgressMonitor monitor) {
                monitor.beginTask(getOperationMessage(), 100);
                try {
                    ResourcesPlugin.getWorkspace().build(buildType,
                            new SubProgressMonitor(monitor, 100));
                } catch (CoreException e) {
                    return e.getStatus();
                } finally {
                    monitor.done();
                }
                return Status.OK_STATUS;
            }

            /*
             * (non-Javadoc)
             * 
             * @see org.eclipse.core.runtime.jobs.Job#belongsTo(java.lang.Object)
             */
            public boolean belongsTo(Object family) {
                return ResourcesPlugin.FAMILY_MANUAL_BUILD == family;
            }
        };
        buildJob.setUser(true);
        buildJob.setProperty(IProgressConstants2.SHOW_IN_TASKBAR_ICON_PROPERTY, Boolean.TRUE);
        buildJob.schedule();
    }

    /**
     * Returns an array of all projects in the workspace
     */
    /* package */IProject[] getWorkspaceProjects() {
        return ResourcesPlugin.getWorkspace().getRoot().getProjects();
    }

    /*
     * (non-Javadoc) Method declared on IAction.
     * 
     * Builds all projects within the workspace. Saves all editors prior to
     * build depending on user's preference.
     */
    public void run() {
        if (workbenchWindow == null) {
            // action has been disposed
            return;
        }
        // Do nothing if there are no projects...
        IProject[] roots = getWorkspaceProjects();
        if (roots.length < 1) {
			return;
		}
        // Verify that there are builders registered on at
        // least one project
        if (!verifyBuildersAvailable(roots)) {
			return;
		}
        if (!verifyNoManualRunning()) {
			return;
		}
        // Save all resources prior to doing build
        BuildUtilities.saveEditors(null);
        // Perform the build on all the projects
        doBuildOperation();
    }

    /**
     * Checks that there is at least one project with a builder registered on
     * it.
     */
    /* package */boolean verifyBuildersAvailable(IProject[] roots) {
        try {
            for (int i = 0; i < roots.length; i++) {
                if (roots[i].isAccessible()) {
					if (roots[i].getDescription().getBuildSpec().length > 0) {
						return true;
					}
				}
            }
        } catch (CoreException e) {
        	StatusManager.getManager().handle(e, IDEWorkbenchPlugin.IDE_WORKBENCH);
            ErrorDialog
                    .openError(
                            getShell(),
                            IDEWorkbenchMessages.GlobalBuildAction_buildProblems,
                            NLS.bind(IDEWorkbenchMessages.GlobalBuildAction_internalError, e.getMessage()),
                            e.getStatus());
            return false;
        }
        return false;
    }

    /*
     * (non-Javadoc) Method declared on ActionFactory.IWorkbenchAction.
     * 
     * @since 3.0
     */
    public void dispose() {
        if (workbenchWindow == null) {
            // action has already been disposed
            return;
        }
        workbenchWindow = null;
    }

    /**
     * Verify that no manual build is running. If it is then give the use the
     * option to cancel. If they cancel, cancel the jobs and return true,
     * otherwise return false.
     * 
     * @return whether or not there is a manual build job running.
     */
    private boolean verifyNoManualRunning() {
		Job[] buildJobs = Job.getJobManager().find(
                ResourcesPlugin.FAMILY_MANUAL_BUILD);
        if (buildJobs.length == 0) {
			return true;
		}
        boolean cancel = MessageDialog.openQuestion(getShell(),
                IDEWorkbenchMessages.GlobalBuildAction_BuildRunningTitle,
                IDEWorkbenchMessages.GlobalBuildAction_BuildRunningMessage);
        if (cancel) {
            for (int i = 0; i < buildJobs.length; i++) {
                Job job = buildJobs[i];
                job.cancel();
            }
        }
        //If they cancelled get them to do it again.
        return false;
    }
}
