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
package org.eclipse.ui.tests.performance.layout;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.internal.WindowTrimProxy;
import org.eclipse.ui.internal.layout.TrimLayout;

/**
 * @since 3.1
 */
public class RecursiveTrimLayoutWidgetFactory extends TestWidgetFactory {

    private Shell shell;
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.tests.performance.layout.TestWidgetFactory#getName()
     */
    public String getName() {
        return "Massively Recursive TrimLayout";
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.tests.performance.layout.TestWidgetFactory#done()
     */
    public void done() throws CoreException, WorkbenchException {
        super.done();
        
        shell.dispose();
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.tests.performance.layout.TestWidgetFactory#init()
     */
    public void init() throws CoreException, WorkbenchException {
        super.init();
        
		Display display = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().getDisplay();
		
		shell = new Shell(display);
	
		createTrimLayout(shell, 10, SWT.TOP);
		
		shell.setBounds(0,0,1024,768);
		shell.setVisible(true);
    }
    
    
    /**
     * Create a set of recursive trim layouts with the given depth. That is, the
     * trim controls themselves are also trim layouts. This ensures that TrimLayout 
     * can handle 
     * Note that this
     * will create O(2^depth) children.
     * 
     * @param composite
     * @param depth
     * @param side
     * @since 3.1
     */
    public void createTrimLayout(Composite composite, int depth, int side) {
        if (depth == 0) {
            composite.setLayout(new ConstantAreaLayout(5000, 300));
        } else {
            TrimLayout layout = new TrimLayout();
            
            composite.setLayout(layout);
            
            int nextSide = SWT.TOP; 
            
            switch (side) {
            	case SWT.TOP : nextSide = SWT.RIGHT; break;
            	case SWT.RIGHT : nextSide = SWT.BOTTOM; break;
            	case SWT.BOTTOM : nextSide = SWT.LEFT; break;
            	case SWT.LEFT : nextSide = SWT.TOP; break;
            }
            
            // Add a resizable child
            Composite child = new Composite(composite, SWT.NONE);
            WindowTrimProxy proxy = new WindowTrimProxy(child, 
            		"child1." + side + "." + depth, "Resizable Child", SWT.NONE, true);
            layout.addTrim(side, proxy);
            createTrimLayout(child, depth - 1, nextSide); 

            // Add a non-resizable child
            child = new Composite(composite, SWT.NONE);
            proxy = new WindowTrimProxy(child, 
            		"child2." + side + "." + depth, "Non-Resizable Child", SWT.NONE, false);
            layout.addTrim(side, proxy);
            createTrimLayout(child, depth - 1, nextSide);
            
            // Fill the client area
            child = new Composite(composite, SWT.NONE);
            layout.setCenterControl(child);
            child.setLayout(new ConstantAreaLayout(3000, 150));
        }
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.tests.performance.layout.TestWidgetFactory#getControl()
     */
    public Composite getControl() throws CoreException, WorkbenchException {
		return shell;
    }

}
