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
package org.eclipse.team.internal.ui.mapping;

import java.util.Collections;

import org.eclipse.core.commands.*;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.team.ui.mapping.MergeActionHandler;
import org.eclipse.team.ui.operations.ResourceMappingSynchronizeParticipant;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

/**
 * An action that delegates to an appropriate handler when performing 
 * a merge opereration.
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/Team team.
 * </p>
 * 
 * @since 3.2
 */
public class MergeAction extends Action {
	
	private final String handlerId;
	private final CommonMenuManager manager;
	private final ISynchronizePageConfiguration configuration;
	
	public MergeAction(String handlerId, CommonMenuManager manager, ISynchronizePageConfiguration configuration) {
		Assert.isNotNull(handlerId);
		Assert.isNotNull(manager);
		Assert.isNotNull(configuration);
		this.handlerId = handlerId;
		this.manager = manager;
		this.configuration = configuration;
	}
	
	public void runWithEvent(Event event) {
		if (!promptForUnsavedChanges()) {
			return;
		}
		IHandler handler = getHandler();
		if (handler != null && handler.isEnabled()) {
			try {
				handler.execute(new ExecutionEvent(null, Collections.EMPTY_MAP, event, null));
			} catch (ExecutionException e) {
				handle(e);
			}
		}
	}

	private boolean promptForUnsavedChanges() {
		ResourceMappingSynchronizeParticipant participant = (ResourceMappingSynchronizeParticipant)configuration.getParticipant();
		if (participant.hasUnsavedChanges()) {
			return MessageDialog.openQuestion(configuration.getSite().getShell(), "Unsaved Changes", "There are unsaved changes");
		}
		return true;
	}

	private void handle(Throwable e) {
		if (e instanceof ExecutionException) {
			ExecutionException ee = (ExecutionException) e;
			if (ee.getCause() != null) {
				handle(e.getCause());
			}
		}
		//TODO: handle the exception
	}

	private IHandler getHandler() {
		IHandler handler = manager.getHandler(handlerId);
		if (handler == null)
			return getDefaultHandler();
		return handler;
	}
	
	private IHandler getDefaultHandler() {
		return MergeActionHandler.getDefaultHandler(handlerId, configuration);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#isEnabled()
	 */
	public boolean isEnabled() {
		IHandler handler = getHandler();
		return handler != null && handler.isEnabled();
	}

}
