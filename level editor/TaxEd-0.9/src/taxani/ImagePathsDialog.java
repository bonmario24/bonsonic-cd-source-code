package taxani;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;
import java.io.*;
import rsonic.*;

public class ImagePathsDialog
	extends JDialog
{
	private TaxAni taxAni;
	private JTextField rootDirField;
	private ArrayList<String> imagePaths;
	JScrollPane pathScroll = new JScrollPane( );
	private JDialog imagePathsDialog = this;
	JTextField pathFields[ ];
	private boolean status = false;

	private int getMaximumNeededImage()
	{
		int max = -1;
		
		for(Map.Entry<String,Map.Entry<RSAnimation, ArrayList<RSFrame>>> e :
			taxAni.data.entrySet())
		{
			for(RSFrame f : e.getValue().getValue())
			{
				int im = f.getImageNum();
					
				if(im > max)
					max = im;
			}
		}
		
		return max;
	}
	
	private boolean allImagePathsExist()
	{
		takeFieldPaths();
		
		for(String p : imagePaths)
		{
			String complete = rootDirField.getText() + "/" + p;
			
			if(!((new File(complete)).exists()))
			{
				JOptionPane.showMessageDialog(imagePathsDialog,
					complete + " not found!\nFix the image paths or cancel changes.",
					"Error", JOptionPane.ERROR_MESSAGE);
				
				return false;
			}
		}
		
		return true;
	}
	
	private void takeFieldPaths()
	{
		imagePaths.clear();
		
		for(JTextField f : pathFields)
			imagePaths.add( f.getText() );
	}
	
/*	private class SetImagePathListener
		implements ActionListener
	{
		private int n;
		
		public SetImagePathListener(int n)
		{
			this.n = n;
		}
		
		public void actionPerformed(ActionEvent e)
		{
			JFileChooser chooser = new JFileChooser();
					
			int r = chooser.showOpenDialog(null);
					
			if(r == JFileChooser.APPROVE_OPTION)
				pathFields[n].setText(chooser.getSelectedFile().getPath());
		}
	}
*/

	private class DeleteImagePathListener
		implements ActionListener
	{
		private int n;
		
		public DeleteImagePathListener(int n)
		{
			this.n = n;
		}
		
		public void actionPerformed(ActionEvent e)
		{
			takeFieldPaths();
			imagePaths.remove(n);
			pathScroll.setViewportView( makePathPanel( ) );
		}
	}
			
	private JPanel makePathPanel( )
	{	
		JPanel pathPanel = new JPanel();

		pathPanel.setLayout(new GridBagLayout());
		
		GridBagConstraints c2 = new GridBagConstraints();
			
		c2.fill = GridBagConstraints.BOTH;
		c2.insets = new Insets(0, 2, 5, 2);
		c2.gridx = 0;
		c2.gridy = 0;
		c2.gridwidth = 1;
		c2.gridheight = 1;
		c2.weightx = 0;
		c2.weighty = 0;
		c2.anchor = GridBagConstraints.WEST;
			
		pathFields = new JTextField[imagePaths.size()];
		
		for(int i = 0, s = imagePaths.size(); i < s; i++)
		{
			c2.gridx = 0;
			c2.gridy = i;
			
			JLabel pathLabel = new JLabel("Image " + (i+1));
			
			pathPanel.add(pathLabel, c2);
			
			c2.gridx++;
			
			pathFields[i] = new JTextField(imagePaths.get(i), 30);
			
			pathPanel.add(pathFields[i], c2);
			
		/*	c2.gridx++;
			
			JButton pathButton = new JButton("...");
			
			pathButton.addActionListener(new SetImagePathListener(i));
			pathPanel.add(pathButton, c2);
		*/	
			c2.gridx++;
			
			JButton deleteButton = new JButton("Del");
			
			deleteButton.addActionListener(new DeleteImagePathListener(i));
			pathPanel.add(deleteButton, c2);
		}
		
		c2.gridx = 0;
		c2.gridy++;
		
		JButton addNewPath = new JButton("Add new path");
		
		addNewPath.addActionListener(
			new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					takeFieldPaths();
					imagePaths.add("");
					pathScroll.setViewportView( makePathPanel( ) );
				}
			}
		);
			
		pathPanel.add(addNewPath, c2);
			
		return pathPanel;
	}
	
	public boolean returnStatus()
	{
		return status;
	}
	
	public ImagePathsDialog(final TaxAni taxAni)
	{
		this.taxAni = taxAni;
		JPanel rootDirPanel = new JPanel();
		JPanel panel = (JPanel)getContentPane();
		
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setModalityType(ModalityType.APPLICATION_MODAL);
		setTitle("Image paths");
		
		panel.setLayout(new BorderLayout());//GridBagLayout());
		rootDirPanel.setLayout(new GridBagLayout());
		
		GridBagConstraints c = new GridBagConstraints();
		
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(0, 2, 5, 2);
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.weightx = 0;
		c.weighty = 0;
		c.anchor = GridBagConstraints.WEST;
		
		JLabel rootDirLabel = new JLabel("Root directory");
		
		rootDirPanel.add(rootDirLabel, c);
		
		c.gridx = 1;
		c.gridy = 0;
		c.weightx = 0;
		
		rootDirField = new JTextField(taxAni.imageRoot, 40);
		
		rootDirPanel.add(rootDirField, c);
		
		c.fill = 0;
		c.gridx = 2;
		c.gridy = 0;
		c.weightx = 0;
		
		JButton rootDirButton = new JButton("...");
		
		rootDirPanel.add(rootDirButton, c);
		
		rootDirButton.addActionListener(
			new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					JFileChooser chooser = new JFileChooser(rootDirField.getText());
					chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					
					int r = chooser.showOpenDialog(null);
					
					if(r == JFileChooser.APPROVE_OPTION)
						rootDirField.setText(chooser.getSelectedFile().getPath());
				}
			}
		);
	
	//	String[ ] imagePaths = taxAni.animationFile.getImagePaths();
		imagePaths = new ArrayList<String>();
			
		for(String s : taxAni.imagePaths)
			imagePaths.add(s);

		pathScroll.setViewportView(makePathPanel());
		
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;
		c.gridheight = 4;
		c.weightx = 0;//1.0;
		
		pathScroll.setSize(640, 200);
		
		JPanel okCancelPanel = new JPanel();
		
		okCancelPanel.setLayout(new FlowLayout());
		
		JButton okButton = new JButton("OK");
		JButton cancelButton = new JButton("Cancel");
		
		okButton.addActionListener(
			new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					// OK
					int lastImage = imagePaths.size() - 1;
					int maxNeededImage = getMaximumNeededImage();
					
					if( lastImage < maxNeededImage )
					{
						JOptionPane.showMessageDialog(imagePathsDialog,
						"Animation frames require at least " + (maxNeededImage+1) + 
						" image" + ((maxNeededImage>0)?"s":"") + 
						", but " + ((lastImage < 0) ? "none" : ("only " +(lastImage + 1))) + 
						((lastImage>0)?" were":" was") + " specified.\n"
						+ "Fix the image paths or cancel changes.",
						"Error", JOptionPane.ERROR_MESSAGE);
						
						return;
					}
					
					if( !allImagePathsExist() )
						return;
					
					takeFieldPaths();
					taxAni.imagePaths = imagePaths.toArray(new String[0]);
					taxAni.imageRoot = rootDirField.getText();
					
					status = true;
					
					dispose();
				}
			}
		);
			
		cancelButton.addActionListener(
			new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					status = false;
					
					dispose();
				}
			}
		);
			
		okCancelPanel.add(okButton);
		okCancelPanel.add(cancelButton);
		
		panel.add(rootDirPanel, BorderLayout.NORTH);
		panel.add(pathScroll, BorderLayout.CENTER);
		panel.add(okCancelPanel, BorderLayout.SOUTH);
		
		setSize(640, 480);
		setLocationRelativeTo(taxAni);
		setVisible(true);
	}
}
