/*******************************************************************************
 * Copyright (c) 2018 Remain Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     wim.jongman@remainsoftware.com - initial API and implementation
 *******************************************************************************/
package org.eclipse.tips.core;

import java.net.URL;

/**
 * Decoration of {@link Tip} that enables URL content.
 *
 */
public interface IUrlTip {

	/**
	 * Return an URL with the primary goal to be rendered by the tip manager.
	 * Implementations of Tip may also use the URL to aid the rendering (e.g. by
	 * providing other data than HTML, e.g. a text file).
	 *
	 * @return the URL to the (remote) content
	 *
	 */
	public URL getURL();
}