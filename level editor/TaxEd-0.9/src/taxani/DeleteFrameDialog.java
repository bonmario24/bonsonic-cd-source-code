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

class DeleteFrameDialog
	extends JDialog
{
	JButton deleteButton = new JButton("Delete");
	JButton cancelButton = new JButton("Cancel");
	JComboBox<RSFrame> frameList = new JComboBox<RSFrame>();
	
	DeleteFrameDialog(final TaxAni taxAni)
	{
		final AnimationPanel animationPanel = taxAni.animationPanel;
		
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setModalityType(ModalityType.APPLICATION_MODAL);
		setLayout(new GridBagLayout());
		
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(0, 2, 5, 2);
		c.fill = GridBagConstraints.BOTH;
					
		setTitle("Select frame to delete");
		
		((JPanel)getContentPane()).setBorder(new TitledBorder(new EtchedBorder(), "Delete Frame"));

		int maxWidth = -1;
		int maxHeight = -1;
		
		for(int i = 0, l = animationPanel.entry.getValue().size(); i < l; i++)
		{
			RSFrame frame = animationPanel.entry.getValue().get(i);
			
			frameList.addItem(frame);
			
			if(frame.getWidth() > maxWidth)  maxWidth = frame.getWidth();
			if(frame.getHeight() > maxHeight) maxHeight = frame.getHeight();
		}
		
		FrameListRenderer customRenderer = new FrameListRenderer(animationPanel);
		customRenderer.allowIconDisplay();
		
		frameList.setRenderer(customRenderer);
		
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.gridheight = 1;
		c.weightx = 0;
		c.anchor = GridBagConstraints.CENTER;
		
		//frameList.setPreferredSize( new Dimension(maxWidth + 128, maxHeight + 24) );
		add(frameList, c);
		
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;
		
		deleteButton.addActionListener(
			new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					taxAni.keepOldData();
					
					ArrayList<RSFrame> frameArrayList = animationPanel.entry.getValue();
					
					frameArrayList.remove(frameList.getSelectedIndex());
					
					animationPanel.setEntry(animationPanel.entry);
					
					taxAni.opChangeData();
					
					dispose();
				}
			}
		);
		
		add(deleteButton, c);
		
		c.gridx = 1;
		c.gridy = 1;
		
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
