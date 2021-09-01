package taxani;

import rsonic.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.imageio.*;
import java.io.*;
import java.util.*;

public class TaxAni
	extends JFrame
{
	final JMenuItem mntmNew = new JMenuItem("New");
	final JMenuItem mntmOpen = new JMenuItem("Open");
	final JMenuItem mntmSave = new JMenuItem("Save");
	final JMenuItem mntmSaveAs = new JMenuItem("Save As...");
	final JMenu menuImportFrom = new JMenu("Import from...");
	final JMenu menuExportAs = new JMenu("Export As...");
	final JMenuItem mntmImportRS = new JMenuItem("Retro-Sonic 2007");
	final JMenuItem mntmImportRSDC = new JMenuItem("Retro-Sonic DC");
	final JMenuItem mntmImportNexus = new JMenuItem("Sonic Nexus");
	final JMenuItem mntmExportJson = new JMenuItem("JSON data");
	final JMenuItem mntmClose = new JMenuItem("Close");
	final JMenuItem mntmRevert = new JMenuItem("Revert");
	final JMenuItem mntmExit = new JMenuItem("Exit");
	
	final JMenuItem mntmUndo = new JMenuItem("Undo");
	final JMenuItem mntmRedo = new JMenuItem("Redo");
	final JMenuItem mntmImagePaths = new JMenuItem("Image paths...");
	
	final JMenuItem mntmAbout = new JMenuItem("About TaxAni");
	
	JButton addAnimationButton = new JButton("Add");
	JButton renameAnimationButton = new JButton("Rename");
	JButton deleteAnimationButton = new JButton("Delete");
	
	JScrollPane animationListScroll = new JScrollPane();
	JList<String> animationList = new JList<String>();
	
	HashMap<String,Map.Entry<RSAnimation, ArrayList<RSFrame>>> data =
		new HashMap<String,Map.Entry<RSAnimation, ArrayList<RSFrame>>>();
	
	String curAniName = null;
	Map.Entry<RSAnimation, ArrayList<RSFrame>> curAniEntry = null;
	
	TaxAni taxAni = this;
	
	Vector<String> sv = new Vector<String>();
	
	private LinkedList<ProgramState> undoStack = new LinkedList<ProgramState>();
	private LinkedList<ProgramState> redoStack = new LinkedList<ProgramState>();
	
	private boolean inSetEntry = false;
	
	private boolean modified = false;
	//private boolean oldModified = false;
	
	private String currentDirectory = null;
	protected String imagePaths[];
	
	protected String imageRoot;
	private boolean firstTimeImageLoad = true;
	
	private boolean noRecordOp = false;
	
	private class ProgramState
	{
		boolean progModified;
		Vector<String> progSv;
		HashMap<String,Map.Entry<RSAnimation, ArrayList<RSFrame>>> progData;
		String aniName;
		
		public ProgramState()
		{
			progSv = new Vector<String>();
			progData = new HashMap<String,Map.Entry<RSAnimation, ArrayList<RSFrame>>>();
			progModified = modified;

			for(String s : sv)
				progSv.add(s);
			
			for(Map.Entry<String,Map.Entry<RSAnimation, ArrayList<RSFrame>>>
					e : data.entrySet())
			{
				String key = e.getKey();
				
				RSAnimation value_key = e.getValue().getKey().newInstance();
				ArrayList<RSFrame> value_value = new ArrayList<RSFrame>();
				
				for(RSFrame f : e.getValue().getValue())
					value_value.add(f.newInstance());
				
				
				Map.Entry<RSAnimation, ArrayList<RSFrame>> value =
					new AbstractMap.SimpleEntry<RSAnimation, ArrayList<RSFrame>>
						(value_key, value_value);
				
				progData.put(key, value);
			}
			
			aniName = curAniName;
		}
		
		public void restore()
		{
			sv.clear();
			data.clear();
			
			for(String s : progSv)
				sv.add(s);
			
			for(Map.Entry<String,Map.Entry<RSAnimation, ArrayList<RSFrame>>>
					e : progData.entrySet())
			{
				String key = e.getKey();
				
				RSAnimation value_key = e.getValue().getKey().newInstance();
				ArrayList<RSFrame> value_value = new ArrayList<RSFrame>();
				
				for(RSFrame f : e.getValue().getValue())
					value_value.add(f.newInstance());
				
				
				Map.Entry<RSAnimation, ArrayList<RSFrame>> value =
					new AbstractMap.SimpleEntry<RSAnimation, ArrayList<RSFrame>>
						(value_key, value_value);
				
				data.put(key, value);
			}
			
			updateList(false);
			
			selectAnimation(aniName);
			
			modified = progModified;
			setAppTitle(getCurrentName() + (modified?"*":""));
		}
	}
	
	private RSAnimationFile generateAnimationFile()
	{
		RSAnimationFile saved =
			new RSAnimationFile();
			
		saved.setImagePaths( imagePaths );
			
		ArrayList<RSAnimation> animationList =
			new ArrayList<RSAnimation>();
				
		for(String s : sv)
		{
			Map.Entry<RSAnimation, ArrayList<RSFrame>> e = data.get(s);
			
			RSAnimation animation = e.getKey().newInstance();
			animation.setName(s);
			
			animation.setFrames(e.getValue().toArray(new RSFrame[0]));
			
			animationList.add(animation);
		}
			
		saved.setAnimations(animationList.toArray(new RSAnimation[0]));
		
		return saved;
	}
		
	private boolean file_save(boolean saveAs)
	{
		// Current state = not modified, all other states = modified
			
		File selFile = null;
		
		if(curFile == null || saveAs)
		{
			JFileChooser chooser = new JFileChooser(currentDirectory);
			
			int r = chooser.showSaveDialog(null);
			
			if(r == JFileChooser.APPROVE_OPTION)
				selFile = chooser.getSelectedFile();
			else
				return false;
		}
		else
			selFile = curFile;
			
		try
		{
			(generateAnimationFile()).write(selFile);
			
			curFile = selFile;
		}
					
		catch(Exception exc)
		{
			error_message("Could not save animation file!");
			exc.printStackTrace();
			return false;
		}
		
		currentDirectory = selFile.getParent();
		setAppTitle( getCurrentName() );
		
		for(ProgramState state : undoStack)
			state.progModified = true;
			
		for(ProgramState state : redoStack)
			state.progModified = true;
		
		modified = false;
		
		return true;
	}
	
	private boolean file_save_check()
	{
		if(modified)
		{
			int option =
				JOptionPane.showConfirmDialog(null, "The animation file has been modified.\nDo you want to save it?");
				
			switch(option)
			{
				case JOptionPane.CANCEL_OPTION: return false;
				case JOptionPane.YES_OPTION: return file_save(false);
				case JOptionPane.NO_OPTION: break;
			}
		}
		
		return true;
	}
	
	private String getCurrentName()
	{
		return (curFile==null)?"New":curFile.getName();
	}
	
	protected boolean loadImages()
	{
		images = new BufferedImage[ imagePaths.length ];
		
		for(int x = 0, l = imagePaths.length; x < l; x++)
		{
			String imagePath = imageRoot + "/" + imagePaths[x];
							
			//System.out.println("Loading image: " + imagePath);
			
			try
			{
				images[x] = ImageIO.read(new File(imagePath));
			}
			
			catch(Exception e)
			{
				error_message("Image " + (x+1) + " could not be loaded");
				ImagePathsDialog d =	new ImagePathsDialog(this);
				
				
				if(d.returnStatus() == false)
					return false;
				else
				{
					x = -1;
					continue;
				}
			}
		}
		
		return true;
	}
		
	boolean dataKept = false;
	
	public void pushState()
	{
		if(undoStack.size() == 16)
			undoStack.removeFirst();
		
		undoStack.push( new ProgramState() );
		redoStack.clear();
		
		mntmRedo.setEnabled(false);
		mntmUndo.setEnabled(true);
	}
		
	public void keepOldData()
	{
		//System.out.println("keepOldData()");
		
		dataKept = true;
		pushState();
	}
	
	public void opChangeData()
	{
		if(!dataKept)
			throw new RuntimeException("keepOldData() had to be called before executing this!");
				
		//System.out.println("opChangeData()");
		modified = true;
		setAppTitle(getCurrentName() + "*");
		
		dataKept = false;
	}
	
	public void opDeleteAnimation(int index)
	{
		pushState();
		//System.out.println("opDeleteAnimation()");
		
	/*	data.remove(sv.get(index));
		sv.remove(index);*/
		
		data.remove(sv.get(index));
		sv.remove(index);
						
		int sv_size = sv.size();
						
//						System.out.println("sv.size() = " + sv.size());
					
		int selIndex = -1;
		
		if(sv_size == 0)
		{
			animationPanel.internalPanel.setVisible(false);
		}
		else if(index == 0)
		{
			selectAnimation(sv.get(0));
			selIndex = 0;
		}
		else
		{
			selectAnimation(sv.get(index - 1));
			selIndex = index - 1;
		}
						
	//	inSetEntry = true;
		updateList(false);
	//	animationList.setListData(sv.toArray(new String[0]));
			
		selectAnimation( (selIndex == -1) ? null : sv.get(selIndex) );
		
		/*if(selIndex != -1)
		{
			animationList.setSelectedIndex(selIndex);
		}*/
		
	//	inSetEntry = false;
							
		enableListButtons();
		
		modified = true;
		setAppTitle(getCurrentName() + "*");
	}
	
	public void opCreateAnimation(String name)//, int index)
	{
		pushState();
		//System.out.println("opCreateAnimation()");
		
		int newAniIndex = animationList.getSelectedIndex();
					
		if(newAniIndex == -1)
			newAniIndex = sv.size();
		else
			newAniIndex++;
		
		RSAnimation value_key = new RSAnimation();
		ArrayList<RSFrame> value_value = new ArrayList<RSFrame>();
				
		Map.Entry<RSAnimation, ArrayList<RSFrame>> value =
				new AbstractMap.SimpleEntry<RSAnimation, ArrayList<RSFrame>>
						(value_key, value_value);
				
		data.put(name, value);
		sv.insertElementAt(name, newAniIndex);
		
		updateList(false);
		selectAnimation(name);
		modified = true;
		setAppTitle(getCurrentName() + "*");
		enableListButtons();
	}
	
	public void opRenameAnimation(String oldName, String newName)
	{
		pushState();
		//System.out.println("opRenameAnimation()");

		
		int index = sv.indexOf(oldName);
		
		Map.Entry<RSAnimation, ArrayList<RSFrame>> value = data.remove(oldName);
		data.put(newName, value);
		
		sv.set(index, newName);
		
		updateList(false);
		selectAnimation(newName);
		modified = true;
		setAppTitle(getCurrentName() + "*");
		enableListButtons();
	}
	
	public void opSelectAnimation(String name)
	{
		//System.out.println("opSelectAnimation()");
		
		selectAnimation(name);
		setAppTitle(getCurrentName() + (modified?"*":""));
	}
	
	void enableListButtons()
	{
		addAnimationButton.setEnabled(true);
		renameAnimationButton.setEnabled(data.size() > 0);
		deleteAnimationButton.setEnabled(data.size() > 0);
	}
	
	final JFrame mainFrame = this;
		
	File curFile = null;
	
	final static String appTitle = "TaxAni";
	final static String appTitleLong = "TaxAni - The Retro-Sonic Animation Editor";
	
	protected RSAnimationFile animationFile = null;
	
	AnimationPanel animationPanel = new AnimationPanel(this);
	
	public BufferedImage[] images;
	
	void error_message(String s)
	{
		JOptionPane.showMessageDialog(this,
				s,
				appTitle + " - Error",
				JOptionPane.ERROR_MESSAGE);
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
	
	private void selectAnimation(String name)
	{
		if(name == null)
			animationPanel.setVisible(false);
		else
		{
			Map.Entry<RSAnimation, ArrayList<RSFrame>> entry =
				data.get(name);
			
			if(entry == null)
				throw new RuntimeException("Tried to select animation with name " + name + " that does not exist.");
		
			curAniEntry = entry;
			curAniName = name;

			animationPanel.setEntry(entry);		
			animationPanel.setVisible(true);
		}
	}
	
	private void introduceData()
	{
		sv.clear();
		
		HashMap<String,Map.Entry<RSAnimation, ArrayList<RSFrame>>> newData =
			new HashMap<String,Map.Entry<RSAnimation, ArrayList<RSFrame>>>();
		
		for(RSAnimation anim : animationFile.getAnimations() )
		{
			ArrayList<RSFrame> frameList =
				new ArrayList<RSFrame>();
			
			for(RSFrame frame : anim.getFrames())
				frameList.add(frame);
			
			newData.put(anim.getName(), new AbstractMap.SimpleEntry<RSAnimation, ArrayList<RSFrame>>(anim, frameList));
			
			sv.add(anim.getName());
		}
		
		data = newData;
	}
	
	private void updateList(boolean setUp)
	{
		String firstName = null; 
		
		/*for(String animName : data.keySet() )
		{
			if(firstName == null)
				firstName = animName;
			
			sv.add(animName);
		}*/
		if(sv.size() > 0)
			firstName = sv.get(0);
			
		animationList = new JList<String>(sv.toArray(new String[0]));
		
		animationList.setVisibleRowCount(16);
		animationList.setFixedCellWidth(160);
		
		animationList.addListSelectionListener(
			new ListSelectionListener()
			{
				public void valueChanged(ListSelectionEvent e) 
				{
					if (e.getValueIsAdjusting() == false) 
					{
						if (animationList.getSelectedIndex() != -1) 
						{
							if(!inSetEntry)
							{
								opSelectAnimation(animationList.getSelectedValue());
							}
						}
					}
				}
			}	
		);
				
		
		animationListScroll.getViewport().setView(animationList);
			
		if(setUp)
		{
			if(firstName != null)
			{
				selectAnimation(firstName);
				animationList.setSelectedIndex(0);
			}
		}
	}
	
	private void loadFile(File selFile)
		throws Exception
	{
		animationFile = new RSAnimationFile(selFile);
		imageRoot = selFile.getParent() + "/../Sprites";
		
		loadFile2(selFile);
	}
	
	private void loadFile2(File selFile)
		throws Exception
	{						
		curAniName = null;
		curAniEntry = null;
		curFile = selFile;
		modified = false;
		setAppTitle(getCurrentName());
						
		int imageCount = animationFile.getImageCount();
		imagePaths = animationFile.getImagePaths();
						
						
		if(!loadImages())
		{
			closeFile();
			return;
		}

		mntmClose.setEnabled(true);
		mntmSave.setEnabled(true);
		mntmSaveAs.setEnabled(true);
		menuExportAs.setEnabled(true);
		mntmImagePaths.setEnabled(true);
		mntmUndo.setEnabled(false);
		mntmRedo.setEnabled(false);
		mntmRevert.setEnabled(true);
						
		introduceData();
		updateList(true);
		enableListButtons();
						
		currentDirectory = selFile.getParent();
						
		undoStack.clear();
		redoStack.clear();
	}
	
	private void closeFile()
	{
		animationFile = null;
		curFile = null;
		setAppTitleLong(null);
		
		mntmSave.setEnabled(false);
		mntmSaveAs.setEnabled(false);
		menuExportAs.setEnabled(false);
		mntmImagePaths.setEnabled(false);
		mntmUndo.setEnabled(false);
		mntmRedo.setEnabled(false);
		mntmClose.setEnabled(false);
		mntmRevert.setEnabled(false);
		
		addAnimationButton.setEnabled(false);
		deleteAnimationButton.setEnabled(false);
		renameAnimationButton.setEnabled(false);
		
		animationPanel.internalPanel.setVisible(false);
		
		animationList.setListData(new String[0]);
		
		undoStack.clear();
		redoStack.clear();
		
		modified = false;
	}
						
	
	public TaxAni()
	{
		JPanel panel = (JPanel)getContentPane();
		JMenuBar menuBar = new JMenuBar();
		
		ImageIcon icon =
			new ImageIcon(this.getClass().getResource("/icons/taxani.gif"));
				
		setIconImage(icon.getImage());
		
		JMenu mnFile = new JMenu("File");
		JMenu mnEdit = new JMenu("Edit");
		JMenu mnView = new JMenu("View");
		JMenu mnHelp = new JMenu("Help");
		
		panel.setLayout(new BorderLayout());
		
		mntmNew.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
			{
				if(!file_save_check())
					return;
				
				curFile = null;
				curAniName = null;
				curAniEntry = null;

				mntmClose.setEnabled(true);
				mntmSave.setEnabled(true);
				mntmSaveAs.setEnabled(true);
				menuExportAs.setEnabled(true);
				mntmImagePaths.setEnabled(true);
				mntmUndo.setEnabled(false);
				mntmRedo.setEnabled(false);
				mntmRevert.setEnabled(false);
				
				setAppTitle("New");
				
				animationPanel.internalPanel.setVisible(false);
				imagePaths = new String[0];
				
				animationFile = new RSAnimationFile();
				
				introduceData();
				updateList(true);
				enableListButtons();
				undoStack.clear();
				redoStack.clear();
				
				curAniEntry = null;
				modified = false;
			}
		});
		
		mntmOpen.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
			{
				if(!file_save_check())
					return;
				
				JFileChooser fileChooser = new JFileChooser(currentDirectory);
				

				
				if(fileChooser.showOpenDialog(mainFrame) == JFileChooser.APPROVE_OPTION)
				{
					try
					{
						loadFile(fileChooser.getSelectedFile());
					}
					
					catch(Exception exc)
					{
						exc.printStackTrace();
						error_message(exc.getMessage());
					}
				}
			}
		});
		
		mntmClose.addActionListener(
			new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					if(!file_save_check())
						return;
				
					closeFile();
				}
			}
		);
			
		mntmSave.addActionListener(
			new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					file_save(false);
				}
			}
		);
			
		mntmSaveAs.addActionListener(
			new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					file_save(true);
				}
			}
		);
			
		mntmRevert.addActionListener(
			new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					if(!file_save_check())
						return;

					try
					{
						loadFile(curFile);
					}
					
					catch(Exception exc)
					{
						exc.printStackTrace();
						error_message(exc.getMessage());
					}
				}
			}
		);
		
		mntmExit.addActionListener(
			new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					if(file_save_check())
						System.exit(0);
				}
			}
		);
			
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
		
		mnFile.add(mntmNew);
		mnFile.add(mntmOpen);
		
		mntmClose.setEnabled(false);
		mntmRevert.setEnabled(false);
		mntmSave.setEnabled(false);
		mntmSaveAs.setEnabled(false);
		menuExportAs.setEnabled(false);
		
		mnFile.add(mntmSave);
		mnFile.add(mntmSaveAs);
		mnFile.add(menuImportFrom);
		menuImportFrom.add(mntmImportRS);
		menuImportFrom.add(mntmImportRSDC);
		menuImportFrom.add(mntmImportNexus);
		mnFile.add(menuExportAs);
		menuExportAs.add(mntmExportJson);
		mnFile.add(mntmClose);
		mnFile.add(mntmRevert);
		mnFile.add(new JSeparator());
		mnFile.add(mntmExit);
		
		mntmNew.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,
				ActionEvent.CTRL_MASK));
		
		mntmOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
				ActionEvent.CTRL_MASK));
		
		mntmSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
				ActionEvent.CTRL_MASK));
		
		mntmSaveAs.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
				ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK));
		
		mntmClose.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W,
				ActionEvent.CTRL_MASK));
		
		mntmRevert.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R,
				ActionEvent.CTRL_MASK));
		
		mntmExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q,
				ActionEvent.CTRL_MASK));
		
		menuBar.add(mnFile);

		mntmUndo.setEnabled(false);
		mntmRedo.setEnabled(false);
		mntmImagePaths.setEnabled(false);

		mntmUndo.addActionListener(
			new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					ProgramState state = undoStack.pop();
					redoStack.push(new ProgramState());
					
					state.restore();
					
					mntmUndo.setEnabled(undoStack.size() > 0);
					mntmRedo.setEnabled(redoStack.size() > 0);
				}
			}
		);
			
		mntmRedo.addActionListener(
			new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					ProgramState state = redoStack.pop();
					undoStack.push(new ProgramState());
					
					state.restore();
					
					mntmUndo.setEnabled(undoStack.size() > 0);
					mntmRedo.setEnabled(redoStack.size() > 0);
				}
			}
		);
			
		mntmImagePaths.addActionListener(
			new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					// image paths setup
					new ImagePathsDialog(taxAni);
					
					loadImages();
					
					if(curAniEntry != null)
						animationPanel.setEntry(curAniEntry);
				}
			}
		);
		
		mntmImportRS.addActionListener(
			new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					if(!file_save_check())
						return;
					
					JFileChooser chooser = new JFileChooser(currentDirectory);
					
					int r = chooser.showOpenDialog(null);
					
					if(r == JFileChooser.APPROVE_OPTION)
					{
						try
						{
							File selFile = chooser.getSelectedFile();
							animationFile = RSAnimationFile.importRSFormat(selFile);
							imageRoot = selFile.getParent();
							loadFile2(selFile);
							curFile = null;
						}
						
						catch(Exception exc)
						{
							exc.printStackTrace();
							error_message(exc.getMessage());
						}
					}
					else if(r == JFileChooser.ERROR_OPTION)
						error_message("Error while importing Retro-Sonic 2007 animation file.");
				}
			}
		);
			
		mntmImportRSDC.addActionListener(
			new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					if(!file_save_check())
						return;
					
					JFileChooser chooser = new JFileChooser(currentDirectory);
					
					int r = chooser.showOpenDialog(null);
					
					if(r == JFileChooser.APPROVE_OPTION)
					{
						try
						{
							File selFile = chooser.getSelectedFile();
							animationFile = RSAnimationFile.importRSDCFormat(selFile);
							imageRoot = selFile.getParent();
							loadFile2(selFile);
							curFile = null;
						}
						
						catch(Exception exc)
						{
							exc.printStackTrace();
							error_message(exc.getMessage());
						}
					}
					else if(r == JFileChooser.ERROR_OPTION)
						error_message("Error while importing Retro-Sonic DC animation file.");
				}
			}
		);
			
		mntmImportNexus.addActionListener(
			new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					if(!file_save_check())
						return;
					
					JFileChooser chooser = new JFileChooser(currentDirectory);
					
					int r = chooser.showOpenDialog(null);
					
					if(r == JFileChooser.APPROVE_OPTION)
					{
						try
						{
							File selFile = chooser.getSelectedFile();
							animationFile = RSAnimationFile.importNexusFormat(selFile);
							imageRoot = selFile.getParent() + "/../Sprites";
							loadFile2(selFile);
							curFile = null;
						}
						
						catch(Exception exc)
						{
							exc.printStackTrace();
							error_message(exc.getMessage());
						}
					}
					else if(r == JFileChooser.ERROR_OPTION)
						error_message("Error while importing Sonic Nexus animation file.");
				}
			}
		);
			
		mntmExportJson.addActionListener(
			new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					// export to JSON
					JFileChooser chooser = new JFileChooser(currentDirectory);

					int r = chooser.showSaveDialog(null);
					
					if(r == JFileChooser.APPROVE_OPTION)
					{
						try
						{
							RSAnimationFile genAniFile = generateAnimationFile();
							FileWriter outWriter = new FileWriter( chooser.getSelectedFile() );
							
							outWriter.write(AniJSON.stringify(genAniFile));
							
							outWriter.flush();
							outWriter.close();
						}
						
						catch(Exception exc)
						{
							error_message("Error when exporting file.");
						}
					}
					else if(r == JFileChooser.ERROR_OPTION)
						error_message("Error when choosing file.");
				}
			}
		);
			

		mnEdit.add(mntmUndo);
		mnEdit.add(mntmRedo);
		mnEdit.add(mntmImagePaths);
		
		mntmUndo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z,
				ActionEvent.CTRL_MASK));
		mntmRedo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z,
				ActionEvent.SHIFT_MASK | ActionEvent.CTRL_MASK));
		mntmImagePaths.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I,
				ActionEvent.CTRL_MASK));
			
		menuBar.add(mnEdit);
		
		//menuBar.add(mnView);
		
		mnHelp.add(mntmAbout);
		menuBar.add(mnHelp);
		
		mntmAbout.addActionListener(
			new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					new AboutDialog(taxAni);
				}
			}
		);

		panel.add(menuBar, BorderLayout.NORTH);
		
		animationList.setVisibleRowCount(16);
		animationList.setFixedCellWidth(160);
		animationListScroll.getViewport().setView(animationList);
		animationListScroll.setVerticalScrollBarPolicy(
			ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

		JPanel listPanel = new JPanel();
		
		listPanel.setLayout(new GridBagLayout());
		
		GridBagConstraints c2 = new GridBagConstraints();
		
		c2.insets = new Insets(0, 2, 5, 2);
		
		c2.fill = GridBagConstraints.BOTH;
		c2.gridx = 0;
		c2.gridy = 0;
		c2.gridwidth = GridBagConstraints.REMAINDER;
		c2.gridheight = 1;
		c2.weightx = 1.0;
		c2.weighty = 1.0;
		
		listPanel.add(animationListScroll, c2);
		
		c2.gridx = 0;
		c2.gridy = 1;
		c2.gridwidth = 1;
		c2.gridheight = 1;
		c2.weightx = 0.5;
		c2.weighty = 0;
		
		addAnimationButton.addActionListener
		(
			new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					String s = JOptionPane.showInputDialog(taxAni, "Type name of new animation", "Create Animation", JOptionPane.QUESTION_MESSAGE);
					
					if(s != null)
					{
						if(data.containsKey(s))
							error_message("An animation named " + s + " already exists.");
						else
							opCreateAnimation(s);
					}
				}
			}
		);
		
		addAnimationButton.setEnabled(false);
		listPanel.add(addAnimationButton, c2);
		
		c2.gridx = 1;
		c2.gridy = 1;
			
			
		renameAnimationButton.addActionListener
		(
			new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					int aniIndex = animationList.getSelectedIndex();
					
					if(aniIndex == -1)
						error_message("Select an animation first!");
					else
					{
						String oldName = animationList.getSelectedValue();
						
						String s = (String)JOptionPane.showInputDialog(taxAni, "Type new name of animation", "Rename Animation", 
							JOptionPane.QUESTION_MESSAGE, null, null, oldName);
						
						if(s != null)
						{
							if(data.containsKey(s))
								error_message("An animation named " + s + " already exists.");
							else
								opRenameAnimation(oldName, s);
						}
					}
				}
			}
		);
		
		renameAnimationButton.setEnabled(false);
		listPanel.add(renameAnimationButton, c2);
			
		c2.gridx = 0;
		c2.gridy = 2;
		c2.gridwidth = GridBagConstraints.REMAINDER;
		c2.weightx = 0;

		deleteAnimationButton.addActionListener
		(
			new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					int index = animationList.getSelectedIndex();
		
					if (index != -1) 
						opDeleteAnimation(index);
				}
			}
		);
		
		deleteAnimationButton.setEnabled(false);
		listPanel.add(deleteAnimationButton, c2);
		
		panel.add(listPanel, BorderLayout.WEST);
		
		panel.add( animationPanel, BorderLayout.CENTER );

		setSize(640, 480);
		setAppTitleLong(null);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setLocationRelativeTo(null);
	}
}
