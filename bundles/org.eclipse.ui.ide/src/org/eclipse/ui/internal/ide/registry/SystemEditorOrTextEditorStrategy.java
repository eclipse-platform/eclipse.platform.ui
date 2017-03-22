/*******************************************************************************
 * Copyright (c) 2015 IBM Corportation, Red Hat Inc. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mickael Istria (Red Hat Inc.) - extracted from IDE.getEditorDescription
 *******************************************************************************/
package org.eclipse.ui.internal.ide.registry;

import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.ide.IUnassociatedEditorStrategy;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;

/**
 * Strategy for unassociated file types:
 * <ol>
 * <li>The operating system is consulted to determine if an in-place component
 * editor is available (e.g. OLE editor on Win32 platforms).</li>
 * <li>The operating system is consulted to determine if an external editor is
 * available.</li>
 * <li>The workbench editor registry is consulted to determine if the default
 * text editor is available.</li></li>
 * </ol>
 * This is the default strategy, as it mimics the legacy behavior of IDE before
 * {@link IUnassociatedEditorStrategy} got introduced.
 *
 * @since 3.12
 */
public final class SystemEditorOrTextEditorStrategy implements IUnassociatedEditorStrategy {
	/**
	 * Id of the extension. Defined in plugin.xml and may be used as value for
	 * preference {@link IDE#UNASSOCIATED_EDITOR_STRATEGY_PREFERENCE_KEY}
	 */
	public static final String EXTENSION_ID = "org.eclipse.ui.ide.systemEditorThenTextEditor"; //$NON-NLS-1$

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
			editorDesc = editorReg.findEditor(IDEWorkbenchPlugin.DEFAULT_TEXT_EDITOR_ID);
		}
		return editorDesc;
	}
}