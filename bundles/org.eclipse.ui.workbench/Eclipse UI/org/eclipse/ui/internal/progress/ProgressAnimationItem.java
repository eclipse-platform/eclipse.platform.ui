/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.progress;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

/**
 * Create an instance of the receiver in the window.
 * 
 * @param workbenchWindow
 *            The window this is being created in.
 */
public class ProgressAnimationItem extends AnimationItem implements FinishedJobs.KeptJobsListener {

	ProgressBar bar;
	MouseListener mouseListener;
	Composite top;
	ToolBar toolbar;
	ToolItem toolButton;
	ProgressRegion progressRegion;
    Image noneImage, okImage, errorImage;
	
	
	/**
	 * Create an instance of the receiver in the supplied region.
	 * 
	 * @param region. The ProgressRegion that contains the receiver.
	 */
	ProgressAnimationItem(ProgressRegion region) {
		super(region.workbenchWindow);
		
	    FinishedJobs.getInstance().addListener(this);
		
		progressRegion= region;
		mouseListener = new MouseAdapter() {
			/* (non-Javadoc)
			 * @see org.eclipse.swt.events.MouseAdapter#mouseDoubleClick(org.eclipse.swt.events.MouseEvent)
			 */
			public void mouseDoubleClick(MouseEvent e) {
			    progressRegion.processDoubleClick();
			}
		};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.progress.AnimationItem#createAnimationItem(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createAnimationItem(Composite parent) {
	    	    
	    if (okImage == null) {
	        Display display= parent.getDisplay();
	        noneImage= ImageDescriptor.createFromFile(getClass(), "newprogress_none.gif").createImage(display);
	        okImage= ImageDescriptor.createFromFile(getClass(), "newprogress_ok.gif").createImage(display);
	        errorImage= ImageDescriptor.createFromFile(getClass(), "newprogress_error.gif").createImage(display);
	    }
		
		top = new Composite(parent, SWT.NULL);
		top.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
        	    		FinishedJobs.getInstance().removeListener(ProgressAnimationItem.this);
                noneImage.dispose();
    	        		okImage.dispose();
    	    	        errorImage.dispose();
           }
		});
		//top.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_YELLOW));
		GridLayout gl= new GridLayout();
		gl.numColumns= 2;
		gl.marginHeight= 0;
		gl.marginWidth= 0;
		gl.horizontalSpacing= 0;
		top.setLayout(gl);
		
		toolbar= new ToolBar(top, SWT.FLAT);
		toolButton= new ToolItem(toolbar, SWT.NONE);
		toolButton.setImage(noneImage);
		toolButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                progressRegion.processDoubleClick();
        			toolButton.setImage(noneImage);
            }
        });
		
		bar = new ProgressBar(top, SWT.HORIZONTAL | SWT.INDETERMINATE);
		bar.addMouseListener(mouseListener);
		
		GridData gd= new GridData();
		gd.widthHint= 30;
		gd.verticalAlignment= GridData.CENTER;
		bar.setLayoutData(gd);
		
		return top;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.progress.AnimationItem#getControl()
	 */
	public Control getControl() {
		return top;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.progress.AnimationItem#animationDone()
	 */
	void animationDone() {
		super.animationDone();
		if (bar.isDisposed())
			return;
		bar.setVisible(false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.progress.AnimationItem#animationStart()
	 */
	void animationStart() {
		super.animationStart();
		if (bar.isDisposed())
			return;
		bar.setVisible(true);
	}

    public void removed(JobInfo info) {
    	infoVisited();
    }
    
    public void finished(JobInfo info) {
        final Job job= info.getJob();
        if (job != null) {
		    final Display display= Display.getDefault();
		    display.asyncExec(new Runnable() {
		        public void run() {
		            setStatus(job);
		        }
		    });
        }
    }
    
	void setStatus(Job job) {
	    IStatus status= job.getResult();
	    if (status != null) {
	        toolbar.getDisplay().beep();
	        if (status.getSeverity() == IStatus.ERROR)
	            toolButton.setImage(errorImage);
	        else
	            toolButton.setImage(okImage);
	    }
	}

	public void infoVisited() {
	    final Display display= Display.getDefault();
	    display.asyncExec(new Runnable() {
	        public void run() {
	        	toolButton.setImage(noneImage);
	        }
	    });
	}
}
