/**
 * Copyright (c) 2012 IBM Corporation and BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *      IBM Corporation - initial API and implementation
 *      Tom Schindl - initial API and implementation
 */
package org.eclipse.e4.demo.split.model.split.impl;

import java.util.Collection;
import java.util.List;

import org.eclipse.e4.demo.split.model.split.MStackSashContainer;

import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MGenericTile;
import org.eclipse.e4.ui.model.application.ui.MUIElement;

import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainer;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainerElement;
import org.eclipse.e4.ui.model.application.ui.basic.MWindowElement;

import org.eclipse.e4.ui.model.application.ui.impl.UIElementImpl;
import org.eclipse.e4.ui.model.application.ui.impl.UiPackageImpl;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

import org.eclipse.emf.ecore.util.EObjectContainmentWithInverseEList;
import org.eclipse.emf.ecore.util.InternalEList;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Stack Sash Container</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.eclipse.e4.demo.split.model.split.impl.StackSashContainerImpl#getChildren <em>Children</em>}</li>
 *   <li>{@link org.eclipse.e4.demo.split.model.split.impl.StackSashContainerImpl#getSelectedElement <em>Selected Element</em>}</li>
 *   <li>{@link org.eclipse.e4.demo.split.model.split.impl.StackSashContainerImpl#isHorizontal <em>Horizontal</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class StackSashContainerImpl extends UIElementImpl implements MStackSashContainer {
	/**
	 * The cached value of the '{@link #getChildren() <em>Children</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getChildren()
	 * @generated
	 * @ordered
	 */
	protected EList<MPartSashContainerElement> children;

	/**
	 * The cached value of the '{@link #getSelectedElement() <em>Selected Element</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSelectedElement()
	 * @generated
	 * @ordered
	 */
	protected MPartSashContainerElement selectedElement;

	/**
	 * The default value of the '{@link #isHorizontal() <em>Horizontal</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isHorizontal()
	 * @generated
	 * @ordered
	 */
	protected static final boolean HORIZONTAL_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isHorizontal() <em>Horizontal</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isHorizontal()
	 * @generated
	 * @ordered
	 */
	protected boolean horizontal = HORIZONTAL_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected StackSashContainerImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return SplitPackageImpl.Literals.STACK_SASH_CONTAINER;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public List<MPartSashContainerElement> getChildren() {
		if (children == null) {
			children = new EObjectContainmentWithInverseEList<MPartSashContainerElement>(MUIElement.class, this, SplitPackageImpl.STACK_SASH_CONTAINER__CHILDREN, UiPackageImpl.UI_ELEMENT__PARENT);
		}
		return children;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MPartSashContainerElement getSelectedElement() {
		if (selectedElement != null && ((EObject)selectedElement).eIsProxy()) {
			InternalEObject oldSelectedElement = (InternalEObject)selectedElement;
			selectedElement = (MPartSashContainerElement)eResolveProxy(oldSelectedElement);
			if (selectedElement != oldSelectedElement) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, SplitPackageImpl.STACK_SASH_CONTAINER__SELECTED_ELEMENT, oldSelectedElement, selectedElement));
			}
		}
		return selectedElement;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MPartSashContainerElement basicGetSelectedElement() {
		return selectedElement;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setSelectedElement(MPartSashContainerElement newSelectedElement) {
		MPartSashContainerElement oldSelectedElement = selectedElement;
		selectedElement = newSelectedElement;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, SplitPackageImpl.STACK_SASH_CONTAINER__SELECTED_ELEMENT, oldSelectedElement, selectedElement));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isHorizontal() {
		return horizontal;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setHorizontal(boolean newHorizontal) {
		boolean oldHorizontal = horizontal;
		horizontal = newHorizontal;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, SplitPackageImpl.STACK_SASH_CONTAINER__HORIZONTAL, oldHorizontal, horizontal));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public NotificationChain eInverseAdd(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case SplitPackageImpl.STACK_SASH_CONTAINER__CHILDREN:
				return ((InternalEList<InternalEObject>)(InternalEList<?>)getChildren()).basicAdd(otherEnd, msgs);
		}
		return super.eInverseAdd(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case SplitPackageImpl.STACK_SASH_CONTAINER__CHILDREN:
				return ((InternalEList<?>)getChildren()).basicRemove(otherEnd, msgs);
		}
		return super.eInverseRemove(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case SplitPackageImpl.STACK_SASH_CONTAINER__CHILDREN:
				return getChildren();
			case SplitPackageImpl.STACK_SASH_CONTAINER__SELECTED_ELEMENT:
				if (resolve) return getSelectedElement();
				return basicGetSelectedElement();
			case SplitPackageImpl.STACK_SASH_CONTAINER__HORIZONTAL:
				return isHorizontal();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case SplitPackageImpl.STACK_SASH_CONTAINER__CHILDREN:
				getChildren().clear();
				getChildren().addAll((Collection<? extends MPartSashContainerElement>)newValue);
				return;
			case SplitPackageImpl.STACK_SASH_CONTAINER__SELECTED_ELEMENT:
				setSelectedElement((MPartSashContainerElement)newValue);
				return;
			case SplitPackageImpl.STACK_SASH_CONTAINER__HORIZONTAL:
				setHorizontal((Boolean)newValue);
				return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
			case SplitPackageImpl.STACK_SASH_CONTAINER__CHILDREN:
				getChildren().clear();
				return;
			case SplitPackageImpl.STACK_SASH_CONTAINER__SELECTED_ELEMENT:
				setSelectedElement((MPartSashContainerElement)null);
				return;
			case SplitPackageImpl.STACK_SASH_CONTAINER__HORIZONTAL:
				setHorizontal(HORIZONTAL_EDEFAULT);
				return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
			case SplitPackageImpl.STACK_SASH_CONTAINER__CHILDREN:
				return children != null && !children.isEmpty();
			case SplitPackageImpl.STACK_SASH_CONTAINER__SELECTED_ELEMENT:
				return selectedElement != null;
			case SplitPackageImpl.STACK_SASH_CONTAINER__HORIZONTAL:
				return horizontal != HORIZONTAL_EDEFAULT;
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int eBaseStructuralFeatureID(int derivedFeatureID, Class<?> baseClass) {
		if (baseClass == MElementContainer.class) {
			switch (derivedFeatureID) {
				case SplitPackageImpl.STACK_SASH_CONTAINER__CHILDREN: return UiPackageImpl.ELEMENT_CONTAINER__CHILDREN;
				case SplitPackageImpl.STACK_SASH_CONTAINER__SELECTED_ELEMENT: return UiPackageImpl.ELEMENT_CONTAINER__SELECTED_ELEMENT;
				default: return -1;
			}
		}
		if (baseClass == MGenericTile.class) {
			switch (derivedFeatureID) {
				case SplitPackageImpl.STACK_SASH_CONTAINER__HORIZONTAL: return UiPackageImpl.GENERIC_TILE__HORIZONTAL;
				default: return -1;
			}
		}
		if (baseClass == MPartSashContainerElement.class) {
			switch (derivedFeatureID) {
				default: return -1;
			}
		}
		if (baseClass == MWindowElement.class) {
			switch (derivedFeatureID) {
				default: return -1;
			}
		}
		if (baseClass == MPartSashContainer.class) {
			switch (derivedFeatureID) {
				default: return -1;
			}
		}
		return super.eBaseStructuralFeatureID(derivedFeatureID, baseClass);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int eDerivedStructuralFeatureID(int baseFeatureID, Class<?> baseClass) {
		if (baseClass == MElementContainer.class) {
			switch (baseFeatureID) {
				case UiPackageImpl.ELEMENT_CONTAINER__CHILDREN: return SplitPackageImpl.STACK_SASH_CONTAINER__CHILDREN;
				case UiPackageImpl.ELEMENT_CONTAINER__SELECTED_ELEMENT: return SplitPackageImpl.STACK_SASH_CONTAINER__SELECTED_ELEMENT;
				default: return -1;
			}
		}
		if (baseClass == MGenericTile.class) {
			switch (baseFeatureID) {
				case UiPackageImpl.GENERIC_TILE__HORIZONTAL: return SplitPackageImpl.STACK_SASH_CONTAINER__HORIZONTAL;
				default: return -1;
			}
		}
		if (baseClass == MPartSashContainerElement.class) {
			switch (baseFeatureID) {
				default: return -1;
			}
		}
		if (baseClass == MWindowElement.class) {
			switch (baseFeatureID) {
				default: return -1;
			}
		}
		if (baseClass == MPartSashContainer.class) {
			switch (baseFeatureID) {
				default: return -1;
			}
		}
		return super.eDerivedStructuralFeatureID(baseFeatureID, baseClass);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String toString() {
		if (eIsProxy()) return super.toString();

		StringBuffer result = new StringBuffer(super.toString());
		result.append(" (horizontal: "); //$NON-NLS-1$
		result.append(horizontal);
		result.append(')');
		return result.toString();
	}

} //StackSashContainerImpl
