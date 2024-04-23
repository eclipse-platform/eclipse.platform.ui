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
import org.w3c.css.sac.Selector;
import org.w3c.css.sac.SimpleSelector;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * This class provides an implementation for the
 * {@link org.w3c.css.sac.DescendantSelector} interface.
 */
public class CSSDirectAdjacentSelectorImpl extends AbstractSiblingSelector {

	/**
	 * Creates a new CSSDirectAdjacentSelector object.
	 */
	public CSSDirectAdjacentSelectorImpl(short type, Selector parent, SimpleSelector simple) {
		super(type, parent, simple);
	}

	/**
	 * <b>SAC</b>: Implements {@link
	 * org.w3c.css.sac.Selector#getSelectorType()}.
	 */
	@Override
	public short getSelectorType() {
		return SAC_DIRECT_ADJACENT_SELECTOR;
	}

	/**
	 * Tests whether this selector matches the given element.
	 */
	@Override
	public boolean match(Element e, Node[] hiearchy, int parentIndex, String pseudoE) {
		Node n = e;
		if (!((ExtendedSelector) getSiblingSelector()).match(e, hiearchy, parentIndex, pseudoE)) {
			return false;
		}

		while ((n = n.getPreviousSibling()) != null && n.getNodeType() != Node.ELEMENT_NODE) {
		}

		if (n == null) {
			return false;
		}

		return ((ExtendedSelector) getSelector()).match((Element) n, hiearchy, parentIndex, null);
	}

	/**
	 * Tests whether this selector matches the given element.
	 */
	@Override
	public boolean match(Element e, String pseudoE) {
		Node n = e;
		if (!((ExtendedSelector)getSiblingSelector()).match(e, pseudoE)) {
			return false;
		}
		while ((n = n.getPreviousSibling()) != null && n.getNodeType() != Node.ELEMENT_NODE) {
		}

		if (n == null) {
			return false;
		}

		return ((ExtendedSelector)getSelector()).match((Element)n, null);
	}

	/**
	 * Fills the given set with the attribute names found in this selector.
	 */
	@Override
	public void fillAttributeSet(Set<String> attrSet) {
		((ExtendedSelector)getSelector()).fillAttributeSet(attrSet);
		((ExtendedSelector)getSiblingSelector()).fillAttributeSet(attrSet);
	}

	/**
	 * Returns a representation of the selector.
	 */
	@Override
	public String toString() {
		return getSelector() + " + " + getSiblingSelector();
	}
}
