package org.eclipse.team.internal.ccvs.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.util.ArrayList;

import org.eclipse.compare.CompareUI;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.ccvs.core.CVSTag;
import org.eclipse.team.ccvs.core.CVSTeamProvider;
import org.eclipse.team.ccvs.core.ICVSRemoteResource;
import org.eclipse.team.core.ITeamProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.TeamPlugin;
import org.eclipse.team.internal.ccvs.core.resources.FolderSyncInfo;
import org.eclipse.team.internal.ccvs.core.resources.LocalFile;
import org.eclipse.team.internal.ccvs.core.resources.LocalFolder;
import org.eclipse.team.internal.ccvs.core.resources.LocalResource;
import org.eclipse.team.internal.ccvs.core.resources.ResourceSyncInfo;
import org.eclipse.team.internal.ccvs.ui.CVSCompareEditorInput;
import org.eclipse.team.internal.ccvs.ui.CVSResourceNode;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.ResourceEditionNode;
import org.eclipse.team.ui.TeamUIPlugin;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Action for container compare with stream.
 */
public class CompareWithRemoteAction implements IObjectActionDelegate {
	private ISelection selection;
	
	/**
	 * Convenience method: extract all <code>IResources</code> from given selection.
	 * Never returns null.
	 */
	public static IResource[] getResources(ISelection selection) {
		ArrayList tmp = new ArrayList();
		if (selection instanceof IStructuredSelection) {
			Object[] s = ((IStructuredSelection) selection).toArray();
			for (int i = 0; i < s.length; i++) {
				Object o = s[i];
				if (o instanceof IResource) {
					tmp.add(o);
					continue;
				}
				if (o instanceof IAdaptable) {
					IAdaptable a = (IAdaptable) o;
					Object adapter = a.getAdapter(IResource.class);
					if (adapter instanceof IResource)
						tmp.add(adapter);
					continue;
				}
			}
		}
		IResource[] resourceSelection = new IResource[tmp.size()];
		tmp.toArray(resourceSelection);
		return resourceSelection;
	}
	
	/**
	 * Convenience method for getting the current shell.
	 */
	protected Shell getShell() {
		return TeamUIPlugin.getPlugin().getWorkbench().getActiveWorkbenchWindow().getShell();
	}

	/*
	 * Method declared on IActionDelegate.
	 */
	public void run(IAction action) {
		String title = Policy.bind("CompareWithStreamAction.sync");
		try {
			IResource[] resources = getResources(selection);
			if (resources.length != 1) return;
			CVSTeamProvider provider = (CVSTeamProvider)TeamPlugin.getManager().getProvider(resources[0].getProject());

			LocalResource cvsResource = null;
			if(resources[0].getType()==IResource.FILE) {
				cvsResource = new LocalFile(resources[0].getLocation().toFile());
			} else {
				cvsResource = new LocalFolder(resources[0].getLocation().toFile());
			}
			
			
			CVSTag tag = null;
			if(cvsResource.isFolder()) {
				FolderSyncInfo folderInfo = ((LocalFolder)cvsResource).getFolderSyncInfo();
				if(folderInfo!=null) {
					tag = folderInfo.getTag();
				}
			} else {
				ResourceSyncInfo info = cvsResource.getSyncInfo();
				if(info!=null) {					
					tag = info.getTag();
				}
			}
			if(tag==null) {
				if(cvsResource.getParent().isCVSFolder()) {
					tag = cvsResource.getParent().getFolderSyncInfo().getTag();
				} else {
					// XXX: this is wrong :> should return an error
					tag = CVSTag.DEFAULT;
				}
			}
			
			ICVSRemoteResource remoteResource = (ICVSRemoteResource)provider.getRemoteTree(resources[0], tag, new NullProgressMonitor());
			CompareUI.openCompareEditor(new CVSCompareEditorInput(new CVSResourceNode(resources[0]), new ResourceEditionNode(remoteResource)));
		} catch (TeamException e) {
			ErrorDialog.openError(getShell(), title, null, e.getStatus());
		}
	}
	
	/*
	 * Method declared on IActionDelegate.
	 */
	public void selectionChanged(IAction action, ISelection s) {
		selection = s;
		IResource[] resources = getResources(s);
		for (int i = 0; i < resources.length; i++) {
			if (!resources[i].isAccessible()) {
				action.setEnabled(false);
				return;
			}
			ITeamProvider provider = TeamPlugin.getManager().getProvider(resources[i].getProject());
			if (provider == null) {
				action.setEnabled(false);
				return;
			}
			if (!(provider instanceof CVSTeamProvider)) {
				action.setEnabled(false);
				return;
			}
		}
		action.setEnabled(true);
	}
	
	/*
	 * Method declared on IObjectActionDelegate.
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}
}
