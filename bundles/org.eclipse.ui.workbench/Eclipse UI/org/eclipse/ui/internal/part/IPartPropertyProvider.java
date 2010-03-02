/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.part;

import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Provides the listeners and get methods that are available to Eclipse 3.0
 * IWorkbenchPart, IEditorPart, IViewPart, and IWorkbenchPart2. Every new-style
 * part needs to be supplied with one of these so that it can later be adapted
 * back to an IWorkbenchPart.
 * <p>
 * If the new-style part wraps an old-style part, it supply an implementation that
 * redirects directly to the old-style part (see <code>OldPartToNewAdapter</code>).
 * When wrapping a new-style part inside an old-style part, the wrapper should supply 
 * an <code>IPartPropertyProvider</code>. Otherwise, the new-style part will use the 
 * default implementation (<code>PartPropertyProvider</code>).
 * </p>
 * 
 * @since 3.1
 */
public interface IPartPropertyProvider {
    public void addPropertyListener(IWorkbenchPart part, IPropertyListener l);
    public void removePropertyListener(IWorkbenchPart part, IPropertyListener l);
    public String getTitleToolTip();
    public Image getTitleImage();
    public String getPartName();
    public String getTitle();
    public String getContentDescription();
    public IEditorInput getEditorInput();
    public boolean isDirty();
}
