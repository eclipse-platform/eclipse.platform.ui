/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.tests.viewers;


/**
 * The VirtualLazyTableViewerTest is a test of table viewers
 * with lazy population.
 */
public class VirtualLazyTableViewerTest extends VirtualTableViewerTest {
	
	/**
	 * Create a new instance of the receiver/
	 * @param name
	 */
	public VirtualLazyTableViewerTest(String name) {
		super(name);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.tests.viewers.TableViewerTest#getContentProvider()
	 */
	protected TestModelContentProvider getContentProvider() {
		return new TestLazyModelContentProvider(this);
	}
	

}
