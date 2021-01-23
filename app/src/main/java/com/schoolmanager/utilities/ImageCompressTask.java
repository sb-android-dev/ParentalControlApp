package com.schoolmanager.utilities;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by root on 9/14/17.
 */

public class ImageCompressTask implements Runnable {

    private Context mContext;
    private List<String> originalPaths = new ArrayList<>();
    private String originalPath = "";
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private List<File> result = new ArrayList<>();
//    private File result = null;
    private IImageCompressTaskListener mIImageCompressTaskListener;
    private int width = 300, height = 300;


    public ImageCompressTask(Context context, String path, int width, int height,
                             IImageCompressTaskListener compressTaskListener) {

        originalPaths.add(path);
//        originalPath = path;
        mContext = context;
        if(width >= 300)
            this.width = width;
        if(height >= 300)
            this.height = height;
        mIImageCompressTaskListener = compressTaskListener;
    }
    public ImageCompressTask(Context context, List<String> paths, int width, int height,
                             IImageCompressTaskListener compressTaskListener) {
        originalPaths = paths;
        mContext = context;
        if(width >= 300)
            this.width = width;
        if(height >= 300)
            this.height = height;
        mIImageCompressTaskListener = compressTaskListener;
    }
    @Override
    public void run() {

        try {

//            Loop through all the given paths and collect the compressed file from Util.getCompressed(Context, String)
            for (String path : originalPaths) {
                File file = Util.getCompressed(mContext, path, width, height);
                //add it!
                result.add(file);
            }
//            File file = Util.getCompressed(mContext, originalPath, width, height);
//            result = file;

        }catch (final IOException ex) {
            //There was an error, report the error back through the callback
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if(mIImageCompressTaskListener != null)
                        mIImageCompressTaskListener.onError(ex);
                }
            });
        } finally {
            //use Handler to post the result back to the main Thread
            mHandler.post(new Runnable() {
                @Override
                public void run() {

                    if(mIImageCompressTaskListener != null) {
                            mIImageCompressTaskListener.onComplete(result);
                    }
                }
            });
        }
    }
}
