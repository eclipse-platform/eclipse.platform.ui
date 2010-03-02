/*******************************************************************************
 * Copyright (c) 2003, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.contexts;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.commands.contexts.ContextManager;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISources;
import org.eclipse.ui.LegacyHandlerSubmissionExpression;
import org.eclipse.ui.contexts.EnabledSubmission;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextManager;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.contexts.IWorkbenchContextSupport;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.keys.IBindingService;

/**
 * Provides support for contexts within the workbench -- including key bindings,
 * and some default contexts for shell types.
 * 
 * @since 3.0
 */
public class WorkbenchContextSupport implements IWorkbenchContextSupport {

	/**
	 * The map of activations that have been given to the handler service (<code>IHandlerActivation</code>),
	 * indexed by the submissions (<code>HandlerSubmission</code>). This map
	 * should be <code>null</code> if there are no such activations.
	 */
	private Map activationsBySubmission = null;

	/**
	 * The binding service for the workbench. This value is never
	 * <code>null</code>.
	 */
	private IBindingService bindingService;

	/**
	 * The context service for the workbench. This value is never
	 * <code>null</code>.
	 */
	private IContextService contextService;

	/**
	 * The legacy context manager supported by this application.
	 */
	private ContextManagerLegacyWrapper contextManagerWrapper;

	/**
	 * The workbench for which context support is being provided. This value
	 * must not be <code>null</code>.
	 */
	private final Workbench workbench;

	/**
	 * Constructs a new instance of <code>WorkbenchCommandSupport</code>.
	 * This attaches the key binding support, and adds a global shell activation
	 * filter.
	 * 
	 * @param workbenchToSupport
	 *            The workbench that needs to be supported by this instance;
	 *            must not be <code>null</code>.
	 * @param contextManager
	 *            The context manager to be wrappered; must not be
	 *            <code>null</code>.
	 */
	public WorkbenchContextSupport(final Workbench workbenchToSupport,
			final ContextManager contextManager) {
		workbench = workbenchToSupport;
		contextService = (IContextService) workbench.getService(IContextService.class);
		bindingService = (IBindingService) workbench.getService(IBindingService.class);
		contextManagerWrapper = ContextManagerFactory
				.getContextManagerWrapper(contextManager);
	}

	public final void addEnabledSubmission(
			final EnabledSubmission enabledSubmission) {
		/*
		 * Create the source priorities based on the conditions mentioned in the
		 * submission.
		 */
		int sourcePriorities = 0;
		if (enabledSubmission.getActivePartId() != null) {
			sourcePriorities |= ISources.ACTIVE_PART_ID;
		}
		if (enabledSubmission.getActiveShell() != null) {
			sourcePriorities |= (ISources.ACTIVE_SHELL | ISources.ACTIVE_WORKBENCH_WINDOW);
		}
		if (enabledSubmission.getActiveWorkbenchPartSite() != null) {
			sourcePriorities |= ISources.ACTIVE_SITE;
		}

		final IContextActivation activation = contextService.activateContext(
				enabledSubmission.getContextId(),
				new LegacyHandlerSubmissionExpression(enabledSubmission
						.getActivePartId(), enabledSubmission.getActiveShell(),
						enabledSubmission.getActiveWorkbenchPartSite()));
		if (activationsBySubmission == null) {
			activationsBySubmission = new HashMap();
		}
		activationsBySubmission.put(enabledSubmission, activation);
	}

	public final void addEnabledSubmissions(final Collection enabledSubmissions) {
		final Iterator submissionItr = enabledSubmissions.iterator();
		while (submissionItr.hasNext()) {
			addEnabledSubmission((EnabledSubmission) submissionItr.next());
		}
	}

	public final IContextManager getContextManager() {
		return contextManagerWrapper;
	}

	public final int getShellType(Shell shell) {
		return contextService.getShellType(shell);
	}

	public final boolean isKeyFilterEnabled() {
		return bindingService.isKeyFilterEnabled();
	}

	public final void openKeyAssistDialog() {
		bindingService.openKeyAssistDialog();
	}

	public final boolean registerShell(final Shell shell, final int type) {
		return contextService.registerShell(shell, type);
	}

	public final void removeEnabledSubmission(
			final EnabledSubmission enabledSubmission) {
		if (activationsBySubmission == null) {
			return;
		}

		final Object value = activationsBySubmission.remove(enabledSubmission);
		if (value instanceof IContextActivation) {
			final IContextActivation activation = (IContextActivation) value;
			contextService.deactivateContext(activation);
		}
	}

	public final void removeEnabledSubmissions(
			final Collection enabledSubmissions) {
		final Iterator submissionItr = enabledSubmissions.iterator();
		while (submissionItr.hasNext()) {
			removeEnabledSubmission((EnabledSubmission) submissionItr.next());
		}
	}

	public final void setKeyFilterEnabled(final boolean enabled) {
		bindingService.setKeyFilterEnabled(enabled);
	}

	public final boolean unregisterShell(final Shell shell) {
		return contextService.unregisterShell(shell);
	}
}
