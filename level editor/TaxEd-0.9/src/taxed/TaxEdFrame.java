package taxed;
import java.awt.BorderLayout;
import java.awt.EventQueue;

import java.io.*;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JButton;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.SwingConstants;
import javax.swing.JSeparator;
import javax.swing.UIManager;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.Graphics;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import rsonic.*;
import javax.swing.JScrollPane;
import java.awt.Canvas;
import java.awt.Rectangle;
import javax.swing.JScrollBar;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowListener;
import java.awt.event.WindowEvent;
import java.util.List;
import javax.swing.JSplitPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.JTabbedPane;
import javax.swing.JPopupMenu;
import javax.swing.ImageIcon;
import java.awt.event.MouseMotionAdapter;
import java.util.LinkedList;
import javax.swing.KeyStroke;
import java.awt.event.KeyEvent;
import javax.swing.filechooser.*;
import javax.swing.filechooser.FileFilter;
import javax.imageio.ImageIO;
import java.awt.image.*;

public class TaxEdFrame extends JFrame {

	private JPanel contentPane;
	private final JMenuBar menuBar = new JMenuBar();
	private final JMenu mnFile = new JMenu("File");
	private final JMenuItem mntmOpen = new JMenuItem("Open...");
	private final JMenuItem mntmSave = new JMenuItem("Save");
	private final JMenuItem mntmSaveAs = new JMenuItem("Save As...");
	private final JMenuItem mntmRevert = new JMenuItem("Revert");
	private final JMenuItem mntmExit = new JMenuItem("Exit");
	private final JSeparator separator = new JSeparator();
	private final JMenu mnEdit = new JMenu("Edit");
	private final JMenuItem mntmUndo = new JMenuItem("Undo");
	private final JMenuItem mntmRedo = new JMenuItem("Redo");
	private final JMenu mnView = new JMenu("View");
	private final JMenu mnExtra = new JMenu("Extra");
	private final JMenuItem mntmClearMap = new JMenuItem("Clear map");
	private final JMenuItem mntmClearObjects = new JMenuItem("Clear objects");
	private final JMenuItem mntmDumpBlocks = new JMenuItem("Dump blocks to image");
	private final JMenuItem mntmDumpMap = new JMenuItem("Dump map to image");
	private final JMenu mnHelp = new JMenu("Help");
	private final JMenuItem mntmAbout = new JMenuItem("About TaxEd");
	private final String appTitleLong = "TaxEd - The Retro-Sonic Level Editor";
	private final String appTitle = "TaxEd";
	private String currentDirectory = null;
	RSMap map;
	private File curFile = null;
	boolean modified = false;
	private int saveMapType = 0;
	JMapDisplay display = new JMapDisplay();
	private final JScrollPane scrollPane = new JScrollPane();
	int mapData[][];
 	java.util.List<RSObject> objList;
	String zoneName;
	private final JSplitPane splitPane = new JSplitPane();
	private final JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
	private BlockSelectionPanel blockSelectionPanel;
	private int oldMx;
	private int oldMy;
	private RSObject draggedObject;
	private int selectedObjectIndex;
	private RSObject popupObject;
	private TaxEdFrame mainFrame = this;
	int popupX;
	int popupY;
	private boolean doneDragging = false;
	private LinkedList<LevelState> undoStack;
	private LinkedList<LevelState> redoStack;
	private LevelState oldState;
	
	class MapFileFilter extends FileFilter
	{
		private String description;
		private int type;
	
		public boolean accept(File f)
		{
			return true;
		}
	
		public String getDescription()
		{
			return this.description;
		}
		
		public int getType()
		{
			return this.type;
		}
		
		MapFileFilter(String description, int type)
		{
			this.description = description;
			this.type = type;
		}
	}
	
	class LevelState
	{
		String myZoneName;
		int myMapData[][];
		java.util.List<RSObject> myObjList;
		boolean myModified;
		int scrollHorizontalValue;
		int scrollVerticalValue;
		int selectedX, selectedY;
		int selectedObjectX, selectedObjectY;
		boolean objSel;
		Rectangle viewRect;
	
		LevelState()
		{
			int mapWidth = mapData[0].length;
			int mapHeight = mapData.length;
		
			myZoneName = zoneName;
	
			myMapData = new int[mapHeight][mapWidth];
	
			for(int y = 0; y < mapHeight; y++)
				for(int x = 0; x < mapWidth; x++)
					myMapData[y][x] = mapData[y][x];
					
			myObjList = new java.util.ArrayList<RSObject>();
			
			for(RSObject obj : objList)
				myObjList.add(obj.newInstance());
			
			myModified = modified;
			scrollHorizontalValue = scrollPane.getHorizontalScrollBar().getValue();
			scrollVerticalValue = scrollPane.getVerticalScrollBar().getValue();
			
			selectedX = display.getSelectedX();
			selectedY = display.getSelectedY();
			selectedObjectX = display.getSelectedObjectX();
			selectedObjectY = display.getSelectedObjectY();
			objSel = display.isObjectedSelected();
			
			viewRect = scrollPane.getViewport().getViewRect();
		}
		
		void restore()
		{
			mapData = myMapData;
			objList = myObjList;
			modified = myModified;
			zoneName = myZoneName;
			
			display.setMapData(mapData);
			display.setObjectList(objList);
			display.setSelectedCoords(selectedX, selectedY);
			
			blockSelectionPanel.setCurrentBlock(mapData, selectedX, selectedY, false);
			
			if(objSel)
				display.setSelectedObjectCoords(selectedObjectX, selectedObjectY);
			else
				display.setNoObjectSelected();
			
			setAppTitle(zoneName + (modified?"*":""));
			
			scrollPane.getHorizontalScrollBar().setValue(scrollHorizontalValue);
			scrollPane.getVerticalScrollBar().setValue(scrollVerticalValue);
			
			display.scrollRectToVisible(viewRect);
			display.repaint();
		}
	}
	
	private void error_message(String s)
	{
		JOptionPane.showMessageDialog(null,
				s,
				appTitle + " - Error",
				JOptionPane.ERROR_MESSAGE);
	}
	
	
	private boolean file_save()
	{
		return file_save(this.saveMapType);
	}
	
	private boolean file_save(int saveMapType)
	{
		try
		{
			map.setTileMap(mapData);
			map.setObjectList(objList);
			map.save(curFile, saveMapType);
			setModified(false);
			this.saveMapType = saveMapType;
				
			for(int i = 0, sz = undoStack.size(); i < sz; i++)
				undoStack.get(i).myModified = true;
					
			for(int i = 0, sz = redoStack.size(); i < sz; i++)
				redoStack.get(i).myModified = true;
					
			if(!redoStack.isEmpty())
				oldState.myModified = false;					
		}
		
		catch(Exception e)
		{
			curFile.delete();
			error_message(e.getMessage());
			return false;
		}
		
		return true;
	}
	
	private boolean file_save_check()
	{
		if(modified)
		{
			int option =
				JOptionPane.showConfirmDialog(null, "The map has been modified.\nDo you want to save it?");
				
			switch(option)
			{
				case JOptionPane.CANCEL_OPTION: return false;
				case JOptionPane.YES_OPTION: return file_save();
				case JOptionPane.NO_OPTION: break;
			}
		}
		
		return true;
	}
	
	public void setAppTitle(String s)
	{
		if(s == null)
			setTitle(appTitle);
		else
			setTitle(appTitle + " - " + s);
	}
	
	public void setAppTitleLong(String s)
	{
		if(s == null)
			setTitle(appTitleLong);
		else
			setTitle(appTitleLong + " - " + s);
	}

	/**
	 * Create the frame.
	 */
	public TaxEdFrame() {
		setMinimumSize(new Dimension(640, 550));
		setMaximumSize(new Dimension(0, 0));
			
		ImageIcon icon =
			new ImageIcon(this.getClass().getResource("/icons/taxed.gif"));
				
		setIconImage(icon.getImage());
		
		initGUI();
	}
	private void initGUI() {
		setAppTitleLong(null);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	//	setBounds(100, 100, 640, 500);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));
		contentPane.add(menuBar, BorderLayout.NORTH);
		
		addWindowListener(new WindowListener() {
			public void windowActivated(WindowEvent e) { }
			public void windowClosed(WindowEvent e) { }
			public void windowDeactivated(WindowEvent e) { }
			public void windowDeiconified(WindowEvent e) { }
			public void windowIconified(WindowEvent e) { }
			public void windowOpened(WindowEvent e) { }
			
			public void windowClosing(WindowEvent e)
			{
				if(file_save_check())
					System.exit(0);
			}
		});
		
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.anchor = GridBagConstraints.NORTH;
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 1;
		gbc_scrollPane.gridwidth = GridBagConstraints.REMAINDER;
		gbc_scrollPane.gridheight = GridBagConstraints.REMAINDER;
		
		menuBar.add(mnFile);
		mntmOpen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(!file_save_check())
					return;
			
				//new MapFileFilter("Autodetect map type");
				JFileChooser chooser = new JFileChooser(currentDirectory);
				chooser.setAcceptAllFileFilterUsed(false);
				chooser.addChoosableFileFilter(
					new MapFileFilter("Autodetect map type", RSMap.TYPE_AUTO));
				chooser.addChoosableFileFilter(
					new MapFileFilter("Sonic Nexus, Sonic CD map", RSMap.TYPE_v1));
				chooser.addChoosableFileFilter(
					new MapFileFilter("Sonic 1, Sonic 2 map", RSMap.TYPE_v2));
				chooser.addChoosableFileFilter(
					new MapFileFilter("Retro-Sonic map", RSMap.TYPE_RS));
				
				int r = chooser.showOpenDialog(null);
				
				if(r == JFileChooser.APPROVE_OPTION)
				{
					File selFile = chooser.getSelectedFile();
					
					try
					{
						map = new RSMap(selFile, ((MapFileFilter)chooser.getFileFilter()).getType());
						saveMapType = map.getLoadType();
						zoneName = map.getZoneName();
						setAppTitle(zoneName);
						curFile = selFile;
						
						mapData = map.getTileMap();
						objList = map.getObjectList();
						display.loadOtherData(curFile, map.getLoadType());

						gui_components_at_open();
						reset_display_area(mapData, objList);
						
						setModified(false);
						undoStack = new LinkedList<LevelState>();
						redoStack = new LinkedList<LevelState>();
						
						mntmUndo.setEnabled(false);
						mntmRedo.setEnabled(false);
						
						currentDirectory = selFile.getParent();
					}
					
					catch(Exception exc)
					{
						exc.printStackTrace();
						error_message(exc.getMessage());
					}
				}
				else if(r == JFileChooser.ERROR_OPTION)
				{
					error_message("Error when choosing file");
				}
			}
		});
		
		mntmOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
				ActionEvent.CTRL_MASK));
		
		mnFile.add(mntmOpen);
		mntmSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
/* Here goes code to save the map to the currently opened file */	
				file_save();	
			}
		});
		
		mntmSave.setEnabled(false);
		
		mntmSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
				ActionEvent.CTRL_MASK));
		
		mnFile.add(mntmSave);
		mntmSaveAs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
/* Here goes code to save the map to another file,
 * and to change the current file to that other file*/		
 
 				JFileChooser chooser = new JFileChooser(currentDirectory);
 				
				chooser.setAcceptAllFileFilterUsed(false);
				chooser.addChoosableFileFilter(
					new MapFileFilter("Current type", saveMapType));
				chooser.addChoosableFileFilter(
					new MapFileFilter("Sonic Nexus, Sonic CD map", RSMap.TYPE_v1));
				chooser.addChoosableFileFilter(
					new MapFileFilter("Sonic 1, Sonic 2 map", RSMap.TYPE_v2));
 				chooser.addChoosableFileFilter(
					new MapFileFilter("Retro-Sonic map", RSMap.TYPE_RS));
				
				int option = chooser.showSaveDialog(null);
				
				if(option == JFileChooser.APPROVE_OPTION)
				{
					curFile = chooser.getSelectedFile();
					
					MapFileFilter filter = (MapFileFilter)chooser.getFileFilter();
					
					if( file_save(filter.getType()) )
					{
						currentDirectory = curFile.getParent();
						modified = false;
					}
				}
				else if(option == JFileChooser.ERROR_OPTION)
				{
					error_message("Error when choosing file");
				}
			}
		});
		
		mntmSaveAs.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
				ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK));
		
		mntmSaveAs.setEnabled(false);
		mnFile.add(mntmSaveAs);
		mntmRevert.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
/*
 * Here goes code to revert to the map as it was loaded
 */			
				try
				{
					map = new RSMap(curFile);
					zoneName = map.getZoneName();
					setAppTitle(zoneName);
					setModified(false);
					//modified = false;
					display.loadOtherData(curFile, map.getLoadType());
					
					gui_components_at_open();
					reset_display_area(map.getTileMap(), map.getObjectList());
					
				/*	mapData = map.getTileMap();
					objList = map.getObjectList();
					display.setMapData(mapData);
					display.scrollRectToVisible(
						new Rectangle(0, 0, display.getWidth(), display.getHeight()));
					 display.setObjectList(objList);
					display.loadOtherData(curFile);
					scrollPane.getHorizontalScrollBar().setValue(0);
					scrollPane.getVerticalScrollBar().setValue(0);
					
					display.setSelectedCoords(0, 0);
					display.setNoObjectSelected();*/
				}
				
				catch(Exception exc)
				{
					error_message(exc.getMessage());
				}
			}
		});
		
		mntmRevert.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R,
				ActionEvent.CTRL_MASK));
		
		mntmRevert.setEnabled(false);
		mnFile.add(mntmRevert);
		
		mnFile.add(separator);		gbc_scrollPane.gridheight = GridBagConstraints.REMAINDER;

		mntmExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
/*
 * Here goes code to exit the program,
 * first we need to alert the user if the map was modified,
 * and if he wants to save it; then do what the user requests,
 * then exit.
 */			
				if(file_save_check())
					System.exit(0);
			}
		});
		
		mntmExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q,
			ActionEvent.CTRL_MASK));
		
		mnFile.add(mntmExit);
		
		menuBar.add(mnEdit);
		
		mntmUndo.setEnabled(false);
		mntmRedo.setEnabled(false);
		
		mntmUndo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(redoStack.isEmpty())
					redoStack.push(new LevelState());
				else
					redoStack.push(oldState);
				
				oldState = undoStack.pop();
				oldState.restore();
				
				mntmRedo.setEnabled(true);
				
				if(undoStack.isEmpty())
					mntmUndo.setEnabled(false);
			}
		});
		
		mntmRedo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				undoStack.push(oldState);
				
				oldState = redoStack.pop();
				oldState.restore();
				
				mntmUndo.setEnabled(true);
				
				if(redoStack.isEmpty())
					mntmRedo.setEnabled(false);
			}
		});
		
		mntmUndo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z,
			ActionEvent.CTRL_MASK));
		mntmRedo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z,
			ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK));
		
		mnEdit.add(mntmUndo);
		mnEdit.add(mntmRedo);
		
		
		menuBar.add(mnView);
		
		mntmAbout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				new AboutDialog(mainFrame);
			}
		});
		
		mnHelp.add(mntmAbout);
		
		mnExtra.add(mntmClearMap);
		
		mntmClearMap.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				saveState();
				
				int mapWidth = mapData[0].length;
				int mapHeight = mapData.length;
				
				for(int y = 0; y < mapHeight; y++)
					for(int x = 0; x < mapWidth; x++)
						mapData[y][x] = 0;
				
				setModified(true);
				reset_display_area(mapData, objList);
			}
		});		
		
		mnExtra.add(mntmClearObjects);
		
		mntmClearObjects.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				saveState();
				
				objList = new java.util.ArrayList<RSObject>();
				display.setNoObjectSelected();
				
				setModified(true);
				reset_display_area(mapData, objList);
			}
		});	
		
		mnExtra.add(mntmDumpBlocks);
		
		mntmDumpBlocks.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				
				JFileChooser chooser = new JFileChooser(currentDirectory);
				chooser.setAcceptAllFileFilterUsed(false);
					
				String formatNames[] = ImageIO.getWriterFormatNames();
					
				for(int i = 0, l = formatNames.length; i < l; i++)
					chooser.addChoosableFileFilter(
						new FileNameExtensionFilter(formatNames[i] + " Image", formatNames[i]));
						
				int option = chooser.showSaveDialog(null);
				
				if(option == JFileChooser.APPROVE_OPTION)
				{
					
					try {
						File imgFile = chooser.getSelectedFile();
						FileNameExtensionFilter filter = (FileNameExtensionFilter)chooser.getFileFilter();
						
						String selExt = (filter.getExtensions())[0];
						
						setAppTitle("Dumping blocks to image...");
						mainFrame.setEnabled(false);
						mainFrame.repaint();
						
						BufferedImage outImage = new BufferedImage(2048, 4096, BufferedImage.TYPE_INT_RGB);
						
						Graphics imgGfx = outImage.getGraphics();
						
						for(int y = 0, b = 0; y < 4096; y+=128)
							for(int x = 0; x < 2048; x+=128, b++)
								imgGfx.drawImage(display.blockImages[b], x, y, mainFrame);
						
						ImageIO.write(outImage, selExt, imgFile);
						
						setAppTitle(zoneName + (modified?"*":""));
						mainFrame.setEnabled(true);
						mainFrame.repaint();
						currentDirectory = imgFile.getParent();
					} 
					
					catch (IOException exc) 
					{
						error_message(exc.getMessage());
					}
				}
			}
		});
		
		mnExtra.add(mntmDumpMap);
		
		mntmDumpMap.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				JFileChooser chooser = new JFileChooser(currentDirectory);
				chooser.setAcceptAllFileFilterUsed(false);
					
				String formatNames[] = ImageIO.getWriterFormatNames();
					
				for(int i = 0, l = formatNames.length; i < l; i++)
					chooser.addChoosableFileFilter(
						new FileNameExtensionFilter(formatNames[i] + " Image", formatNames[i]));
						
				int option = chooser.showSaveDialog(null);
				
				if(option == JFileChooser.APPROVE_OPTION)
				{
					
					try {
						File imgFile = chooser.getSelectedFile();
						FileNameExtensionFilter filter = (FileNameExtensionFilter)chooser.getFileFilter();
						
						String selExt = (filter.getExtensions())[0];
						
						setAppTitle("Dumping map to image...");
						mainFrame.setEnabled(false);
						mainFrame.repaint();
						
						try
						{
							BufferedImage outImage = new BufferedImage(map.getWidth() * 32, 
																map.getHeight() * 32,
														BufferedImage.TYPE_INT_RGB);
							
													
							Graphics imgGfx = outImage.getGraphics();
						
							display.paintMap(imgGfx);
							
							ImageIO.write(outImage, selExt, imgFile);
							
							setAppTitle(zoneName + (modified?"*":""));
							mainFrame.setEnabled(true);
							mainFrame.repaint();
							currentDirectory = imgFile.getParent();
						}
						
						catch(OutOfMemoryError exc)
						{
							error_message("Out of memory error. You probably need to increase the heap size.");
							error_message("This can be done by invoking like: java -Xms256m -Xmx512m TaxEd.jar ");
						}

					} 
					
					catch (IOException exc) 
					{
						error_message(exc.getMessage());
					}
				}
				
			}
		});
		
		
		mnExtra.setEnabled(false);
		menuBar.add(mnExtra);
		
		menuBar.add(mnHelp);
		
		contentPane.add(splitPane, BorderLayout.SOUTH);
		display.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
			
				if(draggedObject != null)
				{
					if(!doneDragging)
						saveState();
				
					int mx = e.getX();
					int my = e.getY();
				
					draggedObject.setXPos( draggedObject.getXPos() + (mx - oldMx) );
					draggedObject.setYPos( draggedObject.getYPos() + (my - oldMy) );
				
					display.setSelectedObjectCoords(draggedObject.getXPos(),
													draggedObject.getYPos());
				
					oldMx = mx;
					oldMy = my;
					doneDragging = true;
				}
			}
		});
		display.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if(!display.ready)
					return;
			
				if(e.getButton() == MouseEvent.BUTTON1)
				{				
					int mx = e.getX();
					int my = e.getY();
					boolean isObject = false;
				
					// Check if we pressed on an object
					for(int x = 0, l = objList.size(); x < l; x++)
					{
						RSObject obj = objList.get(x);
					
						int ox = obj.getXPos();
						int oy = obj.getYPos();
					
						if(mx >= ox && mx < (ox+16) &&
							my >= oy && my < (oy+32))
						{
							/*if(draggingObject && (oldMx!=mx || oldMy!=my))
							{
								System.out.println("Dragging, fuck yeah");
								obj.setXPos(mx);
								obj.setYPos(my);
								ox = mx;
								oy = my;
							}*/
						
							draggedObject = obj;
							selectedObjectIndex = x;
							doneDragging = false;
							display.setSelectedObjectCoords(ox, oy);
							isObject = true;
							oldMx = mx;
							oldMy = my;
							break;
						}
					}
				
					if(!isObject)
					{
						int tx = mx / 128;
						int ty = my / 128;
				
						if(tx < mapData[0].length && ty < mapData.length)
						{
							display.setSelectedCoords(tx, ty);
							blockSelectionPanel.setCurrentBlock(mapData, tx, ty);
						}
						
						draggedObject = null;
					}
				}
					//error_message("Left, X: " + e.getX() + ", Y: " + e.getY());
				else if(e.getButton() == MouseEvent.BUTTON3)
				{
					//error_message("Right, X: " + e.getX() + ", Y: " + e.getY());*/
					int mx = e.getX();
					int my = e.getY();
					boolean isObject = false;
				
					// Check if we pressed on an object
					for(int x = 0, l = objList.size(); x < l; x++)
					{
						RSObject obj = objList.get(x);
					
						int ox = obj.getXPos();
						int oy = obj.getYPos();
					
						if(mx >= ox && mx < (ox+16) &&
							my >= oy && my < (oy+32))
						{
							JPopupMenu objActionMenu =
								new JPopupMenu();
								
							JMenuItem deleteItem =
								new JMenuItem("Delete");
								
							JMenuItem duplicateItem =
								new JMenuItem("Duplicate");
								
							JMenuItem editItem =
								new JMenuItem("Edit");
								
							objActionMenu.add(deleteItem);
							objActionMenu.add(duplicateItem);							
							objActionMenu.add(editItem);
							
							objActionMenu.show(e.getComponent(), e.getX(), e.getY());
							display.setSelectedObjectCoords(ox, oy);
							
							popupObject = obj;
							selectedObjectIndex = x;
							isObject = true;

							deleteItem.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent e)
								{
									saveState();
									display.setNoObjectSelected();
									objList.remove(selectedObjectIndex);
									display.repaint();
									//modified = true;
									setModified(true);
								}
							});
							
							duplicateItem.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent e)
								{
									saveState();
									objList.add(popupObject.newInstance());
									//modified = true;
									setModified(true);
								}
							});
							
							editItem.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent e)
								{
									new EditObjectDialog(mainFrame, popupObject);
								}
							});
						}
					}
					
					if(!isObject)
					{
						JPopupMenu actionMenu =
							new JPopupMenu();
					
						JMenuItem newObjectItem =
							new JMenuItem("New object");
					
						JMenuItem objectListItem =
							new JMenuItem("Object list");
					
						JMenuItem mapInfoItem =
							new JMenuItem("Map information");
					
						JMenuItem resizeMapItem =
							new JMenuItem("Resize map");
							
						
						newObjectItem.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e)
							{
								new EditObjectDialog(mainFrame, null);
							}
						});
											
						mapInfoItem.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e)
							{
								new MapInfoDialog(mainFrame);
							}
						});
						
						objectListItem.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e)
							{
								new ObjectListDialog(mainFrame);
							}
						});
						
						resizeMapItem.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e)
							{
								new ResizeMapDialog(mainFrame);
							}
						});
						
						
						actionMenu.add(newObjectItem);
						actionMenu.add(objectListItem);
						actionMenu.add(new JSeparator());
						actionMenu.add(mapInfoItem);
						actionMenu.add(resizeMapItem);
						
						popupX = e.getX();
						popupY = e.getY();
						actionMenu.show(e.getComponent(), popupX, popupY);
					}
				}
			}
			
			public void mouseReleased(MouseEvent e)
			{
				//System.out.println("Released!");
				if(e.getButton() == MouseEvent.BUTTON1)
				{
					if(doneDragging)
					{
						setModified(true);
						doneDragging = false;
					}
						
					draggedObject = null;
				}
			}
		});
		display.setPreferredSize(new Dimension(640, 480));
		display.setMaximumSize(new Dimension(1000000, 1000000));
		display.setMinimumSize(new Dimension(3, 3));
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setPreferredSize(new Dimension(640, 420));
		scrollPane.setMinimumSize(new Dimension(426, 0));
		
		scrollPane.setViewportView(display);
		
	//	splitPane.add(scrollPane);
		splitPane.setDividerLocation(0.75);
		splitPane.setResizeWeight(0.75);
		splitPane.setLeftComponent(scrollPane);
		
		blockSelectionPanel = new BlockSelectionPanel(this);
		
		tabbedPane.addTab("Blocks", blockSelectionPanel);
		
		splitPane.setRightComponent(tabbedPane);
		
		contentPane.add(splitPane, BorderLayout.CENTER);		
	}
	
	private void gui_components_at_open()
	{
		mntmSave.setEnabled(true);
		mntmSaveAs.setEnabled(true);
		mntmRevert.setEnabled(true);
		mnExtra.setEnabled(true);
	
		mntmUndo.setEnabled(false);
		mntmRedo.setEnabled(false);
	}
	
	void reset_display_area(int[][] newMapData, List<RSObject> newObjList)
	{
		mapData = newMapData;
		objList = newObjList;
	
	/*	display.setMapData(mapData);
		display.scrollRectToVisible(
			new Rectangle(0, 0, display.getWidth(), display.getHeight()));
		 display.setObjectList(newObjList);
		scrollPane.getHorizontalScrollBar().setValue(0);
		scrollPane.getVerticalScrollBar().setValue(0);
		
		display.setSelectedCoords(0, 0);
		display.setNoObjectSelected();
		display.repaint(); */
		
		display.setSelectedCoords(0, 0);
		display.setNoObjectSelected();
		display.setMapData(mapData);
		display.setObjectList(objList);
		display.scrollRectToVisible(
			new Rectangle(0, 0, display.getWidth(), display.getHeight()));
		// display.setObjectList(objList);
		//display.loadOtherData(curFile);
		scrollPane.getHorizontalScrollBar().setMaximum(map.getWidth() * 128);
		scrollPane.getHorizontalScrollBar().setValue(0);
		scrollPane.getVerticalScrollBar().setMaximum(map.getHeight() * 128);
		scrollPane.getVerticalScrollBar().setValue(0);

		blockSelectionPanel.setCurrentBlock(mapData, 0, 0);
		display.repaint();
	}

	public void saveState()
	{
		redoStack = new LinkedList<LevelState>();		
		mntmRedo.setEnabled(false);

// Limit the number of undo entries to 16
		
		if(undoStack.size() == 16)
			undoStack.removeLast();
		
		undoStack.push(new LevelState());
		
		mntmUndo.setEnabled(true);		
	}

	public void setModified(boolean status)
	{
		setAppTitle(zoneName + (status?"*":""));
		modified = status;
	}
	
}
