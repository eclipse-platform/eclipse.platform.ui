/*******************************************************************************
 * Copyright (c) 2010, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *      Maxime Porhel <maxime.porhel@obeo.fr> Obeo - Bug 435949
 *      Lars Vogel <Lars.Vogel@vogella.com> - Bug 472654
 ******************************************************************************/

package org.eclipse.e4.ui.internal.workbench;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionInfo;
import org.eclipse.core.internal.expressions.ReferenceExpression;
import org.eclipse.e4.core.commands.ExpressionContext;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.ui.MCoreExpression;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MExpression;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimBar;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimElement;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuContribution;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuSeparator;
import org.eclipse.e4.ui.model.application.ui.menu.MPopupMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBarContribution;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBarElement;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBarSeparator;
import org.eclipse.e4.ui.model.application.ui.menu.MTrimContribution;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;

public final class ContributionsAnalyzer {
	public static void trace(String msg, Throwable error) {
		Activator.trace("/trace/menus", msg, error); //$NON-NLS-1$
	}

	private static boolean DEBUG = true;

	private static void trace(String msg, Object menu, Object menuModel) {
		trace(msg + ": " + menu + ": " + menuModel, null); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static void gatherTrimContributions(MTrimBar trimModel,
			List<MTrimContribution> trimContributions, String elementId,
			ArrayList<MTrimContribution> toContribute, ExpressionContext eContext) {
		if (elementId == null || elementId.length() == 0) {
			return;
		}
		for (MTrimContribution contribution : trimContributions) {
			String parentId = contribution.getParentId();
			boolean filtered = isFiltered(trimModel, contribution);
			if (filtered || !elementId.equals(parentId) || !contribution.isToBeRendered()) {
				continue;
			}
			toContribute.add(contribution);
		}
	}

	static boolean isFiltered(MTrimBar trimModel, MTrimContribution contribution) {
		return false;
	}

	public static void XXXgatherToolBarContributions(final MToolBar toolbarModel,
			final List<MToolBarContribution> toolbarContributionList, final String id,
			final ArrayList<MToolBarContribution> toContribute) {
		if (id == null || id.length() == 0) {
			return;
		}
		for (MToolBarContribution toolBarContribution : toolbarContributionList) {
			String parentID = toolBarContribution.getParentId();
			boolean filtered = isFiltered(toolbarModel, toolBarContribution);
			if (filtered || !id.equals(parentID) || !toolBarContribution.isToBeRendered()) {
				continue;
			}
			toContribute.add(toolBarContribution);
		}
	}

	public static void gatherToolBarContributions(final MToolBar toolbarModel,
			final List<MToolBarContribution> toolbarContributionList, final String id,
			final ArrayList<MToolBarContribution> toContribute, final ExpressionContext eContext) {
		if (id == null || id.length() == 0) {
			return;
		}
		for (MToolBarContribution toolBarContribution : toolbarContributionList) {
			String parentID = toolBarContribution.getParentId();
			boolean filtered = isFiltered(toolbarModel, toolBarContribution);
			if (filtered || !id.equals(parentID) || !toolBarContribution.isToBeRendered()) {
				continue;
			}
			toContribute.add(toolBarContribution);
		}
	}

	static boolean isFiltered(MToolBar toolbarModel, MToolBarContribution toolBarContribution) {
		return false;
	}

	public static void XXXgatherMenuContributions(final MMenu menuModel,
			final List<MMenuContribution> menuContributionList, final String id,
			final ArrayList<MMenuContribution> toContribute, final ExpressionContext eContext,
			boolean includePopups) {
		if (id == null || id.length() == 0) {
			return;
		}
		ArrayList<String> popupIds = new ArrayList<>();
		if (includePopups) {
			popupIds.add(id);
			for (String tag : menuModel.getTags()) {
				if (tag.startsWith("popup:")) { //$NON-NLS-1$
					String tmp = tag.substring("popup:".length()); //$NON-NLS-1$
					if (!popupIds.contains(tmp)) {
						popupIds.add(tmp);
					}
				}
			}
		}
		ArrayList<MMenuContribution> includedPopups = new ArrayList<>();
		for (MMenuContribution menuContribution : menuContributionList) {
			String parentID = menuContribution.getParentId();
			if (parentID == null) {
				// it doesn't make sense for this to be null, temporary workaround for bug 320790
				continue;
			}
			boolean popupTarget = includePopups && popupIds.contains(parentID);
			boolean popupAny = includePopups && menuModel instanceof MPopupMenu
					&& POPUP_PARENT_ID.equals(parentID);
			boolean filtered = isFiltered(menuModel, menuContribution, includePopups);
			if (!filtered && menuContribution.isToBeRendered() && popupAny) {
				// process POPUP_ANY first
				toContribute.add(menuContribution);
			} else {
				if (filtered || (!popupTarget && !parentID.equals(id))
				|| !menuContribution.isToBeRendered()) {
					continue;
				}
				includedPopups.add(menuContribution);
			}
		}
		toContribute.addAll(includedPopups);
	}

	public static void gatherMenuContributions(final MMenu menuModel,
			final List<MMenuContribution> menuContributionList, final String id,
			final ArrayList<MMenuContribution> toContribute, final ExpressionContext eContext,
			boolean includePopups) {
		if (id == null || id.length() == 0) {
			return;
		}
		boolean menuBar = (((MUIElement) ((EObject) menuModel).eContainer()) instanceof MWindow);
		for (MMenuContribution menuContribution : menuContributionList) {
			String parentID = menuContribution.getParentId();
			if (parentID == null) {
				// it doesn't make sense for this to be null, temporary workaround for bug 320790
				continue;
			}
			boolean popup = parentID.equals(POPUP_PARENT_ID) && (menuModel instanceof MPopupMenu)
					&& includePopups;
			boolean filtered = isFiltered(menuModel, menuContribution, includePopups);
			if (filtered || (!popup && !parentID.equals(id)) || !menuContribution.isToBeRendered()) {
				continue;
			}
			if (menuBar || isVisible(menuContribution, eContext)) {
				toContribute.add(menuContribution);
			}
		}
	}

	static boolean isFiltered(MMenu menuModel, MMenuContribution menuContribution,
			boolean includePopups) {
		if (includePopups || menuModel.getTags().contains(ContributionsAnalyzer.MC_POPUP)) {
			return !menuContribution.getTags().contains(ContributionsAnalyzer.MC_POPUP)
					&& menuContribution.getTags().contains(ContributionsAnalyzer.MC_MENU);
		}
		if (menuModel.getTags().contains(ContributionsAnalyzer.MC_MENU)) {
			return !menuContribution.getTags().contains(ContributionsAnalyzer.MC_MENU)
					&& menuContribution.getTags().contains(ContributionsAnalyzer.MC_POPUP);
		}
		if (!includePopups) {
			// not including popups, so filter out popup menu contributions if the menu is a regular
			// menu
			return menuContribution.getTags().contains(ContributionsAnalyzer.MC_POPUP);
		}
		return false;
	}

	public static void collectInfo(ExpressionInfo info, MExpression exp) {
		if (!(exp instanceof MCoreExpression)) {
			return;
		}
		MCoreExpression expr = (MCoreExpression) exp;
		Expression ref = null;
		if (expr.getCoreExpression() instanceof Expression) {
			ref = (Expression) expr.getCoreExpression();
		} else {
			ref = new ReferenceExpression(expr.getCoreExpressionId());
			expr.setCoreExpression(ref);
		}
		ref.collectExpressionInfo(info);
	}

	public static boolean isVisible(MMenuContribution menuContribution, ExpressionContext eContext) {
		if (menuContribution.getVisibleWhen() == null) {
			return true;
		}
		return isVisible((MCoreExpression) menuContribution.getVisibleWhen(), eContext);
	}

	public static boolean isVisible(MToolBarContribution contribution, ExpressionContext eContext) {
		if (contribution.getVisibleWhen() == null) {
			return true;
		}
		return isVisible((MCoreExpression) contribution.getVisibleWhen(), eContext);
	}

	public static boolean isVisible(MTrimContribution contribution, ExpressionContext eContext) {
		if (contribution.getVisibleWhen() == null) {
			return true;
		}
		return isVisible((MCoreExpression) contribution.getVisibleWhen(), eContext);
	}

	public static boolean isVisible(MCoreExpression exp, final ExpressionContext eContext) {
		final Expression ref;
		if (exp.getCoreExpression() instanceof Expression) {
			ref = (Expression) exp.getCoreExpression();
		} else {
			ref = new ReferenceExpression(exp.getCoreExpressionId());
			exp.setCoreExpression(ref);
		}
		// Creates dependency on a predefined value that can be "poked" by the evaluation
		// service
		ExpressionInfo info = ref.computeExpressionInfo();
		String[] names = info.getAccessedPropertyNames();
		for (String name : names) {
			eContext.getVariable(name + ".evaluationServiceLink"); //$NON-NLS-1$
		}
		boolean ret = false;
		try {
			ret = ref.evaluate(eContext) != EvaluationResult.FALSE;
		} catch (Exception e) {
			trace("isVisible exception", e); //$NON-NLS-1$
		}
		return ret;
	}

	public static void addMenuContributions(final MMenu menuModel,
			final ArrayList<MMenuContribution> toContribute,
			final ArrayList<MMenuElement> menuContributionsToRemove) {

		HashSet<String> existingMenuIds = new HashSet<>();
		HashSet<String> existingSeparatorNames = new HashSet<>();
		for (MMenuElement child : menuModel.getChildren()) {
			String elementId = child.getElementId();
			if (child instanceof MMenu && elementId != null) {
				existingMenuIds.add(elementId);
			} else if (child instanceof MMenuSeparator && elementId != null) {
				existingSeparatorNames.add(elementId);
			}
		}

		boolean done = toContribute.size() == 0;
		while (!done) {
			ArrayList<MMenuContribution> curList = new ArrayList<>(toContribute);
			int retryCount = toContribute.size();
			toContribute.clear();

			for (MMenuContribution menuContribution : curList) {
				if (!processAddition(menuModel, menuContributionsToRemove, menuContribution,
						existingMenuIds, existingSeparatorNames)) {
					toContribute.add(menuContribution);
				}
			}
			// We're done if the retryList is now empty (everything done) or
			// if the list hasn't changed at all (no hope)
			done = (toContribute.size() == 0) || (toContribute.size() == retryCount);
		}
	}

	public static boolean processAddition(final MMenu menuModel,
			final ArrayList<MMenuElement> menuContributionsToRemove,
			MMenuContribution menuContribution, final HashSet<String> existingMenuIds,
			HashSet<String> existingSeparatorNames) {
		int idx = getIndex(menuModel, menuContribution.getPositionInParent());
		if (idx == -1) {
			return false;
		}
		for (MMenuElement item : menuContribution.getChildren()) {
			if (item instanceof MMenu && existingMenuIds.contains(item.getElementId())) {
				// skip this, it's already there
				continue;
			} else if (item instanceof MMenuSeparator
					&& existingSeparatorNames.contains(item.getElementId())) {
				// skip this, it's already there
				continue;
			}
			MMenuElement copy = (MMenuElement) EcoreUtil.copy((EObject) item);
			if (DEBUG) {
				trace("addMenuContribution " + copy, menuModel.getWidget(), menuModel); //$NON-NLS-1$
			}
			menuContributionsToRemove.add(copy);
			menuModel.getChildren().add(idx++, copy);
			if (copy instanceof MMenu && copy.getElementId() != null) {
				existingMenuIds.add(copy.getElementId());
			} else if (copy instanceof MMenuSeparator && copy.getElementId() != null) {
				existingSeparatorNames.add(copy.getElementId());
			}
		}
		return true;
	}

	public static boolean processAddition(final MToolBar toolBarModel,
			MToolBarContribution toolBarContribution, List<MToolBarElement> contributions,
			HashSet<String> existingSeparatorNames) {
		int idx = getIndex(toolBarModel, toolBarContribution.getPositionInParent());
		if (idx == -1) {
			return false;
		}
		for (MToolBarElement item : toolBarContribution.getChildren()) {
			if (item instanceof MToolBarSeparator
					&& existingSeparatorNames.contains(item.getElementId())) {
				// skip this, it's already there
				continue;
			}
			MToolBarElement copy = (MToolBarElement) EcoreUtil.copy((EObject) item);
			if (DEBUG) {
				trace("addToolBarContribution " + copy, toolBarModel.getWidget(), toolBarModel); //$NON-NLS-1$
			}
			toolBarModel.getChildren().add(idx++, copy);
			contributions.add(copy);
			if (copy instanceof MToolBarSeparator && copy.getElementId() != null) {
				existingSeparatorNames.add(copy.getElementId());
			}
		}
		return true;
	}

	public static boolean processAddition(final MTrimBar trimBar, MTrimContribution contribution,
			List<MTrimElement> contributions, HashSet<String> existingToolbarIds) {
		int idx = getIndex(trimBar, contribution.getPositionInParent());
		if (idx == -1) {
			return false;
		}
		for (MTrimElement item : contribution.getChildren()) {
			if (item instanceof MToolBar && existingToolbarIds.contains(item.getElementId())) {
				// skip this, it's already there
				continue;
			}
			MTrimElement copy = (MTrimElement) EcoreUtil.copy((EObject) item);
			if (DEBUG) {
				trace("addTrimContribution " + copy, trimBar.getWidget(), trimBar); //$NON-NLS-1$
			}
			trimBar.getChildren().add(idx++, copy);
			contributions.add(copy);
			if (copy instanceof MToolBar && copy.getElementId() != null) {
				existingToolbarIds.add(copy.getElementId());
			}
		}
		return true;
	}

	private static int getIndex(MElementContainer<?> menuModel, String positionInParent) {
		String id = null;
		String modifier = null;
		if (positionInParent != null && positionInParent.length() > 0) {
			String[] array = positionInParent.split("="); //$NON-NLS-1$
			modifier = array[0];
			// may have an invalid position, check for this
			if (array.length > 1) {
				id = array[1];
			}
		}
		if (id == null) {
			return menuModel.getChildren().size();
		}

		int idx = 0;
		int size = menuModel.getChildren().size();
		while (idx < size) {
			if (id.equals(menuModel.getChildren().get(idx).getElementId())) {
				if ("after".equals(modifier)) { //$NON-NLS-1$
					idx++;
				}
				return idx;
			}
			idx++;
		}
		return id.equals("additions") ? menuModel.getChildren().size() : -1; //$NON-NLS-1$
	}

	public static MCommand getCommandById(MApplication app, String cmdId) {
		final List<MCommand> cmds = app.getCommands();
		for (MCommand cmd : cmds) {
			if (cmdId.equals(cmd.getElementId())) {
				return cmd;
			}
		}
		return null;
	}

	static class Key {
		private int tag = -1;
		private int hc = -1;
		private String parentId;
		private String position;
		private MCoreExpression vexp;
		private Object factory;

		public Key(String parentId, String position, List<String> tags, MCoreExpression vexp,
				Object factory) {
			this.parentId = parentId;
			this.position = position;
			this.vexp = vexp;
			this.factory = factory;
			if (tags.contains("scheme:menu")) { //$NON-NLS-1$
				tag = 1;
			} else if (tags.contains("scheme:popup")) { //$NON-NLS-1$
				tag = 2;
			} else if (tags.contains("scheme:toolbar")) { //$NON-NLS-1$
				tag = 3;
			} else {
				tag = 0;
			}
		}

		int getSchemeTag() {
			return tag;
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof Key)) {
				return false;
			}
			Key other = (Key) obj;
			Object exp1 = vexp == null ? null : vexp.getCoreExpression();
			Object exp2 = other.vexp == null ? null : other.vexp.getCoreExpression();
			return Util.equals(parentId, other.parentId) && Util.equals(position, other.position)
					&& getSchemeTag() == other.getSchemeTag() && Util.equals(exp1, exp2)
					&& Util.equals(factory, other.factory);
		}

		@Override
		public int hashCode() {
			if (hc == -1) {
				Object exp1 = vexp == null ? null : vexp.getCoreExpression();
				hc = Util.hashCode(parentId);
				hc = hc * 87 + Util.hashCode(position);
				hc = hc * 87 + getSchemeTag();
				hc = hc * 87 + Util.hashCode(exp1);
				hc = hc * 87 + Util.hashCode(factory);
			}
			return hc;
		}

		@Override
		public String toString() {
			return getClass().getName() + " " + parentId + "--" + position //$NON-NLS-1$ //$NON-NLS-2$
					+ "--" + getSchemeTag() + "--" + vexp; //$NON-NLS-1$//$NON-NLS-2$
		}
	}

	static class MenuKey extends Key {
		static final String FACTORY = "ContributionFactory"; //$NON-NLS-1$
		private MMenuContribution contribution;

		public MenuKey(MMenuContribution mc) {
			super(mc.getParentId(), mc.getPositionInParent(), mc.getTags(), (MCoreExpression) mc
					.getVisibleWhen(), mc.getTransientData().get(FACTORY));
			this.contribution = mc;
			mc.setWidget(this);
		}

		public MMenuContribution getContribution() {
			return contribution;
		}
	}

	static class ToolBarKey extends Key {
		static final String FACTORY = "ToolBarContributionFactory"; //$NON-NLS-1$
		private MToolBarContribution contribution;

		public ToolBarKey(MToolBarContribution mc) {
			super(mc.getParentId(), mc.getPositionInParent(), mc.getTags(), (MCoreExpression) mc
					.getVisibleWhen(), mc.getTransientData().get(FACTORY));
			this.contribution = mc;
			mc.setWidget(this);
		}

		public MToolBarContribution getContribution() {
			return contribution;
		}
	}

	static class TrimKey extends Key {
		private MTrimContribution contribution;

		public TrimKey(MTrimContribution mc) {
			super(mc.getParentId(), mc.getPositionInParent(), mc.getTags(), (MCoreExpression) mc
					.getVisibleWhen(), null);
			this.contribution = mc;
			mc.setWidget(this);
		}

		public MTrimContribution getContribution() {
			return contribution;
		}
	}

	private static MenuKey getKey(MMenuContribution contribution) {
		if (contribution.getWidget() instanceof MenuKey) {
			return (MenuKey) contribution.getWidget();
		}
		return new MenuKey(contribution);
	}

	private static ToolBarKey getKey(MToolBarContribution contribution) {
		if (contribution.getWidget() instanceof ToolBarKey) {
			return (ToolBarKey) contribution.getWidget();
		}
		return new ToolBarKey(contribution);
	}

	private static TrimKey getKey(MTrimContribution contribution) {
		if (contribution.getWidget() instanceof TrimKey) {
			return (TrimKey) contribution.getWidget();
		}
		return new TrimKey(contribution);
	}

	public static void printContributions(ArrayList<MMenuContribution> contributions) {
		for (MMenuContribution c : contributions) {
			trace("\n" + c, null); //$NON-NLS-1$
			for (MMenuElement element : c.getChildren()) {
				printElement(1, element);
			}
		}
	}

	private static void printElement(int level, MMenuElement element) {
		StringBuilder buf = new StringBuilder();
		for (int i = 0; i < level; i++) {
			buf.append('\t');
		}
		buf.append(element.toString());
		trace(buf.toString(), null);
		if (element instanceof MMenu) {
			for (MMenuElement item : ((MMenu) element).getChildren()) {
				printElement(level + 1, item);
			}
		}
	}

	public static void mergeToolBarContributions(ArrayList<MToolBarContribution> contributions,
			ArrayList<MToolBarContribution> result) {
		HashMap<ToolBarKey, ArrayList<MToolBarContribution>> buckets = new HashMap<>();
		trace("mergeContributions size: " + contributions.size(), null); //$NON-NLS-1$
		// first pass, sort by parentId?position,scheme,visibleWhen
		for (MToolBarContribution contribution : contributions) {
			ToolBarKey key = getKey(contribution);
			ArrayList<MToolBarContribution> slot = buckets.get(key);
			if (slot == null) {
				slot = new ArrayList<>();
				buckets.put(key, slot);
			}
			slot.add(contribution);
		}
		Iterator<MToolBarContribution> i = contributions.iterator();
		while (i.hasNext() && !buckets.isEmpty()) {
			MToolBarContribution contribution = i.next();
			ToolBarKey key = getKey(contribution);
			ArrayList<MToolBarContribution> slot = buckets.remove(key);
			if (slot == null) {
				continue;
			}
			MToolBarContribution toContribute = null;
			for (MToolBarContribution item : slot) {
				if (toContribute == null) {
					toContribute = item;
					continue;
				}
				Object[] array = item.getChildren().toArray();
				for (int c = 0; c < array.length; c++) {
					MToolBarElement me = (MToolBarElement) array[c];
					if (!containsMatching(toContribute.getChildren(), me)) {
						toContribute.getChildren().add(me);
					}
				}
			}
			if (toContribute != null) {
				toContribute.setWidget(null);
				result.add(toContribute);
			}
		}
		trace("mergeContributions: final size: " + result.size(), null); //$NON-NLS-1$
	}

	public static void mergeContributions(ArrayList<MMenuContribution> contributions,
			ArrayList<MMenuContribution> result) {
		HashMap<MenuKey, ArrayList<MMenuContribution>> buckets = new HashMap<>();
		trace("mergeContributions size: " + contributions.size(), null); //$NON-NLS-1$
		printContributions(contributions);
		// first pass, sort by parentId?position,scheme,visibleWhen
		for (MMenuContribution contribution : contributions) {
			MenuKey key = getKey(contribution);
			ArrayList<MMenuContribution> slot = buckets.get(key);
			if (slot == null) {
				slot = new ArrayList<>();
				buckets.put(key, slot);
			}
			slot.add(contribution);
		}
		Iterator<MMenuContribution> i = contributions.iterator();
		while (i.hasNext() && !buckets.isEmpty()) {
			MMenuContribution contribution = i.next();
			MenuKey key = getKey(contribution);
			ArrayList<MMenuContribution> slot = buckets.remove(key);
			if (slot == null) {
				continue;
			}
			MMenuContribution toContribute = null;
			for (MMenuContribution item : slot) {
				if (toContribute == null) {
					toContribute = item;
					continue;
				}
				Object[] array = item.getChildren().toArray();
				int idx = getIndex(toContribute, item.getPositionInParent());
				if (idx == -1) {
					idx = 0;
				}
				for (int c = 0; c < array.length; c++) {
					MMenuElement me = (MMenuElement) array[c];
					if (!containsMatching(toContribute.getChildren(), me)) {
						toContribute.getChildren().add(idx, me);
						idx++;
					}
				}
			}
			if (toContribute != null) {
				toContribute.setWidget(null);
				result.add(toContribute);
			}
		}
		trace("mergeContributions: final size: " + result.size(), null); //$NON-NLS-1$
	}

	private static boolean containsMatching(List<MMenuElement> children, MMenuElement me) {
		for (MMenuElement element : children) {
			if (Util.equals(me.getElementId(), element.getElementId())
					&& element.getClass().isInstance(me)
					&& (element instanceof MMenuSeparator || element instanceof MMenu)) {
				return true;
			}
		}
		return false;
	}

	private static boolean containsMatching(List<MToolBarElement> children, MToolBarElement me) {
		for (MToolBarElement element : children) {
			if (Util.equals(me.getElementId(), element.getElementId())
					&& element.getClass().isInstance(me)
					&& (element instanceof MToolBarSeparator || element instanceof MToolBar)) {
				return true;
			}
		}
		return false;
	}

	private static boolean containsMatching(List<MTrimElement> children, MTrimElement me) {
		for (MTrimElement element : children) {
			if (Util.equals(me.getElementId(), element.getElementId())
					&& element.getClass().isInstance(me)
					&& (element instanceof MToolBarSeparator || element instanceof MToolBar)) {
				return true;
			}
		}
		return false;
	}

	public static int indexForId(MElementContainer<MMenuElement> parentMenu, String id) {
		if (id == null || id.length() == 0) {
			return -1;
		}
		int i = 0;
		for (MMenuElement item : parentMenu.getChildren()) {
			if (id.equals(item.getElementId())) {
				return i;
			}
			i++;
		}
		return -1;
	}

	public static final String MC_POPUP = "menuContribution:popup"; //$NON-NLS-1$
	public static final String MC_MENU = "menuContribution:menu"; //$NON-NLS-1$
	public static final String MC_TOOLBAR = "menuContribution:toolbar"; //$NON-NLS-1$
	public static final String POPUP_PARENT_ID = "popup"; //$NON-NLS-1$

	public static void mergeTrimContributions(ArrayList<MTrimContribution> contributions,
			ArrayList<MTrimContribution> result) {
		HashMap<TrimKey, ArrayList<MTrimContribution>> buckets = new HashMap<>();
		trace("mergeContributions size: " + contributions.size(), null); //$NON-NLS-1$
		// first pass, sort by parentId?position,scheme,visibleWhen
		for (MTrimContribution contribution : contributions) {
			TrimKey key = getKey(contribution);
			ArrayList<MTrimContribution> slot = buckets.get(key);
			if (slot == null) {
				slot = new ArrayList<>();
				buckets.put(key, slot);
			}
			slot.add(contribution);
		}
		Iterator<MTrimContribution> i = contributions.iterator();
		while (i.hasNext() && !buckets.isEmpty()) {
			MTrimContribution contribution = i.next();
			TrimKey key = getKey(contribution);
			ArrayList<MTrimContribution> slot = buckets.remove(key);
			if (slot == null) {
				continue;
			}
			MTrimContribution toContribute = null;
			for (MTrimContribution item : slot) {
				if (toContribute == null) {
					toContribute = item;
					continue;
				}
				Object[] array = item.getChildren().toArray();
				for (int c = 0; c < array.length; c++) {
					MTrimElement me = (MTrimElement) array[c];
					if (!containsMatching(toContribute.getChildren(), me)) {
						toContribute.getChildren().add(me);
					}
				}
			}
			if (toContribute != null) {
				toContribute.setWidget(null);
				result.add(toContribute);
			}
		}
		trace("mergeContributions: final size: " + result.size(), null); //$NON-NLS-1$
	}

	public static void populateModelInterfaces(Object modelObject, IEclipseContext context,
			Class<?>[] interfaces) {
		for (Class<?> intf : interfaces) {
			Activator.trace(Policy.DEBUG_CONTEXTS, "Adding " + intf.getName() + " for " //$NON-NLS-1$ //$NON-NLS-2$
					+ modelObject.getClass().getName(), null);
			context.set(intf.getName(), modelObject);

			populateModelInterfaces(modelObject, context, intf.getInterfaces());
		}
	}
}
