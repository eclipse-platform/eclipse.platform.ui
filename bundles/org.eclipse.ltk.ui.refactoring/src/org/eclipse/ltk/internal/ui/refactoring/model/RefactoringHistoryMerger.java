/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
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
package org.eclipse.ltk.internal.ui.refactoring.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
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

/**
 * Stream merger for refactoring history files.
 *
 * @since 3.2
 */
public final class RefactoringHistoryMerger implements IStorageMerger {

	/**
	 * Creates a new refactoring history merger.
	 */
	public RefactoringHistoryMerger() {
		// Do nothing
	}

	@Override
	public boolean canMergeWithoutAncestor() {
		return true;
	}

	@Override
	public IStatus merge(final OutputStream output, final String encoding, final IStorage ancestor, final IStorage target, final IStorage other, final IProgressMonitor monitor) throws CoreException {
		try (InputStream targetStream= target.getContents();
				InputStream sourceStream= target.getContents()) {
			performMerge(output, targetStream, sourceStream);
		} catch (CoreException exception) {
			return new Status(IStatus.ERROR, RefactoringUIPlugin.getPluginId(), 1, RefactoringUIMessages.RefactoringHistoryMerger_error_auto_merge, exception);
		} catch (IOException exception) {
			// Do nothing
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
		final Set<DefaultRefactoringDescriptor> set= new HashSet<>(Arrays.asList(sourceDescriptors));
		set.addAll(Arrays.asList(targetDescriptors));
		final RefactoringDescriptor[] outputDescriptors= new RefactoringDescriptor[set.size()];
		set.toArray(outputDescriptors);
		RefactoringHistoryManager.sortRefactoringDescriptorsAscending(outputDescriptors);
		RefactoringHistoryManager.writeRefactoringSession(output, new RefactoringSessionDescriptor(outputDescriptors, IRefactoringSerializationConstants.CURRENT_VERSION, null), true);
	}
}
