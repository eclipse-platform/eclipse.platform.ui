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
package org.eclipse.ui.internal.intro.impl.presentations;

import org.eclipse.core.runtime.*;
import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.internal.intro.impl.*;
import org.eclipse.ui.internal.intro.impl.model.*;
import org.eclipse.ui.internal.intro.impl.util.*;

/**
 * This is an Text based implementation of an Intro Part. It simply walks the
 * model and prints the content of pages. It is used for debugging.
 */
public class TextIntroPartImplementation extends
        AbstractIntroPartImplementation {


    public void createPartControl(Composite container) {
        Text text = new Text(container, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        IntroModelRoot model = IntroPlugin.getDefault().getIntroModelRoot();
        IntroModelSerializer serializer = new IntroModelSerializer(model);
        text.setText(serializer.toString());
        addToolBarActions();
    }

    protected void updateNavigationActionsState() {
    }


    public void setFocus() {

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.internal.intro.impl.model.AbstractIntroPartImplementation#navigateBackward()
     */
    public boolean navigateBackward() {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.internal.intro.impl.model.AbstractIntroPartImplementation#navigateForward()
     */
    public boolean navigateForward() {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.internal.intro.impl.model.AbstractIntroPartImplementation#handleRegistryChanged(org.eclipse.core.runtime.IRegistryChangeEvent)
     */
    protected void handleRegistryChanged(IRegistryChangeEvent event) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.internal.intro.impl.model.AbstractIntroPartImplementation#navigateHome()
     */
    public boolean navigateHome() {
        return false;
    }


}