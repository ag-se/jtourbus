/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package de.fu_berlin.inf.jtourbus.actions;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.corext.dom.TokenScanner;
import org.eclipse.jdt.internal.corext.util.Strings;
import org.eclipse.jdt.internal.ui.JavaUIStatus;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MultiTextEdit;

import de.fu_berlin.inf.jtourbus.BusStop;

/**
 * 
 * This class was taken from
 * org.eclipse.jdt.internal.corext.codemanipulation.AddJavaDocStubOperation
 * 
 */
public class InsertJTourBusCommentOperation implements IWorkspaceRunnable {

    private IMember member;

    private double stopNumber;

    private String route;

    private String description;

    public InsertJTourBusCommentOperation(IMember member, double d,
            String route, String string) {
        super();
        this.member = member;
        this.stopNumber = d;
        this.route = route;
        this.description = string;
    }

    /**
     * @return Returns the scheduling rule for this operation
     */
    public ISchedulingRule getScheduleRule() {
        return ResourcesPlugin.getWorkspace().getRoot();
    }

    /**
     * Runs the operation.
     * @throws OperationCanceledException Runtime error thrown when operation is
     * cancelled.
     */
    public void run(IProgressMonitor monitor) throws CoreException,
            OperationCanceledException {
        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }
        try {
            monitor.beginTask("Adding JTourBusStop at current cursor position",
                    3);
            addJTourBusStop(monitor);
        } finally {
            monitor.done();
        }
    }

    private void addJTourBusStop(IProgressMonitor monitor) throws CoreException {
        ICompilationUnit cu = member.getCompilationUnit();

        ITextFileBufferManager manager = FileBuffers.getTextFileBufferManager();
        IPath path = cu.getPath();
        manager.connect(path, LocationKind.IFILE, new SubProgressMonitor(monitor, 1));
        try {
            IDocument document = manager.getTextFileBuffer(path, LocationKind.IFILE).getDocument();

            String lineDelim = TextUtilities.getDefaultLineDelimiter(document);
            MultiTextEdit edit = new MultiTextEdit();

            IMember curr = member;
            int memberStartOffset = getMemberStartOffset(curr, cu);

            StringBuffer buf = new StringBuffer();
            buf.append("/**").append(lineDelim); //$NON-NLS-1$
            buf
                    .append(
                            " * "   + BusStop.toCode(stopNumber, route, description)).append(lineDelim); //$NON-NLS-1$
            buf.append(" */").append(lineDelim); //$NON-NLS-1$
            String comment = buf.toString();

            final IJavaProject project = cu.getJavaProject();
            IRegion region = document
                    .getLineInformationOfOffset(memberStartOffset);

            String line = document.get(region.getOffset(), region.getLength());
            String indentString = Strings.getIndentString(line, project);

            String indentedComment = Strings.changeIndent(comment, 0, project,
                    indentString, lineDelim);

            edit.addChild(new InsertEdit(memberStartOffset, indentedComment));

            monitor.worked(1);

            edit.apply(document); // apply all edits
        } catch (BadLocationException e) {
            throw new CoreException(JavaUIStatus.createError(IStatus.ERROR, e));
        } finally {
            manager.disconnect(path, LocationKind.IFILE, new SubProgressMonitor(monitor, 1));
        }

    }

    private int getMemberStartOffset(IMember curr, ICompilationUnit document)
            throws JavaModelException {
        int offset = curr.getSourceRange().getOffset();
        
        try {
        	TokenScanner scanner = new TokenScanner(document);
        	
        	return scanner.getNextStartOffset(offset, true); // read to the
            // first real
            // non comment
            // token
        } catch (CoreException e) {
            // ignore
        }
        return offset;
    }

}
