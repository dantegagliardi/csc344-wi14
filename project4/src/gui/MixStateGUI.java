package gui;

import java.awt.BorderLayout;
import java.io.File;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JPanel;

import main.StateMachine;
import main.WavFile;
import main.WavFileException;

public class MixStateGUI
{
   public static WavFile m_Song1;
   public static WavFile m_Song2;
   public static File m_SaveSong;
   
   public static int m_Song1Frame1;
   public static int m_Song1Frame2;
   public static int m_Song2Frame1;
   public static int m_Song2Frame2;
   
   static StateMachinePanel m_StatePanel;
   
   public static void main(String[] args)
   {
      javax.swing.SwingUtilities.invokeLater(new Runnable()
      {
         public void run()
         {
            createAndShowGui();
         }
      });
   }
   
   private static void createAndShowGui()
   {
      JFrame frame = new JFrame("MixState");
      frame.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
      
      JPanel windowPanel = new JPanel(new BorderLayout());
      
      windowPanel.add(new SetupPanel(), BorderLayout.NORTH);
      
      m_StatePanel = new StateMachinePanel();
      
      windowPanel.add(m_StatePanel, BorderLayout.CENTER);

      frame.getContentPane().add(windowPanel);

      // Pack and render the application
      frame.setVisible(true);
      frame.pack();
   }
   
   public static void createAndRunTheMagic()
   {
      new Thread(new Runnable()
      {
         
         @Override
         public void run()
         {
            try
            {
               new StateMachine(m_StatePanel);
            }
            catch (IOException | WavFileException e)
            {
               // Oh well
            }
         }
      }).start();
   }
}
