package org.eclipse.ui.externaltools.group;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
**********************************************************************/

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;

/**
 * Represents the API for a group of visual components
 * to access the dialog page that contains it.
 * <p>
 * This interface is not intended to be extended
 * nor implemented by clients.
 * </p>
 */
public interface IGroupDialogPage extends IMessageProvider {
	/**
	 * Sets the <code>GridData</code> on the specified button to
	 * be one that is spaced for the current dialog page units.
	 * 
	 * @param button the button to set the <code>GridData</code>
	 * @return the <code>GridData</code> set on the specified button
	 */
	public GridData setButtonGridData(Button button);

	/**
	 * Sets the message for this page with an indication of what type
	 * of message it is.
	 * <p>
	 * The valid message types are one of <code>NONE</code>, 
	 * <code>INFORMATION</code>, <code>WARNING</code>, or <code>ERROR</code>.
	 * </p>
	 *
	 * @param newMessage the message, or <code>null</code> to clear the message
	 * @param newType the message type
	 */
	public void setMessage(String newMessage, int newType);

	/**
	 * Updates the page's valid state using the group's
	 * current valid state. This will cause the dialog's
	 * buttons dependent on the page's valid state to
	 * update to reflect the new state.
	 */
	public void updateValidState();
	
	/**
	 * Converts a height in characters to a height in pixels.
	 * 
	 * @param chars the height in characters to be converted
	 * @return the corresponding height in pixels
	 */
	public int convertHeightHint(int chars);
}
