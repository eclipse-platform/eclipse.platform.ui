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
package org.eclipse.jface.examples.binding.javabean;

import org.eclipse.jface.binding.DatabindingContext;
import org.eclipse.jface.binding.IUpdatable;
import org.eclipse.jface.binding.IUpdatableFactory;
import org.eclipse.jface.binding.swt.SWTDatabindingContext;
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

		IUpdatableFactory emfValueFactory = new IUpdatableFactory() {
			public IUpdatable createUpdatable(Object object, Object attribute) {
				return new JavaBeanUpdatableValue(object, (String) attribute);
			}
		};
		dbc.addUpdatableFactory(Person.class, emfValueFactory);

		return dbc;

	}

}
