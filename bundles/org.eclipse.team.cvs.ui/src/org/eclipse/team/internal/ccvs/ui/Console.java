/*******************************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial implementation
 ******************************************************************************/
package org.eclipse.team.internal.ccvs.ui;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.custom.LineStyleEvent;
import org.eclipse.swt.custom.LineStyleListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.CVSStatus;
import org.eclipse.team.internal.ccvs.core.client.listeners.IConsoleListener;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;

/**
 * Console is a view that displays the communication with the CVS server
 */
public class Console extends ViewPart {
	public static final String CONSOLE_ID = "org.eclipse.team.ccvs.ui.console"; //$NON-NLS-1$
	private static final DateFormat TIME_FORMAT = new SimpleDateFormat(Policy.bind("Console.resultTimeFormat")); //$NON-NLS-1$
	private static ConsoleDocument document;
	private static List /* of Console */ instances;
	
	private TextViewer viewer;
	private Color commandColor;
	private Color messageColor;
	private Color errorColor;
	
	private IDocumentListener documentListener;
	private IPropertyChangeListener propertyChangeListener;
	private TextViewerAction copyAction;
	private TextViewerAction selectAllAction;
	private Action clearOutputAction;

	//For buffering console content prior to flushing to view
	private static class PendingConsoleLine {
		public int type;
		public String line;
		public PendingConsoleLine(int type, String line) {
			this.type = type;
			this.line = line;
		}
	}
	private static Vector pendingConsoleData = new Vector();
	private static Runnable aSyncRunnable;
	private static final int MAX_BUFFER_SIZE = 200; //maximum size of buffer if console not open
	
	/*
	 * Called on UI plugin startup.
	 */
	public static void startup() {
		instances = new ArrayList();
		CVSProviderPlugin.getPlugin().setConsoleListener(new ConsoleListener());
	}
	
	/*
	 * Called on UI plugin shutdown.
	 */
	public static void shutdown() {
	 	document = null;
	 	instances = null;
		CVSProviderPlugin.getPlugin().setConsoleListener(null);
	}
	
	public Console() {
	}

	/*
	 * @see WorkbenchPart#dispose
	 */
	public void dispose() {
		// remove listeners
		if (documentListener != null) {
			document.removeDocumentListener(documentListener);
			documentListener = null;
		}
		if (propertyChangeListener != null) {
			getPreferenceStore().removePropertyChangeListener(propertyChangeListener);
			propertyChangeListener = null;
		}
		instances.remove(this);
		//if (instances.isEmpty()) document.clear();
		
		// dispose of allocated colors and fonts
		super.dispose();
		if (commandColor != null) {
			commandColor.dispose();
			commandColor = null;
		}
		if (messageColor != null) {
			messageColor.dispose();
			messageColor = null;
		}
		if (errorColor != null) {
			errorColor.dispose();
			errorColor = null;
		}
	}
	
	/*
	 * @see WorkbenchPart#setFocus()
	 */
	public void setFocus() {
		viewer.getTextWidget().setFocus();
	}

	/*
	 * @see WorkbenchPart#createPartControl(Composite)
	 */
	public void createPartControl(Composite parent) {
		if(document == null) {
			document = new ConsoleDocument();
		}

		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));

		updatePreferences(null);

		// F1 Help
		WorkbenchHelp.setHelp(composite, IHelpContextIds.CONSOLE_VIEW);
			
		// create the viewer
		viewer = new TextViewer(composite, SWT.V_SCROLL | SWT.H_SCROLL);
		GridData data = new GridData(GridData.FILL_BOTH);
		viewer.getControl().setLayoutData(data);
		viewer.setEditable(false);
		viewer.setDocument(document);
		viewer.getTextWidget().setFont(JFaceResources.getFont(ICVSUIConstants.PREF_CONSOLE_FONT));
				
		// add a selection listener to control enablement of the copy action
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				copyAction.update();
			}
		});
		
		// add a line styler for colouring lines according to their type
		viewer.getTextWidget().addLineStyleListener(new LineStyleListener() {
			public void lineGetStyle(LineStyleEvent event) {
				StyleRange style = new StyleRange(event.lineOffset, event.lineText.length(),
					getConsoleLineColor(event.lineOffset), null);
				event.styles = new StyleRange[] { style };
			}
		});

		// create the viewer actions
		createActions();

		// add a property change listener to update when preferences change
		propertyChangeListener = new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				updatePreferences(event.getProperty());
			}
		};
		getPreferenceStore().addPropertyChangeListener(propertyChangeListener);
		
		// add a document listener for auto-scrolling
		documentListener = new IDocumentListener() {
			public void documentAboutToBeChanged(DocumentEvent event) {
			}
			public void documentChanged(DocumentEvent event) {
				if (viewer == null) return;
				// always focus on the last line without changing the horizontal scrolling index
				// we can get away with doing it this way because the viewer is read only
				StyledText styledText = viewer.getTextWidget();
				if (styledText.isDisposed()) return;
				styledText.setTopIndex(styledText.getLineCount());
			}
		};
		document.addDocumentListener(documentListener);

		// we're open -- remember us
		instances.add(this);
		flushConsoleBuffer();	//in case there's anything in the buffer
	}
	
	/**
	 * Create contributed actions
	 */
	private void createActions() {
		CVSUIPlugin plugin = CVSUIPlugin.getPlugin();
		clearOutputAction = new Action(Policy.bind("Console.clearOutput"), plugin.getImageDescriptor(ICVSUIConstants.IMG_CLEAR_ENABLED)) { //$NON-NLS-1$
			public void run() {
				document.clear();
			}
		};
		clearOutputAction.setToolTipText(Policy.bind("Console.clearOutput")); //$NON-NLS-1$
		clearOutputAction.setDisabledImageDescriptor(plugin.getImageDescriptor(ICVSUIConstants.IMG_CLEAR_DISABLED));
		clearOutputAction.setHoverImageDescriptor(plugin.getImageDescriptor(ICVSUIConstants.IMG_CLEAR));
		
		MenuManager mgr = new MenuManager();
		mgr.setRemoveAllWhenShown(true);
		mgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager mgr) {
				fillContextMenu(mgr);
			}
		});
		Menu menu = mgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);

		// Create the local tool bar
		IToolBarManager tbm = getViewSite().getActionBars().getToolBarManager();
		tbm.add(clearOutputAction);
		tbm.update(false);


		// Create actions for the text editor
		IActionBars actionBars = getViewSite().getActionBars();
		
		copyAction = new TextViewerAction(viewer, ITextOperationTarget.COPY);
		copyAction.setText(Policy.bind("Console.copy")); //$NON-NLS-1$
		actionBars.setGlobalActionHandler(ITextEditorActionConstants.COPY, copyAction);
		
		selectAllAction = new TextViewerAction(viewer, ITextOperationTarget.SELECT_ALL);
		selectAllAction.setText(Policy.bind("Console.selectAll")); //$NON-NLS-1$
		actionBars.setGlobalActionHandler(ITextEditorActionConstants.SELECT_ALL, selectAllAction);

		actionBars.updateActionBars();
	}

	/**
	 * Add the actions to the context menu
	 * 
	 * @param manager  the manager of the context menu
	 */
	private void fillContextMenu(IMenuManager manager) {
		manager.add(copyAction);
		manager.add(selectAllAction);
		manager.add(new Separator());
		manager.add(clearOutputAction);
	}

	/**
	 * Returns the color for the line containing the specified offset.
	 */
	private Color getConsoleLineColor(int offset) {
		switch (document.getLineType(offset)) {
			case ConsoleDocument.COMMAND:
			case ConsoleDocument.STATUS:
			case ConsoleDocument.DELIMITER:
				return commandColor;
			case ConsoleDocument.MESSAGE:
				return messageColor;
			case ConsoleDocument.ERROR:
				return errorColor;
			default:
				throw new IllegalStateException();
		}
	}
	
	/**
	 * Updates the console's font and colors.
	 */
	private void updatePreferences(String property) {
		Display display = getViewSite().getShell().getDisplay();
		// update the console colors
		if (property == null ||
			property.equals(ICVSUIConstants.PREF_CONSOLE_COMMAND_COLOR) ||
			property.equals(ICVSUIConstants.PREF_CONSOLE_MESSAGE_COLOR) ||
			property.equals(ICVSUIConstants.PREF_CONSOLE_ERROR_COLOR)) {
			Color oldCommandColor = commandColor;
			Color oldMessageColor = messageColor;
			Color oldErrorColor = errorColor;
			commandColor = createColor(display, ICVSUIConstants.PREF_CONSOLE_COMMAND_COLOR);
			messageColor = createColor(display, ICVSUIConstants.PREF_CONSOLE_MESSAGE_COLOR);
			errorColor = createColor(display, ICVSUIConstants.PREF_CONSOLE_ERROR_COLOR);
			if (oldCommandColor != null) {
				if (viewer != null && ! viewer.getControl().isDisposed()) {
					viewer.refresh();
				}
				oldCommandColor.dispose();
				oldMessageColor.dispose();
				oldErrorColor.dispose();
			}
		}
		// update the console font
		if (property == null ||
				property.equals(ICVSUIConstants.PREF_CONSOLE_FONT)) {
			if (viewer != null && ! viewer.getControl().isDisposed()) {
				viewer.getTextWidget().setFont(JFaceResources.getFont(ICVSUIConstants.PREF_CONSOLE_FONT));
			}
		}
	}
	
	/**
	 * Returns a color instance based on data from a preference field.
	 */
	private Color createColor(Display display, String preference) {
		RGB rgb = PreferenceConverter.getColor(getPreferenceStore(), preference);
		return new Color(display, rgb);
	}

	/**
	 * Returns a font instance based on data from a preference field.
	 */
	private Font createFont(Display display, String preference) {
		FontData fontData = PreferenceConverter.getFontData(getPreferenceStore(), preference);
		return new Font(display, fontData);
	}

	/**
	 * Appends a line to the console if any views are open.
	 */
	private static void appendConsoleLine(final int type, final String line) {
		if (Policy.DEBUG_CONSOLE_BUFFERING) {
			System.out.println("<<Console buffering [" + String.valueOf(type) + "] :" + line);	//$NON-NLS-1$ //$NON-NLS-2$
		}

		//add to end of buffer
		pendingConsoleData.add(
			new PendingConsoleLine(type, line));

		//If no document, ensure buffer size doesn't grow too long, don't flush
		if (document == null) {
			if(pendingConsoleData.size() > MAX_BUFFER_SIZE) {
				//remove extra from front of buffer
				pendingConsoleData.remove(0);
			}				
			return;
		} 
		
		//Do nothing if there is already an aSyncRunnable emptying the buffer
		if(aSyncRunnable != null) return;

		flushConsoleBuffer();
	}
	
	private static void flushConsoleBuffer() {			
		Display display = Display.getCurrent();
		if (display == null) {
			display = Display.getDefault();
		}
		
		if (Policy.DEBUG_CONSOLE_BUFFERING) {
			System.out.println("++Console creating runnable");	//$NON-NLS-1$
		}

		display.asyncExec(aSyncRunnable = new Runnable() {
			public void run() {
				if (Policy.DEBUG_CONSOLE_BUFFERING) {
					System.out.println("==Console running runnable");	//$NON-NLS-1$
				}
				//if we don't have a console open and the pref is to open one, then do so
				while (! pendingConsoleData.isEmpty()) {
					PendingConsoleLine consoleLine = (PendingConsoleLine) pendingConsoleData.remove(0);
					if (Policy.DEBUG_CONSOLE_BUFFERING) {
						System.out.println(">>Console flushing ["  + String.valueOf(consoleLine.type) + "] :" + consoleLine.line);	//$NON-NLS-1$ //$NON-NLS-2$
					}
					document.appendConsoleLine(consoleLine.type, consoleLine.line);					
				}
				if (Policy.DEBUG_CONSOLE_BUFFERING) {
					System.out.println("--Console discarding runnable");	//$NON-NLS-1$
				}
				aSyncRunnable = null;	//flush the instance so that a new one will be created when required
			}
		});
	}

	/**
	 * Makes the CVS Console view visible in the active perspective. If there
	 * isn't a CVS Console view registered <code>null</code> is returned.
	 * Otherwise the opened view part is returned.
	 * 
	 * Must be called from the UI thread.
	 */
	private static Console findInActivePerspective() {
		try {
			IWorkbenchPage page = CVSUIPlugin.getActivePage();
			IViewPart consolePart = page.findView(CONSOLE_ID);
			if (consolePart == null) {
				IWorkbenchPart activePart = page.getActivePart();
				consolePart = page.showView(CONSOLE_ID);
				//restore focus stolen by the creation of the console
				if (activePart != null) page.activate(activePart);
			} else {
				page.bringToTop(consolePart);
			}
			return (Console) consolePart;
		} catch (PartInitException pe) {
			return null;
		}
	}

	/**
	 * Returns the console preference store.
	 */
	private static IPreferenceStore getPreferenceStore() {
		return CVSUIPlugin.getPlugin().getPreferenceStore();
	}

	private static class ConsoleListener implements IConsoleListener {
		private long commandStarted = 0;
		
		public void commandInvoked(String line) {
			commandStarted = System.currentTimeMillis();
			appendConsoleLine(ConsoleDocument.DELIMITER, Policy.bind("Console.preExecutionDelimiter")); //$NON-NLS-1$
			appendConsoleLine(ConsoleDocument.COMMAND, line);
		}
		public void messageLineReceived(String line) {
			appendConsoleLine(ConsoleDocument.MESSAGE, "  " + line); //$NON-NLS-1$
		}
		public void errorLineReceived(String line) {
			appendConsoleLine(ConsoleDocument.ERROR, "  " + line); //$NON-NLS-1$
		}
		public void commandCompleted(IStatus status, Exception exception) {
			long commandRuntime = System.currentTimeMillis() - commandStarted;
			String time;
			try {
				time = TIME_FORMAT.format(new Date(commandRuntime));
			} catch (RuntimeException e) {
				CVSUIPlugin.log(new Status(IStatus.ERROR, CVSUIPlugin.ID, 0, Policy.bind("Console.couldNotFormatTime"), e)); //$NON-NLS-1$
				time = ""; //$NON-NLS-1$
			}
			String statusText;
			if (status != null) {
				if (status.getCode() == CVSStatus.SERVER_ERROR) {
					statusText = Policy.bind("Console.resultServerError", status.getMessage(), time); //$NON-NLS-1$
				} else {
					statusText = Policy.bind("Console.resultOk", time); //$NON-NLS-1$
				}
				appendConsoleLine(ConsoleDocument.STATUS, statusText);
				IStatus[] children = status.getChildren();
				if (children.length == 0) {
					if (!status.isOK())
						appendConsoleLine(ConsoleDocument.STATUS, messageLineForStatus(status));
				} else {
					for (int i = 0; i < children.length; i++) {
						if (!children[i].isOK())
							appendConsoleLine(ConsoleDocument.STATUS, messageLineForStatus(children[i]));
					}
				}
			} else if (exception != null) {
				if (exception instanceof OperationCanceledException) {
					statusText = Policy.bind("Console.resultAborted", time); //$NON-NLS-1$
				} else {
					statusText = Policy.bind("Console.resultException", time); //$NON-NLS-1$
				}
				appendConsoleLine(ConsoleDocument.STATUS, statusText);
			} else {
				statusText = Policy.bind("Console.resultOk", time); //$NON-NLS-1$
			}
			appendConsoleLine(ConsoleDocument.DELIMITER, Policy.bind("Console.postExecutionDelimiter")); //$NON-NLS-1$
			appendConsoleLine(ConsoleDocument.DELIMITER, ""); //$NON-NLS-1$
		}
		/**
		 * Method messageLineForStatus.
		 * @param status
		 */
		private String messageLineForStatus(IStatus status) {
			if (status.getSeverity() == IStatus.ERROR) {
				return Policy.bind("Console.error", status.getMessage()); //$NON-NLS-1$
			} else if (status.getSeverity() == IStatus.WARNING) {
				return Policy.bind("Console.warning", status.getMessage()); //$NON-NLS-1$
			} else if (status.getSeverity() == IStatus.INFO) {
				return Policy.bind("Console.info", status.getMessage()); //$NON-NLS-1$
			}
			return status.getMessage();
		}
	}
}
