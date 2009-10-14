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

import org.eclipse.emf.common.util.EList;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Command</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.model.application.MCommand#getCommandURI <em>Command URI</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.MCommand#getImpl <em>Impl</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.MCommand#getArgs <em>Args</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.MCommand#getCommandName <em>Command Name</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.eclipse.e4.ui.model.application.MApplicationPackage#getCommand()
 * @model
 * @generated
 */
public interface MCommand extends MApplicationElement {
	/**
	 * Returns the value of the '<em><b>Command URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Command URI</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Command URI</em>' attribute.
	 * @see #setCommandURI(String)
	 * @see org.eclipse.e4.ui.model.application.MApplicationPackage#getCommand_CommandURI()
	 * @model
	 * @generated
	 */
	String getCommandURI();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.MCommand#getCommandURI <em>Command URI</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Command URI</em>' attribute.
	 * @see #getCommandURI()
	 * @generated
	 */
	void setCommandURI(String value);

	/**
	 * Returns the value of the '<em><b>Impl</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Impl</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Impl</em>' attribute.
	 * @see #setImpl(Object)
	 * @see org.eclipse.e4.ui.model.application.MApplicationPackage#getCommand_Impl()
	 * @model transient="true"
	 * @generated
	 */
	Object getImpl();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.MCommand#getImpl <em>Impl</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Impl</em>' attribute.
	 * @see #getImpl()
	 * @generated
	 */
	void setImpl(Object value);

	/**
	 * Returns the value of the '<em><b>Args</b></em>' attribute list.
	 * The list contents are of type {@link java.lang.String}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Args</em>' attribute list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Args</em>' attribute list.
	 * @see org.eclipse.e4.ui.model.application.MApplicationPackage#getCommand_Args()
	 * @model
	 * @generated
	 */
	EList<String> getArgs();

	/**
	 * Returns the value of the '<em><b>Command Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Command Name</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Command Name</em>' attribute.
	 * @see #setCommandName(String)
	 * @see org.eclipse.e4.ui.model.application.MApplicationPackage#getCommand_CommandName()
	 * @model
	 * @generated
	 */
	String getCommandName();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.MCommand#getCommandName <em>Command Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Command Name</em>' attribute.
	 * @see #getCommandName()
	 * @generated
	 */
	void setCommandName(String value);

} // MCommand
