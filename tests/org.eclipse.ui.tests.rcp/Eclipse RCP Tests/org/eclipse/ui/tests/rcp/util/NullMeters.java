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
 * An empty array of meters, all calls are ignored.
 * 
 * @since 3.1
 */
public class NullMeters implements IPerformanceMeterArray {

    public NullMeters() {
        // do nothing
    }

    public void start(int meterIndex) {
        // do nothing
    }

    public void stop(int meterIndex) {
        // do nothing
    }

    public void intervalBoundary(int completedIntervalIndex) {
        // do nothing
    }

    public void commit() {
        // do nothing
    }

    public void assertPerformance() {
        // do nothing
    }

    public void dispose() {
        // do nothing
    }
}
