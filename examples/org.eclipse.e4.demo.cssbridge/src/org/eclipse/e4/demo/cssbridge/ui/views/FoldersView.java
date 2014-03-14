/*******************************************************************************
 * Copyright (c) 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.demo.cssbridge.ui.views;

import static org.eclipse.e4.demo.cssbridge.util.ViewUtils.getDisplay;

import java.util.List;

import org.eclipse.e4.demo.cssbridge.core.IMailService;
import org.eclipse.e4.demo.cssbridge.model.FolderType;
import org.eclipse.e4.demo.cssbridge.model.TreeItem;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

public class FoldersView extends ViewPart {
	public static final String ID = "org.eclipse.e4.demo.cssbridge.ui.views.foldersView";

	private TreeViewer viewer;

	private IMailService mailService;

	private Listener treeItemPaintListener = new ItemPaintListener<org.eclipse.swt.widgets.TreeItem>() {
		@Override
		protected String getText(org.eclipse.swt.widgets.TreeItem item,
				int index) {
			return item.getText(index);
		}

		@Override
		protected Rectangle getBounds(org.eclipse.swt.widgets.TreeItem item,
				int index) {
			return item.getBounds(index);
		}

		@Override
		protected Rectangle getParentBounds(
				org.eclipse.swt.widgets.TreeItem item) {
			return item.getParent().getBounds();
		}

		@Override
		protected Image getImage(org.eclipse.swt.widgets.TreeItem item,
				int index) {
			return item.getImage();
		}

		@Override
		protected int calculateTextLeftPadding(
				org.eclipse.swt.widgets.TreeItem item, int index) {
			return item.getImage().getBounds().width + 5;
		}
	};

	private Listener shellReskinListener = new Listener() {
		@Override
		public void handleEvent(Event event) {
			viewer.refresh();
		}
	};

	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		mailService = (IMailService) site.getService(IMailService.class);
	}

	@Override
	public void dispose() {
		if (!viewer.getTree().isDisposed()) {
			viewer.getTree().removeListener(SWT.PaintItem,
					treeItemPaintListener);
		}
		getDisplay(getSite()).removeListener(SWT.Skin, shellReskinListener);
	}

	@Override
	public void createPartControl(Composite parent) {
		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.BORDER);
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setInput(createMailboxStructure(mailService.getMailboxName()));
		viewer.getTree().addListener(SWT.PaintItem, treeItemPaintListener);

		getSite().setSelectionProvider(viewer);
		selectFolder(FolderType.Inbox);

		getDisplay(getSite()).addListener(SWT.Skin, shellReskinListener);
	}

	private TreeItem createMailboxStructure(String mailboxName) {
		TreeItem root = new TreeItem(null, null);
		TreeItem item = new TreeItem(root, mailboxName);
		for (FolderType folder : FolderType.values()) {
			item.addChild(new TreeItem(item, folder));
		}
		root.addChild(item);
		return root;
	}

	private void selectFolder(FolderType type) {
		TreeItem root = (TreeItem) viewer.getInput();
		for (TreeItem child : root.getChildren().get(0).getChildren()) {
			if (child.getValue() == type) {
				viewer.setSelection(new StructuredSelection(child));
				break;
			}
		}
	}

	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	private static class ViewContentProvider implements
			IStructuredContentProvider, ITreeContentProvider {
		@Override
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}

		@Override
		public void dispose() {
		}

		@Override
		public Object[] getElements(Object parent) {
			return getChildren(parent);
		}

		@Override
		public Object getParent(Object child) {
			if (child instanceof TreeItem) {
				return ((TreeItem) child).getParent();
			}
			return null;
		}

		@Override
		public Object[] getChildren(Object parent) {
			List<TreeItem> children = null;
			if (parent instanceof TreeItem) {
				children = ((TreeItem) parent).getChildren();
			}
			return children != null ? children.toArray() : new Object[0];
		}

		@Override
		public boolean hasChildren(Object elem) {
			if (elem instanceof TreeItem) {
				return ((TreeItem) elem).getChildren() != null;
			}
			return false;
		}
	}

	private class ViewLabelProvider extends LabelProvider implements
			IFontProvider, IColorProvider {
		@Override
		public String getText(Object obj) {
			return obj.toString();
		}

		@Override
		public Image getImage(Object elem) {
			ISharedImages sharedImages = PlatformUI.getWorkbench()
					.getSharedImages();
			return sharedImages
					.getImage(isFolderType(elem) ? ISharedImages.IMG_OBJ_ELEMENT
							: ISharedImages.IMG_OBJ_FOLDER);
		}

		@Override
		public Font getFont(Object element) {
			return isFolderType(element) ? Theme
					.getFont(Theme.FoldersView.FOLDER_TYPE_FONT) : Theme
					.getFont(Theme.FoldersView.MAILBOX_NAME_FONT);
		}

		private boolean isFolderType(Object elem) {
			return elem instanceof TreeItem
					&& ((TreeItem) elem).getValue() instanceof FolderType;
		}

		@Override
		public Color getForeground(Object element) {
			return isFolderType(element) ? Theme
					.getColor(Theme.FoldersView.FOLDER_TYPE_FOREGROUND) : Theme
					.getColor(Theme.FoldersView.MAILBOX_NAME_FOREGROUND);
		}

		@Override
		public Color getBackground(Object element) {
			return null;
		}
	}
}