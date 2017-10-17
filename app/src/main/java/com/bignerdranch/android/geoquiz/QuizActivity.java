package com.bignerdranch.android.geoquiz;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class QuizActivity extends AppCompatActivity {
    private static final String TAG = "QuizActivity";
    private static final String IS_CHEATER =
            "com.bignerdranch.android.geoquiz.is_cheater";
    private static final String KEY_INDEX = "index";
    private static final int REQUEST_CODE_CHEAT = 100;
    private Button mTrueButton;
    private Button mFalseButton;
    private Button mCheatButton;
    private ImageButton mNextButton;
    private ImageButton mPreviousButton;
    private TextView mQuestionTextView;
    private TextView mVersionTextView;
    private TextView mRemainingCheatsTextView;
    private Question[] mQuestionBank = new Question[] {
            new Question(R.string.question_australia, true),
            new Question(R.string.question_oceans, true),
            new Question(R.string.question_mideast, false),
            new Question(R.string.question_africa, false),
            new Question(R.string.question_americas, true),
            new Question(R.string.question_asia, true)
    };
    private int mCurrentIndex;
    private int mCheatCount;
    private boolean mIsCheater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate(Bundle) called");
        setContentView(R.layout.activity_quiz);
        if(savedInstanceState != null) {
            mCurrentIndex =savedInstanceState.getInt(KEY_INDEX, 0);
            mIsCheater = savedInstanceState.getBoolean(IS_CHEATER);
        }
        mQuestionTextView = (TextView) findViewById(R.id.question_text_view);
        mVersionTextView = (TextView) findViewById(R.id.version);
        mRemainingCheatsTextView = (TextView) findViewById(R.id.remaining_cheats);
        mVersionTextView.setText("API Version: " + Build.VERSION.SDK_INT);

        // Configure buttons
        mTrueButton = (Button) findViewById(R.id.true_button);
        mTrueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                answerQuestion();
                checkAnswer(true);
                toggleButtons();
                if (isQuizFinished()) displayQuizResults();
            }
        });
        mFalseButton = (Button) findViewById(R.id.false_button);
        mFalseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                answerQuestion();
                checkAnswer(false);
                toggleButtons();
                if (isQuizFinished()) displayQuizResults();
            }
        });
        mCheatButton = (Button) findViewById(R.id.cheat_button);
        mCheatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean answerIsTrue = mQuestionBank[mCurrentIndex].isAnswerTrue();
                Intent intent = CheatActivity.newIntent(QuizActivity.this, answerIsTrue);
                mCheatCount += 1;
                checkCheatCount();
                startActivityForResult(intent, REQUEST_CODE_CHEAT);
                mRemainingCheatsTextView.setText("Remaining Cheats: " + (3 - mCheatCount));
            }
        });
        mNextButton = (ImageButton) findViewById(R.id.next_button);
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCurrentIndex = (mCurrentIndex + 1) % mQuestionBank.length;
                mIsCheater = false;
                updateQuestion();
                toggleButtons();
            }
        });
        mPreviousButton = (ImageButton) findViewById(R.id.previous_button);
        mPreviousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCurrentIndex -= 1;
                if(mCurrentIndex < 0) mCurrentIndex = mQuestionBank.length - 1;
                updateQuestion();
                toggleButtons();
            }
        });
        mQuestionTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int nextQuestion = (mCurrentIndex + 1) % mQuestionBank.length;
                Question question = mQuestionBank[nextQuestion];
                Toast.makeText(QuizActivity.this, question.getTextResId(), Toast.LENGTH_SHORT)
                        .show();
            }
        });
        updateQuestion();
        toggleButtons();
    }

    private void checkCheatCount() {
        if(mCheatCount >= 3) {
            mCheatButton.setEnabled(false);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode != Activity.RESULT_OK) {
            return;
        }
        if(requestCode == REQUEST_CODE_CHEAT) {
            if(data == null) {
                return;
            }
            mIsCheater = CheatActivity.wasAnswerShown(data);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart() called");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume() called");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause() called");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.i(TAG, "onSaveInstanceState");
        outState.putInt(KEY_INDEX, mCurrentIndex);
        outState.putBoolean(IS_CHEATER, mIsCheater);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop() called");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy() called");
    }


    private void updateQuestion() {
        Log.d(TAG, "Updating question text", new Exception());
        int question = mQuestionBank[mCurrentIndex].getTextResId();
        mQuestionTextView.setText(question);
    }

    private void checkAnswer(boolean userPressedTrue) {
        Question question = mQuestionBank[mCurrentIndex];
        boolean answerIsTrue = question.isAnswerTrue();
        int messageResId;
        if(mIsCheater) {
            messageResId = R.string.judgment_toast;
        } else {
            if (userPressedTrue == answerIsTrue) {
                messageResId = R.string.correct_toast;
                question.setCorrectAnswer(true);
            } else {
                messageResId = R.string.incorrect_toast;
            }
        }
        Toast.makeText(this, messageResId, Toast.LENGTH_SHORT).show();
    }

    private void toggleButtons() {
        Question question = mQuestionBank[mCurrentIndex];
        boolean isAnswered = question.isAnswered();
        if (isAnswered) {
            mTrueButton.setEnabled(false);
            mFalseButton.setEnabled(false);
        } else {
            mTrueButton.setEnabled(true);
            mFalseButton.setEnabled(true);
        }
    }

    private void answerQuestion() {
        Question question = mQuestionBank[mCurrentIndex];
        question.setAnswered(true);
    }

    private boolean isQuizFinished() {
        boolean areAllAnswered = true;
        for(Question question : mQuestionBank) {
            if(!question.isAnswered()) {
                areAllAnswered = false;
                break;
            }
        }
        return areAllAnswered;
    }

    private void displayQuizResults() {
        float correctAnswers = 0;
        for(Question question : mQuestionBank) {
            if(question.isCorrectAnswer()) correctAnswers += 1;
        }
        int score = Math.round((correctAnswers / mQuestionBank.length) * 100);
        Toast.makeText(this, "Quiz Complete! Your score is: " + score + "%",
                Toast.LENGTH_SHORT).show();
    }
}
