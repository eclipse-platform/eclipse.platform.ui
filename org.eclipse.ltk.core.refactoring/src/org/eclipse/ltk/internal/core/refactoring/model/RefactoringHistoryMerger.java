/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.core.refactoring.model;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.compare.IStreamMerger;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;

import org.eclipse.ltk.internal.core.refactoring.RefactoringCoreMessages;
import org.eclipse.ltk.internal.core.refactoring.RefactoringCorePlugin;
import org.eclipse.ltk.internal.core.refactoring.history.RefactoringHistoryManager;

/**
 * Stream merger for refactoring histories.
 * 
 * @since 3.2
 */
public final class RefactoringHistoryMerger implements IStreamMerger {

	/**
	 * Comparator class which compares refactoring descriptors with acending
	 * time stamps
	 */
	private static final class RefactoringDescriptorComparator implements Comparator {

		public final int compare(final Object first, final Object second) {
			final RefactoringDescriptor predecessor= (RefactoringDescriptor) first;
			final RefactoringDescriptor successor= (RefactoringDescriptor) second;
			return (int) (successor.getTimeStamp() - predecessor.getTimeStamp());
		}
	}

	/**
	 * Creates a new refactoring history merger.
	 */
	public RefactoringHistoryMerger() {
		// Do nothing
	}

	/**
	 * {@inheritDoc}
	 */
	public IStatus merge(final OutputStream output, final String outputEncoding, final InputStream ancestor, final String ancestorEncoding, final InputStream target, final String targetEncoding, final InputStream source, final String sourceEncoding, final IProgressMonitor monitor) {
		try {
			final RefactoringDescriptor[] sourceDescriptors= RefactoringHistoryManager.readRefactoringDescriptors(source, 0, Long.MAX_VALUE);
			final RefactoringDescriptor[] targetDescriptors= RefactoringHistoryManager.readRefactoringDescriptors(target, 0, Long.MAX_VALUE);
			final RefactoringDescriptor[] outputDescriptors= new RefactoringDescriptor[sourceDescriptors.length + targetDescriptors.length];
			System.arraycopy(sourceDescriptors, 0, outputDescriptors, 0, sourceDescriptors.length);
			System.arraycopy(targetDescriptors, 0, outputDescriptors, sourceDescriptors.length, targetDescriptors.length);
			Arrays.sort(outputDescriptors, new RefactoringDescriptorComparator());
			RefactoringHistoryManager.writeRefactoringDescriptors(output, outputDescriptors);
		} catch (CoreException exception) {
			return new Status(IStatus.ERROR, RefactoringCorePlugin.getPluginId(), 1, RefactoringCoreMessages.RefactoringHistoryStreamMerger_error_io, exception);
		}
		return Status.OK_STATUS;
	}
}