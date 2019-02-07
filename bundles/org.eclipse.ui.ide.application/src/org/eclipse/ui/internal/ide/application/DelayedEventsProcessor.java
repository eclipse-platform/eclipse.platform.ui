/*******************************************************************************
 * Copyright (c) 2010, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Fabio Zadrozny - Bug 305336 - Ability to open a file from command line
 *                      at a specific line/col
 ******************************************************************************/

package org.eclipse.ui.internal.ide.application;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.wizards.datatransfer.SmartImportWizard;
import org.eclipse.urischeme.IUriSchemeProcessor;

/**
 * Helper class used to process delayed events. Events currently supported:
 * <ul>
 * <li>SWT.OpenDocument</li>
 * </ul>
 *
 * @since 3.3
 */
@SuppressWarnings("restriction")
public class DelayedEventsProcessor implements Listener {

	private ArrayList<String> filesToOpen = new ArrayList<>(1);
	private ArrayList<Event> urlsToOpen = new ArrayList<>(1);

	/**
	 * Constructor.
	 *
	 * @param display display used as a source of event
	 */
	public DelayedEventsProcessor(Display display) {
		display.addListener(SWT.OpenDocument, this);
		display.addListener(SWT.OpenUrl, this);
	}

	@Override
	public void handleEvent(Event event) {
		final String path = event.text;
		if (path == null)
			return;
		// If we start supporting events that can arrive on a non-UI thread, the
		// following lines will need to be in a "synchronized" block:
		if (event.type == SWT.OpenUrl) {
			urlsToOpen.add(event);
		} else {
			filesToOpen.add(path);
		}
	}

	/**
	 * Process delayed events.
	 *
	 * @param display display associated with the workbench
	 */
	public void catchUp(Display display) {
		if (filesToOpen.isEmpty() && urlsToOpen.isEmpty())
			return;

		// If we start supporting events that can arrive on a non-UI thread, the
		// following
		// lines will need to be in a "synchronized" block:
		String[] filePaths = new String[filesToOpen.size()];
		filesToOpen.toArray(filePaths);
		filesToOpen.clear();

		for (String filePath : filePaths) {
			openFile(display, filePath);
		}

		Event[] events = new Event[urlsToOpen.size()];
		urlsToOpen.toArray(events);
		urlsToOpen.clear();

		for (Event event : events) {
			openUrl(display, event);
		}
	}

	/**
	 * Handles an URL asynchronously (e.g. clicked in another application).<br>
	 * Handlers are registered via Extension Point
	 * "org.eclipse.ui.uriSchemeHandlers". Matching is done by uri scheme.
	 *
	 * @param display the display to run the asynchronous operation.
	 * @param event   the url to open is contained in <code>event.text</code>.
	 *
	 */
	private static void openUrl(Display display, Event event) {
		display.asyncExec(() -> {
			String uriScheme = getUriSchemeFromEvent(event.text);
			try {
				IUriSchemeProcessor.INSTANCE.handleUri(uriScheme, event.text);

			} catch (CoreException e) {
				String message = NLS.bind(IDEWorkbenchMessages.OpenDelayedUrlAction_cannotHandle, uriScheme);
				IDEWorkbenchPlugin.log(message, new Status(IStatus.ERROR, IDEApplication.PLUGIN_ID, message, e));
				IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				if (window == null) {
					return;
				}
				MessageDialog.open(MessageDialog.ERROR, window.getShell(),
						IDEWorkbenchMessages.OpenDelayedUrlAction_title, message, SWT.SHEET);
			}
		});
	}

	private static String getUriSchemeFromEvent(String uriString) {
		try {
			URI uri = new URI(uriString);
			return uri.getScheme();

		} catch (URISyntaxException e) {
			String message = NLS.bind(IDEWorkbenchMessages.OpenDelayedUrlAction_invalidURL, uriString);
			IDEWorkbenchPlugin.log(message, new Status(IStatus.ERROR, IDEApplication.PLUGIN_ID, message, e));
			IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			if (window == null) {
				return null;
			}
			MessageDialog.open(MessageDialog.ERROR, window.getShell(), IDEWorkbenchMessages.OpenDelayedUrlAction_title,
					message, SWT.SHEET);
			return null;
		}
	}

	/**
	 * Opens a file from a path in the filesystem (asynchronously).
	 *
	 * @param display     the display to run the asynchronous operation.
	 * @param initialPath the path to be used, optionally suffixed with '+line:col'
	 *                    or ':line:col' to open it at the given line/col.
	 *
	 *                    For example: "{@code file.py+10}" will open file.py at
	 *                    line 10; "{@code file.py+10:3}" will open file.py at line
	 *                    10, column 3. Note that the line and column are 1-based.
	 */
	public static void openFile(Display display, final String initialPath) {
		display.asyncExec(new Runnable() {
			@Override
			public void run() {
				IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				if (window == null)
					return;
				// System.err.println(System.currentTimeMillis());
				FileLocationDetails details = FileLocationDetails.resolve(initialPath);
				if (details == null || !details.fileInfo.exists()) {
					String msg = NLS.bind(IDEWorkbenchMessages.OpenDelayedFileAction_message_fileNotFound, initialPath);
					MessageDialog.open(MessageDialog.ERROR, window.getShell(),
							IDEWorkbenchMessages.OpenDelayedFileAction_title, msg, SWT.SHEET);
				} else if (details.fileInfo.isDirectory()) {
					SmartImportWizard wizard = new SmartImportWizard();
					wizard.setInitialImportSource(new File(details.fileStore.toURI()));
					WizardDialog dialog = new WizardDialog(window.getShell(), wizard);
					dialog.setBlockOnOpen(false);
					dialog.open();
				} else {
					IWorkbenchPage page = window.getActivePage();
					if (page == null) {
						String msg = NLS.bind(IDEWorkbenchMessages.OpenDelayedFileAction_message_noWindow,
								details.path);
						MessageDialog.open(MessageDialog.ERROR, window.getShell(),
								IDEWorkbenchMessages.OpenDelayedFileAction_title, msg, SWT.SHEET);
					}
					try {
						IEditorPart openEditor = IDE.openInternalEditorOnFileStore(page, details.fileStore);
						Shell shell = window.getShell();
						if (shell != null) {
							if (shell.getMinimized())
								shell.setMinimized(false);
							shell.forceActive();
						}

						if (details.line >= 1) {
							try {
								// Do things with reflection to avoid having to
								// rely on the text editor plugins.
								Object documentProvider = invoke(openEditor, "getDocumentProvider"); //$NON-NLS-1$

								Object editorInput = invoke(openEditor, "getEditorInput"); //$NON-NLS-1$

								Object document = invoke(documentProvider, "getDocument", new Class[] { Object.class }, //$NON-NLS-1$
										new Object[] { editorInput });

								int numberOfLines = (Integer) invoke(document, "getNumberOfLines"); //$NON-NLS-1$
								if (details.line > numberOfLines) {
									details.line = numberOfLines;
								}
								int lineLength = (Integer) invoke(document, "getLineLength", new Class[] { int.class }, //$NON-NLS-1$
										new Object[] { details.line - 1 });
								if (details.column > lineLength) {
									details.column = lineLength;
								}
								if (details.column < 1) {
									details.column = 1;
								}
								int offset = (Integer) invoke(document, "getLineOffset", new Class[] { int.class }, //$NON-NLS-1$
										new Object[] { (details.line - 1) });
								offset += (details.column - 1);

								invoke(openEditor, "selectAndReveal", new Class[] { int.class, int.class }, //$NON-NLS-1$
										new Object[] { offset, 0 });
							} catch (Exception e) {
								// Ignore (not an ITextEditor).
							}
						}
					} catch (PartInitException e) {
						String msg = NLS.bind(IDEWorkbenchMessages.OpenDelayedFileAction_message_errorOnOpen,
								details.fileStore.getName());
						CoreException eLog = new PartInitException(e.getMessage());
						IDEWorkbenchPlugin.log(msg, new Status(IStatus.ERROR, IDEApplication.PLUGIN_ID, msg, eLog));
						MessageDialog.open(MessageDialog.ERROR, window.getShell(),
								IDEWorkbenchMessages.OpenDelayedFileAction_title, msg, SWT.SHEET);
					}
				}
			}

			/*
			 * Helper function to invoke a method on an object.
			 */
			private Object invoke(Object object, String methodName) throws NoSuchMethodException, SecurityException,
					IllegalAccessException, IllegalArgumentException, InvocationTargetException {
				Method method = object.getClass().getMethod(methodName);
				return method.invoke(object);
			}

			/*
			 * Helper function to invoke a method on an object with arguments.
			 */
			private Object invoke(Object object, String methodName, Class<?>[] classes, Object[] params)
					throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException,
					InvocationTargetException {
				Method method = object.getClass().getMethod(methodName, classes);
				return method.invoke(object, params);
			}
		});
	}
}
