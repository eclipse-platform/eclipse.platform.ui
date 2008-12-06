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
package org.eclipse.e4.ui.css.core.sac;

import org.eclipse.e4.ui.css.core.impl.sac.SACParserFactoryImpl;
import org.w3c.css.sac.Parser;
import org.w3c.css.sac.helpers.ParserFactory;

/**
 * SAC Parser Factory.
 */
public abstract class SACParserFactory extends ParserFactory implements
		ISACParserFactory {

	private String preferredParserName;

	/**
	 * Return default instance of SAC Parser. If preferredParserName is filled,
	 * it return the instance of SAC Parser registered with this name, otherwise
	 * this method search teh SAC Parser class name to instanciate into System
	 * property with key org.w3c.css.sac.parser.
	 */
	public Parser makeParser() throws ClassNotFoundException,
			IllegalAccessException, InstantiationException,
			NullPointerException, ClassCastException {
		if (preferredParserName != null)
			return makeParser(preferredParserName);
		return super.makeParser();
	}

	/**
	 * Return preferred SAC parser name if it is filled and null otherwise.
	 * 
	 * @return
	 */
	public String getPreferredParserName() {
		return preferredParserName;
	}

	/**
	 * Set the preferred SAC parser name to use when makeParser is called.
	 * 
	 * @param preferredParserName
	 */
	public void setPreferredParserName(String preferredParserName) {
		this.preferredParserName = preferredParserName;
	}

	/**
	 * Return instance of SACParserFactory
	 * 
	 * @return
	 */
	public static ISACParserFactory newInstance() {
		// TODO : manage new instance of SAC Parser Factory like
		// SAXParserFactory.
		return new SACParserFactoryImpl();
	}

	/**
	 * Return instance of SAC Parser registered into the factory with name
	 * <code>name</code>.
	 * 
	 * @param name
	 * @return
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws NullPointerException
	 * @throws ClassCastException
	 */
	public abstract Parser makeParser(String name)
			throws ClassNotFoundException, IllegalAccessException,
			InstantiationException, NullPointerException, ClassCastException;
}
