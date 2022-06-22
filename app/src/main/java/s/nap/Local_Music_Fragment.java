package s.nap;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.rangeview.RangeSeekBarView;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;

import static s.nap.FragmentViewPagerActivity.mediaPlayer;

public class Local_Music_Fragment extends Fragment {

    private RecyclerView recycler_view;
    private ArrayList<ModelAudio> audioArrayList;

    private int audio_index = 0;
    private ImageView pause,prev, next;
    private TextView audio_name;
    private BottomSheetDialog mBottomSheetDialog;

    private RangeSeekBarView audiorangeseekbar;
    private int musictotSecond;
    private TextView startTime,endTime,vid_duration,tottime,music_ok;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view=inflater.inflate(R.layout.fragment_local__music_, container, false);
        recycler_view=view.findViewById(R.id.recycler_view);

        mBottomSheetDialog = new BottomSheetDialog(getActivity());
        View sheetView = getActivity().getLayoutInflater().inflate(R.layout.edit_music_bottom_sheet, null);
        mBottomSheetDialog.setContentView(sheetView);

        pause = sheetView.findViewById(R.id.pause);
        audio_name = sheetView.findViewById(R.id.audio_name);
        prev = sheetView.findViewById(R.id.prev);
        next = sheetView.findViewById(R.id.next);
        audiorangeseekbar=sheetView.findViewById(R.id.audiorangeseekbar);
        startTime=sheetView.findViewById(R.id.starttime);
        endTime=sheetView.findViewById(R.id.endtime);
        vid_duration=sheetView.findViewById(R.id.vid_duration);
        tottime=sheetView.findViewById(R.id.tottime);
        music_ok=sheetView.findViewById(R.id.music_ok);

        ImageView dismiss_dialog= sheetView.findViewById(R.id.dismiss_dialog);

        dismiss_dialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BaseCameraActivity.music_txt.setVisibility(View.GONE);
                BaseCameraActivity.add_music.setImageResource(R.drawable.ic_music);
                BaseCameraActivity.isMusic=false;
                mBottomSheetDialog.dismiss();
                if (mediaPlayer.isPlaying())
                {
                    mediaPlayer.pause();
                }
            }
        });

        music_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                }
                BaseCameraActivity.music_txt.setVisibility(View.VISIBLE);
                BaseCameraActivity.music_txt.setText(audio_name.getText().toString());
                BaseCameraActivity.add_music.setImageResource(R.drawable.ic_audio_music);
                BaseCameraActivity.trimAudio();
                getActivity().finish();
            }
        });

        setAudio();

        return view;
    }


    public void getAudioFiles() {
        ContentResolver contentResolver = getActivity().getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = contentResolver.query(uri, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {

                String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                String duration = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
                String url = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                ModelAudio modelAudio = new ModelAudio();
                modelAudio.setaudioTitle(title);
                modelAudio.setaudioArtist(artist);
                modelAudio.setaudioUri(Uri.parse(url));
                modelAudio.setaudioDuration(duration);
                audioArrayList.add(modelAudio);

            } while (cursor.moveToNext());
        }

        AudioAdapter adapter = new AudioAdapter(getActivity(), audioArrayList);
        recycler_view.setAdapter(adapter);

        adapter.setOnItemClickListener(new AudioAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int pos, View v) {
                mBottomSheetDialog.show();
                playAudio(pos);
            }
        });
    }



    public static class AudioAdapter extends RecyclerView.Adapter<AudioAdapter.viewHolder> {

        Context context;
        ArrayList<ModelAudio> audioArrayList;
        public OnItemClickListener onItemClickListener;

        public AudioAdapter(Context context, ArrayList<ModelAudio> audioArrayList) {
            this.context = context;
            this.audioArrayList = audioArrayList;
        }

        @Override
        public AudioAdapter.viewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(context).inflate(R.layout.audio_list, viewGroup, false);
            return new viewHolder(view);
        }

        @Override
        public void onBindViewHolder(final AudioAdapter.viewHolder holder, final int i) {
            holder.title.setText(audioArrayList.get(i).getaudioTitle());
            holder.artist.setText(audioArrayList.get(i).getaudioArtist());
        }

        @Override
        public int getItemCount() {
            return audioArrayList.size();
        }

        public class viewHolder extends RecyclerView.ViewHolder {
            TextView title, artist;
            public viewHolder(View itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.title);
                artist = itemView.findViewById(R.id.artist);

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onItemClickListener.onItemClick(getAdapterPosition(), v);
                    }
                });
            }
        }

        public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
            this.onItemClickListener = onItemClickListener;
        }

        public interface OnItemClickListener {
            void onItemClick(int pos, View v);
        }
    }

    void setAudio()
    {

        recycler_view.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        audioArrayList = new ArrayList<>();
        mediaPlayer = new MediaPlayer();
        getAudioFiles();

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                audio_index++;
                if (audio_index < (audioArrayList.size())) {
                    playAudio(audio_index);
                } else {
                    audio_index = 0;
                    playAudio(audio_index);
                }

            }
        });

        if (!audioArrayList.isEmpty()) {
            prevAudio();
            nextAudio();
            setPause();
        }


    }

    public void playAudio(int pos) {
        try  {
            mediaPlayer.reset();
            //set file path
            mediaPlayer.setDataSource(getActivity(), audioArrayList.get(pos).getaudioUri());
            BaseCameraActivity.selectedpath= String.valueOf(audioArrayList.get(pos).getaudioUri());
            mediaPlayer.prepare();
            mediaPlayer.start();
            pause.setImageResource(R.drawable.ic_pause);
            audio_name.setText(audioArrayList.get(pos).getaudioTitle());
            audio_index=pos;


            Uri uri = Uri.parse(String.valueOf(audioArrayList.get(pos).getaudioUri()));
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(getActivity(),uri);
            String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            musictotSecond = Integer.parseInt(durationStr);
            tottime.setText(convertIntToTime(musictotSecond));
            vid_duration.setText("Video Recording Length : "+musictotSecond/5000+" sec");

            BaseCameraActivity.musicstarttime=0;
            BaseCameraActivity.musicendtime=musictotSecond/5000;

            RangeSeekBarView.TimeLineChangeListener listener = new RangeSeekBarView.TimeLineChangeListener() {
                @Override
                public void onRangeChanged(float start, float end) {
                    mediaPlayer.seekTo((int) (musictotSecond*start));
                    int time= ((int) (musictotSecond * end)-(int) (musictotSecond * start));
                    BaseCameraActivity.musicendtime=time/5000;
                    BaseCameraActivity.musicstarttime=(int) ((musictotSecond * start)/1000);
                    BaseCameraActivity.video_length=BaseCameraActivity.musicendtime*1000;
                    mediaPlayer.start();


                    tottime.setText(convertIntToTime(musictotSecond));
                    startTime.setText(convertIntToTime((int) (musictotSecond*start)));
                    endTime.setText(convertIntToTime((int) (musictotSecond * end)));

                    vid_duration.setText("Video Recording Length : "+BaseCameraActivity.musicendtime+" sec");
                }

                @Override
                public void onRangeMove(float start, float end) {

                }
            };

            audiorangeseekbar.addIndicatorChangeListener(listener);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setPause() {
        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    pause.setImageResource(R.drawable.ic_play);
                } else {
                    mediaPlayer.start();
                    pause.setImageResource(R.drawable.ic_pause);
                }
            }
        });
    }

    public void prevAudio() {
        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (audio_index > 0) {
                    audio_index--;
                } else {
                    audio_index = audioArrayList.size() - 1;
                }
                playAudio(audio_index);
            }
        });
    }

    public void nextAudio() {
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (audio_index < (audioArrayList.size()-1)) {
                    audio_index++;
                } else {
                    audio_index = 0;
                }
                playAudio(audio_index);
            }
        });
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

    @Override
    public void onDestroy() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        super.onDestroy();
    }

    @Override
    public void onPause() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
        super.onPause();
    }
}