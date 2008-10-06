/*
 * Created on 08.02.2005
 *
 */
package de.fu_berlin.inf.jtourbus;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;

import org.eclipse.jdt.core.ICompilationUnit;

public class TourPlan {

    /*
     * Switch to treeset so that we can get the first one easily
     */ 
    public Map<String, TreeSet<BusStop>> routes = new TreeMap<String, TreeSet<BusStop>>();

    public Map<ICompilationUnit, Set<BusStop>> cus = new HashMap<ICompilationUnit, Set<BusStop>>() {

		private static final long serialVersionUID = -3716402864743092432L;

		public String toString() {
            StringBuffer s = new StringBuffer();
            s.append("{");
            Iterator<Entry<ICompilationUnit, Set<BusStop>>> i = entrySet()
                    .iterator();
            if (i.hasNext()) {
                Entry<ICompilationUnit, Set<BusStop>> e = i.next();
                s.append(e.getKey().getElementName());
                s.append("=>");
                s.append(e.getValue().toString());
            }
            while (i.hasNext()) {
                Entry<ICompilationUnit, Set<BusStop>> e = i.next();
                s.append(",\n");
                s.append(e.getKey().getElementName());
                s.append("=>");
                s.append(e.getValue().toString());
            }

            s.append("}");
            return s.toString();
        }
    };

    public void remove(BusStop oldStop) {

        Set<BusStop> cu = cus.get(oldStop.getCompilationUnit());
        if (cu != null) {
            cu.remove(oldStop);
            if (cu.size() == 0) {
                cus.remove(oldStop.getCompilationUnit());
            }
        }

        Set<BusStop> route = routes.get(oldStop.getRoute());
        if (route != null) {
            route.remove(oldStop);
            if (route.size() == 0) {
                routes.remove(oldStop.getRoute());
            }
        }
    }

    public void add(BusStop newStop) {

        if (!cus.containsKey(newStop.getCompilationUnit())) {
            cus.put(newStop.getCompilationUnit(), new TreeSet<BusStop>());
        }
        cus.get(newStop.getCompilationUnit()).add(newStop);

        if (!routes.containsKey(newStop.getRoute())) {
            routes.put(newStop.getRoute(), new TreeSet<BusStop>());
        }
        routes.get(newStop.getRoute()).add(newStop);
    }

    public BusStop getNext(BusStop stop) {
        Iterator<BusStop> i = routes.get(stop.getRoute()).iterator();

        while (i.hasNext()) {
            if (i.next() == stop) {
                return (i.hasNext() ? i.next() : null);
            }
        }
        return null;
    }

    public BusStop getPrevious(BusStop stop) {
        Iterator<BusStop> i = routes.get(stop.getRoute()).iterator();

        BusStop previous = null;
        while (i.hasNext()) {

            if (previous == null) {
                previous = i.next();
                if (!i.hasNext()) {
                    return null;
                }
            }

            BusStop current = i.next();
            if (current == stop) {
                return previous;
            }
            previous = current;
        }
        return null;
    }

    public void removeAll() {
        cus.clear();
        routes.clear();
    }

    public Set<BusStop> removeCU(ICompilationUnit cu) {
        Set<BusStop> stops = cus.get(cu);
        Set<BusStop> copy = new TreeSet<BusStop>();
        
        if (stops != null) {
            copy.addAll(stops);
            for (BusStop bs : copy) {
                remove(bs);
            }
        }
        return copy;
    }
}