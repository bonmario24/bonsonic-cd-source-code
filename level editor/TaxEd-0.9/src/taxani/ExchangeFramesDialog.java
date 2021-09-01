package taxani;

import rsonic.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import javax.imageio.*;
import java.io.*;
import java.util.*;

class ExchangeFramesDialog
	extends JDialog
{
	JButton exchangeButton = new JButton("Exchange");
	JButton cancelButton = new JButton("Cancel");
	JComboBox<RSFrame> frameList = new JComboBox<RSFrame>();
	JComboBox<RSFrame> frameList2 = new JComboBox<RSFrame>();
	
	ExchangeFramesDialog(final TaxAni taxAni)
	{
		final AnimationPanel animationPanel = 
			taxAni.animationPanel;
		
		((JPanel)getContentPane()).setBorder(new TitledBorder(new EtchedBorder(), "Exchange Frames"));

		setModalityType(ModalityType.APPLICATION_MODAL);
		setTitle("Select frames to exchange");
		
		setLayout(new GridBagLayout());
		
		GridBagConstraints c = new GridBagConstraints();
		
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(0, 2, 5, 2);
		
		for(int i = 0, l = animationPanel.entry.getValue().size(); i < l; i++)
		{
			RSFrame frame = animationPanel.entry.getValue().get(i);
			
			frameList.addItem(frame);
			frameList2.addItem(frame);
		}
		
		FrameListRenderer customRenderer = new FrameListRenderer(animationPanel);
		
		customRenderer.allowIconDisplay();
		
		frameList.setRenderer( customRenderer );
		frameList2.setRenderer( customRenderer );
		
		

		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.gridheight = 1;
		c.weightx = 0;
		c.anchor = GridBagConstraints.CENTER;
		
		add(frameList, c);
		
		c.gridx = 0;
		c.gridy = 1;
		
		add(frameList2, c);
		
		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 1;
		
		exchangeButton.addActionListener(
			new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					taxAni.keepOldData();
					
					int idx1 = frameList.getSelectedIndex();
					int idx2 = frameList2.getSelectedIndex();
					
					ArrayList<RSFrame> frameArrayList = animationPanel.entry.getValue();
					
					RSFrame ex = frameArrayList.get(idx1);
					frameArrayList.set(idx1, frameArrayList.get(idx2));
					frameArrayList.set(idx2, ex);
					
					animationPanel.setEntry(animationPanel.entry);
					
					taxAni.opChangeData();
					
					dispose();
				}
			}
		);
		
		add(exchangeButton, c);
		
		c.gridx = 1;
		c.gridy = 2;
		
		cancelButton.addActionListener(
			new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					dispose();
				}
			}
		);
			
		add(cancelButton, c);
		
		pack();
		setLocationRelativeTo(taxAni);
		setVisible(true);
	}
}
