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
package org.eclipse.jface.action;

/**
 * The IStatusLineWithProgressManager is the interface for status line managers
 * that can handle both static messages and those that are a result of
 * progress.
 * @since 3.0
 * 
 * <b>NOTE: This is experimental API and may change without notification</b>
 */
public interface IStatusLineWithProgressManager extends IStatusLineManager {

	/**
	 * Sets the message text to be displayed on the status line. The image on
	 * the status line is cleared.
	 * <p>
	 * This method replaces neither the current message but does not affect the
	 * error message. When the progress is done the progress message will be
	 * cleared and the message restored.
	 * </p> 
	 * 
	 * @param message
	 *           the message, or <code>null</code> for no message
	 */
	public void setProgressMessage(String message);
	
	/**
	 * Progress has completed. Restore the message or error message
	 * as appropriate.
	 *
	 */
	public void clearProgress();

}
