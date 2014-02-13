/*
  ==============================================================================

    This file was auto-generated!

    It contains the basic startup code for a Juce application.

  ==============================================================================
*/

#ifndef PLUGINPROCESSOR_H_INCLUDED
#define PLUGINPROCESSOR_H_INCLUDED

#include "../JuceLibraryCode/JuceHeader.h"


//==============================================================================
/**
*/
class Project3AudioProcessor  : public AudioProcessor
{
public:
    //==============================================================================
    Project3AudioProcessor();
    ~Project3AudioProcessor();

    //==============================================================================
    void prepareToPlay (double sampleRate, int samplesPerBlock);
    void releaseResources();

    void processBlock (AudioSampleBuffer& buffer, MidiBuffer& midiMessages);

    //==============================================================================
    AudioProcessorEditor* createEditor();
    bool hasEditor() const;

    //==============================================================================
    const String getName() const;

    int getNumParameters();

    float getParameter (int index);
    void setParameter (int index, float newValue);

    const String getParameterName (int index);
    const String getParameterText (int index);

	float Project3AudioProcessor::getParameterDefaultValue(int index);

    const String getInputChannelName (int channelIndex) const;
    const String getOutputChannelName (int channelIndex) const;
    bool isInputChannelStereoPair (int index) const;
    bool isOutputChannelStereoPair (int index) const;

    bool acceptsMidi() const;
    bool producesMidi() const;
    bool silenceInProducesSilenceOut() const;
    double getTailLengthSeconds() const;

    //==============================================================================
    int getNumPrograms();
    int getCurrentProgram();
    void setCurrentProgram (int index);
    const String getProgramName (int index);
    void changeProgramName (int index, const String& newName);

    //==============================================================================
    void getStateInformation (MemoryBlock& destData);
    void setStateInformation (const void* data, int sizeInBytes);

	enum Parameters
	{
		frequency = 0,
		preDis,
		postDis,

		totalNumParams
	};

	//Allow Plugin processor to signal UI updates
	bool NeedsUIUpdate(){ return UIUpdateFlag; }
	void RequestUIUpdate() { UIUpdateFlag = true; }
	void ClearUIUpdateFlag() { UIUpdateFlag = false; }

private:
    //==============================================================================
	float UserParams[totalNumParams];
	float DefaultParams[totalNumParams];
	bool UIUpdateFlag;
	float coefficients[5];
	float v1, v2;
	bool newFreqFlag;
	int frame;

    JUCE_DECLARE_NON_COPYABLE_WITH_LEAK_DETECTOR (Project3AudioProcessor)
};

#endif  // PLUGINPROCESSOR_H_INCLUDED
