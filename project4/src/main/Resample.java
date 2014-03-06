package main;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Resample
{
   private static final int             NUM_BEATS       = 250;
   private static final int             FRAMES_PER_BEAT = 21097 * 2;
   private static final int             SAMPLE_RATE     = 44100;
   private static final double          DURATION        = 60.0;

   private static ArrayList<double[][]> beats           = new ArrayList<>();
   private static int                   numChannels;

   private static STATE                 state           = STATE.forward;
   private static REPEAT                repeat          = REPEAT.one;
   private static int                   currentBeat     = 0;
   
   private static final String IN_FILE = "01.wav";
   private static final String OUT_FILE = "01-2.wav";

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

      WavFile inFile = WavFile.openWavFile(new File(IN_FILE));
      numChannels = inFile.getNumChannels();

      double[][] buffer;

      while (inFile.getFramesRemaining() > FRAMES_PER_BEAT)
      {
         buffer = new double[numChannels][FRAMES_PER_BEAT];
         inFile.readFrames(buffer, FRAMES_PER_BEAT);

         beats.add(buffer);
      }

      inFile.close();

      WavFile outFile = WavFile.newWavFile(new File(OUT_FILE), numChannels,
            (long) (DURATION * SAMPLE_RATE), 16, SAMPLE_RATE);

      for (int i = 0; i < NUM_BEATS; i++)
      {
         double[][] beat = null;

         switch (state)
         {
            case forward:
               beat = getForwardBeat(currentBeat);
               currentBeat++;
               if (currentBeat >= beats.size()) currentBeat = 0;
               break;

            case backward:
               beat = getBackwardBeat(currentBeat);
               currentBeat--;
               if (currentBeat < 0) currentBeat = beats.size() - 1;
               break;

            case jump:
               currentBeat = (int) (Math.random() * beats.size());
               state = getState();
               repeat = getRepeat();
               continue;
         }

         switch (repeat)
         {
            case three:
               state = getState();
         }

         repeat = getRepeat();

         if (beat != null) outFile.writeFrames(beat, FRAMES_PER_BEAT);
      }

      outFile.close();
   }

   private static STATE getState()
   {
      int rand = (int) (Math.random() * 10);

      switch (rand)
      {
         case 0:
         case 1:
            return STATE.backward;
         case 2:
         case 3:
         case 4:
            return STATE.forward;
         case 5:
         case 6:
         case 7:
         case 8:
         case 9:
            return STATE.jump;
      }

      return STATE.forward;
   }

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

   private static double[][] getForwardBeat(int beat)
   {
      return beats.get(beat);
   }

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
