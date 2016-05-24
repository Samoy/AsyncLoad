package com.samoy.asyncload;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Samoy on 16/5/24.
 */
public class NewsAdapter extends BaseAdapter implements AbsListView.OnScrollListener{

    private List<NewsBean> mNewsBeanList;
    private LayoutInflater mInflater;
    private ImageLoader mImageLoader;
    private int mStart,mEnd;
    private static String[] URLS;
    private boolean mFirstIn;

    public static String[] getURLS() {
        return URLS;
    }

    public static void setURLS(String[] URLS) {
        NewsAdapter.URLS = URLS;
    }

    public NewsAdapter(Context context, List<NewsBean> newsBeanList, ListView listView) {
        mNewsBeanList = newsBeanList;
        mInflater = LayoutInflater.from(context);
        mImageLoader = new ImageLoader(listView);
        URLS = new String[newsBeanList.size()];
        for (int i = 0; i < newsBeanList.size(); i++){
            URLS[i] = newsBeanList.get(i).getNewsIconUrl();
        }
        mFirstIn = true;

        listView.setOnScrollListener(this);
    }

    @Override
    public int getCount() {
        return mNewsBeanList.size();
    }

    @Override
    public Object getItem(int position) {
        return mNewsBeanList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null){
            viewHolder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.item_layout,null);
            viewHolder.title = (TextView) convertView.findViewById(R.id.tv_title);
            viewHolder.content = (TextView) convertView.findViewById(R.id.tv_content);
            viewHolder.icon = (ImageView) convertView.findViewById(R.id.image);
            convertView.setTag(viewHolder);
        }else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.icon.setImageResource(R.mipmap.ic_launcher);
        String url = mNewsBeanList.get(position).getNewsIconUrl();
        viewHolder.icon.setTag(url);
        //new ImageLoader().showImageByThread(viewHolder.icon,mNewsBeanList.get(position).getNewsIconUrl());
        mImageLoader.showImageByAsyncTask(viewHolder.icon,mNewsBeanList.get(position).getNewsIconUrl());
        viewHolder.title.setText(mNewsBeanList.get(position).getNewsTitle());
        viewHolder.content.setText(mNewsBeanList.get(position).getNewsContent());

        return convertView;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == SCROLL_STATE_IDLE){
            //加载可见项
            mImageLoader.loadImages(mStart,mEnd);
        }else {
            //停止任务
            mImageLoader.cancelAllTask();
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        mStart = firstVisibleItem;
        mEnd = firstVisibleItem + visibleItemCount;
        if (mFirstIn && visibleItemCount > 0){
            mImageLoader.loadImages(mStart,mEnd);
            mFirstIn = false;
        }
    }

    class ViewHolder{
        private TextView title,content;
        private ImageView icon;
    }
}