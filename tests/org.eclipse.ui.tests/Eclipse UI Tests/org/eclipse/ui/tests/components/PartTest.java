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

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.components.framework.FactoryMap;
import org.eclipse.ui.internal.components.framework.ServiceFactory;
import org.eclipse.ui.internal.part.Part;
import org.eclipse.ui.tests.autotests.AbstractTestLogger;
import org.eclipse.ui.tests.autotests.UITestCaseWithResult;

/**
 * @since 3.1
 */
public abstract class PartTest extends UITestCaseWithResult {
    protected Part part;
    protected IPartBuilder builder;
    private String name;
    
    public PartTest(String testName, AbstractTestLogger log, IPartBuilder partBuilder) {
        super(testName, log);
        this.builder = partBuilder;
        this.name = testName + " " + partBuilder.toString();
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.tests.result.AutoTest#getName()
     */
    public String getName() {
        return name;
    }
    
    protected Part createPart(Composite parent, ServiceFactory context, IMemento savedState) throws Throwable {
        return builder.createPart(parent, context, savedState);
    }
    
    protected Part createPart(Composite parent) throws Throwable {
        return builder.createPart(parent, new FactoryMap(), null);
    }
    
    protected void destroyPart(Part toDestroy) throws Throwable {
        toDestroy.getControl().dispose();
    }
    
    protected Shell createShell() {
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        
        Shell testShell = new Shell(page.getWorkbenchWindow().getShell(), SWT.NONE);
        testShell.setLayout(new FillLayout());
     
        return testShell;
    }
    
    
}
