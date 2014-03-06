package main;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Resample
{
   private static final int             FRAMES_PER_BEAT = 37091-8114;//624769-585910;//21097 * 2;
   private static final int             SAMPLE_RATE     = 44100;
   private static final double          DURATION        = 60.0;
   private static final int             NUM_BEATS       = (int)((DURATION * SAMPLE_RATE) / FRAMES_PER_BEAT);

   private static ArrayList<double[][]> beats           = new ArrayList<>();
   private static int                   numChannels;

   private static STATE                 state           = STATE.forward;
   private static REPEAT                repeat          = REPEAT.one;
   private static int                   currentBeat     = 0;
   
   private static final String IN_FILE = "02.wav";
   private static final String OUT_FILE = "02-2.wav";

   private enum STATE
   {
      forward,
      backward,
      jump
   }

   private enum REPEAT
   {
      one,
      two,
      three
   }

   public static void main(String[] args) throws IOException, WavFileException
   {
      // Debug information
      System.out.println("Target number of frames: " + (int)(NUM_BEATS * FRAMES_PER_BEAT));
      System.out.println("Ideal number of frames : " + (int)(DURATION * SAMPLE_RATE));

      // Open file, determine num channels
      WavFile inFile = WavFile.openWavFile(new File(IN_FILE));
      numChannels = inFile.getNumChannels();

      double[][] buffer;

      // Read all of the file, and store it into an arraylist
      // for easy beat-based random-access later
      while (inFile.getFramesRemaining() > FRAMES_PER_BEAT)
      {
         buffer = new double[numChannels][FRAMES_PER_BEAT];
         inFile.readFrames(buffer, FRAMES_PER_BEAT);

         beats.add(buffer);
      }

      inFile.close();
      
      // initialize a beat to start at a random location
      currentBeat = (int)(Math.random() * beats.size());

      // Create the output file
      WavFile outFile = WavFile.newWavFile(new File(OUT_FILE), numChannels,
            (long) (NUM_BEATS * FRAMES_PER_BEAT), 16, SAMPLE_RATE);

      // Fill the output file
      for (int i = 0; i < NUM_BEATS; i++)
      {
         double[][] beat = null;

         switch (state)
         {
            // Nothing special. Increment our beat number
            case forward:
               beat = getForwardBeat(currentBeat);
               currentBeat++;
               if (currentBeat >= beats.size()) currentBeat = 0;
               break;

            // Reverse this beat. Decrement our beat number
            case backward:
               beat = getBackwardBeat(currentBeat);
               currentBeat--;
               if (currentBeat < 0) currentBeat = beats.size() - 1;
               break;

            // Jump to a new beat in the song
            // get new state and repeat count,
            // decrement our loop counter because we aren't writing this iteration
            case jump:
               currentBeat = (int) (Math.random() * beats.size());
               state = getState();
               repeat = getRepeat();
               i--;
               continue;
         }

         // If we have done our third repeat, get a new state
         switch (repeat)
         {
            case three:
               state = getState();
         }

         // Increments repeat, or randomizes if necessary
         repeat = getRepeat();

         // Write this beat to the file
         if (beat != null) outFile.writeFrames(beat, FRAMES_PER_BEAT);
      }

      outFile.close();
   }

   // Randomly select a new state to be in.
   // The backward state happens too often even at 5%
   private static STATE getState()
   {
      int rand = (int) (Math.random() * 100);

      if (rand < 5)
         return STATE.backward;
      else if (rand < 70)
         return STATE.forward;
      else
         return STATE.jump;
   }

   // Increments or randomizes the repeat enum
   private static REPEAT getRepeat()
   {
      switch (repeat)
      {
         case one:
            return REPEAT.two;
         case two:
            return REPEAT.three;
         case three:
            return Math.random() > 0.7 ? REPEAT.two : REPEAT.one;
      }

      return REPEAT.one;
   }

   // Do nothing special, just a wrapper to the array list
   private static double[][] getForwardBeat(int beat)
   {
      return beats.get(beat);
   }

   // Reverse a beat so we get backwards sound
   private static double[][] getBackwardBeat(int beat)
   {
      double[][] backward = new double[numChannels][FRAMES_PER_BEAT];
      double[][] forward = beats.get(beat);

      for (int i = 0; i < FRAMES_PER_BEAT; i++)
      {
         for (int j = 0; j < numChannels; j++)
         {
            backward[j][i] = forward[j][FRAMES_PER_BEAT - i - 1];
         }
      }

      return backward;
   }
}
