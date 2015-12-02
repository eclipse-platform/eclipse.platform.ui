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

import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>UI Element</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * <p>
 * This is the base mix-in shared by all model elements that can be rendered into the
 * UI presentation of the application. Its main job is to manage the bindings between
 * the concrete element and the UI 'widget' representing it in the UI.
 * </p>
 * @since 1.0
 * @noimplement This interface is not intended to be implemented by clients.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
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
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.MUIElement#getLocalizedAccessibilityPhrase <em>Localized Accessibility Phrase</em>}</li>
 * </ul>
 *
 * @model abstract="true"
 * @generated
 */
public interface MUIElement extends MApplicationElement, MLocalizable {
	/**
	 * Returns the value of the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * <p>
	 * This field represents the platform specific UI 'widget' that is representing this
	 * UIElement on the screen. It will only be non-null when the element has been rendered.
	 * </p>
	 * <!-- end-model-doc -->
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
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * <p>
	 * This field tracks the specific renderer used to create the 'widget'.
	 * </p>
	 * <!-- end-model-doc -->
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
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * <p>
	 * This field controls whether the given UIElement should be displayed within
	 * the application. Note that due to lazy loading it is possible to have this field
	 * set to true but to not have actually rendered the element itself (it does show up
	 * as a tab on the appropiate stack but will only be rendered when that tab is
	 * selected.
	 * </p>
	 * <!-- end-model-doc -->
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
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * <p>
	 * <strong>Developers</strong>:
	 * Add more detailed documentation by editing this comment in 
	 * org.eclipse.ui.model.workbench/model/UIElements.ecore. 
	 * There is a GenModel/documentation node under each type and attribute.
	 * </p>
	 * <!-- end-model-doc -->
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
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * <p>
	 * This field determines whether or not the given UIElement appears in the presentation
	 * or whether it should be 'cached' for specialized use. Under normal circumstances
	 * this flag should always be 'true'.
	 * </p><p>
	 * The MinMaxAddon uses this flag for example when a stack becomes minimized. By
	 * setting the flag to false the stack's widget is cleanly removed from the UI but
	 * is still 'rendered'. Once the widget has been cached the minimized stack can then
	 * display the widget using its own technques.
	 * </p>
	 * <!-- end-model-doc -->
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
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * <p>
	 * This field is a reference to this element's container. Note that while this field is valid
	 * for most UIElements there are a few (such as TrimBars and the Windows associated
	 * with top level windows and perspectives) where this will return 'null' 
	 * </p>
	 * <!-- end-model-doc -->
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
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * <p>
	 * This is a persistend field that may be used by the <b>parent</b> element's renderer
	 * to maintain any data that it needs to control the container. For example this is where
	 * the SashRenderer stores the 'weight' of a particular element.
	 * </p> <p>
	 * <b>NOTE:</b> This field is effectively deprecated in favor of the parent renderer
	 * simply adding a new keyed value to the UIElement's 'persistentData' map.
	 * </p>
	 * <!-- end-model-doc -->
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
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * <p>
	 * This is a transient (i.e. non-persisted) field which is used in conjunction with
	 * MPlaceholders which are used to share elements actoss multiple perspectives. This
	 * field will point back to the MPlaceholder (if any) currently hosting this one.
	 * </p>
	 * <!-- end-model-doc -->
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
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * <p>
	 * <strong>Developers</strong>:
	 * Add more detailed documentation by editing this comment in 
	 * org.eclipse.ui.model.workbench/model/UIElements.ecore. 
	 * There is a GenModel/documentation node under each type and attribute.
	 * </p>
	 * <!-- end-model-doc -->
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
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * <p>
	 * This field is provided as a way to inform accessibility screen readers with extra
	 * information. The intent is that the reader should 'say' this phrase as well as what
	 * it would normally emit given the widget hierarchy.
	 * </p>
	 * <!-- end-model-doc -->
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
	 * Returns the value of the '<em><b>Localized Accessibility Phrase</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Localized Accessibility Phrase</em>' attribute.
	 * @model transient="true" changeable="false" volatile="true" derived="true"
	 * @generated
	 */
	String getLocalizedAccessibilityPhrase();

} // MUIElement
