package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jface.action.*;

import org.eclipse.swt.SWT;

import org.eclipse.ui.*;

/**
 * The action bars for an editor.
 */
public class EditorActionBars extends SubActionBars
{
	private String type;
	private int refCount;
	private IEditorActionBarContributor editorContributor;
	private IEditorActionBarContributor extensionContributor;
	private CoolBarContributionItem coolBarItem;
/**
 * Constructs the EditorActionBars for an editor.  
 */
public EditorActionBars(IActionBars parent, String type) {
	super(parent);
	this.type = type;
}
/**
 * Activate the contributions.
 */
public void activate(boolean forceVisibility) {
	setActive(true, forceVisibility);
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
 * Deactivate the contributions.
 */
public void deactivate(boolean forceVisibility) {
	setActive(false, forceVisibility);
}
/**
 * Dispose the contributions.
 */
public void dispose() {
	super.dispose();
	if (coolBarItem != null)
		coolBarItem.dispose();
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
 * Returns the tool bar manager.  If items are added or
 * removed from the manager be sure to call <code>updateActionBars</code>.
 * Overridden to support CoolBars.
 *
 * @return the tool bar manager
 */
public IToolBarManager getToolBarManager() {
	IToolBarManager parentMgr = parent.getToolBarManager();
	if (parentMgr instanceof ToolBarManager) {
		return super.getToolBarManager();
	} else if (parentMgr instanceof CoolBarManager) {
		if (coolBarItem == null) {
			// Create a CoolBar item for this action bar.
			CoolBarManager cBarMgr = ((CoolBarManager)parentMgr);
			ToolBarManager tBarMgr = new ToolBarManager(cBarMgr.getStyle());
			toolBarMgr = createSubToolBarManager(tBarMgr);
			coolBarItem = new CoolBarContributionItem(cBarMgr, tBarMgr, type);
			coolBarItem.setVisible(active);
		}
		return toolBarMgr;
	}
	return null;
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
 * Activate / Deactivate the contributions.
 * 
 * Workaround for flashing when editor contributes
 * many menu/tool contributions. In this case, the force visibility
 * flag determines if the contributions should be actually
 * made visible/hidden or just change the enablement state.
 */
private void setActive(boolean set, boolean forceVisibility) {
	active = set;
	if (menuMgr != null)
		((EditorMenuManager)menuMgr).setVisible(set, forceVisibility);
	
	if (statusLineMgr != null)
		statusLineMgr.setVisible(set);
		
	if (toolBarMgr != null)
		((EditorToolBarManager)toolBarMgr).setVisible(set, forceVisibility);

	if (coolBarItem != null)
		coolBarItem.setVisible(set, forceVisibility);
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
