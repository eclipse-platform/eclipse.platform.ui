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

import org.eclipse.e4.core.contexts.IEclipseContext;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.EMap;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Context</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.model.application.MContext#getContext <em>Context</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.MContext#getVariables <em>Variables</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.MContext#getProperties <em>Properties</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.eclipse.e4.ui.model.application.MApplicationPackage#getContext()
 * @model abstract="true"
 * @generated
 */
public interface MContext {
	/**
	 * Returns the value of the '<em><b>Context</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Context</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Context</em>' attribute.
	 * @see #setContext(IEclipseContext)
	 * @see org.eclipse.e4.ui.model.application.MApplicationPackage#getContext_Context()
	 * @model dataType="org.eclipse.e4.ui.model.application.IEclipseContext" transient="true"
	 * @generated
	 */
	IEclipseContext getContext();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.MContext#getContext <em>Context</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Context</em>' attribute.
	 * @see #getContext()
	 * @generated
	 */
	void setContext(IEclipseContext value);

	/**
	 * Returns the value of the '<em><b>Variables</b></em>' attribute list.
	 * The list contents are of type {@link java.lang.String}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Variables</em>' attribute list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Variables</em>' attribute list.
	 * @see org.eclipse.e4.ui.model.application.MApplicationPackage#getContext_Variables()
	 * @model ordered="false"
	 * @generated
	 */
	EList<String> getVariables();

	/**
	 * Returns the value of the '<em><b>Properties</b></em>' map.
	 * The key is of type {@link java.lang.String},
	 * and the value is of type {@link java.lang.String},
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Properties</em>' map isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Properties</em>' map.
	 * @see org.eclipse.e4.ui.model.application.MApplicationPackage#getContext_Properties()
	 * @model mapType="org.eclipse.e4.ui.model.application.StringToStringMap<org.eclipse.emf.ecore.EString, org.eclipse.emf.ecore.EString>"
	 * @generated
	 */
	EMap<String, String> getProperties();

} // MContext
