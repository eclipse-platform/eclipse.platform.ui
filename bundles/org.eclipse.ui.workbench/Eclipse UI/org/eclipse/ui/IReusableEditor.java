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
 * Interface for reusable editors. 
 * 
 * An editors may support changing its input so that 
 * the workbench may change its contents instead of 
 * opening a new editor.
 * 
 * Note: For EXPERIMENTAL use only. IT MAY CHANGE IN NEAR FUTURE.
 * 
 */
public interface IReusableEditor extends IEditorPart {
/**
 * @see EditorPart#setInput
 */
public void setInput(IEditorInput newInput);
}

