package s.nap;

import android.animation.ObjectAnimator;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.arthenica.mobileffmpeg.Config;
import com.arthenica.mobileffmpeg.ExecuteCallback;
import com.arthenica.mobileffmpeg.FFmpeg;
import com.arthenica.mobileffmpeg.Statistics;
import com.arthenica.mobileffmpeg.StatisticsCallback;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS;
import static s.nap.BaseCameraActivity.getAndroidImageFolder;

public class UploadVideo extends AppCompatActivity {
    private SeekBar seekBar;
    private int stopPosition;
    private VideoView videoView;
    private TextView startTime,endTime,daytodaytxt,ctimetxt;
    private Handler videoHandler;
    private ImageView playbtn,pausebtn,txt_img;
    private boolean isSavedToGallery=false,isTextEnabled=false,isImageEnabled=false;

    private ViewGroup root_layout;
    private LinearLayout drag_view;
    private int _xDelta;
    private int _yDelta;
    private String txt_video_path,img_video_path;
    private ProgressBar mProgressBar;

    private ImageView img_view,filter_frame_imageview;
    private String image_name;
    private HorizontalScrollView show_frame_layout;
    private LinearLayout media_view,apply_remove_frame;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_video);
        videoView=findViewById(R.id.myvideo);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        startTime=findViewById(R.id.starttime);
        endTime=findViewById(R.id.endtime);
        playbtn=findViewById(R.id.playbtn);
        pausebtn=findViewById(R.id.pausebtn);
        daytodaytxt=findViewById(R.id.daytodaytxt);
        ctimetxt=findViewById(R.id.ctimetxt);
        txt_img=findViewById(R.id.txt_img);
        mProgressBar= findViewById(R.id.progressbar);
        img_view=findViewById(R.id.img_view);
        show_frame_layout=findViewById(R.id.show_frame_layout);
        media_view=findViewById(R.id.media_view);
        apply_remove_frame=findViewById(R.id.apply_remove_frame);
        filter_frame_imageview=findViewById(R.id.filter_frame_imageview);

        BaseCameraActivity.slide_layout.setVisibility(View.VISIBLE);

        Calendar date = Calendar.getInstance();
        String dayToday = android.text.format.DateFormat.format("EEEE", date).toString();
        daytodaytxt.setText(dayToday);

        SimpleDateFormat dateFormat = new SimpleDateFormat("hh.mm a");
        String formattedDate = dateFormat.format(new Date()).toString();
        ctimetxt.setText(formattedDate);

        videoView.setVideoURI(Uri.parse(StoreClass.vide_path));
        videoView.start();
        setHandler();


        playbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Animation fadeIn = new AlphaAnimation(0, 1);
                fadeIn.setInterpolator(new DecelerateInterpolator());
                fadeIn.setDuration(500);
                AnimationSet animation = new AnimationSet(false);
                animation.addAnimation(fadeIn);
                pausebtn.setAnimation(animation);

                videoView.start();
                pausebtn.setVisibility(View.VISIBLE);
                playbtn.setVisibility(View.GONE);
            }
        });

        pausebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Animation fadeIn = new AlphaAnimation(0, 1);
                fadeIn.setInterpolator(new DecelerateInterpolator());
                fadeIn.setDuration(500);
                AnimationSet animation = new AnimationSet(false);
                animation.addAnimation(fadeIn);
                playbtn.setAnimation(animation);

                pausebtn.setVisibility(View.GONE);
                playbtn.setVisibility(View.VISIBLE);
                videoView.pause();
            }
        });


        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setLooping(true);
                seekBar.setMax(videoView.getDuration());
                seekBar.postDelayed(onEverySecond, 1000);
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    videoView.seekTo(progress);
                }
            }
        });


        drag_view=findViewById(R.id.drag_view);
        root_layout=findViewById(R.id.root_layout);
        drag_view=root_layout.findViewById(R.id.drag_view);

        RelativeLayout.LayoutParams layoutParams=new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        drag_view.setLayoutParams(layoutParams);
        drag_view.setOnTouchListener(new ChoiceTouchListner());

        drag_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

    }

    public void saveToGallery(View view) {
        BaseCameraActivity.isMusic=false;
        BaseCameraActivity.add_music.setImageResource(R.drawable.ic_music);
        BaseCameraActivity.music_txt.setVisibility(View.GONE);

         if (isTextEnabled && isImageEnabled)
        {
            mProgressBar.setVisibility(View.VISIBLE);
            File moviesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
            String filePrefix = "txt_video";
            String fileExtn = ".mp4";

            File dest = new File(moviesDir, filePrefix + fileExtn);
            int fileNo = 0;
            while (dest.exists()) {
                fileNo++;
                dest = new File(moviesDir, filePrefix + fileNo + fileExtn);
            }

            int timex=_xDelta;
            int timey=_yDelta;

            if (daytodaytxt.getText().toString().equalsIgnoreCase("monday")||
                    daytodaytxt.getText().toString().equalsIgnoreCase("friday")||
                    daytodaytxt.getText().toString().equalsIgnoreCase("sunday"))
                timex+=50;
            else if (daytodaytxt.getText().toString().equalsIgnoreCase("tuesday"))
                timex+=70;
            else
                timex+=180;

            timey+=100;

            txt_video_path = dest.getAbsolutePath();
            String[] complexCommand = new String[]{"-i",  StoreClass.vide_path,"-b:v", "7000k", "-vf","[in]drawtext=fontfile=/system/fonts/DancingScript-Regular.ttf:text='"+daytodaytxt.getText().toString()+"':x="+_xDelta+":y="+_yDelta+":fontsize=100:fontcolor=white, drawtext=fontsize=40:fontcolor=White:fontfile='/system/fonts/Roboto-Thin.ttf':text='"+ctimetxt.getText().toString()+"':x="+timex+":y="+timey+"[out]","-y", txt_video_path};

            long executionId = FFmpeg.executeAsync(complexCommand, new ExecuteCallback() {

                @Override
                public void apply(final long executionId, final int returnCode) {
                    if (returnCode == RETURN_CODE_SUCCESS) {
                        mProgressBar.setProgress(0);
                        mProgressBar.setVisibility(View.VISIBLE);
                        File moviesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
                        String filePrefix = "img_video";
                        String fileExtn = ".mp4";


                        File dest = new File(moviesDir, filePrefix + fileExtn);
                        int fileNo = 0;
                        while (dest.exists()) {
                            fileNo++;
                            dest = new File(moviesDir, filePrefix + fileNo + fileExtn);
                        }


                        img_video_path = dest.getAbsolutePath();

                        String[] complexCommand = {"-i", txt_video_path,"-i",getImageFilePath(), "-filter_complex","overlay=10:x=0:y=0","-b:v", "7000k", img_video_path};

                        long executionId2 = FFmpeg.executeAsync(complexCommand, new ExecuteCallback() {

                            @Override
                            public void apply(final long executionId, final int returnCode) {
                                if (returnCode == RETURN_CODE_SUCCESS) {
                                    mProgressBar.setProgress(0);
                                    mProgressBar.setVisibility(View.INVISIBLE);
                                    BaseCameraActivity.exportMp4ToGallery(UploadVideo.this,img_video_path);
                                    Toast.makeText(UploadVideo.this, "saved", Toast.LENGTH_SHORT).show();
                                }
                                else
                                {
                                    Toast.makeText(UploadVideo.this, "Oops! unable to render image pls try again", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                        Config.enableStatisticsCallback(new StatisticsCallback() {
                            public void apply(Statistics newStatistics) {
                                float progress = Float.parseFloat(String.valueOf(newStatistics.getTime())) / (videoView.getDuration());
                                float progressFinal = progress * 100;
                                mProgressBar.setProgress((int) progressFinal);

                            }
                        });
                    }
                    else
                    {
                        Toast.makeText(UploadVideo.this, "Oops! unable to render text pls try again", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            Config.enableStatisticsCallback(new StatisticsCallback() {
                public void apply(Statistics newStatistics) {
                    float progress = Float.parseFloat(String.valueOf(newStatistics.getTime())) / (videoView.getDuration());
                    float progressFinal = progress * 100;
                    mProgressBar.setProgress((int) progressFinal);

                }
            });
        }

        else if (isTextEnabled)
        {
            mProgressBar.setVisibility(View.VISIBLE);
            File moviesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
            String filePrefix = "txt_video";
            String fileExtn = ".mp4";

            File dest = new File(moviesDir, filePrefix + fileExtn);
            int fileNo = 0;
            while (dest.exists()) {
                fileNo++;
                dest = new File(moviesDir, filePrefix + fileNo + fileExtn);
            }

            int timex=_xDelta;
            int timey=_yDelta;

            if (daytodaytxt.getText().toString().equalsIgnoreCase("monday")||
                    daytodaytxt.getText().toString().equalsIgnoreCase("friday")||
                    daytodaytxt.getText().toString().equalsIgnoreCase("sunday"))
                timex+=50;
            else if (daytodaytxt.getText().toString().equalsIgnoreCase("tuesday"))
                timex+=70;
            else
                timex+=180;

            timey+=100;

            txt_video_path = dest.getAbsolutePath();
            String[] complexCommand = new String[]{"-i",  StoreClass.vide_path,"-b:v", "7000k", "-vf","[in]drawtext=fontfile=/system/fonts/DancingScript-Regular.ttf:text='"+daytodaytxt.getText().toString()+"':x="+_xDelta+":y="+_yDelta+":fontsize=100:fontcolor=white, drawtext=fontsize=40:fontcolor=White:fontfile='/system/fonts/Roboto-Thin.ttf':text='"+ctimetxt.getText().toString()+"':x="+timex+":y="+timey+"[out]","-y", txt_video_path};

            long executionId = FFmpeg.executeAsync(complexCommand, new ExecuteCallback() {

                @Override
                public void apply(final long executionId, final int returnCode) {
                    if (returnCode == RETURN_CODE_SUCCESS) {
                        mProgressBar.setProgress(0);
                        mProgressBar.setVisibility(View.INVISIBLE);
                        BaseCameraActivity.exportMp4ToGallery(UploadVideo.this,txt_video_path);
                        Toast.makeText(UploadVideo.this, "saved", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Toast.makeText(UploadVideo.this, "Oops! unable to render text pls try again", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            Config.enableStatisticsCallback(new StatisticsCallback() {
                public void apply(Statistics newStatistics) {
                    float progress = Float.parseFloat(String.valueOf(newStatistics.getTime())) / (videoView.getDuration());
                    float progressFinal = progress * 100;
                    mProgressBar.setProgress((int) progressFinal);

                }
            });


        }
        else if (isImageEnabled)
        {
            mProgressBar.setVisibility(View.VISIBLE);
            File moviesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
            String filePrefix = "img_video";
            String fileExtn = ".mp4";


            File dest = new File(moviesDir, filePrefix + fileExtn);
            int fileNo = 0;
            while (dest.exists()) {
                fileNo++;
                dest = new File(moviesDir, filePrefix + fileNo + fileExtn);
            }


            txt_video_path = dest.getAbsolutePath();

            String[] complexCommand = {"-i", StoreClass.vide_path,"-i",getImageFilePath(), "-filter_complex","overlay=10:x=0:y=0","-b:v", "7000k", txt_video_path};

            long executionId = FFmpeg.executeAsync(complexCommand, new ExecuteCallback() {

                @Override
                public void apply(final long executionId, final int returnCode) {
                    if (returnCode == RETURN_CODE_SUCCESS) {
                        mProgressBar.setProgress(0);
                        mProgressBar.setVisibility(View.INVISIBLE);
                        BaseCameraActivity.exportMp4ToGallery(UploadVideo.this,txt_video_path);
                        Toast.makeText(UploadVideo.this, "saved", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Toast.makeText(UploadVideo.this, "Oops! unable to render image pls try again", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            Config.enableStatisticsCallback(new StatisticsCallback() {
                public void apply(Statistics newStatistics) {
                    float progress = Float.parseFloat(String.valueOf(newStatistics.getTime())) / (videoView.getDuration());
                    float progressFinal = progress * 100;
                    mProgressBar.setProgress((int) progressFinal);

                }
            });
        }
        else
        {
            mProgressBar.setVisibility(View.INVISIBLE);
            isSavedToGallery=true;
            BaseCameraActivity.exportMp4ToGallery(UploadVideo.this,StoreClass.vide_path);
            Toast.makeText(this, "saved", Toast.LENGTH_SHORT).show();
        }

    }



    @Override
    protected void onPause() {
        super.onPause();
        stopPosition = videoView.getCurrentPosition();
        videoView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        videoView.seekTo(stopPosition);
        videoView.start();
    }

    private Runnable onEverySecond = new Runnable() {

        @Override
        public void run() {

            if (seekBar != null) {
                seekBar.setProgress(videoView.getCurrentPosition());
            }

            if (videoView.isPlaying()) {
                seekBar.postDelayed(onEverySecond, 1000);
            }

        }
    };

    void setHandler(){
        videoHandler=new Handler();
        Runnable videoRunnable = new Runnable() {
            @Override
            public void run() {
                if (videoView.getDuration() > 0) {
                    int currentPosition = videoView.getCurrentPosition();
                    seekBar.setProgress(currentPosition);
                    startTime.setText("" + convertIntToTime(currentPosition));
                    endTime.setText("" + convertIntToTime(videoView.getDuration() - currentPosition));
                }

                videoHandler.postDelayed(this, 0);
            }
        };

        videoHandler.postDelayed(videoRunnable,500);
    }

    private String convertIntToTime(int ms) {
        String time = null;
        int x,seconds,minutes,hours;
        x= ms/1000;
        seconds= x % 60;
        x /= 60;
        minutes= x % 60;
        x /= 60;
        hours= x % 24;

        if (hours != 0)
            time= String.format("%02d",hours) + ":" + String.format("%02d",minutes)+ ":" + String.format("%02d",seconds);
        else
            time= String.format("%02d",minutes)+ ":" + String.format("%02d",seconds);

        return time;
    }

    public void uploadVideo(View view) {

    }

    public void shareVideo(View view) {
        String mediapath;
        if (isTextEnabled && isImageEnabled)
            mediapath=img_video_path;
        else if (isTextEnabled|| isImageEnabled)
            mediapath=txt_video_path;
        else mediapath=StoreClass.vide_path;

        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        Uri screenshotUri = Uri.parse(mediapath);
        sharingIntent.setType("video/*");
        sharingIntent.putExtra(Intent.EXTRA_STREAM, screenshotUri);
        startActivity(Intent.createChooser(sharingIntent, "Share video using"));
    }

    public void back(View view) {
        if (!isSavedToGallery)
        {
            File fdelete = new File(BaseCameraActivity.filepath);
            if (fdelete.exists()) {
                fdelete.delete();
            }

            if (BaseCameraActivity.isMusic)
            {
                BaseCameraActivity.add_music.setImageResource(R.drawable.ic_music);
                File mdelete = new File(BaseCameraActivity.song_path);
                if (mdelete.exists()) {
                    mdelete.delete();
                }
            }

            File fdelete1 = new File(StoreClass.vide_path);
            if (fdelete1.exists()) {
                fdelete1.delete();
            }
        }
        else
        {
            File fdelete = new File(BaseCameraActivity.filepath);
            if (fdelete.exists()) {
                fdelete.delete();
            }

            if (BaseCameraActivity.isMusic)
            {
                BaseCameraActivity.add_music.setImageResource(R.drawable.ic_music);
                File mdelete = new File(BaseCameraActivity.song_path);
                if (mdelete.exists()) {
                    mdelete.delete();
                }
            }

        }

        BaseCameraActivity.isMusic=false;
        BaseCameraActivity.add_music.setImageResource(R.drawable.ic_music);
        BaseCameraActivity.music_txt.setVisibility(View.GONE);

        finish();
    }

    @Override
    public void onBackPressed() {
        if (!isSavedToGallery)
        {
            File fdelete = new File(BaseCameraActivity.filepath);
            if (fdelete.exists()) {
                fdelete.delete();
            }

            if (BaseCameraActivity.isMusic)
            {
                BaseCameraActivity.add_music.setImageResource(R.drawable.ic_music);
                File mdelete = new File(BaseCameraActivity.song_path);
                if (mdelete.exists()) {
                    mdelete.delete();
                }
            }

            File fdelete1 = new File(StoreClass.vide_path);
            if (fdelete1.exists()) {
                fdelete1.delete();
            }
        }
        else
        {
            File fdelete = new File(BaseCameraActivity.filepath);
            if (fdelete.exists()) {
                fdelete.delete();
            }

            if (BaseCameraActivity.isMusic)
            {
                BaseCameraActivity.add_music.setImageResource(R.drawable.ic_music);
                File mdelete = new File(BaseCameraActivity.song_path);
                if (mdelete.exists()) {
                    mdelete.delete();
                }
            }
        }

        BaseCameraActivity.isMusic=false;
        BaseCameraActivity.add_music.setImageResource(R.drawable.ic_music);
        BaseCameraActivity.music_txt.setVisibility(View.GONE);

        finish();
    }

    public void addText(View view) {
        if (drag_view.getVisibility()==View.VISIBLE) {
            txt_img.setImageResource(R.drawable.ic_txt_field);
            drag_view.setVisibility(View.GONE);
            isTextEnabled=false;
        }
        else
        {
            Animation fadeIn = new AlphaAnimation(0, 1);
            fadeIn.setInterpolator(new DecelerateInterpolator());
            fadeIn.setDuration(500);
            AnimationSet animation = new AnimationSet(false);
            animation.addAnimation(fadeIn);
            drag_view.setAnimation(animation);
            drag_view.setVisibility(View.VISIBLE);
            txt_img.setImageResource(R.drawable.ic_text_red);
            isTextEnabled=true;
        }
    }

    public void addFrame(View view) {
        if (show_frame_layout.getVisibility()==View.VISIBLE) {
            show_frame_layout.setVisibility(View.GONE);
            media_view.setVisibility(View.VISIBLE);
            apply_remove_frame.setVisibility(View.GONE);

        }
        else {
            media_view.setVisibility(View.GONE);
            Animation fadeIn = new AlphaAnimation(0, 1);
            fadeIn.setInterpolator(new DecelerateInterpolator());
            fadeIn.setDuration(500);
            AnimationSet animation = new AnimationSet(false);
            animation.addAnimation(fadeIn);
            show_frame_layout.setAnimation(animation);
            apply_remove_frame.setAnimation(animation);
            show_frame_layout.setVisibility(View.VISIBLE);
            apply_remove_frame.setVisibility(View.VISIBLE);
        }
    }

    public void applyFrame0(View view) {
        isImageEnabled=true;
        img_view.setVisibility(View.VISIBLE);
        image_name="frame_0";
        img_view.setImageResource(R.drawable.frame_0);
        filter_frame_imageview.setImageResource(R.drawable.ic_filter_frame_red);
        saveImage();
    }

    public void applyFrame1(View view) {
        isImageEnabled=true;
        img_view.setVisibility(View.VISIBLE);
        image_name="frame_1";
        img_view.setImageResource(R.drawable.frame_1);
        filter_frame_imageview.setImageResource(R.drawable.ic_filter_frame_red);
        saveImage();
    }

    public void applyFrame2(View view) {
        isImageEnabled=true;
        img_view.setVisibility(View.VISIBLE);
        image_name="frame_2";
        img_view.setImageResource(R.drawable.frame_2);
        filter_frame_imageview.setImageResource(R.drawable.ic_filter_frame_red);
        saveImage();
    }

    public void applyFrame3(View view) {
        isImageEnabled=true;
        img_view.setVisibility(View.VISIBLE);
        image_name="frame_3";
        img_view.setImageResource(R.drawable.frame_3);
        filter_frame_imageview.setImageResource(R.drawable.ic_filter_frame_red);
        saveImage();
    }

    public void applyFrame4(View view) {
        isImageEnabled=true;
        img_view.setVisibility(View.VISIBLE);
        image_name="frame_4";
        img_view.setImageResource(R.drawable.frame_4);
        filter_frame_imageview.setImageResource(R.drawable.ic_filter_frame_red);
        saveImage();
    }

    public void applyFrame5(View view) {
        isImageEnabled=true;
        img_view.setVisibility(View.VISIBLE);
        image_name="frame_5";
        img_view.setImageResource(R.drawable.frame_5);
        filter_frame_imageview.setImageResource(R.drawable.ic_filter_frame_red);
        saveImage();
    }

    public void applyFrame6(View view) {
        isImageEnabled=true;
        img_view.setVisibility(View.VISIBLE);
        image_name="frame_6";
        img_view.setImageResource(R.drawable.frame_6);
        filter_frame_imageview.setImageResource(R.drawable.ic_filter_frame_red);
        saveImage();
    }

    public void applyFrame7(View view) {
        isImageEnabled=true;
        img_view.setVisibility(View.VISIBLE);
        image_name="frame_7";
        img_view.setImageResource(R.drawable.frame_7);
        filter_frame_imageview.setImageResource(R.drawable.ic_filter_frame_red);
        saveImage();
    }

    public void applyFrame8(View view) {
        isImageEnabled=true;
        img_view.setVisibility(View.VISIBLE);
        image_name="frame_8";
        img_view.setImageResource(R.drawable.frame_8);
        filter_frame_imageview.setImageResource(R.drawable.ic_filter_frame_red);
        saveImage();
    }

    public void applyFrame9(View view) {
        isImageEnabled=true;
        img_view.setVisibility(View.VISIBLE);
        image_name="frame_9";
        img_view.setImageResource(R.drawable.frame_9);
        filter_frame_imageview.setImageResource(R.drawable.ic_filter_frame_red);
        saveImage();
    }

    public void applyFrame10(View view) {
        isImageEnabled=true;
        img_view.setVisibility(View.VISIBLE);
        image_name="frame_10";
        img_view.setImageResource(R.drawable.frame_10);
        filter_frame_imageview.setImageResource(R.drawable.ic_filter_frame_red);
        saveImage();
    }

    public void applyFrame11(View view) {
        isImageEnabled=true;
        img_view.setVisibility(View.VISIBLE);
        image_name="frame_11";
        img_view.setImageResource(R.drawable.frame_11);
        filter_frame_imageview.setImageResource(R.drawable.ic_filter_frame_red);
        saveImage();
    }

    public void applyFrame12(View view) {
        isImageEnabled=true;
        img_view.setVisibility(View.VISIBLE);
        image_name="frame_12";
        img_view.setImageResource(R.drawable.frame_12);
        filter_frame_imageview.setImageResource(R.drawable.ic_filter_frame_red);
        saveImage();
    }

    public void applyFrame13(View view) {
        isImageEnabled=true;
        img_view.setVisibility(View.VISIBLE);
        image_name="frame_13";
        img_view.setImageResource(R.drawable.frame_13);
        filter_frame_imageview.setImageResource(R.drawable.ic_filter_frame_red);
        saveImage();
    }

    public void applyFrame14(View view) {
        isImageEnabled=true;
        img_view.setVisibility(View.VISIBLE);
        image_name="frame_14";
        img_view.setImageResource(R.drawable.frame_14);
        filter_frame_imageview.setImageResource(R.drawable.ic_filter_frame_red);
        saveImage();
    }

    public void applyFrame15(View view) {
        isImageEnabled=true;
        img_view.setVisibility(View.VISIBLE);
        image_name="frame_15";
        img_view.setImageResource(R.drawable.frame_15);
        filter_frame_imageview.setImageResource(R.drawable.ic_filter_frame_red);
        saveImage();
    }

    public void applyFrame16(View view) {
        isImageEnabled=true;
        img_view.setVisibility(View.VISIBLE);
        image_name="frame_16";
        img_view.setImageResource(R.drawable.frame_16);
        filter_frame_imageview.setImageResource(R.drawable.ic_filter_frame_red);
        saveImage();
    }

    public void noFrame(View view) {
        isImageEnabled=false;
        filter_frame_imageview.setImageResource(R.drawable.ic_filter_frame);
        apply_remove_frame.setVisibility(View.GONE);
        img_view.setVisibility(View.GONE);
        media_view.setVisibility(View.VISIBLE);
        show_frame_layout.setVisibility(View.GONE);

        File mdelete = new File(getImageFilePath());
        if (mdelete.exists()) {
            mdelete.delete();
        }


    }

    public void applyFrame(View view) {
        if (isImageEnabled)
        {
            isImageEnabled=true;
            apply_remove_frame.setVisibility(View.GONE);
            media_view.setVisibility(View.VISIBLE);
            show_frame_layout.setVisibility(View.GONE);
        }
        else Toast.makeText(this, "Frame not selected", Toast.LENGTH_SHORT).show();

    }




    private final class ChoiceTouchListner implements View.OnTouchListener {
        @Override
        public boolean onTouch(View view, MotionEvent event) {
            final int X= (int) event.getRawX();
            final int Y= (int) event.getRawY();

            switch (event.getAction() & MotionEvent.ACTION_MASK)
            {
                case MotionEvent.ACTION_DOWN:
                    RelativeLayout.LayoutParams lParams= (RelativeLayout.LayoutParams) view.getLayoutParams();
                    _xDelta = X - lParams.leftMargin;
                    _yDelta = Y - lParams.topMargin;
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_POINTER_UP:
                case MotionEvent.ACTION_POINTER_DOWN:
                    break;

                case MotionEvent.ACTION_MOVE:
                    RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
                    layoutParams.leftMargin = X - _xDelta;
                    layoutParams.topMargin = Y - _yDelta;
                    layoutParams.rightMargin = -250;
                    layoutParams.bottomMargin = -250;
                    view.setLayoutParams(layoutParams);
                    break;
            }

            return false;
        }
    }

    public String getImageFilePath() {
        return getAndroidImageFolder().getAbsolutePath() + "/" + image_name + ".png";
    }

    private void saveImage() {

        BitmapDrawable draw = (BitmapDrawable) img_view.getDrawable();
        Bitmap bitmap = draw.getBitmap();

        File extStorageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File file = new File(extStorageDirectory, image_name + ".png");
        if (!file.exists()) {
            try {
                FileOutputStream outStream = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.WEBP_LOSSLESS, 100, outStream);
                outStream.flush();
                outStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}