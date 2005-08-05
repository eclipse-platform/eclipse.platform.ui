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
package org.eclipse.jface.viewers;

/**
 * The ILazyContentProvider is the content provider
 * for table viewers created using the SWT.VIRTUAL flag that
 * only wish to return thier contents as they are queried.
 * 
 * <strong>NOTE:</strong> As the ILazyContentProvider does
 * not have API for determining the total item count any
 * changes to the number of items for this object while
 * require a call to <code>#setItemCount</code> on the 
 * viewer that uses it.
 */
public interface ILazyContentProvider extends IContentProvider {
	/**
	 * Called when a previously-blank item becomes visible in the
	 * TableViewer. If the content provider knows the element
	 * at this row, it should respond by calling 
	 * TableViewer#replace(Object, int)
	 * 
	 * @param index The index that is being updateed in the
	 * table.
	 */
	public void updateElement(int index);
	
}
