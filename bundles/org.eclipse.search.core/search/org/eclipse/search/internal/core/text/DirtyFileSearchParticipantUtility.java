/*******************************************************************************
 * Copyright (c) 2023 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Inc - initial API and implementation
 *******************************************************************************/
package org.eclipse.search.internal.core.text;

import java.util.Collections;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.Platform;

import org.eclipse.core.resources.IFile;

import org.eclipse.jface.text.IDocument;

import org.eclipse.search.internal.core.SearchCorePlugin;

public class DirtyFileSearchParticipantUtility {
	private static final String EXTENSION_POINT_ID = "org.eclipse.search.dirtyFileSearchParticipant"; //$NON-NLS-1$
	private static final String NODE_NAME = "dirtyFileSearchParticipant"; //$NON-NLS-1$
	private static final String ATTRIB_CLASS = "class"; //$NON-NLS-1$

	public static IDirtyFileSearchParticipant findFirstDirtyFileSearchParticipant() {
		final IDirtyFileSearchParticipant[] res = new IDirtyFileSearchParticipant[] { null };

		ISafeRunnable safe = new ISafeRunnable() {
			@Override
			public void run() throws Exception {
				IConfigurationElement[] extensions = Platform.getExtensionRegistry()
						.getConfigurationElementsFor(SearchCorePlugin.PLUGIN_ID, NODE_NAME);
				for (IConfigurationElement curr : extensions) {
					if (NODE_NAME.equals(curr.getName())) {
						res[0] = (IDirtyFileSearchParticipant) curr.createExecutableExtension(ATTRIB_CLASS);
						return;
					}
				}
			}

			@Override
			public void handleException(Throwable e) {
				SearchCorePlugin.log(e);
			}
		};
		try {
			safe.run();
		} catch (Exception | LinkageError e) {
			safe.handleException(e);
		}

		if (res[0] == null) {
			res[0] = new IDirtyFileSearchParticipant() {
				@Override
				public Map<IFile, IDocument> findDirtyFiles() {
					return Collections.EMPTY_MAP;
				}
			};
		}
		return res[0];
	}
}
