/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.ui;

import java.util.Dictionary;

/**
 * Descriptor of a concrete instance of a search engine. It describes a search
 * engine that is either loaded from the plug-in extension point contribution,
 * or created by the user in the scope settings dialog. Engines created by the
 * user are marked as such. Only user-defined engines will permit their label or
 * description changed.
 * 
 * @since 3.1
 */

public interface IEngineDescriptor {
	/**
	 * Returns the unique identifier of this engine instance.
	 * 
	 * @return the unique engine identifier
	 */
	String getId();

	/**
	 * Returns the unique identifier of the engine type of which this is an
	 * instance.
	 * 
	 * @return the engine type identifier
	 */
	String getEngineTypeId();

	/**
	 * Returns the label of this engine for rendering in the UI.
	 * 
	 * @return the engine label
	 */
	String getLabel();

	/**
	 * Changes the label of this engine. This method does nothing for engine
	 * descriptors that are not user-defined.
	 * 
	 * @param label
	 *            the new engine label
	 */
	void setLabel(String label);

	/**
	 * 
	 * @return
	 */
	String getDescription();

	/**
	 * Changes the description of this engine. This method does nothing for
	 * engine descriptors that are not user-defined.
	 * 
	 * @param desc
	 *            the new engine description
	 */
	void setDescription(String desc);

	/**
	 * Returns the parameters used to configure this engine according to the
	 * valid parameters for the associated engine type.
	 * 
	 * @return the parameter dictionary
	 */
	Dictionary getParameters();

	/**
	 * Tests whether this engine is provided as an extension point contribution
	 * or is created by the user in the scope settings dialog.
	 * 
	 * @return <code>true</code> if the engine is user defined, or
	 *         <code>false</code> otherwise.
	 */
	boolean isUserDefined();
}
