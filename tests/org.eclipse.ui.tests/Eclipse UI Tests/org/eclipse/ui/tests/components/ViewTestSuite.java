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
package org.eclipse.ui.tests.components;

import junit.framework.Test;

import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.tests.TestPlugin;
import org.eclipse.ui.tests.autotests.AutoTestSuite;
import org.eclipse.ui.views.IViewDescriptor;
import org.eclipse.ui.views.IViewRegistry;

/**
 * @since 3.1
 */
public class ViewTestSuite extends AutoTestSuite {
    /**
     * Returns the suite. This is required to use the JUnit Launcher.
     */
    public static Test suite() {
        return new ViewTestSuite();
    }

    private void addTests(IPartBuilder builder) {
        //addTest(new CreatePartTest(builder));
        addTest(new PersistPartTest(getLog(), builder));
    }
    
    /**
     * 
     */
    public ViewTestSuite() {
        super(Platform.find(TestPlugin.getDefault().getBundle(), new Path("data/ViewTestSuite.xml")));

        IViewRegistry viewRegistry = WorkbenchPlugin.getDefault().getViewRegistry();
        
        IViewDescriptor[] views = viewRegistry.getViews();
        for (int i = 0; i < views.length; i++) {
            IViewDescriptor descriptor = views[i];
            
            addTests(new ViewBuilder(descriptor.getId()));
        }
    }
    
}
