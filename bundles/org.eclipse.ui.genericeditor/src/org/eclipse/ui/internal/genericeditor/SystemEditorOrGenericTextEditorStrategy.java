/*******************************************************************************
 * Copyright (c) 2023 Andrey Loskutov (loskutov@gmx.de) and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrey Loskutov (loskutov@gmx.de)
 *******************************************************************************/
package org.eclipse.ui.internal.genericeditor;

import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.ide.IUnassociatedEditorStrategy;

/**
 * Strategy for unassociated file types:
 * <ol>
 * <li>The operating system is consulted to determine if an in-place component
 * editor is available (e.g. OLE editor on Win32 platforms).</li>
 * <li>The operating system is consulted to determine if an external editor is
 * available.</li>
 * <li>The workbench editor registry is consulted to determine if the generic
 * text editor is available.</li>
 * </ol>
 */
public final class SystemEditorOrGenericTextEditorStrategy implements IUnassociatedEditorStrategy {

	@Override
	public IEditorDescriptor getEditorDescriptor(String name, IEditorRegistry editorReg) {
		IEditorDescriptor editorDesc = null;
		// next check the OS for in-place editor (OLE on Win32)
		if (editorReg.isSystemInPlaceEditorAvailable(name)) {
			editorDesc = editorReg.findEditor(IEditorRegistry.SYSTEM_INPLACE_EDITOR_ID);
		}

		// next check with the OS for an external editor
		if (editorDesc == null && editorReg.isSystemExternalEditorAvailable(name)) {
			editorDesc = editorReg.findEditor(IEditorRegistry.SYSTEM_EXTERNAL_EDITOR_ID);
		}

		// next lookup the default text editor
		if (editorDesc == null) {
			editorDesc = editorReg.findEditor(ExtensionBasedTextEditor.GENERIC_EDITOR_ID);
		}
		return editorDesc;
	}
}