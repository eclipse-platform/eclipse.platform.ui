package org.eclipse.ui.internal;

/************************************************************************
Copyright (c) 2000, 2003 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM - Initial implementation
************************************************************************/

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.WorkbenchException;

/**
 * Extends PartPluginAction for usage in editor parts. Objects
 * of this class are created by reading the registry (extension point "editorActions").
 */
public final class EditorPluginAction extends PartPluginAction {
	private IEditorPart currentEditor;

	/**
	 * This class adds the requirement that action delegates
	 * loaded on demand implement IViewActionDelegate.
	 * 
	 * @deprecated
	 */
	public EditorPluginAction(IConfigurationElement actionElement, String runAttribute, IEditorPart part, String definitionId) {
		this(actionElement, runAttribute, part, definitionId, IAction.AS_PUSH_BUTTON);
	}

	/**
	 * This class adds the requirement that action delegates
	 * loaded on demand implement IViewActionDelegate
	 */
	public EditorPluginAction(IConfigurationElement actionElement, String runAttribute, IEditorPart part, String definitionId, int style) {
		super(actionElement, runAttribute, definitionId, style);
		if (part != null)
			editorChanged(part);
	}

	/* (non-Javadoc)
	 * Method declared on PluginAction.
	 */
	protected IActionDelegate validateDelegate(Object obj) throws WorkbenchException {
		if (obj instanceof IEditorActionDelegate)
			return (IEditorActionDelegate)obj;
		else
			throw new WorkbenchException("Action must implement IEditorActionDelegate"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * Method declared on PluginAction.
	 */
	protected void initDelegate() {
		super.initDelegate();
		((IEditorActionDelegate)getDelegate()).setActiveEditor(this, currentEditor);
	}

	/**
	 * Handles editor change by re-registering for selection
	 * changes and updating IEditorActionDelegate.
	 */
	public void editorChanged(IEditorPart part) {
		if (currentEditor != null)
			unregisterSelectionListener(currentEditor);

		currentEditor = part;
		
		if (getDelegate() == null && isOkToCreateDelegate())
			createDelegate();
		if (getDelegate() != null)
			((IEditorActionDelegate)getDelegate()).setActiveEditor(this, part);

		if (part != null)
			registerSelectionListener(part);
	}
}
