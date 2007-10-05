/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.ui.refactoring.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.team.core.mapping.IStorageMerger;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.eclipse.core.resources.IStorage;

import org.eclipse.ltk.core.refactoring.RefactoringDescriptorProxy;

import org.eclipse.ltk.internal.core.refactoring.history.RefactoringHistoryManager;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringUIMessages;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringUIPlugin;

/**
 * Combined storage and stream merger for refactoring history index files.
 * 
 * @since 3.2
 */
public final class RefactoringIndexMerger implements IStorageMerger {

	/**
	 * Creates a new refactoring index merger.
	 */
	public RefactoringIndexMerger() {
		// Do nothing
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean canMergeWithoutAncestor() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public IStatus merge(final OutputStream output, final String encoding, final IStorage ancestor, final IStorage target, final IStorage source, final IProgressMonitor monitor) throws CoreException {
		InputStream targetStream= null;
		InputStream sourceStream= null;
		try {
			targetStream= target.getContents();
			sourceStream= target.getContents();
			performMerge(output, encoding, targetStream, sourceStream);
		} catch (IOException exception) {
			return new Status(IStatus.ERROR, RefactoringUIPlugin.getPluginId(), 1, RefactoringUIMessages.RefactoringHistoryMerger_error_auto_merge, exception);
		} catch (CoreException exception) {
			return new Status(IStatus.ERROR, RefactoringUIPlugin.getPluginId(), 1, RefactoringUIMessages.RefactoringHistoryMerger_error_auto_merge, exception);
		} finally {
			if (targetStream != null) {
				try {
					targetStream.close();
				} catch (IOException exception) {
					// Do nothing
				}
			}
			if (sourceStream != null) {
				try {
					sourceStream.close();
				} catch (IOException exception) {
					// Do nothing
				}
			}
		}
		return Status.OK_STATUS;
	}

	/**
	 * Performs the actual merge operation.
	 * 
	 * @param output
	 *            the output stream
	 * @param encoding
	 *            the output stream encoding
	 * @param target
	 *            the target input stream
	 * @param source
	 *            the source input stream
	 * @throws IOException
	 *             if an input/output error occurs
	 * @throws UnsupportedEncodingException
	 *             if the encoding is not supported
	 */
	private void performMerge(final OutputStream output, final String encoding, final InputStream target, final InputStream source) throws IOException, UnsupportedEncodingException {
		final RefactoringDescriptorProxy[] sourceProxies= RefactoringHistoryManager.readRefactoringDescriptorProxies(source, null, 0, Long.MAX_VALUE);
		final RefactoringDescriptorProxy[] targetProxies= RefactoringHistoryManager.readRefactoringDescriptorProxies(target, null, 0, Long.MAX_VALUE);
		final Set set= new HashSet();
		for (int index= 0; index < sourceProxies.length; index++)
			set.add(sourceProxies[index]);
		for (int index= 0; index < targetProxies.length; index++)
			set.add(targetProxies[index]);
		RefactoringHistoryManager.writeRefactoringDescriptorProxies(output, (RefactoringDescriptorProxy[]) set.toArray(new RefactoringDescriptorProxy[set.size()]));
	}
}
