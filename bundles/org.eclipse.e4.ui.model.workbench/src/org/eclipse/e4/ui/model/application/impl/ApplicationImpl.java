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
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationPackage;
import org.eclipse.e4.ui.model.application.MBindingContainer;
import org.eclipse.e4.ui.model.application.MCommand;
import org.eclipse.e4.ui.model.application.MContext;
import org.eclipse.e4.ui.model.application.MHandler;
import org.eclipse.e4.ui.model.application.MHandlerContainer;
import org.eclipse.e4.ui.model.application.MKeyBinding;
import org.eclipse.e4.ui.model.application.MPartDescriptor;
import org.eclipse.e4.ui.model.application.MPartDescriptorContainer;
import org.eclipse.e4.ui.model.application.MWindow;

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
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.ApplicationImpl#getBindings <em>Bindings</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.ApplicationImpl#getDescriptors <em>Descriptors</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.ApplicationImpl#getCommands <em>Commands</em>}</li>
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
	 * The cached value of the '{@link #getBindings() <em>Bindings</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getBindings()
	 * @generated
	 * @ordered
	 */
	protected EList<MKeyBinding> bindings;

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
	 * The cached value of the '{@link #getCommands() <em>Commands</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCommands()
	 * @generated
	 * @ordered
	 */
	protected EList<MCommand> commands;

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
		return MApplicationPackage.Literals.APPLICATION;
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
			eNotify(new ENotificationImpl(this, Notification.SET, MApplicationPackage.APPLICATION__CONTEXT, oldContext, context));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<String> getVariables() {
		if (variables == null) {
			variables = new EDataTypeUniqueEList<String>(String.class, this, MApplicationPackage.APPLICATION__VARIABLES);
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
			properties = new EcoreEMap<String,String>(MApplicationPackage.Literals.STRING_TO_STRING_MAP, StringToStringMapImpl.class, this, MApplicationPackage.APPLICATION__PROPERTIES);
		}
		return properties;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<MHandler> getHandlers() {
		if (handlers == null) {
			handlers = new EObjectContainmentEList<MHandler>(MHandler.class, this, MApplicationPackage.APPLICATION__HANDLERS);
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
			bindings = new EObjectContainmentEList<MKeyBinding>(MKeyBinding.class, this, MApplicationPackage.APPLICATION__BINDINGS);
		}
		return bindings;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<MPartDescriptor> getDescriptors() {
		if (descriptors == null) {
			descriptors = new EObjectContainmentEList<MPartDescriptor>(MPartDescriptor.class, this, MApplicationPackage.APPLICATION__DESCRIPTORS);
		}
		return descriptors;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<MCommand> getCommands() {
		if (commands == null) {
			commands = new EObjectContainmentEList<MCommand>(MCommand.class, this, MApplicationPackage.APPLICATION__COMMANDS);
		}
		return commands;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case MApplicationPackage.APPLICATION__PROPERTIES:
				return ((InternalEList<?>)getProperties()).basicRemove(otherEnd, msgs);
			case MApplicationPackage.APPLICATION__HANDLERS:
				return ((InternalEList<?>)getHandlers()).basicRemove(otherEnd, msgs);
			case MApplicationPackage.APPLICATION__BINDINGS:
				return ((InternalEList<?>)getBindings()).basicRemove(otherEnd, msgs);
			case MApplicationPackage.APPLICATION__DESCRIPTORS:
				return ((InternalEList<?>)getDescriptors()).basicRemove(otherEnd, msgs);
			case MApplicationPackage.APPLICATION__COMMANDS:
				return ((InternalEList<?>)getCommands()).basicRemove(otherEnd, msgs);
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
			case MApplicationPackage.APPLICATION__CONTEXT:
				return getContext();
			case MApplicationPackage.APPLICATION__VARIABLES:
				return getVariables();
			case MApplicationPackage.APPLICATION__PROPERTIES:
				if (coreType) return getProperties();
				else return getProperties().map();
			case MApplicationPackage.APPLICATION__HANDLERS:
				return getHandlers();
			case MApplicationPackage.APPLICATION__BINDINGS:
				return getBindings();
			case MApplicationPackage.APPLICATION__DESCRIPTORS:
				return getDescriptors();
			case MApplicationPackage.APPLICATION__COMMANDS:
				return getCommands();
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
			case MApplicationPackage.APPLICATION__CONTEXT:
				setContext((IEclipseContext)newValue);
				return;
			case MApplicationPackage.APPLICATION__VARIABLES:
				getVariables().clear();
				getVariables().addAll((Collection<? extends String>)newValue);
				return;
			case MApplicationPackage.APPLICATION__PROPERTIES:
				((EStructuralFeature.Setting)getProperties()).set(newValue);
				return;
			case MApplicationPackage.APPLICATION__HANDLERS:
				getHandlers().clear();
				getHandlers().addAll((Collection<? extends MHandler>)newValue);
				return;
			case MApplicationPackage.APPLICATION__BINDINGS:
				getBindings().clear();
				getBindings().addAll((Collection<? extends MKeyBinding>)newValue);
				return;
			case MApplicationPackage.APPLICATION__DESCRIPTORS:
				getDescriptors().clear();
				getDescriptors().addAll((Collection<? extends MPartDescriptor>)newValue);
				return;
			case MApplicationPackage.APPLICATION__COMMANDS:
				getCommands().clear();
				getCommands().addAll((Collection<? extends MCommand>)newValue);
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
			case MApplicationPackage.APPLICATION__CONTEXT:
				setContext(CONTEXT_EDEFAULT);
				return;
			case MApplicationPackage.APPLICATION__VARIABLES:
				getVariables().clear();
				return;
			case MApplicationPackage.APPLICATION__PROPERTIES:
				getProperties().clear();
				return;
			case MApplicationPackage.APPLICATION__HANDLERS:
				getHandlers().clear();
				return;
			case MApplicationPackage.APPLICATION__BINDINGS:
				getBindings().clear();
				return;
			case MApplicationPackage.APPLICATION__DESCRIPTORS:
				getDescriptors().clear();
				return;
			case MApplicationPackage.APPLICATION__COMMANDS:
				getCommands().clear();
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
			case MApplicationPackage.APPLICATION__CONTEXT:
				return CONTEXT_EDEFAULT == null ? context != null : !CONTEXT_EDEFAULT.equals(context);
			case MApplicationPackage.APPLICATION__VARIABLES:
				return variables != null && !variables.isEmpty();
			case MApplicationPackage.APPLICATION__PROPERTIES:
				return properties != null && !properties.isEmpty();
			case MApplicationPackage.APPLICATION__HANDLERS:
				return handlers != null && !handlers.isEmpty();
			case MApplicationPackage.APPLICATION__BINDINGS:
				return bindings != null && !bindings.isEmpty();
			case MApplicationPackage.APPLICATION__DESCRIPTORS:
				return descriptors != null && !descriptors.isEmpty();
			case MApplicationPackage.APPLICATION__COMMANDS:
				return commands != null && !commands.isEmpty();
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
				case MApplicationPackage.APPLICATION__CONTEXT: return MApplicationPackage.CONTEXT__CONTEXT;
				case MApplicationPackage.APPLICATION__VARIABLES: return MApplicationPackage.CONTEXT__VARIABLES;
				case MApplicationPackage.APPLICATION__PROPERTIES: return MApplicationPackage.CONTEXT__PROPERTIES;
				default: return -1;
			}
		}
		if (baseClass == MHandlerContainer.class) {
			switch (derivedFeatureID) {
				case MApplicationPackage.APPLICATION__HANDLERS: return MApplicationPackage.HANDLER_CONTAINER__HANDLERS;
				default: return -1;
			}
		}
		if (baseClass == MBindingContainer.class) {
			switch (derivedFeatureID) {
				case MApplicationPackage.APPLICATION__BINDINGS: return MApplicationPackage.BINDING_CONTAINER__BINDINGS;
				default: return -1;
			}
		}
		if (baseClass == MPartDescriptorContainer.class) {
			switch (derivedFeatureID) {
				case MApplicationPackage.APPLICATION__DESCRIPTORS: return MApplicationPackage.PART_DESCRIPTOR_CONTAINER__DESCRIPTORS;
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
				case MApplicationPackage.CONTEXT__CONTEXT: return MApplicationPackage.APPLICATION__CONTEXT;
				case MApplicationPackage.CONTEXT__VARIABLES: return MApplicationPackage.APPLICATION__VARIABLES;
				case MApplicationPackage.CONTEXT__PROPERTIES: return MApplicationPackage.APPLICATION__PROPERTIES;
				default: return -1;
			}
		}
		if (baseClass == MHandlerContainer.class) {
			switch (baseFeatureID) {
				case MApplicationPackage.HANDLER_CONTAINER__HANDLERS: return MApplicationPackage.APPLICATION__HANDLERS;
				default: return -1;
			}
		}
		if (baseClass == MBindingContainer.class) {
			switch (baseFeatureID) {
				case MApplicationPackage.BINDING_CONTAINER__BINDINGS: return MApplicationPackage.APPLICATION__BINDINGS;
				default: return -1;
			}
		}
		if (baseClass == MPartDescriptorContainer.class) {
			switch (baseFeatureID) {
				case MApplicationPackage.PART_DESCRIPTOR_CONTAINER__DESCRIPTORS: return MApplicationPackage.APPLICATION__DESCRIPTORS;
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
		result.append(')');
		return result.toString();
	}

} //ApplicationImpl
