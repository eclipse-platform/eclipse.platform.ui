/*******************************************************************************
 * Copyright (c) 2014 TwelveTone LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Steven Spungin <steven@spungin.tv> - initial API and implementation
 *******************************************************************************/

package org.eclipse.e4.tools.emf.ui.internal.common.component.tabs;

import org.eclipse.e4.tools.emf.ui.internal.Messages;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;

/**
 * Convenience methods for accessing EMF Objects
 *
 * @author Steven Spungin
 *
 */
public class EmfUtil {
	/**
	 * Returns the EAttribute with the given name for the specified object, or
	 * null if non-existent
	 *
	 * @param eObject
	 * @param attName
	 * @return the EAttribute with the given name for the specified object, or
	 *         null if non-existent
	 */
	static public EAttribute getAttribute(EObject eObject, String attName) {
		if (attName == null || attName.isEmpty()) {
			return null;
		}
		// return (EAttribute)
		// eObject.eGet(eObject.eClass().getEStructuralFeature(attName));
		for (EAttribute att : eObject.eClass().getEAllAttributes()) {
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
	 * @param eObject
	 * @param attName
	 * @return the EAttribute value with the given attribute name for the
	 *         specified object, Returns null if the attribute is not defined,
	 *         or has null as the value.
	 */
	static public Object getAttributeValue(EObject eObject, String attName) {
		EAttribute att = getAttribute(eObject, attName);
		if (att == null) {
			return null;
		} else {
			return eObject.eGet(att);
		}
	}

	/**
	 * Returns the EAttribute value with the given attribute name for the
	 * specified object. Throws if the attribute is not defined.
	 *
	 * @param eObject
	 * @param attName
	 * @return
	 * @throws Exception
	 */
	static public Object getAttributeValueThrows(EObject eObject, String attName) throws Exception {
		EAttribute att = getAttribute(eObject, attName);
		if (att == null) {
			throw new Exception(Messages.EmfUtil_ex_attribute_not_found + attName);
		} else {
			return eObject.eGet(att);
		}
	}
}