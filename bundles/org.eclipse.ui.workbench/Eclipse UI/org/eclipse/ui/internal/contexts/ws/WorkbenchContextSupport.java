/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.contexts.ws;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.commands.contexts.ContextManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISources;
import org.eclipse.ui.LegacyHandlerSubmissionExpression;
import org.eclipse.ui.contexts.EnabledSubmission;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextManager;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.contexts.IWorkbenchContextSupport;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.contexts.ContextManagerFactory;
import org.eclipse.ui.internal.contexts.ContextManagerWrapper;
import org.eclipse.ui.internal.keys.WorkbenchKeyboard;

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
	 * The context service for the workbench. This value is never
	 * <code>null</code>.
	 */
	private IContextService contextService;

	/**
	 * The key binding support for the contexts. In the workbench, key bindings
	 * are intimately tied to the context mechanism.
	 */
	private WorkbenchKeyboard keyboard;

	/**
	 * The legacy context manager supported by this application.
	 */
	private ContextManagerWrapper contextManagerWrapper;

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
		contextService = (IContextService) workbench
				.getAdapter(IContextService.class);
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
			sourcePriorities |= ISources.ACTIVE_PART;
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
						enabledSubmission.getActiveWorkbenchPartSite()),
				sourcePriorities);
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

	/**
	 * An accessor for the underlying key binding support. This method is
	 * internal, and is not intended to be used by clients. It is currently only
	 * used for testing purposes.
	 * 
	 * @return A reference to the key binding support; never <code>null</code>.
	 */
	public final WorkbenchKeyboard getKeyboard() {
		return keyboard;
	}

	public final int getShellType(Shell shell) {
		return contextService.getShellType(shell);
	}

	/**
	 * Initializes the key binding support.
	 */
	public final void initialize() {
		// Hook up the key binding support.
		keyboard = new WorkbenchKeyboard(workbench);
		final Display display = workbench.getDisplay();
		final Listener listener = keyboard.getKeyDownFilter();
		display.addFilter(SWT.KeyDown, listener);
		display.addFilter(SWT.Traverse, listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.contexts.IWorkbenchContextSupport#isKeyFilterEnabled()
	 */
	public final boolean isKeyFilterEnabled() {
		return keyboard.getKeyDownFilter().isEnabled();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.contexts.IWorkbenchContextSupport#openKeyAssistDialog()
	 */
	public final void openKeyAssistDialog() {
		keyboard.openMultiKeyAssistShell();
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
		keyboard.getKeyDownFilter().setEnabled(enabled);
	}

	public final boolean unregisterShell(final Shell shell) {
		return contextService.unregisterShell(shell);
	}
}
