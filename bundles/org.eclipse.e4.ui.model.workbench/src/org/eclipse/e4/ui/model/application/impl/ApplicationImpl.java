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
import java.util.List;
import java.util.Map;

import org.eclipse.e4.core.contexts.IEclipseContext;

import org.eclipse.e4.ui.model.application.MAddon;
import org.eclipse.e4.ui.model.application.MApplication;

import org.eclipse.e4.ui.model.application.commands.MBindingContext;
import org.eclipse.e4.ui.model.application.commands.MBindingTable;
import org.eclipse.e4.ui.model.application.commands.MBindingTableContainer;
import org.eclipse.e4.ui.model.application.commands.MBindings;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.commands.MHandler;
import org.eclipse.e4.ui.model.application.commands.MHandlerContainer;

import org.eclipse.e4.ui.model.application.commands.impl.CommandsPackageImpl;

import org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor;
import org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptorContainer;

import org.eclipse.e4.ui.model.application.descriptor.basic.impl.BasicPackageImpl;

import org.eclipse.e4.ui.model.application.ui.MContext;

import org.eclipse.e4.ui.model.application.ui.basic.MWindow;

import org.eclipse.e4.ui.model.application.ui.impl.ElementContainerImpl;
import org.eclipse.e4.ui.model.application.ui.impl.UiPackageImpl;

import org.eclipse.e4.ui.model.application.ui.menu.MMenuContribution;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuContributions;
import org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl;
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
 * An implementation of the model object '<em><b>Application</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.ApplicationImpl#getContext <em>Context</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.ApplicationImpl#getVariables <em>Variables</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.ApplicationImpl#getProperties <em>Properties</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.ApplicationImpl#getHandlers <em>Handlers</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.ApplicationImpl#getBindingTables <em>Binding Tables</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.ApplicationImpl#getRootContext <em>Root Context</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.ApplicationImpl#getDescriptors <em>Descriptors</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.ApplicationImpl#getBindingContexts <em>Binding Contexts</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.ApplicationImpl#getMenuContributions <em>Menu Contributions</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.ApplicationImpl#getCommands <em>Commands</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.ApplicationImpl#getAddons <em>Addons</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class ApplicationImpl extends ElementContainerImpl<MWindow> implements MApplication {
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
	 * The cached value of the '{@link #getHandlers() <em>Handlers</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getHandlers()
	 * @generated
	 * @ordered
	 */
	protected EList<MHandler> handlers;

	/**
	 * The cached value of the '{@link #getBindingTables() <em>Binding Tables</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getBindingTables()
	 * @generated
	 * @ordered
	 */
	protected EList<MBindingTable> bindingTables;

	/**
	 * The cached value of the '{@link #getRootContext() <em>Root Context</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getRootContext()
	 * @generated
	 * @ordered
	 */
	protected MBindingContext rootContext;

	/**
	 * The cached value of the '{@link #getDescriptors() <em>Descriptors</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDescriptors()
	 * @generated
	 * @ordered
	 */
	protected EList<MPartDescriptor> descriptors;

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
	 * The cached value of the '{@link #getMenuContributions() <em>Menu Contributions</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMenuContributions()
	 * @generated
	 * @ordered
	 */
	protected EList<MMenuContribution> menuContributions;

	/**
	 * The cached value of the '{@link #getCommands() <em>Commands</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCommands()
	 * @generated
	 * @ordered
	 */
	protected EList<MCommand> commands;

	/**
	 * The cached value of the '{@link #getAddons() <em>Addons</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getAddons()
	 * @generated
	 * @ordered
	 */
	protected EList<MAddon> addons;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ApplicationImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ApplicationPackageImpl.Literals.APPLICATION;
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
			eNotify(new ENotificationImpl(this, Notification.SET, ApplicationPackageImpl.APPLICATION__CONTEXT, oldContext, context));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public List<String> getVariables() {
		if (variables == null) {
			variables = new EDataTypeUniqueEList<String>(String.class, this, ApplicationPackageImpl.APPLICATION__VARIABLES);
		}
		return variables;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Map<String, String> getProperties() {
		if (properties == null) {
			properties = new EcoreEMap<String,String>(ApplicationPackageImpl.Literals.STRING_TO_STRING_MAP, StringToStringMapImpl.class, this, ApplicationPackageImpl.APPLICATION__PROPERTIES);
		}
		return properties.map();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public List<MHandler> getHandlers() {
		if (handlers == null) {
			handlers = new EObjectContainmentEList<MHandler>(MHandler.class, this, ApplicationPackageImpl.APPLICATION__HANDLERS);
		}
		return handlers;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public List<MBindingTable> getBindingTables() {
		if (bindingTables == null) {
			bindingTables = new EObjectContainmentEList<MBindingTable>(MBindingTable.class, this, ApplicationPackageImpl.APPLICATION__BINDING_TABLES);
		}
		return bindingTables;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MBindingContext getRootContext() {
		return rootContext;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetRootContext(MBindingContext newRootContext, NotificationChain msgs) {
		MBindingContext oldRootContext = rootContext;
		rootContext = newRootContext;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, ApplicationPackageImpl.APPLICATION__ROOT_CONTEXT, oldRootContext, newRootContext);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setRootContext(MBindingContext newRootContext) {
		if (newRootContext != rootContext) {
			NotificationChain msgs = null;
			if (rootContext != null)
				msgs = ((InternalEObject)rootContext).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - ApplicationPackageImpl.APPLICATION__ROOT_CONTEXT, null, msgs);
			if (newRootContext != null)
				msgs = ((InternalEObject)newRootContext).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - ApplicationPackageImpl.APPLICATION__ROOT_CONTEXT, null, msgs);
			msgs = basicSetRootContext(newRootContext, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ApplicationPackageImpl.APPLICATION__ROOT_CONTEXT, newRootContext, newRootContext));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public List<MPartDescriptor> getDescriptors() {
		if (descriptors == null) {
			descriptors = new EObjectContainmentEList<MPartDescriptor>(MPartDescriptor.class, this, ApplicationPackageImpl.APPLICATION__DESCRIPTORS);
		}
		return descriptors;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public List<String> getBindingContexts() {
		if (bindingContexts == null) {
			bindingContexts = new EDataTypeUniqueEList<String>(String.class, this, ApplicationPackageImpl.APPLICATION__BINDING_CONTEXTS);
		}
		return bindingContexts;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public List<MMenuContribution> getMenuContributions() {
		if (menuContributions == null) {
			menuContributions = new EObjectContainmentEList<MMenuContribution>(MMenuContribution.class, this, ApplicationPackageImpl.APPLICATION__MENU_CONTRIBUTIONS);
		}
		return menuContributions;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public List<MCommand> getCommands() {
		if (commands == null) {
			commands = new EObjectContainmentEList<MCommand>(MCommand.class, this, ApplicationPackageImpl.APPLICATION__COMMANDS);
		}
		return commands;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public List<MAddon> getAddons() {
		if (addons == null) {
			addons = new EObjectContainmentEList<MAddon>(MAddon.class, this, ApplicationPackageImpl.APPLICATION__ADDONS);
		}
		return addons;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case ApplicationPackageImpl.APPLICATION__PROPERTIES:
				return ((InternalEList<?>)((EMap.InternalMapView<String, String>)getProperties()).eMap()).basicRemove(otherEnd, msgs);
			case ApplicationPackageImpl.APPLICATION__HANDLERS:
				return ((InternalEList<?>)getHandlers()).basicRemove(otherEnd, msgs);
			case ApplicationPackageImpl.APPLICATION__BINDING_TABLES:
				return ((InternalEList<?>)getBindingTables()).basicRemove(otherEnd, msgs);
			case ApplicationPackageImpl.APPLICATION__ROOT_CONTEXT:
				return basicSetRootContext(null, msgs);
			case ApplicationPackageImpl.APPLICATION__DESCRIPTORS:
				return ((InternalEList<?>)getDescriptors()).basicRemove(otherEnd, msgs);
			case ApplicationPackageImpl.APPLICATION__MENU_CONTRIBUTIONS:
				return ((InternalEList<?>)getMenuContributions()).basicRemove(otherEnd, msgs);
			case ApplicationPackageImpl.APPLICATION__COMMANDS:
				return ((InternalEList<?>)getCommands()).basicRemove(otherEnd, msgs);
			case ApplicationPackageImpl.APPLICATION__ADDONS:
				return ((InternalEList<?>)getAddons()).basicRemove(otherEnd, msgs);
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
			case ApplicationPackageImpl.APPLICATION__CONTEXT:
				return getContext();
			case ApplicationPackageImpl.APPLICATION__VARIABLES:
				return getVariables();
			case ApplicationPackageImpl.APPLICATION__PROPERTIES:
				if (coreType) return ((EMap.InternalMapView<String, String>)getProperties()).eMap();
				else return getProperties();
			case ApplicationPackageImpl.APPLICATION__HANDLERS:
				return getHandlers();
			case ApplicationPackageImpl.APPLICATION__BINDING_TABLES:
				return getBindingTables();
			case ApplicationPackageImpl.APPLICATION__ROOT_CONTEXT:
				return getRootContext();
			case ApplicationPackageImpl.APPLICATION__DESCRIPTORS:
				return getDescriptors();
			case ApplicationPackageImpl.APPLICATION__BINDING_CONTEXTS:
				return getBindingContexts();
			case ApplicationPackageImpl.APPLICATION__MENU_CONTRIBUTIONS:
				return getMenuContributions();
			case ApplicationPackageImpl.APPLICATION__COMMANDS:
				return getCommands();
			case ApplicationPackageImpl.APPLICATION__ADDONS:
				return getAddons();
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
			case ApplicationPackageImpl.APPLICATION__CONTEXT:
				setContext((IEclipseContext)newValue);
				return;
			case ApplicationPackageImpl.APPLICATION__VARIABLES:
				getVariables().clear();
				getVariables().addAll((Collection<? extends String>)newValue);
				return;
			case ApplicationPackageImpl.APPLICATION__PROPERTIES:
				((EStructuralFeature.Setting)((EMap.InternalMapView<String, String>)getProperties()).eMap()).set(newValue);
				return;
			case ApplicationPackageImpl.APPLICATION__HANDLERS:
				getHandlers().clear();
				getHandlers().addAll((Collection<? extends MHandler>)newValue);
				return;
			case ApplicationPackageImpl.APPLICATION__BINDING_TABLES:
				getBindingTables().clear();
				getBindingTables().addAll((Collection<? extends MBindingTable>)newValue);
				return;
			case ApplicationPackageImpl.APPLICATION__ROOT_CONTEXT:
				setRootContext((MBindingContext)newValue);
				return;
			case ApplicationPackageImpl.APPLICATION__DESCRIPTORS:
				getDescriptors().clear();
				getDescriptors().addAll((Collection<? extends MPartDescriptor>)newValue);
				return;
			case ApplicationPackageImpl.APPLICATION__BINDING_CONTEXTS:
				getBindingContexts().clear();
				getBindingContexts().addAll((Collection<? extends String>)newValue);
				return;
			case ApplicationPackageImpl.APPLICATION__MENU_CONTRIBUTIONS:
				getMenuContributions().clear();
				getMenuContributions().addAll((Collection<? extends MMenuContribution>)newValue);
				return;
			case ApplicationPackageImpl.APPLICATION__COMMANDS:
				getCommands().clear();
				getCommands().addAll((Collection<? extends MCommand>)newValue);
				return;
			case ApplicationPackageImpl.APPLICATION__ADDONS:
				getAddons().clear();
				getAddons().addAll((Collection<? extends MAddon>)newValue);
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
			case ApplicationPackageImpl.APPLICATION__CONTEXT:
				setContext(CONTEXT_EDEFAULT);
				return;
			case ApplicationPackageImpl.APPLICATION__VARIABLES:
				getVariables().clear();
				return;
			case ApplicationPackageImpl.APPLICATION__PROPERTIES:
				getProperties().clear();
				return;
			case ApplicationPackageImpl.APPLICATION__HANDLERS:
				getHandlers().clear();
				return;
			case ApplicationPackageImpl.APPLICATION__BINDING_TABLES:
				getBindingTables().clear();
				return;
			case ApplicationPackageImpl.APPLICATION__ROOT_CONTEXT:
				setRootContext((MBindingContext)null);
				return;
			case ApplicationPackageImpl.APPLICATION__DESCRIPTORS:
				getDescriptors().clear();
				return;
			case ApplicationPackageImpl.APPLICATION__BINDING_CONTEXTS:
				getBindingContexts().clear();
				return;
			case ApplicationPackageImpl.APPLICATION__MENU_CONTRIBUTIONS:
				getMenuContributions().clear();
				return;
			case ApplicationPackageImpl.APPLICATION__COMMANDS:
				getCommands().clear();
				return;
			case ApplicationPackageImpl.APPLICATION__ADDONS:
				getAddons().clear();
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
			case ApplicationPackageImpl.APPLICATION__CONTEXT:
				return CONTEXT_EDEFAULT == null ? context != null : !CONTEXT_EDEFAULT.equals(context);
			case ApplicationPackageImpl.APPLICATION__VARIABLES:
				return variables != null && !variables.isEmpty();
			case ApplicationPackageImpl.APPLICATION__PROPERTIES:
				return properties != null && !properties.isEmpty();
			case ApplicationPackageImpl.APPLICATION__HANDLERS:
				return handlers != null && !handlers.isEmpty();
			case ApplicationPackageImpl.APPLICATION__BINDING_TABLES:
				return bindingTables != null && !bindingTables.isEmpty();
			case ApplicationPackageImpl.APPLICATION__ROOT_CONTEXT:
				return rootContext != null;
			case ApplicationPackageImpl.APPLICATION__DESCRIPTORS:
				return descriptors != null && !descriptors.isEmpty();
			case ApplicationPackageImpl.APPLICATION__BINDING_CONTEXTS:
				return bindingContexts != null && !bindingContexts.isEmpty();
			case ApplicationPackageImpl.APPLICATION__MENU_CONTRIBUTIONS:
				return menuContributions != null && !menuContributions.isEmpty();
			case ApplicationPackageImpl.APPLICATION__COMMANDS:
				return commands != null && !commands.isEmpty();
			case ApplicationPackageImpl.APPLICATION__ADDONS:
				return addons != null && !addons.isEmpty();
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
		if (baseClass == MContext.class) {
			switch (derivedFeatureID) {
				case ApplicationPackageImpl.APPLICATION__CONTEXT: return UiPackageImpl.CONTEXT__CONTEXT;
				case ApplicationPackageImpl.APPLICATION__VARIABLES: return UiPackageImpl.CONTEXT__VARIABLES;
				case ApplicationPackageImpl.APPLICATION__PROPERTIES: return UiPackageImpl.CONTEXT__PROPERTIES;
				default: return -1;
			}
		}
		if (baseClass == MHandlerContainer.class) {
			switch (derivedFeatureID) {
				case ApplicationPackageImpl.APPLICATION__HANDLERS: return CommandsPackageImpl.HANDLER_CONTAINER__HANDLERS;
				default: return -1;
			}
		}
		if (baseClass == MBindingTableContainer.class) {
			switch (derivedFeatureID) {
				case ApplicationPackageImpl.APPLICATION__BINDING_TABLES: return CommandsPackageImpl.BINDING_TABLE_CONTAINER__BINDING_TABLES;
				case ApplicationPackageImpl.APPLICATION__ROOT_CONTEXT: return CommandsPackageImpl.BINDING_TABLE_CONTAINER__ROOT_CONTEXT;
				default: return -1;
			}
		}
		if (baseClass == MPartDescriptorContainer.class) {
			switch (derivedFeatureID) {
				case ApplicationPackageImpl.APPLICATION__DESCRIPTORS: return BasicPackageImpl.PART_DESCRIPTOR_CONTAINER__DESCRIPTORS;
				default: return -1;
			}
		}
		if (baseClass == MBindings.class) {
			switch (derivedFeatureID) {
				case ApplicationPackageImpl.APPLICATION__BINDING_CONTEXTS: return CommandsPackageImpl.BINDINGS__BINDING_CONTEXTS;
				default: return -1;
			}
		}
		if (baseClass == MMenuContributions.class) {
			switch (derivedFeatureID) {
				case ApplicationPackageImpl.APPLICATION__MENU_CONTRIBUTIONS: return MenuPackageImpl.MENU_CONTRIBUTIONS__MENU_CONTRIBUTIONS;
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
		if (baseClass == MContext.class) {
			switch (baseFeatureID) {
				case UiPackageImpl.CONTEXT__CONTEXT: return ApplicationPackageImpl.APPLICATION__CONTEXT;
				case UiPackageImpl.CONTEXT__VARIABLES: return ApplicationPackageImpl.APPLICATION__VARIABLES;
				case UiPackageImpl.CONTEXT__PROPERTIES: return ApplicationPackageImpl.APPLICATION__PROPERTIES;
				default: return -1;
			}
		}
		if (baseClass == MHandlerContainer.class) {
			switch (baseFeatureID) {
				case CommandsPackageImpl.HANDLER_CONTAINER__HANDLERS: return ApplicationPackageImpl.APPLICATION__HANDLERS;
				default: return -1;
			}
		}
		if (baseClass == MBindingTableContainer.class) {
			switch (baseFeatureID) {
				case CommandsPackageImpl.BINDING_TABLE_CONTAINER__BINDING_TABLES: return ApplicationPackageImpl.APPLICATION__BINDING_TABLES;
				case CommandsPackageImpl.BINDING_TABLE_CONTAINER__ROOT_CONTEXT: return ApplicationPackageImpl.APPLICATION__ROOT_CONTEXT;
				default: return -1;
			}
		}
		if (baseClass == MPartDescriptorContainer.class) {
			switch (baseFeatureID) {
				case BasicPackageImpl.PART_DESCRIPTOR_CONTAINER__DESCRIPTORS: return ApplicationPackageImpl.APPLICATION__DESCRIPTORS;
				default: return -1;
			}
		}
		if (baseClass == MBindings.class) {
			switch (baseFeatureID) {
				case CommandsPackageImpl.BINDINGS__BINDING_CONTEXTS: return ApplicationPackageImpl.APPLICATION__BINDING_CONTEXTS;
				default: return -1;
			}
		}
		if (baseClass == MMenuContributions.class) {
			switch (baseFeatureID) {
				case MenuPackageImpl.MENU_CONTRIBUTIONS__MENU_CONTRIBUTIONS: return ApplicationPackageImpl.APPLICATION__MENU_CONTRIBUTIONS;
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
		result.append(" (context: "); //$NON-NLS-1$
		result.append(context);
		result.append(", variables: "); //$NON-NLS-1$
		result.append(variables);
		result.append(", bindingContexts: "); //$NON-NLS-1$
		result.append(bindingContexts);
		result.append(')');
		return result.toString();
	}

} //ApplicationImpl
