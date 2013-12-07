/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.ui.css.core.resources;

import org.w3c.dom.css.CSSValue;

public class ResourceRegistryKeyFactory {
	public Object createKey(CSSValue value) {
		return CSSResourcesHelpers.getCSSValueKey(value);
	}
}
