/**
 * Copyright (c) 2008, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *      IBM Corporation - initial API and implementation
 */
package org.eclipse.e4.ui.model.application.ui.impl;

import java.util.Collection;
import java.util.List;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.internal.ModelUtils;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.util.EObjectContainmentWithInverseEList;
import org.eclipse.emf.ecore.util.InternalEList;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Element Container</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.impl.ElementContainerImpl#getChildren <em>Children</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.impl.ElementContainerImpl#getSelectedElement <em>Selected Element</em>}</li>
 * </ul>
 *
 * @since 1.0
 * @generated
 */
public abstract class ElementContainerImpl<T extends MUIElement> extends UIElementImpl implements MElementContainer<T> {
	/**
	 * The cached value of the '{@link #getChildren() <em>Children</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getChildren()
	 * @generated
	 * @ordered
	 */
	protected EList<T> children;

	/**
	 * The cached value of the '{@link #getSelectedElement() <em>Selected Element</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSelectedElement()
	 * @generated
	 * @ordered
	 */
	protected T selectedElement;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ElementContainerImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return UiPackageImpl.Literals.ELEMENT_CONTAINER;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated NOT
	 */
	@Override
	public List<T> getChildren() {
		if (children == null) {
			EClassifier classifier = ModelUtils.getTypeArgument(eClass(),
					UiPackageImpl.Literals.ELEMENT_CONTAINER__CHILDREN.getEGenericType());
			final Class<?> clazz;

			if (classifier != null && classifier.getInstanceClass() != null) {
				clazz = classifier.getInstanceClass();
			} else {
				clazz = null;
			}

			children = new EObjectContainmentWithInverseEList<>(MUIElement.class, this,
					UiPackageImpl.ELEMENT_CONTAINER__CHILDREN, UiPackageImpl.UI_ELEMENT__PARENT) {

				private static final long serialVersionUID = 1L;

				@Override
				protected boolean isInstance(Object object) {
					return super.isInstance(object) && (clazz == null || clazz.isInstance(object));
				}

				@Override
				protected T validate(int index, T object) {
					if (isInstance(object)) {
						return object;
					} else {
						throw new IllegalArgumentException(
								"The added object '" + object + "' is not assignable to '" + clazz.getName() + "'");
					}
				}
			};
		}
		return children;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public T getSelectedElement() {
		if (selectedElement != null && ((EObject) selectedElement).eIsProxy()) {
			InternalEObject oldSelectedElement = (InternalEObject) selectedElement;
			selectedElement = (T) eResolveProxy(oldSelectedElement);
			if (selectedElement != oldSelectedElement) {
				if (eNotificationRequired()) {
					eNotify(new ENotificationImpl(this, Notification.RESOLVE,
							UiPackageImpl.ELEMENT_CONTAINER__SELECTED_ELEMENT, oldSelectedElement, selectedElement));
				}
			}
		}
		return selectedElement;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public T basicGetSelectedElement() {
		return selectedElement;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private void setSelectedElementGen(T newSelectedElement) {
		T oldSelectedElement = selectedElement;
		selectedElement = newSelectedElement;
		if (eNotificationRequired()) {
			eNotify(new ENotificationImpl(this, Notification.SET, UiPackageImpl.ELEMENT_CONTAINER__SELECTED_ELEMENT,
					oldSelectedElement, selectedElement));
		}
	}

	@Override
	public void setSelectedElement(T newSelectedElement) {
		// Ensure that the new candidate is in *our* child list
		if (newSelectedElement != null && newSelectedElement.getParent() != this) {
			throw new IllegalArgumentException(
					"The selected element " + newSelectedElement + " is not a child of this container");
		}

		// Ensure that the new candidate is visible in the UI
		if (newSelectedElement != null && !newSelectedElement.isToBeRendered()) {
			throw new IllegalArgumentException(
					"The selected element " + newSelectedElement + " must be visible in the UI presentation");
		}

		setSelectedElementGen(newSelectedElement);
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
		case UiPackageImpl.ELEMENT_CONTAINER__CHILDREN:
			return ((InternalEList<InternalEObject>) (InternalEList<?>) getChildren()).basicAdd(otherEnd, msgs);
		default:
			return super.eInverseAdd(otherEnd, featureID, msgs);
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
		case UiPackageImpl.ELEMENT_CONTAINER__CHILDREN:
			return ((InternalEList<?>) getChildren()).basicRemove(otherEnd, msgs);
		default:
			return super.eInverseRemove(otherEnd, featureID, msgs);
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
		case UiPackageImpl.ELEMENT_CONTAINER__CHILDREN:
			return getChildren();
		case UiPackageImpl.ELEMENT_CONTAINER__SELECTED_ELEMENT:
			if (resolve) {
				return getSelectedElement();
			}
			return basicGetSelectedElement();
		default:
			return super.eGet(featureID, resolve, coreType);
		}
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
		case UiPackageImpl.ELEMENT_CONTAINER__CHILDREN:
			getChildren().clear();
			getChildren().addAll((Collection<? extends T>) newValue);
			return;
		case UiPackageImpl.ELEMENT_CONTAINER__SELECTED_ELEMENT:
			setSelectedElement((T) newValue);
			return;
		default:
			super.eSet(featureID, newValue);
			return;
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
		case UiPackageImpl.ELEMENT_CONTAINER__CHILDREN:
			getChildren().clear();
			return;
		case UiPackageImpl.ELEMENT_CONTAINER__SELECTED_ELEMENT:
			setSelectedElement((T) null);
			return;
		default:
			super.eUnset(featureID);
			return;
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
		case UiPackageImpl.ELEMENT_CONTAINER__CHILDREN:
			return children != null && !children.isEmpty();
		case UiPackageImpl.ELEMENT_CONTAINER__SELECTED_ELEMENT:
			return selectedElement != null;
		default:
			return super.eIsSet(featureID);
		}
	}

} //ElementContainerImpl
