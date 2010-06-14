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
package org.eclipse.e4.ui.model.application.ui.menu.impl;

import java.util.Collection;
import java.util.List;
import org.eclipse.e4.ui.model.application.ui.impl.ElementContainerImpl;

import org.eclipse.e4.ui.model.application.ui.menu.MToolBarContribution;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBarContributions;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.impl.EObjectImpl;
import org.eclipse.emf.ecore.util.EObjectResolvingEList;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Tool Bar Contributions</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.menu.impl.ToolBarContributionsImpl#getToolBarContributions <em>Tool Bar Contributions</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class ToolBarContributionsImpl extends EObjectImpl implements MToolBarContributions {
	/**
	 * The cached value of the '{@link #getToolBarContributions() <em>Tool Bar Contributions</em>}' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getToolBarContributions()
	 * @generated
	 * @ordered
	 */
	protected EList<MToolBarContribution> toolBarContributions;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ToolBarContributionsImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return MenuPackageImpl.Literals.TOOL_BAR_CONTRIBUTIONS;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public List<MToolBarContribution> getToolBarContributions() {
		if (toolBarContributions == null) {
			toolBarContributions = new EObjectResolvingEList<MToolBarContribution>(MToolBarContribution.class, this, MenuPackageImpl.TOOL_BAR_CONTRIBUTIONS__TOOL_BAR_CONTRIBUTIONS);
		}
		return toolBarContributions;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case MenuPackageImpl.TOOL_BAR_CONTRIBUTIONS__TOOL_BAR_CONTRIBUTIONS:
				return getToolBarContributions();
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
			case MenuPackageImpl.TOOL_BAR_CONTRIBUTIONS__TOOL_BAR_CONTRIBUTIONS:
				getToolBarContributions().clear();
				getToolBarContributions().addAll((Collection<? extends MToolBarContribution>)newValue);
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
			case MenuPackageImpl.TOOL_BAR_CONTRIBUTIONS__TOOL_BAR_CONTRIBUTIONS:
				getToolBarContributions().clear();
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
			case MenuPackageImpl.TOOL_BAR_CONTRIBUTIONS__TOOL_BAR_CONTRIBUTIONS:
				return toolBarContributions != null && !toolBarContributions.isEmpty();
		}
		return super.eIsSet(featureID);
	}

} //ToolBarContributionsImpl
