/*******************************************************************************
 * Copyright (c) 2014 TwelveTone LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Steven Spungin <steven@spungin.tv> - initial API and implementation
 *******************************************************************************/

package org.eclipse.e4.internal.tools.wizards.project;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.pde.internal.core.util.CoreUtility;
import org.eclipse.pde.internal.ui.wizards.plugin.PluginFieldData;
import org.osgi.framework.FrameworkUtil;

@SuppressWarnings("restriction")
public class LifeCycleClassCodeGenerator {
	private static final String PACKAGE_DECLARATION = "PACKAGE_DECLARATION"; //$NON-NLS-1$
	private final PluginFieldData fPluginData;
	private final IProject fProject;
	private final String fQualifiedClassName;

	public LifeCycleClassCodeGenerator(IProject project, String qualifiedClassName, PluginFieldData data) {
		fProject = project;
		fQualifiedClassName = qualifiedClassName;
		fPluginData = data;

	}

	public IFile generate(IProgressMonitor monitor) throws CoreException {
		final int nameloc = fQualifiedClassName.lastIndexOf('.');
		String packageName = nameloc == -1 ? "" : fQualifiedClassName.substring(0, nameloc); //$NON-NLS-1$
		// CoreUtility.createFolder was throwing exception if Activator was already created with lower case package and
		// this method called it with different case.
		packageName = packageName.toLowerCase();
		final String className = fQualifiedClassName.substring(nameloc + 1);

		IPath path = new Path(packageName.replace('.', '/'));
		if (fPluginData.getSourceFolderName().trim().length() > 0) {
			path = new Path(fPluginData.getSourceFolderName()).append(path);
		}

		CoreUtility.createFolder(fProject.getFolder(path));

		final IFile file = fProject.getFile(path.append(className + ".java")); //$NON-NLS-1$
		final StringWriter swriter = new StringWriter();
		final PrintWriter writer = new PrintWriter(swriter);
		generateClass(packageName, className, swriter);
		writer.flush();

		try {
			swriter.close();
			final ByteArrayInputStream stream = new ByteArrayInputStream(swriter.toString().getBytes(
				fProject.getDefaultCharset()));
			if (file.exists()) {
				file.setContents(stream, false, true, monitor);
			} else {
				file.create(stream, false, monitor);
			}
			stream.close();
		} catch (final Exception e) {

		}
		return file;
	}

	private void generateClass(String packageName, String className, StringWriter writer) {

		final Map<String, String> map = new HashMap<String, String>();
		if (packageName.equals("")) { //$NON-NLS-1$
			map.put(PACKAGE_DECLARATION, ""); //$NON-NLS-1$
		} else {
			map.put(PACKAGE_DECLARATION, "package " + packageName + ";"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		map.put("PACKAGE_NAME", packageName); //$NON-NLS-1$
		map.put("CLASS_NAME", className); //$NON-NLS-1$

		try {
			String template = getResourceAsString(this.getClass(), "/templates/E4LifeCycle.java"); //$NON-NLS-1$
			template = SimpleTemplate.process(template, map);
			writer.write(template);
		} catch (final Exception e) {
			e.printStackTrace();
		}

	}

	public static String getResourceAsString(Class<?> classInBundle, String templatePath) throws FileNotFoundException,
		IOException {
		final URL url = FrameworkUtil.getBundle(classInBundle).getResource(templatePath);
		return readTextFile(url.openStream());
	}

	public static String readTextFile(InputStream stream) throws IOException, FileNotFoundException {
		final StringBuilder sb = new StringBuilder();
		final BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

		final char[] chars = new char[1024];
		int numRead = 0;
		while ((numRead = reader.read(chars)) > -1) {
			sb.append(String.copyValueOf(chars, 0, numRead));
		}

		reader.close();
		return sb.toString();
	}
}
