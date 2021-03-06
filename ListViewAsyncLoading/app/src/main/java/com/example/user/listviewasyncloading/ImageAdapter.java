package com.example.user.listviewasyncloading;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by user on 2017/12/1.
 */

public class ImageAdapter extends ArrayAdapter<String> {
    //读片缓存技术核心类，用于缓存好下载好的图片，在图片内存达到设定值的时候会将最少用到的图片移除掉
    private LruCache<String, BitmapDrawable> mMemoryCache;
    private static final String TAG = "ImageAdapter";
    private ListView mListView;

    public ImageAdapter(Context context, int resource, String[] objects) {
        super(context, resource, objects);
        //获取应用程序最大可用内存
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        Log.d(TAG, "可用最大内存为" + maxMemory);
        int cacheSize = maxMemory/3;
        mMemoryCache = new LruCache<String, BitmapDrawable>(cacheSize) {
            @Override
            protected int sizeOf(String key, BitmapDrawable drawable) {
                return drawable.getBitmap().getByteCount();
            }
        };
    }

    @NonNull
    @Override
    //设置findViewWithTag,防止异步加载乱序的问题
    //设置tag需要获取listview的实例
    //通过getView传递进来的parent，可获得listView的实例
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if(mListView==null){
            mListView=(ListView)parent;
        }
        String url = getItem(position);
        View view;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.image_item, null);
        } else {
            view = convertView;
        }
        ImageView image = (ImageView) view.findViewById(R.id.image);
        //给imageView设定对应的tag
        image.setTag(url);
        image.setImageResource(R.drawable.th);
        BitmapDrawable drawable = getBitmapFromMemoryCache(url);
        if (drawable != null) {
            image.setImageDrawable(drawable);
        } else {
            BitmapWorkerTask task = new BitmapWorkerTask();
            task.execute(url);
        }
        return view;
    }

    //将一张图片存储到LruCache中
    //key是url
    public void addBitmapToMemoryCache(String key,BitmapDrawable drawable){
        if(getBitmapFromMemoryCache(key)==null){
            mMemoryCache.put(key,drawable);
        }
    }

    //从LruCache中获取一张图片，如果不存在，则返回null
    public BitmapDrawable getBitmapFromMemoryCache(String key){
        return mMemoryCache.get(key);
    }

    //异步下载任务

    class BitmapWorkerTask extends AsyncTask<String,Void,BitmapDrawable>{
//        private ImageView mImageView;
        String imageUrl;

//        public BitmapWorkerTask(ImageView imageView){
//            mImageView=imageView;
//        }

        @Override
        protected BitmapDrawable doInBackground(String... strings) {
            imageUrl=strings[0];

            //后台开始下载图片
            Bitmap bitmap=downloadBitmap(imageUrl);
            BitmapDrawable drawable=new BitmapDrawable(getContext().getResources(), bitmap);
            addBitmapToMemoryCache(imageUrl,drawable);
            return drawable;
        }

        //设置tag
        @Override
        protected void onPostExecute(BitmapDrawable drawable) {
            //下载完成后，找到对应的tag，刷新对应iamgeview上的图片
            ImageView imageView=(ImageView)mListView.findViewWithTag(imageUrl);
            if(imageView!=null&&drawable!=null){
                imageView.setImageDrawable(drawable);
            }
        }

        //建立http请求，获取Bitmap对象
        private Bitmap downloadBitmap(String imageUrl){
            Bitmap bitmap=null;
            HttpURLConnection con=null;
            try{
                URL url=new URL(imageUrl);
                con=(HttpURLConnection)url.openConnection();
                con.setConnectTimeout(5*1000);
                con.setReadTimeout(10*1000);
                bitmap= BitmapFactory.decodeStream(con.getInputStream());

            }catch (Exception e){
                e.printStackTrace();
            }finally {
                if(con!=null){
                    con.disconnect();
                }
            }
            return bitmap;
        }
    }
}
