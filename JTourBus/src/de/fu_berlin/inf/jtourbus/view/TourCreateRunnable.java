package de.fu_berlin.inf.jtourbus.view;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;

import de.fu_berlin.inf.jtourbus.BusStop;
import de.fu_berlin.inf.jtourbus.TourPlan;
import de.fu_berlin.inf.jtourbus.utility.Utilities;

class TourCreateRunnable implements IRunnableWithProgress {

    private final TourBusRoutesContentProvider fContentProvider;

    TourCreateRunnable(TourBusRoutesContentProvider provider) {
        fContentProvider = provider;
    }

    public void run(IProgressMonitor monitor) {

        try {
            if (fContentProvider.fProject == null)
                return;

            monitor.beginTask("Updating JTourBus Info...", 1);

            fContentProvider.fTourPlan = new TourPlan();

            for (BusStop s : Utilities.findJTourBusStops(monitor, new IResource[] { fContentProvider.fProject
                    .getResource() })){
                fContentProvider.fTourPlan.add(s);
            }

            monitor.worked(1);
            monitor.done();
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

}