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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.ccvs.core.CVSStatus;
import org.eclipse.team.ccvs.core.CVSTeamProvider;
import org.eclipse.team.core.ITeamProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.TeamPlugin;
import org.eclipse.team.internal.ccvs.core.client.Command.KSubstOption;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.Policy;

/**
 * An operation to change the keyword substitution and optionally commit
 * resources in a CVS repository.
 */
public class SetKeywordSubstitutionOperation implements IRunnableWithProgress {
	private IResource[] resources;
	private int depth;
	private Shell shell;
	private KSubstOption ksubst;

	SetKeywordSubstitutionOperation(IResource[] resources, int depth, KSubstOption ksubst, Shell shell) {
		this.resources = resources;
		this.depth = depth;
		this.shell = shell;
		this.ksubst = ksubst;
	}

	/**
	 * @see IRunnableWithProgress#run(IProgressMonitor)
	 */
	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		List messages = new ArrayList();
		try {
			Hashtable table = getProviderMapping(resources);
			Set keySet = table.keySet();
			monitor.beginTask("", keySet.size() * 1000);
			monitor.setTaskName(Policy.bind("SetKeywordSubstitution.working"));
			Iterator iterator = keySet.iterator();
			
			while (iterator.hasNext()) {
				IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 1000);
				CVSTeamProvider provider = (CVSTeamProvider)iterator.next();
				List list = (List)table.get(provider);
				IResource[] providerResources = (IResource[])list.toArray(new IResource[list.size()]);
				IStatus status = provider.setKeywordSubstitution(providerResources, depth,
					ksubst, subMonitor);
				if (status.getCode() != CVSStatus.OK) {
					messages.add(status);
				}
			}
		} catch (TeamException e) {
			throw new InvocationTargetException(e);
		} finally {
			monitor.done();
		}

		// Check for any status messages and display them
		if (!messages.isEmpty()) {
			boolean error = false;
			MultiStatus combinedStatus = new MultiStatus(CVSUIPlugin.ID, 0,
				Policy.bind("SetKeywordSubstitution.problemsMessage"), null);
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
				title = Policy.bind("SetKeywordSubstitution.errorTitle");
			} else {
				title = Policy.bind("SetKeywordSubstitution.warningTitle");
			}
			ErrorDialog.openError(shell, title, message, statusToDisplay);
		}
	}
	
	/**
	 * Convenience method that maps the given resources to their providers.
	 * The returned Hashtable has keys which are ITeamProviders, and values
	 * which are Lists of IResources that are shared with that provider.
	 * 
	 * @return a hashtable mapping providers to their resources
	 */
	protected Hashtable getProviderMapping(IResource[] resources) {
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