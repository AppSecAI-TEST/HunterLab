package edu.ucdenver.anschutz.cpbs.hunter;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MechAnIC_GUI {
    private JTree ontologyViewer;
    private JLabel termViewer;
    private JButton fileButton;
    private JPanel panelMain;
    private JTabbedPane editors;
    private JPanel editorPanel;
    private JEditorPane editor;
    private JScrollPane editorScrollPanel;

    public MechAnIC_GUI(JFrame frame) {

        JMenuBar menuBar = new JMenuBar();
        frame.setJMenuBar(menuBar);

        JMenu file = new JMenu("File");
        menuBar.add(file);
        JMenuItem exit = new JMenuItem("Exit");
        exit.addActionListener(new exitaction());
        file.add(exit);

        JMenu help = new JMenu("Help");
        menuBar.add(help);
        JMenuItem about = new JMenuItem("About");
        help.add(about);
    }

    class exitaction implements ActionListener {
        public void actionPerformed (ActionEvent e) {
            System.exit(0);
        }
    }

    public static void main(String[] args){
        JFrame frame = new JFrame("MechAnIC");
        frame.setContentPane(new MechAnIC_GUI(frame).panelMain);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
