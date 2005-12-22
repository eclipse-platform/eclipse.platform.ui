/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.internal.databinding.swt;

import org.eclipse.swt.widgets.Display;

/**
 * This class will ensure that the code in the run() method is executed asynchronously on the Display thread
 */
public abstract class AsyncRunnable implements Runnable {
		
	/**
	 * Run the receiver asynchronously on the Display thread
	 * @param aDisplay The display 
	 */
	public final void runOn(Display aDisplay){
		if(aDisplay == Display.getCurrent()){
			run();
		} else {
			aDisplay.asyncExec(this);
		}		
	}
}
