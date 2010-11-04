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
package org.eclipse.e4.ui.model.application.ui.advanced.impl;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.e4.core.contexts.IEclipseContext;

import org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl;
import org.eclipse.e4.ui.model.application.impl.StringToStringMapImpl;

import org.eclipse.e4.ui.model.application.ui.MContext;
import org.eclipse.e4.ui.model.application.ui.MUILabel;

import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;

import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainerElement;
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
import org.eclipse.emf.ecore.util.EcoreEMap;
import org.eclipse.emf.ecore.util.InternalEList;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Perspective</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.advanced.impl.PerspectiveImpl#getLabel <em>Label</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.advanced.impl.PerspectiveImpl#getIconURI <em>Icon URI</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.advanced.impl.PerspectiveImpl#getTooltip <em>Tooltip</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.advanced.impl.PerspectiveImpl#getLocalLabel <em>Local Label</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.advanced.impl.PerspectiveImpl#getLocalTooltip <em>Local Tooltip</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.advanced.impl.PerspectiveImpl#getLocalImage <em>Local Image</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.advanced.impl.PerspectiveImpl#getContext <em>Context</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.advanced.impl.PerspectiveImpl#getVariables <em>Variables</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.advanced.impl.PerspectiveImpl#getProperties <em>Properties</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.advanced.impl.PerspectiveImpl#getWindows <em>Windows</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class PerspectiveImpl extends ElementContainerImpl<MPartSashContainerElement> implements MPerspective {
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
	 * The default value of the '{@link #getLocalLabel() <em>Local Label</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLocalLabel()
	 * @generated
	 * @ordered
	 */
	protected static final String LOCAL_LABEL_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getLocalLabel() <em>Local Label</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLocalLabel()
	 * @generated
	 * @ordered
	 */
	protected String localLabel = LOCAL_LABEL_EDEFAULT;

	/**
	 * The default value of the '{@link #getLocalTooltip() <em>Local Tooltip</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLocalTooltip()
	 * @generated
	 * @ordered
	 */
	protected static final String LOCAL_TOOLTIP_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getLocalTooltip() <em>Local Tooltip</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLocalTooltip()
	 * @generated
	 * @ordered
	 */
	protected String localTooltip = LOCAL_TOOLTIP_EDEFAULT;

	/**
	 * The default value of the '{@link #getLocalImage() <em>Local Image</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLocalImage()
	 * @generated
	 * @ordered
	 */
	protected static final Object LOCAL_IMAGE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getLocalImage() <em>Local Image</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLocalImage()
	 * @generated
	 * @ordered
	 */
	protected Object localImage = LOCAL_IMAGE_EDEFAULT;

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
	 * The cached value of the '{@link #getWindows() <em>Windows</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getWindows()
	 * @generated
	 * @ordered
	 */
	protected EList<MWindow> windows;

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
			eNotify(new ENotificationImpl(this, Notification.SET, AdvancedPackageImpl.PERSPECTIVE__LABEL, oldLabel, label));
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
			eNotify(new ENotificationImpl(this, Notification.SET, AdvancedPackageImpl.PERSPECTIVE__ICON_URI, oldIconURI, iconURI));
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
			eNotify(new ENotificationImpl(this, Notification.SET, AdvancedPackageImpl.PERSPECTIVE__TOOLTIP, oldTooltip, tooltip));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getLocalLabel() {
		return localLabel;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setLocalLabel(String newLocalLabel) {
		String oldLocalLabel = localLabel;
		localLabel = newLocalLabel;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, AdvancedPackageImpl.PERSPECTIVE__LOCAL_LABEL, oldLocalLabel, localLabel));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getLocalTooltip() {
		return localTooltip;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setLocalTooltip(String newLocalTooltip) {
		String oldLocalTooltip = localTooltip;
		localTooltip = newLocalTooltip;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, AdvancedPackageImpl.PERSPECTIVE__LOCAL_TOOLTIP, oldLocalTooltip, localTooltip));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Object getLocalImage() {
		return localImage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setLocalImage(Object newLocalImage) {
		Object oldLocalImage = localImage;
		localImage = newLocalImage;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, AdvancedPackageImpl.PERSPECTIVE__LOCAL_IMAGE, oldLocalImage, localImage));
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
			eNotify(new ENotificationImpl(this, Notification.SET, AdvancedPackageImpl.PERSPECTIVE__CONTEXT, oldContext, context));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public List<String> getVariables() {
		if (variables == null) {
			variables = new EDataTypeUniqueEList<String>(String.class, this, AdvancedPackageImpl.PERSPECTIVE__VARIABLES);
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
			properties = new EcoreEMap<String,String>(ApplicationPackageImpl.Literals.STRING_TO_STRING_MAP, StringToStringMapImpl.class, this, AdvancedPackageImpl.PERSPECTIVE__PROPERTIES);
		}
		return properties.map();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public List<MWindow> getWindows() {
		if (windows == null) {
			windows = new EObjectContainmentEList<MWindow>(MWindow.class, this, AdvancedPackageImpl.PERSPECTIVE__WINDOWS);
		}
		return windows;
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
				return ((InternalEList<?>)((EMap.InternalMapView<String, String>)getProperties()).eMap()).basicRemove(otherEnd, msgs);
			case AdvancedPackageImpl.PERSPECTIVE__WINDOWS:
				return ((InternalEList<?>)getWindows()).basicRemove(otherEnd, msgs);
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
			case AdvancedPackageImpl.PERSPECTIVE__LABEL:
				return getLabel();
			case AdvancedPackageImpl.PERSPECTIVE__ICON_URI:
				return getIconURI();
			case AdvancedPackageImpl.PERSPECTIVE__TOOLTIP:
				return getTooltip();
			case AdvancedPackageImpl.PERSPECTIVE__LOCAL_LABEL:
				return getLocalLabel();
			case AdvancedPackageImpl.PERSPECTIVE__LOCAL_TOOLTIP:
				return getLocalTooltip();
			case AdvancedPackageImpl.PERSPECTIVE__LOCAL_IMAGE:
				return getLocalImage();
			case AdvancedPackageImpl.PERSPECTIVE__CONTEXT:
				return getContext();
			case AdvancedPackageImpl.PERSPECTIVE__VARIABLES:
				return getVariables();
			case AdvancedPackageImpl.PERSPECTIVE__PROPERTIES:
				if (coreType) return ((EMap.InternalMapView<String, String>)getProperties()).eMap();
				else return getProperties();
			case AdvancedPackageImpl.PERSPECTIVE__WINDOWS:
				return getWindows();
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
			case AdvancedPackageImpl.PERSPECTIVE__LABEL:
				setLabel((String)newValue);
				return;
			case AdvancedPackageImpl.PERSPECTIVE__ICON_URI:
				setIconURI((String)newValue);
				return;
			case AdvancedPackageImpl.PERSPECTIVE__TOOLTIP:
				setTooltip((String)newValue);
				return;
			case AdvancedPackageImpl.PERSPECTIVE__LOCAL_LABEL:
				setLocalLabel((String)newValue);
				return;
			case AdvancedPackageImpl.PERSPECTIVE__LOCAL_TOOLTIP:
				setLocalTooltip((String)newValue);
				return;
			case AdvancedPackageImpl.PERSPECTIVE__LOCAL_IMAGE:
				setLocalImage(newValue);
				return;
			case AdvancedPackageImpl.PERSPECTIVE__CONTEXT:
				setContext((IEclipseContext)newValue);
				return;
			case AdvancedPackageImpl.PERSPECTIVE__VARIABLES:
				getVariables().clear();
				getVariables().addAll((Collection<? extends String>)newValue);
				return;
			case AdvancedPackageImpl.PERSPECTIVE__PROPERTIES:
				((EStructuralFeature.Setting)((EMap.InternalMapView<String, String>)getProperties()).eMap()).set(newValue);
				return;
			case AdvancedPackageImpl.PERSPECTIVE__WINDOWS:
				getWindows().clear();
				getWindows().addAll((Collection<? extends MWindow>)newValue);
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
			case AdvancedPackageImpl.PERSPECTIVE__LABEL:
				setLabel(LABEL_EDEFAULT);
				return;
			case AdvancedPackageImpl.PERSPECTIVE__ICON_URI:
				setIconURI(ICON_URI_EDEFAULT);
				return;
			case AdvancedPackageImpl.PERSPECTIVE__TOOLTIP:
				setTooltip(TOOLTIP_EDEFAULT);
				return;
			case AdvancedPackageImpl.PERSPECTIVE__LOCAL_LABEL:
				setLocalLabel(LOCAL_LABEL_EDEFAULT);
				return;
			case AdvancedPackageImpl.PERSPECTIVE__LOCAL_TOOLTIP:
				setLocalTooltip(LOCAL_TOOLTIP_EDEFAULT);
				return;
			case AdvancedPackageImpl.PERSPECTIVE__LOCAL_IMAGE:
				setLocalImage(LOCAL_IMAGE_EDEFAULT);
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
			case AdvancedPackageImpl.PERSPECTIVE__WINDOWS:
				getWindows().clear();
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
			case AdvancedPackageImpl.PERSPECTIVE__LABEL:
				return LABEL_EDEFAULT == null ? label != null : !LABEL_EDEFAULT.equals(label);
			case AdvancedPackageImpl.PERSPECTIVE__ICON_URI:
				return ICON_URI_EDEFAULT == null ? iconURI != null : !ICON_URI_EDEFAULT.equals(iconURI);
			case AdvancedPackageImpl.PERSPECTIVE__TOOLTIP:
				return TOOLTIP_EDEFAULT == null ? tooltip != null : !TOOLTIP_EDEFAULT.equals(tooltip);
			case AdvancedPackageImpl.PERSPECTIVE__LOCAL_LABEL:
				return LOCAL_LABEL_EDEFAULT == null ? localLabel != null : !LOCAL_LABEL_EDEFAULT.equals(localLabel);
			case AdvancedPackageImpl.PERSPECTIVE__LOCAL_TOOLTIP:
				return LOCAL_TOOLTIP_EDEFAULT == null ? localTooltip != null : !LOCAL_TOOLTIP_EDEFAULT.equals(localTooltip);
			case AdvancedPackageImpl.PERSPECTIVE__LOCAL_IMAGE:
				return LOCAL_IMAGE_EDEFAULT == null ? localImage != null : !LOCAL_IMAGE_EDEFAULT.equals(localImage);
			case AdvancedPackageImpl.PERSPECTIVE__CONTEXT:
				return CONTEXT_EDEFAULT == null ? context != null : !CONTEXT_EDEFAULT.equals(context);
			case AdvancedPackageImpl.PERSPECTIVE__VARIABLES:
				return variables != null && !variables.isEmpty();
			case AdvancedPackageImpl.PERSPECTIVE__PROPERTIES:
				return properties != null && !properties.isEmpty();
			case AdvancedPackageImpl.PERSPECTIVE__WINDOWS:
				return windows != null && !windows.isEmpty();
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
		if (baseClass == MUILabel.class) {
			switch (derivedFeatureID) {
				case AdvancedPackageImpl.PERSPECTIVE__LABEL: return UiPackageImpl.UI_LABEL__LABEL;
				case AdvancedPackageImpl.PERSPECTIVE__ICON_URI: return UiPackageImpl.UI_LABEL__ICON_URI;
				case AdvancedPackageImpl.PERSPECTIVE__TOOLTIP: return UiPackageImpl.UI_LABEL__TOOLTIP;
				case AdvancedPackageImpl.PERSPECTIVE__LOCAL_LABEL: return UiPackageImpl.UI_LABEL__LOCAL_LABEL;
				case AdvancedPackageImpl.PERSPECTIVE__LOCAL_TOOLTIP: return UiPackageImpl.UI_LABEL__LOCAL_TOOLTIP;
				case AdvancedPackageImpl.PERSPECTIVE__LOCAL_IMAGE: return UiPackageImpl.UI_LABEL__LOCAL_IMAGE;
				default: return -1;
			}
		}
		if (baseClass == MContext.class) {
			switch (derivedFeatureID) {
				case AdvancedPackageImpl.PERSPECTIVE__CONTEXT: return UiPackageImpl.CONTEXT__CONTEXT;
				case AdvancedPackageImpl.PERSPECTIVE__VARIABLES: return UiPackageImpl.CONTEXT__VARIABLES;
				case AdvancedPackageImpl.PERSPECTIVE__PROPERTIES: return UiPackageImpl.CONTEXT__PROPERTIES;
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
		if (baseClass == MUILabel.class) {
			switch (baseFeatureID) {
				case UiPackageImpl.UI_LABEL__LABEL: return AdvancedPackageImpl.PERSPECTIVE__LABEL;
				case UiPackageImpl.UI_LABEL__ICON_URI: return AdvancedPackageImpl.PERSPECTIVE__ICON_URI;
				case UiPackageImpl.UI_LABEL__TOOLTIP: return AdvancedPackageImpl.PERSPECTIVE__TOOLTIP;
				case UiPackageImpl.UI_LABEL__LOCAL_LABEL: return AdvancedPackageImpl.PERSPECTIVE__LOCAL_LABEL;
				case UiPackageImpl.UI_LABEL__LOCAL_TOOLTIP: return AdvancedPackageImpl.PERSPECTIVE__LOCAL_TOOLTIP;
				case UiPackageImpl.UI_LABEL__LOCAL_IMAGE: return AdvancedPackageImpl.PERSPECTIVE__LOCAL_IMAGE;
				default: return -1;
			}
		}
		if (baseClass == MContext.class) {
			switch (baseFeatureID) {
				case UiPackageImpl.CONTEXT__CONTEXT: return AdvancedPackageImpl.PERSPECTIVE__CONTEXT;
				case UiPackageImpl.CONTEXT__VARIABLES: return AdvancedPackageImpl.PERSPECTIVE__VARIABLES;
				case UiPackageImpl.CONTEXT__PROPERTIES: return AdvancedPackageImpl.PERSPECTIVE__PROPERTIES;
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
		result.append(" (label: "); //$NON-NLS-1$
		result.append(label);
		result.append(", iconURI: "); //$NON-NLS-1$
		result.append(iconURI);
		result.append(", tooltip: "); //$NON-NLS-1$
		result.append(tooltip);
		result.append(", localLabel: "); //$NON-NLS-1$
		result.append(localLabel);
		result.append(", localTooltip: "); //$NON-NLS-1$
		result.append(localTooltip);
		result.append(", localImage: "); //$NON-NLS-1$
		result.append(localImage);
		result.append(", context: "); //$NON-NLS-1$
		result.append(context);
		result.append(", variables: "); //$NON-NLS-1$
		result.append(variables);
		result.append(')');
		return result.toString();
	}

} //PerspectiveImpl
