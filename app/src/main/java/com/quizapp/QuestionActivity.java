package com.quizapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import static com.quizapp.SetsActivity.cat_id;

public class QuestionActivity extends AppCompatActivity implements View.OnClickListener {
    TextView question, qcount, timer;
    Button op1, op2, op3, op4;
    private List<Questions> questionsList;
    CountDownTimer countDownTimer;
    int qNum;
    private int score;
    private FirebaseFirestore firestore;
    private int set_no;
    private Dialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);

        question = findViewById(R.id.question_id);
        qcount = findViewById(R.id.qcounting);
        timer = findViewById(R.id.countdown);
        op1 = findViewById(R.id.opt_a);
        op2 = findViewById(R.id.opt_b);
        op3 = findViewById(R.id.opt_c);
        op4 = findViewById(R.id.opt_d);

        op1.setOnClickListener(this);
        op2.setOnClickListener(this);
        op3.setOnClickListener(this);
        op4.setOnClickListener(this);
        loadingDialog = new Dialog(QuestionActivity.this);
        loadingDialog.setContentView(R.layout.loading_progressbar);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setBackgroundDrawableResource(R.drawable.progress_back);
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        loadingDialog.show();

        set_no = getIntent().getIntExtra("SETNO", 1);

        firestore = FirebaseFirestore.getInstance();

        getQuestionList();

        score = 0;

    }

    private void getQuestionList() {
        questionsList = new ArrayList<>();
        firestore.collection("QUIZ").document("CAT" + String.valueOf(cat_id)).
                collection("SET" + String.valueOf(set_no)).get().
                addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot questions = task.getResult();
                            for (QueryDocumentSnapshot doc : questions) {
                                questionsList.add(new Questions(doc.getString("QUESTION"),
                                        doc.getString("A"),
                                        doc.getString("B"),
                                        doc.getString("C"),
                                        doc.getString("D"),
                                        Integer.valueOf(doc.getString("ANSWER"))));
                            }
                            setQuestion();
                        } else {
                            Toast.makeText(QuestionActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        loadingDialog.cancel();
                    }
                });

    }

    private void setQuestion() {
        timer.setText(String.valueOf(10));
        question.setText(questionsList.get(0).getQuestion());
        op1.setText(questionsList.get(0).getOpA());
        op2.setText(questionsList.get(0).getOpB());
        op3.setText(questionsList.get(0).getOpC());
        op4.setText(questionsList.get(0).getOpD());

        qcount.setText(String.valueOf(1) + "/" + String.valueOf(questionsList.size()));
        startTimer();
        qNum = 0;
    }

    private void startTimer() {
        countDownTimer = new CountDownTimer(12000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (millisUntilFinished < 10000)
                    timer.setText(String.valueOf(millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                changeQuestion();
            }
        };
        countDownTimer.start();
    }

    @Override
    public void onClick(View v) {
        int selectedOpt = 0;
        switch (v.getId()) {
            case R.id.opt_a:
                selectedOpt = 1;
                break;
            case R.id.opt_b:
                selectedOpt = 2;
                break;
            case R.id.opt_c:
                selectedOpt = 3;
                break;
            case R.id.opt_d:
                selectedOpt = 4;
                break;
            default:
        }
        countDownTimer.cancel();
        checkAnswer(selectedOpt, v);
    }

    private void checkAnswer(int selectedOpt, View view) {
        if (selectedOpt == questionsList.get(qNum).getCorrectAns()) {
            //right ans
            ((Button) view).setBackgroundTintList(ColorStateList.valueOf(Color.GREEN));
            score++;

        } else {
            //wrong ans
            ((Button) view).setBackgroundTintList(ColorStateList.valueOf(Color.RED));
            switch (questionsList.get(qNum).getCorrectAns()) {
                case 1:
                    op1.setBackgroundTintList(ColorStateList.valueOf(Color.GREEN));
                    break;
                case 2:
                    op2.setBackgroundTintList(ColorStateList.valueOf(Color.GREEN));
                    break;
                case 3:
                    op3.setBackgroundTintList(ColorStateList.valueOf(Color.GREEN));
                    break;
                case 4:
                    op4.setBackgroundTintList(ColorStateList.valueOf(Color.GREEN));
                    break;
            }
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                changeQuestion();
            }
        }, 2000);
    }

    private void changeQuestion() {
        qNum++;
        if (qNum < questionsList.size()) {
            playAnim(question, 0, 0);
            playAnim(op1, 0, 1);
            playAnim(op2, 0, 2);
            playAnim(op3, 0, 3);
            playAnim(op4, 0, 4);

            qcount.setText(String.valueOf(qNum + 1 + " / " + questionsList.size()));
            timer.setText(String.valueOf(10));
            startTimer();
        } else {
            //go to score activity
            Intent i = new Intent(QuestionActivity.this, ScoreActivity.class);
            i.putExtra("SCORE", String.valueOf(score) + " / " + String.valueOf(questionsList.size()));
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            //QuestionActivity.this.finish();
        }
    }

    private void playAnim(final View view, final int value, final int viewNum) {
        view.animate().alpha(value).scaleX(value).scaleY(value).setDuration(500).
                setStartDelay(100).setInterpolator(new DecelerateInterpolator()).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (value == 0) {
                    switch (viewNum) {
                        case 0:
                            ((TextView) view).setText(questionsList.get(qNum).getQuestion());
                            break;
                        case 1:
                            ((Button) view).setText(questionsList.get(qNum).getOpA());
                            break;
                        case 2:
                            ((Button) view).setText(questionsList.get(qNum).getOpB());
                            break;
                        case 3:
                            ((Button) view).setText(questionsList.get(qNum).getOpC());
                            break;
                        case 4:
                            ((Button) view).setText(questionsList.get(qNum).getOpD());
                            break;
                    }
                    if (viewNum != 0) {
                        ((Button) view).setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E99C03")));
                    }
                    playAnim(view, 1, viewNum);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        countDownTimer.cancel();
    }
}
