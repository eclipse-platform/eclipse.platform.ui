package org.eclipse.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * The primary interface between an editor part and the outside world.
 * <p>
 * The workbench exposes its implemention of editor part sites via this 
 * interface, which is not intended to be implemented or extended by clients.
 * </p>
 */
public interface IEditorSite extends IWorkbenchPartSite {
/**
 * Returns the editor action bar contributor for this editor.
 * <p>
 * An action contributor is responsable for the creation of actions.
 * By design, this contributor is used for one or more editors of the same type.
 * Thus, the contributor returned by this method is not owned completely
 * by the editor.  It is shared.
 * </p>
 *
 * @return the editor action bar contributor, or <code>null</code> if none exists
 */
public IEditorActionBarContributor getActionBarContributor();
}
