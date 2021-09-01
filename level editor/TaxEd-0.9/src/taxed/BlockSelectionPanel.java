package taxed;

import java.awt.*;
import javax.swing.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.awt.event.*;

public class BlockSelectionPanel extends JPanel {
	private final JMapDisplay display;
	private int map[][];
	private int mapX, mapY;
	private boolean ready;
	private JPanel blockShowPanel;
	private JScrollPane blockShowScroll;
	private JSpinner spinner;
	private boolean doNotModify;
	
	public void setCurrentBlock(int map[][], int mapX, int mapY)
	{
		setCurrentBlock(map, mapX, mapY, true);
	}
	
	public void setCurrentBlock(int map[][], int mapX, int mapY, boolean scroll)
	{
		this.map = map;
		this.mapX = mapX;
		this.mapY = mapY;
		this.ready = true;
		doNotModify = true;
		spinner.setValue((map == null)?0:map[mapY][mapX]);
		
		if(map != null && scroll)
		{
			JScrollBar v = blockShowScroll.getVerticalScrollBar();
		
			int c = map[mapY][mapX];
			int pos = c * 128;
			
			if(pos > v.getMaximum())
				pos = v.getMaximum();
			
			v.setValue(pos);
		}
		
		repaint();
	}
	
	class BlockShowPanel extends JPanel
	{
		public void paintComponent(Graphics g)
		{
			if(ready && display.ready && (map != null))
			{
				g.setColor(Color.blue);
				g.fillRect(0, 0, getWidth(), getHeight());
			//	g.drawImage(display.blockImages[map[mapY][mapX]], 0, 0, this);
				
				for(int i = 0; i < 512; i++)
				{
					g.drawImage(display.blockImages[i], 0, i * 128, this);
					g.setColor(Color.black);
					g.drawRect(0, i*128, 127, 127);
				}
				
				g.setColor(Color.red);
				g.draw3DRect(0, map[mapY][mapX] * 128, 127, 127, true);
			}
			else
			{
				g.setColor(Color.black);
				g.fillRect(0, 0, getWidth(), getHeight());
			}
		}
	
		public BlockShowPanel()
		{
			setPreferredSize(new Dimension(128, 65536));
		}
	}

	public BlockSelectionPanel(final TaxEdFrame taxEd)
	{
		JMapDisplay displayComp = taxEd.display;
	
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 46, 0};
		gridBagLayout.rowHeights = new int[]{15, 128, 15, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 0.0, 0.0};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);

		JLabel lblCurrentlySelectedBlock = new JLabel("Currently selected block");
		lblCurrentlySelectedBlock.setVerticalAlignment(SwingConstants.TOP);
		GridBagConstraints gbc_lblCurrentlySelectedBlock = new GridBagConstraints();
		gbc_lblCurrentlySelectedBlock.anchor = GridBagConstraints.NORTHWEST;
		gbc_lblCurrentlySelectedBlock.insets = new Insets(0, 0, 5, 0);
		gbc_lblCurrentlySelectedBlock.gridwidth = 2;
		gbc_lblCurrentlySelectedBlock.gridx = 0;
		gbc_lblCurrentlySelectedBlock.gridy = 0;
		add(lblCurrentlySelectedBlock, gbc_lblCurrentlySelectedBlock);
		
		
		blockShowPanel = new BlockShowPanel();
		
		blockShowScroll = new JScrollPane();
		
	//	blockShowScroll.setMinimumSize(new Dimension(128, 384));
	//	blockShowScroll.setPreferredSize(new Dimension(128, 384));
		blockShowScroll.setViewportView(blockShowPanel);
		blockShowScroll.setMinimumSize(new Dimension(146, 384));
		blockShowScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		blockShowScroll.getVerticalScrollBar().setUnitIncrement(128);

		
		GridBagConstraints gbc_blockShowPanel = new GridBagConstraints();
		gbc_blockShowPanel.anchor = GridBagConstraints.NORTHWEST;
		gbc_blockShowPanel.insets = new Insets(0, 0, 5, 0);
		gbc_blockShowPanel.gridx = 0;
		gbc_blockShowPanel.gridy = 1;
		gbc_blockShowPanel.gridwidth = 2;
		add(blockShowScroll, gbc_blockShowPanel);
		
		JLabel blockNumberLabel = new JLabel("Block number: ");
		GridBagConstraints gbc_blockNumberLabel = new GridBagConstraints();
		gbc_blockNumberLabel.anchor = GridBagConstraints.NORTHWEST;
		gbc_blockNumberLabel.gridx = 0;
		gbc_blockNumberLabel.gridy = 2;
		add(blockNumberLabel, gbc_blockNumberLabel);
		
		blockShowPanel.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if(!ready || !display.ready)
					return;
				
				taxEd.saveState();
				
				map[mapY][mapX] = e.getY() / 128;
				setCurrentBlock(map, mapX, mapY, false);
				display.repaint();
							
				taxEd.setModified(true);
			}
			
			public void mouseReleased(MouseEvent e)
			{
				
			}
		});
		
		spinner = new JSpinner(new SpinnerNumberModel(0, 0, 511, 1));
		spinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if(ready && display.ready && map != null)
				{					
					if(!doNotModify)
					{
						taxEd.saveState();
					
						map[mapY][mapX] = (int)spinner.getValue();
					//	repaint();
						display.repaint();
						setCurrentBlock(map, mapX, mapY, true);
						taxEd.setModified(true);
					}
					
					doNotModify = false;
				}
			}
		});

		spinner.setToolTipText("Block number of currently selected block");
		
		GridBagConstraints gbc_spinner = new GridBagConstraints();
		gbc_spinner.anchor = GridBagConstraints.NORTHWEST;
		gbc_spinner.gridx = 1;
		gbc_spinner.gridy = 2;
		add(spinner, gbc_spinner);

		setCurrentBlock(null, 0, 0);
		
		
		
		this.display = displayComp;
	}
}
