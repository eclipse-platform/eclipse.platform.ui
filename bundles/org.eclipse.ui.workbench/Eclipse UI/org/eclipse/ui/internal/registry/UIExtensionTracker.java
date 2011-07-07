/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.registry;

import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.dynamichelpers.ExtensionTracker;
import org.eclipse.core.runtime.dynamichelpers.IExtensionChangeHandler;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * @since 3.1
 */
public class UIExtensionTracker extends ExtensionTracker {
    private Display display;

    // SOMETHING HAS NOT BEEN DONE IN THE REGISTTRY CHANGED CODE
    // if (!PlatformUI.isWorkbenchRunning())
    // return;
    // int numDeltas = 0;
    // Display display = PlatformUI.getWorkbench().getDisplay();
    // if (display == null || display.isDisposed())
    // return;
    // It seems that the tracker should be closed.

    /**
	 * @param display
	 */
	public UIExtensionTracker(Display display) {
		this.display = display;
	}

	protected void applyRemove(final IExtensionChangeHandler handler, final IExtension removedExtension, final Object[] objects) {
        display.syncExec(new Runnable() {

            public void run() {
                try {
                    handler.removeExtension(removedExtension, objects);
                } catch (Exception e) {
                    WorkbenchPlugin.log(getClass(), "doRemove", e); //$NON-NLS-1$
                }
            }
        });
    }

    protected void applyAdd(final IExtensionChangeHandler handler, final IExtension addedExtension) {
        display.syncExec(new Runnable() {
            public void run() {
                try {
                    handler.addExtension(UIExtensionTracker.this, addedExtension);
                } catch (Exception e) {
                    WorkbenchPlugin.log(getClass(), "doAdd", e); //$NON-NLS-1$
                }
            }
        });
    }
}
