package com.example.wulinfeng.gobang;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private Context context;
    private static final int MAXN = 11;
    private int item_size;

    private Button mPlayGameBtn;
    private TextView mTurnTextView;
    private RecyclerView mRecyclerView;
    private GridLayoutManager mGridLayoutManager;
    private GridAdapter mGridAdapter;

    private static final int NONE = 0;
    private static final int PLAYER1 = 1;
    private static final int PLAYER2 = 2;
    private Drawable[] drawableCell = new Drawable[3];  // 0 is NONE, 1 is white -> PLAYER1, 2 is black -> PLAYER1
    private int[][] valueCell = new int[MAXN][MAXN];
    private int winner_play;    // who is winner? 0 is NONE, 1 is PLAYER1, 2 is PLAYER1
    private int xMove, yMove;   // xMove, yMove is the position [y,x]
    private int turnPlay;   // who play first, 1  or 2
    private boolean gameStarted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        loadResources();
        setupUI();
        addListener();


        TextView textView;
    }

    private void loadResources() {
        drawableCell[0] = null;
        drawableCell[1] = getResources().getDrawable(R.drawable.ic_lens_white_30dp);
        drawableCell[2] = getResources().getDrawable(R.drawable.ic_lens_black_30dp);
    }

    private void addListener() {
        mPlayGameBtn = findViewById(R.id.play_game_button);
        mTurnTextView = findViewById(R.id.turn_text_view);
        mTurnTextView.setText(R.string.not_start_title);
        mPlayGameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initGame();
                playGame();
            }
        });

        mGridAdapter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                makeMove(view,position);
            }
        });
    }

    private void animateView(View targetView) {
        ObjectAnimator objectAnimatorX = ObjectAnimator.ofFloat(targetView,"scaleX",1f,1.2f,1);
        objectAnimatorX.setRepeatCount(5);
        objectAnimatorX.setDuration(2000).start();

        ObjectAnimator objectAnimatorY = ObjectAnimator.ofFloat(targetView,"scaleY",1f,1.2f,1);
        objectAnimatorY.setRepeatCount(5);
        objectAnimatorY.setDuration(2000).start();
    }

    private void initGame() {
        winner_play = 0;

        // clear data
        for (int i = 0; i < MAXN; i++) {
            for (int j = 0; j < MAXN; j++) {
                valueCell[i][j] = 0;
            }
        }
        mGridAdapter.notifyDataSetChanged();
    }

    private void playGame() {
        // Random who play first
        Random random = new Random();
        turnPlay = random.nextInt(2)+1;
        refreshTurn();
        Toast.makeText(context,playerDescript(turnPlay) + " First", Toast.LENGTH_SHORT).show();
    }

    private void makeMove(View view, int position) {
        int y = position/MAXN;
        int x = position%MAXN;
        int value = valueCell[y][x];

        // is clicked?
        if ( value != NONE) {
            return;
        } else {
            valueCell[y][x] = turnPlay;
        }
        ImageView imageView = view.findViewById(R.id.item_image_view);
        imageView.setImageDrawable(drawableCell[turnPlay]);

        // all clicked?
        GameResult gameResult = GoBangGameUtil.gameResult(valueCell, y, x);
        if (GoBangGameUtil.isFull(valueCell)) {
            gameOver(NONE);
            return;
        } else if ( gameResult.getWinner() != NONE) {
            gameOver(gameResult.getWinner());
            animateView(gameResult.getIndexes());
            return;
        }

        if (turnPlay == PLAYER1) {
            turnPlay = PLAYER2;
        } else {
            turnPlay = PLAYER1;
        }
        refreshTurn();
    }

    private void animateView(int[] indexes) {
        for (int i = 0; i < indexes.length; i++) {
            GridAdapter.ViewHolder viewHolder = (GridAdapter.ViewHolder) mRecyclerView.findViewHolderForLayoutPosition(indexes[i]);
            animateView(viewHolder.mImageView);
        }
    }

    private void gameOver(int player) {
        if (player == NONE) { // draw, no body win
            mTurnTextView.setCompoundDrawables(null,null,null,null);
            mTurnTextView.setText(R.string.winner_draw);
            animateView(mTurnTextView);
        } else {
            mTurnTextView.setText("Win");
            animateView(mTurnTextView);
        }
    }

    private void refreshTurn() {
        mTurnTextView.setText(playerDescript(turnPlay) + " Turn");
        Drawable drawable = drawableCell[turnPlay];
        drawable.setBounds(0, 0, DisplayUtil.dip2px(context,30), DisplayUtil.dip2px(context,30));
        mTurnTextView.setCompoundDrawables(drawable, null, null, null);
        animateView(mTurnTextView);
    }

    private String playerDescript(int player) {
        return player == 1 ? "White" : "Black";
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
        return itemSize/MAXN;
    }

    /**
     * GridAdapter
     */
    class GridAdapter extends RecyclerView.Adapter<GridAdapter.ViewHolder> implements View.OnClickListener {

        private AdapterView.OnItemClickListener mOnItemClickListener = null;

        @Override
        public GridAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.grid_item_layout, null);
            view.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, item_size));
            view.setOnClickListener(this);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(GridAdapter.ViewHolder holder, int position) {
            holder.itemView.setTag(position);
            holder.mImageView.setImageDrawable(drawableCell[0]);
            holder.mTextView.setText(String.valueOf(position));
        }

        @Override
        public int getItemCount() {
            return MAXN*MAXN;
        }

        public void setOnItemClickListener(AdapterView.OnItemClickListener onItemClickListener) {
            this.mOnItemClickListener = onItemClickListener;
        }

        @Override
        public void onClick(View v) {
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onItemClick(null, v, (int)v.getTag(), 0);
            }
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            ImageView mImageView;
            TextView mTextView;
            public ViewHolder(View itemView) {
                super(itemView);
                mImageView = itemView.findViewById(R.id.item_image_view);
                mTextView = itemView.findViewById(R.id.item_index_text);
            }
        }
    }
}

class GoBangGameUtil {

    public static boolean isFull(int[][] values) {
        for (int i = 0; i < values.length; i++) {
            for (int j = 0; j < values[i].length; j++) {
                if (values[i][j] == 0) {
                    return false;
                }
            }
        }
        return true;
    }

    //判断输赢的函数，传入当前点位置坐标
    public static GameResult gameResult(int[][] values, int y3, int x3) {
        int w = values[0].length;
        int h = values.length;
        int winIndex = 0;

        {// 横向数据流
            StringBuffer buf = new StringBuffer();
            int[] allIndexCell = new int[w];
            for (int y = y3, x = 0; x < w; x++) {
                buf.append(values[y][x]);
                allIndexCell[x] = y*w + x;
            }

            String allValueString = buf.toString();
            winIndex = stepJudgeWin(allValueString);
            if ( winIndex != 0 ) {
                return new GameResult(winIndex, winIndex(allIndexCell, matchedStartIndex(allValueString)));
            }
        }


        {// 纵向数据流
            StringBuffer buf = new StringBuffer();
            int[] allIndexCell = new int[w];
            for (int y = 0, x = x3; y < h; y++) {
                buf.append(values[y][x]);
                allIndexCell[y] = y*w + x;
            }
            String allValueString = buf.toString();
            winIndex = stepJudgeWin(allValueString);
            if ( winIndex != 0 ) {
                return new GameResult(winIndex, winIndex(allIndexCell, matchedStartIndex(allValueString)));
            }
        }

        {// 二四象限数据流
            StringBuffer buf = new StringBuffer();
            int[] allIndexCell = new int[w];
            if (y3 >= x3) {
                for (int y = y3 - x3, x = 0; y < h; y++, x++) {
                    buf.append(values[y][x]);
                    allIndexCell[x] = y * w + x;
                }
            } else {
                for (int y = 0, x = x3 - y3; x < w; y++, x++) {
                    buf.append(values[y][x]);
                    allIndexCell[y] = y * w + x;
                }
            }
            String allValueString = buf.toString();
            winIndex = stepJudgeWin(allValueString);
            if ( winIndex != 0 ) {
                return new GameResult(winIndex, winIndex(allIndexCell, matchedStartIndex(allValueString)));
            }
        }

        {// 一三象限数据流
            StringBuffer buf = new StringBuffer();
            int[] allIndexCell = new int[w];
            if ((x3 + y3) < h) {
                for (int x = x3 + y3, y = 0; y <= x3 + y3; y++, x--) {
                    buf.append(values[y][x]);
                    allIndexCell[y] = y * w + x;
                }

            } else {
                for (int x = h - 1, y = x3 + y3 - (h - 1); y < h; y++, x--) {
                    buf.append(values[y][x]);
                    allIndexCell[x] = y * w + x;
                }
                for (int start = 0, end = allIndexCell.length - 1; start < end; start++, end--) {
                    int temp = allIndexCell[end];
                    allIndexCell[end] = allIndexCell[start];
                    allIndexCell[start] = temp;
                }
            }
            String allValueString = buf.toString();
            winIndex = stepJudgeWin(allValueString);
            if ( winIndex != 0 ) {
                return new GameResult(winIndex, winIndex(allIndexCell, matchedStartIndex(allValueString)));
            }
        }

        return new GameResult(0, null);
    }

    private static int stepJudgeWin(String str) {
        if (str.matches("\\d*1{5}\\d*")) {
            return 1;
        }
        if (str.matches("\\d*2{5}\\d*")) {
            return 2;
        }
        return 0;
    }

    private static int matchedStartIndex(String str) {
        int index = -1;
        if ( (index = str.indexOf("11111")) == -1 ) {
            return str.indexOf("22222");
        }
        return index;
    }

    public static int[] winIndex(int[] fromAll, int startIndex) {
        int[] winIndex = new int[5];
        for (int i = 0; i < 5; i++) {
            winIndex[i] = fromAll[i+startIndex];
        }
        return winIndex;
    }
}

class GameResult {
    private int winner;
    private int[] indexes;

    public GameResult(int winner, int[] indexes) {
        this.winner = winner;
        this.indexes = indexes;
    }

    public int getWinner() {
        return winner;
    }

    public int[] getIndexes() {
        return indexes;
    }
}
