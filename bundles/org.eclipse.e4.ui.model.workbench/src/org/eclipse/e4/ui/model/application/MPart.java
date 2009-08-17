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
 * $Id: MPart.java,v 1.4 2009/04/13 19:47:35 emoffatt Exp $
 */
package org.eclipse.e4.ui.model.application;

import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.emf.common.util.EList;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>MPart</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.model.application.MPart#getMenu <em>Menu</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.MPart#getToolBar <em>Tool Bar</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.MPart#getPolicy <em>Policy</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.MPart#getChildren <em>Children</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.MPart#getActiveChild <em>Active Child</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.MPart#getHandlers <em>Handlers</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.MPart#getWidget <em>Widget</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.MPart#getParent <em>Parent</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.MPart#isVisible <em>Visible</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.MPart#getContext <em>Context</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.MPart#getVariables <em>Variables</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.eclipse.e4.ui.model.application.ApplicationPackage#getMPart()
 * @model
 * @generated
 */
public interface MPart<P extends MPart<?>> extends MApplicationElement {
	/**
	 * Returns the value of the '<em><b>Menu</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Menu</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Menu</em>' containment reference.
	 * @see #setMenu(MMenu)
	 * @see org.eclipse.e4.ui.model.application.ApplicationPackage#getMPart_Menu()
	 * @model containment="true"
	 * @generated
	 */
	MMenu getMenu();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.MPart#getMenu <em>Menu</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Menu</em>' containment reference.
	 * @see #getMenu()
	 * @generated
	 */
	void setMenu(MMenu value);

	/**
	 * Returns the value of the '<em><b>Tool Bar</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Tool Bar</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Tool Bar</em>' containment reference.
	 * @see #setToolBar(MToolBar)
	 * @see org.eclipse.e4.ui.model.application.ApplicationPackage#getMPart_ToolBar()
	 * @model containment="true"
	 * @generated
	 */
	MToolBar getToolBar();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.MPart#getToolBar <em>Tool Bar</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Tool Bar</em>' containment reference.
	 * @see #getToolBar()
	 * @generated
	 */
	void setToolBar(MToolBar value);

	/**
	 * Returns the value of the '<em><b>Policy</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The policy would define how the parent displays the children (stack/sashforms)
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Policy</em>' attribute.
	 * @see #setPolicy(String)
	 * @see org.eclipse.e4.ui.model.application.ApplicationPackage#getMPart_Policy()
	 * @model
	 * @generated
	 */
	String getPolicy();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.MPart#getPolicy <em>Policy</em>}' attribute.
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
	 * It is bidirectional and its opposite is '{@link org.eclipse.e4.ui.model.application.MPart#getParent <em>Parent</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Children</em>' containment reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Children</em>' containment reference list.
	 * @see org.eclipse.e4.ui.model.application.ApplicationPackage#getMPart_Children()
	 * @see org.eclipse.e4.ui.model.application.MPart#getParent
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
	 * @see #setActiveChild(MPart)
	 * @see org.eclipse.e4.ui.model.application.ApplicationPackage#getMPart_ActiveChild()
	 * @model
	 * @generated
	 */
	P getActiveChild();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.MPart#getActiveChild <em>Active Child</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Active Child</em>' reference.
	 * @see #getActiveChild()
	 * @generated
	 */
	void setActiveChild(P value);

	/**
	 * Returns the value of the '<em><b>Handlers</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.e4.ui.model.application.MHandler}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Handlers</em>' containment reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Handlers</em>' containment reference list.
	 * @see org.eclipse.e4.ui.model.application.ApplicationPackage#getMPart_Handlers()
	 * @model containment="true"
	 * @generated
	 */
	EList<MHandler> getHandlers();

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
	 * @see org.eclipse.e4.ui.model.application.ApplicationPackage#getMPart_Widget()
	 * @model transient="true"
	 * @generated
	 */
	Object getWidget();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.MPart#getWidget <em>Widget</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Widget</em>' attribute.
	 * @see #getWidget()
	 * @generated
	 */
	void setWidget(Object value);

	/**
	 * Returns the value of the '<em><b>Parent</b></em>' container reference.
	 * It is bidirectional and its opposite is '{@link org.eclipse.e4.ui.model.application.MPart#getChildren <em>Children</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Parent</em>' container reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Parent</em>' container reference.
	 * @see #setParent(MPart)
	 * @see org.eclipse.e4.ui.model.application.ApplicationPackage#getMPart_Parent()
	 * @see org.eclipse.e4.ui.model.application.MPart#getChildren
	 * @model opposite="children" transient="false"
	 * @generated
	 */
	MPart<?> getParent();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.MPart#getParent <em>Parent</em>}' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Parent</em>' container reference.
	 * @see #getParent()
	 * @generated
	 */
	void setParent(MPart<?> value);

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
	 * @see #setVisible(boolean)
	 * @see org.eclipse.e4.ui.model.application.ApplicationPackage#getMPart_Visible()
	 * @model default="true" derived="true"
	 * @generated
	 */
	boolean isVisible();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.MPart#isVisible <em>Visible</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Visible</em>' attribute.
	 * @see #isVisible()
	 * @generated
	 */
	void setVisible(boolean value);

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
	 * @see org.eclipse.e4.ui.model.application.ApplicationPackage#getMPart_Context()
	 * @model dataType="org.eclipse.e4.ui.model.application.IEclipseContext" transient="true"
	 * @generated
	 */
	IEclipseContext getContext();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.MPart#getContext <em>Context</em>}' attribute.
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
	 * @see org.eclipse.e4.ui.model.application.ApplicationPackage#getMPart_Variables()
	 * @model unique="false"
	 * @generated
	 */
	EList<String> getVariables();

} // MPart
