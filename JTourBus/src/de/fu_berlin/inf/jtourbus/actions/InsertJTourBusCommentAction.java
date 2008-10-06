/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package de.fu_berlin.inf.jtourbus.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;
import org.eclipse.jdt.internal.ui.actions.ActionMessages;
import org.eclipse.jdt.internal.ui.actions.ActionUtil;
import org.eclipse.jdt.internal.ui.actions.SelectionConverter;
import org.eclipse.jdt.internal.ui.actions.WorkbenchRunnableAdapter;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.internal.ui.util.ElementValidator;
import org.eclipse.jdt.internal.ui.util.ExceptionHandler;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.PlatformUI;

import de.fu_berlin.inf.jtourbus.BusStop;

/**
 * Insert a JTourBusTag for the selected member.
 * 
 * This class was adapted from jdt.ui.actions.AddJavaDocStubAction
 * 
 * Will open the parent compilation unit in a Java editor. The result is
 * unsaved, so the user can decide if the changes are acceptable. <p> The action
 * is applicable to structured selections containing elements of type
 * <code>IMember</code>.
 * 
 */
public class InsertJTourBusCommentAction extends Action {

    IWorkbenchSite fSite;
    private BusStop current;

    /**
     * Creates a new <code>AddJavaDocStubAction</code>. The action requires
     * that the selection provided by the site's selection provider is of type
     * <code>
     * org.eclipse.jface.viewers.IStructuredSelection</code>.
     * 
     * @param site the site providing context information for this action
     */
    public InsertJTourBusCommentAction(IWorkbenchSite site) {
        fSite = site;
        setText("Insert a JTourBusStop");
        setDescription("Will add an JTourBusStop to the javadoc of the current member");
        setToolTipText("Insert a JTourBusStop");
        // PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
        // IJavaHelpContextIds.ADD_JAVADOC_STUB_ACTION);
    }

    public void setCurrentStop(BusStop current){
        this.current = current;
    }

    /**
     * Note: This constructor is for internal use only. Clients should not call
     * this constructor.
     * @param editor the compilation unit editor
     */
    public InsertJTourBusCommentAction(CompilationUnitEditor editor) {
        // this(editor.getEditorSite());
        // fEditor = editor;
        // setEnabled(checkEnabledEditor());
    }

    public void selectionChanged(IStructuredSelection selection) {
        IMember[] members = getSelectedMembers(selection);
        setEnabled(members != null && members.length > 0);
    }

    public void run(IStructuredSelection selection) {
        
        IMember[] members = getSelectedMembers(selection);
        if (members == null || members.length == 0) {
            return;
        }

        try {
            ICompilationUnit cu = members[0].getCompilationUnit();
            if (!ActionUtil.isProcessable(fSite.getShell(), cu)) {
                return;
            }

            // open the editor, forces the creation of a working copy
            IEditorPart editor = EditorUtility.openInEditor(cu);

            if (ElementValidator.check(members, fSite.getShell(),
                    getDialogTitle(), false))
                run(cu, members[0]);
            JavaModelUtil.reconcile(cu);
            EditorUtility.revealInEditor(editor, members[0]);

        } catch (CoreException e) {
            ExceptionHandler.handle(e, fSite.getShell(), getDialogTitle(),
                    ActionMessages.AddJavaDocStubsAction_error_actionFailed);
        }
    }

    // private boolean checkEnabledEditor() {
    // return fEditor != null && SelectionConverter.canOperateOn(fEditor);
    // }

    public void run(ITextSelection selection, JavaEditor fEditor) {
        try {
            IJavaElement element = SelectionConverter
                    .getElementAtOffset(fEditor);
            if (!ActionUtil.isProcessable(fSite.getShell(), element))
                return;
            int type = element != null ? element.getElementType() : -1;
            if (type != IJavaElement.METHOD && type != IJavaElement.TYPE
                    && type != IJavaElement.FIELD) {
                element = SelectionConverter.getTypeAtOffset(fEditor);
                if (element == null) {
                    MessageDialog
                            .openInformation(
                                    fSite.getShell(),
                                    getDialogTitle(),
                                    ActionMessages.AddJavaDocStubsAction_not_applicable);
                    return;
                }
            }
            IMember[] members = new IMember[] { (IMember) element };
            if (ElementValidator.checkValidateEdit(members, fSite.getShell(),
                    getDialogTitle()))
                run(((IMember) element).getCompilationUnit(), (IMember) element);
        } catch (CoreException e) {
            ExceptionHandler.handle(e, fSite.getShell(), getDialogTitle(),
                    ActionMessages.AddJavaDocStubsAction_error_actionFailed);
        }
    }

    // ---- Helpers
    // -------------------------------------------------------------------

    /**
     * Note this method is for internal use only.
     * 
     * @param cu the compilation unit
     * @param members an array of members
     */
    public void run(ICompilationUnit cu, IMember member) {
        try {
            InsertJTourBusCommentOperation op;

            if (current != null) {
                op = new InsertJTourBusCommentOperation(member, current.getStopNumber() + 1.0, current.getRoute(), "<<Description>>");
            } else {
                op = new InsertJTourBusCommentOperation(member, 1.0, "Main Route", "<<Description>>");
            }

            PlatformUI.getWorkbench().getProgressService().runInUI(
                    PlatformUI.getWorkbench().getProgressService(),
                    new WorkbenchRunnableAdapter(op, op.getScheduleRule()),
                    op.getScheduleRule());
        } catch (InvocationTargetException e) {
            ExceptionHandler.handle(e, fSite.getShell(), getDialogTitle(),
                    ActionMessages.AddJavaDocStubsAction_error_actionFailed);
        } catch (InterruptedException e) {
            // operation cancelled
        }
    }

    private IMember[] getSelectedMembers(IStructuredSelection selection) {
        List<?> elements = selection.toList();
        int nElements = elements.size();
        if (nElements > 0) {
            IMember[] res = new IMember[nElements];
            ICompilationUnit cu = null;
            for (int i = 0; i < nElements; i++) {
                Object curr = elements.get(i);
                if (curr instanceof IMethod || curr instanceof IType
                        || curr instanceof IField) {
                    IMember member = (IMember) curr; // limit to methods,
                    // types & fields

                    if (i == 0) {
                        cu = member.getCompilationUnit();
                        if (cu == null) {
                            return null;
                        }
                    } else if (!cu.equals(member.getCompilationUnit())) {
                        return null;
                    }
                    if (member instanceof IType
                            && member.getElementName().length() == 0) {
                        return null; // anonymous type
                    }
                    res[i] = member;
                } else {
                    return null;
                }
            }
            return res;
        }
        return null;
    }

    private String getDialogTitle() {
        return "Error while adding JTourBusStop";
    }

}
