JFaceSnippets
=============

JFace-Snippets are small and easy understandable programming examples of how to use the JFace API. 
To browse the examples, navigate to the [examples GIT repository](https://github.com/eclipse-platform/eclipse.platform.ui/tree/master/examples/org.eclipse.jface.snippets).

**Copy Paste**

The header section of each snippet is a link to the plain source. 
You can copy the source and paste it directly on the source folder or package in an eclipse project. 
The package and the class will be created automatically.  

![Jfacecopypaste.gif](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/docs/images/Jfacecopypaste.gif)
   

Contents
--------

*   [1 Dialogs](#Dialogs)
    *   [1.1 Snippet012 - Dialog with Image Buttons](#Snippet012---Dialog-with-Image-Buttons)
    *   [1.2 Snippet082 - Color Selector](#Snippet082---Color-Selector)
*   [2 Notification](#Notification)
    *   [2.1 Snippet081 - Notification API](#Snippet081---Notification-API)
    *   [2.2 Snippet083 - Notification Popup with Functions](#Snippet083---Notification-Popup-with-Functions)
    *   [2.3 Snippet084 - Notification Popup with Custom Delay and Fade](#Snippet084---Notification-Popup-with-Custom-Delay-and-Fade)
    *   [2.4 Snippet085 - Notification popup with user interaction](#Snippet085---Notification-popup-with-user-interaction)
*   [3 Layout](#Layout)
    *   [3.1 Snippet013 - Grid Layout Factory](#Snippet013---Grid-Layout-Factory)
    *   [3.2 Snippet016 - Table Layout](#Snippet016---Table-Layout)
    *   [3.3 Snippet027 - Tree Layout](#Snippet027---Tree-Layout)
*   [4 Viewers](#Viewers)
    *   [4.1 Snippet001 - Table Viewer](#Snippet001---Table-Viewer)
    *   [4.2 Snippet002 - Tree Viewer](#Snippet002---Tree-Viewer)
    *   [4.3 Snippet003 - Table Label Provider](#Snippet003---Table-Label-Provider)
    *   [4.4 Snippet004 - Hide Selection](#Snippet004---Hide-Selection)
    *   [4.5 Snippet005 - Tree Custom Menu](#Snippet005---Tree-Custom-Menu)
    *   [4.6 Snippet006 - Table Multi Line Cells](#Snippet006---Table-Multi-Line-Cells)
    *   [4.7 Snippet007 - Full Selection](#Snippet007---Full-Selection)
    *   [4.8 Snippet008 - Reveal Element](#Snippet008---Reveal-Element)
    *   [4.9 Snippet009 - Cell Editors](#Snippet009---Cell-Editors)
    *   [4.10 Snippet010 - Owner Draw](#Snippet010---Owner-Draw)
    *   [4.11 Snippet011 - Custom Tooltips](#Snippet011---Custom-Tooltips)
    *   [4.12 Snippet013 - Table Viewer No Mandatory Label Provider\]](#Snippet013---Table-Viewer-No-Mandatory-Label-Provider.5D)
    *   [4.13 Snippet014 - Tree Viewer No Mandatory Label Provider\]](#Snippet014---Tree-Viewer-No-Mandatory-Label-Provider.5D)
    *   [4.14 Snippet015 - Custom Tooltips For Tree](#Snippet015---Custom-Tooltips-For-Tree)
    *   [4.15 Snippet017 - Table Viewer Hide Show Columns](#Snippet017---Table-Viewer-Hide-Show-Columns)
    *   [4.16 Snippet019 - Table Viewer Add Remove Columns With Editing](#Snippet019---Table-Viewer-Add-Remove-Columns-With-Editing)
    *   [4.17 Snippet024 - Table Viewer Explore](#Snippet024---Table-Viewer-Explore)
    *   [4.18 Snippet025 - Tab Editing](#Snippet025---Tab-Editing)
    *   [4.19 Snippet026 - Tree Viewer Tab Editing](#Snippet026---Tree-Viewer-Tab-Editing)
    *   [4.20 Snippet027 - Combo Box Cell Editors](#Snippet027---Combo-Box-Cell-Editors)
    *   [4.21 Snippet029 - Virtual Table Viewer](#Snippet029---Virtual-Table-Viewer)
    *   [4.22 Snippet030 - Virtual Lazy Table Viewer](#Snippet030---Virtual-Lazy-Table-Viewer)
    *   [4.23 Snippet031 - Table Viewer Custom Tooltips Multi Selection\]](#Snippet031---Table-Viewer-Custom-Tooltips-Multi-Selection.5D)
    *   [4.24 Snippet034 - Cell Editor Per Row](#Snippet034---Cell-Editor-Per-Row)
    *   [4.25 Snippet035 - Table Cursor Cell Highlighter](#Snippet035---Table-Cursor-Cell-Highlighter)
    *   [4.26 Snippet036 - Focus Border Cell Highlighter](#Snippet036---Focus-Border-Cell-Highlighter)
    *   [4.27 Snippet037 - Fancy Custom Tooltips](#Snippet037---Fancy-Custom-Tooltips)
    *   [4.28 Snippet039 - List Viewer](#Snippet039---List-Viewer)
    *   [4.29 Snippet040 - Table Viewer Sorting](#Snippet040---Table-Viewer-Sorting)
    *   [4.30 Snippet 041 - Table Viewer Alternating Colors and Viewer Filters](#Snippet-041---Table-Viewer-Alternating-Colors-and-Viewer-Filters)
    *   [4.31 Snippet043 - Tree Viewer Keyboard Editing](#Snippet043---Tree-Viewer-Keyboard-Editing)
    *   [4.32 Snippet044 - Table Viewer Keyboard Editing](#Snippet044---Table-Viewer-Keyboard-Editing)
    *   [4.33 Snippet045 - Table Viewer Fill From Background Thread](#Snippet045---Table-Viewer-Fill-From-Background-Thread)
    *   [4.34 Snippet046 - Update Viewer From Background Thread](#Snippet046---Update-Viewer-From-Background-Thread)
    *   [4.35 Snippet047 - Virtual Lazy Tree Viewer](#Snippet047---Virtual-Lazy-Tree-Viewer)
    *   [4.36 Snippet048 - Tree Viewer Tab With Checkbox](#Snippet048---Tree-Viewer-Tab-With-Checkbox)
    *   [4.37 Snippet049 - Styled Cell Label Provider](#Snippet049---Styled-Cell-Label-Provider)
    *   [4.38 Snippet050 - Delegating Styled Cell Label Provider](#Snippet050---Delegating-Styled-Cell-Label-Provider)
    *   [4.39 Snippet051 - Table Centered Image](#Snippet051---Table-Centered-Image)
    *   [4.40 Snippet052 - Double Click Cell Editor](#Snippet052---Double-Click-Cell-Editor)
    *   [4.41 Snippet053 - Start Editor With Context Menu](#Snippet053---Start-Editor-With-Context-Menu)
    *   [4.42 Snippet055 - Hide Show Column](#Snippet055---Hide-Show-Column)
    *   [4.43 Snippet056 - Boolean Cell Editor](#Snippet056---Boolean-Cell-Editor)
    *   [4.44 Snippet057 - Table Viewer Skip Hidden Cells](#Snippet057---Table-Viewer-Skip-Hidden-Cells)
    *   [4.45 Snippet058 - Cell Navigation](#Snippet058---Cell-Navigation)
    *   [4.46 Snippet060 - Text Cell Editor With Content Proposal/Field assists](#Snippet060---Text-Cell-Editor-With-Content-Proposal.2FField-assists)
    *   [4.47 Snippet061 - Faked Native Cell Editor](#Snippet061---Faked-Native-Cell-Editor)
    *   [4.48 Snippet062 - Text And Dialog Cell Editor](#Snippet062---Text-And-Dialog-Cell-Editor)
    *   [4.49 Snippet063 - Combo Viewer](#Snippet063---Combo-Viewer)
    *   [4.50 Snippet064 - Replacing elements in a TreeViewer with child elements](#Snippet064---Replacing-elements-in-a-TreeViewer-with-child-elements)
    *   [4.51 Snippet065 - Replacing elements in a TreeViewer without child elements](#Snippet065---Replacing-elements-in-a-TreeViewer-without-child-elements)
    *   [4.52 Snippet066 - TableViewer with Label Decorator](#Snippet066---TableViewer-with-Label-Decorator)
*   [5 Window](#Window)
    *   [5.1 Snippet020 - Customized Control Tooltips](#Snippet020---Customized-Control-Tooltips)
    *   [5.2 Snippet031 - Table Static Tooltip](#Snippet031---Table-Static-Tooltip)
*   [6 Wizard](#Wizard)
    *   [6.1 Snippet047 - Wizard with Long Running Operation from Page](#Snippet047---Wizard-with-Long-Running-Operation-from-Page)
    *   [6.2 Snippet071 - Wizard with Progress and Cancel](#Snippet071---Wizard-with-Progress-and-Cancel)
    *   [6.3 Snippet072 Wizard with Progress Subtasks and Cancel](#Snippet072-Wizard-with-Progress-Subtasks-and-Cancel)
    *   [6.4 Snippet074 Wizard with access to application window](#Snippet074-Wizard-with-access-to-application-window)

Dialogs
-------

### [Snippet012 - Dialog with Image Buttons](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/dialogs/Snippet012DialogWithImageButtons.java)

*   [Snippet012 - Dialog with Image Buttons](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/dialogs/Snippet012DialogWithImageButtons.java)

  
Demonstrates usage of Icons in Buttons of Dialogs

![Snippet012DialogWithImageButtons.png](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/docs/images/Snippet012DialogWithImageButtons.png)

Drop these icons also in the same package

![Filesave.png](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/docs/images/Filesave.png)
![Cancel.png](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/docs/images/Cancel.png)

### [Snippet082 - Color Selector](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/dialogs/Snippet082ColorSelectDialog.java)

*   [Snippet082 - Color Selector](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/dialogs/Snippet082ColorSelectDialog.java)

  
The JFace ColorSelector widget is a convenient composition of button and color selector dialog. The button displays a swatch of the selected color.

  
![Snippet082ColorSelectDialog.gif](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/docs/images/325px-Snippet082ColorSelectDialog.gif)

Notification
------------

### [Snippet081 - Notification API](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/dialogs/Snippet081NotificationPopup.java)

*   [Snippet081 - Notication API](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/dialogs/Snippet081NotificationPopup.java)

  
Demonstrates usage of the non-blocking notification API

  
![Snippet081 Shell1.gif](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/docs/images/Snippet081_Shell1.gif)

### [Snippet083 - Notification Popup with Functions](https://github.com/eclipse-platform/eclipse.platform.ui/blob/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/notifications/Snippet083NotificationPopupWithFunctions.java)

*   [Snippet083 - Notification Popup with Functions](https://github.com/eclipse-platform/eclipse.platform.ui/blob/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/notifications/Snippet083NotificationPopupWithFunctions.java)

Demonstrates the creation of notification popups that include function callbacks for user interactions.

### [Snippet084 - Notification Popup with Custom Delay and Fade](https://github.com/eclipse-platform/eclipse.platform.ui/blob/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/notifications/Snippet084NotificationPopupWithCustomDelayAndFade.java)

*   [Snippet084 - Notification Popup with Custom Delay and Fade](https://github.com/eclipse-platform/eclipse.platform.ui/blob/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/notifications/Snippet084NotificationPopupWithCustomDelayAndFade.java)

Shows how to create notification popups with custom delay and fade effects for enhanced visual feedback.

### [Snippet085 - Notification popup with user interaction](https://github.com/eclipse-platform/eclipse.platform.ui/blob/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/notifications/Snippet085NotificationPopupWithUserInteraction.java)

This snippet demonstrates how to create a `NotificationPopup` that includes user interaction with a button. When the button is clicked, a confirmation dialog is shown.

For the full code, please visit the [GitHub repository](https://github.com/eclipse-platform/eclipse.platform.ui/blob/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/notifications/Snippet085NotificationPopupWithUserInteraction.java).

 
![Snippet085.png](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/docs/images/Snippet085NotificationPopupWithUserInteraction.png)


Layout
------

### [Snippet013 - Grid Layout Factory](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/layout/Snippet013GridLayoutFactory.java)

*   [Snippet013 - Grid Layout Factory](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/layout/Snippet013GridLayoutFactory.java)

  
Demonstrates usage of the GridLayoutFactory to enhance readability

![Snippet013 Shell1.png](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/docs/images/Snippet013_Shell1.png)
![Snippet013 Shell2.png](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/docs/images/Snippet013_Shell2.png)
![Snippet013 Shell3.png](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/docs/images/Snippet013_Shell3.png)

### [Snippet016 - Table Layout](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/layout/Snippet016TableLayout.java)

*   [Snippet016 - Table Layout](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/layout/Snippet016TableLayout.java)

  
Demonstrates (dynamic)layout support for TableColumns available as of JFace 3.3

![Snippet016.png](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/docs/images/Snippet016.png)

### [Snippet027 - Tree Layout](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/layout/Snippet027TreeLayout.java)

*   [Snippet027 - Tree Layout](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/layout/Snippet027TreeLayout.java)

  
Demonstrates (dynamic)layout support for TreeColumns available as of JFace 3.3

![Snippet027.png](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/docs/images/Snippet027.png)

Viewers
-------

### [Snippet001 - Table Viewer](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet001TableViewer.java)

*   [Snippet001 - Table Viewer](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet001TableViewer.java)

  
Demonstrates a simply TableViewer with one column. It holds all important classes used for all Table-like JFace-Viewers (_[LabelProvider](http://help.eclipse.org/help32/index.jsp?topic=/org.eclipse.platform.doc.isv/reference/api/org/eclipse/jface/viewers/LabelProvider.html)_,_[IStructuredContentProvider](http://help.eclipse.org/help32/index.jsp?topic=/org.eclipse.platform.doc.isv/reference/api/org/eclipse/jface/viewers/IStructuredContentProvider.html)_)

![Snippet1.png](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/docs/images/Snippet1.png)

### [Snippet002 - Tree Viewer](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet002TreeViewer.java)

*   [Snippet002 - Tree Viewer](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet002TreeViewer.java)

  
Demonstrates a simply TreeViewer with one column. It describes all important classes used for all Tree-like JFace-Viewers (_[LabelProvider](http://help.eclipse.org/help32/index.jsp?topic=/org.eclipse.platform.doc.isv/reference/api/org/eclipse/jface/viewers/LabelProvider.html)_,_[ITreeContentProvider](http://help.eclipse.org/help32/index.jsp?topic=/org.eclipse.platform.doc.isv/reference/api/org/eclipse/jface/viewers/ITreeContentProvider.html)_)

![Snippet2.png](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/docs/images/Snippet2.png)

### [Snippet003 - Table Label Provider](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet003TableLabelProvider.java)

*   [Snippet003 - Table Label Provider](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet003TableLabelProvider.java)

  
Demonstrates tables with more than one column and the usage of _[ITableLabelProvider](http://help.eclipse.org/help32/index.jsp?topic=/org.eclipse.platform.doc.isv/reference/api/org/eclipse/jface/viewers/ITableLabelProvider.html)_

![Snippet003.png](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/docs/images/Snippet003.png)

### [Snippet004 - Hide Selection](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet004HideSelection.java)

*   [Snippet004 - Hide Selection](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet004HideSelection.java)

  
Demonstrates how the selection can be hidden when the user clicks in a table-row/column which doesn't hold any information. The standard behavior of SWT-Table is to leave the selection on the last column. This snippet removes the selection if the user clicks in an area not selectable

![Snippet004.png](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/docs/images/Snippet004.png)

### [Snippet005 - Tree Custom Menu](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet005TreeCustomMenu.java)

*   [Snippet005 - Tree Custom Menu](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet005TreeCustomMenu.java)

  
Demonstrates how to create a different context menu depending on which item in the tree is currently selected this can also be used with a table of course

![Snippet005.png](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/docs/images/Snippet005.png)

### [Snippet006 - Table Multi Line Cells](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet006TableMultiLineCells.java)

*   [Snippet006 - Table Multi Line Cells](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet006TableMultiLineCells.java)

  
Demonstrates first use case for the OwnerDraw-Support added to JFace in 3.3 (available at SWT-Level since 3.2). This example uses the Viewers API in this special case the _OwnerDrawLabelProvider_ to make items with more than one line of text.

![Snippet006.png](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/docs/images/Snippet006.png)

### [Snippet007 - Full Selection](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet007FullSelection.java)

*   [Snippet007 - Full Selection](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet007FullSelection.java)

  
Demonstrates how you can use inline editing in tables with multiple columns that require to use SWT.FULL_SELECTION but hiding the selection from the user.

![Snippet007.png](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/docs/images/Snippet007.png)

### [Snippet008 - Reveal Element](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet008RevealElement.java)

*   [Snippet008 - Reveal Element](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet008RevealElement.java)

  
Demonstrates how you can scroll a TableViewer to the specific model element using TableViewer#reveal(Object)

![Snippet008.png](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/docs/images/Snippet008.png)

### [Snippet009 - Cell Editors](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet009CellEditors.java)

*   [Snippet009 - Cell Editors](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet009CellEditors.java)

  
Demonstrates minimal example when trying to add inline editing to tables to get familiar with the various classes needed (3.2 API)

![Snippet009.png](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/docs/images/Snippet009.png)

### [Snippet010 - Owner Draw](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet010OwnerDraw.java)

*   [Snippet010 - Owner Draw](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet010OwnerDraw.java)

  
Demonstrates usage of the OwnerDraw-Support feature provided by JFace in 3.3(available in SWT since 3.2). This example uses the Viewers API in this special case the _OwnerDrawLabelProvider_.

![Snippet010.png](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/docs/images/Snippet010.png)

### [Snippet011 - Custom Tooltips](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet011CustomTooltips.java)

*   [Snippet011 - Custom Tooltips](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet011CustomTooltips.java)

  
Demonstrates usage of custom tooltip support in 3.3 used to provide a tooltip for each cell in TableViewer

![Snippet011.png](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/docs/images/Snippet011.png)

### [Snippet013 - Table Viewer No Mandatory Label Provider](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet013TableViewerNoMandatoryLabelProvider.java)

*   [Snippet013 - Table Viewer No Mandatory Label Provider](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet013TableViewerNoMandatoryLabelProvider.java)

  
Demonstrates usage of none mandatory LabelProviders in TableViewers to set colors and fonts with 3.2-API

![Jfacesnippet013.png](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/docs/images/Jfacesnippet013.png)

### [Snippet014 - Tree Viewer No Mandatory Label Provider](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet014TreeViewerNoMandatoryLabelProvider.java)

*   [Snippet014 - Tree Viewer No Mandatory Label Provider](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet014TreeViewerNoMandatoryLabelProvider.java)

  
Demonstrates usage of none mandatory LabelProviders in TreeViewers to set colors and font with 3.2-API

![Snippet014TreeViewerNoMandatoryLabelProvider.png](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/docs/images/Snippet014TreeViewerNoMandatoryLabelProvider.png)

### [Snippet015 - Custom Tooltips For Tree](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet015CustomTooltipsForTree.java)

*   [Snippet015 - Custom Tooltips For Tree](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet015CustomTooltipsForTree.java)

  
Demonstrates usage of custom tooltip support used to provide a tooltip for each cell in a TreeViewer

![Snippet015CustomTooltipsForTree.png](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/docs/images/Snippet015CustomTooltipsForTree.png)

### [Snippet017 - Table Viewer Hide Show Columns](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet017TableViewerHideShowColumns.java)

*   [Snippet017 - Table Viewer Hide Show Columns](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet017TableViewerHideShowColumns.java)

  
Demonstrates hiding and showing columns (animated)

![Snippet017TableViewerHideShowColumns.png](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/docs/images/Snippet017TableViewerHideShowColumns.png)

### [Snippet019 - Table Viewer Add Remove Columns With Editing](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet019TableViewerAddRemoveColumnsWithEditing.java)

*   [Snippet019 - Table Viewer Add Remove Columns With Editing](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet019TableViewerAddRemoveColumnsWithEditing.java)

  
Demonstrates adding/removing of columns in conjunction with the inline editing with JFace-API

![Snippet019TableViewerAddRemoveColumnsWithEditing.png](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/docs/images/Snippet019TableViewerAddRemoveColumnsWithEditing.png)

### [Snippet024 - Table Viewer Explore](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet024TableViewerExploreNewAPI.java)

*   [Snippet024 - Table Viewer Explore](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet024TableViewerExploreNewAPI.java)

  
Demonstrates the base classes of 3.3 API

![Snippet024TableViewerExploreNewAPI.png](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/docs/images/Snippet024TableViewerExploreNewAPI.png)

### [Snippet025 - Tab Editing](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet025TabEditing.java)

*   [Snippet025 - Tab Editing](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet025TabEditing.java)

  
Demonstrates how one can use the 3.3 API to add tab-editing support to your viewer

![Snippet025TabEditing.png](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/docs/images/Snippet025TabEditing.png)

Press Tab to jump from cell to cell

### [Snippet026 - Tree Viewer Tab Editing](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet026TreeViewerTabEditing.java)

*   [Snippet026 - Tree Viewer Tab Editing](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet026TreeViewerTabEditing.java)

  
Demonstrates all fancy things one can do with the 3.3 API (Tab-Editing, Keyboard-Navigation from Cell to Cell, Editor-Activation with the Keyboard)

![Snippet026TreeViewerTabEditing.gif](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/docs/images/Snippet026TreeViewerTabEditing.gif)

### [Snippet027 - Combo Box Cell Editors](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet027ComboBoxCellEditors.java)

*   [Snippet027 - Combo Box Cell Editors](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet027ComboBoxCellEditors.java)

  
Demonstrates usage of the ComboBoxCellEditor in JFace-Viewers

![Snippet027ComboBoxCellEditors.png](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/docs/images/Snippet027ComboBoxCellEditors.png)

### [Snippet029 - Virtual Table Viewer](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet029VirtualTableViewer.java)

*   [Snippet029 - Virtual Table Viewer](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet029VirtualTableViewer.java)

  
Demonstrates usage of JFace-Viewers in "virtual" mode with an ordinary content provider (often the bottleneck is not the model but the UI). Using these Virtual viewers in conjunction with an ordinary content provider has the advantage that Sorting and Filtering are supported in 3.3.

### [Snippet030 - Virtual Lazy Table Viewer](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet030VirtualLazyTableViewer.java)

*   [Snippet030 - Virtual Lazy Table Viewer](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet030VirtualLazyTableViewer.java)

  
Demonstrates usage of JFace-Viewer virtual mode with a lazy content provider

### [Snippet031 - Table Viewer Custom Tooltips Multi Selection](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet031TableViewerCustomTooltipsMultiSelection.java)

*   [Snippet031 - Table Viewer Custom Tooltips Multi Selection](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet031TableViewerCustomTooltipsMultiSelection.java)

  
Demonstrates creation of tooltips for cells for pre 3.3 users

![Snippet031TableViewerCustomTooltipsMultiSelection.png](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/docs/images/Snippet031TableViewerCustomTooltipsMultiSelection.png)

### [Snippet034 - Cell Editor Per Row](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet034CellEditorPerRow.java)

*   [Snippet034 - Cell Editor Per Row](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet034CellEditorPerRow.java)

  
Demonstrates different CellEditor-Types in one COLUMN of JFace-Viewers

![Snippet034CellEditorPerRow.png](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/docs/images/Snippet034CellEditorPerRow.png)

### [Snippet035 - Table Cursor Cell Highlighter](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet035TableCursorCellHighlighter.java)

*   [Snippet035 - Table Cursor Cell Highlighter](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet035TableCursorCellHighlighter.java)

  
Demonstrates keyboard navigation in TableViewers using a TableCursor showing the flexibility of the cell navigation support

You also need these classes:

*   [CursorCellHighlighter.java](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/CursorCellHighlighter.java)
*   [AbstractCellCursor.java](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/AbstractCellCursor.java)
*   [TableCursor.java](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/TableCursor.java)

  
![Snippet035TableCursorCellHighlighter.png](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/docs/images/Snippet035TableCursorCellHighlighter.png)

### [Snippet036 - Focus Border Cell Highlighter](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet036FocusBorderCellHighlighter.java)

*   [Snippet036 - Focus Border Cell Highlighter](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet036FocusBorderCellHighlighter.java)

  
Demonstrates keyboard navigation by highlighting the currently selected cell with a focus border showing once more the flexibility of the cell navigation support

You also need:

*   [FocusBorderCellHighlighter.java](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/FocusBorderCellHighlighter.java)

  
![Snippet036FocusBorderCellHighlighter.png](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/docs/images/Snippet036FocusBorderCellHighlighter.png)

### [Snippet037 - Fancy Custom Tooltips](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet037FancyCustomTooltips.java)

*   [Snippet037 - Fancy Custom Tooltips](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet037FancyCustomTooltips.java)

  
Demonstrates customizability of the 3.3 JFace-Support for cell tooltips using the Browser-Widget and presenting HTML

![Snippet037FancyCustomTooltips.png](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/docs/images/Snippet037FancyCustomTooltips.png)

### [Snippet039 - List Viewer](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet039ListViewer.java)

*   [Snippet039 - List Viewer](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet039ListViewer.java)

  
Demonstrates a very simple usage of ListViewer

![Snippet039ListViewer.png](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/docs/images/Snippet039ListViewer.png)

### [Snippet040 - Table Viewer Sorting](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet040TableViewerSorting.java)

*   [Snippet040 - Table Viewer Sorting](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet040TableViewerSorting.java)

  
Demonstrates sorting (ascending/descending) in TableViewers by clicking the column header.

![Snippet040TableViewerSorting.png](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/docs/images/Snippet040TableViewerSorting.png)

### [Snippet 041 - Table Viewer Alternating Colors and Viewer Filters](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet041TableViewerAlternatingColors.java)

*   [Snippet 041 - Table Viewer Alternating Colors and Viewer Filters](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet041TableViewerAlternatingColors.java)

  
Demonstrates how to achieve alternating row-colors with TableViewer. It can also be used in conjunction with virtual-bits to even work with big tables (e.g. 100,000 rows in this example). In addition, this snippet provided a button that will demonstrate the usage of viewer filters.

![Snippet041TableViewerAlternatingColors.png](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/docs/images/Snippet041TableViewerAlternatingColors.png)

### [Snippet043 - Tree Viewer Keyboard Editing](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet043TreeViewerKeyboardEditing.java)

*   [Snippet043 - Tree Viewer Keyboard Editing](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet043TreeViewerKeyboardEditing.java)

  
Demonstrates the JFace 3.3 keyboard editing support for Trees without columns. Tabbing from editor to editor is supported since 3.4. In addition, this snippet provided a button that shows how to enter in edit mode programmatically.

![Snippet043TreeViewerKeyboardEditing.png](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/docs/images/Snippet043TreeViewerKeyboardEditing.png)

### [Snippet044 - Table Viewer Keyboard Editing](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet044TableViewerKeyboardEditing.java)

*   [Snippet044 - Table Viewer Keyboard Editing](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet044TableViewerKeyboardEditing.java)

  
Demonstrates the JFace 3.3 keyboard editing support for Tables without columns. Tabbing from editor to editor is supported since 3.4.

You also need:

*   [FocusBorderCellHighlighter.java](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/FocusBorderCellHighlighter.java)

### [Snippet045 - Table Viewer Fill From Background Thread](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet045TableViewerFillFromBackgroundThread.java)

*   [Snippet045 - Table Viewer Fill From Background Thread](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet045TableViewerFillFromBackgroundThread.java)

  
Demonstrates how a TableViewer with a sorter can be filled from a NON-UI thread

![Snippet045TableViewerFillFromBackgroundThread.gif](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/docs/images/Snippet045TableViewerFillFromBackgroundThread.gif)

### [Snippet046 - Update Viewer From Background Thread](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet046UpdateViewerFromBackgroundThread.java)

*   [Snippet046 - Update Viewer From Background Thread](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet046UpdateViewerFromBackgroundThread.java)

  
Demonstrates how to update a viewer from a long-running task (which is executed in a thread) and calls back to the UI-Thread using "asyncExec".

![Snippet046UpdateViewerFromBackgroundThread.gif](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/docs/images/Snippet046UpdateViewerFromBackgroundThread.gif)

### [Snippet047 - Virtual Lazy Tree Viewer](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet047VirtualLazyTreeViewer.java)

*   [Snippet047 - Virtual Lazy Tree Viewer](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet047VirtualLazyTreeViewer.java)

  
Demonstrates the usage of ILazyContentProvider in conjunction with a Virtual-TreeViewer. The snippet shows how using a lazy tree can minimize the memory footprint and maximize the speed when viewing large models.

![Snippet047VirtualLazyTreeViewer.png](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/docs/images/Snippet047VirtualLazyTreeViewer.png)

### [Snippet048 - Tree Viewer Tab With Checkbox](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet048TreeViewerTabWithCheckboxFor3_3.java)

*   [Snippet048 - Tree Viewer Tab With Checkbox](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet048TreeViewerTabWithCheckboxFor3_3.java)

  
Demonstrates how to overcome a limitation when it comes to key-navigation and CheckBoxEditors in 3.3.1.

This is a workaround for [Bug #198502](https://bugs.eclipse.org/bugs/show_bug.cgi?id=198502)

![Snippet048TreeViewerTabWithCheckboxFor3 3.png](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/docs/images/Snippet048TreeViewerTabWithCheckboxFor3_3.png)

### [Snippet049 - Styled Cell Label Provider](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet049StyledCellLabelProvider.java)

*   [Snippet049 - Styled Cell Label Provider](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet049StyledCellLabelProvider.java)

  
Demonstrates a LabelProvider-Type which uses StyleRanges. This Snippet requires SWT/JFace 3.4.

![Snippet049StyledCellLabelProvider.gif](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/docs/images/Snippet049StyledCellLabelProvider.gif)

### [Snippet050 - Delegating Styled Cell Label Provider](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet050DelegatingStyledCellLabelProvider.java)

*   [Snippet050 - Delegating Styled Cell Label Provider](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet050DelegatingStyledCellLabelProvider.java)

  
Demonstrates how you can add styled text by wrapping an existing label provider. This Snippet requires SWT/JFace 3.4.

![Snippet050DelegatingStyledCellLabelProvider.png](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/docs/images/Snippet050DelegatingStyledCellLabelProvider.png)

### [Snippet051 - Table Centered Image](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet051TableCenteredImage.java)

*   [Snippet051 - Table Centered Image](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet051TableCenteredImage.java)

  
Demonstrate how to center an image and create graphics in a cell using a technique called "owner draw".

![Snippet051TableCenteredImage.png](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/docs/images/Snippet051TableCenteredImage.png)

### [Snippet052 - Double Click Cell Editor](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet052DoubleClickCellEditor.java)

*   [Snippet052 - Double Click Cell Editor](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet052DoubleClickCellEditor.java)

  
Demonstrate how to start cell-editors on double click.

![Snippet052DoubleClickCellEditor.png](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/docs/images/Snippet052DoubleClickCellEditor.png)

### [Snippet053 - Start Editor With Context Menu](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet053StartEditorWithContextMenu.java)

*   [Snippet053 - Start Editor With Context Menu](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet053StartEditorWithContextMenu.java)

  
Demonstrate how to start up a cell editor with a context menu and not with mouse clicking on the cell.

![Snippet053StartEditorWithContextMenu.png](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/docs/images/Snippet053StartEditorWithContextMenu.png)

### [Snippet055 - Hide Show Column](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet055HideShowColumn.java)

*   [Snippet055 - Hide Show Column](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet055HideShowColumn.java)

  
Demonstrate hiding and showing columns and starting a cell editor programmatically.

![Snippet055HideShowColumn.png](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/docs/images/Snippet055HideShowColumn.png)

### [Snippet056 - Boolean Cell Editor](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet056BooleanCellEditor.java)

*   [Snippet056 - Boolean Cell Editor](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet056BooleanCellEditor.java)

  
Demonstrate a custom cell-editor which uses a real Checkbox-Button

You also need these classes:

*   [BooleanCellEditor.java](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/BooleanCellEditor.java)

  
![Snippet056BooleanCellEditor.png](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/docs/images/Snippet056BooleanCellEditor.png)

### [Snippet057 - Table Viewer Skip Hidden Cells](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet057TableViewerSkipHiddenCells.java)

*   [Snippet057 - Table Viewer Skip Hidden Cells](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet057TableViewerSkipHiddenCells.java)

  
Example of showing how easy cell-navigation with hidden cells is. Use the cursor keys to navigate between cells. Then use the context menu to hide a column.

![Snippet057TableViewerSkipHiddenCells.png](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/docs/images/Snippet057TableViewerSkipHiddenCells.png)

### [Snippet058 - Cell Navigation](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet058CellNavigationIn34.java)

*   [Snippet058 - Cell Navigation](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet058CellNavigationIn34.java)

  
Shows how to automatically reveal cells when navigating. Run the snippet and then edit the first cell by double-clicking. Pressing the tab key will advance to the next cell in edit mode and reveal the cell if it is not in the viewport.

![Snippet058CellNavigationIn34.gif](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/docs/images/Snippet058CellNavigationIn34.gif)

### [Snippet060 - Text Cell Editor With Content Proposal/Field assists](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet060TextCellEditorWithContentProposal.java)

*   [Snippet060 - Text Cell Editor With Content Proposal/Field assists](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet060TextCellEditorWithContentProposal.java)

  
Show how to use content-proposal inside a CellEditor

![Snippet060TextCellEditorWithContentProposal.png](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/docs/images/Snippet060TextCellEditorWithContentProposal.png)

### [Snippet061 - Faked Native Cell Editor](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet061FakedNativeCellEditor.java)

*   [Snippet061 - Faked Native Cell Editor](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet061FakedNativeCellEditor.java)

  
Full-featured native-looking viewer with checkboxes in an arbitrary column

You also need these classes:

*   [BooleanCellEditor.java](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/BooleanCellEditor.java)

  

![Snippet061FakedNativeCellEditor.png](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/docs/images/Snippet061FakedNativeCellEditor.png)

### [Snippet062 - Text And Dialog Cell Editor](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet062TextAndDialogCellEditor.java)

*   [Snippet062 - Text And Dialog Cell Editor](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet062TextAndDialogCellEditor.java)

  
Demonstrates usage of TextAndDialogCellEditor. The email column uses the TextAndDialogCellEditor; othe columns use ordinary TextCellEditor.

You also need these classes:

*   [TextAndDialogCellEditor.java](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/TextAndDialogCellEditor.java)

  
![Snippet062TextAndDialogCellEditor.png](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/docs/images/Snippet062TextAndDialogCellEditor.png)

### [Snippet063 - Combo Viewer](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet063ComboViewer.java)

*   [Snippet063 - Combo Viewer](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet063ComboViewer.java)

  
Show how to use ComboViewer and set an initial selection

![Snippet063ComboViewer.png](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/docs/images/Snippet063ComboViewer.png)

### [Snippet064 - Replacing elements in a TreeViewer with child elements](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet064TreeViewerReplacingElements.java)

*   [Snippet064 - Replacing elements in a TreeViewer with child elements](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet064TreeViewerReplacingElements.java)

  
A TreeViewer with observable collections as input, to demonstrate, how elements are replaced, especially what happens to selected items on replacement

![Snippet064TreeViewerReplacingElements.png](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/docs/images/Snippet064TreeViewerReplacingElements.png)

### [Snippet065 - Replacing elements in a TreeViewer without child elements](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet065TableViewerReplacingElements.java)

*   [Snippet065 - Replacing elements in a TreeViewer without child elements](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet065TableViewerReplacingElements.java)

  
A TreeViewer with observable collections as input, to demonstrate, how elements are replaced, especially what happens to selected items on replacement

![Snippet065TableViewerReplacingElements.png](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/docs/images/Snippet065TableViewerReplacingElements.png)

### [Snippet066 - TableViewer with Label Decorator](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet066TableViewerWithLabelDecorator.java)

*   [Snippet066 - TableViewer with Label Decorator](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet066TableViewerWithLabelDecorator.java)

  
A TableViewer that shows how to add a status icon to a Label with IStyledLabelProvider and DecorationOverlayIcon

![Snippet066TableViewerWithLabelDecorator.png](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/docs/images/Snippet066TableViewerWithLabelDecorator.png)

Window
------

### [Snippet020 - Customized Control Tooltips](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/window/Snippet020CustomizedControlTooltips.java)

*   [Snippet020 - Customized Control Tooltips](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/window/Snippet020CustomizedControlTooltips.java)

  
Demonstrates usage of JFace 3.3 to show really cool ToolTips for your controls

For full fun you also need:

*   [Help Icon](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/window/linkto_help.gif)
*   [Error Icon](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/window/showerr_tsk.gif)

  
![Snippet020CustomizedControlTooltips.png](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/docs/images/Snippet020CustomizedControlTooltips.png)

### [Snippet031 - Table Static Tooltip](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/window/Snippet031TableStaticTooltip.java)

*   [Snippet031 - Table Static Tooltip](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/window/Snippet031TableStaticTooltip.java)

  
Demonstrates creation of ToolTips for Tables without using the JFace-Viewers API but only JFace-Tooltips

![Snippet031TableStaticTooltip.png](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/docs/images/Snippet031TableStaticTooltip.png)

Wizard
------

### [Snippet047 - Wizard with Long Running Operation from Page](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/wizard/Snippet047WizardWithLongRunningOperation.java)

*   [Snippet047 - Wizard with Long Running Operation from Page](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/wizard/Snippet047WizardWithLongRunningOperation.java)

  
Demonstrates how to work with JFace-Wizards and fill a TableViewer from a Background-Thread without blocking the UI showing a progress bar in the meanwhile

![Snippet047.gif](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/docs/images/Snippet047.gif)

### [Snippet071 - Wizard with Progress and Cancel](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/wizard/Snippet071WizardWithProgressAndCancel.java)

*   [Snippet071 - Wizard with Progress and Cancel](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/wizard/Snippet071WizardWithProgressAndCancel.java)

  
Demonstrates a wizard with internal progress.

![Snippet071WizardWithProgressAndCancel.gif](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/docs/images/Snippet071WizardWithProgressAndCancel.gif)

### [Snippet072 Wizard with Progress Subtasks and Cancel](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/wizard/Snippet072WizardWithProgressSubtasksAndCancel.java)

*   [Snippet072 Wizard with Progress Subtasks and Cancel](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/wizard/Snippet072WizardWithProgressSubtasksAndCancel.java)

[Demonstrates a wizard with internal progress using SubMonitor and subtasks.](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/FocusBorderCellHighlighter.java)

![Snippet072WizardWithProgressSubtasksAndCancel.png](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/docs/images/Snippet072WizardWithProgressSubtasksAndCancel.png)

  

### [Snippet074 Wizard with access to application window](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/wizard/Snippet074ModelesWizard.java)

*   [Snippet074 Wizard with access to application window](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/wizard/Snippet074ModelesWizard.java)

  
Demonstrates a wizard that enables access to the calling shell...

![Snippet074ModelesWizard.gif](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/docs/images/Snippet074ModelesWizard.gif)

