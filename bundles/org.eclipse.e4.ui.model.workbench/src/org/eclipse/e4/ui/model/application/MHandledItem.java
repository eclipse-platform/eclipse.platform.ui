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
 * $Id: MHandledItem.java,v 1.1 2009/02/03 14:25:34 emoffatt Exp $
 */
package org.eclipse.e4.ui.model.application;

import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.emf.common.util.EList;


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
 *   <li>{@link org.eclipse.e4.ui.model.application.MHandledItem#getWbCommand <em>Wb Command</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.MHandledItem#getParameters <em>Parameters</em>}</li>
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

	/**
	 * Returns the value of the '<em><b>Wb Command</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Wb Command</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Wb Command</em>' attribute.
	 * @see #setWbCommand(ParameterizedCommand)
	 * @see org.eclipse.e4.ui.model.application.ApplicationPackage#getMHandledItem_WbCommand()
	 * @model dataType="org.eclipse.e4.ui.model.application.ParameterizedCommand" transient="true"
	 * @generated
	 */
	ParameterizedCommand getWbCommand();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.MHandledItem#getWbCommand <em>Wb Command</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Wb Command</em>' attribute.
	 * @see #getWbCommand()
	 * @generated
	 */
	void setWbCommand(ParameterizedCommand value);

	/**
	 * Returns the value of the '<em><b>Parameters</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.e4.ui.model.application.MParameter}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Parameters</em>' containment reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Parameters</em>' containment reference list.
	 * @see org.eclipse.e4.ui.model.application.ApplicationPackage#getMHandledItem_Parameters()
	 * @model containment="true"
	 * @generated
	 */
	EList<MParameter> getParameters();

} // MHandledItem
