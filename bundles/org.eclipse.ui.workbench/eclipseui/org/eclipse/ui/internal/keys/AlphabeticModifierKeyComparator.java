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

package org.eclipse.ui.internal.keys;

import java.util.Comparator;
import org.eclipse.ui.keys.ModifierKey;

/**
 * Compares modifier keys lexicographically by the name of the key.
 *
 * @since 3.0
 */
public class AlphabeticModifierKeyComparator implements Comparator<ModifierKey> {

	@Override
	public int compare(ModifierKey modifierKeyLeft, ModifierKey modifierKeyRight) {
		return modifierKeyLeft.toString().compareTo(modifierKeyRight.toString());
	}
}
