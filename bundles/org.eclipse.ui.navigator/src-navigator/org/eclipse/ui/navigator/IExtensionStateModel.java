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
package org.eclipse.ui.navigator;

import org.eclipse.jface.util.IPropertyChangeListener;
/**
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * @since 3.2
 *
 */
public interface IExtensionStateModel {

	String getId();

	String getViewerId();

	String getStringProperty(String aPropertyName);

	boolean getBooleanProperty(String aPropertyName);

	int getIntProperty(String aPropertyName);
	
	Object getProperty(String aPropertyName);

	void setStringProperty(String aPropertyName, String aPropertyValue);

	void setBooleanProperty(String aPropertyName, boolean aPropertyValue);

	void setIntProperty(String aPropertyName, int aPropertyValue);
	
	void setProperty(String aPropertyName, Object aPropertyValue);

	void addPropertyChangeListener(IPropertyChangeListener aListener);

	void removePropertyChangeListener(IPropertyChangeListener aListener);
}
