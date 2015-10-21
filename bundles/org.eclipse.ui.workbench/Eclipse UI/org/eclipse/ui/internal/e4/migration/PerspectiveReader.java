/*******************************************************************************
 * Copyright (c) 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.e4.migration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.e4.migration.InfoReader.PageReader;
import org.eclipse.ui.internal.e4.migration.InfoReader.PartState;

public class PerspectiveReader extends MementoReader {

	private DescriptorReader descriptor;

	public PerspectiveReader(IMemento memento) {
		super(memento);
	}

	String getId() {
		return getDescriptor().getId();
	}

	String getLabel() {
		return getDescriptor().getLabel();
	}

	private DescriptorReader getDescriptor() {
		if (descriptor == null) {
			IMemento desriptorMem = getChild(IWorkbenchConstants.TAG_DESCRIPTOR);
			if (desriptorMem == null) {
				throw new NullPointerException("Perspective descriptor not found"); //$NON-NLS-1$
			}
			descriptor = new DescriptorReader(desriptorMem);
		}
		return descriptor;
	}

	List<InfoReader> getInfos() {
		IMemento[] infoMems = getInfoMems();
		List<InfoReader> infos = new ArrayList<>(infoMems.length);
		for (IMemento infoMem : infoMems) {
			infos.add(new InfoReader(infoMem));
		}
		return infos;
	}

	private IMemento[] getInfoMems() {
		IMemento[] infoMems = null;
		IMemento layout = getLayout();
		if (layout != null) {
			IMemento mainWindow = layout.getChild(IWorkbenchConstants.TAG_MAIN_WINDOW);
			if (mainWindow != null) {
				infoMems = mainWindow.getChildren(IWorkbenchConstants.TAG_INFO);
			}
		}
		if (infoMems == null) {
			infoMems = new IMemento[0];
		}
		return infoMems;
	}

	Map<String, ViewLayoutReader> getViewLayouts() {
		IMemento[] viewLayoutMems = getChildren(IWorkbenchConstants.TAG_VIEW_LAYOUT_REC);
		Map<String, ViewLayoutReader> viewLayouts = new HashMap<>(viewLayoutMems.length);
		for (IMemento memento : viewLayoutMems) {
			ViewLayoutReader viewLayout = new ViewLayoutReader(memento);
			viewLayouts.put(viewLayout.getViewId(), viewLayout);
		}
		return viewLayouts;
	}

	private IMemento getLayout() {
		return getChild(IWorkbenchConstants.TAG_LAYOUT);
	}

	List<String> getPerspectiveShortcutIds() {
		return getChildrenIds(IWorkbenchConstants.TAG_PERSPECTIVE_ACTION);
	}

	List<String> getActionSetIds() {
		return getChildrenIds(IWorkbenchConstants.TAG_ALWAYS_ON_ACTION_SET);
	}

	List<String> getShowViewActionIds() {
		return getChildrenIds(IWorkbenchConstants.TAG_SHOW_VIEW_ACTION);
	}

	List<String> getNewWizardActionIds() {
		return getChildrenIds(IWorkbenchConstants.TAG_NEW_WIZARD_ACTION);
	}

	List<String> getRenderedViewIds() {
		List<String> viewIds = getChildrenIds(IWorkbenchConstants.TAG_VIEW);
		viewIds.addAll(getFastViewIds());
		return viewIds;
	}

	/**
	 * @return map of fast view bar's ID and side
	 */
	Map<String, Integer> getFastViewBars() {
		Map<String, Integer> bars = new HashMap<>();
		for (IMemento bar : getFastViewBarMems()) {
			bars.put(bar.getString(IWorkbenchConstants.TAG_ID),
					bar.getInteger(IWorkbenchConstants.TAG_FAST_VIEW_SIDE));
		}
		return bars;
	}

	private List<String> getFastViewIds() {
		List<String> fastViewIds = new ArrayList<>();

		IMemento fastViews = getChild(IWorkbenchConstants.TAG_FAST_VIEWS);
		if (fastViews != null) {
			for (IMemento view : fastViews.getChildren(IWorkbenchConstants.TAG_VIEW)) {
				fastViewIds.add(view.getString(IWorkbenchConstants.TAG_ID));
			}
		}

		IMemento[] fastViewBarArr = getFastViewBarMems();
		for (IMemento fastViewBar : fastViewBarArr) {
			IMemento fastViewsInBar = fastViewBar.getChild(IWorkbenchConstants.TAG_FAST_VIEWS);
			if (fastViewsInBar != null) {
				for (IMemento view : fastViewsInBar.getChildren(IWorkbenchConstants.TAG_VIEW)) {
					fastViewIds.add(view.getString(IWorkbenchConstants.TAG_ID));
				}
			}
		}
		return fastViewIds;
	}

	List<String> getDefaultFastViewBarViewIds() {
		List<String> fastViewIds = new ArrayList<>();
		IMemento fastViews = getChild(IWorkbenchConstants.TAG_FAST_VIEWS);
		if (fastViews != null) {
			for (IMemento view : fastViews.getChildren(IWorkbenchConstants.TAG_VIEW)) {
				fastViewIds.add(view.getString(IWorkbenchConstants.TAG_ID));
			}
		}

		return fastViewIds;
	}

	private IMemento[] getFastViewBarMems() {
		IMemento[] emptyArr = new IMemento[0];
		IMemento fastViewBars = getChild(IWorkbenchConstants.TAG_FAST_VIEW_BARS);
		if (fastViewBars == null) {
			return emptyArr;
		}
		IMemento[] fastViewBarArr = fastViewBars.getChildren(IWorkbenchConstants.TAG_FAST_VIEW_BAR);
		return fastViewBarArr == null ? emptyArr : fastViewBarArr;
	}

	List<String> getHiddenMenuItemIds() {
		return getChildrenIds(IWorkbenchConstants.TAG_HIDE_MENU);
	}

	List<String> getHiddenToolbarItemIds() {
		return getChildrenIds(IWorkbenchConstants.TAG_HIDE_TOOLBAR);
	}

	private List<String> getChildrenIds(String tag) {
		IMemento[] idMemArr = getChildren(tag);
		List<String> idList = new ArrayList<>(idMemArr.length);
		for (IMemento idMem : idMemArr) {
			idList.add(idMem.getString(IWorkbenchConstants.TAG_ID));
		}
		return idList;
	}

	List<DetachedWindowReader> getDetachedWindows() {
		List<DetachedWindowReader> readers = new ArrayList<>();
		IMemento layout = getLayout();
		if (layout != null) {
			IMemento[] mems = layout.getChildren(IWorkbenchConstants.TAG_DETACHED_WINDOW);
			for (IMemento mem : mems) {
				readers.add(new DetachedWindowReader(mem));
			}
		}
		return readers;
	}

	boolean isCustom() {
		return getDescriptor().isCustom();
	}

	String getBasicPerspectiveId() {
		return getDescriptor().getBasicPerspectiveId();
	}

	String getOriginalId() {
		return getDescriptor().getOriginalId();
	}

	boolean isEditorAreaVisible() {
		return Integer.valueOf(1).equals(getInteger(IWorkbenchConstants.TAG_AREA_VISIBLE));
	}

	PartState getEditorAreaState() {
		PartState state = PartState.RESTORED;
		int value = getInteger(IWorkbenchConstants.TAG_AREA_TRIM_STATE);
		switch (value) {
		case 0:
		case 4: // minimized by zoom
			state = PartState.MINIMIZED;
			break;
		case 1:
			state = PartState.MAXIMIZED;
			break;
		}
		return state;
	}

	static class DetachedWindowReader extends MementoReader {

		private DetachedWindowReader(IMemento memento) {
			super(memento);
		}

		Rectangle getBounds() {
			Rectangle windowBounds = new Rectangle(0, 0, 0, 0);
			Integer bigInt = getInteger(IWorkbenchConstants.TAG_X);
			windowBounds.x = bigInt == null ? 0 : bigInt.intValue();
			bigInt = getInteger(IWorkbenchConstants.TAG_Y);
			windowBounds.y = bigInt == null ? 0 : bigInt.intValue();
			bigInt = getInteger(IWorkbenchConstants.TAG_WIDTH);
			windowBounds.width = bigInt == null ? 0 : bigInt.intValue();
			bigInt = getInteger(IWorkbenchConstants.TAG_HEIGHT);
			windowBounds.height = bigInt == null ? 0 : bigInt.intValue();
			return windowBounds;
		}

		String getActivePageId() {
			String activePageId = null;
			IMemento folder = getFolder();
			if (folder != null) {
				activePageId = folder.getString(IWorkbenchConstants.TAG_ACTIVE_PAGE_ID);
			}
			return activePageId;
		}

		List<PageReader> getPages() {
			IMemento folder = getFolder();
			List<PageReader> pages = new ArrayList<>();
			if (folder != null) {
				IMemento[] pageMems = folder.getChildren(IWorkbenchConstants.TAG_PAGE);
				for (IMemento pageMem : pageMems) {
					pages.add(new PageReader(pageMem));
				}
			}
			return pages;
		}

		private IMemento getFolder() {
			return getChild(IWorkbenchConstants.TAG_FOLDER);
		}

	}

	private static class DescriptorReader extends MementoReader {

		private static final String TAG_DESCRIPTOR = IWorkbenchConstants.TAG_DESCRIPTOR;

		DescriptorReader(IMemento memento) {
			super(memento);
		}

		String getId() {
			String id = getOriginalId();
			if (isCustom()) {
				id = getBasicPerspectiveId() + "." + id; //$NON-NLS-1$
			}
			return id;
		}

		String getOriginalId() {
			String id = getString(IWorkbenchConstants.TAG_ID);
			if (id == null) {
				throw new NullPointerException("Perspective ID not found"); //$NON-NLS-1$
			}
			return id;
		}

		boolean isCustom() {
			return contains(TAG_DESCRIPTOR);
		}

		String getBasicPerspectiveId() {
			String id = getString(TAG_DESCRIPTOR);
			if (id == null) {
				throw new NullPointerException("Basic perspective ID not found"); //$NON-NLS-1$
			}
			return id;
		}

		String getLabel() {
			return getString(IWorkbenchConstants.TAG_LABEL);
		}

	}

	static class ViewLayoutReader extends MementoReader {

		private ViewLayoutReader(IMemento memento) {
			super(memento);
		}

		String getViewId() {
			return getString(IWorkbenchConstants.TAG_ID);
		}

		boolean isCloseable() {
			return getBoolean(IWorkbenchConstants.TAG_CLOSEABLE, true);
		}

		boolean isStandalone() {
			return getBoolean(IWorkbenchConstants.TAG_STANDALONE);
		}

	}

}
