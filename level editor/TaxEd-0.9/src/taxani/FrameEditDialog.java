package taxani;

import rsonic.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.io.*;
import java.util.*;

class FrameEditDialog
	extends JDialog
{
	RSFrame frame;
	Map.Entry<RSAnimation, ArrayList<RSFrame>> entry;
	
	JScrollPane imagePanelScroll = new JScrollPane();
	ImagePanel imagePanel = new ImagePanel();
	TaxAni taxAni;
	private boolean doingDragging = false;
	int selX;
	int selY;
	int selX2;
	int selY2;
	int selHsX;
	int selHsY;
	
	int selImgW;
	int selImgH;
	
	JLabel imageNumLabel = new JLabel("Image number:");
	JSpinner imageNumSpinner;
	JCheckBox flag1CheckBox = new JCheckBox("Flag 1 set");
	JCheckBox flag2CheckBox = new JCheckBox("Flag 2 set");
	JButton adjustHotspotButton = new JButton("Adjust hotspot");
	JButton applyButton = new JButton("Apply");
	JButton discardButton = new JButton("Discard");
	FrameEditDialog frameEdit = this;
	
	class ImagePanel
		extends JPanel
	{
		int selectedImage = -1;
		int zoom = 1;
		Image image;
		
		public void paintComponent(Graphics g)
		{
			g.setColor(getBackground());
			g.fillRect(0, 0, getWidth(), getHeight());
			
			if(selectedImage != -1)
				g.drawImage(image, 0, 0, this);
			
			g.setColor(Color.green);
			
			/*int sX= selX*zoom;
			int sY= selY*zoom;
			int sW= selX2*zoom;
			int s= selY2*zoom;*/
			
			int rX = (selX2<selX)?selX2:selX;
			int rY = (selY2<selY)?selY2:selY;
			int rX2 = (selX2<selX)?selX:selX2;
			int rY2 = (selY2<selY)?selY:selY2;
			
			for(int d = 0; d < zoom; d++)
				g.drawRect((rX * zoom) +d, (rY * zoom) +d , ((rX2 - rX) * zoom) - (d*2), ((rY2 - rY) * zoom) - (d*2));
			
			
		}
		
		public void setImage(int imageNum)
		{
			selectedImage = imageNum;
			
			selImgW = taxAni.images[selectedImage].getWidth();
			selImgH = taxAni.images[selectedImage].getHeight();
			
			int newWidth = taxAni.images[selectedImage].getWidth() * zoom;
			int newHeight = taxAni.images[selectedImage].getHeight() * zoom;
			
			image = 
				taxAni.images[selectedImage].getScaledInstance(newWidth, newHeight, Image.SCALE_DEFAULT);
			
			setSize(newWidth, newHeight);
			setPreferredSize(new Dimension(newWidth, newHeight));			
		}
		
		public int getZoom()
		{
			return zoom;
		}
		
		public void setZoom(int scale)
		{
			zoom = scale;
			
			if(selectedImage != -1)
				setImage(selectedImage);
		}
		
		ImagePanel()
		{
			setSize(256, 256);
			setPreferredSize(new Dimension(256, 256));
		}
	}
	
	FrameEditDialog(final TaxAni taxAni,
				  final Map.Entry<RSAnimation, ArrayList<RSFrame>> entry,
				 final  RSFrame frame,
				 final boolean isNew)
	{
		this.taxAni = taxAni;
		this.entry  = entry;
		this.frame = frame;
		
		if(isNew)
		{
			applyButton.setText("Add");
			discardButton.setText("Cancel");
		}
		
		selX = frame.getXPos();
		selY = frame.getYPos();
		selX2 = selX + frame.getWidth();
		selY2 = selY + frame.getHeight();
		selHsX = frame.getHsX();
		selHsY = frame.getHsY();
		
		imageNumSpinner = new JSpinner( new SpinnerNumberModel( frame.getImageNum() + 1, 1, taxAni.images.length, 1 ) );
		
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		
		setModalityType(ModalityType.APPLICATION_MODAL);
		
		setLayout(new GridBagLayout());
		
		GridBagConstraints c = new GridBagConstraints();
		
		c.insets = new Insets(0, 2, 5, 2);
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 3;
		c.gridheight = 1;
		c.weightx = 1.0;
		c.weighty = 1.0;
		
		imagePanel.setImage( frame.getImageNum() );
		imagePanel.setZoom(1);
		
		imagePanel.addMouseListener(

			new MouseAdapter()
			{
				public void mousePressed(MouseEvent e) 
				{
					if(e.getButton() == MouseEvent.BUTTON1)
					{
						int gotX = e.getX() / imagePanel.getZoom();
						int gotY = e.getY() / imagePanel.getZoom();
						
						if(gotX >= 256 || gotY >= 256)
							return;
						
						selX = gotX;
						selY = gotY;
						selX2 = selX;
						selY2 = selY;

						doingDragging = true;
						
						imagePanel.repaint();
					}
					else if(e.getButton() == MouseEvent.BUTTON3)
					{
						JPopupMenu menu = new JPopupMenu();
						
						JMenuItem one = new JMenuItem("1x");
						
						one.addActionListener(new ActionListener()
						{
							public void actionPerformed(ActionEvent e){ imagePanel.setZoom(1); }
						});
						
						JMenuItem two = new JMenuItem("2x");
						
						two.addActionListener(new ActionListener()
						{
							public void actionPerformed(ActionEvent e){ imagePanel.setZoom(2); }
						});
						
						JMenuItem three = new JMenuItem("3x");
						
						three.addActionListener(new ActionListener()
						{
							public void actionPerformed(ActionEvent e){ imagePanel.setZoom(3); }
						});
						
						JMenuItem four = new JMenuItem("4x");
						
						four.addActionListener(new ActionListener()
						{
							public void actionPerformed(ActionEvent e){ imagePanel.setZoom(4); }
						});
						
						menu.add(one);
						menu.add(two);
						menu.add(three);
						menu.add(four);
						
					//	menu.setLabel("Zoom");
						
						menu.show(e.getComponent(), e.getX(), e.getY());
					}
				}
				
				public void mouseReleased(MouseEvent e)
				{
					doingDragging = false;
				}
			}
		);
			
		imagePanel.addMouseMotionListener(
			new MouseMotionAdapter()
			{
				public void mouseDragged(MouseEvent e)
				{
					if(doingDragging)
					{
						int gotX = e.getX() / imagePanel.getZoom();
						int gotY = e.getY() / imagePanel.getZoom();
						
						if(gotX >= selImgW)
							gotX = selImgW - 1;
						if(gotY >= selImgH)
							gotY = selImgH - 1;
						
						selX2 = gotX;
						selY2 = gotY;
						
						imagePanel.repaint();
					}
				}
			}
		);

		imagePanelScroll.getViewport().setView(imagePanel);
		imagePanelScroll.setMinimumSize(new Dimension(320, 280));
		
		add(imagePanelScroll, c);
		
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;
		c.weightx = 0;
		c.weighty = 0;	
			
		add(imageNumLabel, c);

		c.gridx = 1;
		c.gridy = 1;
		
		imageNumSpinner.addChangeListener(
			new ChangeListener()
			{
				public void stateChanged(ChangeEvent e)
				{
					imagePanel.setImage( (int)imageNumSpinner.getValue() - 1 );
					selX = 0;
					selY = 0;
					selX2 = 0;
					selY2 = 0;
					
					imagePanel.repaint();
				}
			}
		);
			
		add(imageNumSpinner, c);
			
		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 1;
		c.weightx = 0;
		c.weighty = 0;
		
		flag1CheckBox.setSelected( frame.getFlag1() );
			
		add(flag1CheckBox, c);
			
		c.gridx = 0;
		c.gridy = 3;
		c.gridwidth = 1;
			
		flag2CheckBox.setSelected( frame.getFlag2() );
			
		add(flag2CheckBox, c);
		
		c.gridx = 0;
		c.gridy = 4;
		
		adjustHotspotButton.addActionListener(
			new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					new AdjustHotspotDialog(frameEdit);
				}
			}
		);
		
		add(adjustHotspotButton, c);
		
		c.gridx = 0;
		c.gridy = 5;
		
		applyButton.addActionListener(
			new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					int rX = (selX2<selX)?selX2:selX;
					int rY = (selY2<selY)?selY2:selY;
					int rX2 = (selX2<selX)?selX:selX2;
					int rY2 = (selY2<selY)?selY:selY2;
					
					taxAni.keepOldData();
					
					frame.setImageNum((int)imageNumSpinner.getValue() - 1);
					frame.setXPos(rX);
					frame.setYPos(rY);
					frame.setWidth(rX2 - rX);
					frame.setHeight(rY2 - rY);
					frame.setHsX(selHsX);
					frame.setHsY(selHsY);
					
					if(isNew)
						entry.getValue().add(frame);
					
					taxAni.animationPanel.setEntry(entry);
					
					taxAni.opChangeData();
					dispose();
				}
			});
		
		add(applyButton, c);
		
		c.gridx = 1;
		c.gridy = 5;
		c.gridwidth = 2;
		
		discardButton.addActionListener(
			new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					dispose();
				}
			});
			
		add(discardButton, c);
		
	/*	c.gridx = 0;
		c.gridy++;
		c.weighty = 1.0;
		
		add(new JLabel(""), c);*/
		
		pack();//setSize(512, 512);
		setTitle(isNew? "New Frame" : "Frame Editor");
		setLocationRelativeTo(taxAni);
		setVisible(true);
	}
}
