/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.fields;

import org.eclipse.swt.widgets.Control;

/**
 * This interface is used to set and retrieve text content from an arbitrary
 * control. Clients are expected to implement this interface when defining a
 * {@link ContentProposalAdapter}, in order to specify how to retrieve and set
 * the contents of the control being adapted.
 * 
 * <p>
 * This API is considered experimental. It is still evolving during 3.2 and is
 * subject to change. It is being released to obtain feedback from early
 * adopters.
 * 
 * @since 3.2
 */
public interface IControlContentAdapter {
	/**
	 * Set the contents of the specified control to the specified text. Must not
	 * be <code>null</code>.
	 * 
	 * @param control
	 *            the control whose contents are to be set (replaced).
	 * @param contents
	 *            the String specifying the new control content.
	 */
	public void setControlContents(Control control, String contents);

	/**
	 * Insert the specified contents into the control's current contents. Must
	 * not be <code>null</code>.
	 * 
	 * @param control
	 *            the control whose contents are to be altered.
	 * @param contents
	 *            the String to be inserted into the control contents.
	 */
	public void insertControlContents(Control control, String contents);

	/**
	 * Get the text contents of the control.
	 * 
	 * @param control
	 *            the control whose contents are to be retrieved.
	 * @return the String contents of the control.
	 */
	public String getControlContents(Control control);
}
