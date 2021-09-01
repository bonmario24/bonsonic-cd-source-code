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

public class PreviewAnimationDialog
	extends JDialog
{
	private BufferedImage frameImages[];
	private DrawingPanel drawingPanel;
	private RSAnimation animation;
	private ArrayList<RSFrame> frames;
	private JDialog previewDialog = this;
	
	private int minHsX = 256;
	private int minHsY = 256;
	private int maxHsX = -128;
	private int maxHsY = -128;
	
	class DrawingPanel
		extends JPanel
	{
		int currentFrame = 0;
		int speedCounter = 0;
		private Color magicColor = new Color(255, 0, 255);
		private boolean timerFired = false;
		private javax.swing.Timer timer;
				
		public void paintComponent(Graphics g)
		{
			while(speedCounter > 0)
			{
				currentFrame++;
				if(currentFrame >= frames.size())
					currentFrame = animation.getLoopStart();
				
				speedCounter--;
			}
			
			g.setColor(magicColor);
			g.fillRect(0, 0, getWidth(), getHeight());

			g.drawImage(frameImages[currentFrame], -(minHsX - frames.get(currentFrame).getHsX()), 
				-(minHsY - frames.get(currentFrame).getHsY()), previewDialog);
			
			if(!timerFired && animation.getSpeed() > 0)
			{
				timer.start();
				timerFired =  true;
			}
		}
		
		DrawingPanel(int width, int height)
		{
			setPreferredSize(new Dimension(width + (maxHsX-minHsX), height + (maxHsY-minHsY)));
			setSize(width, height);
			
			if(animation.getSpeed() > 0)
			{
				timer = new javax.swing.Timer(
					5000 / animation.getSpeed(),
						new ActionListener()
						{
							public void actionPerformed(ActionEvent e)
							{
								previewDialog.repaint();
				
								speedCounter++;
							}
						}
					);						
			}
		}
	}

	PreviewAnimationDialog(TaxAni taxAni, Map.Entry<RSAnimation, ArrayList<RSFrame>> entry)
	{
		int maxWidth = -1;
		int maxHeight = -1;

		
		frameImages = new BufferedImage[ entry.getValue().size() ];
		int cnt = 0;
		
		animation = entry.getKey();
		frames = entry.getValue();
		
		for(RSFrame frame : frames)
		{
			if(frame.getWidth() > maxWidth)
				maxWidth = frame.getWidth();
			
			if(frame.getHeight() > maxHeight)
				maxHeight = frame.getHeight();
			
			int hsX = frame.getHsX();
			int hsY = frame.getHsY();
			
			if(hsX < minHsX)
				minHsX = hsX;
			
			if(hsY < minHsY)
				minHsY = hsY;
			
			if(hsX > maxHsX)
				maxHsX = hsX;
			
			if(hsY > maxHsY)
				maxHsY = hsY;
			
			frameImages[cnt++] = taxAni.images[ frame.getImageNum() ].getSubimage
				(frame.getXPos(), frame.getYPos(), frame.getWidth(), frame.getHeight() );
			
			//System.out.println(cnt + ": " + frame.getHsX() + ", " + frame.getHsY());
		}
		
		/*/System.out.println();
		System.out.println("minHsX = + " + minHsX);
		System.out.println("minHsY = + " + minHsY);
		System.out.println();*/
		
		setLayout(new GridBagLayout());
		setTitle("Animation Preview");
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setModalityType(ModalityType.APPLICATION_MODAL);
		
		JButton okButton = new JButton("OK");
		
		GridBagConstraints c = new GridBagConstraints();
		
		c.insets = new Insets(0, 2, 5, 2);
		
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		
		drawingPanel = new DrawingPanel(maxWidth, maxHeight);
		
		add(drawingPanel, c);
		
		c.gridx = 0;
		c.gridy = 1;
		
		okButton.addActionListener(
			new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					dispose();
				}
			}
		);
			
		add(okButton, c);
			
		c.gridx = 0;
		c.weighty = 1.0;
		c.gridy++;

		pack();
		
		setLocationRelativeTo(taxAni);
		setVisible(true);
	}
}
