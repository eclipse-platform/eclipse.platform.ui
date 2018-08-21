/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ant.tests.core.testplugin;

import org.eclipse.ant.core.IAntPropertyValueProvider;

public class AntTestPropertyValueProvider implements IAntPropertyValueProvider {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ant.core.IAntPropertyValueProvider#getAntPropertyValue(java.lang.String)
	 */
	@Override
	public String getAntPropertyValue(String antPropertyName) {
		return "AntTestPropertyValueProvider"; //$NON-NLS-1$
	}

}
