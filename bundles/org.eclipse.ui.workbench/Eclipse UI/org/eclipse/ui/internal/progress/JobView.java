package org.eclipse.ui.internal.progress;

import java.net.URL;
import java.util.HashSet;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.internal.misc.Assert;
import org.eclipse.ui.part.*;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;


public class JobView extends ViewPart {
	
	static final String PROPERTY_PREFIX= "org.eclipse.ui.workbench.progress"; //$NON-NLS-1$

	/* an property of type URL that specifies the icon to use for this job. */
	static final String PROPERTY_ICON= "icon"; //$NON-NLS-1$
	/* this Boolean property controls whether a finished job is kept in the list. */
	static final String PROPERTY_KEEP= "keep"; //$NON-NLS-1$
	/* an property of type IAction that is run when link is activated. */
	static final String PROPERTY_GOTO= "goto"; //$NON-NLS-1$

	
	/*
	 * Label with hyperlink capability.
	 */
	class Hyperlink extends Canvas implements Listener {
		final static int MARGINWIDTH = 1;
		final static int MARGINHEIGHT = 1;
		boolean hasFocus;
		String text;
		boolean underlined;
		IAction gotoAction;
		
		Hyperlink(Composite parent, int flags) {
			super(parent, SWT.NO_BACKGROUND | flags);
			addListener(SWT.KeyDown, this);
			addListener(SWT.Paint, this);
			addListener(SWT.MouseEnter, this);
			addListener(SWT.MouseExit, this);
			addListener(SWT.MouseUp, this);
			addListener(SWT.FocusIn, this);
			addListener(SWT.FocusOut, this);
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
				if (underlined) {
					setForeground(fLink2Color);
					redraw();
				}
				break;
			case SWT.FocusOut :
				hasFocus = false;
			case SWT.MouseExit :
				if (underlined) {
					setForeground(fLinkColor);
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
		void setText(String t) {
			text= t != null ? t : ""; //$NON-NLS-1$
			redraw();
		}
		void setAction(IAction action) {
			gotoAction= action;
			underlined= action != null;
			setForeground(underlined ? fLinkColor : fTaskColor);
			if (underlined)
				setCursor(fHandCursor);
			redraw();
		}
		public Point computeSize(int wHint, int hHint, boolean changed) {
			checkWidget();
			int innerWidth= wHint;
			if (innerWidth != SWT.DEFAULT)
				innerWidth -= MARGINWIDTH * 2;
			GC gc= new GC(this);
			gc.setFont(getFont());
			Point extent= gc.textExtent(text);
			gc.dispose();
			return new Point(extent.x + 2 * MARGINWIDTH, extent.y + 2 * MARGINHEIGHT);
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
			bufferGC.drawText(text, MARGINWIDTH, MARGINHEIGHT, true);
			int sw= bufferGC.stringExtent(text).x;
			if (underlined) {
				FontMetrics fm= bufferGC.getFontMetrics();
				int lineY= clientArea.height - MARGINHEIGHT - fm.getDescent() + 1;
				bufferGC.drawLine(MARGINWIDTH, lineY, MARGINWIDTH + sw, lineY);
			}
			if (hasFocus)
				bufferGC.drawFocus(0, 0, sw, clientArea.height);
			gc.drawImage(buffer, 0, 0);
			bufferGC.dispose();
			buffer.dispose();
		}
		protected void handleActivate() {
			if (underlined && gotoAction != null && gotoAction.isEnabled())
				gotoAction.run();
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

		private JobTreeElement fInfo;
		boolean fKeep;
		boolean fTerminated;
		boolean fSelected;
		boolean fProgressIsShown;
		boolean fTaskIsShown;

		int fCachedWidth= -1;
		int fCachedHeight= -1;
		Label fIcon;
		Label fName;
		ProgressBar fProgressBar;
		Hyperlink fTask;
		ToolBar fActionBar;
		ToolItem fActionButton;
		ToolItem fGotoButton;
		
		/////////////////////////////////////////////////
		

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
		
		/////////////////////////////////////////////////


		JobItem(Composite parent, JobTreeElement info) {
			super(parent, SWT.NONE);
			
			Assert.isNotNull(info);
			fInfo= info;
			
			Display display= getDisplay();

			Job job= getJob();
			String jobName= null;
			IAction gotoAction= null;	
			Image image= null;
			if (job != null) {
				jobName= job.getName();
				Object property= job.getProperty(new QualifiedName(PROPERTY_PREFIX, PROPERTY_KEEP));
				if (property instanceof Boolean)
					fKeep= ((Boolean)property).booleanValue();
				property= job.getProperty(new QualifiedName(PROPERTY_PREFIX, PROPERTY_GOTO));
				if (property instanceof IAction)
					gotoAction= (IAction) property;
				property= job.getProperty(new QualifiedName(PROPERTY_PREFIX, PROPERTY_ICON));
				if (property instanceof URL) {
					URL url= (URL) property;
					ImageDescriptor id= ImageDescriptor.createFromURL(url);
					image= id.createImage(display);
				}
			}
			
			MouseListener ml= new MouseAdapter() {
				public void mouseDown(MouseEvent e) {
//					select(JobItem.this);
				}
			};
			
			setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

			fIcon= new Label(this, SWT.NONE);
			if (image != null)
				fIcon.setImage(image);				
			fIcon.addMouseListener(ml);
			
			if (jobName == null)
				jobName= "???" + fInfo.getCondensedDisplayString();
			fName= new Label(this, SWT.NONE);
			fName.setFont(fFont);
			fName.setText(jobName);
			fName.addMouseListener(ml);
			
			fActionBar= new ToolBar(this, SWT.FLAT);
						
			if (false && gotoAction != null) {
				final IAction gotoAction2= gotoAction;
				fGotoButton= new ToolItem(fActionBar, SWT.NONE);
				fGotoButton.setImage(getImage(parent.getDisplay(), "newprogress_goto.gif")); //$NON-NLS-1$
				fGotoButton.setToolTipText("Show Results");
				fGotoButton.setEnabled(gotoAction.isEnabled());
				fGotoButton.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						if (gotoAction2.isEnabled()) {
							gotoAction2.run();
							if (fTerminated)
								kill(true);
						}
					}
				});
			}

			fActionButton= new ToolItem(fActionBar, SWT.NONE);
			fActionButton.setImage(getImage(parent.getDisplay(), "newprogress_cancel.gif")); //$NON-NLS-1$
			fActionButton.setToolTipText("Cancel Job");
			fActionButton.setEnabled(true | fInfo.isCancellable());
			fActionButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					if (fTerminated) {
						kill(true);
					} else {
						fActionButton.setEnabled(false);
						fInfo.cancel();
						setTask("cancelled");
					}
				}
			});
			
			fActionBar.pack();

			fProgressIsShown= true;
			if (false) {
				fProgressBar= new ProgressBar(this, SWT.HORIZONTAL);
				fProgressBar.setMaximum(100);
				fProgressBar.addMouseListener(ml);
			}
			
 			fTask= new Hyperlink(this, SWT.NONE);
 			if (gotoAction != null) {
 				fTask.setToolTipText(gotoAction.getToolTipText());
 				fTask.setAction(gotoAction);
 			}
			fTask.setFont(fSmallFont);
			fTask.setText(getTaskName());
			fTaskIsShown= true;
			//fTask.addMouseListener(ml);
			
			addMouseListener(ml);
			
			addControlListener(new ControlAdapter() {
				public void controlResized(ControlEvent e) {
					handleResize();
				}
			});
		}
		
		boolean remove() {
			fTerminated= true;
			if (fKeep) {
				if (fProgressBar != null && !fProgressBar.isDisposed()) {
					fProgressIsShown= false;
					fProgressBar.setVisible(false);
				}
				if (!fActionButton.isDisposed()) {
					fActionButton.setImage(getImage(fActionBar.getDisplay(), "newprogress_clear.gif")); //$NON-NLS-1$
					fActionButton.setToolTipText("Remove Job");
					fActionButton.setEnabled(true);
				}
				return false;
			} else {
				dispose();
				return true;				
			}
		}
		
		boolean kill(boolean refresh) {
			if (fTerminated) {
				dispose();
				if (refresh) {
					relayout();
					refreshBackgrounds();
				}
				return true;
			}
			return false;
		}
		
		void handleResize() {
			Point e= getSize();
			Point e1= fIcon.computeSize(SWT.DEFAULT, SWT.DEFAULT); e1.x= MIN_ICON_SIZE;
			Point e2= fName.computeSize(SWT.DEFAULT, SWT.DEFAULT);
			Point e4= fTask.computeSize(SWT.DEFAULT, SWT.DEFAULT);
			Point e5= fActionBar.computeSize(SWT.DEFAULT, SWT.DEFAULT);
			
			int iw= e.x-MARGIN-HGAP-e5.x-MARGIN;
			int indent= 16+HGAP;
				
			int y= MARGIN;
			int h= Math.max(e1.y, e2.y);
			fIcon.setBounds(MARGIN, y+(h-e1.y)/2, e1.x, e1.y);
			fName.setBounds(MARGIN+e1.x+HGAP, y+(h-e2.y)/2, iw-e1.x-HGAP, e2.y);
			y+= h;
			if (fProgressIsShown && fProgressBar != null /* fProgressBar.isVisible() */) {
				Point e3= fProgressBar.computeSize(SWT.DEFAULT, SWT.DEFAULT); e3.y= MAX_PROGRESS_HEIGHT;
				y+= VGAP;
				fProgressBar.setBounds(MARGIN+indent, y, iw-indent, e3.y);
				y+= e3.y;
			}
			if (fTaskIsShown /* fTask.isVisible() */) {
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
				Point e4= fTask.computeSize(SWT.DEFAULT, SWT.DEFAULT);
//				Point e5= fActionBar.computeSize(SWT.DEFAULT, SWT.DEFAULT);
				
				fCachedWidth= MARGIN + e1.x + HGAP + 100 + MARGIN;
					
				fCachedHeight= MARGIN + Math.max(e1.y, e2.y);
				if (fProgressIsShown && fProgressBar != null/* fProgressBar.isVisible() */) {
					Point e3= fProgressBar.computeSize(SWT.DEFAULT, SWT.DEFAULT); e3.y= MAX_PROGRESS_HEIGHT;
					fCachedHeight+= VGAP + e3.y;
				}
				if (fTaskIsShown /* fTask.isVisible() */)
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
				c= fSelectedColor;				
			else
				c= dark ? fDarkColor : fWhiteColor;
			setBackground(c);				
			fIcon.setBackground(c);	
			fName.setBackground(c);
			fTask.setBackground(c);
			fActionBar.setBackground(c);
		}
		
		/*
		 * Sets the progress.
		 */
		void setPercentDone(int percentDone) {
			if (percentDone >= 0 && percentDone < 100) {
				if (fProgressBar == null) {
					fProgressBar= new ProgressBar(this, SWT.HORIZONTAL);
					fProgressBar.setMaximum(100);
					fProgressBar.setSelection(percentDone);
					relayout();
				} else
					fProgressBar.setSelection(percentDone);
			}
		}
		
		/*
		 * Sets the task message.
		 */
		void setTask(String message) {
			if (fTask != null && !fTask.isDisposed())
				fTask.setText(message);
		}

		/*
		 * Update the visual item from the model.
		 */
		void refresh() {
			
			if (fKeep && fTerminated) {
				String message= null;
				Job job= getJob();
				if (job != null) {
					IStatus result= job.getResult();
					if (result != null) {
						String m= result.getMessage();
						if (m != null && m.trim().length() > 0)
							message= m;
					}
				}
				setTask(message);
				return;
			}
			
			if (fInfo instanceof JobInfo) {
				TaskInfo ti= ((JobInfo)fInfo).getTaskInfo();
				if (ti != null)
					setPercentDone(ti.getPercentDone());
			}
			
			setTask(getTaskName());
		}
	}
	
	private Color fLinkColor;
	private Color fLink2Color;
	private Color fDarkColor;
	private Color fWhiteColor;
	private Color fTaskColor;
	private Color fSelectedColor;
	private Font fFont;
	private Font fSmallFont;
	private Cursor fHandCursor;


	private Composite fContent;
	private ScrolledComposite fScrolledComposite;
	private IAction clearAllAction;
	private IAction verboseAction;
	

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
		
		final IProgressUpdateCollector puc= new IProgressUpdateCollector() {
			public void refresh(Object[] elements) {
				for (int i= 0; i < elements.length; i++) {
					JobItem ji= find(((JobTreeElement) elements[i]));
					if (ji != null)
						ji.refresh();
				}
			}
			public void add(Object[] elements) {
				add2(elements);
			}
			public void remove(Object[] elements) {
				remove2(elements);
			}
			public void refresh() {
				refreshAll2();
			}
		};
		
		final ProgressViewUpdater pvu= ProgressViewUpdater.getSingleton();
		pvu.addCollector(puc);
		
		Display display= parent.getDisplay();
		fHandCursor= new Cursor(display, SWT.CURSOR_HAND);

		boolean carbon= "carbon".equals(SWT.getPlatform()); //$NON-NLS-1$
		fWhiteColor= display.getSystemColor(SWT.COLOR_WHITE);
		if (carbon)
			fDarkColor= new Color(display, 230, 230, 230);
		else
			fDarkColor= new Color(display, 245, 245, 245);
		fTaskColor= new Color(display, 120, 120, 120);
		fSelectedColor= display.getSystemColor(SWT.COLOR_LIST_SELECTION);
		fLinkColor= display.getSystemColor(SWT.COLOR_DARK_BLUE);
		fLink2Color= display.getSystemColor(SWT.COLOR_BLUE);
		
		Font f= parent.getFont();
		FontData fd= f.getFontData()[0];
		if (carbon)
			fd.setHeight(fd.getHeight()-1);
		fFont= new Font(display, fd);
		fd.setHeight(fd.getHeight()-1);
		fSmallFont= new Font(display, fd);
		fd.setStyle(fd.getStyle() | SWT.BOLD);
		

		fScrolledComposite= new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		fScrolledComposite.setExpandHorizontal(true);
		fScrolledComposite.setExpandVertical(true);
				
		fContent= new Composite(fScrolledComposite, SWT.NONE);
		fContent.setBackground(fWhiteColor);
		
		fScrolledComposite.setContent(fContent);
		
		GridLayout layout= new GridLayout();
		layout.numColumns= 1;
		layout.marginHeight= layout.marginWidth= layout.verticalSpacing= 0;
		fContent.setLayout(layout);
		
		fContent.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				pvu.removeCollector(puc);				
			}
		});
		
		refreshAll2();

		// build the actions
		clearAllAction= new Action() {
			public void run() {
				clearAll();
			}
		};
		clearAllAction.setText(ProgressMessages.getString("ProgressView.ClearAllAction")); //$NON-NLS-1$	
		ImageDescriptor id= ImageDescriptor.createFromFile(JobView.class, "newprogress_clearall.gif"); //$NON-NLS-1$
		if (id != null)
			clearAllAction.setImageDescriptor(id);
		
		verboseAction= new Action(ProgressMessages.getString("ProgressView.VerboseAction"), //$NON-NLS-1$
							IAction.AS_CHECK_BOX) {
			public void run() {
				/*
				ProgressViewUpdater updater = ProgressViewUpdater.getSingleton();
				updater.debug = !updater.debug;
				setChecked(updater.debug);
				updater.refreshAll();
				*/
			}
		};

		IActionBars bars= getViewSite().getActionBars();
		IMenuManager mm= bars.getMenuManager();
		mm.add(clearAllAction);
		mm.add(verboseAction);
		
		IToolBarManager tm= bars.getToolBarManager();
		tm.add(clearAllAction);
	}
	
	void clearAll() {
		Control[] children= fContent.getChildren();
		boolean changed= false;
		for (int i= 0; i < children.length; i++)
			changed |= ((JobItem)children[i]).kill(false);
		if (changed) {
			relayout();
			refreshBackgrounds();
		}
	}
	
	private void add2(Object[] elements) {
		for (int i= 0; i < elements.length; i++) {
			JobTreeElement jte= (JobTreeElement) elements[i];
			new JobItem(fContent, jte);
		}
		relayout();
		refreshBackgrounds();		
	}
	
	private void remove2(Object[] elements) {
		boolean changed= false;
		for (int i= 0; i < elements.length; i++) {
			JobTreeElement jte= (JobTreeElement) elements[i];
			JobItem ji= find(jte);
			if (ji != null)
				changed |= ji.remove();
		}
		if (changed) {
			relayout();
			refreshBackgrounds();
		}
	}
	
	private void refreshAll2() {
		JobTreeElement[] roots= ProgressManager.getInstance().getRootElements(ProgressViewUpdater.getSingleton().debug);
		
		HashSet modelJobs= new HashSet();
		for (int z= 0; z < roots.length; z++)
			modelJobs.add(roots[z]);
		
		HashSet shownJobs= new HashSet();
		
		// find all removed
		Control[] children= fContent.getChildren();
		for (int i= 0; i < children.length; i++) {
			JobItem ji= (JobItem)children[i];
			JobTreeElement jte= ji.fInfo;
			shownJobs.add(jte);
			if (!modelJobs.contains(jte))
				ji.remove();
		}
		
		// find all added
		for (int i= 0; i < roots.length; i++) {
			JobTreeElement jte= roots[i];
			if (!shownJobs.contains(jte))
				new JobItem(fContent, jte);
		}
		
		// refresh
		relayout();
		refreshBackgrounds();
	}
	
	private JobItem find(JobTreeElement jte) {
		Control[] children= fContent.getChildren();
		for (int i= 0; i < children.length; i++) {
			JobItem ji= (JobItem) children[i];
			if (jte == ji.fInfo)
				return ji;
		}
		return null;
	}
	
	public void setFocus() {
		if (fContent != null && !fContent.isDisposed())
			fContent.setFocus();
	}
	
	private Image getImage(Display display, String name) {
		ImageDescriptor id= ImageDescriptor.createFromFile(JobView.class, name);
		if (id != null)
			return id.createImage(display);
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
}
