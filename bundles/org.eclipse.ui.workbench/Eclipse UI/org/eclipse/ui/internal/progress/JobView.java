package org.eclipse.ui.internal.progress;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import org.eclipse.swt.widgets.*;
//import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.internal.misc.Assert;
import org.eclipse.ui.part.*;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ProgressIndicator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.swt.SWT;


public class JobView extends ViewPart {
	
	static final String PROPERTY_PREFIX= "org.eclipse.ui.workbench.progress";
	
	static Color fgLink;
	static Color fgLink2;
	static Color fgDark;
	static Color fgWhite;
	static Color fgTask;
	static Color fgSelected;
	static Color fgBlack;
	static Font fgFont;
	static Font fgSmallFont;
	static Font fgSmallBoldFont;
	
	/*
	 * JobsModel change types.
	 */
	static final int ADD= 1;
	static final int REMOVE= 2;
	static final int REFRESH= 3;

	/*
	 * Listener for JobsModel changes.
	 */
	interface IJobsModelListener {
		void refresh(int changeType, JobModel jm);
	}
	
	/*
	 * The model of all Jobs.
	 * Tracks the ProgressManager.
	 * Supports "sticky" jobs.
	 */
	static class JobsModel {
	
		static JobsModel fgJobsModel;

		private ArrayList fJobModels= new ArrayList();
		private HashMap fJobInfoToJobModel= new HashMap();
		private ListenerList fListeners= new ListenerList();

		static synchronized JobsModel getJobsModel() {
			if (fgJobsModel == null)
				fgJobsModel= new JobsModel();
			return fgJobsModel;
		}
		
		public void addListener(IJobsModelListener listener) {
			fListeners.add(listener);
		}
		
		public void removeListener(IJobsModelListener listener) {
			fListeners.remove(listener);
		}
		
		private void fire(int changeType, JobModel jm) {
			Object[] ls= fListeners.getListeners();
			for (int i= 0; i < ls.length; i++)
				((IJobsModelListener)ls[i]).refresh(changeType, jm);
		}
		
		private JobsModel() {
			
			ProgressManager pm= ProgressManager.getInstance();
			
			Object[] o= pm.getRootElements(true);
			for (int i= 0; i < o.length; i++) {
				JobTreeElement jte= (JobTreeElement) o[i];
				JobModel jm= new JobModel(jte);
				fJobModels.add(jm);
				fJobInfoToJobModel.put(jte, jm);
			}

			pm.addListener(new IJobProgressManagerListener() {
				public void addJob(JobInfo info) {
					add(info);
				}
				public void refreshJobInfo(JobInfo info) {
					refresh(info);
				}
				public void removeJob(JobInfo info) {
					remove(info);
				}
				
				public void addGroup(GroupInfo group) {
					add(group);
				}
				public void refreshGroup(GroupInfo group) {
					refresh(group);
				}
				public void removeGroup(GroupInfo group) {
					remove(group);
				}
				
				public void refreshAll() {
					fire(REFRESH, null);
				}
				public boolean showsDebug() {
					return false;
				}
			});
		}

		private void add(JobTreeElement jte) {
			JobModel jm= new JobModel(jte);
			synchronized (fJobModels) {
				fJobModels.add(jm);
				fJobInfoToJobModel.put(jte, jm);
			}
			fire(ADD, null);
		}

		private void refresh(JobTreeElement jte) {
			JobModel jm= null;
			synchronized (fJobModels) {
				jm= (JobModel) fJobInfoToJobModel.get(jte);
			}
			fire(REFRESH, jm);
		}

		private void remove(JobTreeElement jte) {
			JobModel jm= null;
			synchronized (fJobModels) {
				jm= (JobModel) fJobInfoToJobModel.get(jte);
				if (jm != null) {
					jm.setTerminated();
					if (!jm.fKeep) {
						fJobModels.remove(jm);
						fJobInfoToJobModel.remove(jte);
					}
				}
			}
			if (jm != null)
				fire(jm.fKeep ? REFRESH : REMOVE, jm);
		}
		
		public HashSet getSet() {
			HashSet set= new HashSet();
			synchronized (fJobModels) {
				Object[] os= fJobModels.toArray();
				for (int i= 0; i < os.length; i++)
					set.add(os[i]);
			}
			return set;
		}

		public void remove(JobModel model) {
			synchronized (fJobModels) {
				fJobModels.remove(model);
				fJobInfoToJobModel.remove(model.fInfo);
			}
			fire(REMOVE, model);
		}

		public void clearAll() {
			synchronized (fJobModels) {
				JobModel[] jms= (JobModel[]) fJobModels.toArray(new JobModel[fJobModels.size()]);
				for (int i= 0; i < jms.length; i++) {
					JobModel jm= jms[i];
					if (jm.fKeep && jm.fTerminated) {
						fJobModels.remove(jm);
						fJobInfoToJobModel.remove(jm.fInfo);
					}
				}
			}
			fire(REMOVE, null);
		}
	}
	
	static class Hyperlink extends Canvas implements Listener {
		boolean hasFocus;
		String fText;
		boolean fUnderlined;
		int marginWidth= 1;
		int marginHeight= 1;
		IAction fAction;
		
		Hyperlink(Composite parent, int flags) {
			super(parent, SWT.NO_BACKGROUND | flags);
			addListener(SWT.KeyDown, this);
			addListener(SWT.Paint, this);
			addListener(SWT.MouseEnter, this);
			addListener(SWT.MouseExit, this);
			addListener(SWT.MouseUp, this);
			addListener(SWT.FocusIn, this);
			addListener(SWT.FocusOut, this);
			//setCursor(FormsResources.getHandCursor());
		}
		public void handleEvent(Event e) {
			switch (e.type) {
			case SWT.KeyDown:
				if (e.character == '\r')
					handleActivate();
				break;
			case SWT.Paint:
				paint(e.gc);
				break;
			case SWT.FocusIn :
				hasFocus = true;
			case SWT.MouseEnter :
				if (fUnderlined) {
					setForeground(fgLink2);
					redraw();
				}
				break;
			case SWT.FocusOut :
				hasFocus = false;
			case SWT.MouseExit :
				if (fUnderlined) {
					setForeground(fgLink);
					redraw();
				}
				break;
			case SWT.DefaultSelection :
				handleActivate();
				break;
			case SWT.MouseUp :
				Point size= getSize();
				if (e.button != 1 || e.x < 0 || e.y < 0 || e.x >= size.x || e.y >= size.y)
					return;
				handleActivate();
				break;
			}
		}
		void setText(String text) {
			fText= text != null ? text : "";
			redraw();
		}
		void setAction(IAction action) {
			fAction= action;
			fUnderlined= action != null;
			setForeground(fUnderlined ? fgLink : fgTask);
			redraw();
		}
		public Point computeSize(int wHint, int hHint, boolean changed) {
			checkWidget();
			int innerWidth= wHint;
			if (innerWidth != SWT.DEFAULT)
				innerWidth -= marginWidth * 2;
			GC gc= new GC(this);
			gc.setFont(getFont());
			Point extent= gc.textExtent(fText);
			gc.dispose();
			return new Point(extent.x + 2 * marginWidth, extent.y + 2 * marginHeight);
		}
		protected void paint(GC gc) {
			Rectangle clientArea= getClientArea();
			Image buffer= new Image(getDisplay(), clientArea.width, clientArea.height);
			buffer.setBackground(getBackground());
			GC bufferGC= new GC(buffer, gc.getStyle());
			bufferGC.setBackground(getBackground());
			bufferGC.fillRectangle(0, 0, clientArea.width, clientArea.height);
			bufferGC.setFont(getFont());
			bufferGC.setForeground(getForeground());
			bufferGC.drawText(fText, marginWidth, marginHeight, true);
			int sw= bufferGC.stringExtent(fText).x;
			if (fUnderlined) {
				FontMetrics fm= bufferGC.getFontMetrics();
				int lineY= clientArea.height - marginHeight - fm.getDescent() + 1;
				bufferGC.drawLine(marginWidth, lineY, marginWidth + sw, lineY);
			}
			if (hasFocus)
				bufferGC.drawFocus(0, 0, sw, clientArea.height);
			gc.drawImage(buffer, 0, 0);
			bufferGC.dispose();
			buffer.dispose();
		}
		protected void handleActivate() {
			if (fUnderlined && fAction != null && fAction.isEnabled())
				fAction.run();
		}
	}
	
	/*
	 * Represents a Job in the JobsModel.
	 */
	static class JobModel {
		private JobTreeElement fInfo;
		int fLastWorkDone;
		boolean fKeep;
		boolean fTerminated;

		public JobModel(JobTreeElement info) {
			Assert.isNotNull(info);
			fInfo= info;
			Job job= getJob();
			if (job != null) {
				Object property= job.getProperty(new QualifiedName(PROPERTY_PREFIX, "keep"));
				if (property instanceof Boolean)
					fKeep= ((Boolean)property).booleanValue();
			}			
			//fKeep= "Synchronizing".equals(getName());
		}
		
		Image getImage(Display display) {
			Job job= getJob();
			if (job != null) {
				Object property= job.getProperty(new QualifiedName(PROPERTY_PREFIX, "icon"));
				if (property instanceof URL) {
					URL url= (URL) property;
					ImageDescriptor id= ImageDescriptor.createFromURL(url);
					return id.createImage(display);
				}
			}
			return null;
		}
		
		/*
		 * Temporary: we try to find icons for some well known jobs. 
		 */
		String getIconURL() {
			String name= getName();
			if (name.startsWith("Search"))
				return "search.gif";
			if (name.startsWith("Synchronizing"))
				return "synch_synch.gif";
			if (name.startsWith("Building"))
				return "build.gif";
			if (name.startsWith("Periodic"))
				return "save.gif";
			return null;
		}
		
		String getName() {
			if (fInfo instanceof JobInfo) {
				Job job= ((JobInfo)fInfo).getJob();
				if (job != null)
					return job.getName();
			}
			return "G" + fInfo.getCondensedDisplayString();
		}
		
		boolean isTerminated() {
			return fKeep && fTerminated;
		}
		
		boolean isStale() {
			return fTerminated && !fKeep;
		}
		
		boolean setTerminated() {
			fTerminated= true;
			return fKeep && fTerminated;
		}
		
		Job getJob() {
			if (fInfo instanceof JobInfo)
				return ((JobInfo)fInfo).getJob();
			return null;
		}
		
		private String getStatus() {
			if (fInfo instanceof JobInfo) {
				JobInfo ji= (JobInfo) fInfo;
				if (ji.isCanceled())
					return "Canceled";
				if (ji.isBlocked())
					return "Blocked (" + ji.getBlockedStatus().getMessage() + ")";
				switch (ji.getJob().getState()) {
				case Job.NONE:
					return "Terminated";
				case Job.RUNNING:
					return "Running";
				case Job.SLEEPING:
					return "Sleeping";
				case Job.WAITING:
					return "Waiting";
				}
				return "Unknown state";
			}
			return "Group";
		}
		
		String getTaskName() {
			if (fInfo instanceof JobInfo) {
				JobInfo ji= (JobInfo) fInfo;
				String s= "[" + getStatus() + "]";
				if (ji.isCanceled())
					return s;
				if (ji.isBlocked())
					return s;
				if (ji.getJob().getState() == Job.RUNNING) {
					TaskInfo taskInfo= ji.getTaskInfo();
					if (taskInfo != null) {
						String tm= taskInfo.getDisplayString();
						int pos= tm.indexOf(':');
						if (pos > 0)
							tm= tm.substring(pos+2);
						s+= ": " + tm;
					}
				}
				return s;
			}
			return fInfo.getDisplayString();
		}
		
		int getPercentDone() {
			if (fInfo instanceof JobInfo) {
				TaskInfo ti= ((JobInfo)fInfo).getTaskInfo();
				if (ti != null) {
					int pd= ti.getPercentDone();
					if (pd >= 0) {
						int delta= pd-fLastWorkDone;
						fLastWorkDone= pd;				
						return delta;
					}	
				}
			}
			return -1;
		}

		void cancel() {
			fInfo.cancel();
		}
		
		boolean isCancellable() {
			return fInfo.isCancellable();
		}

		public IAction getGotoAction() {
			Job job= getJob();
			if (job != null) {
				Object property= job.getProperty(new QualifiedName(PROPERTY_PREFIX, "goto"));
				if (property instanceof IAction)
					return (IAction) property;
			}
			return null;
		}
	}
	
	/*
	 * An SWT widget representing a JobModel
	 */
	class JobItem extends Composite {
		
		static final int MARGIN= 2;
		static final int HGAP= 7;
		static final int VGAP= 2;
		static final int MAX_PROGRESS_HEIGHT= 12;
		static final int MIN_ICON_SIZE= 16;

		JobModel fModel;
		boolean fSelected;
		boolean fFinished;
		boolean fInitialized;

		int fCachedWidth= -1;
		int fCachedHeight= -1;
		Label fIcon;
		Label fName;
		ProgressIndicator fProgressBar;
		Hyperlink fTask;
		ToolBar fActionBar;
		ToolItem fActionButton;
		ToolItem fGotoButton;
		
		JobItem(Composite parent, JobModel model) {
			super(parent, SWT.NONE);
			
			fModel= model;
			
			Display display= getDisplay();

			MouseListener ml= new MouseAdapter() {
				public void mouseDown(MouseEvent e) {
//					select(JobItem.this);
				}
			};
			
			setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

			fIcon= new Label(this, SWT.NONE);
			Image im= fModel.getImage(display);
			if (im == null) {
				String iconName= fModel.getIconURL();
				if (iconName != null)
					im= getImage(display, iconName);
			}
			if (im != null)
				fIcon.setImage(im);				
			fIcon.addMouseListener(ml);
			
			fName= new Label(this, SWT.NONE);
			fName.setFont(fgFont);
			fName.setText(fModel.getName());
			fName.addMouseListener(ml);
			
			fActionBar= new ToolBar(this, SWT.FLAT);
			
			final IAction gotoAction= fModel.getGotoAction();
			if (false && gotoAction != null) {
				fGotoButton= new ToolItem(fActionBar, SWT.NONE);
				fGotoButton.setImage(getImage(parent.getDisplay(), "goto.gif"));
				fGotoButton.setToolTipText("Show Results");
				fGotoButton.setEnabled(gotoAction.isEnabled());
				fGotoButton.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						if (gotoAction.isEnabled()) {
							gotoAction.run();
							if (fFinished)
								JobsModel.getJobsModel().remove(fModel);
						}
					}
				});
			}

			fActionButton= new ToolItem(fActionBar, SWT.NONE);
			fActionButton.setImage(getImage(parent.getDisplay(), "stop.gif"));
			fActionButton.setToolTipText("Cancel Job");
			fActionButton.setEnabled(true | fModel.isCancellable());
			fActionButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					if (fFinished) {
						JobsModel.getJobsModel().remove(fModel);
					} else {
						fModel.cancel();
						setTask("cancelled");
					}
				}
			});
			
			fActionBar.pack();

			fProgressBar= new ProgressIndicator(this);
			//fProgressBar.beginAnimatedTask();
			fProgressBar.beginTask(100);
			fProgressBar.addMouseListener(ml);
			
 			fTask= new Hyperlink(this, SWT.NONE);
 			if (gotoAction != null) {
 				fTask.setToolTipText(gotoAction.getToolTipText());
 				fTask.setAction(gotoAction);
 			}
			fTask.setFont(fgSmallFont);
			fTask.setText(fModel.getTaskName());
			//fTask.addMouseListener(ml);
			
			addMouseListener(ml);
			
			addControlListener(new ControlAdapter() {
				public void controlResized(ControlEvent e) {
					handleResize();
				}
			});

			redraw();
		}
		
		void handleResize() {
			Point e= getSize();
			Point e1= fIcon.computeSize(SWT.DEFAULT, SWT.DEFAULT); e1.x= MIN_ICON_SIZE;
			Point e2= fName.computeSize(SWT.DEFAULT, SWT.DEFAULT);
			Point e3= fProgressBar.computeSize(SWT.DEFAULT, SWT.DEFAULT); e3.y= MAX_PROGRESS_HEIGHT;
			Point e4= fTask.computeSize(SWT.DEFAULT, SWT.DEFAULT);
			Point e5= fActionBar.computeSize(SWT.DEFAULT, SWT.DEFAULT);
			
			int iw= e.x-MARGIN-HGAP-e5.x-MARGIN;
			int indent= 16+HGAP;
				
			int y= MARGIN;
			int h= Math.max(e1.y, e2.y);
			fIcon.setBounds(MARGIN, y+(h-e1.y)/2, e1.x, e1.y);
			fName.setBounds(MARGIN+e1.x+HGAP, y+(h-e2.y)/2, iw-e1.x-HGAP, e2.y);
			y+= h;
			if (fProgressBar.isVisible()) {
				y+= VGAP;
				fProgressBar.setBounds(MARGIN+indent, y, iw-indent, e3.y);
				y+= e3.y;
			}
			if (fTask.isVisible()) {
				y+= VGAP;
				fTask.setBounds(MARGIN+indent, y, iw-indent, e4.y);
				y+= e4.y;
			}
			
			fActionBar.setBounds(e.x-MARGIN-e5.x, (e.y-e5.y)/2, e5.x, e5.y);
		}
		
		public Point computeSize(int wHint, int hHint, boolean changed) {
			
			int w, h;
			
			if (changed || fCachedHeight <= 0 || fCachedWidth <= 0) {
				Point e1= fIcon.computeSize(SWT.DEFAULT, SWT.DEFAULT); e1.x= MIN_ICON_SIZE;
				Point e2= fName.computeSize(SWT.DEFAULT, SWT.DEFAULT);
				Point e3= fProgressBar.computeSize(SWT.DEFAULT, SWT.DEFAULT); e3.y= MAX_PROGRESS_HEIGHT;
				Point e4= fTask.computeSize(SWT.DEFAULT, SWT.DEFAULT);
				Point e5= fActionBar.computeSize(SWT.DEFAULT, SWT.DEFAULT);
				
				fCachedWidth= MARGIN + e1.x + HGAP + 100 + MARGIN;
					
				fCachedHeight= MARGIN + Math.max(e1.y, e2.y);
				if (fProgressBar.isVisible())
					fCachedHeight+= VGAP + e3.y;
				if (fTask.isVisible())
					fCachedHeight+= VGAP + e4.y;
				fCachedHeight+= MARGIN;
			}
			
			w= wHint == SWT.DEFAULT ? fCachedWidth : wHint;
			h= hHint == SWT.DEFAULT ? fCachedHeight : hHint;
			
			return new Point(w, h);
		}
		
		/*
		 * Update the background colors.
		 */
		void updateBackground(boolean dark) {
			Color c;
			if (fSelected)
				c= fgSelected;				
			else
				c= dark ? fgDark : fgWhite;
			setBackground(c);				
			fIcon.setBackground(c);	
			fName.setBackground(c);
			fTask.setBackground(c);
			fActionBar.setBackground(c);
			fProgressBar.setBackground(c);
		}
		
		/*
		 * Sets the progress.
		 */
		void worked(int delta) {
			if (!fProgressBar.isDisposed()) {
				if (! fInitialized) {
					if (delta < 0)
						fProgressBar.beginAnimatedTask();
					else
						fProgressBar.beginTask(100);
					fInitialized= true;
				}
				if (delta > 0)
					fProgressBar.worked(delta);
			}
		}
		
		/*
		 * Sets the task message.
		 */
		void setTask(String message) {
			if (!fTask.isDisposed())
				fTask.setText(message);
		}

		/*
		 * Sets the terminated status.
		 */
		void setTerminatedStatus(String message) {
			fFinished= true;
			if (!fTask.isDisposed()) {
				//fTask.setForeground(fgBlack);
				//fTask.setFont(fgSmallBoldFont);
				if (message != null)
					fTask.setText(message);
			}
			if (!fProgressBar.isDisposed())
				fProgressBar.setVisible(false);
			if (!fActionButton.isDisposed()) {
				fActionButton.setImage(getImage(fActionBar.getDisplay(), "remove.gif"));
				fActionButton.setToolTipText("Remove Job");
			}
			relayout();
		}

		/*
		 * Update the visual item from the model.
		 */
		void refresh() {
			
			if (fModel.isTerminated()) {
				String message= null;
				Job job= fModel.getJob();
				if (job != null) {
					IStatus result= job.getResult();
					if (result != null) {
						String m= result.getMessage();
						if (m != null && m.trim().length() > 0)
							message= m;
					}
				}
				setTerminatedStatus(message);
				return;
			}
			
			int pd= fModel.getPercentDone();
			if (pd > 0)
				worked(pd);
			setTask(fModel.getTaskName());
		}
	}
	
	protected Composite fContent;
	private ScrolledComposite fScrolledComposite;

	/**
	 * The constructor.
	 */
	public JobView() {
	}
	
	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {
		
		if (fgWhite == null) {
			boolean carbon= "carbon".equals(SWT.getPlatform());
			Display display= parent.getDisplay();
			fgWhite= display.getSystemColor(SWT.COLOR_WHITE);
			if (carbon)
				fgDark= new Color(display, 230, 230, 230);
			else
				fgDark= new Color(display, 245, 245, 245);
			fgTask= new Color(display, 120, 120, 120);
			fgSelected= display.getSystemColor(SWT.COLOR_LIST_SELECTION);
			fgBlack= display.getSystemColor(SWT.COLOR_BLACK);
			fgLink= display.getSystemColor(SWT.COLOR_DARK_BLUE);
			fgLink2= display.getSystemColor(SWT.COLOR_BLUE);
			
			Font f= parent.getFont();
			FontData fd= f.getFontData()[0];
			if (carbon)
				fd.setHeight(fd.getHeight()-1);
			fgFont= new Font(display, fd);
			fd.setHeight(fd.getHeight()-1);
			fgSmallFont= new Font(display, fd);
			fd.setStyle(fd.getStyle() | SWT.BOLD);
			fgSmallBoldFont= new Font(display, fd);
		}

		fScrolledComposite= new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		fScrolledComposite.setExpandHorizontal(true);
		fScrolledComposite.setExpandVertical(true);
				
		fContent= new Composite(fScrolledComposite, SWT.NONE);
		fContent.setBackground(fgWhite);
		
		fScrolledComposite.setContent(fContent);
		
		GridLayout layout= new GridLayout();
		layout.numColumns= 1;
		layout.marginHeight= layout.marginWidth= layout.verticalSpacing= 0;
		fContent.setLayout(layout);

		// register with model
		JobsModel jobsmodel= JobsModel.getJobsModel();
		jobsmodel.addListener(new IJobsModelListener() {
			public void refresh(final int changeType, final JobModel jm) {
				if (fContent != null && !fContent.isDisposed()) {
					fContent.getDisplay().asyncExec(new Runnable() {
						public void run() {
							intRefresh(changeType, jm);
						}
					});
				}				
			}
		});

		// initially sync view with model
		intRefresh(REFRESH, null);
	}
	
	private void intRefresh(int changeType, JobModel jobModel) {
		
		if (jobModel != null) {
			JobItem ji;
			switch (changeType) {
			case REFRESH:
				ji= find(jobModel);
				if (ji != null)
					ji.refresh();
				return;
			case ADD:
				new JobItem(fContent, jobModel);
				relayout();
				refreshBackgrounds();		
				return;
			case REMOVE:
				ji= find(jobModel);
				if (ji != null)
					ji.dispose();
				relayout();
				refreshBackgrounds();		
				return;
			}
		}
		
		JobsModel jobsmodel= JobsModel.getJobsModel();
		HashSet modelJobs= jobsmodel.getSet();
		HashSet shownJobs= new HashSet();
		
		// find all removed
		Control[] children= fContent.getChildren();
		for (int i= 0; i < children.length; i++) {
			JobModel jm= ((JobItem)children[i]).fModel;
			shownJobs.add(jm);
			if (!modelJobs.contains(jm))
				children[i].dispose();
		}
		
		// find all added
		JobModel[] jobs= (JobModel[]) modelJobs.toArray(new JobModel[modelJobs.size()]);
		for (int i= 0; i < jobs.length; i++) {
			JobModel jm= jobs[i];
			if (!shownJobs.contains(jm) && !jm.isStale())
				new JobItem(fContent, jm);
		}
		
		// refresh
		relayout();
		refreshBackgrounds();
	}
	
	private JobItem find(JobModel jobModel) {
		Control[] children= fContent.getChildren();
		for (int i= 0; i < children.length; i++) {
			JobItem ji= (JobItem) children[i];
			if (jobModel == ji.fModel)
				return ji;
		}
		return null;
	}
	
	public void setFocus() {
		fContent.setFocus();
	}

	protected Image getImage(Display display, String name) {
		return null;
	}
	
	/*
	 * Marks the given JobItem as selected.
	 */
	private void select(JobItem c) {
		Control[] cs= fContent.getChildren();
		for (int i= 0; i < cs.length; i++) {
			JobItem ji= (JobItem) cs[i];
			if (ji == c) {
				ji.fSelected= !ji.fSelected;
			} else {
				ji.fSelected= false;	
			}
		}		
		refreshBackgrounds();
	}
	
	/*
	 * Updates the background of all items.
	 * Ensures that the background following the last item is always white.
	 */
	private void refreshBackgrounds() {
		Control[] children= fContent.getChildren();
		boolean dark= (children.length % 2) == 1;
		for (int i= 0; i < children.length; i++) {
			JobItem ji= (JobItem) children[i];
			ji.updateBackground(dark);
			dark= !dark;
		}
	}
	
	/*
	 * Needs to be called after items have been added or removed,
	 * or after the size of an item has changed.
	 */
	private void relayout() {
		Point size= fContent.computeSize(fContent.getClientArea().x, SWT.DEFAULT);
		fContent.setSize(size);
		fScrolledComposite.setMinSize(size);		
	}

	/*
	 * Remove all terminated "sticky" jobs from the job views.
	 */
	protected void clearAll() {
		JobsModel.getJobsModel().clearAll();
	}
}
