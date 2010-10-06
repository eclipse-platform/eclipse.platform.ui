/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.internal.workbench;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MGenericStack;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MArea;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;

class PartActivationHistory {

	private EModelService modelService;

	private LinkedList<MPart> generalActivationHistory = new LinkedList<MPart>();
	private Map<MUIElement, LinkedList<MPart>> perspectiveActivationHistories = new WeakHashMap<MUIElement, LinkedList<MPart>>();

	PartActivationHistory(EModelService modelService) {
		this.modelService = modelService;
	}

	public void clear() {
		generalActivationHistory.clear();
		perspectiveActivationHistories.clear();
	}

	public void activate(MPart part) {
		IEclipseContext partContext = part.getContext();
		partContext.activateBranch();

		LinkedList<MPart> perspectiveActivationHistory = getPerspectiveActivationHistory(part);
		if (perspectiveActivationHistory != null) {
			perspectiveActivationHistory.remove(part);
			perspectiveActivationHistory.addFirst(part);
		}

		generalActivationHistory.remove(part);
		generalActivationHistory.addFirst(part);
	}

	/**
	 * Checks to see if this element and its parents are actually being rendered.
	 */
	private boolean isValid(MUIElement element) {
		if (element instanceof MApplication) {
			return true;
		}

		MElementContainer<MUIElement> parent = element.getParent();
		if (parent == null) {
			MPlaceholder placeholder = element.getCurSharedRef();
			if (placeholder == null) {
				return false;
			}

			parent = placeholder.getParent();
			if (parent == null) {
				return false;
			}
		}

		return element.isToBeRendered() && element.isVisible() && isValid(parent);
	}

	/**
	 * Determines whether this element is contained within an MArea, and to return the area if it
	 * is.
	 */
	private MArea isInArea(MUIElement element) {
		MPlaceholder placeholder = element.getCurSharedRef();
		MUIElement parent = placeholder == null ? element.getParent() : placeholder.getParent();
		return parent instanceof MApplication ? null : parent instanceof MArea ? (MArea) parent
				: isInArea(parent);
	}

	/**
	 * Finds and returns a part that is a valid candidate to be granted activation.
	 */
	private MPart getActivationCandidate(MPart part) {
		Collection<MPart> candidates = part.getContext().get(EPartService.class).getParts();
		for (MPart candidate : candidates) {
			if (part != candidate && candidate.isToBeRendered() && candidate.isVisible()) {
				MPlaceholder placeholder = candidate.getCurSharedRef();
				if (placeholder != null) {
					if (!placeholder.isToBeRendered() || !placeholder.isVisible()) {
						continue;
					}
				}

				MElementContainer<MUIElement> parent = placeholder == null ? candidate.getParent()
						: placeholder.getParent();
				if (isValid(parent)) {
					if (parent instanceof MGenericStack<?>) {
						MUIElement element = parent.getSelectedElement();
						if (element == part) {
							return candidate;
						}
						placeholder = element.getCurSharedRef();
						return placeholder == null ? candidate : (MPart) placeholder.getRef();
					}
					return candidate;
				}
			}
		}
		return null;
	}

	private MPart getActivationCandidate(LinkedList<MPart> activationHistory, MPart part) {
		MArea area = isInArea(part);
		if (area != null) {
			// focus should stay in the area if possible
			MPart candidate = getSiblingActivationCandidate(part);
			if (candidate != null) {
				return candidate;
			}

			// no sibling candidate, find another part in the area to activate
			for (MPart element : modelService.findElements(area, null, MPart.class, null)) {
				if (element != part) {
					if (element.isToBeRendered() && element.isVisible()) {
						MPlaceholder placeholder = element.getCurSharedRef();
						if (placeholder == null
								|| (placeholder.isToBeRendered() && placeholder.isVisible())) {
							return element;
						}
					}
				}
			}
		}

		for (Iterator<MPart> it = activationHistory.iterator(); it.hasNext();) {
			if (it.next() == part) {
				if (part == activationHistory.getFirst() && it.hasNext()) {
					MPart candidate = it.next();
					if (candidate.isToBeRendered() && candidate.isVisible()) {
						MPlaceholder placeholder = candidate.getCurSharedRef();
						if (placeholder == null
								|| (placeholder.isToBeRendered() && placeholder.isVisible())) {
							return candidate;
						}
					}
				}
				return getActivationCandidate(part);
			}
		}
		return getActivationCandidate(part);
	}

	public MPart deactivate(MPart part) {
		part.getContext().deactivate();
		LinkedList<MPart> activationHistory = getPerspectiveActivationHistory(part);
		return getActivationCandidate(activationHistory == null ? generalActivationHistory
				: activationHistory, part);
	}

	public void forget(MPart part, boolean full) {
		generalActivationHistory.remove(part);
		if (full) {
			for (LinkedList<MPart> history : perspectiveActivationHistories.values()) {
				history.remove(part);
			}
		} else {
			LinkedList<MPart> history = getPerspectiveActivationHistory(part);
			if (history != null) {
				history.remove(part);
			}
		}
	}

	private MPart getSiblingActivationCandidate(MPart part, MUIElement element,
			Collection<MPart> activationHistory) {
		List<MUIElement> siblings = element.getParent().getChildren();
		for (MPart previouslyActivatedPart : activationHistory) {
			if (previouslyActivatedPart != part && previouslyActivatedPart.isToBeRendered()) {
				if (siblings.contains(previouslyActivatedPart)) {
					return previouslyActivatedPart;
				}

				MPlaceholder placeholder = previouslyActivatedPart.getCurSharedRef();
				if (placeholder != null && placeholder.isToBeRendered()
						&& siblings.contains(placeholder)) {
					return previouslyActivatedPart;
				}
			}
		}
		return null;
	}

	private LinkedList<MPart> getPerspectiveActivationHistory(MPart part) {
		MPlaceholder placeholder = part.getCurSharedRef();
		MUIElement parent = placeholder == null ? part.getParent() : placeholder.getParent();
		while (parent != null) {
			if (parent instanceof MPerspective) {
				LinkedList<MPart> history = perspectiveActivationHistories.get(parent);
				if (history == null) {
					history = new LinkedList<MPart>();
					perspectiveActivationHistories.put(parent, history);
				}
				return history;
			} else if (parent instanceof MWindow) {
				MUIElement windowParent = parent.getParent();
				if (windowParent instanceof MApplication) {
					return null;
				}
				parent = windowParent;
			} else {
				MUIElement parentCandidate = parent.getParent();
				if (parentCandidate == null) {
					placeholder = parent.getCurSharedRef();
					if (placeholder == null) {
						return null;
					}
					parentCandidate = placeholder.getParent();
					if (parentCandidate == null) {
						return null;
					}
				}
				parent = parentCandidate;
			}
		}
		return null;
	}

	Object getSiblingActivationCandidate2() {
		Object history = perspectiveActivationHistories.get(null);
		if (history == null) {
			return null;
		}
		return history == null ? null : null;
	}

	private MPart getSiblingActivationCandidate(MPart part, MUIElement element) {
		MUIElement parent = element.getParent();
		while (parent != null) {
			if (parent instanceof MPerspective) {
				LinkedList<MPart> history = perspectiveActivationHistories.get(parent);
				return history == null ? null : getSiblingActivationCandidate(part, element,
						history);
			} else if (parent instanceof MWindow) {
				MUIElement windowParent = parent.getParent();
				if (windowParent instanceof MApplication) {
					return getSiblingActivationCandidate(part, element, generalActivationHistory);
				}
				parent = windowParent;
			} else {
				MUIElement parentCandidate = parent.getParent();
				if (parentCandidate == null) {
					MPlaceholder placeholder = parent.getCurSharedRef();
					if (placeholder == null) {
						return null;
					}
					parentCandidate = placeholder.getParent();
					if (parentCandidate == null) {
						return null;
					}
				}
				parent = parentCandidate;
			}
		}
		return null;
	}

	public MPart getSiblingActivationCandidate(MPart part) {
		MPlaceholder placeholder = part.getCurSharedRef();
		return getSiblingActivationCandidate(part, placeholder == null ? part : placeholder);
	}
}
