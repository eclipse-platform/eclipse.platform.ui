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
package org.eclipse.e4.ui.css.core.sac;

import org.w3c.css.sac.Parser;

/**
 * SAC parser factory interface to get instance of SAC {@link Parser}.
 */
public interface ISACParserFactory {

	/**
	 * Return default instance of SAC Parser. If preferredParserName is filled,
	 * it return the instance of SAC Parser registered with this name, otherwise
	 * this method search teh SAC Parser class name to instanciate into System
	 * property with key org.w3c.css.sac.parser.
	 */
	public Parser makeParser() throws ClassNotFoundException,
			IllegalAccessException, InstantiationException,
			NullPointerException, ClassCastException;

	/**
	 * Return instance of SAC Parser registered into the factory with name
	 * <code>name</code>.
	 */
	public abstract Parser makeParser(String name)
			throws ClassNotFoundException, IllegalAccessException,
			InstantiationException, NullPointerException, ClassCastException;
}
