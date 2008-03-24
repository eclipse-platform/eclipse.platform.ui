/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.wizard;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IProgressMonitorWithBlocking;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.ProgressIndicator;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Listener;

/**
 * A standard implementation of an IProgressMonitor. It consists
 * of a label displaying the task and subtask name, and a
 * progress indicator to show progress. In contrast to
 * <code>ProgressMonitorDialog</code> this class only implements
 * <code>IProgressMonitor</code>.
 */
public class ProgressMonitorPart extends Composite implements
        IProgressMonitorWithBlocking {

	/** the label */
    protected Label fLabel;

    /** the current task name */
    protected String fTaskName;

    /** the current sub task name */
    protected String fSubTaskName;

    /** the progress indicator */
    protected ProgressIndicator fProgressIndicator;

    /** the cancel component */
    protected Control fCancelComponent;

    /** true if canceled */
    protected boolean fIsCanceled;

    /** current blocked status */
    protected IStatus blockedStatus;

    /** the cancel lister attached to the cancel component */
    protected Listener fCancelListener = new Listener() {
        public void handleEvent(Event e) {
            setCanceled(true);
            if (fCancelComponent != null) {
				fCancelComponent.setEnabled(false);
			}
        }
    };

    /**
     * Creates a ProgressMonitorPart.
     * @param parent The SWT parent of the part.
     * @param layout The SWT grid bag layout used by the part. A client
     * can supply the layout to control how the progress monitor part
     * is layed out. If null is passed the part uses its default layout.
     */
    public ProgressMonitorPart(Composite parent, Layout layout) {
        this(parent, layout, SWT.DEFAULT);
    }

    /**
     * Creates a ProgressMonitorPart.
     * @param parent The SWT parent of the part.
     * @param layout The SWT grid bag layout used by the part. A client
     * can supply the layout to control how the progress monitor part
     * is layed out. If null is passed the part uses its default layout.
     * @param progressIndicatorHeight The height of the progress indicator in pixel.
     */
    public ProgressMonitorPart(Composite parent, Layout layout,
            int progressIndicatorHeight) {
        super(parent, SWT.NONE);
        initialize(layout, progressIndicatorHeight);
    }

    /**
     * Attaches the progress monitor part to the given cancel
     * component.
     * @param cancelComponent the control whose selection will
     * trigger a cancel
     */
    public void attachToCancelComponent(Control cancelComponent) {
        Assert.isNotNull(cancelComponent);
        fCancelComponent = cancelComponent;
        fCancelComponent.addListener(SWT.Selection, fCancelListener);
    }

    /**
     * Implements <code>IProgressMonitor.beginTask</code>.
     * @see IProgressMonitor#beginTask(java.lang.String, int)
     */
    public void beginTask(String name, int totalWork) {
        fTaskName = name;
        updateLabel();
        if (totalWork == IProgressMonitor.UNKNOWN || totalWork == 0) {
            fProgressIndicator.beginAnimatedTask();
        } else {
            fProgressIndicator.beginTask(totalWork);
        }
    }

    /**
     * Implements <code>IProgressMonitor.done</code>.
     * @see IProgressMonitor#done()
     */
    public void done() {
        fLabel.setText("");//$NON-NLS-1$
        fProgressIndicator.sendRemainingWork();
        fProgressIndicator.done();
    }

    /**
     * Escapes any occurrence of '&' in the given String so that
     * it is not considered as a mnemonic
     * character in SWT ToolItems, MenuItems, Button and Labels.
     * @param in the original String
     * @return The converted String
     */
    protected static String escapeMetaCharacters(String in) {
        if (in == null || in.indexOf('&') < 0) {
			return in;
		}
        int length = in.length();
        StringBuffer out = new StringBuffer(length + 1);
        for (int i = 0; i < length; i++) {
            char c = in.charAt(i);
            if (c == '&') {
				out.append("&&");//$NON-NLS-1$
			} else {
				out.append(c);
			}
        }
        return out.toString();
    }

    /**
     * Creates the progress monitor's UI parts and layouts them
     * according to the given layout. If the layout is <code>null</code>
     * the part's default layout is used.
     * @param layout The layout for the receiver.
     * @param progressIndicatorHeight The suggested height of the indicator
     */
    protected void initialize(Layout layout, int progressIndicatorHeight) {
        if (layout == null) {
            GridLayout l = new GridLayout();
            l.marginWidth = 0;
            l.marginHeight = 0;
            l.numColumns = 1;
            layout = l;
        }
        setLayout(layout);

        fLabel = new Label(this, SWT.LEFT);
        fLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        if (progressIndicatorHeight == SWT.DEFAULT) {
            GC gc = new GC(fLabel);
            FontMetrics fm = gc.getFontMetrics();
            gc.dispose();
            progressIndicatorHeight = fm.getHeight();
        }

        fProgressIndicator = new ProgressIndicator(this);
        GridData gd = new GridData();
        gd.horizontalAlignment = GridData.FILL;
        gd.grabExcessHorizontalSpace = true;
        gd.verticalAlignment = GridData.CENTER;
        gd.heightHint = progressIndicatorHeight;
        fProgressIndicator.setLayoutData(gd);
    }

    /**
     * Implements <code>IProgressMonitor.internalWorked</code>.
     * @see IProgressMonitor#internalWorked(double)
     */
    public void internalWorked(double work) {
        fProgressIndicator.worked(work);
    }

    /**
     * Implements <code>IProgressMonitor.isCanceled</code>.
     * @see IProgressMonitor#isCanceled()
     */
    public boolean isCanceled() {
        return fIsCanceled;
    }

    /**
     * Detach the progress monitor part from the given cancel
     * component
     * @param cc
     */
    public void removeFromCancelComponent(Control cc) {
        Assert.isTrue(fCancelComponent == cc && fCancelComponent != null);
        fCancelComponent.removeListener(SWT.Selection, fCancelListener);
        fCancelComponent = null;
    }

    /**
     * Implements <code>IProgressMonitor.setCanceled</code>.
     * @see IProgressMonitor#setCanceled(boolean)
     */
    public void setCanceled(boolean b) {
        fIsCanceled = b;
    }

    /**
     * Sets the progress monitor part's font.
     */
    public void setFont(Font font) {
        super.setFont(font);
        fLabel.setFont(font);
        fProgressIndicator.setFont(font);
    }

    /*
     *  (non-Javadoc)
     * @see org.eclipse.core.runtime.IProgressMonitor#setTaskName(java.lang.String)
     */
    public void setTaskName(String name) {
        fTaskName = name;
        updateLabel();
    }

    /*
     *  (non-Javadoc)
     * @see org.eclipse.core.runtime.IProgressMonitor#subTask(java.lang.String)
     */
    public void subTask(String name) {
        fSubTaskName = name;
        updateLabel();
    }

    /**
     * Updates the label with the current task and subtask names.
     */
    protected void updateLabel() {
        if (blockedStatus == null) {
            String text = taskLabel();
            fLabel.setText(text);
        } else {
			fLabel.setText(blockedStatus.getMessage());
		}

        //Force an update as we are in the UI Thread
        fLabel.update();
    }

    /**
     * Return the label for showing tasks
     * @return String
     */
    private String taskLabel() {
    	boolean hasTask= fTaskName != null && fTaskName.length() > 0;
    	boolean hasSubtask= fSubTaskName != null && fSubTaskName.length() > 0;
    	
		if (hasTask) {
			if (hasSubtask)
				return escapeMetaCharacters(JFaceResources.format(
    					"Set_SubTask", new Object[] { fTaskName, fSubTaskName }));//$NON-NLS-1$
   			return escapeMetaCharacters(fTaskName);
   			
    	} else if (hasSubtask) {
    		return escapeMetaCharacters(fSubTaskName);
    	
    	} else {
    		return ""; //$NON-NLS-1$
    	}
    }

    /**
     * Implements <code>IProgressMonitor.worked</code>.
     * @see IProgressMonitor#worked(int)
     */
    public void worked(int work) {
        internalWorked(work);
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IProgressMonitorWithBlocking#clearBlocked()
     */
    public void clearBlocked() {
        blockedStatus = null;
        updateLabel();

    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IProgressMonitorWithBlocking#setBlocked(org.eclipse.core.runtime.IStatus)
     */
    public void setBlocked(IStatus reason) {
        blockedStatus = reason;
        updateLabel();

    }
}
