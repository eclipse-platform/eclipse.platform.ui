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
package org.eclipse.e4.ui.model.application.ui.menu;

import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.MUILabel;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Element</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * <p>
 * This is the bsae type for both menu items and Separators.
 * </p>
 * @since 1.0
 * @noimplement This interface is not intended to be implemented by clients.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.menu.MMenuElement#getMnemonics <em>Mnemonics</em>}</li>
 * </ul>
 *
 * @model abstract="true"
 * @generated
 */
public interface MMenuElement extends MUIElement, MUILabel {

	/**
	 * Returns the value of the '<em><b>Mnemonics</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * <p>
	 * This is the character that is interpreted by the platform to allow for easier navigation
	 * through menus.
	 * </p>
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Mnemonics</em>' attribute.
	 * @see #setMnemonics(String)
	 * @model
	 * @generated
	 */
	String getMnemonics();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.ui.menu.MMenuElement#getMnemonics <em>Mnemonics</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Mnemonics</em>' attribute.
	 * @see #getMnemonics()
	 * @generated
	 */
	void setMnemonics(String value);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * <p>
	 * This is a method that will return the translated mnemonic for this element.
	 * </p>
	 * <!-- end-model-doc -->
	 * @model kind="operation"
	 * @generated
	 */
	String getLocalizedMnemonics();
} // MMenuElement
