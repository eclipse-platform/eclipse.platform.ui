/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.tests.workbench;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.workbench.swt.modeling.EMenuService;
import org.eclipse.e4.workbench.modeling.ESelectionService;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeColumn;

public class ContextMenuView {
	public static final String ITEMS_MENU = "ContextMenuView.treeMenu";
	public static final String TAGS_MENU = "ContextMenuView.tags";
	public static final String INFO_MENU = "ContextMenuView.info";

	static class Entry {
		final public Date date;
		final public String desc;

		public Entry(Date d, String s) {
			date = d;
			desc = s;
		}
	}

	static class Tag {
		final public String name;

		public Tag(String s) {
			name = s;
		}
	}

	private TreeViewer items;
	private ListViewer tagList;
	private Text info;

	@Inject
	private ESelectionService selectionService;

	@Inject
	private EHandlerService handlerService;

	@Inject
	private EMenuService menuService;

	@Inject
	public ContextMenuView(final Composite parent) {
		final Composite root = new Composite(parent, SWT.NONE);
		root.setLayout(new GridLayout(2, true));
		items = new TreeViewer(root, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
		items.getControl().setLayoutData(
				new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		TreeViewerColumn dateColumn = new TreeViewerColumn(items, SWT.LEFT);
		TreeColumn dcol = dateColumn.getColumn();
		dcol.setText("Date");
		dcol.setWidth(200);
		TreeViewerColumn descColumn = new TreeViewerColumn(items, SWT.LEFT);
		TreeColumn descCol = descColumn.getColumn();
		descCol.setText("Description");
		descCol.setWidth(300);
		tagList = new ListViewer(root, SWT.MULTI | SWT.V_SCROLL);
		tagList.getControl().setLayoutData(
				new GridData(SWT.FILL, SWT.FILL, false, true));
		info = new Text(root, SWT.MULTI | SWT.READ_ONLY);
		info.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
	}

	ArrayList<Entry> itemModel = new ArrayList<Entry>();
	HashMap<Entry, String> textMap = new HashMap<Entry, String>();
	HashMap<Entry, ArrayList<Tag>> tagMap = new HashMap<Entry, ArrayList<Tag>>();
	private CopyHandler itemCopyHandler;
	private CopyHandler tagCopyHandler;

	class ViewContentProvider implements IStructuredContentProvider,
			ITreeContentProvider {

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		public void dispose() {
		}

		public Object[] getElements(Object inputElement) {
			if (inputElement instanceof ArrayList<?>) {
				return ((ArrayList<?>) inputElement).toArray();
			}
			return null;
		}

		public boolean hasChildren(Object element) {
			if (element instanceof ArrayList<?>) {
				return true;
			}
			return false;
		}

		public Object getParent(Object element) {
			// TODO Auto-generated method stub
			return null;
		}

		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof ArrayList<?>) {
				return ((ArrayList<?>) parentElement).toArray();
			}
			return new Object[0];
		}
	}

	static class CopyHandler {
		StructuredViewer viewer;
		Display display;

		public CopyHandler(Display d, StructuredViewer v) {
			viewer = v;
			display = d;
		}

		@CanExecute
		public boolean canExecute() {
			ISelection sel = viewer.getSelection();
			System.out.println("canExecute: " + sel);
			return sel != null && sel instanceof IStructuredSelection
					&& !((IStructuredSelection) sel).isEmpty();
		}

		@Execute
		public void execute() {
			Clipboard cb = new Clipboard(display);
			TextTransfer t = TextTransfer.getInstance();
			String text = getText();
			System.out.println("execute: " + text);
			cb.setContents(new Object[] { text }, new Transfer[] { t });
			cb.dispose();
		}

		private String getText() {
			ISelection sel = viewer.getSelection();
			if (sel instanceof IStructuredSelection) {
				IStructuredSelection ssel = (IStructuredSelection) sel;
				Object obj = ssel.getFirstElement();
				if (obj instanceof Entry) {
					return ((Entry) obj).desc
							+ " ("
							+ SimpleDateFormat.getInstance().format(
									((Entry) obj).date) + ")";
				} else if (obj instanceof Tag) {
					return ((Tag) obj).name;
				}
			}
			return "";
		}
	}

	@PostConstruct
	public void init() {
		for (int i = 1; i < 5; i++) {
			createEntry(i);
		}

		items.setContentProvider(new ViewContentProvider());
		items.setLabelProvider(new EntryLabelProvider());
		items.setInput(itemModel);
		items.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				ISelection selection = event.getSelection();
				selectionService.setSelection(selection);
				if (selection instanceof IStructuredSelection) {
					Entry e = (Entry) ((IStructuredSelection) selection)
							.getFirstElement();
					String text = textMap.get(e);
					info.setText(text == null ? "" : text);
					ArrayList<Tag> list = tagMap.get(e);
					tagList.setInput(list == null ? Collections.EMPTY_LIST
							: list);
				}
			}
		});

		items.getTree().setLinesVisible(true);
		items.getTree().setHeaderVisible(true);
		items.getTree().layout(true);
		Menu menu = new Menu(items.getControl());
		items.getControl().setMenu(menu);
		menuService.registerContextMenu(menu, ITEMS_MENU);

		itemCopyHandler = new CopyHandler(items.getTree().getDisplay(), items);
		items.getTree().addListener(SWT.Activate, new Listener() {
			public void handleEvent(Event event) {
				handlerService.activateHandler("org.eclipse.ui.edit.copy",
						itemCopyHandler);
			}
		});

		tagList.setContentProvider(new ArrayContentProvider());
		tagList.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof Tag) {
					return ((Tag) element).name;
				}
				return null;
			}
		});
		menu = new Menu(tagList.getControl());
		tagList.getControl().setMenu(menu);
		menuService.registerContextMenu(menu, TAGS_MENU);

		tagCopyHandler = new CopyHandler(tagList.getControl().getDisplay(),
				tagList);
		tagList.getControl().addListener(SWT.Activate, new Listener() {
			public void handleEvent(Event event) {
				handlerService.activateHandler("org.eclipse.ui.edit.copy",
						tagCopyHandler);
			}
		});

		info.addListener(SWT.Activate, new Listener() {
			public void handleEvent(Event event) {
				// just remove 'em both for simplicity
				handlerService.deactivateHandler("org.eclipse.ui.edit.copy",
						itemCopyHandler);
				handlerService.deactivateHandler("org.eclipse.ui.edit.copy",
						tagCopyHandler);
			}
		});
		menu = new Menu(info);
		info.setMenu(menu);
		menuService.registerContextMenu(menu, INFO_MENU);

	}

	private static class EntryLabelProvider extends LabelProvider implements
			ITableLabelProvider {
		public Image getColumnImage(Object element, int columnIndex) {
			// TODO Auto-generated method stub
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
		 */
		@Override
		public String getText(Object element) {
			return getColumnText(element, 0);
		}

		public String getColumnText(Object element, int columnIndex) {
			switch (columnIndex) {
			case 0:
				return SimpleDateFormat.getInstance().format(
						((Entry) element).date);
			case 1:
				return ((Entry) element).desc;
			}
			return null;
		}
	}

	private void createEntry(int index) {
		Entry e = new Entry(new Date(), "item " + index);
		itemModel.add(e);
		textMap.put(e, "text for item " + index);
		ArrayList<Tag> tags = new ArrayList<Tag>();
		tagMap.put(e, tags);
		for (int i = 1; i < 5; i++) {
			createTag(tags, index, i);
		}
	}

	private void createTag(ArrayList<Tag> tags, int index, int i) {
		tags.add(new Tag("item_" + index + "_tag_" + i));
	}
}
