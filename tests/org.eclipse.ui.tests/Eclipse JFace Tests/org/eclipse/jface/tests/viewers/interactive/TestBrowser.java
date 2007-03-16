/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.tests.viewers.interactive;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.tests.viewers.TestElement;
import org.eclipse.jface.tests.viewers.TestModelChange;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public abstract class TestBrowser extends ApplicationWindow {
    TestElement fInput;

    Viewer fViewer;

    Action fChangeLabelAction;

    Action fChangeInputLabelAction;

    Action fChangeChildLabelAction;

    Action fReloadAction;

    Action fReloadActionLarge;

    Action fReloadActionFlat;

    Action fDeleteAction;

    Action fDeleteChildrenAction;

    Action fDeleteSomeChildrenAction;

    Action fDeleteSiblingsAction;

    Action fFlushInputAction;

    Action fAddElementAction;

    Action fAddSiblingAction;

    Action fAddSiblingRevealAction;

    Action fAddSiblingSelectAction;

    Action fAddChildAction;

    Action fAddChildRevealAction;

    Action fAddChildSelectAction;

    Action fWorldChangedAction;

    Action fSetLabelProvider;

    Action fAddFilterAction;

    Action fResetFilters;

    Action fSetSorter;

    Action fResetSorter;

    Action fClearSelection;

    int fPanes = 1;

    public TestBrowser() {
        super(null);
        addMenuBar();
    }

    /* (non-Javadoc)
     * Method declared on Window.
     */
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText("Test Browser");
    }

    protected void createActions() {
        fChangeLabelAction = new ChangeLabelAction("Change Label", this);
        fChangeChildLabelAction = new ChangeChildLabelAction(
                "Change Child Label", this);
        //	fChangeInputLabelAction =
        //		new ChangeInputLabelAction("Change Input Label", this);

        fReloadAction = new CreateModelAction("Reload Test Data (small)", this,
                3, 10);
        fReloadActionLarge = new CreateModelAction("Reload Test Data (large)",
                this, 3, 33);
        fReloadActionFlat = new CreateModelAction("Reload Test Data (flat)",
                this, 1, 2000);

        fDeleteAction = new DeleteAction("Delete", this);
        fDeleteChildrenAction = new DeleteChildrenAction("Delete Children",
                this, true);
        fDeleteSomeChildrenAction = new DeleteChildrenAction(
                "Delete Odd Children", this, false);
        fDeleteSiblingsAction = new DeleteSiblingsAction("Delete Siblings",
                this, true);

        fFlushInputAction = new FlushInputAction("Flush Input", this);

        fAddElementAction = new AddElementAction("Add Element to Input", this);
        fAddSiblingAction = new AddSiblingAction("Add Sibling", this);
        fAddSiblingRevealAction = new AddSiblingAction(
                "Add Sibling and Reveal", this, TestModelChange.INSERT
                        | TestModelChange.REVEAL);
        fAddSiblingSelectAction = new AddSiblingAction(
                "Add Sibling and Select", this, TestModelChange.INSERT
                        | TestModelChange.REVEAL | TestModelChange.SELECT);
        fAddChildAction = new AddChildAction("Add Child", this);
        fAddChildRevealAction = new AddChildAction("Add Child and Reveal",
                this, TestModelChange.INSERT | TestModelChange.REVEAL);
        fAddChildSelectAction = new AddChildAction("Add Child and Select",
                this, TestModelChange.INSERT | TestModelChange.REVEAL
                        | TestModelChange.SELECT);

        fWorldChangedAction = new WorldChangedAction("World Changed", this);

        fSetLabelProvider = new SetLabelProviderAction(
                "Set Custom Label Provider", this);

        fAddFilterAction = new AddFilterAction("Add Filter", this);
        fResetFilters = new ResetFilterAction("Reset All Filters", this);

        fSetSorter = new SetSorterAction("Set Sorter", this);
        fResetSorter = new ResetSorterAction("Reset Sorter", this);

        fClearSelection = new ClearSelectionAction("Clear Selection", this);
    }

    protected Control createContents(Composite parent) {
        ViewForm form = new ViewForm(parent, SWT.NONE);
        CLabel label = new CLabel(form, SWT.NONE);
        form.setTopLeft(label);
        Object input = getInput();
        label.setText(input.toString());
        if (fPanes == 1) {
            Viewer viewer = createViewer(form);
            form.setContent(viewer.getControl());
            fViewer = viewer;
            setInput((TestElement) input);
        } else if (fPanes == 2) {
            SashForm sashForm = new SashForm(form, SWT.VERTICAL);
            form.setContent(sashForm);
            Viewer viewer = createViewer(sashForm);
            fViewer = viewer;
            viewer.setInput(input);
            viewer = createViewer(sashForm);
            viewer.setInput(input);
        }
        createActions();
        fillMenuBar(getMenuBarManager());
        viewerFillMenuBar(getMenuBarManager());
        getMenuBarManager().updateAll(false);
        return form;
    }

    public abstract Viewer createViewer(Composite parent);

    protected void fillMenuBar(MenuManager mgr) {

        MenuManager setupMenu = new MenuManager("Setup", "Setup");
        mgr.add(setupMenu);
        setupMenu.add(fReloadAction);
        setupMenu.add(fReloadActionLarge);
        setupMenu.add(fReloadActionFlat);
        setupMenu.add(new Separator());
        setupMenu.add(fFlushInputAction);
        setupMenu.add(new Separator());
        setupMenu.add(fSetLabelProvider);
        setupMenu.add(new Separator());
        setupMenu.add(fAddFilterAction);
        setupMenu.add(fResetFilters);
        setupMenu.add(new Separator());
        setupMenu.add(fSetSorter);
        setupMenu.add(fResetSorter);

        MenuManager testMenu = new MenuManager("Tests", "Tests");
        mgr.add(testMenu);
        testMenu.add(fChangeLabelAction);
        testMenu.add(fChangeChildLabelAction);
        //	testMenu.add(fChangeInputLabelAction);
        testMenu.add(new Separator());

        testMenu.add(fDeleteAction);
        testMenu.add(fDeleteChildrenAction);
        testMenu.add(fDeleteSomeChildrenAction);
        testMenu.add(fDeleteSiblingsAction);
        testMenu.add(new Separator());

        testMenu.add(fAddElementAction);
        testMenu.add(new Separator());

        testMenu.add(fAddSiblingAction);
        testMenu.add(fAddSiblingRevealAction);
        testMenu.add(fAddSiblingSelectAction);
        testMenu.add(new Separator());

        testMenu.add(fAddChildAction);
        testMenu.add(fAddChildRevealAction);
        testMenu.add(fAddChildSelectAction);
        testMenu.add(new Separator());

        testMenu.add(fClearSelection);
        testMenu.add(new Separator());

        testMenu.add(fWorldChangedAction);
        //	((TestTree)this).testTreeFillMenuBar(testMenu);
    }

    public TestElement getInput() {
        return fInput;
    }

    public Viewer getViewer() {
        return fViewer;
    }

    public Composite getViewerContainer() {
        return null;
    }

    public void open(TestElement input) {
    	setInput(input);
        super.open();
    }

    public void setInput(TestElement input) {
        fInput = input;
        if (getViewer() != null)
            getViewer().setInput(input);
    }

    public void show2Panes() {
        fPanes = 2;
    }

    protected abstract void viewerFillMenuBar(MenuManager mgr);
}
