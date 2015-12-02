/**
 * Copyright (c) 2010, 2015 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *      IBM Corporation - initial API and implementation
 *      Steven Spungin <steven@spungin.tv> - Bug 437958
 */
package org.eclipse.e4.ui.model.fragment;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>String Model Fragment</b></em>'.
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
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.model.fragment.MStringModelFragment#getFeaturename <em>Featurename</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.fragment.MStringModelFragment#getParentElementId <em>Parent Element Id</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.fragment.MStringModelFragment#getPositionInList <em>Position In List</em>}</li>
 * </ul>
 *
 * @model
 * @generated
 */
public interface MStringModelFragment extends MModelFragment {
	/**
	 * Returns the value of the '<em><b>Featurename</b></em>' attribute.
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
	 * @return the value of the '<em>Featurename</em>' attribute.
	 * @see #setFeaturename(String)
	 * @model required="true"
	 * @generated
	 */
	String getFeaturename();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.fragment.MStringModelFragment#getFeaturename <em>Featurename</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Featurename</em>' attribute.
	 * @see #getFeaturename()
	 * @generated
	 */
	void setFeaturename(String value);

	/**
	 * Returns the value of the '<em><b>Parent Element Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * The parentElementId specifies the targeted parent or parents when a fragment is merged.  The ID can be specified in 3 ways: <br />
	 * <ol>
	 * <li> elementId (a single elementId)
	 * <li> elementId1,elementId2,elementId3 (a comma delimited list of elementIds)
	 * <li> xpath:[xpath goes here] (an xpath expression)
	 * </ol>
	 * <br />
	 * An xpath example targeting 2 destinations in the model <br />
	 * <pre>
	 * xpath://*[@elementId='app.menu.primary' or @elementId='app.menu.secondary']
	 * </pre>
	 * @return The parentElementId expression.  Must not be null, but may be an empty string if the fragment did not specify a target.
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * <p>
	 * <strong>Developers</strong>:
	 * Add more detailed documentation by editing this comment in 
	 * /org.eclipse.e4.ui.model.workbench/model/ModelFragment.ecore. 
	 * There is a GenModel/documentation node under each type and attribute.
	 * </p>
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Parent Element Id</em>' attribute.
	 * @see #setParentElementId(String)
	 * @model required="true"
	 * @generated
	 */
	String getParentElementId();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.fragment.MStringModelFragment#getParentElementId <em>Parent Element Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * @see setParentElementId
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Parent Element Id</em>' attribute.
	 * @see #getParentElementId()
	 * @generated
	 */
	void setParentElementId(String value);

	/**
	 * Returns the value of the '<em><b>Position In List</b></em>' attribute.
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
	 * @return the value of the '<em>Position In List</em>' attribute.
	 * @see #setPositionInList(String)
	 * @model
	 * @generated
	 */
	String getPositionInList();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.fragment.MStringModelFragment#getPositionInList <em>Position In List</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Position In List</em>' attribute.
	 * @see #getPositionInList()
	 * @generated
	 */
	void setPositionInList(String value);

} // MStringModelFragment
