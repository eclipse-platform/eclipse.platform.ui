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
public EditorPluginAction(IConfigurationElement actionElement, String runAttribute) {
	this(actionElement, runAttribute, null);
}
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
 * Creates an instance of the delegate class as defined on
 * the configuration element. It will also initialize
 * it with the editor part.
 */
protected IActionDelegate createDelegate() {
	IActionDelegate delegate = super.createDelegate();
	if (delegate == null)
		return null;
	if (delegate instanceof IEditorActionDelegate) {
		IEditorActionDelegate editorDelegate = (IEditorActionDelegate) delegate;
	} else {
		WorkbenchPlugin.log("Action should implement IEditorActionDelegate: " + getText());//$NON-NLS-1$
		return null;
	}
	return delegate;
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
		   setDelegate(createDelegate());
	}
	if (getDelegate() != null) {
		IEditorActionDelegate editorDelegate = (IEditorActionDelegate)getDelegate();
		editorDelegate.setActiveEditor(this, part);
	}
	registerSelectionListener(part);
}
}
