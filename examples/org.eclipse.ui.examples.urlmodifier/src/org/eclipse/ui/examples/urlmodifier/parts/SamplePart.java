package org.eclipse.ui.examples.urlmodifier.parts;

import java.util.Arrays;
import java.util.List;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;

import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.IWorkbench;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.widgets.ButtonFactory;
import org.eclipse.jface.widgets.LabelFactory;
import org.eclipse.jface.widgets.TextFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.examples.urlmodifier.IconPacks;
import org.osgi.service.prefs.BackingStoreException;

public class SamplePart {

	private TableViewer tableViewer;

	@Inject
	private MPart part;

	@Inject
	private IWorkbench workbench;

	@PostConstruct
	public void createComposite(Composite parent) {
		parent.setLayout(new GridLayout(1, false));

		String pack = IconPacks.selected();
		LabelFactory.newLabel(SWT.NONE) //
				.text(pack == null ? "Icon pack: none (default icons)" : "Icon pack: " + pack) //
				.create(parent);
		ButtonFactory.newButton(SWT.PUSH) //
				.text(pack == null ? "Use the \"bold\" icon pack and restart" : "Use the default icons and restart") //
				.onSelect(e -> switchPack(pack == null ? IconPacks.BOLD : null)) //
				.create(parent);

		TextFactory.newText(SWT.BORDER) //
				.message("Enter text to mark part as dirty") //
				.onModify(e -> part.setDirty(true)) //
				.layoutData(new GridData(GridData.FILL_HORIZONTAL))//
				.create(parent);

		tableViewer = new TableViewer(parent);

		tableViewer.setContentProvider(ArrayContentProvider.getInstance());
		tableViewer.setInput(createInitialDataModel());
		tableViewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
	}

	private void switchPack(String pack) {
		try {
			IconPacks.select(pack);
		} catch (BackingStoreException e) {
			throw new IllegalStateException(e);
		}
		workbench.restart();
	}

	@Focus
	public void setFocus() {
		tableViewer.getTable().setFocus();
	}

	@Persist
	public void save() {
		part.setDirty(false);
	}

	private List<String> createInitialDataModel() {
		return Arrays.asList("Sample item 1", "Sample item 2", "Sample item 3", "Sample item 4", "Sample item 5");
	}
}