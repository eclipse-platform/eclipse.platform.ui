package org.eclipse.ui.internal.quickaccess;

import javax.annotation.PostConstruct;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class SearchField {
	Shell shell;

	@PostConstruct
	void createWidget(final Composite parent) {
		// borderColor = new Color(parent.getDisplay(), 170, 176, 191);
		final Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout());
		final Text text = new Text(comp, SWT.SEARCH | SWT.ICON_SEARCH);
		GridDataFactory.fillDefaults().hint(100, SWT.DEFAULT).applyTo(text);
		text.setMessage(QuickAccessMessages.QuickAccess_EnterSearch);

		QuickAccessProvider[] providers = new QuickAccessProvider[] { new EditorProvider(),
				new ViewProvider(), new PerspectiveProvider(), new CommandProvider(),
				new ActionProvider(), new WizardProvider(), new PreferenceProvider(),
				new PropertiesProvider() };
		QuickAccessContents quickAccessContents = new QuickAccessContents(providers) {
			void updateFeedback(boolean filterTextEmpty, boolean showAllMatches) {
			}

			void doClose() {
			}

			QuickAccessElement getPerfectMatch(String filter) {
				return null;
			}

			void handleElementSelected(String string, Object selectedElement) {
				if (selectedElement instanceof QuickAccessElement) {
					QuickAccessElement element = (QuickAccessElement) selectedElement;
					text.setText(""); //$NON-NLS-1$
					element.execute();
				}
			}
		};
		quickAccessContents.hookFilterText(text);
		shell = new Shell(parent.getShell(), SWT.RESIZE | SWT.ON_TOP);
		shell.setBackground(shell.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		GridLayoutFactory.fillDefaults().applyTo(shell);
		quickAccessContents.createTable(shell, Window.getDefaultOrientation());
		text.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				boolean wasVisible = shell.getVisible();
				boolean nowVisible = !text.getText().isEmpty();
				if (!wasVisible && nowVisible) {
					Rectangle tempBounds = comp.getBounds();
					Rectangle compBounds = e.display.map(comp, null, tempBounds);
					shell.setBounds(compBounds.x, compBounds.y + compBounds.height,
							Math.max(350, compBounds.width), 250);
					shell.layout();
				}
				shell.setVisible(nowVisible);
			}
		});

	}
}
