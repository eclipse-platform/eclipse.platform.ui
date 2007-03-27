/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.compare.internal;

public interface ICompareAsText {
	
	/**
	 * Property key for the set of test inputs (instance of Set)
	 */
	public static final String PROP_TEXT_INPUTS = "org.eclipse.compare.TextInputs"; //$NON-NLS-1$
	
	public void compareAsText(Object input);

}
