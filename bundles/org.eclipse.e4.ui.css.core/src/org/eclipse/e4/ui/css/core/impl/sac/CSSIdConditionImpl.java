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
public class CSSIdConditionImpl extends AbstractAttributeCondition {

	/**
	 * The id attribute namespace URI.
	 */
	protected String namespaceURI;

	/**
	 * The id attribute local name.
	 */
	protected String localName;

	/**
	 * Creates a new CSSAttributeCondition object.
	 */
	public CSSIdConditionImpl(String ns, String ln, String value) {
		super(value);
		namespaceURI = ns;
		localName = ln;
	}

	/**
	 * <b>SAC</b>: Implements {@link
	 * org.w3c.css.sac.Condition#getConditionType()}.
	 */
	@Override
	public short getConditionType() {
		return SAC_ID_CONDITION;
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
		return localName;
	}

	/**
	 * <b>SAC</b>: Implements {@link
	 * org.w3c.css.sac.AttributeCondition#getSpecified()}.
	 */
	@Override
	public boolean getSpecified() {
		return true;
	}

	/**
	 * Tests whether this condition matches the given element.
	 */
	@Override
	public boolean match(Element e, String pseudoE) {
		String id = null;
		if (e instanceof CSSStylableElement) {
			id = ((CSSStylableElement) e).getCSSId();
		} else
			id = e.getAttribute("id");
		if (id == null)
			return false;
		return id.equals(getValue());
		// return super.match(e, pseudoE);
	}

	/**
	 * Fills the given set with the attribute names found in this selector.
	 */
	@Override
	public void fillAttributeSet(Set attrSet) {
		attrSet.add(localName);
	}

	/**
	 * Returns the specificity of this condition.
	 */
	@Override
	public int getSpecificity() {
		return 1 << 16;
	}

	/**
	 * Returns a text representation of this object.
	 */
	@Override
	public String toString() {
		return '#' + getValue();
	}
}
