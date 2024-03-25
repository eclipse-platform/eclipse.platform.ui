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
 * Use generic text editor for unassociated text files.
 * <p>
 * This allows to see syntax highlighting in all editors opened on "not
 * associated" file extensions, if there is a tm4e support for that syntax
 * available - for example with python, css, html, xml files.
 * </p>
 */
public final class GenericTextEditorStrategy implements IUnassociatedEditorStrategy {

	@Override
	public IEditorDescriptor getEditorDescriptor(String name, IEditorRegistry editorReg) {
		// don't care about file name and always return generic editor id
		// No files from "registered" editors will appear here
		return editorReg.findEditor(ExtensionBasedTextEditor.GENERIC_EDITOR_ID);
	}

}