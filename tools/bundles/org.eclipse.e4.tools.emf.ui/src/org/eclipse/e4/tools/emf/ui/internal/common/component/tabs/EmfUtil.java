/*******************************************************************************
 * Copyright (c) 2014 TwelveTone LLC and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Steven Spungin <steven@spungin.tv> - initial API and implementation, Ongoing Maintenance
 *******************************************************************************/

package org.eclipse.e4.tools.emf.ui.internal.common.component.tabs;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;

/**
 * Convenience methods for accessing EMF Objects
 *
 * @author Steven Spungin
 */
public class EmfUtil {
	/**
	 * Returns the EAttribute with the given name for the specified object, or
	 * null if non-existent
	 *
	 * @return the EAttribute with the given name for the specified object, or
	 *         null if non-existent
	 */
	static public EAttribute getAttribute(EObject eObject, String attName) {
		if (attName == null || attName.isEmpty()) {
			return null;
		}
		// return (EAttribute)
		// eObject.eGet(eObject.eClass().getEStructuralFeature(attName));
		for (final EAttribute att : eObject.eClass().getEAllAttributes()) {
			if (attName.equals(att.getName())) {
				return att;
			}
		}
		return null;
	}

	/**
	 * Returns the EAttribute value with the given attribute name for the
	 * specified object. Returns null if the attribute is not define, or has
	 * null as the value.
	 *
	 * @return the EAttribute value with the given attribute name for the
	 *         specified object, Returns null if the attribute is not defined,
	 *         or has null as the value.
	 */
	static public Object getAttributeValue(EObject eObject, String attName) {
		final EAttribute att = getAttribute(eObject, attName);
		if (att == null) {
			return null;
		}
		return eObject.eGet(att);
	}

	/**
	 * Returns the EAttribute value with the given attribute name for the
	 * specified object. Throws if the attribute is not defined.
	 */
	static public Object getAttributeValueThrows(EObject eObject, String attName) throws Exception {
		final EAttribute att = getAttribute(eObject, attName);
		if (att == null) {
			throw new Exception(Messages.EmfUtil_ex_attribute_not_found + " : " + attName); //$NON-NLS-1$
		}
		return eObject.eGet(att);
	}
}