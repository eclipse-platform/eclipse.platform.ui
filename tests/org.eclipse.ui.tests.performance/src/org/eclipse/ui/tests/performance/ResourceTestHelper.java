/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.tests.performance;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.tests.harness.util.FileTool;

public class ResourceTestHelper {

	public static void replicate(String src, String destPrefix, String destSuffix, int n) throws CoreException {
		for (int i= 0; i < n; i++)
			copy(src, destPrefix + i + destSuffix);
	}

	public static void copy(String src, String dest) throws CoreException {
		IFile file= getRoot().getFile(new Path(src));
		file.copy(new Path(dest), true, null);
	}

	public static void delete(String file) throws CoreException {
		getRoot().getFile(new Path(file)).delete(true, null);
	}

	public static IFile findFile(String path) {
		return getRoot().getFile(new Path(path));
	}

	public static IFile[] findFiles(String prefix, String suffix, int i, int n) {
		IWorkspaceRoot root= getRoot();
		List files= new ArrayList(n - i);
		for (int j= i; j < i + n; j++) {
			String path= root.getLocation().toString() + "/" + prefix + j + suffix;
			files.add(findFile(path));
		}
		return (IFile[]) files.toArray(new IFile[files.size()]);
	}

	public static StringBuffer read(String src) throws IOException, CoreException {
		return FileTool.read(new InputStreamReader(getRoot().getFile(new Path(src)).getContents()));
	}

	public static void write(String dest, final String content) throws IOException, CoreException {
		InputStream stream= new InputStream() {
			private Reader fReader= new StringReader(content);
			public int read() throws IOException {
				return fReader.read();
			}
		};
		getRoot().getFile(new Path(dest)).create(stream, true, null);
	}
	

	public static void replicate(String src, String destPrefix, String destSuffix, int n, String srcName, String destNamePrefix) throws IOException, CoreException {
		
		StringBuffer s= read(src);
		
		List positions= identifierPositions(s, srcName);
		
		for (int j= 0; j < n; j++) {
			StringBuffer c= new StringBuffer(s.toString());
			replacePositions(c, srcName.length(), destNamePrefix + j, positions);
			write(destPrefix + j + destSuffix, c.toString());
		}
	}

	public static void copy(String src, String dest, String srcName, String destName) throws IOException, CoreException {
		StringBuffer buf= read(src);
		List positions= identifierPositions(buf, srcName);
		replacePositions(buf, srcName.length(), destName, positions);
		write(dest, buf.toString());
	}

	private static void replacePositions(StringBuffer c, int origLength, String string, List positions) {
		int offset= 0;
		for (Iterator iter= positions.iterator(); iter.hasNext();) {
			int position= ((Integer) iter.next()).intValue();
			c.replace(offset + position, offset + position + origLength, string);
			offset += string.length() - origLength;
		}
	}

	private static List identifierPositions(StringBuffer buffer, String identifier) {
		List positions= new ArrayList();
		int i= -1;
		while (true) {
			i= buffer.indexOf(identifier, i + 1);
			if (i == -1)
				break;
			if (i > 0 && Character.isJavaIdentifierPart(buffer.charAt(i - 1)))
				continue;
			if (i < buffer.length() - 1 && Character.isJavaIdentifierPart(buffer.charAt(i + identifier.length())))
				continue;
			positions.add(new Integer(i));
		}
		return positions;
	}

	private static IWorkspaceRoot getRoot() {
		return ResourcesPlugin.getWorkspace().getRoot();
	}

	public static boolean enableAutoBuilding() {
		IWorkspaceDescription description= ResourcesPlugin.getWorkspace().getDescription();
		boolean wasOff= !description.isAutoBuilding();
		if (wasOff)
			description.setAutoBuilding(true);
		return wasOff;
	}

	public static void incrementalBuild() throws CoreException {
		ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
	}

	public static void fullBuild() throws CoreException {
		ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
	}

	public static boolean disableAutoBuilding() {
		IWorkspaceDescription description= ResourcesPlugin.getWorkspace().getDescription();
		boolean wasOn= description.isAutoBuilding();
		if (wasOn)
			description.setAutoBuilding(false);
		return wasOn;
	}
}
