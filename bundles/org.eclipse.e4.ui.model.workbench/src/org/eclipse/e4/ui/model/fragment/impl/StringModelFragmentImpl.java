/**
 * Copyright (c) 2010 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *      IBM Corporation - initial API and implementation
 */
package org.eclipse.e4.ui.model.fragment.impl;

import java.util.Collections;
import java.util.List;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.fragment.MStringModelFragment;
import org.eclipse.e4.ui.model.internal.ModelUtils;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.impl.ENotificationImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>String Model Fragment</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.model.fragment.impl.StringModelFragmentImpl#getFeaturename <em>Featurename</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.fragment.impl.StringModelFragmentImpl#getParentElementId <em>Parent Element Id</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.fragment.impl.StringModelFragmentImpl#getPositionInList <em>Position In List</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class StringModelFragmentImpl extends ModelFragmentImpl implements MStringModelFragment {
	/**
	 * The default value of the '{@link #getFeaturename() <em>Featurename</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFeaturename()
	 * @generated
	 * @ordered
	 */
	protected static final String FEATURENAME_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getFeaturename() <em>Featurename</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFeaturename()
	 * @generated
	 * @ordered
	 */
	protected String featurename = FEATURENAME_EDEFAULT;

	/**
	 * The default value of the '{@link #getParentElementId() <em>Parent Element Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getParentElementId()
	 * @generated
	 * @ordered
	 */
	protected static final String PARENT_ELEMENT_ID_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getParentElementId() <em>Parent Element Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getParentElementId()
	 * @generated
	 * @ordered
	 */
	protected String parentElementId = PARENT_ELEMENT_ID_EDEFAULT;

	/**
	 * The default value of the '{@link #getPositionInList() <em>Position In List</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPositionInList()
	 * @generated
	 * @ordered
	 */
	protected static final String POSITION_IN_LIST_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getPositionInList() <em>Position In List</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPositionInList()
	 * @generated
	 * @ordered
	 */
	protected String positionInList = POSITION_IN_LIST_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected StringModelFragmentImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return FragmentPackageImpl.Literals.STRING_MODEL_FRAGMENT;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getFeaturename() {
		return featurename;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setFeaturename(String newFeaturename) {
		String oldFeaturename = featurename;
		featurename = newFeaturename;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, FragmentPackageImpl.STRING_MODEL_FRAGMENT__FEATURENAME, oldFeaturename, featurename));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getParentElementId() {
		return parentElementId;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setParentElementId(String newParentElementId) {
		String oldParentElementId = parentElementId;
		parentElementId = newParentElementId;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, FragmentPackageImpl.STRING_MODEL_FRAGMENT__PARENT_ELEMENT_ID, oldParentElementId, parentElementId));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getPositionInList() {
		return positionInList;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setPositionInList(String newPositionInList) {
		String oldPositionInList = positionInList;
		positionInList = newPositionInList;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, FragmentPackageImpl.STRING_MODEL_FRAGMENT__POSITION_IN_LIST, oldPositionInList, positionInList));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case FragmentPackageImpl.STRING_MODEL_FRAGMENT__FEATURENAME:
				return getFeaturename();
			case FragmentPackageImpl.STRING_MODEL_FRAGMENT__PARENT_ELEMENT_ID:
				return getParentElementId();
			case FragmentPackageImpl.STRING_MODEL_FRAGMENT__POSITION_IN_LIST:
				return getPositionInList();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case FragmentPackageImpl.STRING_MODEL_FRAGMENT__FEATURENAME:
				setFeaturename((String)newValue);
				return;
			case FragmentPackageImpl.STRING_MODEL_FRAGMENT__PARENT_ELEMENT_ID:
				setParentElementId((String)newValue);
				return;
			case FragmentPackageImpl.STRING_MODEL_FRAGMENT__POSITION_IN_LIST:
				setPositionInList((String)newValue);
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
			case FragmentPackageImpl.STRING_MODEL_FRAGMENT__FEATURENAME:
				setFeaturename(FEATURENAME_EDEFAULT);
				return;
			case FragmentPackageImpl.STRING_MODEL_FRAGMENT__PARENT_ELEMENT_ID:
				setParentElementId(PARENT_ELEMENT_ID_EDEFAULT);
				return;
			case FragmentPackageImpl.STRING_MODEL_FRAGMENT__POSITION_IN_LIST:
				setPositionInList(POSITION_IN_LIST_EDEFAULT);
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
			case FragmentPackageImpl.STRING_MODEL_FRAGMENT__FEATURENAME:
				return FEATURENAME_EDEFAULT == null ? featurename != null : !FEATURENAME_EDEFAULT.equals(featurename);
			case FragmentPackageImpl.STRING_MODEL_FRAGMENT__PARENT_ELEMENT_ID:
				return PARENT_ELEMENT_ID_EDEFAULT == null ? parentElementId != null : !PARENT_ELEMENT_ID_EDEFAULT.equals(parentElementId);
			case FragmentPackageImpl.STRING_MODEL_FRAGMENT__POSITION_IN_LIST:
				return POSITION_IN_LIST_EDEFAULT == null ? positionInList != null : !POSITION_IN_LIST_EDEFAULT.equals(positionInList);
		}
		return super.eIsSet(featureID);
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
		result.append(" (featurename: "); //$NON-NLS-1$
		result.append(featurename);
		result.append(", parentElementId: "); //$NON-NLS-1$
		result.append(parentElementId);
		result.append(", positionInList: "); //$NON-NLS-1$
		result.append(positionInList);
		result.append(')');
		return result.toString();
	}

	@Override
	public List<MApplicationElement> merge(MApplication application) {
		MApplicationElement o =  ModelUtils.findElementById(application, getParentElementId());
		if( o != null ) {
			EStructuralFeature feature = ((EObject)o).eClass().getEStructuralFeature(getFeaturename());
			if( feature != null ) {
				return ModelUtils.merge(o, feature, getElements(), getPositionInList());
			}

		}

		return Collections.emptyList();
	}

} //StringModelFragmentImpl
