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
 *     IBM Corporation - ongoing development
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 422702
 *     Karsten Thoms <karste.thoms@itemis.de> - Bug 532869
 *******************************************************************************/

package org.eclipse.e4.ui.css.core.impl.dom;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.e4.ui.css.core.dom.ExtendedDocumentCSS;
import org.w3c.dom.Element;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.DocumentCSS;
import org.w3c.dom.stylesheets.StyleSheet;
import org.w3c.dom.stylesheets.StyleSheetList;

/**
 * w3c {@link DocumentCSS} implementation.
 */
public class DocumentCSSImpl implements ExtendedDocumentCSS {

	private final StyleSheetListImpl styleSheetList = new StyleSheetListImpl();

	private final List<StyleSheetChangeListener> styleSheetChangeListeners = new ArrayList<>(1);

	@Override
	public StyleSheetList getStyleSheets() {
		return styleSheetList;
	}

	@Override
	public CSSStyleDeclaration getOverrideStyle(Element element, String s) {
		return null;
	}

	@Override
	public void addStyleSheet(StyleSheet styleSheet) {
		styleSheetList.addStyleSheet(styleSheet);
		styleSheetChangeListeners.forEach(l -> l.styleSheetAdded(styleSheet));
	}

	@Override
	public void removeAllStyleSheets() {
		for (int i = 0; i < styleSheetList.getLength(); i++) {
			StyleSheet styleSheet = styleSheetList.item(i);
			styleSheetChangeListeners.forEach(l -> l.styleSheetRemoved(styleSheet));
		}
		styleSheetList.removeAllStyleSheets();
	}

	@Override
	public void addStyleSheetChangeListener(StyleSheetChangeListener listener) {
		styleSheetChangeListeners.add(listener);
	}

	@Override
	public void removeStyleSheetChangeListener(StyleSheetChangeListener listener) {
		styleSheetChangeListeners.remove(listener);
	}
}
