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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * A cheat sheet viewer.
 * <p>
 * Clients call {@link CheatSheetViewerFactory#createCheatSheetView()} to create
 * a cheat sheet viewer instance, and then call the viewer's 
 * <code>createPartControl</code> method to have it create the viewer's controls
 * under the specified SWT composite. The <code>setInput</code> methods are used
 * to set (or clear) the cheat sheet shown in the viewer, either before or after
 * the viewer's controls have been created.
 * </p>
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
	// TODO - to may be possible to get rid of this as API method
	public void dispose();

	/**
	 * Returns the primary control associated with this viewer.
	 *
	 * @return the SWT control which displays this viewer's
	 * content, or <code>null</code> if this viewer's controls
	 * have not yet been created.
	 */
	// TODO - is this method needed in API?
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
	 * Sets the cheat sheet viewer to show the cheat sheet with 
	 * the given id. The cheat sheet content file is located via the
	 * <code>org.eclipse.ui.cheatsheet.cheatSheetContent</code>
	 * extension point. The viewer shows an error message if there
	 * is no cheat sheet with the given id.
	 * </p>
	 * <p>
	 * The execution states of open cheat sheets are maintained
	 * and persisted globally using the cheat sheet id as the key. 
	 * </p>
	 * 
	 * @param id the cheat sheet id, or <code>null</code> to show
	 * no cheat sheet in this viewer
	 */
	public void setInput(String id);

	/**
	 * Sets the cheat sheet viewer to show the cheat sheet with the 
	 * given cheat sheet content file. The viewer shows an error
	 * message if the cheat sheet content file cannot be opened or
	 * parsed.
	 * <p>
	 * The execution states of open cheat sheets are maintained
	 * and persisted globally using the cheat sheet id as the key. 
	 * This means that each cheat sheet must have a distinct id,
	 * including ones opened from URLs.
	 * </p>
	 * <p>
	 * Use the other <code>setInput</code> method to clear
	 * the viewer; that is, call <code>setInput(null)</code>.
	 * </p>
	 * 
	 * @param id the id to give this cheat sheet
	 * @param name the name to give this cheat sheet
	 * @param url URL of the cheat sheet content file
	 * @exception IllegalArgumentException if the parameters
	 * are <code>null</code>
	 */
	public void setInput(String id, String name, URL url);
}
