/*******************************************************************************
 * Copyright (c) 2002, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ua.tests.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/*
 * Utility methods for working with files.
 */
public class FileUtil {

	public static String getContents(String path) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(path));
		StringBuffer buf = new StringBuffer();
		String line = null;
		while ((line = in.readLine()) != null) {
			buf.append(line);
			buf.append("\n");
		}
		in.close();
		return buf.toString();
	}
}
