/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui;


/**
 * The primary interface between an editor part and the outside world.
 * <p>
 * The workbench exposes its implemention of editor part sites via this 
 * interface, which is not intended to be implemented or extended by clients.
 * </p>
 */
public interface IEditorSite extends IWorkbenchPartSite {

	/**
	 * Returns the action bar contributor for this editor.
	 * <p>
	 * An action contributor is responsable for the creation of actions.
	 * By design, this contributor is used for one or more editors of the same type.
	 * Thus, the contributor returned by this method is not owned completely
	 * by the editor - it is shared.
	 * </p>
	 *
	 * @return the editor action bar contributor, or <code>null</code> if none exists
	 */
	public IEditorActionBarContributor getActionBarContributor();

	/**
	 * Returns the action bars for this part site. Editors of the same type
	 * share the same action bars. Contributions to the action bars are done
	 * by the <code>IEditorActionBarContributor</code>.
	 *
	 * @return the action bars
	 * @since 2.1
	 */
	public IActionBars getActionBars();
}
