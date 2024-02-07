

Platform UI Error Handling
==========================

This proposal is for error messages only. It does not include Log, trace or Application Life Cycle linked to errors in a software application. Yet, this proposal should fit very nicely in above mentioned features.

Contents
--------

*   [1 Use cases](#Use-cases)
    *   [1.1 Issue to solve](#Issue-to-solve)
    *   [1.2 See the error](#See-the-error)
    *   [1.3 What does it mean to me the user?](#What-does-it-mean-to-me-the-user)
    *   [1.4 What can I do next?](#What-can-I-do-next)
*   [2 Status manager & Status handlers](#Status-manager--Status-handlers)
    *   [2.1 Status manager](#Status-manager)
    *   [2.2 Styles](#Styles)
    *   [2.3 Status handlers](#Status-handlers)
    *   [2.4 StatusAdapter](#StatusAdapter)
    *   [2.5 The facility and Platform](#The-facility-and-Platform)
*   [3 WorkbenchStatusHandler and IDEWorkbenchStatusHandler](#WorkbenchStatusHandler-and-IDEWorkbenchStatusHandler)
*   [4 Error Messages: User Interface](#Error-Messages-User-Interface)
    *   [4.1 Error Messages User Interface Use Cases](#Error-Messages-User-Interface-Use-Cases)
        *   [4.1.1 Eclipse mapping](#Eclipse-mapping)
        *   [4.1.2 Use cases](#Use-cases-2)
    *   [4.2 Main requirements](#Main-requirements)
    *   [4.3 New StatusDialog](#New-StatusDialog)
        *   [4.3.1 Overview](#Overview)
        *   [4.3.2 Use cases](#Use-cases-3)
        *   [4.3.3 Usage](#Usage)
        *   [4.3.4 Bugs](#Bugs)
    *   [4.4 Work plan for 3.4.1 & 3.5](#work-plan-for-341--35)

Use cases
---------

We will use 4 different customers to ensure the proposal is scalable

*   BigCompany is using Eclipse 3.3. They decide to buy different features from different companies. They want an AJAX feature from company AjaxWorldCom and they decide on a database tooling feature from SQLForever Company. All user will have the same desktop feature and all call should be routed to the BigCompany’s help desk. Users do not have access to the web.

*   TaxInc develops an Tax Solution software. It has RCP clients and a server. TaxRUS bought the tax solution. They installed a server internally and deployed the RCP client to 250 employees around the world. The employees are not developers. They just use the RCP application to do Tax related tasks. They have an internal help desk.

*   BigSoftware develops a set of Eclipse feature for a product named: J2EE development 13.0. User can buy the product off the shelf or order a large amount of products and install them in their enterprise. BigSoftware has a huge support center. BigSoftware also ships 3rd party features it supports in its tooling.

*   John is a Java Perl developer. He downloaded Eclipse 3.3 and a Perl feature from the open source web site.

### Issue to solve

This is the main issue to solve for the 4 customers

When an error occurs in the tooling (Error message, error dialog, error in the console view, error in the log view, error in the job dialog or any other error), Eclipse needs to provide a way for the users to:

*   see what the error is
*   understand what it means to the them
*   how can they act on the error.

The behavior for each is based on policies.

*   The feature who threw the error should have a chance to handle the error and help the customer.
*   The feature should have an idea about what the error handler wants it to do.
    *   i.e. when there is an error opening a view should we show the error view or not
    *   also do we prompt when the workbench layout cannot be reset?
*   The product/branding that is installed can override all feature’s behavior it ships and manage or delegate to them as appropriate.

### See the error

It is very important to distinguish between expected and unexpected error.

*   Expected Error is when a developer expects the exception to be thrown in particular situation (network not available, resource locked by another person, invalid license etc).
*   Unexpected Error is an error that should not happen (internal error, NPE etc).

Generally expected errors should come with idea how to solve the problem, while it should be easy to report unexpected one and/or to get workaround.

When an error occurs, the developer may decide to show the error to the user. The code is opening an error dialog. However handler may ignore developer request and handle the error in different way, f.e. log it. Before opening the error dialog, and based on the policy, the flow can be re-routed and the error dialog may never show up like the developer intended to. There should be a hook in the code, based on policy that will express manage the behavior.

### What does it mean to me the user?

Most users are not interested in the ‘stack trace’ of the error. When a user sees an error or actively double clicks on an error we ought to see the information on how to solve the error (without technological background). This presumes the feature or the product or the company provided the data the user can understand and that the associated policy allows such data to be shown.

### What can I do next?

Based on the policy, it is the responsibility of the feature provider (component provider), the product provider or the company to decide what the ‘what to do next’ action will do. Eclipse could still provide a ‘show log’ button that policy provider can extend (this is a nice to have…)

Status manager & Status handlers
--------------------------------

### Status manager

StatusManager is the entry point for all statuses to be reported in the user interface. Handlers are not intended to be used directly. They should be referenced via the StatusManager which selects the handler corresponding to the product to apply. Right now it is impossible to have more than one handler per product because of scalability issues.

  
The following methods are the API entry points to the StatusManager

    StatusManager#handle(IStatus)
    
    StatusManager#handle(IStatus, int)
    
    StatusManager#handle(StatusAdapter)
    
    StatusManager#handle(StatusAdapter, int)
    

 

The StatusManager singleton is accessed using

    StatusManager.getManager()
    

 

The `int` parameter are for supplying style for handling. See [Acceptable styles](/Platform_UI_Error_Handling#Styles "Platform UI Error Handling").

**NOTE!** the style is a suggestion and may not be honoured by the current handler. For instance a handler may choose to not show the user anything when the SHOW flag is sent. See [Status handlers](/Platform_UI_Error_Handling#Status_handlers "Platform UI Error Handling") for more details.

The StatusManager gets it's list of handlers from the extension point `org.eclipse.ui.statusHandlers`. Should none of those handlers process the status it will fall through to the default handler (the the SDk this is `WorkbenchAdvisor#getWorkbenchErrorHandler()`). If a handler is associated with a product, it is used instead of this defined in advisor.

### Styles

Below is a list of StatusManager styles which can be combined with logical OR.

*   NONE - a style indicating that the status should not be acted on. This is used by objects such as log listeners that do not want to report a status twice
*   LOG - a style indicating that the status should be logged only
*   SHOW - a style indicating that handlers should show a problem to an user without blocking the calling method while awaiting user response. This is generally done using a non modal dialog
*   BLOCK - a style indicating that the handling should block the calling method until the user has responded. This is generally done using a modal window such as a dialog

### Status handlers

Status handlers are part of the status handling facility. The handlers are responsible for presenting statuses by logging or showing appropriate feedback to the user (generally dialogs). All status handlers extend `org.eclipse.ui.statushandlers.AbstractStatusHandler` which requires each handler to implement `handle(StatusAdapter status, int style)`. This method handles statuses based on a handling style. The style indicates how status handler should handle a status. See [Acceptable styles](/Platform_UI_Error_Handling#Styles "Platform UI Error Handling").

There are two ways for adding handlers to the handling flow.

*   using extension point `org.eclipse.ui.statusHandlers`
*   by the workbench advisor and its method {@link WorkbenchAdvisor#getWorkbenchErrorHandler()}.

If a handler is associated with a product, it is used instead of this defined in advisor.

  
A status handler has the id and a set of parameters. The handler can use them during handling. If the handler is added as an extension, both are set during initialization of the handler using elements and attributes of `statusHandler` element.

  
**WARNING!** We have to take the extra action when something has to be logged using the default logging mechanism, because the facility is hooked into it. See [Hooking the facility into Platform](/Platform_UI_Error_Handling#Hooking_the_facility_into_Platform "Platform UI Error Handling"). For this special case the status manager provides API.

 

      StatusManager#addLoggedStatus(IStatus status)
    

 

And below is the example of `addLoggedStatus(IStatus status)` proper usage.

 

      public void handle(final StatusAdapter statusAdapter, int style) {
      
        ...
    
        if ((style & StatusManager.LOG) == StatusManager.LOG) {
          StatusManager.getManager().addLoggedStatus(statusAdapter.getStatus());
            WorkbenchPlugin.getDefault().getLog().log(statusAdapter.getStatus());
        }
      }
    

 

### StatusAdapter

The StatusAdapter wraps an instance of IStatus subclass and can hold additional information either by using properties or by adding a new adapter. Used during status handling process.

### The facility and Platform

The places where the facility is hooked in

*   `WorkbenchAdvisor#eventLoopException(Throwable)` \- it handles all uncaught exceptions from main application loop
*   Jobs framework
*   Error log file (all logged messages are forwarded to the facility with the LOG style)
*   Exceptions from opening a part (the error view and error editor)

Platform is still under refactoring aimed at introducing the status handling facility.

  
**WARNING!** The facility isn't hooked into JFace ErrorDialog or MessageDialog in any way. The code has to be refactored if the facility is to be used.

The old code

 

      ErrorDialog.openError(...);
    

 

or

 

      MessageDialog.openError(...);
    

 

should be refactored into

 

      StatusManager.getManager().handle(..., StatusManager.SHOW);
    

 

WorkbenchStatusHandler and IDEWorkbenchStatusHandler
----------------------------------------------------

There are two implementation of status handlers

*   org.eclipse.ui.application.WorkbenchErrorHandler which is assigned to WorkbenchAdvisor
*   org.eclipse.ui.internal.ide.IDEWorkbenchErrorHandler assigned to IDEWorkbenchAdvisor

The current advisor indicates which handler is the workbench one.

Error Messages: User Interface
------------------------------

### Error Messages User Interface Use Cases

There are three types of user interfaces that will present a message, an error or a warning to a user. The three categories are

*   Message that requires user’s action now
*   Message that requires user’s action that can be done later
*   List of messages.

Message that requires action now.

They are typically represented in a modal dialog box. The user has to act on it or he/she will not be able to finish the task. Such a dialog should provide a standard view with an icon and a message. The message can provide an advanced view, in which case a ‘details’ or ‘advanced’ button is present on the standard view. When the user selects the details button, the advanced information about the message are displayed. The provider of the message or the Error handler will provide the user interface that will be embedded in the dialog. The user can pres the details button again, and this will collapse the advanced view. Messages must be persisted in an external file. The message in the modal window is the most relevant to the user and is decided by the ErrorHandler. If the message has offspring, they will be rendered in the advanced user interface.

Message that can have action later.

They are messages that can be error, but do not prevent the user from pursuing his/her task. All message may need to be solved at a certain point, but do not require immediate action. They are usually represented by information and icon in the user interface. The user can finish some other part of the interface before solving the message. Concrete examples are wizard pages and editors. In wizard pages the message is presented at the top of the page, in an editor it is usually embedded in the form editor or on the side of the editor (right or left side) To get more information about the message, the user clicks on it. A modeless window opens with the information. When the user clicks elsewhere, the window closes. Messages do not have to be persistent. The owner of the user interface can decide to save them or decide to recalculate the messages when the user interface is reopened. The owner of the user interface can decide to not allow the task to be saved or finished if the message is not act upon.

List of errors

The user is presented with a double pane view. One pane represents the list of messages. The user can filter them and reorganized them. Some filter could be based on the resources, the format (tree, table) or the dependency (ex: acting on this message will also resolve the following messages…). The filtering mechanism should be lighter in its usage than the current one. When the user selects a message, the second pane is filled with the details of the message. This will show the exact same information as #1. The messages are persistent. PS: the semantic of this user interface is like an ‘email reader’ where you select the email message to see the content of the email. A provider could replace the first pane of the list of errors. ErrorView and Problem view should be merged in a unique view.

#### Eclipse mapping

MessageDialog and ErrorDialog are of type 1.

The user must act of them. The best practice is that only error message should appear to the user using this format. Warning and Information messages should not. The error handler can decide if a message is worth showing or not and will also provide the details.

Errors in a background process are of type 2.

We should not open a modal dialog when an issue occurs. The user can ‘glance’ in the result in the bottom right corner, click on it to see the list of errors and click on an error to see a detail.

Wizard messages are of type 1.

The user must act on them to go to the next page but also clicking on them will open a pop up. There is no ‘details’ button in the wizard user interface

Error log is of type 3

The view shows the different errors from the log. The user can click on an error in the list to see more details.

#### Use cases

The User Interface behavior will depend from the context of the error. If an error occurs during a Job, we will not prompt the user but rather notify the Job View. If the same error occurs in the plug-in itself, we will open a modal window.

The error manager framework must keep track of the context and if the error handler decides to show the error to the user, the Error Manager Framework should use the appropriate User Interface.

Context of the use cases

The plug-in is looking for a file named ‘result.txt’ on a web site. When executing, the plug-in is unable to connect to the web server. The plug-in throws an error using the error handler mechanism. The error is modified to notify the user that the remote site does not answer and that the user should check if there is any proxy of if the site is actually down.

Use Case 1

Modal Dialog

The framework realizes the context and opens an ErrorDialog. The ErrorDialog detail is filled with content from the plug-in error handler.

Use Case 2

Job Error

The framework realizes the context and modifies the user interface for the Job. A red ‘x’ appears in the bottom right hand corner of eclipse. The User double clicks on the red ‘x’ and a window opens, showing the message of the error. When the user clicks on the message; the framework opens a modal window like in use case #1.

Use Case 3

Wizard

The framework realizes the context and updates the top of the wizard with the message. When the user hovers over the message a pop up appears. If the user wants to see more details, a modal window opens.

Use Case 4

Error Log

The error handler decided to not show the user about the error. It decided to only log it. The error appears in the error log view. The user clicks on the entry and a modal dialog (like in #1) opens.

### Main requirements

*   shows list of the problems
*   shows details of the problems
*   shows view which helps user to handle or report the problems

Each problem on the list should have error reporting view associated to it. This relation can be set in error handler which handles the problem. This reporting view will be showed in the tray part of the dialog.

For workbench handler it can be simply a basic view with stack trace. For product handler it can be a html view with a form for registering the problem or for checking the state of the problem.

### New StatusDialog

#### Overview

New Status Dialog is integrated with Status Handling facility. Unless no other handler is specified, the default dialog will be used.

**Main concepts**

*   Message - This is the most important piece of information.
*   Details - Sometimes there is not enough place to display all necessary info on the dialog, or we do not want to scary the user. Then we can place leading message on the dialog, and all other things make accessible via details area. Currently it is possible to set up one kind of details area provider, but there is no further requirement.
*   Support - It is possible to place some contact form on the dialog, so the user will know where to look for help or will be able to easily contact vendor.

Support and Details are terms introduced to distinct the behavior and functionality although they extend common abstract class. One should remember that user expects more information after pressing "Details >>>" button.

**Messages hierarchy**

Status Dialog uses following messages hierarchy:

*   Job name (if available)
*   StatusAdapter title
*   IStatus message
*   MultiStatus information (applies only if MultiStatus is handled).
*   message extracted from exception
*   exception name

Status Dialog displays always two the most important messages. If first one is not found then the dialog should give information about general error instead. If the second one is not found, dialog should point to the details.

**User interface**

*   Only one status is handled
    *   First message displayed is the most important message.
    *   Second message displayed is the second most important message.
    *   Timestamp is displayed in the details (if available)
*   More statuses handled - the selection list appears
    *   There is the most important message used on the selection list.
    *   The second most important message is displayed right to the severity icon.
    *   Timestamp (if available) is appended to the message in the selection list.

**Extensions**

*   Details area can be replaced - it does not display stack trace anymore, rather the error hierarchy (message from Status, Exception, Nested statuses, etc) by default. Product may decide to provide some high level description, or even retrieve description from database.
*   Support area can be added - product may hook into the error dialog and place there f.e. browser with url pointing to the helpdesk, or some reporting mechanism.

The basic idea is that even unexperienced user should be able to understand what is going on while looking at details, and should be able to report/get help using support area.

**Examples:**

*   Only one status is reported. Primary message and secondary message should be displayed. Secondary message should have timestamp if one is available.

![Singlestatus2.jpg](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/docs/images/Singlestatus2.jpg)

*   Job reported an error. Display message informing which job, and primary message.


*   Multiple errors occured. On the list should be displayed job name or primary message of the status. In the title primary message should appear for jobs and secondary for statuses.

![Manystatuses.jpg](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/docs/images/Manystatuses.jpg)

*   One stastus has been reported and support is available.

after pressing bug icon:

[![Statusdialogwithsupportopened.JPG](/images/a/ab/Statusdialogwithsupportopened.JPG)](/File:Statusdialogwithsupportopened.JPG)

and many statuses. The selected in the list Status is a base for support area.

[![Manystatuseswithsupportopened.JPG](/images/f/fe/Manystatuseswithsupportopened.JPG)](/File:Manystatuseswithsupportopened.JPG)

#### Use cases

To be done

#### Usage

**Note for WorkbenchStatusDialog clients**  
Please note that WorkbenchStatusDialog#getPrimaryMessage & WorkbenchStatusDialog#getSecondaryMessage begin searching for the message from StatusAdapter title. It means that when we are handling job statuses we display:

*   single status
    *   job name as the first message
    *   WorkbenchStatusDialog#getPrimaryMessage result as second message
*   list
    *   job name (and eventually timestamp) as the position on the list.
    *   WorkbenchStatusDialog#getPrimaryMessage result right to the severity icon.

#### Bugs

| No | Task | Assigne | Status | Bug id |
| --- | --- | --- | --- | --- |
| 1. | Create new StatusDialog exteds TrayDialog | krzysztof.michalski@pl.ibm.com | done | 193612 |
| 2. | Merge old StatusDialog and StatusNotificatonsManager functionality | krzysztof.michalski@pl.ibm.com | done |  |
| 3. | Add possibility of set label provider on StatusListView | krzysztof.michalski@pl.ibm.com | done |  |
| 4. | Add slection change Listener on StatusListView which recreate reportingView | krzysztof.michalski@pl.ibm.com | done |  |
| 5. | Create ReportingTray extends DialogTray | krzysztof.michalski@pl.ibm.com | done |  |
| 6. | Create InternalDialog to manage modal of window | krzysztof.michalski@pl.ibm.com | done |  |
| 7. | Add possibility of create reporting view: by method createReportingView, setStatusAdapter | krzysztof.michalski@pl.ibm.com | in progress |  |
| 8. | Creating reporting view by Policy | krzysztof_daniel@pl.ibm.com | done | 180300 |
| 9. | Explanation and Action in IStatus | krzysztof_daniel@pl.ibm.com | done | 179373 |
| 10. | User decides if open support area | krzysztof.michalski@pl.ibm.com | done | 179375 |

### Work plan for 3.4.1 & 3.5

| No | Task | When | Assigne | Status | Bug id |
| --- | --- | --- | --- | --- | --- |
| 1. | Update this document to reflect real Status Handling capabilities | 3.4.1 | krzysztof_daniel@pl.ibm.com | in progress |  |
| 2. | Incorporate ErrorDialog into status handling | 3.5 | krzysztof_daniel@pl.ibm.com |  |  |

