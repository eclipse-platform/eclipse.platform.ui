/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 440810
 *******************************************************************************/
package org.eclipse.ui.internal.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.jface.bindings.BindingManager;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ExecutionException;
import org.eclipse.ui.commands.ICommand;
import org.eclipse.ui.commands.ICommandListener;
import org.eclipse.ui.commands.NotDefinedException;
import org.eclipse.ui.commands.NotHandledException;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.keys.KeySequenceBinding;
import org.eclipse.ui.keys.IBindingService;
import org.eclipse.ui.keys.KeySequence;

/**
 * A wrapper around a core command so that it satisfies the deprecated
 * <code>ICommand</code> interface.
 *
 * @since 3.1
 */
final class CommandLegacyWrapper implements ICommand {

	/**
	 * The supporting binding manager; never <code>null</code>.
	 */
	private final BindingManager bindingManager;

	/**
	 * The wrapped command; never <code>null</code>.
	 */
	private final Command command;

	/**
	 * A parameterized representation of the command. This is created lazily. If
	 * it has not yet been created, it is <code>null</code>.
	 */
	private ParameterizedCommand parameterizedCommand;

	/**
	 * Constructs a new <code>CommandWrapper</code>
	 *
	 * @param command
	 *            The command to be wrapped; must not be <code>null</code>.
	 * @param bindingManager
	 *            The binding manager to support this wrapper; must not be
	 *            <code>null</code>.
	 */
	CommandLegacyWrapper(final Command command,
			final BindingManager bindingManager) {
		if (command == null) {
			throw new NullPointerException(
					"The wrapped command cannot be <code>null</code>."); //$NON-NLS-1$
		}

		if (bindingManager == null) {
			throw new NullPointerException(
					"A binding manager is required to wrap a command"); //$NON-NLS-1$
		}

		this.command = command;
		this.bindingManager = bindingManager;
	}


	@Override
	public final void addCommandListener(final ICommandListener commandListener) {
		command.addCommandListener(new LegacyCommandListenerWrapper(
				commandListener, bindingManager));
	}

	@Override
	public final Object execute(Map parameterValuesByName)
			throws ExecutionException, NotHandledException {
		try {
			IHandlerService service = PlatformUI.getWorkbench().getService(
					IHandlerService.class);

			return command.execute(new ExecutionEvent(command,
					(parameterValuesByName == null) ? Collections.EMPTY_MAP
									: parameterValuesByName, null, service.getCurrentState()));
		} catch (final org.eclipse.core.commands.ExecutionException e) {
			throw new ExecutionException(e);
		} catch (final org.eclipse.core.commands.NotHandledException e) {
			throw new NotHandledException(e);
		}
	}

	@Override
	public final Map getAttributeValuesByName() {
		final Map attributeValues = new HashMap();
		// avoid using Boolean.valueOf to allow compilation against JCL
		// Foundation (bug 80053)
		attributeValues.put(ILegacyAttributeNames.ENABLED,
				command.isEnabled() ? Boolean.TRUE : Boolean.FALSE);
		attributeValues.put(ILegacyAttributeNames.HANDLED,
				command.isHandled() ? Boolean.TRUE : Boolean.FALSE);
		return attributeValues;
	}

	@Override
	public final String getCategoryId() throws NotDefinedException {
		try {
			return command.getCategory().getId();
		} catch (final org.eclipse.core.commands.common.NotDefinedException e) {
			throw new NotDefinedException(e);
		}
	}

	@Override
	public final String getDescription() throws NotDefinedException {
		try {
			return command.getDescription();
		} catch (final org.eclipse.core.commands.common.NotDefinedException e) {
			throw new NotDefinedException(e);
		}
	}

	@Override
	public final String getId() {
		return command.getId();
	}

	@Override
	public final List getKeySequenceBindings() {
		final List legacyBindings = new ArrayList();
		if (parameterizedCommand == null) {
			parameterizedCommand = new ParameterizedCommand(command, null);
		}
		IBindingService bindingService = PlatformUI.getWorkbench().getService(
				IBindingService.class);
		final TriggerSequence[] activeBindings = bindingService
				.getActiveBindingsFor(parameterizedCommand);
		final int activeBindingsCount = activeBindings.length;
		for (int i = 0; i < activeBindingsCount; i++) {
			final TriggerSequence triggerSequence = activeBindings[i];
			if (triggerSequence instanceof org.eclipse.jface.bindings.keys.KeySequence) {
				legacyBindings
						.add(new KeySequenceBinding(
								KeySequence
										.getInstance((org.eclipse.jface.bindings.keys.KeySequence) triggerSequence),
								0));
			}
		}

		return legacyBindings;
	}

	@Override
	public final String getName() throws NotDefinedException {
		try {
			return command.getName();
		} catch (final org.eclipse.core.commands.common.NotDefinedException e) {
			throw new NotDefinedException(e);
		}
	}

	@Override
	public final boolean isDefined() {
		return command.isDefined();
	}

	@Override
	public final boolean isHandled() {
		return command.isHandled();
	}

	@Override
	public final void removeCommandListener(
			final ICommandListener commandListener) {
		command.removeCommandListener(new LegacyCommandListenerWrapper(
				commandListener, bindingManager));
	}

	@Override
	public final int compareTo(final Object o) {
		return command.compareTo(o);
	}

}
