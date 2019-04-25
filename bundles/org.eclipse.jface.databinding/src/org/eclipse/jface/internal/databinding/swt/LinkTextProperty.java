/*******************************************************************************
 * Copyright (c) 2008, 2015 Matthew Hall and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 194734)
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.swt;

import org.eclipse.swt.widgets.Link;

/**
 * @since 3.3
 *
 */
public class LinkTextProperty extends WidgetStringValueProperty<Link> {
	@Override
	String doGetStringValue(Link source) {
		return source.getText();
	}

	@Override
	void doSetStringValue(Link source, String value) {
		source.setText(value == null ? "" : value); //$NON-NLS-1$
	}

	@Override
	public String toString() {
		return "Link.text <String>"; //$NON-NLS-1$
	}
}
