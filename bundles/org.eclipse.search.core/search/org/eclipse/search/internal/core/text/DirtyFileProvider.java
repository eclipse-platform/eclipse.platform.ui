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

import java.util.Map;

import org.eclipse.core.resources.IFile;

import org.eclipse.jface.text.IDocument;

public interface DirtyFileProvider {
	/**
	 * Discover a list of dirty IFile resources and the current content of those
	 * resources in the dirty editor
	 * 
	 * @return A map of dirty resources to contents
	 */
	Map<IFile, IDocument> dirtyFiles();
}
