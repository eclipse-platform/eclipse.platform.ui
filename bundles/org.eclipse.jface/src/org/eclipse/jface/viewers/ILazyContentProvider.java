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
 * The ILazyContentProvider is the content provider
 * for table viewers created using the SWT.VIRTUAL flag that
 * only wish to return thier contents as they are queried.
 * <strong>Note: This API is experimental and may be changed
 * before Eclipse 3.1 is released.</strong>
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
