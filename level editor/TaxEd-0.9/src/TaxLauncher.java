import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import taxed.*;
import taxani.*;

public class TaxLauncher
	extends JFrame
{
	JButton taxedButton;
	JButton taxaniButton;
	JButton exitButton;
	JFrame taxLauncher;
	
	public TaxLauncher()
	{
		taxLauncher = this;
		JPanel panel = (JPanel)getContentPane();
		
		
		setTitle("TaxEd Suite Launcher");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		panel.setLayout(new GridBagLayout());
		
		Font buttonFont =
			new Font(null, Font.ITALIC | Font.BOLD, 14);
		
		taxedButton = new JButton("Level Editor");
		taxaniButton = new JButton("Animation Editor");
		exitButton = new JButton("Exit");
		
		ImageIcon launcherIcon =
			new ImageIcon(this.getClass().getResource("/icons/launcher.gif"));
		
		setIconImage(launcherIcon.getImage());
		
		ImageIcon taxedIcon =
			new ImageIcon(this.getClass().getResource("/icons/taxed.gif"));
				
		ImageIcon taxaniIcon =
			new ImageIcon(this.getClass().getResource("/icons/taxani.gif"));
		
		ImageIcon exitIcon =
			new ImageIcon(this.getClass().getResource("/icons/exit.gif"));
		
		taxedButton.setFont(buttonFont);
		taxedButton.setIcon(taxedIcon);
		
		taxaniButton.setFont(buttonFont);
		taxaniButton.setIcon(taxaniIcon);
		
		exitButton.setFont(buttonFont);
		exitButton.setIcon(exitIcon);
		
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.CENTER;
		//c.fill = GridBagConstraints.BOTH;
		c.gridwidth = 1;
		c.gridheight = 1;
		
		panel.add(taxedButton, c);
		
		c.gridx = 1;
		c.gridy = 0;
		
		panel.add(taxaniButton, c);
		
		c.gridx = 0;
		c.gridy = 1;
		c.fill = 0;//GridBagConstraints.BOTH;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.gridheight = 1;
		
		panel.add(exitButton, c);
		
		taxedButton.addActionListener(
		new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				(new TaxEdFrame()).setVisible(true);
				taxLauncher.dispose();
			}
		});
		
		taxaniButton.addActionListener(
		new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				(new TaxAni()).setVisible(true);
				taxLauncher.dispose();
			}
		});
		
		exitButton.addActionListener(
			new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				System.exit(0);
			}
		});
		
		pack();
		setLocationRelativeTo(null);
	}
	
	public static void main(String[ ] args)
	{
		( new TaxLauncher() ).setVisible(true);
	}
}
