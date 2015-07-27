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
import org.w3c.css.sac.LangCondition;
import org.w3c.dom.Element;

/**
 * This class provides an implementation of the
 * {@link org.w3c.css.sac.LangCondition} interface.
 */
public class CSSLangConditionImpl implements LangCondition, ExtendedCondition {
	/**
	 * The language.
	 */
	protected String lang;

	/**
	 * The language with a hyphen suffixed.
	 */
	protected String langHyphen;

	/**
	 * Creates a new LangCondition object.
	 */
	public CSSLangConditionImpl(String lang) {
		this.lang = lang.toLowerCase();
		this.langHyphen = lang + '-';
	}

	/**
	 * Indicates whether some other object is "equal to" this one.
	 * @param obj the reference object with which to compare.
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null || (obj.getClass() != getClass())) {
			return false;
		}
		CSSLangConditionImpl c = (CSSLangConditionImpl)obj;
		return c.lang.equals(lang);
	}

	/**
	 * <b>SAC</b>: Implements {@link
	 * org.w3c.css.sac.Condition#getConditionType()}.
	 */
	@Override
	public short getConditionType() {
		return SAC_LANG_CONDITION;
	}

	/**
	 * <b>SAC</b>: Implements {@link org.w3c.css.sac.LangCondition#getLang()}.
	 */
	@Override
	public String getLang() {
		return lang;
	}

	/**
	 * Returns the specificity of this condition.
	 */
	@Override
	public int getSpecificity() {
		return 1 << 8;
	}

	/**
	 * Tests whether this condition matches the given element.
	 */
	@Override
	public boolean match(Element e, String pseudoE) {
		String s = e.getAttribute("lang").toLowerCase();
		if (s.equals(lang) || s.startsWith(langHyphen)) {
			return true;
		}
		//        s = e.getAttributeNS(XMLConstants.XML_NAMESPACE_URI,
		//                             XMLConstants.XML_LANG_ATTRIBUTE).toLowerCase();
		return s.equals(lang) || s.startsWith(langHyphen);
	}

	/**
	 * Fills the given set with the attribute names found in this selector.
	 */
	@Override
	public void fillAttributeSet(Set<String> attrSet) {
		attrSet.add("lang");
	}

	/**
	 * Returns a text representation of this object.
	 */
	@Override
	public String toString() {
		return ":lang(" + lang + ')';
	}
}
