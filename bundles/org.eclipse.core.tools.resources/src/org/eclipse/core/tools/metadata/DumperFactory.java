/**********************************************************************
 * Copyright (c) 2002, 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.tools.metadata;

import java.io.*;
import java.util.Map;
import java.util.Properties;

/**
 * A dumper factory creates a dumper object given a file name.
 */
public class DumperFactory {

	/**
	 * The <code>DumperFactory</code> singleton reference.
	 */
	private static DumperFactory ref;

	/**
	 * This dumper factory mappings configuration.
	 */
	private Map configuration;

	/**
	 * Provides access to a DumperFactory instance.
	 * 
	 * @return a <code>DumperFactory</code> instance
	 * @throws DumpException if a problem occurs while instantiating the factory
	 * object
	 */
	public synchronized static DumperFactory getInstance() throws DumpException {
		// currently we allow only one instance for this class
		if (ref == null)
			ref = new DumperFactory();
		return ref;
	}

	/**
	 * Constructs a dumper factory, reading mappings configuration from a properties
	 * file. Forbids instantiation from outside this class. 
	 * 
	 * @throws DumpException if there is no mappings configuration file, or if 
	 * there was a error when reading it.   
	 */
	private DumperFactory() throws DumpException {
		InputStream input = getClass().getResourceAsStream("/dumper_factory.properties"); //$NON-NLS-1$
		if (input == null)
			throw new DumpException("Dumper factory registry file not found"); //$NON-NLS-1$

		Properties configuration = new Properties();
		try {
			configuration.load(input);
			this.configuration = configuration;
		} catch (IOException ioe) {
			throw new DumpException("Error opening Dumper factory registry file", ioe); //$NON-NLS-1$
		} finally {
			try {
				input.close();
			} catch (IOException ioe) {
				throw new DumpException("Error closing Dumper factory registry file", ioe); //$NON-NLS-1$
			}
		}

	}

	/**
	 * Returns an array containing all known file names.
	 * 
	 * @return an array containing file names registered in this factory. 
	 */
	public String[] getRegisteredFileNames() {
		String[] fileNames = new String[configuration.size()];
		return (String[]) configuration.keySet().toArray(fileNames);
	}

	/**
	 * Returns an instance of the dumper class registered for the provided file name. 
	 * If there is no dumper class registered for the provided file name, raises an 
	 * exception.
	 * 
	 * @param fileName the file to be dumped's name
	 * @return a <code>IDumper</code> that knows how to read the file
	 * @throws DumpException if there is no dumper class registered for the 
	 * provided file name
	 */
	public IDumper getDumper(String fileName) throws DumpException {
		fileName = fileName.substring(fileName.lastIndexOf(File.separator) + 1);
		String className = (String) configuration.get(fileName);

		if (className == null)
			throw new DumpException("There is no dumper class for <" + fileName + "> files"); //$NON-NLS-1$ //$NON-NLS-2$

		try {
			return (IDumper) Class.forName(className).newInstance();
		} catch (Exception e) {
			throw new DumpException("Error instantiating dumper for <" + fileName + "> file", e); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
}