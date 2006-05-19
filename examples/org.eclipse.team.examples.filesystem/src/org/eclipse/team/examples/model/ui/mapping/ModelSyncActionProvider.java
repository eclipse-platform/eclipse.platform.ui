/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.examples.model.ui.mapping;

import org.eclipse.core.commands.*;
import org.eclipse.core.runtime.Assert;
import org.eclipse.team.ui.mapping.MergeActionHandler;
import org.eclipse.team.ui.mapping.SynchronizationActionProvider;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

/**
 * The action provider that is used for synchronizations.
 */
public class ModelSyncActionProvider extends SynchronizationActionProvider {

	/** Delegate for merge action handlers */
	private final class ActionHandlerDelegate extends AbstractHandler {

		/** The delegate handler */
		private final IHandler fDelegateHandler;

		/**
		 * Creates a new synchronization handler delegate.
		 * 
		 * @param handler
		 *            the delegate handler
		 */
		public ActionHandlerDelegate(final IHandler handler) {
			Assert.isNotNull(handler);
			fDelegateHandler= handler;
		}

		/**
		 * {@inheritDoc}
		 */
		public void dispose() {
			fDelegateHandler.dispose();
			super.dispose();
		}

		/**
		 * {@inheritDoc}
		 */
		public Object execute(final ExecutionEvent event) throws ExecutionException {
			return fDelegateHandler.execute(event);
		}

		/**
		 * {@inheritDoc}
		 */
		public boolean isEnabled() {
			return fDelegateHandler.isEnabled();
		}
	}
	
	public ModelSyncActionProvider() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.mapping.SynchronizationActionProvider#initialize()
	 */
	protected void initialize() {
		super.initialize();
		final ISynchronizePageConfiguration configuration= getSynchronizePageConfiguration();
		// TODO: We should provide custom handlers that ensure that the MOD files get updated properly
		// when MOE files are merged.
		registerHandler(MERGE_ACTION_ID, new ActionHandlerDelegate(MergeActionHandler.getDefaultHandler(MERGE_ACTION_ID, configuration)));
		registerHandler(OVERWRITE_ACTION_ID, new ActionHandlerDelegate(MergeActionHandler.getDefaultHandler(OVERWRITE_ACTION_ID, configuration)));
		// We can just use the default mark as merged handler
	}
}
