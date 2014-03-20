package gui;

import static gui.MixStateGUI.*;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

import main.WavFile;
import main.WavFileException;

public class SetupPanel extends JPanel implements ActionListener
{
   private static final Dimension DIMENSION = new Dimension(150, 25);

   private JFileChooser           m_FileChooser;

   private JButton                m_OpenSong1Button;
   private JButton                m_OpenSong2Button;
   private JButton                m_SaveFileButton;
   private JButton                m_StartTheMagicButton;

   private JTextField             m_Song1StartBeatFrame;
   private JTextField             m_Song1FinalBeatFrame;

   private JTextField             m_Song2StartBeatFrame;
   private JTextField             m_Song2FinalBeatFrame;

   public SetupPanel()
   {
      super(new BorderLayout());

      initElements();

      addElements();

      setMaximumSize(new Dimension(300, 250));
      setMinimumSize(getMaximumSize());
   }

   /**
    * Add all the gui elements to this panel
    */
   private void addElements()
   {
      // Outer wrapper
      JPanel buttonPanel = new JPanel();
      buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));

      // Open file buttons
      JPanel openPanel = new JPanel();
      openPanel.setLayout(new BoxLayout(openPanel, BoxLayout.X_AXIS));

      JPanel song1Panel = new JPanel();
      song1Panel.setLayout(new BoxLayout(song1Panel, BoxLayout.Y_AXIS));

      song1Panel.add(m_OpenSong1Button);
      song1Panel.add(m_Song1StartBeatFrame);
      song1Panel.add(m_Song1FinalBeatFrame);

      JPanel song2Panel = new JPanel();
      song2Panel.setLayout(new BoxLayout(song2Panel, BoxLayout.Y_AXIS));

      song2Panel.add(m_OpenSong2Button);
      song2Panel.add(m_Song2StartBeatFrame);
      song2Panel.add(m_Song2FinalBeatFrame);

      openPanel.add(song1Panel);
      openPanel.add(song2Panel);

      // Save file button
      JPanel savePanel = new JPanel();
      savePanel.setLayout(new BoxLayout(savePanel, BoxLayout.X_AXIS));

      savePanel.add(m_SaveFileButton);

      // Start remixing button
      JPanel magicPanel = new JPanel();
      magicPanel.setLayout(new BoxLayout(magicPanel, BoxLayout.X_AXIS));

      magicPanel.add(m_StartTheMagicButton);

      // Add everything to the outer wrapper
      buttonPanel.add(openPanel);
      buttonPanel.add(savePanel);
      buttonPanel.add(magicPanel);

      // Add everything to this panel
      add(buttonPanel, BorderLayout.NORTH);
   }

   /**
    * Initialize all the gui elements
    */
   private void initElements()
   {
      // File chooser dialog
      m_FileChooser = new JFileChooser();
      m_FileChooser.setMultiSelectionEnabled(false);
      m_FileChooser.setFileFilter(new FileNameExtensionFilter(
            "WAV files only (.wav)", "wav"));

      // Open file 1
      m_OpenSong1Button = new JButton("Open Song 1...");
      m_OpenSong1Button.addActionListener(this);
      m_OpenSong1Button.setMaximumSize(DIMENSION);
      m_OpenSong1Button.setMinimumSize(DIMENSION);
      m_OpenSong1Button.setPreferredSize(DIMENSION);
      m_OpenSong1Button.setAlignmentX(CENTER_ALIGNMENT);

      m_Song1StartBeatFrame = new JTextField("S1: Beat 1 Start Frame");
      m_Song1StartBeatFrame.setMaximumSize(DIMENSION);
      m_Song1StartBeatFrame.setMinimumSize(DIMENSION);
      m_Song1StartBeatFrame.setPreferredSize(DIMENSION);

      m_Song1FinalBeatFrame = new JTextField("S1: Beat 1 Final Frame");
      m_Song1FinalBeatFrame.setMaximumSize(DIMENSION);
      m_Song1FinalBeatFrame.setMinimumSize(DIMENSION);
      m_Song1FinalBeatFrame.setPreferredSize(DIMENSION);

      // Open file 2
      m_OpenSong2Button = new JButton("Open Song 2...");
      m_OpenSong2Button.addActionListener(this);
      m_OpenSong2Button.setMaximumSize(DIMENSION);
      m_OpenSong2Button.setMinimumSize(DIMENSION);
      m_OpenSong2Button.setPreferredSize(DIMENSION);
      m_OpenSong2Button.setAlignmentX(CENTER_ALIGNMENT);

      m_Song2StartBeatFrame = new JTextField("S2: Beat 1 Start Frame");
      m_Song2StartBeatFrame.setMaximumSize(DIMENSION);
      m_Song2StartBeatFrame.setMinimumSize(DIMENSION);
      m_Song2StartBeatFrame.setPreferredSize(DIMENSION);

      m_Song2FinalBeatFrame = new JTextField("S2: Beat 1 Final Frame");
      m_Song2FinalBeatFrame.setMaximumSize(DIMENSION);
      m_Song2FinalBeatFrame.setMinimumSize(DIMENSION);
      m_Song2FinalBeatFrame.setPreferredSize(DIMENSION);

      // Open file 3
      m_SaveFileButton = new JButton("Choose Output File...");
      m_SaveFileButton.addActionListener(this);
      m_SaveFileButton.setMaximumSize(DIMENSION);
      m_SaveFileButton.setMinimumSize(DIMENSION);
      m_SaveFileButton.setPreferredSize(DIMENSION);
      m_SaveFileButton.setEnabled(false);

      // Start the magic button
      m_StartTheMagicButton = new JButton("Start The Magic!");
      m_StartTheMagicButton.addActionListener(this);
      m_StartTheMagicButton.setMaximumSize(DIMENSION);
      m_StartTheMagicButton.setMinimumSize(DIMENSION);
      m_StartTheMagicButton.setPreferredSize(DIMENSION);
      m_StartTheMagicButton.setEnabled(false);
   }

   @Override
   public void actionPerformed(ActionEvent event)
   {
      // If open file 1, and selected a file
      if (event.getSource() == m_OpenSong1Button)
      {
         // Try opening the input file
         if (m_FileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
            try
            {
               m_Song1 = WavFile.openWavFile(m_FileChooser.getSelectedFile());
               m_OpenSong1Button.setText(m_FileChooser.getSelectedFile()
                     .getName());
               m_SaveFileButton.setEnabled(true);
            }
            catch (IOException | WavFileException e1)
            {
               JOptionPane.showMessageDialog(null, "Couldn't open the file.");
            }
      }
      // If open file 2, and selected a file
      if (event.getSource() == m_OpenSong2Button)
      {
         // Try opening the input file
         if (m_FileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
            try
            {
               m_Song2 = WavFile.openWavFile(m_FileChooser.getSelectedFile());
               m_OpenSong2Button.setText(m_FileChooser.getSelectedFile()
                     .getName());
               m_SaveFileButton.setEnabled(true);
            }
            catch (IOException | WavFileException e1)
            {
               JOptionPane.showMessageDialog(null, "Couldn't open the file.");
            }
      }
      // If save file
      if (event.getSource() == m_SaveFileButton)
      {
         // Must have already selected input file(s)
         if (m_Song1 == null && m_Song2 == null)
         {
            JOptionPane.showMessageDialog(null,
                  "Please select at least one input file.");
            return;
         }

         // Try to open the output file
         if (m_FileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION)
         {
            // Create a new wav file
            m_SaveSong = m_FileChooser.getSelectedFile();
            m_StartTheMagicButton.setEnabled(true);
         }
      }
      // If start the magic, and an input song exists
      if (event.getSource() == m_StartTheMagicButton
            && (m_Song1 != null || m_Song2 != null))
      {
         // Must have already select output file
         if (m_SaveSong == null)
         {
            JOptionPane
                  .showMessageDialog(null, "Please select an output file.");
            return;
         }

         try
         {
            if (m_Song1 != null)
            {
               m_Song1Frame1 = Integer
                     .parseInt(m_Song1StartBeatFrame.getText());
               m_Song1Frame2 = Integer
                     .parseInt(m_Song1FinalBeatFrame.getText());
            }
            if (m_Song2 != null)
            {
               m_Song2Frame1 = Integer
                     .parseInt(m_Song2StartBeatFrame.getText());
               m_Song2Frame2 = Integer
                     .parseInt(m_Song2FinalBeatFrame.getText());
            }
         }
         catch (NumberFormatException nfe)
         {
            JOptionPane
                  .showMessageDialog(null,
                        "Please enter the start and end frame number of the first beat.");
            return;
         }

         MixStateGUI.createAndRunTheMagic();
      }
   }
}
