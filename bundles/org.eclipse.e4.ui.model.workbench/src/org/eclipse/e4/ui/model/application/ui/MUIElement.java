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

import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>UI Element</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.MUIElement#getWidget <em>Widget</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.MUIElement#getRenderer <em>Renderer</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.MUIElement#isToBeRendered <em>To Be Rendered</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.MUIElement#isOnTop <em>On Top</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.MUIElement#isVisible <em>Visible</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.MUIElement#getParent <em>Parent</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.MUIElement#getContainerData <em>Container Data</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.MUIElement#getCurSharedRef <em>Cur Shared Ref</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.MUIElement#getVisibleWhen <em>Visible When</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.MUIElement#getAccessibilityPhrase <em>Accessibility Phrase</em>}</li>
 * </ul>
 * </p>
 *
 * @model abstract="true"
 * @generated
 */
public interface MUIElement extends MApplicationElement {
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
	 * @model transient="true" derived="true"
	 * @generated
	 */
	Object getWidget();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.ui.MUIElement#getWidget <em>Widget</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Widget</em>' attribute.
	 * @see #getWidget()
	 * @generated
	 */
	void setWidget(Object value);

	/**
	 * Returns the value of the '<em><b>Renderer</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Renderer</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Renderer</em>' attribute.
	 * @see #setRenderer(Object)
	 * @model transient="true" derived="true"
	 * @generated
	 */
	Object getRenderer();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.ui.MUIElement#getRenderer <em>Renderer</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Renderer</em>' attribute.
	 * @see #getRenderer()
	 * @generated
	 */
	void setRenderer(Object value);

	/**
	 * Returns the value of the '<em><b>To Be Rendered</b></em>' attribute.
	 * The default value is <code>"true"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>To Be Rendered</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>To Be Rendered</em>' attribute.
	 * @see #setToBeRendered(boolean)
	 * @model default="true"
	 * @generated
	 */
	boolean isToBeRendered();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.ui.MUIElement#isToBeRendered <em>To Be Rendered</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>To Be Rendered</em>' attribute.
	 * @see #isToBeRendered()
	 * @generated
	 */
	void setToBeRendered(boolean value);

	/**
	 * Returns the value of the '<em><b>On Top</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>On Top</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>On Top</em>' attribute.
	 * @see #setOnTop(boolean)
	 * @model
	 * @generated
	 */
	boolean isOnTop();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.ui.MUIElement#isOnTop <em>On Top</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>On Top</em>' attribute.
	 * @see #isOnTop()
	 * @generated
	 */
	void setOnTop(boolean value);

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
	 * @model default="true"
	 * @generated
	 */
	boolean isVisible();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.ui.MUIElement#isVisible <em>Visible</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Visible</em>' attribute.
	 * @see #isVisible()
	 * @generated
	 */
	void setVisible(boolean value);

	/**
	 * Returns the value of the '<em><b>Parent</b></em>' container reference.
	 * It is bidirectional and its opposite is '{@link org.eclipse.e4.ui.model.application.ui.MElementContainer#getChildren <em>Children</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Parent</em>' container reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Parent</em>' container reference.
	 * @see #setParent(MElementContainer)
	 * @see org.eclipse.e4.ui.model.application.ui.MElementContainer#getChildren
	 * @model opposite="children" transient="false"
	 * @generated
	 */
	MElementContainer<MUIElement> getParent();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.ui.MUIElement#getParent <em>Parent</em>}' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Parent</em>' container reference.
	 * @see #getParent()
	 * @generated
	 */
	void setParent(MElementContainer<MUIElement> value);

	/**
	 * Returns the value of the '<em><b>Container Data</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Container Data</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Container Data</em>' attribute.
	 * @see #setContainerData(String)
	 * @model
	 * @generated
	 */
	String getContainerData();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.ui.MUIElement#getContainerData <em>Container Data</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Container Data</em>' attribute.
	 * @see #getContainerData()
	 * @generated
	 */
	void setContainerData(String value);

	/**
	 * Returns the value of the '<em><b>Cur Shared Ref</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Cur Shared Ref</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Cur Shared Ref</em>' reference.
	 * @see #setCurSharedRef(MPlaceholder)
	 * @model transient="true" derived="true"
	 * @generated
	 */
	MPlaceholder getCurSharedRef();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.ui.MUIElement#getCurSharedRef <em>Cur Shared Ref</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Cur Shared Ref</em>' reference.
	 * @see #getCurSharedRef()
	 * @generated
	 */
	void setCurSharedRef(MPlaceholder value);

	/**
	 * Returns the value of the '<em><b>Visible When</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Visible When</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Visible When</em>' containment reference.
	 * @see #setVisibleWhen(MExpression)
	 * @model containment="true"
	 * @generated
	 */
	MExpression getVisibleWhen();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.ui.MUIElement#getVisibleWhen <em>Visible When</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Visible When</em>' containment reference.
	 * @see #getVisibleWhen()
	 * @generated
	 */
	void setVisibleWhen(MExpression value);

	/**
	 * Returns the value of the '<em><b>Accessibility Phrase</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Accessibility Phrase</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Accessibility Phrase</em>' attribute.
	 * @see #setAccessibilityPhrase(String)
	 * @model
	 * @generated
	 */
	String getAccessibilityPhrase();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.ui.MUIElement#getAccessibilityPhrase <em>Accessibility Phrase</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Accessibility Phrase</em>' attribute.
	 * @see #getAccessibilityPhrase()
	 * @generated
	 */
	void setAccessibilityPhrase(String value);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model kind="operation"
	 * @generated
	 */
	String getLocalizedAccessibilityPhrase();

} // MUIElement
