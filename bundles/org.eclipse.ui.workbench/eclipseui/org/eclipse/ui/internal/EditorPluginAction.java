/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.WorkbenchException;

/**
 * Extends PartPluginAction for usage in editor parts. Objects of this class are
 * created by reading the registry (extension point "editorActions").
 */
public final class EditorPluginAction extends PartPluginAction {
	private IEditorPart currentEditor;

	/**
	 * This class adds the requirement that action delegates loaded on demand
	 * implement IViewActionDelegate
	 */
	public EditorPluginAction(IConfigurationElement actionElement, IEditorPart part, String id, int style) {
		super(actionElement, id, style);
		if (part != null) {
			editorChanged(part);
		}
	}

	@Override
	protected IActionDelegate validateDelegate(Object obj) throws WorkbenchException {
		if (obj instanceof IEditorActionDelegate) {
			return (IEditorActionDelegate) obj;
		}
		throw new WorkbenchException("Action must implement IEditorActionDelegate"); //$NON-NLS-1$
	}

	@Override
	protected void initDelegate() {
		super.initDelegate();
		((IEditorActionDelegate) getDelegate()).setActiveEditor(this, currentEditor);
	}

	/**
	 * Handles editor change by re-registering for selection changes and updating
	 * IEditorActionDelegate.
	 */
	public void editorChanged(IEditorPart part) {
		if (currentEditor != null) {
			unregisterSelectionListener(currentEditor);
		}

		currentEditor = part;

		if (getDelegate() == null && isOkToCreateDelegate()) {
			createDelegate();
		}
		if (getDelegate() != null) {
			((IEditorActionDelegate) getDelegate()).setActiveEditor(this, part);
		}

		if (part != null) {
			registerSelectionListener(part);
		}
	}

	@Override
	public void dispose() {
		if (currentEditor != null) {
			unregisterSelectionListener(currentEditor);
			currentEditor = null;
		}
		super.dispose();
	}
}
