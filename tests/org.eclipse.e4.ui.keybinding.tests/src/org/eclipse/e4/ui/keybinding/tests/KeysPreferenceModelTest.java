/*******************************************************************************
 * Copyright (c) 2007, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.e4.ui.keybinding.tests;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.bindings.keys.KeyBinding;
import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.internal.keys.model.BindingElement;
import org.eclipse.ui.internal.keys.model.BindingModel;
import org.eclipse.ui.internal.keys.model.CommonModel;
import org.eclipse.ui.internal.keys.model.ConflictModel;
import org.eclipse.ui.internal.keys.model.ContextElement;
import org.eclipse.ui.internal.keys.model.ContextModel;
import org.eclipse.ui.internal.keys.model.KeyController;
import org.eclipse.ui.internal.keys.model.ModelElement;
import org.eclipse.ui.internal.keys.model.SchemeElement;
import org.eclipse.ui.internal.keys.model.SchemeModel;
import org.eclipse.ui.keys.IBindingService;
import org.eclipse.ui.tests.harness.util.UITestCase;

/**
 * @since 3.4
 * 
 */
public class KeysPreferenceModelTest extends UITestCase {

	private static final String ID_QUICK_SWITCH = "org.eclipse.ui.window.openEditorDropDown";
	private static final String SCHEME_EMACS_ID = "org.eclipse.ui.emacsAcceleratorConfiguration";
	private static final String ID_QUICK_ACCESS = "org.eclipse.ui.window.quickAccess";
	private static final String ID_ACTIVATE_EDITOR = "org.eclipse.ui.window.activateEditor";
	private static final String ID_ABOUT = "org.eclipse.ui.help.aboutAction";
	private static final String ID_CMD_CONFLICT1 = "org.eclipse.ui.tests.keyModel.conflict1";
	private static final String ID_CMD_CONFLICT2 = "org.eclipse.ui.tests.keyModel.conflict2";
	private static final String ID_CMD_CONFLICT3 = "org.eclipse.ui.tests.keyModel.conflict3";
	private static final String ID_CMD_CONFLICT4 = "org.eclipse.ui.tests.keyModel.conflict4";
	private static final String ID_CMD_EMACS1 = "org.eclipse.ui.tests.keyModel.emacs1";

	/**
	 * @param testName
	 */
	public KeysPreferenceModelTest(String testName) {
		super(testName);
	}

	public void testDefaults() throws Exception {
		KeyController controller = new KeyController();
		controller.init(getWorkbench());

		ContextModel cm = controller.getContextModel();
		boolean foundWindow = false;
		boolean foundDialog = false;
		Iterator i = cm.getContexts().iterator();
		while (i.hasNext()) {
			ContextElement elem = (ContextElement) i.next();
			if (elem.getId().equals(IContextService.CONTEXT_ID_WINDOW)) {
				foundWindow = true;
			} else if (elem.getId().equals(IContextService.CONTEXT_ID_DIALOG)) {
				foundDialog = true;
			}
		}
		assertTrue("No window context", foundWindow);
		assertTrue("No dialog context", foundDialog);
		assertNull(cm.getSelectedElement());
		assertNotNull(cm.getContextIdToElement().get(
				IContextService.CONTEXT_ID_DIALOG_AND_WINDOW));

		SchemeModel sm = controller.getSchemeModel();
		boolean foundDefault = false;
		i = sm.getSchemes().iterator();
		while (i.hasNext()) {
			SchemeElement e = (SchemeElement) i.next();
			if (e.getId().equals(
					IBindingService.DEFAULT_DEFAULT_ACTIVE_SCHEME_ID)) {
				foundDefault = true;
			}
		}
		assertTrue("No default scheme", foundDefault);
		assertEquals(IBindingService.DEFAULT_DEFAULT_ACTIVE_SCHEME_ID, sm
				.getSelectedElement().getId());

		ConflictModel cf = controller.getConflictModel();
		assertNull("There should not be any conflicts", cf.getConflicts());
		assertNull(cf.getSelectedElement());

		BindingModel bm = controller.getBindingModel();
		BindingElement quickAccess = getBindingElement(bm, ID_QUICK_ACCESS);
		assertNotNull(quickAccess);
		assertNull(bm.getSelectedElement());
	}

	public void testContexts() throws Exception {
		final KeyController controller = new KeyController();
		controller.init(getWorkbench());

		ContextModel cm = controller.getContextModel();
		ContextElement dialog = (ContextElement) cm.getContextIdToElement()
				.get(IContextService.CONTEXT_ID_DIALOG);
		assertNull(cm.getSelectedElement());
		assertNotNull(dialog);

		final ArrayList events = new ArrayList();
		// test setup vars
		controller.addPropertyChangeListener(new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				events.add(event);
			}
		});
		cm.setSelectedElement(dialog);

		assertTrue(cm.getSelectedElement() == dialog);

		assertEquals(1, events.size());

		ContextElement window = (ContextElement) cm.getContextIdToElement()
				.get(IContextService.CONTEXT_ID_WINDOW);
		assertNotNull(window);

		cm.setSelectedElement(window);
		assertEquals(2, events.size());

		cm.setSelectedElement(null);
		assertEquals(3, events.size());

		PropertyChangeEvent[] expected = new PropertyChangeEvent[] {
				new PropertyChangeEvent(controller.getContextModel(),
						CommonModel.PROP_SELECTED_ELEMENT, null, dialog),
				new PropertyChangeEvent(controller.getContextModel(),
						CommonModel.PROP_SELECTED_ELEMENT, dialog, window),
				new PropertyChangeEvent(controller.getContextModel(),
						CommonModel.PROP_SELECTED_ELEMENT, window, null) };
		assertChanges(expected, events);
	}

	public void testBindings() throws Exception {
		final KeyController controller = new KeyController();
		controller.init(getWorkbench());

		ContextModel cm = controller.getContextModel();
		BindingModel bm = controller.getBindingModel();
		BindingElement activateEditor = getBindingElement(bm,
				ID_ACTIVATE_EDITOR);
		assertNotNull(activateEditor);
		assertNotNull(activateEditor.getContext());
		assertNull(bm.getSelectedElement());

		// test setup vars
		final ArrayList events = new ArrayList();
		controller.addPropertyChangeListener(new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				events.add(event);
			}
		});

		bm.setSelectedElement(activateEditor);

		assertTrue(bm.getSelectedElement() == activateEditor);
		assertNotNull(cm.getSelectedElement());
		assertTrue(cm.getSelectedElement().getId().equals(
				activateEditor.getContext().getId()));

		PropertyChangeEvent[] expected = new PropertyChangeEvent[] {
				new PropertyChangeEvent(controller.getConflictModel(),
						CommonModel.PROP_SELECTED_ELEMENT, null, activateEditor),
				new PropertyChangeEvent(controller.getContextModel(),
						CommonModel.PROP_SELECTED_ELEMENT, null, activateEditor
								.getContext()),
				new PropertyChangeEvent(controller.getBindingModel(),
						CommonModel.PROP_SELECTED_ELEMENT, null, activateEditor), };
		assertChanges(expected, events);

		events.clear();
		bm.setSelectedElement(null);
		assertNull(bm.getSelectedElement());
		assertTrue(cm.getSelectedElement().getId().equals(
				activateEditor.getContext().getId()));

		expected = new PropertyChangeEvent[] {
				new PropertyChangeEvent(controller.getConflictModel(),
						CommonModel.PROP_SELECTED_ELEMENT, activateEditor, null),
				new PropertyChangeEvent(controller.getBindingModel(),
						CommonModel.PROP_SELECTED_ELEMENT, activateEditor, null) };
		assertChanges(expected, events);
	}

	public void testBasicConflicts() throws Exception {
		final KeyController controller = new KeyController();
		controller.init(getWorkbench());

		final ConflictModel cf = controller.getConflictModel();
		final BindingModel bm = controller.getBindingModel();
		final BindingElement conflict1 = getBindingElement(bm, ID_CMD_CONFLICT1);
		assertNotNull(conflict1);
		assertEquals(Boolean.TRUE, conflict1.getConflict());
		final BindingElement activateEditor = getBindingElement(bm,
				ID_ACTIVATE_EDITOR);
		assertNotNull(activateEditor);
		assertEquals(Boolean.FALSE, activateEditor.getConflict());

		// test setup vars
		final ArrayList events = new ArrayList();
		controller.addPropertyChangeListener(new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				events.add(event);
			}
		});

		bm.setSelectedElement(conflict1);
		assertEquals(conflict1, bm.getSelectedElement());
		assertEquals(conflict1, cf.getSelectedElement());

		final Collection conflicts = cf.getConflicts();
		assertEquals(3, conflicts.size());

		PropertyChangeEvent[] expected = new PropertyChangeEvent[] {
				new PropertyChangeEvent(controller.getConflictModel(),
						ConflictModel.PROP_CONFLICTS, null, conflicts),
				new PropertyChangeEvent(controller.getConflictModel(),
						CommonModel.PROP_SELECTED_ELEMENT, null, conflict1),
				new PropertyChangeEvent(controller.getContextModel(),
						CommonModel.PROP_SELECTED_ELEMENT, null, conflict1
								.getContext()),
				new PropertyChangeEvent(controller.getBindingModel(),
						CommonModel.PROP_SELECTED_ELEMENT, null, conflict1) };

		assertChanges(expected, events);

		events.clear();
		bm.setSelectedElement(activateEditor);
		assertEquals(activateEditor, bm.getSelectedElement());

		expected = new PropertyChangeEvent[] {
				new PropertyChangeEvent(controller.getConflictModel(),
						ConflictModel.PROP_CONFLICTS, conflicts, null),
				new PropertyChangeEvent(controller.getConflictModel(),
						CommonModel.PROP_SELECTED_ELEMENT, conflict1,
						activateEditor),
				new PropertyChangeEvent(controller.getContextModel(),
						CommonModel.PROP_SELECTED_ELEMENT, conflict1
								.getContext(), activateEditor.getContext()),
				new PropertyChangeEvent(controller.getBindingModel(),
						CommonModel.PROP_SELECTED_ELEMENT, conflict1,
						activateEditor) };

		assertChanges(expected, events);
	}

	public void testConflictSelection() throws Exception {
		final KeyController controller = new KeyController();
		controller.init(getWorkbench());

		final ConflictModel cf = controller.getConflictModel();
		final BindingModel bm = controller.getBindingModel();
		final BindingElement conflict1 = getBindingElement(bm, ID_CMD_CONFLICT1);
		final BindingElement conflict3 = getBindingElement(bm, ID_CMD_CONFLICT3);

		// test setup vars
		final ArrayList events = new ArrayList();
		controller.addPropertyChangeListener(new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				events.add(event);
			}
		});

		bm.setSelectedElement(conflict1);
		assertEquals(conflict1, bm.getSelectedElement());
		assertEquals(conflict1, cf.getSelectedElement());

		assertEquals(4, events.size());

		events.clear();

		cf.setSelectedElement(conflict3);
		assertEquals(conflict3, bm.getSelectedElement());
		assertEquals(conflict3, cf.getSelectedElement());

		PropertyChangeEvent[] expected = new PropertyChangeEvent[] {
				new PropertyChangeEvent(controller.getBindingModel(),
						CommonModel.PROP_SELECTED_ELEMENT, conflict1, conflict3),
				new PropertyChangeEvent(controller.getConflictModel(),
						CommonModel.PROP_SELECTED_ELEMENT, conflict1, conflict3) };
		assertChanges(expected, events);
	}

	public void failsOnCocoatestCreateConflict() throws Exception {
		final KeyController controller = new KeyController();
		controller.init(getWorkbench());

		final ConflictModel cf = controller.getConflictModel();
		final BindingModel bm = controller.getBindingModel();
		BindingElement about = getBindingElement(bm, ID_ABOUT);
		assertNotNull(about);
		assertNotNull(about.getTrigger());

		BindingElement active = getBindingElement(bm, ID_ACTIVATE_EDITOR);
		assertNotNull(active);
		assertNotNull(active.getTrigger());
		assertEquals(Boolean.FALSE, active.getConflict());

		bm.setSelectedElement(about);

		about.setTrigger(KeySequence.getInstance("F12"));
		assertEquals(Boolean.TRUE, about.getConflict());
		assertEquals(Boolean.TRUE, active.getConflict());
		assertEquals(about, cf.getSelectedElement());

		about.setTrigger(KeySequence.getInstance("F12 A"));
		assertEquals(Boolean.FALSE, about.getConflict());
		assertEquals(Boolean.FALSE, active.getConflict());
		assertEquals(about, cf.getSelectedElement());
		assertNull(cf.getConflicts());

		about.setTrigger(null);
		assertEquals(Boolean.FALSE, about.getConflict());
		assertEquals(Boolean.FALSE, active.getConflict());
		assertEquals(about, cf.getSelectedElement());
		assertNull(cf.getConflicts());
	}

	public void failsOnMacCocoatestConflictRemove() throws Exception {
		final KeyController controller = new KeyController();
		controller.init(getWorkbench());

		final ConflictModel cf = controller.getConflictModel();
		final BindingModel bm = controller.getBindingModel();
		BindingElement about = getBindingElement(bm, ID_ABOUT);
		assertNotNull(about);
		assertNotNull(about.getTrigger());

		BindingElement active = getBindingElement(bm, ID_ACTIVATE_EDITOR);
		assertNotNull(active);
		assertNotNull(active.getTrigger());
		assertEquals(Boolean.FALSE, active.getConflict());

		bm.setSelectedElement(about);

		about.setTrigger(KeySequence.getInstance("F12"));
		assertEquals(Boolean.TRUE, about.getConflict());
		assertEquals(Boolean.TRUE, active.getConflict());
		assertEquals(about, cf.getSelectedElement());

		bm.remove();
		assertEquals(Boolean.FALSE, about.getConflict());
		assertEquals(Boolean.FALSE, active.getConflict());
		assertEquals(about, cf.getSelectedElement());
		assertNull(cf.getConflicts());
	}

	public void failsOnMacCocoatestConflictRestore() throws Exception {
		final KeyController controller = new KeyController();
		controller.init(getWorkbench());

		final ConflictModel cf = controller.getConflictModel();
		final BindingModel bm = controller.getBindingModel();
		BindingElement about = getBindingElement(bm, ID_ABOUT);
		assertNotNull(about);
		assertNotNull(about.getTrigger());

		BindingElement active = getBindingElement(bm, ID_ACTIVATE_EDITOR);
		assertNotNull(active);
		assertNotNull(active.getTrigger());
		assertEquals(Boolean.FALSE, active.getConflict());

		bm.setSelectedElement(about);

		about.setTrigger(KeySequence.getInstance("F12"));
		assertEquals(Boolean.TRUE, about.getConflict());
		assertEquals(Boolean.TRUE, active.getConflict());
		assertEquals(about, cf.getSelectedElement());

		bm.restoreBinding(controller.getContextModel());

		active = getBindingElement(bm, ID_ACTIVATE_EDITOR);
		about = getBindingElement(bm, ID_ABOUT);

		assertEquals(Boolean.FALSE, about.getConflict());
		assertEquals(Boolean.FALSE, active.getConflict());
		assertEquals(bm.getSelectedElement(), cf.getSelectedElement());
		assertNull(cf.getConflicts());
	}

	public void testUpdateContext() throws Exception {
		final KeyController controller = new KeyController();
		controller.init(getWorkbench());

		final ContextModel cm = controller.getContextModel();
		final ContextElement dialog = (ContextElement) cm
				.getContextIdToElement().get(IContextService.CONTEXT_ID_DIALOG);
		final ContextElement window = (ContextElement) cm
				.getContextIdToElement().get(IContextService.CONTEXT_ID_WINDOW);

		final BindingModel bm = controller.getBindingModel();
		final BindingElement conflict2 = getBindingElement(bm, ID_CMD_CONFLICT2);
		final Binding c2model = (Binding) conflict2.getModelObject();
		assertEquals(dialog, conflict2.getContext());

		bm.setSelectedElement(conflict2);
		assertEquals(dialog, cm.getSelectedElement());

		// test setup vars
		final ArrayList events = new ArrayList();
		controller.addPropertyChangeListener(new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				events.add(event);
			}
		});

		cm.setSelectedElement(window);
		assertEquals(window, ((BindingElement) bm.getSelectedElement())
				.getContext());
		assertNotSame(c2model, conflict2.getModelObject());

		PropertyChangeEvent[] expected = new PropertyChangeEvent[] {
				new PropertyChangeEvent(conflict2,
						BindingElement.PROP_CONFLICT, Boolean.TRUE,
						Boolean.FALSE),
				new PropertyChangeEvent(conflict2, BindingElement.PROP_CONTEXT,
						dialog, window),
				new PropertyChangeEvent(conflict2,
						BindingElement.PROP_USER_DELTA, new Integer(
								Binding.SYSTEM), new Integer(Binding.USER)),
				new PropertyChangeEvent(conflict2,
						ModelElement.PROP_MODEL_OBJECT, c2model, conflict2
								.getModelObject()),
				new PropertyChangeEvent(cm, CommonModel.PROP_SELECTED_ELEMENT,
						dialog, window), };
		assertChanges(expected, events);
	}

	public void failsOnWinAndLinuxWith16VMtestUpdateKeySequence() throws Exception {
		final KeyController controller = new KeyController();
		controller.init(getWorkbench());

		final ContextModel cm = controller.getContextModel();
		final ContextElement dialog = (ContextElement) cm
				.getContextIdToElement().get(IContextService.CONTEXT_ID_DIALOG);

		final BindingModel bm = controller.getBindingModel();
		final BindingElement conflict1 = getBindingElement(bm, ID_CMD_CONFLICT1);
		final BindingElement conflict2 = getBindingElement(bm, ID_CMD_CONFLICT2);
		final BindingElement conflict3 = getBindingElement(bm, ID_CMD_CONFLICT3);
		final Binding c2model = (Binding) conflict2.getModelObject();
		final Binding c3model = (Binding) conflict3.getModelObject();
		final ParameterizedCommand c3parameterized = c3model
				.getParameterizedCommand();
		assertEquals(dialog, conflict2.getContext());
		assertTrue(c2model instanceof KeyBinding);
		bm.setSelectedElement(conflict2);
		assertEquals(dialog, cm.getSelectedElement());

		// test setup vars
		final ArrayList events = new ArrayList();
		controller.addPropertyChangeListener(new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				events.add(event);
			}
		});

		KeySequence oldKeySequence = (KeySequence) conflict2.getTrigger();
		KeySequence ctrl5 = KeySequence.getInstance("CTRL+5 N");
		Object bindingConflict1 = conflict1.getModelObject();
		conflict2.setTrigger(ctrl5);
		ConflictModel conflictModel = controller.getConflictModel();
		ArrayList oldValue = new ArrayList();
		oldValue.add(conflict3);

		assertEquals(dialog, ((BindingElement) bm.getSelectedElement())
				.getContext());
		assertTrue(conflict2.getModelObject() instanceof KeyBinding);
		PropertyChangeEvent[] expected = new PropertyChangeEvent[] {
				new PropertyChangeEvent(conflict2,
						BindingElement.PROP_CONFLICT, Boolean.TRUE,
						Boolean.FALSE),
				new PropertyChangeEvent(conflict2,
						BindingElement.PROP_USER_DELTA, new Integer(
								Binding.SYSTEM), new Integer(Binding.USER)),
				new PropertyChangeEvent(controller.getConflictModel(),
						ConflictModel.PROP_CONFLICTS_REMOVE, null, conflict2),
				new PropertyChangeEvent(conflict2,
						ModelElement.PROP_MODEL_OBJECT, c2model, conflict2
								.getModelObject()),
				new PropertyChangeEvent(conflict1,
						BindingElement.PROP_CONFLICT, Boolean.TRUE,
						Boolean.FALSE),
				new PropertyChangeEvent(
						conflict1,
						BindingElement.PROP_CONTEXT,
						((BindingElement) bm.getSelectedElement()).getContext(),
						null),
				new PropertyChangeEvent(conflictModel,
						ConflictModel.PROP_CONFLICTS_REMOVE, null, conflict1),
				new PropertyChangeEvent(conflict3,
						BindingElement.PROP_CONFLICT, Boolean.TRUE,
						Boolean.FALSE),
				new PropertyChangeEvent(conflictModel,
						ConflictModel.PROP_CONFLICTS, oldValue, null),
				new PropertyChangeEvent(conflict1,
						ModelElement.PROP_MODEL_OBJECT, bindingConflict1,
						conflict1.getModelObject()),
				new PropertyChangeEvent(conflict1, BindingElement.PROP_TRIGGER,
						oldKeySequence, null),
				new PropertyChangeEvent(conflict3, BindingElement.PROP_CONTEXT,
						dialog, null),
				new PropertyChangeEvent(conflict3,
						ModelElement.PROP_MODEL_OBJECT, c3model,
						c3parameterized),
				new PropertyChangeEvent(conflict3,
						BindingElement.PROP_TRIGGER, oldKeySequence, null),
				new PropertyChangeEvent(bm,
						BindingModel.PROP_CONFLICT_ELEMENT_MAP, null, conflict3),
				new PropertyChangeEvent(conflict2, BindingElement.PROP_TRIGGER,
						oldKeySequence, ctrl5), };
		assertChanges(expected, events);
	}

	public void testCreateKeyBinding() throws Exception {
		final KeyController controller = new KeyController();
		controller.init(getWorkbench());

		final ContextModel cm = controller.getContextModel();
		final ContextElement window = (ContextElement) cm
				.getContextIdToElement().get(IContextService.CONTEXT_ID_WINDOW);

		final BindingModel bm = controller.getBindingModel();
		final BindingElement conflict4 = getBindingElement(bm, ID_CMD_CONFLICT4);
		assertNull(conflict4.getContext());
		Object c4model = conflict4.getModelObject();
		assertTrue(c4model instanceof ParameterizedCommand);

		bm.setSelectedElement(conflict4);
		assertNull(cm.getSelectedElement());

		// test setup vars
		final ArrayList events = new ArrayList();
		controller.addPropertyChangeListener(new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				events.add(event);
			}
		});

		KeySequence ctrl5 = KeySequence.getInstance("CTRL+5 N");
		conflict4.setTrigger(ctrl5);
		assertEquals(window, ((BindingElement) bm.getSelectedElement())
				.getContext());
		assertTrue(conflict4.getModelObject() instanceof KeyBinding);
		assertEquals(cm.getSelectedElement(), conflict4.getContext());

		PropertyChangeEvent[] expected = new PropertyChangeEvent[] {
				new PropertyChangeEvent(conflict4, BindingElement.PROP_CONTEXT,
						null, window),
				new PropertyChangeEvent(conflict4,
						BindingElement.PROP_USER_DELTA, new Integer(
								Binding.SYSTEM), new Integer(Binding.USER)),
				new PropertyChangeEvent(cm, CommonModel.PROP_SELECTED_ELEMENT,
						null, conflict4.getContext()),
				new PropertyChangeEvent(conflict4,
						ModelElement.PROP_MODEL_OBJECT, c4model, conflict4
								.getModelObject()),
				new PropertyChangeEvent(conflict4, BindingElement.PROP_TRIGGER,
						null, ctrl5), };
		assertChanges(expected, events);
	}

	public void testChangeSchemes() throws Exception {
		final KeyController controller = new KeyController();
		controller.init(getWorkbench());

		SchemeModel sm = controller.getSchemeModel();
		SchemeElement emacsScheme = null;
		Iterator i = sm.getSchemes().iterator();
		while (i.hasNext()) {
			SchemeElement e = (SchemeElement) i.next();
			if (e.getId().equals(SCHEME_EMACS_ID)) {
				emacsScheme = e;
			}
		}
		assertNotNull(emacsScheme);

		BindingModel bm = controller.getBindingModel();
		BindingElement quickSwitch = null;
		int quickCount = 0;
		i = bm.getBindings().iterator();
		while (i.hasNext()) {
			BindingElement e = (BindingElement) i.next();
			if (e.getId().equals(ID_QUICK_SWITCH)) {
				quickSwitch = e;
				quickCount++;
			}
		}
		assertNotNull(quickSwitch);
		assertEquals(1, quickCount);

		sm.setSelectedElement(emacsScheme);

		i = bm.getBindings().iterator();
		ArrayList quick2 = new ArrayList();
		boolean foundOriginal = false;
		while (i.hasNext()) {
			BindingElement e = (BindingElement) i.next();
			if (e.getId().equals(ID_QUICK_SWITCH)) {
				quick2.add(e);
				if (e == quickSwitch) {
					foundOriginal = true;
				}
			}
		}
		assertEquals(2, quick2.size());
		assertTrue(foundOriginal);
	}

	public void testChangeSchemesTwice() throws Exception {
		final KeyController controller = new KeyController();
		controller.init(getWorkbench());

		SchemeModel sm = controller.getSchemeModel();
		SchemeElement emacsScheme = null;
		SchemeElement defaultScheme = null;
		Iterator i = sm.getSchemes().iterator();
		while (i.hasNext()) {
			SchemeElement e = (SchemeElement) i.next();
			if (e.getId().equals(SCHEME_EMACS_ID)) {
				emacsScheme = e;
			} else if (e.getId().equals(
					IBindingService.DEFAULT_DEFAULT_ACTIVE_SCHEME_ID)) {
				defaultScheme = e;
			}
		}
		assertNotNull(emacsScheme);
		assertNotNull(defaultScheme);

		BindingModel bm = controller.getBindingModel();
		BindingElement quickSwitch = null;
		int quickCount = 0;
		i = bm.getBindings().iterator();
		while (i.hasNext()) {
			BindingElement e = (BindingElement) i.next();
			if (e.getId().equals(ID_QUICK_SWITCH)) {
				quickSwitch = e;
				quickCount++;
			}
		}
		assertNotNull(quickSwitch);
		assertEquals(1, quickCount);

		sm.setSelectedElement(emacsScheme);

		i = bm.getBindings().iterator();
		ArrayList quick2 = new ArrayList();
		boolean foundOriginal = false;
		while (i.hasNext()) {
			BindingElement e = (BindingElement) i.next();
			if (e.getId().equals(ID_QUICK_SWITCH)) {
				quick2.add(e);
				if (e == quickSwitch) {
					foundOriginal = true;
				}
			}
		}
		assertEquals(2, quick2.size());
		assertTrue(foundOriginal);

		sm.setSelectedElement(defaultScheme);

		i = bm.getBindings().iterator();
		quick2.clear();
		foundOriginal = false;
		while (i.hasNext()) {
			BindingElement e = (BindingElement) i.next();
			if (e.getId().equals(ID_QUICK_SWITCH)) {
				quick2.add(e);
				if (e == quickSwitch) {
					foundOriginal = true;
				}
			}
		}
		assertEquals(1, quick2.size());
		assertTrue(foundOriginal);
	}

	public void testSchemesWithNoDefaultBinding() throws Exception {
		final KeyController controller = new KeyController();
		controller.init(getWorkbench());

		final SchemeModel sm = controller.getSchemeModel();
		SchemeElement emacsScheme = null;
		SchemeElement defaultScheme = null;
		Iterator i = sm.getSchemes().iterator();
		while (i.hasNext()) {
			SchemeElement e = (SchemeElement) i.next();
			if (e.getId().equals(SCHEME_EMACS_ID)) {
				emacsScheme = e;
			} else if (e.getId().equals(
					IBindingService.DEFAULT_DEFAULT_ACTIVE_SCHEME_ID)) {
				defaultScheme = e;
			}
		}
		assertNotNull(emacsScheme);
		assertNotNull(defaultScheme);

		final BindingModel bm = controller.getBindingModel();
		BindingElement emacsElement = getBindingElement(bm, ID_CMD_EMACS1);
		assertNotNull(emacsElement);
		assertTrue(emacsElement.getModelObject() instanceof ParameterizedCommand);

		sm.setSelectedElement(emacsScheme);

		emacsElement = getBindingElement(bm, ID_CMD_EMACS1);
		assertTrue(emacsElement.getModelObject() instanceof KeyBinding);

		sm.setSelectedElement(defaultScheme);

		emacsElement = getBindingElement(bm, ID_CMD_EMACS1);
		assertTrue(emacsElement.getModelObject() instanceof ParameterizedCommand);
	}

	public void testCopyBinding() throws Exception {
		final KeyController controller = new KeyController();
		controller.init(getWorkbench());

		BindingModel bm = controller.getBindingModel();
		BindingElement activateEditor = null;
		ArrayList activates = new ArrayList();
		Iterator i = bm.getBindings().iterator();
		while (i.hasNext()) {
			BindingElement be = (BindingElement) i.next();
			if (be.getId().equals(ID_ACTIVATE_EDITOR)) {
				activates.add(be);
				if (be.getModelObject() instanceof KeyBinding) {
					activateEditor = be;
				}
			}
		}
		assertEquals(1, activates.size());
		assertNotNull(activateEditor);

		bm.setSelectedElement(activateEditor);
		bm.copy();
		activates.clear();
		i = bm.getBindings().iterator();
		while (i.hasNext()) {
			BindingElement be = (BindingElement) i.next();
			if (be.getId().equals(ID_ACTIVATE_EDITOR)) {
				activates.add(be);
			}
		}
		assertEquals(2, activates.size());
	}

	public void testCopyCommand() throws Exception {
		final KeyController controller = new KeyController();
		controller.init(getWorkbench());

		BindingModel bm = controller.getBindingModel();
		BindingElement conflict4 = null;
		ArrayList activates = new ArrayList();
		Iterator i = bm.getBindings().iterator();
		while (i.hasNext()) {
			BindingElement be = (BindingElement) i.next();
			if (be.getId().equals(ID_CMD_CONFLICT4)) {
				activates.add(be);
				if (be.getModelObject() instanceof ParameterizedCommand) {
					conflict4 = be;
				}
			}
		}
		assertEquals(1, activates.size());
		assertNotNull(conflict4);

		bm.setSelectedElement(conflict4);
		bm.copy();
		activates.clear();
		i = bm.getBindings().iterator();
		while (i.hasNext()) {
			BindingElement be = (BindingElement) i.next();
			if (be.getId().equals(ID_CMD_CONFLICT4)) {
				activates.add(be);
			}
		}
		assertEquals(1, activates.size());
	}

	public void testRemoveActiveEditor() throws Exception {
		final KeyController controller = new KeyController();
		controller.init(getWorkbench());

		BindingModel bm = controller.getBindingModel();
		BindingElement activateEditor = getBindingElement(bm,
				ID_ACTIVATE_EDITOR);

		assertNotNull(activateEditor);
		assertTrue(activateEditor.getModelObject() instanceof KeyBinding);

		bm.setSelectedElement(activateEditor);
		bm.remove();

		assertTrue(activateEditor.getModelObject() instanceof ParameterizedCommand);
	}

	public void testRestoreBinding() throws Exception {
		final KeyController controller = new KeyController();
		controller.init(getWorkbench());

		BindingModel bm = controller.getBindingModel();
		BindingElement activateEditor = getBindingElement(bm,
				ID_ACTIVATE_EDITOR);

		bm.setSelectedElement(activateEditor);

		KeySequence ctrl5 = KeySequence.getInstance("CTRL+5 N");
		activateEditor.setTrigger(ctrl5);

		assertEquals(new Integer(Binding.USER), activateEditor.getUserDelta());
		bm.copy();
		BindingElement activeTwo = (BindingElement) bm.getSelectedElement();
		assertFalse(activateEditor == activeTwo);

		activeTwo.setTrigger(KeySequence.getInstance("CTRL+5 M"));
		assertEquals(new Integer(Binding.USER), activeTwo.getUserDelta());

		ArrayList activates = new ArrayList();
		Iterator i = bm.getBindings().iterator();
		while (i.hasNext()) {
			BindingElement be = (BindingElement) i.next();
			if (be.getId().equals(ID_ACTIVATE_EDITOR)) {
				activates.add(be);
			}
		}
		assertEquals(2, activates.size());

		bm.restoreBinding(controller.getContextModel());

		activates = new ArrayList();
		i = bm.getBindings().iterator();
		while (i.hasNext()) {
			BindingElement be = (BindingElement) i.next();
			if (be.getId().equals(ID_ACTIVATE_EDITOR)) {
				activates.add(be);
				activateEditor = be;
			}
		}
		assertEquals(1, activates.size());
		assertEquals(new Integer(Binding.SYSTEM), activateEditor.getUserDelta());
	}

	public void testRestoreCommand() throws Exception {
		final KeyController controller = new KeyController();
		controller.init(getWorkbench());

		final ContextModel cm = controller.getContextModel();
		final ContextElement window = (ContextElement) cm
				.getContextIdToElement().get(IContextService.CONTEXT_ID_WINDOW);

		final BindingModel bm = controller.getBindingModel();
		BindingElement conflict4 = getBindingElement(bm, ID_CMD_CONFLICT4);
		assertNull(conflict4.getContext());
		Object c4model = conflict4.getModelObject();
		assertTrue(c4model instanceof ParameterizedCommand);

		bm.setSelectedElement(conflict4);
		assertNull(cm.getSelectedElement());

		KeySequence ctrl5 = KeySequence.getInstance("CTRL+5 N");
		conflict4.setTrigger(ctrl5);
		assertEquals(window, ((BindingElement) bm.getSelectedElement())
				.getContext());
		assertTrue(conflict4.getModelObject() instanceof KeyBinding);

		bm.setSelectedElement(conflict4);
		bm.restoreBinding(cm);

		conflict4 = getBindingElement(bm, ID_CMD_CONFLICT4);
		assertTrue(conflict4.getModelObject() instanceof ParameterizedCommand);
	}

	public void testRestoreContext() throws Exception {
		final KeyController controller = new KeyController();
		controller.init(getWorkbench());

		final ContextModel cm = controller.getContextModel();
		final ContextElement dialog = (ContextElement) cm
				.getContextIdToElement().get(IContextService.CONTEXT_ID_DIALOG);

		final BindingModel bm = controller.getBindingModel();
		BindingElement activateEditor = getBindingElement(bm,
				ID_ACTIVATE_EDITOR);
		activateEditor.setContext(dialog);

		bm.setSelectedElement(activateEditor);
		bm.restoreBinding(cm);

		activateEditor = getBindingElement(bm, ID_ACTIVATE_EDITOR);
		assertNotNull(activateEditor);
	}

	private void assertChangeEvent(int eventNum, PropertyChangeEvent expected,
			PropertyChangeEvent event) {
		assertEquals("source: " + eventNum, expected.getSource(), event
				.getSource());
		assertEquals("property: " + eventNum, expected.getProperty(), event
				.getProperty());
		assertEquals("old: " + eventNum, expected.getOldValue(), event
				.getOldValue());
		assertEquals("new: " + eventNum, expected.getNewValue(), event
				.getNewValue());
	}

	private void assertChanges(PropertyChangeEvent[] expected, List events) {
		assertEquals("events length", expected.length, events.size());
		for (int i = 0; i < expected.length; i++) {
			assertChangeEvent(i, expected[i], (PropertyChangeEvent) events
					.get(i));
		}
	}

	private BindingElement getBindingElement(BindingModel bm, String bindingId) {
		BindingElement quickAccess = null;
		Iterator i = bm.getBindings().iterator();
		while (i.hasNext()) {
			BindingElement e = (BindingElement) i.next();
			if (e.getId().equals(bindingId)) {
				quickAccess = e;
			}
		}
		return quickAccess;
	}
}
