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
import java.util.List;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.layout.ITrimManager;
import org.eclipse.ui.internal.layout.TrimArea;

public class WindowReader extends MementoReader {

	WindowReader(IMemento memento) {
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

	boolean isCoolbarVisible() {
		IMemento trimLayoutMem = getChild(IWorkbenchConstants.TAG_TRIM);
		if (trimLayoutMem == null) {
			return false;
		}

		boolean visible = false;
		IMemento[] trimAreas = trimLayoutMem.getChildren(IWorkbenchConstants.TAG_TRIM_AREA);
		IMemento topTrim = null;
		for (IMemento trimArea : trimAreas) {
			if (ITrimManager.TOP == trimArea.getInteger(IMemento.TAG_ID)) {
				topTrim = trimArea;
				break;
			}
		}
		if (topTrim != null) {
			IMemento[] trimItems = topTrim.getChildren(IWorkbenchConstants.TAG_TRIM_ITEM);
			for (IMemento trimItem : trimItems) {
				if ("org.eclipse.ui.internal.WorkbenchWindow.topBar".equals(trimItem //$NON-NLS-1$
						.getString(IMemento.TAG_ID))) {
					visible = true;
					break;
				}
			}
		}
		return visible;
	}

	boolean isStatusBarVisible() {
		return getStatusBar() != null;
	}

	private IMemento getStatusBar() {
		IMemento trimLayoutMem = getChild(IWorkbenchConstants.TAG_TRIM);
		if (trimLayoutMem == null) {
			return null;
		}

		IMemento[] trimAreas = trimLayoutMem.getChildren(IWorkbenchConstants.TAG_TRIM_AREA);
		for (IMemento trimArea : trimAreas) {
			if (ITrimManager.BOTTOM == trimArea.getInteger(IMemento.TAG_ID)) {
				return trimArea;
			}
		}
		return null;
	}

	boolean hasStatusLine() {
		IMemento statusBar = getStatusBar();
		if (statusBar != null) {
			IMemento[] trimItemMems = statusBar.getChildren(IWorkbenchConstants.TAG_TRIM_ITEM);
			for (IMemento trimItemMem : trimItemMems) {
				if (TrimArea.STATUS_LINE_MANAGER_ID.equals(trimItemMem.getID())) {
					return true;
				}
			}
		}
		return false;
	}

	boolean isWelcomePageOpen() {
		return (getChild(IWorkbenchConstants.TAG_INTRO) != null);
	}

	List<PerspectiveReader> getPerspectiveReaders() {
		List<PerspectiveReader> perspectives = new ArrayList<>();
		IMemento perspContainer = getPerspectiveContainer();
		if (perspContainer != null) {
			IMemento[] perspectiveMems = perspContainer
					.getChildren(IWorkbenchConstants.TAG_PERSPECTIVE);
			for (IMemento perspectiveMem : perspectiveMems) {
				PerspectiveReader perspective = new PerspectiveReader(perspectiveMem);
				perspectives.add(perspective);
			}
		}
		return perspectives;
	}

	private IMemento getPerspectiveContainer() {
		IMemento perspContainer = null;
		IMemento page = getPage();
		if (page != null) {
			perspContainer = page.getChild(IWorkbenchConstants.TAG_PERSPECTIVES);
		}
		return perspContainer;
	}

	boolean isSelected() {
		IMemento page = getPage();
		if (page == null) {
			return false;
		}
		Boolean selected = page.getBoolean(IWorkbenchConstants.TAG_FOCUS);
		return selected == null ? false : selected.booleanValue();
	}

	String getActivePerspectiveId() {
		String activePerspectiveId = null;
		IMemento perspContainer = getPerspectiveContainer();
		if (perspContainer != null) {
			activePerspectiveId = perspContainer.getString(IWorkbenchConstants.TAG_ACTIVE_PERSPECTIVE);
		}
		return activePerspectiveId;
	}

	List<InfoReader> getEditorStacks() {
		IMemento editorArea = getEditorArea();
		List<InfoReader> readers = new ArrayList<>();
		if (editorArea != null) {
			IMemento[] editorStackMems = editorArea.getChildren(IWorkbenchConstants.TAG_INFO);
			for (IMemento memento : editorStackMems) {
				readers.add(new InfoReader(memento));
			}
		}
		return readers;
	}

	private IMemento getEditorArea() {
		IMemento editors = getEditorsMemento();
		if (editors != null) {
			return editors.getChild(IWorkbenchConstants.TAG_AREA);
		}
		return null;
	}

	List<EditorReader> getEditors() {
		List<EditorReader> readers = new ArrayList<>();
		IMemento editors = getEditorsMemento();
		if (editors != null) {
			IMemento[] editorMems = editors.getChildren(IWorkbenchConstants.TAG_EDITOR);
			for (IMemento memento : editorMems) {
				readers.add(new EditorReader(memento));
			}
		}
		return readers;
	}

	private IMemento getEditorsMemento() {
		IMemento page = getPage();
		if (page == null) {
			return null;
		}
		return page.getChild(IWorkbenchConstants.TAG_EDITORS);
	}

	List<ViewReader> getViews() {
		List<ViewReader> readers = new ArrayList<>();
		IMemento page = getPage();
		if (page == null) {
			return readers;
		}
		IMemento editors = page.getChild(IWorkbenchConstants.TAG_VIEWS);
		if (editors != null) {
			IMemento[] editorMems = editors.getChildren(IWorkbenchConstants.TAG_VIEW);
			for (IMemento memento : editorMems) {
				readers.add(new ViewReader(memento));
			}
		}
		return readers;
	}

	private IMemento getPage() {
		return getChild(IWorkbenchConstants.TAG_PAGE);
	}

	static class EditorReader extends MementoReader {

		private EditorReader(IMemento memento) {
			super(memento);
		}

		String getLabel() {
			return getString(IWorkbenchConstants.TAG_TITLE);
		}

		String getType() {
			return getString(IWorkbenchConstants.TAG_ID);
		}

		String getStackId() {
			return getString(IWorkbenchConstants.TAG_WORKBOOK);
		}

		boolean isSelected() {
			return getBoolean(IWorkbenchConstants.TAG_FOCUS);
		}

	}

	static class ViewReader extends MementoReader {

		private ViewReader(IMemento memento) {
			super(memento);
		}

		String getId() {
			return getString(IWorkbenchConstants.TAG_ID);
		}

		String getLabel() {
			return getString(IWorkbenchConstants.TAG_PART_NAME);
		}

		IMemento getViewState() {
			IMemento viewStateMem = getChild(IWorkbenchConstants.TAG_VIEW_STATE);
			if (viewStateMem == null) {
				return null;
			}
			return createRenamedCopy(viewStateMem, IWorkbenchConstants.TAG_VIEW);
		}

		private XMLMemento createRenamedCopy(IMemento memento, String newName) {
			XMLMemento newMem = XMLMemento.createWriteRoot(newName);
			newMem.putMemento(memento);
			return newMem;
		}

	}

}
