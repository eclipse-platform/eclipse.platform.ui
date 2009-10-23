/**
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *      IBM Corporation - initial API and implementation
 */
package org.eclipse.e4.ui.model.application;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>IDE Window</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.model.application.MIDEWindow#getMainMenu <em>Main Menu</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.eclipse.e4.ui.model.application.MApplicationPackage#getIDEWindow()
 * @model
 * @generated
 */
public interface MIDEWindow extends MTrimStructure<MVSCElement>, MUIItem, MContext, MHandlerContainer {
	/**
	 * Returns the value of the '<em><b>Main Menu</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Main Menu</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Main Menu</em>' reference.
	 * @see #setMainMenu(MMenu)
	 * @see org.eclipse.e4.ui.model.application.MApplicationPackage#getIDEWindow_MainMenu()
	 * @model
	 * @generated
	 */
	MMenu getMainMenu();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.MIDEWindow#getMainMenu <em>Main Menu</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Main Menu</em>' reference.
	 * @see #getMainMenu()
	 * @generated
	 */
	void setMainMenu(MMenu value);

} // MIDEWindow
