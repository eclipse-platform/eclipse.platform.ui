/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.net.auth;

/**
 * Keeps user and password strings.
 */
public class Authentication {
	protected String user;
	protected String password;
	public Authentication(String user, String password){
		this.user = user;
		this.password = password;
	}

	/**
	 * @return Returns the password.
	 */
	public String getPassword() {
		return password;
	}
	/**
	 * @return Returns the user.
	 */
	public String getUser() {
		return user;
	}
}
