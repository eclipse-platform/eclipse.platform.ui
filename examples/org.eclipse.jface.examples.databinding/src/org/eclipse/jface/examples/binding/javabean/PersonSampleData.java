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
/*
 *  $RCSfile: PersonSampleData.java,v $
 *  $Revision: 1.3 $  $Date: 2005/10/18 17:38:36 $ 
 */
package org.eclipse.jface.examples.binding.javabean;

import org.eclipse.jface.binding.DatabindingService;
import org.eclipse.jface.binding.IUpdatable;
import org.eclipse.jface.binding.IUpdatableFactory;
import org.eclipse.jface.binding.swt.SWTDatabindingService;
import org.eclipse.swt.SWT;
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
	public static DatabindingService getSWTtoJavaBeanDatabindingService(
			Control aControl) {

		DatabindingService dbs = new SWTDatabindingService(aControl,
				SWT.FocusOut, SWT.FocusOut);

		IUpdatableFactory emfValueFactory = new IUpdatableFactory() {
			public IUpdatable createUpdatable(Object object, Object attribute) {
				return new JavaBeanUpdatableValue(object, (String) attribute);
			}
		};
		dbs.addUpdatableFactory(Person.class, emfValueFactory);

		return dbs;

	}

}
