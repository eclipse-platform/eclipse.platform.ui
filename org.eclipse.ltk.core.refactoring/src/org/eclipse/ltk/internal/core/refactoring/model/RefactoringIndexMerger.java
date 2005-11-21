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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.compare.IStreamMerger;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.eclipse.ltk.core.refactoring.RefactoringDescriptorProxy;

import org.eclipse.ltk.internal.core.refactoring.RefactoringCoreMessages;
import org.eclipse.ltk.internal.core.refactoring.RefactoringCorePlugin;
import org.eclipse.ltk.internal.core.refactoring.history.RefactoringHistoryManager;

/**
 * Stream merger for refactoring history indexes.
 * 
 * @since 3.2
 */
public final class RefactoringIndexMerger implements IStreamMerger {

	/**
	 * Creates a new refactoring index merger.
	 */
	public RefactoringIndexMerger() {
		// Do nothing
	}

	/**
	 * {@inheritDoc}
	 */
	public IStatus merge(final OutputStream output, final String outputEncoding, final InputStream ancestor, final String ancestorEncoding, final InputStream target, final String targetEncoding, final InputStream source, final String sourceEncoding, final IProgressMonitor monitor) {
		try {
			final RefactoringDescriptorProxy[] sourceProxies= RefactoringHistoryManager.readRefactoringDescriptorProxies(source, null, 0, Long.MAX_VALUE);
			final RefactoringDescriptorProxy[] targetProxies= RefactoringHistoryManager.readRefactoringDescriptorProxies(target, null, 0, Long.MAX_VALUE);
			final Set set= new HashSet();
			for (int index= 0; index < sourceProxies.length; index++)
				set.add(sourceProxies[index]);
			for (int index= 0; index < targetProxies.length; index++)
				set.add(targetProxies[index]);
			final RefactoringDescriptorProxy[] outputProxies= new RefactoringDescriptorProxy[set.size()];
			set.toArray(outputProxies);
			Arrays.sort(outputProxies, new Comparator() {

				public final int compare(final Object first, final Object second) {
					final RefactoringDescriptorProxy predecessor= (RefactoringDescriptorProxy) first;
					final RefactoringDescriptorProxy successor= (RefactoringDescriptorProxy) second;
					return (int) (successor.getTimeStamp() - predecessor.getTimeStamp());
				}
			});
			final StringBuffer buffer= new StringBuffer(256);
			for (int index= 0; index < outputProxies.length; index++) {
				final RefactoringDescriptorProxy proxy= outputProxies[index];
				buffer.append(proxy.getTimeStamp());
				buffer.append(RefactoringHistoryManager.DELIMITER_STAMP);
				buffer.append(proxy.getDescription());
				buffer.append(RefactoringHistoryManager.DELIMITER_ENTRY);
				output.write(buffer.toString().getBytes(outputEncoding));
			}
		} catch (IOException exception) {
			return new Status(IStatus.ERROR, RefactoringCorePlugin.getPluginId(), 1, RefactoringCoreMessages.RefactoringHistoryStreamMerger_error_io, exception);
		}
		return Status.OK_STATUS;
	}
}