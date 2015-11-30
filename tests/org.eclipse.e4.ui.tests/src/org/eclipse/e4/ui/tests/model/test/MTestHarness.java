/**
 * <copyright>
 * </copyright>
 *
 * $Id: MTestHarness.java,v 1.2 2010, 2011/04/16 17:28:39 pwebster Exp $
 */
package org.eclipse.e4.ui.tests.model.test;

import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.MContribution;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.commands.MParameter;
import org.eclipse.e4.ui.model.application.ui.MContext;
import org.eclipse.e4.ui.model.application.ui.MDirtyable;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MInput;
import org.eclipse.e4.ui.model.application.ui.MSnippetContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.MUILabel;

/**
 * <!-- begin-user-doc --> A representation of the model object '
 * <em><b>Harness</b></em>'. <!-- end-user-doc -->
 *
 *
 * @see org.eclipse.e4.ui.tests.model.test.MTestPackage#getTestHarness()
 * @model
 * @generated
 */
public interface MTestHarness extends MApplicationElement, MCommand, MContext, MContribution, MElementContainer<MUIElement>, MParameter, MInput, MUILabel, MDirtyable, MSnippetContainer {
} // MTestHarness
