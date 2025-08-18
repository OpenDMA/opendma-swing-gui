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
import java.io.FileInputStream;
import java.io.IOException;
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
import javax.swing.SwingUtilities;

import org.opendma.api.OdmaAdaptor;
import org.opendma.api.OdmaAdaptorDiscovery;
import org.opendma.api.OdmaId;
import org.opendma.api.OdmaRepository;
import org.opendma.api.OdmaSession;
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
		// Adaptor Discovery
		OdmaAdaptorDiscovery adaptorDiscovery = new OdmaAdaptorDiscovery();
		// load config from properties
		Properties cfg = new Properties();
		FileInputStream is;
		try
		{
			is = new FileInputStream("./opendma-swing-gui.properties");
			try
			{
				cfg.load(is);
			}
			finally
			{
				is.close();
			}
		}
		catch(IOException ioe)
		{
			System.err.println("Error reading config file ./opendma-swing-gui.properties:");
			ioe.printStackTrace(System.err);
			return;
		}
		// setup
		ArrayList<RepoData> repos = new ArrayList<RepoData>();
		String countStr = cfg.getProperty("count");
		if(countStr == null)
		{
			System.err.println("Missing config key: count");
			return;
		}
		int count;
		try
		{
			count = Integer.parseInt(countStr);
		}
		catch(NumberFormatException nfe)
		{
			System.err.println("Invalid number in config key: count");
			return;
		}
		for(int i = 0; i < count; i++)
		{
			String enabled = cfg.getProperty("repo."+i+".enabled");
			if(enabled != null && !enabled.equalsIgnoreCase("true"))
			{
				continue;
			}
			String adaptorId = cfg.getProperty("repo."+i+".adaptor");
			if(adaptorId == null)
			{
				System.err.println("Missing config key: repo."+i+".adaptor. Skipping");
				continue;
			}
			String repoId = cfg.getProperty("repo."+i+".repo");
			if(repoId == null)
			{
				System.err.println("Missing config key: repo."+i+".repo. Skipping");
				continue;
			}
			Properties adaptorProps = new Properties();
			for(String s : cfg.stringPropertyNames())
			{
				String prefix = "repo."+i+".props.";
				if(s.startsWith(prefix))
				{
					adaptorProps.setProperty(s.substring(prefix.length()), cfg.getProperty(s));
				}
			}
			System.out.println("Setting up session with adaptor "+adaptorId+"...");
			OdmaAdaptor adaptor = adaptorDiscovery.getAdaptor(adaptorId);
			if(adaptor == null)
			{
				System.err.println("No suitable adaptor found with this id.");
				return;
			}
			try
			{
				OdmaSession session = adaptor.connect(adaptorProps);
				System.out.println("Retrieving repository "+repoId+"...");
				OdmaId rid = new OdmaId(repoId);
				OdmaRepository repo = session.getRepository(rid);
				RepoData rd = new RepoData();
				rd.session =  session;
				rd.repoId = rid;
				rd.repo = repo;
				repos.add(rd);
			}
			catch (OdmaException e)
			{
				System.err.println("Error establishing connection: "+e);
				e.printStackTrace(System.err);
				return;
			}
		}
		// start ui
		System.out.println("Initialized. Starting UI...");
		new OpendmaSwingManager(repos);
	}
	
	private static class RepoData
	{
	    OdmaSession  session;
	    OdmaId repoId;
	    OdmaRepository repo;
	}
	
	protected JSplitPane splitPane;
	
	protected JPanel emptyViewComponent = new JPanel();
	
	public OpendmaSwingManager(List<RepoData> repositories)
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
			System.err.println("Error setting native Look-and-Feel: " + e);
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
		for(RepoData rd : repositories)
		{
			rootNode.add(new RepositoryNode(rd.session, rd.repoId, rd.repo));
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
				int currentDividerLocation = splitPane.getDividerLocation();
				Object o = tree.getLastSelectedPathComponent();
				Component viewComponent = null;
				if( (o != null) && (o instanceof ViewNode) )
				{
					viewComponent = ((ViewNode)o).getViewComponent();
				}
				splitPane.setRightComponent((viewComponent != null) ? viewComponent : emptyViewComponent );
				// restore divider location immediately and once again after layout
				splitPane.setDividerLocation(currentDividerLocation);
				SwingUtilities.invokeLater(new Runnable() {
					public void run()
					{
						splitPane.setDividerLocation(currentDividerLocation);
					}
				});
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
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treeScrollPane, emptyViewComponent);
		splitPane.setOneTouchExpandable(false);
		splitPane.setDividerLocation(250);
		splitPane.setResizeWeight(0.0);
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
