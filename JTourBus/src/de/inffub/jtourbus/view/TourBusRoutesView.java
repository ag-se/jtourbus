package de.inffub.jtourbus.view;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.texteditor.ITextEditor;

import de.inffub.jtourbus.BusStop;
import de.inffub.jtourbus.JTourBusStop;
import de.inffub.jtourbus.actions.InsertJTourBusCommentAction;
import de.inffub.jtourbus.actions.UpdateStopsOperation;
import de.inffub.jtourbus.utility.IconManager;
import de.inffub.jtourbus.utility.Utilities;

/**
 * The TourBusRoutesView is used to display information about the routes and
 * stops that have been found in the project currently selected.
 * 
 * @StopInformation "Important Types"
 * 
 * What you should know after visiting this stop:
 * 
 * o That this is the central class of the Plugin.
 * 
 * o It host connects the individual parts and handles the user interaction.
 * 
 * The type is explained in more detail on the route "Interaction".
 */
@JTourBusStop(value = 1.0, route = "Important Types", description = "JTourBusRoutesView - The View manages the whole affair.")
public class TourBusRoutesView extends ViewPart {

    private TreeViewer fViewer;

    private Action fActionRefresh, fActionSetFromEditor, fActionNextStop,
            fActionPreviousStop, fRenameTourAction;

    private InsertJTourBusCommentAction fActionInsertBusStop;

    private TourBusRoutesContentProvider fContentProvider;

    private IJavaProject fJavaProject;

    private PrintStream log;

    private IStructuredSelection fLastSelection;

    /**
     * @JTourBusStop 4.0, Logging Route, Close log when disposing the Part:
     */
    public void dispose() {
        super.dispose();
        log("FINISH");
        if (log != null) {
            log.close();
            log = null;
        }
    }

    /**
     * This is a callback that will allow us to create the viewer and initialize
     * it.
     * 
     * @JTourBusStop 0.0, Interaction, createPartControl - Entry Point of the
     * plugin:
     * 
     * This is the setup-entry-point for this plugin. The Eclipse Platform calls
     * this method when it initializes this view.
     * 
     * The following connections between the classes get established here: - A
     * new treeviewer is created for showing the routes later on. - The
     * associated ContentProvider is created and connected to the viewer. - A
     * label provider is created and connected also. - The view-part subscribes
     * to selection listener here (so when the user selects something new we can
     * change our view)
     */
    public void createPartControl(Composite parent) {

        fJavaProject = null;

        if (log != null) {
            log("START");
        }

        { // Create TreeViewer
            fViewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL
                                             | SWT.V_SCROLL);
            fContentProvider = new TourBusRoutesContentProvider(getViewSite());
            fViewer.setContentProvider(fContentProvider);
            fViewer.setLabelProvider(new TourBusRoutesLabelProvider());
            fViewer.setInput(getViewSite());

            // Selection Handling
            fViewer
                    .addSelectionChangedListener(new ISelectionChangedListener() {

                        public void selectionChanged(SelectionChangedEvent event) {
                            Object obj = ((IStructuredSelection) event
                                    .getSelection()).getFirstElement();

                            fActionNextStop
                                    .setEnabled(obj instanceof BusStop
                                                && fContentProvider.fTourPlan
                                                        .getNext((BusStop) obj) != null);
                            fActionPreviousStop
                                    .setEnabled(obj instanceof BusStop
                                                && fContentProvider.fTourPlan
                                                        .getPrevious((BusStop) obj) != null);
                        }
                    });

            // Drag and Drop Handling
            int ops = DND.DROP_MOVE;

            Transfer[] types = new Transfer[] { LocalSelectionTransfer
                    .getTransfer() };
            de.inffub.jtourbus.utility.ViewerDropAdapter vda = new de.inffub.jtourbus.utility.ViewerDropAdapter(
                    fViewer) {

                public boolean performDrop(Object data) {

                    IStructuredSelection selection = (IStructuredSelection) LocalSelectionTransfer
                            .getTransfer().getSelection();

                    @SuppressWarnings("unchecked")
                    Vector<BusStop> stops = new Vector(selection.toList());

                    BusStop target = (BusStop) getCurrentTarget();
                    boolean before = getCurrentLocation() == ViewerDropAdapter.LOCATION_BEFORE;

                    BusStop other = (before ? fContentProvider.fTourPlan
                            .getPrevious(target) : fContentProvider.fTourPlan
                            .getNext(target));

                    double delta;
                    if (other == null) {
                        delta = (before ? -1.0 : 1.0);
                    } else {
                        delta = (other.getStopNumber() - target.getStopNumber())
                                / (stops.size() + 1);
                    }

                    String targetRoute = target.getRoute();
                    double targetStopNumber = target.getStopNumber();
                    int i = 0;
                    for (BusStop stop : stops) {
                        fContentProvider.fTourPlan.remove(stop);
                        stop.setRoute(targetRoute);
                        stop.setStopNumber(targetStopNumber + delta * ++i);
                        fContentProvider.fTourPlan.add(stop);
                    }

                    double d = 1;
                    for (BusStop stop : fContentProvider.fTourPlan.routes
                            .get(targetRoute)) {
                        if (stop.getStopNumber() != d) {
                            stop.setStopNumber(d);
                            if (!stops.contains(stop)) {
                                stops.add(stop);
                            }
                        }
                        d++;
                    }

                    UpdateStopsOperation op = new UpdateStopsOperation(stops);
                    op.run();

                    return true;
                }

                public boolean validateDrop(Object target, int operation,
                        TransferData transferType) {
                    return target instanceof BusStop;
                }
            };

            // We do care about DND-sorting in this case.
            vda.setFeedbackEnabled(true);

            fViewer.addDropSupport(ops, types, vda);

            fViewer.addDragSupport(ops, types, new DragSourceListener() {
                /*
                 * Todo todo;
                 * 
                 * Todo parent;
                 */
                public void dragStart(DragSourceEvent arg0) {
                    IStructuredSelection selection = (IStructuredSelection) fViewer
                            .getSelection();

                    arg0.doit = true;
                    Iterator i = selection.iterator();
                    while (i.hasNext()) {
                        if (!(i.next() instanceof BusStop)) {
                            arg0.doit = false;
                            break;
                        }
                    }
                }

                // List<BusStop> stops;

                public void dragSetData(DragSourceEvent arg0) {
                    IStructuredSelection selection = (IStructuredSelection) fViewer
                            .getSelection();
                    // List<BusStop>stops = (List<BusStop>)selection.toList();
                    LocalSelectionTransfer.getTransfer()
                            .setSelection(selection);
                }

                public void dragFinished(DragSourceEvent arg0) {
                    // Not needed because of incremental update
                    // fActionRefresh.run();
                }
            });
        }

        makeActions();
        hookActions(getViewSite().getActionBars().getToolBarManager());
        hookContextMenu();

    }

    DateFormat loggerFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM,
            DateFormat.MEDIUM);

    /**
     * @JTourBusStop 3.0, Logging Route, Log Method - Write Timestamp and
     * message:
     */
    private void log(String s) {
    	
    	// Not used really
    	if (true)
    		return;
    	
        if (log == null) {
            Calendar d = Calendar.getInstance();

            String now = String
                    .format("%1$tY-%1$tm-%1$td-%1$tH.%1$tM.%1$tS", d);

            try {
                log = new PrintStream(new FileOutputStream(
                        "Z:\\Logs\\jtourbus-log-" + now + ".txt", true));
            } catch (FileNotFoundException e) {
                try {
                    log = new PrintStream(new FileOutputStream("jtourbus-log-"
                                                               + now + ".txt"));
                } catch (FileNotFoundException e1) {
                    log = null;
                }
            }

        }
        if (log != null) {
            Date d = new Date();
            log.println(loggerFormat.format(d) + "\t"
                        + System.currentTimeMillis() + "\t" + s);
            log.flush();
        }
    }

    private void makeActions() {

        { // Refresh
            fActionRefresh = new Action() {
                public void run() {
                    log("REFRESH");
                    BusStop oldStop = getSelectedBusStop();
                    // fViewer.setInput(fViewer.getInput());
                    fViewer.setInput(fJavaProject);
                    if (oldStop != null) {
                        Set<BusStop> ts = fContentProvider.fTourPlan.routes
                                .get(oldStop.getRoute());
                        if (ts != null) {
                            Iterator<BusStop> i = ts.iterator();
                            BusStop bs = null;
                            while (i.hasNext()) {
                                bs = i.next();
                                if (oldStop.getStopNumber() <= bs
                                        .getStopNumber()) {
                                    break;
                                }
                            }

                            if (bs != null) {
                                fViewer.setSelection(
                                        new StructuredSelection(bs), true);
                            }
                        }
                    }
                }
            };
            fActionRefresh.setText("Refresh BusTours");
            fActionRefresh
                    .setToolTipText("Updates the Bus Tour information from the current project. The list view will be updated to show the stops and routes of the project.");

            JavaPluginImages.setLocalImageDescriptors(fActionRefresh,
                    "refresh_nav.gif");
        }
        
        { // Action for renaming tours
            
            fRenameTourAction = new Action(){
                
                public void renameTourStop(BusStop stop, String targetTour){
                    fContentProvider.fTourPlan.remove(stop);
                    stop.setRoute(targetTour);
                    fContentProvider.fTourPlan.add(stop);
                }
                
                public void run() {
                    IStructuredSelection selection = ((IStructuredSelection) fViewer.getSelection());
                    if (!selection.isEmpty()){
                        
                        Object o = selection.getFirstElement();
                        String tour = null;
                        if (o instanceof BusStop){
                            tour = ((BusStop)o).getRoute();
                        } else {
                            tour = (String)o;
                        }
                        
                        InputDialog inputBox = new InputDialog(getSite().getShell(), "Rename tour", "Please enter the name of tour the selected stops are supposed to be on:", 
                                tour, new IInputValidator(){
                                    public String isValid(String newText) {
                                        newText = newText.trim();
                                        if ("".equals(newText) || newText == null){
                                            return "Please enter a new tour name!";
                                        }
                                        if (newText.contains(",") || newText.contains("/*") || newText.contains("*/")){
                                            return "Please don't \",\", " + "\"\\" + "*" + "\" or \"*" + "\\\" in tour names!";
                                        }
                                        return null;
                                    }}); 
                        inputBox.open();
                        if (inputBox.getReturnCode() == InputDialog.OK){
                            
                            String newTour = inputBox.getValue().trim();
                            
                            List<BusStop> busStopsToWriteBack = new ArrayList<BusStop>();
                            
                            for (Object selected : selection.toList()){
                                if (selected instanceof BusStop){
                                    renameTourStop((BusStop)selected, newTour);
                                    busStopsToWriteBack.add((BusStop)selected);
                                } else {
                                    for (BusStop bs : new ArrayList<BusStop>(fContentProvider.fTourPlan.routes.get(selected))){
                                        if (!busStopsToWriteBack.contains(bs)){
                                            renameTourStop(bs, newTour);
                                            busStopsToWriteBack.add(bs);
                                        }
                                    }
                                }
                            }
                                                        
                            UpdateStopsOperation op = new UpdateStopsOperation(busStopsToWriteBack);
                            op.run();
                        }
                    }
                }
            };
            fRenameTourAction.setText("&Rename tour");
            
            /*
             * 
             * ICommandService cs = (ICommandService)getViewSite().getAdapter(CommandService.class);
            // cs.getCommand();
             
             ICommandService cs = (ICommandService) 
             getViewSite().getWorkbenchWindow().getWorkbench().getAdapter(ICommandService.class); 

             cs.getCommand("org.eclipse.ui.edit.rename").setHandler(new 
             ActionHandler(fRenameTourAction));
             
             // fRenameTourAction.
            //fRenameTourAction.setAccelerator()
             */
            fRenameTourAction .setToolTipText("Rename a tour or move individual stops to other tour.");
        }
        

        { // Action for setting the current project form the active editor

            fActionSetFromEditor = new Action() {
                public void run() {
                    IJavaProject project = Utilities.getProject(fLastSelection);
                    if (project != null){
                        fJavaProject = project;
                    }
                    fActionRefresh.run();

                }
            };
            fActionSetFromEditor.setText("(Re)build Tours");
            fActionSetFromEditor
                    .setToolTipText("Will determine the project you are working on and build tours by scanning this project.");

            JavaPluginImages.setLocalImageDescriptors(fActionSetFromEditor,
            "refresh_nav.gif");
            //JavaPluginImages.setLocalImageDescriptors(fActionSetFromEditor,
            //        "gointo_toplevel_type.gif");

            /**
             * @JTourBusStop 2, Interaction, selectionChanged
             * 
             * This Listener tracks the previously selected JavaElement in the
             * whole Eclipse page.
             * 
             * We track these changes, since when the action above is triggered
             * we would like to know which element has been selected.
             */
            getViewSite().getPage().addSelectionListener(
                    new ISelectionListener() {
                        public void selectionChanged(IWorkbenchPart part,
                                ISelection selection) {

                            if (selection != null
                                && selection instanceof IStructuredSelection)
                                fLastSelection = (IStructuredSelection) selection;
                        }
                    });

        }

        { // Actions for moving to next and previous stop

            fActionNextStop = new Action() {
                public void run() {
                    BusStop currentStop = getSelectedBusStop();
                    if (currentStop != null) {
                        BusStop stop = fContentProvider.fTourPlan
                                .getNext(currentStop);
                        fViewer.setSelection(new StructuredSelection(stop),
                                true);
                        showStop(stop);
                    }
                }
            };

            fActionNextStop.setText("Move cursor to next stop");
            fActionNextStop
                    .setToolTipText("Moves the cursor to the next stop on the tour.");
            IconManager.setImageDescriptors(fActionNextStop, IconManager.NEXT);
            fActionNextStop.setEnabled(false);

            fActionPreviousStop = new Action() {
                public void run() {

                    BusStop currentStop = getSelectedBusStop();
                    if (currentStop != null) {
                        BusStop stop = fContentProvider.fTourPlan
                                .getPrevious(currentStop);
                        fViewer.setSelection(new StructuredSelection(stop),
                                true);
                        showStop(stop);
                    }
                }
            };
            fActionPreviousStop.setText("Move cursor to previous stop");
            fActionPreviousStop
                    .setToolTipText("Moves the cursor to the previous stop on the tour.");
            fActionPreviousStop.setEnabled(false);

            IconManager.setImageDescriptors(fActionPreviousStop,
                    IconManager.PREVIOUS);
        }

        { // Action for insert a bus stop in the current editor
            fActionInsertBusStop = new InsertJTourBusCommentAction(getSite()) {
                public void run() {
                    ISelection selection = null;
                    try {
                        selection = JavaPlugin.getActivePage()
                                .getActiveEditor().getEditorSite()
                                .getSelectionProvider().getSelection();
                    } catch (Exception e) {
                        // If there is no selection. Then just do nothing.
                        return;
                    }
                    BusStop currentStop = getSelectedBusStop();
                    setCurrentStop(currentStop);

                    if (selection instanceof ITextSelection) {
                        run((ITextSelection) selection, (JavaEditor) JavaPlugin
                                .getActivePage().getActiveEditor().getAdapter(
                                        JavaEditor.class));
                    }
                    fContentProvider.fTourChangeListener.notifyView = true;
                }
            };

            IconManager.setImageDescriptors(fActionInsertBusStop,
                    IconManager.STOP);
        }

        { // Add handler for double click
            fViewer.addDoubleClickListener(new IDoubleClickListener() {
                public void doubleClick(DoubleClickEvent event) {
                    fDoubleClickAction.run();
                }
            });
        }
    }

    /**
     * Triggering the action will jump from the view into the editor.
     * 
     * @JTourBusStop 3.0, Interaction, doubleClickAction - From
     * TourBusRoutesView to the Editor:
     * 
     * This is the reverse direction as the previous stop (2.0). The user has
     * selected a project and tours are being displayed. To take a tour the user
     * can double click on a stop to be taken to the annotated spot in the code.
     * 
     */
    private Action fDoubleClickAction = new Action() {
        public void run() {
            Object obj = getSelectedBusStop();
            if (obj instanceof BusStop) {
                showStop((BusStop) obj);
            }
        }
    };

    

    private void hookContextMenu() {
        MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
        
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(new IMenuListener() {
            public void menuAboutToShow(IMenuManager manager) {
                
                IStructuredSelection selection = ((IStructuredSelection) fViewer.getSelection());
                if (!selection.isEmpty()){
                    manager.add(fRenameTourAction);
                    manager.add(new Separator());
                }
                                
                TourBusRoutesView.this.hookActions(manager);
            }
        });
        Menu menu = menuMgr.createContextMenu(fViewer.getControl());
        fViewer.getControl().setMenu(menu);
        getSite().registerContextMenu(menuMgr, fViewer);
    }

    /**
     * Add actions to the toolbar
     */
    private void hookActions(IContributionManager manager) {
        // Difference between refresh and set is confusing
        // manager.add(fActionRefresh);

        manager.add(fActionSetFromEditor);
        manager.add(new Separator());
        manager.add(fActionInsertBusStop);
        manager.add(new Separator());
        manager.add(fActionNextStop);
        manager.add(fActionPreviousStop);
        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
    }

    /*
     * -------------------------------------------------------------------------
     * Little helper functions
     * -------------------------------------------------------------------------
     */
    /**
     * Passing the focus request to the viewer's control.
     */
    public void setFocus() {
        fViewer.getControl().setFocus();
    }

    /**
     * @JTourBusStop 2.0, Logging Route, When opening a tour-stop log to file:
     */
    private void showStop(BusStop busStop) {
        try {
            log("GOTOSTOP\t" + busStop.getRoute() + "\t"
                + busStop.getStopNumber() + "\t" + busStop.getDescription());

            // Show in Editor
            IEditorPart editorPart = EditorUtility.openInEditor(busStop
                    .getCompilationUnit());

            if (editorPart != null && editorPart instanceof ITextEditor)
                ((ITextEditor) editorPart).selectAndReveal(busStop
                        .getSourceRange().getOffset(), busStop.getSourceRange()
                        .getLength());

            /*
             * But don't forget to set selection of the viewer to match!
             */
            fViewer.setSelection(new StructuredSelection(busStop), true);

        } catch (PartInitException e) {
            showMessage("Could not open editor." + e.getLocalizedMessage()); //$NON-NLS-1$
        } catch (JavaModelException e) {
            showMessage("Could not open editor." + e.getLocalizedMessage()); //$NON-NLS-1$
        }
    }

    protected BusStop getSelectedBusStop() {
        ISelection selection = fViewer.getSelection();
        Object obj = ((IStructuredSelection) selection).getFirstElement();
        // If it is a BusStop just return it
        if (obj instanceof BusStop) {
            return (BusStop) obj;
        }
        // If it is a tour then return the first stop if such exist
        if (obj instanceof String) {
            TreeSet<BusStop> route = fContentProvider.fTourPlan.routes.get(obj);
            if (route != null && route.size() > 0) {
                return route.first();
            }
        }
        // Otherwise nothing selected
        return null;
    }

    private void showMessage(String message) {
        MessageDialog.openInformation(fViewer.getControl().getShell(),
                "JTourBus", message);
    }
}