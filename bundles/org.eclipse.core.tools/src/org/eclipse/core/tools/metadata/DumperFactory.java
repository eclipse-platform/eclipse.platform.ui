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

import java.io.File;
import java.util.Properties;
import org.eclipse.core.runtime.*;
import org.eclipse.core.tools.CoreToolsPlugin;

/**
 * A dumper factory creates a dumper object given a file name.
 */
public class DumperFactory {

	private static final String ELEM_DUMPER = "dumper"; //$NON-NLS-1$
	private static final String ATTR_FILE_NAME = "file-name"; //$NON-NLS-1$
	private static final String PT_METADATA_DUMPERS = "metadataDumpers"; //$NON-NLS-1$
	/**
	 * The <code>DumperFactory</code> singleton reference.
	 */
	private static DumperFactory ref;

	/**
	 * This dumper factory mappings configuration.
	 */
	private Properties configuration = new Properties();

	/**
	 * Provides access to a DumperFactory instance.
	 * 
	 * @return a <code>DumperFactory</code> instance
	 * @throws DumpException if a problem occurs while instantiating the factory
	 * object
	 */
	public synchronized static DumperFactory getInstance() {
		// currently we allow only one instance for this class
		if (ref == null)
			ref = new DumperFactory();
		return ref;
	}

	/**
	 * Constructs a dumper factory, reading dumper definitions from the 
	 * extension registry. Forbids instantiation from outside this class. 
	 * 
	 * @throws DumpException if there is an error when reading 
	 * the dumper definition file   
	 */
	private DumperFactory() {
		loadDumpers();
	}

	private void loadDumpers() {
		IExtensionPoint dumpersPoint = Platform.getExtensionRegistry().getExtensionPoint(CoreToolsPlugin.PLUGIN_ID, PT_METADATA_DUMPERS);
		IConfigurationElement[] dumperDefinitions = dumpersPoint.getConfigurationElements();
		for (int i = 0; i < dumperDefinitions.length; i++)
			if (dumperDefinitions[i].getName().equals(ELEM_DUMPER))
				configuration.put(dumperDefinitions[i].getAttributeAsIs(ATTR_FILE_NAME), dumperDefinitions[i]);
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

		Object dumper = configuration.get(fileName);

		if (dumper == null)
			throw new DumpException("There is no dumper class for <" + fileName + "> files"); //$NON-NLS-1$ //$NON-NLS-2$

		// legacy-style definition (from the properties file)
		if (dumper instanceof String)
			try {
				return (IDumper) Class.forName((String) dumper).newInstance();
			} catch (Exception e) {
				throw new DumpException("Error instantiating dumper for <" + fileName + "> file", e); //$NON-NLS-1$ //$NON-NLS-2$
			}
		// dumper defined through extension mechanism	
		try {
			return (IDumper) ((IConfigurationElement) dumper).createExecutableExtension("class"); //$NON-NLS-1$
		} catch (CoreException ce) {
			throw new DumpException("Error instantiating dumper for <" + fileName + "> file", ce); //$NON-NLS-1$ //$NON-NLS-2$		
		}
	}
}