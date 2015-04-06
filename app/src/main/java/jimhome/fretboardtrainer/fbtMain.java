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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import android.os.Handler;


public class fbtMain extends Activity implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    //http://www.phy.mtu.edu/~suits/notefreqs.html
    final double C4 = 261.6;
    final double D4 = 293.66;
    final double E4 = 329.63;
    final double F4 = 349.23;
    final double G4 = 392.0;
    final double A4 = 440.00;
    final double B4 = 493.88;
    final double C5 = 523.25;
    final double D5 = 587.33;
    final double E5 = 659.25;
    final double F5 = 698.46;
    final double G5 = 783.99;
    final double A5 = 880.00;
    final double B5 = 987.77;
    final double C6 = 1046.50;

    int NOTE_DURATION = 1000;

    String[] speeds = {"20 bpm","30 bpm", "40 bpm", "50 bpm", "60 bpm", "70 bpm", "80 bpm"};
    String[] scales = {"C Major", "C Harmonic (not implemented)", "C Melodic (not implemented)"};

    Random random = new Random();

    AudioTrack track = null;
    NoteList noteList;
    LinearLayout  bg;
    CheckBox cbxPlay;
    CheckBox cbxSkip;
    CheckBox cbxRandom;
    TextView tvNote;
    Spinner spinner;
    Spinner spinner_scales;
    Handler timerHandler = new Handler();

    public class Note{
       public double frequency = 0;
       public String name;
       public Note(double _frequency, String _name) {
           frequency = _frequency;
           name = _name;
       }
    }

    public class NoteList {
        final int LAST_NOTE = 14;
        Note note;
        int ItemIndex = 0;
        boolean going_up = true;
        boolean going_down = true;
        char current_direction = 'u';
        public ArrayList<Note> noteList = new ArrayList<Note>();
        public NoteList() {
            note = new Note(C4,"C"); noteList.add(note);
            note = new Note(D4,"D"); noteList.add(note);
            note = new Note(E4,"E"); noteList.add(note);
            note = new Note(F4,"F"); noteList.add(note);
            note = new Note(G4,"G"); noteList.add(note);
            note = new Note(A4,"A"); noteList.add(note);
            note = new Note(B4,"B"); noteList.add(note);
            note = new Note(C5,"C"); noteList.add(note);
            note = new Note(D5,"D"); noteList.add(note);
            note = new Note(E5,"E"); noteList.add(note);
            note = new Note(F5,"F"); noteList.add(note);
            note = new Note(G5,"G"); noteList.add(note);
            note = new Note(A5,"A"); noteList.add(note);
            note = new Note(B5,"B"); noteList.add(note);
            note = new Note(C6,"C"); noteList.add(note);
        }

        public Note get_current_note() {
            Note result = null;
            if (ItemIndex > LAST_NOTE)
                ItemIndex = LAST_NOTE;
            if (ItemIndex < 0)
                ItemIndex = 0;
            result = (Note) noteList.get(ItemIndex);
            return result;
        }

        public Note get_note(int _idx) {
            Note result = null;
            if (_idx > LAST_NOTE)
                _idx = LAST_NOTE;
            if (_idx < 0)
                _idx = 0;
            result = (Note) noteList.get(_idx);
            return result;
        }

        public Note move_next()  {
           Note result = null;
           if (current_direction == 'u')  {
               if (ItemIndex < LAST_NOTE) {
                   ItemIndex++;
                   result = (Note) noteList.get(ItemIndex);
                   return result;
               }
               else {
                   current_direction = 'd';
                   ItemIndex--;
                   result = (Note) noteList.get(ItemIndex);
                   return result;
               }
           }
            if (current_direction == 'd')  {
                if (ItemIndex > 0) {
                    ItemIndex--;
                    result = (Note) noteList.get(ItemIndex);
                    return result;
                }
                else {
                    current_direction = 'u';
                    ItemIndex++;
                    result = (Note) noteList.get(ItemIndex);
                    return result;
                }
            }
           result = (Note) noteList.get(ItemIndex);
           return result;
        }
    }

    Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            Note note;
            if (cbxPlay.isChecked()) {
                note  = noteList.get_current_note();
                if (cbxRandom.isChecked()) {
                    note  = noteList.get_note(random.nextInt(noteList.LAST_NOTE+1));
                }
                else
                    if (cbxSkip.isChecked()) {
                        if (random.nextInt(2)==1) {
                            noteList.move_next();
                        }
                    }
                tvNote.setText(note.name);
                bg.invalidate();
                generateTone(note.frequency, NOTE_DURATION);
                noteList.move_next();
            }
            timerHandler.postDelayed(this, NOTE_DURATION); }
    };

    ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_SYSTEM,100);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        this.getWindow().setFlags(WindowManager.LayoutParams.
                FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);//Hide Status bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);//Hide title
        setContentView(R.layout.activity_fbt_main);
        bg = (LinearLayout)findViewById(R.id.bg);
        cbxPlay = (CheckBox)findViewById(R.id.cbxPlay);
        cbxPlay.setOnClickListener(this);
        cbxSkip = (CheckBox)findViewById(R.id.cbxSkip);
        cbxRandom = (CheckBox)findViewById(R.id.cbxRandom);
        cbxSkip.setOnClickListener(this);
        cbxRandom.setOnClickListener(this);
        tvNote  = (TextView)findViewById(R.id.tvNote);
        tvNote.setText("?");
        spinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, speeds);
        ArrayAdapter<String> adapter_scales = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, scales);
        spinner = (Spinner) findViewById(R.id.spinner);
        spinner_scales = (Spinner) findViewById(R.id.spinner_scales);
        spinner.setAdapter(adapter);
        spinner.setSelection(4);//60pb
        spinner.setOnItemSelectedListener(this);
        spinner_scales.setAdapter(adapter_scales);
        spinner_scales.setSelection(0);//C Major
        noteList = new NoteList();
        timerHandler.postDelayed(timerRunnable, 0);
    }

    @Override
    public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
                               long arg3) {
        int index = arg0.getSelectedItemPosition();
        //String[] speeds = {"40 bpm", "50 bpm", "60 bpm", "70 bpm", "80 bpm"};
        //http://tomhess.net/Tools/DelayCalculator.aspx
        switch (index){
            case(0): //20 bpm    1000/(60
                NOTE_DURATION = 3000;
                break;
            case(1): //30 bpm
                NOTE_DURATION = 2000;
                break;
            case(2): //40 bpm    1000/(60
                NOTE_DURATION = 1500;
                break;
            case(3): //50 bpm
                NOTE_DURATION = 1200;
                break;
            case(4): //60 bpm
                NOTE_DURATION = 1000;
                break;
            case(5): //70 bpm
                NOTE_DURATION = 857;
                break;
            case(6): //80 bpm
                NOTE_DURATION = 750;
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub

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
