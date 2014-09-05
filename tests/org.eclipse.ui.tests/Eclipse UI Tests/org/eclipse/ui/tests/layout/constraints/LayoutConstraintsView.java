/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Stefan Xenos, IBM Corporation - initial API and implementation
 *     Chris Torrence, ITT Visual Information Solutions - initial API and implementation (bug 51580)
 *******************************************************************************/
package org.eclipse.ui.tests.layout.constraints;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.LayoutConstants;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISizeProvider;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

public class LayoutConstraintsView extends ViewPart implements ISizeProvider {

    private Control control;

    private int minWidth = ISizeProvider.INFINITE;
    private int maxWidth = ISizeProvider.INFINITE;
    private int minHeight = ISizeProvider.INFINITE;
    private int maxHeight = ISizeProvider.INFINITE;
    private int quantizedWidth = ISizeProvider.INFINITE;
    private int quantizedHeight = ISizeProvider.INFINITE;
    private int fixedArea = ISizeProvider.INFINITE;
    private Text minWidthText;
    private Text maxWidthText;
    private Text quantizedWidthText;
    private Text minHeightText;
    private Text maxHeightText;
    private Text quantizedHeightText;
    private Text fixedAreaText;
    private Text sampleImplementation;

    @Override
	public void createPartControl(Composite parent) {
        control = parent;

        Composite buttonBar = new Composite(parent, SWT.NONE);
        {	
            GridDataFactory buttonData = GridDataFactory.fillDefaults().grab(true, false);

            Button applyButton = new Button(buttonBar, SWT.PUSH);
            applyButton.setText("Apply");
            applyButton.addSelectionListener(new SelectionAdapter() {
                /* (non-Javadoc)
                 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
                 */
                @Override
				public void widgetSelected(SelectionEvent e) {
                    applyPressed();
                }
            });
            buttonData.applyTo(applyButton);

            Button clearButton = new Button(buttonBar, SWT.PUSH);
            clearButton.setText("Reset");
            clearButton.addSelectionListener(new SelectionAdapter() {
                /* (non-Javadoc)
                 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
                 */
                @Override
				public void widgetSelected(SelectionEvent e) {
                    minWidthText.setText("");
                    maxWidthText.setText("");
                    quantizedWidthText.setText("");
                    minHeightText.setText("");
                    maxHeightText.setText("");
                    quantizedHeightText.setText("");
                    fixedAreaText.setText("");
                    applyPressed();
                }
            });
            buttonData.applyTo(clearButton);

            Button newViewButton = new Button(buttonBar, SWT.PUSH);
            newViewButton.setText("New View");
            newViewButton.addSelectionListener(new SelectionAdapter() {
                /* (non-Javadoc)
                 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
                 */
                @Override
				public void widgetSelected(SelectionEvent e) {
                    try {
                        getSite().getPage().showView("org.eclipse.ui.tests.layout.constraints.LayoutConstraintsView", 
                                "" + System.currentTimeMillis(), IWorkbenchPage.VIEW_ACTIVATE);
                    } catch (PartInitException e1) {
                        MessageDialog.openError(getSite().getShell(), "Error opening view", "Unable to open view.");
                    }
                }
            });
            buttonData.applyTo(newViewButton);

            GridLayoutFactory.fillDefaults().equalWidth(true).numColumns(3).applyTo(buttonBar);
        }
        GridDataFactory.fillDefaults().grab(true, false).span(2,1).applyTo(buttonBar);

        new Label(parent, SWT.NONE).setText("Min Width"); 
        minWidthText = createText(parent);

        new Label(parent, SWT.NONE).setText("Max Width (blank = unbounded)"); 
        maxWidthText = createText(parent);

        new Label(parent, SWT.NONE).setText("Quantized Width (blank = none)"); 
        quantizedWidthText = createText(parent);

        new Label(parent, SWT.NONE).setText("Min Height"); 
        minHeightText = createText(parent);

        new Label(parent, SWT.NONE).setText("Max Height (blank = unbounded)");
        maxHeightText = createText(parent);

        new Label(parent, SWT.NONE).setText("Quantized Height (blank = none)"); 
        quantizedHeightText = createText(parent);		

        new Label(parent, SWT.NONE).setText("Fixed Area (blank = none"); 
        fixedAreaText = createText(parent);

        sampleImplementation = new Text(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        sampleImplementation.setFont(JFaceResources.getFont(JFaceResources.TEXT_FONT));
        sampleImplementation.setTabs(4);
        GridDataFactory.fillDefaults().grab(true, true).span(2,1).applyTo(sampleImplementation);

        GridLayoutFactory.fillDefaults().numColumns(2).margins(LayoutConstants.getMargins()).generateLayout(parent);

        applyPressed();

    }

    /**
     * 
     */
    protected void applyPressed() {
        // Copy the values from the text boxes
        minWidth = getInt(minWidthText);
        maxWidth = getInt(maxWidthText);
        quantizedWidth = getInt(quantizedWidthText);
        minHeight = getInt(minHeightText);
        maxHeight = getInt(maxHeightText);
        quantizedHeight = getInt(quantizedHeightText);
        fixedArea = getInt(fixedAreaText);

        StringBuffer result = new StringBuffer();
        result.append("// Sample implementation: Make sure your ViewPart adapts to ISizeProvider.\n");
        result.append("// Then implement the following two methods.\n\n");
        sampleImplementation.setText(result.toString() +
                getSizeFlagsString() + computePreferredSizeString());

        // Trigger a workbench layout
        updateLayout();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.ISizeProvider#getSizeFlags(boolean)
     */
    @Override
	public int getSizeFlags(boolean width) {
        int flags = 0;
        if (width) {
            if (minWidth != ISizeProvider.INFINITE) {
                flags |= SWT.MIN;
            }
            if (maxWidth != ISizeProvider.INFINITE) {
                flags |= SWT.MAX;
            }
            if (quantizedWidth != ISizeProvider.INFINITE || fixedArea != ISizeProvider.INFINITE) {
                flags |= SWT.FILL;
            }
            if (fixedArea != ISizeProvider.INFINITE) {
                flags |= SWT.WRAP;
            }
        } else {
            if (minHeight != ISizeProvider.INFINITE) {
                flags |= SWT.MIN;
            }
            if (maxHeight != ISizeProvider.INFINITE) {
                flags |= SWT.MAX;
            }
            if (quantizedHeight != ISizeProvider.INFINITE || fixedArea != ISizeProvider.INFINITE) {
                flags |= SWT.FILL;
            }
            if (fixedArea != ISizeProvider.INFINITE) {
                flags |= SWT.WRAP;
            }			
        }

        return flags;
    }

    public String getSizeFlagsString() {
        StringBuffer result = new StringBuffer();
        result.append("/* (non-Javadoc)\n");
        result.append(" * @see org.eclipse.ui.ISizeProvider#getSizeFlags(boolean)\n");
        result.append(" */\n");
        result.append("public int getSizeFlags(boolean width) {\n");
        result.append("\tint flags = 0;\n");
        result.append("\tif (width) {\n");
        if (minWidth != ISizeProvider.INFINITE) {
            result.append("\t\tflags |= SWT.MIN;\n");
        }
        if (maxWidth != ISizeProvider.INFINITE) {
            result.append("\t\tflags |= SWT.MAX;\n");
        }
        if (quantizedWidth != ISizeProvider.INFINITE || fixedArea != ISizeProvider.INFINITE) {
            result.append("\t\tflags |= SWT.FILL;\n");
        }
        if (fixedArea != ISizeProvider.INFINITE) {
            result.append("\t\tflags |= SWT.WRAP;\n");
        }
        result.append("\t} else {\n");
        if (minHeight != ISizeProvider.INFINITE) {
            result.append("\t\tflags |= SWT.MIN;\n");
        }
        if (maxHeight != ISizeProvider.INFINITE) {
            result.append("\t\tflags |= SWT.MAX;\n");
        }
        if (quantizedHeight != ISizeProvider.INFINITE || fixedArea != ISizeProvider.INFINITE) {
            result.append("\t\tflags |= SWT.FILL;\n");
        }
        if (fixedArea != ISizeProvider.INFINITE) {
            result.append("\t\tflags |= SWT.WRAP;\n");
        }     
        result.append("\t}\n");
        result.append("\treturn flags;\n");
        result.append("}\n\n");
        return result.toString();
    }

    /**
     * @param minWidth2
     * @return
     */
    private int getInt(Text text) {
        if (text.getText().equals("")) {
            return ISizeProvider.INFINITE;
        }

        try {
            return Integer.parseInt(text.getText());
        } catch (NumberFormatException e) {
            return ISizeProvider.INFINITE;
        }
    }

    /**
     * 
     */
    protected void updateLayout() {
        firePropertyChange(IWorkbenchPartConstants.PROP_PREFERRED_SIZE);
    }

    /**
     * @param parent
     */
    private Text createText(Composite parent) {
        return new Text(parent, SWT.BORDER);		
    }

    @Override
	public void setFocus() {
        control.setFocus();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.ISizeProvider#computePreferredSize(boolean, int, int, int)
     */
    @Override
	public int computePreferredSize(boolean width, int availableParallel,
            int availablePerpendicular, int preferredResult) {

        int result = preferredResult;

        if (fixedArea != ISizeProvider.INFINITE) {
            // Try to maintain a fixed area
            result = (availablePerpendicular != 0) ? fixedArea / availablePerpendicular : 0;
            if (result < 30) result = 30;
        }

        if (width) {
            if (quantizedWidth != ISizeProvider.INFINITE && quantizedWidth != 0) {
                // Jump to the nearest multiple of the quantized size
                result = Math.min(result + quantizedWidth/2, availableParallel);
                result = result - (result % quantizedWidth);
            }
            if (minWidth != ISizeProvider.INFINITE) {
                // Ensure we go no smaller than the minimum size
                if (result < minWidth) result = minWidth;
            }
            if (maxWidth != ISizeProvider.INFINITE) {
                // Ensure we go no larger than the maximum size
                if (result > maxWidth) result = maxWidth;
            }
        } else {
            // Jump to the nearest multiple of the quantized size
            if (quantizedHeight != ISizeProvider.INFINITE && quantizedHeight != 0) {
                result = Math.min(result + quantizedHeight/2, availableParallel);
                result = result - (result % quantizedHeight);
            }
            if (minHeight != ISizeProvider.INFINITE) {
                // Ensure we go no smaller than the minimum size
                if (result < minHeight) result = minHeight;
            }
            if (maxHeight != ISizeProvider.INFINITE) {
                // Ensure we go no larger than the maximum size
                if (result > maxHeight) result = maxHeight;
            }
        }

        // Ensure that we do not use more than the available space
        if (result > availableParallel) result = availableParallel;
        if (result < 0) result = 0;
        return result;
    }

    private String computePreferredSizeString() {
        StringBuffer result = new StringBuffer();
        result.append("/* (non-Javadoc)\n");
        result.append(" * @see org.eclipse.ui.ISizeProvider#computePreferredSize(boolean, int, int, int)\n");
        result.append(" */\n");
        result.append("public int computePreferredSize(boolean width, int availableParallel,\n");
        result.append("\tint availablePerpendicular, int preferredResult) {\n");
        result.append("\tint result = preferredResult;\n");
        if (fixedArea != ISizeProvider.INFINITE) {
            result.append("\t// Try to maintain a fixed area\n");
            result.append("\tresult = (availablePerpendicular != 0) ? " + fixedArea + "/availablePerpendicular : 0;\n");
            result.append("\tif (result < 30) result = 30;\n");
        }
        result.append("\tif (width) {\n");
        if (quantizedWidth != ISizeProvider.INFINITE && quantizedWidth != 0) {
            result.append("\t\t// Jump to the nearest multiple of the quantized size\n");
            result.append("\t\tresult = Math.min(result + " + quantizedWidth + "/2, availableParallel);\n");
            result.append("\t\tresult = result - (result % " + quantizedWidth + ");\n");
        }
        if (minWidth != ISizeProvider.INFINITE) {
            result.append("\t\t// Ensure we go no smaller than the minimum size\n");
            result.append("\t\tif (result < " + minWidth + ") result = " + minWidth + ";\n");
        }
        if (maxWidth != ISizeProvider.INFINITE) {
            result.append("\t\t// Ensure we go no larger than the maximum size\n");
            result.append("\t\tif (result > " + maxWidth + ") result = " + maxWidth + ";\n");
        }
        result.append("\t} else {\n");
        if (quantizedHeight != ISizeProvider.INFINITE && quantizedHeight != 0) {
            result.append("\t\t// Jump to the nearest multiple of the quantized size\n");
            result.append("\t\tresult = Math.min(result + " + quantizedHeight + "/2, availableParallel);\n");
            result.append("\t\tresult = result - (result % " + quantizedHeight + ");\n");
        }
        if (minHeight != ISizeProvider.INFINITE) {
            result.append("\t\t// Ensure we go no smaller than the minimum size\n");
            result.append("\t\tif (result < " + minHeight + ") result = " + minHeight + ";\n");
        }
        if (maxHeight != ISizeProvider.INFINITE) {
            result.append("\t\t// Ensure we go no larger than the maximum size\n");
            result.append("\t\tif (result > " + maxHeight + ") result = " + maxHeight + ";\n");
        }
        result.append("\t}\n");
        result.append("\t// Ensure that we do not use more than the available space\n");
        result.append("\tif (result > availableParallel) result = availableParallel;\n");
        result.append("\tif (result < 0) result = 0;\n");
        result.append("\treturn result;\n");
        result.append("}\n");
        return result.toString();
    }

}
