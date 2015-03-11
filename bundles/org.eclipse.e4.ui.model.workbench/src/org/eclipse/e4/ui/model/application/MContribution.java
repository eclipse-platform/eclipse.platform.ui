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
package org.eclipse.e4.ui.model.application;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Contribution</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * <p>
 * MContribution is a mix-in class used by concrete elements such as Parts to define
 * the location of the client supplied class implementing the specific logic needed.
 * </p>
 * @since 1.0
 * @noimplement This interface is not intended to be implemented by clients.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.model.application.MContribution#getContributionURI <em>Contribution URI</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.MContribution#getObject <em>Object</em>}</li>
 * </ul>
 * </p>
 *
 * @model abstract="true"
 * @generated
 */
public interface MContribution extends MApplicationElement {
	/**
	 * Returns the value of the '<em><b>Contribution URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * <p>
	 * The ContributionURI defines the complete path to a class implementing the logic
	 * for elements require external code to handle the UI such as MParts and MHandlers.
	 * </p>
	 * @since 1.0
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Contribution URI</em>' attribute.
	 * @see #setContributionURI(String)
	 * @model
	 * @generated
	 */
	String getContributionURI();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.MContribution#getContributionURI <em>Contribution URI</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Contribution URI</em>' attribute.
	 * @see #getContributionURI()
	 * @generated
	 */
	void setContributionURI(String value);

	/**
	 * Returns the value of the '<em><b>Object</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * <p>
	 * This is the DI created instance of the class implementing the logic for the element.
	 * It will only be non-null if the element has been rendered into the presentation.
	 * </p>
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Object</em>' attribute.
	 * @see #setObject(Object)
	 * @model transient="true" derived="true"
	 * @generated
	 */
	Object getObject();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.MContribution#getObject <em>Object</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Object</em>' attribute.
	 * @see #getObject()
	 * @generated
	 */
	void setObject(Object value);

} // MContribution
