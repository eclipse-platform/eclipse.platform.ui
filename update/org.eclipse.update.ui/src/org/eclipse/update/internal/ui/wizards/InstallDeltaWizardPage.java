package org.eclipse.update.internal.ui.wizards;

import java.util.*;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.update.configuration.ISessionDelta;
import org.eclipse.update.core.*;
import org.eclipse.update.internal.ui.*;
import org.eclipse.update.internal.ui.model.MissingFeature;
import org.eclipse.update.internal.ui.parts.DefaultContentProvider;
import org.eclipse.update.internal.ui.parts.SWTUtil;

/**
 * @author dejan
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 */
public class InstallDeltaWizardPage extends WizardPage {
	private ISessionDelta[] deltas;
	private static final String KEY_TITLE = "InstallDeltaWizard.title";
	private static final String KEY_DESC = "InstallDeltaWizard.desc";
	private static final String KEY_LABEL = "InstallDeltaWizard.label";
	private static final String KEY_DELETE = "InstallDeltaWizard.delete";
	private CheckboxTreeViewer deltaViewer;
	private Button deleteButton;
	private Image deltaImage;
	private Image featureImage;
	private Hashtable features;
	private ArrayList removed = new ArrayList();

	class DeltaFeature {
		IFeature feature;
		ISessionDelta delta;
		public DeltaFeature(ISessionDelta delta, IFeature feature) {
			this.feature = feature;
			this.delta = delta;
		}
		public String toString() {
			return feature.getLabel()
				+ " ("
				+ feature.getVersionedIdentifier().getVersion().toString()
				+ ")";
		}
	}

	class DeltaContentProvider
		extends DefaultContentProvider
		implements ITreeContentProvider {
		public boolean hasChildren(Object parent) {
			if (parent instanceof ISessionDelta)
				return true;
			return false;
		}
		public Object[] getChildren(Object parent) {
			if (parent instanceof ISessionDelta) {
				return (Object[]) features.get(parent);
			}
			return new Object[0];
		}
		public Object getParent(Object child) {
			if (child instanceof DeltaFeature) {
				return ((DeltaFeature) child).delta;
			}
			return null;
		}
		public Object[] getElements(Object input) {
			return deltas;
		}
	}

	class DeltaLabelProvider extends LabelProvider {
		public String getText(Object obj) {
			if (obj instanceof ISessionDelta) {
				return ((ISessionDelta) obj).getDate().toString();
			}
			return super.getText(obj);
		}
		public Image getImage(Object obj) {
			if (obj instanceof ISessionDelta)
				return deltaImage;
			if (obj instanceof DeltaFeature)
				return featureImage;
			return super.getImage(obj);
		}
	}

	/**
	 * Constructor for InstallDeltaWizardPage.
	 * @param pageName
	 */
	public InstallDeltaWizardPage(ISessionDelta[] deltas) {
		super("installDeltaPage");
		this.deltas = deltas;
		setTitle(UpdateUIPlugin.getResourceString(KEY_TITLE));
		setDescription(UpdateUIPlugin.getResourceString(KEY_DESC));
		deltaImage = UpdateUIPluginImages.DESC_UPDATES_OBJ.createImage();
		featureImage = UpdateUIPluginImages.DESC_FEATURE_OBJ.createImage();
	}

	public void dispose() {
		deltaImage.dispose();
		featureImage.dispose();
		super.dispose();
	}

	private void initializeFeatures() {
		features = new Hashtable();
		for (int i = 0; i < deltas.length; i++) {
			ISessionDelta delta = deltas[i];
			IFeatureReference[] references = delta.getFeatureReferences();
			Object[] dfeatures = new Object[references.length];
			for (int j = 0; j < references.length; j++) {
				IFeatureReference reference = references[j];
				DeltaFeature dfeature = null;
				try {
					IFeature feature = reference.getFeature();
					dfeature = new DeltaFeature(delta, feature);
				} catch (CoreException e) {
					IFeature feature =
						new MissingFeature(
							reference.getSite(),
							reference.getURL());
					dfeature = new DeltaFeature(delta, feature);
				}
				dfeatures[j] = dfeature;
			}
			features.put(delta, dfeatures);
		}
	}

	/**
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		container.setLayout(layout);
		Label label = new Label(container, SWT.NULL);
		label.setText(UpdateUIPlugin.getResourceString(KEY_LABEL));
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan = 2;
		label.setLayoutData(gd);
		deltaViewer = new CheckboxTreeViewer(container, SWT.BORDER);
		deltaViewer.setContentProvider(new DeltaContentProvider());
		deltaViewer.setLabelProvider(new DeltaLabelProvider());
		deltaViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				handleCheckStateChanged(event.getElement(), event.getChecked());
			}
		});
		deltaViewer
			.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent e) {
				updateDeleteButton((IStructuredSelection) e.getSelection());
			}
		});
		deltaViewer.addFilter(new ViewerFilter() {
			public boolean select(Viewer viewer, Object parent, Object child) {
				if (child instanceof ISessionDelta) {
					return !removed.contains(child);
				}
				return true;
			}
		});
		gd = new GridData(GridData.FILL_BOTH);
		deltaViewer.getControl().setLayoutData(gd);

		deleteButton = new Button(container, SWT.PUSH);
		deleteButton.setEnabled(false);
		deleteButton.setText(UpdateUIPlugin.getResourceString(KEY_DELETE));
		gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
		deleteButton.setLayoutData(gd);
		deleteButton.addSelectionListener(new SelectionAdapter() {
			public void selectionChanged(SelectionEvent e) {
				handleDelete();
			}
		});
		SWTUtil.setButtonDimensionHint(deleteButton);

		initializeFeatures();
		deltaViewer.setInput(this);
		setFeaturesGray();
		dialogChanged();
		setControl(container);
	}

	private void updateDeleteButton(IStructuredSelection selection) {
		boolean enable = true;
		for (Iterator iter = selection.iterator(); iter.hasNext();) {
			Object obj = iter.next();
			if (!(obj instanceof ISessionDelta)) {
				enable = false;
				break;
			}
		}
		deleteButton.setEnabled(enable);
	}

	private void handleDelete() {
		IStructuredSelection selection =
			(IStructuredSelection) deltaViewer.getSelection();
		for (Iterator iter = selection.iterator(); iter.hasNext();) {
			Object obj = iter.next();
			if (!(obj instanceof ISessionDelta)) {
				if (!removed.contains(obj)) {
					removed.add(obj);
				}
			}
		}
		deltaViewer.refresh();
	}

	private void setFeaturesGray() {
		if (features == null)
			return;
		ArrayList grayed = new ArrayList();
		for (Enumeration enum = features.elements(); enum.hasMoreElements();) {
			Object[] dfeatures = (Object[]) enum.nextElement();
			for (int i = 0; i < dfeatures.length; i++) {
				grayed.add(dfeatures[i]);
			}
		}
		deltaViewer.setGrayedElements(grayed.toArray());
	}

	private void handleCheckStateChanged(Object obj, boolean checked) {
		if (obj instanceof DeltaFeature) {
			// do not allow it
			deltaViewer.setChecked(obj, !checked);
		}
		if (obj instanceof ISessionDelta) {
			Object[] dfeatures = (Object[]) features.get(obj);
			for (int i = 0; i < dfeatures.length; i++) {
				deltaViewer.setChecked(dfeatures[i], checked);
			}
			dialogChanged();
		}
	}
	private void dialogChanged() {
		ISessionDelta[] deltas = getSelectedDeltas();
		setPageComplete(deltas.length > 0);
	}

	public ISessionDelta[] getRemovedDeltas() {
		return (ISessionDelta[]) removed.toArray(
			new ISessionDelta[removed.size()]);
	}
	public ISessionDelta[] getSelectedDeltas() {
		Object[] checked = deltaViewer.getCheckedElements();
		ArrayList selected = new ArrayList();
		for (int i = 0; i < checked.length; i++) {
			Object obj = checked[i];
			if (obj instanceof ISessionDelta)
				selected.add(obj);
		}
		return (ISessionDelta[]) selected.toArray(
			new ISessionDelta[selected.size()]);
	}
}