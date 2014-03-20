package main;

import static gui.MixStateGUI.*;
import gui.StateMachinePanel;

import java.io.IOException;
import java.util.ArrayList;

public class StateMachine
{
   public interface StateEnum
   {
      public String getLabel();
   }

   //@formatter:off
   public enum SongState implements StateEnum
   {
      SONG_1("Using Song 1"),
      SONG_2("Using Song 2"),
      ;

      private String e_Label;

      SongState(String label) { e_Label = label; }

      @Override
      public String getLabel() { return e_Label; }
   }

   public enum RemixState implements StateEnum
   {
      PLAY("Standard Play"),
      JUMP("Jumping"),
      //SCRATCH("Scratch"),
      REVERSE("Reverse"),
      ;

      private String e_Label;

      RemixState(String label) { e_Label = label; }

      @Override
      public String getLabel() { return e_Label; }

   }

   public enum RepeatState implements StateEnum
   {
      NONE("None"),
      BEAT("Beats"),
      MEASURE("Measures"),
      DOUBLE_MEASURE("2x Measures"),
      //QUAD_MEASURE("4x Measures"),
      ;

      private String e_Label;

      RepeatState(String label) { e_Label = label; }

      @Override
      public String getLabel() { return e_Label; }
   }
   // @formatter:on

   private class Beat
   {
      double[][] m_BeatData;
      double     m_BeatVolume;

      Beat(double[][] beat)
      {
         m_BeatData = beat;

         for (int i = 0; i < m_BeatData.length; i++)
         {
            for (int j = 0; j < m_BeatData[0].length; j++)
            {
               m_BeatVolume += Math.pow(m_BeatData[i][j], 2);
            }
         }

         m_BeatVolume /= (m_BeatData.length * m_BeatData[0].length);
         m_BeatVolume = Math.pow(m_BeatVolume, 2);
      }

      public double[][] getBeat()
      {
         return m_BeatData;
      }

      public double getVolume()
      {
         return m_BeatVolume;
      }
   }

   private static StateMachinePanel m_StatePanel;

   private static int               DURATION                = 120 * 44100;
   private static Beat              LAST_BEAT;

   private static SongState         SONG_STATE;
   private static RemixState        REMIX_STATE;
   private static RepeatState       REPEAT_STATE;
   private static int               REPEAT_COUNTER;

   private static ArrayList<Beat>   m_Song1Beats;
   private static ArrayList<Beat>   m_Song2Beats;
   private static ArrayList<Beat>   CURRENT_SOURCE;

   private static int               SONG_INDEX;

   private static ArrayList<Beat>   OUTPUT_BEATS;
   private static int               BEATS_SINCE_REPEAT      = 0;
   private static int               BEATS_SINCE_SONG_CHANGE = 0;

   /**
    * This is where the real magic happens
    * 
    * @throws WavFileException
    * @throws IOException
    */
   public StateMachine(StateMachinePanel stateMachinePanel) throws IOException,
         WavFileException
   {
      m_StatePanel = stateMachinePanel;

      initMachine();

      magicLoop();
   }

   private int getDelay()
   {
      return m_StatePanel.getDelay();
   }

   /**
    * Turns a WavFile into an array list of beats
    * 
    * @param song
    *           WavFile to parse
    * @param beats
    *           ArrayList to put the beats in
    * @param startFrame
    *           First frame of the first beat
    * @param endFrame
    *           Last frame of the first beat
    * @throws IOException
    * @throws WavFileException
    */
   private void parseSong(WavFile song, ArrayList<Beat> beats, int startFrame,
         int endFrame) throws IOException, WavFileException
   {
      // Metadata
      int numChannels = song.getNumChannels();
      int beatSize = endFrame - startFrame;

      // Throw away everything before the first beat
      double[][] buffer = new double[numChannels][startFrame];
      song.readFrames(buffer, startFrame);

      // Parse the song into beats (this is our discrete unit)
      while (song.getFramesRemaining() > beatSize)
      {
         buffer = new double[numChannels][beatSize];
         song.readFrames(buffer, beatSize);

         beats.add(new Beat(buffer));
      }

      song.close();
   }

   /**
    * Initializes everything the state machine needs to operate
    * 
    * @throws IOException
    * @throws WavFileException
    */
   private void initMachine() throws IOException, WavFileException
   {
      if (m_Song2 != null)
      {
         SONG_STATE = SongState.SONG_2;
         m_Song2Beats = new ArrayList<Beat>();
         parseSong(m_Song2, m_Song2Beats, m_Song2Frame1, m_Song2Frame2);
         CURRENT_SOURCE = m_Song2Beats;
      }

      if (m_Song1 != null)
      {
         SONG_STATE = SongState.SONG_1;
         m_Song1Beats = new ArrayList<Beat>();
         parseSong(m_Song1, m_Song1Beats, m_Song1Frame1, m_Song1Frame2);
         CURRENT_SOURCE = m_Song1Beats;
      }

      updateStates();

      while (REMIX_STATE == RemixState.JUMP)
         updateRemixState();
      
      OUTPUT_BEATS = new ArrayList<Beat>();
   }

   private void updateStates()
   {
      updateSongState();
      updateRepeatState();
      updateRemixState();
      updateRepeatCounter();
   }

   private void magicLoop() throws IOException, WavFileException
   {
      while (true)
      {
         try
         {
            Thread.sleep(getDelay());
         }
         catch (InterruptedException e)
         {
            // Do nothing
         }

         if (REPEAT_COUNTER == 0)
         {
            if (BEATS_SINCE_REPEAT > 16)
            ;
            doRepeat();

            System.out.println("-------------------------------------");
            updateStates();
         }
         else
         {
            // We've mixed enough, let's save it and quit.
            if (DURATION <= 0) break;

            // Find a new place to use
            if (REMIX_STATE == RemixState.JUMP)
            {
               findBeatByVolume();

               // Find a new remix state
               while (REMIX_STATE == RemixState.JUMP)
                  updateRemixState();
            }
            else
            {
               // Add a beat
               LAST_BEAT = CURRENT_SOURCE.get(SONG_INDEX);
               OUTPUT_BEATS.add(processBeat());
               LAST_BEAT = OUTPUT_BEATS.get(OUTPUT_BEATS.size() - 1);
               
               // decrement our remaining beats
               DURATION -= LAST_BEAT.getBeat()[0].length;

               if (REMIX_STATE == RemixState.PLAY) SONG_INDEX++;
               else if (REMIX_STATE == RemixState.REVERSE) SONG_INDEX--;

               if (SONG_INDEX < 0) SONG_INDEX = CURRENT_SOURCE.size() - 1;
               if (SONG_INDEX >= CURRENT_SOURCE.size()) SONG_INDEX = 0;

               REPEAT_COUNTER--;
               BEATS_SINCE_REPEAT++;
               BEATS_SINCE_SONG_CHANGE++;
            }

         }
      }

      WavFile output = WavFile.newWavFile(m_SaveSong, getNumChannels(),
            getNumFrames(), getBitWidth(), 44100);

      for (Beat beat : OUTPUT_BEATS)
         output.writeFrames(beat.getBeat(), beat.getBeat()[0].length);

      output.close();

      m_StatePanel.setRemixState(null);
      m_StatePanel.setRepeatState(null);
      m_StatePanel.setSongState(null);
   }
   
   /**
    * Reveses a beat, if necessary
    * @return
    */
   private Beat processBeat()
   {
      if (REMIX_STATE != RemixState.REVERSE)
         return LAST_BEAT;
      
      double[][] oldBeat = LAST_BEAT.getBeat();
      double[][] newBeat = new double[oldBeat.length][oldBeat[0].length];
      
      System.out.println("double[" + oldBeat.length + "][" + oldBeat[0].length + "]");
      for (int i = 0; i < oldBeat[0].length; i++)
      {
         for (int j = 0; j < oldBeat.length; j++)
         {
            newBeat[j][i] = oldBeat[oldBeat.length - 1 - j][oldBeat[0].length - 1 - i];
         }
      }
      
      return new Beat(newBeat);
   }

   /**
    * Returns the number of channels for the song with fewer channels (to avoid index out of bounds)
    * @return
    */
   private int getNumChannels()
   {
      int s1 = m_Song1 != null ? m_Song1.getNumChannels() : 0;
      int s2 = m_Song2 != null ? m_Song2.getNumChannels() : 0;

      return s1 < s2 ? s1 : s2;
   }

   /**
    * Sums the number of frames used for the output song
    * @return
    */
   private long getNumFrames()
   {
      long numFrames = 0;

      for (Beat beat : OUTPUT_BEATS)
      {
         numFrames += beat.getBeat()[0].length;
      }

      return numFrames;
   }

   /**
    * Returns the bit width of the song with the higher bit width
    * @return
    */
   private int getBitWidth()
   {
      int s1 = m_Song1 != null ? m_Song1.getValidBits() : 0;
      int s2 = m_Song2 != null ? m_Song2.getValidBits() : 0;

      return s1 < s2 ? s2 : s1;
   }

   /**
    * Repeats the last X beats of the output
    */
   private void doRepeat()
   {
      // Repeat the previous 1, 4, 8, 16 beats
      switch (REPEAT_STATE)
      {
         case BEAT:
            if (OUTPUT_BEATS.size() >= 1)
            {
               LAST_BEAT = OUTPUT_BEATS.get(OUTPUT_BEATS.size() - 1);
               OUTPUT_BEATS.add(LAST_BEAT);
               DURATION -= LAST_BEAT.getBeat()[0].length;
            }
            break;
         case MEASURE:
            if (OUTPUT_BEATS.size() >= 4) for (int i = 4; i > 0; i--)
            {
               LAST_BEAT = OUTPUT_BEATS.get(OUTPUT_BEATS.size() - i);
               OUTPUT_BEATS.add(LAST_BEAT);
               DURATION -= LAST_BEAT.getBeat()[0].length;
            }
            break;
         case DOUBLE_MEASURE:
            if (OUTPUT_BEATS.size() >= 8) for (int i = 8; i > 0; i--)
            {
               LAST_BEAT = OUTPUT_BEATS.get(OUTPUT_BEATS.size() - i);
               OUTPUT_BEATS.add(LAST_BEAT);
               DURATION -= LAST_BEAT.getBeat()[0].length;
            }
            break;
      // case QUAD_MEASURE:
      // for (int i = 16; i > 0; i--)
      // {
      // LAST_BEAT = OUTPUT_BEATS.get(OUTPUT_BEATS.size() - i);
      // OUTPUT_BEATS.add(LAST_BEAT);
      // DURATION -= LAST_BEAT.getBeat()[0].length;
      // }
      // break;
      }
      BEATS_SINCE_REPEAT = 0;
   }

   private void findBeatByVolume()
   {
      double threshold = 0.2;
      
      while (true)
      {
         // Check random parts of the song for an acceptable volume section
         int index = (int) (Math.random() * CURRENT_SOURCE.size());
         double relativeVolume = LAST_BEAT.getVolume()
               / CURRENT_SOURCE.get(index).getVolume();

         // If they are within 30% of each other, use that location
         if (relativeVolume < 1 + threshold && relativeVolume > 1 - threshold)
         {
            SONG_INDEX = index;
            return;
         }
         
         threshold += 0.1;
      }
   }

   private void updateSongState()
   {
      // Only switch songs if we have two songs.
      if (m_Song1 == null || m_Song2 == null) return;

      int rand = (int) (Math.random() * 10.0);

      // Simple 30% chance to switch to the other song
      if (rand < 3 && BEATS_SINCE_SONG_CHANGE > 24)
      {
         switch (SONG_STATE)
         {
            case SONG_1:
               SONG_STATE = SongState.SONG_2;
               m_StatePanel.setSongState(SONG_STATE);
               CURRENT_SOURCE = m_Song1Beats;
               System.out.println("Update - Song State: "
                     + SONG_STATE.getLabel());
               break;
            case SONG_2:
               SONG_STATE = SongState.SONG_1;
               m_StatePanel.setSongState(SONG_STATE);
               CURRENT_SOURCE = m_Song2Beats;
               System.out.println("Update - Song State: "
                     + SONG_STATE.getLabel());
               break;
         }
         BEATS_SINCE_SONG_CHANGE = 0;
      }
   }

   /**
    * Decides on how many beats to repeat when a repeat action occurs
    */
   private void updateRepeatState()
   {
      int rand = (int) (Math.random() * 16.0);

      if (rand < 16) REPEAT_STATE = RepeatState.NONE;
      if (rand < 12) REPEAT_STATE = RepeatState.BEAT;
      if (rand < 7) REPEAT_STATE = RepeatState.MEASURE;
      if (rand < 2) REPEAT_STATE = RepeatState.DOUBLE_MEASURE;

      System.out.println("Update - Repeat State: " + REPEAT_STATE.getLabel());

      m_StatePanel.setRepeatState(REPEAT_STATE);
   }

   /**
    * Decides on which play state to use
    */
   private void updateRemixState()
   {
      int rand = (int) (Math.random() * 16.0);

      if (rand < 16) REMIX_STATE = RemixState.PLAY;
      if (rand < 12) REMIX_STATE = RemixState.JUMP;
      if (rand < 9) REMIX_STATE = RemixState.PLAY;
      if (rand < 1) REMIX_STATE = RemixState.REVERSE;

      System.out.println("Update - Remix State: " + REMIX_STATE.getLabel());

      m_StatePanel.setRemixState(REMIX_STATE);

   }
   
   /**
    * Decides on how many times to repeat the current state
    */
   private void updateRepeatCounter()
   {
      int rand = (int) (Math.random() * 10.0);

      if (rand < 10) REPEAT_COUNTER = 8;
      if (rand < 6) REPEAT_COUNTER = 7;
      if (rand < 3) REPEAT_COUNTER = 6;
      if (rand < 1) REPEAT_COUNTER = 4;

      System.out.println("Update - Repeat Counter: " + REPEAT_COUNTER);
   }
}
