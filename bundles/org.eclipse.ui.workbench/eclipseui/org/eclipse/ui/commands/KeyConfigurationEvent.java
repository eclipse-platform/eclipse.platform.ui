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
 * An instance of this class describes changes to an instance of
 * <code>IKeyConfiguration</code>.
 * <p>
 * This class is not intended to be extended by clients.
 * </p>
 *
 * @since 3.0
 * @see IKeyConfigurationListener#keyConfigurationChanged(KeyConfigurationEvent)
 * @deprecated Please use the bindings support in the "org.eclipse.jface"
 *             plug-in instead. This API is scheduled for deletion, see Bug
 *             431177 for details
 * @see org.eclipse.jface.bindings.SchemeEvent
 * @noreference This class is scheduled for deletion.
 */
@Deprecated
@SuppressWarnings("all")
public final class KeyConfigurationEvent {

	/**
	 * whether the key configuration has become or active or inactive.
	 */
	private final boolean activeChanged;

	/**
	 * Whether the key configuration has become defined or undefined.
	 */
	private final boolean definedChanged;

	/**
	 * The key configuration that has changed; this value is never
	 * <code>null</code>.
	 */
	private final IKeyConfiguration keyConfiguration;

	/**
	 * Whether the name of the key configuration has changed.
	 */
	private final boolean nameChanged;

	/**
	 * Whether the parent identifier has changed.
	 */
	private final boolean parentIdChanged;

	/**
	 * Creates a new instance of this class.
	 *
	 * @param keyConfiguration the instance of the interface that changed.
	 * @param activeChanged    true, iff the active property changed.
	 * @param definedChanged   true, iff the defined property changed.
	 * @param nameChanged      true, iff the name property changed.
	 * @param parentIdChanged  true, iff the parentId property changed.
	 */
	@Deprecated
	public KeyConfigurationEvent(IKeyConfiguration keyConfiguration, boolean activeChanged, boolean definedChanged,
			boolean nameChanged, boolean parentIdChanged) {
		if (keyConfiguration == null) {
			throw new NullPointerException();
		}

		this.keyConfiguration = keyConfiguration;
		this.activeChanged = activeChanged;
		this.definedChanged = definedChanged;
		this.nameChanged = nameChanged;
		this.parentIdChanged = parentIdChanged;
	}

	/**
	 * Returns the instance of the interface that changed.
	 *
	 * @return the instance of the interface that changed. Guaranteed not to be
	 *         <code>null</code>.
	 */
	@Deprecated
	public IKeyConfiguration getKeyConfiguration() {
		return keyConfiguration;
	}

	/**
	 * Returns whether or not the active property changed.
	 *
	 * @return true, iff the active property changed.
	 */
	@Deprecated
	public boolean hasActiveChanged() {
		return activeChanged;
	}

	/**
	 * Returns whether or not the defined property changed.
	 *
	 * @return true, iff the defined property changed.
	 */
	@Deprecated
	public boolean hasDefinedChanged() {
		return definedChanged;
	}

	/**
	 * Returns whether or not the name property changed.
	 *
	 * @return true, iff the name property changed.
	 */
	@Deprecated
	public boolean hasNameChanged() {
		return nameChanged;
	}

	/**
	 * Returns whether or not the parentId property changed.
	 *
	 * @return true, iff the parentId property changed.
	 */
	@Deprecated
	public boolean hasParentIdChanged() {
		return parentIdChanged;
	}
}
