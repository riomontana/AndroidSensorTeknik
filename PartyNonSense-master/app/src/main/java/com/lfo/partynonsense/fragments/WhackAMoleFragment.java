package com.lfo.partynonsense.fragments;


import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.lfo.partynonsense.R;

import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * Wack-A-Mole Class
 * Uses the proximitysensor to sense "smashing" movement towards the device
 * in order for the player to whack the randomly appearing mole.
 *
 * @author Olle Olsson
 */

public class WhackAMoleFragment extends Fragment implements SensorEventListener {

    private static final int POINTS = 100;
    private static final int MINUS_POINT = 50;

    private SensorManager mSensorManager;
    private Animation mFallInAnim, mFallUpAnim, mFallOutDownAnim;
    private Sensor mProximitySensor;
    private GameWorker mGameWorker;
    private MediaPlayer mMediaPlayer;
    private Thread animation;

    private int pointCounter = 0;
    private int rounds = 0;
    private int repeat = 12;
    private boolean moleIsVisible = false;
    private boolean running = true;

    private TextView tvPoints;
    private TextView tvFinalScore;
    private ImageView ivMole;
    private ImageView ivFinalScore;
    private Button btnStart;

    private int[] moleDamage = new int[]{
            R.drawable.moleup,
            R.drawable.moleup2,
            R.drawable.moleup3,
            R.drawable.moleup4,
            R.drawable.moleup5};

    public WhackAMoleFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_whack_amole, container, false);

        //Initializes the components used in the application.

        tvPoints = (TextView) view.findViewById(R.id.tvPoints);
        tvFinalScore = (TextView) view.findViewById(R.id.tvFinalScore);
        tvPoints.setText(String.valueOf(pointCounter));
        ivMole = (ImageView) view.findViewById(R.id.ivMole);
        ivFinalScore = (ImageView) view.findViewById(R.id.ivFinalScore);
        ivFinalScore.setVisibility(View.INVISIBLE);
        btnStart = (Button) view.findViewById(R.id.btnStart);

        //Registers sensorListener for the proximity sensor.

        mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        mProximitySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        if (mProximitySensor != null) {
            mSensorManager.registerListener(this, mProximitySensor, SensorManager.SENSOR_DELAY_NORMAL);

        } else {
            Toast.makeText(getActivity(), "Proximitysensor not avaiable", Toast.LENGTH_LONG).show();
        }
        createGameInfoAlertDialog();
        return view;

    }

    /**
     * Create a new alert dialog
     */
    public void createGameInfoAlertDialog() {
        GameInfoAlertDialogFragment gameInfoDialog = new GameInfoAlertDialogFragment();
        gameInfoDialog.setTitle("Whack A Mole");
        gameInfoDialog.setText(getResources().getString(R.string.whack_a_mole));
//        gameInfoDialog.setImageResource(R.drawable.how_to_volumegame);
        gameInfoDialog.show(getActivity().getFragmentManager(), "Whack A Mole");
    }

    /**
     * Create a new alert dialog when the game stops
     */
    public void createGameStopAlertDialog() {
        GameStopAlertDialog gameStopAlertDialog = new GameStopAlertDialog();
        gameStopAlertDialog.setTitle("Whack A Mole");
        gameStopAlertDialog.setScore(pointCounter);
        gameStopAlertDialog.show(getActivity().getFragmentManager(), "Whack A Mole");
    }

    /**
     * Overriden methods for sensorevents.
     * Inside the method the player gets points if the mole is visible and the eventlistener is 0.
     * When the proximitysensor is 0, it means that a 'hit' motion has been made by the player.
     * Hit plays a punch-like sound, and miss plays a miss-like sound.
     *
     * @param event
     */

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.values[0] == 0 && moleIsVisible == true) {
            pointCounter = pointCounter + POINTS;
            tvPoints.setText(String.valueOf(pointCounter));
            playWhackSound(event, true);

        } else if (event.values[0] == 0 && moleIsVisible == false) {
            pointCounter = pointCounter - MINUS_POINT;
            if (pointCounter < 0) pointCounter = 0;
            tvPoints.setText(String.valueOf(pointCounter));
            playWhackSound(event, false);

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    /**
     * Method that handles the soundsamples played when the mole is hit or missed.
     *
     * @param event   - from sensor
     * @param moleHit - if the mole was hit or not
     */

    public void playWhackSound(SensorEvent event, boolean moleHit) {
        if (event != null && moleHit == true) {
            mMediaPlayer = MediaPlayer.create(getActivity(), R.raw.whack);
            mMediaPlayer.start();

        } else if (event != null && moleHit == false) {
            mMediaPlayer = MediaPlayer.create(getActivity(), R.raw.miss);
            mMediaPlayer.start();

        }
    }

    /**
     * onPause unregisteres the listener for this class.
     */

    @Override
    public void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);

    }

    /**
     * onDestroy sets the sensormanager and the sensor to null.
     */

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSensorManager = null;
        mProximitySensor = null;

    }

    /**
     * onResume register the listener.
     */

    @Override
    public void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mProximitySensor, SensorManager.SENSOR_DELAY_NORMAL);

    }

    /**
     * Method that is called that starts the game from MainActivity.
     */

    public void start() {
        btnStart.setAnimation(mFallOutDownAnim);
        btnStart.setVisibility(View.INVISIBLE);
        mGameWorker = new GameWorker();
        animation = new Thread(mGameWorker);
        animation.start();

    }

    /**
     * Method that is called to stop the game from MainActivity.
     */


    public void stop() {
        mGameWorker = null;
        running = false;
        animation.interrupt();
        createGameStopAlertDialog();
        mSensorManager.unregisterListener(this);

    }

    /**
     * Returns the total scoring from the gameround to MainActivity.
     *
     * @return - pointcounter
     */


    public int getScore() {
        return pointCounter;

    }

    /**
     * Inner Runnable-class that handles the "animation" for the mole and sets the desired image
     * based on the players score. The pause method pauses the GameWorker (not main thread) in a
     * random time-frame to make the mole unpredictable.
     *
     * @author Olle Olsson
     */

    private class GameWorker implements Runnable {

        Random rand = new Random();

        @Override
        public void run() {
            while (running) {
                Log.d("WhackAmoleFragment", "run: round = " + rounds);

                pause(rand.nextInt(500) + 500);

                if (pointCounter < 5 * POINTS) {
                    setImageRes(moleDamage[0]);
                    moleIsVisible = true;

                } else if (pointCounter < 7 * POINTS) {
                    setImageRes(moleDamage[1]);
                    moleIsVisible = true;

                } else if (pointCounter < 10 * POINTS) {
                    setImageRes(moleDamage[2]);
                    moleIsVisible = true;

                } else if (pointCounter < 12 * POINTS) {
                    setImageRes(moleDamage[3]);
                    moleIsVisible = true;

                } else if (pointCounter > 12 * POINTS) {
                    setImageRes(moleDamage[4]);
                    moleIsVisible = true;
                }

                pause(rand.nextInt(500) + 200);

                moleIsVisible = false;
                setImageRes(R.drawable.moledown);

                pause(rand.nextInt(1000) + 500);

                rounds++;

            }
            rounds = 0;
        }

        /**
         * Method that changes the image in the imageView on the UiThread.
         *
         * @param res
         */

        public void setImageRes(final int res) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ivMole.setImageResource(res);

                }
            });
        }

        /**
         * Method that pauses the gameWorker-thread for a random period of time.
         *
         * @param milliseconds - random interval
         */

        public void pause(int milliseconds) {
            try {
                Thread.sleep(milliseconds);
            } catch (InterruptedException e) {

            }
        }
    }
}
