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

import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.ui.MLocalizable;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Category</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * <p>
 * This defines a logical grouping of Commands in order to facilitate showing 
 * the current set of Commands in dialogs, lists etc
 * </p>
 * @since 1.0
 * @noimplement This interface is not intended to be implemented by clients.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.model.application.commands.MCategory#getName <em>Name</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.commands.MCategory#getDescription <em>Description</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.commands.MCategory#getLocalizedName <em>Localized Name</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.commands.MCategory#getLocalizedDescription <em>Localized Description</em>}</li>
 * </ul>
 *
 * @model
 * @generated
 */
public interface MCategory extends MApplicationElement, MLocalizable {
	/**
	 * Returns the value of the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * <p>
	 * The name to be displayed for this category.
	 * </p>
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Name</em>' attribute.
	 * @see #setName(String)
	 * @model required="true"
	 * @generated
	 */
	String getName();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.commands.MCategory#getName <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Name</em>' attribute.
	 * @see #getName()
	 * @generated
	 */
	void setName(String value);

	/**
	 * Returns the value of the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * <p>
	 * The description to display for this category.
	 * </p>
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Description</em>' attribute.
	 * @see #setDescription(String)
	 * @model
	 * @generated
	 */
	String getDescription();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.commands.MCategory#getDescription <em>Description</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Description</em>' attribute.
	 * @see #getDescription()
	 * @generated
	 */
	void setDescription(String value);

	/**
	 * Returns the value of the '<em><b>Localized Name</b></em>' attribute.
	 * The default value is <code>""</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Localized Name</em>' attribute.
	 * @model default="" transient="true" changeable="false" volatile="true" derived="true"
	 * @generated
	 */
	String getLocalizedName();

	/**
	 * Returns the value of the '<em><b>Localized Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Localized Description</em>' attribute.
	 * @model transient="true" changeable="false" volatile="true" derived="true"
	 * @generated
	 */
	String getLocalizedDescription();

} // MCategory
