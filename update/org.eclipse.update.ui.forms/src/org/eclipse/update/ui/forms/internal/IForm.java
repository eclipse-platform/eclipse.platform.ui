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
package org.eclipse.update.ui.forms.internal;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.graphics.*;

public interface IForm {
public void commitChanges(boolean onSave);
public Control createControl(Composite parent);
public void dispose();
public boolean doGlobalAction(String actionId);
public void expandTo(Object object);
public Control getControl();
public FormWidgetFactory getFactory();
public Color getHeadingBackground();
public Color getHeadingForeground();
public Image getHeadingImage();
public String getHeadingText();
public void initialize(Object model);
public boolean isHeadingVisible();
public void registerSection(FormSection section);
public void setFocus();
public void setHeadingBackground(Color newHeadingBackground);
public void setHeadingForeground(Color newHeadingForeground);
public void setHeadingImage(Image headingImage);
public void setHeadingVisible(boolean newHeadingVisible);
public void setHeadingText(String heading);
public void update();
}
