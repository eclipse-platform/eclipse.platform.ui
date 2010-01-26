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

import org.eclipse.core.resources.mapping.ModelProvider;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.team.core.mapping.provider.SynchronizationContext;
import org.eclipse.team.ui.TeamUI;
import org.eclipse.team.ui.mapping.SynchronizationActionProvider;
import org.eclipse.team.ui.synchronize.*;

public class ApplyPatchModelSynchronizeParticipant extends
		ModelSynchronizeParticipant {

	public static final String ID = "org.eclipse.team.ui.applyPatchModelParticipant"; //$NON-NLS-1$

	public ApplyPatchModelSynchronizeParticipant(SynchronizationContext context) {
		super(context);
		init();
	}

	private void init() {
		try {
			ISynchronizeParticipantDescriptor descriptor = TeamUI
					.getSynchronizeManager().getParticipantDescriptor(ID);
			setInitializationData(descriptor);
			setSecondaryId(Long.toString(System.currentTimeMillis()));
		} catch (CoreException e) {
			// ignore
		}
	}

	protected void initializeConfiguration(
			final ISynchronizePageConfiguration configuration) {
		super.initializeConfiguration(configuration);
		configuration
				.setSupportedModes(ISynchronizePageConfiguration.INCOMING_MODE
						| ISynchronizePageConfiguration.CONFLICTING_MODE);
		configuration.setMode(ISynchronizePageConfiguration.INCOMING_MODE);
	}

	protected ModelSynchronizeParticipantActionGroup createMergeActionGroup() {
		return new ApplyPatchModelSynchronizeParticipantActionGroup();
	}

	public class ApplyPatchModelSynchronizeParticipantActionGroup extends
			ModelSynchronizeParticipantActionGroup {
		protected void addToContextMenu(String mergeActionId, Action action,
				IMenuManager manager) {
			if (mergeActionId == SynchronizationActionProvider.OVERWRITE_ACTION_ID) {
				// omit this action
				return;
			} else if (mergeActionId == SynchronizationActionProvider.MARK_AS_MERGE_ACTION_ID) {
				// omit this action
				return;
			}
			super.addToContextMenu(mergeActionId, action, manager);
		}
	}

	public ModelProvider[] getEnabledModelProviders() {
		ModelProvider[] enabledProviders = super.getEnabledModelProviders();
		// add Patch model provider if it's not there
		for (int i = 0; i < enabledProviders.length; i++) {
			ModelProvider provider = enabledProviders[i];
			if (provider.getId().equals(PatchModelProvider.ID))
				return enabledProviders;
		}
		ModelProvider[] extended = new ModelProvider[enabledProviders.length + 1];
		for (int i = 0; i < enabledProviders.length; i++) {
			extended[i] = enabledProviders[i];
		}
		PatchModelProvider provider = PatchModelProvider.getProvider();
		if (provider == null)
			return enabledProviders;
		extended[extended.length - 1] = provider;
		return extended;
	}
}
