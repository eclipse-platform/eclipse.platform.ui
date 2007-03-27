/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.debug.internal.ui.views.memory.renderings;

public interface IVirtualContentListener {
	
	public int BUFFER_START = 0;
	public int BUFFER_END = 1;
	
	/**
	 * Called when the viewer is at the beginning of its bufferred content
	 */
	public void handledAtBufferStart();
	
	/**
	 * Called when viewer is at the end of its bufferred content
	 */
	public void handleAtBufferEnd();
	
	/**
	 * @return
	 */
	public int getThreshold(int bufferEndOrStart);

}
