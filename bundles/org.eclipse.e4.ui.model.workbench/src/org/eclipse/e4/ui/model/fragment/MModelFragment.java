/**
 * Copyright (c) 2010, 2013 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *      IBM Corporation - initial API and implementation
 */
package org.eclipse.e4.ui.model.fragment;

import java.util.List;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationElement;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Model Fragment</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * <p>
 * <strong>Developers</strong>:
 * Add more detailed documentation by editing this comment in
 * /org.eclipse.e4.ui.model.workbench/model/ModelFragment.ecore.
 * There is a GenModel/documentation node under each type and attribute.
 * </p>
 * @since 1.0
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.model.fragment.MModelFragment#getElements <em>Elements</em>}</li>
 * </ul>
 * </p>
 *
 * @model abstract="true"
 * @generated
 */
public interface MModelFragment {
	/**
	 * Returns the value of the '<em><b>Elements</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.e4.ui.model.application.MApplicationElement}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * <p>
	 * <strong>Developers</strong>:
	 * Add more detailed documentation by editing this comment in
	 * /org.eclipse.e4.ui.model.workbench/model/ModelFragment.ecore.
	 * There is a GenModel/documentation node under each type and attribute.
	 * </p>
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Elements</em>' containment reference list.
	 * @model containment="true"
	 * @generated
	 */
	List<MApplicationElement> getElements();

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * <p>
	 * <strong>Developers</strong>:
	 * Add more detailed documentation by editing this comment in
	 * /org.eclipse.e4.ui.model.workbench/model/ModelFragment.ecore.
	 * There is a GenModel/documentation node under each type and attribute.
	 * </p>
	 * <!-- end-model-doc -->
	 * @model
	 * @generated
	 */
	List<MApplicationElement> merge(MApplication application);

} // MModelFragment
