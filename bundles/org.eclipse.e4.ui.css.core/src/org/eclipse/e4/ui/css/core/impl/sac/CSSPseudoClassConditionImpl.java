/*

   Copyright 2002  The Apache Software Foundation

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
import org.eclipse.e4.ui.css.core.dom.CSSStylableElement;
import org.w3c.dom.Element;

/**
 * This class provides an implementation of the
 * {@link org.w3c.css.sac.AttributeCondition} interface.
 */
public class CSSPseudoClassConditionImpl extends AbstractAttributeCondition {
	/**
	 * The namespaceURI.
	 */
	protected String namespaceURI;

	/**
	 * Creates a new CSSAttributeCondition object.
	 */
	public CSSPseudoClassConditionImpl(String namespaceURI, String value) {
		super(value);
		this.namespaceURI = namespaceURI;
	}

	/**
	 * Indicates whether some other object is "equal to" this one.
	 *
	 * @param obj
	 *            the reference object with which to compare.
	 */
	@Override
	public boolean equals(Object obj) {
		if (!super.equals(obj)) {
			return false;
		}
		CSSPseudoClassConditionImpl c = (CSSPseudoClassConditionImpl) obj;
		return c.namespaceURI.equals(namespaceURI);
	}

	/**
	 * equal objects should have equal hashCodes.
	 *
	 * @return hashCode of this CSSPseudoClassCondition
	 */
	@Override
	public int hashCode() {
		return namespaceURI.hashCode();
	}

	/**
	 * <b>SAC</b>: Implements {@link
	 * org.w3c.css.sac.Condition#getConditionType()}.
	 */
	@Override
	public short getConditionType() {
		return SAC_PSEUDO_CLASS_CONDITION;
	}

	/**
	 * <b>SAC</b>: Implements {@link
	 * org.w3c.css.sac.AttributeCondition#getNamespaceURI()}.
	 */
	@Override
	public String getNamespaceURI() {
		return namespaceURI;
	}

	/**
	 * <b>SAC</b>: Implements {@link
	 * org.w3c.css.sac.AttributeCondition#getLocalName()}.
	 */
	@Override
	public String getLocalName() {
		return null;
	}

	/**
	 * <b>SAC</b>: Implements {@link
	 * org.w3c.css.sac.AttributeCondition#getSpecified()}.
	 */
	@Override
	public boolean getSpecified() {
		return false;
	}

	/**
	 * Tests whether this selector matches the given element.
	 */
	@Override
	public boolean match(Element e, String pseudoE) {
		if (pseudoE != null && !pseudoE.equals(getValue())) {
			// pseudo instance is filled, it is not valid.
			return false;
		}
		if (!(e instanceof CSSStylableElement)) {
			return false;
		}
		CSSStylableElement element = (CSSStylableElement) e;
		boolean isPseudoInstanceOf = element.isPseudoInstanceOf(getValue());
		if (!isPseudoInstanceOf) {
			return false;
		}
		if (pseudoE == null) {
			// pseudo element is not filled.
			// test if this CSSPseudoClassCondition is NOT a static pseudo
			// instance
			return (!element.isStaticPseudoInstance(getValue()));
		}
		return true;
	}

	/**
	 * Fills the given set with the attribute names found in this selector.
	 */
	@Override
	public void fillAttributeSet(Set<String> attrSet) {
	}

	/**
	 * Returns a text representation of this object.
	 */
	@Override
	public String toString() {
		return ":" + getValue();
	}
}
