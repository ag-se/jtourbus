/*******************************************************************************
 * Copyright (c) 2005-2008 Christopher Oezbek
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christopher Oezbek - initial API and implementation
 *******************************************************************************/
package de.fu_berlin.inf.jtourbus.view;

import java.util.List;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;

import de.fu_berlin.inf.jtourbus.BusStop;
import de.fu_berlin.inf.jtourbus.TourPlan;
import de.fu_berlin.inf.jtourbus.plugin.JTourBusPlugin;
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

            List<BusStop> stops;
            if (JTourBusPlugin.ALL_PROJECTS){
            	stops = Utilities.findJTourBusStops(monitor,
               	 ResourcesPlugin.getWorkspace().getRoot());
            } else {
            	stops = Utilities.findJTourBusStops(monitor,
            		fContentProvider.fProject.getResource());
            }
            for (BusStop s : stops){
                fContentProvider.fTourPlan.add(s);
            }

            monitor.worked(1);
            monitor.done();
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

}