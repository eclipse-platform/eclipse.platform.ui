/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.progress;

import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TreeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.internal.misc.Assert;
import org.eclipse.ui.progress.IProgressConstants;

/**
 * The NewProgressViewer is the viewer for progress using
 * progress monitors.
 */
public class NewProgressViewer extends TreeViewer implements
        FinishedJobs.KeptJobsListener {

    /*
     * Label with hyperlink capability.
     */
    class Hyperlink extends JobTreeItem implements Listener,
            IPropertyChangeListener {

        final static int MARGINHEIGHT = 1;
        final static int MARGINWIDTH = 1;

        boolean foundImage;

        IAction gotoAction;

        boolean hasFocus;

        boolean isError;

        JobItem jobitem;

        boolean linkEnabled;

        boolean mouseOver;

        IStatus result;

        String text = ""; //$NON-NLS-1$

        Hyperlink(JobItem parent, JobTreeElement info) {
            super(parent, info, SWT.NO_BACKGROUND);

            jobitem = parent;

            setFont(smallerFont);

            addListener(SWT.KeyDown, this);
            addListener(SWT.Paint, this);
            addListener(SWT.MouseEnter, this);
            addListener(SWT.MouseExit, this);
            addListener(SWT.MouseDown, this);
            addListener(SWT.MouseUp, this);
            addListener(SWT.FocusIn, this);
            addListener(SWT.FocusOut, this);

            refresh();
        }

        public Point computeSize(int wHint, int hHint, boolean changed) {
            checkWidget();
            int innerWidth = wHint;
            if (innerWidth != SWT.DEFAULT)
                innerWidth -= MARGINWIDTH * 2;
            GC gc = new GC(this);
            gc.setFont(getFont());
            Point extent = gc.textExtent(text);
            gc.dispose();
            return new Point(extent.x + 2 * MARGINWIDTH, extent.y + 2
                    * MARGINHEIGHT);
        }

        private Color getFGColor() {
            if (jobitem.selected)
                return selectedTextColor;

            if (isLinkEnabled()) {
            	
            	 if (isError)
                    return getErrorColor();
            	 
                if (mouseOver)                    
                    return getActiveHyperlinkColor();
                return getHyperlinkColor();
            }
            return textColor;
        }

        protected boolean handleActivate() {
            if (isLinkEnabled() && gotoAction != null && gotoAction.isEnabled()) {
                jobitem.locked = true;
                gotoAction.run();
                if (jobitem.jobTerminated)
                    jobitem.kill(jobTreeElement);
                return true;
            }
            return false;
        }

        public void handleEvent(Event e) {
            super.handleEvent(e);
            switch (e.type) {
            case SWT.Dispose:
                if (gotoAction != null) {
                    gotoAction.removePropertyChangeListener(this);
                    gotoAction = null;
                }
                break;
            case SWT.KeyDown:
                if (e.character == '\r')
                    handleActivate();
                else if (e.keyCode == SWT.DEL) {
                    cancelSelection();
                } else {
                    select(null, e);
                }
                break;
            case SWT.Paint:
                paint(e.gc);
                break;
            case SWT.FocusIn:
                hasFocus = true;
            case SWT.MouseEnter:
                if (isLinkEnabled()) {
                    mouseOver = true;
                    redraw();
                }
                break;
            case SWT.FocusOut:
                hasFocus = false;
            case SWT.MouseExit:
                if (isLinkEnabled()) {
                    mouseOver = false;
                    redraw();
                }
                break;
            case SWT.DefaultSelection:
                handleActivate();
                break;
            case SWT.MouseDown:
                if (!isLinkEnabled())
                    select((JobItem) getParent(), e);
                break;
            case SWT.MouseUp:
                if (isLinkEnabled()) {
                    Point size = getSize();
                    if (e.button != 1 || e.x < 0 || e.y < 0 || e.x >= size.x
                            || e.y >= size.y)
                        return;
                    handleActivate();
                }
                break;
            }
        }

        boolean isLinkEnabled() {
            return !dialogContext && linkEnabled;
        }

        protected void paint(GC gc) {
            Rectangle clientArea = getClientArea();
            if (clientArea.isEmpty())
                return;
            Color fg = getFGColor(), bg = getBackground();
            if (jobitem.selected)
                bg = selectedColor;
            Image buffer = null;
            GC bufferGC = gc;
            if (!isCarbon) {
                buffer = new Image(getDisplay(), clientArea.width,
                        clientArea.height);
                buffer.setBackground(bg);
                bufferGC = new GC(buffer, gc.getStyle());
            }
            bufferGC.setForeground(fg);
            bufferGC.setBackground(bg);
            bufferGC.fillRectangle(0, 0, clientArea.width, clientArea.height);
            bufferGC.setFont(getFont());
            String t = shortenText(bufferGC, clientArea.width, text);
            bufferGC.drawText(t, MARGINWIDTH, MARGINHEIGHT, true);
            int sw = bufferGC.stringExtent(t).x;
            if (isLinkEnabled()) {
                FontMetrics fm = bufferGC.getFontMetrics();
                int lineY = clientArea.height - MARGINHEIGHT - fm.getDescent()
                        + 1;
                bufferGC.drawLine(MARGINWIDTH, lineY, MARGINWIDTH + sw, lineY);
                if (hasFocus)
                    bufferGC.drawFocus(0, 0, sw, clientArea.height);
            }
            if (buffer != null) {
                gc.drawImage(buffer, 0, 0);
                bufferGC.dispose();
                buffer.dispose();
            }
        }

        public void propertyChange(PropertyChangeEvent event) {
            if (gotoAction != null) {
                getDisplay().asyncExec(new Runnable() {
                    public void run() {
                        if (!isDisposed()) {
                            checkKeep();
                            setLinkEnable(gotoAction != null
                                    && gotoAction.isEnabled());
                        }
                    }
                });
            }
        }

        public boolean refresh() {

            if (jobTreeElement == null) // shouldn't happen
                return false;

            final Job job = getJob();
            if (job != null) {
                // check for icon property and propagate to parent
                if (jobitem.image == null)
                    jobitem.updateIcon(job);

                // check for action property
                Object property = job
                        .getProperty(IProgressConstants.ACTION_PROPERTY);
                if (property instanceof IAction)
                    setAction((IAction) property);

                // poll for result status
                IStatus status = job.getResult();
                if (status != null && status != result) {
                    result = status;
                    if (result.getSeverity() == IStatus.ERROR) {
                        setKeep();
                        isError = true;
                        setAction(new Action() {
                            /*
                             * Get the notificationManager that this is being created for.
                             */
                            private ErrorNotificationManager getManager() {
                                return ProgressManager.getInstance().errorManager;
                            }
                            public void run() {
                                String title = ProgressMessages.NewProgressView_errorDialogTitle; 
	                            String msg = ProgressMessages.NewProgressView_errorDialogMessage; 
                                if (!getManager().showErrorFor(job, title, msg)) {
                                    // The error is missing from the error manager.
                                    // This should only occur if what the progress view is showing is
                                    // out-of-sync with the ErrorNotificationManager and/or FinishedJobs
                                    // manager. In other words, it shouldn't happen but may so it is
                                    // better to show the user something than fail silently
		                            ErrorDialog.openError(getShell(), title, msg,
		                                    result);
                                }
                            }
                        });
                    }
                }
            }
            checkKeep();

            // now build the string for displaying
            String name = null;

            if (jobTreeElement instanceof SubTaskInfo) { // simple job case

                SubTaskInfo sti = (SubTaskInfo) jobTreeElement;
                String taskName = null;
                if (sti.jobInfo != null) {
                    TaskInfo ti = sti.jobInfo.getTaskInfo();
                    if (ti != null)
                        taskName = ti.getTaskName();
                }

                if (jobTerminated && result != null) {
                    name = result.getMessage();
                    if (taskName != null && taskName.trim().length() > 0)
                        name = NLS.bind(ProgressMessages.JobInfo_TaskFormat, taskName, name); 
                } else {
                    name = jobTreeElement.getDisplayString();
                    if (taskName != null && taskName.trim().length() > 0)
                        name = NLS.bind(ProgressMessages.JobInfo_TaskFormat2, taskName, name); 
                }

                if (name.length() == 0) {
                    dispose();
                    return true;
                }

            } else if (jobTreeElement instanceof JobInfo) { // group case
                JobInfo ji = (JobInfo) jobTreeElement;

                if (/*jobTerminated && */result != null) {
                    name = result.getMessage();
                    if (name == null || name.trim().length() == 0
                            || "OK".equals(name)) { //$NON-NLS-1$
                        if (!keepItem) {
                            dispose();
                            return true;
                        }
                    }
                } else {
                    // get the Job name
                    name = getJobNameAndStatus(ji, job, jobTerminated, false);

                    // get percentage and task name
                    String taskName = null;
                    TaskInfo info = ji.getTaskInfo();
                    if (info != null) {
                        taskName = info.getTaskName();
                        int percent = info.getPercentDone();
                        if (percent >= 0 && percent <= 100) {
                            if (taskName != null)
                                taskName = NLS.bind(ProgressMessages.JobInfo_Percent,Integer.toString(percent),taskName );
                            else
                                taskName = NLS.bind(ProgressMessages.JobInfo_Percent2, Integer.toString(percent) );
                        }
                    }

                    // get sub task name
                    String subTaskName = null;
                    Object[] subtasks = ji.getChildren();
                    if (subtasks != null && subtasks.length > 0) {
                        JobTreeElement sub = (JobTreeElement) subtasks[0];
                        if (sub != null)
                            subTaskName = sub.getDisplayString();
                    }

                    boolean hasTask = taskName != null
                            && taskName.trim().length() > 0;
                    boolean hasSubTask = subTaskName != null
                            && subTaskName.trim().length() > 0;

                    if (hasTask && hasSubTask) {
                        name = NLS.bind(ProgressMessages.JobInfo_Format, (new Object[] { name, taskName, subTaskName })); 
                    } else if (hasTask) {
                        name = NLS.bind(ProgressMessages.JobInfo_TaskFormat, name, taskName ); 
                    } else if (hasSubTask) {
                        name = NLS.bind(ProgressMessages.JobInfo_TaskFormat, name, subTaskName); 
                    }
                }

                if (highlightJob == job)
                    highlightItem = jobitem;
            }

            if (name == null)
                name = jobTreeElement.getDisplayString();

            setText(name);

            return false;
        }

        private void setAction(IAction action) {
            if (action == gotoAction)
                return;
            if (gotoAction != null)
                gotoAction.removePropertyChangeListener(this);
            gotoAction = action;
            if (gotoAction != null) {

                // temporary workaround for CVS actionWrapper problem
                String actionName = gotoAction.getClass().getName();
                if (actionName.indexOf("RefreshSubscriberJob$2") >= 0) //$NON-NLS-1$
                    gotoAction.setEnabled(false);
                // end of temporary workaround

                gotoAction.addPropertyChangeListener(this);
            }
            updateToolTip();
            setLinkEnable(action != null && action.isEnabled());
        }

        private void setLinkEnable(boolean enable) {
            if (enable != linkEnabled) {
                linkEnabled = enable;
                if (isLinkEnabled())
                    setCursor(handCursor);
                updateToolTip();
                redraw();
            }
        }

        private void setText(String t) {
            if (t == null)
                t = ""; //$NON-NLS-1$
            if (!t.equals(text)) {
                text = t;
                updateToolTip();
                redraw();
            }
        }

        private void updateToolTip() {
            String tt = text;
            if (isLinkEnabled() && gotoAction != null && gotoAction.isEnabled()) {
                String tooltip = gotoAction.getToolTipText();
                if (tooltip != null && tooltip.trim().length() > 0)
                    tt = tooltip;
            }
            String oldtt = getToolTipText();
            if (oldtt == null || !oldtt.equals(tt))
                setToolTipText(tt);
        }
    }

    /*
     * An SWT widget representing a JobModel
     */
    class JobItem extends JobTreeItem {

        static final int HGAP = 7;

        static final int MARGIN = 2;

        static final int MAX_PROGRESS_HEIGHT = 12;

        static final int MIN_ICON_SIZE = 16;

        static final int VGAP = 1;

        ToolBar actionBar;

        ToolItem actionButton;

        int cachedHeight = -1;

        int cachedWidth = -1;

        private boolean disposeImage;

        ToolItem gotoButton;

        Label iconItem;

        private Image image;

        private boolean locked; // don't add children

        Label nameItem;

        ProgressBar progressBar;

        boolean selected;

        JobItem(Composite parent, JobTreeElement info) {
            super(parent, info, SWT.NONE);

            Assert.isNotNull(info);

            Display display = getDisplay();

            iconItem = new Label(this, SWT.NONE);
            iconItem.addListener(SWT.MouseDown, this);
            updateIcon(getJob());
            if (image == null)
                iconItem.setImage(defaultJobIcon);

            nameItem = new Label(this, SWT.NONE);
            nameItem.setFont(boldFont);
            nameItem.addListener(SWT.MouseDown, this);

            actionBar = new ToolBar(this, SWT.FLAT);
            actionBar.setCursor(normalCursor); // set cursor to overwrite any busy cursor we might have
            actionButton = new ToolItem(actionBar, SWT.NONE);
            actionButton.setImage(cancelJobIcon);
            actionButton.setDisabledImage(cancelJobDIcon);
            actionButton.setToolTipText(ProgressMessages.NewProgressView_CancelJobToolTip); 
            actionButton.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e) {
                    actionButton.setEnabled(false);
                    cancelOrRemove();
                }
            });

            addListener(SWT.MouseDown, this);
            addListener(SWT.KeyDown, this);

            addControlListener(new ControlAdapter() {
                public void controlResized(ControlEvent e) {
                    handleResize();
                }
            });

            refresh();
        }

        /**
         * Kill selected element if it is the receiver rather than
         * a child of it.
         * @param selectedElement
         */
        public void kill(JobTreeElement selectedElement) {
        	//Only kill the receiver if it is the one we are showing
			if(jobTreeElement.equals(selectedElement))
				kill();
		}

		private boolean aboutToKeep() {
            boolean changed = false;

            // finish progress reporting
            if (progressBar != null && !progressBar.isDisposed()) {
                progressBar.setSelection(100);
                progressBar.dispose();
                changed = true;
            }

            // turn cancel button in remove button
            if (!actionButton.isDisposed()) {
                actionButton.setImage(clearJobIcon);
                actionButton.setDisabledImage(clearJobDIcon);
                actionButton.setToolTipText(ProgressMessages.NewProgressView_RemoveJobToolTip); 
                actionButton.setEnabled(true);
                changed = true;
            }

            changed |= refresh();

            Control[] c = getChildren();
            for (int i = 0; i < c.length; i++) {
                if (c[i] instanceof JobTreeItem)
                    changed |= ((JobTreeItem) c[i]).refresh();
            }

            return changed;
        }

        boolean cancelOrRemove() {
            if (jobTerminated)
                return kill();
            jobTreeElement.cancel();
            return false;
        }

        public Point computeSize(int wHint, int hHint, boolean changed) {

            if (changed || cachedHeight <= 0 || cachedWidth <= 0) {
                Point e1 = iconItem.computeSize(SWT.DEFAULT, SWT.DEFAULT);
                e1.x = MIN_ICON_SIZE;
                Point e2 = nameItem.computeSize(SWT.DEFAULT, SWT.DEFAULT);

                cachedWidth = MARGIN + e1.x + HGAP + 100 + MARGIN;

                cachedHeight = MARGIN + Math.max(e1.y, e2.y);
                if (progressBar != null && !progressBar.isDisposed()) {
                    cachedHeight += 1;
                    Point e3 = progressBar
                            .computeSize(SWT.DEFAULT, SWT.DEFAULT);
                    e3.y = MAX_PROGRESS_HEIGHT;
                    cachedHeight += VGAP + e3.y;
                }
                Control[] cs = getChildren();
                for (int i = 0; i < cs.length; i++) {
                    if (cs[i] instanceof Hyperlink) {
                        Point e4 = cs[i].computeSize(SWT.DEFAULT, SWT.DEFAULT);
                        cachedHeight += VGAP + e4.y;
                    }
                }
                cachedHeight += MARGIN;
            }

            int w = wHint == SWT.DEFAULT ? cachedWidth : wHint;
            int h = hHint == SWT.DEFAULT ? cachedHeight : hHint;

            return new Point(w, h);
        }

        void createChild(JobTreeElement jte) {
            if (!locked)
                new Hyperlink(this, jte);
        }

        private String getGroupHeader(GroupInfo gi) {
            String name = stripPercent(jobTreeElement.getDisplayString());
            if (jobTerminated)
                return getFinishedString(gi, name, true);
            return name;
        }

        IStatus getResult() {
            //checkKeep();
            if (jobTerminated) {
                Job job = getJob();
                if (job != null)
                    return job.getResult();
            }
            return null;
        }

        public void handleEvent(Event event) {
            switch (event.type) {
            case SWT.Dispose:
                super.handleEvent(event);
                if (disposeImage && image != null && !image.isDisposed()) {
                    if (DEBUG)
                        System.err.println("JobItem.image disposed"); //$NON-NLS-1$
                    image.dispose();
                }
                image = null;
                break;
            case SWT.KeyDown:
                if (event.character == '\r') {
                    doSelection();
                } else if (event.character == '\t') {
                    scroller.getParent().forceFocus();
                } else if (event.keyCode == SWT.DEL) {
                    cancelSelection();
                } else {
                    select(null, event);
                }
                break;
            case SWT.MouseDown:
                forceFocus();
                select(JobItem.this, event);
                break;
            default:
                super.handleEvent(event);
                break;
            }
        }

        void handleResize() {
            Point e = getSize();
            Point e1 = iconItem.computeSize(SWT.DEFAULT, SWT.DEFAULT);
            e1.x = MIN_ICON_SIZE;
            Point e2 = nameItem.computeSize(SWT.DEFAULT, SWT.DEFAULT);
            Point e5 = actionBar.computeSize(SWT.DEFAULT, SWT.DEFAULT);

            int iw = e.x - MARGIN - HGAP - e5.x - MARGIN;
            int indent = 16 + HGAP;

            int y = MARGIN;
            int h = Math.max(e1.y, e2.y);

            nameItem.setBounds(MARGIN + e1.x + HGAP, y + (h - e2.y) / 2, iw
                    - e1.x - HGAP, e2.y);
            y += h;
            if (progressBar != null && !progressBar.isDisposed()) {
                Point e3 = progressBar.computeSize(SWT.DEFAULT, SWT.DEFAULT);
                e3.y = MAX_PROGRESS_HEIGHT;
                y += VGAP + 1;
                progressBar.setBounds(MARGIN + indent, y, iw - indent, e3.y);
                y += e3.y;
            }
            Control[] cs = getChildren();
            for (int i = 0; i < cs.length; i++) {
                if (cs[i] instanceof Hyperlink) {
                    Point e4 = cs[i].computeSize(SWT.DEFAULT, SWT.DEFAULT);
                    y += VGAP;
                    cs[i].setBounds(MARGIN + indent, y, iw - indent, e4.y);
                    y += e4.y;
                }
            }

            int hm = (MARGIN + HGAP) / 2;
            int vm = (y - e1.y) / 2;
            if (hm < (y - e1.y) / 2)
                vm = hm;
            iconItem.setBounds(hm, vm, e1.x, e1.y);

            actionBar.setBounds(e.x - MARGIN - e5.x, (e.y - e5.y) / 2, e5.x,
                    e5.y);
        }

        boolean isCanceled() {
            if (jobTreeElement instanceof JobInfo)
                return ((JobInfo) jobTreeElement).isCanceled();
            return false;
        }

        public boolean kill() {
            if (jobTerminated) {
                // Removing the job from the list of jobs will 
                // remove the job from the view using a callback
                if (!finishedJobs.remove(jobTreeElement)) {
                    // The terminated job has already been removed
                    // from the list of finished jobs but was somehow
                    // left in the view. Dispose of the item and refresh 
                    // the view
                    dispose();
                    relayout(true, true);
                    return true;
                }
            }
            return false;
        }

        /*
         * Update the widgets from the model.
         */
        public boolean refresh() {

            if (isDisposed())
                return false;

            boolean changed = false;
            boolean isGroup = jobTreeElement instanceof GroupInfo;
            Object[] roots = contentProviderGetChildren(jobTreeElement);
            Job job = getJob();

            // poll for properties
            checkKeep();
            if (image == null && job != null)
                updateIcon(job);

            // name
            String name = null;
            if (isGroup) {
                name = getGroupHeader((GroupInfo) jobTreeElement);
            } else if (jobTreeElement instanceof JobInfo)
                name = getJobNameAndStatus((JobInfo) jobTreeElement, job,
                        jobTerminated, true);
            if (name == null)
                name = stripPercent(jobTreeElement.getDisplayString());

            if (highlightJob != null
                    && (highlightJob == job || highlightItem == this))
                name = NLS.bind(ProgressMessages.JobInfo_BlocksUserOperationFormat, name); 

            nameItem.setToolTipText(name);
            nameItem.setText(shortenText(nameItem, name));

            // percentage
            if (jobTreeElement instanceof JobInfo) {
                TaskInfo ti = ((JobInfo) jobTreeElement).getTaskInfo();
                if (ti != null)
                    changed |= setPercentDone(ti.getPercentDone());
            } else if (isGroup) {
                if (roots.length == 1 && roots[0] instanceof JobTreeElement) {
                    TaskInfo ti = ((JobInfo) roots[0]).getTaskInfo();
                    if (ti != null)
                        changed |= setPercentDone(ti.getPercentDone());
                } else {
                    GroupInfo gi = (GroupInfo) jobTreeElement;
                    changed |= setPercentDone(gi.getPercentDone());
                }
            }

            // children
            if (!jobTreeElement.hasChildren())
                return changed;

            Control[] children = getChildren();
            int n = 0;
            for (int i = 0; i < children.length; i++)
                if (children[i] instanceof Hyperlink)
                    n++;

            if (!isGroup && roots.length == n) { // reuse all children
                int z = 0;
                for (int i = 0; i < children.length; i++) {
                    if (children[i] instanceof Hyperlink) {
                        Hyperlink l = (Hyperlink) children[i];
                        l.init((JobTreeElement) roots[z++]);
                    }
                }
            } else {
                HashSet modelJobs = new HashSet();
                for (int z = 0; z < roots.length; z++)
                    modelJobs.add(roots[z]);

                // find all removed
                HashSet shownJobs = new HashSet();
                for (int i = 0; i < children.length; i++) {
                    if (children[i] instanceof Hyperlink) {
                        JobTreeItem ji = (JobTreeItem) children[i];
                        shownJobs.add(ji.jobTreeElement);
                        if (modelJobs.contains(ji.jobTreeElement)) {
                            ji.refresh();
                        } else {
                            changed |= ji.remove();
                        }
                    }
                }

                // find all added
                for (int i = 0; i < roots.length; i++) {
                    Object element = roots[i];
                    if (!shownJobs.contains(element)) {
                        createChild((JobTreeElement) element);
                        changed = true;
                    }
                }
            }

            return changed;
        }

        public boolean remove() {

            jobTerminated = true;

            if (finishedJobs.isFinished(jobTreeElement)) {
                keepItem = true;
            }

            // propagate status down
            Control[] children = getChildren();
            for (int i = 0; i < children.length; i++) {
                if (children[i] instanceof JobTreeItem)
                    ((JobTreeItem) children[i]).jobTerminated = true;
            }

            if (!dialogContext) { // we never keep jobs in dialogs
                if (!keepItem)
                    checkKeep();

                if (keepItem)
                    return aboutToKeep();
            }
            return super.remove();
        }

        boolean setPercentDone(int percentDone) {
            if (percentDone >= 0 && percentDone < 100) {
                if (progressBar == null) {
                    progressBar = new ProgressBar(this, SWT.HORIZONTAL);
                    progressBar.setMaximum(100);
                    progressBar.setSelection(percentDone);
                    progressBar.addListener(SWT.MouseDown, this);
                    return true;
                } else if (!progressBar.isDisposed())
                    progressBar.setSelection(percentDone);
            } else {
                if (progressBar == null) {
                    progressBar = new ProgressBar(this, SWT.HORIZONTAL
                            | SWT.INDETERMINATE);
                    progressBar.addListener(SWT.MouseDown, this);
                    return true;
                }
            }
            return false;
        }

        /*
         * Update the background colors.
         */
        void updateBackground(boolean dark) {
            Color fg, bg;

            if (selected) {
                fg = selectedTextColor;
                bg = selectedColor;
            } else {
                if (highlightJob != null
                        && (highlightJob == getJob() || highlightItem == this))
                    fg = getHighlightColor();
                else
                    fg = textColor;
                bg = dark ? darkColor : lightColor;
            }
            setForeground(fg);
            setBackground(bg);

            Control[] cs = getChildren();
            for (int i = 0; i < cs.length; i++) {
                if (!(cs[i] instanceof ProgressBar)) {
                    cs[i].setForeground(fg);
                    cs[i].setBackground(bg);
                }
            }
        }

        void updateIcon(Job job) {
            if (job != null) {
                Image im = null;
                boolean dispImage = false;
                Display display = getDisplay();
                Object property = job.getProperty(ICON_PROPERTY);
                if (property instanceof ImageDescriptor) {
                    dispImage = true;
                    im = ((ImageDescriptor) property).createImage(display);
                } else if (property instanceof URL) {
                    dispImage = true;
                    im = ImageDescriptor.createFromURL((URL) property)
                            .createImage(display);
                } else {
                    dispImage = false;
                    im = ProgressManager.getInstance().getIconFor(job);
                }
                if (im != null && im != image) {
                    if (disposeImage && image != null) {
                        if (DEBUG)
                            System.err
                                    .println("JobItem.setImage: disposed image"); //$NON-NLS-1$
                        image.dispose();
                    }
                    image = im;
                    disposeImage = dispImage;
                    if (iconItem != null)
                        iconItem.setImage(image);
                }
            }
        }
    }

    abstract class JobTreeItem extends Canvas implements Listener {

        boolean jobTerminated;
        JobTreeElement jobTreeElement;

        boolean keepItem;

        JobTreeItem(Composite parent, JobTreeElement info, int flags) {
            super(parent, flags);
            jobTreeElement = info;
            map.put(jobTreeElement, this);
            addListener(SWT.Dispose, this);
        }

        boolean checkKeep() {
            if (jobTreeElement instanceof JobInfo
                    && FinishedJobs.keep((JobInfo) jobTreeElement))
                setKeep();
            return keepItem;
        }

        protected void dump(String message) {
            if (DEBUG) {
                System.out.println(message
                        + " (" + jobTreeElement.hashCode() + ")"); //$NON-NLS-1$ //$NON-NLS-2$

                System.out.println("  terminated: " + jobTerminated); //$NON-NLS-1$
                if (jobTreeElement instanceof JobInfo)
                    System.out.println("  type: JobInfo"); //$NON-NLS-1$
                else if (jobTreeElement instanceof SubTaskInfo)
                    System.out.println("  type: SubTaskInfo"); //$NON-NLS-1$
                else if (jobTreeElement instanceof GroupInfo)
                    System.out.println("  type: GroupInfo"); //$NON-NLS-1$

                Job job = getJob();
                if (job != null) {
                    System.out.println("  name: " + job.getName()); //$NON-NLS-1$
                    System.out.println("  isSystem: " + job.isSystem()); //$NON-NLS-1$
                }
                System.out.println("  keep: " + keepItem); //$NON-NLS-1$
            }
        }

        Job getJob() {
            if (jobTreeElement instanceof JobInfo)
                return ((JobInfo) jobTreeElement).getJob();
            if (jobTreeElement instanceof SubTaskInfo)
                return ((SubTaskInfo) jobTreeElement).jobInfo.getJob();
            return null;
        }

        public void handleEvent(Event e) {
            switch (e.type) {
            case SWT.Dispose:
                map.remove(jobTreeElement);
                break;
            }
        }

        void init(JobTreeElement info) {
            if (jobTreeElement != info) {
                map.remove(jobTreeElement);
                jobTreeElement = info;
                map.put(jobTreeElement, this);
            }
            refresh();
        }

        public boolean kill() {
            return true;
        }

        abstract boolean refresh();

        public boolean remove() {
            jobTerminated = true;
            refresh();
            if (dialogContext || !keepItem) {
                dispose();
                return true;
            }
            return false;
        }

        void setKeep() {
            keepItem = true;
            Composite parent = getParent();
            if (parent instanceof JobTreeItem) {
                ((JobTreeItem) parent).keepItem = true;
            }
        }

        void setKept() {
            if (!jobTerminated) {
                keepItem = jobTerminated = true;
                remove(); // bring to keep mode
            }
        }
    }

    //

    class ListLayout extends Layout {
        static final int VERTICAL_SPACING = 1;

        boolean refreshBackgrounds;

        protected Point computeSize(Composite composite, int wHint, int hHint,
                boolean flushCache) {
            int w = 0, h = -VERTICAL_SPACING;
            Control[] cs = composite.getChildren();
            for (int i = 0; i < cs.length; i++) {
                Control c = cs[i];
                Point e = c.computeSize(SWT.DEFAULT, SWT.DEFAULT, flushCache);
                w = Math.max(w, e.x);
                h += e.y + VERTICAL_SPACING;
            }
            return new Point(w, h);
        }

        protected void layout(Composite composite, boolean flushCache) {
            int x = 0, y = 0;
            Point e = composite.getSize();
            Control[] cs = getSortedChildren();
            boolean dark = (cs.length % 2) == 1;
            for (int i = 0; i < cs.length; i++) {
                Control c = cs[i];
                Point s = c.computeSize(e.x, SWT.DEFAULT, flushCache);
                c.setBounds(x, y, s.x, s.y);
                y += s.y + VERTICAL_SPACING;
                if (refreshBackgrounds && c instanceof JobItem) {
                    ((JobItem) c).updateBackground(dark);
                    dark = !dark;
                }
            }
        }
    }

    static final boolean DEBUG = false;

    private static String ELLIPSIS = ProgressMessages.ProgressFloatingWindow_EllipsisValue; 

    static final QualifiedName ICON_PROPERTY = IProgressConstants.ICON_PROPERTY;

    static final boolean isCarbon = "carbon".equals(SWT.getPlatform()); //$NON-NLS-1$

    static final QualifiedName KEEP_PROPERTY = IProgressConstants.KEEP_PROPERTY;

    static final QualifiedName KEEPONE_PROPERTY = IProgressConstants.KEEPONE_PROPERTY;

    /**
     * Shorten the given text <code>t</code> so that its length
     * doesn't exceed the width of the given control. This implementation
     * replaces characters in the center of the original string with an
     * ellipsis ("...").
     */
    static String shortenText(Control control, String textValue) {
        if (textValue != null) {
            Display display = control.getDisplay();
            GC gc = new GC(display);
            int maxWidth = control.getBounds().width;
            textValue = shortenText(gc, maxWidth, textValue);
            gc.dispose();
        }
        return textValue;
    }

    /**
     * Shorten the given text <code>t</code> so that its length
     * doesn't exceed the given width. This implementation
     * replaces characters in the center of the original string with an
     * ellipsis ("...").
     */
    static String shortenText(GC gc, int maxWidth, String textValue) {
        if (gc.textExtent(textValue).x < maxWidth) {
            return textValue;
        }
        int length = textValue.length();
        int ellipsisWidth = gc.textExtent(ELLIPSIS).x;
        int pivot = length / 2;
        int start = pivot;
        int end = pivot + 1;
        while (start >= 0 && end < length) {
            String s1 = textValue.substring(0, start);
            String s2 = textValue.substring(end, length);
            int l1 = gc.textExtent(s1).x;
            int l2 = gc.textExtent(s2).x;
            if (l1 + ellipsisWidth + l2 < maxWidth) {
                return s1 + ELLIPSIS + s2;
            }
            start--;
            end++;
        }
        return textValue;
    }

    private Font boldFont;

    private Image cancelJobIcon, cancelJobDIcon;

    private Image clearJobIcon, clearJobDIcon;

    private Color darkColor;

    private Image defaultJobIcon;

    private boolean dialogContext; // viewer runs in dialog: filter accordingly

    private FinishedJobs finishedJobs;
    
    // to be disposed
    private Cursor handCursor;

    private JobTreeItem highlightItem;

    private Job highlightJob;

    private Color lightColor;

    private Composite list;

    private HashMap map = new HashMap();

    private Cursor normalCursor;

    private ScrolledComposite scroller;

    private Color selectedColor;

    private Color selectedTextColor;

    private Font smallerFont;

    private Color textColor;

    /**
     * Create a new ProgressViewer
     */
    public NewProgressViewer(Composite parent, int flags) {
        super(parent, flags);
        Tree c = getTree();
        if (c instanceof Tree)
            c.dispose();

        dialogContext = (flags & SWT.BORDER) != 0; // hack to determine context

        finishedJobs = FinishedJobs.getInstance();

        finishedJobs.addListener(this);

        Display display = parent.getDisplay();
        handCursor = new Cursor(display, SWT.CURSOR_HAND);
        normalCursor = new Cursor(display, SWT.CURSOR_ARROW);

        defaultJobIcon = WorkbenchImages.getWorkbenchImageDescriptor("progress/progress_task.gif").createImage(display); //$NON-NLS-1$
        cancelJobIcon = WorkbenchImages.getWorkbenchImageDescriptor("elcl16/progress_stop.gif").createImage(display); //$NON-NLS-1$
        cancelJobDIcon =WorkbenchImages.getWorkbenchImageDescriptor("dlcl16/progress_stop.gif").createImage(display); //$NON-NLS-1$
        clearJobIcon = WorkbenchImages.getWorkbenchImageDescriptor("elcl16/progress_rem.gif").createImage(display); //$NON-NLS-1$
        clearJobDIcon = WorkbenchImages.getWorkbenchImageDescriptor("dlcl16/progress_rem.gif").createImage(display); //$NON-NLS-1$

        boldFont = getDefaultFont();
        FontData fds[] = getDefaultFontData();
        if (fds.length > 0) {
            FontData fd = fds[0];
            int h = fd.getHeight();
            boldFont = new Font(display, fd.getName(), h, fd.getStyle()
                    | SWT.BOLD);
            smallerFont = new Font(display, fd.getName(), h, fd.getStyle());
        }

        int shift = isCarbon ? -25 : -10; // Mac has different Gamma value
        lightColor = display.getSystemColor(SWT.COLOR_LIST_BACKGROUND);
        darkColor = new Color(display,
                Math.max(0, lightColor.getRed() + shift), Math.max(0,
                        lightColor.getGreen() + shift), Math.max(0, lightColor
                        .getBlue()
                        + shift));
        textColor = display.getSystemColor(SWT.COLOR_LIST_FOREGROUND);
        selectedTextColor = display
                .getSystemColor(SWT.COLOR_LIST_SELECTION_TEXT);
        selectedColor = display.getSystemColor(SWT.COLOR_LIST_SELECTION);

        scroller = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL
                | flags);
        int height = getDefaultFontData()[0].getHeight();
        scroller.getVerticalBar().setIncrement(height * 2);
        scroller.setExpandHorizontal(true);
        scroller.setExpandVertical(true);

        list = new Composite(scroller, SWT.NONE);
        list.setFont(getDefaultFont());
        list.setBackground(lightColor);
        list.setLayout(new ListLayout());

        list.addListener(SWT.MouseDown, new Listener() {
            public void handleEvent(Event event) {
                select(null, event); // clear selection
            }
        });

        scroller.addListener(SWT.Dispose, new Listener() {
            public void handleEvent(Event event) {
                finishedJobs.removeListener(NewProgressViewer.this);

                defaultJobIcon.dispose();
                cancelJobIcon.dispose();
                cancelJobDIcon.dispose();
                clearJobIcon.dispose();
                clearJobDIcon.dispose();

                handCursor.dispose();
                normalCursor.dispose();

                Font defaultFont = getDefaultFont();
                if (boldFont != defaultFont)
                    boldFont.dispose();
                if (smallerFont != defaultFont)
                    smallerFont.dispose();

                darkColor.dispose();

                NewProgressViewer.this.handleDispose(null);
            }
        });

        scroller.setContent(list);

        // refresh UI
        refresh(true);
    }

    public void add(Object parentElement, Object[] elements) {
        if (DEBUG)
            System.err.println("add"); //$NON-NLS-1$
        if (list.isDisposed())
            return;
        JobTreeItem lastAdded = null;
        for (int i = 0; i < elements.length; i++)
            if (!filtered(elements[i]))
                lastAdded = findJobItem(elements[i], true);
        relayout(true, true);
        if (lastAdded != null)
            reveal(lastAdded);
    }

    ///////////////////////////////////

    protected void addTreeListener(Control c, TreeListener listener) {
    }

    public void cancelSelection() {
        boolean changed = false;
        Control[] cs = list.getChildren();
        for (int i = 0; i < cs.length; i++) {
            JobItem ji = (JobItem) cs[i];
            if (ji.selected)
                changed |= ji.cancelOrRemove();
        }
        relayout(changed, changed);
    }

    void clearAll() {

        finishedJobs.clearAll();

        if (DEBUG) {
            JobTreeElement[] elements = finishedJobs.getJobInfos();
            System.out.println("jobs: " + elements.length); //$NON-NLS-1$
            for (int i = 0; i < elements.length; i++)
                System.out.println("  " + elements[i]); //$NON-NLS-1$
        }
    }

    Object[] contentProviderGetChildren(Object parent) {
        IContentProvider provider = getContentProvider();
        if (provider instanceof ITreeContentProvider)
            return ((ITreeContentProvider) provider).getChildren(parent);
        return new Object[0];
    }

    Object[] contentProviderGetRoots(Object parent) {
        IContentProvider provider = getContentProvider();
        if (provider instanceof ITreeContentProvider)
            return ((ITreeContentProvider) provider).getElements(parent);
        return new Object[0];
    }

    protected void createChildren(Widget widget) {
        refresh(true);
    }

    private JobItem createItem(Object element) {
        return new JobItem(list, (JobTreeElement) element);
    }

    void doSelection() {
        boolean changed = false;
        Control[] cs = list.getChildren();
        for (int i = 0; i < cs.length; i++) {
            JobItem ji = (JobItem) cs[i];
            if (ji.selected) {
                Control[] children = ji.getChildren();
                for (int j = 0; j < children.length; j++) {
                    if (children[j] instanceof Hyperlink) {
                        Hyperlink hl = (Hyperlink) children[j];
                        if (hl.handleActivate())
                            return;
                    }
                }
            }
        }
    }

    protected void doUpdateItem(final Item item, Object element) {
    }

    /**
     * Returns true if given element is filtered (i.e. not shown).
     */
    private boolean filtered(Object element) {

        if (element == null)
            return true;

        if (!dialogContext && ProgressViewUpdater.getSingleton().debug) // display all  in debug mode
            return false;

        if (element instanceof JobInfo)
            return jobFiltered((JobInfo) element);

        if (element instanceof GroupInfo) {
            Object[] children = ((GroupInfo) element).getChildren();
            for (int i = 0; i < children.length; i++)
                if (jobFiltered((JobInfo) children[i]))
                    return true;
        }

        if (element instanceof TaskInfo) {
            Object parent = ((TaskInfo) element).getParent();
            if (parent instanceof JobInfo)
                return jobFiltered((JobInfo) parent);
        }

        return false;
    }

    private JobTreeItem findJobItem(Object element, boolean create) {
        JobTreeItem ji = (JobTreeItem) map.get(element);
        if (ji == null && create) {
            JobTreeElement jte = (JobTreeElement) element;
            Object parent = jte.getParent();
            if (parent != null) {
                JobTreeItem parentji = findJobItem(parent, true);
                if (parentji instanceof JobItem && !(jte instanceof TaskInfo)) {
                    if (findJobItem(jte, false) == null) {
                        JobItem p = (JobItem) parentji;
                        p.createChild(jte);
                    }
                }
            } else {
                ji = createItem(jte);
            }
        }
        return ji;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.progress.NewKeptJobs.KeptJobsListener#finished(org.eclipse.ui.internal.progress.JobInfo)
     */
    public void finished(JobTreeElement jte) {
    }

    private void forcedRemove(final JobTreeElement jte) {
        if (list != null && !list.isDisposed()) {
            list.getDisplay().asyncExec(new Runnable() {
                public void run() {
                    if (DEBUG)
                        System.err.println("  forced remove"); //$NON-NLS-1$
                    JobTreeItem ji = findJobItem(jte, false);
                    if (ji != null && !ji.isDisposed() && ji.remove())
                        relayout(true, true);
                }
            });
        }
    }
    
    /**
     * Get the color for active hyperlinks.
     * @return Color
     */
    private Color getActiveHyperlinkColor(){
    	return JFaceColors.getActiveHyperlinkText(getControl().getDisplay());
    }

    protected Item[] getChildren(Widget o) {
        return new Item[0];
    }

    public Control getControl() {
        return scroller;
    }
    
    /**
     * Get the default font to use.
     * @return Font
     */
    private Font getDefaultFont(){
    	return JFaceResources.getDefaultFont();
    }
    
    /**
     * Get the default font data for the workbench
     * @return FontData[]
     */
    private FontData[] getDefaultFontData(){
    	return JFaceResources.getDefaultFont().getFontData();
    }
    
    /**
     * Get the color for errors.
     * @return Color
     */
    private Color getErrorColor(){
    	return JFaceColors.getErrorText(getControl().getDisplay());
    }

    protected boolean getExpanded(Item item) {
        return true;
    }

    private String getFinishedString(JobTreeElement jte, String name,
            boolean withTime) {
        String time = null;
        if (withTime)
            time = getTimeString(jte);
        if (time != null)
			return NLS.bind(ProgressMessages.JobInfo_FinishedAt, name, time ); 
        return NLS.bind(ProgressMessages.JobInfo_Finished,  name ); 
    }
    
    /**
     * Get the color for highlight.
     * @return Color
     */
    private Color getHighlightColor(){
    	return JFaceColors.getErrorText(getControl().getDisplay());
    }
    
    /**
     * Get the color for hyperlinks.
     * @return Color
     */
    private Color getHyperlinkColor(){
    	return JFaceColors.getHyperlinkText(getControl().getDisplay());
    }

    protected Item getItem(int x, int y) {
        return null;
    }

    protected int getItemCount(Control widget) {
        return 1;
    }

    protected int getItemCount(Item item) {
        return 0;
    }

    protected Item[] getItems(Item item) {
        return new Item[0];
    }

    private String getJobNameAndStatus(JobInfo ji, Job job, boolean terminated,
            boolean withTime) {
        String name = job.getName();

        if (job.isSystem())
            name = NLS.bind(ProgressMessages.JobInfo_System, name); 

        if (ji.isCanceled())
			return NLS.bind(ProgressMessages.JobInfo_Cancelled, name);

        if (terminated)
            return getFinishedString(ji, name, withTime);

        if (ji.isBlocked()) {
            IStatus blockedStatus = ji.getBlockedStatus();
            return NLS.bind(ProgressMessages.JobInfo_Blocked, name, blockedStatus.getMessage());
        }

        switch (job.getState()) {
        case Job.RUNNING:
            return name;
        case Job.SLEEPING:
            return NLS.bind(ProgressMessages.JobInfo_Sleeping, name); 
        default:
            return NLS.bind(ProgressMessages.JobInfo_Waiting, name); 
        }
    }

    protected Item getParentItem(Item item) {
        return null;
    }

    ////// SelectionProvider

    public ISelection getSelection() {
        if (list.isDisposed())
            return new StructuredSelection();
        ArrayList l = new ArrayList();
        Control[] cs = list.getChildren();
        for (int i = 0; i < cs.length; i++) {
            JobItem ji = (JobItem) cs[i];
            l.add(ji.jobTreeElement);
        }
        return new StructuredSelection(l);
    }

    protected Item[] getSelection(Control widget) {
        return new Item[0];
    }

    private Control[] getSortedChildren() {
        Control[] cs = list.getChildren();
        ViewerSorter vs = getSorter();
        if (vs != null) {
            HashMap map2 = new HashMap(); // temp remember items for sorting
            JobTreeElement[] elements = new JobTreeElement[cs.length];
            for (int i = 0; i < cs.length; i++) {
                JobItem ji = (JobItem) cs[i];
                elements[i] = ji.jobTreeElement;
                map2.put(elements[i], ji);
            }
            vs.sort(NewProgressViewer.this, elements);
            for (int i = 0; i < cs.length; i++)
                cs[i] = (JobItem) map2.get(elements[i]);
        }
        return cs;
    }

    private String getTimeString(JobTreeElement jte) {
        Date date = finishedJobs.getFinishDate(jte);
        if (date != null)
            return DateFormat.getTimeInstance(DateFormat.SHORT).format(date);
        return null;
    }

    public Tree getTree() {
        Tree t = super.getTree();
        if (t != null && !t.isDisposed())
            return t;
        return null;
    }

    protected void internalRefresh(Object element, boolean updateLabels) {
    }

    private boolean jobFiltered(JobInfo ji) {
        Job job = ji.getJob();
        if (job != null && job == highlightJob)
            return false;
        
        if (job == null || job.getState() == Job.SLEEPING)
            return true;
        
        if(job.isSystem()){
        	if(getContentProvider() instanceof ProgressContentProvider)
        		return !((ProgressContentProvider) getContentProvider()).debug();
        	return false;
        }
        return false;
    }

    protected Item newItem(Widget parent, int flags, int ix) {
        return null;
    }

    public void refresh(boolean updateLabels) {
        if (list.isDisposed())
            return;
        if (DEBUG)
            System.err.println("refreshAll"); //$NON-NLS-1$
        boolean changed = false;
        boolean countChanged = false;
        JobTreeItem lastAdded = null;

        Object[] roots = contentProviderGetRoots(getInput());
        HashSet modelJobs = new HashSet();
        for (int z = 0; z < roots.length; z++)
            modelJobs.add(roots[z]);

        // find all removed
        Control[] children = list.getChildren();
        for (int i = 0; i < children.length; i++) {
            JobItem ji = (JobItem) children[i];
            if (modelJobs.contains(ji.jobTreeElement)) {
                if (DEBUG)
                    System.err.println("  refresh"); //$NON-NLS-1$
                changed |= ji.refresh();
            } else {
                if (DEBUG)
                    System.err.println("  remove: " + ji.jobTreeElement); //$NON-NLS-1$
                if (ji.remove())
                    countChanged = changed = true;
            }
        }

        // find all added
        for (int i = 0; i < roots.length; i++) {
            Object element = roots[i];
            if (filtered(element))
                continue;
            if (findJobItem(element, false) == null) {
                if (DEBUG)
                    System.err.println("  added"); //$NON-NLS-1$
                lastAdded = createItem(element);
                changed = countChanged = true;
            }
        }
        // now add kept finished jobs
        if (!dialogContext) {
            JobTreeElement[] infos = finishedJobs.getJobInfos();
            for (int i = 0; i < infos.length; i++) {
                Object element = infos[i];
                if (filtered(element))
                    continue;
                JobTreeItem jte = findJobItem(element, true);
                if (jte != null) {
                    jte.setKept();
                    lastAdded = jte;

                    if (jte instanceof Hyperlink) {
                        JobItem p = (JobItem) jte.getParent();
                        p.setKept();
                        lastAdded = p;
                    }

                    changed = countChanged = true;
                }
            }
        }

        relayout(changed, countChanged);
        if (lastAdded != null)
            reveal(lastAdded);
    }

    public void refresh(Object element, boolean updateLabels) {
        if (list.isDisposed())
            return;
        if (filtered(element))
            return;
        JobTreeItem ji = findJobItem(element, false);
        if (ji == null) { // not found -> add it (workaround for #68151)
            ji = findJobItem(element, true);
            relayout(true, true);
        } else { // just a refresh
            if (ji.refresh())
                relayout(true, true);
        }
    }

    /*
     * Needs to be called after items have been added or removed,
     * or after the size of an item has changed.
     * Optionally updates the background of all items.
     * Ensures that the background following the last item is always white.
     */
    private void relayout(boolean layout, boolean refreshBackgrounds) {
        if (layout) {
            ListLayout l = (ListLayout) list.getLayout();
            l.refreshBackgrounds = refreshBackgrounds;
            Point size = list.computeSize(list.getClientArea().x, SWT.DEFAULT);
            list.setSize(size);
            scroller.setMinSize(size);
        }
    }

    public void remove(Object[] elements) {
        if (list.isDisposed())
            return;
        if (DEBUG)
            System.err.println("remove"); //$NON-NLS-1$
        boolean changed = false;
        for (int i = 0; i < elements.length; i++) {
            JobTreeItem ji = findJobItem(elements[i], false);
            if (ji != null)
                changed |= ji.remove();
        }
        relayout(changed, changed);
    }

    protected void removeAll(Control widget) {
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.progress.NewKeptJobs.KeptJobsListener#removed(org.eclipse.ui.internal.progress.JobInfo)
     */
    public void removed(final JobTreeElement info) {
        if (list != null && !list.isDisposed()) {
            list.getDisplay().asyncExec(new Runnable() {
                public void run() {
                    if (info != null) { // we got a specific item to remove
                        JobTreeItem ji = findJobItem(info, false);
                        if (ji != null && ji.jobTerminated) {
                            ji.dispose();
                            relayout(true, true);
                        }
                    } else {
                        // remove all terminated
                        Control[] children = list.getChildren();
                        for (int i = 0; i < children.length; i++) {
                            JobTreeItem ji = (JobItem) children[i];
                            if (ji.jobTerminated)
                                ji.dispose();
                        }
                        relayout(true, true);
                    }
                }
            });
        }
    }

    public void reveal(JobTreeItem jti) {
        if (jti != null && !jti.isDisposed()) {

            Rectangle bounds = jti.getBounds();

            int s = bounds.y;
            int e = bounds.y + bounds.height;

            int as = scroller.getOrigin().y;
            int ae = as + scroller.getClientArea().height;

            if (s < as)
                scroller.setOrigin(0, s);
            else if (e > ae)
                scroller.setOrigin(0, as + (e - ae));
        }
    }

    private void select(JobItem newSelection, Event e) {

        boolean clearAll = false;
        JobItem newSel = null;
        Control[] cs = getSortedChildren();

        JobTreeElement element = null;
        if (newSelection != null)
            element = newSelection.jobTreeElement;

        if (e.type == SWT.KeyDown) { // key
            if (e.keyCode == SWT.ARROW_UP) {
                for (int i = 0; i < cs.length; i++) {
                    JobItem ji = (JobItem) cs[i];
                    if (ji.selected) {
                        if (i - 1 >= 0) {
                            newSel = (JobItem) cs[i - 1];
                            if ((e.stateMask & SWT.MOD2) != 0) {
                                newSel.selected = true;
                            } else {
                                clearAll = true;
                            }
                            break;
                        }
                        return;
                    }
                }
                if (newSel == null && cs.length > 0) {
                    newSel = (JobItem) cs[cs.length - 1];
                    newSel.selected = true;
                }
            } else if (e.keyCode == SWT.ARROW_DOWN) {
                for (int i = cs.length - 1; i >= 0; i--) {
                    JobItem ji = (JobItem) cs[i];
                    if (ji.selected) {
                        if (i + 1 < cs.length) {
                            newSel = (JobItem) cs[i + 1];
                            if ((e.stateMask & SWT.MOD2) != 0) {
                                newSel.selected = true;
                            } else {
                                clearAll = true;
                            }
                            break;
                        }
                        return;
                    }
                }
                if (newSel == null && cs.length > 0) {
                    newSel = (JobItem) cs[0];
                    newSel.selected = true;
                }
            }
        } else if (e.type == SWT.MouseDown) { // mouse

            if (newSelection == null) {
                clearAll = true;
            } else {
                if ((e.stateMask & SWT.MOD1) != 0) {
                    newSelection.selected = !newSelection.selected;
                } else if ((e.stateMask & SWT.MOD2) != 0) {
                    // not yet implemented
                } else {
                    if (newSelection.selected)
                        return;
                    clearAll = true;
                    newSel = newSelection;
                }
            }
        }

        if (clearAll) {
            for (int i = 0; i < cs.length; i++) {
                JobItem ji = (JobItem) cs[i];
                ji.selected = ji == newSel;
            }
        }

        boolean dark = (cs.length % 2) == 1;
        for (int i = 0; i < cs.length; i++) {
            JobItem ji = (JobItem) cs[i];
            ji.updateBackground(dark);
            dark = !dark;
        }

        if (newSel != null)
            reveal(newSel);
    }

    protected void setExpanded(Item node, boolean expand) {
    }

    public void setFocus() {
        if (list != null) {
            Control[] cs = list.getChildren();
            for (int i = 0; i < cs.length; i++) {
                JobItem ji = (JobItem) cs[i];
                if (ji.selected) {
                    ji.forceFocus();
                    return;
                }
            }
            if (cs.length > 0)
                cs[0].forceFocus();
        }
    }

    public void setHighlightJob(Job job) {
        highlightJob = job;
        relayout(true, true);
    }

    public void setInput(IContentProvider provider) {
        refresh(true);
    }

    public void setSelection(ISelection selection) {
    }

    protected void setSelection(List items) {
    }

    public void setUseHashlookup(boolean b) {
    }

    protected void showItem(Item item) {
    }

    private String stripPercent(String s) {
        int l = s.length();
        if (l > 0) {
            if (s.charAt(0) == '(') {
                int pos = s.indexOf("%) "); //$NON-NLS-1$
                if (pos >= 0)
                    s = s.substring(pos + 3);
            } else if (s.charAt(l - 1) == ')') {
                int pos = s.lastIndexOf(": ("); //$NON-NLS-1$
                if (pos >= 0)
                    s = s.substring(0, pos);
            }
        }
        return s;
    }
}
