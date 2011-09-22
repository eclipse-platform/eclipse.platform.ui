/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.performance.layout;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.presentations.AbstractPresentationFactory;
import org.eclipse.ui.tests.performance.UIPerformancePlugin;
import org.eclipse.ui.tests.performance.presentations.PresentationTestbed;
import org.eclipse.ui.tests.performance.presentations.TestPresentablePart;

public class PresentationWidgetFactory extends TestWidgetFactory {

    private AbstractPresentationFactory factory;
    private int type;
    private Shell shell;
    private Image img;
    private Control ctrl;
    private int numParts;
    
    public PresentationWidgetFactory(AbstractPresentationFactory factory, int type, int numParts) {
        this.factory = factory;
        this.type = type;
        this.numParts = numParts;
    }
    
    public void init() throws CoreException, WorkbenchException {
        super.init();
        
        img = UIPerformancePlugin.getImageDescriptor("icons/anything.gif").createImage();
        Display display = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().getDisplay();
        
        shell = new Shell(display);
    
        TestPresentablePart selection = null;
        PresentationTestbed testBed = new PresentationTestbed(shell, factory, type);
        for(int partCount = 0; partCount < numParts; partCount++) {
            TestPresentablePart part = new TestPresentablePart(shell, img);
            part.setName("Some part");
            part.setContentDescription("Description");
            part.setTitle("Some title");
            part.setDirty(partCount % 2 == 0);
            part.setTooltip("This is a tooltip");
            testBed.add(part);
            selection = part;
        }
        
        testBed.setSelection(selection);
        
        ctrl = testBed.getControl();
        shell.setBounds(0,0,1024,768);
        ctrl.setBounds(shell.getClientArea());
        shell.setVisible(true);
    }
    
    public void done() throws CoreException, WorkbenchException {
        shell.dispose();
        img.dispose();

        super.done();
    }
    
    public static String describePresentation(AbstractPresentationFactory factory, int type) {
        String typeDesc = "unknown";
        
        switch(type) {
//        case PresentationFactoryUtil.ROLE_EDITOR: typeDesc = "editor"; break;
//        case PresentationFactoryUtil.ROLE_STANDALONE: typeDesc = "standalone with title"; break;
//        case PresentationFactoryUtil.ROLE_STANDALONE_NOTITLE: typeDesc = "standalone without title"; break;
//        case PresentationFactoryUtil.ROLE_VIEW: typeDesc = "view"; break;
        }
        
        return "Presentation " + factory.getId() + " " + typeDesc;
        
    }
    
    public String getName() {
        return describePresentation(factory, type);
    }

    public Composite getControl() throws CoreException, WorkbenchException {
        return (Composite)ctrl;
    }

}
