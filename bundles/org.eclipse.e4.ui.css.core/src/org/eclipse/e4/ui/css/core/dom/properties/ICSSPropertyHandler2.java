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
	public void onAllCSSPropertiesApplyed(Object element, CSSEngine engine)
			throws Exception;
}
