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
package org.eclipse.jface.databinding.internal.swt;

import org.eclipse.swt.widgets.Display;

/**
 * This class will ensure that the code in the run() method is executed synchronously on the Display thread
 */
public abstract class SyncRunnable {
	
	private Object result;
	/**
	 * Execute the code that is guaranteed to run on the Display thread
	 * @return Object 
	 */
	public abstract Object run();
	
	/**
	 * @param aDisplay
	 * @return Object the result of running the run() method
	 */
	public final Object runOn(Display aDisplay){
		if(aDisplay == Display.getCurrent()){
			result = run();
		} else {
			aDisplay.syncExec(new Runnable(){
				public void run(){
					result = SyncRunnable.this.run();
				}
			});
		}
		return result;		
	}
}
