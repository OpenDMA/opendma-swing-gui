package com.xaldon.opendma.swingmanager;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.opendma.AdaptorManager;
import org.opendma.OdmaSession;
import org.opendma.api.OdmaId;
import org.opendma.api.OdmaRepository;
import org.opendma.exceptions.OdmaException;

import com.xaldon.opendma.swingmanager.treemodel.ContextMenuNode;
import com.xaldon.opendma.swingmanager.treemodel.ViewNode;
import com.xaldon.opendma.swingmanager.treenodes.RepositoryNode;
import com.xaldon.opendma.swingmanager.treenodes.ServersNode;

public class OpendmaSwingManager extends JFrame
{

    private static final long serialVersionUID = 3726489983715185270L;

    public static void main(String[] args)
    {
        System.setProperty("wasp.location", "<location-of-systinet-wasp>");
        // register Adaptors
        try
        {
            loadAdaptors();
        }
        catch (Exception e)
        {
            System.out.println("Error loading Adaptors: "+e);
            e.printStackTrace(System.out);
            System.exit(0);
        }
        // Filenet
        Properties filenetConnectionProperties = new Properties();
        filenetConnectionProperties.put("user", "<username>");
        filenetConnectionProperties.put("password", "<password>");
        filenetConnectionProperties.put("url", "filenetp8:<filenet-p8-endpoint-url>");
        System.out.println("Setting up Filenet session...");
        OdmaSession filenetSession = null;
        OdmaRepository filenetRepository = null;
        try
        {
            filenetSession = AdaptorManager.getSession(filenetConnectionProperties);
            System.out.println("Retrieving Filenet repository...");
            filenetRepository = filenetSession.getRepository(new OdmaId("<objectstore-name>"));
        }
        catch (OdmaException e)
        {
            System.out.println("Error establishing connection: "+e);
            e.printStackTrace(System.out);
            System.exit(0);
        }
        // IBMCM
        /*
        Properties ibmcmConnectionProperties = new Properties();
        //ibmcmConnectionProperties.put("user", "<username>");
        //ibmcmConnectionProperties.put("password", "<password>");
        ibmcmConnectionProperties.put("user", "<username>");
        ibmcmConnectionProperties.put("password", "<password>");
        ibmcmConnectionProperties.put("url", "ibmcm:<libraryserver-name>");
        System.out.println("Setting up IBMCM session...");
        OdmaSession ibmcmSession = null;
        OdmaRepository ibmcmRepository = null;
        try
        {
            ibmcmSession = AdaptorManager.getSession(ibmcmConnectionProperties);
            System.out.println("Retrieving IBMCM repository...");
            ibmcmRepository = ibmcmSession.getRepository(new OdmaId("<libraryserver>"));
        }
        catch (OdmaException e)
        {
            System.out.println("Error establishing connection: "+e);
            e.printStackTrace(System.out);
            System.exit(0);
        }
        */
        // start ui
        System.out.println("Initialized. Starting UI...");
        new OpendmaSwingManager(new OdmaRepository[]{ filenetRepository /*, ibmcmRepository*/ });
    }
    
    public static void loadAdaptors() throws Exception
    {
        //loadFilenetAdaptor();
        try
        {
            Class.forName("com.xaldon.opendma.adaptor.filenet.Adaptor");
            Class.forName("com.xaldon.opendma.adaptor.ibmcm.Adaptor");
            Class.forName("com.xaldon.opendma.adaptor.cmis.Adaptor");
        }
        catch (ClassNotFoundException e)
        {
            System.out.println("Error loading adapter: "+e);
            e.printStackTrace(System.out);
        }
    }
    
    public static void loadFilenetAdaptor() throws Exception
    {
        List<URL> classPath = new ArrayList<URL>();
        classPath.add(new File( "<p8-api-file>" ).toURI().toURL());
        classPath.add(new File( "<p8-api-file>" ).toURI().toURL());
        classPath.add(new File( "<p8-api-file>" ).toURI().toURL());
        classPath.add(new File( "<p8-api-file>" ).toURI().toURL());
        classPath.add(new File( "<p8-api-file>" ).toURI().toURL());
        classPath.add(new File( "<p8-api-file>" ).toURI().toURL());
        URL[] urls = classPath.toArray(new URL[0]);
        ClassLoader classLoader = new URLClassLoader(urls, Thread.currentThread().getContextClassLoader());
        classLoader.loadClass("com.xaldon.opendma.adaptor.filenet.Adaptor").newInstance();
    }
    
    protected JSplitPane splitPane;
    
    protected JPanel emptyViewComponent = new JPanel();
    
    protected JScrollPane viewScrollPane = new JScrollPane(emptyViewComponent);
    
    public OpendmaSwingManager(OdmaRepository[] repositories)
    {
        // set the title of this Window
        super("OpenDMA Swing Manager");
        // Set the platform Look and Feel
        try
        {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
          }
        catch(Exception e)
        {
            System.out.println("Error setting native Look-and-Feel: " + e);
        }
        // exit this program when the window is closed
        addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent event)
            {
                System.exit(0);
            }
        });
        // create the tree
        DefaultMutableTreeNode rootNode = new ServersNode();
        for(OdmaRepository repo : repositories)
        {
            rootNode.add(new RepositoryNode(repo));
        }
        final JTree tree = new JTree(rootNode);
        tree.setCellRenderer(new IconNodeRenderer());
        JScrollPane treeScrollPane = new JScrollPane(tree);
        treeScrollPane.setBorder(null);
        // create the table
        final JTable table = new JTable();
        JScrollPane tableScrollPane = new JScrollPane(table);
        tableScrollPane.setBorder(null);
        // add SelectionListener to tree that triggers a Model Change in the table
        tree.addTreeSelectionListener(new TreeSelectionListener(){
            public void valueChanged(TreeSelectionEvent e)
            {
                Object o = tree.getLastSelectedPathComponent();
                /*
                if( (o != null) && (o instanceof TableModelProvider) )
                {
                    TableModel newModel = ((TableModelProvider)o).getTableModel();
                    if(newModel != null)
                    {
                        table.setModel(newModel);
                    }
                }
                */
                Component viewComponent = null;
                if( (o != null) && (o instanceof ViewNode) )
                {
                    viewComponent = ((ViewNode)o).getViewComponent();
                }
                viewScrollPane.setViewportView( (viewComponent != null) ? viewComponent : emptyViewComponent );
                //int divLoc = splitPane.getDividerLocation();
                //splitPane.setRightComponent( (viewComponent != null) ? viewComponent : emptyViewComponent );
                //splitPane.setDividerLocation(divLoc);
            }});
        // add content menu to tree and table
        MouseListener popupListener = new MouseAdapter(){
            public void mousePressed(MouseEvent e)
            {
                maybeShowPopup(e);
            }

            public void mouseReleased(MouseEvent e)
            {
                maybeShowPopup(e);
            }

            private void maybeShowPopup(MouseEvent e)
            {
                TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
                if (selPath == null)
                {
                    return;
                }
                else
                {
                    tree.setSelectionPath(selPath);
                }
                if (e.isPopupTrigger())
                {
                    //popup.show(e.getComponent(), e.getX(), e.getY());
                    Object o = tree.getLastSelectedPathComponent();
                    if( (o != null) && (o instanceof ContextMenuNode) )
                    {
                        JPopupMenu contextMenu = ((ContextMenuNode)o).getContextMenu();
                        contextMenu.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            }
        };
        tree.addMouseListener(popupListener);
        // create SplitPane between tree and table
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treeScrollPane, viewScrollPane);
        //splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treeScrollPane, tableScrollPane);
        splitPane.setOneTouchExpandable(false);
        splitPane.setDividerLocation(250);
        splitPane.setResizeWeight(0.5);
        splitPane.setContinuousLayout(true);
        // set the splitter as center component in this window
        Container content = getContentPane();
        content.add(splitPane, BorderLayout.CENTER);
        setSize(1000, 500);
        setVisible(true);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = getSize();
        setLocation( ( screenSize.width - frameSize.width ) / 2, ( screenSize.height - frameSize.height ) / 2 );
    }

}
