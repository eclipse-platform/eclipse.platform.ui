/*******************************************************************************
 * Copyright (c) 2008, 2013 Angelo Zerr and others.
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

/**
 *
 * {@link ICSSPropertyHandler2} delegate.
 *
 * @version 1.0.0
 * @author <a href="mailto:angelo.zerr@gmail.com">Angelo ZERR</a>
 */
public interface ICSSPropertyHandler2Delegate {

	/**
	 * Return {@link ICSSPropertyHandler2} to call when all CSS Properties are
	 * applied .
	 */
	public ICSSPropertyHandler2 getCSSPropertyHandler2();
}
