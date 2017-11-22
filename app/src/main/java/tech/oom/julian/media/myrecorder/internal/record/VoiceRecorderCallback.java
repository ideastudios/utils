package tech.oom.julian.media.myrecorder.internal.record;

public abstract interface VoiceRecorderCallback
{
  public abstract boolean onRecorderStart();
  
  public abstract boolean onRecorderReady();
  
  public abstract void onRecorderStop();
  
  public abstract void onRecorded(short[] wave);
  
  public abstract void onVoiceValue(int paramInt);
  
  public abstract void onRecordedFail(int paramInt);
}
