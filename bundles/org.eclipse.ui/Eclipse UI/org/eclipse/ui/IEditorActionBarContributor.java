package org.eclipse.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * A editor action bar contributor defines the actions for
 * one or more editors.  
 * <p>
 * Within the workbench there may be more than one open editor of a particular
 * type.  For instance, there may be 1 or more open Java Editors.  To avoid the 
 * creation of duplicate actions and action images the editor concept has been 
 * split into two.  An action contributor is responsable for the creation of 
 * actions.  The editor is responsible for action implementation.  Furthermore,
 * the contributor is shared by each open editor.  As a result of this design
 * there is only 1 set of actions for 1 or more open editors.
 * </p><p>
 * The relationship between editor and contributor is defined by
 * the <code>org.eclipse.ui.editorss</code> extension point in the plugin registry.  
 * For each extension an editor class and a contributor class must be defined. 
 * </p><p>
 * For convenience, an implementation of this interface has been 
 * created in <code>EditorActionBarContributor</code>.  Implementors
 * should subclass this and specialize as required.
 * </p>
 *
 * @see org.eclipse.ui.actions.EditorActionBarContributor
 */
public interface IEditorActionBarContributor {
/**
 * Initializes this contributor, which is expected to add contributions as
 * required to the given action bars and global action handlers.
 *
 * @param bars the action bars
 */
public void init(IActionBars bars);
/**
 * Sets the active editor for the contributor.  
 * Implementors should disconnect from the old editor, connect to the 
 * new editor, and update the actions to reflect the new editor.
 *
 * @param targetEditor the new editor target
 */
public void setActiveEditor(IEditorPart targetEditor);
}
