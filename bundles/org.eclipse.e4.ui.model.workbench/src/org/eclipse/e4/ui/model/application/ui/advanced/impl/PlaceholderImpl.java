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
package org.eclipse.e4.ui.model.application.ui.advanced.impl;

import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.impl.UIElementImpl;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.ENotificationImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Placeholder</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.advanced.impl.PlaceholderImpl#getRef <em>Ref</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.advanced.impl.PlaceholderImpl#isCloseable <em>Closeable</em>}</li>
 * </ul>
 *
 * @since 1.0
 * @generated
 */
public class PlaceholderImpl extends UIElementImpl implements MPlaceholder {
	/**
	 * The cached value of the '{@link #getRef() <em>Ref</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getRef()
	 * @generated
	 * @ordered
	 */
	protected MUIElement ref;

	/**
	 * The default value of the '{@link #isCloseable() <em>Closeable</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isCloseable()
	 * @generated
	 * @ordered
	 */
	protected static final boolean CLOSEABLE_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isCloseable() <em>Closeable</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isCloseable()
	 * @generated
	 * @ordered
	 */
	protected boolean closeable = CLOSEABLE_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected PlaceholderImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return AdvancedPackageImpl.Literals.PLACEHOLDER;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public MUIElement getRef() {
		if (ref != null && ((EObject) ref).eIsProxy()) {
			InternalEObject oldRef = (InternalEObject) ref;
			ref = (MUIElement) eResolveProxy(oldRef);
			if (ref != oldRef) {
				if (eNotificationRequired()) {
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, AdvancedPackageImpl.PLACEHOLDER__REF,
							oldRef, ref));
				}
			}
		}
		return ref;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MUIElement basicGetRef() {
		return ref;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setRef(MUIElement newRef) {
		MUIElement oldRef = ref;
		ref = newRef;
		if (eNotificationRequired()) {
			eNotify(new ENotificationImpl(this, Notification.SET, AdvancedPackageImpl.PLACEHOLDER__REF, oldRef, ref));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isCloseable() {
		return closeable;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setCloseable(boolean newCloseable) {
		boolean oldCloseable = closeable;
		closeable = newCloseable;
		if (eNotificationRequired()) {
			eNotify(new ENotificationImpl(this, Notification.SET, AdvancedPackageImpl.PLACEHOLDER__CLOSEABLE,
					oldCloseable, closeable));
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
		case AdvancedPackageImpl.PLACEHOLDER__REF:
			if (resolve) {
				return getRef();
			}
			return basicGetRef();
		case AdvancedPackageImpl.PLACEHOLDER__CLOSEABLE:
			return isCloseable();
		default:
			return super.eGet(featureID, resolve, coreType);
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
		case AdvancedPackageImpl.PLACEHOLDER__REF:
			setRef((MUIElement) newValue);
			return;
		case AdvancedPackageImpl.PLACEHOLDER__CLOSEABLE:
			setCloseable((Boolean) newValue);
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
		case AdvancedPackageImpl.PLACEHOLDER__REF:
			setRef((MUIElement) null);
			return;
		case AdvancedPackageImpl.PLACEHOLDER__CLOSEABLE:
			setCloseable(CLOSEABLE_EDEFAULT);
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
		case AdvancedPackageImpl.PLACEHOLDER__REF:
			return ref != null;
		case AdvancedPackageImpl.PLACEHOLDER__CLOSEABLE:
			return closeable != CLOSEABLE_EDEFAULT;
		default:
			return super.eIsSet(featureID);
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String toString() {
		if (eIsProxy()) {
			return super.toString();
		}

		StringBuilder result = new StringBuilder(super.toString());
		result.append(" (closeable: "); //$NON-NLS-1$
		result.append(closeable);
		result.append(')');
		return result.toString();
	}

} //PlaceholderImpl
