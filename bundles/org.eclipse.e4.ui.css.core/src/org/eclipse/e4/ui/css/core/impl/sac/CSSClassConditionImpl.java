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

import org.eclipse.e4.ui.css.core.dom.CSSStylableElement;
import org.w3c.dom.Element;

/**
 * This class provides an implementation of the
 * {@link org.w3c.css.sac.AttributeCondition} interface. 
 */
public class CSSClassConditionImpl extends CSSAttributeConditionImpl {

	/**
	 * Creates a new CSSAttributeCondition object.
	 */
	public CSSClassConditionImpl(String localName, String namespaceURI,
			String value) {
		super(localName, namespaceURI, true, value);
	}

	public boolean match(Element e, String pseudoE) {
		String attr = null;
		if ((e instanceof CSSStylableElement))
			attr = ((CSSStylableElement) e).getCSSClass();
		else
			attr = e.getAttribute("class");
		if (attr == null || attr.length() < 1)
			return false;
		String val = getValue();
		int attrLen = attr.length();
		int valLen = val.length();
		for (int i = attr.indexOf(val); i != -1; i = attr.indexOf(val, i
				+ valLen))
			if ((i == 0 || Character.isSpaceChar(attr.charAt(i - 1)))
					&& (i + valLen == attrLen || Character.isSpaceChar(attr
							.charAt(i + valLen))))
				return true;

		return false;
	}
}
