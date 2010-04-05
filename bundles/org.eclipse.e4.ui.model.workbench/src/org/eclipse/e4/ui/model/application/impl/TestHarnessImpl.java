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

import org.eclipse.e4.core.contexts.IEclipseContext;

import org.eclipse.e4.ui.model.application.ItemType;
import org.eclipse.e4.ui.model.application.MApplicationPackage;
import org.eclipse.e4.ui.model.application.MCommand;
import org.eclipse.e4.ui.model.application.MCommandParameter;
import org.eclipse.e4.ui.model.application.MContext;
import org.eclipse.e4.ui.model.application.MContribution;
import org.eclipse.e4.ui.model.application.MDirtyable;
import org.eclipse.e4.ui.model.application.MElementContainer;
import org.eclipse.e4.ui.model.application.MInput;
import org.eclipse.e4.ui.model.application.MItem;
import org.eclipse.e4.ui.model.application.MParameter;
import org.eclipse.e4.ui.model.application.MTestHarness;
import org.eclipse.e4.ui.model.application.MUIElement;
import org.eclipse.e4.ui.model.application.MUILabel;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.common.util.EMap;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

import org.eclipse.emf.ecore.util.EDataTypeUniqueEList;
import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.EObjectContainmentWithInverseEList;
import org.eclipse.emf.ecore.util.EcoreEMap;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.util.InternalEList;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Test Harness</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.TestHarnessImpl#getCommandName <em>Command Name</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.TestHarnessImpl#getDescription <em>Description</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.TestHarnessImpl#getParameters <em>Parameters</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.TestHarnessImpl#getContext <em>Context</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.TestHarnessImpl#getVariables <em>Variables</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.TestHarnessImpl#getProperties <em>Properties</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.TestHarnessImpl#getURI <em>URI</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.TestHarnessImpl#getObject <em>Object</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.TestHarnessImpl#getPersistedState <em>Persisted State</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.TestHarnessImpl#getWidget <em>Widget</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.TestHarnessImpl#getRenderer <em>Renderer</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.TestHarnessImpl#isToBeRendered <em>To Be Rendered</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.TestHarnessImpl#isOnTop <em>On Top</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.TestHarnessImpl#isVisible <em>Visible</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.TestHarnessImpl#getParent <em>Parent</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.TestHarnessImpl#getContainerData <em>Container Data</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.TestHarnessImpl#getChildren <em>Children</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.TestHarnessImpl#getSelectedElement <em>Selected Element</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.TestHarnessImpl#getTag <em>Tag</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.TestHarnessImpl#getValue <em>Value</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.TestHarnessImpl#getInputURI <em>Input URI</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.TestHarnessImpl#getLabel <em>Label</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.TestHarnessImpl#getIconURI <em>Icon URI</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.TestHarnessImpl#getTooltip <em>Tooltip</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.TestHarnessImpl#isEnabled <em>Enabled</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.TestHarnessImpl#isSelected <em>Selected</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.TestHarnessImpl#getType <em>Type</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.TestHarnessImpl#isDirty <em>Dirty</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class TestHarnessImpl extends ApplicationElementImpl implements MTestHarness {
	/**
	 * The default value of the '{@link #getCommandName() <em>Command Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCommandName()
	 * @generated
	 * @ordered
	 */
	protected static final String COMMAND_NAME_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getCommandName() <em>Command Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCommandName()
	 * @generated
	 * @ordered
	 */
	protected String commandName = COMMAND_NAME_EDEFAULT;

	/**
	 * The default value of the '{@link #getDescription() <em>Description</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDescription()
	 * @generated
	 * @ordered
	 */
	protected static final String DESCRIPTION_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getDescription() <em>Description</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDescription()
	 * @generated
	 * @ordered
	 */
	protected String description = DESCRIPTION_EDEFAULT;

	/**
	 * The cached value of the '{@link #getParameters() <em>Parameters</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getParameters()
	 * @generated
	 * @ordered
	 */
	protected EList<MCommandParameter> parameters;

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
	 * The cached value of the '{@link #getProperties() <em>Properties</em>}' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getProperties()
	 * @generated
	 * @ordered
	 */
	protected EMap<String, String> properties;

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
	 * The cached value of the '{@link #getPersistedState() <em>Persisted State</em>}' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPersistedState()
	 * @generated
	 * @ordered
	 */
	protected EMap<String, String> persistedState;

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
	 * The cached value of the '{@link #getChildren() <em>Children</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getChildren()
	 * @generated
	 * @ordered
	 */
	protected EList<MUIElement> children;

	/**
	 * The cached value of the '{@link #getSelectedElement() <em>Selected Element</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSelectedElement()
	 * @generated
	 * @ordered
	 */
	protected MUIElement selectedElement;

	/**
	 * The default value of the '{@link #getTag() <em>Tag</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTag()
	 * @generated
	 * @ordered
	 */
	protected static final String TAG_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getTag() <em>Tag</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTag()
	 * @generated
	 * @ordered
	 */
	protected String tag = TAG_EDEFAULT;

	/**
	 * The default value of the '{@link #getValue() <em>Value</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getValue()
	 * @generated
	 * @ordered
	 */
	protected static final String VALUE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getValue() <em>Value</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getValue()
	 * @generated
	 * @ordered
	 */
	protected String value = VALUE_EDEFAULT;

	/**
	 * The default value of the '{@link #getInputURI() <em>Input URI</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getInputURI()
	 * @generated
	 * @ordered
	 */
	protected static final String INPUT_URI_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getInputURI() <em>Input URI</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getInputURI()
	 * @generated
	 * @ordered
	 */
	protected String inputURI = INPUT_URI_EDEFAULT;

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
	protected TestHarnessImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return MApplicationPackage.Literals.TEST_HARNESS;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getCommandName() {
		return commandName;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setCommandName(String newCommandName) {
		String oldCommandName = commandName;
		commandName = newCommandName;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, MApplicationPackage.TEST_HARNESS__COMMAND_NAME, oldCommandName, commandName));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setDescription(String newDescription) {
		String oldDescription = description;
		description = newDescription;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, MApplicationPackage.TEST_HARNESS__DESCRIPTION, oldDescription, description));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<MCommandParameter> getParameters() {
		if (parameters == null) {
			parameters = new EObjectContainmentEList<MCommandParameter>(MCommandParameter.class, this, MApplicationPackage.TEST_HARNESS__PARAMETERS);
		}
		return parameters;
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
			eNotify(new ENotificationImpl(this, Notification.SET, MApplicationPackage.TEST_HARNESS__CONTEXT, oldContext, context));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<String> getVariables() {
		if (variables == null) {
			variables = new EDataTypeUniqueEList<String>(String.class, this, MApplicationPackage.TEST_HARNESS__VARIABLES);
		}
		return variables;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EMap<String, String> getProperties() {
		if (properties == null) {
			properties = new EcoreEMap<String,String>(MApplicationPackage.Literals.STRING_TO_STRING_MAP, StringToStringMapImpl.class, this, MApplicationPackage.TEST_HARNESS__PROPERTIES);
		}
		return properties;
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
			eNotify(new ENotificationImpl(this, Notification.SET, MApplicationPackage.TEST_HARNESS__URI, oldURI, uri));
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
			eNotify(new ENotificationImpl(this, Notification.SET, MApplicationPackage.TEST_HARNESS__OBJECT, oldObject, object));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EMap<String, String> getPersistedState() {
		if (persistedState == null) {
			persistedState = new EcoreEMap<String,String>(MApplicationPackage.Literals.STRING_TO_STRING_MAP, StringToStringMapImpl.class, this, MApplicationPackage.TEST_HARNESS__PERSISTED_STATE);
		}
		return persistedState;
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
			eNotify(new ENotificationImpl(this, Notification.SET, MApplicationPackage.TEST_HARNESS__WIDGET, oldWidget, widget));
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
			eNotify(new ENotificationImpl(this, Notification.SET, MApplicationPackage.TEST_HARNESS__RENDERER, oldRenderer, renderer));
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
			eNotify(new ENotificationImpl(this, Notification.SET, MApplicationPackage.TEST_HARNESS__TO_BE_RENDERED, oldToBeRendered, toBeRendered));
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
			eNotify(new ENotificationImpl(this, Notification.SET, MApplicationPackage.TEST_HARNESS__ON_TOP, oldOnTop, onTop));
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
			eNotify(new ENotificationImpl(this, Notification.SET, MApplicationPackage.TEST_HARNESS__VISIBLE, oldVisible, visible));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	public MElementContainer<MUIElement> getParent() {
		if (eContainerFeatureID() != MApplicationPackage.TEST_HARNESS__PARENT) return null;
		return (MElementContainer<MUIElement>)eContainer();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetParent(MElementContainer<MUIElement> newParent, NotificationChain msgs) {
		msgs = eBasicSetContainer((InternalEObject)newParent, MApplicationPackage.TEST_HARNESS__PARENT, msgs);
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setParent(MElementContainer<MUIElement> newParent) {
		if (newParent != eInternalContainer() || (eContainerFeatureID() != MApplicationPackage.TEST_HARNESS__PARENT && newParent != null)) {
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
			eNotify(new ENotificationImpl(this, Notification.SET, MApplicationPackage.TEST_HARNESS__PARENT, newParent, newParent));
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
			eNotify(new ENotificationImpl(this, Notification.SET, MApplicationPackage.TEST_HARNESS__CONTAINER_DATA, oldContainerData, containerData));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<MUIElement> getChildren() {
		if (children == null) {
			children = new EObjectContainmentWithInverseEList<MUIElement>(MUIElement.class, this, MApplicationPackage.TEST_HARNESS__CHILDREN, MApplicationPackage.UI_ELEMENT__PARENT);
		}
		return children;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MUIElement getSelectedElement() {
		if (selectedElement != null && ((EObject)selectedElement).eIsProxy()) {
			InternalEObject oldSelectedElement = (InternalEObject)selectedElement;
			selectedElement = (MUIElement)eResolveProxy(oldSelectedElement);
			if (selectedElement != oldSelectedElement) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, MApplicationPackage.TEST_HARNESS__SELECTED_ELEMENT, oldSelectedElement, selectedElement));
			}
		}
		return selectedElement;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MUIElement basicGetSelectedElement() {
		return selectedElement;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setSelectedElement(MUIElement newSelectedElement) {
		MUIElement oldSelectedElement = selectedElement;
		selectedElement = newSelectedElement;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, MApplicationPackage.TEST_HARNESS__SELECTED_ELEMENT, oldSelectedElement, selectedElement));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getTag() {
		return tag;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setTag(String newTag) {
		String oldTag = tag;
		tag = newTag;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, MApplicationPackage.TEST_HARNESS__TAG, oldTag, tag));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getValue() {
		return value;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setValue(String newValue) {
		String oldValue = value;
		value = newValue;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, MApplicationPackage.TEST_HARNESS__VALUE, oldValue, value));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getInputURI() {
		return inputURI;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setInputURI(String newInputURI) {
		String oldInputURI = inputURI;
		inputURI = newInputURI;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, MApplicationPackage.TEST_HARNESS__INPUT_URI, oldInputURI, inputURI));
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
			eNotify(new ENotificationImpl(this, Notification.SET, MApplicationPackage.TEST_HARNESS__LABEL, oldLabel, label));
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
			eNotify(new ENotificationImpl(this, Notification.SET, MApplicationPackage.TEST_HARNESS__ICON_URI, oldIconURI, iconURI));
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
			eNotify(new ENotificationImpl(this, Notification.SET, MApplicationPackage.TEST_HARNESS__TOOLTIP, oldTooltip, tooltip));
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
			eNotify(new ENotificationImpl(this, Notification.SET, MApplicationPackage.TEST_HARNESS__ENABLED, oldEnabled, enabled));
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
			eNotify(new ENotificationImpl(this, Notification.SET, MApplicationPackage.TEST_HARNESS__SELECTED, oldSelected, selected));
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
			eNotify(new ENotificationImpl(this, Notification.SET, MApplicationPackage.TEST_HARNESS__TYPE, oldType, type));
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
			eNotify(new ENotificationImpl(this, Notification.SET, MApplicationPackage.TEST_HARNESS__DIRTY, oldDirty, dirty));
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
			case MApplicationPackage.TEST_HARNESS__PARENT:
				if (eInternalContainer() != null)
					msgs = eBasicRemoveFromContainer(msgs);
				return basicSetParent((MElementContainer<MUIElement>)otherEnd, msgs);
			case MApplicationPackage.TEST_HARNESS__CHILDREN:
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
			case MApplicationPackage.TEST_HARNESS__PARAMETERS:
				return ((InternalEList<?>)getParameters()).basicRemove(otherEnd, msgs);
			case MApplicationPackage.TEST_HARNESS__PROPERTIES:
				return ((InternalEList<?>)getProperties()).basicRemove(otherEnd, msgs);
			case MApplicationPackage.TEST_HARNESS__PERSISTED_STATE:
				return ((InternalEList<?>)getPersistedState()).basicRemove(otherEnd, msgs);
			case MApplicationPackage.TEST_HARNESS__PARENT:
				return basicSetParent(null, msgs);
			case MApplicationPackage.TEST_HARNESS__CHILDREN:
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
			case MApplicationPackage.TEST_HARNESS__PARENT:
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
			case MApplicationPackage.TEST_HARNESS__COMMAND_NAME:
				return getCommandName();
			case MApplicationPackage.TEST_HARNESS__DESCRIPTION:
				return getDescription();
			case MApplicationPackage.TEST_HARNESS__PARAMETERS:
				return getParameters();
			case MApplicationPackage.TEST_HARNESS__CONTEXT:
				return getContext();
			case MApplicationPackage.TEST_HARNESS__VARIABLES:
				return getVariables();
			case MApplicationPackage.TEST_HARNESS__PROPERTIES:
				if (coreType) return getProperties();
				else return getProperties().map();
			case MApplicationPackage.TEST_HARNESS__URI:
				return getURI();
			case MApplicationPackage.TEST_HARNESS__OBJECT:
				return getObject();
			case MApplicationPackage.TEST_HARNESS__PERSISTED_STATE:
				if (coreType) return getPersistedState();
				else return getPersistedState().map();
			case MApplicationPackage.TEST_HARNESS__WIDGET:
				return getWidget();
			case MApplicationPackage.TEST_HARNESS__RENDERER:
				return getRenderer();
			case MApplicationPackage.TEST_HARNESS__TO_BE_RENDERED:
				return isToBeRendered();
			case MApplicationPackage.TEST_HARNESS__ON_TOP:
				return isOnTop();
			case MApplicationPackage.TEST_HARNESS__VISIBLE:
				return isVisible();
			case MApplicationPackage.TEST_HARNESS__PARENT:
				return getParent();
			case MApplicationPackage.TEST_HARNESS__CONTAINER_DATA:
				return getContainerData();
			case MApplicationPackage.TEST_HARNESS__CHILDREN:
				return getChildren();
			case MApplicationPackage.TEST_HARNESS__SELECTED_ELEMENT:
				if (resolve) return getSelectedElement();
				return basicGetSelectedElement();
			case MApplicationPackage.TEST_HARNESS__TAG:
				return getTag();
			case MApplicationPackage.TEST_HARNESS__VALUE:
				return getValue();
			case MApplicationPackage.TEST_HARNESS__INPUT_URI:
				return getInputURI();
			case MApplicationPackage.TEST_HARNESS__LABEL:
				return getLabel();
			case MApplicationPackage.TEST_HARNESS__ICON_URI:
				return getIconURI();
			case MApplicationPackage.TEST_HARNESS__TOOLTIP:
				return getTooltip();
			case MApplicationPackage.TEST_HARNESS__ENABLED:
				return isEnabled();
			case MApplicationPackage.TEST_HARNESS__SELECTED:
				return isSelected();
			case MApplicationPackage.TEST_HARNESS__TYPE:
				return getType();
			case MApplicationPackage.TEST_HARNESS__DIRTY:
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
			case MApplicationPackage.TEST_HARNESS__COMMAND_NAME:
				setCommandName((String)newValue);
				return;
			case MApplicationPackage.TEST_HARNESS__DESCRIPTION:
				setDescription((String)newValue);
				return;
			case MApplicationPackage.TEST_HARNESS__PARAMETERS:
				getParameters().clear();
				getParameters().addAll((Collection<? extends MCommandParameter>)newValue);
				return;
			case MApplicationPackage.TEST_HARNESS__CONTEXT:
				setContext((IEclipseContext)newValue);
				return;
			case MApplicationPackage.TEST_HARNESS__VARIABLES:
				getVariables().clear();
				getVariables().addAll((Collection<? extends String>)newValue);
				return;
			case MApplicationPackage.TEST_HARNESS__PROPERTIES:
				((EStructuralFeature.Setting)getProperties()).set(newValue);
				return;
			case MApplicationPackage.TEST_HARNESS__URI:
				setURI((String)newValue);
				return;
			case MApplicationPackage.TEST_HARNESS__OBJECT:
				setObject(newValue);
				return;
			case MApplicationPackage.TEST_HARNESS__PERSISTED_STATE:
				((EStructuralFeature.Setting)getPersistedState()).set(newValue);
				return;
			case MApplicationPackage.TEST_HARNESS__WIDGET:
				setWidget(newValue);
				return;
			case MApplicationPackage.TEST_HARNESS__RENDERER:
				setRenderer(newValue);
				return;
			case MApplicationPackage.TEST_HARNESS__TO_BE_RENDERED:
				setToBeRendered((Boolean)newValue);
				return;
			case MApplicationPackage.TEST_HARNESS__ON_TOP:
				setOnTop((Boolean)newValue);
				return;
			case MApplicationPackage.TEST_HARNESS__VISIBLE:
				setVisible((Boolean)newValue);
				return;
			case MApplicationPackage.TEST_HARNESS__PARENT:
				setParent((MElementContainer<MUIElement>)newValue);
				return;
			case MApplicationPackage.TEST_HARNESS__CONTAINER_DATA:
				setContainerData((String)newValue);
				return;
			case MApplicationPackage.TEST_HARNESS__CHILDREN:
				getChildren().clear();
				getChildren().addAll((Collection<? extends MUIElement>)newValue);
				return;
			case MApplicationPackage.TEST_HARNESS__SELECTED_ELEMENT:
				setSelectedElement((MUIElement)newValue);
				return;
			case MApplicationPackage.TEST_HARNESS__TAG:
				setTag((String)newValue);
				return;
			case MApplicationPackage.TEST_HARNESS__VALUE:
				setValue((String)newValue);
				return;
			case MApplicationPackage.TEST_HARNESS__INPUT_URI:
				setInputURI((String)newValue);
				return;
			case MApplicationPackage.TEST_HARNESS__LABEL:
				setLabel((String)newValue);
				return;
			case MApplicationPackage.TEST_HARNESS__ICON_URI:
				setIconURI((String)newValue);
				return;
			case MApplicationPackage.TEST_HARNESS__TOOLTIP:
				setTooltip((String)newValue);
				return;
			case MApplicationPackage.TEST_HARNESS__ENABLED:
				setEnabled((Boolean)newValue);
				return;
			case MApplicationPackage.TEST_HARNESS__SELECTED:
				setSelected((Boolean)newValue);
				return;
			case MApplicationPackage.TEST_HARNESS__TYPE:
				setType((ItemType)newValue);
				return;
			case MApplicationPackage.TEST_HARNESS__DIRTY:
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
			case MApplicationPackage.TEST_HARNESS__COMMAND_NAME:
				setCommandName(COMMAND_NAME_EDEFAULT);
				return;
			case MApplicationPackage.TEST_HARNESS__DESCRIPTION:
				setDescription(DESCRIPTION_EDEFAULT);
				return;
			case MApplicationPackage.TEST_HARNESS__PARAMETERS:
				getParameters().clear();
				return;
			case MApplicationPackage.TEST_HARNESS__CONTEXT:
				setContext(CONTEXT_EDEFAULT);
				return;
			case MApplicationPackage.TEST_HARNESS__VARIABLES:
				getVariables().clear();
				return;
			case MApplicationPackage.TEST_HARNESS__PROPERTIES:
				getProperties().clear();
				return;
			case MApplicationPackage.TEST_HARNESS__URI:
				setURI(URI_EDEFAULT);
				return;
			case MApplicationPackage.TEST_HARNESS__OBJECT:
				setObject(OBJECT_EDEFAULT);
				return;
			case MApplicationPackage.TEST_HARNESS__PERSISTED_STATE:
				getPersistedState().clear();
				return;
			case MApplicationPackage.TEST_HARNESS__WIDGET:
				setWidget(WIDGET_EDEFAULT);
				return;
			case MApplicationPackage.TEST_HARNESS__RENDERER:
				setRenderer(RENDERER_EDEFAULT);
				return;
			case MApplicationPackage.TEST_HARNESS__TO_BE_RENDERED:
				setToBeRendered(TO_BE_RENDERED_EDEFAULT);
				return;
			case MApplicationPackage.TEST_HARNESS__ON_TOP:
				setOnTop(ON_TOP_EDEFAULT);
				return;
			case MApplicationPackage.TEST_HARNESS__VISIBLE:
				setVisible(VISIBLE_EDEFAULT);
				return;
			case MApplicationPackage.TEST_HARNESS__PARENT:
				setParent((MElementContainer<MUIElement>)null);
				return;
			case MApplicationPackage.TEST_HARNESS__CONTAINER_DATA:
				setContainerData(CONTAINER_DATA_EDEFAULT);
				return;
			case MApplicationPackage.TEST_HARNESS__CHILDREN:
				getChildren().clear();
				return;
			case MApplicationPackage.TEST_HARNESS__SELECTED_ELEMENT:
				setSelectedElement((MUIElement)null);
				return;
			case MApplicationPackage.TEST_HARNESS__TAG:
				setTag(TAG_EDEFAULT);
				return;
			case MApplicationPackage.TEST_HARNESS__VALUE:
				setValue(VALUE_EDEFAULT);
				return;
			case MApplicationPackage.TEST_HARNESS__INPUT_URI:
				setInputURI(INPUT_URI_EDEFAULT);
				return;
			case MApplicationPackage.TEST_HARNESS__LABEL:
				setLabel(LABEL_EDEFAULT);
				return;
			case MApplicationPackage.TEST_HARNESS__ICON_URI:
				setIconURI(ICON_URI_EDEFAULT);
				return;
			case MApplicationPackage.TEST_HARNESS__TOOLTIP:
				setTooltip(TOOLTIP_EDEFAULT);
				return;
			case MApplicationPackage.TEST_HARNESS__ENABLED:
				setEnabled(ENABLED_EDEFAULT);
				return;
			case MApplicationPackage.TEST_HARNESS__SELECTED:
				setSelected(SELECTED_EDEFAULT);
				return;
			case MApplicationPackage.TEST_HARNESS__TYPE:
				setType(TYPE_EDEFAULT);
				return;
			case MApplicationPackage.TEST_HARNESS__DIRTY:
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
			case MApplicationPackage.TEST_HARNESS__COMMAND_NAME:
				return COMMAND_NAME_EDEFAULT == null ? commandName != null : !COMMAND_NAME_EDEFAULT.equals(commandName);
			case MApplicationPackage.TEST_HARNESS__DESCRIPTION:
				return DESCRIPTION_EDEFAULT == null ? description != null : !DESCRIPTION_EDEFAULT.equals(description);
			case MApplicationPackage.TEST_HARNESS__PARAMETERS:
				return parameters != null && !parameters.isEmpty();
			case MApplicationPackage.TEST_HARNESS__CONTEXT:
				return CONTEXT_EDEFAULT == null ? context != null : !CONTEXT_EDEFAULT.equals(context);
			case MApplicationPackage.TEST_HARNESS__VARIABLES:
				return variables != null && !variables.isEmpty();
			case MApplicationPackage.TEST_HARNESS__PROPERTIES:
				return properties != null && !properties.isEmpty();
			case MApplicationPackage.TEST_HARNESS__URI:
				return URI_EDEFAULT == null ? uri != null : !URI_EDEFAULT.equals(uri);
			case MApplicationPackage.TEST_HARNESS__OBJECT:
				return OBJECT_EDEFAULT == null ? object != null : !OBJECT_EDEFAULT.equals(object);
			case MApplicationPackage.TEST_HARNESS__PERSISTED_STATE:
				return persistedState != null && !persistedState.isEmpty();
			case MApplicationPackage.TEST_HARNESS__WIDGET:
				return WIDGET_EDEFAULT == null ? widget != null : !WIDGET_EDEFAULT.equals(widget);
			case MApplicationPackage.TEST_HARNESS__RENDERER:
				return RENDERER_EDEFAULT == null ? renderer != null : !RENDERER_EDEFAULT.equals(renderer);
			case MApplicationPackage.TEST_HARNESS__TO_BE_RENDERED:
				return toBeRendered != TO_BE_RENDERED_EDEFAULT;
			case MApplicationPackage.TEST_HARNESS__ON_TOP:
				return onTop != ON_TOP_EDEFAULT;
			case MApplicationPackage.TEST_HARNESS__VISIBLE:
				return visible != VISIBLE_EDEFAULT;
			case MApplicationPackage.TEST_HARNESS__PARENT:
				return getParent() != null;
			case MApplicationPackage.TEST_HARNESS__CONTAINER_DATA:
				return CONTAINER_DATA_EDEFAULT == null ? containerData != null : !CONTAINER_DATA_EDEFAULT.equals(containerData);
			case MApplicationPackage.TEST_HARNESS__CHILDREN:
				return children != null && !children.isEmpty();
			case MApplicationPackage.TEST_HARNESS__SELECTED_ELEMENT:
				return selectedElement != null;
			case MApplicationPackage.TEST_HARNESS__TAG:
				return TAG_EDEFAULT == null ? tag != null : !TAG_EDEFAULT.equals(tag);
			case MApplicationPackage.TEST_HARNESS__VALUE:
				return VALUE_EDEFAULT == null ? value != null : !VALUE_EDEFAULT.equals(value);
			case MApplicationPackage.TEST_HARNESS__INPUT_URI:
				return INPUT_URI_EDEFAULT == null ? inputURI != null : !INPUT_URI_EDEFAULT.equals(inputURI);
			case MApplicationPackage.TEST_HARNESS__LABEL:
				return LABEL_EDEFAULT == null ? label != null : !LABEL_EDEFAULT.equals(label);
			case MApplicationPackage.TEST_HARNESS__ICON_URI:
				return ICON_URI_EDEFAULT == null ? iconURI != null : !ICON_URI_EDEFAULT.equals(iconURI);
			case MApplicationPackage.TEST_HARNESS__TOOLTIP:
				return TOOLTIP_EDEFAULT == null ? tooltip != null : !TOOLTIP_EDEFAULT.equals(tooltip);
			case MApplicationPackage.TEST_HARNESS__ENABLED:
				return enabled != ENABLED_EDEFAULT;
			case MApplicationPackage.TEST_HARNESS__SELECTED:
				return selected != SELECTED_EDEFAULT;
			case MApplicationPackage.TEST_HARNESS__TYPE:
				return type != TYPE_EDEFAULT;
			case MApplicationPackage.TEST_HARNESS__DIRTY:
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
		if (baseClass == MCommand.class) {
			switch (derivedFeatureID) {
				case MApplicationPackage.TEST_HARNESS__COMMAND_NAME: return MApplicationPackage.COMMAND__COMMAND_NAME;
				case MApplicationPackage.TEST_HARNESS__DESCRIPTION: return MApplicationPackage.COMMAND__DESCRIPTION;
				case MApplicationPackage.TEST_HARNESS__PARAMETERS: return MApplicationPackage.COMMAND__PARAMETERS;
				default: return -1;
			}
		}
		if (baseClass == MContext.class) {
			switch (derivedFeatureID) {
				case MApplicationPackage.TEST_HARNESS__CONTEXT: return MApplicationPackage.CONTEXT__CONTEXT;
				case MApplicationPackage.TEST_HARNESS__VARIABLES: return MApplicationPackage.CONTEXT__VARIABLES;
				case MApplicationPackage.TEST_HARNESS__PROPERTIES: return MApplicationPackage.CONTEXT__PROPERTIES;
				default: return -1;
			}
		}
		if (baseClass == MContribution.class) {
			switch (derivedFeatureID) {
				case MApplicationPackage.TEST_HARNESS__URI: return MApplicationPackage.CONTRIBUTION__URI;
				case MApplicationPackage.TEST_HARNESS__OBJECT: return MApplicationPackage.CONTRIBUTION__OBJECT;
				case MApplicationPackage.TEST_HARNESS__PERSISTED_STATE: return MApplicationPackage.CONTRIBUTION__PERSISTED_STATE;
				default: return -1;
			}
		}
		if (baseClass == MUIElement.class) {
			switch (derivedFeatureID) {
				case MApplicationPackage.TEST_HARNESS__WIDGET: return MApplicationPackage.UI_ELEMENT__WIDGET;
				case MApplicationPackage.TEST_HARNESS__RENDERER: return MApplicationPackage.UI_ELEMENT__RENDERER;
				case MApplicationPackage.TEST_HARNESS__TO_BE_RENDERED: return MApplicationPackage.UI_ELEMENT__TO_BE_RENDERED;
				case MApplicationPackage.TEST_HARNESS__ON_TOP: return MApplicationPackage.UI_ELEMENT__ON_TOP;
				case MApplicationPackage.TEST_HARNESS__VISIBLE: return MApplicationPackage.UI_ELEMENT__VISIBLE;
				case MApplicationPackage.TEST_HARNESS__PARENT: return MApplicationPackage.UI_ELEMENT__PARENT;
				case MApplicationPackage.TEST_HARNESS__CONTAINER_DATA: return MApplicationPackage.UI_ELEMENT__CONTAINER_DATA;
				default: return -1;
			}
		}
		if (baseClass == MElementContainer.class) {
			switch (derivedFeatureID) {
				case MApplicationPackage.TEST_HARNESS__CHILDREN: return MApplicationPackage.ELEMENT_CONTAINER__CHILDREN;
				case MApplicationPackage.TEST_HARNESS__SELECTED_ELEMENT: return MApplicationPackage.ELEMENT_CONTAINER__SELECTED_ELEMENT;
				default: return -1;
			}
		}
		if (baseClass == MParameter.class) {
			switch (derivedFeatureID) {
				case MApplicationPackage.TEST_HARNESS__TAG: return MApplicationPackage.PARAMETER__TAG;
				case MApplicationPackage.TEST_HARNESS__VALUE: return MApplicationPackage.PARAMETER__VALUE;
				default: return -1;
			}
		}
		if (baseClass == MInput.class) {
			switch (derivedFeatureID) {
				case MApplicationPackage.TEST_HARNESS__INPUT_URI: return MApplicationPackage.INPUT__INPUT_URI;
				default: return -1;
			}
		}
		if (baseClass == MUILabel.class) {
			switch (derivedFeatureID) {
				case MApplicationPackage.TEST_HARNESS__LABEL: return MApplicationPackage.UI_LABEL__LABEL;
				case MApplicationPackage.TEST_HARNESS__ICON_URI: return MApplicationPackage.UI_LABEL__ICON_URI;
				case MApplicationPackage.TEST_HARNESS__TOOLTIP: return MApplicationPackage.UI_LABEL__TOOLTIP;
				default: return -1;
			}
		}
		if (baseClass == MItem.class) {
			switch (derivedFeatureID) {
				case MApplicationPackage.TEST_HARNESS__ENABLED: return MApplicationPackage.ITEM__ENABLED;
				case MApplicationPackage.TEST_HARNESS__SELECTED: return MApplicationPackage.ITEM__SELECTED;
				case MApplicationPackage.TEST_HARNESS__TYPE: return MApplicationPackage.ITEM__TYPE;
				default: return -1;
			}
		}
		if (baseClass == MDirtyable.class) {
			switch (derivedFeatureID) {
				case MApplicationPackage.TEST_HARNESS__DIRTY: return MApplicationPackage.DIRTYABLE__DIRTY;
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
		if (baseClass == MCommand.class) {
			switch (baseFeatureID) {
				case MApplicationPackage.COMMAND__COMMAND_NAME: return MApplicationPackage.TEST_HARNESS__COMMAND_NAME;
				case MApplicationPackage.COMMAND__DESCRIPTION: return MApplicationPackage.TEST_HARNESS__DESCRIPTION;
				case MApplicationPackage.COMMAND__PARAMETERS: return MApplicationPackage.TEST_HARNESS__PARAMETERS;
				default: return -1;
			}
		}
		if (baseClass == MContext.class) {
			switch (baseFeatureID) {
				case MApplicationPackage.CONTEXT__CONTEXT: return MApplicationPackage.TEST_HARNESS__CONTEXT;
				case MApplicationPackage.CONTEXT__VARIABLES: return MApplicationPackage.TEST_HARNESS__VARIABLES;
				case MApplicationPackage.CONTEXT__PROPERTIES: return MApplicationPackage.TEST_HARNESS__PROPERTIES;
				default: return -1;
			}
		}
		if (baseClass == MContribution.class) {
			switch (baseFeatureID) {
				case MApplicationPackage.CONTRIBUTION__URI: return MApplicationPackage.TEST_HARNESS__URI;
				case MApplicationPackage.CONTRIBUTION__OBJECT: return MApplicationPackage.TEST_HARNESS__OBJECT;
				case MApplicationPackage.CONTRIBUTION__PERSISTED_STATE: return MApplicationPackage.TEST_HARNESS__PERSISTED_STATE;
				default: return -1;
			}
		}
		if (baseClass == MUIElement.class) {
			switch (baseFeatureID) {
				case MApplicationPackage.UI_ELEMENT__WIDGET: return MApplicationPackage.TEST_HARNESS__WIDGET;
				case MApplicationPackage.UI_ELEMENT__RENDERER: return MApplicationPackage.TEST_HARNESS__RENDERER;
				case MApplicationPackage.UI_ELEMENT__TO_BE_RENDERED: return MApplicationPackage.TEST_HARNESS__TO_BE_RENDERED;
				case MApplicationPackage.UI_ELEMENT__ON_TOP: return MApplicationPackage.TEST_HARNESS__ON_TOP;
				case MApplicationPackage.UI_ELEMENT__VISIBLE: return MApplicationPackage.TEST_HARNESS__VISIBLE;
				case MApplicationPackage.UI_ELEMENT__PARENT: return MApplicationPackage.TEST_HARNESS__PARENT;
				case MApplicationPackage.UI_ELEMENT__CONTAINER_DATA: return MApplicationPackage.TEST_HARNESS__CONTAINER_DATA;
				default: return -1;
			}
		}
		if (baseClass == MElementContainer.class) {
			switch (baseFeatureID) {
				case MApplicationPackage.ELEMENT_CONTAINER__CHILDREN: return MApplicationPackage.TEST_HARNESS__CHILDREN;
				case MApplicationPackage.ELEMENT_CONTAINER__SELECTED_ELEMENT: return MApplicationPackage.TEST_HARNESS__SELECTED_ELEMENT;
				default: return -1;
			}
		}
		if (baseClass == MParameter.class) {
			switch (baseFeatureID) {
				case MApplicationPackage.PARAMETER__TAG: return MApplicationPackage.TEST_HARNESS__TAG;
				case MApplicationPackage.PARAMETER__VALUE: return MApplicationPackage.TEST_HARNESS__VALUE;
				default: return -1;
			}
		}
		if (baseClass == MInput.class) {
			switch (baseFeatureID) {
				case MApplicationPackage.INPUT__INPUT_URI: return MApplicationPackage.TEST_HARNESS__INPUT_URI;
				default: return -1;
			}
		}
		if (baseClass == MUILabel.class) {
			switch (baseFeatureID) {
				case MApplicationPackage.UI_LABEL__LABEL: return MApplicationPackage.TEST_HARNESS__LABEL;
				case MApplicationPackage.UI_LABEL__ICON_URI: return MApplicationPackage.TEST_HARNESS__ICON_URI;
				case MApplicationPackage.UI_LABEL__TOOLTIP: return MApplicationPackage.TEST_HARNESS__TOOLTIP;
				default: return -1;
			}
		}
		if (baseClass == MItem.class) {
			switch (baseFeatureID) {
				case MApplicationPackage.ITEM__ENABLED: return MApplicationPackage.TEST_HARNESS__ENABLED;
				case MApplicationPackage.ITEM__SELECTED: return MApplicationPackage.TEST_HARNESS__SELECTED;
				case MApplicationPackage.ITEM__TYPE: return MApplicationPackage.TEST_HARNESS__TYPE;
				default: return -1;
			}
		}
		if (baseClass == MDirtyable.class) {
			switch (baseFeatureID) {
				case MApplicationPackage.DIRTYABLE__DIRTY: return MApplicationPackage.TEST_HARNESS__DIRTY;
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
		result.append(" (commandName: "); //$NON-NLS-1$
		result.append(commandName);
		result.append(", description: "); //$NON-NLS-1$
		result.append(description);
		result.append(", context: "); //$NON-NLS-1$
		result.append(context);
		result.append(", variables: "); //$NON-NLS-1$
		result.append(variables);
		result.append(", URI: "); //$NON-NLS-1$
		result.append(uri);
		result.append(", object: "); //$NON-NLS-1$
		result.append(object);
		result.append(", widget: "); //$NON-NLS-1$
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
		result.append(", tag: "); //$NON-NLS-1$
		result.append(tag);
		result.append(", value: "); //$NON-NLS-1$
		result.append(value);
		result.append(", inputURI: "); //$NON-NLS-1$
		result.append(inputURI);
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
		result.append(", dirty: "); //$NON-NLS-1$
		result.append(dirty);
		result.append(')');
		return result.toString();
	}

} //TestHarnessImpl
