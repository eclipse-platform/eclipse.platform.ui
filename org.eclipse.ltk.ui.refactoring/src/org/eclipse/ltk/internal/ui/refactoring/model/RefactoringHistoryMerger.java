/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
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
import java.util.HashSet;
import java.util.Set;

import org.eclipse.team.core.mapping.IStorageMerger;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.eclipse.core.resources.IStorage;

import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringSessionDescriptor;

import org.eclipse.ltk.internal.core.refactoring.IRefactoringSerializationConstants;
import org.eclipse.ltk.internal.core.refactoring.history.DefaultRefactoringDescriptor;
import org.eclipse.ltk.internal.core.refactoring.history.RefactoringHistoryManager;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringUIMessages;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringUIPlugin;

import org.eclipse.compare.IStreamMerger;

/**
 * Stream merger for refactoring history files.
 * 
 * @since 3.2
 */
public final class RefactoringHistoryMerger implements IStreamMerger, IStorageMerger {

	/**
	 * Creates a new refactoring history merger.
	 */
	public RefactoringHistoryMerger() {
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
	public IStatus merge(final OutputStream output, final String outputEncoding, final InputStream ancestor, final String ancestorEncoding, final InputStream target, final String targetEncoding, final InputStream source, final String sourceEncoding, final IProgressMonitor monitor) {
		try {
			performMerge(output, target, source);
		} catch (CoreException exception) {
			return new Status(IStatus.ERROR, RefactoringUIPlugin.getPluginId(), 1, RefactoringUIMessages.RefactoringHistoryMerger_error_auto_merge, exception);
		}
		return Status.OK_STATUS;
	}

	/**
	 * {@inheritDoc}
	 */
	public IStatus merge(final OutputStream output, final String encoding, final IStorage ancestor, final IStorage target, final IStorage other, final IProgressMonitor monitor) throws CoreException {
		InputStream targetStream= null;
		InputStream sourceStream= null;
		try {
			targetStream= target.getContents();
			sourceStream= target.getContents();
			performMerge(output, targetStream, sourceStream);
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
	 * @param target
	 *            the target input stream
	 * @param source
	 *            the source input stream
	 * @throws CoreException
	 *             if an error occurs
	 */
	private void performMerge(final OutputStream output, final InputStream target, final InputStream source) throws CoreException {
		final DefaultRefactoringDescriptor[] sourceDescriptors= RefactoringHistoryManager.readRefactoringDescriptors(source);
		final DefaultRefactoringDescriptor[] targetDescriptors= RefactoringHistoryManager.readRefactoringDescriptors(target);
		final Set set= new HashSet();
		for (int index= 0; index < sourceDescriptors.length; index++)
			set.add(sourceDescriptors[index]);
		for (int index= 0; index < targetDescriptors.length; index++)
			set.add(targetDescriptors[index]);
		final RefactoringDescriptor[] outputDescriptors= new RefactoringDescriptor[set.size()];
		set.toArray(outputDescriptors);
		RefactoringHistoryManager.sortRefactoringDescriptorsAscending(outputDescriptors);
		RefactoringHistoryManager.writeRefactoringSession(output, new RefactoringSessionDescriptor(outputDescriptors, IRefactoringSerializationConstants.CURRENT_VERSION, null), true);
	}
}
