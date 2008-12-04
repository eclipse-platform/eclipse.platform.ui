/**
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *      IBM Corporation - initial API and implementation
 *
 * $Id: Part.java,v 1.1 2008/11/11 18:19:12 bbokowski Exp $
 */
package org.eclipse.e4.ui.model.application;

import org.eclipse.emf.common.util.EList;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Part</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.model.application.Part#getMenu <em>Menu</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.Part#getToolBar <em>Tool Bar</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.Part#getPolicy <em>Policy</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.Part#getChildren <em>Children</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.Part#getActiveChild <em>Active Child</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.Part#getHandlers <em>Handlers</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.Part#getTrim <em>Trim</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.Part#getWidget <em>Widget</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.Part#getParent <em>Parent</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.Part#isVisible <em>Visible</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.eclipse.e4.ui.model.application.ApplicationPackage#getPart()
 * @model
 * @generated
 */
public interface Part<P extends Part<?>> extends ApplicationElement {
	/**
	 * Returns the value of the '<em><b>Policy</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The policy would define how the parent displays the children (stack/sashforms)
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Policy</em>' attribute.
	 * @see #setPolicy(String)
	 * @see org.eclipse.e4.ui.model.application.ApplicationPackage#getPart_Policy()
	 * @model
	 * @generated
	 */
	String getPolicy();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.Part#getPolicy <em>Policy</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Policy</em>' attribute.
	 * @see #getPolicy()
	 * @generated
	 */
	void setPolicy(String value);

	/**
	 * Returns the value of the '<em><b>Children</b></em>' containment reference list.
	 * The list contents are of type {@link P}.
	 * It is bidirectional and its opposite is '{@link org.eclipse.e4.ui.model.application.Part#getParent <em>Parent</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Children</em>' containment reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Children</em>' containment reference list.
	 * @see org.eclipse.e4.ui.model.application.ApplicationPackage#getPart_Children()
	 * @see org.eclipse.e4.ui.model.application.Part#getParent
	 * @model opposite="parent" containment="true"
	 * @generated
	 */
	EList<P> getChildren();

	/**
	 * Returns the value of the '<em><b>Active Child</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Active Child</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Active Child</em>' reference.
	 * @see #setActiveChild(Part)
	 * @see org.eclipse.e4.ui.model.application.ApplicationPackage#getPart_ActiveChild()
	 * @model
	 * @generated
	 */
	P getActiveChild();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.Part#getActiveChild <em>Active Child</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Active Child</em>' reference.
	 * @see #getActiveChild()
	 * @generated
	 */
	void setActiveChild(P value);

	/**
	 * Returns the value of the '<em><b>Handlers</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.e4.ui.model.application.Handler}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Handlers</em>' containment reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Handlers</em>' containment reference list.
	 * @see org.eclipse.e4.ui.model.application.ApplicationPackage#getPart_Handlers()
	 * @model containment="true"
	 * @generated
	 */
	EList<Handler> getHandlers();

	/**
	 * Returns the value of the '<em><b>Menu</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Menu</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Menu</em>' containment reference.
	 * @see #setMenu(Menu)
	 * @see org.eclipse.e4.ui.model.application.ApplicationPackage#getPart_Menu()
	 * @model containment="true"
	 * @generated
	 */
	Menu getMenu();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.Part#getMenu <em>Menu</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Menu</em>' containment reference.
	 * @see #getMenu()
	 * @generated
	 */
	void setMenu(Menu value);

	/**
	 * Returns the value of the '<em><b>Tool Bar</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Tool Bar</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Tool Bar</em>' containment reference.
	 * @see #setToolBar(ToolBar)
	 * @see org.eclipse.e4.ui.model.application.ApplicationPackage#getPart_ToolBar()
	 * @model containment="true"
	 * @generated
	 */
	ToolBar getToolBar();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.Part#getToolBar <em>Tool Bar</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Tool Bar</em>' containment reference.
	 * @see #getToolBar()
	 * @generated
	 */
	void setToolBar(ToolBar value);

	/**
	 * Returns the value of the '<em><b>Trim</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Trim</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Trim</em>' containment reference.
	 * @see #setTrim(Trim)
	 * @see org.eclipse.e4.ui.model.application.ApplicationPackage#getPart_Trim()
	 * @model containment="true"
	 * @generated
	 */
	Trim getTrim();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.Part#getTrim <em>Trim</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Trim</em>' containment reference.
	 * @see #getTrim()
	 * @generated
	 */
	void setTrim(Trim value);

	/**
	 * Returns the value of the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Widget</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Widget</em>' attribute.
	 * @see #setWidget(Object)
	 * @see org.eclipse.e4.ui.model.application.ApplicationPackage#getPart_Widget()
	 * @model transient="true"
	 * @generated
	 */
	Object getWidget();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.Part#getWidget <em>Widget</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Widget</em>' attribute.
	 * @see #getWidget()
	 * @generated
	 */
	void setWidget(Object value);

	/**
	 * Returns the value of the '<em><b>Parent</b></em>' container reference.
	 * It is bidirectional and its opposite is '{@link org.eclipse.e4.ui.model.application.Part#getChildren <em>Children</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Parent</em>' container reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Parent</em>' container reference.
	 * @see #setParent(Part)
	 * @see org.eclipse.e4.ui.model.application.ApplicationPackage#getPart_Parent()
	 * @see org.eclipse.e4.ui.model.application.Part#getChildren
	 * @model opposite="children" transient="false"
	 * @generated
	 */
	Part<?> getParent();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.Part#getParent <em>Parent</em>}' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Parent</em>' container reference.
	 * @see #getParent()
	 * @generated
	 */
	void setParent(Part<?> value);

	/**
	 * Returns the value of the '<em><b>Visible</b></em>' attribute.
	 * The default value is <code>"true"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Visible</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Visible</em>' attribute.
	 * @see org.eclipse.e4.ui.model.application.ApplicationPackage#getPart_Visible()
	 * @model default="true" transient="true" changeable="false" derived="true"
	 * @generated
	 */
	boolean isVisible();

} // Part
