/*******************************************************************************
 * Copyright (c) 2003, 2009 IBM Corporation and others.
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
 * Allows clients to coordinate state across components that are part of the
 * same logical extension.
 * 
 * <p>
 * That is, a content provider might vary how it exposes its content based on
 * the state of a specific property in the model. Interested parties may add
 * themselves as {@link IPropertyChangeListener}s to track changes in the state
 * model.
 * </p>
 * 
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 * @since 3.2
 * 
 */
public interface IExtensionStateModel {

	/**
	 * The id is used to look up the state model across different components of
	 * the same logical extension. Generally, the id of the content extension is
	 * used.
	 * 
	 * @return The unique identifier of this state model.
	 */
	String getId();

	/**
	 * 
	 * @return The viewer id that this state model is associated with.
	 */
	String getViewerId();

	/**
	 * 
	 * @param aPropertyName
	 *            The name of a given property
	 * @return The current value of the property.
	 */
	String getStringProperty(String aPropertyName);

	/**
	 * 
	 * @param aPropertyName
	 *            The name of a given property
	 * @return The current value of the property.
	 */
	boolean getBooleanProperty(String aPropertyName);

	/**
	 * 
	 * @param aPropertyName
	 *            The name of a given property
	 * @return The current value of the property.
	 */
	int getIntProperty(String aPropertyName);

	/**
	 * 
	 * @param aPropertyName
	 *            The name of a given property
	 * @return The current value of the property.
	 */
	Object getProperty(String aPropertyName);

	/**
	 * 
	 * @param aPropertyName
	 *            The name of a given property
	 * @param aPropertyValue
	 *            The new value of a the given property.
	 */
	void setStringProperty(String aPropertyName, String aPropertyValue);

	/**
	 * 
	 * @param aPropertyName
	 *            The name of a given property
	 * @param aPropertyValue
	 *            The new value of a the given property.
	 */
	void setBooleanProperty(String aPropertyName, boolean aPropertyValue);

	/**
	 * 
	 * @param aPropertyName
	 *            The name of a given property
	 * @param aPropertyValue
	 *            The new value of a the given property.
	 */
	void setIntProperty(String aPropertyName, int aPropertyValue);

	/**
	 * 
	 * @param aPropertyName
	 *            The name of a given property
	 * @param aPropertyValue
	 *            The new value of a the given property.
	 */
	void setProperty(String aPropertyName, Object aPropertyValue);

	/**
	 * 
	 * @param aListener
	 *            An implementation of {@link IPropertyChangeListener} that
	 *            should be notified when changes occur in this model.
	 */
	void addPropertyChangeListener(IPropertyChangeListener aListener);

	/**
	 * 
	 * @param aListener
	 *            An implementation of {@link IPropertyChangeListener} that
	 *            should no longer be notified when changes occur in this model.
	 */
	void removePropertyChangeListener(IPropertyChangeListener aListener);
}
