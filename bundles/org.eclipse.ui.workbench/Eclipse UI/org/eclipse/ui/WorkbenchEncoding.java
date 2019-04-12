/*******************************************************************************
 * Copyright (c) 2004, 2018 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui;

import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.internal.registry.RegistryReader;

/**
 * WorkbenchEncoding is a utility class for plug-ins that want to use the list
 * of encodings defined by default in the workbench.
 *
 * @since 3.1
 */
public class WorkbenchEncoding {

	private static class EncodingsRegistryReader extends RegistryReader {

		private List<String> encodings;

		/**
		 * Create a new instance of the receiver.
		 * 
		 * @param definedEncodings
		 */
		public EncodingsRegistryReader(List<String> definedEncodings) {
			super();
			encodings = definedEncodings;
		}

		@Override
		protected boolean readElement(IConfigurationElement element) {
			String name = element.getAttribute(IWorkbenchRegistryConstants.ATT_NAME);
			if (name != null) {
				encodings.add(name);
			}
			return true;
		}
	}

	/**
	 * Get the default encoding from the virtual machine.
	 *
	 * @return String
	 */
	public static String getWorkbenchDefaultEncoding() {
		return System.getProperty("file.encoding", "UTF-8");//$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Return the list of encodings defined using the org.eclipse.ui.encodings
	 * extension point.
	 *
	 * @return List of String
	 */
	public static List<String> getDefinedEncodings() {
		List<String> definedEncodings = Collections.synchronizedList(new ArrayList<>());
		EncodingsRegistryReader reader = new EncodingsRegistryReader(definedEncodings);

		reader.readRegistry(Platform.getExtensionRegistry(), PlatformUI.PLUGIN_ID,
				IWorkbenchRegistryConstants.PL_ENCODINGS);

		// Make it an array in case of concurrency issues with Iterators
		String[] encodings = new String[definedEncodings.size()];
		List<String> invalid = new ArrayList<>();
		definedEncodings.toArray(encodings);
		for (String encoding : encodings) {
			try {
				if (!Charset.isSupported(encoding)) {
					invalid.add(encoding);
				}
			} catch (IllegalCharsetNameException e) {
				invalid.add(encoding);
			}
		}

		for (String next : invalid) {
			WorkbenchPlugin.log(NLS.bind(WorkbenchMessages.WorkbenchEncoding_invalidCharset, next));
			definedEncodings.remove(next);

		}

		return definedEncodings;
	}
}
