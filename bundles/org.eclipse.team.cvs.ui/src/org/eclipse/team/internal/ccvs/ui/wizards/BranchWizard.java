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
import org.eclipse.team.ccvs.core.CVSTag;
import org.eclipse.team.ccvs.core.CVSTeamProvider;
import org.eclipse.team.core.ITeamProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.TeamPlugin;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.Policy;

public class BranchWizard extends Wizard {
	BranchWizardPage mainPage;
	IResource[] resources;
	
	public BranchWizard() {
		setNeedsProgressMonitor(true);
	}
	
	public void addPages() {
		mainPage = new BranchWizardPage("branchPage", Policy.bind("BranchWizard.createABranch"), null);
		addPage(mainPage);
	}
	public boolean performFinish() {
		final boolean[] result = new boolean[] {false};
		try {
			getContainer().run(false, false, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) {
					try {
						String tagString = mainPage.getBranchTag();
						boolean update = mainPage.getUpdate();
						String versionString = mainPage.getVersionTag();
						CVSTag versionTag = null;
						if (versionString != null) {
							versionTag = new CVSTag(versionString, CVSTag.VERSION);
						}
						
						// To do: use the wizard's progress monitor
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
							CVSTag tag = new CVSTag(tagString, CVSTag.BRANCH);
							try {
								if (versionString != null) {
									provider.tag(providerResources, IResource.DEPTH_INFINITE, versionTag, subMonitor);
								}
								provider.tag(providerResources, IResource.DEPTH_INFINITE, tag, subMonitor);
								if (update) {
									provider.update(providerResources, IResource.DEPTH_INFINITE, tag, true, subMonitor);
								}
							} catch (TeamException e) {
								status.merge(e.getStatus());
							}
						}
						if (!status.isOK()) {
							ErrorDialog.openError(getShell(), null, null, status);
						}
						result[0] = true;	
					} finally {
						monitor.done();
					}
				}
			});
		} catch (InterruptedException e) {
			return true;
		} catch (InvocationTargetException e) {
			// no exceptions are explicitly thrown by the above runnable
			// pass through runtime exceptions to catch coding errors
			Throwable target = e.getTargetException();
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
			ITeamProvider provider = TeamPlugin.getManager().getProvider(resources[i].getProject());
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
