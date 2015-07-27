/*

   Copyright 2002, 2015  The Apache Software Foundation

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */

/* This class copied from org.apache.batik.css.engine.sac */

package org.eclipse.e4.ui.css.core.impl.sac;

import java.util.Set;
import org.w3c.css.sac.Condition;
import org.w3c.dom.Element;

/**
 * This class provides an implementation of the
 * {@link org.w3c.css.sac.CombinatorCondition} interface.
 */
public class CSSAndConditionImpl extends AbstractCombinatorCondition {
	/**
	 * Creates a new CombinatorCondition object.
	 */
	public CSSAndConditionImpl(Condition c1, Condition c2) {
		super(c1, c2);
	}

	/**
	 * <b>SAC</b>: Implements {@link
	 * org.w3c.css.sac.Condition#getConditionType()}.
	 */
	@Override
	public short getConditionType() {
		return SAC_AND_CONDITION;
	}

	/**
	 * Tests whether this condition matches the given element.
	 */
	@Override
	public boolean match(Element e, String pseudoE) {
		return ((ExtendedCondition)getFirstCondition()).match(e, pseudoE) &&
				((ExtendedCondition)getSecondCondition()).match(e, pseudoE);
	}

	/**
	 * Fills the given set with the attribute names found in this selector.
	 */
	@Override
	public void fillAttributeSet(Set<String> attrSet) {
		((ExtendedCondition)getFirstCondition()).fillAttributeSet(attrSet);
		((ExtendedCondition)getSecondCondition()).fillAttributeSet(attrSet);
	}

	/**
	 * Returns a text representation of this object.
	 */
	@Override
	public String toString() {
		return String.valueOf( getFirstCondition() ) + getSecondCondition();
	}
}
