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
package org.eclipse.e4.ui.model.application.ui.menu;

import java.util.List;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Trim Contributions</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * <p>
 * Provisional for 4.3. This is a container aggregating the trim bar contributions to be
 * applied.
 * </p>
 * @noreference This interface is not intended to be referenced by clients.
 * @since 1.0
 * @noimplement This interface is not intended to be implemented by clients.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.menu.MTrimContributions#getTrimContributions <em>Trim Contributions</em>}</li>
 * </ul>
 *
 * @model interface="true" abstract="true"
 * @generated
 */
public interface MTrimContributions {

	/**
	 * Returns the value of the '<em><b>Trim Contributions</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.e4.ui.model.application.ui.menu.MTrimContribution}.
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
	 * @return the value of the '<em>Trim Contributions</em>' containment reference list.
	 * @model containment="true"
	 * @generated
	 */
	List<MTrimContribution> getTrimContributions();
} // MTrimContributions
