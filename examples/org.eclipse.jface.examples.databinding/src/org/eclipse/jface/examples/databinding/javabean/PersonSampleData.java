/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.examples.databinding.javabean;

import java.util.Map;

import org.eclipse.jface.databinding.BindingException;
import org.eclipse.jface.databinding.DatabindingContext;
import org.eclipse.jface.databinding.IUpdatable;
import org.eclipse.jface.databinding.IUpdatableFactory2;
import org.eclipse.jface.databinding.IValidationContext;
import org.eclipse.jface.databinding.PropertyDescription;
import org.eclipse.jface.databinding.swt.SWTDatabindingContext;
import org.eclipse.swt.widgets.Control;

/**
 * @since 3.2
 * 
 */
public class PersonSampleData {

	/**
	 * @param aControl
	 * @return the data binding service
	 */
	public static DatabindingContext getSWTtoJavaBeanDatabindingContext(
			Control aControl) {

		DatabindingContext dbc = new SWTDatabindingContext(aControl);

		IUpdatableFactory2 emfValueFactory = new IUpdatableFactory2() {
			public IUpdatable createUpdatable(Map properties,
					Object description, IValidationContext validationContext)
					throws BindingException {
				if (description instanceof PropertyDescription) {
					PropertyDescription propertyDescription = (PropertyDescription) description;
					Object object = propertyDescription.getObject();
					if (object instanceof Person) {
						return new JavaBeanUpdatableValue(object,
								(String) propertyDescription.getPropertyID());
					}
				}
				return null;
			}
		};
		dbc.addUpdatableFactory2(emfValueFactory);

		return dbc;

	}

}
