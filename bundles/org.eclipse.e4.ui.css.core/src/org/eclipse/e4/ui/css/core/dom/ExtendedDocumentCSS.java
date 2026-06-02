/*******************************************************************************
 * Copyright (c) 2008, 2026 Angelo Zerr and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *     Karsten Thoms <karste.thoms@itemis.de> - Bug 532869
 *******************************************************************************/
package org.eclipse.e4.ui.css.core.dom;

import java.util.EventListener;
import org.w3c.dom.css.DocumentCSS;
import org.w3c.dom.stylesheets.StyleSheet;

/**
 * Extend {@link DocumentCSS} to add methods like add/remove style sheet.
 */
public interface ExtendedDocumentCSS extends DocumentCSS {

	public void addStyleSheet(StyleSheet styleSheet);

	public void removeAllStyleSheets();

	/**
	 * @since 0.12.200
	 */
	interface StyleSheetChangeListener extends EventListener {
		void styleSheetAdded(StyleSheet styleSheet);

		void styleSheetRemoved(StyleSheet styleSheet);
	}

	/**
	 * @since 0.12.200
	 */
	default void addStyleSheetChangeListener(StyleSheetChangeListener listener) {
	}

	/**
	 * @since 0.12.200
	 */
	default void removeStyleSheetChangeListener(StyleSheetChangeListener listener) {
	}
}
