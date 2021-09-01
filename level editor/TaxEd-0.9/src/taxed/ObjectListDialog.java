package taxed;

import javax.swing.JDialog;
import javax.swing.JList;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.FlowLayout;
import javax.swing.JScrollPane;
import rsonic.*;
import java.awt.Component;
import javax.swing.Box;

public class ObjectListDialog extends JDialog
{
	private JList<RSObject> list = new JList<RSObject>();

	public RSObject[] getObjectArray(TaxEdFrame taxEd)
	{
		return taxEd.objList.toArray(new RSObject[0]);
	}

	public ObjectListDialog(final TaxEdFrame taxEd) 
	{
		getContentPane().setLayout(new FlowLayout());
		setTitle("Object list");
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		getContentPane().setLayout(new FlowLayout());
	
		JScrollPane scrollPane = new JScrollPane();
		list = new JList<RSObject>();
		list.setListData(getObjectArray(taxEd));
		JButton closeButton = new JButton("Close");
		closeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		JButton editButton = new JButton("Edit");
		
		editButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new EditObjectDialog(taxEd, list.getSelectedValue());
				repaint();
			}
		});
	
		list.setVisibleRowCount(10);
		
		scrollPane.setViewportView(list);
		
		getContentPane().add(scrollPane);
		
		getContentPane().add(editButton);
		getContentPane().add(closeButton);
		
		pack();

		setResizable(false);
		setLocationRelativeTo(taxEd);
		setVisible(true);
	}

}
