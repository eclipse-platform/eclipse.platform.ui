/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.registry.RegistryReader;

/**
 * WorkbenchEncoding is a utility class for plug-ins that want to use the list
 * of encodings defined by default in the workbench.
 */
public class WorkbenchEncoding {
	
	/**
	 * The list of encodings read from the extension registry
	 */
	private static List definedEncodings;
	
	private static class EncodingsRegistryReader extends RegistryReader{
		
		private static final String ATT_NAME = "name"; //$NON-NLS-1$
		
		/**
		 * Create a new instance of the receiver.
		 */
		public EncodingsRegistryReader() {
			super();
		}
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.internal.registry.RegistryReader#readElement(org.eclipse.core.runtime.IConfigurationElement)
		 */
		protected boolean readElement(IConfigurationElement element) {
			String name = element.getAttribute(ATT_NAME);
			if(name != null)
				definedEncodings.add(name);
			return true;
		}
	}

	/**
	 * Workbench constant for ISO-8859-1
	 * 
	 * @deprecated Use #getDefinedEncodings. This API will be deleted before 3.1
	 *             M2.
	 */
	public static String ISO_8859_1 = "ISO-8859-1";//$NON-NLS-1$

	/**
	 * Workbench constant for UTF-8
	 * 
	 * @deprecated Use #getDefinedEncodings. This API will be deleted before 3.1
	 *             M2.
	 */
	public static String UTF_8 = "UTF-8";//$NON-NLS-1$

	/**
	 * Workbench constant for UTF-16
	 * 
	 * @deprecated Use #getDefinedEncodings. This API will be deleted before 3.1
	 *             M2.
	 */
	public static String UTF_16 = "UTF-16";//$NON-NLS-1$

	/**
	 * Workbench constant for UTF-16 big endian
	 * 
	 * @deprecated Use #getDefinedEncodings. This API will be deleted before 3.1
	 *             M2.
	 */
	public static String UTF_16_BIG_ENDIAN = "UTF-16BE";//$NON-NLS-1$

	/**
	 * Workbench constant for UTF-16 little endian
	 * 
	 * @deprecated Use #getDefinedEncodings. This API will be deleted before 3.1
	 *             M2.
	 */
	public static String UTF_16_LITTLE_ENDIAN = "UTF-16LE";//$NON-NLS-1$

	/**
	 * Workbench constant for US ASCII
	 * 
	 * @deprecated Use #getDefinedEncodings. This API will be deleted before 3.1
	 *             M2.
	 */
	public static String US_ASCII = "US-ASCII";//$NON-NLS-1$

	/**
	 * Get the default encoding from the virtual machine.
	 * 
	 * @return String
	 */
	public static String getWorkbenchDefaultEncoding() {
		return System.getProperty("file.encoding", "UTF-8");//$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Return the list of encodings supplied as standard choices in the
	 * workbench. Include the default encoding as well.
	 * 
	 * @deprecated Use #getDefinedEncodings. This API will be deleted before 3.1
	 *             M2.
	 * 
	 * @return List of String
	 */
	public static List getStandardEncodings() {
		ArrayList encodings = new ArrayList();

		encodings.add(US_ASCII);
		encodings.add(UTF_16);
		encodings.add(UTF_16_BIG_ENDIAN);
		encodings.add(UTF_16_LITTLE_ENDIAN);
		encodings.add(UTF_8);
		encodings.add(ISO_8859_1);

		String defaultEnc = getWorkbenchDefaultEncoding();

		if (!encodings.contains(defaultEnc)) {
			encodings.add(defaultEnc);
		}

		return encodings;
	}
	
	/**
	 * Return the list of encodings defined using the org.eclipse.ui.encodings
	 * extension point.
	 * 
	 * @return List of String
	 */
	public static List getDefinedEncodings(){
		
		if(definedEncodings == null)
			readEncodings();
		return definedEncodings;
		
	}

	/**
	 * Read the encodings from the extension registry.
	 */
	private static void readEncodings() {
		
		definedEncodings = new ArrayList();
		EncodingsRegistryReader reader = new EncodingsRegistryReader();
		
		reader.readRegistry(Platform.getExtensionRegistry(), PlatformUI.PLUGIN_ID,
                IWorkbenchConstants.PL_ENCODINGS);
		
	}

		
}