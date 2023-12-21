

JFace Data Binding/Snippets
===========================

< [JFace Data Binding](/JFace_Data_Binding "JFace Data Binding")

| **JFace Data Binding** |
| :-: |
| [Home](/JFace_Data_Binding "JFace Data Binding") |
| [How to Contribute](/JFace_Data_Binding/How_to_Contribute "JFace Data Binding/How to Contribute") |
| [FAQ](/JFace_Data_Binding/FAQ "JFace Data Binding/FAQ") |
| **Snippets** |
| Concepts |
| [Binding](/JFace_Data_Binding/Binding "JFace Data Binding/Binding") |
| [Converter](/JFace_Data_Binding/Converter "JFace Data Binding/Converter") |
| [Observable](/JFace_Data_Binding/Observable "JFace Data Binding/Observable") |
| [Realm](/JFace_Data_Binding/Realm "JFace Data Binding/Realm") |

Snippets show how common use cases can be implemented using the JFace Data Binding framework. They are typically a single self-contained Java class with a main method. See the bottom of this page for additional instructions on how to get and run the snippets.

Contents
--------

*   [1 Running the Snippets](#Running-the-Snippets)
    *   [1.1 Basic](#Basic)
    *   [1.2 WizardPage](#WizardPage)
    *   [1.3 ComputedValue](#ComputedValue)
    *   [1.4 Bindings](#Bindings)
    *   [1.5 Master Detail](#Master-Detail)
    *   [1.6 SWT](#SWT)
    *   [1.7 Viewers](#Viewers)
    *   [1.8 Additional Run Options](#Additional-Run-Options)

Running the Snippets
--------------------

*   [Create an Eclipse plug-in project](http://help.eclipse.org/juno/index.jsp?topic=%2Forg.eclipse.rse.doc.isv%2Fguide%2Ftutorial%2FpdeProject.html) and in the Manifest editor go to the **Dependencies tab**
*   Add dependencies to the following bundles: org.eclipse.core.databinding, org.eclipse.core.databinding.beans, org.eclipse.core.databinding.property, org.eclipse.jface.databinding, org.eclipse.swt, and org.eclipse.core.runtime
*   Click on any snippet in this list
*   Copy (CTRL+C) the source to your clipboard
*   Go to the src folder of your project and paste (CTRL+V). This will automatically create the correct package and source file.
*   Right click on the new source file and select "Run as / Java Application"

### Basic

*   [Hello World](http://git.eclipse.org/c/platform/eclipse.platform.ui.git/plain/examples/org.eclipse.jface.examples.databinding/src/org/eclipse/jface/examples/databinding/snippets/Snippet000HelloWorld.java) \- the most basic of bindings

### WizardPage

*   [Wizard Dialog](http://git.eclipse.org/c/platform/eclipse.platform.ui.git/plain/examples/org.eclipse.jface.examples.databinding/src/org/eclipse/jface/examples/databinding/snippets/Snippet014WizardDialog.java) \- Shows how to use data binding in a wizard dialog so that validation results are displayed in the dialog's title area

### ComputedValue

*   [Spreadsheet](http://git.eclipse.org/c/platform/eclipse.platform.ui.git/plain/examples/org.eclipse.jface.examples.databinding/src/org/eclipse/jface/examples/databinding/snippets/Snippet006Spreadsheet.java) \- fills a Table updating cells upon change
*   [Name Formatter](http://git.eclipse.org/c/platform/eclipse.platform.ui.git/plain/examples/org.eclipse.jface.examples.databinding/src/org/eclipse/jface/examples/databinding/snippets/Snippet008ComputedValue.java) \- observable value that updates when the first or last name changes

### Bindings

*   [Bind validation status to a Label](http://git.eclipse.org/c/platform/eclipse.platform.ui.git/plain/examples/org.eclipse.jface.examples.databinding/src/org/eclipse/jface/examples/databinding/snippets/Snippet004DataBindingContextErrorLabel.java)
*   [Validate observables across Bindings](http://git.eclipse.org/c/platform/eclipse.platform.ui.git/plain/examples/org.eclipse.jface.examples.databinding/src/org/eclipse/jface/examples/databinding/snippets/Snippet011ValidateMultipleBindingsSnippet.java?view=markup)

### Master Detail

*   [Master detail](http://git.eclipse.org/c/platform/eclipse.platform.ui.git/plain/examples/org.eclipse.jface.examples.databinding/src/org/eclipse/jface/examples/databinding/snippets/Snippet010MasterDetail.java) \- display the detail of the selection of a ListViewer in a Text widget
*   [Nested Selection With ComboViewer](http://git.eclipse.org/c/platform/eclipse.platform.ui.git/plain/examples/org.eclipse.jface.examples.databinding/src/org/eclipse/jface/examples/databinding/snippets/Snippet001NestedSelectionWithCombo.java)

### SWT

*   [MenuUpdater](http://git.eclipse.org/c/platform/eclipse.platform.ui.git/plain/examples/org.eclipse.jface.examples.databinding/src/org/eclipse/jface/examples/databinding/snippets/Snippet005MenuUpdater.java)
*   [CompositeUpdater](http://git.eclipse.org/c/platform/eclipse.platform.ui.git/plain/examples/org.eclipse.jface.examples.databinding/src/org/eclipse/jface/examples/databinding/snippets/Snippet012CompositeUpdater.java)

### Viewers

*   [Model to TableViewer binding](http://git.eclipse.org/c/platform/eclipse.platform.ui.git/plain/examples/org.eclipse.jface.examples.databinding/src/org/eclipse/jface/examples/databinding/snippets/Snippet009TableViewer.java) \- basic binding to a TableViewer
*   [TableViewer binding with colors](http://git.eclipse.org/c/platform/eclipse.platform.ui.git/plain/examples/org.eclipse.jface.examples.databinding/src/org/eclipse/jface/examples/databinding/snippets/Snippet007ColorLabelProvider.java) \- label provider that provides Colors and auto updates the viewer
*   [TableViewer inline editing](http://git.eclipse.org/c/platform/eclipse.platform.ui.git/plain/examples/org.eclipse.jface.examples.databinding/src/org/eclipse/jface/examples/databinding/snippets/Snippet013TableViewerEditing.java) \- TableViewer editing with the Eclipse 3.3 JFace viewer APIs. _(requires Eclipse 3.3)_
*   [ComputedList and Combo](http://git.eclipse.org/c/platform/eclipse.platform.ui.git/plain/examples/org.eclipse.jface.examples.databinding/src/org/eclipse/jface/examples/databinding/snippets/Snippet022ComputedListCombo.java) \- Filter the elements in one viewer based on the selection in another
*   [ComboViewer and Java enum](http://git.eclipse.org/c/platform/eclipse.platform.ui.git/plain/examples/org.eclipse.jface.examples.databinding/src/org/eclipse/jface/examples/databinding/snippets/Snippet034ComboViewerAndEnum.java) \- Demonstrates binding the list of a ComboViewer to the values of an enum and the selected value to a simple model property.

### Additional Run Options

A great way to learn is to look at concrete examples that you can run and experiment with.

You can clone the [eclipse.platform.ui git repository](http://git.eclipse.org/c/platform/eclipse.platform.ui.git/) which contains the "org.eclipse.jface.examples.databinding" project with the JFace Databinding examples. For cloning a git repository you can use the [EGit](http://eclipse.org/egit/) tooling of Eclipse.

Use the following URL to clone the repository via File -> Import -> Git repository

*   [http://anonymous@git.eclipse.org/gitroot/platform/eclipse.platform.ui.git](http://anonymous@git.eclipse.org/gitroot/platform/eclipse.platform.ui.git)

After you cloned the project the clone wizard will allow you to import the included projects. For the databinding examples you only have to import the "org.eclipse.jface.examples.databinding" project. For an introduction into EGit please see [EGit](http://www.vogella.de/articles/EGit/article.html)

Most of the examples provide a main method, you can run it as a Java Application to see what happens.  

You can also copy any of the snippets (see below) into a scratch project within Eclipse. If you are copying single snippets into a scratch project, make sure that it is set up with the correct dependencies. You will need org.eclipse.core.databinding, org.eclipse.core.databinding.beans, and org.eclipse.jface.databinding.

