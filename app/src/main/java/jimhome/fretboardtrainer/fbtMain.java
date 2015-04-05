package jimhome.fretboardtrainer;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import android.os.Handler;


public class fbtMain extends Activity implements View.OnClickListener {

    final double C4 = 261.6;
    final double D4 = 293.66;
    final double E4 = 329.63;
    final double F4 = 349.23;
    final double G4 = 392.0;
    final double A4 = 440.00;
    final double B4 = 493.88;
    final double C5 = 523.25;
    int last_note = 0;
    Random random = new Random();

    AudioTrack track = null;

    LinearLayout  bg;
    CheckBox cbxPlay;
    CheckBox cbxSkip;
    CheckBox cbxRandom;
    TextView tvNote;
    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (cbxPlay.isChecked()) {
                if (cbxRandom.isChecked()) {
                    last_note = random.nextInt(8);
                }
                else
                    if (cbxSkip.isChecked()) {
                        if (random.nextInt(2)==1) {
                            last_note = last_note + 1;
                        }
                    }
                if (last_note > 7)
                    last_note = 0;
                switch (last_note) {
                    case 0:
                        tvNote.setText("C");
                        bg.invalidate();
                        generateTone(C4, 1000);
                        last_note = 1;
                        break;
                    case 1:
                        tvNote.setText("D");
                        bg.invalidate();
                        generateTone(D4, 1000);
                        last_note = 2;
                        break;
                    case 2:
                        tvNote.setText("E");
                        bg.invalidate();
                        generateTone(E4, 1000);
                        last_note = 3;
                        break;
                    case 3:
                        tvNote.setText("F");
                        bg.invalidate();
                        last_note = 4;
                        generateTone(F4, 1000);
                        break;
                    case 4:
                        tvNote.setText("G");
                        bg.invalidate();
                        last_note = 5;
                        generateTone(G4, 1000);
                        break;
                    case 5:
                        tvNote.setText("A");
                        bg.invalidate();
                        last_note = 6;
                        generateTone(A4, 1000);
                        break;
                    case 6:
                        tvNote.setText("B");
                        bg.invalidate();
                        last_note = 7;
                        generateTone(B4, 1000);
                        break;
                    case 7:
                        tvNote.setText("C");
                        bg.invalidate();
                        last_note = 0;
                        generateTone(C5, 1000);
                        break;
                }
            }
            timerHandler.postDelayed(this, 2000); }
    };

    ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_SYSTEM,100);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fbt_main);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
       // requestWindowFeature(Window.FEATURE_NO_TITLE);//Hide title
       // this.getWindow().setFlags(WindowManager.LayoutParams.
       //         FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);//Hide Status bar
        bg = (LinearLayout)findViewById(R.id.bg);
        cbxPlay = (CheckBox)findViewById(R.id.cbxPlay);
        cbxPlay.setOnClickListener(this);
        cbxSkip = (CheckBox)findViewById(R.id.cbxSkip);
        cbxRandom = (CheckBox)findViewById(R.id.cbxRandom);
        cbxSkip.setOnClickListener(this);
        cbxRandom.setOnClickListener(this);
        tvNote  = (TextView)findViewById(R.id.tvNote);
        tvNote.setText("?");

        timerHandler.postDelayed(timerRunnable, 0);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cbxSkip:
                if (cbxSkip.isChecked()) cbxRandom.setChecked(false);
                break;
            case R.id.cbxRandom:
                if (cbxRandom.isChecked()) cbxSkip.setChecked(false);
                 break;
        }
    }


    private void generateTone(double freqHz, int durationMs)
    {
        int count = (int)(44100.0 * 2.0 * (durationMs / 1000.0)) & ~1;
        short[] samples = new short[count];
        for(int i = 0; i < count; i += 2){
            short sample = (short)(Math.sin(2 * Math.PI * i / (44100.0 / freqHz)) * 0x7FFF);
            samples[i + 0] = sample;
            samples[i + 1] = sample;
        }
        if (track != null) {
            track.release();
            track.flush();
            track = null;
        }
        track = new AudioTrack(AudioManager.STREAM_MUSIC, 44100,
                AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT,
                count * (Short.SIZE / 8), AudioTrack.MODE_STATIC); //MODE_STATIC
        track.write(samples, 0, count); //-12 is ENOMEM Not enough space
        track.play();
        /*
        track.setVolume(AudioTrack.getMaxVolume());
        track.play();
        try {
            Thread.sleep(durationMs+500);
        } catch (InterruptedException e) {
        }
        */
    }

}
