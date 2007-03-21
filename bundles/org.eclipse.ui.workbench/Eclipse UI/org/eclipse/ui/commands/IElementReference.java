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

package org.eclipse.ui.commands;

import java.util.Map;

import org.eclipse.ui.menus.UIElement;

/**
 * the ICommandService will return a reference for all callbacks that are
 * registered. This reference can be used to unregister the specific callback.
 * <p>
 * Similar in functionality to an IHandlerActivation.  This interface should
 * not be implemented or extended by clients.
 * </p>
 * 
 * @since 3.3
 */
public interface IElementReference {
	/**
	 * The command id that this callback was registered against.
	 * 
	 * @return The command id. Will not be <code>null</code>.
	 */
	public String getCommandId();

	/**
	 * The callback that was registered.
	 * 
	 * @return Adapts to provide appropriate user feedback. Will not be
	 *         <code>null</code>.
	 */
	public UIElement getElement();

	/**
	 * Parameters that help scope this callback registration. For example, it
	 * can include parameters from the ParameterizedCommand. Callers should not
	 * change the map that is returned.
	 * 
	 * @return scoping parameters. Will not be <code>null</code>.
	 */
	public Map getParameters();
}
