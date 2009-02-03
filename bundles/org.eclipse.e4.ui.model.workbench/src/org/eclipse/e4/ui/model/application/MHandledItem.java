/**
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *      IBM Corporation - initial API and implementation
 *
 * $Id$
 */
package org.eclipse.e4.ui.model.application;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>MHandled Item</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.model.application.MHandledItem#getCommand <em>Command</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.MHandledItem#getMenu <em>Menu</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.eclipse.e4.ui.model.application.ApplicationPackage#getMHandledItem()
 * @model
 * @generated
 */
public interface MHandledItem extends MItem {
	/**
	 * Returns the value of the '<em><b>Command</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Command</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Command</em>' reference.
	 * @see #setCommand(MCommand)
	 * @see org.eclipse.e4.ui.model.application.ApplicationPackage#getMHandledItem_Command()
	 * @model
	 * @generated
	 */
	MCommand getCommand();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.MHandledItem#getCommand <em>Command</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Command</em>' reference.
	 * @see #getCommand()
	 * @generated
	 */
	void setCommand(MCommand value);

	/**
	 * Returns the value of the '<em><b>Menu</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Menu</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Menu</em>' containment reference.
	 * @see #setMenu(MMenu)
	 * @see org.eclipse.e4.ui.model.application.ApplicationPackage#getMHandledItem_Menu()
	 * @model containment="true"
	 * @generated
	 */
	MMenu getMenu();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.MHandledItem#getMenu <em>Menu</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Menu</em>' containment reference.
	 * @see #getMenu()
	 * @generated
	 */
	void setMenu(MMenu value);

} // MHandledItem
