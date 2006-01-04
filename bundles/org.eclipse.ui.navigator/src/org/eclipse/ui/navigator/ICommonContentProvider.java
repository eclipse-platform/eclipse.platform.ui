/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.navigator;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.ui.IMemento;

/**
 * 
 * The Common content provider allows extensions to vary their behavior based on
 * properties in the extension model and the given memento. The state model
 * should be initialized from values in the memento if necessary.
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * 
 * <p>
 * Clients are not required to implement this interface if there is no cause to
 * do so. {@link ITreeContentProvider} is respected by the Common Navigator.
 * </p>
 * 
 * @since 3.2
 * 
 */
public interface ICommonContentProvider extends ITreeContentProvider,
		IMementoAware {

	/**
	 * Initialize the content provider with the given extension model and
	 * memento.
	 * 
	 * @param aStateModel
	 *            The state model associated with this logical extension.
	 * @param aMemento
	 *            The associated memento for the given viewer. Clients should
	 *            ensure that the memento keys are unique; perhaps using the id
	 *            of the content extension as a prefix.
	 * 
	 * @see ICommonLabelProvider
	 */
	void init(IExtensionStateModel aStateModel, IMemento aMemento);

}
