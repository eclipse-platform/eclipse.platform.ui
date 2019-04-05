/**
 * Copyright (c) 2008, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *      IBM Corporation - initial API and implementation
 */
package org.eclipse.e4.ui.model.application.commands;

/**
 * <!-- begin-user-doc -->
 * The <b>Factory</b> for the model.
 * It provides a create method for each non-abstract class of the model.
 * <!-- end-user-doc -->
 * @generated
 */
public interface MCommandsFactory {
	/**
	 * The singleton instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	MCommandsFactory INSTANCE = org.eclipse.e4.ui.model.application.commands.impl.CommandsFactoryImpl.eINSTANCE;

	/**
	 * Returns a new object of class '<em>Binding Context</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Binding Context</em>'.
	 * @since 1.0
	 * @generated
	 */
	MBindingContext createBindingContext();

	/**
	 * Returns a new object of class '<em>Binding Table</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Binding Table</em>'.
	 * @since 1.0
	 * @generated
	 */
	MBindingTable createBindingTable();

	/**
	 * Returns a new object of class '<em>Command</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Command</em>'.
	 * @since 1.0
	 * @generated
	 */
	MCommand createCommand();

	/**
	 * Returns a new object of class '<em>Command Parameter</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Command Parameter</em>'.
	 * @since 1.0
	 * @generated
	 */
	MCommandParameter createCommandParameter();

	/**
	 * Returns a new object of class '<em>Handler</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Handler</em>'.
	 * @since 1.0
	 * @generated
	 */
	MHandler createHandler();

	/**
	 * Returns a new object of class '<em>Key Binding</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Key Binding</em>'.
	 * @since 1.0
	 * @generated
	 */
	MKeyBinding createKeyBinding();

	/**
	 * Returns a new object of class '<em>Parameter</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Parameter</em>'.
	 * @since 1.0
	 * @generated
	 */
	MParameter createParameter();

	/**
	 * Returns a new object of class '<em>Category</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Category</em>'.
	 * @since 1.0
	 * @generated
	 */
	MCategory createCategory();

} //MCommandsFactory
