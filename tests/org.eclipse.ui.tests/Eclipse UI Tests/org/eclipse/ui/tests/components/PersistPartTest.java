/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.components;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.components.FactoryMap;
import org.eclipse.ui.part.Part;
import org.eclipse.ui.tests.result.AbstractTestLogger;

/**
 * @since 3.1
 */
public class PersistPartTest extends PartTest {
    /**
     * @param testName
     * @param partBuilder
     */
    public PersistPartTest(AbstractTestLogger log, IPartBuilder partBuilder) {
        super("persist", log, partBuilder);
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#runTest()
     */
    public String performTest() throws Throwable {
        // Create a part, persist it to a memento, then destroy it
        Shell shell1 = createShell();
        Part part1 = createPart(shell1);
        IMemento theMemento = XMLMemento.createWriteRoot("part");
        part1.saveState(theMemento);
        shell1.dispose();
        destroyPart(part1);

        // Create another part of the same type, and load it from the persisted memento
        Shell shell2 = createShell();
        Part part2 = createPart(shell2, new FactoryMap(), theMemento);
        shell2.dispose();
        destroyPart(part2);
        
        return "";
    }

}
