/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.core;

import org.eclipse.core.runtime.*;
import org.eclipse.update.core.*;

/**
 * Install progress monitor
 * Delegating wrapper for IProgressMonitor used for installation handling.
 * 
 * @since 2.0
 */
public class MultiDownloadMonitor extends InstallMonitor {

	/**
	 * Install monitor constructor
	 * 
	 * @param monitor base install monitor
	 * @since 2.0
	 */
	public MultiDownloadMonitor(IProgressMonitor monitor, IFeature feature) {
		super(monitor);
		//super.setTaskName(Policy.bind("FeatureContentProvider.Downloading") + feature.getVersionedIdentifier().getIdentifier() + " ");
		super.setTaskName(feature.getVersionedIdentifier().getIdentifier() + ": ");
	}

	public void setTaskName(String name) {
		return;
	}
	
	public void subTask(String name) {
		return;
	}
	
	/**
	 * Sets the total number of bytes to copy.
	 * 
	 * @see #showCopyDetails(boolean)
	 * @see #setCopyCount(long)
	 * @param count total number of bytes to copy.
	 * @since 2.0
	 */
	public synchronized void setTotalCount(long count) {
		this.totalCopyCount += count;
	}

	/**
	 * Sets the number of bytes already copied.
	 * 
	 * @see #showCopyDetails(boolean)
	 * @see #setTotalCount(long)
	 * @param count number of bytes already copied.
	 * @since 2.0
	 */
	public void setCopyCount(long count) {
		if (showDetails && count > 0) {
			currentCount = count;
			long countK = count / 1024;
			long totalK = totalCopyCount / 1024;
			String msg =
				(totalK <= 0)
					? Policy.bind("InstallMonitor.DownloadSize", Long.toString(countK))
					: Policy.bind(
						"InstallMonitor.DownloadSizeLong",
						Long.toString(countK),
						Long.toString(totalK));
			//$NON-NLS-1$ //$NON-NLS-2$
			monitor.subTask(Policy.bind("FeatureContentProvider.Downloading") + " " + msg);
		}
	}
	
	/**
	 * Increments the number of bytes copied.
	 * 
	 * @see #showCopyDetails(boolean)
	 * @see #setTotalCount(long)
	 * @param count number of new bytes  copied.
	 * @since 2.0
	 */
	public synchronized void incrementCount(long increment) {
		setCopyCount(currentCount + increment);
	}
}

