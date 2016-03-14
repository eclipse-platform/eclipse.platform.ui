/*******************************************************************************
 * Copyright (c) 2010, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Fabio Zadrozny - Bug 305336 - Ability to open a file from command line
 *                      at a specific line/col
 ******************************************************************************/

package org.eclipse.ui.internal.ide.application;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
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

/**
 * Helper class used to process delayed events.
 * Events currently supported:
 * <ul>
 * <li>SWT.OpenDocument</li>
 * </ul>
 * @since 3.3
 */
public class DelayedEventsProcessor implements Listener {
	private ArrayList<String> filesToOpen = new ArrayList<>(1);

	/**
	 * Constructor.
	 * @param display display used as a source of event
	 */
	public DelayedEventsProcessor(Display display) {
		display.addListener(SWT.OpenDocument, this);
	}

	@Override
	public void handleEvent(Event event) {
		final String path = event.text;
		if (path == null)
			return;
		// If we start supporting events that can arrive on a non-UI thread, the following
		// line will need to be in a "synchronized" block:
		filesToOpen.add(path);
	}

	/**
	 * Process delayed events.
	 * @param display display associated with the workbench
	 */
	public void catchUp(Display display) {
		if (filesToOpen.isEmpty())
			return;

		// If we start supporting events that can arrive on a non-UI thread, the following
		// lines will need to be in a "synchronized" block:
		String[] filePaths = new String[filesToOpen.size()];
		filesToOpen.toArray(filePaths);
		filesToOpen.clear();

		for(int i = 0; i < filePaths.length; i++) {
			openFile(display, filePaths[i]);
		}
	}

	/**
	 * Opens a file from a path in the filesystem (asynchronously).
	 *
	 * @param display
	 *            the display to run the asynchronous operation.
	 * @param initialPath
	 *            the path to be used, optionally suffixed with '+line:col' or
	 *            ':line:col' to open it at the given line/col.
	 *
	 *            For example: "{@code file.py+10}" will open file.py at line
	 *            10; "{@code file.py+10:3}" will open file.py at line 10,
	 *            column 3. Note that the line and column are 1-based.
	 */
	public static void openFile(Display display, final String initialPath) {
		display.asyncExec(new Runnable() {
			@Override
			public void run() {
				IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				if (window == null)
					return;
				FileLocationDetails details = FileLocationDetails.resolve(initialPath);
				if (details == null || details.fileInfo.isDirectory() || !details.fileInfo.exists()) {
					String msg = NLS.bind(IDEWorkbenchMessages.OpenDelayedFileAction_message_fileNotFound, initialPath);
					MessageDialog.open(MessageDialog.ERROR, window.getShell(),
							IDEWorkbenchMessages.OpenDelayedFileAction_title, msg, SWT.SHEET);
				} else {
					IWorkbenchPage page = window.getActivePage();
					if (page == null) {
						String msg = NLS.bind(IDEWorkbenchMessages.OpenDelayedFileAction_message_noWindow,
								details.path);
						MessageDialog.open(MessageDialog.ERROR, window.getShell(),
								IDEWorkbenchMessages.OpenDelayedFileAction_title,
								msg, SWT.SHEET);
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
								IDEWorkbenchMessages.OpenDelayedFileAction_title,
								msg, SWT.SHEET);
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

	/**
	 * Class to record decoded pathname and line location information from
	 * command-line
	 *
	 * @see #resolve(String)
	 */
	private static class FileLocationDetails {
		Path path;
		IFileStore fileStore;
		IFileInfo fileInfo;

		int line = -1;
		int column = -1;

		/**
		 * Check if path exists with optional encoded line and/or column
		 * specification
		 *
		 * @param path
		 *            the possibly-encoded file path with optional line/column
		 *            details
		 * @return the location details or {@code null} if the file doesn't
		 *         exist
		 */
		private static FileLocationDetails resolve(String path) {
			FileLocationDetails details = checkLocation(path, -1, -1);
			if (details != null) {
				return details;
			}
			// Ideally we'd use a regex, except that we need to be greedy
			// in matching. For example, we're trying to open /tmp/foo:3:3
			// and there is an actual file named /tmp/foo:3
			Pattern lPattern = Pattern.compile("^(?<path>.*?)[+:](?<line>\\d+)$"); //$NON-NLS-1$
			Pattern lcPattern = Pattern.compile("^(?<path>.*?)[+:](?<line>\\d+):(?<column>\\d+)$"); //$NON-NLS-1$
			Matcher m = lPattern.matcher(path);
			if (m.matches()) {
				try {
					details = checkLocation(m.group("path"), Integer.parseInt(m.group("line")), -1); //$NON-NLS-1$//$NON-NLS-2$
					if (details != null) {
						return details;
					}
				} catch (NumberFormatException e) {
					// shouldn't happen
				}

			}
			m = lcPattern.matcher(path);
			if (m.matches()) {
				try {
					details = checkLocation(m.group("path"), Integer.parseInt(m.group("line")), //$NON-NLS-1$//$NON-NLS-2$
							m.group("column") != null ? Integer.parseInt(m.group("column")) : -1); //$NON-NLS-1$ //$NON-NLS-2$
					if (details != null) {
						return details;
					}
				} catch (NumberFormatException e) {
					// shouldn't happen invalid line or column
				}
			}
			// no matches on line or line+column
			return null;
		}

		/** Return details if {@code path} exists */
		private static FileLocationDetails checkLocation(String path, int line, int column) {
			FileLocationDetails spec = new FileLocationDetails();
			spec.path = new Path(path);
			spec.fileStore = EFS.getLocalFileSystem().getStore(spec.path);
			spec.fileInfo = spec.fileStore.fetchInfo();
			spec.line = line;
			spec.column = column;
			return spec.fileInfo.exists() ? spec : null;
		}
	}

}
