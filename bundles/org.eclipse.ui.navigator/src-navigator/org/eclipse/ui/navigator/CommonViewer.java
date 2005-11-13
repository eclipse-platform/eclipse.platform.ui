/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.navigator;

import java.util.ArrayList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.navigator.internal.AdaptabilityUtility;
import org.eclipse.ui.navigator.internal.CommonNavigatorMessages;
import org.eclipse.ui.navigator.internal.NavigatorContentService;
import org.eclipse.ui.navigator.internal.NavigatorPlugin;
import org.eclipse.ui.navigator.internal.dnd.CommonNavigatorDragAdapter;
import org.eclipse.ui.navigator.internal.dnd.CommonNavigatorDropAdapter;
import org.eclipse.ui.part.PluginTransfer;

/**
 * <p>
 * Provides the Tree Viewer for the Common Navigator. Content and labels are
 * provided by an instance of
 * {@link org.eclipse.ui.navigator.internal.NavigatorContentService}&nbsp;
 * which uses the ID supplied in the constructor
 * {@link CommonViewer#CommonViewer(String, Composite, int)}.
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * @since 3.2
 */
public class CommonViewer extends TreeViewer {

	private final NavigatorContentService contentService;
 
	/**
	 * <p>
	 * Constructs the Tree Viewer for the Common Navigator and the corresponding
	 * NavigatorContentService. The NavigatorContentService will provide the
	 * Content Provider and Label Provider -- these need not be supplied by
	 * clients.
	 * <p>
	 * For the valid bits to supply in the style mask (aStyle), see
	 * documentation provided by {@link TreeViewer}.
	 * </p>
	 * 
	 * @param anId
	 *            An id tied to the extensions that is used to focus specific
	 *            content to a particular instance of the Common Navigator
	 * @param aParent
	 *            A Composite parent to contain the actual SWT widget
	 * @param aStyle
	 *            A style mask that will be used to create the TreeViewer
	 *            Composite.
	 */
	public CommonViewer(String aCommonNavigatorId, Composite aParent, int aStyle) {
		super(aParent, aStyle);
		contentService = new NavigatorContentService(aCommonNavigatorId, this);
		init();
	}

	/**
	 * <p>
	 * Initializes the content provider, label provider, and drag and drop
	 * support. Should not be called by clients -- this method is invoked when
	 * the constructor is invoked.
	 * </p>
	 */
	protected void init() {
		setUseHashlookup(true);
		setContentProvider(contentService.createCommonContentProvider());
		DecoratingLabelProvider decoratingProvider = new DecoratingLabelProvider(
				contentService.createCommonLabelProvider(), PlatformUI
						.getWorkbench().getDecoratorManager()
						.getLabelDecorator());
		setLabelProvider(decoratingProvider); 
		initDragAndDrop();

	}

	/**
	 * <p>
	 * Disposes of the NavigatorContentService, which will dispose the Content
	 * and Label providers.
	 * </p>
	 */
	public void dispose() {

		if (contentService != null)
			contentService.dispose();

	}

	/**
	 * <p>
	 * The {@link NavigatorContentService}provides the hook into the framework
	 * to provide content from the various extensions.
	 * </p>
	 * 
	 * @return The {@link NavigatorContentService}that was created when the
	 *         viewer was created.
	 */
	public NavigatorContentService getNavigatorContentService() {
		return contentService;
	}

	/**
	 * <p>
	 * Whenever the internal structure of the tree changes through this method,
	 * a refresh of the parent will occur. When one extension has found the need
	 * to modify a parent, other extensions may also choose to do update that
	 * node as well. Extensions are also not permitted to add children to a
	 * viewer that they would not have returned from a call to the extension
	 * content provider ({@link ITreeContentProvider}).
	 * 
	 * @see org.eclipse.jface.viewers.AbstractTreeViewer#internalAdd(org.eclipse.swt.widgets.Widget,
	 *      java.lang.Object, java.lang.Object[])
	 */
	protected void internalAdd(Widget widget, Object parentElement,
			Object[] childElements) {
		super.internalRefresh(widget, parentElement, true, true);
	}

	/**
	 * <p>
	 * Removals are handled by refreshing the parents of each of the given
	 * elements. The parents are determined via calls ot the contentProvider.
	 * </p>
	 * 
	 * @see org.eclipse.jface.viewers.AbstractTreeViewer#remove(java.lang.Object[])
	 */
	public void remove(Object[] elements) {
		super.remove(elements);
		ITreeContentProvider contentProvider = (ITreeContentProvider) getContentProvider();
		for (int i = 0; i < elements.length; i++) {
			super.internalRefresh(contentProvider.getParent(elements[i]));
		}
	}

	protected void removeWithoutRefresh(Object[] elements) {
		super.remove(elements);
	}

	/**
	 * <p>
	 * Adds DND support to the Navigator. Uses hooks into the extensible
	 * framework for DND.
	 * </p>
	 * <p>
	 * By default, the following Transfer types are supported:
	 * <ul>
	 * <li>LocalSelectionTransfer.getInstance(),
	 * <li>PluginTransfer.getInstance(),
	 * <li>FileTransfer.getInstance(), and
	 * <li>ResourceTransfer.getInstance()
	 * </ul>
	 * </p>
	 * 
	 * @see CommonNavigatorDragAdapter
	 * @see CommonNavigatorDropAdapter
	 */
	protected void initDragAndDrop() {
		/* Handle Drag and Drop */
		int operations = DND.DROP_COPY | DND.DROP_MOVE;
		Transfer[] transfers = new Transfer[] {
		// TODO Removed LocalSelectTransfer and ResourceTransfer to break dependency on ide and resources plugins.
		//		LocalSelectionTransfer.getInstance(),
				PluginTransfer.getInstance(), FileTransfer.getInstance(),
		//		ResourceTransfer.getInstance() 
		};
		addDragSupport(operations, transfers, new CommonNavigatorDragAdapter(
				this));
		addDropSupport(operations, transfers, new CommonNavigatorDropAdapter(
				this));

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.AbstractTreeViewer#createTreeItem(org.eclipse.swt.widgets.Widget,
	 *      java.lang.Object, int)
	 */
	protected void createTreeItem(Widget parent, final Object element, int index) {
		// TODO Auto-generated method stub
		try {
			super.createTreeItem(parent, element, index);
		} catch (Exception ex) {
			ex.printStackTrace();
		} catch (Error e) {
			e.printStackTrace();
		}
		Job job = new Job(CommonNavigatorMessages.CommonViewer_0) {
			public IStatus run(IProgressMonitor monitor) {
				try {
					contentService.findRelevantContentProviders(element);
				} catch (RuntimeException ex) {
					String msg = CommonNavigatorMessages.CommonViewer_1
							+ element.getClass();
					NavigatorPlugin.log(msg, new Status(IStatus.ERROR,
							NavigatorPlugin.PLUGIN_ID, 0, msg, ex));
				}
				return Status.OK_STATUS;
			}
		};
		if (element instanceof ISchedulingRule)
			job.setRule((ISchedulingRule) element);
		else {
			ISchedulingRule rule = (ISchedulingRule) AdaptabilityUtility
					.getAdapter(element, ISchedulingRule.class);
			if (rule != null) {
				job.setRule(rule);
			}
		}

		job.schedule();

	}

	/*
	 * @see ContentViewer#handleLabelProviderChanged(LabelProviderChangedEvent)
	 */
	protected void handleLabelProviderChanged(LabelProviderChangedEvent event) {

		Object[] changed = event.getElements();
		if (changed != null) {
			ArrayList others = new ArrayList();
			for (int i = 0; i < changed.length; i++) {
				Object curr = changed[i];
				// TODO Resource Mapper removed. Perhaps this would be a good chance to use ResourceMapping?  
//				if (curr instanceof IResource) {
//					fResourceToItemsMapper.resourceChanged((IResource) curr);
//				} else {
					others.add(curr);
//				}
			}
			if (others.isEmpty()) {
				return;
			}
			event = new LabelProviderChangedEvent((IBaseLabelProvider) event
					.getSource(), others.toArray());
		}
		super.handleLabelProviderChanged(event);
	}

	protected void handleDispose(DisposeEvent event) {
		super.handleDispose(event);
		dispose();
	}

}