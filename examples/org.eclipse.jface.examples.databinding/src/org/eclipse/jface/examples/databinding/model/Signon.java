/*******************************************************************************
 * Copyright (c) 2005, 2014 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.examples.databinding.model;

import org.eclipse.jface.examples.databinding.ModelObject;

public class Signon extends ModelObject {

	String userId;
	String password;

	public Signon(String aUserId, String aPassword) {
		userId = aUserId;
		password = aPassword;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String aPassword) {
		String oldValue = password;
		password = aPassword;
		firePropertyChange("password", oldValue, password);
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String aUserId) {
		String oldValue = userId;
		userId = aUserId;
		firePropertyChange("userId", oldValue, userId);
	}

}
