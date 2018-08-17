/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
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
		List<IFile> files = new ArrayList<>(n - i);
		for (int j= i; j < i + n; j++) {
			String path= root.getLocation().toString() + "/" + prefix + j + suffix;
			files.add(findFile(path));
		}
		return files.toArray(new IFile[files.size()]);
	}

	public static StringBuilder read(String src) throws IOException, CoreException {
		return FileTool.readToBuilder(new InputStreamReader(getRoot().getFile(new Path(src)).getContents()));
	}

	public static void write(String dest, final String content) throws IOException, CoreException {
		InputStream stream= new InputStream() {
			private Reader fReader= new StringReader(content);
			@Override
			public int read() throws IOException {
				return fReader.read();
			}
		};
		getRoot().getFile(new Path(dest)).create(stream, true, null);
	}


	public static void replicate(String src, String destPrefix, String destSuffix, int n, String srcName, String destNamePrefix) throws IOException, CoreException {

		StringBuilder s = read(src);

		List<Integer> positions = identifierPositions(s, srcName);

		for (int j= 0; j < n; j++) {
			StringBuilder c = new StringBuilder(s.toString());
			replacePositions(c, srcName.length(), destNamePrefix + j, positions);
			write(destPrefix + j + destSuffix, c.toString());
		}
	}

	public static void copy(String src, String dest, String srcName, String destName) throws IOException, CoreException {
		StringBuilder buf = read(src);
		List<Integer> positions = identifierPositions(buf, srcName);
		replacePositions(buf, srcName.length(), destName, positions);
		write(dest, buf.toString());
	}

	private static void replacePositions(StringBuilder c, int origLength, String string, List<Integer> positions) {
		int offset= 0;
		for (Iterator<Integer> iter = positions.iterator(); iter.hasNext();) {
			int position= iter.next().intValue();
			c.replace(offset + position, offset + position + origLength, string);
			offset += string.length() - origLength;
		}
	}

	private static List<Integer> identifierPositions(StringBuilder buffer, String identifier) {
		List<Integer> positions = new ArrayList<Integer>();
		int i= -1;
		while (true) {
			i= buffer.indexOf(identifier, i + 1);
			if (i == -1)
				break;
			if (i > 0 && Character.isJavaIdentifierPart(buffer.charAt(i - 1)))
				continue;
			if (i < buffer.length() - 1 && Character.isJavaIdentifierPart(buffer.charAt(i + identifier.length())))
				continue;
			positions.add(Integer.valueOf(i));
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
