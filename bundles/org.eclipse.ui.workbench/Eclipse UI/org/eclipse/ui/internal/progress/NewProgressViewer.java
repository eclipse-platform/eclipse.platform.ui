package org.eclipse.ui.internal.progress;

import java.net.URL;
import java.util.ArrayList;
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
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
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
import org.eclipse.ui.internal.misc.Assert;

public class NewProgressViewer extends ProgressTreeViewer implements FinishedJobs.KeptJobsListener {
	
	static final boolean DEBUG= false;
	
	static final boolean isCarbon = "carbon".equals(SWT.getPlatform()); //$NON-NLS-1$
    
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
	private Color selectedTextColor;
	private Cursor handCursor;
	private Cursor normalCursor;
	private Image defaultJobIcon;
	private Font defaultFont= JFaceResources.getDefaultFont();
	private Font boldFont;
	private Font smallerFont;
	private HashMap map= new HashMap();
    private IJobProgressManagerListener progressManagerListener;

	
	class ListLayout extends Layout {
	    static final int VERTICAL_SPACING = 1;
		boolean refreshBackgrounds;
		
		protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {		
			int w= 0, h= -VERTICAL_SPACING;
			Control[] cs= composite.getChildren();
			for (int i= 0; i < cs.length; i++) {
				Control c= cs[i];
				Point e= c.computeSize(SWT.DEFAULT, SWT.DEFAULT, flushCache);
				w= Math.max(w, e.x);
				h+= e.y+VERTICAL_SPACING;
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
				HashMap map2= new HashMap();	// temp remember items for sorting
				JobTreeElement[] elements= new JobTreeElement[cs.length];
				for (int i= 0; i < cs.length; i++) {
					JobItem ji= (JobItem)cs[i];
					elements[i]= ji.jobTreeElement;
					map2.put(elements[i], ji);
				}
				vs.sort(NewProgressViewer.this, elements);
				for (int i= 0; i < cs.length; i++)
					cs[i]= (JobItem) map2.get(elements[i]);
			}
			// sort
			boolean dark= (cs.length % 2) == 1;
			for (int i= 0; i < cs.length; i++) {
				Control c= cs[i];
				Point s= c.computeSize(e.x, SWT.DEFAULT, flushCache);
				c.setBounds(x, y, s.x, s.y);
				y+= s.y+VERTICAL_SPACING;
				if (refreshBackgrounds && c instanceof JobItem) {
					((JobItem)c).updateBackground(dark);
					dark= !dark;
				}
			}
		}
	}
	
	abstract class JobTreeItem extends Canvas implements Listener {
		JobTreeElement jobTreeElement;
		boolean jobTerminated;
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
		
		void setKept() {
			if (!jobTerminated) {
				keepItem= jobTerminated= true;
				remove();	// bring to keep mode
			}
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
					//Boolean modelProperty= (Boolean)job.getProperty(ProgressManager.PROPERTY_IN_DIALOG);
					if (property instanceof Boolean && ((Boolean)property).booleanValue())
						setKeep();
					IStatus result= job.getResult();
					if (result != null && result.getSeverity() == IStatus.ERROR)
						setKeep();
				}
			}
			return keepItem;			
		}
		
		void setKeep() {
			//if (DEBUG) System.err.println("got keep for: " + jobTreeElement);
			keepItem= true;
			Composite parent= getParent();
			if (parent instanceof JobTreeItem) {
			    //if (DEBUG) System.err.println("propagating keep to: " + ((JobTreeItem)parent).jobTreeElement);
			    ((JobTreeItem)parent).keepItem= true;
			}			
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
			if (DEBUG) System.err.println("  JobTreeItem.remove:"); //$NON-NLS-1$
			
			refresh();
			
			if (!keepItem) {
			    if (DEBUG) System.err.println("  JobItem.dispose:"); //$NON-NLS-1$
				dispose();
				return true;
			}
//			Job job= getJob();
//			if (job != null) {
//				Boolean modelProperty= (Boolean)job.getProperty(ProgressManager.PROPERTY_IN_DIALOG);
//				if (modelProperty != null) {
//					dispose();
//					return true;			
//				}
//			}
			//System.err.println("  JobItem.remove: keep");
			return false;
		}
	}
	
	/*
	 * Label with hyperlink capability.
	 */
	class Hyperlink extends JobTreeItem implements Listener, IPropertyChangeListener {
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
		public void handleEvent(Event e) {
			super.handleEvent(e);
			switch (e.type) {
			case SWT.Dispose:
				if (gotoAction != null)
					gotoAction.removePropertyChangeListener(this);
				break;
			case SWT.KeyDown:
				//System.out.println("SWT.KeyDown3 " + this);
				if (e.character == '\r')
					handleActivate();
				else
					select(null, e);
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
			case SWT.MouseDown :
				if (!underlined)
					select((JobItem) getParent(), e);
				break;
			case SWT.MouseUp :
				if (underlined) {
					Point size= getSize();
					if (e.button != 1 || e.x < 0 || e.y < 0 || e.x >= size.x || e.y >= size.y)
						return;
					handleActivate();
				}
				break;
			}
		}
		void setStatus(IStatus r) {
			result= r;
	    	if (result != null) {
	    		String message= result.getMessage().trim();
	    		if (message.length() > 0) {
	    			if (r.getSeverity() == IStatus.ERROR) {
	    				setKeep();
	    				
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
			if (action == gotoAction)
				return;
			if (gotoAction != null) {
				gotoAction.removePropertyChangeListener(this);
			}
			gotoAction= action;
			if (gotoAction != null) {
				gotoAction.addPropertyChangeListener(this);
			}
			underlined= action != null;
			setForeground(underlined ? lColor : taskColor);
			if (underlined)
				setCursor(handCursor);
			redraw();
		}
		public void propertyChange(PropertyChangeEvent event) {
		    if (DEBUG) System.err.println("action changed: " + gotoAction); //$NON-NLS-1$
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
				if (hasFocus)
					bufferGC.drawFocus(0, 0, sw, clientArea.height);
			}
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
		public boolean remove() {
			boolean b= super.remove();
			return b;
		}
	}
		

	/*
	 * An SWT widget representing a JobModel
	 */
	class JobItem extends JobTreeItem {
		
		static final int MARGIN= 2;
		static final int HGAP= 7;
		static final int VGAP= 1;
		static final int MAX_PROGRESS_HEIGHT= 12;
		static final int MIN_ICON_SIZE= 16;

		int cachedWidth= -1;
		int cachedHeight= -1;

		Image image;
		Label nameItem, iconItem;
		ProgressBar progressBar;
		ToolBar actionBar;
		ToolItem actionButton;
		ToolItem gotoButton;
		boolean selected;
		

		JobItem(Composite parent, JobTreeElement info) {
			super(parent, info, SWT.NONE);
			
			Assert.isNotNull(info);
						
			Display display= getDisplay();
						
			iconItem= new Label(this, SWT.NONE);
			iconItem.addListener(SWT.MouseDown, this);
			image= checkIcon();
			if (image != null)
				iconItem.setImage(image);
			else
				iconItem.setImage(defaultJobIcon);			
			
			nameItem= new Label(this, SWT.NONE);
			nameItem.setFont(boldFont);
			nameItem.addListener(SWT.MouseDown, this);
			
			actionBar= new ToolBar(this, SWT.FLAT);
			actionBar.setCursor(normalCursor);	// set cursor to overwrite any busy cursor we might have
			actionButton= new ToolItem(actionBar, SWT.NONE);
			actionButton.setImage(getImage(display, "newprogress_cancel.gif")); //$NON-NLS-1$
			actionButton.setToolTipText(ProgressMessages.getString("NewProgressView.CancelJobToolTip")); //$NON-NLS-1$
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
		
		void cancelOrRemove() {
			if (jobTerminated)
				kill(true, true);
			else
				jobTreeElement.cancel();
		}

		public void handleEvent(Event event) {
	        switch (event.type) {
		    case SWT.Dispose:
		    	super.handleEvent(event);
	        	if (image != null && !image.isDisposed()) {
	        		image.dispose();
	        		image= null;
	        	}
		        break;
		    case SWT.KeyDown:
				//System.err.println("KeyDown0 " + this);
				select(null, event);
		    	break;
		    case SWT.MouseDown:
		    	//setFocus();
				select(JobItem.this, event);
	        	break;
	        default:
		        super.handleEvent(event);
	        	break;
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
			
			checkKeep();
						
			if (keepItem) {
				boolean changed= false;
				if (progressBar != null && !progressBar.isDisposed()) {
				    progressBar.setSelection(100);
					progressBar.dispose();
					changed= true;
				}
				if (!actionButton.isDisposed()) {
					actionButton.setImage(getImage(actionBar.getDisplay(), "newprogress_clear.gif")); //$NON-NLS-1$
					actionButton.setToolTipText(ProgressMessages.getString("NewProgressView.RemoveJobToolTip")); //$NON-NLS-1$
					actionButton.setEnabled(true);
					changed= true;
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
			return super.remove();
		}
		
		public boolean kill(boolean refresh, boolean broadcast) {
			if (jobTerminated) {
				
				if (broadcast)
				    FinishedJobs.getInstance().remove(NewProgressViewer.this, jobTreeElement);
				
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
			
			nameItem.setBounds(MARGIN+e1.x+HGAP, y+(h-e2.y)/2, iw-e1.x-HGAP, e2.y);
			y+= h;
			if (progressBar != null && !progressBar.isDisposed()) {
				Point e3= progressBar.computeSize(SWT.DEFAULT, SWT.DEFAULT); e3.y= MAX_PROGRESS_HEIGHT;
				y+= VGAP+1;
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
			
			int hm= (MARGIN+HGAP)/2;
			int vm= (y-e1.y)/2;
			if (hm < (y-e1.y)/2)
				vm= hm;
			iconItem.setBounds(hm, vm, e1.x, e1.y);
			
			actionBar.setBounds(e.x-MARGIN-e5.x, (e.y-e5.y)/2, e5.x, e5.y);
		}
		
		public Point computeSize(int wHint, int hHint, boolean changed) {
			
			if (changed || cachedHeight <= 0 || cachedWidth <= 0) {
				Point e1= iconItem.computeSize(SWT.DEFAULT, SWT.DEFAULT); e1.x= MIN_ICON_SIZE;
				Point e2= nameItem.computeSize(SWT.DEFAULT, SWT.DEFAULT);
				
				cachedWidth= MARGIN + e1.x + HGAP + 100 + MARGIN;
					
				cachedHeight= MARGIN + Math.max(e1.y, e2.y);
				if (progressBar != null && !progressBar.isDisposed()) {
					cachedHeight+= 1;
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
			Color fg, bg;
			if (selected) {
				fg= selectedTextColor;
				bg= selectedColor;				
			} else {
				fg= taskColor;
				bg= dark ? darkColor : lightColor;
			}
			setForeground(fg);
			setBackground(bg);
			
			Control[] cs= getChildren();
			for (int i= 0; i < cs.length; i++) {
				if (cs[i] != this.progressBar) {
					cs[i].setForeground(fg);
					cs[i].setBackground(bg);
				}
			}
		}
		
		boolean setPercentDone(int percentDone) {
			if (percentDone >= 0 && percentDone < 100) {
				if (progressBar == null) {
					progressBar= new ProgressBar(this, SWT.HORIZONTAL);
					progressBar.setMaximum(100);
					progressBar.setSelection(percentDone);
					progressBar.addListener(SWT.MouseDown, this);
					return true;
				} else if (!progressBar.isDisposed())
					progressBar.setSelection(percentDone);
			} else {
				if (progressBar == null) {
					progressBar= new ProgressBar(this, SWT.HORIZONTAL | SWT.INDETERMINATE);
					progressBar.addListener(SWT.MouseDown, this);
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
			//checkKeep();
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
		    String name;
		    if (jobTerminated) {
		        name= "Terminated: " + jobTreeElement.getCondensedDisplayString();
		    } else {
			    name= jobTreeElement.getDisplayString();
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
		    }
		    if (DEBUG) {
			    if (isGroup)
			    	name= "G " + name; //$NON-NLS-1$
			    if (jobTreeElement.getParent() != null)
			    	name= "X " + name; //$NON-NLS-1$
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
			
			if (roots.length == n) {	// reuse all children
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
        
	    progressManagerListener= new IJobProgressManagerListener() {
            public void addJob(JobInfo info) { }
            public void addGroup(GroupInfo info) { }
            public void refreshJobInfo(JobInfo info) { }
            public void refreshGroup(GroupInfo info) { }
            public void refreshAll() { }
            public void removeJob(JobInfo info) {
                forcedRemove(info);
            }
            public void removeGroup(GroupInfo group) {
                forcedRemove(group);
            }
            public boolean showsDebug() {
                return false;
            }
	    };
	    ProgressManager.getInstance().addListener(progressManagerListener);	
       
        FinishedJobs.getInstance().addListener(this);
		
		Display display= parent.getDisplay();
		handCursor= new Cursor(display, SWT.CURSOR_HAND);
		normalCursor= new Cursor(display, SWT.CURSOR_ARROW);

		defaultJobIcon= getImage(display, "newprogress_circle.gif"); //$NON-NLS-1$
		
		FontData fds[]= defaultFont.getFontData();
		if (fds.length > 0) {
			FontData fd= fds[0];
			int h= fd.getHeight();
			if (isCarbon)
				h-=2;
			boldFont= new Font(display, fd.getName(), h, fd.getStyle() | SWT.BOLD);
			smallerFont= new Font(display, fd.getName(), h, fd.getStyle());
		}
		
		int shift= isCarbon ? -25 : -10; // Mac has different Gamma value
		lightColor= display.getSystemColor(SWT.COLOR_LIST_BACKGROUND);
		darkColor= new Color(display, lightColor.getRed()+shift, lightColor.getGreen()+shift, lightColor.getBlue()+shift);
		taskColor= display.getSystemColor(SWT.COLOR_LIST_FOREGROUND);
		selectedTextColor= display.getSystemColor(SWT.COLOR_LIST_SELECTION_TEXT);
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
		
		list.addListener(SWT.Traverse, new Listener() {
			public void handleEvent(Event event) {
				//System.err.println("Traverse");
				//select(null, event);
			}			
		});
		
		list.addListener(SWT.MouseDown, new Listener() {
			public void handleEvent(Event event) {
				select(null, event);	// clear selection
			}			
		});

		scroller.setContent(list);
		
		// refresh UI
		refresh(true);
    }

    protected void handleDispose(DisposeEvent event) {
        super.handleDispose(event);
        FinishedJobs.getInstance().removeListener(this);
	    ProgressManager.getInstance().removeListener(progressManagerListener);	
    }
 
    public Control getControl() {
        return scroller;
    }

 	public void add(Object parentElement, Object[] elements) {
	    if (DEBUG) System.err.println("add"); //$NON-NLS-1$
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
 	    if (DEBUG) System.err.println("remove"); //$NON-NLS-1$
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
		if (ji != null && ji.refresh())
			relayout(true, true);
	}
	
	public void refresh(boolean updateLabels) {
	    if (list.isDisposed())
	        return;
	    if (DEBUG) System.err.println("refreshAll"); //$NON-NLS-1$
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
				if (DEBUG) System.err.println("  refresh"); //$NON-NLS-1$
				changed |= ji.refresh();
			} else {
				if (DEBUG) System.err.println("  remove: " + ji.jobTreeElement); //$NON-NLS-1$
				countChanged= true;
				changed |= ji.remove();
			}
		}
		
		// find all added
		for (int i= 0; i < roots.length; i++) {
			Object element= roots[i];
			if (findJobItem(element, false) == null) {
				if (DEBUG) System.err.println("  added"); //$NON-NLS-1$
			    lastAdded= createItem(element);
				changed= countChanged= true;
			}
		}
	    // now add kept finished jobs
		JobTreeElement[] infos= FinishedJobs.getInstance().getJobInfos();
		for (int i= 0; i < infos.length; i++) {
			Object element= infos[i];
			JobTreeItem jte= findJobItem(element, true);
			if (jte != null) {
				jte.setKept();
				lastAdded= jte;
				
				if (jte instanceof Hyperlink) {
					JobItem p= (JobItem) jte.getParent();
					p.setKept();
					lastAdded= p;
				}
				
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
				if (parentji instanceof JobItem && !(jte instanceof TaskInfo)) {
					if (findJobItem(jte, false) == null)
						ji= new Hyperlink((JobItem)parentji, jte);
				}
			} else {
				ji= createItem(jte);
			}
		}
		return ji;
	}	
		
	public void reveal(JobTreeItem jti) {
		if (jti != null && !jti.isDisposed()) {
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
		
		if (DEBUG) {
			JobTreeElement[] elements = FinishedJobs.getInstance().getJobInfos();
			System.out.println("jobs: " + elements.length);
			for (int i= 0; i < elements.length; i++)
				System.out.println("  " + elements[i]);
		}
	}
	
	private Image getImage(Display display, String name) {
		ImageDescriptor id= ImageDescriptor.createFromFile(getClass(), name);
		if (id != null)
			return id.createImage(display);
		return null;
	}

	private void select(JobItem newSelection, Event e) {

		boolean clearAll= false;
		JobItem newSel= null;
		Control[] cs= list.getChildren();		

		JobTreeElement element= null;
		if (newSelection != null)
			element= newSelection.jobTreeElement;
		
		if (e.type == SWT.KeyDown) { // key
			if (e.keyCode == SWT.ARROW_UP) {
				for (int i= 0; i < cs.length; i++) {
					JobItem ji= (JobItem) cs[i];
					if (ji.selected) {
						if (i-1 >= 0) {
							newSel= (JobItem) cs[i-1];
							if ((e.stateMask & SWT.MOD2) != 0) {
								newSel.selected= true;
							} else {
								clearAll= true;
							}
							break;
						}
						return;
					}
				}
			} else if (e.keyCode == SWT.ARROW_DOWN) {
				for (int i= cs.length-1; i >= 0; i--) {
					JobItem ji= (JobItem) cs[i];
					if (ji.selected) {
						if (i+1 < cs.length) {
							newSel= (JobItem) cs[i+1];
							if ((e.stateMask & SWT.MOD2) != 0) {
								newSel.selected= true;
							} else {
								clearAll= true;
							}
							break;
						}
						return;
					}
				}
			}
		} else if (e.type == SWT.MouseDown) {	// mouse
			
			if (newSelection == null) {
				clearAll= true;
			} else {			
				if ((e.stateMask & SWT.MOD1) != 0) {
					newSelection.selected= !newSelection.selected;
				} else if ((e.stateMask & SWT.MOD2) != 0) {
					
					
					
					//System.out.println("MOD2");
				} else {
					if (newSelection.selected)
						return;
					clearAll= true;
					newSel= newSelection;
				}
			}
		}
		
		if (clearAll) {
			for (int i= 0; i < cs.length; i++) {
				JobItem ji= (JobItem) cs[i];
				ji.selected= ji == newSel;
			}			
		}
		
		boolean dark= (cs.length % 2) == 1;
		for (int i= 0; i < cs.length; i++) {
			JobItem ji= (JobItem) cs[i];
			ji.updateBackground(dark);
			dark= !dark;
		}
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
	
	private void forcedRemove(final JobTreeElement jte) {
		if (list != null && !list.isDisposed()) {
			list.getDisplay().asyncExec(new Runnable() {
	            public void run() {
	                if (DEBUG) System.err.println("  forced remove"); //$NON-NLS-1$
	        			JobTreeItem ji= findJobItem(jte, false);
	                if (ji != null && !ji.isDisposed() && ji.remove())
	                    relayout(true, true);
	            }
	        });
		}	    
	}

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.progress.NewKeptJobs.KeptJobsListener#finished(org.eclipse.ui.internal.progress.JobInfo)
     */
    public void finished(JobTreeElement jte) {
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.progress.NewKeptJobs.KeptJobsListener#removed(org.eclipse.ui.internal.progress.JobInfo)
     */
    public void removed(JobTreeElement info) {
		final JobTreeItem ji= findJobItem(info, false);
		if (ji != null) {
	        ji.getDisplay().asyncExec(new Runnable() {
	            public void run() {
	                ji.kill(true, false);
	            }
	        });
		}
    }

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.progress.FinishedJobs.KeptJobsListener#infoVisited()
	 */
	public void infoVisited() {
    	// we should not have to do anything here
	}
	
	////// SelectionProvider

    public ISelection getSelection() {
    	ArrayList l= new ArrayList();
		Control[] cs= list.getChildren();
		for (int i= 0; i < cs.length; i++) {
			JobItem ji= (JobItem) cs[i];
			l.add(ji.jobTreeElement);
		} 
    	return new StructuredSelection(l);
    }

    public void setSelection(ISelection selection) {
    }

    public void setUseHashlookup(boolean b) {
    }

    public void setInput(IContentProvider provider) {
    }
    
	public void cancelSelection() {
		Control[] cs= list.getChildren();
		for (int i= 0; i < cs.length; i++) {
			JobItem ji= (JobItem) cs[i];
			ji.cancelOrRemove();
		} 
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
		getControl().addKeyListener(new KeyAdapter() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.KeyAdapter#keyPressed(org.eclipse.swt.events.KeyEvent)
			 */
			public void keyPressed(KeyEvent e) {
				//Bind escape to cancel
				if (e.keyCode == SWT.DEL) {
					cancelSelection();
				}
			}
		});
	}
	
	protected void internalRefresh(Object element, boolean updateLabels) {
	}
}