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

import java.util.List;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.commands.MParameter;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Handled Item</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * <p>
 * This is the base class for menu and tool items associated with Commands.
 * </p>
 * @since 1.0
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.menu.MHandledItem#getCommand <em>Command</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.menu.MHandledItem#getWbCommand <em>Wb Command</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.menu.MHandledItem#getParameters <em>Parameters</em>}</li>
 * </ul>
 * </p>
 *
 * @model abstract="true"
 * @generated
 */
public interface MHandledItem extends MItem {
	/**
	 * Returns the value of the '<em><b>Command</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * <p>
	 * A reference to the Command associated with this item.
	 * </p>
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Command</em>' reference.
	 * @see #setCommand(MCommand)
	 * @model
	 * @generated
	 */
	MCommand getCommand();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.ui.menu.MHandledItem#getCommand <em>Command</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Command</em>' reference.
	 * @see #getCommand()
	 * @generated
	 */
	void setCommand(MCommand value);

	/**
	 * Returns the value of the '<em><b>Wb Command</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * <p>
	 * This is used for low level implementation and is not intended to be used by clients
	 * </p>
	 * @noreference
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Wb Command</em>' attribute.
	 * @see #setWbCommand(ParameterizedCommand)
	 * @model dataType="org.eclipse.e4.ui.model.application.commands.ParameterizedCommand" transient="true"
	 * @generated
	 */
	ParameterizedCommand getWbCommand();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.ui.menu.MHandledItem#getWbCommand <em>Wb Command</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Wb Command</em>' attribute.
	 * @see #getWbCommand()
	 * @generated
	 */
	void setWbCommand(ParameterizedCommand value);

	/**
	 * Returns the value of the '<em><b>Parameters</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.e4.ui.model.application.commands.MParameter}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * <p>
	 * Defines the specific parameters to use when executing the command through this item.
	 * </p>
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Parameters</em>' containment reference list.
	 * @model containment="true"
	 * @generated
	 */
	List<MParameter> getParameters();

} // MHandledItem
