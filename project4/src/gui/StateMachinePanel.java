package gui;

import java.awt.Color;
import java.awt.Dimension;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import main.StateMachine.RemixState;
import main.StateMachine.RepeatState;
import main.StateMachine.SongState;
import main.StateMachine.StateEnum;

public class StateMachinePanel extends JPanel
{
   private static final Dimension DIMENSION = new Dimension(700, 100);
   
   private JSlider             m_DelayIntervalSlider;
   private JLabel              m_DelayIntervalLabel;
   
   private static final String DELAY_START  = "Operation Delay (";
   private static final String DELAY_END    = " ms)";
   private static final String NO_DELAY_END = "no delay)";
   
   private HashMap<StateEnum, JLabel> labelMap = new HashMap<StateEnum, JLabel>();

   public StateMachinePanel()
   {
      super();
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

      addSlider();
      
      addSongState();
      
      addRemixState();
      
      addRepeatState();
   }
   
   /**
    * This is where the visual magic is created. Make all the state machine nodes.
    * @param states
    * @return
    */
   private JPanel createStateNodes(StateEnum[] states, JLabel stateNameLabel, int color)
   {
      // State Type panel
      JPanel panel = new JPanel();
      panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
      panel.setAlignmentX(CENTER_ALIGNMENT);
      panel.setMinimumSize(DIMENSION);
      panel.setMaximumSize(DIMENSION);
      panel.setPreferredSize(DIMENSION);
      
      // State Node Panel
      JPanel statePanel = new JPanel();
      statePanel.setLayout(new BoxLayout(statePanel, BoxLayout.X_AXIS));
      
      
      int numStates = states.length;
      int maxX = (int) (DIMENSION.getWidth() / numStates);
      
      // Create each label
      for (StateEnum state : states)
      {
         JPanel node = new JPanel();
         node.setLayout(new BoxLayout(node, BoxLayout.Y_AXIS));
         node.setMaximumSize(new Dimension(maxX, (int) DIMENSION.getHeight()));
         node.setMinimumSize(node.getMaximumSize());
         node.setAlignmentX(CENTER_ALIGNMENT);
         
         JLabel nodeLabel = new JLabel(state.getLabel());
         nodeLabel.setBackground(new Color(color));
         nodeLabel.setFont(nodeLabel.getFont().deriveFont(14.0f));
         nodeLabel.setAlignmentX(CENTER_ALIGNMENT);
         
         node.add(nodeLabel);
         
         statePanel.add(node);
         
         labelMap.put(state, nodeLabel);
      }
      
      stateNameLabel.setFont(stateNameLabel.getFont().deriveFont(18.0f));
      stateNameLabel.setAlignmentX(CENTER_ALIGNMENT);
      
      panel.add(stateNameLabel);
      
      statePanel.setBorder(BorderFactory.createEmptyBorder(25, 0, 0, 0));
      
      panel.add(statePanel);
      return panel;
   }
   
   /**
    * returns the delay value of the slider
    * @return
    */
   public int getDelay()
   {
      return m_DelayIntervalSlider.getValue();
   }
   
   /**
    * Add all the song state nodes
    */
   private void addSongState()
   {
      add(createStateNodes(SongState.values(), new JLabel("Song States"), 0x78FDFF));
   }
   
   /**
    * Set the song state label for the state we are in to opaque
    * @param song
    */
   public void setSongState(SongState song)
   {
      for (SongState state : SongState.values())
      {
         JLabel label = labelMap.get(state);
         label.setOpaque(state == song);
         label.repaint();
      }
   }
   
   /**
    * Add all the remix state nodes
    */
   private void addRemixState()
   {
      add(createStateNodes(RemixState.values(), new JLabel("Remix States"), 0xFF5C5C));
   }
   
   /**
    * Set the remix state label for the state we are in to opaque
    * @param remix
    */
   public void setRemixState(RemixState remix)
   {
      for (RemixState state : RemixState.values())
      {
         JLabel label = labelMap.get(state);
         label.setOpaque(state == remix);
         label.repaint();
      }
   }
   
   /**
    * Add all the repeat state nodes
    */
   private void addRepeatState()
   {
      add(createStateNodes(RepeatState.values(), new JLabel("Repeat States"), 0xFFFA70));
   }
   
   /**
    * Set the repeat state label for the state we are in to opaque
    * @param repeat
    */
   public void setRepeatState(RepeatState repeat)
   {
      for (RepeatState state : RepeatState.values())
      {
         JLabel label = labelMap.get(state);
         label.setOpaque(state == repeat);
         label.repaint();
      }
   }

   /**
    * Add the slider and label to control operation delay
    */
   private void addSlider()
   {
      m_DelayIntervalSlider = new JSlider(0, 1000, 0);
      m_DelayIntervalSlider.setMinorTickSpacing(25);
      m_DelayIntervalSlider.setSnapToTicks(true);
      m_DelayIntervalSlider.setBorder(BorderFactory.createEmptyBorder(20, 10,
            20, 10));
      m_DelayIntervalSlider.setMaximumSize(new Dimension(500, 25));
      m_DelayIntervalSlider.addChangeListener(new ChangeListener()
      {
         @Override
         public void stateChanged(ChangeEvent e)
         {
            int value = m_DelayIntervalSlider.getValue();
            String end = (value == 0 ? NO_DELAY_END : value + DELAY_END);

            m_DelayIntervalLabel.setText(DELAY_START + end);
         }
      });

      m_DelayIntervalLabel = new JLabel(DELAY_START + NO_DELAY_END);
      m_DelayIntervalLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0,
            0));
      m_DelayIntervalLabel.setAlignmentX(CENTER_ALIGNMENT);

      add(m_DelayIntervalLabel);
      add(m_DelayIntervalSlider);
   }
}
