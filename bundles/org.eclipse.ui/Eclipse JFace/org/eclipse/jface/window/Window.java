package org.eclipse.jface.window;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.events.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.GridLayout;

/**
 * A JFace window is an object that has no visual representation (no widgets)
 * until it is told to open.
 * <p>
 * Creating a window involves the following steps:
 * <ul>
 *   <li>creating an instance of a concrete subclass of <code>Window</code>
 *   </li>
 *   <li>creating the window's shell and widget tree by calling
 *     <code>create</code> (optional)
 *   </li>
 *   <li>assigning the window to a window manager using 
 *     <code>WindowManager.add</code> (optional)
 *   </li>
 *   <li>opening the window by calling <code>open</code>
 *   </li>
 * </ul>
 * Opening the window will create its shell and widget tree if they have not
 * already been created. When the window is closed, the shell and widget tree
 * are disposed of and are no longer referenced, and the window is automatically
 * removed from its window manager. A window may be reopened.
 * </p>
 * <p>
 * The JFace window framework (this package) consists of this class, 
 * <code>Window</code>, the abstract base of all windows, and one concrete
 * window classes (<code>ApplicationWindow</code>) which may also be subclassed.
 * Clients may define additional window subclasses as required.
 * </p>
 * <p>
 * The <code>Window</code> class provides the following methods which subclasses
 * may override:
 * <ul>
 *   <li><code>close</code> - extend to free other SWT resources</li>
 *   <li><code>configureShell</code> - extend or reimplement to set shell
 *      properties before window opens</li>
 *   <li><code>createContents</code> - extend or reimplement to create
 *      controls before window opens</li>
 *   <li><code>getInitialSize</code> - reimplement to give the initial 
 *      size for the shell</li>
 *   <li><code>getInitialLocation</code> - reimplement to give the initial 
 *      location for the shell</li>
 *   <li><code>getShellListener</code> - extend or reimplement to receive
 *      shell events</li>
 *   <li><code>getToolTipText</code> - reimplement to add tool tips</li>
 *   <li><code>handleFontChange</code> - reimplement to respond to font
 *      changes</li>
 *   <li><code>handleShellCloseEvent</code> - extend or reimplement to handle
 *      shell closings</li>
 * </ul>
 * </p>
 */
public abstract class Window {
	
	/**
	 * Standard return code constant (value 0) indicating that the
	 * window was opened.
	 *
	 * @see #open
	 */
	public static final int OK = 0;

	/**
	 * Standard return code constant (value 1) indicating that the
	 * window was canceled.
	 *
	 * @see #open
	 */
	public static final int CANCEL = 1;
	
	/**
	 * Default title image, or <code>null</code> if none.
	 * 
	 * @see #setDefaultImage
	 */
	private static Image defaultImage;
	/**
	 * This interface defines a Exception Handler which 
	 * can be set as a global handler and will be called
	 * if an exception happens in the event loop.
	 */
	public static interface IExceptionHandler {
		public void handleException(Throwable t);
	}
	/**
	 * Defines a default exception handler.
	 */
	private static class DefaultExceptionHandler implements IExceptionHandler {
		public void handleException(Throwable t) {
			if(t instanceof ThreadDeath)
				// Don't catch ThreadDeath as this is a normal occurrence when the thread dies
				throw (ThreadDeath)t;
			else
				// Try to keep running.
				t.printStackTrace();
		}
	}
	/**
	 * The exception handler for this application.
	 */
	private static IExceptionHandler exceptionHandler = new DefaultExceptionHandler();
	
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
	 * <code>true</code> if the <code>open</code> method should
	 * not return until the window closes, and <code>false</code> if the
	 * <code>open</code> method should return immediately;
	 * initially <code>false</code> (non-blocking).
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
 * Creates a window instance, whose shell will be created under the
 * given parent shell.
 * Note that the window will have no visual representation until it is told 
 * to open. By default, <code>open</code> does not block.
 *
 * @param parentShell the parent shell, or <code>null</code> to create a top-level shell
 * @see #setBlockOnOpen
 */
protected Window(Shell parentShell) {
	this.parentShell = parentShell;
}
/**
 * Closes this window, disposes its shell, and removes this
 * window from its window manager (if it has one).
 * <p>
 * This framework method may be extended (<code>super.close</code> must be 
 * called).
 * </p>
 *
 * @return <code>true</code> if the window is (or was already) closed, 
 *   and <code>false</code> if it is still open
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
	// Instead, we need to "dispose" the shell to remove it from the display.
	shell.dispose();
	shell = null;

	if (windowManager != null) {
		windowManager.remove(this);
		windowManager = null;
	}
	return true;		
}
/**
 * Configures the given shell in preparation for opening this window
 * in it.
 * <p>
 * The default implementation of this framework method
 * sets the shell's image and gives it a grid layout. 
 * Subclasses may extend or reimplement.
 * </p>
 * 
 * @param newShell the shell
 */
protected void configureShell(Shell newShell) {

	if (defaultImage != null) {	
		newShell.setImage(defaultImage);
	}

	GridLayout layout = new GridLayout();
	layout.marginHeight = 0;
	layout.marginWidth = 0;
	newShell.setLayout(layout);
}
/**
 * Constrain the shell size to be no larger than the display bounds.
 * Reduce the shell size and move its origin as required.
 * 
 * @since 2.0
 */
protected void constrainShellSize() {
	// limit the shell size to the display size
	Point size = shell.getSize();
	Rectangle bounds = shell.getDisplay().getClientArea();
	int newX = Math.min(size.x, bounds.width);
	int newY = Math.min(size.y, bounds.height);
	if (size.x != newX || size.y != newY)
		shell.setSize(newX, newY);
}
/**
 * Creates this window's widgetry in a new top-level shell.
 * <p>
 * The default implementation of this framework method
 * creates this window's shell (by calling <code>createShell</code>),
 * and its controls (by calling <code>createContents</code>),
 * then initializes this window's shell bounds 
 * (by calling <code>initializeBounds</code>).
 * </p>
 */
public void create() {
	shell = createShell();
	contents = createContents(shell);

	//initialize the bounds of the shell to that appropriate for the contents
	initializeBounds();
}
/**
 * Creates and returns this window's contents.
 * <p>
 * The default implementation of this framework method
 * creates an instance of <code>Composite</code>.
 * Subclasses may override.
 * </p>
 * 
 * @return the control
 */
protected Control createContents(Composite parent) {
	// by default, just create a composite 
	return new Composite(parent, SWT.NONE);
}
/**
 * Creates and returns this window's shell.
 * <p>
 * The default implementation of this framework method creates
 * a new shell and configures it using <code/>configureShell</code>. Rather 
 * than override this method, subclasses should instead override 
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
	
	newShell.addListener(SWT.Resize,resizeListener);
	newShell.setData(this);

	//Add a listener
	newShell.addShellListener(getShellListener());
	
	//Set the layout
	configureShell(newShell);
		
	//Register for font changes
	if (fontChangeListener == null) {
		fontChangeListener= new FontChangeListener();
	}
	JFaceResources.getFontRegistry().addListener(fontChangeListener);

	return newShell;
}
/**
 * Returns the top level control for this window.
 * The parent of this control is the shell.
 *
 * @return the top level control, or <code>null</code> if this window's
 *   control has not been created yet
 */
protected Control getContents() {
	return contents;
}
/**
 * Returns the default image. This is the image that will
 * be used for windows that have no shell image at the time they
 * are opened. There is no default image unless one is
 * installed via <code>setDefaultImage</code>.
 *
 * @return the default  image, or <code>null</code> if none
 * @see #setDefaultImage
 */
public static Image getDefaultImage() {
	return defaultImage;
}
/**
 * Returns the initial location to use for the shell.
 * The default implementation centers the shell horizontally 
 * (1/2 of the difference to the left and 1/2 to the right)
 * and vertically (1/3 above and 2/3 below) relative to the parent shell, 
 * or display bounds if there is no parent shell.
 *
 * @param initialSize the initial size of the shell, as returned by <code>getInitialSize</code>.
 * @return the initial location of the shell
 */
protected Point getInitialLocation(Point initialSize) {
	Composite parentShell = shell.getParent();
	Rectangle containerBounds = (parentShell != null) ? parentShell.getBounds() : shell.getDisplay().getClientArea();
	int x = Math.max(0, containerBounds.x + (containerBounds.width - initialSize.x) / 2);
	int y = Math.max(0, containerBounds.y + (containerBounds.height - initialSize.y) / 3);
	return new Point(x, y);
}
/**
 * Returns the initial size to use for the shell.
 * The default implementation returns the preferred size of the shell,
 * using <code>Shell.computeSize(SWT.DEFAULT, SWT.DEFAULT, true)</code>.
 *
 * @return the initial size of the shell
 */
protected Point getInitialSize() {
	return shell.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
}
/**
 * Returns parent shell, under which this window's shell is created.
 *
 * @return the parent shell, or <code>null</code> if there is no parent shell
 */
protected Shell getParentShell() {
	return parentShell;
}
/**
 * Returns this window's return code.
 * A window's return codes are window-specific, although two standard
 * return codes are predefined: <code>OK</code> and <code>CANCEL</code>.
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
 *   shell has not been created yet
 */
public Shell getShell() {
	return shell;
}
/**
 * Returns a shell listener. This shell listener gets registered
 * with this window's shell.
 * <p>
 * The default implementation of this framework method
 * returns a new listener that makes this window the
 * active window for its window manager (if it has one)
 * when the shell is activated, and calls the framework
 * method <code>handleShellCloseEvent</code> when the
 * shell is closed. Subclasses may extend or reimplement.
 * </p>
 *
 * @return a shell listener
 */
protected ShellListener getShellListener() {
	return new ShellAdapter() {
		public void shellClosed(ShellEvent event) {
			event.doit= false;	// don't close now
			handleShellCloseEvent();
		}
	};
}
/**
 * Returns the shell style bits.
 * <p>
 * The default value is <code>SWT.CLOSE|SWT.MIN|SWT.MAX|SWT.RESIZE</code>.
 * Subclassers should call <code>setShellStyle</code> to
 * change this value, rather than overriding this method.
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
 * The default implementation of this framework method
 * does nothing. Subclasses may reimplement.
 * </p>
 *
 * @param event the property change event detailing what changed
 */
protected void handleFontChange(PropertyChangeEvent event) {
	// do nothing
}
/**
 * Notifies that the window's close button was pressed, 
 * the close menu was selected, or the ESCAPE key pressed.
 * <p>
 * The default implementation of this framework method
 * sets the window's return code to <code>CANCEL</code>
 * and closes the window using <code>close</code>.
 * Subclasses may extend or reimplement.
 * </p>
 */
protected void handleShellCloseEvent() {
	setReturnCode(CANCEL);
	close();
}
/**
 * Initializes the location and size of this window's SWT shell 
 * after it has been created.
 * <p>
 * This framework method is called by the <code>create</code> framework method. 
 * The default implementation calls <code>getInitialSize</code> and
 * <code>getInitialLocation</code> and passes the results to <code>Shell.setBounds</code>.
 * This is only done if the bounds of the shell have not already been modified.
 * Subclasses may extend or reimplement. 
 * </p>
 */
protected void initializeBounds() {


	if (resizeListener != null) {
		shell.removeListener(SWT.RESIZE,resizeListener);
	}
	
	if (resizeHasOccurred) { // Check if shell size has been set already.
		return;
	}

	Point size = getInitialSize();
	Point location = getInitialLocation(size);

	shell.setBounds(location.x, location.y, size.x, size.y);
}
/**
 * Opens this window, creating it first if it has not yet been created.
 * <p>
 * If this window has been configured to block on open
 * (<code>setBlockOnOpen</code>), this method waits until
 * the window is closed by the end user, and then it returns the window's
 * return code; otherwise, this method returns immediately.
 * A window's return codes are window-specific, although two standard
 * return codes are predefined: <code>OK</code> and <code>CANCEL</code>.
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
 * @param shell the shell
 */
private void runEventLoop(Shell shell) {
	Display display = Display.getCurrent();
	while (shell != null && ! shell.isDisposed()) {
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
 * Sets whether the <code>open</code> method should block
 * until the window closes.
 *
 * @param shouldBlock <code>true</code> if the
 *   <code>open</code> method should not return 
 *   until the window closes, and <code>false</code> if the
 *   <code>open</code> method should return immediately
 */
public void setBlockOnOpen(boolean shouldBlock) {
	block = shouldBlock;
}
/**
 * Sets the default image. This is the image that will
 * be used for windows that have no shell image at the time they
 * are opened. There is no default image unless one is
 * installed via this method.
 *
 * @param image the default image, or <code>null</code> if none
 */
public static void setDefaultImage(Image image) {
	defaultImage= image;
}
/**
 * Sets this window's return code. The return code is automatically returned
 * by <code>open</code> if block on open is enabled. For non-blocking
 * opens, the return code needs to be retrieved manually using 
 * <code>getReturnCode</code>.
 *
 * @param code the return code
 */
protected void setReturnCode(int code) {
	returnCode = code;
}
/**
 * Sets the shell style bits.
 * This method has no effect after the shell is created.
 * <p>
 * The shell style bits are used by the framework method
 * <code>createShell</code> when creating this window's shell.
 * </p>
 *
 * @param newShellStyle the new shell style bits
 */
protected void setShellStyle(int newShellStyle) {
	shellStyle = newShellStyle;
}
/**
 * Sets the window manager of this window.
 * <p>
 * Note that this method is used by <code>WindowManager</code> to maintain a
 * backpointer. Clients must not call the method directly.
 * </p>
 *
 * @param manager the window manager, or <code>null</code> if none
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
 * Note that only one handler may be set. Other calls to this method will be ignored.
 * <p>
 * @param the exception handler for the application.
 */
public static void setExceptionHandler(IExceptionHandler handler) {
	if(exceptionHandler instanceof DefaultExceptionHandler)
		exceptionHandler = handler;
}

}
