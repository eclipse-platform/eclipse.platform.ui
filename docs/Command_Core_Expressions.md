Command Core Expressions
========================

Core expressions are declarative or programmatic expressions based on the org.eclipse.core.expressions plugin.

Contents
--------

*   [1 Expressions and the Command Framework](#Expressions-and-the-Command-Framework)
*   [2 Variables and the Command Framework](#Variables-and-the-Command-Framework)
*   [3 Property Testers](#Property-Testers)
*   [4 Expression examples](#Expression-examples)
    *   [4.1 Basic IStructuredSelection](#Basic-IStructuredSelection)
    *   [4.2 Package Explorer IStructuredSelection](#Package-Explorer-IStructuredSelection)
    *   [4.3 Active editor type](#Active-editor-type)
    *   [4.4 Complex nested boolean expressions](#Complex-nested-boolean-expressions)
*   [5 New Core Expressions in 3.3](#New-Core-Expressions-in-3.3)
    *   [5.1 count and iterate](#count-and-iterate)
    *   [5.2 definitions](#definitions)

Expressions and the Command Framework
=====================================

The [Platform Command Framework](https://github.com/eclipse-platform/eclipse.platform.ui/blob/master/docs/PlatformCommandFramework.md) uses [core expressions](https://github.com/eclipse-platform/eclipse.platform.ui/blob/master/docs/Platform_Expression_Framework.md) for enabledWhen and activeWhen for handlers, programmatic activation of contexts, and for visibleWhen for menu contributions. 
The command framework provides the IEvaluationContext that command core expressions are evaluate against.

The IEvaluationContext provides a default variable for evaluations, and a number of named variables. 
In the command framework, we provide the global selection as a `java.util.Collection` as the default variable. 
It can either be empty, have one entry (if the ISelection was something like an ITextSelection), or have the contents of an IStructuredSelection.

The <with/> element can be used to change which variable the child expression elements are evaluating against.

Variables and the Command Framework
===================================

The variables used for command framework evaluation are listed in [ISources.java](https://git.eclipse.org/c/platform/eclipse.platform.ui.git/tree/bundles/org.eclipse.ui.workbench/Eclipse%20UI/org/eclipse/ui/ISources.java)

Some of the variables may not be set, depending on the current application context when they are evaluated.

| Name | Type | Description | Since |
| --- | --- | --- | --- |
| activeContexts | A `java.util.Collection` of `java.lang.String` |   This is a collection of the active context IDs as strings. Most commonly used with <iterate/>, <count/>, and <test/> with a combined `org.eclipse.common.expressions.PropertyTester`. In **3.3** action sets are mirrored by contexts whose parent is `org.eclipse.ui.actionSet`, and the active action sets show up in the list of active contexts.   | 3.2 |
| activeActionSets | An `IActionSetDescriptor\[\]` |   **Note:** This is currently not used as it points to an internal class and the type might change in any release.   | 3.2 |
| activeShell | `org.eclipse.swt.widgets.Shell` |   The currently active shell. It can be a dialog or workbench window shell.   | 3.2 |
| activeWorkbenchWindowShell | `org.eclipse.swt.widgets.Shell` |   The active workbench window shell.   | 3.2 |
| activeWorkbenchWindow | `org.eclipse.ui.IWorkbenchWindow` |   The active workbench window.   | 3.2 |
| activeWorkbenchWindow.<br>isCoolbarVisible | `java.lang.Boolean` |   Reports coolbar visibility for the currently active workbench window.   | 3.3 |
| activeWorkbenchWindow.<br>isPerspectiveBarVisible | `java.lang.Boolean` |   Reports perspective bar visibility for the currently active workbench window.   | 3.3 |
| activeWorkbenchWindow.<br>activePerspective | `java.lang.String` |   Reports the name of the current perspective of the active workbench window.   | 3.4 |
| activeEditor | `org.eclipse.ui.IEditorPart` |   The currently active editor. This is remembered even if the editor is not the currently active part.   | 3.2 |
| activeEditorId | `java.lang.String` |   The ID of the currently active editor. This can be used for expressions on the editor type.   | 3.2 |
| activePart | `org.eclipse.ui.IWorkbenchPart` |   The active part, which can be the same as the active editor.   | 3.2 |
| activePartId | `java.lang.String` |   The ID of the currently active part.   | 3.2 |
| activeSite | `org.eclipse.ui.IWorkbenchPartSite` |   The site of the currently active part.   | 3.2 |
| selection | `org.eclipse.jface.viewers.ISelection` |   The current global selection. It is often used with <test/> elements with `org.eclipse.core.expressions.PropertyTester`, in programmatic core expressions, and in **3.3** with <iterate/> and <count/> elements.   | 3.2 |
| activeMenu | A `java.util.Collection` of `java.lang.String` |   This is the list of IDs of the showing context menu. Examples are like #TextEditorRuler or a part ID. Most commonly used with <iterate/>, <count/>, and <test/> with a combined `org.eclipse.common.expressions.PropertyTester`.   | 3.2 |
| activeMenuSelection | `org.eclipse.jface.viewers.ISelection` |   This is a selection that is available while a context menu is showing. It is the selection from the selection provider used to register the context menu, usually from `getSite().registerContextMenu(*)`. It is usually the same as the `selection`variable, but not always. This is more for legacy compatibility.   | 3.3 |
| activeMenuEditorInput | `org.eclipse.jface.viewers.ISelection` |   This is a selection that is available while a context menu is showing. It is the selection from the editor input, usually if includeEditorInput was set to `true` during `getEditorSite().registerContextMenu(*)`. This is more for legacy compatibility.   | 3.3 |
| activeFocusControl | `org.eclipse.swt.widgets.Control` |   A control that has focus and has been registered with the [IFocusService](https://git.eclipse.org/c/platform/eclipse.platform.ui.git/tree/bundles/org.eclipse.ui.workbench/Eclipse%20UI/org/eclipse/ui/swt/IFocusService.java).   | 3.3 |
| activeFocusControlId | `java.lang.String` |   The ID of a control that has focus and has been registered with the `org.eclipse.ui.swt.IFocusService`.   | 3.3 |

Note: All these variables can be used with <test/> and a `org.eclipse.common.expressions.PropertyTester`.

Property Testers
================

The Eclipse SDK provides a couple of property testers that can be used in core expressions. 
The expression defines a property attribute and then takes a combination of args and a value that is tester implementation dependent. 
The property attribute is the combination of the namespace and property name. 
For example, to test an IResource name the property would be `org.eclipse.core.resources.name`.

  

||||
| --- | --- | --- |
| **Namespace** | **Type** | **Implementation** |
| org.eclipse.core.runtime |   `org.eclipse.core.runtime.Platform`   |   [PlatformPropertyTester.java](https://git.eclipse.org/c/platform/eclipse.platform.runtime.git/tree/bundles/org.eclipse.core.expressions/src/org/eclipse/core/internal/expressions/propertytester/PlatformPropertyTester.java)   |
| **Property** | **Description**| |
|   product   |   Test the id of the currently active product.   ||
|   isBundleInstalled   |   Test if a given bundle is installed in the running environment. Use the args attribute to pass in the bundle id.   ||
|  |  |  |
| **Namespace**| **Type**| **Implementation** |
| org.eclipse.core.resources |   `org.eclipse.core.resources.IResource`   |   [ResourcePropertyTester.java](https://git.eclipse.org/c/platform/eclipse.platform.resources.git/tree/bundles/org.eclipse.core.resources/src/org/eclipse/core/internal/propertytester/ResourcePropertyTester.java)   |
| **Property** | **Description** |  |
|   name   |   A property indicating the file name (value `"name"`). "*" and "?" wild cards are supported.   |  |
|   path   |   A property indicating the file path (value `"path"`). "*" and "?" wild cards are supported.   |  |
|   extension   |   A property indicating the file extension (value `"extension"`). "*" and "?" wild cards are supported.   |  |
|   readOnly   |   A property indicating whether the file is read only (value `"readOnly"`).   |  |
|   projectNature   |   A property indicating the project nature (value `"projectNature"`).   |  |
|   persistentProperty   |   A property indicating a persistent property on the selected resource (value `"persistentProperty"`). If two arguments are given, this treats the first as the property name, and the second as the expected property value. If only one argument (or just the expected value) is given, this treats it as the property name, and simply tests for existence of the property on the resource.   |  |
|   projectPersistentProperty   |   A property indicating a persistent property on the selected resource's project. (value `"projectPersistentProperty"`). If two arguments are given, this treats the first as the property name, and the second as the expected property value. If only one argument (or just the expected value) is given, this treats it as the property name, and simply tests for existence of the property on the resource.   |  |
|   sessionProperty   |   A property indicating a session property on the selected resource (value `"sessionProperty"`). If two arguments are given, this treats the first as the property name, and the second as the expected property value. If only one argument (or just the expected value) is given, this treats it as the property name, and simply tests for existence of the property on the resource.   |  |
|   projectSessionProperty   |   A property indicating a session property on the selected resource's project. (value `"projectSessionProperty"`). If two arguments are given, this treats the first as the property name, and the second as the expected property value. If only one argument (or just the expected value) is given, this treats it as the property name, and simply tests for existence of the property on the resource.   |  |
|  |  |  |
| **Namespace** | **Type** | **Implementation** |
| org.eclipse.core.resources |   `org.eclipse.core.resources.IFile`   |   [FilePropertyTester.java](https://git.eclipse.org/c/platform/eclipse.platform.resources.git/tree/bundles/org.eclipse.core.resources/src/org/eclipse/core/internal/propertytester/FilePropertyTester.java)   |
| **Property** | **Description** |  |
|   contentTypeId   |   A property indicating that we are looking to verify that the file matches the content type matching the given identifier. The identifier is provided as the expected value.   |  |
|  |  |  |
| **Namespace** | **Type** | **Implementation** |
| org.eclipse.core.resources |   `org.eclipse.core.resources.IProject`   |   [ProjectPropertyTester.java](https://git.eclipse.org/c/platform/eclipse.platform.resources.git/tree/bundles/org.eclipse.core.resources/src/org/eclipse/core/internal/propertytester/ProjectPropertyTester.java)   |
| **Property**| **Description** |  |
|   open   |   A property indicating whether the project is open (value `"open"`).   |  |
|  |  |  |
| **Namespace** | **Type** | **Implementation** |
| org.eclipse.core.resources |   `org.eclipse.core.resources.mapping.ResourceMapping`   |   [ResourceMappingPropertyTester.java](https://git.eclipse.org/c/platform/eclipse.platform.resources.git/tree/bundles/org.eclipse.core.resources/src/org/eclipse/core/internal/propertytester/ResourceMappingPropertyTester.java)   |
| **Property** | **Description** |  |
|   projectPersistentProperty   |   A property indicating a persistent property on the selected resource's project. (value `"projectPersistentProperty"`). If two arguments are given, this treats the first as the property name, and the second as the expected property value. If only one argument (or just the expected value) is given, this treats it as the property name, and simply tests for existence of the property on the resource.   |  |
|  |  |  |
| **Namespace** | **Type** | **Implementation** |
| org.eclipse.ui |   `org.eclipse.ui.IWorkbench`   |   [ActivityPropertyTester.java](https://git.eclipse.org/c/platform/eclipse.platform.ui.git/tree/bundles/org.eclipse.ui.workbench/Eclipse%20UI/org/eclipse/ui/internal/activities/ActivityPropertyTester.java)   |
| **Property** | **Description** |  |
|   isActivityEnabled   |   Test if the activity in args is enabled.   |  |
|   isCategoryEnabled   |   Test if the category in args is enabled.   |  |
|  |  |  |
| **Namespace** | **Type** | **Implementation** |
| org.eclipse.ui.workbenchWindow |   `org.eclipse.ui.IWorkbenchWindow`   |   [OpenPerspectivePropertyTester.java](https://git.eclipse.org/c/platform/eclipse.platform.ui.git/tree/bundles/org.eclipse.ui.workbench/Eclipse%20UI/org/eclipse/ui/internal/OpenPerspectivePropertyTester.java)   |
| **Property** | **Description** |  |
|   isPerspectiveOpen   |   Tests if any perspective is open.   |  |

Expression examples
===================

Here are some examples. I'll pretend all of the examples are deciding when a handler is active.

Basic IStructuredSelection
--------------------------

A view provides a structured selection through its selection provider. 
An example would be the InfoView in **org.eclipse.ui.examples.contributions**. 
You can browse the [plugin.xml](http://git.eclipse.org/c/platform/eclipse.platform.ui.git/tree/examples/org.eclipse.ui.examples.contributions/plugin.xml) and [InfoView.java](http://git.eclipse.org/c/platform/eclipse.platform.ui.git/tree/examples/org.eclipse.ui.examples.contributions/src/org/eclipse/ui/examples/contributions/view/InfoView.java) files. 
The InfoView provides an `IStructuredSelection` with 0 or more `org.eclipse.ui.examples.contributions.model.Person`.

When using the default variable, you must treat it as an `java.util.Collection`. 
That means using <count> or <iterate>

    <activeWhen>
        <iterate>
           <instanceof value="org.eclipse.ui.examples.contributions.model.Person"/>
        </iterate>
    </activeWhen>

Package Explorer IStructuredSelection
-------------------------------------

The Package Explorer is a mixture of `org.eclipse.core.resources.IResource`, `org.eclipse.jdt.core.IJavaElement` and other classes. 
If you are trying to find all of the *.java files, you would need to:

1.  Iterate through the default variable
2.  adapt the selection elements to your class, in this case `IResource`
3.  use one of the org.eclipse.core.resources property testers to test the IResource property

For example:

    <activeWhen>
        <iterate>
           <adapt type="org.eclipse.core.resources.IResource">
              <test property="org.eclipse.core.resources.name" 
                    value="*.java"/>
           </adapt>
        </iterate>
    </activeWhen>

Active editor type
------------------

If you want your handler to be active for a specific type of editor, you can use **activeEditorId** to target your handler.

    <activeWhen>
        <with variable="activeEditorId">
           <equals value="org.eclipse.ui.DefaultTextEditor"/>
        </with>
    </activeWhen>

Complex nested boolean expressions
----------------------------------

You can also write complex nested boolean expressions, like **(a & b & (c | d | (!e)))**:

    <and>
      <test args="a" property="rcpAuthActivitiesExample.test" />
      <test args="b" property="rcpAuthActivitiesExample.test" />
      <or>
        <test args="c" property="rcpAuthActivitiesExample.test" />
        <test args="d" property="rcpAuthActivitiesExample.test" />
        <not>
          <test args="e" property="rcpAuthActivitiesExample.test" />
        </not>
      </or>
    </and>

You can build the complete boolean expression out of arbitrary single boolean expressions. 
Not only property testers like in this example.

New Core Expressions in 3.3
===========================

In 3.3 there were 2 additions to the core expressions framework.

count and iterate
-----------------

Count and iterate have always worked against `java.util.Collection`. 
The <count/> and <iterate> elements can now be used on any variable that adapts to `org.eclipse.core.expressions.ICountable` and `org.eclipse.core.expressions.IIterable` or implements the interfaces directly. 
It wasn't possible to use the java 1.5 constructs for iterable.

The workbench provides an adapter for `ISelection` and `IStructuredSelection`.

definitions
-----------

The **org.eclipse.core.expressions.definitions** extension point was introduced. 
You can create core expression definitions, and then reference them from other core expressions.

    <extension point="org.eclipse.core.expressions.definitions">
        <definition id="org.eclipse.ui.examples.contributions.view.inView">
           <with variable="activePartId">
              <equals value="org.eclipse.ui.examples.contributions.view"/>
           </with>
        </definition>
    </extension>

Then:

    <activeWhen>
        <reference definitionId="org.eclipse.ui.examples.contributions.view.inView"/>
    </activeWhen>

The referenced expression will be evaluated at this point.

