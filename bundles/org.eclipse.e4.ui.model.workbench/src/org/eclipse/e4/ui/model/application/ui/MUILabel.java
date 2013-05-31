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
package org.eclipse.e4.ui.model.application.ui;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>UI Label</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * <p>
 * This is a mix in that will be used for UI Elements that are capable of showing label
 * information in the GUI (e.g. Parts, Menus / Toolbars, Persepectives...)
 * </p>
 * @since 1.0
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.MUILabel#getLabel <em>Label</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.MUILabel#getIconURI <em>Icon URI</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.MUILabel#getTooltip <em>Tooltip</em>}</li>
 * </ul>
 * </p>
 *
 * @model interface="true" abstract="true"
 * @generated
 */
public interface MUILabel {
	/**
	 * Returns the value of the '<em><b>Label</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * <p>
	 * The label to display for this element. If the label is expected to be internationalized
	 * then the label may be set to a 'key' value to be used by the translation service.
	 * </p>
	 * @since 1.0
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Label</em>' attribute.
	 * @see #setLabel(String)
	 * @model
	 * @generated
	 */
	String getLabel();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.ui.MUILabel#getLabel <em>Label</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Label</em>' attribute.
	 * @see #getLabel()
	 * @generated
	 */
	void setLabel(String value);

	/**
	 * Returns the value of the '<em><b>Icon URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * <p>
	 * This field contains a fully qualified URL defining the path to an Image to display
	 * for this element.
	 * </p>
	 * @since 1.0
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Icon URI</em>' attribute.
	 * @see #setIconURI(String)
	 * @model
	 * @generated
	 */
	String getIconURI();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.ui.MUILabel#getIconURI <em>Icon URI</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Icon URI</em>' attribute.
	 * @see #getIconURI()
	 * @generated
	 */
	void setIconURI(String value);

	/**
	 * Returns the value of the '<em><b>Tooltip</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * <p>
	 * The tooltip to display for this element. If the tooltip is expected to be internationalized
	 * then the tooltip may be set to a 'key' value to be used by the translation service.
	 * </p>
	 * @since 1.0
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Tooltip</em>' attribute.
	 * @see #setTooltip(String)
	 * @model
	 * @generated
	 */
	String getTooltip();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.ui.MUILabel#getTooltip <em>Tooltip</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Tooltip</em>' attribute.
	 * @see #getTooltip()
	 * @generated
	 */
	void setTooltip(String value);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * <p>
	 * This is a method that will retrieve the internationalized label by using the current
	 * value of the label itself and some translation service.
	 * </p>
	 * @since 1.0
	 * <!-- end-model-doc -->
	 * @model kind="operation"
	 * @generated
	 */
	String getLocalizedLabel();

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * <p>
	 * This is a method that will retrieve the internationalized tooltip by using the current
	 * value of the label itself and some translation service.
	 * </p>
	 * @since 1.0
	 * <!-- end-model-doc -->
	 * @model kind="operation"
	 * @generated
	 */
	String getLocalizedTooltip();

} // MUILabel
