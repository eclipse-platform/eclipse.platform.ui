/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.dependencies;

import java.util.*;

public class DependencySystem implements IDependencySystem {
	public final static int UP_TO_DATE = 0;
	public final static int SATISFACTION = 1;
	public final static int SELECTION = 2;
	public final static int RESOLUTION = 3;
	public final static int TRAVERSAL_PASSES = 3;

	private long elementCount;
	private int mark;

	private ResolutionDelta lastDelta = new ResolutionDelta();
	private ResolutionDelta delta = new ResolutionDelta();
	private Comparator comparator;

	// id-accessible element sets collection
	private Map elementSets = new HashMap();
	// the policy to be used in the selection stage
	private ISelectionPolicy selectionPolicy;
	// should we print debug messages?
	private boolean debug;

	/**
	 * Uses a user-provided comparator for comparing versions.
	 */
	public DependencySystem(Comparator comparator, ISelectionPolicy selectionPolicy, boolean debug) {
		this.comparator = comparator;
		this.selectionPolicy = selectionPolicy;
		this.debug = debug;
	}
	public DependencySystem(Comparator comparator, ISelectionPolicy selectionPolicy) {
		this(comparator, selectionPolicy, false);
	}	
	public IElementSet getElementSet(Object id) {
		ElementSet elementSet = (ElementSet) this.elementSets.get(id);
		// create an element set for the given id if one does not exist yet
		if (elementSet == null)
			this.elementSets.put(id, elementSet = new ElementSet(id, this));
		return elementSet;
	}
	public Collection discoverRoots() {
		Collection roots = new LinkedList();
		for (Iterator elementSetsIter = elementSets.values().iterator(); elementSetsIter.hasNext();) {
			ElementSet elementSet = (ElementSet) elementSetsIter.next();
			if (elementSet.isRoot())
				roots.add(elementSet);
		}
		return roots;
	}
	/**
	 * Determines which versions of each element set are resolved.
	 */
	public IResolutionDelta resolve() throws CyclicSystemException {
		return this.resolve(true);
	}
	public IResolutionDelta resolve(boolean produceDelta) throws CyclicSystemException {
		Collection roots = discoverRoots();
		// traverse from roots to leaves - returns leaves
		Collection satisfied = visit(roots, new SatisfactionVisitor(SATISFACTION));
		// traverse from leaves to roots - returns roots
		Collection selected = visit(satisfied, new SelectionVisitor(SELECTION, this.selectionPolicy));
		// traverse from roots to leaves - returns leaves (result is ignored)
		visit(selected, new ResolutionVisitor(RESOLUTION));
		this.lastDelta = this.delta;
		this.delta = new ResolutionDelta();
		return this.lastDelta;
	}
	/**
	 * Traverses depth-first a graph starting from the given element sets.
	 * Returns a set containing all leaf element sets that satisfied the visitor.
	 */
	public Collection visit(Collection elementSets, IElementSetVisitor visitor) throws CyclicSystemException {
		int visitCounter = 0;
		int mark = getNewMark(visitor.getOrder());
		if (elementSets.isEmpty())
			return Collections.EMPTY_SET;
		Collection leaves = new LinkedList();
		while (!elementSets.isEmpty()) {
			Collection nextLevel = new LinkedList();
			// first visit all element sets in the given set
			for (Iterator elementSetsIter = elementSets.iterator(); elementSetsIter.hasNext();) {
				ElementSet elementSet = (ElementSet) elementSetsIter.next();
				// skip if already visited
				if (mark == elementSet.getVisitedMark())
					continue;

				// last time was visited it has been changed, need to recompute
				// only a change in a previous phase causes the next phase to need to recompute
				if (elementSet.getVisitedMark() == elementSet.getChangedMark() && visitor.getOrder() > getVisitorOrder(elementSet.getChangedMark()))
					elementSet.markNeedingUpdate(visitor.getOrder());
				boolean shouldVisit = true;
				for (Iterator ancestorIter = visitor.getAncestors(elementSet).iterator(); ancestorIter.hasNext();) {
					ElementSet ancestorNode = (ElementSet) ancestorIter.next();
					if (ancestorNode.getVisitedMark() != mark) {
						// one ancestor element set has not been visited yet - bail out			
						shouldVisit = false;
						break;
					}
					if (ancestorNode.getChangedMark() == mark)
						// ancestor has changed - we need to recompute			
						elementSet.markNeedingUpdate(visitor.getOrder());
				}
				if (!shouldVisit)
					continue;

				elementSet.setVisitedMark(mark);

				// only update if necessary
				if (elementSet.isNeedingUpdate(visitor.getOrder()))
					visitor.update(elementSet);

				visitCounter++;

				if (visitor.getDescendants(elementSet).isEmpty())
					leaves.add(elementSet);
				else
					nextLevel.addAll(visitor.getDescendants(elementSet));
			}
			elementSets = nextLevel;
		}
		// if visited more nodes than exist in the graph, a cycle has been found
		// XXX: is this condition enough for detecting a cycle?  
		if (visitCounter != this.elementSets.size())
			throw new CyclicSystemException();
		return leaves;
	}
	private int getVisitorOrder(int mark) {
		return mark & 0xFF;
	}
	private int getNewMark(int order) {
		mark = mark % 0xFF + 1;
		return (mark << 8) + (order & 0xFF);
	}

	public void addElements(IElement[] elementsToAdd) {
		for (int i = 0; i < elementsToAdd.length; i++)
			addElement(elementsToAdd[i]);
	}
	public void addElement(IElement element) {
		((ElementSet) this.getElementSet(element.getId())).addElement(element);
		this.elementCount++;
	}
	public void removeElements(IElement[] elementsToRemove) {
		for (int i = 0; i < elementsToRemove.length; i++)
			removeElement(elementsToRemove[i]);
	}
	public void removeElement(IElement element) {
		ElementSet elementSet = (ElementSet) elementSets.get(element.getId());
		if (elementSet == null)
			return;
		elementSet.removeElement(element);
		if (elementSet.getElementCount() == 0 && elementSet.getRequiringCount() == 0)
			elementSets.remove(element.getId());
	}
	public long getElementCount() {
		return elementCount;
	}
	public Map getNodes() {
		return this.elementSets;
	}
	public Set getResolved() {
		final Set resolved = new HashSet();
		for (Iterator elementSetsIter = elementSets.values().iterator(); elementSetsIter.hasNext();) {
			ElementSet elementSet = (ElementSet) elementSetsIter.next();
			if (elementSet.getResolved() != null)
				resolved.addAll(elementSet.getResolved());
		}
		return resolved;
	}
	public String toString() {
		StringBuffer result = new StringBuffer();
		for (Iterator elementSetsIter = elementSets.values().iterator(); elementSetsIter.hasNext();) {
			IElementSet elementSet = (IElementSet) elementSetsIter.next();
			for (Iterator elementsIter = elementSet.getAvailable().iterator(); elementsIter.hasNext();) {
				IElement element = (IElement) elementsIter.next();
				result.append(element + ": " + Arrays.asList(element.getDependencies())); //$NON-NLS-1$
				result.append(',');
			}
			result.deleteCharAt(result.length() - 1);
			result.append('\n');
		}
		return result.toString();
	}
	void recordElementStatusChanged(IElement element, int kind) {
		if (this.delta == null)
			return;
		this.delta.recordChange(element, kind);
	}
	void recordDependencyChanged(Collection oldResolved, Collection newResolved) {
		if (this.delta == null)
			return;
		for (Iterator oldResolvedIter = oldResolved.iterator(); oldResolvedIter.hasNext();) {
			IElement element = (IElement) oldResolvedIter.next();
			if (!newResolved.contains(element))
				this.delta.recordChange(element, IElementChange.RESOLVED_TO_UNRESOLVED);
		}
		for (Iterator newResolvedIter = newResolved.iterator(); newResolvedIter.hasNext();) {
			IElement element = (IElement) newResolvedIter.next();
			if (!oldResolved.contains(element))
				this.delta.recordChange(element, IElementChange.UNRESOLVED_TO_RESOLVED);
		}
	}
	public IElement getElement(Object id, Object versionId) {
		ElementSet elementSet = (ElementSet) elementSets.get(id);
		if (elementSet == null)
			return null;
		return elementSet.getElement(versionId);
	}

	public IElement createElement(Object id, Object versionId, IDependency[] dependencies, boolean singleton) {
		return new Element(id, versionId, dependencies, singleton);
	}
	public IDependency createDependency(Object requiredObjectId, IMatchRule satisfactionRule, Object requiredVersionId, boolean optional) {
		return new Dependency(requiredObjectId, satisfactionRule, requiredVersionId, optional);
	}
	public int compare(Object obj1, Object obj2) {
		return comparator.compare(obj1, obj2);
	}
	public IResolutionDelta getLastDelta() {
		return lastDelta;
	}
	boolean inDebugMode() {
		return debug;
	}
}
