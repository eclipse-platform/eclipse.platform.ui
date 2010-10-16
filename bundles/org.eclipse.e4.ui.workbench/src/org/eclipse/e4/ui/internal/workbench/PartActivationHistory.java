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
import java.util.LinkedList;
import java.util.List;
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

	private PartServiceImpl partService;

	private EModelService modelService;

	private LinkedList<MPart> generalActivationHistory = new LinkedList<MPart>();

	PartActivationHistory(PartServiceImpl partService, EModelService modelService) {
		this.partService = partService;
		this.modelService = modelService;
	}

	public void clear() {
		generalActivationHistory.clear();
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
					if (!placeholder.isToBeRendered() || !placeholder.isVisible() || parent == null) {
						return false;
					}

					// if in a stack, then only valid if we're the selected element
					if (parent instanceof MGenericStack
							&& parent.getSelectedElement() != placeholder) {
						return false;
					}
					break;
				}
			}
		} else if (parent instanceof MGenericStack && parent.getSelectedElement() != element) {
			// if in a stack, then only valid if we're the selected element
			return false;
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

	private MPart findActivationCandidate(Collection<MPart> candidates, MPart currentlyActivePart) {
		for (MPart candidate : candidates) {
			if (candidate != currentlyActivePart && candidate.isToBeRendered()
					&& candidate.isVisible()) {
				MPlaceholder placeholder = partService.getLocalPlaceholder(candidate);
				if (placeholder == null) {
					MElementContainer<MUIElement> parent = candidate.getParent();
					// if stack, then must be the selected element to be valid
					if (parent instanceof MGenericStack) {
						if (parent.getSelectedElement() == candidate) {
							return candidate;
						}
					} else {
						return candidate;
					}
				} else if (placeholder.isToBeRendered() && placeholder.isVisible()) {
					MElementContainer<MUIElement> parent = placeholder.getParent();
					// if stack, then must be the selected element to be valid
					if (parent instanceof MGenericStack) {
						if (parent.getSelectedElement() == placeholder) {
							return candidate;
						}
					} else {
						return candidate;
					}
				}
			}
		}
		return null;
	}

	MPart getNextActivationCandidate(MPart part) {
		MArea area = isInArea(part);
		if (area != null) {
			// focus should stay in the area if possible
			MPart candidate = getSiblingActivationCandidate(part);
			if (candidate != null) {
				return candidate;
			}

			// no sibling candidate, find another part in the area to activate
			candidate = findActivationCandidate(
					modelService.findElements(area, null, MPart.class, null), part);
			if (candidate != null) {
				return candidate;
			}
		}

		// check activation history
		MPart candidate = findActivationCandidate(generalActivationHistory, part);
		return candidate == null ? getActivationCandidate(part) : candidate;
	}

	void forget(MWindow window, MPart part, boolean full) {
		if (full) {
			generalActivationHistory.remove(part);
		} else {
			for (MPlaceholder placeholder : modelService.findElements(window, null,
					MPlaceholder.class, null)) {
				// if there is at least one placeholder around, we should keep this
				if (placeholder.getRef() == part && placeholder.isToBeRendered()) {
					return;
				}
			}

			generalActivationHistory.remove(part);
		}
	}

	MPart getActivationCandidate(MPerspective perspective) {
		for (MPart candidate : generalActivationHistory) {
			if (partService.isInContainer(perspective, candidate)
					&& isValid(perspective, candidate)) {
				return candidate;
			}
		}

		Collection<MPart> candidates = perspective.getContext().get(EPartService.class).getParts();
		for (MPart candidate : candidates) {
			if (isValid(perspective, candidate)) {
				return candidate;
			}
		}
		return null;
	}

	private MPart getSiblingActivationCandidate(MPart part) {
		MPlaceholder placeholder = part.getCurSharedRef();
		MUIElement candidate = getSiblingSelectionCandidate(part, placeholder == null ? part
				: placeholder);
		return (MPart) (candidate instanceof MPlaceholder ? ((MPlaceholder) candidate).getRef()
				: candidate);
	}

	private MUIElement getSiblingSelectionCandidate(MPart part, MUIElement element) {
		List<MUIElement> siblings = element.getParent().getChildren();
		for (MPart previouslyActivatedPart : generalActivationHistory) {
			if (previouslyActivatedPart != part && previouslyActivatedPart.isToBeRendered()) {
				if (siblings.contains(previouslyActivatedPart)) {
					return previouslyActivatedPart;
				}

				MPlaceholder placeholder = partService.getLocalPlaceholder(previouslyActivatedPart);
				if (placeholder != null && placeholder.isToBeRendered()
						&& siblings.contains(placeholder)) {
					return placeholder;
				}
			}
		}
		return null;
	}

	MUIElement getSiblingSelectionCandidate(MPart part) {
		MPlaceholder placeholder = part.getCurSharedRef();
		return getSiblingSelectionCandidate(part, placeholder == null ? part : placeholder);
	}
}
