/**
 * 
 */
package org.eclipse.ui.internal.progress;

import java.net.URL;
import java.util.HashSet;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.internal.misc.Assert;
import org.eclipse.ui.internal.progress.NewProgressViewer.Hyperlink;
import org.eclipse.ui.internal.progress.NewProgressViewer.JobTreeItem;

class JobItem extends Canvas {

	/**
	 * 
	 */

	static final int HGAP = 7;

	static final int MARGIN = 2;

	static final int MAX_PROGRESS_HEIGHT = 12;

	static final int MIN_ICON_SIZE = 16;

	static final int VGAP = 1;

	ToolBar actionBar;

	ToolItem actionButton;

	int cachedHeight = -1;

	int cachedWidth = -1;

	boolean disposeImage;

	ToolItem gotoButton;

	Label iconItem;

	Image image;

	boolean jobTerminated;

	JobTreeElement jobTreeElement;

	boolean keepItem;

	boolean locked; // don't add children

	Label nameItem;

	ProgressBar progressBar;

	boolean selected;

	protected final JobsProgressViewer viewer;

	JobItem(JobsProgressViewer newViewer, Composite parent, JobTreeElement info) {
		this(newViewer, parent, info, SWT.NONE);

		Assert.isNotNull(info);

		Display display = getDisplay();

		iconItem = new Label(this, SWT.NONE);

		MouseListener mouseListener = getMouseListener();
		iconItem.addMouseListener(mouseListener);
		updateIcon(getJob());
		if (image == null)
			iconItem.setImage(this.viewer.defaultJobIcon);

		nameItem = new Label(this, SWT.NONE);
		nameItem.setFont(this.viewer.boldFont);
		nameItem.addMouseListener(mouseListener);

		actionBar = new ToolBar(this, SWT.FLAT);
		actionBar.setCursor(this.viewer.normalCursor); // set cursor to
		// overwrite any busy
		// cursor we might have
		actionButton = new ToolItem(actionBar, SWT.NONE);
		actionButton.setImage(viewer.cancelJobIcon);
		actionButton.setDisabledImage(viewer.cancelJobDIcon);
		actionButton.setToolTipText(ProgressMessages
				.getString("NewProgressView.CancelJobToolTip")); //$NON-NLS-1$
		actionButton.addSelectionListener(new SelectionAdapter() {
			/* (non-Javadoc)
			 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			public void widgetSelected(SelectionEvent e) {
				actionButton.setEnabled(false);
				cancelOrRemove();
			}
			/* (non-Javadoc)
			 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			public void widgetDefaultSelected(SelectionEvent e){
				defaultSelected(e);
			}
		});

		addMouseListener(mouseListener);
		addKeyListener(getKeyListener());

		addControlListener(new ControlAdapter() {
			public void controlResized(ControlEvent e) {
				handleResize();
			}
		});

		refresh();
	}

	/**
	 * A default select event has occurred.
	 * @param e
	 */
	protected void defaultSelected(SelectionEvent e) {
		
		
	}

	/**
	 * @param newViewer
	 * @param parent
	 * @param info
	 * @param flags
	 */
	public JobItem(JobsProgressViewer newViewer, Composite parent,
			JobTreeElement info, int flags) {
		super(parent, flags);
		this.viewer = newViewer;
		jobTreeElement = info;
		this.viewer.map.put(jobTreeElement, this);
	}

	boolean aboutToKeep() {
		boolean changed = false;

		// finish progress reporting
		if (progressBar != null && !progressBar.isDisposed()) {
			progressBar.setSelection(100);
			progressBar.dispose();
			changed = true;
		}

		// turn cancel button in remove button
		if (!actionButton.isDisposed()) {
			actionButton.setImage(this.viewer.clearJobIcon);
			actionButton.setDisabledImage(this.viewer.clearJobDIcon);
			actionButton.setToolTipText(ProgressMessages
					.getString("NewProgressView.RemoveJobToolTip")); //$NON-NLS-1$
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

	boolean checkKeep() {
		if (jobTreeElement instanceof JobInfo
				&& FinishedJobs.keep((JobInfo) jobTreeElement))
			setKeep();
		return keepItem;
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
				Point e3 = progressBar.computeSize(SWT.DEFAULT, SWT.DEFAULT);
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
			new HyperLinkItem(viewer, this, jte);
	}

	protected void dump(String message) {
		if (JobsProgressViewer.DEBUG) {
			System.out
					.println(message + " (" + jobTreeElement.hashCode() + ")"); //$NON-NLS-1$ //$NON-NLS-2$

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

	String getGroupHeader(GroupInfo gi) {
		String name = this.viewer.stripPercent(jobTreeElement
				.getDisplayString());
		if (jobTerminated)
			return this.viewer.getFinishedString(gi, name, true);
		return name;
	}

	/**
	 * Get the job we are displaying.
	 * 
	 * @return Job
	 */
	protected Job getJob() {

		if (jobTreeElement instanceof JobInfo)
			return ((JobInfo) jobTreeElement).getJob();
		if (jobTreeElement instanceof SubTaskInfo)
			return ((SubTaskInfo) jobTreeElement).jobInfo.getJob();
		return null;

	}

	protected KeyListener getKeyListener() {
		return new KeyAdapter() {
			public void keyPressed(KeyEvent event) {
				handleKeyPress(event);
			}
		};
	}

	protected MouseListener getMouseListener() {
		return new MouseAdapter() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.MouseListener#mouseDown(org.eclipse.swt.events.MouseEvent)
			 */
			public void mouseDown(MouseEvent event) {
				forceFocus();
				viewer.select(null, event);
			}

		};
	}

	IStatus getResult() {
		// checkKeep();
		if (jobTerminated) {
			Job job = getJob();
			if (job != null)
				return job.getResult();
		}
		return null;
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

		nameItem.setBounds(MARGIN + e1.x + HGAP, y + (h - e2.y) / 2, iw - e1.x
				- HGAP, e2.y);
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

		actionBar.setBounds(e.x - MARGIN - e5.x, (e.y - e5.y) / 2, e5.x, e5.y);
	}

	void init(JobTreeElement info) {
		if (jobTreeElement != info) {
			this.viewer.map.remove(jobTreeElement);
			jobTreeElement = info;
			this.viewer.map.put(jobTreeElement, this);
		}
		refresh();
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
			if (!this.viewer.finishedJobs.remove(jobTreeElement)) {
				// The terminated job has already been removed
				// from the list of finished jobs but was somehow
				// left in the view. Dispose of the item and refresh
				// the view
				dispose();
				this.viewer.relayout(true, true);
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
		Object[] roots = this.viewer.contentProviderGetChildren(jobTreeElement);
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
			name = this.viewer.getJobNameAndStatus((JobInfo) jobTreeElement,
					job, jobTerminated, true);
		if (name == null)
			name = this.viewer.stripPercent(jobTreeElement.getDisplayString());

		if (this.viewer.highlightJob != null
				&& (this.viewer.highlightJob == job || viewer.highlightItem == this))
			name = ProgressMessages.format(
					"JobInfo.BlocksUserOperationFormat", new Object[] { name }); //$NON-NLS-1$

		nameItem.setToolTipText(name);
		nameItem.setText(JobsProgressViewer.shortenText(nameItem, name));

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

		if (this.viewer.finishedJobs.isFinished(jobTreeElement)) {
			keepItem = true;
		}

		// propagate status down
		Control[] children = getChildren();
		for (int i = 0; i < children.length; i++) {
			if (children[i] instanceof JobTreeItem)
				((JobTreeItem) children[i]).jobTerminated = true;
		}

		if (!this.viewer.dialogContext) { // we never keep jobs in dialogs
			if (!keepItem)
				checkKeep();

			if (keepItem)
				return aboutToKeep();
		}
		jobTerminated = true;
		refresh();
		if (this.viewer.dialogContext || !keepItem) {
			dispose();
			return true;
		}
		return false;
	}

	void setKeep() {
		keepItem = true;
		Composite parent = getParent();
		if (parent instanceof JobItem)
			((JobItem) parent).keepItem = true;
	}

	void setKept() {
		if (!jobTerminated) {
			keepItem = jobTerminated = true;
			remove(); // bring to keep mode
		}
	}

	boolean setPercentDone(int percentDone) {
		if (percentDone >= 0 && percentDone < 100) {
			if (progressBar == null) {
				progressBar = new ProgressBar(this, SWT.HORIZONTAL);
				progressBar.setMaximum(100);
				progressBar.setSelection(percentDone);
				progressBar.addMouseListener(getMouseListener());
				return true;
			} else if (!progressBar.isDisposed())
				progressBar.setSelection(percentDone);
		} else {
			if (progressBar == null) {
				progressBar = new ProgressBar(this, SWT.HORIZONTAL
						| SWT.INDETERMINATE);
				progressBar.addMouseListener(getMouseListener());
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
			fg = this.viewer.selectedTextColor;
			bg = this.viewer.selectedColor;
		} else {
			if (this.viewer.highlightJob != null
					&& (this.viewer.highlightJob == getJob() || viewer.highlightItem == this))
				fg = this.viewer.getHighlightColor();
			else
				fg = this.viewer.textColor;
			bg = dark ? this.viewer.darkColor : this.viewer.lightColor;
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
			Object property = job.getProperty(JobsProgressViewer.ICON_PROPERTY);
			if (property instanceof ImageDescriptor) {
				dispImage = true;
				im = ((ImageDescriptor) property).createImage(display);
			} else if (property instanceof URL) {
				dispImage = true;
				im = ImageDescriptor.createFromURL((URL) property).createImage(
						display);
			} else {
				dispImage = false;
				im = ProgressManager.getInstance().getIconFor(job);
			}
			if (im != null && im != image) {
				if (disposeImage && image != null) {
					if (JobsProgressViewer.DEBUG)
						System.err.println("JobItem.setImage: disposed image"); //$NON-NLS-1$
					image.dispose();
				}
				image = im;
				disposeImage = dispImage;
				if (iconItem != null)
					iconItem.setImage(image);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Widget#dispose()
	 */
	public void dispose() {
		super.dispose();
		viewer.map.remove(jobTreeElement);
		if (disposeImage && image != null && !image.isDisposed()) {
			if (JobsProgressViewer.DEBUG)
				System.err.println("JobItem.image disposed"); //$NON-NLS-1$
			image.dispose();
		}
		image = null;

	}

	/**
	 * Handle a key press on the receiver.
	 * @param event
	 */
	protected void handleKeyPress(KeyEvent event) {
		if (event.character == '\r') {
			viewer.doSelection();
		} else if (event.character == '\t') {
			viewer.scroller.getParent().forceFocus();
		} else if (event.keyCode == SWT.DEL) {
			viewer.cancelSelection();
		} else {
			viewer.select(null, event);
		}
	}

}