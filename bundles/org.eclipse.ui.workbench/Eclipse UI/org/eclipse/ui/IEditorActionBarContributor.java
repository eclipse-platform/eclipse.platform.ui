/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui;

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
 * the <code>org.eclipse.ui.editors</code> extension point in the plugin registry.  
 * For each extension an editor class and a contributor class must be defined. 
 * </p><p>
 * This interface should not be implemented directly. An implementation of this 
 * interface has been created in <code>EditorActionBarContributor</code>.  
 * Implementors should subclass this and specialize as required.
 * </p>
 *
 * @see IEditorActionBarContributor
 */
public interface IEditorActionBarContributor {
    /**
     * Initializes this contributor, which is expected to add contributions as
     * required to the given action bars and global action handlers.
     * <p>
     * The page is passed to support the use of <code>RetargetAction</code> by 
     * the contributor. In this case the init method implementors should: 
     * </p>
     * <p><ul>
     * <li>1) set retarget actions as global action handlers</li>
     * <li>2) add the retarget actions as part listeners</li>
     * <li>3) get the active part and if not <code>null</code> 
     * call partActivated on the retarget actions</li>
     * </ul></p>
     * <p>
     * And in the dispose method the retarget actions should be removed as part listeners.
     * </p>
     * 
     * @param bars the action bars
     * @param page the workbench page for this contributor
     * @since 2.0
     */
    public void init(IActionBars bars, IWorkbenchPage page);

    /**
     * Sets the active editor for the contributor.  
     * Implementors should disconnect from the old editor, connect to the 
     * new editor, and update the actions to reflect the new editor.
     *
     * @param targetEditor the new editor target
     */
    public void setActiveEditor(IEditorPart targetEditor);

    /**
     * Disposes this contributor. 
     * 
     * @since 2.0
     */
    public void dispose();
}
