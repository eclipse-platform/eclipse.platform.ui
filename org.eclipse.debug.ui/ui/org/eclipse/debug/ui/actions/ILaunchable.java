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
package org.eclipse.debug.ui.actions;

/**
 * Interface used to tag objects as launchable. Objects that provide
 * an adapter of this type will be considered by the contextual
 * launch support. 
 * <p>
 * This interface is not intended to be implemented. Instead clients
 * may contribute an adapter of this type for launchable objects
 * via the <code>org.eclipse.core.runtime.adapters</code> extension
 * point. A factory and implementation of this interface are not actually
 * required.
 * </p>
 * @see org.eclipse.debug.ui.actions.ContextualLaunchAction
 * @since 3.0
 */
public interface ILaunchable {

}
