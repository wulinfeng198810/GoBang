package com.example.wulinfeng.gobang;

import android.content.Context;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity {

    private Context context;
    private static final int MAXN = 11;
    private int item_size;

    private RecyclerView mRecyclerView;
    private GridLayoutManager mGridLayoutManager;
    private GridAdapter mGridAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        setupUI();
    }

    private void setupUI() {
        item_size = getItemSize();
        mRecyclerView = findViewById(R.id.grid_recycle_view);
        mGridLayoutManager = new GridLayoutManager(context, MAXN, OrientationHelper.VERTICAL, false);
        mRecyclerView.setLayoutManager(mGridLayoutManager);
        mGridAdapter = new GridAdapter();
        mRecyclerView.setAdapter(mGridAdapter);
    }

    public int getItemSize() {
        int itemSize = getResources().getDisplayMetrics().widthPixels;
        return 100;
    }

    class GridAdapter extends RecyclerView.Adapter<GridAdapter.ViewHolder> {

        @Override
        public GridAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.grid_item_layout, null);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(GridAdapter.ViewHolder holder, int position) {
            holder.itemView.setTag(position);
        }

        @Override
        public int getItemCount() {
            return MAXN*MAXN;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            ImageView mImageView;
            public ViewHolder(View itemView) {
                super(itemView);
                mImageView = itemView.findViewById(R.id.item_image_view);
                mImageView.setLayoutParams(new LinearLayout.LayoutParams(item_size, item_size));
            }
        }
    }
}
