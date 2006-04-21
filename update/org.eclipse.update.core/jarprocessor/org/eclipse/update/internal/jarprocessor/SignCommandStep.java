/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.jarprocessor;

import java.io.*;
import java.util.*;
import java.util.jar.*;

/**
 * @author aniefer
 *
 */
public class SignCommandStep extends CommandStep {
	private Set exclusions = null;

	public SignCommandStep(Properties options, String command) {
		super(options, command, ".jar"); //$NON-NLS-1$
		exclusions = Utils.getSignExclusions(options);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.update.jarprocessor.IProcessStep#recursionEffect(java.lang.String)
	 */
	public String recursionEffect(String entryName) {
		if (entryName.endsWith(extension) && !exclusions.contains(entryName))
			return entryName;
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.update.jarprocessor.IProcessStep#preProcess(java.io.File, java.io.File)
	 */
	public File preProcess(File input, File workingDirectory) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.update.jarprocessor.IProcessStep#postProcess(java.io.File, java.io.File)
	 */
	public File postProcess(File input, File workingDirectory) {
		if (command != null) {
			try {
				String[] cmd = new String[] {command, input.getCanonicalPath()};
				int result = execute(cmd);
				if (result == 0) {
					normalize(input, workingDirectory);
					return input;
				}
			} catch (IOException e) {
				//boo
				e.printStackTrace();
			}
		}
		return null;
	}

	private void normalize(File input, File workingDirectory) {
		try {
			File tempJar = new File(workingDirectory, "temp_" + input.getName()); //$NON-NLS-1$
			JarFile jar = new JarFile(input, false);
			JarOutputStream jarOut = new JarOutputStream(new BufferedOutputStream(new FileOutputStream(tempJar)));
			InputStream in = null;
			try {
				Enumeration entries = jar.entries();
				for (JarEntry entry = (JarEntry) entries.nextElement(); entry != null; entry = entries.hasMoreElements() ? (JarEntry) entries.nextElement() : null) {
					JarEntry newEntry = new JarEntry(entry.getName());
					newEntry.setTime(entry.getTime());
					in = new BufferedInputStream(jar.getInputStream(entry));
					jarOut.putNextEntry(newEntry);
					Utils.transferStreams(in, jarOut, false);
					jarOut.closeEntry();
					in.close();
				}
			} finally {
				Utils.close(jarOut);
				Utils.close(jar);
				Utils.close(in);
			}
			tempJar.setLastModified(input.lastModified());
			input.delete();
			tempJar.renameTo(input);
		} catch (IOException e) {
			//boo
		}
	}
}
