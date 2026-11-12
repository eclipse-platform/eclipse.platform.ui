/*******************************************************************************
 * Copyright (c) 2026 Vector Informatik GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Vector Informatik GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.search.tests;

import org.eclipse.ui.texteditor.ITextEditor;

/**
 * {@link MultiFileEditor} that adapts to the text editor of its own file, whichever page is active.
 */
public class OwnPageAdaptingEditor extends MultiFileEditor {

	@Override
	public <T> T getAdapter(Class<T> adapter) {
		if (adapter == ITextEditor.class && getOwnEditor() != null) {
			return adapter.cast(getOwnEditor());
		}
		return super.getAdapter(adapter);
	}
}
