/**
 * Copyright (c) 2012 IBM Corporation and BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *      IBM Corporation - initial API and implementation
 *      Tom Schindl - initial API and implementation
 */
package org.eclipse.e4.demo.split.model.split;


/**
 * <!-- begin-user-doc -->
 * The <b>Factory</b> for the model.
 * It provides a create method for each non-abstract class of the model.
 * <!-- end-user-doc -->
 * @generated
 */
public interface MSplitFactory {
	/**
	 * The singleton instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	MSplitFactory INSTANCE = org.eclipse.e4.demo.split.model.split.impl.SplitFactoryImpl.eINSTANCE;

	/**
	 * Returns a new object of class '<em>Stack Sash Container</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Stack Sash Container</em>'.
	 * @generated
	 */
	MStackSashContainer createStackSashContainer();

} //MSplitFactory
