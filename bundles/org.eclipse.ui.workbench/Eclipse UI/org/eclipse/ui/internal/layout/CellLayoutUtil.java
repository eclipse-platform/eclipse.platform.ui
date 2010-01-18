/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.layout;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * Contains helper methods for CellLayout. Many of these methods are workarounds for
 * known SWT bugs. They should be updated once the original bugs are fixed in SWT.
 * 
 * @since 3.0
 */
class CellLayoutUtil {

    private static Point zero = new Point(0, 0);

    private static Point minimumShellSize;

    private static CellData defaultData = new CellData();

    /**
     * Returns the minimum size for the given composite. That is, 
     * this returns the smallest values that will have any effect 
     * when passed into the composite's setSize method. Passing any
     * smaller value is equivalent to passing the minimum size.
     * <p>
     * This method is intended for use by layouts. The layout can
     * use this information when determining its preferred size. 
     * Returning a preferred size smaller than the composite's 
     * minimum size is pointless since the composite could never
     * be set to that size. The layout may choose a different preferred
     * size in this situation. 
     * </p><p>
     * Note that this method is only concerned with restrictions imposed
     * by the composite; not it's layout. If the only restriction on the 
     * composite's size is imposed by the layout, then this method returns (0,0). 
     * </p><p>
     * Currently SWT does not expose this information through
     * API, so this method is developed using trial-and-error. Whenever
     * a composite is discovered that will not accept sizes below
     * a certain threshold on some platform, this method should be
     * updated to reflect that fact. 
     * </p><p>
     * At this time, the only known composite that has a minimum size
     * are Shells. 
     * </p>
     * 
     * @param toCompute the composite whose minimum size is being computed
     * @return a size, in pixels, which is the smallest value that can be
     * passed into the composite's setSize(...) method.
     */
    static Point computeMinimumSize(Composite toCompute) {
        if (toCompute instanceof Shell) {
            if (minimumShellSize == null) {
                Shell testShell = new Shell((Shell) toCompute, SWT.DIALOG_TRIM
                        | SWT.RESIZE);
                testShell.setSize(0, 0);
                minimumShellSize = testShell.getSize();
                testShell.dispose();
            }

            return minimumShellSize;
        }

        // If any other composites are discovered to have minimum sizes,
        // add heuristics for them here.

        // Otherwise, the composite can be reduced to size (0,0)

        return zero;
    }

    /**
     * Returns the CellData associated with the given control. If the control
     * does not have any layout data associated with it, a default object is returned.
     * If the control has a GridData object associated with it, an equivalent
     * CellData object will be returned.
     *  
     * @param control
     * @return
     */
    static CellData getData(Control control) {
        Object layoutData = control.getLayoutData();
        CellData data = null;

        if (layoutData instanceof CellData) {
            data = (CellData) layoutData;
        } else if (layoutData instanceof GridData) {
            data = new CellData((GridData) layoutData);
        }

        if (data == null) {
            data = defaultData;
        }

        return data;
    }
}
