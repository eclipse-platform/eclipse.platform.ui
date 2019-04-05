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
package org.eclipse.e4.ui.model.application.ui.menu;

/**
 * <!-- begin-user-doc -->
 * The <b>Factory</b> for the model.
 * It provides a create method for each non-abstract class of the model.
 * <!-- end-user-doc -->
 * @generated
 */
public interface MMenuFactory {
	/**
	 * The singleton instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	MMenuFactory INSTANCE = org.eclipse.e4.ui.model.application.ui.menu.impl.MenuFactoryImpl.eINSTANCE;

	/**
	 * Returns a new object of class '<em>Separator</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Separator</em>'.
	 * @since 1.0
	 * @generated
	 */
	MMenuSeparator createMenuSeparator();

	/**
	 * Returns a new object of class '<em>Menu</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Menu</em>'.
	 * @since 1.0
	 * @generated
	 */
	MMenu createMenu();

	/**
	 * Returns a new object of class '<em>Contribution</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Contribution</em>'.
	 * @since 1.0
	 * @noreference See {@link MMenuContribution model documentation} for details.
	 * @generated
	 */
	MMenuContribution createMenuContribution();

	/**
	 * Returns a new object of class '<em>Popup Menu</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Popup Menu</em>'.
	 * @since 1.0
	 * @generated
	 */
	MPopupMenu createPopupMenu();

	/**
	 * Returns a new object of class '<em>Direct Menu Item</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Direct Menu Item</em>'.
	 * @since 1.0
	 * @generated
	 */
	MDirectMenuItem createDirectMenuItem();

	/**
	 * Returns a new object of class '<em>Handled Menu Item</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Handled Menu Item</em>'.
	 * @since 1.0
	 * @generated
	 */
	MHandledMenuItem createHandledMenuItem();

	/**
	 * Returns a new object of class '<em>Tool Bar</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Tool Bar</em>'.
	 * @since 1.0
	 * @generated
	 */
	MToolBar createToolBar();

	/**
	 * Returns a new object of class '<em>Tool Control</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Tool Control</em>'.
	 * @since 1.0
	 * @generated
	 */
	MToolControl createToolControl();

	/**
	 * Returns a new object of class '<em>Handled Tool Item</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Handled Tool Item</em>'.
	 * @since 1.0
	 * @generated
	 */
	MHandledToolItem createHandledToolItem();

	/**
	 * Returns a new object of class '<em>Direct Tool Item</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Direct Tool Item</em>'.
	 * @since 1.0
	 * @generated
	 */
	MDirectToolItem createDirectToolItem();

	/**
	 * Returns a new object of class '<em>Tool Bar Separator</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Tool Bar Separator</em>'.
	 * @since 1.0
	 * @generated
	 */
	MToolBarSeparator createToolBarSeparator();

	/**
	 * Returns a new object of class '<em>Tool Bar Contribution</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Tool Bar Contribution</em>'.
	 * @since 1.0
	 * @noreference See {@link MToolBarContribution model documentation} for details.
	 * @generated
	 */
	MToolBarContribution createToolBarContribution();

	/**
	 * Returns a new object of class '<em>Trim Contribution</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Trim Contribution</em>'.
	 * @since 1.0
	 * @noreference See {@link MTrimContribution model documentation} for details.
	 * @generated
	 */
	MTrimContribution createTrimContribution();

	/**
	 * Returns a new object of class '<em>Dynamic Menu Contribution</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Dynamic Menu Contribution</em>'.
	 * @since 1.0
	 * @generated
	 */
	MDynamicMenuContribution createDynamicMenuContribution();

} //MMenuFactory
