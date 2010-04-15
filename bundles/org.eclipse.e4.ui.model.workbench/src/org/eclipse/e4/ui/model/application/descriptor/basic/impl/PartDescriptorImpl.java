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
package org.eclipse.e4.ui.model.application.descriptor.basic.impl;

import java.util.Collection;
import java.util.List;

import java.util.Map;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.commands.MBindings;
import org.eclipse.e4.ui.model.application.commands.MHandler;

import org.eclipse.e4.ui.model.application.commands.MHandlerContainer;
import org.eclipse.e4.ui.model.application.commands.impl.CommandsPackageImpl;

import org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor;

import org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl;
import org.eclipse.e4.ui.model.application.impl.StringToStringMapImpl;
import org.eclipse.e4.ui.model.application.ui.impl.UILabelImpl;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.common.util.EMap;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

import org.eclipse.emf.ecore.util.EDataTypeUniqueEList;
import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.EcoreEMap;
import org.eclipse.emf.ecore.util.InternalEList;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Part Descriptor</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.model.application.descriptor.basic.impl.PartDescriptorImpl#getHandlers <em>Handlers</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.descriptor.basic.impl.PartDescriptorImpl#getBindingContexts <em>Binding Contexts</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.descriptor.basic.impl.PartDescriptorImpl#getElementId <em>Element Id</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.descriptor.basic.impl.PartDescriptorImpl#getTags <em>Tags</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.descriptor.basic.impl.PartDescriptorImpl#isAllowMultiple <em>Allow Multiple</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.descriptor.basic.impl.PartDescriptorImpl#getCategory <em>Category</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.descriptor.basic.impl.PartDescriptorImpl#getMenus <em>Menus</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.descriptor.basic.impl.PartDescriptorImpl#getToolbar <em>Toolbar</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.descriptor.basic.impl.PartDescriptorImpl#isCloseable <em>Closeable</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.descriptor.basic.impl.PartDescriptorImpl#isDirtyable <em>Dirtyable</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.descriptor.basic.impl.PartDescriptorImpl#getContributionURI <em>Contribution URI</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class PartDescriptorImpl extends UILabelImpl implements MPartDescriptor {
	/**
	 * The cached value of the '{@link #getHandlers() <em>Handlers</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getHandlers()
	 * @generated
	 * @ordered
	 */
	protected EList<MHandler> handlers;

	/**
	 * The cached value of the '{@link #getBindingContexts() <em>Binding Contexts</em>}' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getBindingContexts()
	 * @generated
	 * @ordered
	 */
	protected EList<String> bindingContexts;

	/**
	 * The default value of the '{@link #getElementId() <em>Element Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getElementId()
	 * @generated
	 * @ordered
	 */
	protected static final String ELEMENT_ID_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getElementId() <em>Element Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getElementId()
	 * @generated
	 * @ordered
	 */
	protected String elementId = ELEMENT_ID_EDEFAULT;

	/**
	 * The cached value of the '{@link #getTags() <em>Tags</em>}' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTags()
	 * @generated
	 * @ordered
	 */
	protected EList<String> tags;

	/**
	 * The default value of the '{@link #isAllowMultiple() <em>Allow Multiple</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isAllowMultiple()
	 * @generated
	 * @ordered
	 */
	protected static final boolean ALLOW_MULTIPLE_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isAllowMultiple() <em>Allow Multiple</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isAllowMultiple()
	 * @generated
	 * @ordered
	 */
	protected boolean allowMultiple = ALLOW_MULTIPLE_EDEFAULT;

	/**
	 * The default value of the '{@link #getCategory() <em>Category</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCategory()
	 * @generated
	 * @ordered
	 */
	protected static final String CATEGORY_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getCategory() <em>Category</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCategory()
	 * @generated
	 * @ordered
	 */
	protected String category = CATEGORY_EDEFAULT;

	/**
	 * The cached value of the '{@link #getMenus() <em>Menus</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMenus()
	 * @generated
	 * @ordered
	 */
	protected EList<MMenu> menus;

	/**
	 * The cached value of the '{@link #getToolbar() <em>Toolbar</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getToolbar()
	 * @generated
	 * @ordered
	 */
	protected MToolBar toolbar;

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
	 * The default value of the '{@link #isDirtyable() <em>Dirtyable</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isDirtyable()
	 * @generated
	 * @ordered
	 */
	protected static final boolean DIRTYABLE_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isDirtyable() <em>Dirtyable</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isDirtyable()
	 * @generated
	 * @ordered
	 */
	protected boolean dirtyable = DIRTYABLE_EDEFAULT;

	/**
	 * The default value of the '{@link #getContributionURI() <em>Contribution URI</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getContributionURI()
	 * @generated
	 * @ordered
	 */
	protected static final String CONTRIBUTION_URI_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getContributionURI() <em>Contribution URI</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getContributionURI()
	 * @generated
	 * @ordered
	 */
	protected String contributionURI = CONTRIBUTION_URI_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected PartDescriptorImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return BasicPackageImpl.Literals.PART_DESCRIPTOR;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public List<MHandler> getHandlers() {
		if (handlers == null) {
			handlers = new EObjectContainmentEList<MHandler>(MHandler.class, this, BasicPackageImpl.PART_DESCRIPTOR__HANDLERS);
		}
		return handlers;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public List<String> getBindingContexts() {
		if (bindingContexts == null) {
			bindingContexts = new EDataTypeUniqueEList<String>(String.class, this, BasicPackageImpl.PART_DESCRIPTOR__BINDING_CONTEXTS);
		}
		return bindingContexts;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getElementId() {
		return elementId;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setElementId(String newElementId) {
		String oldElementId = elementId;
		elementId = newElementId;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, BasicPackageImpl.PART_DESCRIPTOR__ELEMENT_ID, oldElementId, elementId));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public List<String> getTags() {
		if (tags == null) {
			tags = new EDataTypeUniqueEList<String>(String.class, this, BasicPackageImpl.PART_DESCRIPTOR__TAGS);
		}
		return tags;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isAllowMultiple() {
		return allowMultiple;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setAllowMultiple(boolean newAllowMultiple) {
		boolean oldAllowMultiple = allowMultiple;
		allowMultiple = newAllowMultiple;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, BasicPackageImpl.PART_DESCRIPTOR__ALLOW_MULTIPLE, oldAllowMultiple, allowMultiple));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getCategory() {
		return category;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setCategory(String newCategory) {
		String oldCategory = category;
		category = newCategory;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, BasicPackageImpl.PART_DESCRIPTOR__CATEGORY, oldCategory, category));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public List<MMenu> getMenus() {
		if (menus == null) {
			menus = new EObjectContainmentEList<MMenu>(MMenu.class, this, BasicPackageImpl.PART_DESCRIPTOR__MENUS);
		}
		return menus;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MToolBar getToolbar() {
		return toolbar;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetToolbar(MToolBar newToolbar, NotificationChain msgs) {
		MToolBar oldToolbar = toolbar;
		toolbar = newToolbar;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, BasicPackageImpl.PART_DESCRIPTOR__TOOLBAR, oldToolbar, newToolbar);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setToolbar(MToolBar newToolbar) {
		if (newToolbar != toolbar) {
			NotificationChain msgs = null;
			if (toolbar != null)
				msgs = ((InternalEObject)toolbar).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - BasicPackageImpl.PART_DESCRIPTOR__TOOLBAR, null, msgs);
			if (newToolbar != null)
				msgs = ((InternalEObject)newToolbar).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - BasicPackageImpl.PART_DESCRIPTOR__TOOLBAR, null, msgs);
			msgs = basicSetToolbar(newToolbar, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, BasicPackageImpl.PART_DESCRIPTOR__TOOLBAR, newToolbar, newToolbar));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isCloseable() {
		return closeable;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setCloseable(boolean newCloseable) {
		boolean oldCloseable = closeable;
		closeable = newCloseable;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, BasicPackageImpl.PART_DESCRIPTOR__CLOSEABLE, oldCloseable, closeable));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isDirtyable() {
		return dirtyable;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setDirtyable(boolean newDirtyable) {
		boolean oldDirtyable = dirtyable;
		dirtyable = newDirtyable;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, BasicPackageImpl.PART_DESCRIPTOR__DIRTYABLE, oldDirtyable, dirtyable));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getContributionURI() {
		return contributionURI;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setContributionURI(String newContributionURI) {
		String oldContributionURI = contributionURI;
		contributionURI = newContributionURI;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, BasicPackageImpl.PART_DESCRIPTOR__CONTRIBUTION_URI, oldContributionURI, contributionURI));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case BasicPackageImpl.PART_DESCRIPTOR__HANDLERS:
				return ((InternalEList<?>)getHandlers()).basicRemove(otherEnd, msgs);
			case BasicPackageImpl.PART_DESCRIPTOR__MENUS:
				return ((InternalEList<?>)getMenus()).basicRemove(otherEnd, msgs);
			case BasicPackageImpl.PART_DESCRIPTOR__TOOLBAR:
				return basicSetToolbar(null, msgs);
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
			case BasicPackageImpl.PART_DESCRIPTOR__HANDLERS:
				return getHandlers();
			case BasicPackageImpl.PART_DESCRIPTOR__BINDING_CONTEXTS:
				return getBindingContexts();
			case BasicPackageImpl.PART_DESCRIPTOR__ELEMENT_ID:
				return getElementId();
			case BasicPackageImpl.PART_DESCRIPTOR__TAGS:
				return getTags();
			case BasicPackageImpl.PART_DESCRIPTOR__ALLOW_MULTIPLE:
				return isAllowMultiple();
			case BasicPackageImpl.PART_DESCRIPTOR__CATEGORY:
				return getCategory();
			case BasicPackageImpl.PART_DESCRIPTOR__MENUS:
				return getMenus();
			case BasicPackageImpl.PART_DESCRIPTOR__TOOLBAR:
				return getToolbar();
			case BasicPackageImpl.PART_DESCRIPTOR__CLOSEABLE:
				return isCloseable();
			case BasicPackageImpl.PART_DESCRIPTOR__DIRTYABLE:
				return isDirtyable();
			case BasicPackageImpl.PART_DESCRIPTOR__CONTRIBUTION_URI:
				return getContributionURI();
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
			case BasicPackageImpl.PART_DESCRIPTOR__HANDLERS:
				getHandlers().clear();
				getHandlers().addAll((Collection<? extends MHandler>)newValue);
				return;
			case BasicPackageImpl.PART_DESCRIPTOR__BINDING_CONTEXTS:
				getBindingContexts().clear();
				getBindingContexts().addAll((Collection<? extends String>)newValue);
				return;
			case BasicPackageImpl.PART_DESCRIPTOR__ELEMENT_ID:
				setElementId((String)newValue);
				return;
			case BasicPackageImpl.PART_DESCRIPTOR__TAGS:
				getTags().clear();
				getTags().addAll((Collection<? extends String>)newValue);
				return;
			case BasicPackageImpl.PART_DESCRIPTOR__ALLOW_MULTIPLE:
				setAllowMultiple((Boolean)newValue);
				return;
			case BasicPackageImpl.PART_DESCRIPTOR__CATEGORY:
				setCategory((String)newValue);
				return;
			case BasicPackageImpl.PART_DESCRIPTOR__MENUS:
				getMenus().clear();
				getMenus().addAll((Collection<? extends MMenu>)newValue);
				return;
			case BasicPackageImpl.PART_DESCRIPTOR__TOOLBAR:
				setToolbar((MToolBar)newValue);
				return;
			case BasicPackageImpl.PART_DESCRIPTOR__CLOSEABLE:
				setCloseable((Boolean)newValue);
				return;
			case BasicPackageImpl.PART_DESCRIPTOR__DIRTYABLE:
				setDirtyable((Boolean)newValue);
				return;
			case BasicPackageImpl.PART_DESCRIPTOR__CONTRIBUTION_URI:
				setContributionURI((String)newValue);
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
			case BasicPackageImpl.PART_DESCRIPTOR__HANDLERS:
				getHandlers().clear();
				return;
			case BasicPackageImpl.PART_DESCRIPTOR__BINDING_CONTEXTS:
				getBindingContexts().clear();
				return;
			case BasicPackageImpl.PART_DESCRIPTOR__ELEMENT_ID:
				setElementId(ELEMENT_ID_EDEFAULT);
				return;
			case BasicPackageImpl.PART_DESCRIPTOR__TAGS:
				getTags().clear();
				return;
			case BasicPackageImpl.PART_DESCRIPTOR__ALLOW_MULTIPLE:
				setAllowMultiple(ALLOW_MULTIPLE_EDEFAULT);
				return;
			case BasicPackageImpl.PART_DESCRIPTOR__CATEGORY:
				setCategory(CATEGORY_EDEFAULT);
				return;
			case BasicPackageImpl.PART_DESCRIPTOR__MENUS:
				getMenus().clear();
				return;
			case BasicPackageImpl.PART_DESCRIPTOR__TOOLBAR:
				setToolbar((MToolBar)null);
				return;
			case BasicPackageImpl.PART_DESCRIPTOR__CLOSEABLE:
				setCloseable(CLOSEABLE_EDEFAULT);
				return;
			case BasicPackageImpl.PART_DESCRIPTOR__DIRTYABLE:
				setDirtyable(DIRTYABLE_EDEFAULT);
				return;
			case BasicPackageImpl.PART_DESCRIPTOR__CONTRIBUTION_URI:
				setContributionURI(CONTRIBUTION_URI_EDEFAULT);
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
			case BasicPackageImpl.PART_DESCRIPTOR__HANDLERS:
				return handlers != null && !handlers.isEmpty();
			case BasicPackageImpl.PART_DESCRIPTOR__BINDING_CONTEXTS:
				return bindingContexts != null && !bindingContexts.isEmpty();
			case BasicPackageImpl.PART_DESCRIPTOR__ELEMENT_ID:
				return ELEMENT_ID_EDEFAULT == null ? elementId != null : !ELEMENT_ID_EDEFAULT.equals(elementId);
			case BasicPackageImpl.PART_DESCRIPTOR__TAGS:
				return tags != null && !tags.isEmpty();
			case BasicPackageImpl.PART_DESCRIPTOR__ALLOW_MULTIPLE:
				return allowMultiple != ALLOW_MULTIPLE_EDEFAULT;
			case BasicPackageImpl.PART_DESCRIPTOR__CATEGORY:
				return CATEGORY_EDEFAULT == null ? category != null : !CATEGORY_EDEFAULT.equals(category);
			case BasicPackageImpl.PART_DESCRIPTOR__MENUS:
				return menus != null && !menus.isEmpty();
			case BasicPackageImpl.PART_DESCRIPTOR__TOOLBAR:
				return toolbar != null;
			case BasicPackageImpl.PART_DESCRIPTOR__CLOSEABLE:
				return closeable != CLOSEABLE_EDEFAULT;
			case BasicPackageImpl.PART_DESCRIPTOR__DIRTYABLE:
				return dirtyable != DIRTYABLE_EDEFAULT;
			case BasicPackageImpl.PART_DESCRIPTOR__CONTRIBUTION_URI:
				return CONTRIBUTION_URI_EDEFAULT == null ? contributionURI != null : !CONTRIBUTION_URI_EDEFAULT.equals(contributionURI);
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
		if (baseClass == MHandlerContainer.class) {
			switch (derivedFeatureID) {
				case BasicPackageImpl.PART_DESCRIPTOR__HANDLERS: return CommandsPackageImpl.HANDLER_CONTAINER__HANDLERS;
				default: return -1;
			}
		}
		if (baseClass == MBindings.class) {
			switch (derivedFeatureID) {
				case BasicPackageImpl.PART_DESCRIPTOR__BINDING_CONTEXTS: return CommandsPackageImpl.BINDINGS__BINDING_CONTEXTS;
				default: return -1;
			}
		}
		if (baseClass == MApplicationElement.class) {
			switch (derivedFeatureID) {
				case BasicPackageImpl.PART_DESCRIPTOR__ELEMENT_ID: return ApplicationPackageImpl.APPLICATION_ELEMENT__ELEMENT_ID;
				case BasicPackageImpl.PART_DESCRIPTOR__TAGS: return ApplicationPackageImpl.APPLICATION_ELEMENT__TAGS;
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
		if (baseClass == MHandlerContainer.class) {
			switch (baseFeatureID) {
				case CommandsPackageImpl.HANDLER_CONTAINER__HANDLERS: return BasicPackageImpl.PART_DESCRIPTOR__HANDLERS;
				default: return -1;
			}
		}
		if (baseClass == MBindings.class) {
			switch (baseFeatureID) {
				case CommandsPackageImpl.BINDINGS__BINDING_CONTEXTS: return BasicPackageImpl.PART_DESCRIPTOR__BINDING_CONTEXTS;
				default: return -1;
			}
		}
		if (baseClass == MApplicationElement.class) {
			switch (baseFeatureID) {
				case ApplicationPackageImpl.APPLICATION_ELEMENT__ELEMENT_ID: return BasicPackageImpl.PART_DESCRIPTOR__ELEMENT_ID;
				case ApplicationPackageImpl.APPLICATION_ELEMENT__TAGS: return BasicPackageImpl.PART_DESCRIPTOR__TAGS;
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
		result.append(" (bindingContexts: "); //$NON-NLS-1$
		result.append(bindingContexts);
		result.append(", elementId: "); //$NON-NLS-1$
		result.append(elementId);
		result.append(", tags: "); //$NON-NLS-1$
		result.append(tags);
		result.append(", allowMultiple: "); //$NON-NLS-1$
		result.append(allowMultiple);
		result.append(", category: "); //$NON-NLS-1$
		result.append(category);
		result.append(", closeable: "); //$NON-NLS-1$
		result.append(closeable);
		result.append(", dirtyable: "); //$NON-NLS-1$
		result.append(dirtyable);
		result.append(", contributionURI: "); //$NON-NLS-1$
		result.append(contributionURI);
		result.append(')');
		return result.toString();
	}

} //PartDescriptorImpl
