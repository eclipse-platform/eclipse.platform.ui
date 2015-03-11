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

import java.util.List;
import java.util.Map;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Element</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * <p>
 * This is the root element for all UI Model elements, defining attribtues common
 * to every element; the element's id as well as three general storage elements:
 * <ul>
 * <li>Tags: This is a set of strings which can be used to stereotype a particular
 * element. Tags may be specified in element searches and can also be referred
 * to in the CSS styling definition.</li>
 * <li>PersistedState: A string to string map used to store information that nneds
 * to be persisted between sessions.</li>
 * <li>TransientData: A string to object map which can be used to store runtime data
 * relevant to a particular model element.</li>
 * </ul>
 * </p>
 * @since 1.0
 * @noimplement This interface is not intended to be implemented by clients.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.model.application.MApplicationElement#getElementId <em>Element Id</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.MApplicationElement#getPersistedState <em>Persisted State</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.MApplicationElement#getTags <em>Tags</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.MApplicationElement#getContributorURI <em>Contributor URI</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.MApplicationElement#getTransientData <em>Transient Data</em>}</li>
 * </ul>
 * </p>
 *
 * @model abstract="true"
 * @generated
 */
public interface MApplicationElement {
	/**
	 * Returns the value of the '<em><b>Element Id</b></em>' attribute.
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
	 * @return the value of the '<em>Element Id</em>' attribute.
	 * @see #setElementId(String)
	 * @model
	 * @generated
	 */
	String getElementId();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.MApplicationElement#getElementId <em>Element Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Element Id</em>' attribute.
	 * @see #getElementId()
	 * @generated
	 */
	void setElementId(String value);

	/**
	 * Returns the value of the '<em><b>Persisted State</b></em>' map.
	 * The key is of type {@link java.lang.String},
	 * and the value is of type {@link java.lang.String},
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * <p>
	 * This is a Strimg to String map that can be used to persist information avout model
	 * elements across program sessions. The format of the 'value' string is defined by
	 * the code setting the value into the map. Information stored in this map is part of
	 * the model and will be persisted and restored as such.
	 * </p>
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Persisted State</em>' map.
	 * @model mapType="org.eclipse.e4.ui.model.application.StringToStringMap<org.eclipse.emf.ecore.EString, org.eclipse.emf.ecore.EString>"
	 * @generated
	 */
	Map<String, String> getPersistedState();

	/**
	 * Returns the value of the '<em><b>Tags</b></em>' attribute list.
	 * The list contents are of type {@link java.lang.String}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * <p>
	 * Tags are a list of Strings that are persistent parts of the UI Model. They can be used to 'refine' a particular
	 * model element, supplying extra 'meta' information. These tags interact with the CSS engine so that it's
	 * possible to write CSS specific to a particular tag. The platform currently uses this mechanism to cause the
	 * color change in the stack comtaining the currently active part
	 * </p>
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Tags</em>' attribute list.
	 * @model
	 * @generated
	 */
	List<String> getTags();

	/**
	 * Returns the value of the '<em><b>Contributor URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * <p>
	 * This field is used to track the bundle (if any) from which the UI element was
	 * derived in order to faciliate its removal should the bundle go away or be updated.
	 * </p>
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Contributor URI</em>' attribute.
	 * @see #setContributorURI(String)
	 * @model
	 * @generated
	 */
	String getContributorURI();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.MApplicationElement#getContributorURI <em>Contributor URI</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Contributor URI</em>' attribute.
	 * @see #getContributorURI()
	 * @generated
	 */
	void setContributorURI(String value);

	/**
	 * Returns the value of the '<em><b>Transient Data</b></em>' map.
	 * The key is of type {@link java.lang.String},
	 * and the value is of type {@link java.lang.Object},
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * <p>
	 * This is a String to Object map into which any desired runtime information realted to a particular element
	 * may be stored. It is <i>not</i> persisted across sessions so it is not necessary that the 'values' be
	 * serializable.
	 * </p>
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Transient Data</em>' map.
	 * @model mapType="org.eclipse.e4.ui.model.application.StringToObjectMap<org.eclipse.emf.ecore.EString, org.eclipse.emf.ecore.EJavaObject>" transient="true"
	 * @generated
	 */
	Map<String, Object> getTransientData();

} // MApplicationElement
