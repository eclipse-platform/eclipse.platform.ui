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
import org.eclipse.emf.ecore.EObject;

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

	void activate(MPart part, boolean activateBranch) {
		IEclipseContext context = part.getContext();
		if (activateBranch) {
			context.activateBranch();
		} else {
			IEclipseContext parent = context.getParent();
			do {
				context.activate();
				context = parent;
				parent = parent.getParent();
			} while (parent.get(MWindow.class) != null);
		}

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
		if (element == null) {
			return false;
		}

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
			if (parent == null || !placeholder.isToBeRendered() || !placeholder.isVisible()) {
				return false;
			}
		}

		return element.isToBeRendered() && element.isVisible() && isValid(parent);
	}

	/**
	 * Checks to see if this element and its parents are actually being rendered.
	 */
	boolean isValid(MPerspective perspective, MUIElement element) {
		if (element instanceof MApplication) {
			return true;
		} else if (!element.isToBeRendered() || !element.isVisible()) {
			return false;
		}

		MElementContainer<MUIElement> parent = element.getParent();
		if (parent == null) {
			for (MPlaceholder placeholder : modelService.findElements(perspective, null,
					MPlaceholder.class, null)) {
				if (placeholder.getRef() == element) {
					parent = placeholder.getParent();
					if (!placeholder.isToBeRendered() || !placeholder.isVisible()) {
						return false;
					}
					break;
				}
			}

			if (parent == null) {
				return false;
			}
		}

		return isValid(perspective, parent);
	}

	/**
	 * Determines whether this element is contained within an MArea, and to return the area if it
	 * is.
	 */
	private MArea isInArea(MUIElement element) {
		MPlaceholder placeholder = element.getCurSharedRef();
		if (placeholder == null) {
			MUIElement parent = element.getParent();
			if (parent == null) {
				// may be null for detached windows
				parent = (MUIElement) ((EObject) element).eContainer();
			}
			return parent instanceof MApplication ? null : parent instanceof MArea ? (MArea) parent
					: isInArea(parent);
		}

		MUIElement parent = placeholder.getParent();
		if (parent == null) {
			// may be null for detached windows
			parent = (MUIElement) ((EObject) placeholder).eContainer();
		}
		return parent instanceof MApplication ? null : parent instanceof MArea ? (MArea) parent
				: isInArea(parent);
	}

	/**
	 * Finds and returns a part that is a valid candidate to be granted activation.
	 */
	private MPart getActivationCandidate(MPart part) {
		// get all the possible parts that we can possibly activate
		Collection<MPart> candidates = part.getContext().get(EPartService.class).getParts();
		for (MPart candidate : candidates) {
			// make sure it's rendered and visible
			if (part != candidate && candidate.isToBeRendered() && candidate.isVisible()) {
				MPlaceholder placeholder = candidate.getCurSharedRef();
				if (placeholder != null) {
					// check that the placeholder is valid
					if (!placeholder.isToBeRendered() || !placeholder.isVisible()) {
						continue;
					}
				}

				MElementContainer<MUIElement> parent = placeholder == null ? candidate.getParent()
						: placeholder.getParent();
				// check that the part is in a structure that's rendered
				if (isValid(parent)) {
					// stacks require considerations because we don't want to activate something
					// that's not the selected element if possible
					if (parent instanceof MGenericStack<?>) {
						// get the selected element
						MUIElement element = parent.getSelectedElement();
						// get the real element if we're a placeholder
						if (element instanceof MPlaceholder) {
							element = ((MPlaceholder) element).getRef();
						}

						// the selected element is the part itself, just return the candidate
						if (element == part) {
							return candidate;
						}

						// FIXME: if we're a part, then we should return it since it is selected,
						// this is correct, but if we're not a part, we should technically drill
						// down further instead of just returning the candidate part, as it is not
						// the selected element of the stack and will cause the stack to
						// unnecessarily change its selection
						return element instanceof MPart ? (MPart) element : candidate;
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

	public MPart getNextActivationCandidate(MPart part) {
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

	MPart getActivationCandidate(MPerspective perspective) {
		Collection<MPart> candidates = perspectiveActivationHistories.get(perspective);
		if (candidates != null) {
			for (Iterator<MPart> it = candidates.iterator(); it.hasNext();) {
				MPart part = it.next();
				if (!part.isToBeRendered()) {
					// this part isn't being rendered, remove this as a candidate
					it.remove();
					continue;
				}

				if (part.isVisible()) {
					MElementContainer<MUIElement> parent = part.getParent();
					if (parent == null) {
						MPlaceholder placeholder = part.getCurSharedRef();
						if (placeholder == null || !placeholder.isToBeRendered()
								|| !isValid(placeholder.getParent())) {
							it.remove();
							continue;
						} else if (!placeholder.isVisible()) {
							continue;
						}
						return part;
					} else if (isValid(parent)) {
						return part;
					}
				}
			}
		}

		candidates = perspective.getContext().get(EPartService.class).getParts();
		for (MPart candidate : candidates) {
			if (isValid(perspective, candidate)) {
				return candidate;
			}
		}
		return null;
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

				if (windowParent == null) {
					parent = (MUIElement) ((EObject) parent).eContainer();
				} else {
					parent = windowParent;
				}
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

	private MPart getSiblingActivationCandidate(MPart part, MUIElement element) {
		LinkedList<MPart> perspectiveActivationHistory = getPerspectiveActivationHistory(part);
		return getSiblingActivationCandidate(part, element,
				perspectiveActivationHistory == null ? generalActivationHistory
						: perspectiveActivationHistory);
	}

	public MPart getSiblingActivationCandidate(MPart part) {
		MPlaceholder placeholder = part.getCurSharedRef();
		return getSiblingActivationCandidate(part, placeholder == null ? part : placeholder);
	}
}
