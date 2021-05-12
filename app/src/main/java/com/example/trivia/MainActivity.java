package com.example.trivia;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.example.trivia.controller.AppController;
import com.example.trivia.data.AnswerListAsyncResponse;
import com.example.trivia.data.Repository;
import com.example.trivia.databinding.ActivityMainBinding;
import com.example.trivia.model.Question;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String MESSAGE_ID = "prefs";
    private ActivityMainBinding binding;
    private int currentQuestionIndex = 0;
    List<Question> questionList;
    private SharedPreferences sharedPrefs;
    private int value = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        binding.btnNext.setOnClickListener(this);
        binding.btnTrue.setOnClickListener(this);
        binding.btnFalse.setOnClickListener(this);
        binding.btnPrev.setOnClickListener(this);
        binding.shareButtonId.setOnClickListener(this);

        questionList = new Repository().getQuestions(new AnswerListAsyncResponse() {
            @Override
            public void processFinished(ArrayList<Question> questionArrayList) {
                updateQuestion();
            }
        });

        /**
         * Put prefs into its own class
         */
        sharedPrefs = getSharedPreferences(MESSAGE_ID, MODE_PRIVATE);
        currentQuestionIndex = sharedPrefs.getInt("currentLevel", 0);
        value = sharedPrefs.getInt("score", 0);
        updateScore(value);

    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.btn_next){
            getNextQuestion();
        }else if(v.getId() == R.id.btn_true){
            isCorrect(true);
            updateQuestion();
        }else if(v.getId() == R.id.btn_false){
            isCorrect(false);
            updateQuestion();
        }else if(v.getId() == R.id.btn_prev){
            currentQuestionIndex = currentQuestionIndex - 1;
            if(currentQuestionIndex < 0){
                currentQuestionIndex = questionList.size() - 1;
            }
            updateQuestion();
        }else if(v.getId() == R.id.share_button_id){
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("plain/text");
            intent.putExtra(Intent.EXTRA_SUBJECT, "I am playing Trivia");
            intent.putExtra(Intent.EXTRA_TEXT, "My current score is " + value);
            startActivity(intent);
        }

    }

    private void getNextQuestion() {
        currentQuestionIndex = (currentQuestionIndex + 1) % questionList.size();
        updateQuestion();
    }

    private void isCorrect(boolean btnPressed){
        if(btnPressed == questionList.get(currentQuestionIndex).isAnswerTrue()){
            fadeAnimation();
            Toast.makeText(this, "Correct Answer!", Toast.LENGTH_SHORT).show();
            value += 100;
            updateScore(value);
        }else{
            shakeAnimation();
            Toast.makeText(this, "Incorrect!", Toast.LENGTH_SHORT).show();
            value -= 100;
            if(value < 0) value = 0;
            updateScore(value);
        }
    }

    private void updateQuestion(){
        String currentQuestionString;
        binding.txtCardView.setText(questionList.get(currentQuestionIndex).getQuestion());
        currentQuestionString = getString(R.string.question) + " " +(currentQuestionIndex + 1) + " / " + questionList.size();
        binding.txtQuesNum.setText(currentQuestionString);
    }

    private void updateScore(int score){
        String scoreNum = "Score: " + String.valueOf(score);
        binding.txtScore.setText(scoreNum);
    }

    private void shakeAnimation(){
        Animation shake = AnimationUtils.loadAnimation(MainActivity.this,
                R.anim.shake_animation);
        binding.cardView.setAnimation(shake);

        shake.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                binding.txtCardView.setTextColor(Color.RED);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                binding.txtCardView.setTextColor(Color.WHITE);
                getNextQuestion();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

    }

    private void fadeAnimation(){
        AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);
        alphaAnimation.setDuration(200);
        alphaAnimation.setRepeatCount(1);
        alphaAnimation.setRepeatMode(Animation.REVERSE);

        binding.cardView.setAnimation(alphaAnimation);

        alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                binding.txtCardView.setTextColor(Color.GREEN);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                binding.txtCardView.setTextColor(Color.WHITE);
                getNextQuestion();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    @Override
    protected void onPause() {
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putInt("score", value);
        editor.putInt("currentLevel", currentQuestionIndex);
        editor.apply();
        super.onPause();
    }
}