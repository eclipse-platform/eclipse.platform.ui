/*******************************************************************************
 * Copyright (c) 2004, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.tests.performance.presentations;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;

import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;

/**
 * @since 3.1
 */
public class PresentationPerformanceTestSuite extends TestSuite {

    
    /**
     * Returns the suite. This is required to use the JUnit Launcher.
     */
    public static Test suite() {
        return new PresentationPerformanceTestSuite();
    }
    
    /**
     * 
     */
    public PresentationPerformanceTestSuite() {        
        String[] ids = getPresentationIds();
        
        for (int i = 0; i < ids.length; i++) {
            String string = ids[i];
            
            addTests(string);
        }
    }

    // TBD Does it make sense to keep those in 4.x? They should be replaced 
    // by CSS styling performance tests in SWT
    private void addTests(String presentationId) {
//        AbstractPresentationFactory factory = WorkbenchPlugin.getDefault().getPresentationFactory(presentationId);
//        
//        boolean r21Presentation = "org.eclipse.ui.internal.r21presentationFactory".equals(presentationId);
//
//        addTest(new PresentationInactivePartPropertyTest(factory, PresentationFactoryUtil.ROLE_EDITOR, 5));
//        addTest(new PresentationInactivePartPropertyTest(factory, PresentationFactoryUtil.ROLE_VIEW, 5));
//        
//        addTest(new PresentationActivePartPropertyTest(factory, PresentationFactoryUtil.ROLE_EDITOR, 5, false));
//        addTest(new PresentationActivePartPropertyTest(factory, PresentationFactoryUtil.ROLE_VIEW, 5, false));
//        addTest(new PresentationActivePartPropertyTest(factory, PresentationFactoryUtil.ROLE_STANDALONE, 1, false));
//        if(r21Presentation)
//        	addTest(new PresentationActivePartPropertyTest(factory, PresentationFactoryUtil.ROLE_STANDALONE_NOTITLE, 1, false));
//
//        addTest(new PresentationCreateTest(factory, PresentationFactoryUtil.ROLE_EDITOR, 100, "large folder creation"));
//        addTest(new PresentationCreateTest(factory, PresentationFactoryUtil.ROLE_VIEW, 100, "large folder creation"));
//        
//        addTest(new PresentationCreateTest(factory, PresentationFactoryUtil.ROLE_EDITOR, 5));
//        addTest(new PresentationCreateTest(factory, PresentationFactoryUtil.ROLE_VIEW, 5));
//        addTest(new PresentationCreateTest(factory, PresentationFactoryUtil.ROLE_STANDALONE, 1));
//        if (r21Presentation)
//        	addTest(new PresentationCreateTest(factory, PresentationFactoryUtil.ROLE_STANDALONE_NOTITLE, 1));
//        
//        addTest(new PresentationSelectTest(factory, PresentationFactoryUtil.ROLE_EDITOR, 100));
//        addTest(new PresentationSelectTest(factory, PresentationFactoryUtil.ROLE_VIEW, 100));
//        
//        addTest(new PresentationActivateTest(factory, PresentationFactoryUtil.ROLE_EDITOR, 5));
//        addTest(new PresentationActivateTest(factory, PresentationFactoryUtil.ROLE_VIEW, 5));
//        addTest(new PresentationActivateTest(factory, PresentationFactoryUtil.ROLE_STANDALONE, 1));
//        if(r21Presentation)
//        	addTest(new PresentationActivateTest(factory, PresentationFactoryUtil.ROLE_STANDALONE_NOTITLE, 1));
//        
//        addTest(new ResizeTest(new PresentationWidgetFactory(factory, PresentationFactoryUtil.ROLE_EDITOR, 5)));
//        addTest(new ResizeTest(new PresentationWidgetFactory(factory, PresentationFactoryUtil.ROLE_VIEW, 5)));
//        addTest(new ResizeTest(new PresentationWidgetFactory(factory, PresentationFactoryUtil.ROLE_STANDALONE, 1)));
//        if (r21Presentation)
//        	addTest(new ResizeTest(new PresentationWidgetFactory(factory, PresentationFactoryUtil.ROLE_STANDALONE_NOTITLE, 1)));
        
    }
    
    private static String[] getPresentationIds() {
        return listIds(IWorkbenchRegistryConstants.PL_PRESENTATION_FACTORIES,
                "factory");
    }
    
    private static String[] listIds(String extensionPointId, String elementName) {
        
        List result = new ArrayList();
        
        IExtensionPoint extensionPoint = Platform.getExtensionRegistry()
                .getExtensionPoint(WorkbenchPlugin.PI_WORKBENCH, extensionPointId);
        if (extensionPoint == null) {
            WorkbenchPlugin
                    .log("Unable to find extension. Extension point: " + extensionPointId + " not found"); //$NON-NLS-1$ //$NON-NLS-2$
            return null;
        }
        
        // Loop through the config elements.
        IConfigurationElement[] elements = extensionPoint
                .getConfigurationElements();
        for (int j = 0; j < elements.length; j++) {
            IConfigurationElement element = elements[j];
            if (elementName == null || elementName.equals(element.getName())) {
                String strID = element.getAttribute("id"); //$NON-NLS-1$
                if (strID != null) {
                    result.add(strID);
                }
            }
        }
        
        return (String[]) result.toArray(new String[result.size()]);
    }    
    
}
