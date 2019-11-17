package com.lfo.partynonsense;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.support.constraint.ConstraintLayout;
import android.text.Layout;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Random;

/**
 * Created by Alexander on 2018-02-22.
 */

public class BallGameSensorListener implements SensorEventListener {
    private ImageView playerIv;
    private ImageView goalIv;
    private TextView scoreTv;
    private TextView multiTv;
    private int scoreMulti;
    private float xVel;
    private float yVel;
    private float goalX;
    private float goalY;
    private int currentGoal;
    private int score;
    private ConstraintLayout layout;
    public BallGameSensorListener(ImageView playerIv, ImageView goalIv, ConstraintLayout layout, TextView scoreTV, TextView multiTv){
        this.playerIv = playerIv;
        this.goalIv = goalIv;
        this.layout = layout;
        this.scoreTv = scoreTV;
        this.multiTv = multiTv;
        scoreMulti = 1;
        xVel = 0;
        yVel = 0;
        score = 0;
        goalX = 200;
        goalY = 200;
        scoreTv.setText(score + "p");
        multiTv.setText(scoreMulti + "X");
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        xVel = xVel + event.values[0];
        yVel = yVel + event.values[1];
        moveBall(xVel, yVel);


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
    private void moveBall(float x, float y){
        float newXPos = playerIv.getX() - x/2;
        float newYPos = playerIv.getY () + y/2;
        if(newXPos > 0 && newXPos < layout.getWidth() - playerIv.getWidth()){
            playerIv.setX(newXPos);
        }else{
            scoreMulti = 1;
            multiTv.setText(scoreMulti+"X");
            xVel = (xVel * -1) / 2;
        }
        if(newYPos > 0 && newYPos < layout.getHeight() - playerIv.getHeight()){
            playerIv.setY(newYPos);
        }else{
            scoreMulti = 1;
            multiTv.setText(scoreMulti+"X");
            yVel = (yVel * -1) / 2;
        }
        float xDiff = newXPos - goalX;
        float yDiff = newYPos - goalY;
        updateGoal();
        if(xDiff > - playerIv.getWidth()+30 && xDiff < goalIv.getWidth()-30 && yDiff < goalIv.getHeight()-30 && yDiff > - playerIv.getHeight()+30){
            score = score + (50*scoreMulti);
            scoreMulti++;
            scoreTv.setText(score + "p");
            multiTv.setText(scoreMulti + "X");
            newGoal();
            updateGoal();
        }

    }

    public int getScore() {
        return score;
    }
    private int updateGoal(){
        goalIv.setY(goalY);
        goalIv.setX(goalX);
        return currentGoal;
    }
    private void newGoal(){
        Random ran = new Random();
        goalX = ran.nextInt((int) layout.getWidth()-300) + 150;
        goalY = ran.nextInt((int) layout.getHeight() - 300) + 150;
        float xDiff = playerIv.getX() - goalX;
        float yDiff = playerIv.getY() - goalY;
        if(xDiff > - playerIv.getWidth()+30 && xDiff < goalIv.getWidth()-30 && yDiff < goalIv.getHeight()-30 && yDiff > - playerIv.getHeight()+30){
            newGoal();
        }
    }
}
