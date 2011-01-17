/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.ui.internal.views;

import java.util.Observable;
import java.util.Observer;

import org.eclipse.help.*;
import org.eclipse.help.base.AbstractHelpScope;
import org.eclipse.help.internal.base.scope.ScopeUtils;
import org.eclipse.help.internal.base.scope.WorkingSetScope;
import org.eclipse.help.internal.search.federated.LocalHelpScope;
import org.eclipse.help.internal.workingset.WorkingSet;
import org.eclipse.help.search.ISearchScope;
import org.eclipse.help.ui.internal.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;

public abstract class HyperlinkTreePart extends AbstractFormPart implements
		IHelpPart {
	
	public class ScopeObserver implements Observer {

		public void update(Observable o, Object arg) {
			if (o instanceof ScopeSetManager) {
				refilter();
			}
		}
	}
	
	private class ScopeFilter extends ViewerFilter {
		
		public ScopeFilter(AbstractHelpScope scope) {
			this.scope = scope;
		}
		
		AbstractHelpScope scope;

		public boolean select(Viewer viewer, Object parentElement, Object element) {
			if (element instanceof IToc) {
				return ScopeUtils.showInTree((IToc)element, scope);
			}
			if (element instanceof ITopic) {
				return ScopeUtils.showInTree((ITopic)element, scope);
			}
			if (element instanceof IIndexEntry) {
				return ScopeUtils.showInTree((IIndexEntry)element, scope);
			}
			if (element instanceof IIndexSee) {
				return ScopeUtils.showInTree((IIndexSee)element, scope);
			}
			return true;
		}
		
	}

	protected ReusableHelpPart parent;

	private String id;

	//private Composite container;

	protected TreeViewer treeViewer;

	private TreeItem lastItem;

	private Cursor handCursor;

	private ScopeObserver scopeObserver;

	/**
	 * @param parent
	 * @param toolkit
	 * @param style
	 */
	public HyperlinkTreePart(Composite parent, final FormToolkit toolkit,
			IToolBarManager tbm) {
		handCursor = new Cursor(parent.getDisplay(), SWT.CURSOR_HAND);
		/*
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
		*/
		treeViewer = new TreeViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		treeViewer.getTree().setMenu(parent.getMenu());
		treeViewer.getTree().setForeground(
				toolkit.getHyperlinkGroup().getForeground());
		configureTreeViewer();
		treeViewer.setInput(this);
		treeViewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
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
				long eventTime = e.time & 0xFFFFFFFFL;
				if (eventTime - lastTime <= e.display.getDoubleClickTime())
					return;
				if (e.button != 1)
					return;
				lastTime = eventTime;
				Point p = new Point(e.x, e.y);
				TreeItem item = treeViewer.getTree().getItem(p);
				if (item != null) {
					Object obj = item.getData();
					if (obj != null) {
						doOpen(obj);
					}
				}
			}
		});

		treeViewer.getTree().addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				validateLastItem();
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
					e.gc.setForeground(e.display.getSystemColor(
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
				validateLastItem();
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
				validateLastItem();
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
					treeViewer.getTree().setCursor(handCursor);
					IStructuredSelection ssel = (IStructuredSelection) treeViewer
							.getSelection();
					if (ssel.getFirstElement() == obj)
						item.setForeground(e.display
								.getSystemColor(SWT.COLOR_LIST_SELECTION_TEXT));
					else
						item.setForeground(toolkit.getHyperlinkGroup()
								.getActiveForeground());
					lastItem = item;
					repaintItem(lastItem);
					if (obj instanceof IHelpResource)
						updateStatus((IHelpResource) obj);
					else
						updateStatus(null);
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
		scopeObserver = new ScopeObserver();
		ScopeState.getInstance().getScopeSetManager().addObserver(scopeObserver);
	}

	public void dispose() {
		handCursor.dispose();
		if (scopeObserver != null) {
			ScopeState.getInstance().getScopeSetManager().deleteObserver(scopeObserver);
		}
		super.dispose();
	}

	private void repaintItem(TreeItem item) {
		Rectangle bounds = item.getBounds();
		item.getParent().redraw(bounds.x, bounds.y, bounds.width,
				bounds.height, false);
	}

	protected void contributeToToolBar(IToolBarManager tbm) {
		Action collapseAllAction = new Action() {
			public void run() {
				BusyIndicator.showWhile(getControl().getDisplay(), new Runnable() {
					public void run() {
						doCollapseAll();
					}
				});
			}
		};
		collapseAllAction.setImageDescriptor(HelpUIResources
				.getImageDescriptor(IHelpUIConstants.IMAGE_COLLAPSE_ALL));
		collapseAllAction
				.setToolTipText(Messages.AllTopicsPart_collapseAll_tooltip); 
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
		return treeViewer.getControl();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.help.ui.internal.views.IHelpPart#init(org.eclipse.help.ui.internal.views.NewReusableHelpPart)
	 */
	public void init(ReusableHelpPart parent, String id, IMemento memento) {
		this.parent = parent;
		this.id = id;
	    refilter();
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
		getControl().setVisible(visible);
		if (visible)
			treeViewer.refresh();
	}

	private void doOpenSelection(IStructuredSelection sel) {
		Object obj = sel.getFirstElement();
		if (obj != null) {
			doOpen(obj);
		}
	}

	protected void handleSelectionChanged(IStructuredSelection sel) {
		Object obj = sel.getFirstElement();
		if (lastItem != null && !lastItem.isDisposed()) {
			Object lastObj = lastItem.getData();
			if (lastObj==obj)
				lastItem.setForeground(getControl().getDisplay()
							.getSystemColor(SWT.COLOR_LIST_SELECTION_TEXT));
			else
				lastItem.setForeground(parent.getForm().getToolkit().getHyperlinkGroup()
						.getActiveForeground());
			repaintItem(lastItem);
		}
		if (obj instanceof IHelpResource) {
			IHelpResource res = (IHelpResource) obj;
			updateStatus(res, false);
		} else
			updateStatus(null, false);
	}

	private void updateStatus(IHelpResource res) {
		updateStatus(res, true);
	}

	private void updateStatus(IHelpResource res, boolean defaultToSelection) {
		if (defaultToSelection && res == null) {
			IStructuredSelection ssel = (IStructuredSelection) treeViewer
					.getSelection();
			Object obj = ssel.getFirstElement();
			if (obj instanceof IHelpResource)
				res = (IHelpResource) obj;
		}
		if (res != null) {
			String label = res.getLabel();
			String href = getHref(res);
			HyperlinkTreePart.this.parent.handleLinkEntered(new HyperlinkEvent(
					treeViewer.getTree(), href, label, SWT.NULL));
		} else {
			HyperlinkTreePart.this.parent.handleLinkExited(null);
		}
	}

	protected String getHref(IHelpResource res) {
		return res.getHref();
	}

	protected abstract void configureTreeViewer();

	protected abstract void doOpen(Object obj);

	protected void postUpdate(final Object obj) {
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
		return parent.fillSelectionProviderMenu(treeViewer, manager,
				canAddBookmarks());
	}

	protected abstract boolean canAddBookmarks();

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
	}

	private void validateLastItem() {
		if (lastItem != null && lastItem.isDisposed())
			lastItem = null;
	}
	
	public void refilter() {
		ScopeSetManager manager = ScopeState.getInstance().getScopeSetManager();
		ScopeSet set = manager.getActiveSet();
		EngineDescriptor[] engineDescriptors = parent.getEngineManager().getDescriptors();
		ISearchScope scope = null;
		for (int i = 0; i < engineDescriptors.length; i++) {
			final EngineDescriptor ed = engineDescriptors[i];
			if (ed.getEngineTypeId().equals("org.eclipse.help.ui.localSearch") //$NON-NLS-1$
				  && ed.getEngine() != null) {
				scope = ed.createSearchScope(set.getPreferenceStore());
			}
		}
		WorkingSet workingSet = null;
		LocalHelpScope localScope = (LocalHelpScope) scope;
		workingSet = localScope.getWorkingSet() ;
		treeViewer.resetFilters();
		if (workingSet != null) {
			WorkingSetScope helpScope = new WorkingSetScope(workingSet, set.getName());
		    treeViewer.addFilter(new ScopeFilter(helpScope));
		}
		treeViewer.refresh();
	}
}
