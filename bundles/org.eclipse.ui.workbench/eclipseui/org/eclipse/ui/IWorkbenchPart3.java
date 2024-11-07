/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
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
 ******************************************************************************/
package org.eclipse.ui;

import java.util.Map;
import org.eclipse.jface.util.IPropertyChangeListener;

/**
 * A part can provide arbitrary properties. The properties will be persisted
 * between sessions by the part reference, and will be available from the part
 * reference as well as the part. The properties can only be set on a part, not
 * on the reference. The properties will be available to the IPresentablePart.
 * <p>
 * Setting a property must fire a PropertyChangeEvent.
 * </p>
 *
 * @since 3.3
 */
public interface IWorkbenchPart3 extends IWorkbenchPart2 {
	/**
	 * Add a listener for changes in the arbitrary properties set.
	 * <p>
	 * <b>Note:</b> this is a different set of properties than the ones covered by
	 * the IWorkbenchPartConstants.PROP_* constants.
	 * </p>
	 *
	 * @param listener Must not be <code>null</code>.
	 */
	void addPartPropertyListener(IPropertyChangeListener listener);

	/**
	 * Remove a change listener from the arbitrary properties set.
	 * <p>
	 * <b>Note:</b> this is a different set of properties than the ones covered by
	 * the IWorkbenchPartConstants.PROP_* constants.
	 * </p>
	 *
	 * @param listener Must not be <code>null</code>.
	 */
	void removePartPropertyListener(IPropertyChangeListener listener);

	/**
	 * Return the value for the arbitrary property key, or <code>null</code>.
	 *
	 * @param key the arbitrary property. Must not be <code>null</code>.
	 * @return the property value, or <code>null</code>.
	 */
	String getPartProperty(String key);

	/**
	 * Set an arbitrary property on the part. It is the implementor's responsibility
	 * to fire the corresponding PropertyChangeEvent.
	 * <p>
	 * A default implementation has been added to WorkbenchPart.
	 * </p>
	 *
	 * @param key   the arbitrary property. Must not be <code>null</code>.
	 * @param value the property value. A <code>null</code> value will remove that
	 *              property.
	 */
	void setPartProperty(String key, String value);

	/**
	 * Return an unmodifiable map of the arbitrary properties. This method can be
	 * used to save the properties during workbench save/restore.
	 *
	 * @return A Map of the properties. Must not be <code>null</code>.
	 */
	Map<String, String> getPartProperties();
}
