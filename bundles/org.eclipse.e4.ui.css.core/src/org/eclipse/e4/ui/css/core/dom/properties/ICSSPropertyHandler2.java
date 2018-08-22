/*******************************************************************************
 * Copyright (c) 2008, 2015 Angelo Zerr and others.
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
package org.eclipse.e4.ui.css.core.dom.properties;

import org.eclipse.e4.ui.css.core.engine.CSSEngine;

/**
 * CSS Property Handler to intercept when all CSS Properties are applied.
 *
 * @version 1.0.0
 * @author <a href="mailto:angelo.zerr@gmail.com">Angelo ZERR</a>
 *
 */
public interface ICSSPropertyHandler2 {

	/**
	 * Callback method called when all CSS properties are applied.
	 *
	 * @param element
	 * @param engine
	 * @throws Exception
	 */
	default void onAllCSSPropertiesApplyed(Object element, CSSEngine engine) throws Exception {
		// do nothing
	}

	/**
	 * Callback method called when all CSS properties are applied.
	 *
	 * @param element
	 * @param engine
	 * @param pseudo
	 * @throws Exception
	 */
	default void onAllCSSPropertiesApplyed(Object element, CSSEngine engine, String pseudo) throws Exception {
		onAllCSSPropertiesApplyed(element, engine);
	}
}
