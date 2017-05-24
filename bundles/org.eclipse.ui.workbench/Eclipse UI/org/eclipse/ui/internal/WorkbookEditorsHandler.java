/*******************************************************************************
 * Copyright (c) 2007, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Marc-Andre Laperle (Ericsson) - Bug 413278
 *     Patrik Suzzi <psuzzi@gmail.com> - Bug 497618, 368977, 504088, 506019
 ******************************************************************************/

package org.eclipse.ui.internal;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.renderers.swt.StackRenderer;
import org.eclipse.e4.ui.workbench.swt.internal.copy.SearchPattern;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Font;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.themes.ITheme;

/**
 * Shows a list of open editor and parts in the current or last active workbook.
 *
 * @since 3.4
 *
 */
public class WorkbookEditorsHandler extends FilteredTableBaseHandler {

	/**
	 * Preference node for the workbench SWT renderer
	 */
	private static final String ORG_ECLIPSE_E4_UI_WORKBENCH_RENDERERS_SWT = "org.eclipse.e4.ui.workbench.renderers.swt"; //$NON-NLS-1$

	/**
	 * Id for the command that opens the editor drop down
	 */
	private static final String ORG_ECLIPSE_UI_WINDOW_OPEN_EDITOR_DROP_DOWN = "org.eclipse.ui.window.openEditorDropDown"; //$NON-NLS-1$

	/**
	 * E4 Tag used to identify the active part
	 */
	private static final String TAG_ACTIVE = "active"; //$NON-NLS-1$

	private SearchPattern searchPattern;

	/**
	 * Gets the preference "show most recently used tabs" (MRU tabs)
	 *
	 * @return Returns the enableMRU.
	 */
	private static boolean isMruEnabled() {
		IEclipsePreferences preferences = InstanceScope.INSTANCE.getNode(ORG_ECLIPSE_E4_UI_WORKBENCH_RENDERERS_SWT);
		boolean initialMRUValue = preferences.getBoolean(StackRenderer.MRU_KEY_DEFAULT, StackRenderer.MRU_DEFAULT);
		boolean enableMRU = preferences.getBoolean(StackRenderer.MRU_KEY, initialMRUValue);
		return enableMRU;
	}

	@Override
	protected Object getInput(WorkbenchPage page) {
		return getParts(page);
	}

	private List<EditorReference> getParts(WorkbenchPage page) {
		List<EditorReference> refs;
		if (isMruEnabled()) {
			// sorted, MRU order
			refs = page.getSortedEditorReferences();
		} else {
			// non sorted, First Opened order
			refs = new ArrayList<>();
			for (IEditorReference ier : page.getEditorReferences()) {
				refs.add((EditorReference) ier);
			}
		}
		return refs;
	}

	@Override
	protected boolean isFiltered() {
		return true;
	}

	SearchPattern getMatcher() {
		return searchPattern;
	}

	@Override
	protected void setMatcherString(String pattern) {
		if (pattern.length() == 0) {
			searchPattern = null;
		} else {
			SearchPattern patternMatcher = new SearchPattern();
			patternMatcher.setPattern(pattern);
			searchPattern = patternMatcher;
		}
	}

	/**
	 * Specializes
	 * {@link FilteredTableBaseHandler#setLabelProvider(TableViewerColumn)} by
	 * providing custom styles to the table cells
	 */
	@Override
	protected void setLabelProvider(final TableViewerColumn tableViewerColumn) {

		tableViewerColumn.setLabelProvider(new StyledCellLabelProvider() {

			@Override
			public void update(ViewerCell cell) {
				Object element = cell.getElement();
				if (element instanceof WorkbenchPartReference) {
					WorkbenchPartReference ref = (WorkbenchPartReference) element;
					String text = getWorkbenchPartReferenceText(ref);
					cell.setText(text);
					cell.setImage(ref.getTitleImage());
					// get the model to define the style
					MPart model = ref.getModel();
					// build the style range
					StyleRange style = new StyleRange();
					style.start = 0;
					style.length = cell.getText().length();
					// if hidden use the bold font, if active italic
					style.font = getFont(isHiddenEditor(model), isActiveEditor(model));
					cell.setStyleRanges(new StyleRange[] { style });
				}
			}

			@Override
			public String getToolTipText(Object element) {
				if (element instanceof WorkbenchPartReference) {
					WorkbenchPartReference ref = (WorkbenchPartReference) element;
					return ref.getTitleToolTip();
				}
				return super.getToolTipText(element);
			}

		});

		ColumnViewerToolTipSupport.enableFor(tableViewerColumn.getViewer());
	}

	/** True if the given model represents the active editor */
	protected boolean isActiveEditor(MPart model) {
		if (model == null || model.getTags() == null) {
			return false;
		}
		return model.getTags().contains(TAG_ACTIVE);
	}

	/** True is the given model represents an hidden editor */
	protected boolean isHiddenEditor(MPart model) {
		if (model == null || model.getParent() == null || !(model.getParent().getRenderer() instanceof StackRenderer)) {
			return false;
		}
		StackRenderer renderer = (StackRenderer) model.getParent().getRenderer();
		CTabItem item = renderer.findItemForPart(model);
		return (item != null && !item.isShowing());
	}

	private Font getFont(boolean hidden, boolean active) {
		ITheme theme = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme();
		if (active) {
			return theme.getFontRegistry().getItalic(IWorkbenchThemeConstants.TAB_TEXT_FONT);
		}
		if (hidden) {
			return theme.getFontRegistry().getBold(IWorkbenchThemeConstants.TAB_TEXT_FONT);
		}
		return theme.getFontRegistry().get(IWorkbenchThemeConstants.TAB_TEXT_FONT);
	}

	@Override
	protected ViewerFilter getFilter() {
		return new ViewerFilter() {
			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				SearchPattern matcher = getMatcher();
				if (matcher == null || !(viewer instanceof TableViewer)) {
					return true;
				}
				String matchName = null;
				if (element instanceof EditorReference) {
					matchName = ((EditorReference) element).getTitle();
					// skips dirty editor prefix
					if (matchName.startsWith("*")) { //$NON-NLS-1$
						matchName = matchName.substring(1);
					}
				}
				if (matchName == null) {
					return false;
				}
				return matcher.matches(matchName);
			}
		};
	}

	@Override
	protected ParameterizedCommand getBackwardCommand() {
		return null;
	}

	@Override
	protected ParameterizedCommand getForwardCommand() {
		final ICommandService commandService = window.getWorkbench().getService(ICommandService.class);
		final Command command = commandService.getCommand(ORG_ECLIPSE_UI_WINDOW_OPEN_EDITOR_DROP_DOWN);
		ParameterizedCommand commandF = new ParameterizedCommand(command, null);
		return commandF;
	}

	@Override
	protected int getCurrentItemIndex() {
		if (isMruEnabled()) {
			return 0;
		}
		// We need to find previously selected part and return the index of the
		// part before this part in our list, which ordered not by use but by
		// position

		WorkbenchPage page = (WorkbenchPage) window.getActivePage();
		List<EditorReference> sortedByUse = page.getSortedEditorReferences();
		if (sortedByUse.size() < 2) {
			return 0;
		}

		// this is the previously used editor
		EditorReference next = sortedByUse.get(1);

		// now let's find it position in our list
		List<EditorReference> sortedByPosition = getParts(page);
		for (int i = 0; i < sortedByPosition.size(); i++) {
			EditorReference ref = sortedByPosition.get(i);
			if (ref == next) {
				if (i > 0) {
					// return the position of the previous part in our list
					gotoDirection = true;
					return i - 1;
				}
				// if the previous part is the *first* one in our list,
				// we should invert the traversal direction to "back".
				gotoDirection = false;
				return 1;
			}
		}
		return super.getCurrentItemIndex();
	}

}
