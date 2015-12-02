/**
 * Copyright (c) 2008, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      IBM Corporation - initial API and implementation
 */
package org.eclipse.e4.ui.model.application.commands;

import java.util.List;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.ui.MLocalizable;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Command</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * <p>
 * A Command represents a logical operation within the applicaiton. The implementation
 * is provided by an MHandler chosen by examining all the candidate's enablement.
 * </p>
 * @since 1.0
 * @noimplement This interface is not intended to be implemented by clients.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.model.application.commands.MCommand#getCommandName <em>Command Name</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.commands.MCommand#getDescription <em>Description</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.commands.MCommand#getParameters <em>Parameters</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.commands.MCommand#getCategory <em>Category</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.commands.MCommand#getLocalizedCommandName <em>Localized Command Name</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.commands.MCommand#getLocalizedDescription <em>Localized Description</em>}</li>
 * </ul>
 *
 * @model
 * @generated
 */
public interface MCommand extends MApplicationElement, MLocalizable {
	/**
	 * Returns the value of the '<em><b>Command Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * <p>
	 * This field holds the command's name, used in the UI by default when there
	 * are menu or toolbar items representing this command.
	 * </p>
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Command Name</em>' attribute.
	 * @see #setCommandName(String)
	 * @model
	 * @generated
	 */
	String getCommandName();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.commands.MCommand#getCommandName <em>Command Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Command Name</em>' attribute.
	 * @see #getCommandName()
	 * @generated
	 */
	void setCommandName(String value);

	/**
	 * Returns the value of the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * <p>
	 * This field holds the command's description, used in the UI when the commands
	 * being shown in dialogs....
	 * </p>
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Description</em>' attribute.
	 * @see #setDescription(String)
	 * @model
	 * @generated
	 */
	String getDescription();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.commands.MCommand#getDescription <em>Description</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Description</em>' attribute.
	 * @see #getDescription()
	 * @generated
	 */
	void setDescription(String value);

	/**
	 * Returns the value of the '<em><b>Parameters</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.e4.ui.model.application.commands.MCommandParameter}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * <p>
	 * This list defines the ste of parameters that this command expects to have defined
	 * during execution.
	 * </p>
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Parameters</em>' containment reference list.
	 * @model containment="true"
	 * @generated
	 */
	List<MCommandParameter> getParameters();

	/**
	 * Returns the value of the '<em><b>Category</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * <p>
	 * <strong>Developers</strong>:
	 * Add more detailed documentation by editing this comment in 
	 * org.eclipse.ui.model.workbench/model/UIElements.ecore. 
	 * There is a GenModel/documentation node under each type and attribute.
	 * </p>
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Category</em>' reference.
	 * @see #setCategory(MCategory)
	 * @model
	 * @generated
	 */
	MCategory getCategory();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.commands.MCommand#getCategory <em>Category</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Category</em>' reference.
	 * @see #getCategory()
	 * @generated
	 */
	void setCategory(MCategory value);

	/**
	 * Returns the value of the '<em><b>Localized Command Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Localized Command Name</em>' attribute.
	 * @model transient="true" changeable="false" volatile="true" derived="true"
	 * @generated
	 */
	String getLocalizedCommandName();

	/**
	 * Returns the value of the '<em><b>Localized Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Localized Description</em>' attribute.
	 * @model transient="true" changeable="false" volatile="true" derived="true"
	 * @generated
	 */
	String getLocalizedDescription();

} // MCommand
