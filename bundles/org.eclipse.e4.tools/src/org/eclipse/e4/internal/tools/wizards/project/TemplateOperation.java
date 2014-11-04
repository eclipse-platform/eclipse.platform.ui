/*******************************************************************************
 * Copyright (c) 2006, 2010 Soyatec (http://www.soyatec.com) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Soyatec - initial API and implementation
 * Sopot Cela - ongoing enhancements
 *******************************************************************************/
package org.eclipse.e4.internal.tools.wizards.project;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.e4.internal.tools.ToolsPlugin;
import org.eclipse.pde.internal.ui.PDEUIMessages;
import org.eclipse.pde.ui.templates.IVariableProvider;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

@SuppressWarnings("restriction")
public class TemplateOperation extends WorkspaceModifyOperation implements
	IVariableProvider {

	private final URL templateDirectory;
	private final IContainer target;
	private final Map<String, String> keys;
	private final Set<String> binaryExtentions;
	private final boolean isMinimalist;

	public TemplateOperation(URL source, IContainer target,
		Map<String, String> keys, Set<String> binaryExtentions, boolean justProduct) {
		templateDirectory = source;
		this.binaryExtentions = binaryExtentions;
		this.target = target;
		this.keys = keys;
		isMinimalist = justProduct;
	}

	@Override
	protected void execute(IProgressMonitor monitor) throws CoreException,
		InvocationTargetException, InterruptedException {
		monitor.setTaskName(PDEUIMessages.AbstractTemplateSection_generating);

		if ("jar".equals(templateDirectory.getProtocol())) { //$NON-NLS-1$
			final String file = templateDirectory.getFile();
			final int exclamation = file.indexOf('!');
			if (exclamation < 0) {
				return;
			}
			URL fileUrl = null;
			try {
				fileUrl = new URL(file.substring(0, exclamation));
			} catch (final MalformedURLException mue) {
				ToolsPlugin.logError(mue);
				return;
			}
			final File pluginJar = new File(fileUrl.getFile());
			if (!pluginJar.exists()) {
				return;
			}
			final String templateDirectory = file.substring(exclamation + 1); // "/some/path/"
			final IPath path = new Path(templateDirectory);
			ZipFile zipFile = null;
			try {
				zipFile = new ZipFile(pluginJar);
				generateFiles(zipFile, path, target, monitor);
			} catch (final ZipException ze) {
			} catch (final IOException ioe) {
			} finally {
				if (zipFile != null) {
					try {
						zipFile.close();
					} catch (final IOException e) {
					}
				}
			}
		} else if ("file".equals(templateDirectory.getProtocol())) { //$NON-NLS-1$
			final File directory = new File(templateDirectory.getFile());
			if (!directory.exists()) {
				return;
			}
			generateFiles(directory, target, true, monitor);
		}
	}

	private void generateFiles(File src, IContainer dst, boolean firstLevel,
		IProgressMonitor monitor) throws CoreException {
		if (!firstLevel && isMinimalist) {
			return;
		}
		final File[] members = src.listFiles();

		for (int i = 0; i < members.length; i++) {
			final File member = members[i];
			final String name = member.getName();
			if (member.isDirectory()) {
				if (".svn".equals(name) || "cvs".equalsIgnoreCase(name)) { //$NON-NLS-1$ //$NON-NLS-2$
					continue;
				}
				IContainer dstContainer = null;

				final String folderName = getProcessedString(name, name);
				dstContainer = dst.getFolder(new Path(folderName));
				if (dstContainer != null && !dstContainer.exists()) {
					((IFolder) dstContainer).create(true, true, monitor);
				}

				generateFiles(member, dstContainer, false, monitor);
			} else {
				InputStream in = null;
				try {
					in = new FileInputStream(member);
					copyFile(name, in, dst, monitor);
				} catch (final IOException ioe) {
				} finally {
					if (in != null) {
						try {
							in.close();
						} catch (final IOException ioe2) {
						}
					}
				}
			}
		}
	}

	/**
	 *
	 *
	 * @param zipFile
	 * @param path
	 * @param dst
	 * @param monitor
	 * @throws CoreException
	 */
	private void generateFiles(ZipFile zipFile, IPath path, IContainer dst,
		IProgressMonitor monitor) throws CoreException {
		final int pathLength = path.segmentCount();
		// Immidiate children
		final Map<String, ZipEntry> childZipEntries = new HashMap<String, ZipEntry>(); // "dir/" or "dir/file.java"

		for (final Enumeration<? extends ZipEntry> zipEntries = zipFile.entries(); zipEntries
			.hasMoreElements();) {
			final ZipEntry zipEntry = zipEntries.nextElement();
			final IPath entryPath = new Path(zipEntry.getName());
			if (entryPath.segmentCount() <= pathLength) {
				// ancestor or current directory
				continue;
			}
			if (!path.isPrefixOf(entryPath)) {
				// not a descendant
				continue;
			}
			if (entryPath.segmentCount() == pathLength + 1) {
				childZipEntries.put(zipEntry.getName(), zipEntry);
			} else {
				final String name = entryPath.uptoSegment(pathLength + 1)
					.addTrailingSeparator().toString();
				if (!childZipEntries.containsKey(name)) {
					final ZipEntry dirEntry = new ZipEntry(name);
					childZipEntries.put(name, dirEntry);
				}
			}
		}

		for (final Iterator<ZipEntry> it = childZipEntries.values().iterator(); it.hasNext();) {
			final ZipEntry zipEnry = it.next();
			final String name = new Path(zipEnry.getName()).lastSegment().toString();
			if (zipEnry.isDirectory()) {
				IContainer dstContainer = null;

				final String folderName = getProcessedString(name, name);
				dstContainer = dst.getFolder(new Path(folderName));
				if (dstContainer != null && !dstContainer.exists()) {
					((IFolder) dstContainer).create(true, true, monitor);
				}
				generateFiles(zipFile, path.append(name), dstContainer, monitor);
			} else {
				InputStream in = null;
				try {
					in = zipFile.getInputStream(zipEnry);
					copyFile(name, in, dst, monitor);
				} catch (final IOException ioe) {
				} finally {
					if (in != null) {
						try {
							in.close();
						} catch (final IOException ioe2) {
						}
					}
				}
			}
		}
	}

	private void copyFile(String fileName, InputStream input, IContainer dst,
		IProgressMonitor monitor) throws CoreException {
		final String targetFileName = getProcessedString(fileName, fileName);

		monitor.subTask(targetFileName);
		final IFile dstFile = dst.getFile(new Path(targetFileName));

		try {
			final InputStream stream = isBinary(fileName) ? input
				: getProcessedStream(fileName, input);
			if (dstFile.exists()) {
				dstFile.setContents(stream, true, true, monitor);
			} else {
				dstFile.create(stream, true, monitor);
			}
			stream.close();

		} catch (final IOException e) {
		}
	}

	protected void copyFile(String fileName, InputStream input, IContainer dst,
		final String destPath, IProgressMonitor monitor)
		throws CoreException {
		String targetFileName = null;
		if (destPath == null) {
			targetFileName = getProcessedString(fileName, fileName);
		} else {
			targetFileName = destPath;
		}

		monitor.subTask(targetFileName);
		final IFile dstFile = dst.getFile(new Path(targetFileName));

		try {
			final InputStream stream = isBinary(fileName) ? input
				: getProcessedStream(fileName, input);
			if (dstFile.exists()) {
				dstFile.setContents(stream, true, true, monitor);
			} else {
				dstFile.create(stream, true, monitor);
			}
			stream.close();

		} catch (final IOException e) {
		}
	}

	/**
	 *
	 * @param fileName
	 * @param input
	 * @param dst
	 * @param basePath
	 * @param monitor
	 * @throws CoreException
	 */
	public void copyFile(String fileName, InputStream input, IContainer dst,
		final String basePath, final String destName,
		IProgressMonitor monitor) throws CoreException {
		if (basePath == null || basePath.equals("")) { //$NON-NLS-1$
			copyFile(fileName, input, dst, monitor);
		}

		final String targetFileName = destName == null ? getProcessedString(fileName,
			fileName) : destName;

		monitor.subTask(targetFileName);
		final IFile dstFile = dst.getFile(new Path(basePath + targetFileName));

		try {
			final InputStream stream = isBinary(fileName) ? input
				: getProcessedStream(fileName, input);
			if (dstFile.exists()) {
				dstFile.setContents(stream, true, true, monitor);
			} else {
				dstFile.create(stream, true, monitor);
			}
			stream.close();

		} catch (final IOException e) {
		}
	}

	private boolean isBinary(String fileName) {
		if (binaryExtentions == null) {
			return false;
		}

		final String ext = getfileExtention(fileName);
		if (ext == null) {
			return false;
		}
		return binaryExtentions.contains(ext);
	}

	private String getfileExtention(String name) {
		final int indexOf = name.lastIndexOf('.');
		if (indexOf == -1) {
			return null;
		}
		return name.substring(indexOf);
	}

	private InputStream getProcessedStream(String fileName, InputStream stream)
		throws IOException, CoreException {
		final InputStreamReader reader = new InputStreamReader(stream);
		final int bufsize = 1024;
		final char[] cbuffer = new char[bufsize];
		int read = 0;
		final StringBuffer keyBuffer = new StringBuffer();
		final StringBuffer outBuffer = new StringBuffer();

		boolean replacementMode = false;
		boolean almostReplacementMode = false;
		boolean escape = false;
		while (read != -1) {
			read = reader.read(cbuffer);
			for (int i = 0; i < read; i++) {
				final char c = cbuffer[i];

				if (escape) {
					final StringBuffer buf = outBuffer;
					buf.append(c);
					escape = false;
					continue;
				}

				if (c == '@') {
					if (replacementMode && almostReplacementMode) {
						almostReplacementMode = false;
					} else if (replacementMode) {
						replacementMode = false;
						final String key = keyBuffer.toString();
						final String value = key.length() == 0 ? "@@" //$NON-NLS-1$
							: getReplacementString(key);
						outBuffer.append(value);
						keyBuffer.delete(0, keyBuffer.length());
					} else if (almostReplacementMode) {
						replacementMode = true;
					} else {
						almostReplacementMode = true;
					}
				} else {
					if (replacementMode) {
						keyBuffer.append(c);
					} else {
						if (almostReplacementMode) {
							outBuffer.append('@');
						}
						outBuffer.append(c);
						almostReplacementMode = false;
					}
				}
			}
		}
		return new ByteArrayInputStream(outBuffer.toString().getBytes());
		// return new
		// ByteArrayInputStream(outBuffer.toString().getBytes(project.
		// getDefaultCharset()));
	}

	private String getProcessedString(String fileName, String source) {
		if (source.indexOf('$') == -1) {
			return source;
		}
		int loc = -1;
		final StringBuffer buffer = new StringBuffer();
		boolean replacementMode = false;
		for (int i = 0; i < source.length(); i++) {
			final char c = source.charAt(i);
			if (c == '$') {
				if (replacementMode) {
					final String key = source.substring(loc, i);
					final String value = key.length() == 0 ? "$" : getReplacementString(key); //$NON-NLS-1$
					buffer.append(value);
					replacementMode = false;
				} else {
					replacementMode = true;
					loc = i + 1;
					continue;
				}
			} else if (!replacementMode) {
				buffer.append(c);
			}
		}
		return buffer.toString();
	}

	public String getReplacementString(String key) {
		final String result = keys.get(key);
		return result != null ? result : key;
	}

	@Override
	public Object getValue(String variable) {
		return getReplacementString(variable);
	}

}
