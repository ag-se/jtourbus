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
package de.fu_berlin.inf.jtourbus;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.ISourceRange;

/**
 * BusStop is an internal representation of an JTourBusStop-Annotation.
 * 
 * It is subclassed to work with Annotation-based stops and JavaDoc-based stops.
 * 
 * @version 0.1 - 08.02.2005
 * @author Christopher Oezbek
 */
public abstract class BusStop implements Comparable<BusStop> {

    protected String fRoute;

    protected double fStopNumber;

    protected String fDescription;

    protected ICompilationUnit fCompilationUnit;

    public BusStop(ICompilationUnit compilationUnit) {
        fRoute = "Main Route";
        fStopNumber = 0.0;
        fDescription = "";
        fCompilationUnit = compilationUnit;
    }

    public String getRoute() {
        return fRoute;
    }

    public double getStopNumber() {
        return fStopNumber;
    }

    public void setRoute(String s) {
        fRoute = s;
    }

    public void setStopNumber(double newStopNumber) {
        fStopNumber = newStopNumber;
    }

    public String getDescription() {
        return fDescription;
    }

    public abstract ISourceRange getSourceRange();

    public ICompilationUnit getCompilationUnit() {
        return fCompilationUnit;
    }

    /**
     * CompareTo makes individual BusStops comparable using the value of their
     * fStopNumber.
     * 
     * @param arg0
     * @return
     */
    public int compareTo(BusStop other) {
        if (other instanceof BusStop) {
            int result = Double.compare(fStopNumber, ((BusStop) other)
                    .getStopNumber());
            if (result == 0)
                return this.getSourceRange().getOffset()
                       - ((BusStop) other).getSourceRange().getOffset();
            return result;
        }
        return 0;
    }

    public static String getStopNumberAsString(double stopNumber){
        if (stopNumber - Math.round(stopNumber) == 0.0) {
            return String.valueOf(Math.round(stopNumber));
        }
        return String.valueOf(stopNumber);
    }
    
    public String getStopNumberAsString() {
        return getStopNumberAsString(fStopNumber);
    }

    public String toString() {
        if ("".equals(fDescription)) {
            return "Stop #" + getStopNumberAsString() + " @ "
                   + getCompilationUnit().getElementName() + " line "
                   + getSourceRange().getOffset() + " ( "
                   + getSourceRange().getLength() + " ) ";

        }
        return fDescription + " (" + getStopNumberAsString() + ")";
    }
 
    public String toCode(){
        return toCode(getStopNumber(), getRoute(), getDescription());
    }
    
    public static String toCode(double stopNumber, String route,
            String description) {
        return "@JTourBusStop " + getStopNumberAsString(stopNumber) + ", " + route + ", "
               + description + ":";
    }
}