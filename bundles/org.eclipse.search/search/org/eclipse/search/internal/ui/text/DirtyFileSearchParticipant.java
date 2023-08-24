/*******************************************************************************
 * Copyright (c) 2023 Red Hat Inc and others.
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
package org.eclipse.search.internal.ui.text;

import java.util.Map;

import org.eclipse.core.resources.IFile;

import org.eclipse.jface.text.IDocument;

import org.eclipse.search.internal.core.text.IDirtyFileSearchParticipant;
import org.eclipse.search.internal.ui.util.DirtyEditorUtility;

public class DirtyFileSearchParticipant implements IDirtyFileSearchParticipant {

	@Override
	public Map<IFile, IDocument> findDirtyFiles() {
		return new DirtyEditorUtility().evalNonFileBufferDocuments();
	}
}
