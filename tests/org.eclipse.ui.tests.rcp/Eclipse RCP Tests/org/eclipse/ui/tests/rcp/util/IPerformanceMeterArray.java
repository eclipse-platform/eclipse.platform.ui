/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.rcp.util;

/**
 * An array of performance meters that can be used for things like interval timing.
 * 
 * @since 3.1
 */
public interface IPerformanceMeterArray {

    /**
     * Start the meter at the argument index.
     */
    public void start(int meterIndex);

    /**
     * Stop the meter at the argument index.
     */
    public void stop(int meterIndex);

    /**
     * Commit all meters in this array.
     */
    public void commit();

    /**
     * Assert the performance of all meters in this array.
     */
    public void assertPerformance();

    /**
     * Dispose all meters in this array.
     */
    public void dispose();
}
