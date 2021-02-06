package com.schoolmanager.utilities;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Method;

public class PathFinder {

    public static final String TAG = "path_finder";

    public static final String GOOGLE_PHOTOS_URI_AUTHORITY = "com.google.android.apps.photos.content";
    public static final String GOOGLE_DRIVE_URI_AUTHORITY = "com.google.android.apps.docs.storage";
    public static final String GOOGLE_DRIVE_LEGACY_URI_AUTHORITY = "com.google.android.apps.docs.storage.legacy";
    public static final String DOWNLOAD_DOCUMENT_URI_AUTHORITY = "com.android.providers.downloads.documents";
    public static final String EXTERNAL_STORAGE_URI_AUTHORITY = "com.android.externalstorage.documents";
    public static final String MEDIA_DOCUMENT_URI_AUTHORITY = "com.android.providers.media.documents";
    public static final String WHATS_APP_URI_AUTHORITY = "com.whatsapp.provider.media";

    private Context mContext;

    public PathFinder(Context mContext) {
        this.mContext = mContext;
    }

    public String getPath(Uri mUri){
        Log.e(TAG, "getPath: mUri: " + mUri);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

            if (mUri.getAuthority() != null) {
                switch (mUri.getAuthority()) {
                    case EXTERNAL_STORAGE_URI_AUTHORITY:
                        return getPathFromExternalStorage(mUri);

                    case DOWNLOAD_DOCUMENT_URI_AUTHORITY:
                        return getPathFromDownloadProvider(mUri);

                    case MEDIA_DOCUMENT_URI_AUTHORITY:
                        return getPathFromMediaProvider(mUri);

                    case GOOGLE_PHOTOS_URI_AUTHORITY:
                        return getPathFromGooglePhotos(mUri);

                    case GOOGLE_DRIVE_URI_AUTHORITY:
                    case GOOGLE_DRIVE_LEGACY_URI_AUTHORITY:
                        return getPathFromDrive(mUri);

                    case WHATS_APP_URI_AUTHORITY:
                        return copyFileToInternalStorage(mUri, "WhatsApp");
                }
            }

            if(mUri.getScheme() != null) {
                if (mUri.getScheme().equalsIgnoreCase("content")) {
                    switch (mUri.getAuthority()){
                        case GOOGLE_PHOTOS_URI_AUTHORITY:
                            return getPathFromGooglePhotos(mUri);
                        case GOOGLE_DRIVE_URI_AUTHORITY:
                        case GOOGLE_DRIVE_LEGACY_URI_AUTHORITY:
                            return getPathFromDrive(mUri);
                    }

                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
                        return copyFileToInternalStorage(mUri, "User Files");
                    } else {
                        return getDataColumn(mContext, mUri, null, null);
                    }
                }

                if(mUri.getScheme().equalsIgnoreCase("file")){
                    return getPathFromFileScheme(mUri);
                }
            }
        } else {

            if(mUri.getAuthority().equals(WHATS_APP_URI_AUTHORITY)){
                return copyFileToInternalStorage(mUri, "WhatsApp");
            }

            if ("content".equalsIgnoreCase(mUri.getScheme())) {
                return getDataColumn(mContext, mUri, null, null);
            } else if("file".equalsIgnoreCase(mUri.getScheme())) {
                return getPathFromFileScheme(mUri);
            }

        }

        return null;
    }

    private String getPathFromExternalStorage(Uri uri) {
        String docId = DocumentsContract.getDocumentId(uri);
        String[] split = docId.split(":");
        String type = split[0];
        String relativePath = "/" + split[1];
        String fullPath = null;

        if(type.equalsIgnoreCase("primary")){
            fullPath = Environment.getExternalStorageDirectory() + relativePath;
            Log.e(TAG, "ExternalStorage: primary: " + fullPath);

            if(fileExists(fullPath))
                return fullPath;
        }

        fullPath = System.getenv("SECONDARY_STORAGE") + relativePath;
        if(fileExists(fullPath)) {
            Log.e(TAG, "ExternalStorage: Secondary Storage: " + fullPath);
            return fullPath;
        }

        fullPath = System.getenv("EXTERNAL_STORAGE") + relativePath;
        if(fileExists(fullPath)){
            Log.e(TAG, "ExternalStorage: External Storage: " + fullPath);
            return fullPath;
        }

        return fullPath;
    }

    private String getPathFromDownloadProvider(Uri uri){
        String fullPath = null;
        String id;

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){

            try (Cursor cursor = mContext.getContentResolver().query(uri,
                    new String[]{MediaStore.MediaColumns.DISPLAY_NAME},
                    null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    String fileName = cursor.getString(0);
                    fullPath = Environment.getExternalStorageDirectory() + "/Download/" + fileName;

                    if (!TextUtils.isEmpty(fullPath) && fileExists(fullPath)) {
                        Log.e(TAG, "DownloadProvider: DataColumn: " + fullPath);
                        return fullPath;
                    }
                }
            }

            id = DocumentsContract.getDocumentId(uri);
            if(!TextUtils.isEmpty(id)){
                if(id.startsWith("raw:")) {
                    fullPath = id.replaceFirst("raw:", "");
                    Log.e(TAG, "DownloadProvider: raw: " + fullPath);
                    return fullPath;
                }

                String[] contentUriPrefixesToTry = new String[]{
                        "content://downloads/public_downloads",
                        "content://downloads/my_downloads"
                };

                for (String contentUriPrefix : contentUriPrefixesToTry) {
                    try {
                        final Uri contentUri = ContentUris.withAppendedId(Uri.parse(contentUriPrefix), Long.parseLong(id));

                        fullPath = getDataColumn(mContext, contentUri, null, null);
                        Log.e(TAG, "DownloadProvider: Using Id: " + fullPath);
                        return fullPath;
                    } catch (NumberFormatException e) {
                        //In Android 8 and Android P the id is not a number
                        fullPath = uri.getPath()
                                .replaceFirst("^/document/raw:", "")
                                .replaceFirst("^raw:", "");
                        Log.e(TAG, "DownloadProvider: Using  Id(For O & P): " + fullPath);
                        return fullPath;
                    }
                }
            }
        } else {
            id = DocumentsContract.getDocumentId(uri);
            Uri cUri = null;

            if(id.startsWith("raw:")){
                fullPath = id.replaceFirst("raw:", "");
                Log.e(TAG, "DownloadProvider: raw: " + fullPath);
                return fullPath;
            }

            try{
                cUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.parseLong(id));
            } catch (NumberFormatException e){
                Log.e(TAG, "getPathFromDownloadProvider: " + e.getMessage());
            }

            if(cUri != null){
                fullPath = getDataColumn(mContext, cUri, null, null);
                Log.e(TAG, "DownloadProvider: ContentUri: " + fullPath);
                return fullPath;
            }
        }

        return fullPath;
    }

    private String getPathFromMediaProvider(Uri uri) {
        String docId = DocumentsContract.getDocumentId(uri);
        String[] split = docId.split(":");
        String type = split[0];
        String fullPath = null;

        Uri contentUri = null;

        switch (type.toLowerCase()){
            case "image":
            case "content":
                contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                break;
            case "video":
                contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                break;
            case "audio":
                contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                break;
        }
        String selection = "_id=?";
        String[] selectionArgs = new String[]{split[1]};
        fullPath = getDataColumn(mContext, contentUri, selection, selectionArgs);
        Log.e(TAG, "MediaProvider: "+ type + ": " + fullPath);
        return fullPath;
    }

    private String getPathFromGooglePhotos(Uri uri) {
        String fullPath = null;
        fullPath = uri.getLastPathSegment();
        Log.e(TAG, "GooglePhotos: " + fullPath);
        return fullPath;
    }

    private String getPathFromDrive(Uri uri) {
        String fullPath = null;
        Cursor returnCursor = mContext.getContentResolver()
                .query(uri, null, null, null, null);
        /*
         * Get the column indexes of the data in the Cursor,
         *     * move to the first row in the Cursor, get the data,
         *     * and display it.
         * */
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        returnCursor.moveToFirst();
        String name = (returnCursor.getString(nameIndex));
        File file = new File(mContext.getCacheDir(), name);
        returnCursor.close();

        try {
            InputStream inputStream = mContext.getContentResolver().openInputStream(uri);
            FileOutputStream outputStream = new FileOutputStream(file);
            int read = 0;
            int maxBufferSize = 1024 * 1024;
            int bytesAvailable = inputStream.available();

            //int bufferSize = 1024;
            int bufferSize = Math.min(bytesAvailable, maxBufferSize);

            final byte[] buffers = new byte[bufferSize];
            while ((read = inputStream.read(buffers)) != -1) {
                outputStream.write(buffers, 0, read);
            }
            inputStream.close();
            outputStream.close();
            fullPath = file.getPath();
        } catch (Exception e) {
            Log.e(TAG, "getPathFromDrive: exception: " + e.getMessage());
        }

        Log.e(TAG, "GoogleDrive: " + fullPath);
        return fullPath;
    }

    /***
     * Used for Android Q+
     * @param uri current uri of file
     * @param newDirName if you want to create a directory, you can set this variable
     * @return new path of file
     */
    private String copyFileToInternalStorage(Uri uri,String newDirName) {
        String fullPath = null;
        Cursor returnCursor = mContext.getContentResolver().query(uri, new String[]{
                OpenableColumns.DISPLAY_NAME,OpenableColumns.SIZE
        }, null, null, null);


        /*
         * Get the column indexes of the data in the Cursor,
         *     * move to the first row in the Cursor, get the data,
         *     * and display it.
         * */
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
        returnCursor.moveToFirst();
        String name = (returnCursor.getString(nameIndex));
        String size = (Long.toString(returnCursor.getLong(sizeIndex)));
        returnCursor.close();

        File output;
        if(!newDirName.equals("")) {
            File dir = new File(mContext.getFilesDir() + "/" + newDirName);
            if (!dir.exists()) {
                dir.mkdir();
            }
            output = new File(mContext.getFilesDir() + "/" + newDirName + "/" + name);
        }
        else{
            output = new File(mContext.getFilesDir() + "/" + name);
        }
        try {
            InputStream inputStream = mContext.getContentResolver().openInputStream(uri);
            FileOutputStream outputStream = new FileOutputStream(output);
            int read = 0;
            int bufferSize = 1024;
            final byte[] buffers = new byte[bufferSize];
            while ((read = inputStream.read(buffers)) != -1) {
                outputStream.write(buffers, 0, read);
            }

            inputStream.close();
            outputStream.close();
            fullPath = output.getPath();
        } catch (Exception e) {
            Log.e(TAG, "copyFileToInternalStorage: exception: " + e.getMessage());;
        }

        Log.e(TAG, "copyFileToInternalStorage: " + fullPath);
        return fullPath;
    }

    private String getPathFromFileScheme(Uri uri){
        String fullPath = null;
        fullPath = uri.getPath();
        Log.e(TAG, "getPathFromFileScheme: " + fullPath);
        return fullPath;
    }



    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        String path = null;
        final String column = "_data";
        final String[] projection = {column};
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                path = cursor.getString(index);
            }
        } catch (Exception e){
            Log.e(TAG, "getDataColumn: " + e.getMessage());
        } finally {
            if (cursor != null)
                cursor.close();
        }

        Log.e(TAG, "content: " + path);
        return path;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    private static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is External Storage.
     */
    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * Get external sd card path using reflection
     * @param mContext
     * @param is_removable is external storage removable
     * @return
     */
    private static String getExternalStoragePath(Context mContext, boolean is_removable) {

        StorageManager mStorageManager = (StorageManager) mContext.getSystemService(Context.STORAGE_SERVICE);
        Class<?> storageVolumeClazz = null;
        try {
            storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");
            Method getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");
            Method getPath = storageVolumeClazz.getMethod("getPath");
            Method isRemovable = storageVolumeClazz.getMethod("isRemovable");
            Object result = getVolumeList.invoke(mStorageManager);
            final int length = Array.getLength(result);
            for (int i = 0; i < length; i++) {
                Object storageVolumeElement = Array.get(result, i);
                String path = (String) getPath.invoke(storageVolumeElement);
                boolean removable = (Boolean) isRemovable.invoke(storageVolumeElement);
                if (is_removable == removable) {
                    return path;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "getExternalStoragePath: " + e.getMessage());
        }
        return null;
    }



    private  boolean fileExists(String filePath) {
        File file = new File(filePath);

        return file.exists();
    }
}
