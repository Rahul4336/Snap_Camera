package s.nap;

import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class FragmentViewPagerActivity extends AppCompatActivity {
    private TabLayout tab_layout;
    private ViewPager2 view_pager;
    private final String[] titles = new String[]{"Discover", "Local"};
    static MediaPlayer mediaPlayer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_view_pager);

        view_pager=findViewById(R.id.view_pager);
        tab_layout=findViewById(R.id.tab_layout);

        view_pager.setAdapter(new ViewPagerFragmentAdapter(this));
        new TabLayoutMediator(tab_layout, view_pager,
                (tab, position) -> tab.setText(titles[position])).attach();
    }


    private class ViewPagerFragmentAdapter extends FragmentStateAdapter {

        public ViewPagerFragmentAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return new Music_Fragment();
                case 1:
                    return new Local_Music_Fragment();
            }
            return new Music_Fragment();
        }

        @Override
        public int getItemCount() {
            return titles.length;
        }
    }
}