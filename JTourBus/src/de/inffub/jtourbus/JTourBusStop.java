package de.inffub.jtourbus;

/**
 * The JTourBusStop is a normal annotation that can be used to tag a program
 * element so that it will be included in the list of tours offered for the
 * current project.
 * 
 * This is an alternative to using the JavaDoc based tags.
 * 
 * @JTourBusStop 0.0, Important Types, JTourBusStop - Annotation to get program
 * elements listed:
 * 
 * When you stop by here make sure to understand the following concepts by
 * reading the JavaDocs:
 * 
 * o How the value attribute affects the ordering of the stops on a route.
 * 
 * o How to create routes.
 * 
 * o What the description parameter is for.
 * 
 * o Be aware that some of the documentation might be a little bit out of date.
 * Since the JavaDoc tags were introduced after the annotations.
 * 
 * @version 0.2 - 27.07.2005
 * @author Christopher Oezbek
 */
public @interface JTourBusStop {
    /**
     * Using the value parameter it is possible to set the position on the route
     * that the stop is on. For instance a stop with value == 0.5 will be in
     * front of a stop with value == 1.2
     * 
     * @Example public @ JTourBusStop(0.0) static void main(String[] args){...}
     * 
     */
    double value();

    /**
     * The route parameter can be used to categorize BusStops. A viewer of the
     * BusPlan can then put BusStops of different routes into different folders.
     * 
     * @Example public @ JTourBusStop(0.0, "Europe-Trip") paris(){...} public @ JTourBusStop(1.0,
     * "Europe-Trip") london(){...} public @ JTourBusStop(1.2, "Europe-Trip")
     * londonDungeon(){...} public @ JTourBusStop(0.0, "The States")
     * newYork(){...}
     * 
     * @return The Route the bus stop should be displayed on
     */
    String route() default "Main Route";

    /**
     * A *short* descriptive text for the bus-stop that can be used in the .
     * Important information about the tagged element should be given in the
     * associated JavaDoc comment.
     * 
     * @Example public @ JTourBusStop(0.0, "Main-Route", "The most important
     * function to know") doFoo(){...} public @ JTourBusStop(1.0, "Main-Route",
     * "If you then have time have a look here") doBar(){...}
     * 
     * @return A *short* descriptive text about the bus-stop
     */
    String description() default "";
}