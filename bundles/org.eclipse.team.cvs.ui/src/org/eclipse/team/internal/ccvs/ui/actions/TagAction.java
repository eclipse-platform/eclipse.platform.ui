package org.eclipse.team.internal.ccvs.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.CVSStatus;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.CVSTeamProvider;
import org.eclipse.team.internal.ccvs.core.ICVSFile;
import org.eclipse.team.internal.ccvs.core.ICVSResource;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.ui.actions.TeamAction;

/**
 * TagAction tags the selected resources with a version tag specified by the user.
 */
public class TagAction extends TeamAction {
	// The previously remembered tag
	private static String previousTag = ""; //$NON-NLS-1$
	
	/**
	 * Copied from CVSDecorationRunnable
	 */
	protected boolean isDirty(IResource resource) {
		final CoreException DECORATOR_EXCEPTION = new CoreException(new Status(IStatus.OK, "id", 1, "", null)); //$NON-NLS-1$ //$NON-NLS-2$
		try {
			resource.accept(new IResourceVisitor() {
				public boolean visit(IResource resource) throws CoreException {

					// a project can't be dirty, continue with its children
					if (resource.getType() == IResource.PROJECT) {
						return true;
					}
					
					// if the resource does not exist in the workbench or on the file system, stop searching.
					if (!resource.exists()) {
						return false;
					}

					ICVSResource cvsResource = CVSWorkspaceRoot.getCVSResourceFor(resource);
					try {
						if (!cvsResource.isManaged()) {
							if (cvsResource.isIgnored()) {
								return false;
							} else {
								// new resource, show as dirty
								throw DECORATOR_EXCEPTION;
							}
						}
						if (!cvsResource.isFolder()) {
							if (((ICVSFile) cvsResource).isModified()) {
								// file has changed, show as dirty
								throw DECORATOR_EXCEPTION;
							}
						}
					} catch (CVSException e) {
						return true;
					}
					// no change -- keep looking in children
					return true;
				}
			}, IResource.DEPTH_INFINITE, true);
		} catch (CoreException e) {
			//if our exception was caught, we know there's a dirty child
			return e == DECORATOR_EXCEPTION;
		}
		return false;
	}
	
	/*
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		final List messages = new ArrayList();
		run(new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {
					final IResource[] resources = getSelectedResources();
					boolean isAnyDirty = false;
					for (int i = 0; i < resources.length; i++) {
						if(isDirty(resources[i])) { 
							isAnyDirty = true;
						}
					}
					
					// Confirm tagging with locally modified resources
					if (isAnyDirty) {
						final Shell shell = getShell();
						final boolean[] result = new boolean[] { false };
						shell.getDisplay().syncExec(new Runnable() {
							public void run() {
								result[0] = MessageDialog.openConfirm(getShell(), Policy.bind("TagAction.uncommittedChangesTitle"), Policy.bind("TagAction.uncommittedChanges")); //$NON-NLS-1$ //$NON-NLS-2$
							}
						});
						if (!result[0]) return;
					}
					final String[] result = new String[1];
					getShell().getDisplay().syncExec(new Runnable() {
						public void run() {
							result[0] = promptForTag();
						}
					});
					if (result[0] == null) return;
					Hashtable table = getProviderMapping();
					Set keySet = table.keySet();
					monitor.beginTask("", keySet.size() * 1000); //$NON-NLS-1$
					Iterator iterator = keySet.iterator();
					
					while (iterator.hasNext()) {
						IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 1000);
						CVSTeamProvider provider = (CVSTeamProvider)iterator.next();
						List list = (List)table.get(provider);
						IResource[] providerResources = (IResource[])list.toArray(new IResource[list.size()]);
						CVSTag tag = new CVSTag(result[0], CVSTag.VERSION);
						IStatus status = provider.tag(providerResources, IResource.DEPTH_INFINITE, tag, subMonitor);
						if (status.getCode() != CVSStatus.OK) {
							messages.add(status);
						}
						// Cache the new tag creation even if the tag may of has warnings.
						CVSUIPlugin.getPlugin().getRepositoryManager().addVersionTags(
										CVSWorkspaceRoot.getCVSFolderFor(provider.getProject()), 
										new CVSTag[] {tag});

					}	
					previousTag = result[0];				
				} catch (TeamException e) {
					throw new InvocationTargetException(e);
				}
			}
		}, Policy.bind("TagAction.tagProblemsMessage"), this.PROGRESS_DIALOG); //$NON-NLS-1$
		
		// Check for any status messages and display them
		if (!messages.isEmpty()) {
			boolean error = false;
			MultiStatus combinedStatus = new MultiStatus(CVSUIPlugin.ID, 0, Policy.bind("TagAction.tagProblemsMessage"), null); //$NON-NLS-1$
			for (int i = 0; i < messages.size(); i++) {
				IStatus status = (IStatus)messages.get(i);
				if (status.getSeverity() == IStatus.ERROR || status.getCode() == CVSStatus.SERVER_ERROR) {
					error = true;
				}
				combinedStatus.merge(status);
			}
			String message = null;
			IStatus statusToDisplay;
			if (combinedStatus.getChildren().length == 1) {
				message = combinedStatus.getMessage();
				statusToDisplay = combinedStatus.getChildren()[0];
			} else {
				statusToDisplay = combinedStatus;
			}
			String title;
			if (error) {
				title = Policy.bind("TagAction.tagErrorTitle"); //$NON-NLS-1$
			} else {
				title = Policy.bind("TagAction.tagWarningTitle"); //$NON-NLS-1$
			}
			ErrorDialog.openError(getShell(), title, message, statusToDisplay);
		}		
	}
	
	/*
	 * @see TeamAction#isEnabled()
	 */
	protected boolean isEnabled() throws TeamException {
		IResource[] resources = getSelectedResources();
		if (resources.length == 0) return false;
		for (int i = 0; i < resources.length; i++) {
			IResource resource = resources[i];
			RepositoryProvider provider = RepositoryProvider.getProvider(resource.getProject(), CVSProviderPlugin.getTypeId());
			if (provider == null) return false;
			ICVSResource cvsResource = CVSWorkspaceRoot.getCVSResourceFor(resource);
			if (resource.getType()!=IResource.PROJECT&&!cvsResource.isManaged()) return false;
		}
		return true;
	}

	/**
	 * Prompts the user for a tag name.
	 * @return the tag, or null to cancel
	 */
	protected String promptForTag() {
		// Prompt for the tag
		IInputValidator validator = new IInputValidator() {
			public String isValid(String tagName) {
				IStatus status = CVSTag.validateTagName(tagName);
				if (status.isOK()) {
					return null;
				} else {
					return status.getMessage();
				}
			}
		};
		InputDialog dialog = new InputDialog(getShell(),
			Policy.bind("TagAction.tagResources"), Policy.bind("TagAction.enterTag"), previousTag, validator); //$NON-NLS-1$ //$NON-NLS-2$
		if (dialog.open() != InputDialog.OK) return null;
		return dialog.getValue();
	}
}

