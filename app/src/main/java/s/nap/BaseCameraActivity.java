package s.nap;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.opengl.GLException;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


import com.arthenica.mobileffmpeg.Config;
import com.arthenica.mobileffmpeg.ExecuteCallback;
import com.arthenica.mobileffmpeg.FFmpeg;
import com.arthenica.mobileffmpeg.Statistics;
import com.arthenica.mobileffmpeg.StatisticsCallback;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.IntBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.opengles.GL10;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS;

public class BaseCameraActivity extends AppCompatActivity {

    private SampleGLView sampleGLView;
    protected CameraRecorder cameraRecorder;
    static String filepath,song_path;
    private ImageView recordBtn;
    protected LensFacing lensFacing = LensFacing.BACK;
    protected int cameraWidth = 1280;
    protected int cameraHeight = 720;
    protected int videoWidth = 720;
    protected int videoHeight = 720;
    private boolean toggleClick = false;
    private ImageView btn_flash_on,btn_flash_off,okbtn,cancel_btn,
            arrow_down,arrow_up,ic_timer,ic_timer_red;
    private int i=0;
    public static int video_length=10000;
    private static ProgressBar mProgressBar;
    private CountDownTimer mCountDownTimer = null;
    public static String selectedpath;
    private TextView timer_ok,txt10sec,txt20sec,txt30sec,rec_done;
    public static boolean isMusic=false,isTimer=false;
    private LinearLayout mytimer_layout;
    private FrameLayout rec_layout;
    private HorizontalScrollView filterlayout;

    public static ImageView add_music;
    public static int musicstarttime,musicendtime;
    static TextView music_txt;
    static LinearLayout slide_layout;


    MediaPlayer mp;

    private float x1,x2;
    static final int MIN_DISTANCE = 150;
    Filters[] filters;
    ImageView btn_filter;

    @SuppressLint("ClickableViewAccessibility")
    protected void onCreateActivity() {
        recordBtn = findViewById(R.id.btn_record);
        btn_flash_on=findViewById(R.id.btn_flash_on);
        btn_flash_off =findViewById(R.id.btn_flash_off);
        add_music=findViewById(R.id.add_music);
        mProgressBar= findViewById(R.id.progressbar);
        okbtn=findViewById(R.id.okbtn);
        music_txt=findViewById(R.id.music_txt);

        cancel_btn=findViewById(R.id.cancel_btn);
        rec_layout=findViewById(R.id.rec_layout);
        rec_done=findViewById(R.id.rec_done);
        arrow_down=findViewById(R.id.arrow_down);
        ic_timer=findViewById(R.id.ic_timer);
        arrow_up=findViewById(R.id.arrow_up);
        slide_layout=findViewById(R.id.slide_layout);
        timer_ok=findViewById(R.id.timer_ok);
        mytimer_layout=findViewById(R.id.mytimer_layout);
        txt10sec=findViewById(R.id.txt10sec);
        txt20sec=findViewById(R.id.txt20sec);
        txt30sec=findViewById(R.id.txt30sec);
        ic_timer_red=findViewById(R.id.ic_timer_red);
        FrameLayout close_timer = findViewById(R.id.close_timer);
        filterlayout=findViewById(R.id.filterlayout);


        recordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recordBtn.setEnabled(false);
                filepath = getVideoFilePath();
                cameraRecorder.start(filepath);
                recordBtn.setImageResource(R.drawable.ic_stop_rec);

                mCountDownTimer=new CountDownTimer(video_length,1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        i++;
                        ObjectAnimator animation = ObjectAnimator.ofInt(mProgressBar, "progress", i *100/(video_length/1000));
                        animation.setDuration(1000);
                        animation.setInterpolator(new DecelerateInterpolator());
                        animation.start();
                    }

                    @Override
                    public void onFinish() {
                        recordBtn.setImageResource(R.drawable.ic_rec);
                        recordBtn.setEnabled(true);
                        cancelTimer();
                        cameraRecorder.stop();
                    }
                };
                mCountDownTimer.start();
            }
        });


        add_music.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(BaseCameraActivity.this, FragmentViewPagerActivity.class));
            }
        });

        btn_flash_off.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cameraRecorder != null && cameraRecorder.isFlashSupport()) {
                    cameraRecorder.switchFlashMode();
                    cameraRecorder.changeAutoFocus();
                    btn_flash_off.setVisibility(View.GONE);
                    btn_flash_on.setVisibility(View.VISIBLE);
                }
            }
        });


        btn_flash_on.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cameraRecorder != null && cameraRecorder.isFlashSupport()) {
                    cameraRecorder.switchFlashMode();
                    cameraRecorder.changeAutoFocus();
                    btn_flash_off.setVisibility(View.VISIBLE);
                    btn_flash_on.setVisibility(View.GONE);
                }
            }
        });

        ic_timer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recordBtn.setEnabled(false);
                mytimer_layout.setVisibility(View.VISIBLE);
            }
        });
        close_timer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recordBtn.setEnabled(true);
                isTimer=false;
                mytimer_layout.setVisibility(View.GONE);
            }
        });



        ImageView btn_switch_camera=findViewById(R.id.btn_switch_camera);

        //FILTER


        CircleImageView normal,bilateral,box_blur,bludge_distortion,cga_color_space,gussian_blur,
        gray_scale,invert,lookup_table,monochrome,overlay,sepia,sharpen,sphere_refraction,tone_curve,tone,
        vignette,weak_pexel_inclusion,filter_group,notblue,tgreen,dull,set,spark;

        normal=findViewById(R.id.normal);
        bilateral=findViewById(R.id.bilateral);
        box_blur=findViewById(R.id.box_blur);
        bludge_distortion=findViewById(R.id.bludge_distortion);
        cga_color_space=findViewById(R.id.cga_color_space);
        gussian_blur=findViewById(R.id.gussian_blur);
        gray_scale=findViewById(R.id.gray_scale);
        invert=findViewById(R.id.invert);
        lookup_table=findViewById(R.id.lookup_table);
        monochrome=findViewById(R.id.monochrome);
        overlay=findViewById(R.id.overlay);
        sepia=findViewById(R.id.sepia);
        sharpen=findViewById(R.id.sharpen);
        sphere_refraction=findViewById(R.id.sphere_refraction);
        tone_curve=findViewById(R.id.tone_curve);
        tone=findViewById(R.id.tone);
        vignette=findViewById(R.id.vignette);
        weak_pexel_inclusion=findViewById(R.id.weak_pexel_inclusion);
        filter_group=findViewById(R.id.filter_group);
        notblue=findViewById(R.id.notblue);
        tgreen=findViewById(R.id.tgreen);
        dull=findViewById(R.id.dull);
        set=findViewById(R.id.set);
        spark=findViewById(R.id.spark);

        btn_filter=findViewById(R.id.btn_filter);

        filters = Filters.values();
        CharSequence[] charList = new CharSequence[filters.length];
        for (int i = 0, n = filters.length; i < n; i++) {
            charList[i] = filters[i].name();
        }

         btn_filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (filterlayout.getVisibility()== View.VISIBLE)
                {
                    filterlayout.setVisibility(View.GONE);
                }

                else {

                    Animation fadeIn = new AlphaAnimation(0, 1);
                    fadeIn.setInterpolator(new DecelerateInterpolator());
                    fadeIn.setDuration(500);
                    AnimationSet animation = new AnimationSet(false);
                    animation.addAnimation(fadeIn);

                    filterlayout.setAnimation(animation);
                    filterlayout.setVisibility(View.VISIBLE);
                }

            }
          });

           normal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BaseCameraActivity.this.changeFilter(filters[0]);
                filterlayout.setVisibility(View.GONE);
                btn_filter.setImageResource(R.drawable.ic_filter);
            }

           });

             bilateral.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BaseCameraActivity.this.changeFilter(filters[1]);
                filterlayout.setVisibility(View.GONE);
                btn_filter.setImageResource(R.drawable.ic_no_filter);
            }

           });

               box_blur.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BaseCameraActivity.this.changeFilter(filters[2]);
                filterlayout.setVisibility(View.GONE);
                btn_filter.setImageResource(R.drawable.ic_no_filter);
            }

           });

                 bludge_distortion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BaseCameraActivity.this.changeFilter(filters[3]);
                filterlayout.setVisibility(View.GONE);
                btn_filter.setImageResource(R.drawable.ic_no_filter);
            }

           });

           cga_color_space.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               BaseCameraActivity.this.changeFilter(filters[4]);
                filterlayout.setVisibility(View.GONE);
                btn_filter.setImageResource(R.drawable.ic_no_filter);
            }
        });

            gussian_blur.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BaseCameraActivity.this.changeFilter(filters[5]);
                filterlayout.setVisibility(View.GONE);
                btn_filter.setImageResource(R.drawable.ic_no_filter);
            }
        });

             gray_scale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BaseCameraActivity.this.changeFilter(filters[6]);
                filterlayout.setVisibility(View.GONE);
                btn_filter.setImageResource(R.drawable.ic_no_filter);
            }
        });

              invert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BaseCameraActivity.this.changeFilter(filters[7]);
                filterlayout.setVisibility(View.GONE);
                btn_filter.setImageResource(R.drawable.ic_no_filter);
            }
        });

               lookup_table.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BaseCameraActivity.this.changeFilter(filters[8]);
                filterlayout.setVisibility(View.GONE);
                btn_filter.setImageResource(R.drawable.ic_no_filter);
            }
        });

                monochrome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BaseCameraActivity.this.changeFilter(filters[9]);
                filterlayout.setVisibility(View.GONE);
                btn_filter.setImageResource(R.drawable.ic_no_filter);
            }
        });

                 overlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BaseCameraActivity.this.changeFilter(filters[10]);
                filterlayout.setVisibility(View.GONE);
                btn_filter.setImageResource(R.drawable.ic_no_filter);
            }
        });

                  sepia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BaseCameraActivity.this.changeFilter(filters[11]);
                filterlayout.setVisibility(View.GONE);
                btn_filter.setImageResource(R.drawable.ic_no_filter);
            }
        });

                   sharpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BaseCameraActivity.this.changeFilter(filters[12]);
                filterlayout.setVisibility(View.GONE);
                btn_filter.setImageResource(R.drawable.ic_no_filter);
            }
        });

                    sphere_refraction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               BaseCameraActivity.this.changeFilter(filters[13]);
                filterlayout.setVisibility(View.GONE);
                btn_filter.setImageResource(R.drawable.ic_no_filter);
            }
        });

                     tone_curve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BaseCameraActivity.this.changeFilter(filters[14]);
                filterlayout.setVisibility(View.GONE);
                btn_filter.setImageResource(R.drawable.ic_no_filter);
            }
        });

                      tone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BaseCameraActivity.this.changeFilter(filters[15]);
                filterlayout.setVisibility(View.GONE);
                btn_filter.setImageResource(R.drawable.ic_no_filter);
            }
        });

                       vignette.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BaseCameraActivity.this.changeFilter(filters[16]);
                filterlayout.setVisibility(View.GONE);
                btn_filter.setImageResource(R.drawable.ic_no_filter);
            }
        });

                        weak_pexel_inclusion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BaseCameraActivity.this.changeFilter(filters[17]);
                filterlayout.setVisibility(View.GONE);
                btn_filter.setImageResource(R.drawable.ic_no_filter);
            }
        });

                         filter_group.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BaseCameraActivity.this.changeFilter(filters[18]);
                filterlayout.setVisibility(View.GONE);
                btn_filter.setImageResource(R.drawable.ic_no_filter);
            }
        });
                         notblue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BaseCameraActivity.this.changeFilter(filters[19]);
                filterlayout.setVisibility(View.GONE);
                btn_filter.setImageResource(R.drawable.ic_no_filter);
            }
        });

       tgreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BaseCameraActivity.this.changeFilter(filters[20]);
                filterlayout.setVisibility(View.GONE);
                btn_filter.setImageResource(R.drawable.ic_no_filter);
            }
        });

         dull.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BaseCameraActivity.this.changeFilter(filters[21]);
                filterlayout.setVisibility(View.GONE);
                btn_filter.setImageResource(R.drawable.ic_no_filter);
            }
        });

         set.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BaseCameraActivity.this.changeFilter(filters[22]);
                filterlayout.setVisibility(View.GONE);
                btn_filter.setImageResource(R.drawable.ic_no_filter);
            }
        });

          spark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BaseCameraActivity.this.changeFilter(filters[23]);
                filterlayout.setVisibility(View.GONE);
                btn_filter.setImageResource(R.drawable.ic_no_filter);
            }
        });



        btn_switch_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn_filter.setImageResource(R.drawable.ic_filter);
                final Animation myAnim = AnimationUtils.loadAnimation(BaseCameraActivity.this, R.anim.bounce);
                MyBounceInterpolator interpolator = new MyBounceInterpolator(0.1, 15);
                myAnim.setInterpolator(interpolator);
                btn_switch_camera.startAnimation(myAnim);

                BaseCameraActivity.this.releaseCamera();
                if (lensFacing == LensFacing.BACK) {
                    lensFacing = LensFacing.FRONT;
                } else {
                    lensFacing = LensFacing.BACK;
                }
                toggleClick = true;
            }
        });

        arrow_down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Animation myAnim = AnimationUtils.loadAnimation(BaseCameraActivity.this, R.anim.bounce);
                MyBounceInterpolator interpolator = new MyBounceInterpolator(0.1, 15);
                myAnim.setInterpolator(interpolator);

                arrow_up.setVisibility(View.VISIBLE);
                arrow_down.setVisibility(View.INVISIBLE);

                if (isTimer)
                {
                    ic_timer_red.setVisibility(View.VISIBLE);
                    ic_timer.setVisibility(View.GONE);
                    ic_timer_red.startAnimation(myAnim);
                }
                else{
                    ic_timer_red.setVisibility(View.GONE);
                    ic_timer.setVisibility(View.VISIBLE);
                    ic_timer.startAnimation(myAnim);
                }

                ResizeAnimation resizeAnimation = new ResizeAnimation(slide_layout, 110, 480);
                resizeAnimation.setDuration(1000);
                slide_layout.startAnimation(resizeAnimation);

            }
        });

        arrow_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ValueAnimator anim = ValueAnimator.ofInt(slide_layout.getMeasuredHeight(),490);
                anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        int val = (Integer) valueAnimator.getAnimatedValue();
                        ViewGroup.LayoutParams layoutParams = slide_layout.getLayoutParams();
                        layoutParams.height = val;
                        slide_layout.setLayoutParams(layoutParams);
                    }
                });
                anim.setDuration(1000);
                anim.start();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        final Animation myAnim = AnimationUtils.loadAnimation(BaseCameraActivity.this, R.anim.bounce);
                        MyBounceInterpolator interpolator = new MyBounceInterpolator(0.1, 15);
                        myAnim.setInterpolator(interpolator);
                        arrow_down.startAnimation(myAnim);

                        arrow_down.setVisibility(View.VISIBLE);
                        arrow_up.setVisibility(View.INVISIBLE);
                        ic_timer.setVisibility(View.GONE);
                        ic_timer_red.setVisibility(View.GONE);
                    }
                }, 1000);

            }
        });

        okbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelTimer();
                music_txt.setVisibility(View.GONE);
                if (isTimer)
                    recordBtn.setEnabled(true);

                if (isMusic)
                {
                    executeMergeAudioVideoCommand();
                    recordBtn.setEnabled(false);
                    okbtn.setVisibility(View.GONE);
                    cancel_btn.setVisibility(View.GONE);
                }
                else
                {
                    okbtn.setVisibility(View.GONE);
                    cancel_btn.setVisibility(View.GONE);

                    StoreClass.vide_path=filepath;

                    recordBtn.setEnabled(true);
                    rec_done.setVisibility(View.VISIBLE);
                    rec_layout.setVisibility(View.GONE);
                    slide_layout.setVisibility(View.GONE);

                    final Animation myAnim = AnimationUtils.loadAnimation(BaseCameraActivity.this, R.anim.bounce);
                    MyBounceInterpolator interpolator = new MyBounceInterpolator(0.1, 15);
                    myAnim.setInterpolator(interpolator);
                    rec_done.startAnimation(myAnim);
                }
            }
        });
        cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                File fdelete = new File(BaseCameraActivity.filepath);
                if (fdelete.exists()) {
                    fdelete.delete();
                }

                if (isTimer)
                    recordBtn.setEnabled(true);

                okbtn.setVisibility(View.GONE);
                cancel_btn.setVisibility(View.GONE);

                cancelTimer();

            }
        });

        rec_done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rec_done.setVisibility(View.GONE);
                startActivity(new Intent(BaseCameraActivity.this, UploadVideo.class));
                cancelTimer();

                rec_layout.setVisibility(View.VISIBLE);
                music_txt.setVisibility(View.GONE);

                final Animation myAnim = AnimationUtils.loadAnimation(BaseCameraActivity.this, R.anim.bounce);
                MyBounceInterpolator interpolator = new MyBounceInterpolator(0.1, 15);

                myAnim.setInterpolator(interpolator);
                rec_layout.startAnimation(myAnim);
            }
        });



        txt10sec.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                txt10sec.setTextColor(getResources().getColor(R.color.redcolor));
                txt20sec.setTextColor(getResources().getColor(R.color.white));
                txt30sec.setTextColor(getResources().getColor(R.color.white));
                video_length=10000;
            }
        });
        txt20sec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                txt20sec.setTextColor(getResources().getColor(R.color.redcolor));
                txt10sec.setTextColor(getResources().getColor(R.color.white));
                txt30sec.setTextColor(getResources().getColor(R.color.white));
                video_length=20000;
            }
        });
        txt30sec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                txt30sec.setTextColor(getResources().getColor(R.color.redcolor));
                txt10sec.setTextColor(getResources().getColor(R.color.white));
                txt20sec.setTextColor(getResources().getColor(R.color.white));
                video_length=30000;
            }
        });

        timer_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recordBtn.setEnabled(true);

                if (video_length==15000)
                {
                    mytimer_layout.setVisibility(View.GONE);
                    isTimer=false;
                }
                else
                {
                    isTimer=true;
                    mytimer_layout.setVisibility(View.GONE);
                    ic_timer.setVisibility(View.GONE);
                    ic_timer_red.setVisibility(View.VISIBLE);

                    final Animation myAnim = AnimationUtils.loadAnimation(BaseCameraActivity.this, R.anim.bounce);
                    MyBounceInterpolator interpolator = new MyBounceInterpolator(0.1, 15);

                    myAnim.setInterpolator(interpolator);
                    ic_timer_red.startAnimation(myAnim);
                }



            }
        });

        ic_timer_red.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ic_timer.setVisibility(View.VISIBLE);
                ic_timer_red.setVisibility(View.GONE);
                video_length=15000;
                isTimer=false;

                final Animation myAnim = AnimationUtils.loadAnimation(BaseCameraActivity.this, R.anim.bounce);
                MyBounceInterpolator interpolator = new MyBounceInterpolator(0.1, 15);

                myAnim.setInterpolator(interpolator);
                ic_timer.startAnimation(myAnim);
            }
        });
    }

    void cancelTimer() {
        if(mCountDownTimer!=null)
        {
            i=0;
            mCountDownTimer.cancel();
            mProgressBar.setProgress(i);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpCamera();
    }

    @Override
    protected void onStop() {
        super.onStop();
        releaseCamera();
        cancelTimer();
    }

    private void releaseCamera() {
        if (sampleGLView != null) {
            sampleGLView.onPause();
        }

        if (cameraRecorder != null) {
            cameraRecorder.stop();
            cameraRecorder.release();
            cameraRecorder = null;
        }

        if (sampleGLView != null) {
            ((FrameLayout) findViewById(R.id.wrap_view)).removeView(sampleGLView);
            sampleGLView = null;
        }
    }


    private void setUpCameraView() {
        runOnUiThread(() -> {
            FrameLayout frameLayout = findViewById(R.id.wrap_view);
            frameLayout.removeAllViews();
            sampleGLView = null;
            sampleGLView = new SampleGLView(getApplicationContext());
            sampleGLView.setTouchListener((event, width, height) -> {
                if (cameraRecorder == null) return;
                cameraRecorder.changeManualFocusPoint(event.getX(), event.getY(), width, height);
            });
            frameLayout.addView(sampleGLView);
        });
    }


    private void setUpCamera() {
        setUpCameraView();
        cameraRecorder = new CameraRecorderBuilder(this, sampleGLView)
                .cameraRecordListener(new CameraRecordListener() {
                    @Override
                    public void onGetFlashSupport(boolean flashSupport) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (!flashSupport)
                                {
                                    btn_flash_off.setVisibility(View.VISIBLE);
                                    btn_flash_on.setVisibility(View.GONE);
                                }
                            }
                        });
                    }

                    @Override
                    public void onRecordComplete() {
                        cancelTimer();
                        okbtn.setVisibility(View.VISIBLE);
                        cancel_btn.setVisibility(View.VISIBLE);

                        if (isMusic)
                        {
                            mp.pause();
                        }
                    }

                    @Override
                    public void onRecordStart() {
                        okbtn.setVisibility(View.GONE);
                        cancel_btn.setVisibility(View.GONE);

                        if (isMusic) {
                            mp = new MediaPlayer();
                            try {
                                mp.setDataSource(BaseCameraActivity.this, Uri.parse(song_path));
                                mp.prepare();
                                mp.start();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                    }

                    @Override
                    public void onError(Exception exception) {
                        Log.e("CameraRecorder", exception.toString());
                    }

                    @Override
                    public void onCameraThreadFinish() {
                        if (toggleClick) {
                            runOnUiThread(() -> {
                                setUpCamera();
                            });
                        }
                        toggleClick = false;
                    }
                })
                .videoSize(videoWidth, videoHeight)
                .cameraSize(cameraWidth, cameraHeight)
                .lensFacing(lensFacing)
                .build();


    }

    private void changeFilter(Filters filters) {
        cameraRecorder.setFilter(Filters.getFilterInstance(filters, getApplicationContext()));
    }

    private interface BitmapReadyCallbacks {
        void onBitmapReady(Bitmap bitmap);
    }

    private void captureBitmap(final BitmapReadyCallbacks bitmapReadyCallbacks) {
        sampleGLView.queueEvent(() -> {
            EGL10 egl = (EGL10) EGLContext.getEGL();
            GL10 gl = (GL10) egl.eglGetCurrentContext().getGL();
            Bitmap snapshotBitmap = createBitmapFromGLSurface(sampleGLView.getMeasuredWidth(), sampleGLView.getMeasuredHeight(), gl);

            runOnUiThread(() -> {
                bitmapReadyCallbacks.onBitmapReady(snapshotBitmap);
            });
        });
    }

    private Bitmap createBitmapFromGLSurface(int w, int h, GL10 gl) {
        int[] bitmapBuffer = new int[w * h];
        int[] bitmapSource = new int[w * h];
        IntBuffer intBuffer = IntBuffer.wrap(bitmapBuffer);
        intBuffer.position(0);

        try {
            gl.glReadPixels(0, 0, w, h, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, intBuffer);
            int offset1, offset2, texturePixel, blue, red, pixel;
            for (int i = 0; i < h; i++) {
                offset1 = i * w;
                offset2 = (h - i - 1) * w;
                for (int j = 0; j < w; j++) {
                    texturePixel = bitmapBuffer[offset1 + j];
                    blue = (texturePixel >> 16) & 0xff;
                    red = (texturePixel << 16) & 0x00ff0000;
                    pixel = (texturePixel & 0xff00ff00) | red | blue;
                    bitmapSource[offset2 + j] = pixel;
                }
            }
        } catch (GLException e) {
            Log.e("CreateBitmap", "createBitmapFromGLSurface: " + e.getMessage(), e);
            return null;
        }

        return Bitmap.createBitmap(bitmapSource, w, h, Bitmap.Config.ARGB_8888);
    }

    public void saveAsPngImage(Bitmap bitmap, String filePath) {
        try {
            File file = new File(filePath);
            FileOutputStream outStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
            outStream.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void exportMp4ToGallery(Context context, String filePath) {
        final ContentValues values = new ContentValues(2);
        values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
        values.put(MediaStore.Video.Media.DATA, filePath);
        context.getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + filePath)));
    }

    public static String getVideoFilePath() {
        return getAndroidMoviesFolder().getAbsolutePath() + "/" + new SimpleDateFormat("yyyyMM_dd-HHmmss").format(new Date()) + "snap.mp4";
    }

    public static File getAndroidMoviesFolder() {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
    }

    private static void exportPngToGallery(Context context, String filePath) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(filePath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        context.sendBroadcast(mediaScanIntent);
    }

    public static String getImageFilePath() {
        return getAndroidImageFolder().getAbsolutePath() + "/" + new SimpleDateFormat("yyyyMM_dd-HHmmss").format(new Date()) + "snap.png";
    }

    public static File getAndroidImageFolder() {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
    }


    private void executeMergeAudioVideoCommand() {
        File moviesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
        String filePrefix = "merge_audio_video";
        String fileExtn = ".mp4";

        File dest = new File(moviesDir, filePrefix + fileExtn);
        int fileNo = 0;
        while (dest.exists()) {
            fileNo++;
            dest = new File(moviesDir, filePrefix + fileNo + fileExtn);
        }

        StoreClass.vide_path = dest.getAbsolutePath();

        String[] complexCommand = {"-i",filepath,"-i", song_path, "-c:v", "copy", "-c:a", "aac","-map", "0:v:0", "-map", "1:a:0","-shortest", StoreClass.vide_path};


        Config.enableStatisticsCallback(new StatisticsCallback() {
            public void apply(Statistics newStatistics) {
                float progress = Float.parseFloat(String.valueOf(newStatistics.getTime())) / (video_length);
                float progressFinal = progress * 100;
                mProgressBar.setProgress((int) progressFinal);
            }
        });


        long executionId = FFmpeg.executeAsync(complexCommand, new ExecuteCallback() {

            @Override
            public void apply(final long executionId, final int returnCode) {
                if (returnCode == RETURN_CODE_SUCCESS) {

                    recordBtn.setEnabled(true);
                    rec_done.setVisibility(View.VISIBLE);
                    rec_layout.setVisibility(View.GONE);
                    slide_layout.setVisibility(View.GONE);

                    final Animation myAnim = AnimationUtils.loadAnimation(BaseCameraActivity.this, R.anim.bounce);
                    MyBounceInterpolator interpolator = new MyBounceInterpolator(0.1, 15);
                    myAnim.setInterpolator(interpolator);
                    rec_done.startAnimation(myAnim);
                }
                else {
                    Toast.makeText(BaseCameraActivity.this, "Failed"+returnCode, Toast.LENGTH_SHORT).show();
                }

            }
        });
    }


    public static void trimAudio()
    {
        File moviesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
        String filePrefix = "trim_audio";
        String fileExtn = ".mp3";

        File dest = new File(moviesDir, filePrefix + fileExtn);
        int fileNo = 0;
        while (dest.exists()) {
            fileNo++;
            dest = new File(moviesDir, filePrefix + fileNo + fileExtn);
        }

        song_path = dest.getAbsolutePath();
        String[] complexCommand = {"-ss", String.valueOf(musicstarttime),"-i", selectedpath, "-t", String.valueOf(musicendtime), "-c", "copy", song_path};


        Config.enableStatisticsCallback(new StatisticsCallback() {
            public void apply(Statistics newStatistics) {
                float progress = Float.parseFloat(String.valueOf(newStatistics.getTime())) / (video_length);
                float progressFinal = progress * 100;
                mProgressBar.setProgress((int) progressFinal);
            }
        });

        long executionId = FFmpeg.executeAsync(complexCommand, new ExecuteCallback() {
            @Override
            public void apply(final long executionId, final int returnCode) {
                if (returnCode == RETURN_CODE_SUCCESS) {
                    isMusic=true;
                }
            }
        });
    }

    int j=0;
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        if (j==23)
            j=0;
        if (j==0)
            btn_filter.setImageResource(R.drawable.ic_filter);

        switch(event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                x1 = event.getX();
                break;
            case MotionEvent.ACTION_UP:
                x2 = event.getX();

                float deltaX = x2 - x1;
                if (Math.abs(deltaX) > MIN_DISTANCE)
                {
                    j++;
                    BaseCameraActivity.this.changeFilter(filters[j]);
                    filterlayout.setVisibility(View.GONE);
                    btn_filter.setImageResource(R.drawable.ic_no_filter);
                }

                break;

        }
        return super.onTouchEvent(event);
    }
}