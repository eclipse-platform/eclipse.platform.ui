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
package org.eclipse.team.core.synchronize;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.variants.*;
import org.eclipse.team.internal.core.subscribers.*;

/**
 * A <code>SyncInfoFilter</code> tests a <code>SyncInfo</code> for inclusion,
 * typically in a <code>SyncInfoSet</code>.
 * 
 * @see SyncInfo
 * @see SyncInfoSet
 * 
 * @since 3.0
 */
public abstract class SyncInfoFilter {
	
	/**
	 * Selects <code>SyncInfo</code> whose local and remote contents match.
	 * This filter makes use of the <code>IStorage</code> provided by
	 * an <code>IResourceVariant</code> to obtain the remote contents.
	 * This means that the comparison may contact the server unless the contents
	 * were cached locally by a previous operation. The caching of remote
	 * contents is subscriber specific. 
	 * <p>
	 * For folders, the comparison always returns <code>true</code>.
	 */
	public static class ContentComparisonSyncInfoFilter extends SyncInfoFilter {
		ContentComparator criteria = new ContentComparator(false);
		/**
		 * Create a filter that does not ignore whitespace.
		 */
		public ContentComparisonSyncInfoFilter() {
			this(false);
		}
		/**
		 * Create a filter and configure how whitspace is handled.
		 * @param ignoreWhitespace whether whitespace should be ignored
		 */
		public ContentComparisonSyncInfoFilter(boolean ignoreWhitespace) {
			criteria = new ContentComparator(ignoreWhitespace);
		}
		public boolean select(SyncInfo info, IProgressMonitor monitor) {
			IResourceVariant remote = info.getRemote();
			IResource local = info.getLocal();
			if (local.getType() != IResource.FILE) return true;
			if (remote == null) return !local.exists();
			if (!local.exists()) return false;
			return criteria.compare(local, remote, monitor);
		}
	}
	
	/**
	 * Return <code>true</code> if the provided <code>SyncInfo</code> matches the filter.
	 * 
	 * @param info the <code>SyncInfo</code> to be tested
	 * @param monitor a progress monitor
	 * @return <code>true</code> if the <code>SyncInfo</code> matches the filter
	 */
	public abstract boolean select(SyncInfo info, IProgressMonitor monitor);
	
}
