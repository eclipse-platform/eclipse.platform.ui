/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.tests.viewers;

import java.util.Vector;

public class TestElement implements Cloneable {
    TestModel fModel;

    TestElement fContainer;

    String fSomeName;

    String fId;

    Vector fChildren = new Vector();

    boolean fIsDeleted = false;

    public static final String P_SOMENAME = "org.eclipse.jface.viewertest.name";

    public static final String P_COLUMN_1 = "org.eclipse.jface.viewertest.column1";

    public TestElement(TestModel model, TestElement container) {
        fModel = model;
        fContainer = container;
        int p = 0;
        TestElement lastSibling = container.getLastChild();
        if (lastSibling != null) {
            p = lastSibling.childId() + 1;
        }
        fId = container.getID() + "-" + p;
    }

    public TestElement(TestModel model, TestElement container, int level,
            int position) {
        fModel = model;
        fContainer = container;
        if (container != null)
            fId = container.getID() + "-" + position;
        else
            fId = Integer.toString(position);
        fSomeName = "name-" + position;

        if (level < model.getNumLevels()) {
            for (int i = 0; i < model.getNumChildren(); i++) {
                fChildren.add(new TestElement(model, this, level + 1, i));
            }
        }
    }

    public TestElement addChild(int event) {
        TestElement element = new TestElement(fModel, this);
        element.fSomeName = "added";
        addChild(element, new TestModelChange(event, this, element));
        return element;
    }

    public TestElement addChild(TestElement element, TestModelChange change) {
        fChildren.add(element);
        fModel.fireModelChanged(change);
        return element;
    }

    public void addChildren(TestElement[] elements, TestModelChange change) {
        for (int i = 0; i < elements.length; i++)
            fChildren.add(elements[i]);
        fModel.fireModelChanged(change);
    }

    public TestElement[] addChildren(int event) {
        TestElement elements[] = new TestElement[] {
                new TestElement(fModel, this), new TestElement(fModel, this) };

        elements[0].fSomeName = "added1";
        elements[1].fSomeName = "added2";
        // change the id of the second element, otherwise there will be
        // two equal elements under the same parent
        elements[1].fId += "madeUnique";
        addChildren(elements, new TestModelChange(event, this, elements));
        return elements;
    }

    public TestElement basicAddChild() {
        TestElement element = new TestElement(fModel, this);
        element.fSomeName = "added";
        fChildren.add(element);
        return element;
    }

    public void basicDeleteChild(TestElement element) {
        fChildren.remove(element);
        element.fIsDeleted = true;
    }

    private int childId() {
        String id = fId.substring(fId.lastIndexOf("-") + 1);
        return Integer.parseInt(id);
    }

    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new Error(); // should not happen
        }
    }

    static public TestElement createModel(int numLevels, int numChildren) {
        return new TestElement(new TestModel(numLevels, numChildren), null, 0,
                0);
    }

    public void deleteChild(TestElement element) {
        deleteChild(element, new TestModelChange(TestModelChange.REMOVE, this,
                element));
    }

    public void deleteChild(TestElement element, TestModelChange change) {
        basicDeleteChild(element);
        fModel.fireModelChanged(change);
    }

    public void deleteChildren() {
        for (int i = fChildren.size() - 1; i >= 0; i--) {
            TestElement te = (TestElement) fChildren.elementAt(i);
            fChildren.remove(te);
            te.fIsDeleted = true;
        }
        fModel.fireModelChanged(new TestModelChange(
                TestModelChange.STRUCTURE_CHANGE, this));
    }

    public void deleteSomeChildren() {
        for (int i = fChildren.size() - 1; i >= 0; i -= 2) {
            TestElement te = (TestElement) fChildren.elementAt(i);
            fChildren.remove(te);
            te.fIsDeleted = true;
        }
        fModel.fireModelChanged(new TestModelChange(
                TestModelChange.STRUCTURE_CHANGE, this));
    }

    public boolean equals(Object arg) {
        if (!(arg instanceof TestElement))
            return false;
        TestElement element = (TestElement) arg;
        return element.fId.equals(fId);
    }

    public TestElement getChildAt(int i) {
        return (TestElement) fChildren.elementAt(i);
    }

    public int getChildCount() {
        return fChildren.size();
    }
    
    /**
     * Get the children of the receiver.
     * @return TestElement[]
     */
    public TestElement[] getChildren(){
    	TestElement[] result = new TestElement[fChildren.size()];
    	fChildren.toArray(result);
    	return result;    	
    }

    public TestElement getContainer() {
        return fContainer;
    }

    public TestElement getFirstChild() {
        if (fChildren.size() > 0)
            return (TestElement) fChildren.elementAt(0);
        return null;
    }

    public String getID() {
        return fId;
    }

    public String getLabel() {
        return fSomeName;
    }

    public TestElement getLastChild() {
        int size = fChildren.size();
        if (size > 0)
            return (TestElement) fChildren.elementAt(size - 1);
        return null;
    }

    public TestModel getModel() {
        return fModel;
    }

    public int hashCode() {
        return fId.hashCode();
    }

    public boolean isDeleted() {
        return fIsDeleted;
    }

    public void setLabel(String label) {
        fSomeName = label;
        fModel.fireModelChanged(new TestModelChange(
                TestModelChange.NON_STRUCTURE_CHANGE, this));
    }

    public boolean testDeleted() {
        if (fIsDeleted)
            return true;
        if (fContainer != null)
            return fContainer.testDeleted();
        return false;
    }

    public String toString() {
        return getID() + " " + getLabel();
    }
}
