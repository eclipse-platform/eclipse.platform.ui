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
import org.eclipse.team.ccvs.core.ICVSFolder;
import org.eclipse.team.ccvs.core.ICVSRemoteFolder;
import org.eclipse.team.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.ccvs.core.ICVSResource;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.core.syncinfo.FolderSyncInfo;
import org.eclipse.team.internal.ccvs.core.syncinfo.ResourceSyncInfo;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.ICVSUIConstants;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.RepositoryManager;
import org.eclipse.team.internal.ccvs.ui.model.BranchTag;

public class BranchWizard extends Wizard {
	BranchWizardPage mainPage;
	IResource[] resources;
	
	public BranchWizard() {
		setNeedsProgressMonitor(true);
		setWindowTitle(Policy.bind("BranchWizard.title"));
	}
	
	public void addPages() {
		mainPage = new BranchWizardPage("versionPage", Policy.bind("BranchWizard.createABranch"), areAllResourcesSticky(resources), CVSUIPlugin.getPlugin().getImageDescriptor(ICVSUIConstants.IMG_WIZBAN_BRANCH));
		addPage(mainPage);
	}
	public boolean performFinish() {
		final boolean[] result = new boolean[] {false};
		try {
			getContainer().run(false, false, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException {
					try {
						String tagString = mainPage.getBranchTag();
						boolean update = mainPage.getUpdate();
						String versionString = mainPage.getVersionTag();
						CVSTag rootVersionTag = null;
						CVSTag branchTag = new CVSTag(tagString, CVSTag.BRANCH);
						if (versionString != null) {
							rootVersionTag = new CVSTag(versionString, CVSTag.VERSION);
						}
						boolean eclipseWay = true;
						//boolean eclipseWay = methodPage.getEclipseWay();
						
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
							try {
								if(!areAllResourcesSticky(resources)) {													
									// version everything in workspace with the root version tag specified in dialog
									provider.makeBranch(providerResources, rootVersionTag, branchTag, update, eclipseWay, subMonitor);
								} else {
									// all resources are versions, use that version as the root of the branch
									provider.makeBranch(providerResources, null, branchTag, update, eclipseWay, subMonitor);										
								}
								if (rootVersionTag != null) {
									for (int i = 0; i < providerResources.length; i++) {
										ICVSRemoteFolder remoteResource = (ICVSRemoteFolder) CVSWorkspaceRoot.getRemoteResourceFor(providerResources[i]);
										manager.addVersionTags(remoteResource, new CVSTag[] { rootVersionTag });
									}
								}
								if (update) {
									manager.addBranchTags(root, new BranchTag[] { new BranchTag(branchTag, root) });
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
	
	private boolean areAllResourcesSticky(IResource[] resources) {
		for (int i = 0; i < resources.length; i++) {
			if(!hasStickyTag(resources[i])) return false;
		}
		return true;
	}
	
	private boolean hasStickyTag(IResource resource) {
		try {
			ICVSResource cvsResource = CVSWorkspaceRoot.getCVSResourceFor(resource);			
			CVSTag tag;
			if(cvsResource.isFolder()) {
				FolderSyncInfo folderInfo = ((ICVSFolder)cvsResource).getFolderSyncInfo();
				tag = folderInfo.getTag();
			} else {
				ResourceSyncInfo info = cvsResource.getSyncInfo();
				tag = info.getTag();
			}
			if(tag!=null) {
				int tagType = tag.getType();
				if(tagType==tag.VERSION) {
					return true;
				}
			}
		} catch(CVSException e) {
			CVSUIPlugin.log(e.getStatus());
			return false;
		}
		return false;
	}
}
