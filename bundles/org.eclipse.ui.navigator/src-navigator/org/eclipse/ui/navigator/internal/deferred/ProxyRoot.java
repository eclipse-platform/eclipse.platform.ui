/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
/*
 * Created on Sep 15, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.eclipse.ui.navigator.internal.deferred;


/**
 * @author mdelder
 * 
 * TODO To change the template for this generated type comment go to Window - Preferences - Java -
 * Code Style - Code Templates
 */
public class ProxyRoot {

	public final String name;
	public final String id;
	private Object inputElement;

	public ProxyRoot(String name, String id) {
		this.name = name;
		this.id = id;
	}

	public Object getInputElement() {
		return inputElement;
	}

	public void setInputElement(Object inputElement) {
		this.inputElement = inputElement;
	}
}