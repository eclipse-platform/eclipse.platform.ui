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
package org.eclipse.jface.viewers;

/**
 * The IVirtualTableContentProvider is the content provider
 * for table viewers created using the SWT.VIRTUAL flag that
 * only wish to return thier contents as they are queried.
 * <strong>Note: This API is experimental and may be changed
 * before Eclipse 3.1 is released.</strong>
 */
public interface ILazyContentProvider extends IContentProvider {
	

	/**
	 * Update the elements from the start index up to the 
	 * finish index.
	 * @param start The beginning index to populate.
	 * @param length The number of items to populate.
	 * @see TableViewer#replace(Object[], int) for details
	 * on how to insert the updated elements back into the
	 * TableViewer.
	 */
	public void updateElements(int start, int length);
	

	/**
	 * The elements from start to finish are no longer 
	 * valid. Should they require updating later 
	 * #updateElements(int, int) will be called.
	 * @param start The beginning index to invalidate.
	 * @param length The number of items to invalidate.
	 * @see #updateElements(int, int)
	 */
	public void invalidateElements(int start, int length);
	
}
