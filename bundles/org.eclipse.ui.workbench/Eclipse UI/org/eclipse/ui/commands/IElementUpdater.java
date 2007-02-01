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
 * An IHandler for a command that expects to provide feedback through the
 * registered element mechanism must implement this interface.
 * <p>
 * <strong>PROVISIONAL</strong>. This class or interface has been added as part
 * of a work in progress. There is a guarantee neither that this API will work
 * nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.3
 * 
 */
public interface IElementUpdater {
	/**
	 * Whenever the elements for a command are refreshed, this method is called
	 * on the active handler for that command.
	 * <p>
	 * <b>Note:</b> Handlers must never cache the element, which can disappear
	 * or be replaced at any time. Everybody should go through the
	 * ICommandService refreshElements(*) method.
	 * </p>
	 * 
	 * @param element
	 *            An element for a specific UI element. Will not be
	 *            <code>null</code>.
	 * @param parameters
	 *            Any parameters registered with the callback. Will not be
	 *            <code>null</code>, but it may be empty.
	 */
	public void updateElement(UIElement element, Map parameters);
}
