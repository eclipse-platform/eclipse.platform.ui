package org.eclipse.team.internal.ccvs.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
import java.lang.reflect.InvocationTargetException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.CVSTeamProvider;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.ICVSResource;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.TagSelectionDialog;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

/**
 * Action for replace with tag.
 */
public class ReplaceWithTagAction extends ReplaceWithAction {
	/*
	 * Method declared on IActionDelegate.
	 */
	public void run(IAction action) {
		
		// Setup the holders
		final IResource[] resource = new IResource[] {null};
		final CVSTag[] tag = new CVSTag[] {null};
		
		// Show a busy cursor while display the tag selection dialog
		run(new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InterruptedException, InvocationTargetException {
				final IResource[] resources = getSelectedResources();
				boolean isAnyDirty = false;
				for (int i = 0; i < resources.length; i++) {
					if(isDirty(resources[i])) { 
						isAnyDirty = true;
					}
				}
				
				// Confirm overwrite of locally modified resources
				if (isAnyDirty) {
					final Shell shell = getShell();
					final boolean[] result = new boolean[] { false };
					shell.getDisplay().syncExec(new Runnable() {
						public void run() {
							result[0] = MessageDialog.openQuestion(getShell(), Policy.bind("question"), Policy.bind("localChanges")); //$NON-NLS-1$ //$NON-NLS-2$
						}
					});
					if (!result[0]) return;
				}
				
				// show the tags for the projects of the selected resources
				IProject[] projects = new IProject[resources.length];
				for (int i = 0; i < resources.length; i++) {
					projects[i] = resources[i].getProject();
				}
				TagSelectionDialog dialog = new TagSelectionDialog(getShell(), projects, Policy.bind("ReplaceWithTagAction.message")); //$NON-NLS-1$
				dialog.setBlockOnOpen(true);
				if (dialog.open() == Dialog.CANCEL) {
					return;
				}
				tag[0] = dialog.getResult();
				
				// For non-projects determine if the tag being loaded is the same as the resource's parent
				// If it's not, warn the user that they will have strange sync behavior
				try {
					for (int i = 0; i < resources.length; i++) {
						if (resources[i].getType() != IResource.PROJECT) {
							ICVSResource cvsResource = CVSWorkspaceRoot.getCVSResourceFor(resources[i]);
							CVSTag parentTag = cvsResource.getParent().getFolderSyncInfo().getTag();
							if ( ! equalTags(tag[0], parentTag)) {
								final Shell shell = getShell();
								final boolean[] result = new boolean[] { false };
								shell.getDisplay().syncExec(new Runnable() {
									public void run() {
										result[0] = MessageDialog.openQuestion(getShell(), Policy.bind("question"), Policy.bind("ReplaceWithTagAction.mixingTags")); //$NON-NLS-1$ //$NON-NLS-2$
									}
								});
								if (!result[0]) tag[0] = null;
								// Only ask once!
								return;
							}
						}
						
					}
				} catch (CVSException e) {
					throw new InvocationTargetException(e);
				}
			}
		}, Policy.bind("ReplaceWithTagAction.replace"), this.PROGRESS_BUSYCURSOR);			 //$NON-NLS-1$
		
		if (tag[0] == null) return;
		
		// Display a progress dialog while replacing
		run(new WorkspaceModifyOperation() {
			public void execute(IProgressMonitor monitor) throws InterruptedException, InvocationTargetException {
				try {
					Hashtable table = getProviderMapping();
					Set keySet = table.keySet();
					monitor.beginTask("", keySet.size() * 1000); //$NON-NLS-1$
					monitor.setTaskName(Policy.bind("ReplaceWithTagAction.replacing", tag[0].getName())); //$NON-NLS-1$
					Iterator iterator = keySet.iterator();
					while (iterator.hasNext()) {
						IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 1000);
						CVSTeamProvider provider = (CVSTeamProvider)iterator.next();
						List list = (List)table.get(provider);
						IResource[] providerResources = (IResource[])list.toArray(new IResource[list.size()]);
						provider.get(providerResources, IResource.DEPTH_INFINITE, tag[0], Policy.subMonitorFor(monitor, 100));
					}
				} catch (TeamException e) {
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				}
			}
		}, Policy.bind("ReplaceWithTagAction.replace"), this.PROGRESS_DIALOG); //$NON-NLS-1$
	}
	
	protected boolean isEnabled() {
		IResource[] resources = getSelectedResources();
		// allow operation for homegeneous multiple selections
		if(resources.length>0) {
			int type = -1;
			for (int i = 0; i < resources.length; i++) {
				IResource resource = resources[i];
				if(type!=-1) {
					if(type!=resource.getType()) return false;
				}
				if(RepositoryProvider.getProvider(resource.getProject(), CVSProviderPlugin.getTypeId()) == null) {
					return false;
				}
				type = resource.getType();
				ICVSResource cvsResource = CVSWorkspaceRoot.getCVSResourceFor(resource);
				if(cvsResource.isFolder()) {
					if( ! ((ICVSFolder)cvsResource).isCVSFolder()) return false;
				} else {
					if( ! cvsResource.isManaged()) return false;
				}
			}
			return true;
		}
		return false;
	}
	
	protected boolean equalTags(CVSTag tag1, CVSTag tag2) {
		if (tag1 == null) tag1 = CVSTag.DEFAULT;
		if (tag2 == null) tag2 = CVSTag.DEFAULT;
		return tag1.equals(tag2);
	}
}
