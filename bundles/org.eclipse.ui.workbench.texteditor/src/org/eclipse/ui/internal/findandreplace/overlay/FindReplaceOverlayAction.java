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
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;

import org.eclipse.jface.bindings.keys.KeyStroke;

class FindReplaceOverlayAction extends AbstractHandler {
	private final Runnable operation;

	private final List<Runnable> executionListeners = new ArrayList<>();

	private final List<Consumer<String>> shortcutHintListeners = new ArrayList<>();

	private final List<KeyStroke> shortcuts = new ArrayList<>();

	FindReplaceOverlayAction(Runnable operation) {
		this.operation = operation;
	}

	void addShortcuts(List<KeyStroke> shortcutsToAdd) {
		this.shortcuts.addAll(shortcutsToAdd);
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

	List<KeyStroke> getShortcuts() {
		return Collections.unmodifiableList(shortcuts);
	}

	boolean executeIfMatching(KeyStroke keystroke) {
		if (shortcuts.stream().anyMatch(keystroke::equals)) {
			execute();
			return true;
		}
		return false;
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

	void activateKeyBinding() {
		shortcutHintListeners.forEach(listener -> listener.accept(getShortcutHint()));
	}

	private String getShortcutHint() {
		if (shortcuts.isEmpty()) {
			return ""; //$NON-NLS-1$
		}
		return shortcuts.get(0).format(); // $NON-NLS-1$
	}

	void deactivateKeyBinding() {
		shortcutHintListeners.forEach(listener -> listener.accept("")); //$NON-NLS-1$
	}

}
