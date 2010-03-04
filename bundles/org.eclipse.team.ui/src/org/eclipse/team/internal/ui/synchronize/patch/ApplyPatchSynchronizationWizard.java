/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.synchronize.patch;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.internal.patch.*;
import org.eclipse.core.resources.*;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.team.core.subscribers.SubscriberMergeContext;
import org.eclipse.team.core.subscribers.SubscriberScopeManager;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.ui.IConfigurationWizard;
import org.eclipse.team.ui.TeamUI;
import org.eclipse.team.ui.synchronize.ISynchronizeParticipant;
import org.eclipse.team.ui.synchronize.ModelSynchronizeParticipant;
import org.eclipse.ui.IWorkbench;

public class ApplyPatchSynchronizationWizard extends PatchWizard implements
		IConfigurationWizard {

	public ApplyPatchSynchronizationWizard() {
		// TODO: get selection, available when launched from toolbar or main
		// menu
		super((IStorage) null, (IResource) null, new CompareConfiguration());
	}

	public boolean performFinish() {
		ApplyPatchSubscriber subscriber = new ApplyPatchSubscriber(getPatcher());

		// Get ResourceMappings for root resources from the patch.
		ResourceMapping[] inputMappings = Utils.getResourceMappings(subscriber
				.roots());

		// Take the given mappings, consult logical models and construct the
		// complete set of resources to be operated on.
		// Use SubscriberResourceMappingContext which uses subscriber to access
		// to the remote state of local resources.
		SubscriberScopeManager scopeManager = new SubscriberScopeManager(
				subscriber.getName(), inputMappings, subscriber, true);

		// Initialize the diff tree.
		// TODO: are we going to perform head-less auto-merges? do we need to
		// subclass MergeContext?
		SubscriberMergeContext mergeContext = ApplyPatchSubscriberMergeContext
				.createContext(subscriber, scopeManager);

		// Create the participant and show it.
		ModelSynchronizeParticipant participant = new ApplyPatchModelSynchronizeParticipant(
				mergeContext);
		TeamUI.getSynchronizeManager().addSynchronizeParticipants(
				new ISynchronizeParticipant[] { participant });
		// We don't know in which site to show progress because a participant
		// could actually be shown in multiple sites.
		participant.run(null /* no site */);
		return true;
	}

	public void init(IWorkbench workbench, IProject project) {
		// nothing to do here
	}

	public void addPages() {
		if (getPatch() == null)
			addPage(fPatchWizardPage = new InputPatchPage(this));
		if (getPatch() == null || !getPatcher().isWorkspacePatch())
			addPage(fPatchTargetPage = new PatchTargetPage(getPatcher()));
		addPage(new PatchParsedPage());
	}

	public boolean canFinish() {
		IWizardPage currentPage = getContainer().getCurrentPage();
		if (currentPage.getName()
				.equals(PatchParsedPage.PATCH_PARSED_PAGE_NAME)) {
			return currentPage.isPageComplete();
		}
		return super.canFinish();
	}

	public WorkspacePatcher getPatcher() {
		// make the patcher available to other classes in the package
		return super.getPatcher();
	}
}
