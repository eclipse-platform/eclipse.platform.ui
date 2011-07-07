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
package org.eclipse.e4.ui.model.application.ui;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Core Expression</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.MCoreExpression#getCoreExpressionId <em>Core Expression Id</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.MCoreExpression#getCoreExpression <em>Core Expression</em>}</li>
 * </ul>
 * </p>
 *
 * @model
 * @generated
 */
public interface MCoreExpression extends MExpression {
	/**
	 * Returns the value of the '<em><b>Core Expression Id</b></em>' attribute.
	 * The default value is <code>""</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Core Expression Id</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Core Expression Id</em>' attribute.
	 * @see #setCoreExpressionId(String)
	 * @model default=""
	 * @generated
	 */
	String getCoreExpressionId();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.ui.MCoreExpression#getCoreExpressionId <em>Core Expression Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Core Expression Id</em>' attribute.
	 * @see #getCoreExpressionId()
	 * @generated
	 */
	void setCoreExpressionId(String value);

	/**
	 * Returns the value of the '<em><b>Core Expression</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Core Expression</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Core Expression</em>' attribute.
	 * @see #setCoreExpression(Object)
	 * @model transient="true"
	 * @generated
	 */
	Object getCoreExpression();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.ui.MCoreExpression#getCoreExpression <em>Core Expression</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Core Expression</em>' attribute.
	 * @see #getCoreExpression()
	 * @generated
	 */
	void setCoreExpression(Object value);

} // MCoreExpression
