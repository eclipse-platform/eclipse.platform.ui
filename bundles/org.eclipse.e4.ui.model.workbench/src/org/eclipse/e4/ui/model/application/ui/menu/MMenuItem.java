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
package org.eclipse.e4.ui.model.application.ui.menu;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Item</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.menu.MMenuItem#getMnemonics <em>Mnemonics</em>}</li>
 * </ul>
 * </p>
 *
 * @model
 * @generated
 */
public interface MMenuItem extends MItem, MMenuElement {
	/**
	 * Returns the value of the '<em><b>Mnemonics</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Mnemonics</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Mnemonics</em>' attribute.
	 * @see #setMnemonics(String)
	 * @model
	 * @generated
	 */
	String getMnemonics();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.ui.menu.MMenuItem#getMnemonics <em>Mnemonics</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Mnemonics</em>' attribute.
	 * @see #getMnemonics()
	 * @generated
	 */
	void setMnemonics(String value);

} // MMenuItem
