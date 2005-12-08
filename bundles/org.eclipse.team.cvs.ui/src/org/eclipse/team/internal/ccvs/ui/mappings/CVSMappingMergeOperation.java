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
package org.eclipse.team.internal.ccvs.ui.mappings;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.mapping.ModelProvider;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.resources.mapping.ResourceMappingContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.team.core.mapping.IMergeContext;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.ui.*;
import org.eclipse.team.internal.ccvs.ui.operations.CacheBaseContentsOperation;
import org.eclipse.team.internal.ccvs.ui.operations.CacheRemoteContentsOperation;
import org.eclipse.team.internal.ccvs.ui.subscriber.WorkspaceSynchronizeParticipant;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.team.ui.TeamUI;
import org.eclipse.team.ui.operations.ResourceMappingMergeOperation;
import org.eclipse.team.ui.synchronize.ISynchronizeParticipant;
import org.eclipse.team.ui.synchronize.ISynchronizeView;
import org.eclipse.team.ui.synchronize.ResourceScope;
import org.eclipse.ui.IWorkbenchPart;

public class CVSMappingMergeOperation extends ResourceMappingMergeOperation {

	protected static final String UPDATE_CLIENT_MERGE_INFO = "update_client_merge_info_prompt"; //$NON-NLS-1$
	
	public CVSMappingMergeOperation(IWorkbenchPart part, ResourceMapping[] selectedMappings, ResourceMappingContext context) {
		super(part, selectedMappings, context);
	}

	protected IMergeContext buildMergeContext(IProgressMonitor monitor) {
		monitor.beginTask(null, 100);
		IMergeContext context = CVSMergeContext.createContext(getScope(), Policy.subMonitorFor(monitor, 50));
		// cache the base and remote contents
		// TODO: Refreshing and caching now takes 3 round trips.
		// OPTIMIZE: remote state and contents could be obtained in 1
		// OPTIMIZE: Based could be avoided if we always cached base locally
		try {
			new CacheBaseContentsOperation(getPart(), getScope().getMappings(), context.getSyncInfoTree(), true).run(Policy.subMonitorFor(monitor, 25));
			new CacheRemoteContentsOperation(getPart(), getScope().getMappings(), context.getSyncInfoTree()).run(Policy.subMonitorFor(monitor, 25));
		} catch (InvocationTargetException e) {
			CVSUIPlugin.log(CVSException.wrapException(e));
		} catch (InterruptedException e) {
			// Ignore
		}
		monitor.done();
		return context;
	}

	protected void requiresManualMerge(ModelProvider[] providers, IMergeContext context) throws CoreException {

		// Sync Action
		ResourceScope scope = new ResourceScope(getScope().getRoots()); //create resource scope from here; sync
		final WorkspaceSynchronizeParticipant participant = new WorkspaceSynchronizeParticipant(scope);
		TeamUI.getSynchronizeManager().addSynchronizeParticipants(new ISynchronizeParticipant[] {participant});

		provideInfo();

		Display display = getShell().getDisplay();
		if (display != null && !display.isDisposed()) {
			display.asyncExec(new Runnable() {
				public void run() {
					ISynchronizeView view = TeamUI.getSynchronizeManager().showSynchronizeViewInActivePage();
					if (view != null) {
						view.display(participant);
					}
				}
			});
		}
	}

	private void provideInfo() {
		getShell().getDisplay().syncExec(new Runnable() {
			public void run() {
				IPreferenceStore store = TeamUIPlugin.getPlugin().getPreferenceStore();

				if (store.getBoolean(UPDATE_CLIENT_MERGE_INFO)) {
					return;
				}
				MessageDialogWithToggle dialog = MessageDialogWithToggle.openInformation(getShell(), CVSUIMessages.CVSMappingMergeOperation_MergeInfoTitle, CVSUIMessages.CVSMappingMergeOperation_MergeInfoText, CVSUIMessages.AvoidableMessageDialog_dontShowAgain, false, null, null);
				store.setValue(UPDATE_CLIENT_MERGE_INFO, dialog.getToggleState());
			}
		});
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.TeamOperation#canRunAsJob()
	 */
	protected boolean canRunAsJob() {
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.TeamOperation#getJobName()
	 */
	protected String getJobName() {
		return CVSUIMessages.UpdateOperation_taskName;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.operations.ResourceMappingMergeOperation#isAttemptHeadlessMerge()
	 */
	protected boolean isAttemptHeadlessMerge() {
		return CVSUIPlugin.getPlugin().getPreferenceStore().getString(ICVSUIConstants.PREF_UPDATE_HANDLING).equals(ICVSUIConstants.PREF_UPDATE_HANDLING_PERFORM);
	}

}
