/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui;



/**
 * The input element provider. Used to query root elements from a content 
 * provider contributed using the org.eclipse.ui.navigator extension point.
 * To query root elements the Navigator will pass the workspace root 
 * (<code>IWorkspaceRoot</code>) to the input provider and use the result 
 * to get the root elements from the content provider.
 * If the content provider can not supply elements from the workspace root
 * an input provider should be used to convert to an object that is suitable
 * for the content provoder.
 */
public interface IInputProvider {
	/**
	 * @return the object compatible with the content provider contributed 
	 * 	using the same org.eclipse.ui.navigator extension that references
	 * 	this object. <code>null</code> if the input can not be converted.
	 *	An object of type <code>IWorkspaceRoot</code> should always be processed
	 *	since it is used to get the root elements.
	 */
	public Object getInput(Object inputResource);	
}