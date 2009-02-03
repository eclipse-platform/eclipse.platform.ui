/**
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *      IBM Corporation - initial API and implementation
 *
 * $Id$
 */
package org.eclipse.e4.ui.model.internal.application;

import java.util.Collection;

import org.eclipse.e4.ui.model.application.ApplicationPackage;
import org.eclipse.e4.ui.model.application.MHandler;
import org.eclipse.e4.ui.model.application.MMenu;
import org.eclipse.e4.ui.model.application.MPart;
import org.eclipse.e4.ui.model.application.MToolBar;
import org.eclipse.e4.ui.model.application.MTrim;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.EObjectContainmentWithInverseEList;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.util.InternalEList;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>MPart</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.model.internal.application.MPartImpl#getMenu <em>Menu</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.internal.application.MPartImpl#getToolBar <em>Tool Bar</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.internal.application.MPartImpl#getPolicy <em>Policy</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.internal.application.MPartImpl#getChildren <em>Children</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.internal.application.MPartImpl#getActiveChild <em>Active Child</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.internal.application.MPartImpl#getHandlers <em>Handlers</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.internal.application.MPartImpl#getTrim <em>Trim</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.internal.application.MPartImpl#getWidget <em>Widget</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.internal.application.MPartImpl#getParent <em>Parent</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.internal.application.MPartImpl#isVisible <em>Visible</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class MPartImpl<P extends MPart<?>> extends MApplicationElementImpl implements MPart<P> {
	/**
	 * The cached value of the '{@link #getMenu() <em>Menu</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMenu()
	 * @generated
	 * @ordered
	 */
	protected MMenu menu;

	/**
	 * The cached value of the '{@link #getToolBar() <em>Tool Bar</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getToolBar()
	 * @generated
	 * @ordered
	 */
	protected MToolBar toolBar;

	/**
	 * The default value of the '{@link #getPolicy() <em>Policy</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPolicy()
	 * @generated
	 * @ordered
	 */
	protected static final String POLICY_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getPolicy() <em>Policy</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPolicy()
	 * @generated
	 * @ordered
	 */
	protected String policy = POLICY_EDEFAULT;

	/**
	 * The cached value of the '{@link #getChildren() <em>Children</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getChildren()
	 * @generated
	 * @ordered
	 */
	protected EList<P> children;

	/**
	 * The cached value of the '{@link #getActiveChild() <em>Active Child</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getActiveChild()
	 * @generated
	 * @ordered
	 */
	protected P activeChild;

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
	 * The cached value of the '{@link #getTrim() <em>Trim</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTrim()
	 * @generated
	 * @ordered
	 */
	protected MTrim trim;

	/**
	 * The default value of the '{@link #getWidget() <em>Widget</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getWidget()
	 * @generated
	 * @ordered
	 */
	protected static final Object WIDGET_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getWidget() <em>Widget</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getWidget()
	 * @generated
	 * @ordered
	 */
	protected Object widget = WIDGET_EDEFAULT;

	/**
	 * The default value of the '{@link #isVisible() <em>Visible</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isVisible()
	 * @generated
	 * @ordered
	 */
	protected static final boolean VISIBLE_EDEFAULT = true;

	/**
	 * The flag representing the value of the '{@link #isVisible() <em>Visible</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isVisible()
	 * @generated
	 * @ordered
	 */
	protected static final int VISIBLE_EFLAG = 1 << 8;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected MPartImpl() {
		super();
		eFlags |= VISIBLE_EFLAG;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ApplicationPackage.Literals.MPART;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MMenu getMenu() {
		return menu;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetMenu(MMenu newMenu, NotificationChain msgs) {
		MMenu oldMenu = menu;
		menu = newMenu;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, ApplicationPackage.MPART__MENU, oldMenu, newMenu);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setMenu(MMenu newMenu) {
		if (newMenu != menu) {
			NotificationChain msgs = null;
			if (menu != null)
				msgs = ((InternalEObject)menu).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - ApplicationPackage.MPART__MENU, null, msgs);
			if (newMenu != null)
				msgs = ((InternalEObject)newMenu).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - ApplicationPackage.MPART__MENU, null, msgs);
			msgs = basicSetMenu(newMenu, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ApplicationPackage.MPART__MENU, newMenu, newMenu));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MToolBar getToolBar() {
		return toolBar;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetToolBar(MToolBar newToolBar, NotificationChain msgs) {
		MToolBar oldToolBar = toolBar;
		toolBar = newToolBar;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, ApplicationPackage.MPART__TOOL_BAR, oldToolBar, newToolBar);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setToolBar(MToolBar newToolBar) {
		if (newToolBar != toolBar) {
			NotificationChain msgs = null;
			if (toolBar != null)
				msgs = ((InternalEObject)toolBar).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - ApplicationPackage.MPART__TOOL_BAR, null, msgs);
			if (newToolBar != null)
				msgs = ((InternalEObject)newToolBar).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - ApplicationPackage.MPART__TOOL_BAR, null, msgs);
			msgs = basicSetToolBar(newToolBar, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ApplicationPackage.MPART__TOOL_BAR, newToolBar, newToolBar));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getPolicy() {
		return policy;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setPolicy(String newPolicy) {
		String oldPolicy = policy;
		policy = newPolicy;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ApplicationPackage.MPART__POLICY, oldPolicy, policy));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<P> getChildren() {
		if (children == null) {
			children = new EObjectContainmentWithInverseEList<P>(MPart.class, this, ApplicationPackage.MPART__CHILDREN, ApplicationPackage.MPART__PARENT);
		}
		return children;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	public P getActiveChild() {
		if (activeChild != null && activeChild.eIsProxy()) {
			InternalEObject oldActiveChild = (InternalEObject)activeChild;
			activeChild = (P)eResolveProxy(oldActiveChild);
			if (activeChild != oldActiveChild) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, ApplicationPackage.MPART__ACTIVE_CHILD, oldActiveChild, activeChild));
			}
		}
		return activeChild;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public P basicGetActiveChild() {
		return activeChild;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setActiveChild(P newActiveChild) {
		P oldActiveChild = activeChild;
		activeChild = newActiveChild;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ApplicationPackage.MPART__ACTIVE_CHILD, oldActiveChild, activeChild));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<MHandler> getHandlers() {
		if (handlers == null) {
			handlers = new EObjectContainmentEList<MHandler>(MHandler.class, this, ApplicationPackage.MPART__HANDLERS);
		}
		return handlers;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MTrim getTrim() {
		return trim;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetTrim(MTrim newTrim, NotificationChain msgs) {
		MTrim oldTrim = trim;
		trim = newTrim;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, ApplicationPackage.MPART__TRIM, oldTrim, newTrim);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setTrim(MTrim newTrim) {
		if (newTrim != trim) {
			NotificationChain msgs = null;
			if (trim != null)
				msgs = ((InternalEObject)trim).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - ApplicationPackage.MPART__TRIM, null, msgs);
			if (newTrim != null)
				msgs = ((InternalEObject)newTrim).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - ApplicationPackage.MPART__TRIM, null, msgs);
			msgs = basicSetTrim(newTrim, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ApplicationPackage.MPART__TRIM, newTrim, newTrim));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Object getWidget() {
		return widget;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setWidget(Object newWidget) {
		Object oldWidget = widget;
		widget = newWidget;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ApplicationPackage.MPART__WIDGET, oldWidget, widget));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MPart<?> getParent() {
		if (eContainerFeatureID() != ApplicationPackage.MPART__PARENT) return null;
		return (MPart<?>)eContainer();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetParent(MPart<?> newParent, NotificationChain msgs) {
		msgs = eBasicSetContainer((InternalEObject)newParent, ApplicationPackage.MPART__PARENT, msgs);
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setParent(MPart<?> newParent) {
		if (newParent != eInternalContainer() || (eContainerFeatureID() != ApplicationPackage.MPART__PARENT && newParent != null)) {
			if (EcoreUtil.isAncestor(this, newParent))
				throw new IllegalArgumentException("Recursive containment not allowed for " + toString()); //$NON-NLS-1$
			NotificationChain msgs = null;
			if (eInternalContainer() != null)
				msgs = eBasicRemoveFromContainer(msgs);
			if (newParent != null)
				msgs = ((InternalEObject)newParent).eInverseAdd(this, ApplicationPackage.MPART__CHILDREN, MPart.class, msgs);
			msgs = basicSetParent(newParent, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ApplicationPackage.MPART__PARENT, newParent, newParent));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isVisible() {
		return (eFlags & VISIBLE_EFLAG) != 0;
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
			case ApplicationPackage.MPART__CHILDREN:
				return ((InternalEList<InternalEObject>)(InternalEList<?>)getChildren()).basicAdd(otherEnd, msgs);
			case ApplicationPackage.MPART__PARENT:
				if (eInternalContainer() != null)
					msgs = eBasicRemoveFromContainer(msgs);
				return basicSetParent((MPart<?>)otherEnd, msgs);
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
			case ApplicationPackage.MPART__MENU:
				return basicSetMenu(null, msgs);
			case ApplicationPackage.MPART__TOOL_BAR:
				return basicSetToolBar(null, msgs);
			case ApplicationPackage.MPART__CHILDREN:
				return ((InternalEList<?>)getChildren()).basicRemove(otherEnd, msgs);
			case ApplicationPackage.MPART__HANDLERS:
				return ((InternalEList<?>)getHandlers()).basicRemove(otherEnd, msgs);
			case ApplicationPackage.MPART__TRIM:
				return basicSetTrim(null, msgs);
			case ApplicationPackage.MPART__PARENT:
				return basicSetParent(null, msgs);
		}
		return super.eInverseRemove(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eBasicRemoveFromContainerFeature(NotificationChain msgs) {
		switch (eContainerFeatureID()) {
			case ApplicationPackage.MPART__PARENT:
				return eInternalContainer().eInverseRemove(this, ApplicationPackage.MPART__CHILDREN, MPart.class, msgs);
		}
		return super.eBasicRemoveFromContainerFeature(msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case ApplicationPackage.MPART__MENU:
				return getMenu();
			case ApplicationPackage.MPART__TOOL_BAR:
				return getToolBar();
			case ApplicationPackage.MPART__POLICY:
				return getPolicy();
			case ApplicationPackage.MPART__CHILDREN:
				return getChildren();
			case ApplicationPackage.MPART__ACTIVE_CHILD:
				if (resolve) return getActiveChild();
				return basicGetActiveChild();
			case ApplicationPackage.MPART__HANDLERS:
				return getHandlers();
			case ApplicationPackage.MPART__TRIM:
				return getTrim();
			case ApplicationPackage.MPART__WIDGET:
				return getWidget();
			case ApplicationPackage.MPART__PARENT:
				return getParent();
			case ApplicationPackage.MPART__VISIBLE:
				return isVisible();
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
			case ApplicationPackage.MPART__MENU:
				setMenu((MMenu)newValue);
				return;
			case ApplicationPackage.MPART__TOOL_BAR:
				setToolBar((MToolBar)newValue);
				return;
			case ApplicationPackage.MPART__POLICY:
				setPolicy((String)newValue);
				return;
			case ApplicationPackage.MPART__CHILDREN:
				getChildren().clear();
				getChildren().addAll((Collection<? extends P>)newValue);
				return;
			case ApplicationPackage.MPART__ACTIVE_CHILD:
				setActiveChild((P)newValue);
				return;
			case ApplicationPackage.MPART__HANDLERS:
				getHandlers().clear();
				getHandlers().addAll((Collection<? extends MHandler>)newValue);
				return;
			case ApplicationPackage.MPART__TRIM:
				setTrim((MTrim)newValue);
				return;
			case ApplicationPackage.MPART__WIDGET:
				setWidget(newValue);
				return;
			case ApplicationPackage.MPART__PARENT:
				setParent((MPart<?>)newValue);
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
			case ApplicationPackage.MPART__MENU:
				setMenu((MMenu)null);
				return;
			case ApplicationPackage.MPART__TOOL_BAR:
				setToolBar((MToolBar)null);
				return;
			case ApplicationPackage.MPART__POLICY:
				setPolicy(POLICY_EDEFAULT);
				return;
			case ApplicationPackage.MPART__CHILDREN:
				getChildren().clear();
				return;
			case ApplicationPackage.MPART__ACTIVE_CHILD:
				setActiveChild((P)null);
				return;
			case ApplicationPackage.MPART__HANDLERS:
				getHandlers().clear();
				return;
			case ApplicationPackage.MPART__TRIM:
				setTrim((MTrim)null);
				return;
			case ApplicationPackage.MPART__WIDGET:
				setWidget(WIDGET_EDEFAULT);
				return;
			case ApplicationPackage.MPART__PARENT:
				setParent((MPart<?>)null);
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
			case ApplicationPackage.MPART__MENU:
				return menu != null;
			case ApplicationPackage.MPART__TOOL_BAR:
				return toolBar != null;
			case ApplicationPackage.MPART__POLICY:
				return POLICY_EDEFAULT == null ? policy != null : !POLICY_EDEFAULT.equals(policy);
			case ApplicationPackage.MPART__CHILDREN:
				return children != null && !children.isEmpty();
			case ApplicationPackage.MPART__ACTIVE_CHILD:
				return activeChild != null;
			case ApplicationPackage.MPART__HANDLERS:
				return handlers != null && !handlers.isEmpty();
			case ApplicationPackage.MPART__TRIM:
				return trim != null;
			case ApplicationPackage.MPART__WIDGET:
				return WIDGET_EDEFAULT == null ? widget != null : !WIDGET_EDEFAULT.equals(widget);
			case ApplicationPackage.MPART__PARENT:
				return getParent() != null;
			case ApplicationPackage.MPART__VISIBLE:
				return ((eFlags & VISIBLE_EFLAG) != 0) != VISIBLE_EDEFAULT;
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
		result.append(" (policy: "); //$NON-NLS-1$
		result.append(policy);
		result.append(", widget: "); //$NON-NLS-1$
		result.append(widget);
		result.append(", visible: "); //$NON-NLS-1$
		result.append((eFlags & VISIBLE_EFLAG) != 0);
		result.append(')');
		return result.toString();
	}

} //MPartImpl
