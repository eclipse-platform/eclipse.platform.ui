/*******************************************************************************
 * Copyright (c) 2008 Angelo Zerr and others.
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
 *******************************************************************************/

package org.eclipse.e4.ui.css.core.impl.dom;

import org.eclipse.e4.ui.css.core.dom.CSSExtendedProperties;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;

/**
 * {@link CSSExtendedProperties} implementation.
 */
public class CSSExtendedPropertiesImpl extends CSS2PropertiesImpl implements
		CSSExtendedProperties {

	public CSSExtendedPropertiesImpl(Object widget, CSSEngine engine) {
		super(widget, engine);
	}
}
