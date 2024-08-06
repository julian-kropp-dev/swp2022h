package de.uol.swp.client.audio;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** The PlaySound class allows playing sound effects. */
public class PlaySound {
  private static final Logger LOG = LogManager.getLogger(PlaySound.class);
  private FloatControl gainControl;
  private static List<Clip> playedClips = new ArrayList<>();

  public static void stopAll() {
    playedClips.forEach(DataLine::stop);
    playedClips = new ArrayList<>();
  }

  /**
   * Plays the sound effect at the specified file path.
   *
   * @param filepath the file path of the sound effect
   * @throws RuntimeException if there is an error playing the sound effect
   */
  @SuppressWarnings("DataFlowIssue")
  public void playSound(String filepath, boolean loop) {
    try (InputStream audioSrc = getClass().getResourceAsStream(filepath);
        InputStream bufferedIn = new BufferedInputStream(audioSrc);
        AudioInputStream audioStream = AudioSystem.getAudioInputStream(bufferedIn)) {
      if (audioStream != null) {
        Clip clip = AudioSystem.getClip();
        playedClips.add(clip);
        clip.open(audioStream);
        gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        if (loop) {
          clip.loop(Clip.LOOP_CONTINUOUSLY);
        }
        clip.start();
      } else {
        LOG.error("Could not play sound. Is Null");
      }
    } catch (UnsupportedAudioFileException
        | LineUnavailableException
        | IOException
        | NullPointerException e) {
      LOG.error("Could not play sound. ERROR: {}", e.toString());
    }
  }

  /**
   * Sets the volume of the sound player.
   *
   * @param volume the volume level (between 0.0 and 1.0)
   */
  public void setVolume(float volume) {
    if (gainControl != null) {
      float gain = volumeToGain(volume);
      gainControl.setValue(gain);
    }
  }

  /**
   * Converts a volume level (between 0.0 and 1.0) to a gain value (-80.0 to 6.0206).
   *
   * @param volume the volume level
   * @return the corresponding gain value
   */
  private float volumeToGain(float volume) {
    if (volume <= 0.0) {
      return -80.0f; // Minimum volume (mute)
    } else {
      return (float) (Math.log10(volume) * 20.0);
    }
  }
}
