/*******************************************************************************
 * Copyright (c) 2003, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.commands;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.eclipse.core.commands.CommandManager;
import org.eclipse.core.commands.contexts.ContextManager;
import org.eclipse.jface.bindings.BindingManager;
import org.eclipse.ui.LegacyHandlerSubmissionExpression;
import org.eclipse.ui.commands.HandlerSubmission;
import org.eclipse.ui.commands.ICommandManager;
import org.eclipse.ui.commands.IWorkbenchCommandSupport;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.handlers.LegacyHandlerWrapper;

/**
 * Provides command support in terms of the workbench.
 *
 * @since 3.0
 */
public class WorkbenchCommandSupport implements IWorkbenchCommandSupport {

	/**
	 * The map of activations that have been given to the handler service
	 * (<code>IHandlerActivation</code>), indexed by the submissions
	 * (<code>HandlerSubmission</code>). This map should be <code>null</code> if
	 * there are no such activations.
	 */
	private Map<HandlerSubmission, IHandlerActivation> activationsBySubmission = null;

	/**
	 * The mutable command manager that should be notified of changes to the list of
	 * active handlers. This value is never <code>null</code>.
	 */
	private final CommandManagerLegacyWrapper commandManagerWrapper;

	/**
	 * The handler service for the workbench. This value is never <code>null</code>.
	 */
	private final IHandlerService handlerService;

	/**
	 * Constructs a new instance of <code>WorkbenchCommandSupport</code>
	 *
	 * @param bindingManager The binding manager providing support for the command
	 *                       manager; must not be <code>null</code>.
	 * @param commandManager The command manager for the workbench; must not be
	 *                       <code>null</code>.
	 * @param contextManager The context manager providing support for the command
	 *                       manager and binding manager; must not be
	 *                       <code>null</code>.
	 * @param handlerService The handler service for the workbench; must not be
	 *                       <code>null</code>.
	 */
	public WorkbenchCommandSupport(final BindingManager bindingManager, final CommandManager commandManager,
			final ContextManager contextManager, final IHandlerService handlerService) {
		if (handlerService == null) {
			throw new NullPointerException("The handler service cannot be null"); //$NON-NLS-1$
		}

		this.handlerService = handlerService;

		commandManagerWrapper = CommandManagerFactory.getCommandManagerWrapper(bindingManager, commandManager,
				contextManager);

		// Initialize the old key formatter settings.
		org.eclipse.ui.keys.KeyFormatterFactory
				.setDefault(org.eclipse.ui.keys.SWTKeySupport.getKeyFormatterForPlatform());
	}

	@Override
	public final void addHandlerSubmission(final HandlerSubmission handlerSubmission) {
		final IHandlerActivation activation = handlerService.activateHandler(handlerSubmission.getCommandId(),
				new LegacyHandlerWrapper(handlerSubmission.getHandler()),
				new LegacyHandlerSubmissionExpression(handlerSubmission.getActivePartId(),
						handlerSubmission.getActiveShell(), handlerSubmission.getActiveWorkbenchPartSite()));
		if (activationsBySubmission == null) {
			activationsBySubmission = new HashMap<>();
		}
		activationsBySubmission.put(handlerSubmission, activation);
	}

	@Override
	public final void addHandlerSubmissions(final Collection handlerSubmissions) {
		final Iterator<HandlerSubmission> submissionItr = handlerSubmissions.iterator();
		while (submissionItr.hasNext()) {
			addHandlerSubmission(submissionItr.next());
		}
	}

	@Override
	public ICommandManager getCommandManager() {
		return commandManagerWrapper;
	}

	@Override
	public final void removeHandlerSubmission(final HandlerSubmission handlerSubmission) {
		if (activationsBySubmission == null) {
			return;
		}

		final IHandlerActivation activation = activationsBySubmission.remove(handlerSubmission);
		if (activation != null) {
			handlerService.deactivateHandler(activation);
		}
	}

	@Override
	public final void removeHandlerSubmissions(final Collection handlerSubmissions) {
		final Iterator<HandlerSubmission> submissionItr = handlerSubmissions.iterator();
		while (submissionItr.hasNext()) {
			removeHandlerSubmission(submissionItr.next());
		}
	}
}
