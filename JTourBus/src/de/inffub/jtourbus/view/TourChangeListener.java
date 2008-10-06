/*
 * Created on 05.12.2005
 * 
 */
package de.inffub.jtourbus.view;

import java.util.Set;
import java.util.Vector;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;

import de.inffub.jtourbus.BusStop;
import de.inffub.jtourbus.utility.Utilities;

public class TourChangeListener implements IElementChangedListener {

    private TourBusRoutesContentProvider fContentProvider;

    public TourChangeListener(TourBusRoutesContentProvider contentProvider) {
        fContentProvider = contentProvider;
    }

    public boolean notifyView = false;

    public BusStop newBusStop = null;

    void visit(IProgressMonitor monitor, IJavaElementDelta delta) {

        SubProgressMonitor s = null;
        if (monitor != null)
            s = new SubProgressMonitor(monitor, 1);

        visitInternal(s, delta);

        if (s != null) {
            s.worked(1);
            s.done();
        }
    }

    void redoCU(ICompilationUnit cu, IProgressMonitor pm) {

        Set<BusStop> stopsRemoved = fContentProvider.fTourPlan.removeCU(cu);

        Vector<BusStop> newStops = Utilities.findJTourBusStops(pm, cu
                .getResource());

        if (notifyView) {
            for (BusStop bs : newStops) {
                if (!stopsRemoved.contains(bs)) {
                    newBusStop = bs;
                }
                fContentProvider.fTourPlan.add(bs);
            }

        } else {
            for (BusStop bs : newStops)
                fContentProvider.fTourPlan.add(bs);
        }
    }

    void visitInternal(IProgressMonitor pm, IJavaElementDelta delta) {

        IJavaElement elem = delta.getElement();
        IJavaElementDelta[] children = delta.getAffectedChildren();

        if (pm != null)
            pm.beginTask("", children.length + 2);

        if ((delta.getFlags() & IJavaElementDelta.F_CHILDREN) != 0) {

            if (delta.getKind() != IJavaElementDelta.CHANGED) {
                throw new RuntimeException(
                        "ASSERT: CHILDREN should always be CHANGE");
            }

            for (int i = 0; i < children.length; i++) {
                visit(pm, children[i]);
            }
            return;
        }

        switch (delta.getKind()) {
        case IJavaElementDelta.ADDED: {
            if (!((delta.getFlags() & IJavaElementDelta.F_CHILDREN) == 0))
                throw new RuntimeException("ASSERT: ADDED has no children");

            switch (elem.getElementType()) {
            case IJavaElement.JAVA_MODEL:
                throw new RuntimeException(
                        "ASSERT: Adding Java Model not possible");
            case IJavaElement.JAVA_PROJECT:
                throw new RuntimeException(
                        "ASSERT: Adding Java Project not possible");
            case IJavaElement.PACKAGE_FRAGMENT_ROOT:
                // The user added a source folder

                for (BusStop bs : Utilities.findJTourBusStops(pm, elem
                        .getResource()))
                    fContentProvider.fTourPlan.add(bs);
                return;
            case IJavaElement.PACKAGE_FRAGMENT:
                // The user inserted a packaged

                for (BusStop bs : Utilities.findJTourBusStops(pm, elem
                        .getResource()))
                    fContentProvider.fTourPlan.add(bs);
                return;
            case IJavaElement.COMPILATION_UNIT: {
                ICompilationUnit cu = (ICompilationUnit) elem;
                if (cu.getPrimary().equals(cu)) {
                    for (BusStop bs : Utilities.findJTourBusStops(pm, elem
                            .getResource()))
                        fContentProvider.fTourPlan.add(bs);
                }
                return;
            }
            default:
                ICompilationUnit cu = (ICompilationUnit) delta.getElement()
                        .getAncestor(IJavaElement.COMPILATION_UNIT);

                redoCU(cu, pm);
                return;
            }
        }
        case IJavaElementDelta.REMOVED:
            if (!((delta.getFlags() & IJavaElementDelta.F_CHILDREN) == 0))
                throw new RuntimeException("REMOVED has children");

            switch (elem.getElementType()) {
            case IJavaElement.JAVA_MODEL:
                throw new RuntimeException("ASSERT: Java Model not possible");
            case IJavaElement.JAVA_PROJECT:
                fContentProvider.fTourPlan.removeAll();
                return;
            case IJavaElement.PACKAGE_FRAGMENT_ROOT:
            case IJavaElement.PACKAGE_FRAGMENT:
                fContentProvider.fTourPlan.removeAll();

                for (BusStop bs : Utilities.findJTourBusStops(pm, elem
                        .getJavaProject().getResource()))
                    fContentProvider.fTourPlan.add(bs);

                return;
            case IJavaElement.COMPILATION_UNIT: {
                ICompilationUnit cu = (ICompilationUnit) elem;
                if (cu.getPrimary().equals(cu)) {
                    fContentProvider.fTourPlan.removeCU(cu);
                }
                return;
            }
            default: {
                ICompilationUnit cu = (ICompilationUnit) delta.getElement()
                        .getAncestor(IJavaElement.COMPILATION_UNIT);

                redoCU(cu, pm);
                return;
            }
            }
        case IJavaElementDelta.CHANGED:
            // F_CONTENT && F_FINE_GRAINED
            if ((delta.getFlags() & IJavaElementDelta.F_CONTENT) != 0
                && (delta.getFlags() & IJavaElementDelta.F_FINE_GRAINED) != 0) {

                ICompilationUnit cu = (ICompilationUnit) delta.getElement()
                        .getAncestor(IJavaElement.COMPILATION_UNIT);

                redoCU(cu, pm);
            }

            // Closing without saving will trigger this event. We thus re-read
            // the file.
            if ((delta.getFlags() & IJavaElementDelta.F_PRIMARY_WORKING_COPY) != 0) {

                ICompilationUnit cu = (ICompilationUnit) delta.getElement();
                
                /* FIXME */
                
                redoCU(cu, pm);
            }
            break;
        }
    }

    public void elementChanged(final ElementChangedEvent event) {

        try {
            try {

                // If we don't have a project, we don't need updating
                if (fContentProvider.fProject == null)
                    return;

                IJavaElementDelta delta = event.getDelta();

                // As a first step filter out all deltas that don't correspond
                // to the project we are monitoring
                if (delta.getElement().getElementType() == IJavaElement.JAVA_MODEL) {

                    // ASSERT - There are no elementChanged events on the java
                    // model itself
                    if ((delta.getFlags() & IJavaElementDelta.F_CHILDREN) == 0
                        || (delta.getKind() != IJavaElementDelta.CHANGED)) {
                        throw new RuntimeException(
                                "JavaModel assertion failed - 1");
                    }

                    IJavaElementDelta[] children = delta.getAffectedChildren();
                    for (int i = 0; i < children.length; i++) {
                        // ASSERT - The model contains projects
                        if (children[i].getElement().getElementType() != IJavaElement.JAVA_PROJECT) {
                            throw new RuntimeException(
                                    "JavaModel assertion failed - 2");
                        }
                        if (fContentProvider.fProject.equals(children[i]
                                .getElement())) {
                            visit(null, children[i]);
                        }
                    }
                } else {

                    // ASSERT - Everything else but the model has a project
                    if (delta.getElement().getJavaProject() == null) {
                        throw new RuntimeException(
                                "JavaModel assertion failed - 3");
                    }

                    if (fContentProvider.fProject.equals(delta.getElement()
                            .getJavaProject()))
                        visit(null, delta);
                }

                if (!fContentProvider.fViewer.getControl().isDisposed()) {
                    Display display = fContentProvider.fViewer.getControl()
                            .getDisplay();

                    if (!display.isDisposed()) {
                        display.asyncExec(new Runnable() {
                            public void run() {
                                // make sure the tree still exists
                                if (fContentProvider.fViewer != null
                                    && fContentProvider.fViewer.getControl()
                                            .isDisposed())
                                    return;
                                fContentProvider.fViewer.refresh();
                                if (newBusStop != null) {
                                    fContentProvider.fViewer
                                            .setSelection(new StructuredSelection(
                                                    newBusStop));
                                }
                                newBusStop = null;
                            }
                        });
                    }
                }
            } catch (RuntimeException e) {
                e.printStackTrace();
                throw e;
            }
        } finally {
            notifyView = false;
        }
    }
}