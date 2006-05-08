/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.commands;

/**
 * <p>
 * An instance of this interface can be used by clients to receive notification
 * of changes to one or more instances of <code>CommandImageManager</code>.
 * </p>
 * <p>
 * Clients may instantiate, but must not extend.
 * </p>
 * <p>
 * <strong>PROVISIONAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * <p>
 * This class is eventually intended to exist in
 * <code>org.eclipse.jface.commands</code>.
 * </p>
 * 
 * @since 3.2
 */
public interface ICommandImageManagerListener {

	/**
	 * Notifies that one or more properties of an instance of
	 * <code>CommandImageManager</code> have changed. Specific details are
	 * described in the <code>CommandImageManagerEvent</code>.
	 * 
	 * @param event
	 *            The event; never <code>null</code>.
	 */
	void commandImageManagerChanged(CommandImageManagerEvent event);

}
