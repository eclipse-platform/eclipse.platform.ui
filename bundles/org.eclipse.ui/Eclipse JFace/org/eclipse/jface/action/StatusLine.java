package org.eclipse.jface.action;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressIndicator;
import org.eclipse.jface.resource.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

/**
 * A StatusLine control is a SWT Composite with a horizontal layout which hosts
 * a number of status indication controls.
 * Typically it is situated below the content area of the window.
 * <p>
 * By default a StatusLine has two predefined status controls: a MessageLine and a
 * ProgressIndicator and it provides API for easy access.
 * </p>
 * <p>
 * This is an internal class, not intended to be used outside the JFace framework.
 * </p>
 */
/* package */ class StatusLine extends Composite implements IProgressMonitor {
	
	/** Horizontal gaps between items. */
	public static final int GAP= 3;
	/** Progress bar creation is delayed by this ms */
	public static final int DELAY_PROGRESS= 500;	

	// state
	protected boolean fProgressIsVisible= false;
	protected boolean fCancelButtonIsVisible= false;
	protected boolean fCancelEnabled= false;
	protected String fTaskName;
	protected boolean fIsCanceled;
	protected long fStartTime;
	private Cursor fStopButtonCursor;
	protected String fMessageText;
	protected Image fMessageImage;
	protected String fErrorText;
	protected Image fErrorImage;
	
	// SWT widgets
	protected CLabel fMessageLabel;
	protected ProgressIndicator fProgressBar;
	protected ToolBar fToolBar;
	protected ToolItem fCancelButton;
	
	protected static ImageDescriptor fgStopImage= ImageDescriptor.createFromFile(StatusLine.class, "images/stop.gif");//$NON-NLS-1$
	static {
		JFaceResources.getImageRegistry().put("org.eclipse.jface.parts.StatusLine.stopImage", fgStopImage);//$NON-NLS-1$
	}
	
	/**
	 * @private
	 */
	public class StatusLineLayout extends Layout {
	
		public Point computeSize(Composite composite, int wHint, int hHint, boolean changed) {
				
			if (wHint != SWT.DEFAULT && hHint != SWT.DEFAULT)
				return new Point(wHint, hHint);
	
			Control[] children= composite.getChildren();
			int totalWidth= 0;
			int maxHeight= 0;
			int totalCnt= 0;
			for (int i= 0; i < children.length; i++) {
				boolean useWidth= true;
				Control w= children[i];
				if (w == fProgressBar && !fProgressIsVisible)
					useWidth= false;
				else if (w == fToolBar && !fCancelButtonIsVisible)
					useWidth= false;
				Point e= w.computeSize(SWT.DEFAULT, SWT.DEFAULT, changed);
				if (useWidth) {
					totalWidth+= e.x;
					totalCnt++;
				}
				maxHeight= Math.max(maxHeight, e.y);
			}
			if (totalCnt > 0)
				totalWidth+= (totalCnt-1) * GAP;
			if (totalWidth <= 0)
				totalWidth= maxHeight*4;
			return new Point(totalWidth, maxHeight);
		}
	
		public void layout(Composite composite, boolean flushCache) {
		
			if (composite == null)
				return;
			
			// Make sure cancel button and progress bar are at the end.
			fMessageLabel.moveAbove(null);
			fToolBar.moveBelow(fMessageLabel);
			fProgressBar.moveBelow(fToolBar);
			
			Rectangle rect= composite.getClientArea();
			Control[] children= composite.getChildren();
			int count= children.length;
	
			int ws[]= new int[count];
			
			int totalWidth= -GAP;
			for (int i= 0; i < count; i++) {
				Control w= children[i];
				if (w == fProgressBar && !fProgressIsVisible)
					continue;
				if (w == fToolBar && !fCancelButtonIsVisible)
					continue;
				int width= w.computeSize(SWT.DEFAULT, SWT.DEFAULT, flushCache).x;
				ws[i]= width;
				totalWidth+= width + GAP;
			}
	
			int diff= rect.width-totalWidth;
			ws[0]+= diff;	// make the first StatusLabel wider
			
			// Check against minimum recommended width
			final int msgMinWidth = rect.width/3;
			if (ws[0] < msgMinWidth) {
				diff = ws[0] - msgMinWidth;
				ws[0] = msgMinWidth;
			} else {
				diff = 0;
			}
			
			// Take space away from the contributions first.
			for (int i = count -1; i >= 0 && diff < 0; --i) {
				int min = Math.min(ws[i], -diff);
				ws[i] -= min;
				diff += min + GAP;
			}
			
			int x= rect.x;
			int y= rect.y;
			int h= rect.height;
			for (int i= 0; i < count; i++) {
				Control w= children[i];
				/*
				 * Workaround for Linux Motif:
				 * Even if the progress bar and cancel button are
				 * not set to be visible ad of width 0, they still
				 * draw over the first pixel of the editor 
				 * contributions.
				 * 
				 * The fix here is to draw the progress bar and
				 * cancel button off screen if they are not visible.
				 */
				if (w == fProgressBar && !fProgressIsVisible ||
					w == fToolBar && !fCancelButtonIsVisible) {
					w.setBounds(x + rect.width, y, ws[i], h);
					continue;
				}
				w.setBounds(x, y, ws[i], h);
				if (ws[i] > 0) x+= ws[i] + GAP;
			}
		}
	}
	
	
	/**
	 * Create a new StatusLine as a child of the given parent.
	 */
	public StatusLine(Composite parent) {
		super(parent, SWT.NONE);
		
		addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				handleDispose();
			}
		});

		setLayout(new StatusLineLayout());

		fMessageLabel= new CLabel(this, SWT.SHADOW_IN);

		fProgressIsVisible= false;
		fCancelEnabled= false;

		fToolBar= new ToolBar(this, SWT.FLAT);
		fCancelButton= new ToolItem(fToolBar, SWT.PUSH);
		fCancelButton.setImage(fgStopImage.createImage());
		fCancelButton.setToolTipText(JFaceResources.getString("Cancel_Current_Operation")); //$NON-NLS-1$
		fCancelButton.addSelectionListener(
			new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					setCanceled(true);
				}
			}
		);
		fCancelButton.addDisposeListener(new DisposeListener(){
			public void widgetDisposed(DisposeEvent e) {
				Image i = fCancelButton.getImage();
				if((i != null) && (!i.isDisposed()))
					i.dispose();
			}
		});

		fProgressBar= new ProgressIndicator(this);
		fProgressBar.setSize(200, 10);
		
		fStopButtonCursor= new Cursor(getDisplay(), SWT.CURSOR_ARROW);
	}
	/**
	 * Notifies that the main task is beginning.
	 * 
	 * @param name the name (or description) of the main task
	 * @param totalWork the total number of work units into which
	 * the main task is been subdivided. If the value is 0 or UNKNOWN the 
	 * implemenation is free to indicate progress in a way which doesn't 
	 * require the total number of work units in advance. In general users 
	 * should use the UNKNOWN value if they don't know the total amount of
	 * work units. 
	 */
	public void beginTask(String name, int totalWork) {

		fStartTime= System.currentTimeMillis();
		if (totalWork == UNKNOWN || totalWork == 0) {
			
			final long timestamp= fStartTime;
			Thread t= new Thread() {
				public void run() {
					try {
						sleep(DELAY_PROGRESS);
						StatusLine.this.startAnimatedTask(timestamp);
					} catch (InterruptedException e) {
					}
				}
			};
			t.start();
			
		} else {
			fProgressBar.beginTask(totalWork);
		}
		
		if (name == null)
			fTaskName= "";//$NON-NLS-1$
		else
			fTaskName= name;
		setMessage(fTaskName);
	}
	/**
	 * Notifies that the work is done; that is, either the main task is completed or the
	 * user cancelled it.
	 * Done() can be called more than once; an implementation should be prepared to handle
	 * this case.
	 */
	public void done() {
	
		fStartTime= 0;
		
		if (fProgressBar != null) {
			fProgressBar.sendRemainingWork();
			fProgressBar.done();
		}	
		setMessage("");//$NON-NLS-1$
		
		hideProgress();
	}
	/**
	 * Returns the status line's progress monitor
	 */
	public IProgressMonitor getProgressMonitor() {
		return this;
	}
	/**
	 * @private
	 */
	protected void handleDispose() {
		fStopButtonCursor.dispose();
		fStopButtonCursor= null;
	}
/**
 * Hides the Cancel button and ProgressIndicator.
 * @private
 */
protected void hideProgress() {

	if (fProgressIsVisible && !isDisposed()) {
		fProgressIsVisible = false;
		fCancelEnabled = false;
		fCancelButtonIsVisible = false;
		if (fToolBar != null && !fToolBar.isDisposed())
			fToolBar.setVisible(false);
		if (fProgressBar != null && !fProgressBar.isDisposed())
			fProgressBar.setVisible(false);
		layout();
	}
}
	/**
	 * @see IProgressMonitor#internalWorked(double)
	 */
	public void internalWorked(double work) {
		if (! fProgressIsVisible) {
			if (System.currentTimeMillis() - fStartTime > DELAY_PROGRESS)
				showProgress();
		}
			
		if (fProgressBar != null) {
			fProgressBar.worked(work);
		}	
	}
	/**
	 * Returns true if the user does some UI action to cancel this operation.
	 * (like hitting the Cancel button on the progress dialog).
	 * The long running operation typically polls isCanceled().
	 */
	public boolean isCanceled() {
		return fIsCanceled;
	}
	/**
	 * Returns <code>true</true> if the ProgressIndication provides UI for canceling
	 * a long running operation.
	 */
	public boolean isCancelEnabled() {
		return fCancelEnabled;
	}
	/**
	 * Sets the cancel status. This method is usually called with the 
	 * argument false if a client wants to abort a cancel action.
	 */
	public void setCanceled(boolean b) {
		fIsCanceled = b;
		if (fCancelButton != null)
			fCancelButton.setEnabled(!b);
	}
	/**
	 * Controls whether the ProgressIndication provides UI for canceling
	 * a long running operation.
	 * If the ProgressIndication is currently visible calling this method may have
	 * a direct effect on the layout because it will make a cancel button visible. 
	 */
	public void setCancelEnabled(boolean enabled) {
		fCancelEnabled= enabled; 
		if (fProgressIsVisible && !fCancelButtonIsVisible && enabled) {
			showButton();
			layout();
		}
		if (fCancelButton != null && !fCancelButton.isDisposed())
			fCancelButton.setEnabled(enabled);
	}
/**
 * Sets the error message text to be displayed on the status line.
 * The image on the status line is cleared.
 * 
 * @param message the error message, or <code>null</code> for no error message
 */
public void setErrorMessage(String message) {
	setErrorMessage(null, message);
}
/**
 * Sets an image and error message text to be displayed on the status line.
 * 
 * @param image the image to use, or <code>null</code> for no image
 * @param message the error message, or <code>null</code> for no error message
 */
public void setErrorMessage(Image image, String message) {
	fErrorText = trim(message);
	fErrorImage = image;
	updateMessageLabel();
}
	/**
	 * Applies the given font to this status line.
	 */
	public void setFont(Font font) {
		super.setFont(font);
		Control[] children= getChildren();
		for (int i= 0; i < children.length; i++) {
			children[i].setFont(font);
		}
	}
/**
 * Sets the message text to be displayed on the status line.
 * The image on the status line is cleared.
 * 
 * @param message the error message, or <code>null</code> for no error message
 */
public void setMessage(String message) {
	setMessage(null, message);
}
/**
 * Sets an image and a message text to be displayed on the status line.
 * 
 * @param image the image to use, or <code>null</code> for no image
 * @param message the message, or <code>null</code> for no message
 */
public void setMessage(Image image, String message) {
	fMessageText = trim(message);
	fMessageImage = image;
	updateMessageLabel();
}
	/**
	 * @see IProgressMonitor#setTaskName(java.lang.String)
	 */
	public void setTaskName(String name) {
		fTaskName= name;
	}
	/**
	 * Makes the Cancel button visible.
	 * @private
	 */
	protected void showButton() {
		if (fToolBar != null) {
			fToolBar.setVisible(true);
			fToolBar.setEnabled(true);
			fToolBar.setCursor(fStopButtonCursor);
			fCancelButtonIsVisible= true;
		}
	}
	/**
	 * Shows the Cancel button and ProgressIndicator.
	 * @private
 	 */
	protected void showProgress() {
		if (! fProgressIsVisible) {
			fProgressIsVisible= true;
			if (fCancelEnabled)
				showButton();
			if (fProgressBar != null)
				fProgressBar.setVisible(true);
			layout();
		}
	}
	/**
	 * @private
	 */
	void startAnimatedTask(final long timestamp) {
		if (fProgressBar != null) {
			fProgressBar.getDisplay().asyncExec(
				new Runnable() {
					public void run() {
						if (fStartTime == timestamp) {
							showProgress();
							fProgressBar.beginAnimatedTask();
						}
					}
				}
			);
		}	
	}
	/**
	 * Notifies that a subtask of the main task is beginning.
	 * Subtasks are optional; the main task might not have subtasks.
	 * @param name the name (or description) of the subtask
	 * @see IProgressMonitor#subTask(String)
	 */
	public void subTask(String name) {
		String text;
		if (fTaskName.length() == 0)
			text= name;
		else
			text = JFaceResources.format("Set_SubTask", new Object[] {fTaskName, name});//$NON-NLS-1$
		setMessage(text);		
	}
	
	/**
	 * Trims the message to be displayable in the status line.
	 * This just pulls out the first line of the message.
	 * Allows null.
	 */
	String trim(String message) {
		if (message == null)
			return null;
		int cr = message.indexOf('\r');
		int lf = message.indexOf('\n');
		if (cr == -1 && lf == -1)
			return message;
		int len;
		if (cr == -1)
			len = lf;
		else if (lf == -1)
			len = cr;
		else 
			len = Math.min(cr, lf);
		return message.substring(0, len);
	}
	
/**
 * Updates the message label widget.
 */
protected void updateMessageLabel() {
	if (fMessageLabel != null && !fMessageLabel.isDisposed()) {
		Display display = fMessageLabel.getDisplay();
		if ((fErrorText != null && fErrorText.length() > 0) || fErrorImage != null) {
			fMessageLabel.setForeground(JFaceColors.getErrorText(display));
			fMessageLabel.setText(fErrorText);
			fMessageLabel.setImage(fErrorImage);
		}
		else {
			fMessageLabel.setForeground(display.getSystemColor(SWT.COLOR_WIDGET_FOREGROUND));
			fMessageLabel.setText(fMessageText == null ? "" : fMessageText); //$NON-NLS-1$
			fMessageLabel.setImage(fMessageImage);
		}
	}
}
	/**
	 * @see IProgressMonitor#worked(int)
	 */
	public void worked(int work) {
		internalWorked(work);
	}
}
