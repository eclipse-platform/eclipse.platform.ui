/*******************************************************************************
 * Copyright (c) 2008, 2018 Angelo Zerr and others.
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
import java.util.List;
import org.w3c.css.sac.Condition;
import org.w3c.css.sac.Selector;
import org.w3c.dom.css.DocumentCSS;
import org.w3c.dom.stylesheets.StyleSheet;

/**
 * Extend {@link DocumentCSS} to add methods like add/remove style sheet.
 */
public interface ExtendedDocumentCSS extends DocumentCSS {

	public static final Integer SAC_ID_CONDITION = Integer.valueOf(Condition.SAC_ID_CONDITION);
	public static final Integer SAC_CLASS_CONDITION = Integer.valueOf(Condition.SAC_CLASS_CONDITION);
	public static final Integer SAC_PSEUDO_CLASS_CONDITION = Integer.valueOf(Condition.SAC_PSEUDO_CLASS_CONDITION);
	public static final Integer OTHER_SAC_CONDITIONAL_SELECTOR = Integer.valueOf(Selector.SAC_CONDITIONAL_SELECTOR);

	public static final Integer OTHER_SAC_SELECTOR = Integer.valueOf(999);

	public void addStyleSheet(StyleSheet styleSheet);

	public void removeAllStyleSheets();

	public List<?> queryConditionSelector(int conditionType);

	public List<?> querySelector(int selectorType, int conditionType);

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
