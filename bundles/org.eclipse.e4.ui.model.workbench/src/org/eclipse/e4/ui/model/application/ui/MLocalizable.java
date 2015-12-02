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
package org.eclipse.e4.ui.model.application.ui;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Localizable</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * <p>
 * This class should be mixed into any UI element that should participate in the
 * Locale update handling.
 * </p>
 * @since 1.1
 * <!-- end-model-doc -->
 *
 *
 * @model interface="true" abstract="true"
 * @generated
 */
public interface MLocalizable {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * <p>
	 * This method is used to support Locale changes at runtime.
	 * Implementing this method should result in refreshing localizable properties
	 * like labels, tooltips and descriptions.
	 * </p>
	 * <!-- end-model-doc -->
	 * @model
	 * @generated
	 */
	void updateLocalization();

} // MLocalizable
