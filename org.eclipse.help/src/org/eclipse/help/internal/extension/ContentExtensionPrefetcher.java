/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.extension;

import org.eclipse.help.IContentExtension;

/*
 * Fetches all data from the given extension interface and caches it into
 * a model element, for later performance and safety.
 */
public class ContentExtensionPrefetcher {

	/*
	 * Prefetches the given content extension.
	 */
	public static ContentExtension prefetch(IContentExtension original) {
		String content = original.getContent();
		String path = original.getPath();
		int type = original.getType();
		return new ContentExtension(content, path, type);
	}
}
