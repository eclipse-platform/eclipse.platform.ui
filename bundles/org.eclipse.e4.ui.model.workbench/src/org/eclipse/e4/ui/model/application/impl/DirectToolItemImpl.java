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
package org.eclipse.e4.ui.model.application.impl;

import java.util.Collection;

import org.eclipse.e4.ui.model.application.ItemType;
import org.eclipse.e4.ui.model.application.MApplicationPackage;
import org.eclipse.e4.ui.model.application.MDirectToolItem;
import org.eclipse.e4.ui.model.application.MElementContainer;
import org.eclipse.e4.ui.model.application.MItem;
import org.eclipse.e4.ui.model.application.MMenuItem;
import org.eclipse.e4.ui.model.application.MToolItem;
import org.eclipse.e4.ui.model.application.MUIElement;
import org.eclipse.e4.ui.model.application.MUILabel;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

import org.eclipse.emf.ecore.util.EObjectContainmentWithInverseEList;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.util.InternalEList;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Direct Tool Item</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.DirectToolItemImpl#getWidget <em>Widget</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.DirectToolItemImpl#getRenderer <em>Renderer</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.DirectToolItemImpl#isToBeRendered <em>To Be Rendered</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.DirectToolItemImpl#isOnTop <em>On Top</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.DirectToolItemImpl#isVisible <em>Visible</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.DirectToolItemImpl#getParent <em>Parent</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.DirectToolItemImpl#getContainerData <em>Container Data</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.DirectToolItemImpl#getLabel <em>Label</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.DirectToolItemImpl#getIconURI <em>Icon URI</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.DirectToolItemImpl#getTooltip <em>Tooltip</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.DirectToolItemImpl#isEnabled <em>Enabled</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.DirectToolItemImpl#isSelected <em>Selected</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.DirectToolItemImpl#getType <em>Type</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.DirectToolItemImpl#getChildren <em>Children</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.DirectToolItemImpl#getActiveChild <em>Active Child</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class DirectToolItemImpl extends ContributionImpl implements MDirectToolItem {
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
	 * The default value of the '{@link #getRenderer() <em>Renderer</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getRenderer()
	 * @generated
	 * @ordered
	 */
	protected static final Object RENDERER_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getRenderer() <em>Renderer</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getRenderer()
	 * @generated
	 * @ordered
	 */
	protected Object renderer = RENDERER_EDEFAULT;

	/**
	 * The default value of the '{@link #isToBeRendered() <em>To Be Rendered</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isToBeRendered()
	 * @generated
	 * @ordered
	 */
	protected static final boolean TO_BE_RENDERED_EDEFAULT = true;

	/**
	 * The cached value of the '{@link #isToBeRendered() <em>To Be Rendered</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isToBeRendered()
	 * @generated
	 * @ordered
	 */
	protected boolean toBeRendered = TO_BE_RENDERED_EDEFAULT;

	/**
	 * The default value of the '{@link #isOnTop() <em>On Top</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isOnTop()
	 * @generated
	 * @ordered
	 */
	protected static final boolean ON_TOP_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isOnTop() <em>On Top</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isOnTop()
	 * @generated
	 * @ordered
	 */
	protected boolean onTop = ON_TOP_EDEFAULT;

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
	 * The cached value of the '{@link #isVisible() <em>Visible</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isVisible()
	 * @generated
	 * @ordered
	 */
	protected boolean visible = VISIBLE_EDEFAULT;

	/**
	 * The default value of the '{@link #getContainerData() <em>Container Data</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getContainerData()
	 * @generated
	 * @ordered
	 */
	protected static final String CONTAINER_DATA_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getContainerData() <em>Container Data</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getContainerData()
	 * @generated
	 * @ordered
	 */
	protected String containerData = CONTAINER_DATA_EDEFAULT;

	/**
	 * The default value of the '{@link #getLabel() <em>Label</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLabel()
	 * @generated
	 * @ordered
	 */
	protected static final String LABEL_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getLabel() <em>Label</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLabel()
	 * @generated
	 * @ordered
	 */
	protected String label = LABEL_EDEFAULT;

	/**
	 * The default value of the '{@link #getIconURI() <em>Icon URI</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getIconURI()
	 * @generated
	 * @ordered
	 */
	protected static final String ICON_URI_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getIconURI() <em>Icon URI</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getIconURI()
	 * @generated
	 * @ordered
	 */
	protected String iconURI = ICON_URI_EDEFAULT;

	/**
	 * The default value of the '{@link #getTooltip() <em>Tooltip</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTooltip()
	 * @generated
	 * @ordered
	 */
	protected static final String TOOLTIP_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getTooltip() <em>Tooltip</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTooltip()
	 * @generated
	 * @ordered
	 */
	protected String tooltip = TOOLTIP_EDEFAULT;

	/**
	 * The default value of the '{@link #isEnabled() <em>Enabled</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isEnabled()
	 * @generated
	 * @ordered
	 */
	protected static final boolean ENABLED_EDEFAULT = true;

	/**
	 * The cached value of the '{@link #isEnabled() <em>Enabled</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isEnabled()
	 * @generated
	 * @ordered
	 */
	protected boolean enabled = ENABLED_EDEFAULT;

	/**
	 * The default value of the '{@link #isSelected() <em>Selected</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSelected()
	 * @generated
	 * @ordered
	 */
	protected static final boolean SELECTED_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isSelected() <em>Selected</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSelected()
	 * @generated
	 * @ordered
	 */
	protected boolean selected = SELECTED_EDEFAULT;

	/**
	 * The default value of the '{@link #getType() <em>Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getType()
	 * @generated
	 * @ordered
	 */
	protected static final ItemType TYPE_EDEFAULT = ItemType.PUSH;

	/**
	 * The cached value of the '{@link #getType() <em>Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getType()
	 * @generated
	 * @ordered
	 */
	protected ItemType type = TYPE_EDEFAULT;

	/**
	 * The cached value of the '{@link #getChildren() <em>Children</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getChildren()
	 * @generated
	 * @ordered
	 */
	protected EList<MMenuItem> children;

	/**
	 * The cached value of the '{@link #getActiveChild() <em>Active Child</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getActiveChild()
	 * @generated
	 * @ordered
	 */
	protected MMenuItem activeChild;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected DirectToolItemImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return MApplicationPackage.Literals.DIRECT_TOOL_ITEM;
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
			eNotify(new ENotificationImpl(this, Notification.SET, MApplicationPackage.DIRECT_TOOL_ITEM__WIDGET, oldWidget, widget));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Object getRenderer() {
		return renderer;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setRenderer(Object newRenderer) {
		Object oldRenderer = renderer;
		renderer = newRenderer;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, MApplicationPackage.DIRECT_TOOL_ITEM__RENDERER, oldRenderer, renderer));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isToBeRendered() {
		return toBeRendered;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setToBeRendered(boolean newToBeRendered) {
		boolean oldToBeRendered = toBeRendered;
		toBeRendered = newToBeRendered;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, MApplicationPackage.DIRECT_TOOL_ITEM__TO_BE_RENDERED, oldToBeRendered, toBeRendered));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isOnTop() {
		return onTop;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setOnTop(boolean newOnTop) {
		boolean oldOnTop = onTop;
		onTop = newOnTop;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, MApplicationPackage.DIRECT_TOOL_ITEM__ON_TOP, oldOnTop, onTop));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isVisible() {
		return visible;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setVisible(boolean newVisible) {
		boolean oldVisible = visible;
		visible = newVisible;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, MApplicationPackage.DIRECT_TOOL_ITEM__VISIBLE, oldVisible, visible));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	public MElementContainer<MUIElement> getParent() {
		if (eContainerFeatureID() != MApplicationPackage.DIRECT_TOOL_ITEM__PARENT) return null;
		return (MElementContainer<MUIElement>)eContainer();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetParent(MElementContainer<MUIElement> newParent, NotificationChain msgs) {
		msgs = eBasicSetContainer((InternalEObject)newParent, MApplicationPackage.DIRECT_TOOL_ITEM__PARENT, msgs);
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setParent(MElementContainer<MUIElement> newParent) {
		if (newParent != eInternalContainer() || (eContainerFeatureID() != MApplicationPackage.DIRECT_TOOL_ITEM__PARENT && newParent != null)) {
			if (EcoreUtil.isAncestor(this, (EObject)newParent))
				throw new IllegalArgumentException("Recursive containment not allowed for " + toString()); //$NON-NLS-1$
			NotificationChain msgs = null;
			if (eInternalContainer() != null)
				msgs = eBasicRemoveFromContainer(msgs);
			if (newParent != null)
				msgs = ((InternalEObject)newParent).eInverseAdd(this, MApplicationPackage.ELEMENT_CONTAINER__CHILDREN, MElementContainer.class, msgs);
			msgs = basicSetParent(newParent, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, MApplicationPackage.DIRECT_TOOL_ITEM__PARENT, newParent, newParent));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getContainerData() {
		return containerData;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setContainerData(String newContainerData) {
		String oldContainerData = containerData;
		containerData = newContainerData;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, MApplicationPackage.DIRECT_TOOL_ITEM__CONTAINER_DATA, oldContainerData, containerData));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setLabel(String newLabel) {
		String oldLabel = label;
		label = newLabel;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, MApplicationPackage.DIRECT_TOOL_ITEM__LABEL, oldLabel, label));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getIconURI() {
		return iconURI;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setIconURI(String newIconURI) {
		String oldIconURI = iconURI;
		iconURI = newIconURI;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, MApplicationPackage.DIRECT_TOOL_ITEM__ICON_URI, oldIconURI, iconURI));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getTooltip() {
		return tooltip;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setTooltip(String newTooltip) {
		String oldTooltip = tooltip;
		tooltip = newTooltip;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, MApplicationPackage.DIRECT_TOOL_ITEM__TOOLTIP, oldTooltip, tooltip));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setEnabled(boolean newEnabled) {
		boolean oldEnabled = enabled;
		enabled = newEnabled;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, MApplicationPackage.DIRECT_TOOL_ITEM__ENABLED, oldEnabled, enabled));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSelected() {
		return selected;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setSelected(boolean newSelected) {
		boolean oldSelected = selected;
		selected = newSelected;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, MApplicationPackage.DIRECT_TOOL_ITEM__SELECTED, oldSelected, selected));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ItemType getType() {
		return type;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setType(ItemType newType) {
		ItemType oldType = type;
		type = newType == null ? TYPE_EDEFAULT : newType;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, MApplicationPackage.DIRECT_TOOL_ITEM__TYPE, oldType, type));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<MMenuItem> getChildren() {
		if (children == null) {
			children = new EObjectContainmentWithInverseEList<MMenuItem>(MUIElement.class, this, MApplicationPackage.DIRECT_TOOL_ITEM__CHILDREN, MApplicationPackage.UI_ELEMENT__PARENT);
		}
		return children;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MMenuItem getActiveChild() {
		if (activeChild != null && ((EObject)activeChild).eIsProxy()) {
			InternalEObject oldActiveChild = (InternalEObject)activeChild;
			activeChild = (MMenuItem)eResolveProxy(oldActiveChild);
			if (activeChild != oldActiveChild) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, MApplicationPackage.DIRECT_TOOL_ITEM__ACTIVE_CHILD, oldActiveChild, activeChild));
			}
		}
		return activeChild;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MMenuItem basicGetActiveChild() {
		return activeChild;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setActiveChild(MMenuItem newActiveChild) {
		MMenuItem oldActiveChild = activeChild;
		activeChild = newActiveChild;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, MApplicationPackage.DIRECT_TOOL_ITEM__ACTIVE_CHILD, oldActiveChild, activeChild));
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
			case MApplicationPackage.DIRECT_TOOL_ITEM__PARENT:
				if (eInternalContainer() != null)
					msgs = eBasicRemoveFromContainer(msgs);
				return basicSetParent((MElementContainer<MUIElement>)otherEnd, msgs);
			case MApplicationPackage.DIRECT_TOOL_ITEM__CHILDREN:
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
			case MApplicationPackage.DIRECT_TOOL_ITEM__PARENT:
				return basicSetParent(null, msgs);
			case MApplicationPackage.DIRECT_TOOL_ITEM__CHILDREN:
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
	public NotificationChain eBasicRemoveFromContainerFeature(NotificationChain msgs) {
		switch (eContainerFeatureID()) {
			case MApplicationPackage.DIRECT_TOOL_ITEM__PARENT:
				return eInternalContainer().eInverseRemove(this, MApplicationPackage.ELEMENT_CONTAINER__CHILDREN, MElementContainer.class, msgs);
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
			case MApplicationPackage.DIRECT_TOOL_ITEM__WIDGET:
				return getWidget();
			case MApplicationPackage.DIRECT_TOOL_ITEM__RENDERER:
				return getRenderer();
			case MApplicationPackage.DIRECT_TOOL_ITEM__TO_BE_RENDERED:
				return isToBeRendered();
			case MApplicationPackage.DIRECT_TOOL_ITEM__ON_TOP:
				return isOnTop();
			case MApplicationPackage.DIRECT_TOOL_ITEM__VISIBLE:
				return isVisible();
			case MApplicationPackage.DIRECT_TOOL_ITEM__PARENT:
				return getParent();
			case MApplicationPackage.DIRECT_TOOL_ITEM__CONTAINER_DATA:
				return getContainerData();
			case MApplicationPackage.DIRECT_TOOL_ITEM__LABEL:
				return getLabel();
			case MApplicationPackage.DIRECT_TOOL_ITEM__ICON_URI:
				return getIconURI();
			case MApplicationPackage.DIRECT_TOOL_ITEM__TOOLTIP:
				return getTooltip();
			case MApplicationPackage.DIRECT_TOOL_ITEM__ENABLED:
				return isEnabled();
			case MApplicationPackage.DIRECT_TOOL_ITEM__SELECTED:
				return isSelected();
			case MApplicationPackage.DIRECT_TOOL_ITEM__TYPE:
				return getType();
			case MApplicationPackage.DIRECT_TOOL_ITEM__CHILDREN:
				return getChildren();
			case MApplicationPackage.DIRECT_TOOL_ITEM__ACTIVE_CHILD:
				if (resolve) return getActiveChild();
				return basicGetActiveChild();
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
			case MApplicationPackage.DIRECT_TOOL_ITEM__WIDGET:
				setWidget(newValue);
				return;
			case MApplicationPackage.DIRECT_TOOL_ITEM__RENDERER:
				setRenderer(newValue);
				return;
			case MApplicationPackage.DIRECT_TOOL_ITEM__TO_BE_RENDERED:
				setToBeRendered((Boolean)newValue);
				return;
			case MApplicationPackage.DIRECT_TOOL_ITEM__ON_TOP:
				setOnTop((Boolean)newValue);
				return;
			case MApplicationPackage.DIRECT_TOOL_ITEM__VISIBLE:
				setVisible((Boolean)newValue);
				return;
			case MApplicationPackage.DIRECT_TOOL_ITEM__PARENT:
				setParent((MElementContainer<MUIElement>)newValue);
				return;
			case MApplicationPackage.DIRECT_TOOL_ITEM__CONTAINER_DATA:
				setContainerData((String)newValue);
				return;
			case MApplicationPackage.DIRECT_TOOL_ITEM__LABEL:
				setLabel((String)newValue);
				return;
			case MApplicationPackage.DIRECT_TOOL_ITEM__ICON_URI:
				setIconURI((String)newValue);
				return;
			case MApplicationPackage.DIRECT_TOOL_ITEM__TOOLTIP:
				setTooltip((String)newValue);
				return;
			case MApplicationPackage.DIRECT_TOOL_ITEM__ENABLED:
				setEnabled((Boolean)newValue);
				return;
			case MApplicationPackage.DIRECT_TOOL_ITEM__SELECTED:
				setSelected((Boolean)newValue);
				return;
			case MApplicationPackage.DIRECT_TOOL_ITEM__TYPE:
				setType((ItemType)newValue);
				return;
			case MApplicationPackage.DIRECT_TOOL_ITEM__CHILDREN:
				getChildren().clear();
				getChildren().addAll((Collection<? extends MMenuItem>)newValue);
				return;
			case MApplicationPackage.DIRECT_TOOL_ITEM__ACTIVE_CHILD:
				setActiveChild((MMenuItem)newValue);
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
			case MApplicationPackage.DIRECT_TOOL_ITEM__WIDGET:
				setWidget(WIDGET_EDEFAULT);
				return;
			case MApplicationPackage.DIRECT_TOOL_ITEM__RENDERER:
				setRenderer(RENDERER_EDEFAULT);
				return;
			case MApplicationPackage.DIRECT_TOOL_ITEM__TO_BE_RENDERED:
				setToBeRendered(TO_BE_RENDERED_EDEFAULT);
				return;
			case MApplicationPackage.DIRECT_TOOL_ITEM__ON_TOP:
				setOnTop(ON_TOP_EDEFAULT);
				return;
			case MApplicationPackage.DIRECT_TOOL_ITEM__VISIBLE:
				setVisible(VISIBLE_EDEFAULT);
				return;
			case MApplicationPackage.DIRECT_TOOL_ITEM__PARENT:
				setParent((MElementContainer<MUIElement>)null);
				return;
			case MApplicationPackage.DIRECT_TOOL_ITEM__CONTAINER_DATA:
				setContainerData(CONTAINER_DATA_EDEFAULT);
				return;
			case MApplicationPackage.DIRECT_TOOL_ITEM__LABEL:
				setLabel(LABEL_EDEFAULT);
				return;
			case MApplicationPackage.DIRECT_TOOL_ITEM__ICON_URI:
				setIconURI(ICON_URI_EDEFAULT);
				return;
			case MApplicationPackage.DIRECT_TOOL_ITEM__TOOLTIP:
				setTooltip(TOOLTIP_EDEFAULT);
				return;
			case MApplicationPackage.DIRECT_TOOL_ITEM__ENABLED:
				setEnabled(ENABLED_EDEFAULT);
				return;
			case MApplicationPackage.DIRECT_TOOL_ITEM__SELECTED:
				setSelected(SELECTED_EDEFAULT);
				return;
			case MApplicationPackage.DIRECT_TOOL_ITEM__TYPE:
				setType(TYPE_EDEFAULT);
				return;
			case MApplicationPackage.DIRECT_TOOL_ITEM__CHILDREN:
				getChildren().clear();
				return;
			case MApplicationPackage.DIRECT_TOOL_ITEM__ACTIVE_CHILD:
				setActiveChild((MMenuItem)null);
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
			case MApplicationPackage.DIRECT_TOOL_ITEM__WIDGET:
				return WIDGET_EDEFAULT == null ? widget != null : !WIDGET_EDEFAULT.equals(widget);
			case MApplicationPackage.DIRECT_TOOL_ITEM__RENDERER:
				return RENDERER_EDEFAULT == null ? renderer != null : !RENDERER_EDEFAULT.equals(renderer);
			case MApplicationPackage.DIRECT_TOOL_ITEM__TO_BE_RENDERED:
				return toBeRendered != TO_BE_RENDERED_EDEFAULT;
			case MApplicationPackage.DIRECT_TOOL_ITEM__ON_TOP:
				return onTop != ON_TOP_EDEFAULT;
			case MApplicationPackage.DIRECT_TOOL_ITEM__VISIBLE:
				return visible != VISIBLE_EDEFAULT;
			case MApplicationPackage.DIRECT_TOOL_ITEM__PARENT:
				return getParent() != null;
			case MApplicationPackage.DIRECT_TOOL_ITEM__CONTAINER_DATA:
				return CONTAINER_DATA_EDEFAULT == null ? containerData != null : !CONTAINER_DATA_EDEFAULT.equals(containerData);
			case MApplicationPackage.DIRECT_TOOL_ITEM__LABEL:
				return LABEL_EDEFAULT == null ? label != null : !LABEL_EDEFAULT.equals(label);
			case MApplicationPackage.DIRECT_TOOL_ITEM__ICON_URI:
				return ICON_URI_EDEFAULT == null ? iconURI != null : !ICON_URI_EDEFAULT.equals(iconURI);
			case MApplicationPackage.DIRECT_TOOL_ITEM__TOOLTIP:
				return TOOLTIP_EDEFAULT == null ? tooltip != null : !TOOLTIP_EDEFAULT.equals(tooltip);
			case MApplicationPackage.DIRECT_TOOL_ITEM__ENABLED:
				return enabled != ENABLED_EDEFAULT;
			case MApplicationPackage.DIRECT_TOOL_ITEM__SELECTED:
				return selected != SELECTED_EDEFAULT;
			case MApplicationPackage.DIRECT_TOOL_ITEM__TYPE:
				return type != TYPE_EDEFAULT;
			case MApplicationPackage.DIRECT_TOOL_ITEM__CHILDREN:
				return children != null && !children.isEmpty();
			case MApplicationPackage.DIRECT_TOOL_ITEM__ACTIVE_CHILD:
				return activeChild != null;
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
		if (baseClass == MUIElement.class) {
			switch (derivedFeatureID) {
				case MApplicationPackage.DIRECT_TOOL_ITEM__WIDGET: return MApplicationPackage.UI_ELEMENT__WIDGET;
				case MApplicationPackage.DIRECT_TOOL_ITEM__RENDERER: return MApplicationPackage.UI_ELEMENT__RENDERER;
				case MApplicationPackage.DIRECT_TOOL_ITEM__TO_BE_RENDERED: return MApplicationPackage.UI_ELEMENT__TO_BE_RENDERED;
				case MApplicationPackage.DIRECT_TOOL_ITEM__ON_TOP: return MApplicationPackage.UI_ELEMENT__ON_TOP;
				case MApplicationPackage.DIRECT_TOOL_ITEM__VISIBLE: return MApplicationPackage.UI_ELEMENT__VISIBLE;
				case MApplicationPackage.DIRECT_TOOL_ITEM__PARENT: return MApplicationPackage.UI_ELEMENT__PARENT;
				case MApplicationPackage.DIRECT_TOOL_ITEM__CONTAINER_DATA: return MApplicationPackage.UI_ELEMENT__CONTAINER_DATA;
				default: return -1;
			}
		}
		if (baseClass == MUILabel.class) {
			switch (derivedFeatureID) {
				case MApplicationPackage.DIRECT_TOOL_ITEM__LABEL: return MApplicationPackage.UI_LABEL__LABEL;
				case MApplicationPackage.DIRECT_TOOL_ITEM__ICON_URI: return MApplicationPackage.UI_LABEL__ICON_URI;
				case MApplicationPackage.DIRECT_TOOL_ITEM__TOOLTIP: return MApplicationPackage.UI_LABEL__TOOLTIP;
				default: return -1;
			}
		}
		if (baseClass == MItem.class) {
			switch (derivedFeatureID) {
				case MApplicationPackage.DIRECT_TOOL_ITEM__ENABLED: return MApplicationPackage.ITEM__ENABLED;
				case MApplicationPackage.DIRECT_TOOL_ITEM__SELECTED: return MApplicationPackage.ITEM__SELECTED;
				case MApplicationPackage.DIRECT_TOOL_ITEM__TYPE: return MApplicationPackage.ITEM__TYPE;
				default: return -1;
			}
		}
		if (baseClass == MElementContainer.class) {
			switch (derivedFeatureID) {
				case MApplicationPackage.DIRECT_TOOL_ITEM__CHILDREN: return MApplicationPackage.ELEMENT_CONTAINER__CHILDREN;
				case MApplicationPackage.DIRECT_TOOL_ITEM__ACTIVE_CHILD: return MApplicationPackage.ELEMENT_CONTAINER__ACTIVE_CHILD;
				default: return -1;
			}
		}
		if (baseClass == MToolItem.class) {
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
		if (baseClass == MUIElement.class) {
			switch (baseFeatureID) {
				case MApplicationPackage.UI_ELEMENT__WIDGET: return MApplicationPackage.DIRECT_TOOL_ITEM__WIDGET;
				case MApplicationPackage.UI_ELEMENT__RENDERER: return MApplicationPackage.DIRECT_TOOL_ITEM__RENDERER;
				case MApplicationPackage.UI_ELEMENT__TO_BE_RENDERED: return MApplicationPackage.DIRECT_TOOL_ITEM__TO_BE_RENDERED;
				case MApplicationPackage.UI_ELEMENT__ON_TOP: return MApplicationPackage.DIRECT_TOOL_ITEM__ON_TOP;
				case MApplicationPackage.UI_ELEMENT__VISIBLE: return MApplicationPackage.DIRECT_TOOL_ITEM__VISIBLE;
				case MApplicationPackage.UI_ELEMENT__PARENT: return MApplicationPackage.DIRECT_TOOL_ITEM__PARENT;
				case MApplicationPackage.UI_ELEMENT__CONTAINER_DATA: return MApplicationPackage.DIRECT_TOOL_ITEM__CONTAINER_DATA;
				default: return -1;
			}
		}
		if (baseClass == MUILabel.class) {
			switch (baseFeatureID) {
				case MApplicationPackage.UI_LABEL__LABEL: return MApplicationPackage.DIRECT_TOOL_ITEM__LABEL;
				case MApplicationPackage.UI_LABEL__ICON_URI: return MApplicationPackage.DIRECT_TOOL_ITEM__ICON_URI;
				case MApplicationPackage.UI_LABEL__TOOLTIP: return MApplicationPackage.DIRECT_TOOL_ITEM__TOOLTIP;
				default: return -1;
			}
		}
		if (baseClass == MItem.class) {
			switch (baseFeatureID) {
				case MApplicationPackage.ITEM__ENABLED: return MApplicationPackage.DIRECT_TOOL_ITEM__ENABLED;
				case MApplicationPackage.ITEM__SELECTED: return MApplicationPackage.DIRECT_TOOL_ITEM__SELECTED;
				case MApplicationPackage.ITEM__TYPE: return MApplicationPackage.DIRECT_TOOL_ITEM__TYPE;
				default: return -1;
			}
		}
		if (baseClass == MElementContainer.class) {
			switch (baseFeatureID) {
				case MApplicationPackage.ELEMENT_CONTAINER__CHILDREN: return MApplicationPackage.DIRECT_TOOL_ITEM__CHILDREN;
				case MApplicationPackage.ELEMENT_CONTAINER__ACTIVE_CHILD: return MApplicationPackage.DIRECT_TOOL_ITEM__ACTIVE_CHILD;
				default: return -1;
			}
		}
		if (baseClass == MToolItem.class) {
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
		result.append(" (widget: "); //$NON-NLS-1$
		result.append(widget);
		result.append(", renderer: "); //$NON-NLS-1$
		result.append(renderer);
		result.append(", toBeRendered: "); //$NON-NLS-1$
		result.append(toBeRendered);
		result.append(", onTop: "); //$NON-NLS-1$
		result.append(onTop);
		result.append(", visible: "); //$NON-NLS-1$
		result.append(visible);
		result.append(", containerData: "); //$NON-NLS-1$
		result.append(containerData);
		result.append(", label: "); //$NON-NLS-1$
		result.append(label);
		result.append(", iconURI: "); //$NON-NLS-1$
		result.append(iconURI);
		result.append(", tooltip: "); //$NON-NLS-1$
		result.append(tooltip);
		result.append(", enabled: "); //$NON-NLS-1$
		result.append(enabled);
		result.append(", selected: "); //$NON-NLS-1$
		result.append(selected);
		result.append(", type: "); //$NON-NLS-1$
		result.append(type);
		result.append(')');
		return result.toString();
	}

} //DirectToolItemImpl
