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

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;

import org.eclipse.core.commands.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Event;
import org.eclipse.team.internal.ui.*;
import org.eclipse.team.ui.mapping.*;
import org.eclipse.team.ui.operations.ModelSynchronizeParticipant;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
import org.eclipse.ui.PlatformUI;

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
		IHandler handler = getHandler();
		if (handler != null && handler.isEnabled()) {
			final ISaveableCompareModel currentBuffer = ((ModelSynchronizeParticipant)configuration.getParticipant()).getCurrentModel();
			if (currentBuffer != null && currentBuffer.isDirty()) {
				ISaveableCompareModel targetBuffer = null;
				if (handler instanceof MergeActionHandler) {
					MergeActionHandler mah = (MergeActionHandler) handler;
					targetBuffer = mah.getTargetBuffer();
				}
				final ISaveableCompareModel target = targetBuffer;
				try {
					PlatformUI.getWorkbench().getProgressService().run(true, true, new IRunnableWithProgress() {
						public void run(IProgressMonitor monitor) throws InvocationTargetException,
								InterruptedException {
							try {
								ModelProviderAction.handleBufferChange(configuration.getSite().getShell(), target, currentBuffer, true, monitor);
							} catch (CoreException e) {
								throw new InvocationTargetException(e);
							}
						}			
					});
				} catch (InvocationTargetException e) {
					Utils.handle(e);
					return;
				} catch (InterruptedException e) {
					return;
				}
				((ModelSynchronizeParticipant)configuration.getParticipant()).setCurrentModel(targetBuffer);
			}
			try {
				handler.execute(new ExecutionEvent(null, Collections.EMPTY_MAP, event, null));
			} catch (ExecutionException e) {
				handle(e);
			}
		}
	}

	/**
	 * Handle the given exception. By default, the user is prompted
	 * @param exception the exception
	 */
	protected void handle(Throwable exception) {
		if (exception instanceof ExecutionException) {
			ExecutionException ee = (ExecutionException) exception;
			if (ee.getCause() != null) {
				handle(exception.getCause());
			}
		}
		IStatus status;
		if (exception instanceof CoreException) {
			CoreException ce = (CoreException) exception;
			status = ce.getStatus();
		} else {
			status = new Status(IStatus.ERROR, TeamUIPlugin.ID, 0, TeamUIMessages.exception, exception);
		}
		ErrorDialog.openError(configuration.getSite().getShell(), null, null, status);
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
