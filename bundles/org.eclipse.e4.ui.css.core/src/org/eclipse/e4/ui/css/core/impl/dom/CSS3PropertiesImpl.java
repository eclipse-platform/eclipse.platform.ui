/*******************************************************************************
 * Copyright (c) 2008 Angelo Zerr and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *******************************************************************************/

package org.eclipse.e4.ui.css.core.impl.dom;

import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.w3c.dom.css.CSS3Properties;

/**
 * w3c {@link CSS3Properties} implementation.
 */
public class CSS3PropertiesImpl extends CSS2PropertiesImpl implements CSS3Properties
{

	public CSS3PropertiesImpl(Object widget, CSSEngine engine) {
		super(widget, engine);
	}
}
