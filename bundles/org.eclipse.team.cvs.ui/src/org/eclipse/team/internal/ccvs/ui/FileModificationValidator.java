/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui;

import java.lang.reflect.InvocationTargetException;
import java.net.URL;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSCoreFileModificationValidator;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.ui.actions.EditorsAction;
import org.eclipse.ui.progress.IProgressConstants;

/**
 * IFileModificationValidator that is pluged into the CVS Repository Provider
 */
public class FileModificationValidator extends CVSCoreFileModificationValidator {
	
	public FileModificationValidator() {
	}
	
	/* (non-Javadoc)
     * @see org.eclipse.team.internal.ccvs.core.CVSCoreFileModificationValidator#edit(org.eclipse.core.resources.IFile[], java.lang.Object)
     */
    protected IStatus edit(IFile[] readOnlyFiles, Object context) {
        return edit(readOnlyFiles, getShell(context));
    }
    
	private Shell getShell(Object context) {
		if (context instanceof Shell)
			return (Shell)context;
		return null;
	}

	private IStatus getStatus(InvocationTargetException e) {
		Throwable target = e.getTargetException();
		if (target instanceof TeamException) {
			return ((TeamException) target).getStatus();
		} else if (target instanceof CoreException) {
			return ((CoreException) target).getStatus();
		}
		return new Status(IStatus.ERROR, CVSUIPlugin.ID, 0, CVSUIMessages.internal, target); //$NON-NLS-1$
	}
		
	private IStatus edit(final IFile[] files, final Shell shell) {
		if (isPerformEdit()) {
			try {
				if (shell != null && !promptToEditFiles(files, shell)) {
					// The user didn't want to edit.
					// OK is returned but the file remains read-only
					throw new InterruptedException();
				}
				
				// Run the edit in a runnable in order to get a busy cursor.
				// This runnable is syncExeced in order to get a busy cursor
				IRunnableWithProgress editRunnable = new IRunnableWithProgress() {
		        	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		        		try {
		        			performEdit(files, monitor);
		        		} catch (CVSException e) {
		        			new InvocationTargetException(e);
		        		}
		        	}
		        };
				if (isRunningInUIThread()) {
				    // Only show a busy cursor if validate edit is blocking the UI
	                CVSUIPlugin.runWithProgress(shell, false, editRunnable);
				} else {
				    // We can't show a busy cursor (i.e., run in the UI thread)
				    // since this thread may hold locks and
				    // running an edit in the UI thread could try to obtain the
				    // same locks, resulting in a deadlock.
				    editRunnable.run(new NullProgressMonitor());
				}
			} catch (InvocationTargetException e) {
				return getStatus(e);
			} catch (InterruptedException e) {
				// Must return an error to indicate that it is not OK to edit the files
				return new Status(IStatus.CANCEL, CVSUIPlugin.ID, 0, CVSUIMessages.FileModificationValidator_vetoMessage, null); //$NON-NLS-1$;
			}
        } else if (isPerformEditInBackground()) {
            IStatus status = setWritable(files);
            if (status.isOK())
                performEdit(files);
            return status;
		} else {
			// Allow the files to be edited without notifying the server
			return setWritable(files);
		}

		return Status.OK_STATUS;
		
	}
    
    protected void scheduleEditJob(Job job) {
        job.setProperty(IProgressConstants.NO_IMMEDIATE_ERROR_PROMPT_PROPERTY, Boolean.TRUE);
        job.setProperty(IProgressConstants.ICON_PROPERTY, getOperationIcon());
        super.scheduleEditJob(job);
    }
    
    private URL getOperationIcon() {
        return Platform.find(CVSUIPlugin.getPlugin().getBundle(), new Path(ICVSUIConstants.ICON_PATH + ICVSUIConstants.IMG_CVS_PERSPECTIVE));
    }
    
    private boolean isRunningInUIThread() {
        return Display.getCurrent() != null;
    }

    private boolean promptToEditFiles(IFile[] files, Shell shell) throws InvocationTargetException, InterruptedException {
		if (files.length == 0)
			return true;		

		if(isNeverPrompt())	
			return true;

		// Contact the server to see if anyone else is editing the files
		EditorsAction editors = fetchEditors(files, shell);
		if (editors.isEmpty()) {
			if (isAlwaysPrompt()) 
				return (promptEdit(shell));
			return true;
		} else {
			return (editors.promptToEdit(shell));
		}
	}
	
	private boolean promptEdit(Shell shell) {
		// Open the dialog using a sync exec (there are no guarentees that we
		// were called from the UI thread
		final boolean[] result = new boolean[] { false };
		int flags = isRunningInUIThread() ? 0 : CVSUIPlugin.PERFORM_SYNC_EXEC;
		CVSUIPlugin.openDialog(shell, new CVSUIPlugin.IOpenableInShell() {
			public void open(Shell shell) {
				result[0] = MessageDialog.openQuestion(shell,CVSUIMessages.FileModificationValidator_3,CVSUIMessages.FileModificationValidator_4); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}, flags);
		return result[0];
	}

	private boolean isPerformEdit() {
		return ICVSUIConstants.PREF_EDIT_PROMPT_EDIT.equals(CVSUIPlugin.getPlugin().getPreferenceStore().getString(ICVSUIConstants.PREF_EDIT_ACTION));
	}
    
    private boolean isPerformEditInBackground() {
        return ICVSUIConstants.PREF_EDIT_IN_BACKGROUND.equals(CVSUIPlugin.getPlugin().getPreferenceStore().getString(ICVSUIConstants.PREF_EDIT_ACTION));
    }
	
	private EditorsAction fetchEditors(IFile[] files, Shell shell) throws InvocationTargetException, InterruptedException {
		final EditorsAction editors = new EditorsAction(getProvider(files), files);
		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				editors.run(monitor);
			}
		};
		if (isRunningInUIThread()) {
		    // Show a busy cursor if we are running in the UI thread
		    CVSUIPlugin.runWithProgress(shell, false, runnable);
		} else {
		    // We can't show a busy cursor (i.e., run in the UI thread)
		    // since this thread may hold locks and
		    // running a CVS operation in the UI thread could try to obtain the
		    // same locks, resulting in a deadlock.
		    runnable.run(new NullProgressMonitor());
		}
		return editors;
	}

	private boolean isNeverPrompt() {
		return ICVSUIConstants.PREF_EDIT_PROMPT_NEVER.equals(CVSUIPlugin.getPlugin().getPreferenceStore().getString(ICVSUIConstants.PREF_EDIT_PROMPT));
	}

	private boolean isAlwaysPrompt() {
		return ICVSUIConstants.PREF_EDIT_PROMPT_ALWAYS.equals(CVSUIPlugin.getPlugin().getPreferenceStore().getString(ICVSUIConstants.PREF_EDIT_PROMPT));
	}
}
