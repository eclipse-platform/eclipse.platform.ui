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
import org.eclipse.help.ui.internal.util.OverlayIcon;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

public class AllTopicsPart extends HyperlinkTreePart implements IHelpPart {
	private Image containerWithTopicImage;

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
				if (expandable) {
					ITopic topic = (ITopic)obj;
					if (topic.getHref()!=null)
						return containerWithTopicImage;
				}
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
		super(parent, toolkit, tbm);
	}
	
	protected void configureTreeViewer() {
		initializeImages();
		treeViewer.setContentProvider(new TopicsProvider());
		treeViewer.setLabelProvider(new TopicsLabelProvider());
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
	}
	
	private void initializeImages() {
		ImageDescriptor base = HelpUIResources.getImageDescriptor(IHelpUIConstants.IMAGE_CONTAINER);
		ImageDescriptor ovr = HelpUIResources.getImageDescriptor(IHelpUIConstants.IMAGE_DOC_OVR);
		ImageDescriptor desc = new OverlayIcon(base, new ImageDescriptor[][] { {ovr}});
		containerWithTopicImage = desc.createImage();
	}
	
	public void dispose() {
		containerWithTopicImage.dispose();
		super.dispose();
	}

	protected void doOpen(Object obj) {
		if (!(obj instanceof IHelpResource))
			return;
		IHelpResource res = (IHelpResource)obj;
		if (res instanceof IToc
				|| (res instanceof ITopic
						&& ((ITopic) obj).getSubtopics().length > 0 && res.getHref() == null))
			treeViewer.setExpandedState(obj, !treeViewer.getExpandedState(res));
		if (res instanceof IToc)
			postUpdate(res);
		else if (res.getHref() != null)
			parent.showURL(res.getHref());
	}
	
	protected String getHref(IHelpResource res) {
		return (res instanceof ITopic) ? res.getHref() : null;
	}

	protected boolean canAddBookmarks() {
		return true;
	}
}