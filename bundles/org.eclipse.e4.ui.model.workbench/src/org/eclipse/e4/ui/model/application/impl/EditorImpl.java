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

import org.eclipse.e4.core.services.context.IEclipseContext;

import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.MApplicationPackage;
import org.eclipse.e4.ui.model.application.MBindingContainer;
import org.eclipse.e4.ui.model.application.MContext;
import org.eclipse.e4.ui.model.application.MContribution;
import org.eclipse.e4.ui.model.application.MDirtyable;
import org.eclipse.e4.ui.model.application.MEditor;
import org.eclipse.e4.ui.model.application.MElementContainer;
import org.eclipse.e4.ui.model.application.MHandler;
import org.eclipse.e4.ui.model.application.MHandlerContainer;
import org.eclipse.e4.ui.model.application.MKeyBinding;
import org.eclipse.e4.ui.model.application.MMenu;
import org.eclipse.e4.ui.model.application.MPSCElement;
import org.eclipse.e4.ui.model.application.MPart;
import org.eclipse.e4.ui.model.application.MSaveablePart;
import org.eclipse.e4.ui.model.application.MToolBar;
import org.eclipse.e4.ui.model.application.MUIElement;
import org.eclipse.e4.ui.model.application.MUILabel;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

import org.eclipse.emf.ecore.util.EDataTypeUniqueEList;
import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.EObjectResolvingEList;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.util.InternalEList;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Editor</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.EditorImpl#getId <em>Id</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.EditorImpl#getURI <em>URI</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.EditorImpl#getObject <em>Object</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.EditorImpl#getPersistedState <em>Persisted State</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.EditorImpl#getContext <em>Context</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.EditorImpl#getVariables <em>Variables</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.EditorImpl#getWidget <em>Widget</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.EditorImpl#getRenderer <em>Renderer</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.EditorImpl#isToBeRendered <em>To Be Rendered</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.EditorImpl#isVisible <em>Visible</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.EditorImpl#getParent <em>Parent</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.EditorImpl#getLabel <em>Label</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.EditorImpl#getIconURI <em>Icon URI</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.EditorImpl#getTooltip <em>Tooltip</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.EditorImpl#getHandlers <em>Handlers</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.EditorImpl#getBindings <em>Bindings</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.EditorImpl#getMenus <em>Menus</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.EditorImpl#getToolbar <em>Toolbar</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.EditorImpl#isDirty <em>Dirty</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class EditorImpl extends InputImpl implements MEditor {
	/**
	 * The default value of the '{@link #getId() <em>Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getId()
	 * @generated
	 * @ordered
	 */
	protected static final String ID_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getId() <em>Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getId()
	 * @generated
	 * @ordered
	 */
	protected String id = ID_EDEFAULT;

	/**
	 * The default value of the '{@link #getURI() <em>URI</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getURI()
	 * @generated
	 * @ordered
	 */
	protected static final String URI_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getURI() <em>URI</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getURI()
	 * @generated
	 * @ordered
	 */
	protected String uri = URI_EDEFAULT;

	/**
	 * The default value of the '{@link #getObject() <em>Object</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getObject()
	 * @generated
	 * @ordered
	 */
	protected static final Object OBJECT_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getObject() <em>Object</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getObject()
	 * @generated
	 * @ordered
	 */
	protected Object object = OBJECT_EDEFAULT;

	/**
	 * The default value of the '{@link #getPersistedState() <em>Persisted State</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPersistedState()
	 * @generated
	 * @ordered
	 */
	protected static final String PERSISTED_STATE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getPersistedState() <em>Persisted State</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPersistedState()
	 * @generated
	 * @ordered
	 */
	protected String persistedState = PERSISTED_STATE_EDEFAULT;

	/**
	 * The default value of the '{@link #getContext() <em>Context</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getContext()
	 * @generated
	 * @ordered
	 */
	protected static final IEclipseContext CONTEXT_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getContext() <em>Context</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getContext()
	 * @generated
	 * @ordered
	 */
	protected IEclipseContext context = CONTEXT_EDEFAULT;

	/**
	 * The cached value of the '{@link #getVariables() <em>Variables</em>}' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getVariables()
	 * @generated
	 * @ordered
	 */
	protected EList<String> variables;

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
	 * The default value of the '{@link #isVisible() <em>Visible</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isVisible()
	 * @generated
	 * @ordered
	 */
	protected static final boolean VISIBLE_EDEFAULT = false;

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
	 * The cached value of the '{@link #getHandlers() <em>Handlers</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getHandlers()
	 * @generated
	 * @ordered
	 */
	protected EList<MHandler> handlers;

	/**
	 * The cached value of the '{@link #getBindings() <em>Bindings</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getBindings()
	 * @generated
	 * @ordered
	 */
	protected EList<MKeyBinding> bindings;

	/**
	 * The cached value of the '{@link #getMenus() <em>Menus</em>}' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMenus()
	 * @generated
	 * @ordered
	 */
	protected EList<MMenu> menus;

	/**
	 * The cached value of the '{@link #getToolbar() <em>Toolbar</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getToolbar()
	 * @generated
	 * @ordered
	 */
	protected MToolBar toolbar;

	/**
	 * The default value of the '{@link #isDirty() <em>Dirty</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isDirty()
	 * @generated
	 * @ordered
	 */
	protected static final boolean DIRTY_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isDirty() <em>Dirty</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isDirty()
	 * @generated
	 * @ordered
	 */
	protected boolean dirty = DIRTY_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected EditorImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return MApplicationPackage.Literals.EDITOR;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getId() {
		return id;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setId(String newId) {
		String oldId = id;
		id = newId;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, MApplicationPackage.EDITOR__ID, oldId, id));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getURI() {
		return uri;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setURI(String newURI) {
		String oldURI = uri;
		uri = newURI;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, MApplicationPackage.EDITOR__URI, oldURI, uri));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Object getObject() {
		return object;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setObject(Object newObject) {
		Object oldObject = object;
		object = newObject;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, MApplicationPackage.EDITOR__OBJECT, oldObject, object));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getPersistedState() {
		return persistedState;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setPersistedState(String newPersistedState) {
		String oldPersistedState = persistedState;
		persistedState = newPersistedState;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, MApplicationPackage.EDITOR__PERSISTED_STATE, oldPersistedState, persistedState));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public IEclipseContext getContext() {
		return context;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setContext(IEclipseContext newContext) {
		IEclipseContext oldContext = context;
		context = newContext;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, MApplicationPackage.EDITOR__CONTEXT, oldContext, context));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<String> getVariables() {
		if (variables == null) {
			variables = new EDataTypeUniqueEList<String>(String.class, this, MApplicationPackage.EDITOR__VARIABLES);
		}
		return variables;
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
			eNotify(new ENotificationImpl(this, Notification.SET, MApplicationPackage.EDITOR__WIDGET, oldWidget, widget));
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
			eNotify(new ENotificationImpl(this, Notification.SET, MApplicationPackage.EDITOR__RENDERER, oldRenderer, renderer));
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
			eNotify(new ENotificationImpl(this, Notification.SET, MApplicationPackage.EDITOR__TO_BE_RENDERED, oldToBeRendered, toBeRendered));
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
			eNotify(new ENotificationImpl(this, Notification.SET, MApplicationPackage.EDITOR__VISIBLE, oldVisible, visible));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	public MElementContainer<MUIElement> getParent() {
		if (eContainerFeatureID() != MApplicationPackage.EDITOR__PARENT) return null;
		return (MElementContainer<MUIElement>)eContainer();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetParent(MElementContainer<MUIElement> newParent, NotificationChain msgs) {
		msgs = eBasicSetContainer((InternalEObject)newParent, MApplicationPackage.EDITOR__PARENT, msgs);
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setParent(MElementContainer<MUIElement> newParent) {
		if (newParent != eInternalContainer() || (eContainerFeatureID() != MApplicationPackage.EDITOR__PARENT && newParent != null)) {
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
			eNotify(new ENotificationImpl(this, Notification.SET, MApplicationPackage.EDITOR__PARENT, newParent, newParent));
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
			eNotify(new ENotificationImpl(this, Notification.SET, MApplicationPackage.EDITOR__LABEL, oldLabel, label));
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
			eNotify(new ENotificationImpl(this, Notification.SET, MApplicationPackage.EDITOR__ICON_URI, oldIconURI, iconURI));
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
			eNotify(new ENotificationImpl(this, Notification.SET, MApplicationPackage.EDITOR__TOOLTIP, oldTooltip, tooltip));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<MHandler> getHandlers() {
		if (handlers == null) {
			handlers = new EObjectContainmentEList<MHandler>(MHandler.class, this, MApplicationPackage.EDITOR__HANDLERS);
		}
		return handlers;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<MKeyBinding> getBindings() {
		if (bindings == null) {
			bindings = new EObjectContainmentEList<MKeyBinding>(MKeyBinding.class, this, MApplicationPackage.EDITOR__BINDINGS);
		}
		return bindings;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<MMenu> getMenus() {
		if (menus == null) {
			menus = new EObjectResolvingEList<MMenu>(MMenu.class, this, MApplicationPackage.EDITOR__MENUS);
		}
		return menus;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MToolBar getToolbar() {
		if (toolbar != null && ((EObject)toolbar).eIsProxy()) {
			InternalEObject oldToolbar = (InternalEObject)toolbar;
			toolbar = (MToolBar)eResolveProxy(oldToolbar);
			if (toolbar != oldToolbar) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, MApplicationPackage.EDITOR__TOOLBAR, oldToolbar, toolbar));
			}
		}
		return toolbar;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MToolBar basicGetToolbar() {
		return toolbar;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setToolbar(MToolBar newToolbar) {
		MToolBar oldToolbar = toolbar;
		toolbar = newToolbar;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, MApplicationPackage.EDITOR__TOOLBAR, oldToolbar, toolbar));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isDirty() {
		return dirty;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setDirty(boolean newDirty) {
		boolean oldDirty = dirty;
		dirty = newDirty;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, MApplicationPackage.EDITOR__DIRTY, oldDirty, dirty));
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
			case MApplicationPackage.EDITOR__PARENT:
				if (eInternalContainer() != null)
					msgs = eBasicRemoveFromContainer(msgs);
				return basicSetParent((MElementContainer<MUIElement>)otherEnd, msgs);
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
			case MApplicationPackage.EDITOR__PARENT:
				return basicSetParent(null, msgs);
			case MApplicationPackage.EDITOR__HANDLERS:
				return ((InternalEList<?>)getHandlers()).basicRemove(otherEnd, msgs);
			case MApplicationPackage.EDITOR__BINDINGS:
				return ((InternalEList<?>)getBindings()).basicRemove(otherEnd, msgs);
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
			case MApplicationPackage.EDITOR__PARENT:
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
			case MApplicationPackage.EDITOR__ID:
				return getId();
			case MApplicationPackage.EDITOR__URI:
				return getURI();
			case MApplicationPackage.EDITOR__OBJECT:
				return getObject();
			case MApplicationPackage.EDITOR__PERSISTED_STATE:
				return getPersistedState();
			case MApplicationPackage.EDITOR__CONTEXT:
				return getContext();
			case MApplicationPackage.EDITOR__VARIABLES:
				return getVariables();
			case MApplicationPackage.EDITOR__WIDGET:
				return getWidget();
			case MApplicationPackage.EDITOR__RENDERER:
				return getRenderer();
			case MApplicationPackage.EDITOR__TO_BE_RENDERED:
				return isToBeRendered();
			case MApplicationPackage.EDITOR__VISIBLE:
				return isVisible();
			case MApplicationPackage.EDITOR__PARENT:
				return getParent();
			case MApplicationPackage.EDITOR__LABEL:
				return getLabel();
			case MApplicationPackage.EDITOR__ICON_URI:
				return getIconURI();
			case MApplicationPackage.EDITOR__TOOLTIP:
				return getTooltip();
			case MApplicationPackage.EDITOR__HANDLERS:
				return getHandlers();
			case MApplicationPackage.EDITOR__BINDINGS:
				return getBindings();
			case MApplicationPackage.EDITOR__MENUS:
				return getMenus();
			case MApplicationPackage.EDITOR__TOOLBAR:
				if (resolve) return getToolbar();
				return basicGetToolbar();
			case MApplicationPackage.EDITOR__DIRTY:
				return isDirty();
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
			case MApplicationPackage.EDITOR__ID:
				setId((String)newValue);
				return;
			case MApplicationPackage.EDITOR__URI:
				setURI((String)newValue);
				return;
			case MApplicationPackage.EDITOR__OBJECT:
				setObject(newValue);
				return;
			case MApplicationPackage.EDITOR__PERSISTED_STATE:
				setPersistedState((String)newValue);
				return;
			case MApplicationPackage.EDITOR__CONTEXT:
				setContext((IEclipseContext)newValue);
				return;
			case MApplicationPackage.EDITOR__VARIABLES:
				getVariables().clear();
				getVariables().addAll((Collection<? extends String>)newValue);
				return;
			case MApplicationPackage.EDITOR__WIDGET:
				setWidget(newValue);
				return;
			case MApplicationPackage.EDITOR__RENDERER:
				setRenderer(newValue);
				return;
			case MApplicationPackage.EDITOR__TO_BE_RENDERED:
				setToBeRendered((Boolean)newValue);
				return;
			case MApplicationPackage.EDITOR__VISIBLE:
				setVisible((Boolean)newValue);
				return;
			case MApplicationPackage.EDITOR__PARENT:
				setParent((MElementContainer<MUIElement>)newValue);
				return;
			case MApplicationPackage.EDITOR__LABEL:
				setLabel((String)newValue);
				return;
			case MApplicationPackage.EDITOR__ICON_URI:
				setIconURI((String)newValue);
				return;
			case MApplicationPackage.EDITOR__TOOLTIP:
				setTooltip((String)newValue);
				return;
			case MApplicationPackage.EDITOR__HANDLERS:
				getHandlers().clear();
				getHandlers().addAll((Collection<? extends MHandler>)newValue);
				return;
			case MApplicationPackage.EDITOR__BINDINGS:
				getBindings().clear();
				getBindings().addAll((Collection<? extends MKeyBinding>)newValue);
				return;
			case MApplicationPackage.EDITOR__MENUS:
				getMenus().clear();
				getMenus().addAll((Collection<? extends MMenu>)newValue);
				return;
			case MApplicationPackage.EDITOR__TOOLBAR:
				setToolbar((MToolBar)newValue);
				return;
			case MApplicationPackage.EDITOR__DIRTY:
				setDirty((Boolean)newValue);
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
			case MApplicationPackage.EDITOR__ID:
				setId(ID_EDEFAULT);
				return;
			case MApplicationPackage.EDITOR__URI:
				setURI(URI_EDEFAULT);
				return;
			case MApplicationPackage.EDITOR__OBJECT:
				setObject(OBJECT_EDEFAULT);
				return;
			case MApplicationPackage.EDITOR__PERSISTED_STATE:
				setPersistedState(PERSISTED_STATE_EDEFAULT);
				return;
			case MApplicationPackage.EDITOR__CONTEXT:
				setContext(CONTEXT_EDEFAULT);
				return;
			case MApplicationPackage.EDITOR__VARIABLES:
				getVariables().clear();
				return;
			case MApplicationPackage.EDITOR__WIDGET:
				setWidget(WIDGET_EDEFAULT);
				return;
			case MApplicationPackage.EDITOR__RENDERER:
				setRenderer(RENDERER_EDEFAULT);
				return;
			case MApplicationPackage.EDITOR__TO_BE_RENDERED:
				setToBeRendered(TO_BE_RENDERED_EDEFAULT);
				return;
			case MApplicationPackage.EDITOR__VISIBLE:
				setVisible(VISIBLE_EDEFAULT);
				return;
			case MApplicationPackage.EDITOR__PARENT:
				setParent((MElementContainer<MUIElement>)null);
				return;
			case MApplicationPackage.EDITOR__LABEL:
				setLabel(LABEL_EDEFAULT);
				return;
			case MApplicationPackage.EDITOR__ICON_URI:
				setIconURI(ICON_URI_EDEFAULT);
				return;
			case MApplicationPackage.EDITOR__TOOLTIP:
				setTooltip(TOOLTIP_EDEFAULT);
				return;
			case MApplicationPackage.EDITOR__HANDLERS:
				getHandlers().clear();
				return;
			case MApplicationPackage.EDITOR__BINDINGS:
				getBindings().clear();
				return;
			case MApplicationPackage.EDITOR__MENUS:
				getMenus().clear();
				return;
			case MApplicationPackage.EDITOR__TOOLBAR:
				setToolbar((MToolBar)null);
				return;
			case MApplicationPackage.EDITOR__DIRTY:
				setDirty(DIRTY_EDEFAULT);
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
			case MApplicationPackage.EDITOR__ID:
				return ID_EDEFAULT == null ? id != null : !ID_EDEFAULT.equals(id);
			case MApplicationPackage.EDITOR__URI:
				return URI_EDEFAULT == null ? uri != null : !URI_EDEFAULT.equals(uri);
			case MApplicationPackage.EDITOR__OBJECT:
				return OBJECT_EDEFAULT == null ? object != null : !OBJECT_EDEFAULT.equals(object);
			case MApplicationPackage.EDITOR__PERSISTED_STATE:
				return PERSISTED_STATE_EDEFAULT == null ? persistedState != null : !PERSISTED_STATE_EDEFAULT.equals(persistedState);
			case MApplicationPackage.EDITOR__CONTEXT:
				return CONTEXT_EDEFAULT == null ? context != null : !CONTEXT_EDEFAULT.equals(context);
			case MApplicationPackage.EDITOR__VARIABLES:
				return variables != null && !variables.isEmpty();
			case MApplicationPackage.EDITOR__WIDGET:
				return WIDGET_EDEFAULT == null ? widget != null : !WIDGET_EDEFAULT.equals(widget);
			case MApplicationPackage.EDITOR__RENDERER:
				return RENDERER_EDEFAULT == null ? renderer != null : !RENDERER_EDEFAULT.equals(renderer);
			case MApplicationPackage.EDITOR__TO_BE_RENDERED:
				return toBeRendered != TO_BE_RENDERED_EDEFAULT;
			case MApplicationPackage.EDITOR__VISIBLE:
				return visible != VISIBLE_EDEFAULT;
			case MApplicationPackage.EDITOR__PARENT:
				return getParent() != null;
			case MApplicationPackage.EDITOR__LABEL:
				return LABEL_EDEFAULT == null ? label != null : !LABEL_EDEFAULT.equals(label);
			case MApplicationPackage.EDITOR__ICON_URI:
				return ICON_URI_EDEFAULT == null ? iconURI != null : !ICON_URI_EDEFAULT.equals(iconURI);
			case MApplicationPackage.EDITOR__TOOLTIP:
				return TOOLTIP_EDEFAULT == null ? tooltip != null : !TOOLTIP_EDEFAULT.equals(tooltip);
			case MApplicationPackage.EDITOR__HANDLERS:
				return handlers != null && !handlers.isEmpty();
			case MApplicationPackage.EDITOR__BINDINGS:
				return bindings != null && !bindings.isEmpty();
			case MApplicationPackage.EDITOR__MENUS:
				return menus != null && !menus.isEmpty();
			case MApplicationPackage.EDITOR__TOOLBAR:
				return toolbar != null;
			case MApplicationPackage.EDITOR__DIRTY:
				return dirty != DIRTY_EDEFAULT;
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
		if (baseClass == MApplicationElement.class) {
			switch (derivedFeatureID) {
				case MApplicationPackage.EDITOR__ID: return MApplicationPackage.APPLICATION_ELEMENT__ID;
				default: return -1;
			}
		}
		if (baseClass == MContribution.class) {
			switch (derivedFeatureID) {
				case MApplicationPackage.EDITOR__URI: return MApplicationPackage.CONTRIBUTION__URI;
				case MApplicationPackage.EDITOR__OBJECT: return MApplicationPackage.CONTRIBUTION__OBJECT;
				case MApplicationPackage.EDITOR__PERSISTED_STATE: return MApplicationPackage.CONTRIBUTION__PERSISTED_STATE;
				default: return -1;
			}
		}
		if (baseClass == MContext.class) {
			switch (derivedFeatureID) {
				case MApplicationPackage.EDITOR__CONTEXT: return MApplicationPackage.CONTEXT__CONTEXT;
				case MApplicationPackage.EDITOR__VARIABLES: return MApplicationPackage.CONTEXT__VARIABLES;
				default: return -1;
			}
		}
		if (baseClass == MUIElement.class) {
			switch (derivedFeatureID) {
				case MApplicationPackage.EDITOR__WIDGET: return MApplicationPackage.UI_ELEMENT__WIDGET;
				case MApplicationPackage.EDITOR__RENDERER: return MApplicationPackage.UI_ELEMENT__RENDERER;
				case MApplicationPackage.EDITOR__TO_BE_RENDERED: return MApplicationPackage.UI_ELEMENT__TO_BE_RENDERED;
				case MApplicationPackage.EDITOR__VISIBLE: return MApplicationPackage.UI_ELEMENT__VISIBLE;
				case MApplicationPackage.EDITOR__PARENT: return MApplicationPackage.UI_ELEMENT__PARENT;
				default: return -1;
			}
		}
		if (baseClass == MPSCElement.class) {
			switch (derivedFeatureID) {
				default: return -1;
			}
		}
		if (baseClass == MUILabel.class) {
			switch (derivedFeatureID) {
				case MApplicationPackage.EDITOR__LABEL: return MApplicationPackage.UI_LABEL__LABEL;
				case MApplicationPackage.EDITOR__ICON_URI: return MApplicationPackage.UI_LABEL__ICON_URI;
				case MApplicationPackage.EDITOR__TOOLTIP: return MApplicationPackage.UI_LABEL__TOOLTIP;
				default: return -1;
			}
		}
		if (baseClass == MHandlerContainer.class) {
			switch (derivedFeatureID) {
				case MApplicationPackage.EDITOR__HANDLERS: return MApplicationPackage.HANDLER_CONTAINER__HANDLERS;
				default: return -1;
			}
		}
		if (baseClass == MBindingContainer.class) {
			switch (derivedFeatureID) {
				case MApplicationPackage.EDITOR__BINDINGS: return MApplicationPackage.BINDING_CONTAINER__BINDINGS;
				default: return -1;
			}
		}
		if (baseClass == MPart.class) {
			switch (derivedFeatureID) {
				case MApplicationPackage.EDITOR__MENUS: return MApplicationPackage.PART__MENUS;
				case MApplicationPackage.EDITOR__TOOLBAR: return MApplicationPackage.PART__TOOLBAR;
				default: return -1;
			}
		}
		if (baseClass == MDirtyable.class) {
			switch (derivedFeatureID) {
				case MApplicationPackage.EDITOR__DIRTY: return MApplicationPackage.DIRTYABLE__DIRTY;
				default: return -1;
			}
		}
		if (baseClass == MSaveablePart.class) {
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
		if (baseClass == MApplicationElement.class) {
			switch (baseFeatureID) {
				case MApplicationPackage.APPLICATION_ELEMENT__ID: return MApplicationPackage.EDITOR__ID;
				default: return -1;
			}
		}
		if (baseClass == MContribution.class) {
			switch (baseFeatureID) {
				case MApplicationPackage.CONTRIBUTION__URI: return MApplicationPackage.EDITOR__URI;
				case MApplicationPackage.CONTRIBUTION__OBJECT: return MApplicationPackage.EDITOR__OBJECT;
				case MApplicationPackage.CONTRIBUTION__PERSISTED_STATE: return MApplicationPackage.EDITOR__PERSISTED_STATE;
				default: return -1;
			}
		}
		if (baseClass == MContext.class) {
			switch (baseFeatureID) {
				case MApplicationPackage.CONTEXT__CONTEXT: return MApplicationPackage.EDITOR__CONTEXT;
				case MApplicationPackage.CONTEXT__VARIABLES: return MApplicationPackage.EDITOR__VARIABLES;
				default: return -1;
			}
		}
		if (baseClass == MUIElement.class) {
			switch (baseFeatureID) {
				case MApplicationPackage.UI_ELEMENT__WIDGET: return MApplicationPackage.EDITOR__WIDGET;
				case MApplicationPackage.UI_ELEMENT__RENDERER: return MApplicationPackage.EDITOR__RENDERER;
				case MApplicationPackage.UI_ELEMENT__TO_BE_RENDERED: return MApplicationPackage.EDITOR__TO_BE_RENDERED;
				case MApplicationPackage.UI_ELEMENT__VISIBLE: return MApplicationPackage.EDITOR__VISIBLE;
				case MApplicationPackage.UI_ELEMENT__PARENT: return MApplicationPackage.EDITOR__PARENT;
				default: return -1;
			}
		}
		if (baseClass == MPSCElement.class) {
			switch (baseFeatureID) {
				default: return -1;
			}
		}
		if (baseClass == MUILabel.class) {
			switch (baseFeatureID) {
				case MApplicationPackage.UI_LABEL__LABEL: return MApplicationPackage.EDITOR__LABEL;
				case MApplicationPackage.UI_LABEL__ICON_URI: return MApplicationPackage.EDITOR__ICON_URI;
				case MApplicationPackage.UI_LABEL__TOOLTIP: return MApplicationPackage.EDITOR__TOOLTIP;
				default: return -1;
			}
		}
		if (baseClass == MHandlerContainer.class) {
			switch (baseFeatureID) {
				case MApplicationPackage.HANDLER_CONTAINER__HANDLERS: return MApplicationPackage.EDITOR__HANDLERS;
				default: return -1;
			}
		}
		if (baseClass == MBindingContainer.class) {
			switch (baseFeatureID) {
				case MApplicationPackage.BINDING_CONTAINER__BINDINGS: return MApplicationPackage.EDITOR__BINDINGS;
				default: return -1;
			}
		}
		if (baseClass == MPart.class) {
			switch (baseFeatureID) {
				case MApplicationPackage.PART__MENUS: return MApplicationPackage.EDITOR__MENUS;
				case MApplicationPackage.PART__TOOLBAR: return MApplicationPackage.EDITOR__TOOLBAR;
				default: return -1;
			}
		}
		if (baseClass == MDirtyable.class) {
			switch (baseFeatureID) {
				case MApplicationPackage.DIRTYABLE__DIRTY: return MApplicationPackage.EDITOR__DIRTY;
				default: return -1;
			}
		}
		if (baseClass == MSaveablePart.class) {
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
		result.append(" (id: "); //$NON-NLS-1$
		result.append(id);
		result.append(", URI: "); //$NON-NLS-1$
		result.append(uri);
		result.append(", object: "); //$NON-NLS-1$
		result.append(object);
		result.append(", persistedState: "); //$NON-NLS-1$
		result.append(persistedState);
		result.append(", context: "); //$NON-NLS-1$
		result.append(context);
		result.append(", variables: "); //$NON-NLS-1$
		result.append(variables);
		result.append(", widget: "); //$NON-NLS-1$
		result.append(widget);
		result.append(", renderer: "); //$NON-NLS-1$
		result.append(renderer);
		result.append(", toBeRendered: "); //$NON-NLS-1$
		result.append(toBeRendered);
		result.append(", visible: "); //$NON-NLS-1$
		result.append(visible);
		result.append(", label: "); //$NON-NLS-1$
		result.append(label);
		result.append(", iconURI: "); //$NON-NLS-1$
		result.append(iconURI);
		result.append(", tooltip: "); //$NON-NLS-1$
		result.append(tooltip);
		result.append(", dirty: "); //$NON-NLS-1$
		result.append(dirty);
		result.append(')');
		return result.toString();
	}

} //EditorImpl
