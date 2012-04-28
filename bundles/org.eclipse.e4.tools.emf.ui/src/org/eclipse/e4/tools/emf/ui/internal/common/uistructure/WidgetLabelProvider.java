package org.eclipse.e4.tools.emf.ui.internal.common.uistructure;

import org.eclipse.e4.tools.emf.ui.internal.ResourceProvider;
import org.eclipse.e4.tools.services.IResourcePool;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.CoolItem;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Sash;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;

public class WidgetLabelProvider extends StyledCellLabelProvider {
	private IResourcePool resourcePool;

	public WidgetLabelProvider(IResourcePool resourcePool) {
		this.resourcePool = resourcePool;
	}

	@Override
	public void update(ViewerCell cell) {
		Object element = cell.getElement();
		if (element instanceof Menu) {
			Menu m = (Menu) element;
			if ((m.getStyle() & SWT.BAR) == SWT.BAR) {
				cell.setText("MenuBar"); //$NON-NLS-1$
				cell.setImage(resourcePool.getImageUnchecked(ResourceProvider.IMG_Widgets_menubar_obj));
			} else {
				cell.setText("Menu"); //$NON-NLS-1$
				cell.setImage(resourcePool.getImageUnchecked(ResourceProvider.IMG_Widgets_menu_obj));
			}
		} else if (element instanceof MenuItem) {
			MenuItem item = (MenuItem) element;
			if ((item.getStyle() & SWT.SEPARATOR) == SWT.SEPARATOR) {
				cell.setText("Separator"); //$NON-NLS-1$
				cell.setImage(resourcePool.getImageUnchecked(ResourceProvider.IMG_Widgets_menuseparator_obj));
			} else {
				StyledString s = new StyledString("MenuItem"); //$NON-NLS-1$
				s.append(" - " + ((MenuItem) element).getText(), StyledString.DECORATIONS_STYLER); //$NON-NLS-1$
				cell.setStyleRanges(s.getStyleRanges());
				cell.setText(s.getString());
				cell.setImage(resourcePool.getImageUnchecked(ResourceProvider.IMG_Widgets_menuitem_obj));
			}

		} else if (element instanceof CLabel) {
			StyledString s = new StyledString("Label"); //$NON-NLS-1$
			s.append(" - " + ((CLabel) element).getText(), StyledString.DECORATIONS_STYLER); //$NON-NLS-1$
			cell.setStyleRanges(s.getStyleRanges());
			cell.setText(s.getString());
			cell.setImage(resourcePool.getImageUnchecked(ResourceProvider.IMG_Widgets_clabel_obj));
		} else if (element instanceof ToolBar) {
			cell.setText("Toolbar"); //$NON-NLS-1$
			cell.setImage(resourcePool.getImageUnchecked(ResourceProvider.IMG_Widgets_toolbar_obj));
		} else if (element instanceof ToolItem) {
			cell.setText("ToolItem"); //$NON-NLS-1$
			ToolItem item = (ToolItem) element;
			if ((item.getStyle() & SWT.PUSH) == SWT.PUSH) {
				cell.setImage(resourcePool.getImageUnchecked(ResourceProvider.IMG_Widgets_toolitempush_obj));
			} else if ((item.getStyle() & SWT.DROP_DOWN) == SWT.DROP_DOWN) {
				cell.setImage(resourcePool.getImageUnchecked(ResourceProvider.IMG_Widgets_toolitemdrop_obj));
			} else if ((item.getStyle() & SWT.CHECK) == SWT.CHECK) {
				cell.setImage(resourcePool.getImageUnchecked(ResourceProvider.IMG_Widgets_toolitemcheck_obj));
			} else if ((item.getStyle() & SWT.RADIO) == SWT.RADIO) {
				cell.setImage(resourcePool.getImageUnchecked(ResourceProvider.IMG_Widgets_toolitemradio_obj));
			} else if ((item.getStyle() & SWT.SEPARATOR) == SWT.SEPARATOR) {
				cell.setImage(resourcePool.getImageUnchecked(ResourceProvider.IMG_Widgets_toolitemseparator_obj));
			}
		} else if (element instanceof CoolItem) {
			cell.setText("CoolItem"); //$NON-NLS-1$
			CoolItem item = (CoolItem) element;
			if ((item.getStyle() & SWT.PUSH) == SWT.PUSH) {
				cell.setImage(resourcePool.getImageUnchecked(ResourceProvider.IMG_Widgets_toolitempush_obj));
			} else if ((item.getStyle() & SWT.DROP_DOWN) == SWT.DROP_DOWN) {
				cell.setImage(resourcePool.getImageUnchecked(ResourceProvider.IMG_Widgets_toolitemdrop_obj));
			} else if ((item.getStyle() & SWT.CHECK) == SWT.CHECK) {
				cell.setImage(resourcePool.getImageUnchecked(ResourceProvider.IMG_Widgets_toolitemcheck_obj));
			} else if ((item.getStyle() & SWT.RADIO) == SWT.RADIO) {
				cell.setImage(resourcePool.getImageUnchecked(ResourceProvider.IMG_Widgets_toolitemradio_obj));
			} else if ((item.getStyle() & SWT.SEPARATOR) == SWT.SEPARATOR) {
				cell.setImage(resourcePool.getImageUnchecked(ResourceProvider.IMG_Widgets_toolitemseparator_obj));
			}
		} else if (element instanceof CoolBar) {
			cell.setText("Coolbar"); //$NON-NLS-1$
			cell.setImage(resourcePool.getImageUnchecked(ResourceProvider.IMG_Widgets_coolbar_obj));
		} else if (element instanceof Shell) {
			StyledString s = new StyledString("Shell"); //$NON-NLS-1$
			s.append(" - " + ((Shell) element).getText(), StyledString.DECORATIONS_STYLER); //$NON-NLS-1$
			cell.setStyleRanges(s.getStyleRanges());
			cell.setText(s.getString());
			cell.setImage(resourcePool.getImageUnchecked(ResourceProvider.IMG_Widgets_shell_obj));
		} else if (element instanceof ProgressBar) {
			cell.setText("ProgressBar"); //$NON-NLS-1$
			cell.setImage(resourcePool.getImageUnchecked(ResourceProvider.IMG_Widgets_progressbar_obj));
		} else if (element instanceof Group) {
			StyledString s = new StyledString("Group"); //$NON-NLS-1$
			s.append(" - " + ((Group) element).getText(), StyledString.DECORATIONS_STYLER); //$NON-NLS-1$
			cell.setStyleRanges(s.getStyleRanges());
			cell.setText(s.getString());
			cell.setImage(resourcePool.getImageUnchecked(ResourceProvider.IMG_Widgets_group_obj));
		} else if (element instanceof TabFolder) {
			cell.setText("TabFolder"); //$NON-NLS-1$
			cell.setImage(resourcePool.getImageUnchecked(ResourceProvider.IMG_Widgets_tabfolder_obj));
		} else if (element instanceof CTabFolder) {
			cell.setText("CTabFolder"); //$NON-NLS-1$
			cell.setImage(resourcePool.getImageUnchecked(ResourceProvider.IMG_Widgets_ctabfolder_obj));
		} else if (element instanceof Combo) {
			cell.setText("Combo"); //$NON-NLS-1$
			cell.setImage(resourcePool.getImageUnchecked(ResourceProvider.IMG_Widgets_comboviewer_obj));
		} else if (element instanceof CCombo) {
			cell.setText("CCombo"); //$NON-NLS-1$
			cell.setImage(resourcePool.getImageUnchecked(ResourceProvider.IMG_Widgets_ccombo_obj));
		} else if (element instanceof Table) {
			cell.setText("Table"); //$NON-NLS-1$
			cell.setImage(resourcePool.getImageUnchecked(ResourceProvider.IMG_Widgets_table_obj));
		} else if (element instanceof Tree) {
			cell.setText("Tree"); //$NON-NLS-1$
			cell.setImage(resourcePool.getImageUnchecked(ResourceProvider.IMG_Widgets_tree_obj));
		} else if (element instanceof Text) {
			cell.setText("Text"); //$NON-NLS-1$
			cell.setImage(resourcePool.getImageUnchecked(ResourceProvider.IMG_Widgets_text_obj));
		} else if (element instanceof Sash) {
			cell.setText("Sash"); //$NON-NLS-1$
			cell.setImage(resourcePool.getImageUnchecked(ResourceProvider.IMG_Widgets_separator_obj));
		} else if (element instanceof SashForm) {
			cell.setText("Sash Form"); //$NON-NLS-1$
			cell.setImage(resourcePool.getImageUnchecked(ResourceProvider.IMG_Widgets_sashform_obj));
		} else if (element instanceof Label) {
			Label l = (Label) element;
			if ((l.getStyle() & SWT.SEPARATOR) == SWT.SEPARATOR) {
				cell.setText("Separator"); //$NON-NLS-1$
				cell.setImage(resourcePool.getImageUnchecked(ResourceProvider.IMG_Widgets_separator_obj));
			} else {
				StyledString s = new StyledString("Label"); //$NON-NLS-1$
				s.append(" - " + l.getText(), StyledString.DECORATIONS_STYLER); //$NON-NLS-1$
				cell.setStyleRanges(s.getStyleRanges());
				cell.setText(s.getString());
				cell.setImage(resourcePool.getImageUnchecked(ResourceProvider.IMG_Widgets_label_obj));
			}

		} else if (element instanceof TabItem) {
			StyledString s = new StyledString("TabItem"); //$NON-NLS-1$
			s.append(" - " + ((TabItem) element).getText(), StyledString.DECORATIONS_STYLER); //$NON-NLS-1$
			cell.setStyleRanges(s.getStyleRanges());
			cell.setText(s.getString());
		} else if (element instanceof CTabItem) {
			StyledString s = new StyledString("CTabItem"); //$NON-NLS-1$
			s.append(" - " + ((CTabItem) element).getText(), StyledString.DECORATIONS_STYLER); //$NON-NLS-1$
			cell.setStyleRanges(s.getStyleRanges());
			cell.setText(s.getString());
			// cell.setImage(resourcePool.getImageUnchecked(ResourceProvider.IMG_Widgets_clabel_obj));
		} else if (element instanceof Button) {
			Button b = (Button) element;
			if ((b.getStyle() & SWT.PUSH) == SWT.PUSH) {
				cell.setText("Button"); //$NON-NLS-1$
				cell.setImage(resourcePool.getImageUnchecked(ResourceProvider.IMG_Widgets_button_obj));
			} else if ((b.getStyle() & SWT.CHECK) == SWT.CHECK) {
				cell.setText("Checkbox"); //$NON-NLS-1$
				cell.setImage(resourcePool.getImageUnchecked(ResourceProvider.IMG_Widgets_checkbox_obj));
			} else if ((b.getStyle() & SWT.RADIO) == SWT.RADIO) {
				cell.setText("Radiobox"); //$NON-NLS-1$
				cell.setImage(resourcePool.getImageUnchecked(ResourceProvider.IMG_Widgets_radiobutton_obj));
			}
		} else if (element instanceof Composite) {
			cell.setText("Composite"); //$NON-NLS-1$
			cell.setImage(resourcePool.getImageUnchecked(ResourceProvider.IMG_Widgets_composite_obj));
		} else {
			cell.setText(element + ""); //$NON-NLS-1$
		}
		super.update(cell);
	}
}
