/**********************************************************************
Copyright (c) 2000, 2003 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM Corporation - Initial implementation
**********************************************************************/
package org.eclipse.jface.text;

/**
 * Interface implemented by <code>IDocument</code> implementers that offer
 * repair methods on their documents. The following repair methods are
 * provided:
 * <ul>
 * <li>repairing line information</li>
 * </ul>
 * 
 * @since 3.0
 */
public interface IRepairableDocument {
	
	
	/**
	 * Repairs the line information of this document.
	 */
	void repairLineInformation();

}
