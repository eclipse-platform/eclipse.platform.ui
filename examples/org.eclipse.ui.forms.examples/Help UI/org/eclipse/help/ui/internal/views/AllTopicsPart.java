/*
 * Created on Dec 13, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.eclipse.help.ui.internal.views;

import org.eclipse.help.*;
import org.eclipse.jface.util.OpenStrategy;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.examples.internal.ExamplesPlugin;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.internal.forms.widgets.FormsResources;

/**
 * @author dejan
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class AllTopicsPart extends AbstractFormPart implements IHelpPart {
	private ReusableHelpPart parent;
	private String id;

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
		treeViewer = new TreeViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		treeViewer.setContentProvider(new TopicsProvider());
		treeViewer.setLabelProvider(new TopicsLabelProvider());
		treeViewer.setInput(this);
		treeViewer.addTreeListener(new ITreeViewerListener() {
		    public void treeCollapsed(TreeExpansionEvent event) {
		    }
		    public void treeExpanded(TreeExpansionEvent event) {
		    	Object obj = event.getElement();
		    	if (obj instanceof IToc) {
		    		IToc [] tocs = HelpSystem.getTocs();
		    		for (int i=0; i<tocs.length; i++) {
		    			if (!obj.equals(tocs[i]))
		    				treeViewer.setExpandedState(tocs[i], false);
		    		}
		    	}
		    }
		});
		treeViewer.addOpenListener(new IOpenListener() {
			public void open(OpenEvent event) {
				doOpenSelection((IStructuredSelection)event.getSelection());
			}
		});
		/*
		treeViewer.getTree().addMouseMoveListener(new MouseMoveListener() {
			public void mouseMove(MouseEvent e) {
				Point p = new Point(e.x, e.y);
				TreeItem item = treeViewer.getTree().getItem(p);
				if (item!=null) {
					Object obj = item.getData();
					if (obj instanceof IHelpResource) {
						treeViewer.getTree().setCursor(FormsResources.getHandCursor());
						return;
					}
				}
				treeViewer.getTree().setCursor(null);
			}
		});
		*/
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.help.ui.internal.views.IHelpPart#getControl()
	 */
	public Control getControl() {
		return treeViewer != null ? treeViewer.getTree() : null;
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
		if (treeViewer != null) {
			treeViewer.getControl().setVisible(visible);
			if (visible)
				treeViewer.refresh();
		}
	}
	private void doOpenSelection(IStructuredSelection sel) {
		IHelpResource res = (IHelpResource)sel.getFirstElement();
		if (res!=null) {
			if (res instanceof IToc) {
				IToc [] allTocs = HelpSystem.getTocs();
				for (int i=0; i<allTocs.length; i++) {
					boolean state = allTocs[i].equals(res)?true:false;
					treeViewer.setExpandedState(allTocs[i], state);
				}
			}
			else
				parent.showURL(res.getHref());
		}
	}
}