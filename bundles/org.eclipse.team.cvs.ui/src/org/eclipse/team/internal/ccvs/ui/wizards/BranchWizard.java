package org.eclipse.team.internal.ccvs.ui.wizards;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.team.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.ccvs.core.CVSTag;
import org.eclipse.team.ccvs.core.CVSTeamProvider;
import org.eclipse.team.ccvs.core.ICVSRemoteFolder;
import org.eclipse.team.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.ICVSUIConstants;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.RepositoryManager;
import org.eclipse.team.internal.ccvs.ui.model.BranchTag;

public class BranchWizard extends Wizard {
	BranchWizardVersionPage versionPage;
	BranchWizardBranchPage branchPage;
	//BranchWizardMethodPage methodPage;
	IResource[] resources;
	
	public BranchWizard() {
		setNeedsProgressMonitor(true);
		setWindowTitle(Policy.bind("BranchWizard.title"));
	}
	
	public void addPages() {
		versionPage = new BranchWizardVersionPage("versionPage", Policy.bind("BranchWizard.createABranch"), CVSUIPlugin.getPlugin().getImageDescriptor(ICVSUIConstants.IMG_WIZBAN_BRANCH));
		addPage(versionPage);
		branchPage = new BranchWizardBranchPage("branchPage", Policy.bind("BranchWizard.createABranch"), CVSUIPlugin.getPlugin().getImageDescriptor(ICVSUIConstants.IMG_WIZBAN_BRANCH));
		addPage(branchPage);
		//methodPage = new BranchWizardMethodPage("methodPage", Policy.bind("BranchWizard.createABranch"), CVSUIPlugin.getPlugin().getImageDescriptor(ICVSUIConstants.IMG_WIZBAN_BRANCH));
		//addPage(methodPage);
	}
	public boolean performFinish() {
		final boolean[] result = new boolean[] {false};
		try {
			getContainer().run(false, false, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException {
					try {
						String tagString = branchPage.getBranchTag();
						boolean update = branchPage.getUpdate();
						String versionString = versionPage.getVersionTag();
						CVSTag versionTag = null;
						if (versionString != null) {
							versionTag = new CVSTag(versionString, CVSTag.VERSION);
						}
						boolean eclipseWay = true;
						//boolean eclipseWay = methodPage.getEclipseWay();
						
						// To do: use the wizard's progress monitor
						RepositoryManager manager = CVSUIPlugin.getPlugin().getRepositoryManager();
						Hashtable table = getProviderMapping(resources);
						Set keySet = table.keySet();
						monitor.beginTask("", keySet.size() * 1000);
						MultiStatus status = new MultiStatus(CVSUIPlugin.ID, IStatus.INFO, Policy.bind("BranchWizard.errorTagging"), null);
						Iterator iterator = keySet.iterator();
						while (iterator.hasNext()) {
							IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 1000);
							CVSTeamProvider provider = (CVSTeamProvider)iterator.next();
							List list = (List)table.get(provider);
							IResource[] providerResources = (IResource[])list.toArray(new IResource[list.size()]);
							ICVSRepositoryLocation root = provider.getCVSWorkspaceRoot().getRemoteLocation();
							CVSTag tag = new CVSTag(tagString, CVSTag.BRANCH);
							try {
								provider.makeBranch(providerResources, versionTag, tag, update, eclipseWay, subMonitor);
								if (versionTag != null) {
									for (int i = 0; i < providerResources.length; i++) {
										ICVSRemoteFolder remoteResource = (ICVSRemoteFolder) CVSWorkspaceRoot.getRemoteResourceFor(providerResources[i]);
										manager.addVersionTags(remoteResource, new CVSTag[] { versionTag });
									}
								}
								if (update) {
									manager.addBranchTags(root, new BranchTag[] { new BranchTag(tag, root) });
								}
							} catch (TeamException e) {
								status.merge(e.getStatus());
							}
						}
						if (!status.isOK()) {
							ErrorDialog.openError(getShell(), null, null, status);
						}
						result[0] = true;
					} catch (CVSException e) {
						throw new InvocationTargetException(e);
					} finally {
						monitor.done();
					}
				}
			});
		} catch (InterruptedException e) {
			return true;
		} catch (InvocationTargetException e) {
			Throwable target = e.getTargetException();
			if (target instanceof CVSException) {
				ErrorDialog.openError(getShell(), null, null, ((CVSException)target).getStatus());
				return false;
			}
			if (target instanceof RuntimeException) {
				throw (RuntimeException)target;
			}
			if (target instanceof Error) {
				throw (Error)target;
			}
		}
		return result[0];
	}
	public void setResources(IResource[] resources) {
		this.resources = resources;
	}
	private Hashtable getProviderMapping(IResource[] resources) {
		Hashtable result = new Hashtable();
		for (int i = 0; i < resources.length; i++) {
			RepositoryProvider provider = RepositoryProvider.getProvider(resources[i].getProject(), CVSProviderPlugin.getTypeId());
			List list = (List)result.get(provider);
			if (list == null) {
				list = new ArrayList();
				result.put(provider, list);
			}
			list.add(resources[i]);
		}
		return result;
	}
}
