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
package org.eclipse.jface.tests.viewers.interactive;

import java.util.ArrayList;
import java.util.Random;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.tests.viewers.TestComparator;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.deferred.DeferredContentProvider;
import org.eclipse.jface.viewers.deferred.SetModel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.WorkbenchJob;

/**
 * @since 3.1
 */
public class ConcurrentTableTestView extends ViewPart {

    private TableViewer table;
    private boolean enableSlowComparisons = false;
    private TestComparator comparator = new TestComparator() {
        
        /* (non-Javadoc)
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        public int compare(Object arg0, Object arg1) {
//            try {
//                // Insert a bogus delay to simulate doing work
//                Thread.sleep(1);
//            } catch (InterruptedException e) {
//            }

        	if (enableSlowComparisons) {
	            int delay = 2; // Time to spin the CPU for (milliseconds)
	            
	            // Do some work to occupy time 
	            int counter = 0;
	            long timestamp = System.currentTimeMillis();
	            while (System.currentTimeMillis() < timestamp + delay) {
	                counter++;
	            }
        	}
            
            int result = super.compare(arg0, arg1);
            
            scheduleComparisonUpdate();
            
            return result;
        }
    };
    private DeferredContentProvider contentProvider;
    
    private WorkbenchJob updateCountRunnable = new WorkbenchJob("") {
        
        /* (non-Javadoc)
         * @see org.eclipse.ui.progress.UIJob#runInUIThread(org.eclipse.core.runtime.IProgressMonitor)
         */
        public IStatus runInUIThread(IProgressMonitor monitor) {
            updateCount.setText("Comparison count = " + comparator.comparisons);
            return Status.OK_STATUS;
        }
    };
    
    private Label updateCount;
    private SetModel model = new SetModel();
    private Random rand = new Random();
    private Button slowComparisons;
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    public void createPartControl(Composite temp) {
        Composite parent = new Composite(temp, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        
        parent.setLayout(layout);
        
        // Create the table
        {
	        table = new TableViewer(parent, SWT.VIRTUAL);
	        contentProvider = new DeferredContentProvider(comparator);
	        table.setContentProvider(contentProvider);
	        
	        GridData data = new GridData(GridData.FILL_BOTH);
	        table.getControl().setLayoutData(data);
	        table.setInput(model);
        }
        
        // Create the buttons
        Composite buttonBar = new Composite(parent, SWT.NONE);
        buttonBar.setLayoutData(new GridData(GridData.FILL_BOTH));
        GridLayout buttonBarLayout = new GridLayout();
        buttonBarLayout.numColumns = 1;
        buttonBar.setLayout(buttonBarLayout);
        {

            updateCount = new Label(buttonBar, SWT.NONE);
            updateCount.setLayoutData(new GridData(GridData.FILL_BOTH));

            slowComparisons = new Button(buttonBar, SWT.CHECK);
            slowComparisons.setLayoutData(new GridData(GridData.FILL_BOTH));
            slowComparisons.setText("Slow comparisons");
            slowComparisons.setSelection(enableSlowComparisons);
            slowComparisons.addSelectionListener(new SelectionAdapter() {
            	/* (non-Javadoc)
				 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
				 */
				public void widgetSelected(SelectionEvent e) {
					enableSlowComparisons = slowComparisons.getSelection();
					super.widgetSelected(e);
				}
            });
            
            
            final Button limitSize = new Button(buttonBar, SWT.CHECK);
            limitSize.setLayoutData(new GridData(GridData.FILL_BOTH));
            limitSize.setText("Limit table size to 400");
            limitSize.addSelectionListener(new SelectionAdapter() {
            	/* (non-Javadoc)
				 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
				 */
				public void widgetSelected(SelectionEvent e) {
					if (limitSize.getSelection()) {
						contentProvider.setLimit(400);
					} else {
						contentProvider.setLimit(-1);
					}
					super.widgetSelected(e);
				}
            });
            
            Button resetCountButton = new Button(buttonBar, SWT.PUSH);
            resetCountButton.setLayoutData(new GridData(GridData.FILL_BOTH));
            resetCountButton.setText("Reset comparison count");
	        resetCountButton.addSelectionListener(new SelectionAdapter() {
		        /* (non-Javadoc)
	             * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
	             */
	            public void widgetSelected(SelectionEvent e) {
	                comparator.comparisons = 0;
	                scheduleComparisonUpdate();
	            } 
	        });
	        
	        Button testButton = new Button(buttonBar, SWT.PUSH);
	        testButton.setLayoutData(new GridData(GridData.FILL_BOTH));
	        testButton.setText("add 100000 elements");
	        testButton.addSelectionListener(new SelectionAdapter() {
		        /* (non-Javadoc)
	             * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
	             */
	            public void widgetSelected(SelectionEvent e) {
	                addRandomElements(100000);
	            } 
	        });
	        
	        Button removeButton = new Button(buttonBar, SWT.PUSH);
	        removeButton.setLayoutData(new GridData(GridData.FILL_BOTH));
	        removeButton.setText("remove all");
	        removeButton.addSelectionListener(new SelectionAdapter() {
		        /* (non-Javadoc)
	             * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
	             */
	            public void widgetSelected(SelectionEvent e) {
	                clear();
	            } 
	        });

        }
    }
    
    
    /**
     * 
     * @since 3.1
     */
    protected void scheduleComparisonUpdate() {
        updateCountRunnable.schedule(100);
    }



    public void addRandomElements(int amount) {
        
        ArrayList tempList = new ArrayList();

        for (int counter = 0; counter < amount; counter++) {
            tempList.add("" + rand.nextLong() + " " + counter );
        }
        
        model.addAll(tempList);
    }
    
    public void clear() {
        model.clear();
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPart#setFocus()
     */
    public void setFocus() {
        // TODO Auto-generated method stub

    }
}
