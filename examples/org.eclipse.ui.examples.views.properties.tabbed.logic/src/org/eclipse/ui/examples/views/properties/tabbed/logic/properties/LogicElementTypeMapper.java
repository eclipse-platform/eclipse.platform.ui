/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.examples.views.properties.tabbed.logic.properties;

import org.eclipse.gef.EditPart;
import org.eclipse.ui.views.properties.tabbed.ITypeMapper;

/**
 * Type mapper for the logic example. We want to get the GEF model
 * object from the selected element in the outline view and the diagram.
 * We can then filter on the model object type.
 * 
 * @author Anthony Hunter 
 */
public class LogicElementTypeMapper
	implements ITypeMapper {

	/**
	 * @inheritDoc 
	 */
	public Class mapType(Object object) {
		Class type = object.getClass();
		if (object instanceof EditPart) {
			type = ((EditPart) object).getModel().getClass();
		}
		return type;
	}
}