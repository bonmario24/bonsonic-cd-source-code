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

class AnimationPanel
	extends JPanel
{
	//boolean canSetIcon = false;
	
	FrameListRenderer customRenderer;
	JLabel numFramesLabel = new JLabel("Number of frames: ");
	JLabel numFramesLabelValue = new JLabel("0");
	JLabel animationSpeedLabel = new JLabel("Animation speed: ");
	JLabel loopFromFrameLabel = new JLabel("Loop from frame: ");
	private boolean firstTime = true;
	JPanel internalPanel = new JPanel();
	JSlider aniSpeedSlider = new JSlider(JSlider.HORIZONTAL, 0, 255, 0);
	JSpinner aniSpeedSpinner = new JSpinner( new SpinnerNumberModel( 0, 0, 255, 1 ) );
	JSpinner loopFromFrameSpinner = new JSpinner (new SpinnerNumberModel(1, 1, 256, 1) );
	JCheckBox flag1CheckBox = new JCheckBox("Flag 1 set");
	JCheckBox flag2CheckBox = new JCheckBox("Flag 2 set");
	JButton previewButton = new JButton("Preview");

	JButton addFrameButton = new JButton("Add frame...");
	JButton deleteFrameButton = new JButton("Delete frame...");
	JButton exchangeFramesButton = new JButton("Exchange frames");

	Integer numbers[] = new Integer[] { 1, 2, 3};
	JLabel editFrameLabel = new JLabel("Edit frame:");
	JComboBox<RSFrame> frameList = new JComboBox<RSFrame>();
	
	Map.Entry<RSAnimation, ArrayList<RSFrame>> entry = null;
	
	private TaxAni taxAni;
	
	private boolean inSetEntry = false;
	
	ImageIcon frameIcons[];
	
	private AnimationPanel animationPanel = this;
	
	void setEntry(Map.Entry<RSAnimation, ArrayList<RSFrame>> entry)
	{
		this.entry = entry;
		
		RSAnimation animation = entry.getKey();
		
		//numFramesLabel.setText("Number of frames: " + entry.getValue().size());
		numFramesLabelValue.setText(""+entry.getValue().size());
		
		inSetEntry = true;
		
		((SpinnerNumberModel)(loopFromFrameSpinner.getModel())).setMaximum(entry.getValue().size());
		aniSpeedSlider.setValue(animation.getSpeed());
		aniSpeedSpinner.setValue(animation.getSpeed());
		loopFromFrameSpinner.setValue(animation.getLoopStart() + 1);
		flag1CheckBox.setSelected( animation.getFlag1() );
		flag2CheckBox.setSelected( animation.getFlag2() );
		
		boolean shouldEnable = entry.getValue().size() > 0;
		
		aniSpeedSlider.setEnabled(shouldEnable);
		aniSpeedSpinner.setEnabled(shouldEnable);
		loopFromFrameSpinner.setEnabled(shouldEnable);
		
		internalPanel.setVisible(true);
		
		frameIcons = new ImageIcon[ entry.getValue().size() ];
		int cnt = 0;
		int maxWidth = 0;
		int maxHeight = 0;
		
		frameList.removeAllItems();
		
		for(int i = 0, l = entry.getValue().size(); i < l; i++)
		{
			RSFrame frame = entry.getValue().get(i);
			frameList.addItem(frame);
			
			BufferedImage frameImage =
				taxAni.images[ frame.getImageNum() ].getSubimage(
					frame.getXPos(), frame.getYPos(),
					frame.getWidth(), frame.getHeight());
			
			frameIcons[ cnt++] = new ImageIcon(frameImage);
			
			if(frame.getWidth() > maxWidth)  maxWidth = frame.getWidth();
			if(frame.getHeight() > maxHeight) maxHeight = frame.getHeight();
		}

// Gets rid of an annoying Swing bug		
		if(entry.getValue().size() == 1)
		{
			frameList.addItem(entry.getValue().get(0));
			frameList.removeItemAt(1);
		}
			
	//	frameList.setPreferredSize( new Dimension(maxWidth+128, maxHeight+24) );
	//	customRenderer.setPreferredSize( new Dimension(maxWidth+128, maxHeight+24 ) );
		
		inSetEntry = false;
		customRenderer.allowIconDisplay();
	}
	
	void addPanelComponents()
	{
		GridBagLayout gridBagLayout = new GridBagLayout();

		internalPanel.setLayout(gridBagLayout);
		
		GridBagConstraints c = new GridBagConstraints();
		
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(0, 2, 5, 2);
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.weightx = 0;
		c.anchor = GridBagConstraints.CENTER;
		
		internalPanel.add(numFramesLabel, c);
		
		c.gridx = 1;
		c.gridy = 0;
		
		internalPanel.add(numFramesLabelValue, c);
		
		c.gridx = 0;
		c.gridy = 1;
		
		internalPanel.add(animationSpeedLabel, c);
		
		c.gridx = 1;
		c.gridy = 1;
		c.gridwidth = 3;
		c.weightx = 1.0;
		
		aniSpeedSlider.addChangeListener(
			new ChangeListener()
			{
				public void stateChanged(ChangeEvent e)
				{
					if(inSetEntry)
						return;
					
					inSetEntry = true;
					aniSpeedSpinner.setValue(aniSpeedSlider.getValue());
					inSetEntry = false;
					
					if(!aniSpeedSlider.getValueIsAdjusting())
					{
						RSAnimation animation = entry.getKey();
						
						taxAni.keepOldData();
						animation.setSpeed(aniSpeedSlider.getValue());
						taxAni.opChangeData();
					}
				}
			}
		);
		
		internalPanel.add(aniSpeedSlider, c);
		
		c.gridx = 4;
		c.gridy = 1;
		c.gridwidth = 1;
		c.weightx = 0;
		
		aniSpeedSpinner.addChangeListener(
			new ChangeListener()
			{
				public void stateChanged(ChangeEvent e)
				{
					if(inSetEntry)
						return;
					
					inSetEntry = true;
					aniSpeedSlider.setValue((int)aniSpeedSpinner.getValue());
					inSetEntry = false;
					
					RSAnimation animation = entry.getKey();
					
					taxAni.keepOldData();
					animation.setSpeed((int)aniSpeedSpinner.getValue());
					taxAni.opChangeData();
				}
			}
		);
					
		internalPanel.add(aniSpeedSpinner, c);
		
		c.gridx = 0;
		c.gridy = 2;
		c.weightx = 0;

		internalPanel.add(loopFromFrameLabel, c);
		
		c.gridx = 1;
		c.gridy = 2;
		
		loopFromFrameSpinner.addChangeListener(
			new ChangeListener()
			{
				public void stateChanged(ChangeEvent e)
				{
					if(inSetEntry)
						return;
					
					RSAnimation animation = entry.getKey();
					
					taxAni.keepOldData();
					animation.setLoopStart((int)loopFromFrameSpinner.getValue() - 1);
					taxAni.opChangeData();
				}
			}
		);
			
		internalPanel.add(loopFromFrameSpinner, c);
			
		c.gridx = 0;
		c.gridy = 3;
		
		flag1CheckBox.addActionListener(
			new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					if(inSetEntry)
						return;
					
					RSAnimation animation = entry.getKey();
					
					taxAni.keepOldData();
					animation.setFlag1( flag1CheckBox.isSelected() );
					taxAni.opChangeData();
				}
			}
		);
			
		internalPanel.add(flag1CheckBox, c);
			
		c.gridx = 0;
		c.gridy = 4;
		
		flag2CheckBox.addActionListener(
			new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					if(inSetEntry)
						return;
					
					RSAnimation animation = entry.getKey();
					
					taxAni.keepOldData();
					animation.setFlag2( flag2CheckBox.isSelected() );
					taxAni.opChangeData();
				}
			}
		);
			
		internalPanel.add(flag2CheckBox, c);
			
		c.gridx = 0;
		c.gridy = 5;
		c.gridwidth = 1;
			
		internalPanel.add(editFrameLabel, c);
			
		c.gridx = 1;
		c.gridy = 5;
		c.gridwidth = GridBagConstraints.REMAINDER;
		
		customRenderer = new FrameListRenderer(this);
		frameList.setRenderer(customRenderer);
		
		frameList.addActionListener(
			new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					if(inSetEntry)
						return;
					
					new FrameEditDialog(taxAni, entry, (RSFrame)frameList.getSelectedItem(), false);
				}
			}
		);
			
		internalPanel.add(frameList, c);
			
		c.gridwidth = 1;
			
		c.gridx = 0;
		c.gridy = 6;
		
		previewButton.addActionListener(
			new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					new PreviewAnimationDialog(taxAni, entry);
				}
			}
		);
			
		internalPanel.add(previewButton, c);
		
		c.gridx = 0;
		c.gridy = 7;
		
		addFrameButton.addActionListener(
			new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					new FrameEditDialog(taxAni, entry, 
						new RSFrame(0, false, false, 0, 0, 0, 0, 0, 0), true);
				}
			}
		);
			
		internalPanel.add(addFrameButton, c);
			
		c.gridx = 0;
		c.gridy = 8;
		
		deleteFrameButton.addActionListener(
			new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					if(entry.getValue().size() == 0)
						JOptionPane.showMessageDialog(null, "The animation does not contain any frame.");
					else
						new DeleteFrameDialog(taxAni);
				}
			}
		);
			
		internalPanel.add(deleteFrameButton, c);
			
		c.gridx = 0;
		c.gridy = 9;
		
		exchangeFramesButton.addActionListener(
			new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					if(entry.getValue().size() < 2)
						JOptionPane.showMessageDialog(null, "The animation has less than two frames.");
					else
						new ExchangeFramesDialog(taxAni);
				}
			}
		);
			
		internalPanel.add(exchangeFramesButton, c);
			
		c.gridx = 0;
		c.gridy++;
		c.weighty = 1.0;
		
		internalPanel.add(new JLabel(""), c);		
	}
		
	AnimationPanel(TaxAni taxAni)
	{	
		this.taxAni = taxAni;
		
		setBorder(new TitledBorder(new EtchedBorder(), "Animation Editor"));
		setLayout(new BorderLayout());
		
		addPanelComponents();
		add(internalPanel, BorderLayout.WEST);
		
		internalPanel.setVisible(false);
	}
}
