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
 *******************************************************************************/
package org.eclipse.e4.ui.css.core.impl.dom;

/**
 * An {@code @import} rule. The engine resolves and inlines the referenced
 * stylesheet when the surrounding sheet is parsed.
 */
public final class CSSImportRuleImpl implements CssRule {

	private final String href;

	public CSSImportRuleImpl(String href) {
		this.href = href;
	}

	public String getHref() {
		return href;
	}

	@Override
	public String toString() {
		return "@import url(" + href + ")"; //$NON-NLS-1$ //$NON-NLS-2$
	}
}
