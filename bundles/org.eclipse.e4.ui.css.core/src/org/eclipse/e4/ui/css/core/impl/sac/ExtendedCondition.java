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
 * This interface provides additional features to the
 * {@link org.w3c.css.sac.Condition} interface.
 */
public interface ExtendedCondition extends Condition {

	/**
	 * Tests whether this condition matches the given element.
	 */
	boolean match(Element e, String pseudoE);

	/**
	 * Returns the specificity of this condition.
	 */
	int getSpecificity();

	/**
	 * Fills the given set with the attribute names found in this selector.
	 */
	void fillAttributeSet(Set<String> attrSet);
}
