/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.skins;

import org.eclipse.swt.widgets.Composite;

/**
 * This is a factory for objects that control the appearance of editors and
 * views.
 * 
 * @since 3.0
 */
public abstract class AbstractPresentationFactory {
	
	/**
	 * Creates a skin for a tab folder (a stackable set of views docked
	 * in the workbench window. Must not return null;
	 * 
	 * @param flags any combination of SWT.MIN, SWT.MAX, and SWT.CLOSE
	 * @return a newly created part stack
	 */
	public abstract StackPresentation createViewStack(Composite parent, IStackPresentationSite container, int flags);

	/**
	 * Creates a skin for an editor workbook (a stackable set of editors 
	 * docked in the workbench window). Must not return null.
	 * 
	 * @param flags any combination of SWT.MIN, SWT.MAX, and SWT.CLOSE
	 * @return a newly created part stack
	 */
	public abstract StackPresentation createEditorStack(Composite parent, IStackPresentationSite container, int flags);
	
	/**
	 * Creates a skin for a tab folder (a stackable set of views docked
	 * in the workbench window. Returns null iff this skin does not support
	 * fast views.
	 * 
	 * TODO: document flags
	 * 
	 * @param flags any combination of SWT.MIN, SWT.MAX, and SWT.CLOSE
	 * @return a newly created part stack
	 */
	public PartPresentation createFastViewPresentation(Composite parent, 
			IPartPresentationSite container, IPresentablePart thePart, int flags) {
		return null;
	}
	
	/**
	 * Creates a skin for a detached window. Returns null iff this skin
	 * does not support detached windows.
	 * 
	 * TODO: javadoc
	 * 
	 * @since 3.0
	 */
	public PartPresentation createDetachedWindowPresentation(Composite parent, 
			IPartPresentationSite container, IPresentablePart thePart, int flags) {
		return null;
	}
	
}
