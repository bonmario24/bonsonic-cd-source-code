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

public class AdjustHotspotDialog
	extends JDialog
{
	FrameEditDialog frameEdit;
	JLabel hsXLabel = new JLabel("X displacement:");
	JLabel hsYLabel = new JLabel("Y displacement:");
	JSpinner hsXSpinner = new JSpinner( new SpinnerNumberModel( 0, -128, 127, 1 ) );
	JSpinner hsYSpinner = new JSpinner( new SpinnerNumberModel( 0, -128, 127, 1 ) );
	JButton applyButton = new JButton("Apply");
	JButton discardButton = new JButton("Discard");
	
	public AdjustHotspotDialog(final FrameEditDialog frameEdit)
	{
		this.frameEdit = frameEdit;
		setTitle("Adjust hotspot");
		
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		
		setModalityType(ModalityType.APPLICATION_MODAL);
		
		setLayout(new GridBagLayout());
		
		GridBagConstraints c = new GridBagConstraints();
		
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(0, 2, 5, 2);
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.weightx = 0;
		c.anchor = GridBagConstraints.CENTER;
		
		add(hsXLabel, c);
		
		c.gridx = 1;
		c.gridy = 0;
		
		hsXSpinner.setToolTipText("Valid values range from -128 to 127");
		add(hsXSpinner, c);
		
		c.gridx = 0;
		c.gridy = 1;
		
		add(hsYLabel, c);
		
		c.gridx = 1;
		c.gridy = 1;
		
		hsYSpinner.setToolTipText("Valid values range from -128 to 127");
		add(hsYSpinner, c);
		
		c.gridx = 0;
		c.gridy = 2;
		
		applyButton.addActionListener(
			new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					//
					frameEdit.selHsX = (int) hsXSpinner.getValue();
					frameEdit.selHsY = (int) hsYSpinner.getValue();
					dispose();
				}
			}
		);
		
		add(applyButton, c);
		
		c.gridx = 1;
		c.gridy = 2;
		
		discardButton.addActionListener(
			new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					dispose();
				}
			}
		);
			
		add(discardButton, c);
		
		hsXSpinner.setValue( frameEdit.selHsX);
		hsYSpinner.setValue( frameEdit.selHsY);
//		setSize(320, 240);
		pack();
			
		setLocationRelativeTo(frameEdit);
		setVisible(true);
	}
}
