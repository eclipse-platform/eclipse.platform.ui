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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.widgets.Control;

import org.eclipse.jface.bindings.keys.KeyStroke;

public final class FindReplaceShortcutUtil {
	private FindReplaceShortcutUtil() {
	}

	static void registerActionShortcutsAtControl(FindReplaceOverlayAction action, Control control) {
		control.addKeyListener(KeyListener.keyPressedAdapter(event -> {
			KeyStroke actualStroke = extractKeyStroke(event);
			if (action.executeIfMatching(actualStroke)) {
				event.doit = false;
			}
		}));
	}

	static private KeyStroke extractKeyStroke(KeyEvent e) {
		char character = e.character;
		boolean ctrlDown = (e.stateMask & SWT.CTRL) != 0;
		if (ctrlDown && e.character != e.keyCode && e.character < 0x20 && (e.keyCode & SWT.KEYCODE_BIT) == 0) {
			character += 0x40;
		}
		KeyStroke actualStroke = KeyStroke.getInstance(e.stateMask & (SWT.MOD1 | SWT.SHIFT),
				character == 0 ? e.keyCode : character);
		return actualStroke;
	}
}
