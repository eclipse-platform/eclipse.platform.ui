/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.actions;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceRuleFactory;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.IIDEHelpContextIds;

/**
 * Standard action for deleting the currently selected resources.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 */
public class DeleteResourceAction extends SelectionListenerAction {

    static class DeleteProjectDialog extends MessageDialog {

        private IResource[] projects;

        private boolean deleteContent = false;
        
        /**
         * Control testing mode.  In testing mode, it returns true to
         * delete contents and does not pop up the dialog.
         */
        private boolean fIsTesting = false;

        private Button radio1;

        private Button radio2;

        DeleteProjectDialog(Shell parentShell, IResource[] projects) {
            super(parentShell, getTitle(projects), null, // accept the default window icon
                    getMessage(projects), MessageDialog.QUESTION, new String[] {
                            IDialogConstants.YES_LABEL,
                            IDialogConstants.NO_LABEL }, 0); // yes is the default
            this.projects = projects;
        }

        static String getTitle(IResource[] projects) {
            if (projects.length == 1)
                return IDEWorkbenchMessages.DeleteResourceAction_titleProject1;
            return IDEWorkbenchMessages.DeleteResourceAction_titleProjectN;
        }

        static String getMessage(IResource[] projects) {
            if (projects.length == 1) {
                IProject project = (IProject) projects[0];
                return NLS.bind(IDEWorkbenchMessages.DeleteResourceAction_confirmProject1, project.getName());
            }
            return NLS.bind(IDEWorkbenchMessages.DeleteResourceAction_confirmProjectN, new Integer(projects.length));
        }

        /* (non-Javadoc)
         * Method declared on Window.
         */
        protected void configureShell(Shell newShell) {
            super.configureShell(newShell);
            PlatformUI.getWorkbench().getHelpSystem().setHelp(newShell,
                    IIDEHelpContextIds.DELETE_PROJECT_DIALOG);
        }

        protected Control createCustomArea(Composite parent) {
            Composite composite = new Composite(parent, SWT.NONE);
            composite.setLayout(new GridLayout());
            radio1 = new Button(composite, SWT.RADIO);
            radio1.addSelectionListener(selectionListener);
            String text1;
            if (projects.length == 1) {
                IProject project = (IProject) projects[0];
                if (project == null || project.getLocation() == null)
                    text1 = IDEWorkbenchMessages.DeleteResourceAction_deleteContentsN;
                else
                    text1 = NLS.bind(IDEWorkbenchMessages.DeleteResourceAction_deleteContents1, project.getLocation().toOSString());
            } else {
                text1 = IDEWorkbenchMessages.DeleteResourceAction_deleteContentsN;
            }
            radio1.setText(text1);
            radio1.setFont(parent.getFont());

            radio2 = new Button(composite, SWT.RADIO);
            radio2.addSelectionListener(selectionListener);
            String text2 = IDEWorkbenchMessages.DeleteResourceAction_doNotDeleteContents;
            radio2.setText(text2);
            radio2.setFont(parent.getFont());

            // set initial state
            radio1.setSelection(deleteContent);
            radio2.setSelection(!deleteContent);

            return composite;
        }

        private SelectionListener selectionListener = new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                Button button = (Button) e.widget;
                if (button.getSelection()) {
                    deleteContent = (button == radio1);
                }
            }
        };

        boolean getDeleteContent() {
            return deleteContent;
        }

		/* (non-Javadoc)
		 * @see org.eclipse.jface.window.Window#open()
		 */
		public int open() {
			//Override Window#open() to allow for non-interactive testing.
			if (fIsTesting) {
				deleteContent = true;
				return Window.OK;
			}
			return super.open();
		}
        
		/**
		 * Set this delete dialog into testing mode.  It won't pop up, 
		 * and it returns true for deleteContent.
		 * 
		 * @param t the testing mode
		 */
        void setTestingMode(boolean t) {
        	fIsTesting = t;
        }
    }

    /**
     * The id of this action.
     */
    public static final String ID = PlatformUI.PLUGIN_ID
            + ".DeleteResourceAction";//$NON-NLS-1$

    /**
     * The shell in which to show any dialogs.
     */
    private Shell shell;

    /**
     * Whether or not we are deleting content for projects.
     */
    private boolean deleteContent = false;

    /**
     * Whether or not to automatically delete out of sync resources
     */
    private boolean forceOutOfSyncDelete = false;
    
    /**
     * Flag that allows testing mode ... it won't pop up the project
     * delete dialog, and will return "delete all content".
     */
    protected boolean fTestingMode = false;

    /**
     * Creates a new delete resource action.
     *
     * @param shell the shell for any dialogs
     */
    public DeleteResourceAction(Shell shell) {
        super(IDEWorkbenchMessages.DeleteResourceAction_text);
        setToolTipText(IDEWorkbenchMessages.DeleteResourceAction_toolTip);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
				IIDEHelpContextIds.DELETE_RESOURCE_ACTION);
        setId(ID);
        if (shell == null) {
            throw new IllegalArgumentException();
        }
        this.shell = shell;
    }

    /**
     * Returns whether delete can be performed on the current selection.
     *
     * @param resources the selected resources
     * @return <code>true</code> if the resources can be deleted, and 
     *  <code>false</code> if the selection contains non-resources or phantom
     *  resources
     */
    private boolean canDelete(IResource[] resources) {
        // allow only projects or only non-projects to be selected; 
        // note that the selection may contain multiple types of resource
        if (!(containsOnlyProjects(resources) || containsOnlyNonProjects(resources))) {
            return false;
        }

        if (resources.length == 0)
            return false;
        // Return true if everything in the selection exists.
        for (int i = 0; i < resources.length; i++) {
            IResource resource = resources[i];
            if (resource.isPhantom()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns whether the selection contains linked resources.
     *
     * @param resources the selected resources
     * @return <code>true</code> if the resources contain linked   
     *  resources, and <code>false</code> otherwise
     */
    private boolean containsLinkedResource(IResource[] resources) {
        for (int i = 0; i < resources.length; i++) {
            IResource resource = resources[i];
            if (resource.isLinked())
                return true;
        }
        return false;
    }

    /**
     * Returns whether the selection contains only non-projects.
     *
     * @param resources the selected resources
     * @return <code>true</code> if the resources contains only non-projects, and 
     *  <code>false</code> otherwise
     */
    private boolean containsOnlyNonProjects(IResource[] resources) {
        int types = getSelectedResourceTypes(resources);
        // check for empty selection
        if (types == 0)
            return false;
        // note that the selection may contain multiple types of resource
        return (types & IResource.PROJECT) == 0;
    }

    /**
     * Returns whether the selection contains only projects.
     * 
     * @param resources the selected resources
     * @return <code>true</code> if the resources contains only projects, and 
     *  <code>false</code> otherwise
     */
    private boolean containsOnlyProjects(IResource[] resources) {
        int types = getSelectedResourceTypes(resources);
        // note that the selection may contain multiple types of resource
        return types == IResource.PROJECT;
    }
    
    /**
     * Creates and returns a result status appropriate for the given list of exceptions.
     * @param exceptions The list of exceptions that occurred (may be empty)
     * @return The result status for the deletion
     */
    private IStatus createResult(List exceptions) {
    	if (exceptions.isEmpty())
    		return Status.OK_STATUS;
        final int exceptionCount = exceptions.size();
        if (exceptionCount == 1) {
            return ((CoreException) exceptions.get(0)).getStatus();
        }
        CoreException[] children = (CoreException[]) exceptions.toArray(new CoreException[exceptionCount]);
        boolean outOfSync = false;
        for (int i = 0; i < children.length; i++) {
            if (children[i].getStatus().getCode() == IResourceStatus.OUT_OF_SYNC_LOCAL) {
                outOfSync = true;
                break;
            }
        }
        String title = outOfSync ? IDEWorkbenchMessages.DeleteResourceAction_outOfSyncError : IDEWorkbenchMessages.DeleteResourceAction_deletionExceptionMessage;
        final MultiStatus multi = new MultiStatus(
                IDEWorkbenchPlugin.IDE_WORKBENCH,
                0,
                title, null);
        for (int i = 0; i < exceptionCount; i++) {
            CoreException exception = children[i];
            IStatus status = exception.getStatus();
            multi.add(new Status(status.getSeverity(), status.getPlugin(),
                    status.getCode(), status.getMessage(), exception));
        }
        return multi;
    }

    /**
     * Asks the user to confirm a delete operation.
     *
     * @param resources the selected resources
     * @return <code>true</code> if the user says to go ahead, and <code>false</code>
     *  if the deletion should be abandoned
     */
    private boolean confirmDelete(IResource[] resources) {
        if (containsOnlyProjects(resources)) {
            return confirmDeleteProjects(resources);
        }
        return confirmDeleteNonProjects(resources);
    }

    /**
     * Asks the user to confirm a delete operation,
     * where the selection contains no projects.
     *
     * @param resources the selected resources
     * @return <code>true</code> if the user says to go ahead, and <code>false</code>
     *  if the deletion should be abandoned
     */
    private boolean confirmDeleteNonProjects(IResource[] resources) {
        String title;
        String msg;
        if (resources.length == 1) {
            title = IDEWorkbenchMessages.DeleteResourceAction_title1;
            IResource resource = resources[0];
            if (resource.isLinked())
                msg = NLS.bind(IDEWorkbenchMessages.DeleteResourceAction_confirmLinkedResource1, resource.getName());
            else
                msg = NLS.bind(IDEWorkbenchMessages.DeleteResourceAction_confirm1, resource.getName());
        } else {
            title = IDEWorkbenchMessages.DeleteResourceAction_titleN;
            if (containsLinkedResource(resources))
                msg = NLS.bind(IDEWorkbenchMessages.DeleteResourceAction_confirmLinkedResourceN, new Integer(resources.length));
            else
                msg = NLS.bind(IDEWorkbenchMessages.DeleteResourceAction_confirmN, new Integer(resources.length));
        }
        return MessageDialog.openQuestion(shell, title, msg);
    }

    /**
     * Asks the user to confirm a delete operation,
     * where the selection contains only projects.
     * Also remembers whether project content should be deleted.
     *
     * @param resources the selected resources
     * @return <code>true</code> if the user says to go ahead, and <code>false</code>
     *  if the deletion should be abandoned
     */
    private boolean confirmDeleteProjects(IResource[] resources) {
        DeleteProjectDialog dialog = new DeleteProjectDialog(shell, resources);
        dialog.setTestingMode(fTestingMode);
        int code = dialog.open();
        deleteContent = dialog.getDeleteContent();
        return code == 0; // YES
    }

    /**
     * Deletes the given resources.
     */
    private void delete(IResource[] resourcesToDelete, IProgressMonitor monitor) throws CoreException{
        final List exceptions = new ArrayList();
        forceOutOfSyncDelete = false;
        monitor.beginTask("", resourcesToDelete.length); //$NON-NLS-1$
        try {
	        for (int i = 0; i < resourcesToDelete.length; ++i) {
	            if (monitor.isCanceled()) {
	                throw new OperationCanceledException();
	            }
	            try {
	                delete(resourcesToDelete[i], new SubProgressMonitor(monitor, 1,
	                        SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK));
	            } catch (CoreException e) {
	                exceptions.add(e);
	            }
	        }
	        IStatus result = createResult(exceptions);
			if (!result.isOK())
				throw new CoreException(result);
        } finally {
        	monitor.done();
        }
    }

    /**
     * Deletes the given resource.
     */
    private void delete(IResource resourceToDelete, IProgressMonitor monitor)
            throws CoreException {
        boolean force = false; // don't force deletion of out-of-sync resources
        try {
            if (resourceToDelete.getType() == IResource.PROJECT) {
                // if it's a project, ask whether content should be deleted too
                IProject project = (IProject) resourceToDelete;
                project.delete(deleteContent, force, monitor);
            } else {
                // if it's not a project, just delete it
                resourceToDelete.delete(IResource.KEEP_HISTORY, monitor);
            }
        } catch (CoreException exception) {
            if (resourceToDelete.getType() == IResource.FILE) {
                IStatus[] children = exception.getStatus().getChildren();

                if (children.length == 1
                        && children[0].getCode() == IResourceStatus.OUT_OF_SYNC_LOCAL) {
                    if (forceOutOfSyncDelete) {
                        resourceToDelete.delete(IResource.KEEP_HISTORY
                                | IResource.FORCE, monitor);
                    } else {
                        int result = queryDeleteOutOfSync(resourceToDelete);

                        if (result == IDialogConstants.YES_ID) {
                            resourceToDelete.delete(IResource.KEEP_HISTORY
                                    | IResource.FORCE, monitor);
                        } else if (result == IDialogConstants.YES_TO_ALL_ID) {
                            forceOutOfSyncDelete = true;
                            resourceToDelete.delete(IResource.KEEP_HISTORY
                                    | IResource.FORCE, monitor);
                        } else if (result == IDialogConstants.CANCEL_ID) {
                            throw new OperationCanceledException();
                        }
                    }
                } else {
                    throw exception;
                }
            } else {
                throw exception;
            }
        }
    }

    /**
     * Return an array of the currently selected resources.
     *
     * @return the selected resources
     */
    private IResource[] getSelectedResourcesArray() {
        List selection = getSelectedResources();
        IResource[] resources = new IResource[selection.size()];
        selection.toArray(resources);
        return resources;
    }

    /**
     * Returns a bit-mask containing the types of resources in the selection.
     * 
     * @param resources the selected resources
     */
    private int getSelectedResourceTypes(IResource[] resources) {
        int types = 0;
        for (int i = 0; i < resources.length; i++) {
            types |= resources[i].getType();
        }
        return types;
    }

    /* (non-Javadoc)
     * Method declared on IAction.
     */
    public void run() {
        IResource[] resources = getSelectedResourcesArray();
        // WARNING: do not query the selected resources more than once
        // since the selection may change during the run, 
        // e.g. due to window activation when the prompt dialog is dismissed.
        // For more details, see Bug 60606 [Navigator] (data loss) Navigator deletes/moves the wrong file
        if (!confirmDelete(resources))
            return;
        final IResource[] resourcesToDelete = getResourcesToDelete(resources);

        if (resourcesToDelete.length == 0)
            return;
		//use a non-workspace job with a runnable inside so we can avoid periodic updates
    	Job deleteJob = new Job(IDEWorkbenchMessages.DeleteResourceAction_jobName) {
    		public IStatus run(IProgressMonitor monitor) {
				try {
					ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {
						public void run(IProgressMonitor monitor) throws CoreException {
					        delete(resourcesToDelete, monitor);
						}
					},	null, IWorkspace.AVOID_UPDATE, monitor);
				} catch (CoreException e) {
					return e.getStatus();
				}
				return Status.OK_STATUS;
    		}

			/* (non-Javadoc)
			 * @see org.eclipse.core.runtime.jobs.Job#belongsTo(java.lang.Object)
			 */
			public boolean belongsTo(Object family) {
				if (IDEWorkbenchMessages.DeleteResourceAction_jobName
						.equals(family)) {
					return true;
				}
				return super.belongsTo(family);
			}
    		
    	};
    	deleteJob.setRule(getDeleteRule(resourcesToDelete));
    	deleteJob.setUser(true);
    	deleteJob.schedule();
    }

    /*
     * Return the scheduling rule that encompasses the deletion of the selected resources
     */
    private ISchedulingRule getDeleteRule(IResource[] resourcesToDelete) {
        IResourceRuleFactory ruleFactory = ResourcesPlugin.getWorkspace().getRuleFactory();
        ISchedulingRule combinedRule = null;
        for (int i = 0; i < resourcesToDelete.length; i++) {
            IResource resource = resourcesToDelete[i];
            ISchedulingRule deleteRule = ruleFactory.deleteRule(resource);
            if (combinedRule == null) {
                combinedRule = deleteRule;
            } else {
                combinedRule = MultiRule.combine(combinedRule, deleteRule);
            }
        }
        return combinedRule;
    }

    /**
     * Returns the resources to delete based on the selection and their read-only status.
     * 
     * @param resources the selected resources
     * @return the resources to delete
     */
    private IResource[] getResourcesToDelete(IResource[] resources) {

        if (containsOnlyProjects(resources) && !deleteContent) {
            // We can just return the selection
            return resources;
        }

        ReadOnlyStateChecker checker = new ReadOnlyStateChecker(this.shell,
                IDEWorkbenchMessages.DeleteResourceAction_title1,
                IDEWorkbenchMessages.DeleteResourceAction_readOnlyQuestion);
        checker.setIgnoreLinkedResources(true);
        return checker.checkReadOnlyResources(resources);
    }

    /**
     * The <code>DeleteResourceAction</code> implementation of this
     * <code>SelectionListenerAction</code> method disables the action
     * if the selection contains phantom resources or non-resources
     */
    protected boolean updateSelection(IStructuredSelection selection) {
        return super.updateSelection(selection)
                && canDelete(getSelectedResourcesArray());
    }

    /**
     * Ask the user whether the given resource should be deleted
     * despite being out of sync with the file system.
     * @param resource the out of sync resource
     * @return One of the IDialogConstants constants indicating which
     * 	of the Yes, Yes to All, No, Cancel options has been selected by 
     * 	the user.
     */
    private int queryDeleteOutOfSync(IResource resource) {
        final MessageDialog dialog = new MessageDialog(
                shell,
                IDEWorkbenchMessages.DeleteResourceAction_messageTitle,
                null,
                NLS.bind(IDEWorkbenchMessages.DeleteResourceAction_outOfSyncQuestion, resource.getName()),
                MessageDialog.QUESTION, new String[] {
                        IDialogConstants.YES_LABEL,
                        IDialogConstants.YES_TO_ALL_LABEL,
                        IDialogConstants.NO_LABEL,
                        IDialogConstants.CANCEL_LABEL }, 0);
        shell.getDisplay().syncExec(new Runnable() {
            public void run() {
                dialog.open();
            }
        });
        int result = dialog.getReturnCode();
        if (result == 0)
            return IDialogConstants.YES_ID;
        if (result == 1)
            return IDialogConstants.YES_TO_ALL_ID;
        if (result == 2)
            return IDialogConstants.NO_ID;
        return IDialogConstants.CANCEL_ID;
    }
}
