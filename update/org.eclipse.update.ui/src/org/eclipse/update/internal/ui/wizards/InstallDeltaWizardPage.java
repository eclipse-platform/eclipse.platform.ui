package org.eclipse.update.internal.ui.wizards;

import java.util.*;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.update.configuration.ISessionDelta;
import org.eclipse.update.core.*;
import org.eclipse.update.internal.ui.*;
import org.eclipse.update.internal.ui.forms.ActivityConstraints;
import org.eclipse.update.internal.ui.model.MissingFeature;
import org.eclipse.update.internal.ui.parts.DefaultContentProvider;
import org.eclipse.update.internal.ui.parts.OverlayIcon;
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
	private static final String KEY_ERRORS = "InstallDeltaWizard.errors";
	private CheckboxTreeViewer deltaViewer;
	private Button deleteButton;
	private Button errorsButton;
	private Image deltaImage;
	private Image errorDeltaImage;
	private Image featureImage;
	private Hashtable features;
	private ArrayList removed = new ArrayList();
	private Hashtable statusTable;

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
				return Utilities.format(((ISessionDelta) obj).getDate());
			}
			return super.getText(obj);
		}
		public Image getImage(Object obj) {
			if (obj instanceof ISessionDelta) {
				if (statusTable.get(obj) != null)
					return errorDeltaImage;
				else
					return deltaImage;
			}
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
		ImageDescriptor desc =
			new OverlayIcon(
				UpdateUIPluginImages.DESC_UPDATES_OBJ,
				new ImageDescriptor[][] { {
			}, {
			}, {
				UpdateUIPluginImages.DESC_ERROR_CO }
		});
		errorDeltaImage = desc.createImage();
		featureImage = UpdateUIPluginImages.DESC_FEATURE_OBJ.createImage();
		initializeStatusTable();
	}

	public void dispose() {
		errorDeltaImage.dispose();
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
				updateButtons((IStructuredSelection) e.getSelection());
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

		Composite buttonContainer = new Composite(container, SWT.NULL);
		layout = new GridLayout();
		layout.marginWidth = layout.marginHeight = 0;
		gd = new GridData(GridData.FILL_VERTICAL);
		buttonContainer.setLayoutData(gd);
		buttonContainer.setLayout(layout);

		deleteButton = new Button(buttonContainer, SWT.PUSH);
		deleteButton.setEnabled(false);
		deleteButton.setText(UpdateUIPlugin.getResourceString(KEY_DELETE));
		gd =
			new GridData(
				GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
		deleteButton.setLayoutData(gd);
		deleteButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleDelete();
			}
		});
		SWTUtil.setButtonDimensionHint(deleteButton);

		errorsButton = new Button(buttonContainer, SWT.PUSH);
		errorsButton.setEnabled(false);
		errorsButton.setText(UpdateUIPlugin.getResourceString(KEY_ERRORS));
		gd =
			new GridData(
				GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
		errorsButton.setLayoutData(gd);
		errorsButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleShowErrors();
			}
		});
		SWTUtil.setButtonDimensionHint(errorsButton);

		initializeFeatures();
		deltaViewer.setInput(this);
		setFeaturesGray();
		dialogChanged();
		WorkbenchHelp.setHelp(container, "org.eclipse.update.ui.InstallDeltaWizardPage");
		setControl(container);
	}

	private void updateButtons(IStructuredSelection selection) {
		boolean enableShowErrors = false;
		boolean enableDelete = selection.size() > 0;

		if (selection.size() == 1) {
			Object obj = selection.getFirstElement();
			if (obj instanceof ISessionDelta) {
				IStatus status = (IStatus) statusTable.get(obj);
				enableShowErrors = status != null;
			}
		}
		if (enableDelete) {
			for (Iterator iter = selection.iterator(); iter.hasNext();) {
				Object obj = iter.next();
				if (!(obj instanceof ISessionDelta)) {
					enableDelete = false;
					break;
				}
			}
		}
		deleteButton.setEnabled(enableDelete);
		errorsButton.setEnabled(enableShowErrors);
	}

	private void handleDelete() {
		IStructuredSelection selection =
			(IStructuredSelection) deltaViewer.getSelection();
		for (Iterator iter = selection.iterator(); iter.hasNext();) {
			Object obj = iter.next();
			if (obj instanceof ISessionDelta) {
				if (!removed.contains(obj)) {
					removed.add(obj);
				}
			}
		}
		deltaViewer.refresh();
		dialogChanged();
	}

	private void handleShowErrors() {
		IStructuredSelection sel =
			(IStructuredSelection) deltaViewer.getSelection();
		ISessionDelta delta = (ISessionDelta) sel.getFirstElement();
		IStatus status = (IStatus) statusTable.get(delta);

		if (status != null) {
			ErrorDialog.openError(getShell(), null, null, status);
			return;
		}
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
		for (int i = 0; i < deltas.length; i++) {
			ISessionDelta delta = deltas[i];
			IStatus status = (IStatus) statusTable.get(delta);
			if (status != null)
				grayed.add(deltas[i]);
		}
		deltaViewer.setGrayedElements(grayed.toArray());
	}

	private void initializeStatusTable() {
		statusTable = new Hashtable();
		for (int i = 0; i < deltas.length; i++) {
			ISessionDelta delta = deltas[i];
			IStatus status = ActivityConstraints.validateSessionDelta(delta);
			if (status != null)
				statusTable.put(delta, status);
		}
	}

	private void handleCheckStateChanged(Object obj, boolean checked) {
		if (obj instanceof DeltaFeature) {
			// do not allow it
			deltaViewer.setChecked(obj, !checked);
		} else if (obj instanceof ISessionDelta) {
			ISessionDelta delta = (ISessionDelta) obj;
			IStatus status = (IStatus) statusTable.get(delta);
			if (status != null) {
				// delta with errors - do not allow it
				deltaViewer.setChecked(obj, !checked);
				return;
			}

			Object[] dfeatures = (Object[]) features.get(obj);
			for (int i = 0; i < dfeatures.length; i++) {
				deltaViewer.setChecked(dfeatures[i], checked);
			}
			dialogChanged();
		}
	}
	private void dialogChanged() {
		ISessionDelta[] deltas = getSelectedDeltas();
		setPageComplete(deltas.length > 0 || removed.size() > 0);
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