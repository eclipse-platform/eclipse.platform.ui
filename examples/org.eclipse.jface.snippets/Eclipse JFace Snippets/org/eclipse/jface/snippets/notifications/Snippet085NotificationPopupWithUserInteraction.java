package org.eclipse.jface.snippets.notifications;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.notifications.NotificationPopup;
import org.eclipse.jface.widgets.WidgetFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class Snippet085NotificationPopupWithUserInteraction {

    public static void main(String[] args) {
        Display display = new Display();
        Shell shell = new Shell(display); // Create the shell

        // Set the size of the shell
        shell.setSize(400, 200);
        
        // Create content for the notification popup
        Composite contentComposite = new Composite(shell, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginWidth = 10; // Margin width
        layout.marginHeight = 10; // Margin height
        contentComposite.setLayout(layout);

        // Create a bold label with wrapped text
        Label firstLine = new Label(contentComposite, SWT.WRAP);
        firstLine.setText("It is recommended that you");
        Font boldFont = new Font(display, "Arial", 10, SWT.BOLD);
        firstLine.setFont(boldFont);
        // Create a bold label with wrapped text
        Label secondLine = new Label(contentComposite, SWT.WRAP);
        secondLine.setText("update your configuration");
        secondLine.setFont(boldFont);
        
        
        // Create a button that will show the confirmation dialog when clicked
        Button button = new Button(contentComposite, SWT.PUSH);
        button.setText("Confirm");
        
        button.addListener(SWT.Selection, event -> {
            MessageDialog.openConfirm(shell, "Confirmation", "Button was pressed!");
        });

        // Set GridData for button to align properly in the layout
        GridData gridData = new GridData(SWT.LEFT, SWT.CENTER, false, false);
        button.setLayoutData(gridData);

        // Create the notification popup
        NotificationPopup.forDisplay(display)
            .content(composite -> {
                contentComposite.setParent(composite);
                return composite;
            })
            .title(composite -> WidgetFactory.label(SWT.NONE).text("System update!!!").create(composite), true)
            .open();

        shell.open(); // Open the shell
        
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }

        boldFont.dispose(); // Dispose of the font when done
        display.dispose();
    }
}
