/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.window;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.util.Geometry;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

/**
 * A JFace window is an object that has no visual representation (no widgets)
 * until it is told to open.
 * <p>
 * Creating a window involves the following steps:
 * <ul>
 * <li>creating an instance of a concrete subclass of <code>Window</code>
 * </li>
 * <li>creating the window's shell and widget tree by calling
 * <code>create</code> (optional)</li>
 * <li>assigning the window to a window manager using
 * <code>WindowManager.add</code> (optional)</li>
 * <li>opening the window by calling <code>open</code></li>
 * </ul>
 * Opening the window will create its shell and widget tree if they have not
 * already been created. When the window is closed, the shell and widget tree
 * are disposed of and are no longer referenced, and the window is automatically
 * removed from its window manager. A window may be reopened.
 * </p>
 * <p>
 * The JFace window framework (this package) consists of this class,
 * <code>Window</code>, the abstract base of all windows, and one concrete
 * window classes (<code>ApplicationWindow</code>) which may also be
 * subclassed. Clients may define additional window subclasses as required.
 * </p>
 * <p>
 * The <code>Window</code> class provides the following methods which
 * subclasses may override:
 * <ul>
 * <li><code>close</code>- extend to free other SWT resources</li>
 * <li><code>configureShell</code>- extend or reimplement to set shell
 * properties before window opens</li>
 * <li><code>createContents</code>- extend or reimplement to create controls
 * before window opens</li>
 * <li><code>getInitialSize</code>- reimplement to give the initial size for
 * the shell</li>
 * <li><code>getInitialLocation</code>- reimplement to give the initial
 * location for the shell</li>
 * <li><code>getShellListener</code>- extend or reimplement to receive shell
 * events</li>
 * <li><code>getToolTipText</code>- reimplement to add tool tips</li>
 * <li><code>handleFontChange</code>- reimplement to respond to font changes
 * </li>
 * <li><code>handleShellCloseEvent</code>- extend or reimplement to handle
 * shell closings</li>
 * </ul>
 * </p>
 */
public abstract class Window {

	/**
	 * Standard return code constant (value 0) indicating that the window was
	 * opened.
	 * 
	 * @see #open
	 */
	public static final int OK = 0;

	/**
	 * Standard return code constant (value 1) indicating that the window was
	 * canceled.
	 * 
	 * @see #open
	 */
	public static final int CANCEL = 1;

	/**
	 * An array of images to be used for the window. It is expected that the
	 * array will contain the same icon rendered at different resolutions.
	 */
	private static Image[] defaultImages;

	/**
	 * This interface defines a Exception Handler which can be set as a global
	 * handler and will be called if an exception happens in the event loop.
	 */
	public static interface IExceptionHandler {
		/**
		 * Handle the exception.
		 * 
		 * @param t
		 *            The exception that occured.
		 */
		public void handleException(Throwable t);
	}

	/**
	 * Defines a default exception handler.
	 */
	private static class DefaultExceptionHandler implements IExceptionHandler {
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.window.Window.IExceptionHandler#handleException(java.lang.Throwable)
		 */
		public void handleException(Throwable t) {
			if (t instanceof ThreadDeath)
				// Don't catch ThreadDeath as this is a normal occurrence when
				// the thread dies
				throw (ThreadDeath) t;
			// Try to keep running.
			t.printStackTrace();
		}
	}

	/**
	 * The exception handler for this application.
	 */
	private static IExceptionHandler exceptionHandler = new DefaultExceptionHandler();
	
	/**
	 * The default orientation of the window. By default
	 * it is SWT#NONE but it can also be SWT#LEFT_TO_RIGHT
	 * or SWT#RIGHT_TO_LEFT
	 */
	private static int orientation = SWT.NONE;

	/**
	 * The parent shell.
	 */
	private Shell parentShell;

	/**
	 * Shell style bits.
	 * 
	 * @see #setShellStyle
	 */
	private int shellStyle = SWT.SHELL_TRIM;

	/**
	 * Window manager, or <code>null</code> if none.
	 * 
	 * @see #setWindowManager
	 */
	private WindowManager windowManager;

	/**
	 * Window shell, or <code>null</code> if none.
	 */
	private Shell shell;

	/**
	 * Top level SWT control, or <code>null</code> if none
	 */
	private Control contents;

	/**
	 * Window return code; initially <code>OK</code>.
	 * 
	 * @see #setReturnCode
	 */
	private int returnCode = OK;

	/**
	 * <code>true</code> if the <code>open</code> method should not return
	 * until the window closes, and <code>false</code> if the
	 * <code>open</code> method should return immediately; initially
	 * <code>false</code> (non-blocking).
	 * 
	 * @see #setBlockOnOpen
	 */
	private boolean block = false;

	/**
	 * Internal class for informing this window when fonts change.
	 */
	private class FontChangeListener implements IPropertyChangeListener {
		public void propertyChange(PropertyChangeEvent event) {
			handleFontChange(event);
		}
	}

	/**
	 * Internal font change listener.
	 */
	private FontChangeListener fontChangeListener;

	/**
	 * Internal fields to detect if shell size has been set
	 */
	private boolean resizeHasOccurred = false;

	private Listener resizeListener;

	/**
	 * Creates a window instance, whose shell will be created under the given
	 * parent shell. Note that the window will have no visual representation
	 * until it is told to open. By default, <code>open</code> does not block.
	 * 
	 * @param parentShell
	 *            the parent shell, or <code>null</code> to create a top-level
	 *            shell
	 * @see #setBlockOnOpen
	 * @see #getDefaultOrientation()
	 */
	protected Window(Shell parentShell) {
		this.parentShell = parentShell;
		if(parentShell == null)//Inherit the style from the parent if there is one
			setShellStyle(getShellStyle() | getDefaultOrientation());
	}

	/**
	 * Determines if the window should handle the close event or do nothing.
	 * <p>
	 * The default implementation of this framework method returns
	 * <code>true</code>, which will allow the
	 * <code>handleShellCloseEvent</code> method to be called. Subclasses may
	 * extend or reimplement.
	 * </p>
	 * 
	 * @return whether the window should handle the close event.
	 */
	protected boolean canHandleShellCloseEvent() {
		return true;
	}

	/**
	 * Closes this window, disposes its shell, and removes this window from its
	 * window manager (if it has one).
	 * <p>
	 * This framework method may be extended (<code>super.close</code> must
	 * be called).
	 * </p>
	 * 
	 * @return <code>true</code> if the window is (or was already) closed, and
	 *         <code>false</code> if it is still open
	 */
	public boolean close() {
		if (shell == null || shell.isDisposed())
			return true;

		// stop listening for font changes
		if (fontChangeListener != null) {
			JFaceResources.getFontRegistry().removeListener(fontChangeListener);
			fontChangeListener = null;
		}

		// If we "close" the shell recursion will occur.
		// Instead, we need to "dispose" the shell to remove it from the
		// display.
		shell.dispose();
		shell = null;
		contents = null;

		if (windowManager != null) {
			windowManager.remove(this);
			windowManager = null;
		}
		return true;
	}

	/**
	 * Configures the given shell in preparation for opening this window in it.
	 * <p>
	 * The default implementation of this framework method sets the shell's
	 * image and gives it a grid layout. Subclasses may extend or reimplement.
	 * </p>
	 * 
	 * @param newShell
	 *            the shell
	 */
	protected void configureShell(Shell newShell) {

		// The single image version of this code had a comment related to bug
		// 46624,
		// and some code that did nothing if the stored image was already
		// disposed.
		// The equivalent in the multi-image version seems to be to remove the
		// disposed images from the array passed to the shell.
		if (defaultImages != null && defaultImages.length > 0) {
			ArrayList nonDisposedImages = new ArrayList(defaultImages.length);
			for (int i = 0; i < defaultImages.length; ++i)
				if (defaultImages[i] != null && !defaultImages[i].isDisposed())
					nonDisposedImages.add(defaultImages[i]);

			if (nonDisposedImages.size() <= 0)
				System.err.println("Window.configureShell: images disposed"); //$NON-NLS-1$
			else {
				Image[] array = new Image[nonDisposedImages.size()];
				nonDisposedImages.toArray(array);
				newShell.setImages(array);
			}
		}

		Layout layout = getLayout();
		if (layout != null)
			newShell.setLayout(layout);
	}

	/**
	 * Creates the layout for the shell. The layout created here will be
	 * attached to the composite passed into createContents. The default
	 * implementation returns a GridLayout with no margins. Subclasses that
	 * change the layout type by overriding this method should also override
	 * createContents.
	 * 
	 * <p>
	 * A return value of null indicates that no layout should be attached to the
	 * composite. In this case, the layout may be attached within
	 * createContents.
	 * </p>
	 * 
	 * @return a newly created Layout or null if no layout should be attached.
	 * @since 3.0
	 */
	protected Layout getLayout() {
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		return layout;
	}

	/**
	 * Constrain the shell size to be no larger than the display bounds.
	 * 
	 * @since 2.0
	 */
	protected void constrainShellSize() {
		// limit the shell size to the display size
		Rectangle bounds = shell.getBounds();
		Rectangle constrained = getConstrainedShellBounds(bounds);
		if (!bounds.equals(constrained)) {
			shell.setBounds(constrained);
		}
	}

	/**
	 * Creates this window's widgetry in a new top-level shell.
	 * <p>
	 * The default implementation of this framework method creates this window's
	 * shell (by calling <code>createShell</code>), and its controls (by
	 * calling <code>createContents</code>), then initializes this window's
	 * shell bounds (by calling <code>initializeBounds</code>).
	 * </p>
	 */
	public void create() {
		shell = createShell();
		contents = createContents(shell);

		//initialize the bounds of the shell to that appropriate for the
		// contents
		initializeBounds();
	}

	/**
	 * Creates and returns this window's contents. Subclasses may attach any
	 * number of children to the parent. As a convenience, the return value of
	 * this method will be remembered and returned by subsequent calls to
	 * getControl(). Subclasses may modify the parent's layout if they overload
	 * getLayout() to return null.
	 * 
	 * <p>
	 * It is common practise to create and return a single composite that
	 * contains the entire window contents.
	 * </p>
	 * 
	 * <p>
	 * The default implementation of this framework method creates an instance
	 * of <code>Composite</code>. Subclasses may override.
	 * </p>
	 * 
	 * @param parent
	 *            the parent composite for the controls in this window. The type
	 *            of layout used is determined by getLayout()
	 * 
	 * @return the control that will be returned by subsequent calls to
	 *         getControl()
	 */
	protected Control createContents(Composite parent) {
		// by default, just create a composite
		return new Composite(parent, SWT.NONE);
	}

	/**
	 * Creates and returns this window's shell.
	 * <p>
	 * The default implementation of this framework method creates a new shell
	 * and configures it using <code/>configureShell</code>. Rather than
	 * override this method, subclasses should instead override
	 * <code/>configureShell</code>.
	 * </p>
	 * 
	 * @return the shell
	 */
	protected final Shell createShell() {

		//Create the shell
		Shell newShell = new Shell(getParentShell(), getShellStyle());

		resizeListener = new Listener() {
			public void handleEvent(Event e) {
				resizeHasOccurred = true;
			}
		};

		newShell.addListener(SWT.Resize, resizeListener);
		newShell.setData(this);

		//Add a listener
		newShell.addShellListener(getShellListener());

		//Set the layout
		configureShell(newShell);

		//Register for font changes
		if (fontChangeListener == null) {
			fontChangeListener = new FontChangeListener();
		}
		JFaceResources.getFontRegistry().addListener(fontChangeListener);

		return newShell;
	}

	/**
	 * Returns the top level control for this window. The parent of this control
	 * is the shell.
	 * 
	 * @return the top level control, or <code>null</code> if this window's
	 *         control has not been created yet
	 */
	protected Control getContents() {
		return contents;
	}

	/**
	 * Returns the default image. This is the image that will be used for
	 * windows that have no shell image at the time they are opened. There is no
	 * default image unless one is installed via <code>setDefaultImage</code>.
	 * 
	 * @return the default image, or <code>null</code> if none
	 * @see #setDefaultImage
	 */
	public static Image getDefaultImage() {
		return (defaultImages == null || defaultImages.length < 1) ? null
				: defaultImages[0];
	}

	/**
	 * Returns the array of default images to use for newly opened windows. It
	 * is expected that the array will contain the same icon rendered at
	 * different resolutions.
	 * 
	 * @see org.eclipse.swt.widgets.Decorations#setImages(org.eclipse.swt.graphics.Image[])
	 * 
	 * @return the array of images to be used when a new window is opened
	 * @see #setDefaultImages
	 * @since 3.0
	 */
	public static Image[] getDefaultImages() {
		return (defaultImages == null ? new Image[0] : defaultImages);
	}

	/**
	 * Returns the initial location to use for the shell. The default
	 * implementation centers the shell horizontally (1/2 of the difference to
	 * the left and 1/2 to the right) and vertically (1/3 above and 2/3 below)
	 * relative to the parent shell, or display bounds if there is no parent
	 * shell.
	 * 
	 * @param initialSize
	 *            the initial size of the shell, as returned by
	 *            <code>getInitialSize</code>.
	 * @return the initial location of the shell
	 */
	protected Point getInitialLocation(Point initialSize) {
		Composite parent = shell.getParent();

		Monitor monitor = shell.getDisplay().getPrimaryMonitor();
		if (parent != null) {
			monitor = parent.getMonitor();
		}

		Rectangle monitorBounds = monitor.getClientArea();
		Point centerPoint;
		if (parent != null) {
			centerPoint = Geometry.centerPoint(parent.getBounds());
		} else {
			centerPoint = Geometry.centerPoint(monitorBounds);
		}

		return new Point(centerPoint.x - (initialSize.x / 2), Math.max(
				monitorBounds.y, Math.min(centerPoint.y
						- (initialSize.y * 2 / 3), monitorBounds.y
						+ monitorBounds.height - initialSize.y)));
	}

	/**
	 * Returns the initial size to use for the shell. The default implementation
	 * returns the preferred size of the shell, using
	 * <code>Shell.computeSize(SWT.DEFAULT, SWT.DEFAULT, true)</code>.
	 * 
	 * @return the initial size of the shell
	 */
	protected Point getInitialSize() {
		return shell.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
	}

	/**
	 * Returns parent shell, under which this window's shell is created.
	 * 
	 * @return the parent shell, or <code>null</code> if there is no parent
	 *         shell
	 */
	protected Shell getParentShell() {
		return parentShell;
	}

	/**
	 * Returns this window's return code. A window's return codes are
	 * window-specific, although two standard return codes are predefined:
	 * <code>OK</code> and <code>CANCEL</code>.
	 * 
	 * @return the return code
	 */
	public int getReturnCode() {
		return returnCode;
	}

	/**
	 * Returns this window's shell.
	 * 
	 * @return this window's shell, or <code>null</code> if this window's
	 *         shell has not been created yet
	 */
	public Shell getShell() {
		return shell;
	}

	/**
	 * Returns a shell listener. This shell listener gets registered with this
	 * window's shell.
	 * <p>
	 * The default implementation of this framework method returns a new
	 * listener that makes this window the active window for its window manager
	 * (if it has one) when the shell is activated, and calls the framework
	 * method <code>handleShellCloseEvent</code> when the shell is closed.
	 * Subclasses may extend or reimplement.
	 * </p>
	 * 
	 * @return a shell listener
	 */
	protected ShellListener getShellListener() {
		return new ShellAdapter() {
			public void shellClosed(ShellEvent event) {
				event.doit = false; // don't close now
				if (canHandleShellCloseEvent())
					handleShellCloseEvent();
			}
		};
	}

	/**
	 * Returns the shell style bits.
	 * <p>
	 * The default value is <code>SWT.CLOSE|SWT.MIN|SWT.MAX|SWT.RESIZE</code>.
	 * Subclassers should call <code>setShellStyle</code> to change this
	 * value, rather than overriding this method.
	 * </p>
	 * 
	 * @return the shell style bits
	 */
	protected int getShellStyle() {
		return shellStyle;
	}

	/**
	 * Returns the window manager of this window.
	 * 
	 * @return the WindowManager, or <code>null</code> if none
	 */
	public WindowManager getWindowManager() {
		return windowManager;
	}

	/**
	 * Notifies of a font property change.
	 * <p>
	 * The default implementation of this framework method does nothing.
	 * Subclasses may reimplement.
	 * </p>
	 * 
	 * @param event
	 *            the property change event detailing what changed
	 */
	protected void handleFontChange(PropertyChangeEvent event) {
		// do nothing
	}

	/**
	 * Notifies that the window's close button was pressed, the close menu was
	 * selected, or the ESCAPE key pressed.
	 * <p>
	 * The default implementation of this framework method sets the window's
	 * return code to <code>CANCEL</code> and closes the window using
	 * <code>close</code>. Subclasses may extend or reimplement.
	 * </p>
	 */
	protected void handleShellCloseEvent() {
		setReturnCode(CANCEL);
		close();
	}

	/**
	 * Initializes the location and size of this window's SWT shell after it has
	 * been created.
	 * <p>
	 * This framework method is called by the <code>create</code> framework
	 * method. The default implementation calls <code>getInitialSize</code>
	 * and <code>getInitialLocation</code> and passes the results to
	 * <code>Shell.setBounds</code>. This is only done if the bounds of the
	 * shell have not already been modified. Subclasses may extend or
	 * reimplement.
	 * </p>
	 */
	protected void initializeBounds() {
		if (resizeListener != null) {
			shell.removeListener(SWT.Resize, resizeListener);
		}
		if (resizeHasOccurred) { // Check if shell size has been set already.
			return;
		}

		Point size = getInitialSize();
		Point location = getInitialLocation(size);
		shell.setBounds(getConstrainedShellBounds(new Rectangle(location.x,
				location.y, size.x, size.y)));
	}

	/**
	 * Opens this window, creating it first if it has not yet been created.
	 * <p>
	 * If this window has been configured to block on open (
	 * <code>setBlockOnOpen</code>), this method waits until the window is
	 * closed by the end user, and then it returns the window's return code;
	 * otherwise, this method returns immediately. A window's return codes are
	 * window-specific, although two standard return codes are predefined:
	 * <code>OK</code> and <code>CANCEL</code>.
	 * </p>
	 * 
	 * @return the return code
	 * 
	 * @see #create()
	 */
	public int open() {

		if (shell == null) {
			// create the window
			create();
		}

		// limit the shell size to the display size
		constrainShellSize();

		// open the window
		shell.open();

		// run the event loop if specified
		if (block)
			runEventLoop(shell);

		return returnCode;
	}

	/**
	 * Runs the event loop for the given shell.
	 * 
	 * @param loopShell
	 *            the shell
	 */
	private void runEventLoop(Shell loopShell) {

		//Use the display provided by the shell if possible
		Display display;
		if (shell == null)
			display = Display.getCurrent();
		else
			display = loopShell.getDisplay();

		while (loopShell != null && !loopShell.isDisposed()) {
			try {
				if (!display.readAndDispatch())
					display.sleep();
			} catch (Throwable e) {
				exceptionHandler.handleException(e);
			}
		}
		display.update();
	}

	/**
	 * Sets whether the <code>open</code> method should block until the window
	 * closes.
	 * 
	 * @param shouldBlock
	 *            <code>true</code> if the <code>open</code> method should
	 *            not return until the window closes, and <code>false</code>
	 *            if the <code>open</code> method should return immediately
	 */
	public void setBlockOnOpen(boolean shouldBlock) {
		block = shouldBlock;
	}

	/**
	 * Sets the default image. This is the image that will be used for windows
	 * that have no shell image at the time they are opened. There is no default
	 * image unless one is installed via this method.
	 * 
	 * @param image
	 *            the default image, or <code>null</code> if none
	 */
	public static void setDefaultImage(Image image) {
		defaultImages = image == null ? null : new Image[] { image };
	}

	/**
	 * Sets the array of default images to use for newly opened windows. It is
	 * expected that the array will contain the same icon rendered at different
	 * resolutions.
	 * 
	 * @see org.eclipse.swt.widgets.Decorations#setImages(org.eclipse.swt.graphics.Image[])
	 * 
	 * @param images
	 *            the array of images to be used when this window is opened
	 * @since 3.0
	 */
	public static void setDefaultImages(Image[] images) {
		Image[] newArray = new Image[images.length];
		System.arraycopy(images, 0, newArray, 0, newArray.length);
		defaultImages = newArray;
	}
	
	/**
     * Changes the parent shell. This is only safe to use when the shell is not
     * yet realized (i.e., created). Once the shell is created, it must be
     * disposed (i.e., closed) before this method can be called.
     * 
     * @param newParentShell
     *            The new parent shell; this value may be <code>null</code> if
     *            there is to be no parent.
     * @since 3.1
     */
    protected void setParentShell(final Shell newParentShell) {
        Assert.isTrue((shell == null), "There must not be an existing shell."); //$NON-NLS-1$
        parentShell = newParentShell;
    }

	/**
	 * Sets this window's return code. The return code is automatically returned
	 * by <code>open</code> if block on open is enabled. For non-blocking
	 * opens, the return code needs to be retrieved manually using
	 * <code>getReturnCode</code>.
	 * 
	 * @param code
	 *            the return code
	 */
	protected void setReturnCode(int code) {
		returnCode = code;
	}

	/**
	 * Returns the monitor whose client area contains the given point. If no
	 * monitor contains the point, returns the monitor that is closest to the
	 * point. If this is ever made public, it should be moved into a separate
	 * utility class.
	 * 
	 * @param toSearch
	 *            point to find (display coordinates)
	 * @param toFind
	 *            point to find (display coordinates)
	 * @return the montor closest to the given point
	 */
	private static Monitor getClosestMonitor(Display toSearch, Point toFind) {
		int closest = Integer.MAX_VALUE;

		Monitor[] monitors = toSearch.getMonitors();
		Monitor result = monitors[0];

		for (int idx = 0; idx < monitors.length; idx++) {
			Monitor current = monitors[idx];

			Rectangle clientArea = current.getClientArea();

			if (clientArea.contains(toFind)) {
				return current;
			}

			int distance = Geometry.distanceSquared(Geometry
					.centerPoint(clientArea), toFind);
			if (distance < closest) {
				closest = distance;
				result = current;
			}
		}

		return result;
	}

	/**
	 * Given the desired position of the window, this method returns an adjusted
	 * position such that the window is no larger than its monitor, and does not
	 * extend beyond the edge of the monitor. This is used for computing the
	 * initial window position, and subclasses can use this as a utility method
	 * if they want to limit the region in which the window may be moved.
	 * 
	 * @param preferredSize
	 *            the preferred position of the window
	 * @return a rectangle as close as possible to preferredSize that does not
	 *         extend outside the monitor
	 * 
	 * @since 3.0
	 */
	protected Rectangle getConstrainedShellBounds(Rectangle preferredSize) {
		Rectangle result = new Rectangle(preferredSize.x, preferredSize.y,
				preferredSize.width, preferredSize.height);

		Monitor mon = getClosestMonitor(getShell().getDisplay(), Geometry
				.centerPoint(result));

		Rectangle bounds = mon.getClientArea();

		if (result.height > bounds.height) {
			result.height = bounds.height;
		}

		if (result.width > bounds.width) {
			result.width = bounds.width;
		}

		result.x = Math.max(bounds.x, Math.min(result.x, bounds.x
				+ bounds.width - result.width));
		result.y = Math.max(bounds.y, Math.min(result.y, bounds.y
				+ bounds.height - result.height));

		return result;
	}

	/**
	 * Sets the shell style bits. This method has no effect after the shell is
	 * created.
	 * <p>
	 * The shell style bits are used by the framework method
	 * <code>createShell</code> when creating this window's shell.
	 * </p>
	 * 
	 * @param newShellStyle
	 *            the new shell style bits
	 */
	protected void setShellStyle(int newShellStyle) {
		shellStyle = newShellStyle;
	}

	/**
	 * Sets the window manager of this window.
	 * <p>
	 * Note that this method is used by <code>WindowManager</code> to maintain
	 * a backpointer. Clients must not call the method directly.
	 * </p>
	 * 
	 * @param manager
	 *            the window manager, or <code>null</code> if none
	 */
	public void setWindowManager(WindowManager manager) {
		windowManager = manager;

		// Code to detect invalid usage

		if (manager != null) {
			Window[] windows = manager.getWindows();
			for (int i = 0; i < windows.length; i++) {
				if (windows[i] == this)
					return;
			}
			manager.add(this);
		}
	}

	/**
	 * Sets the exception handler for this application.
	 * <p>
	 * Note that only one handler may be set. Other calls to this method will be
	 * ignored.
	 * <p>
	 * 
	 * @param handler
	 *            the exception handler for the application.
	 */
	public static void setExceptionHandler(IExceptionHandler handler) {
		if (exceptionHandler instanceof DefaultExceptionHandler)
			exceptionHandler = handler;
	}

	/**
	 * Gets the default orientation for windows. If it is not
	 * set the default value will be unspecified (SWT#NONE).
	 * 
	 * <strong>NOTE</strong> This API is experimental and may 
	 * be subject to change during the development cycle for
	 * Eclipse 3.1.
	 * 
	 * @return SWT#NONE, SWT.RIGHT_TO_LEFT or SWT.LEFT_TO_RIGHT
	 * @see SWT#RIGHT_TO_LEFT
	 * @see SWT#LEFT_TO_RIGHT
	 * @see SWT#NONE
	 * @since 3.1
	 */
	public static int getDefaultOrientation() {
		return orientation;

	}

	/**
	 * Sets the default orientation of windows.
	 * @param defaultOrientation one of 
	 * 	SWT#RIGHT_TO_LEFT, SWT#LEFT_TO_RIGHT ,SWT#NONE
	 * @see SWT#RIGHT_TO_LEFT
	 * @see SWT#LEFT_TO_RIGHT
	 * @see SWT#NONE
	 * @since 3.1
	 */
	public static void setDefaultOrientation(int defaultOrientation) {
		orientation = defaultOrientation;
		
	}

}