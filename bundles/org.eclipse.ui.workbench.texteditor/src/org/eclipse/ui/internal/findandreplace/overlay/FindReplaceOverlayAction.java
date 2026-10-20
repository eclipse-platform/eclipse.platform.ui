/*******************************************************************************
 * Copyright (c) 2024 Vector Informatik GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.ui.internal.findandreplace.overlay;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;

import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.keys.IBindingService;

class FindReplaceOverlayAction extends AbstractHandler {
	private final Runnable operation;
	private final String commandId;

	private final List<Runnable> executionListeners = new ArrayList<>();

	private final List<Consumer<String>> shortcutHintListeners = new ArrayList<>();

	FindReplaceOverlayAction(Runnable operation, String commandId) {
		this.operation = operation;
		this.commandId = commandId;
	}

	FindReplaceOverlayAction(Runnable operation) {
		this(operation, null);
	}

	@Override
	public Object execute(ExecutionEvent event) {
		execute();
		return null;
	}

	void execute() {
		operation.run();
		notifyExecutionListeners();
	}

	void setAvailable(boolean available) {
		setBaseEnabled(available);
	}

	void addExecutionListener(Runnable listener) {
		executionListeners.add(listener);
	}

	void notifyExecutionListeners() {
		for (Runnable listener : executionListeners) {
			listener.run();
		}
	}

	void addShortcutHintListener(Consumer<String> listener) {
		shortcutHintListeners.add(listener);
	}

	String getCommandId() {
		return commandId;
	}

	void updateHint() {
		if (commandId == null) {
			return;
		}
		IBindingService bindingService = PlatformUI.getWorkbench().getService(IBindingService.class);
		if (bindingService == null) {
			return;
		}
		String hint = bindingService.getBestActiveBindingFormattedFor(commandId);
		shortcutHintListeners.forEach(l -> l.accept(hint == null ? "" : hint)); //$NON-NLS-1$
	}

}
