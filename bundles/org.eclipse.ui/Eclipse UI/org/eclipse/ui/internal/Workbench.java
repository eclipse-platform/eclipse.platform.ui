package org.eclipse.ui.internal;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.core.boot.*;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.ui.internal.dialogs.*;
import org.eclipse.ui.internal.DetachedWindow;
import org.eclipse.ui.internal.misc.*;
import org.eclipse.ui.internal.model.WorkbenchAdapterBuilder;
import org.eclipse.ui.internal.registry.*;
import org.eclipse.ui.*;
import org.eclipse.ui.part.*;
import org.eclipse.jface.*;
import org.eclipse.jface.window.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.operation.ModalContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.*;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.*;
import org.eclipse.swt.custom.BusyIndicator;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.*;

/**
 * The workbench class represents the top of the ITP user interface.  Its primary
 * responsability is the management of workbench windows and other ISV windows.
 */
public class Workbench implements IWorkbench, 
	IPlatformRunnable, IExecutableExtension
{
	private static final String VERSION_STRING = "0.046";
	private static final String P_PRODUCT_INFO = "productInfo";
	private static final String DEFAULT_PRODUCT_INFO_FILENAME = "product.ini";
	private static final String DEFAULT_WORKBENCH_STATE_FILENAME = "workbench.xml";
	private WindowManager windowManager;
	private EditorHistory editorHistory;
	private boolean runEventLoop;
	private boolean isStarting = false;
	private boolean isClosing = false;
	private IPluginDescriptor startingPlugin; // the plugin which caused the workbench to be instantiated
	private String productInfoFilename;
	private ProductInfo productInfo;
	private String[] commandLineArgs;
	private SplashWindow splashWindow;
/**
 * Workbench constructor comment.
 */
public Workbench() {
	super();
	WorkbenchPlugin.getDefault().setWorkbench(this);
}
/**
 * Get the extenders from the registry and adds them to the 
 * extender manager.
 */   
private void addAdapters() {
	WorkbenchAdapterBuilder builder = new WorkbenchAdapterBuilder();
	builder.registerAdapters();
}
/**
 * Close the workbench
 *
 * Assumes that busy cursor is active.
 */
private boolean busyClose() {
	isClosing = true;
	Platform.run(new SafeRunnableAdapter() {
		public void run() {
			XMLMemento mem = recordWorkbenchState();
			//Save the IMemento to a file.
			saveWorkbenchState(mem);
		}
		public void handleException(Throwable e) {
			message = "An error has occurred";
			if (e != null && e.getMessage() != null)
				message += ": " + e.getMessage();
			if (!message.endsWith("."))
				message += ".";
			message += " See error log for more details. Do you want to exit?";
			if(!MessageDialog.openQuestion(null, "Error", message))
				isClosing = false;
		}
	});
	if(!isClosing)
		return false;
		
	Platform.run(new SafeRunnableAdapter("An error has occurred when closing the workbench") {
		public void run() {
			isClosing = windowManager.close();
		}
	});

	if(!isClosing)
		return false;
		
	if (WorkbenchPlugin.getPluginWorkspace() != null)
		disconnectFromWorkspace();
		
	runEventLoop = false;
	return true;
}
/**
 * Opens a new workbench window and page with a specific perspective.
 *
 * Assumes that busy cursor is active.
 */
private IWorkbenchWindow busyOpenWorkbenchWindow(String perspID, IAdaptable input) 
	throws WorkbenchException 
{
	// Create a workbench window (becomes active window)
	WorkbenchWindow newWindow = new WorkbenchWindow(this, getNewWindowNumber());
	newWindow.create(); // must be created before adding to window manager
	windowManager.add(newWindow);

	// Create the initial page.
	newWindow.openPage(perspID, input);

	// Open after opening page, to avoid flicker.
	newWindow.open();

	return newWindow;
}
/**
 * Closes the workbench.
 */
public boolean close() {
	final boolean [] ret = new boolean[1];;
	BusyIndicator.showWhile(null, new Runnable() {
		public void run() {
			ret[0] = busyClose();
		}
	});
	return ret[0];
}
/*
 * Close the splash window
 */
private void closeSplashWindow() {
	if (splashWindow != null) {
		splashWindow.close();
		splashWindow = null;
	}
}
/**
 * Connect to the core workspace.
 */
private void connectToWorkspace() {
	// Nothing to do right now.
}
/**
 * Disconnect from the core workspace.
 */
private void disconnectFromWorkspace() {
	//Save the workbench.
	final MultiStatus status = new MultiStatus(WorkbenchPlugin.PI_WORKBENCH, 1,
		"Problems occurred while trying to save the state of the workbench.", null);
	IRunnableWithProgress runnable = new IRunnableWithProgress() {
		public void run(IProgressMonitor monitor) {
			try {
				status.merge(ResourcesPlugin.getWorkspace().save(true, monitor));
			} catch (CoreException e) {
				status.merge(e.getStatus());
			}
		}
	};
	try {
		new ProgressMonitorDialog(null).run(false, false, runnable);
	} catch (InvocationTargetException e) {
		status.merge(new Status(IStatus.ERROR, WorkbenchPlugin.PI_WORKBENCH, 1, "Internal Error", e.getTargetException()));
	} catch (InterruptedException e) {
		status.merge(new Status(IStatus.ERROR, WorkbenchPlugin.PI_WORKBENCH, 1, "Internal Error", e));
	}
	ErrorDialog.openError(null,
		"Problems saving workspace",
		null,
		status,
		IStatus.ERROR | IStatus.WARNING);
	if (!status.isOK()) {
		WorkbenchPlugin.log("Problems saving workspace", status);
	}
}
/**
 * @see IWorkbench
 */
public IWorkbenchWindow getActiveWorkbenchWindow() {

	Display display = Display.getCurrent();
	// Display will be null if SWT has not been initialized or
	// this method was called from wrong thread.
	if (display == null) 
		return null; 
	Control shell = display.getActiveShell();
	while (shell != null) {
		Object data = shell.getData();
		if (data instanceof IWorkbenchWindow)
			return (IWorkbenchWindow) data;
		shell = shell.getParent();
	}
	Shell shells[] = display.getShells();
	for (int i = 0; i < shells.length; i++){
		Object data = shells[i].getData();
		if (data instanceof IWorkbenchWindow)
			return (IWorkbenchWindow) data;
	}
	return null;
}
/**
 * Returns the command line arguments, excluding any which were filtered out by the launcher.
 */
public String[] getCommandLineArgs() {
	return commandLineArgs;
}
/**
 * Returns the editor history.
 */
public EditorHistory getEditorHistory() {
	if (editorHistory == null) {
		editorHistory = new EditorHistory();
	}
	return editorHistory;
}
/**
 * Returns the editor registry for the workbench.
 *
 * @return the workbench editor registry
 */
public IEditorRegistry getEditorRegistry() {
	return WorkbenchPlugin.getDefault().getEditorRegistry();
}
/*
 * Returns the number for a new window.  This will be the first
 * number > 0 which is not used to identify another window in
 * the workbench.
 */
private int getNewWindowNumber() {
	// Get window list.
	Window [] windows = windowManager.getWindows();
	int count = windows.length;

	// Create an array of booleans (size = window count).  
	// Cross off every number found in the window list.  
	boolean checkArray [] = new boolean[count];
	for (int nX = 0; nX < count; nX++ ) {
		if (windows[nX] instanceof WorkbenchWindow) {
			WorkbenchWindow ww = (WorkbenchWindow)windows[nX];
			int index = ww.getNumber() - 1;
			if (index >= 0 && index < count)
				checkArray[index] = true;
		}
	}

	// Return first index which is not used.
	// If no empty index was found then every slot is full.
	// Return next index.
	for (int index = 0; index < count; index++) {
		if (!checkArray[index])
			return index + 1;
	}
	return count + 1;
}
/**
 * Returns the perspective registry for the workbench.
 *
 * @return the workbench perspective registry
 */
public IPerspectiveRegistry getPerspectiveRegistry() {
	return WorkbenchPlugin.getDefault().getPerspectiveRegistry();
}
/**
 * Returns the preference manager for the workbench.
 *
 * @return the workbench preference manager
 */
public PreferenceManager getPreferenceManager() {
	return WorkbenchPlugin.getDefault().getPreferenceManager();
}
/**
 * @return the product info object
 */
public ProductInfo getProductInfo() {
	return productInfo;
}
/**
 * Returns the shared images for the workbench.
 *
 * @return the shared image manager
 */
public ISharedImages getSharedImages() {
	return WorkbenchPlugin.getDefault().getSharedImages();
}
/*
 * Return the current window manager being used by the workbench
 */
protected WindowManager getWindowManager() {
	return windowManager;
}
/*
 * Answer the workbench state file.
 */
private File getWorkbenchStateFile() {
	IPath path = WorkbenchPlugin.getDefault().getStateLocation();
	path = path.append(DEFAULT_WORKBENCH_STATE_FILENAME);
	return path.toFile();
}
/**
 * Returns the workbench window count.
 * <p>
 * @return the workbench window count
 */
public int getWorkbenchWindowCount() {
	return windowManager.getWindows().length;
}
/**
 * @see IWorkbench
 */
public IWorkbenchWindow [] getWorkbenchWindows() {
	Window [] windows = windowManager.getWindows();
	IWorkbenchWindow [] dwindows = new IWorkbenchWindow[windows.length];
	System.arraycopy(windows, 0, dwindows, 0, windows.length);
	return dwindows;
}
/**
 * Handles a runtime exception or error which was caught in runEventLoop().
 */
private void handleExceptionInEventLoop(Throwable e) {
	// For the status object, use the exception's message, or the exception name if no message.
	String msg = e.getMessage() == null ? e.toString() : e.getMessage();
	WorkbenchPlugin.log("Unhandled exception caught in event loop.", new Status(IStatus.ERROR, IWorkbenchConstants.PLUGIN_ID, 0, msg, e));
	if (WorkbenchPlugin.DEBUG) {
		e.printStackTrace();
	}
	// Open an error dialog, but don't reveal the internal exception name.
	msg = "An internal error has occurred";
	if (e.getMessage() != null)
		msg += ": " + e.getMessage();
	if (!msg.endsWith("."))
		msg += ".";
	msg += "  See error log for more details.";
	MessageDialog.openError(null, "Internal error", msg);
}
/**
 * Initializes the workbench.
 */
private void init(String[] commandLineArgs) {
	isStarting = true;

	this.commandLineArgs = commandLineArgs;
	String debugOption = Platform.getDebugOption(IWorkbenchConstants.PLUGIN_ID + "/debug");
	if ("true".equalsIgnoreCase(debugOption)) {
		WorkbenchPlugin.DEBUG = true;
		ModalContext.setDebugMode(true);
	}
	readProductInfo();
	openSplashWindow();
	initializeProductImage();
	connectToWorkspace();
	addAdapters();
	windowManager = new WindowManager();
	WorkbenchColors.startup();

	// deadlock code
	boolean avoidDeadlock = true;
	for (int i = 0; i < commandLineArgs.length; i++) {
		if (commandLineArgs[i].equalsIgnoreCase("-allowDeadlock"))
			avoidDeadlock = false;
	}
	if (avoidDeadlock) {
		try {
			Display display = Display.getCurrent();
			UIWorkspaceLock uiLock = new UIWorkspaceLock(WorkbenchPlugin.getPluginWorkspace(), display);
			WorkbenchPlugin.getPluginWorkspace().setWorkspaceLock(uiLock);
			display.setSynchronizer(new UISynchronizer(display, uiLock));
		} catch (CoreException e) {
			e.printStackTrace(System.out);
		}
	}
	
	openWindows();
	openWelcomeDialog();
	
	isStarting = false;
}
/**
 * Initialize the product image obtained from the product info file
 */
private void initializeProductImage() {
	ImageDescriptor descriptor = getProductInfo().getProductImageDescriptor();
	if (descriptor == null) {
		// if none was supplied we use a default
		URL path = null;
		try {
			path = new URL(WorkbenchPlugin.getDefault().getDescriptor().getInstallURL(),WorkbenchImages.ICONS_PATH + "obj16/prod.gif");
		} catch (MalformedURLException e) {};
		descriptor = ImageDescriptor.createFromURL(path);
	}
	WorkbenchImages.getImageRegistry().put(IWorkbenchGraphicConstants.IMG_OBJS_DEFAULT_PROD, descriptor);
	Image image = WorkbenchImages.getImage(IWorkbenchGraphicConstants.IMG_OBJS_DEFAULT_PROD);
	if (image != null) {
		Window.setDefaultImage(image);
	}
}
/**
 * Returns true if the workbench is in the process of closing
 */
public boolean isClosing() {
	return isClosing;
}
/**
 * Returns true if the workbench is in the process of starting
 */
public boolean isStarting() {
	return isStarting;
}
/*
 * Create the initial workbench window.
 * @return true if the open succeeds
 */
private void openFirstTimeWindow() {
	// Create the window.
	WorkbenchWindow newWindow = new WorkbenchWindow(this, getNewWindowNumber());
	newWindow.create();
	windowManager.add(newWindow);

	// Create the initial page.
	try {
		IContainer root = WorkbenchPlugin.getPluginWorkspace().getRoot();
		newWindow.openPage(getPerspectiveRegistry().getDefaultPerspective(), root);
	} catch (WorkbenchException e) {
		MessageDialog.openError(newWindow.getShell(), "Problems Opening Page",
			e.getMessage());
	}
	newWindow.open();
}
/*
 * Create the workbench UI from a persistence file.
 */
private boolean openPreviousWorkbenchState() {
	// Read the workbench state file.
	final File stateFile = getWorkbenchStateFile();
	// If there is no state file return false.
	if (!stateFile.exists())
		return false;

	final boolean result[] = {true};
	Platform.run(new SafeRunnableAdapter("Unable to read workbench state. workbench.xml will be deleted") {
		public void run() throws Exception {
			FileInputStream input = new FileInputStream(stateFile);
			InputStreamReader reader = new InputStreamReader(input);
			// Restore the workbench state.
			IMemento memento = XMLMemento.createReadRoot(reader);
			String version = memento.getString(IWorkbenchConstants.TAG_VERSION);
			if((version == null) || (!version.equals(VERSION_STRING))) {
				reader.close();
				MessageDialog.openError((Shell)null, 
					"Restoring Problems", 
					"Invalid workbench state version. workbench.xml will be deleted");
				stateFile.delete();		
				result[0] = false;
				return;
			}
			restoreState(memento);
			reader.close();
		}
		public void handleException(Throwable e) {
			super.handleException(e);
			result[0] = false;
			stateFile.delete();
		}
			
	});
	return result[0];	
}
/*
 * Open the splash window
 */
private void openSplashWindow() {
	splashWindow = new SplashWindow(getProductInfo().getName(), 
		getProductInfo().getSplashImage());
	splashWindow.open();
}
/**
 * Open the Welcome dialog
 */
private void openWelcomeDialog() {
	
	// Show the quick start wizard.
	if (WorkbenchPlugin.getDefault().getPreferenceStore().
		getBoolean(IWorkbenchPreferenceConstants.WELCOME_DIALOG)) 
	{
		// 1FVKH62: ITPUI:WINNT - quick start should be available on file menu
		QuickStartAction action = new QuickStartAction(this);
		action.openQuickStart(getActiveWorkbenchWindow().getShell(), false);
	}	
	
}
/*
 * Open the workbench UI. 
 */
private void openWindows() {
	if (!openPreviousWorkbenchState())
		openFirstTimeWindow();

	// Close splash.
	closeSplashWindow();
}
/**
 * Opens a new workbench window and page with a specific perspective.
 */
public IWorkbenchWindow openWorkbenchWindow(final String perspID, final IAdaptable input) 
	throws WorkbenchException 
{
	// Run op in busy cursor.
	final Object [] result = new Object[1];
	BusyIndicator.showWhile(null, new Runnable() {
		public void run() {
			try {
				result[0] = busyOpenWorkbenchWindow(perspID, input);
			} catch (WorkbenchException e) {
				result[0] = e;
			}
		}
	});
	if (result[0] instanceof IWorkbenchWindow)
		return (IWorkbenchWindow)result[0];
	else if (result[0] instanceof WorkbenchException)
		throw (WorkbenchException)result[0];
	else
		throw new WorkbenchException("Abnormal Workbench Condition");
}
/**
 * Opens a new window and page with the default perspective.
 */
public IWorkbenchWindow openWorkbenchWindow(IAdaptable input) 
	throws WorkbenchException 
{
	return openWorkbenchWindow(getPerspectiveRegistry().getDefaultPerspective(), 
		input);
}
/**
 * Reads the product info.
 * The product info contains the product name, product images,
 * copyright etc.
 *
 */
private void readProductInfo() {
	productInfo = new ProductInfo();
	URL configURL= null;
	IInstallInfo ii= BootLoader.getInstallationInfo();
	String configName= ii.getApplicationConfigurationIdentifier();
	if (configName != null) {
		configURL = ii.getConfigurationInstallURLFor(configName);
	} else {
		//if there is no configuration, use the workbench
		configURL= WorkbenchPlugin.getDefault().getDescriptor().getInstallURL();
	}
	
	try {	
		productInfo.readINIFile(configURL);
	} catch (CoreException e) {
		IWorkbenchWindow window = getActiveWorkbenchWindow();
		Shell shell = null;
		if (window != null) {
			shell = window.getShell();
		}
//		ErrorDialog.openError(shell, null, "Error reading product info file", e.getStatus());
	}
}
/**
 * Record the workbench UI in a document
 */
private XMLMemento recordWorkbenchState() {
	XMLMemento memento = XMLMemento.createWriteRoot(IWorkbenchConstants.TAG_WORKBENCH);
	saveState(memento);
	return memento;
}
/**
 * @see IPersistable
 */
public void restoreState(IMemento memento) {
	// Get the child windows.
	IMemento [] children = memento.getChildren(IWorkbenchConstants.TAG_WINDOW);
	
	// Read the workbench windows.
	for (int x = 0; x < children.length; x ++) {
		IMemento childMem = children[x];
		WorkbenchWindow newWindow = new WorkbenchWindow(this, getNewWindowNumber());
		newWindow.create();
		newWindow.restoreState(childMem);
		windowManager.add(newWindow);
		newWindow.open();
	}
}
/**
 * Runs the workbench.
 */
public Object run(Object arg) {
	String[] commandLineArgs = new String[0];
	if (arg != null && arg instanceof String[])
		commandLineArgs = (String[]) arg;
	init(commandLineArgs);
	runEventLoop();
	shutdown();
	return null;
}
/**
 * run an event loop for the workbench.
 */
private void runEventLoop() {
	Display display = Display.getCurrent();
	runEventLoop = true;
	while (runEventLoop) {
		try {
			if (!display.readAndDispatch())
				display.sleep();
		} catch (RuntimeException e) {
			handleExceptionInEventLoop(e);
		} catch (VirtualMachineError e) {
			// Can't recover. Throw the exception and let the core handle it and shutdonw.
			throw e;
		} catch (SWTError e) {
			// Can't recover. Throw the exception and let the core handle it and shutdonw.
			throw e;
		} catch (ThreadDeath e) {
			// Don't catch ThreadDeath as this is a normal occurrence when the thread dies
			throw e;
		} catch (Error e) {
			// For instance: it may be a LinkageError in a plugin. Try to keep running.
			handleExceptionInEventLoop(e);
		}
	}
}
/**
 * @see IPersistable
 */
public void saveState(IMemento memento) {
	// Save the version number.
	memento.putString(IWorkbenchConstants.TAG_VERSION, VERSION_STRING);
	
	// Save the workbench windows.
	IWorkbenchWindow [] windows = getWorkbenchWindows();
	for (int nX = 0; nX < windows.length; nX ++) {
		WorkbenchWindow window = (WorkbenchWindow) windows[nX];
		IMemento childMem = memento.createChild(IWorkbenchConstants.TAG_WINDOW);
		window.saveState(childMem);
	}
}
/**
 * Save the workbench UI in a persistence file.
 */
private boolean saveWorkbenchState(XMLMemento memento) {
	// Save it to a file.
	File stateFile = getWorkbenchStateFile();
	try {
		FileWriter writer = new FileWriter(stateFile);
		memento.save(writer);
		writer.close();
	} catch (IOException e) {
		stateFile.delete();		
		MessageDialog.openError((Shell)null, 
			"Saving Problems", 
			"Unable to store workbench state.");
		return false;
	}

	// Success !
	return true;
}
/**
 * @see IExecutableExtension
 */
public void setInitializationData(IConfigurationElement configElement, String propertyName, Object data) {
	startingPlugin = configElement.getDeclaringExtension().getDeclaringPluginDescriptor();
	productInfoFilename = (String)((Map)data).get(P_PRODUCT_INFO);
}
/**
 * shutdown the application.
 */
private void shutdown() {
	WorkbenchColors.shutdown();
	Display display = Display.getCurrent();
	if (display != null) {
		display.dispose();
	}
}
/*
 * Answer true if the state file is good.
 */
private boolean testStateFile() {
	// If there is no state file return false.
	File stateFile = getWorkbenchStateFile();
	if (!stateFile.exists())
		return false;

	// There is a file.  Look for a version tag in the first few lines.
	boolean bVersionTagFound = false;
	BufferedReader reader = null;
	try {
		reader = new BufferedReader(new FileReader(stateFile));
		for (int i = 0; i < 3; i ++) {
			String line = reader.readLine();
			if (line != null && line.indexOf(VERSION_STRING) >= 0) {
				bVersionTagFound = true;
				break;
			}
		}
		reader.close();
	} catch (IOException e) {
		bVersionTagFound = false;
		if (reader != null) {
			try {
				reader.close();
			} catch (IOException e2) {
			}
		}
	}

	// If the version string was found return true, else show an error and return false.
	if (bVersionTagFound) {
		return true;
	} else {
		stateFile.delete();		
		MessageDialog.openError((Shell)null, 
			"Restoring Problem", 
			"Unable to read workbench state.");
		return false;
	}
}
}
