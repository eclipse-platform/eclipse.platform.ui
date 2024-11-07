/*******************************************************************************
 * Copyright (c) 2008, 2015 IBM Corporation and others.
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
 ******************************************************************************/

package org.eclipse.ui.internal.statushandlers;

import org.eclipse.jface.util.Policy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.statushandlers.AbstractStatusAreaProvider;
import org.eclipse.ui.statushandlers.StatusAdapter;
import org.eclipse.ui.statushandlers.WorkbenchErrorHandler;
import org.eclipse.ui.statushandlers.WorkbenchStatusDialogManager;

/**
 * This class is responsible for displaying stack trace retrieved from IStatus.
 * It has similar functionality as details area in
 * {@link WorkbenchStatusDialogManager}. This class will be visible only if it
 * is enabled in {@link WorkbenchStatusDialogManager} and no support provider is
 * passed by {@link Policy}
 *
 * @see Policy#setErrorSupportProvider
 * @see Policy#getErrorSupportProvider()
 * @see WorkbenchStatusDialogManager#enableDefaultSupportArea
 * @see WorkbenchErrorHandler
 * @since 3.4
 */
public class StackTraceSupportArea extends AbstractStatusAreaProvider {

	/*
	 * Displays statuses.
	 */
	private List list;

	@Override
	public Control createSupportArea(final Composite parent, StatusAdapter statusAdapter) {

		Label label = new Label(parent, SWT.NONE);
		label.setText(WorkbenchMessages.StackTraceSupportArea_Title);

		list = new List(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = true;
		gd.widthHint = 250;
		list.setLayoutData(gd);
		list.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				list.selectAll();
				super.widgetSelected(e);
			}
		});
		list.removeAll();
		populateList(statusAdapter.getStatus().getException());
		createDNDSource();
		createCopyAction(parent);
		return parent;
	}

	/**
	 * Creates DND source for the list
	 */
	private void createDNDSource() {
		DragSource ds = new DragSource(list, DND.DROP_COPY);
		ds.setTransfer(new Transfer[] { TextTransfer.getInstance() });
		ds.addDragListener(new DragSourceListener() {
			@Override
			public void dragFinished(DragSourceEvent event) {

			}

			@Override
			public void dragSetData(DragSourceEvent event) {
				if (TextTransfer.getInstance().isSupportedType(event.dataType)) {
					event.data = prepareCopyString();
				}
			}

			@Override
			public void dragStart(DragSourceEvent event) {
				list.selectAll();
			}
		});
	}

	private void createCopyAction(final Composite parent) {
		Menu menu = new Menu(parent.getShell(), SWT.POP_UP);
		MenuItem copyAction = new MenuItem(menu, SWT.PUSH);
		copyAction.setText("&Copy"); //$NON-NLS-1$
		copyAction.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Clipboard clipboard = null;
				try {
					clipboard = new Clipboard(parent.getDisplay());
					clipboard.setContents(new Object[] { prepareCopyString() },
							new Transfer[] { TextTransfer.getInstance() });
				} finally {
					if (clipboard != null) {
						clipboard.dispose();
					}
				}
				super.widgetSelected(e);
			}
		});
		list.setMenu(menu);
	}

	private String prepareCopyString() {
		if (list == null || list.isDisposed()) {
			return ""; //$NON-NLS-1$
		}
		StringBuilder sb = new StringBuilder();
		String newLine = System.lineSeparator();
		for (int i = 0; i < list.getItemCount(); i++) {
			sb.append(list.getItem(i));
			sb.append(newLine);
		}
		return sb.toString();
	}

	private void populateList(Throwable t) {
		if (t == null) {
			list.add(WorkbenchMessages.StackTraceSupportArea_NoStackTrace);
			return;
		}
		list.add(t.toString());
		for (StackTraceElement stackTraceElement : t.getStackTrace()) {
			list.add(stackTraceElement.toString());
		}
		if (t.getCause() != null) {
			list.add(WorkbenchMessages.StackTraceSupportArea_CausedBy);
			populateList(t.getCause());
		}
	}

	/**
	 * @return Returns the list.
	 */
	public List getList() {
		return list;
	}

	@Override
	public boolean validFor(StatusAdapter statusAdapter) {
		return statusAdapter.getStatus().getException() != null;
	}

}
