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
package org.eclipse.ui.wizards.datatransfer;

import java.util.*;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.ModalContext;

/**
 * The PopulateElementOperation has a MinimizedFileSystemElement as an object as this is
 * just filled in.
 */
class PopulateElementOperation extends PopulateRootOperation {
/**
 * Create a new <code>PopulateElementsOperation</code>.
 * @param rootObject the object to be populated
 * @param structureProvider the object that defines how we are to populate it.
 */
public PopulateElementOperation(
	MinimizedFileSystemElement rootObject,
	IImportStructureProvider structureProvider) {
	super(rootObject, structureProvider);
}
/**
 * Populates the children of element down to level depth
 */
private void populateElement(
	MinimizedFileSystemElement element,
	IProgressMonitor monitor)
	throws InterruptedException {

	Object fileSystemObject = element.getFileSystemObject();
	ModalContext.checkCanceled(monitor);

	
	List children = provider.getChildren(fileSystemObject);
	if (children == null)
		children = new ArrayList(1);
	Iterator childrenEnum = children.iterator();
	while (childrenEnum.hasNext()) {
		//Create one level below
		createElement(element, childrenEnum.next(), 1);
	}
	element.setPopulated();
}
/**
 * Runs the operation. The result of this operation is always the elemen provided.
 */
public void run(IProgressMonitor monitor) throws InterruptedException {
	try {
		this.monitor = monitor;
		monitor.beginTask(DataTransferMessages.getString("DataTransfer.scanningChildren"),IProgressMonitor.UNKNOWN); //$NON-NLS-1$
		MinimizedFileSystemElement element = (MinimizedFileSystemElement) root;
		populateElement(element,monitor);
		
	} finally {
		monitor.done();
	}
}
}
