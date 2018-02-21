/*******************************************************************************
 * Copyright (c) 2008 Angelo Zerr and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.core.dom.properties.css2;

import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;

public interface CSS2FontProperties extends CSSValue {

	CSSPrimitiveValue getFamily();

	void setFamily(CSSPrimitiveValue family);

	CSSPrimitiveValue getSize();

	void setSize(CSSPrimitiveValue size);

	CSSPrimitiveValue getSizeAdjust();

	void setSizeAdjust(CSSPrimitiveValue sizeAdjust);

	CSSPrimitiveValue getWeight();

	void setWeight(CSSPrimitiveValue weight);

	CSSPrimitiveValue getStyle();

	void setStyle(CSSPrimitiveValue style);

	CSSPrimitiveValue getVariant();

	void setVariant(CSSPrimitiveValue variant);

	CSSPrimitiveValue getStretch();

	void setStretch(CSSPrimitiveValue stretch);
}
