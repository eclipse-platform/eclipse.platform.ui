package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.ui.*;
/**
 * Extends PartPluginAction for usage in editor parts. Objects
 * of this class are created by reading the registry (extension point "editorActions").
 */
public final class EditorPluginAction extends PartPluginAction {
	private IEditorPart currentEditor;
/**
 * This class adds the requirement that action delegates
 * loaded on demand implement IViewActionDelegate
 */
public EditorPluginAction(IConfigurationElement actionElement, String runAttribute, IEditorPart part) {
	super(actionElement, runAttribute);
	if (part != null) 
		editorChanged(part);
}

/** 
 * Initialize an action delegate.
 * Subclasses may override this.
 */
protected IActionDelegate initDelegate(Object obj) 
	throws WorkbenchException
{
	if (obj instanceof IEditorActionDelegate) {
		IEditorActionDelegate ead = (IEditorActionDelegate)obj;
		ead.setActiveEditor(this, currentEditor);
		return ead;
	} else
		throw new WorkbenchException("Action must implement IEditorActionDelegate"); //$NON-NLS-1$
}

/**
 * Handles editor change by re-registering for selection
 * changes and updating IEditorActionDelegate.
 */
public void editorChanged(IEditorPart part) {
	if (currentEditor!=null) {
		unregisterSelectionListener(currentEditor);
	}
	currentEditor = part;
	if (getDelegate() == null) {
		if (isOkToCreateDelegate())
		   createDelegate();
	}
	if (getDelegate() != null) {
		IEditorActionDelegate editorDelegate = (IEditorActionDelegate)getDelegate();
		editorDelegate.setActiveEditor(this, part);
	}
	registerSelectionListener(part);
}
}
