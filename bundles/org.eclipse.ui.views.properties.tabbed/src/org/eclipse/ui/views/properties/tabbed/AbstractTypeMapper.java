/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
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
package org.eclipse.ui.views.properties.tabbed;

/**
 * Default implementation of a type mapper.
 *
 * @author Anthony Hunter
 */
public class AbstractTypeMapper
    implements ITypeMapper {

    @Override
	public Class mapType(Object object) {
        return object.getClass();
    }

}
