/*******************************************************************************
 * Copyright (c) 2003, 2023 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 * Oakland Software (Francis Upton - francisu@ieee.org)
 *    bug 197113 Project Explorer drag and drop selection not working properly
 *******************************************************************************/
package org.eclipse.ui.navigator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.internal.navigator.CommonNavigatorFrameSource;
import org.eclipse.ui.internal.navigator.ContributorTrackingSet;
import org.eclipse.ui.internal.navigator.NavigatorContentService;
import org.eclipse.ui.internal.navigator.NavigatorDecoratingLabelProvider;
import org.eclipse.ui.internal.navigator.NavigatorPipelineService;
import org.eclipse.ui.internal.navigator.dnd.NavigatorDnDService;
import org.eclipse.ui.internal.navigator.extensions.NavigatorContentDescriptorManager;
import org.eclipse.ui.internal.navigator.framelist.FrameList;

/**
 *
 * Provides the Tree Viewer for the Common Navigator. Content and labels are
 * provided by an instance of {@link INavigatorContentService}&nbsp; which uses
 * the ID supplied in the constructor
 * {@link CommonViewer#CommonViewer(String, Composite, int)} or through
 * {@link NavigatorContentServiceFactory#createContentService(String, org.eclipse.jface.viewers.StructuredViewer)}.
 *
 * <p>
 * Clients may extend this class.
 * </p>
 *
 * <p>
 * Note that as of 3.2.1 and 3.3, the common viewer caches its selection.
 * Clients must not set the selection of the viewer's tree control directly.
 * </p>
 *
 * @since 3.2
 */
public class CommonViewer extends TreeViewer {

	private final NavigatorContentService contentService;

	private ISelection cachedSelection;

	private FrameList frameList;

	private CommonNavigator commonNavigator;

	private ICommonViewerMapper _mapper;

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
	 * @param aViewerId
	 *            An id tied to the extensions that is used to focus specific
	 *            content to a particular instance of the Common Navigator
	 * @param aParent
	 *            A Composite parent to contain the actual SWT widget
	 * @param aStyle
	 *            A style mask that will be used to create the TreeViewer
	 *            Composite.
	 */
	public CommonViewer(String aViewerId, Composite aParent, int aStyle) {
		super(aParent, aStyle);
		contentService = new NavigatorContentService(aViewerId, this);
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
		setLabelProvider(new NavigatorDecoratingLabelProvider(contentService.createCommonLabelProvider()));
		initDragAndDrop();
	}

	void setCommonNavigator(CommonNavigator navigator) {
		commonNavigator = navigator;
	}

	/**
	 * Sets the {@link ICommonViewerMapper} to work with this viewer.
	 *
	 * @param mapper the mapper
	 * @since 3.4
	 */
	public void setMapper(ICommonViewerMapper mapper) {
		_mapper = mapper;
	}

	/**
	 * Gets the {@link ICommonViewerMapper} assigned to this viewer.
	 *
	 * @return the mapper
	 * @since 3.4
	 */
	public ICommonViewerMapper getMapper() {
		return _mapper;
	}

	/**
	 * @return the CommonNavigator
	 * @since 3.4
	 */
	public CommonNavigator getCommonNavigator() {
		return commonNavigator;
	}

	protected void removeWithoutRefresh(Object[] elements) {
		super.remove(elements);
	}

	/**
	 * <p>
	 * Adds DND support to the Navigator. Uses hooks into the extensible framework
	 * for DND.
	 * </p>
	 * <p>
	 * By default, the following Transfer types are supported:
	 * </p>
	 * <ul>
	 * <li>LocalSelectionTransfer.getInstance(),
	 * <li>PluginTransfer.getInstance()
	 * </ul>
	 *
	 * @see CommonDragAdapter
	 * @see CommonDropAdapter
	 */
	protected void initDragAndDrop() {

		int operations = DND.DROP_COPY | DND.DROP_MOVE | DND.DROP_LINK;

		CommonDragAdapter dragAdapter = createDragAdapter();
		addDragSupport(operations, dragAdapter.getSupportedDragTransfers(),
				dragAdapter);

		CommonDropAdapter dropAdapter = createDropAdapter();
		addDropSupport(operations, dropAdapter.getSupportedDropTransfers(),
				dropAdapter);

		NavigatorDnDService dnd = (NavigatorDnDService)contentService.getDnDService();
		dnd.setDropAdaptor(dropAdapter);
	}


	/**
	 * Creates the {@link CommonDragAdapter}, this is used to provide a subclass
	 * if desired.
	 *
	 * @return the CommonDragAdapter
	 *
	 * @since 3.4
	 */
	protected CommonDragAdapter createDragAdapter() {
		return new CommonDragAdapter(contentService, this);
	}


	/**
	 * Creates the {@link CommonDropAdapter}, this is used to provide a subclass
	 * if desired.
	 *
	 * @return the CommonDropAdapter
	 *
	 * @since 3.4
	 */
	protected CommonDropAdapter createDropAdapter() {
		return new CommonDropAdapter(contentService, this);
	}


	/*
	 * @see ContentViewer#handleLabelProviderChanged(LabelProviderChangedEvent)
	 */
	@Override
	protected void handleLabelProviderChanged(LabelProviderChangedEvent event) {

		Object[] changed = event.getElements();
		if (changed != null) {
			List<Object> others = new ArrayList<>();
			for (Object changedElement : changed) {
				if (changedElement == null)
					continue;

				if (_mapper != null) {
					if (_mapper.handlesObject(changedElement)) {
						_mapper.objectChanged(changedElement);
						continue;
					}
				}
				others.add(changedElement);
			}
			if (others.isEmpty()) {
				return;
			}
			event = new LabelProviderChangedEvent((IBaseLabelProvider) event
					.getSource(), others.toArray());
		}
		super.handleLabelProviderChanged(event);
	}

	@Override
	protected void handleDispose(DisposeEvent event) {
		dispose();
		super.handleDispose(event);
	}

	/**
	 * <p>
	 * Disposes of the NavigatorContentService, which will dispose the Content
	 * and Label providers.
	 * </p>
	 */
	public void dispose() {
		if (contentService != null) {
			contentService.dispose();
		}
		clearSelectionCache();
	}

	/**
	 * Sets this viewer's sorter and triggers refiltering and resorting of this
	 * viewer's element. Passing <code>null</code> turns sorting off.
	 *
	 * @param sorter
	 *            a viewer sorter, or <code>null</code> if none
	 */
	@Override
	public void setSorter(ViewerSorter sorter) {
		if (sorter != null && sorter instanceof CommonViewerSorter commonSorter) {
			commonSorter.setContentService(contentService);
		}

		super.setSorter(sorter);
	}

	/**
	 * <p>
	 * The {@link INavigatorContentService}provides the hook into the framework
	 * to provide content from the various extensions.
	 * </p>
	 *
	 * @return The {@link INavigatorContentService}that was created when the
	 *         viewer was created.
	 */
	public INavigatorContentService getNavigatorContentService() {
		return contentService;
	}

	@Override
	public void add(Object parentElement, Object... childElements) {
		NavigatorPipelineService pipeDream = (NavigatorPipelineService) contentService
				.getPipelineService();

		PipelinedShapeModification modification = new PipelinedShapeModification(
				parentElement, new ContributorTrackingSet(contentService,
						childElements));

		pipeDream.interceptAdd(modification);

		Object parent = (parentElement == getInput()) ? getInput()
				: modification.getParent();

		super.add(parent, modification.getChildren().toArray());
	}

	/**
	 * <p>
	 * Removals are handled by refreshing the parents of each of the given
	 * elements. The parents are determined via calls ot the contentProvider.
	 * </p>
	 *
	 * @see org.eclipse.jface.viewers.AbstractTreeViewer#remove(java.lang.Object[])
	 */
	@Override
	public void remove(Object... elements) {
		NavigatorPipelineService pipeDream = (NavigatorPipelineService) contentService
				.getPipelineService();

		PipelinedShapeModification modification = new PipelinedShapeModification(
				null, new ContributorTrackingSet(contentService, elements));

		pipeDream.interceptRemove(modification);

		super.remove(modification.getChildren().toArray());
	}

	@Override
	public void refresh(Object element, boolean updateLabels) {
		// Invalidate caches in NavigatorContentDescriptorManager (see
		// https://bugs.eclipse.org/436645).
		NavigatorContentDescriptorManager.getInstance().clearCache();

		if (element != getInput()) {
			INavigatorPipelineService pipeDream = contentService.getPipelineService();

			PipelinedViewerUpdate update = new PipelinedViewerUpdate();
			update.getRefreshTargets().add(element);
			update.setUpdateLabels(updateLabels);
			// If the update is modified.
			if (pipeDream.interceptRefresh(update)) {
				// Intercept and apply the update.
				boolean toUpdateLabels = update.isUpdateLabels();
				for (Object elem : update.getRefreshTargets()) {
					super.refresh(elem, toUpdateLabels);
				}
			} else {
				super.refresh(element, updateLabels);
			}
		} else {
			super.refresh(element, updateLabels);
		}
	}

	@Override
	public void setSelection(ISelection selection, boolean reveal) {

		if (selection instanceof IStructuredSelection sSelection) {
			INavigatorPipelineService pipeDream = contentService
					.getPipelineService();

			PipelinedViewerUpdate update = new PipelinedViewerUpdate();
			update.getRefreshTargets().addAll(sSelection.toList());
			update.setUpdateLabels(false);
			/* if the update is modified */
			if (pipeDream.interceptRefresh(update)) {
				/* intercept and apply the update */
				super.setSelection(new StructuredSelection(update.getRefreshTargets().toArray()) , reveal);
			} else {
				super.setSelection(selection, reveal);
			}
		}
	}

	@Override
	protected void hookControl(Control control) {
		super.hookControl(control);
		// FIXME - This caching thing should not be here; it's brittle.
		// The underlying problem of over-calling of getSelection() should
		// be addressed instead (see bugs 144294 and 140032)
		// The DragStart event will come before the SelectionEvent on
		// some platforms (GTK).  Since DragStart can turn around and
		// call getSelection(), we need to clear the cache.
		control.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				clearSelectionCache();
			}
		});
	}


	/**
	 * Update an item in the tree.
	 *
	 * @param item the item in the tree to update
	 * @since 3.4
	 *
	 */
	public void doUpdateItem(Widget item) {
		doUpdateItem(item, item.getData(), true);
	}

	/*
	 * @see StructuredViewer#mapElement(Object, Widget)
	 */
	@Override
	protected void mapElement(Object element, Widget item) {
		super.mapElement(element, item);
		if (_mapper != null && item instanceof Item it) {
			_mapper.addToMap(element, it);
		}
	}

	/*
	 * @see StructuredViewer#unmapElement(Object, Widget)
	 */
	@Override
	protected void unmapElement(Object element, Widget item) {
		if (_mapper != null && item instanceof Item it) {
			_mapper.removeFromMap(element, it);
		}
		super.unmapElement(element, item);
	}

	/*
	 * @see StructuredViewer#unmapAllElements()
	 */
	@Override
	protected void unmapAllElements() {
		if (_mapper != null)
			_mapper.clearMap();
		super.unmapAllElements();
	}

	@Override
	protected void setSelectionToWidget(List v, boolean reveal) {
		clearSelectionCache();
		super.setSelectionToWidget(v, reveal);
	}

	@Override
	protected void handleDoubleSelect(SelectionEvent event) {
		clearSelectionCache();
		super.handleDoubleSelect(event);
	}

	@Override
	protected void handleOpen(SelectionEvent event) {
		clearSelectionCache();
		super.handleOpen(event);
	}

	@Override
	protected void handlePostSelect(SelectionEvent e) {
		clearSelectionCache();
		super.handlePostSelect(e);
	}

	@Override
	protected void handleSelect(SelectionEvent event) {
		clearSelectionCache();
		super.handleSelect(event);
	}

	/**
	 * Clears the selection cache.
	 */
	private void clearSelectionCache() {
		cachedSelection = null;
	}

	/**
	 * Returns the current selection.
	 * <p>
	 * Note that as of 3.2.1 and 3.3, the common viewer caches its selection.
	 * Clients must not set the selection of the viewer's tree control directly.
	 * </p>
	 *
	 * @see org.eclipse.jface.viewers.AbstractTreeViewer#getSelection()
	 */
	@Override
	public ISelection getSelection() {
		if (cachedSelection == null) {
			cachedSelection = super.getSelection();
		}
		return cachedSelection;
	}

	@Override
	public void refresh(Object element) {
		refresh(element, true);
	}

	@Override
	public void update(Object element, String[] properties) {
		if(element != getInput()) {
			INavigatorPipelineService pipeDream = contentService
					.getPipelineService();

			PipelinedViewerUpdate update = new PipelinedViewerUpdate();
			update.getRefreshTargets().add(element);
			update.setUpdateLabels(true);
			/* if the update is modified */
			if (pipeDream.interceptUpdate(update)) {
				/* intercept and apply the update */
				for (Iterator<Object> iter = update.getRefreshTargets().iterator(); iter
						.hasNext();) {
					super.update(iter.next(), properties);
				}
			} else {
				super.update(element, properties);
			}
		} else {
			super.update(element, properties);
		}
	}

	@Override
	public String toString() {
		return contentService + " Viewer"; //$NON-NLS-1$
	}

	@Override
	protected void internalRefresh(Object element, boolean updateLabels) {
		if (element == null && getRoot() == null) {
			return;
		}
		super.internalRefresh(element, updateLabels);
	}

	/**
	 * @noreference This method is not intended to be referenced by clients.
	 * @nooverride This method is not intended to be re-implemented or extended by clients.
	 * @since 3.4
	 */
	public void createFrameList() {
		CommonNavigatorFrameSource frameSource = new CommonNavigatorFrameSource(commonNavigator);
		frameList = new FrameList(frameSource);
		frameSource.connectTo(frameList);
	}

	/**
	 * @return a FrameList
	 * @noreference This method is not intended to be referenced by clients.
	 * @nooverride This method is not intended to be re-implemented or extended by clients.
	 * @since 3.4
	 */
	public FrameList getFrameList() {
		return frameList;
	}


}
