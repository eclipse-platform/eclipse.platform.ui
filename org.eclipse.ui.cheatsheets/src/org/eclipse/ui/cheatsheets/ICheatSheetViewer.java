/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.cheatsheets;

import java.net.URL;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.widgets.Composite;

/**
 * A cheat sheat viewer.
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * 
 * @see CheatSheetViewerFactory
 * @since 3.0
 */
public interface ICheatSheetViewer {

	/**
	 * Creates the SWT controls for this cheat sheet viewer.
	 *
	 * @param parent the parent control
	 */
	public void createPartControl(Composite parent);

	/**
	 * Disposes of this cheat sheet viewer.
	 */
	public void dispose();

	/**
	 * Returns the primary control associated with this viewer.
	 *
	 * @return the SWT control which displays this viewer's content
	 */
	public Control getControl();

	/**
	 * Returns the id of the cheat sheet showing in this view.
	 * 
	 * @return id the cheat sheet id, or <code>null</code> if the
	 * view is not showing a cheat sheet
	 */
	public String getCheatSheetID();

	/**
	 * Asks this cheat sheet viewer to take focus.
	 */
	public void setFocus();

	/**
	 * Sets the cheat sheet view to the cheat sheet with the given
	 * id.
	 * 
	 * @param id the cheat sheet id
	 * @exception IllegalArgumentException if <code>id</code>
	 * is <code>null</code>
	 */
	public void setInput(String id);

	/**
	 * Sets the cheat sheet view to the cheat sheet with the given
	 * id.
	 * 
	 * @param id the cheat sheet id
	 * @param name the cheat sheet name
	 * @param url the url from which the cheat sheet will be loaded
	 * @exception IllegalArgumentException if <code>id</code>
	 * is <code>null</code>
	 */
	public void setInput(String id, String name, URL url);
}
