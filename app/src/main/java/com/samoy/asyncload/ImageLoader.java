package com.samoy.asyncload;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.LruCache;
import android.widget.ImageView;
import android.widget.ListView;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Samoy on 16/5/24.
 */
public class ImageLoader {

    private ImageView mImageView;
    private String mUrl;
    private ListView mListView;
    private Set<NewsAsyncTask> mTasks;
    private LruCache<String,Bitmap> mLruCache;

    public ImageLoader(ListView listView) {
        mListView = listView;
        mTasks = new HashSet<>();
        //获取最大可用内存
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        int cacheSize = maxMemory/4;
        mLruCache = new LruCache<String,Bitmap>(cacheSize){
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount();
            }
        };
    }

    public void addBitmapToCache(String url,Bitmap bitmap){
        if (getBitmapFromCache(url) == null){
            mLruCache.put(url,bitmap);
        }
    }

    public Bitmap getBitmapFromCache(String url){
        return mLruCache.get(url);
    }

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mImageView.getTag().equals(mUrl)){
                mImageView.setImageBitmap((Bitmap) msg.obj);
            }
        }
    };

    public void  showImageByThread(ImageView imageView, final String url){
        mImageView = imageView;
        mUrl = url;
        new Thread(){
            @Override
            public void run() {
                super.run();
                Bitmap bitmap = getBitMapForUrl(url);
                Message message =  Message.obtain();
                message.obj = bitmap;
                mHandler.sendMessage(message);
            }
        }.start();
    }

    public Bitmap getBitMapForUrl(String urlString){
        Bitmap bitmap = null;
        InputStream inputStream = null;
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(urlString).openConnection();
            inputStream = new BufferedInputStream(connection.getInputStream());
            bitmap = BitmapFactory.decodeStream(inputStream);
            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                assert inputStream != null;
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }

    public void showImageByAsyncTask(ImageView imageView,String url){
        Bitmap bitmap = getBitmapFromCache(url);
        if (bitmap == null){
           // new NewsAsyncTask(imageView,url).execute(url);
            imageView.setImageResource(R.mipmap.ic_launcher);
        }else {
            imageView.setImageBitmap(bitmap);
        }
    }

    public void  loadImages(int start, int end){
        for (int i = start;i< end;i++){
            String url = NewsAdapter.getURLS()[i];
            Bitmap bitmap = getBitmapFromCache(url);
            if (bitmap == null){
                NewsAsyncTask task = new NewsAsyncTask(url);
                task.execute(url);
                mTasks.add(task);
            }else {
                ImageView imageView = (ImageView) mListView.findViewWithTag(url);
                imageView.setImageBitmap(bitmap);
            }
        }
    }

    public void cancelAllTask(){
        if (mTasks != null){
            for (NewsAsyncTask task: mTasks) {
                task.cancel(true);
            }
        }
    }

    class NewsAsyncTask extends AsyncTask<String,Void,Bitmap>{
        //private ImageView mImageView;
        private String mUrl;
        public NewsAsyncTask(/*ImageView imageView,*/String url) {
            //mImageView = imageView;
            mUrl = url;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            Bitmap bitmap = getBitMapForUrl(params[0]);
            if(bitmap !=   null){
                addBitmapToCache(params[0],bitmap);
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
//            if (mImageView.getTag().equals(mUrl)){
//                mImageView.setImageBitmap(bitmap);
//            }
            ImageView imageView = (ImageView) mListView.findViewWithTag(mUrl);
            if (imageView!=null&&bitmap!=null){
                imageView.setImageBitmap(bitmap);
            }
            mTasks.remove(this);
        }
    }
}