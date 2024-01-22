/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.commands;

import org.eclipse.ui.keys.KeySequence;

/**
 * <p>
 * An instance of <code>IKeySequenceBinding</code> represents a binding between
 * a command and a key sequence. This is a wrapper for the a key sequence.
 * </p>
 * <p>
 * This interface is not intended to be extended or implemented by clients.
 * </p>
 *
 * @since 3.0
 * @see org.eclipse.ui.commands.ICommand
 * @deprecated Please use the bindings support in the "org.eclipse.jface"
 *             plug-in instead. This API is scheduled for deletion, see Bug
 *             431177 for details
 * @see org.eclipse.jface.bindings.keys.KeyBinding
 * @noimplement This interface is not intended to be implemented by clients.
 * @noreference This interface is scheduled for deletion.
 * @noextend This interface is not intended to be extended by clients.
 */
@Deprecated
@SuppressWarnings("all")
public interface IKeySequenceBinding extends Comparable {

	/**
	 * Returns the key sequence represented in this binding.
	 *
	 * @return the key sequence. Guaranteed not to be <code>null</code>.
	 */
	@Deprecated
	KeySequence getKeySequence();
}
