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

class FrameListRenderer extends JLabel
                       implements ListCellRenderer<Object> 
{
// Adapted from the Java Tutorial example	
	
	AnimationPanel animationPanel;
	private ImageIcon nullIcon = new ImageIcon(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB));
	private boolean canSetIcon = false;
	
	public FrameListRenderer(AnimationPanel animationPanel) 
	{
		this.animationPanel = animationPanel;
		setOpaque(true);
		setHorizontalAlignment(CENTER);
		setVerticalAlignment(CENTER);
	}
	
	public void allowIconDisplay()
	{
		canSetIcon = true;
	}

    /*
     * This method finds the image and text corresponding
     * to the selected value and returns the label, set up
     * to display the text and image.
     */
    
	public Component getListCellRendererComponent(
                                       JList list,
                                       Object value,
                                       int index,
                                       boolean isSelected,
                                       boolean cellHasFocus)
	{
        //Get the selected index. (The index param isn't
        //always valid, so just use the value.)
		if(!canSetIcon)
			return this;

		int selectedIndex = animationPanel.entry.getValue().indexOf(value);//((Integer)value).intValue();
		
		if (isSelected) 
		{
			setBackground(list.getSelectionBackground());
			setForeground(list.getSelectionForeground());
		} 
		else 
		{
			setBackground(list.getBackground());
			setForeground(list.getForeground());
		}
	
		if(selectedIndex == -1)
		{
			setIcon(nullIcon);
			setText("No frames!");
		}
		else
		{
			if(canSetIcon)
			{
				if(selectedIndex < this.animationPanel.frameIcons.length)
					setIcon(this.animationPanel.frameIcons[selectedIndex]);
			}
		
			setText("Frame " + (selectedIndex + 1));
		}
	
		setFont(list.getFont());
		repaint();

		return this;
	}
}