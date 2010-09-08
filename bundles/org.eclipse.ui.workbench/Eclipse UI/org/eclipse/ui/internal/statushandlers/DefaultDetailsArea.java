/*******************************************************************************
 * Copyright (c) 2008, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.statushandlers;

import com.ibm.icu.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.internal.progress.ProgressMessages;
import org.eclipse.ui.keys.IBindingService;
import org.eclipse.ui.statushandlers.AbstractStatusAreaProvider;
import org.eclipse.ui.statushandlers.IStatusAdapterConstants;
import org.eclipse.ui.statushandlers.StatusAdapter;

/**
 * The default details area displaying a tree of statuses.
 */
public class DefaultDetailsArea extends AbstractStatusAreaProvider {

	private static final int MINIMUM_HEIGHT = 100;

	/*
	 * All statuses should be displayed.
	 */
	private int mask;

	/*
	 * New child entry in the list will be shifted by a number of pixels
	 */
	private static final int NESTING_INDENT = 15;

	/*
	 * Displays statuses.
	 */
	private StyledText text;

	private boolean handleOkStatuses;

	private Map dialogState;

	private MenuItem copyAction;

	/**
	 * @param dialogState
	 */
	public DefaultDetailsArea(Map dialogState) {
		this.dialogState = dialogState;
		handleOkStatuses = ((Boolean) dialogState
				.get(IStatusDialogConstants.HANDLE_OK_STATUSES)).booleanValue();
		mask = ((Integer) dialogState.get(IStatusDialogConstants.MASK))
				.intValue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.statushandlers.AbstractStatusAreaProvider#createSupportArea
	 * (org.eclipse.swt.widgets.Composite,
	 * org.eclipse.ui.statushandlers.StatusAdapter)
	 */
	public Control createSupportArea(Composite parent,
			StatusAdapter statusAdapter) {
		Composite area = createArea(parent);
		setStatusAdapter(statusAdapter);
		return area;
	}

	protected Composite createArea(Composite parent) {
		parent = new Composite(parent, SWT.NONE);
		parent.setLayout(new GridLayout());
		parent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		text = new StyledText(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI
				| SWT.BORDER | SWT.READ_ONLY | SWT.WRAP);
		text.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = true;
		gd.widthHint = 250;
		gd.minimumHeight = MINIMUM_HEIGHT;
		text.setLayoutData(gd);
		// There is no support for triggering commands in the dialogs. I am
		// trying to emulate the workbench behavior as exactly as possible.
		IBindingService binding = (IBindingService) PlatformUI.getWorkbench()
				.getService(IBindingService.class);
		// find bindings for copy action
		final TriggerSequence ts[] = binding
				.getActiveBindingsFor(ActionFactory.COPY.getCommandId());
		text.addKeyListener(new KeyListener() {

			ArrayList keyList = new ArrayList();

			public void keyPressed(KeyEvent e) {
				// get the character. reverse the ctrl modifier if necessary
				char character = e.character;
				boolean ctrlDown = (e.stateMask & SWT.CTRL) != 0;
				if (ctrlDown && e.character != e.keyCode && e.character < 0x20
						&& (e.keyCode & SWT.KEYCODE_BIT) == 0) {
					character += 0x40;
				}
				// do not process modifier keys
				if ((e.keyCode & (~SWT.MODIFIER_MASK)) == 0) {
					return;
				}
				// if there is a character, use it. if no character available,
				// try with key code
				KeyStroke ks = KeyStroke.getInstance(e.stateMask,
						character != 0 ? character : e.keyCode);
				keyList.add(ks);
				KeySequence sequence = KeySequence.getInstance(keyList);
				boolean partialMatch = false;
				for (int i = 0; i < ts.length; i++) {
					if (ts[i].equals(sequence)) {
						copyToClipboard();
						keyList.clear();
						break;
					}
					if (ts[i].startsWith(sequence, false)) {
						partialMatch = true;
					}
					for (int j = 0; j < ts[i].getTriggers().length; j++) {
						if (ts[i].getTriggers()[j].equals(ks)) {
							partialMatch = true;
						}
					}
				}
				if (!partialMatch) {
					keyList.clear();
				}
			}

			public void keyReleased(KeyEvent e) {
				// no op
			}
		});
		text.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				if (text.getSelectionText().length() == 0) {
					if (copyAction != null && !copyAction.isDisposed()) {
						copyAction.setEnabled(false);
					}
				} else {
					if (copyAction != null && !copyAction.isDisposed()) {
						copyAction.setEnabled(true);
					}
				}
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}

		});
		createDNDSource();
		createCopyAction(parent);
		Dialog.applyDialogFont(parent);
		return parent;
	}

	private void setStatusAdapter(StatusAdapter adapter) {
		populateList(text, adapter.getStatus(), 0, new int[] { 0 });
		if (!isMulti()) {
			Long timestamp = (Long) adapter
					.getProperty(IStatusAdapterConstants.TIMESTAMP_PROPERTY);

			if (timestamp != null) {
				String date = DateFormat.getDateTimeInstance(DateFormat.LONG,
						DateFormat.LONG)
						.format(new Date(timestamp.longValue()));
				text.append(NLS.bind(ProgressMessages.JobInfo_Error,
						(new Object[] { "", date }))); //$NON-NLS-1$
			}
		}
		int delimiterLength = getLineSeparator().length();
		text.replaceTextRange(text.getText().length() - delimiterLength,
				delimiterLength, ""); //$NON-NLS-1$
		adjustHeight(text);
	}

	private void adjustHeight(StyledText text) {
		int lineCount = text.getLineCount();
		int lineHeight = text.getLineHeight();
		int startPos = text.getLocation().y;
		Composite c = text.getParent();
		while (c != null) {
			startPos += c.getLocation().y;
			c = c.getParent();
		}
		// the text is not positioned yet, we assume that it will appear
		// on the bottom of the dialog
		startPos += text.getShell().getBounds().height;
		int screenHeight = text.getShell().getMonitor().getBounds().height;
		int availableScreenForText = screenHeight - startPos;
		if (availableScreenForText <= MINIMUM_HEIGHT) {
			// should not happen. But in that case nothing can improve user
			// experience.
			return;
		}
		int desiredHeight = lineCount * lineHeight;
		if (desiredHeight > availableScreenForText * 0.75) {
			((GridData) text.getLayoutData()).heightHint = (int) (availableScreenForText * 0.75);
		}
	}

	/**
	 * Creates DND source for the list
	 */
	private void createDNDSource() {
		DragSource ds = new DragSource(text, DND.DROP_COPY);
		ds.setTransfer(new Transfer[] { TextTransfer.getInstance() });
		ds.addDragListener(new DragSourceListener() {
			public void dragFinished(DragSourceEvent event) {
			}

			public void dragSetData(DragSourceEvent event) {
				if (TextTransfer.getInstance().isSupportedType(event.dataType)) {
					event.data = text.getSelectionText();
				}
			}

			public void dragStart(DragSourceEvent event) {
			}
		});
	}

	private void createCopyAction(final Composite parent) {
		Menu menu = new Menu(parent.getShell(), SWT.POP_UP);
		copyAction = new MenuItem(menu, SWT.PUSH);
		copyAction.setText(JFaceResources.getString("copy")); //$NON-NLS-1$
		copyAction.addSelectionListener(new SelectionAdapter() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse
			 * .swt.events.SelectionEvent)
			 */
			public void widgetSelected(SelectionEvent e) {
				copyToClipboard();
				super.widgetSelected(e);
			}

		});
		text.setMenu(menu);
		if (text.getSelectionText().length() == 0) {
			copyAction.setEnabled(false);
		}
	}

	private void populateList(StyledText text, IStatus status, int nesting,
			int[] lineNumber) {
		if (!status.matches(mask) && !(handleOkStatuses && status.isOK())) {
			return;
		}
		appendNewLine(text, status.getMessage(), nesting, lineNumber[0]++);

		// Look for a nested core exception
		Throwable t = status.getException();
		if (t instanceof CoreException) {
			CoreException ce = (CoreException) t;
			populateList(text, ce.getStatus(), nesting + 1, lineNumber);
		} else if (t != null) {
			// Include low-level exception message
			String message = t.getLocalizedMessage();
			if (message == null) {
				message = t.toString();
			}
			appendNewLine(text, message, nesting, lineNumber[0]++);
		}

		IStatus[] children = status.getChildren();
		for (int i = 0; i < children.length; i++) {
			populateList(text, children[i], nesting + 1, lineNumber);
		}
	}

	private String getLineSeparator() {
		return System.getProperty("line.separator"); //$NON-NLS-1$
	}

	private void appendNewLine(StyledText text, String line, int indentLevel,
			int lineNumber) {
		text.append(line + getLineSeparator());
		int pixelIndent = indentLevel * NESTING_INDENT;
		if (lineNumber != 0) {
			pixelIndent += NESTING_INDENT / 2;
		}
		text.setLineIndent(lineNumber, 1, pixelIndent);
		text.setLineWrapIndent(lineNumber, 1, indentLevel * NESTING_INDENT);
	}

	private void copyToClipboard() {
		Clipboard clipboard = null;
		try {
			clipboard = new Clipboard(text.getDisplay());
			clipboard.setContents(new Object[] { text.getSelectionText() },
					new Transfer[] { TextTransfer.getInstance() });
		} finally {
			if (clipboard != null) {
				clipboard.dispose();
			}
		}
	}

	/**
	 * This method checks if status dialog holds more than one status.
	 * 
	 * @return true if the dialog has one more than one status.
	 */
	private boolean isMulti() {
		return ((Collection) dialogState
				.get(IStatusDialogConstants.STATUS_ADAPTERS)).size() != 1;
	}
}
