package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.*;
import org.eclipse.ui.internal.registry.*;
import org.eclipse.ui.*;
import org.eclipse.ui.*;
import org.eclipse.jface.action.*;

/**
 * The action bars for an editor.
 */
public class EditorActionBars extends SubActionBars
{
	private String type;
	private int refCount;
	private IEditorActionBarContributor editorContributor;
	private IEditorActionBarContributor extensionContributor;
/**
 * Constructs the EditorActionBars for an editor.  
 */
public EditorActionBars(IActionBars parent, String type) {
	super(parent);
	this.type = type;
}
/**
 * Add one ref to the bars.
 */
public void addRef() {
	++ refCount;
}
/* (non-Javadoc)
 * Method declared on SubActionBars.
 */
protected SubMenuManager createSubMenuManager(IMenuManager parent) {
	return new EditorMenuManager(parent);
}
/* (non-Javadoc)
 * Method declared on SubActionBars.
 */
protected SubToolBarManager createSubToolBarManager(IToolBarManager parent) {
	return new EditorToolBarManager(parent);
}
/**
 * Gets the editor contributor
 */
public IEditorActionBarContributor getEditorContributor() {
	return editorContributor;
}
/**
 * Returns the editor type.
 */
public String getEditorType() {
	return type;
}
/**
 * Returns the reference count.
 */
public int getRef() {
	return refCount;
}
/**
 * Sets the target part for the action bars.
 * For views this is ignored because each view has its own action vector.
 * For editors this is important because the action vector is shared by editors of the same type.
 */
public void partChanged(IWorkbenchPart part) {
	super.partChanged(part);
	if (part instanceof IEditorPart) {
		IEditorPart editor = (IEditorPart)part;
		if (editorContributor != null)
			editorContributor.setActiveEditor(editor);
		if (extensionContributor != null)
			extensionContributor.setActiveEditor(editor);
	}
}
/**
 * Remove one ref to the bars.
 */
public void removeRef() {
	-- refCount;
}
/**
 * Sets the editor contributor
 */
public void setEditorContributor(IEditorActionBarContributor c) {
	editorContributor = c;
}
/**
 * Sets the extension contributor
 */
public void setExtensionContributor(IEditorActionBarContributor c) {
	extensionContributor = c;
}
}
