package org.eclipse.ui.internal.keys;

import java.util.Comparator;

import org.eclipse.ui.keys.ModifierKey;

abstract class AbstractModifierKeyComparator implements Comparator {

	public int compare(Object left, Object right) {
		ModifierKey modifierKeyLeft = (ModifierKey) left;
		ModifierKey modifierKeyRight = (ModifierKey) right;
		int modifierKeyLeftRank = rank(modifierKeyLeft);
		int modifierKeyRightRank = rank(modifierKeyRight);

		if (modifierKeyLeftRank != modifierKeyRightRank)
			return modifierKeyLeftRank - modifierKeyRightRank;
		else
			return modifierKeyLeft.compareTo(modifierKeyRight);
	}

	protected abstract int rank(ModifierKey modifierKey);
}
