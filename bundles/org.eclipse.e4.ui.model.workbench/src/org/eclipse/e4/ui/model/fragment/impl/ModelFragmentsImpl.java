/**
 * Copyright (c) 2010, 2015 BestSolution.at and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *      Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *      IBM Corporation - initial API and implementation
 */
package org.eclipse.e4.ui.model.fragment.impl;

import java.util.Collection;
import java.util.List;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.fragment.MModelFragment;
import org.eclipse.e4.ui.model.fragment.MModelFragments;
import org.eclipse.emf.common.notify.NotificationChain;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Model Fragments</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.model.fragment.impl.ModelFragmentsImpl#getImports <em>Imports</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.fragment.impl.ModelFragmentsImpl#getFragments <em>Fragments</em>}</li>
 * </ul>
 *
 * @since 1.0
 * @generated
 */
public class ModelFragmentsImpl extends org.eclipse.emf.ecore.impl.MinimalEObjectImpl.Container
		implements MModelFragments {
	/**
	 * The cached value of the '{@link #getImports() <em>Imports</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getImports()
	 * @generated
	 * @ordered
	 */
	protected EList<MApplicationElement> imports;

	/**
	 * The cached value of the '{@link #getFragments() <em>Fragments</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFragments()
	 * @generated
	 * @ordered
	 */
	protected EList<MModelFragment> fragments;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ModelFragmentsImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return FragmentPackageImpl.Literals.MODEL_FRAGMENTS;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public List<MApplicationElement> getImports() {
		if (imports == null) {
			imports = new EObjectContainmentEList<>(MApplicationElement.class, this,
					FragmentPackageImpl.MODEL_FRAGMENTS__IMPORTS);
		}
		return imports;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public List<MModelFragment> getFragments() {
		if (fragments == null) {
			fragments = new EObjectContainmentEList<>(MModelFragment.class, this,
					FragmentPackageImpl.MODEL_FRAGMENTS__FRAGMENTS);
		}
		return fragments;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
		case FragmentPackageImpl.MODEL_FRAGMENTS__IMPORTS:
			return ((InternalEList<?>) getImports()).basicRemove(otherEnd, msgs);
		case FragmentPackageImpl.MODEL_FRAGMENTS__FRAGMENTS:
			return ((InternalEList<?>) getFragments()).basicRemove(otherEnd, msgs);
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
		case FragmentPackageImpl.MODEL_FRAGMENTS__IMPORTS:
			return getImports();
		case FragmentPackageImpl.MODEL_FRAGMENTS__FRAGMENTS:
			return getFragments();
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
		case FragmentPackageImpl.MODEL_FRAGMENTS__IMPORTS:
			getImports().clear();
			getImports().addAll((Collection<? extends MApplicationElement>) newValue);
			return;
		case FragmentPackageImpl.MODEL_FRAGMENTS__FRAGMENTS:
			getFragments().clear();
			getFragments().addAll((Collection<? extends MModelFragment>) newValue);
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
		case FragmentPackageImpl.MODEL_FRAGMENTS__IMPORTS:
			getImports().clear();
			return;
		case FragmentPackageImpl.MODEL_FRAGMENTS__FRAGMENTS:
			getFragments().clear();
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
		case FragmentPackageImpl.MODEL_FRAGMENTS__IMPORTS:
			return imports != null && !imports.isEmpty();
		case FragmentPackageImpl.MODEL_FRAGMENTS__FRAGMENTS:
			return fragments != null && !fragments.isEmpty();
		default:
			return super.eIsSet(featureID);
		}
	}

} //ModelFragmentsImpl
