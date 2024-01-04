/**
 * Copyright (c) 2008, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *      IBM Corporation - initial API and implementation
 */
package org.eclipse.e4.ui.model.application.ui.advanced.impl;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.LocalizationHelper;
import org.eclipse.e4.ui.model.application.commands.MBindingContext;
import org.eclipse.e4.ui.model.application.commands.MBindings;
import org.eclipse.e4.ui.model.application.commands.MHandler;
import org.eclipse.e4.ui.model.application.commands.MHandlerContainer;
import org.eclipse.e4.ui.model.application.commands.impl.CommandsPackageImpl;
import org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl;
import org.eclipse.e4.ui.model.application.impl.StringToStringMapImpl;
import org.eclipse.e4.ui.model.application.ui.MContext;
import org.eclipse.e4.ui.model.application.ui.MLocalizable;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.MUILabel;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainerElement;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimBar;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.impl.ElementContainerImpl;
import org.eclipse.e4.ui.model.application.ui.impl.UiPackageImpl;
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
import org.eclipse.emf.ecore.util.EObjectContainmentWithInverseEList;
import org.eclipse.emf.ecore.util.EObjectResolvingEList;
import org.eclipse.emf.ecore.util.EcoreEMap;
import org.eclipse.emf.ecore.util.InternalEList;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Perspective</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.advanced.impl.PerspectiveImpl#getLabel <em>Label</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.advanced.impl.PerspectiveImpl#getIconURI <em>Icon URI</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.advanced.impl.PerspectiveImpl#getTooltip <em>Tooltip</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.advanced.impl.PerspectiveImpl#getLocalizedLabel <em>Localized Label</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.advanced.impl.PerspectiveImpl#getLocalizedTooltip <em>Localized Tooltip</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.advanced.impl.PerspectiveImpl#getContext <em>Context</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.advanced.impl.PerspectiveImpl#getVariables <em>Variables</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.advanced.impl.PerspectiveImpl#getProperties <em>Properties</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.advanced.impl.PerspectiveImpl#getHandlers <em>Handlers</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.advanced.impl.PerspectiveImpl#getBindingContexts <em>Binding Contexts</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.advanced.impl.PerspectiveImpl#getWindows <em>Windows</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.advanced.impl.PerspectiveImpl#getTrimBars <em>Trim Bars</em>}</li>
 * </ul>
 *
 * @since 1.0
 * @generated
 */
public class PerspectiveImpl extends ElementContainerImpl<MPartSashContainerElement> implements MPerspective {
	/**
	 * The default value of the '{@link #getLabel() <em>Label</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLabel()
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	protected static final String LABEL_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getLabel() <em>Label</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLabel()
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	protected String label = LABEL_EDEFAULT;

	/**
	 * The default value of the '{@link #getIconURI() <em>Icon URI</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getIconURI()
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	protected static final String ICON_URI_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getIconURI() <em>Icon URI</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getIconURI()
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	protected String iconURI = ICON_URI_EDEFAULT;

	/**
	 * The default value of the '{@link #getTooltip() <em>Tooltip</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTooltip()
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	protected static final String TOOLTIP_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getTooltip() <em>Tooltip</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTooltip()
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	protected String tooltip = TOOLTIP_EDEFAULT;

	/**
	 * The default value of the '{@link #getLocalizedLabel() <em>Localized Label</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLocalizedLabel()
	 * @generated
	 * @ordered
	 */
	protected static final String LOCALIZED_LABEL_EDEFAULT = ""; //$NON-NLS-1$

	/**
	 * The default value of the '{@link #getLocalizedTooltip() <em>Localized Tooltip</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLocalizedTooltip()
	 * @generated
	 * @ordered
	 */
	protected static final String LOCALIZED_TOOLTIP_EDEFAULT = ""; //$NON-NLS-1$

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
	 * The cached value of the '{@link #getBindingContexts() <em>Binding Contexts</em>}' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getBindingContexts()
	 * @generated
	 * @ordered
	 */
	protected EList<MBindingContext> bindingContexts;

	/**
	 * The cached value of the '{@link #getWindows() <em>Windows</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getWindows()
	 * @generated
	 * @ordered
	 */
	protected EList<MWindow> windows;

	/**
	 * The cached value of the '{@link #getTrimBars() <em>Trim Bars</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTrimBars()
	 * @since 1.3
	 * @generated
	 * @ordered
	 */
	protected EList<MTrimBar> trimBars;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected PerspectiveImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return AdvancedPackageImpl.Literals.PERSPECTIVE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * This is specialized for the more specific element type known in this context.
	 * @generated
	 */
	@Override
	public List<MPartSashContainerElement> getChildren() {
		if (children == null) {
			children = new EObjectContainmentWithInverseEList<>(
					MPartSashContainerElement.class, this, AdvancedPackageImpl.PERSPECTIVE__CHILDREN,
					UiPackageImpl.UI_ELEMENT__PARENT) {
				private static final long serialVersionUID = 1L;

				@Override
				public Class<?> getInverseFeatureClass() {
					return MUIElement.class;
				}
			};
		}
		return children;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * This is specialized for the more specific type known in this context.
	 * @generated
	 */
	@Override
	public void setSelectedElement(MPartSashContainerElement newSelectedElement) {
		super.setSelectedElement(newSelectedElement);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 */
	@Override
	public String getLabel() {
		return label;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 */
	@Override
	public void setLabel(String newLabel) {
		String oldLabel = label;
		label = newLabel;
		if (eNotificationRequired()) {
			eNotify(new ENotificationImpl(this, Notification.SET, AdvancedPackageImpl.PERSPECTIVE__LABEL, oldLabel,
					label));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 */
	@Override
	public String getIconURI() {
		return iconURI;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 */
	@Override
	public void setIconURI(String newIconURI) {
		String oldIconURI = iconURI;
		iconURI = newIconURI;
		if (eNotificationRequired()) {
			eNotify(new ENotificationImpl(this, Notification.SET, AdvancedPackageImpl.PERSPECTIVE__ICON_URI, oldIconURI,
					iconURI));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 */
	@Override
	public String getTooltip() {
		return tooltip;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 */
	@Override
	public void setTooltip(String newTooltip) {
		String oldTooltip = tooltip;
		tooltip = newTooltip;
		if (eNotificationRequired()) {
			eNotify(new ENotificationImpl(this, Notification.SET, AdvancedPackageImpl.PERSPECTIVE__TOOLTIP, oldTooltip,
					tooltip));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getLocalizedLabel() {
		return LocalizationHelper.getLocalizedFeature(UiPackageImpl.Literals.UI_LABEL__LABEL, this);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getLocalizedTooltip() {
		return LocalizationHelper.getLocalizedFeature(UiPackageImpl.Literals.UI_LABEL__TOOLTIP, this);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public IEclipseContext getContext() {
		return context;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setContext(IEclipseContext newContext) {
		IEclipseContext oldContext = context;
		context = newContext;
		if (eNotificationRequired()) {
			eNotify(new ENotificationImpl(this, Notification.SET, AdvancedPackageImpl.PERSPECTIVE__CONTEXT, oldContext,
					context));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public List<String> getVariables() {
		if (variables == null) {
			variables = new EDataTypeUniqueEList<>(String.class, this,
					AdvancedPackageImpl.PERSPECTIVE__VARIABLES);
		}
		return variables;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Map<String, String> getProperties() {
		if (properties == null) {
			properties = new EcoreEMap<>(ApplicationPackageImpl.Literals.STRING_TO_STRING_MAP,
					StringToStringMapImpl.class, this, AdvancedPackageImpl.PERSPECTIVE__PROPERTIES);
		}
		return properties.map();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public List<MHandler> getHandlers() {
		if (handlers == null) {
			handlers = new EObjectContainmentEList<>(MHandler.class, this,
					AdvancedPackageImpl.PERSPECTIVE__HANDLERS);
		}
		return handlers;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public List<MBindingContext> getBindingContexts() {
		if (bindingContexts == null) {
			bindingContexts = new EObjectResolvingEList<>(MBindingContext.class, this,
					AdvancedPackageImpl.PERSPECTIVE__BINDING_CONTEXTS);
		}
		return bindingContexts;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public List<MWindow> getWindows() {
		if (windows == null) {
			windows = new EObjectContainmentEList<>(MWindow.class, this,
					AdvancedPackageImpl.PERSPECTIVE__WINDOWS);
		}
		return windows;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.3
	 * @generated
	 */
	@Override
	public List<MTrimBar> getTrimBars() {
		if (trimBars == null) {
			trimBars = new EObjectContainmentEList<>(MTrimBar.class, this,
					AdvancedPackageImpl.PERSPECTIVE__TRIM_BARS);
		}
		return trimBars;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.1
	 * @generated
	 */
	@Override
	public void updateLocalization() {
		if (eNotificationRequired()) {
			super.updateLocalization();
			eNotify(new ENotificationImpl(this, Notification.SET, AdvancedPackageImpl.PERSPECTIVE__LOCALIZED_LABEL,
					null, getLocalizedLabel()));
			eNotify(new ENotificationImpl(this, Notification.SET, AdvancedPackageImpl.PERSPECTIVE__LOCALIZED_TOOLTIP,
					null, getLocalizedTooltip()));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
		case AdvancedPackageImpl.PERSPECTIVE__PROPERTIES:
			return ((InternalEList<?>) ((EMap.InternalMapView<String, String>) getProperties()).eMap())
					.basicRemove(otherEnd, msgs);
		case AdvancedPackageImpl.PERSPECTIVE__HANDLERS:
			return ((InternalEList<?>) getHandlers()).basicRemove(otherEnd, msgs);
		case AdvancedPackageImpl.PERSPECTIVE__WINDOWS:
			return ((InternalEList<?>) getWindows()).basicRemove(otherEnd, msgs);
		case AdvancedPackageImpl.PERSPECTIVE__TRIM_BARS:
			return ((InternalEList<?>) getTrimBars()).basicRemove(otherEnd, msgs);
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
		case AdvancedPackageImpl.PERSPECTIVE__LABEL:
			return getLabel();
		case AdvancedPackageImpl.PERSPECTIVE__ICON_URI:
			return getIconURI();
		case AdvancedPackageImpl.PERSPECTIVE__TOOLTIP:
			return getTooltip();
		case AdvancedPackageImpl.PERSPECTIVE__LOCALIZED_LABEL:
			return getLocalizedLabel();
		case AdvancedPackageImpl.PERSPECTIVE__LOCALIZED_TOOLTIP:
			return getLocalizedTooltip();
		case AdvancedPackageImpl.PERSPECTIVE__CONTEXT:
			return getContext();
		case AdvancedPackageImpl.PERSPECTIVE__VARIABLES:
			return getVariables();
		case AdvancedPackageImpl.PERSPECTIVE__PROPERTIES:
			if (coreType) {
				return ((EMap.InternalMapView<String, String>) getProperties()).eMap();
			} else {
				return getProperties();
			}
		case AdvancedPackageImpl.PERSPECTIVE__HANDLERS:
			return getHandlers();
		case AdvancedPackageImpl.PERSPECTIVE__BINDING_CONTEXTS:
			return getBindingContexts();
		case AdvancedPackageImpl.PERSPECTIVE__WINDOWS:
			return getWindows();
		case AdvancedPackageImpl.PERSPECTIVE__TRIM_BARS:
			return getTrimBars();
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
		case AdvancedPackageImpl.PERSPECTIVE__LABEL:
			setLabel((String) newValue);
			return;
		case AdvancedPackageImpl.PERSPECTIVE__ICON_URI:
			setIconURI((String) newValue);
			return;
		case AdvancedPackageImpl.PERSPECTIVE__TOOLTIP:
			setTooltip((String) newValue);
			return;
		case AdvancedPackageImpl.PERSPECTIVE__CONTEXT:
			setContext((IEclipseContext) newValue);
			return;
		case AdvancedPackageImpl.PERSPECTIVE__VARIABLES:
			getVariables().clear();
			getVariables().addAll((Collection<? extends String>) newValue);
			return;
		case AdvancedPackageImpl.PERSPECTIVE__PROPERTIES:
			((EStructuralFeature.Setting) ((EMap.InternalMapView<String, String>) getProperties()).eMap())
					.set(newValue);
			return;
		case AdvancedPackageImpl.PERSPECTIVE__HANDLERS:
			getHandlers().clear();
			getHandlers().addAll((Collection<? extends MHandler>) newValue);
			return;
		case AdvancedPackageImpl.PERSPECTIVE__BINDING_CONTEXTS:
			getBindingContexts().clear();
			getBindingContexts().addAll((Collection<? extends MBindingContext>) newValue);
			return;
		case AdvancedPackageImpl.PERSPECTIVE__WINDOWS:
			getWindows().clear();
			getWindows().addAll((Collection<? extends MWindow>) newValue);
			return;
		case AdvancedPackageImpl.PERSPECTIVE__TRIM_BARS:
			getTrimBars().clear();
			getTrimBars().addAll((Collection<? extends MTrimBar>) newValue);
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
		case AdvancedPackageImpl.PERSPECTIVE__LABEL:
			setLabel(LABEL_EDEFAULT);
			return;
		case AdvancedPackageImpl.PERSPECTIVE__ICON_URI:
			setIconURI(ICON_URI_EDEFAULT);
			return;
		case AdvancedPackageImpl.PERSPECTIVE__TOOLTIP:
			setTooltip(TOOLTIP_EDEFAULT);
			return;
		case AdvancedPackageImpl.PERSPECTIVE__CONTEXT:
			setContext(CONTEXT_EDEFAULT);
			return;
		case AdvancedPackageImpl.PERSPECTIVE__VARIABLES:
			getVariables().clear();
			return;
		case AdvancedPackageImpl.PERSPECTIVE__PROPERTIES:
			getProperties().clear();
			return;
		case AdvancedPackageImpl.PERSPECTIVE__HANDLERS:
			getHandlers().clear();
			return;
		case AdvancedPackageImpl.PERSPECTIVE__BINDING_CONTEXTS:
			getBindingContexts().clear();
			return;
		case AdvancedPackageImpl.PERSPECTIVE__WINDOWS:
			getWindows().clear();
			return;
		case AdvancedPackageImpl.PERSPECTIVE__TRIM_BARS:
			getTrimBars().clear();
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
		case AdvancedPackageImpl.PERSPECTIVE__LABEL:
			return LABEL_EDEFAULT == null ? label != null : !LABEL_EDEFAULT.equals(label);
		case AdvancedPackageImpl.PERSPECTIVE__ICON_URI:
			return ICON_URI_EDEFAULT == null ? iconURI != null : !ICON_URI_EDEFAULT.equals(iconURI);
		case AdvancedPackageImpl.PERSPECTIVE__TOOLTIP:
			return TOOLTIP_EDEFAULT == null ? tooltip != null : !TOOLTIP_EDEFAULT.equals(tooltip);
		case AdvancedPackageImpl.PERSPECTIVE__LOCALIZED_LABEL:
			return LOCALIZED_LABEL_EDEFAULT == null ? getLocalizedLabel() != null
					: !LOCALIZED_LABEL_EDEFAULT.equals(getLocalizedLabel());
		case AdvancedPackageImpl.PERSPECTIVE__LOCALIZED_TOOLTIP:
			return LOCALIZED_TOOLTIP_EDEFAULT == null ? getLocalizedTooltip() != null
					: !LOCALIZED_TOOLTIP_EDEFAULT.equals(getLocalizedTooltip());
		case AdvancedPackageImpl.PERSPECTIVE__CONTEXT:
			return CONTEXT_EDEFAULT == null ? context != null : !CONTEXT_EDEFAULT.equals(context);
		case AdvancedPackageImpl.PERSPECTIVE__VARIABLES:
			return variables != null && !variables.isEmpty();
		case AdvancedPackageImpl.PERSPECTIVE__PROPERTIES:
			return properties != null && !properties.isEmpty();
		case AdvancedPackageImpl.PERSPECTIVE__HANDLERS:
			return handlers != null && !handlers.isEmpty();
		case AdvancedPackageImpl.PERSPECTIVE__BINDING_CONTEXTS:
			return bindingContexts != null && !bindingContexts.isEmpty();
		case AdvancedPackageImpl.PERSPECTIVE__WINDOWS:
			return windows != null && !windows.isEmpty();
		case AdvancedPackageImpl.PERSPECTIVE__TRIM_BARS:
			return trimBars != null && !trimBars.isEmpty();
		default:
			return super.eIsSet(featureID);
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int eBaseStructuralFeatureID(int derivedFeatureID, Class<?> baseClass) {
		if (baseClass == MUILabel.class) {
			switch (derivedFeatureID) {
			case AdvancedPackageImpl.PERSPECTIVE__LABEL:
				return UiPackageImpl.UI_LABEL__LABEL;
			case AdvancedPackageImpl.PERSPECTIVE__ICON_URI:
				return UiPackageImpl.UI_LABEL__ICON_URI;
			case AdvancedPackageImpl.PERSPECTIVE__TOOLTIP:
				return UiPackageImpl.UI_LABEL__TOOLTIP;
			case AdvancedPackageImpl.PERSPECTIVE__LOCALIZED_LABEL:
				return UiPackageImpl.UI_LABEL__LOCALIZED_LABEL;
			case AdvancedPackageImpl.PERSPECTIVE__LOCALIZED_TOOLTIP:
				return UiPackageImpl.UI_LABEL__LOCALIZED_TOOLTIP;
			default:
				return -1;
			}
		}
		if (baseClass == MContext.class) {
			switch (derivedFeatureID) {
			case AdvancedPackageImpl.PERSPECTIVE__CONTEXT:
				return UiPackageImpl.CONTEXT__CONTEXT;
			case AdvancedPackageImpl.PERSPECTIVE__VARIABLES:
				return UiPackageImpl.CONTEXT__VARIABLES;
			case AdvancedPackageImpl.PERSPECTIVE__PROPERTIES:
				return UiPackageImpl.CONTEXT__PROPERTIES;
			default:
				return -1;
			}
		}
		if (baseClass == MHandlerContainer.class) {
			switch (derivedFeatureID) {
			case AdvancedPackageImpl.PERSPECTIVE__HANDLERS:
				return CommandsPackageImpl.HANDLER_CONTAINER__HANDLERS;
			default:
				return -1;
			}
		}
		if (baseClass == MBindings.class) {
			switch (derivedFeatureID) {
			case AdvancedPackageImpl.PERSPECTIVE__BINDING_CONTEXTS:
				return CommandsPackageImpl.BINDINGS__BINDING_CONTEXTS;
			default:
				return -1;
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
		if (baseClass == MUILabel.class) {
			switch (baseFeatureID) {
			case UiPackageImpl.UI_LABEL__LABEL:
				return AdvancedPackageImpl.PERSPECTIVE__LABEL;
			case UiPackageImpl.UI_LABEL__ICON_URI:
				return AdvancedPackageImpl.PERSPECTIVE__ICON_URI;
			case UiPackageImpl.UI_LABEL__TOOLTIP:
				return AdvancedPackageImpl.PERSPECTIVE__TOOLTIP;
			case UiPackageImpl.UI_LABEL__LOCALIZED_LABEL:
				return AdvancedPackageImpl.PERSPECTIVE__LOCALIZED_LABEL;
			case UiPackageImpl.UI_LABEL__LOCALIZED_TOOLTIP:
				return AdvancedPackageImpl.PERSPECTIVE__LOCALIZED_TOOLTIP;
			default:
				return -1;
			}
		}
		if (baseClass == MContext.class) {
			switch (baseFeatureID) {
			case UiPackageImpl.CONTEXT__CONTEXT:
				return AdvancedPackageImpl.PERSPECTIVE__CONTEXT;
			case UiPackageImpl.CONTEXT__VARIABLES:
				return AdvancedPackageImpl.PERSPECTIVE__VARIABLES;
			case UiPackageImpl.CONTEXT__PROPERTIES:
				return AdvancedPackageImpl.PERSPECTIVE__PROPERTIES;
			default:
				return -1;
			}
		}
		if (baseClass == MHandlerContainer.class) {
			switch (baseFeatureID) {
			case CommandsPackageImpl.HANDLER_CONTAINER__HANDLERS:
				return AdvancedPackageImpl.PERSPECTIVE__HANDLERS;
			default:
				return -1;
			}
		}
		if (baseClass == MBindings.class) {
			switch (baseFeatureID) {
			case CommandsPackageImpl.BINDINGS__BINDING_CONTEXTS:
				return AdvancedPackageImpl.PERSPECTIVE__BINDING_CONTEXTS;
			default:
				return -1;
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
	public int eDerivedOperationID(int baseOperationID, Class<?> baseClass) {
		if (baseClass == MLocalizable.class) {
			switch (baseOperationID) {
			case UiPackageImpl.LOCALIZABLE___UPDATE_LOCALIZATION:
				return AdvancedPackageImpl.PERSPECTIVE___UPDATE_LOCALIZATION;
			default:
				return super.eDerivedOperationID(baseOperationID, baseClass);
			}
		}
		if (baseClass == MUIElement.class) {
			switch (baseOperationID) {
			case UiPackageImpl.UI_ELEMENT___UPDATE_LOCALIZATION:
				return AdvancedPackageImpl.PERSPECTIVE___UPDATE_LOCALIZATION;
			default:
				return super.eDerivedOperationID(baseOperationID, baseClass);
			}
		}
		if (baseClass == MUILabel.class) {
			switch (baseOperationID) {
			default:
				return -1;
			}
		}
		if (baseClass == MContext.class) {
			switch (baseOperationID) {
			default:
				return -1;
			}
		}
		if (baseClass == MHandlerContainer.class) {
			switch (baseOperationID) {
			default:
				return -1;
			}
		}
		if (baseClass == MBindings.class) {
			switch (baseOperationID) {
			default:
				return -1;
			}
		}
		return super.eDerivedOperationID(baseOperationID, baseClass);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eInvoke(int operationID, EList<?> arguments) throws InvocationTargetException {
		switch (operationID) {
		case AdvancedPackageImpl.PERSPECTIVE___UPDATE_LOCALIZATION:
			updateLocalization();
			return null;
		default:
			return super.eInvoke(operationID, arguments);
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String toString() {
		if (eIsProxy()) {
			return super.toString();
		}

		StringBuilder result = new StringBuilder(super.toString());
		result.append(" (label: "); //$NON-NLS-1$
		result.append(label);
		result.append(", iconURI: "); //$NON-NLS-1$
		result.append(iconURI);
		result.append(", tooltip: "); //$NON-NLS-1$
		result.append(tooltip);
		result.append(", context: "); //$NON-NLS-1$
		result.append(context);
		result.append(", variables: "); //$NON-NLS-1$
		result.append(variables);
		result.append(')');
		return result.toString();
	}

} //PerspectiveImpl
