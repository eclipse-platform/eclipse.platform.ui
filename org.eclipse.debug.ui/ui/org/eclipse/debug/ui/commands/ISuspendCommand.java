/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.ui.commands;

/**
 * A suspend command typically suspends an executing program.
 * <p>
 * Clients may implement this interface. The debug platform provides a
 * suspend action that delegates to this command interface. As well, the
 * debug platform provides an implementation of the suspend command registered
 * as an adapter on objects that implement
 * {@link org.eclipse.debug.core.model.ISuspendResume}.
 * </p>
 * @since 3.3
 * <p>
 * <strong>EXPERIMENTAL</strong>. This interface has been added as
 * part of a work in progress. There is no guarantee that this API will
 * remain unchanged during the 3.3 release cycle. Please do not use this API
 * without consulting with the Platform/Debug team.
 * </p>
 */
public interface ISuspendCommand extends IDebugCommand {

}
