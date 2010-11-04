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
package org.eclipse.e4.ui.model.application.ui.impl;

import org.eclipse.e4.ui.model.application.ui.MUILabel;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>UI Label</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.impl.UILabelImpl#getLabel <em>Label</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.impl.UILabelImpl#getIconURI <em>Icon URI</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.impl.UILabelImpl#getTooltip <em>Tooltip</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.impl.UILabelImpl#getLocalLabel <em>Local Label</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.impl.UILabelImpl#getLocalTooltip <em>Local Tooltip</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.impl.UILabelImpl#getLocalImage <em>Local Image</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public abstract class UILabelImpl extends EObjectImpl implements MUILabel {
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
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected UILabelImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return UiPackageImpl.Literals.UI_LABEL;
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
			eNotify(new ENotificationImpl(this, Notification.SET, UiPackageImpl.UI_LABEL__LABEL, oldLabel, label));
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
			eNotify(new ENotificationImpl(this, Notification.SET, UiPackageImpl.UI_LABEL__ICON_URI, oldIconURI, iconURI));
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
			eNotify(new ENotificationImpl(this, Notification.SET, UiPackageImpl.UI_LABEL__TOOLTIP, oldTooltip, tooltip));
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
			eNotify(new ENotificationImpl(this, Notification.SET, UiPackageImpl.UI_LABEL__LOCAL_LABEL, oldLocalLabel, localLabel));
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
			eNotify(new ENotificationImpl(this, Notification.SET, UiPackageImpl.UI_LABEL__LOCAL_TOOLTIP, oldLocalTooltip, localTooltip));
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
			eNotify(new ENotificationImpl(this, Notification.SET, UiPackageImpl.UI_LABEL__LOCAL_IMAGE, oldLocalImage, localImage));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case UiPackageImpl.UI_LABEL__LABEL:
				return getLabel();
			case UiPackageImpl.UI_LABEL__ICON_URI:
				return getIconURI();
			case UiPackageImpl.UI_LABEL__TOOLTIP:
				return getTooltip();
			case UiPackageImpl.UI_LABEL__LOCAL_LABEL:
				return getLocalLabel();
			case UiPackageImpl.UI_LABEL__LOCAL_TOOLTIP:
				return getLocalTooltip();
			case UiPackageImpl.UI_LABEL__LOCAL_IMAGE:
				return getLocalImage();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case UiPackageImpl.UI_LABEL__LABEL:
				setLabel((String)newValue);
				return;
			case UiPackageImpl.UI_LABEL__ICON_URI:
				setIconURI((String)newValue);
				return;
			case UiPackageImpl.UI_LABEL__TOOLTIP:
				setTooltip((String)newValue);
				return;
			case UiPackageImpl.UI_LABEL__LOCAL_LABEL:
				setLocalLabel((String)newValue);
				return;
			case UiPackageImpl.UI_LABEL__LOCAL_TOOLTIP:
				setLocalTooltip((String)newValue);
				return;
			case UiPackageImpl.UI_LABEL__LOCAL_IMAGE:
				setLocalImage(newValue);
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
			case UiPackageImpl.UI_LABEL__LABEL:
				setLabel(LABEL_EDEFAULT);
				return;
			case UiPackageImpl.UI_LABEL__ICON_URI:
				setIconURI(ICON_URI_EDEFAULT);
				return;
			case UiPackageImpl.UI_LABEL__TOOLTIP:
				setTooltip(TOOLTIP_EDEFAULT);
				return;
			case UiPackageImpl.UI_LABEL__LOCAL_LABEL:
				setLocalLabel(LOCAL_LABEL_EDEFAULT);
				return;
			case UiPackageImpl.UI_LABEL__LOCAL_TOOLTIP:
				setLocalTooltip(LOCAL_TOOLTIP_EDEFAULT);
				return;
			case UiPackageImpl.UI_LABEL__LOCAL_IMAGE:
				setLocalImage(LOCAL_IMAGE_EDEFAULT);
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
			case UiPackageImpl.UI_LABEL__LABEL:
				return LABEL_EDEFAULT == null ? label != null : !LABEL_EDEFAULT.equals(label);
			case UiPackageImpl.UI_LABEL__ICON_URI:
				return ICON_URI_EDEFAULT == null ? iconURI != null : !ICON_URI_EDEFAULT.equals(iconURI);
			case UiPackageImpl.UI_LABEL__TOOLTIP:
				return TOOLTIP_EDEFAULT == null ? tooltip != null : !TOOLTIP_EDEFAULT.equals(tooltip);
			case UiPackageImpl.UI_LABEL__LOCAL_LABEL:
				return LOCAL_LABEL_EDEFAULT == null ? localLabel != null : !LOCAL_LABEL_EDEFAULT.equals(localLabel);
			case UiPackageImpl.UI_LABEL__LOCAL_TOOLTIP:
				return LOCAL_TOOLTIP_EDEFAULT == null ? localTooltip != null : !LOCAL_TOOLTIP_EDEFAULT.equals(localTooltip);
			case UiPackageImpl.UI_LABEL__LOCAL_IMAGE:
				return LOCAL_IMAGE_EDEFAULT == null ? localImage != null : !LOCAL_IMAGE_EDEFAULT.equals(localImage);
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
		result.append(')');
		return result.toString();
	}

} //UILabelImpl
