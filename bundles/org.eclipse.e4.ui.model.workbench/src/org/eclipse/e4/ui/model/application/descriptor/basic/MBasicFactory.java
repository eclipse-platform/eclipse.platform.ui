/**
 * Copyright (c) 2008, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      IBM Corporation - initial API and implementation
 */
package org.eclipse.e4.ui.model.application.descriptor.basic;


/**
 * <!-- begin-user-doc -->
 * The <b>Factory</b> for the model.
 * It provides a create method for each non-abstract class of the model.
 * @since 1.0
 * <!-- end-user-doc -->
 * @generated
 */
public interface MBasicFactory {
	/**
	 * The singleton instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	MBasicFactory INSTANCE = org.eclipse.e4.ui.model.application.descriptor.basic.impl.BasicFactoryImpl.eINSTANCE;

	/**
	 * Returns a new object of class '<em>Part Descriptor</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Part Descriptor</em>'.
	 * @generated
	 */
	MPartDescriptor createPartDescriptor();

} //MBasicFactory
