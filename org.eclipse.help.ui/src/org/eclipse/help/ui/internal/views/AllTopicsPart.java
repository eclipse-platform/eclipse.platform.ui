/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.ui.internal.views;

import org.eclipse.help.*;
import org.eclipse.help.ui.internal.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;

public class AllTopicsPart extends AbstractFormPart implements IHelpPart {
	private ReusableHelpPart parent;

	private String id;

	private Composite container;

	private TreeViewer treeViewer;

	private TreeItem lastItem;
	private Cursor handCursor;

	class TopicsProvider implements ITreeContentProvider {
		public Object[] getChildren(Object parentElement) {
			if (parentElement == AllTopicsPart.this)
				return HelpSystem.getTocs();
			if (parentElement instanceof IToc)
				return ((IToc) parentElement).getTopics();
			if (parentElement instanceof ITopic)
				return ((ITopic) parentElement).getSubtopics();
			return new Object[0];
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
		 */
		public Object getParent(Object element) {
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
		 */
		public boolean hasChildren(Object element) {
			return getChildren(element).length > 0;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
		 */
		public Object[] getElements(Object inputElement) {
			return getChildren(inputElement);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
		 */
		public void dispose() {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
		 *      java.lang.Object, java.lang.Object)
		 */
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}

	class TopicsLabelProvider extends LabelProvider {
		public String getText(Object obj) {
			if (obj instanceof IHelpResource)
				return ((IHelpResource) obj).getLabel();
			return super.getText(obj);
		}

		public Image getImage(Object obj) {
			if (obj instanceof IToc) {
				boolean expanded = treeViewer.getExpandedState(obj);
				String key = expanded ? IHelpUIConstants.IMAGE_TOC_OPEN
						: IHelpUIConstants.IMAGE_TOC_CLOSED;
				return HelpUIResources.getImage(key);
			}
			if (obj instanceof ITopic) {
				boolean expandable = treeViewer.isExpandable(obj);
				String key = expandable ? IHelpUIConstants.IMAGE_CONTAINER
						: IHelpUIConstants.IMAGE_FILE_F1TOPIC;
				return HelpUIResources.getImage(key);
			}
			return super.getImage(obj);
		}
	}

	/**
	 * @param parent
	 * @param toolkit
	 * @param style
	 */
	public AllTopicsPart(Composite parent, final FormToolkit toolkit,
			IToolBarManager tbm) {
		handCursor = new Cursor(parent.getDisplay(), SWT.CURSOR_HAND);
		container = toolkit.createComposite(parent);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 0;
		container.setLayout(layout);
		Composite sep = toolkit.createCompositeSeparator(container);
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.heightHint = 1;
		sep.setLayoutData(gd);

		treeViewer = new TreeViewer(container, SWT.H_SCROLL | SWT.V_SCROLL);
		treeViewer.setContentProvider(new TopicsProvider());
		treeViewer.setLabelProvider(new TopicsLabelProvider());
		treeViewer.getTree().setMenu(parent.getMenu());
		treeViewer.getTree().setForeground(
				toolkit.getHyperlinkGroup().getForeground());
		treeViewer.setInput(this);
		treeViewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
		treeViewer.addTreeListener(new ITreeViewerListener() {
			public void treeCollapsed(TreeExpansionEvent event) {
				Object obj = event.getElement();
				if (obj instanceof IToc) {
					postUpdate(obj);
				}
			}

			public void treeExpanded(TreeExpansionEvent event) {
				Object obj = event.getElement();

				if (obj instanceof IToc) {
					postUpdate(event.getElement());
				}
			}
		});
		treeViewer.addOpenListener(new IOpenListener() {
			public void open(OpenEvent event) {
				doOpenSelection((IStructuredSelection) event.getSelection());
			}
		});
		treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				handleSelectionChanged((IStructuredSelection) event
						.getSelection());
			}
		});
		treeViewer.getTree().addMouseListener(new MouseAdapter() {
			long lastTime;
			public void mouseUp(MouseEvent e) {
				if (e.time-lastTime <=e.display.getDoubleClickTime())
					return;
				if (e.button != 1)
					return;
				lastTime = e.time;
				Point p = new Point(e.x, e.y);
				TreeItem item = treeViewer.getTree().getItem(p);
				if (item != null) {
					Object obj = item.getData();
					if (obj instanceof IHelpResource) {
						doOpen((IHelpResource) obj);
					}
				}
			}
		});

		treeViewer.getTree().addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				if (lastItem == null)
					return;
				Rectangle bounds = lastItem.getBounds();
				boolean selected = false;
				TreeItem[] items = lastItem.getParent().getSelection();
				for (int i = 0; i < items.length; i++) {
					if (items[i].equals(lastItem)) {
						selected = true;
						break;
					}
				}
				if (selected)
					e.gc.setForeground(container.getDisplay().getSystemColor(
							SWT.COLOR_LIST_SELECTION_TEXT));
				else
					e.gc.setForeground(toolkit.getHyperlinkGroup()
							.getActiveForeground());
				FontMetrics fm = e.gc.getFontMetrics();
				int height = fm.getHeight();
				int lineY = bounds.y + height;
				e.gc.drawLine(bounds.x, lineY, bounds.x + bounds.width - 1,
						lineY);
			}
		});

		treeViewer.getTree().addMouseTrackListener(new MouseTrackAdapter() {
			public void mouseExit(MouseEvent e) {
				if (lastItem != null) {
					TreeItem item = lastItem;
					lastItem = null;
					item.setForeground(null);
				}
			}
		});

		treeViewer.getTree().addMouseMoveListener(new MouseMoveListener() {
			public void mouseMove(MouseEvent e) {
				Point p = new Point(e.x, e.y);
				TreeItem item = treeViewer.getTree().getItem(p);
				if (item != null) {
					if (lastItem != null) {
						if (!lastItem.equals(item)) {
							lastItem.setForeground(null);
							repaintItem(lastItem);
							updateStatus(null);
							lastItem = null;
						} else
							return;
					}
					Object obj = item.getData();
					IHelpResource res = (IHelpResource) obj;
					treeViewer.getTree().setCursor(handCursor);
					item.setForeground(toolkit.getHyperlinkGroup()
							.getActiveForeground());
					lastItem = item;
					repaintItem(lastItem);
					updateStatus((IHelpResource) obj);
					return;
				} else if (lastItem != null) {
					lastItem.setForeground(null);
					repaintItem(lastItem);
					lastItem = null;
					updateStatus(null);
				}
				treeViewer.getTree().setCursor(null);
			}
		});
		contributeToToolBar(tbm);
	}
	
	public void dispose() {
		handCursor.dispose();
		super.dispose();
	}

	private void repaintItem(TreeItem item) {
		Rectangle bounds = item.getBounds();
		item.getParent().redraw(bounds.x, bounds.y, bounds.width,
				bounds.height, false);
	}

	private void contributeToToolBar(IToolBarManager tbm) {
		Action collapseAllAction = new Action() {
			public void run() {
				BusyIndicator.showWhile(container.getDisplay(), new Runnable() {
					public void run() {
						doCollapseAll();
					}
				});
			}
		};
		collapseAllAction.setImageDescriptor(HelpUIResources
				.getImageDescriptor(IHelpUIConstants.IMAGE_COLLAPSE_ALL));
		collapseAllAction.setToolTipText(HelpUIResources
				.getString("AllTopicsPart.collapseAll.tooltip")); //$NON-NLS-1$
		tbm.insertBefore("back", collapseAllAction); //$NON-NLS-1$
		tbm.insertBefore("back", new Separator()); //$NON-NLS-1$
	}

	private void doCollapseAll() {
		Object[] expanded = treeViewer.getExpandedElements();
		treeViewer.collapseAll();
		treeViewer.update(expanded, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.help.ui.internal.views.IHelpPart#getControl()
	 */
	public Control getControl() {
		return container;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.help.ui.internal.views.IHelpPart#init(org.eclipse.help.ui.internal.views.NewReusableHelpPart)
	 */
	public void init(ReusableHelpPart parent, String id) {
		this.parent = parent;
		this.id = id;
	}

	public String getId() {
		return id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.help.ui.internal.views.IHelpPart#setVisible(boolean)
	 */
	public void setVisible(boolean visible) {
		container.setVisible(visible);
		if (visible)
			treeViewer.refresh();
	}

	private void doOpenSelection(IStructuredSelection sel) {
		IHelpResource res = (IHelpResource) sel.getFirstElement();
		if (res != null) {
			doOpen(res);
		}
	}

	private void handleSelectionChanged(IStructuredSelection sel) {
		IHelpResource res = (IHelpResource) sel.getFirstElement();
		updateStatus(res, false);
	}

	private void updateStatus(IHelpResource res) {
		updateStatus(res, true);
	}

	private void updateStatus(IHelpResource res, boolean defaultToSelection) {
		if (defaultToSelection && res == null) {
			IStructuredSelection ssel = (IStructuredSelection) treeViewer
					.getSelection();
			res = (IHelpResource) ssel.getFirstElement();
		}
		if (res != null) {
			String label = res.getLabel();
			String href = (res instanceof ITopic) ? res.getHref() : null;
			AllTopicsPart.this.parent.handleLinkEntered(new HyperlinkEvent(
					treeViewer.getTree(), href, label, SWT.NULL));
		} else {
			AllTopicsPart.this.parent.handleLinkExited(null);
		}
	}

	private void doOpen(IHelpResource res) {
		if (res instanceof IToc
				|| (res instanceof ITopic
						&& ((ITopic) res).getSubtopics().length > 0 && res
						.getHref() == null))
			treeViewer.setExpandedState(res, !treeViewer.getExpandedState(res));
		if (res instanceof IToc)
			postUpdate(res);
		else if (res.getHref() != null)
			parent.showURL(res.getHref());
	}

	private void postUpdate(final Object obj) {
		treeViewer.getControl().getDisplay().asyncExec(new Runnable() {
			public void run() {
				treeViewer.update(obj, null);
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.help.ui.internal.views.IHelpPart#fillContextMenu(org.eclipse.jface.action.IMenuManager)
	 */
	public boolean fillContextMenu(IMenuManager manager) {
		return parent.fillSelectionProviderMenu(treeViewer, manager);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.help.ui.internal.views.IHelpPart#hasFocus()
	 */
	public boolean hasFocusControl(Control focusControl) {
		return treeViewer.getControl().equals(focusControl);
	}

	public void setFocus() {
		if (treeViewer != null)
			treeViewer.getTree().setFocus();
	}

	public IAction getGlobalAction(String id) {
		if (id.equals(ActionFactory.COPY.getId()))
			return parent.getCopyAction();
		return null;
	}

	public void stop() {
		// TODO Auto-generated method stub
		
	}
}