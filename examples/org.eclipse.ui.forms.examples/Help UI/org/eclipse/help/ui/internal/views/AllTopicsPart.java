/*
 * Created on Dec 13, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.eclipse.help.ui.internal.views;

import org.eclipse.help.HelpSystem;
import org.eclipse.help.IHelpResource;
import org.eclipse.help.IToc;
import org.eclipse.help.ITopic;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.examples.internal.ExamplesPlugin;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * @author dejan
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class AllTopicsPart extends AbstractFormPart implements IHelpPart {
	private ReusableHelpPart parent;

	private String id;

	private Composite container;

	private TreeViewer treeViewer;

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
				String key = expanded ? ExamplesPlugin.IMG_HELP_TOC_OPEN
						: ExamplesPlugin.IMG_HELP_TOC_CLOSED;
				return ExamplesPlugin.getDefault().getImage(key);
			}
			if (obj instanceof ITopic) {
				boolean expandable = treeViewer.isExpandable(obj);
				String key = expandable ? ExamplesPlugin.IMG_HELP_CONTAINER
						: ExamplesPlugin.IMG_HELP_TOPIC;
				return ExamplesPlugin.getDefault().getImage(key);
			}
			return super.getImage(obj);
		}
	}

	/**
	 * @param parent
	 * @param toolkit
	 * @param style
	 */
	public AllTopicsPart(Composite parent, FormToolkit toolkit) {
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
		treeViewer.setInput(this);
		treeViewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
		treeViewer.addTreeListener(new ITreeViewerListener() {
			public void treeCollapsed(TreeExpansionEvent event) {
				Object obj = event.getElement();
				if (obj instanceof IToc) {
					postUpdate(HelpSystem.getTocs());
				}
			}

			public void treeExpanded(TreeExpansionEvent event) {
				Object obj = event.getElement();
				if (obj instanceof IToc) {
					IToc[] tocs = HelpSystem.getTocs();
					for (int i = 0; i < tocs.length; i++) {
						if (!obj.equals(tocs[i]))
							treeViewer.setExpandedState(tocs[i], false);
					}
					postUpdate(tocs);
				}
			}
			private void postUpdate(final Object [] objs) {
				treeViewer.getControl().getDisplay().asyncExec(new Runnable() {
					public void run() {
						treeViewer.update(objs, null);
					}
				});
			}
		});
		treeViewer.addOpenListener(new IOpenListener() {
			public void open(OpenEvent event) {
				doOpenSelection((IStructuredSelection) event.getSelection());
			}
		});
		/*
		 * treeViewer.getTree().addMouseMoveListener(new MouseMoveListener() {
		 * public void mouseMove(MouseEvent e) { Point p = new Point(e.x, e.y);
		 * TreeItem item = treeViewer.getTree().getItem(p); if (item!=null) {
		 * Object obj = item.getData(); if (obj instanceof IHelpResource) {
		 * treeViewer.getTree().setCursor(FormsResources.getHandCursor());
		 * return; } } treeViewer.getTree().setCursor(null); } });
		 */
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
			if (res instanceof IToc) {
				IToc[] allTocs = HelpSystem.getTocs();
				for (int i = 0; i < allTocs.length; i++) {
					boolean state = allTocs[i].equals(res) ? true : false;
					treeViewer.setExpandedState(allTocs[i], state);
				}
			} else
				parent.showURL(res.getHref());
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.help.ui.internal.views.IHelpPart#fillContextMenu(org.eclipse.jface.action.IMenuManager)
	 */
	public boolean fillContextMenu(IMenuManager manager) {
		return parent.fillSelectionProviderMenu(treeViewer, manager);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.help.ui.internal.views.IHelpPart#hasFocus()
	 */
	public boolean hasFocusControl(Control focusControl) {
		return treeViewer.getControl().equals(focusControl);		
	}
}