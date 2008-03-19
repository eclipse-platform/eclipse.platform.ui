/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.statushandlers;

import java.util.Date;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.internal.progress.ProgressMessages;
import org.eclipse.ui.statushandlers.AbstractStatusAreaProvider;
import org.eclipse.ui.statushandlers.IStatusAdapterConstants;
import org.eclipse.ui.statushandlers.StatusAdapter;
import org.eclipse.ui.statushandlers.WorkbenchStatusDialogManager;

import com.ibm.icu.text.DateFormat;

/**
 * The default details area displaying a tree of statuses.
 * 
 * @since 3.4
 */
public class DefaultDetailsArea extends AbstractStatusAreaProvider {

	private WorkbenchStatusDialogManager workbenchStatusDialog;

	public DefaultDetailsArea(WorkbenchStatusDialogManager wsd){
		this.workbenchStatusDialog = wsd;
	}
	
	/*
	 * All statuses should be displayed.
	 */
	protected static final int MASK = IStatus.CANCEL | IStatus.ERROR
			| IStatus.INFO | IStatus.WARNING;

	/*
	 * New child entry in the list will be shifted by two spaces.
	 */
	private static final Object NESTING_INDENT = "  "; //$NON-NLS-1$

	/*
	 * Displays statuses.
	 */
	private List list;

	private Clipboard clipboard;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.statushandlers.AbstractStatusAreaProvider#createSupportArea(org.eclipse.swt.widgets.Composite,
	 *      org.eclipse.ui.statushandlers.StatusAdapter)
	 */
	public Control createSupportArea(Composite parent,
			StatusAdapter statusAdapter) {
		Composite area = createArea(parent);
		setStatusAdapter(statusAdapter);
		return area;
	}

	protected Composite createArea(Composite parent) {
		parent = new Composite(parent, SWT.NONE);
		GridLayout pggl = new GridLayout();
		parent.setLayout(pggl);
		GridData pgd = new GridData(GridData.FILL_HORIZONTAL);
		pgd.grabExcessHorizontalSpace = true;
		parent.setLayoutData(pgd);
		list = new List(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI
				| SWT.BORDER);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = true;
		gd.widthHint = 250;
		gd.heightHint = 100;
		list.setLayoutData(gd);
		list.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				if (clipboard != null) {
					clipboard.dispose();
				}
			}
		});
		list.addSelectionListener(new SelectionAdapter() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			public void widgetSelected(SelectionEvent e) {
				list.selectAll();
				super.widgetSelected(e);
			}
		});
		createDNDSource();
		createCopyAction(parent);
		return parent;
	}

	protected void setStatusAdapter(StatusAdapter adapter) {
		list.removeAll();
		populateList(list, adapter.getStatus(), 0);
		if (workbenchStatusDialog.getStatusAdapters().size() == 1) {
			Long timestamp = (Long) adapter
					.getProperty(IStatusAdapterConstants.TIMESTAMP_PROPERTY);

			if (timestamp != null) {
				String date = DateFormat.getDateTimeInstance(DateFormat.LONG,
						DateFormat.LONG)
						.format(new Date(timestamp.longValue()));
				list.add(NLS.bind(ProgressMessages.JobInfo_Error,
						(new Object[] { "", date }))); //$NON-NLS-1$
			}
		}
	}

	/**
	 * Creates DND source for the list
	 */
	private void createDNDSource() {
		DragSource ds = new DragSource(list, DND.DROP_COPY);
		ds.setTransfer(new Transfer[] { TextTransfer.getInstance() });
		ds.addDragListener(new DragSourceListener() {
			public void dragFinished(DragSourceEvent event) {
			}

			public void dragSetData(DragSourceEvent event) {
				if (TextTransfer.getInstance().isSupportedType(event.dataType)) {
					event.data = prepareCopyString();
				}
			}
			
			public void dragStart(DragSourceEvent event) {
				list.selectAll();
			}
		});
	}

	private void createCopyAction(final Composite parent) {
		Menu menu = new Menu(parent.getShell(), SWT.POP_UP);
		MenuItem copyAction = new MenuItem(menu, SWT.PUSH);
		copyAction.setText(JFaceResources.getString("copy")); //$NON-NLS-1$
		copyAction.addSelectionListener(new SelectionAdapter() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			public void widgetSelected(SelectionEvent e) {
				clipboard = new Clipboard(parent.getDisplay());
				clipboard.setContents(new Object[] { prepareCopyString() },
						new Transfer[] { TextTransfer.getInstance() });
				super.widgetSelected(e);
			}

		});
		list.setMenu(menu);
	}

	private String prepareCopyString() {
		if (list == null || list.isDisposed()) {
			return ""; //$NON-NLS-1$
		}
		StringBuffer sb = new StringBuffer();
		String newLine = System.getProperty("line.separator"); //$NON-NLS-1$
		for (int i = 0; i < list.getItemCount(); i++) {
			sb.append(list.getItem(i));
			sb.append(newLine);
		}
		return sb.toString();
	}

	private void populateList(List list, IStatus status, int nesting) {
		if (!status.matches(MASK)) {
			return;
		}
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < nesting; i++) {
			buffer.append(NESTING_INDENT);
		}
		buffer.append(status.getMessage());
		list.add(buffer.toString());

		// Look for a nested core exception
		Throwable t = status.getException();
		if (t instanceof CoreException) {
			CoreException ce = (CoreException) t;
			populateList(list, ce.getStatus(), nesting + 1);
		} else if (t != null) {
			// Include low-level exception message
			buffer = new StringBuffer();
			for (int i = 0; i < nesting; i++) {
				buffer.append(NESTING_INDENT);
			}
			String message = t.getLocalizedMessage();
			if (message == null) {
				message = t.toString();
			}
			buffer.append(message);
			list.add(buffer.toString());
		}

		IStatus[] children = status.getChildren();
		for (int i = 0; i < children.length; i++) {
			populateList(list, children[i], nesting + 1);
		}
	}

	/**
	 * @return Returns the list.
	 */
	public List getList() {
		return list;
	}
}
