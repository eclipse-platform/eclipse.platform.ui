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

/**
 * <p>
 * An instance of <code>IKeyConfigurationListener</code> can be used by clients
 * to receive notification of changes to one or more instances of
 * <code>IKeyConfiguration</code>.
 * </p>
 * <p>
 * This interface may be implemented by clients.
 * </p>
 *
 * @since 3.0
 * @see IKeyConfiguration#addKeyConfigurationListener(IKeyConfigurationListener)
 * @see IKeyConfiguration#removeKeyConfigurationListener(IKeyConfigurationListener)
 * @see org.eclipse.ui.commands.KeyConfigurationEvent
 * @deprecated Please use the bindings support in the "org.eclipse.jface"
 *             plug-in instead. This API is scheduled for deletion, see Bug
 *             431177 for details
 * @see org.eclipse.jface.bindings.ISchemeListener
 * @noreference This interface is scheduled for deletion.
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
@Deprecated
@SuppressWarnings("all")
public interface IKeyConfigurationListener {

	/**
	 * Notifies that one or more attributes of an instance of
	 * <code>IKeyConfiguration</code> have changed. Specific details are described
	 * in the <code>KeyConfigurationEvent</code>.
	 *
	 * @param keyConfigurationEvent the keyConfiguration event. Guaranteed not to be
	 *                              <code>null</code>.
	 */
	@Deprecated
	void keyConfigurationChanged(KeyConfigurationEvent keyConfigurationEvent);
}
