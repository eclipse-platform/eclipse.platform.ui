/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.api;

import junit.framework.Assert;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.internal.WorkbenchImages;

public class MockViewPart extends MockWorkbenchPart implements IViewPart {
    public static String ID = "org.eclipse.ui.tests.api.MockViewPart";

    public static String ID2 = ID + "2";

    public static String ID3 = ID + "3";

    public static String ID4 = ID + "4";

    public static String IDMULT = ID + "Mult";

    public static String NAME = "Mock View 1";

    private ContributionItem toolbarItem = new ContributionItem("someId") {

    	private DisposeListener disposeListener = new DisposeListener() {
    		public void widgetDisposed(DisposeEvent e) {
    			toolbarContributionItemWidgetDisposed();
    		}

    	};
    	
    	public void fill(ToolBar parent, int index) {
    		super.fill(parent, index);
    		
    		ToolItem item = new ToolItem(parent, index);
    		
    		item.addDisposeListener(disposeListener);
    		item.setImage(WorkbenchImages.getImage(ISharedImages.IMG_DEF_VIEW));
    	}
    	
    	public void dispose() {
    		toolbarContributionItemDisposed();
    		super.dispose();
    	}
    };
    
    private class DummyAction extends Action {
    	public DummyAction() {
    		setText("Monkey");
			setImageDescriptor(getViewSite().getWorkbenchWindow()
					.getWorkbench().getSharedImages()
					.getImageDescriptor(
							ISharedImages.IMG_TOOL_DELETE));
		}    	
    }
    
    public MockViewPart() {
        super();
    }

	/**
     * @see IViewPart#getViewSite()
     */
    public IViewSite getViewSite() {
        return (IViewSite) getSite();
    }

    /**
     * @see IViewPart#init(IViewSite)
     */
    public void init(IViewSite site) throws PartInitException {
        setSite(site);
        callTrace.add("init");
        setSiteInitialized();
        addToolbarContributionItem();
    }

    /**
     * @see IViewPart#init(IViewSite, IMemento)
     */
    public void init(IViewSite site, IMemento memento) throws PartInitException {
        setSite(site);
        callTrace.add("init");
        setSiteInitialized();
        addToolbarContributionItem();
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.tests.api.MockWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    public void createPartControl(Composite parent) {
    	super.createPartControl(parent);
    	
        Button addAction = new Button(parent, SWT.PUSH);
        addAction.setText("Add Action to Tool Bar");
        addAction.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				IActionBars bars = getViewSite().getActionBars();
				bars.getToolBarManager().add(new DummyAction());
				bars.updateActionBars();
			}
		});

        Button removeAction = new Button(parent, SWT.PUSH);
        removeAction.setText("Remove Action from Tool Bar");
        removeAction.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				IActionBars bars = getViewSite().getActionBars();
				IToolBarManager tbm = bars.getToolBarManager();
				IContributionItem[] items = tbm.getItems();
				if (items.length > 0) {
					IContributionItem item = items[items.length-1];
					if (item instanceof ActionContributionItem) {
						if (((ActionContributionItem) item).getAction() instanceof DummyAction) {
							tbm.remove(item);
							bars.updateActionBars();
						}
					}
				}
			}
		});
    }
    
    /* (non-Javadoc)
	 * @see org.eclipse.ui.tests.api.MockPart#dispose()
	 */
	public void dispose() {
		// Test for bug 94457: The contribution items must still be in the toolbar manager at the
		// time the part is disposed. (Changing this behavior would be a breaking change for some
		// clients).
		Assert.assertTrue( 
				"Contribution items should not be removed from the site until after the part is disposed", 
				getViewSite().getActionBars().getToolBarManager().find(toolbarItem.getId())
				== toolbarItem );
		super.dispose();
	}

	private void addToolbarContributionItem() {
    	getViewSite().getActionBars().getToolBarManager().add(toolbarItem);
    }

	public void toolbarContributionItemWidgetDisposed() {
		callTrace.add("toolbarContributionItemWidgetDisposed");
	}
    
    public void toolbarContributionItemDisposed() {
    	callTrace.add("toolbarContributionItemDisposed");
	}
	
    /**
     * @see IViewPart#saveState(IMemento)
     */
    public void saveState(IMemento memento) {
    	// do nothing
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.tests.api.MockWorkbenchPart#getActionBars()
     */
    protected IActionBars getActionBars() {
        return getViewSite().getActionBars();
    }
}

