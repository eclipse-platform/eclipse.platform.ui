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

import org.eclipse.jface.bindings.keys.KeyStroke;

class FindReplaceOverlayAction {
	private final Runnable operation;

	private final List<KeyStroke> shortcuts = new ArrayList<>();

	FindReplaceOverlayAction(Runnable operation) {
		this.operation = operation;
	}

	void addShortcuts(List<KeyStroke> shortcutsToAdd) {
		this.shortcuts.addAll(shortcutsToAdd);
	}

	void execute() {
		operation.run();
	}

	boolean executeIfMatching(KeyStroke keystroke) {
		if (shortcuts.stream().anyMatch(keystroke::equals)) {
			execute();
			return true;
		}
		return false;
	}

	String addShortcutHintToTooltipText(String originalTooltipText) {
		if (shortcuts.isEmpty()) {
			return originalTooltipText;
		}
		return originalTooltipText + " (" + shortcuts.get(0).format() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
	}

}
