/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.internal.forms;

import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.IMessageManager;
import org.eclipse.ui.forms.widgets.Form;

/**
 * The classes that implement this interface are responsible for managing custom
 * tool tips for message-related controls in the form header. By default, a
 * simple manager is installed by the form that uses the built-in widget tool
 * tips for this purpose. Clients can replace this behaviour with richer tool
 * tips that show images, links and other content.
 * <p>
 * The message-related controls in the header are:
 * <ul>
 * <li>Image label - used to replace the form title image with the message type
 * image</li>
 * <li>Message label - renders the message as a static text.</li>
 * <li>Message hyperlink - renders the message as a hyperlink.</li>
 * </ul>
 * The message manager will be asked to create the tool tip for any and all of
 * the controls listed above in its factory method. After that, it will be asked
 * to update whenever the message information changes in the form. For this
 * reason, the manager is expected to retain references to the tool tips and
 * update them with new content when asked.
 * 
 * @see IMessageManager
 * @see Form
 *      <p>
 *      <strong>EXPERIMENTAL</strong>. This class or interface has been added
 *      as part of a work in progress. There is no guarantee that this API will
 *      work or that it will remain the same. Please do not use this API without
 *      consulting with the Platform UA team.
 *      </p>
 * @since 3.3
 */
public interface IMessageToolTipManager {
	/**
	 * Creates the custom tool tip for the provided control.
	 * 
	 * @param control
	 *            the control for which to create a custom tool tip
	 * @param imageControl
	 *            <code>true</code> if the control is used to render the title
	 *            image, <code>false</code> otherwise.
	 */
	void createToolTip(Control control, boolean imageControl);

	/**
	 * Updates all the managed tool tips. The manager should get the current
	 * message, message type and optional children messages from the form to
	 * update the custom tool tips.
	 */
	void update();
}