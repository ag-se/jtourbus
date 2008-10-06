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
package de.inffub.jtourbus.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
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
import org.eclipse.jdt.internal.ui.JavaUIStatus;
import org.eclipse.jdt.internal.ui.actions.WorkbenchRunnableAdapter;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.ui.PlatformUI;

import de.inffub.jtourbus.BusStop;
import de.inffub.jtourbus.BusStopJavaElement;

/**
 * 
 * This class was taken from
 * org.eclipse.jdt.internal.corext.codemanipulation.AddJavaDocStubOperation
 * 
 */
public class UpdateStopsOperation implements IWorkspaceRunnable {

    public void run() {

        try {
            PlatformUI.getWorkbench().getProgressService().runInUI(
                    PlatformUI.getWorkbench().getProgressService(),
                    new WorkbenchRunnableAdapter(this, this.getScheduleRule()),
                    this.getScheduleRule());
        } catch (InvocationTargetException e) {
            //ExceptionHandler.handle(e, fSite.getShell(), getDialogTitle(),
              //      ActionMessages.AddJavaDocStubsAction_error_actionFailed);
        } catch (InterruptedException e) {
            // operation cancelled
        }
    }

    protected List<BusStop> fBusStops;

    public UpdateStopsOperation(List<BusStop> busStopsToWriteBack) {
        super();
        fBusStops = busStopsToWriteBack;
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
            monitor.beginTask("Updating JTourBusStops...", 3 * fBusStops.size());
            changeJTourBusStop(monitor);
        } finally {
            monitor.done();
        }
    }

    private void changeJTourBusStop(IProgressMonitor monitor)
            throws CoreException {

        for (BusStop fBusStop : fBusStops) {
            
            BusStopJavaElement me = (BusStopJavaElement)fBusStop;
            
            String newCode = me.toCode();
            
            for (BusStop fOther : fBusStops){
                
                BusStopJavaElement other = (BusStopJavaElement)fOther;
                if (other != me && other.getCompilationUnit().equals(me.getCompilationUnit())){
                    if (other.offset > me.offset){
                        other.offset = other.offset + newCode.length() - me.length;
                    }
                }
            }
            
            ITextFileBufferManager manager = FileBuffers
                    .getTextFileBufferManager();
            ICompilationUnit cu = fBusStop.getCompilationUnit();

            IPath path = cu.getPath();
            manager.connect(path, new SubProgressMonitor(monitor, 1));
            try {
                IDocument document = manager.getTextFileBuffer(path)
                        .getDocument();
                
                MultiTextEdit edit = new MultiTextEdit();

                edit.addChild(new ReplaceEdit(me.offset,
                        me.length, fBusStop.toCode()));

                monitor.worked(1);

                edit.apply(document); // apply all edits
            } catch (BadLocationException e) {
                throw new CoreException(JavaUIStatus.createError(IStatus.ERROR,
                        e));
            } finally {
                manager.disconnect(path, new SubProgressMonitor(monitor, 1));
            }
            me.length = newCode.length();

        }
    }
}
