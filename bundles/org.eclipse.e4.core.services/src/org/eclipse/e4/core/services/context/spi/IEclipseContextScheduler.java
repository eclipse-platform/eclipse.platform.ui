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

package org.eclipse.e4.core.services.context.spi;

import org.eclipse.e4.core.services.context.IEclipseContext;

public interface IEclipseContextScheduler extends IEclipseContextStrategy {

	public void schedule(Runnable runnable);

	/**
	 * This is the same method but for more involved listeners. It should pass in 
	 * the context that has been changed, name of the changed service, and the arguments.
	 * 
	 * @return false: the runnable no longer valid and needs to be cleaned up from the list
	 */
	public boolean schedule(IEclipseContext context, IRunAndTrack runnable, String name, int eventType, Object[] args);
	
}
