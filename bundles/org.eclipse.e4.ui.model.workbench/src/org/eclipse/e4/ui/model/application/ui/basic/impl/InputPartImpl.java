package org.eclipse.e4.ui.model.application.ui.basic.impl;

import org.eclipse.e4.ui.model.application.ui.MInput;
import org.eclipse.e4.ui.model.application.ui.basic.MInputPart;
import org.eclipse.e4.ui.model.application.ui.impl.UiPackageImpl;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.impl.ENotificationImpl;

/**
 * <!-- begin-user-doc --> An implementation of the model object '<em><b>Input
 * Part</b></em>'. <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 * <li>{@link org.eclipse.e4.ui.model.application.ui.basic.impl.InputPartImpl#getInputURI
 * <em>Input URI</em>}</li>
 * </ul>
 *
 * @since 1.0
 * @deprecated See {@link MInputPart model documentation} for details.
 * @generated NOT
 * @noextend This class is not intended to be subclassed by clients.
 * @noreference This class is not intended to be referenced by clients.
 * @see <a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=509868">Bug
 *      509868</a>
 */
@Deprecated
public class InputPartImpl extends PartImpl implements MInputPart {
	/**
	 * The default value of the '{@link #getInputURI() <em>Input URI</em>}'
	 * attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see #getInputURI()
	 * @deprecated See {@link MInputPart model documentation} for details.
	 * @generated NOT
	 * @ordered
	 * @noreference This field is not intended to be referenced by clients.
	 * @see <a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=509868">Bug
	 *      509868</a>
	 */
	@Deprecated
	protected static final String INPUT_URI_EDEFAULT = null;
	/**
	 * The cached value of the '{@link #getInputURI() <em>Input URI</em>}'
	 * attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see #getInputURI()
	 * @deprecated See {@link MInputPart model documentation} for details.
	 * @generated NOT
	 * @ordered
	 * @noreference This field is not intended to be referenced by clients.
	 * @see <a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=509868">Bug
	 *      509868</a>
	 */
	@Deprecated
	protected String inputURI = INPUT_URI_EDEFAULT;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @deprecated See {@link MInputPart model documentation} for details.
	 * @generated NOT
	 * @noreference This constructor is not intended to be referenced by clients.
	 * @see <a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=509868">Bug
	 *      509868</a>
	 */
	@Deprecated
	protected InputPartImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @deprecated See {@link MInputPart model documentation} for details.
	 * @generated NOT
	 * @noreference This method is not intended to be referenced by clients.
	 * @see <a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=509868">Bug
	 *      509868</a>
	 */
	@Override
	@Deprecated
	protected EClass eStaticClass() {
		return BasicPackageImpl.Literals.INPUT_PART;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @deprecated See {@link MInputPart model documentation} for details.
	 * @generated NOT
	 * @noreference This method is not intended to be referenced by clients.
	 * @see <a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=509868">Bug
	 *      509868</a>
	 */
	@Deprecated
	public String getInputURI() {
		return inputURI;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @deprecated See {@link MInputPart model documentation} for details.
	 * @generated NOT
	 * @noreference This method is not intended to be referenced by clients.
	 * @see <a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=509868">Bug
	 *      509868</a>
	 */
	@Deprecated
	public void setInputURI(String newInputURI) {
		String oldInputURI = inputURI;
		inputURI = newInputURI;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, BasicPackageImpl.INPUT_PART__INPUT_URI, oldInputURI,
					inputURI));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @deprecated See {@link MInputPart model documentation} for details.
	 * @generated NOT
	 * @noreference This method is not intended to be referenced by clients.
	 * @see <a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=509868">Bug
	 *      509868</a>
	 */
	@Override
	@Deprecated
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
		case BasicPackageImpl.INPUT_PART__INPUT_URI:
			return getInputURI();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @deprecated See {@link MInputPart model documentation} for details.
	 * @generated NOT
	 * @noreference This method is not intended to be referenced by clients.
	 * @see <a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=509868">Bug
	 *      509868</a>
	 */
	@Override
	@Deprecated
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
		case BasicPackageImpl.INPUT_PART__INPUT_URI:
			setInputURI((String) newValue);
			return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @deprecated See {@link MInputPart model documentation} for details.
	 * @generated NOT
	 * @noreference This method is not intended to be referenced by clients.
	 * @see <a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=509868">Bug
	 *      509868</a>
	 */
	@Override
	@Deprecated
	public void eUnset(int featureID) {
		switch (featureID) {
		case BasicPackageImpl.INPUT_PART__INPUT_URI:
			setInputURI(INPUT_URI_EDEFAULT);
			return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @deprecated See {@link MInputPart model documentation} for details.
	 * @generated NOT
	 * @noreference This method is not intended to be referenced by clients.
	 * @see <a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=509868">Bug
	 *      509868</a>
	 */
	@Override
	@Deprecated
	public boolean eIsSet(int featureID) {
		switch (featureID) {
		case BasicPackageImpl.INPUT_PART__INPUT_URI:
			return INPUT_URI_EDEFAULT == null ? inputURI != null : !INPUT_URI_EDEFAULT.equals(inputURI);
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @deprecated See {@link MInputPart model documentation} for details.
	 * @generated NOT
	 * @noreference This method is not intended to be referenced by clients.
	 * @see <a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=509868">Bug
	 *      509868</a>
	 */
	@Override
	@Deprecated
	public int eBaseStructuralFeatureID(int derivedFeatureID, Class<?> baseClass) {
		if (baseClass == MInput.class) {
			switch (derivedFeatureID) {
			case BasicPackageImpl.INPUT_PART__INPUT_URI:
				return UiPackageImpl.INPUT__INPUT_URI;
			default:
				return -1;
			}
		}
		return super.eBaseStructuralFeatureID(derivedFeatureID, baseClass);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @deprecated See {@link MInputPart model documentation} for details.
	 * @generated NOT
	 * @noreference This method is not intended to be referenced by clients.
	 * @see <a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=509868">Bug
	 *      509868</a>
	 */
	@Override
	@Deprecated
	public int eDerivedStructuralFeatureID(int baseFeatureID, Class<?> baseClass) {
		if (baseClass == MInput.class) {
			switch (baseFeatureID) {
			case UiPackageImpl.INPUT__INPUT_URI:
				return BasicPackageImpl.INPUT_PART__INPUT_URI;
			default:
				return -1;
			}
		}
		return super.eDerivedStructuralFeatureID(baseFeatureID, baseClass);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @deprecated See {@link MInputPart model documentation} for details.
	 * @generated NOT
	 * @noreference This method is not intended to be referenced by clients.
	 * @see <a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=509868">Bug
	 *      509868</a>
	 */
	@Override
	@Deprecated
	public String toString() {
		if (eIsProxy())
			return super.toString();
		StringBuilder result = new StringBuilder(super.toString());
		result.append(" (inputURI: "); //$NON-NLS-1$
		result.append(inputURI);
		result.append(')');
		return result.toString();
	}
} // InputPartImpl