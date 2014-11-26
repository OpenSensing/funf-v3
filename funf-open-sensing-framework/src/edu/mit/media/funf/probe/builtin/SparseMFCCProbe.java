package edu.mit.media.funf.probe.builtin;

import java.util.Arrays;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import edu.mit.media.funf.FFT;
import edu.mit.media.funf.MFCC;
import edu.mit.media.funf.Window;
import edu.mit.media.funf.probe.Probe;
import edu.mit.media.funf.probe.builtin.ProbeKeys.AudioFeaturesKeys;

public class SparseMFCCProbe extends Probe implements AudioFeaturesKeys {
	
	private static int RECORDER_SOURCE = MediaRecorder.AudioSource.VOICE_RECOGNITION;
	private static int RECORDER_SAMPLERATE = 8000;
	private static int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
	private static int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
	
	private static int FFT_SIZE = 512;
	private static int MFCCS_VALUE = 12;
	private static int MEL_BANDS = 20;
	private static int WINDOW_SIZE = 30 * RECORDER_SAMPLERATE/1000;
	private static int HOP_SIZE = 10 * RECORDER_SAMPLERATE/1000;
	private static int SEGMENT_LENGTH = 6*WINDOW_SIZE;
	
	private Thread recordingThread = null;
	private int bufferSize = 0;
	
    private FFT featureFFT = null;
    private MFCC featureMFCC = null;
    private Window featureWin = null;
    
    private AudioRecord audioRecorder = null;
	
    public double prevSecs = 0;
	public double[] featureBuffer = null;
    
	@Override
	public Parameter[] getAvailableParameters() {
		return new Parameter[] {
				new Parameter(Parameter.Builtin.DURATION, 30L),
				new Parameter(Parameter.Builtin.PERIOD, 300L),
				new Parameter(Parameter.Builtin.START, 0L),
				new Parameter(Parameter.Builtin.END, 0L)
		};
	}

	@Override
	public String[] getRequiredFeatures() {
		return null;
	}

	@Override
	public String[] getRequiredPermissions() {
		return new String[] {
				android.Manifest.permission.RECORD_AUDIO
		};
	}

	@Override
	protected void onDisable() {
		if (null != audioRecorder)
	    {
	        audioRecorder.stop();
	        audioRecorder.release();
	        audioRecorder = null;
	        recordingThread = null;
	    }
	}

	@Override
	protected void onEnable() {
    	bufferSize = AudioRecord.getMinBufferSize(
        		RECORDER_SAMPLERATE,
        		RECORDER_CHANNELS,
        		RECORDER_AUDIO_ENCODING);

	    bufferSize = Math.max(bufferSize, RECORDER_SAMPLERATE);
	    featureFFT = new FFT(FFT_SIZE);
	    featureWin = new Window(WINDOW_SIZE);
	    featureMFCC = new MFCC(FFT_SIZE, MFCCS_VALUE, MEL_BANDS, RECORDER_SAMPLERATE);
	    
	    audioRecorder = new AudioRecord(
	    		RECORDER_SOURCE,
				RECORDER_SAMPLERATE,
				RECORDER_CHANNELS,
				RECORDER_AUDIO_ENCODING,
				bufferSize);
	}

	@Override
	protected void onRun(Bundle params) {
	    audioRecorder.startRecording();
	    recordingThread = new Thread(new Runnable()
	    {
	        @Override
	        public void run()
	        {
	            handleAudioStream();
	        }
	    }, "AudioRecorder Thread");
	    recordingThread.start();
	    
	    //writeLogTextLine("Audio recording started");
	}

	@Override
	protected void onStop() {
		audioRecorder.stop();
	}
	
	@Override
	public void sendProbeData() {
		// TODO Auto-generated method stub

	}

	private void handleAudioStream()
	{
        short data16bit[] = new short[SEGMENT_LENGTH];
    	double fftBufferR[] = new double[FFT_SIZE];
    	double fftBufferI[] = new double[FFT_SIZE];
    	double featureCepstrum[] = new double[(SEGMENT_LENGTH/HOP_SIZE) * (MFCCS_VALUE)];
    	double newMFCC[] = new double[MFCCS_VALUE];
    	
	    int readAudioSamples = 0;
	    while (isRunning())
	    {
	    	readAudioSamples = audioRecorder.read(data16bit, 0, SEGMENT_LENGTH);
	    	double currentSecs = (double)(System.currentTimeMillis())/1000.0d;
	    	
	    	Bundle data = new Bundle();
	    	if (readAudioSamples >= WINDOW_SIZE)
	    	{
	    		for(int n = 0; n < (readAudioSamples-WINDOW_SIZE)/HOP_SIZE; n++)
	    		{
		    		// Frequency analysis
		    		Arrays.fill(fftBufferR, 0);
		    		Arrays.fill(fftBufferI, 0);
	
		    		// Convert audio buffer to doubles
		    		for (int i = 0; i < WINDOW_SIZE; i++)
		    		{
		    			fftBufferR[i] = data16bit[i + n * HOP_SIZE];
		    		}
	
		    		// In-place windowing
		    		featureWin.applyWindow(fftBufferR);
	
		    		// In-place FFT
		    		featureFFT.fft(fftBufferR, fftBufferI);
	
		    		// Get MFCCs
		    		newMFCC = featureMFCC.cepstrum(fftBufferR, fftBufferI);
		    		//System.arraycopy(newMFCC, 1, featureCepstrum, n * (MFCCS_VALUE - 1), MFCCS_VALUE - 1);
		    		System.arraycopy(newMFCC, 0, featureCepstrum, n * MFCCS_VALUE, MFCCS_VALUE);
	    		}
	    		data.putDoubleArray(MFCCS, featureCepstrum);
	    		data.putInt("MFCC_len", MFCCS_VALUE);

	    		// Write out features
	    		sendProbeData((long)currentSecs, data);
	    		
	    		// Sleep for the rest of the second
	    		try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    	}
	    }
	}
}
