package org.eclipse.update.ui.forms.internal;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.graphics.*;

public interface IForm {
public void commitChanges(boolean onSave);
public Control createControl(Composite parent);
public void dispose();
public void doGlobalAction(String actionId);
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