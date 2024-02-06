Platform Expression Framework
=============================

Expressions are declarative or programmatic expressions based on the org.eclipse.core.expressions plugin. 
They are declared in plugin.xml and evaluated by the Expressions Framework. 
The advantages of declaring an expression in plugin.xml are:

*   Lazy loading: Expressions can be evaluated without loading the plug-in
*   Flexible and pluggable, users can re-use expressions and provide custom property testers

  

Contents
--------

*   [1 Where they are useful](#Where-they-are-useful)
*   [2 Declaration of Expressions](#Declaration-of-Expressions)
    *   [2.1 Re-Usable expressions](#Re-Usable-expressions)
*   [3 Evaluation Context](#Evaluation-Context)
    *   [3.1 Evaluation of Collections](#Evaluation-of-Collections)
    *   [3.2 Additional Variables](#Additional-Variables)
*   [4 Operators of the expressions framework](#Operators-of-the-expressions-framework)
    *   [4.1 adapt](#adapt)
    *   [4.2 and / or / not](#and--or--not)
    *   [4.3 count](#count)
    *   [4.4 equals](#equals)
    *   [4.5 instanceof](#instanceof)
    *   [4.6 iterate](#iterate)
    *   [4.7 reference](#reference)
    *   [4.8 resolve](#resolve)
    *   [4.9 systemTest](#systemTest)
    *   [4.10 test](#test)
    *   [4.11 with](#with)
*   [5 Property Testers](#Property-Testers)
*   [6 Links](#Links)

Where they are useful
=====================

Expressions are used in extension points that have to decide things based on a context, but without loading the plugin implementing that decision. 
The most popular examples where they are used are the [Platform Command Framework](https://github.com/eclipse-platform/eclipse.platform.ui/blob/master/docs/PlatformCommandFramework.md), and the 
[Common Navigator Framework](https://github.com/eclipse-platform/eclipse.platform.ui/blob/master/docs/Common_Navigator_Framework.md). 
Depending on the extension implementation, expressions are used to decide any number of things. 
Examples:

*   Should an context menu be enabled and/or visible in a context menu
*   Which implementation for a command handler to use depending on the current context
*   Which label provider to use for an object
*   Which content provider can provide children for an object in a tree

It is also possible to use the expression framework in custom extension points.

Declaration of Expressions
==========================

First of all, an expression must be defined in plugin.xml. 
Here is an example for an enablement expression in the common navigator framework:

     <enablement>
         <or>
             <instanceof value="com.acme.navigator.ContainerObject"/>
             <instanceof value="com.acme.navigator.RootObject"/>
             <adapt type="org.eclipse.core.resources.IResource">
                 <test
                       property="org.eclipse.core.resources.projectNature"
                       value="com.acme.navigator.nature">
                 </test>
             </adapt>
         </or>
     </enablement>

Note how the `<or>` element contains three elements: two `<instanceof>` and one `<adapt>`. 
The `<adapt>` contains one further `<test>` element; what they all do will be explained later. 
For now just focus on the structure of the expression: when it is evaluated, the result of the instanceof and the adapt tests will be logically or-ed because they are nested in a `<or>` element. 
The same expression could also be written as follows in a [polish pseudo-notation](http://en.wikipedia.org/wiki/Polish_notation):

    or (
        instanceof com.acme.navigator.ContainerObject,
        instanceof com.acme.navigator.RootObject,
        and (
            adapt org.eclipse.core.resources.IResource,
            test org.eclipse.core.resources.projectNature = "com.acme.navigator.nature"
        )
    )
    
  

Re-Usable expressions
---------------------

Sometimes you will end up with having the same expression in many different places. 
When one of them changes, you have to change them all. 
Obviously, this is inefficient and not very handy - let alone error prone. 
You can get around this problem by using definitions and re-use expressions that are declared elsewhere.

The expression from the example above can be declared using the `org.eclipse.core.expressions.definitions` extension point, and then re-used using the `<reference>` element:

     <extension
           point="org.eclipse.core.expressions.definitions">
        <definition id="org.acme.navigator.enablement">
           <or>
              <instanceof
                    value="com.acme.navigator.ContainerObject">
              </instanceof>
              <instanceof
                    value="com.acme.navigator.RootObject">
              </instanceof>
              <adapt type="org.eclipse.core.resources.IResource">
                 <test
                       property="org.eclipse.core.resources.projectNature"
                       value="com.acme.navigator.nature">
                 </test>
              </adapt>
           </or>
        </definition>
     </extension>

Then you can just use `<reference>` to that definition:

     <enablement>
        <reference
              definitionId="org.acme.navigator.enablement">
        </reference>
     </enablement>

The definition can be declared in any plug-in, and then cross-referenced from all other plugins. 
They don't even have to have a dependency on each other.

Evaluation Context
==================

An expression alone means nothing without the object that is being tested. 
Which object that is, and what it contains, is defined by the evaluation context. 
The content of the evaluation context depends on who is initiating the evaluation.

An evaluation context contains:

*   The default variable
*   Optional additional variables

Usually, expressions check the default variable in the evaluation context. 
However, it is possible for an expression to select a specific variable from the context using the `<with>` element (examples below).

A common source for confusion are the different default variables provided by the [Common Navigator Framework](https://github.com/eclipse-platform/eclipse.platform.ui/blob/master/docs/Common_Navigator_Framework.md) and the [Platform Command Framework](https://github.com/eclipse-platform/eclipse.platform.ui/blob/master/docs/PlatformCommandFramework.md). 
While the command framework provides a _collection containing the current selection_ as the default variable, the navigator framework just uses _the current object in the tree_. 
For this reason, you cannot use the same expression for both frameworks.

  

Evaluation of Collections
-------------------------

When dealing with a collection, you usually want to test against the _contents_ of the collection, and not the collection itself. 
For this case, the expressions framework provides `<iterate>` and `<count>`. 
They require an iterable object to work (otherwise they fail with an error message on the console).

The default variable in the command framework always contains a collection with the current selection. 
It might be empty, contain just one element (like an `ITextSelection`), or contain the elements of an `IStructuredSelection`.

The following expression will work for the common navigator framework, but _not_ for the commands framework:

     <enablement>
        <instanceof
             value="org.acme.navigator.RootObject">
        </instanceof>
     </enablement>

Doing the same evaluation against the content of a collection:

     <visibleWhen>
         <iterate ifEmpty="false">
             <instanceof
                  value="org.acme.navigator.RootObject">
             </instanceof>
         </iterate>
     </visibleWhen>

This iterates over all elements in the collection and tests if all elements are an instance of `org.acme.navigator.RootObject`. 
Note the `ifEmpty="false"`: this tells the expression framework that the evaluation should be false for an empty selection, which defaults to `true` (good tip: check this if you happen to see lots and lots of stuff in your context menu that should not be there, your selection might be empty).

Count and iterate have always worked against java.util.Collection. 
The count and iterate elements can also be used on any variable that adapts to `org.eclipse.core.expressions.ICountable` or `org.eclipse.core.expressions.IIterable`, or implements these interfaces directly.

Additional Variables
--------------------

An evaluation context (at least the ones provided by eclipse core) usually contains a whole bunch of additional variables, like:

*   **activePart**: the currently active part (could f.i. be an instance of `org.eclipse.ui.navigator.CommonNavigator`)
*   **activePartId**: the id of the currently active part (like org.acme.navigator)
*   **activeEditorId**: the id of the currently active editor
*   **activeWorkbenchWindowShell**: the currently active shell

The variables are defined in `ISources` (try Ctrl+Shift+T to open that one from within JDT) and there are others. 
Note that not all those variables actually contain something at runtime, and that some of them might not be set in the context.

For a complete list of variables, read the [eclipse documentation on core expressions](http://help.eclipse.org/galileo/index.jsp?topic=/org.eclipse.platform.doc.isv/guide/workbench_cmd_expressions.htm).

Operators of the expressions framework
======================================

Currently, the expression framework supports a total of 13 operation elements. 
Some of them can have further operations in them (like `<adapt>`). 
If that is the case, the result of the nested operations will be logically and-ed.

adapt
-----

Checks if the evaluated object is either an instance of, or adapts to the given class. 
Can contain nested elements that will be logically and-ed. See `AdaptExpression.evaluate()`.

  

and / or / not
--------------

The usual boolean operators, can contain nested elements. 
Do what they say on the tin.

  

count
-----

Used to count the elements in a collection. 
Cannot contain nested elements, but can be used in combination with `<iterate>`. 
`<count>` has one argument "value", wich can be one of the following:

*   `*`: matches any number (even 0)
*   `?`: one or none
*   `!`: none
*   `+`: one or more
*   `-NN)`: less than _NN_ (_NN_ is an integer)
*   `(NN-`: greater than _NN_
*   `NN`: exactly _NN_

So, the following example will match all collections that contain 2 elements:

     <visibleWhen>
         <count value="2"/>
     </visibleWhen>

In combination with `<iterate>`, you can count the elements in the collection that match the expression inside the iterate statement. 
For example, to match all collections that contain two or more `ContainerObject` objects, use:

     <visibleWhen>
         <count value="(1-"/>
         <iterate ifEmpty="false">
             <instanceof value="org.acme.navigator.ContainerObject"/>
         </iterate>
     </visibleWhen>

equals
------

Checks if the variable equals the given argument.

*   Numbers will be treated as such (float or integer)
*   Strings can be un-escaped: use `\\string\`
*   "true" will be Boolean.TRUE
*   "false" will be Boolean.FALSE
*   Everything else will be treated as string.

Example:

     <visibleWhen>
         <equals value="3.4"/>
     </visibleWhen>

instanceof
----------

Tests if the object under inspection is an instance of the given class.

  

iterate
-------

Iterates over the contents of a Collection (the evaluated variable must be a Collection, of course). 
Can (and should) contain nested elements. 
Example see [above](#Evaluation-of-Collections).

Iterate comes with two arguments: `operator` and `ifEmpty`.

**operator**: either "and" or "or" (default is "and"). `<iterate>` _and_s the results of evaluating its child expressions for each element in the collection, unless you set the operator to "or".

**ifEmpty**: the value to return for empty collections. 
If not specified, `true` is used with operator "or", `false` for "and".

reference
---------

Reference to a predefined expression. See [above](#Re-Usable-expressions).

resolve
-------

One of the more esotheric operators. 
It is comparable to the `with` operator, but it allows resolving the variable dynamically and to pass additional arguments needed to resolve the argument. 
For example to resolve the plug-in descriptor for a specific plug-in, the following expression can be used:

     <visibleWhen>
        <resolve variable="pluginDescriptor" args="org.eclipse.core.runtime">
            <test property="org.demo.isActive"/>
        </resolve>
     <visibleWhen>

The actual resolving is delegated to the evaluation context (see `IVariableResolver`). 
As of eclipse 3.5, there is no implementation in either the command framework or the common navigator framework for IVariableResolver, so the `<resolve>` operator is of no use for them.

systemTest
----------

Tests against the system properties (see `java.lang.System.getProperties()`).

Example:

     <visibleWhen>
        <systemTest property="user.name" value="martin"/>
     </visibleWhen>

test
----

Calls a property tester with the given parameters and arguments to check the variable. 
This is how you can call user code from an expression.

The following example would call the property tester registered with the namespace `org.acme` and the property name `matchesPattern`, if the variable is, or adapts to, `IFile`:

     <visibleWhen>
         <adapt value="org.eclipse.core.resources.IFile">
             <test property="org.acme.matchesPattern" value="*.html"/>
         </adapt>
     </visibleWhen>

See [below](#Property-Testers) for more information about property testers. 
Tests using an unknown property cause a core exception (this is a programming error).

with
----

Selects a variable different than the default variable for evaluation. 
Can, and should, contain nested elements. Example:

     <visibleWhen>
         <with variable="activePartId">
             <equals value="org.acme.navigator"/>
         </with>
     </visibleWhen>

This selects the "activePartId" variable from the evaluation context and checks if it equals "org.acme.navigator".

Property Testers
================

Property testers are added to the system using the extension point `org.eclipse.core.expressions.propertyTesters`. 
The above matchesPattern property would be declared like:

     <extension point="org.eclipse.core.expressions.propertyTesters">
        <propertyTester
              class="org.acme.PatternPropertyTester"
              id="org.acme.patternPropertyTester"
              namespace="org.acme"
              properties="matchesPattern, equalsPattern"
              type="org.eclipse.core.resources.IResource">
        </propertyTester>
     </extension>

Note that this example would declare a property tester for the properties `org.acme.matchesPattern` and `org.acme.equalsPattern`, and could be extended to any number of additional properties. 
The name of the property to be used from a `<test>` operator is always combined from namespace and one of the property names, the properties belong to that namespace. 
This allows for two plugins (siblings) to define the same property without ambiguity.

The concrete implementation of the property tester has to extend `PropertyTester`. 
Look around, you will find existing property testers to get you started with your own.

