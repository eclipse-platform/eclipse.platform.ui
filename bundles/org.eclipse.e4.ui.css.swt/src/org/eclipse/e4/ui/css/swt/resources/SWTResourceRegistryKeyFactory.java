/*******************************************************************************
 * Copyright (c) 2009, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.ui.css.swt.resources;

import org.eclipse.e4.ui.css.core.resources.ResourceRegistryKeyFactory;
import org.eclipse.e4.ui.css.swt.helpers.CSSSWTColorHelper;
import org.eclipse.e4.ui.css.swt.helpers.CSSSWTFontHelper;
import org.w3c.dom.css.CSSValue;

public class SWTResourceRegistryKeyFactory extends ResourceRegistryKeyFactory {
	@Override
	public Object createKey(CSSValue value) {
		Object key = super.createKey(value);
		if (CSSSWTColorHelper.hasColorDefinitionAsValue(value)
				|| CSSSWTFontHelper.hasFontDefinitionAsFamily(value)) {
			return new ResourceByDefinitionKey(key);
		}
		return key;
	}
}
