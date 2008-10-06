/*
 * Created on 08.02.2005
 *
 */
package de.inffub.jtourbus.view;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.PlatformUI;

import de.inffub.jtourbus.BusStop;
import de.inffub.jtourbus.TourPlan;

/**
 * The Content Provider hosts the business logic of the plugin.
 * 
 * @JTourBusStop 2.0, Important Types, TourBusRoutesContentProvider - The
 * business logic of the plugin:
 * 
 * What you should know after visiting this stop:
 * 
 * o That the logic of the plugin is hosted here.
 * 
 * o That the treeviewer uses an instance of this class to ask for the structure
 * of the displayed data.
 * 
 * The type is explained in more detail on the route "Interaction".
 */
public class TourBusRoutesContentProvider implements
        IStructuredContentProvider, ITreeContentProvider {
    TourPlan fTourPlan;

    Object fRoot;

    IJavaProject fProject;

    Viewer fViewer;

    TourChangeListener fTourChangeListener;

    public TourBusRoutesContentProvider(Object root) {
        fRoot = root;
        fTourChangeListener = new TourChangeListener(this);
        JavaCore.addElementChangedListener(fTourChangeListener);
    }

    /**
     * Input Changed is responsible to updating the internal data-model by
     * reading a constructed AST.
     * 
     * @JTourBusStop 1.0, Interaction, refreshTours - Interaction point between
     * user and data:
     * 
     * Input Changed hosts the business logic of constructing and walking the
     * AST.
     * 
     * Input Changed is trigger (indirectly by calls to setInput) by the
     * following three events:
     * 
     * o When the user presses the refreshAction-Button.
     * 
     * o When the user changes the currently active project by selecting a new
     * project from the navigator.
     * 
     * o When the user changes the currently active project by pressing the "Set
     * from Editor"-Button
     * 
     * Be aware that if you call Input Changed directly the tree-view is not
     * going to update itself because it does not know that the content changed.
     * What you rather should do is call the method setInput on the treeview.
     * This will trigger a call to Input Changed and a subsequent update of the
     * display of the tree.
     * 
     */
    public void inputChanged(Viewer v, Object oldInput, final Object newInput) {
        if (!(newInput instanceof IJavaProject))
            return;

        fViewer = v;

        fProject = (IJavaProject) newInput;

        IRunnableWithProgress op = new TourCreateRunnable(this);

        try {
            /*
             * ISearchResultViewPart view= getSearchView(); if (view != null) {
             * IWorkbenchPartSite site= view.getSite(); if (site != null) return
             * (IWorkbenchSiteProgressService)view.getSite().getAdapter(IWorkbenchSiteProgressService.class); }
             * return null; }
             */
            PlatformUI.getWorkbench().getProgressService().run(false, false, op);
            /*
             * IWorkbenchSiteProgressService service= getProgressService(); if
             * (service != null) service.schedule(jobRecord.fJob, 0, true);
             */
        } catch (InvocationTargetException e) {
            fTourPlan = null;
        } catch (InterruptedException e) {
            fTourPlan = null;
        }
    }

    public void dispose() {
    }

    public Object[] getElements(Object parent) {
        if (parent instanceof IJavaProject) {
            if (fTourPlan != null) {
                return fTourPlan.routes.keySet().toArray();
            }
        }
        if (fTourPlan != null && fTourPlan.routes.containsKey(parent)) {
            return fTourPlan.routes.get(parent).toArray();
        }
        return new Object[0];
    }

    public Object[] getChildren(Object parent) {
        if (parent.equals(fRoot)) {
            if (fTourPlan != null) {
                return fTourPlan.routes.keySet().toArray();
            }
        }
        if (fTourPlan != null && fTourPlan.routes.containsKey(parent)) {
            return fTourPlan.routes.get(parent).toArray();
        }
        return new Object[0];
    }

    public Object getParent(Object element) {
        if (element instanceof BusStop) {
            return ((BusStop) element).getRoute();
        }
        return null;
    }

    public boolean hasChildren(Object element) {
        return (element instanceof String);
    }
}