/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.services;

import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.jface.bindings.TriggerSequence;

/**
 * This interface is expected to become a replacement of the IBindingService
 * for the E4.
 * 
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface EBindingService  {

	public TriggerSequence getBestActiveBindingFor(ParameterizedCommand command);
	
}
