/*******************************************************************************
 * Copyright (c) 2004, 2017 IBM Corporation and others.
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
package org.eclipse.ui.tests.autotests;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;

import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.tests.TestPlugin;

/**
 * @since 3.1
 */
public class XmlUtil {
	public static IMemento read(InputStream toRead) throws WorkbenchException {
		InputStreamReader reader = new InputStreamReader(toRead);

		return XMLMemento.createReadRoot(reader);
	}

	public static IMemento read(URL toRead) throws WorkbenchException {
		try {
			return read(toRead.openStream());
		} catch (IOException e) {
			throw new WorkbenchException(Status.error(TestPlugin.getDefault().getBundle().getSymbolicName(), e));
		}
	}

	public static void write(File file, XMLMemento data) throws WorkbenchException {

		try {
			file.getParentFile().mkdirs();
			file.delete();
			file.createNewFile();

			try (FileOutputStream output = new FileOutputStream(file);
					OutputStreamWriter writer = new OutputStreamWriter(output)) {
				data.save(writer);
			}
		} catch (IOException e) {
			throw new WorkbenchException(Status.error(TestPlugin.getDefault().getBundle().getSymbolicName(), e));
		}
	}
}
