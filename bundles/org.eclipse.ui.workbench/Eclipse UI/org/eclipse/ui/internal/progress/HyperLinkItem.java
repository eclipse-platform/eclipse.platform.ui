package org.eclipse.ui.internal.progress;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.ui.progress.IProgressConstants;

/**
 * @author Administrator
 * 
 */
public class HyperLinkItem extends JobItem implements
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


	HyperLinkItem(JobsProgressViewer viewer, JobItem parent, JobTreeElement info) {
		super(viewer, parent, info, SWT.NO_BACKGROUND);

		jobitem = parent;

		setFont(viewer.getSmallerFont());

		addPaintListener(new PaintListener(){
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.PaintListener#paintControl(org.eclipse.swt.events.PaintEvent)
			 */
			public void paintControl(org.eclipse.swt.events.PaintEvent e){
				paint(e.gc);
			}
		});
		addMouseListener(getMouseListener());
		addKeyListener(getKeyListener());
		addMouseTrackListener(new MouseTrackAdapter(){
			
			public void mouseEnter(MouseEvent e){
				if (isLinkEnabled()) {
					mouseOver = true;
					redraw();
				}
			}
			
			public void mouseExit(MouseEvent e){
				if (isLinkEnabled()) {
					mouseOver = false;
					redraw();
				}
			}
		});
		
		
		addFocusListener(new FocusListener(){
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.FocusListener#focusGained(org.eclipse.swt.events.FocusEvent)
			 */
			public void focusGained(org.eclipse.swt.events.FocusEvent e){
				hasFocus = true;			
			}
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.FocusListener#focusLost(org.eclipse.swt.events.FocusEvent)
			 */
			public void focusLost(org.eclipse.swt.events.FocusEvent e){
				hasFocus = false;
			}
			
			
		});

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
			return viewer.getSelectedTextColor();

		if (isLinkEnabled()) {

			if (isError)
				return viewer.getErrorColor();

			if (mouseOver)
				return viewer.getActiveHyperlinkColor();
			return viewer.getHyperlinkColor();
		}
		return viewer.getTextColor();
	}

	protected boolean handleActivate() {
		if (isLinkEnabled() && gotoAction != null && gotoAction.isEnabled()) {
			jobitem.locked = true;
			gotoAction.run();
			if (jobitem.jobTerminated)
				jobitem.kill();
			return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.widgets.Widget#dispose()
	 */
	public void dispose(){
		super.dispose();
		if (gotoAction != null) {
			gotoAction.removePropertyChangeListener(this);
			gotoAction = null;
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.progress.JobItem#handleKeyPress(org.eclipse.swt.events.KeyEvent)
	 */
	protected void handleKeyPress(KeyEvent event) {
		if (event.character == '\r')
			handleActivate();
		else if (event.keyCode == SWT.DEL) {
			viewer.cancelSelection();
		} else {
			viewer.select(null, event);
		}
		
	}
	
	
	protected MouseListener getMouseListener() {
		return new MouseListener(){

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.MouseListener#mouseDoubleClick(org.eclipse.swt.events.MouseEvent)
			 */
			public void mouseDoubleClick(MouseEvent e) {
				// Do Nothing
				
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.MouseListener#mouseDown(org.eclipse.swt.events.MouseEvent)
			 */
			public void mouseDown(MouseEvent e) {
				if (!isLinkEnabled())
					viewer.select((JobItem) getParent(), e);
				
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.MouseListener#mouseUp(org.eclipse.swt.events.MouseEvent)
			 */
			public void mouseUp(MouseEvent e) {
				if (isLinkEnabled()) {
					Point size = getSize();
					if (e.button != 1 || e.x < 0 || e.y < 0 || e.x >= size.x
							|| e.y >= size.y)
						return;
					handleActivate();
				}
				
			}
			
		};
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.progress.JobItem#defaultSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	protected void defaultSelected(SelectionEvent e) {
		handleActivate();
	}

	boolean isLinkEnabled() {
		return !viewer.dialogContext && linkEnabled;
	}

	protected void paint(GC gc) {
		Rectangle clientArea = getClientArea();
		if (clientArea.isEmpty())
			return;
		Color fg = getFGColor(), bg = getBackground();
		if (jobitem.selected)
			bg = viewer.getSelectedColor();
		Image buffer = null;
		GC bufferGC = gc;
		if (!JobsProgressViewer.isCarbon) {
			buffer = new Image(getDisplay(), clientArea.width,
					clientArea.height);
			buffer.setBackground(bg);
			bufferGC = new GC(buffer, gc.getStyle());
		}
		bufferGC.setForeground(fg);
		bufferGC.setBackground(bg);
		bufferGC.fillRectangle(0, 0, clientArea.width, clientArea.height);
		bufferGC.setFont(getFont());
		String t = JobsProgressViewer.shortenText(bufferGC, clientArea.width, text);
		bufferGC.drawText(t, MARGINWIDTH, MARGINHEIGHT, true);
		int sw = bufferGC.stringExtent(t).x;
		if (isLinkEnabled()) {
			FontMetrics fm = bufferGC.getFontMetrics();
			int lineY = clientArea.height - MARGINHEIGHT - fm.getDescent() + 1;
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
						 * Get the notificationManager that this is being
						 * created for.
						 */
						private ErrorNotificationManager getManager() {
							return ProgressManager.getInstance().errorManager;
						}

						public void run() {
							String title = ProgressMessages
									.getString("NewProgressView.errorDialogTitle"); //$NON-NLS-1$
							String msg = ProgressMessages
									.getString("NewProgressView.errorDialogMessage"); //$NON-NLS-1$
							if (!getManager().showErrorFor(job, title, msg)) {
								// The error is missing from the error manager.
								// This should only occur if what the progress
								// view is showing is
								// out-of-sync with the ErrorNotificationManager
								// and/or FinishedJobs
								// manager. In other words, it shouldn't happen
								// but may so it is
								// better to show the user something than fail
								// silently
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
					name = ProgressMessages
							.format(
									"JobInfo.TaskFormat", new Object[] { taskName, name }); //$NON-NLS-1$
			} else {
				name = jobTreeElement.getDisplayString();
				if (taskName != null && taskName.trim().length() > 0)
					name = ProgressMessages
							.format(
									"JobInfo.TaskFormat2", new Object[] { taskName, name }); //$NON-NLS-1$
			}

			if (name.length() == 0) {
				dispose();
				return true;
			}

		} else if (jobTreeElement instanceof JobInfo) { // group case
			JobInfo ji = (JobInfo) jobTreeElement;

			if (/* jobTerminated && */result != null) {
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
				name = viewer.getJobNameAndStatus(ji, job, jobTerminated, false);

				// get percentage and task name
				String taskName = null;
				TaskInfo info = ji.getTaskInfo();
				if (info != null) {
					taskName = info.getTaskName();
					int percent = info.getPercentDone();
					if (percent >= 0 && percent <= 100) {
						if (taskName != null)
							taskName = ProgressMessages.format(
									"JobInfo.Percent", //$NON-NLS-1$
									new Object[] { Integer.toString(percent),
											taskName });
						else
							taskName = ProgressMessages.format(
									"JobInfo.Percent2", //$NON-NLS-1$
									new Object[] { Integer.toString(percent) });
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
					name = ProgressMessages
							.format(
									"JobInfo.Format", new Object[] { name, taskName, subTaskName }); //$NON-NLS-1$
				} else if (hasTask) {
					name = ProgressMessages
							.format(
									"JobInfo.TaskFormat", new Object[] { name, taskName }); //$NON-NLS-1$
				} else if (hasSubTask) {
					name = ProgressMessages
							.format(
									"JobInfo.TaskFormat", new Object[] { name, subTaskName }); //$NON-NLS-1$
				}
			}

			if (viewer.highlightJob == job)
				viewer.highlightItem = jobitem;
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
				setCursor(viewer.getHandCursor());
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
