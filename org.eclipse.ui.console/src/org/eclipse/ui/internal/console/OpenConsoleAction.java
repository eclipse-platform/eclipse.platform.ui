/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.console;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolItem;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.resource.ImageDescriptor;

import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.activities.WorkbenchActivityHelper;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsoleFactory;

/**
 * @since 3.1
 */
public class OpenConsoleAction extends Action implements IMenuCreator {

    private ConsoleFactoryExtension[] fFactoryExtensions;
    private Menu fMenu;

    public OpenConsoleAction() {
        fFactoryExtensions = ((ConsoleManager)ConsolePlugin.getDefault().getConsoleManager()).getConsoleFactoryExtensions();
		setText(ConsoleMessages.OpenConsoleAction_0); 
		setToolTipText(ConsoleMessages.OpenConsoleAction_1);  
		setImageDescriptor(ConsolePluginImages.getImageDescriptor(IInternalConsoleConstants.IMG_ELCL_NEW_CON));
		setMenuCreator(this);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IConsoleHelpContextIds.CONSOLE_OPEN_CONSOLE_ACTION);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.action.IMenuCreator#dispose()
     */
    public void dispose() {
        fFactoryExtensions = null;
    }
    
    /*
     * @see org.eclipse.jface.action.Action#runWithEvent(org.eclipse.swt.widgets.Event)
     * @since 3.5
     */
    public void runWithEvent(Event event) {
    	if (event.widget instanceof ToolItem) {
			ToolItem toolItem= (ToolItem) event.widget;
			Control control= toolItem.getParent();
    		Menu menu= getMenu(control);
    		
    		Rectangle bounds= toolItem.getBounds();
    		Point topLeft= new Point(bounds.x, bounds.y + bounds.height);
    		menu.setLocation(control.toDisplay(topLeft));
    		menu.setVisible(true);
    	}
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.action.IMenuCreator#getMenu(org.eclipse.swt.widgets.Control)
     */
    public Menu getMenu(Control parent) {
        if (fMenu != null) {
            fMenu.dispose();
        }
        
        fMenu= new Menu(parent);
        int accel = 1;
        for (int i = 0; i < fFactoryExtensions.length; i++) {
            ConsoleFactoryExtension extension = fFactoryExtensions[i];
            if (!WorkbenchActivityHelper.filterItem(extension) && extension.isEnabled()) {
                String label = extension.getLabel();
                ImageDescriptor image = extension.getImageDescriptor();
                addActionToMenu(fMenu, new ConsoleFactoryAction(label, image, extension), accel);
                accel++;
            }
        }
        return fMenu;
    }
    
	private void addActionToMenu(Menu parent, Action action, int accelerator) {
		if (accelerator < 10) {
		    StringBuffer label= new StringBuffer();
			//add the numerical accelerator
			label.append('&');
			label.append(accelerator);
			label.append(' ');
			label.append(action.getText());
			action.setText(label.toString());
		}
		
		ActionContributionItem item= new ActionContributionItem(action);
		item.fill(parent, -1);
	}

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.IMenuCreator#getMenu(org.eclipse.swt.widgets.Menu)
     */
    public Menu getMenu(Menu parent) {
        return null;
    }
    
    private class ConsoleFactoryAction extends Action {
        
        private ConsoleFactoryExtension fConfig;
        private IConsoleFactory fFactory;

        public ConsoleFactoryAction(String label, ImageDescriptor image, ConsoleFactoryExtension extension) {
            setText(label);
            if (image != null) {
                setImageDescriptor(image);
            }
            fConfig = extension;
        }
        
        
        /* (non-Javadoc)
         * @see org.eclipse.jface.action.IAction#run()
         */
        public void run() {
            try {
                if (fFactory == null) {
                    fFactory = fConfig.createFactory();
                }
                
                fFactory.openConsole();
            } catch (CoreException e) {
                ConsolePlugin.log(e);
            }
        }
        
        /* (non-Javadoc)
         * @see org.eclipse.jface.action.IAction#runWithEvent(org.eclipse.swt.widgets.Event)
         */
        public void runWithEvent(Event event) {
            run();
        }
    }
}
