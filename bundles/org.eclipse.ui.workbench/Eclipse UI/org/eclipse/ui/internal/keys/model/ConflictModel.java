/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.keys.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.bindings.BindingManager;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

/**
 * @since 3.4
 * 
 */
public class ConflictModel extends CommonModel {

	public static final String PROP_CONFLICTS = "conflicts"; //$NON-NLS-1$
	public static final String PROP_CONFLICTS_ADD = "conflictsAdd"; //$NON-NLS-1$
	public static final String PROP_CONFLICTS_REMOVE = "conflictsRemove"; //$NON-NLS-1$

	/**
	 * The set of conflicts for the currently selected element.
	 */
	private Collection conflicts;

	private BindingManager bindingManager;

	private BindingModel bindingModel;

	/**
	 * A mapping of binding element to known conflicts.
	 */
	private Map conflictsMap;

	/**
	 * @param kc
	 */
	public ConflictModel(KeyController kc) {
		super(kc);
	}

	/**
	 * @return Returns the conflicts.
	 */
	public Collection getConflicts() {
		return conflicts;
	}

	/**
	 * Sets the conflicts to the given collection. Any conflicts in the
	 * collection that do not exist in the <code>bindingModel</code> are
	 * removed.
	 * 
	 * @param conflicts
	 *            The conflicts to set.
	 */
	public void setConflicts(Collection conflicts) {
		Object old = this.conflicts;
		this.conflicts = conflicts;

		if (this.conflicts != null) {
			Iterator i = this.conflicts.iterator();
			Map bindingToElement = bindingModel.getBindingToElement();
			while (i.hasNext()) {
				Object next = i.next();
				if (!bindingToElement.containsValue(next)
						&& !next.equals(getSelectedElement())) {
					i.remove();
				}
			}
		}

		controller.firePropertyChange(this, PROP_CONFLICTS, old, conflicts);
	}

	public void updateConflictsFor(BindingElement source) {
		updateConflictsFor(source, false);
	}

	public void updateConflictsFor(BindingElement oldValue,
			BindingElement newValue) {
		updateConflictsFor(oldValue, newValue, false);
	}

	public void updateConflictsFor(BindingElement source, boolean removal) {
		updateConflictsFor(null, source, removal);
	}

	private void updateConflictsFor(BindingElement oldValue,
			BindingElement newValue, boolean removal) {
		updateConflictsFor(newValue, oldValue == null ? null : oldValue
				.getTrigger(), newValue == null ? null : newValue.getTrigger(),
				removal);
	}

	public void updateConflictsFor(BindingElement newValue,
			TriggerSequence oldTrigger, TriggerSequence newTrigger,
			boolean removal) {
		Collection matches = (Collection) conflictsMap.get(newValue);
		if (matches != null) {
			if (newTrigger == null || removal) {
				// we need to clear this match
				matches.remove(newValue);
				conflictsMap.remove(newValue);
				if (matches == conflicts) {
					controller.firePropertyChange(this, PROP_CONFLICTS_REMOVE,
							null, newValue);
				}
				if (matches.size() == 1) {
					BindingElement tbe = (BindingElement) matches.iterator()
							.next();
					conflictsMap.remove(tbe);
					tbe.setConflict(Boolean.FALSE);
					if (matches == conflicts) {
						setConflicts(null);
					}
				}
				return;
			} else if (oldTrigger != null && !newTrigger.equals(oldTrigger)) {
				// we need to clear this match
				matches.remove(newValue);
				conflictsMap.remove(newValue);

				if (matches == conflicts) {
					controller.firePropertyChange(this, PROP_CONFLICTS_REMOVE,
							null, newValue);
				}
				if (matches.size() == 1) {
					BindingElement tbe = (BindingElement) matches.iterator()
							.next();
					conflictsMap.remove(tbe);
					tbe.setConflict(Boolean.FALSE);
					if (matches == conflicts) {
						setConflicts(null);
					}
				}
			} else {
				return;
			}
		}

		if (newValue.getTrigger() == null
				|| !(newValue.getModelObject() instanceof Binding)) {
			return;
		}
		Binding binding = (Binding) newValue.getModelObject();
		TriggerSequence trigger = binding.getTriggerSequence();

		matches = (Collection) bindingManager
				.getActiveBindingsDisregardingContext().get(trigger);
		ArrayList localConflicts = new ArrayList();
		if (matches != null) {
			localConflicts.add(newValue);
			Iterator i = matches.iterator();
			while (i.hasNext()) {
				Binding b = (Binding) i.next();
				if (binding != b
						&& b.getContextId().equals(binding.getContextId())
						&& b.getSchemeId().equals(binding.getSchemeId())) {
					Object element = bindingModel.getBindingToElement().get(b);
					if (element != null) {
						localConflicts.add(element);
					}
				}
			}
		}

		if (localConflicts.size() > 1) {
			// first find if it is already a conflict collection
			Collection knownConflicts = null;
			Iterator i = localConflicts.iterator();
			while (i.hasNext() && knownConflicts == null) {
				BindingElement tbe = (BindingElement) i.next();
				knownConflicts = (Collection) conflictsMap.get(tbe);
			}
			if (knownConflicts != null) {
				knownConflicts.add(newValue);
				conflictsMap.put(newValue, knownConflicts);
				newValue.setConflict(Boolean.TRUE);
				if (knownConflicts == conflicts) {
					controller.firePropertyChange(this, PROP_CONFLICTS_ADD,
							null, newValue);
				} else if (newValue == getSelectedElement()) {
					setConflicts(knownConflicts);
				}
				return;
			}
			boolean isSelected = false;
			i = localConflicts.iterator();
			while (i.hasNext()) {
				BindingElement tbe = (BindingElement) i.next();
				if (tbe != null) {
					conflictsMap.put(tbe, localConflicts);
					tbe.setConflict(Boolean.TRUE);
				}
				if (tbe == getSelectedElement()) {
					isSelected = true;
				}
			}
			if (isSelected) {
				setConflicts(localConflicts);
			}
		}
	}

	public void init(BindingManager manager, BindingModel model) {
		bindingManager = manager;
		bindingModel = model;
		conflictsMap = new HashMap();
		Iterator i = bindingModel.getBindings().iterator();
		while (i.hasNext()) {
			BindingElement be = (BindingElement) i.next();
			if (be.getModelObject() instanceof Binding) {
				updateConflictsFor(be);
			}
		}
		controller.addPropertyChangeListener(new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				if (event.getSource() == ConflictModel.this
						&& CommonModel.PROP_SELECTED_ELEMENT.equals(event
								.getProperty())) {
					if (event.getNewValue() != null) {
						updateConflictsFor(
								(BindingElement) event.getOldValue(),
								(BindingElement) event.getNewValue());
						setConflicts((Collection) conflictsMap.get(event
								.getNewValue()));
					} else {
						setConflicts(null);
					}
				} else if (BindingModel.PROP_BINDING_REMOVE.equals(event
						.getProperty())) {
					updateConflictsFor((BindingElement) event.getOldValue(),
							(BindingElement) event.getNewValue(), true);
				}
			}
		});
	}
}
