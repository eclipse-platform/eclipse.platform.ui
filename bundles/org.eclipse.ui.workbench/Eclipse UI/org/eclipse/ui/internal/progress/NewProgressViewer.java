package org.eclipse.ui.internal.progress;

import java.net.URL;
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
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TreeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
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
import org.eclipse.ui.internal.misc.Assert;

public class NewProgressViewer extends ProgressTreeViewer implements FinishedJobs.KeptJobsListener {
	
	static final boolean DEBUG= false;
    
	static final String PROPERTY_PREFIX= "org.eclipse.ui.workbench.progress"; //$NON-NLS-1$

	/* an property of type URL that specifies the icon to use for this job. */
	static final String PROPERTY_ICON= "icon"; //$NON-NLS-1$
	/* this Boolean property controls whether a finished job is kept in the list. */
	static final String PROPERTY_KEEP= "keep"; //$NON-NLS-1$
	/* an property of type IAction that is run when link is activated. */
	static final String PROPERTY_GOTO= "goto"; //$NON-NLS-1$

	private static String ELLIPSIS = ProgressMessages.getString("ProgressFloatingWindow.EllipsisValue"); //$NON-NLS-1$

	static final QualifiedName KEEP_PROPERTY= new QualifiedName(PROPERTY_PREFIX, PROPERTY_KEEP);
	static final QualifiedName ICON_PROPERTY= new QualifiedName(PROPERTY_PREFIX, PROPERTY_ICON);
	static final QualifiedName GOTO_PROPERTY= new QualifiedName(PROPERTY_PREFIX, PROPERTY_GOTO);
	
	private Composite list;
	private ScrolledComposite scroller;
	private Color linkColor;
	private Color linkColor2;
	private Color errorColor;
	private Color errorColor2;
	private Color darkColor;
	private Color lightColor;
	private Color taskColor;
	private Color selectedColor;
	private Cursor handCursor;
	private Cursor normalCursor;
	private Font defaultFont= JFaceResources.getDefaultFont();
	private HashMap map= new HashMap();

	
	class ListLayout extends Layout {
		boolean refreshBackgrounds;
		
		protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {		
			int w= 0, h= 0;
			Control[] cs= composite.getChildren();
			for (int i= 0; i < cs.length; i++) {
				Control c= cs[i];
				Point e= c.computeSize(SWT.DEFAULT, SWT.DEFAULT, flushCache);
				w= Math.max(w, e.x);
				h+= e.y;
			}
			return new Point(w, h);
		}
		
		protected void layout(Composite composite, boolean flushCache) {
			int x= 0, y= 0;
			Point e= composite.getSize();
			Control[] cs= composite.getChildren();
			// sort
			ViewerSorter vs= getSorter();
			if (vs != null) {
				JobTreeElement[] elements= new JobTreeElement[cs.length];
				for (int i= 0; i < cs.length; i++)
					elements[i]= ((JobItem)cs[i]).jobTreeElement;
				vs.sort(NewProgressViewer.this, elements);
				for (int i= 0; i < cs.length; i++)
					cs[i]= findJobItem(elements[i], false);
			}
			// sort
			boolean dark= (cs.length % 2) == 1;
			for (int i= 0; i < cs.length; i++) {
				Control c= cs[i];
				Point s= c.computeSize(e.x, SWT.DEFAULT, flushCache);
				c.setBounds(x, y, s.x, s.y);
				y+= s.y;
				if (refreshBackgrounds && c instanceof JobItem) {
					((JobItem)c).updateBackground(dark);
					dark= !dark;
				}
			}
		}
	}
	
	abstract class JobTreeItem extends Canvas implements Listener {
		JobTreeElement jobTreeElement;
		boolean keepItem;
		
		JobTreeItem(Composite parent, JobTreeElement info, int flags) {
			super(parent, flags);
			jobTreeElement= info;
			map.put(jobTreeElement, this);
			addListener(SWT.Dispose, this);
		}

		void init(JobTreeElement info) {
			map.remove(jobTreeElement);
			jobTreeElement= info;
			map.put(jobTreeElement, this);
			refresh();
		}
		
		public void handleEvent(Event e) {
			switch (e.type) {
			case SWT.Dispose:
				map.remove(jobTreeElement);
				break;
			}
		}
		
		Job getJob() {
			if (jobTreeElement instanceof JobInfo)
				return ((JobInfo)jobTreeElement).getJob();
			if (jobTreeElement instanceof SubTaskInfo)
			    return ((SubTaskInfo)jobTreeElement).jobInfo.getJob();
			return null;
		}

		public boolean kill(boolean refresh, boolean broadcast) {
			return true;
		}
		
		boolean checkKeep() {
			if (jobTreeElement instanceof JobInfo) {
				Job job= getJob();
				if (job != null) {
					Object property= job.getProperty(KEEP_PROPERTY);
					if (property instanceof Boolean && ((Boolean)property).booleanValue()) {
						keepItem= true;
						Composite parent= getParent();
						if (parent instanceof JobTreeItem)
						    ((JobTreeItem)parent).keepItem= true;
					}
				}
			}
			return keepItem;			
		}
		
		Image checkIcon() {
			Job job= getJob();
			if (job != null) {
				Display display= getDisplay();
				Object property= job.getProperty(ICON_PROPERTY);
				if (property instanceof ImageDescriptor) {
					ImageDescriptor id= (ImageDescriptor) property;
					return id.createImage(display);
				}
				if (property instanceof URL) {
					URL url= (URL) property;
					ImageDescriptor id= ImageDescriptor.createFromURL(url);
					return id.createImage(display);
				}
			}
			return null;
		}
		
		abstract boolean refresh();
		
		public boolean remove() {
			if (!keepItem) {
				dispose();
				return true;
			}
			return false;
		}
	}
	
	/*
	 * Label with hyperlink capability.
	 */
	class Hyperlink extends JobTreeItem implements Listener {
		final static int MARGINWIDTH = 1;
		final static int MARGINHEIGHT = 1;
		
		boolean hasFocus;
		String text= ""; //$NON-NLS-1$
		boolean underlined;
		IAction gotoAction;
		IStatus result;
		Color lColor= linkColor;
		Color lColor2= linkColor2;
		boolean foundImage;
		JobItem jobitem;
		
		Hyperlink(JobItem parent, JobTreeElement info) {
			super(parent, info, SWT.NO_BACKGROUND);
			
			jobitem= parent;
			
 			setFont(defaultFont);
			
			addListener(SWT.KeyDown, this);
			addListener(SWT.Paint, this);
			addListener(SWT.MouseEnter, this);
			addListener(SWT.MouseExit, this);
			addListener(SWT.MouseUp, this);
			addListener(SWT.FocusIn, this);
			addListener(SWT.FocusOut, this);
			
 			refresh();
		}
		public void handleEvent(Event e) {
			super.handleEvent(e);
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
					setForeground(lColor2);
					redraw();
				}
				break;
			case SWT.FocusOut :
				hasFocus = false;
			case SWT.MouseExit :
				if (underlined) {
					setForeground(lColor);
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
		void setStatus(IStatus r) {
			result= r;
	    	if (result != null) {
	    		String message= result.getMessage().trim();
	    		if (message.length() > 0) {
	    			if (r.getSeverity() == IStatus.ERROR) {
	    				lColor= errorColor;
	    				lColor2= errorColor2;
		    			setText("Error: " + message);
		    			setAction(new Action() {
		    				public void run() {
		    					ErrorDialog.openError(getShell(), "Title", "Error", result);
		    				}
		    			});
	    			} else {
		    			setText(message);
	    			}
	    		}
	    	}
		}
		private void setText(String t) {
			if (t == null)
				t= "";	//$NON-NLS-1$
			else
				t= shortenText(this, t);
			if (!t.equals(text)) {
				text= t;
				redraw();
			}
		}
		void setAction(IAction action) {
			gotoAction= action;
			underlined= action != null;
			setForeground(underlined ? lColor : taskColor);
			if (underlined)
				setCursor(handCursor);
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
			String t= shortenText(bufferGC, clientArea.height, text);
			bufferGC.drawText(t, MARGINWIDTH, MARGINHEIGHT, true);
			int sw= bufferGC.stringExtent(t).x;
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
		public boolean refresh() {
			checkKeep();
			
			// check for icon property and propagate to parent
			if (jobitem.image == null) {
				Image image= checkIcon();
				if (image != null)
					jobitem.setImage(image);
			}
			
			setText(jobTreeElement.getDisplayString());
			
			Job job= getJob();
			if (job != null) {
		    	Object property= job.getProperty(GOTO_PROPERTY);
		    	if (property instanceof IAction && property != gotoAction)
		    	    setAction((IAction) property);
		    	
		    	IStatus status= job.getResult();
		    	if (status != null)
		    		setStatus(status);
			}
			
			return false;
		}
	}

	/*
	 * An SWT widget representing a JobModel
	 */
	class JobItem extends JobTreeItem {
		
		static final int MARGIN= 2;
		static final int HGAP= 7;
		static final int VGAP= 2;
		static final int MAX_PROGRESS_HEIGHT= 12;
		static final int MIN_ICON_SIZE= 16;

		boolean jobTerminated;
		boolean selected;
		//IAction gotoAction;	
		int cachedWidth= -1;
		int cachedHeight= -1;

		Image image;
		Label nameItem, iconItem;
		ProgressBar progressBar;
		ToolBar actionBar;
		ToolItem actionButton;
		ToolItem gotoButton;
		

		JobItem(Composite parent, JobTreeElement info) {
			super(parent, info, SWT.NONE);
				
			Assert.isNotNull(info);
						
			Display display= getDisplay();

			Job job= getJob();
			if (image != null) {
				if (job != null) {
					Object property= job.getProperty(ICON_PROPERTY);
					if (property instanceof ImageDescriptor) {
						ImageDescriptor id= (ImageDescriptor) property;
						image= id.createImage(display);
					} else if (property instanceof URL) {
						URL url= (URL) property;
						ImageDescriptor id= ImageDescriptor.createFromURL(url);
						image= id.createImage(display);
					}
				}
			}
			
			MouseListener ml= new MouseAdapter() {
				public void mouseDown(MouseEvent e) {
//					select(JobItem.this);	// no selections yet
				}
			};
			
			iconItem= new Label(this, SWT.NONE);
			iconItem.addMouseListener(ml);
			
			nameItem= new Label(this, SWT.NONE);
			nameItem.setFont(defaultFont);
			nameItem.addMouseListener(ml);
			
			actionBar= new ToolBar(this, SWT.FLAT);
			actionBar.setCursor(normalCursor);	// set cursor to overwrite any busy cursor we might have
			actionButton= new ToolItem(actionBar, SWT.NONE);
			actionButton.setImage(getImage(parent.getDisplay(), "newprogress_cancel.gif")); //$NON-NLS-1$
			actionButton.setToolTipText(ProgressMessages.getString("NewProgressView.CancelJobToolTip")); //$NON-NLS-1$
			actionButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					actionButton.setEnabled(false);
					if (jobTerminated)
						kill(true, true);
					else
						jobTreeElement.cancel();
				}
			});
			
			addMouseListener(ml);
			
			addControlListener(new ControlAdapter() {
				public void controlResized(ControlEvent e) {
					handleResize();
				}
			});
			
			refresh();
		}

		public void handleEvent(Event event) {
	        super.handleEvent(event);
	        if (event.type == SWT.Dispose) {
	        	if (image != null && !image.isDisposed()) {
	        		image.dispose();
	        		image= null;
	        	}
	    	}
	    }

		void setImage(Image im) {
			if (im != null && iconItem != null) {
				image= im;
				iconItem.setImage(im);
			}
		}
		
		public boolean remove() {
			jobTerminated= true;
			if (keepItem) {
				boolean changed= false;
				if (progressBar != null && !progressBar.isDisposed()) {
					progressBar.dispose();
					changed= true;
				}
				if (!actionButton.isDisposed()) {
					actionButton.setImage(getImage(actionBar.getDisplay(), "newprogress_clear.gif")); //$NON-NLS-1$
					actionButton.setToolTipText(ProgressMessages.getString("NewProgressView.RemoveJobToolTip")); //$NON-NLS-1$
					actionButton.setEnabled(true);
				}
				
				changed |= refresh();

				IStatus result= getResult();
				if (result != null) {
					Control[] c= getChildren();
					for (int i= 0; i < c.length; i++) {
						if (c[i] instanceof Hyperlink) {
							Hyperlink hl= (Hyperlink) c[i];
							hl.setStatus(result);
							break;
						}
					}
				} else {
					Control[] c= getChildren();
					for (int i= 0; i < c.length; i++) {
						if (c[i] instanceof Hyperlink) {
							Hyperlink hl= (Hyperlink) c[i];
							hl.refresh();
						}
					}	
				}

				return changed;
			}
			dispose();
			return true;	
		}
		
		public boolean kill(boolean refresh, boolean broadcast) {
			if (jobTerminated) {
				
				if (broadcast && jobTreeElement instanceof JobInfo)
				    FinishedJobs.getInstance().remove(NewProgressViewer.this, (JobInfo)jobTreeElement);
				
				dispose();
				relayout(refresh, refresh);
				return true;
			}
			return false;
		}
		
		void handleResize() {
			Point e= getSize();
			Point e1= iconItem.computeSize(SWT.DEFAULT, SWT.DEFAULT); e1.x= MIN_ICON_SIZE;
			Point e2= nameItem.computeSize(SWT.DEFAULT, SWT.DEFAULT);
			Point e5= actionBar.computeSize(SWT.DEFAULT, SWT.DEFAULT);
			
			int iw= e.x-MARGIN-HGAP-e5.x-MARGIN;
			int indent= 16+HGAP;
				
			int y= MARGIN;
			int h= Math.max(e1.y, e2.y);
			iconItem.setBounds(MARGIN, y+(h-e1.y)/2, e1.x, e1.y);
			nameItem.setBounds(MARGIN+e1.x+HGAP, y+(h-e2.y)/2, iw-e1.x-HGAP, e2.y);
			y+= h;
			if (progressBar != null && !progressBar.isDisposed()) {
				Point e3= progressBar.computeSize(SWT.DEFAULT, SWT.DEFAULT); e3.y= MAX_PROGRESS_HEIGHT;
				y+= VGAP;
				progressBar.setBounds(MARGIN+indent, y, iw-indent, e3.y);
				y+= e3.y;
			}
			Control[] cs= getChildren();
			for (int i= 0; i < cs.length; i++) {
				if (cs[i] instanceof Hyperlink) {
					Point e4= cs[i].computeSize(SWT.DEFAULT, SWT.DEFAULT);
					y+= VGAP;
					cs[i].setBounds(MARGIN+indent, y, iw-indent, e4.y);
					y+= e4.y;
				}
			}
			
			actionBar.setBounds(e.x-MARGIN-e5.x, (e.y-e5.y)/2, e5.x, e5.y);
		}
		
		public Point computeSize(int wHint, int hHint, boolean changed) {
			
			if (changed || cachedHeight <= 0 || cachedWidth <= 0) {
				Point e1= iconItem.computeSize(SWT.DEFAULT, SWT.DEFAULT); e1.x= MIN_ICON_SIZE;
				Point e2= nameItem.computeSize(SWT.DEFAULT, SWT.DEFAULT);
				
				cachedWidth= MARGIN + e1.x + HGAP + 100 + MARGIN;
					
				cachedHeight= MARGIN + Math.max(e1.y, e2.y);
				if (progressBar != null && !progressBar.isDisposed()) {
					Point e3= progressBar.computeSize(SWT.DEFAULT, SWT.DEFAULT); e3.y= MAX_PROGRESS_HEIGHT;
					cachedHeight+= VGAP + e3.y;
				}
				Control[] cs= getChildren();
				for (int i= 0; i < cs.length; i++) {
					if (cs[i] instanceof Hyperlink) {
						Point e4= cs[i].computeSize(SWT.DEFAULT, SWT.DEFAULT);
						cachedHeight+= VGAP + e4.y;
					}
				}
				cachedHeight+= MARGIN;
			}
			
			int w= wHint == SWT.DEFAULT ? cachedWidth : wHint;
			int h= hHint == SWT.DEFAULT ? cachedHeight : hHint;
			
			return new Point(w, h);
		}
		
		/*
		 * Update the background colors.
		 */
		void updateBackground(boolean dark) {
			Color c;
			if (selected)
				c= selectedColor;				
			else
				c= dark ? darkColor : lightColor;
			setBackground(c);
			
			Control[] cs= getChildren();
			for (int i= 0; i < cs.length; i++)
				cs[i].setBackground(c);	
		}
		
		boolean setPercentDone(int percentDone) {
			if (percentDone >= 0 && percentDone < 100) {
				if (progressBar == null) {
					progressBar= new ProgressBar(this, SWT.HORIZONTAL);
					progressBar.setMaximum(100);
					progressBar.setSelection(percentDone);
					return true;
				} else if (!progressBar.isDisposed())
					progressBar.setSelection(percentDone);
			} else {
				if (progressBar == null) {
					progressBar= new ProgressBar(this, SWT.HORIZONTAL | SWT.INDETERMINATE);
					return true;
				}
			}
			return false;
		}
		
		boolean isCanceled() {
			if (jobTreeElement instanceof JobInfo)
				return ((JobInfo)jobTreeElement).isCanceled();
			return false;
		}
	
		IStatus getResult() {
			if (jobTerminated) {
				Job job= getJob();
				if (job != null)
			    	return job.getResult();
			}
			return null;
		}
		
		/*
		 * Update the visual item from the model.
		 */
		public boolean refresh() {

		    if (isDisposed())
		        return false;

			boolean changed= false;
		    boolean isGroup= jobTreeElement instanceof GroupInfo;
			Object[] roots= contentProviderGetChildren(jobTreeElement);

			// poll for properties
		    checkKeep();
		    if (image == null)
		    	setImage(checkIcon());

			// name
		    String name= jobTreeElement.getDisplayString();
		    int ll= name.length();
		    if (ll > 0) {
		        if (name.charAt(0) == '(') {
		            int pos= name.indexOf("%) ");
		            if (pos >= 0)
		                name= name.substring(pos+3);
		        } else if (name.charAt(ll-1) == ')') {
		            int pos= name.lastIndexOf(": (");
		            if (pos >= 0)
		                name= name.substring(0, pos);
		        }
		    }
		    nameItem.setText(shortenText(nameItem, name));

			
			// percentage
			if (jobTreeElement instanceof JobInfo) {				
				TaskInfo ti= ((JobInfo)jobTreeElement).getTaskInfo();
				if (ti != null)
					changed |= setPercentDone(ti.getPercentDone());
			} else if (isGroup) {
		        if (roots.length == 1 && roots[0] instanceof JobTreeElement) {
					TaskInfo ti= ((JobInfo)roots[0]).getTaskInfo();
					if (ti != null)
						changed |= setPercentDone(ti.getPercentDone());
		        } else {
					GroupInfo gi= (GroupInfo) jobTreeElement;
					changed |= setPercentDone(gi.getPercentDone());		            
		        }
			}
			
			// children
		    if (!jobTreeElement.hasChildren())
		        return changed;
			
			Control[] children= getChildren();
			int n= 0;
			for (int i= 0; i < children.length; i++)
				if (children[i] instanceof Hyperlink)
					n++;
			
			if (roots.length == n) {
				int z= 0;
				for (int i= 0; i < children.length; i++) {
					if (children[i] instanceof Hyperlink) {
						Hyperlink l= (Hyperlink) children[i];					
						l.init((JobTreeElement) roots[z++]);
					}
				}
			} else {
			
				HashSet modelJobs= new HashSet();
				for (int z= 0; z < roots.length; z++)
					modelJobs.add(roots[z]);
				
				// find all removed
				HashSet shownJobs= new HashSet();
				for (int i= 0; i < children.length; i++) {
					if (children[i] instanceof Hyperlink) {
						JobTreeItem ji= (JobTreeItem)children[i];
						shownJobs.add(ji.jobTreeElement);
						if (modelJobs.contains(ji.jobTreeElement)) {
							ji.refresh();
						} else {
							changed |= ji.remove();
						}
					}
				}
				
				// find all added
				for (int i= 0; i < roots.length; i++) {
					Object element= roots[i];
					if (!shownJobs.contains(element)) {
						JobTreeElement jte= (JobTreeElement)element;
						new Hyperlink(this, jte);
						changed= true;
					}
				}
			}
			
			return changed;
		}
	}
	
    public NewProgressViewer(Composite parent, int flags) {
        super(parent, flags);
        Tree c = getTree();
        if (c instanceof Tree)
            c.dispose();
       
        FinishedJobs.getInstance().addListener(this);
		
		Display display= parent.getDisplay();
		handCursor= new Cursor(display, SWT.CURSOR_HAND);
		normalCursor= new Cursor(display, SWT.CURSOR_ARROW);

		boolean carbon= "carbon".equals(SWT.getPlatform()); //$NON-NLS-1$
		int shift= carbon ? -25 : -10; // Mac has different Gamma value
		lightColor= display.getSystemColor(SWT.COLOR_LIST_BACKGROUND);
		darkColor= new Color(display, lightColor.getRed()+shift, lightColor.getGreen()+shift, lightColor.getBlue()+shift);
		taskColor= display.getSystemColor(SWT.COLOR_LIST_FOREGROUND);
		selectedColor= display.getSystemColor(SWT.COLOR_LIST_SELECTION);
		linkColor= display.getSystemColor(SWT.COLOR_DARK_BLUE);
		linkColor2= display.getSystemColor(SWT.COLOR_BLUE);
		errorColor= display.getSystemColor(SWT.COLOR_DARK_RED);
		errorColor2= display.getSystemColor(SWT.COLOR_RED);
				
		scroller= new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL | flags);
		int height= defaultFont.getFontData()[0].getHeight();
		scroller.getVerticalBar().setIncrement(height * 2);
		scroller.setExpandHorizontal(true);
		scroller.setExpandVertical(true);
				
		list= new Composite(scroller, SWT.NONE);
		list.setFont(defaultFont);
		list.setBackground(lightColor);
		list.setLayout(new ListLayout());
		
		scroller.setContent(list);
		
		// refresh UI
		refresh(true);
    }

    protected void handleDispose(DisposeEvent event) {
        super.handleDispose(event);
        FinishedJobs.getInstance().removeListener(this);
    }
 
    public Control getControl() {
        return scroller;
    }

 	public void add(Object parentElement, Object[] elements) {
	    if (DEBUG) System.err.println("add");
 	    if (list.isDisposed())
 	        return;
 	    JobTreeItem lastAdded= null;
		for (int i= 0; i < elements.length; i++)
			lastAdded= findJobItem(elements[i], true);
		relayout(true, true);
		if (lastAdded != null)
			reveal(lastAdded);
	}
 	
	public void remove(Object[] elements) {
 	    if (list.isDisposed())
 	        return;
 	   if (DEBUG) System.err.println("NewProgressViewr.remove");
		boolean changed= false;
		for (int i= 0; i < elements.length; i++) {
			JobTreeItem ji= findJobItem(elements[i], false);
			if (ji != null)
				changed |= ji.remove();
		}
		relayout(changed, changed);
	}
	
	public void refresh(Object element, boolean updateLabels) {
 	    if (list.isDisposed())
 	        return;
 	    JobTreeItem ji= findJobItem(element, true);
		if (ji != null)
			if (ji.refresh())
				relayout(true, true);
	}
	
	public void refresh(boolean updateLabels) {
	    if (list.isDisposed())
	        return;
	    if (DEBUG) System.err.println("refreshAll");
		boolean changed= false;
		boolean countChanged= false;
		JobTreeItem lastAdded= null;
		
		Object[] roots= contentProviderGetRoots(getInput());
		HashSet modelJobs= new HashSet();
		for (int z= 0; z < roots.length; z++)
			modelJobs.add(roots[z]);
				
		// find all removed
		Control[] children= list.getChildren();
		for (int i= 0; i < children.length; i++) {
			JobItem ji= (JobItem)children[i];
			if (modelJobs.contains(ji.jobTreeElement)) {
				if (DEBUG) System.err.println("  refresh");
				changed |= ji.refresh();
			} else {
				if (DEBUG) System.err.println("  remove");
				countChanged= true;
				changed |= ji.remove();
			}
		}
		
		// find all added
		for (int i= 0; i < roots.length; i++) {
			Object element= roots[i];
			if (findJobItem(element, false) == null) {
				if (DEBUG) System.err.println("  added");
			    lastAdded= createItem(element);
				changed= countChanged= true;
			}
		}
	    // now add kept finished jobs
		JobInfo[] infos= FinishedJobs.getInstance().getJobInfos();
		for (int i= 0; i < infos.length; i++) {
			Object element= infos[i];
			if (findJobItem(element, false) == null) {
				if (DEBUG) System.err.println("  added2");
			    lastAdded= createItem(element);
				changed= countChanged= true;
			}			
		}
		
		relayout(changed, countChanged);
		if (lastAdded != null)
			reveal(lastAdded);
	}
	
	private JobItem createItem(Object element) {
		return new JobItem(list, (JobTreeElement) element);
	}
	
	private JobTreeItem findJobItem(Object element, boolean create) {
		JobTreeItem ji= (JobTreeItem) map.get(element);
				
		if (ji == null && create) {
			JobTreeElement jte= (JobTreeElement) element;
			Object parent= jte.getParent();
			if (parent != null) {
				JobTreeItem parentji= findJobItem(parent, true);
				if (parentji instanceof JobItem)
					ji= new Hyperlink((JobItem)parentji, jte);
			} else {
				createItem(jte);
			}
		}
		return ji;
	}	
		
	public void reveal(JobTreeItem jti) {
		if (jti != null) {
			Rectangle bounds= jti.getBounds();
			scroller.setOrigin(0, bounds.y);
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
			ListLayout l= (ListLayout) list.getLayout();
			l.refreshBackgrounds= refreshBackgrounds;
			Point size= list.computeSize(list.getClientArea().x, SWT.DEFAULT);
			list.setSize(size);
			scroller.setMinSize(size);	
		}
	}
	
	void clearAll() {
		Control[] children= list.getChildren();
		boolean changed= false;
		for (int i= 0; i < children.length; i++)
			changed |= ((JobItem)children[i]).kill(false, true);
		relayout(changed, changed);
	}
	
	private Image getImage(Display display, String name) {
		ImageDescriptor id= ImageDescriptor.createFromFile(getClass(), name);
		if (id != null)
			return id.createImage(display);
		return null;
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
				gc.dispose();
				return s1 + ELLIPSIS + s2;
			}
			start--;
			end++;
		}
		return textValue;
	}
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
	
	Object[] contentProviderGetChildren(Object parent) {
		IContentProvider provider = getContentProvider();
		if (provider instanceof ITreeContentProvider)
			return ((ITreeContentProvider)provider).getChildren(parent);
		return new Object[0];
	}

	Object[] contentProviderGetRoots(Object parent) {
		IContentProvider provider = getContentProvider();
		if (provider instanceof ITreeContentProvider)
			return ((ITreeContentProvider)provider).getElements(parent);
		return new Object[0];
	}

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.progress.NewKeptJobs.KeptJobsListener#added(org.eclipse.ui.internal.progress.JobInfo)
     */
    public void added(JobInfo info) {
    	// we should not have to do anything here
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.progress.NewKeptJobs.KeptJobsListener#removed(org.eclipse.ui.internal.progress.JobInfo)
     */
    public void removed(JobInfo info) {
		JobTreeItem ji= findJobItem(info, false);
		if (ji != null)
			ji.kill(true, false);
    }

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.progress.FinishedJobs.KeptJobsListener#infoVisited()
	 */
	public void infoVisited() {
    	// we should not have to do anything here
	}
	
	////// SelectionProvider

    public ISelection getSelection() {
        return StructuredSelection.EMPTY;
    }

    public void setSelection(ISelection selection) {
    }

    public void setUseHashlookup(boolean b) {
    }

    public void setInput(IContentProvider provider) {
    }

    public void cancelSelection() {
    }
    
    ///////////////////////////////////

    protected void addTreeListener(Control c, TreeListener listener) {
    }

    protected void doUpdateItem(final Item item, Object element) {
    }

    protected Item[] getChildren(Widget o) {
        return new Item[0];
    }

    protected boolean getExpanded(Item item) {
        return true;
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

    protected Item getParentItem(Item item) {
        return null;
    }

    protected Item[] getSelection(Control widget) {
        return new Item[0];
    }

    public Tree getTree() {
        Tree t= super.getTree();
        if (t != null && !t.isDisposed())
            return t;
        return null;
    }

    protected Item newItem(Widget parent, int flags, int ix) {
        return null;
    }

    protected void removeAll(Control widget) {
    }

    protected void setExpanded(Item node, boolean expand) {
    }

    protected void setSelection(List items) {
    }

    protected void showItem(Item item) {
    }
    
	protected void createChildren(Widget widget) {
		refresh(true);
	}
	
	protected void internalRefresh(Object element, boolean updateLabels) {
	}
}