package com.schoolmanager.view;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.downloader.Error;
import com.downloader.OnCancelListener;
import com.downloader.OnDownloadListener;
import com.downloader.OnProgressListener;
import com.downloader.OnStartOrResumeListener;
import com.downloader.PRDownloader;
import com.downloader.Progress;
import com.google.gson.Gson;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.schoolmanager.R;
import com.schoolmanager.databinding.ContentProgressDownloadBinding;

import java.io.File;
import java.net.URLConnection;
import java.util.List;

public class MessageFileDownloadProgressbar extends FrameLayout {

    private String fileName;
    private String fileType;
    private String file_url;
    private Activity activity;
    private int downLoadingId;
    private String key = "";
    private String keyChar = "";
    ContentProgressDownloadBinding binding;
    private OnDownloadCompleteListener listener;


    public MessageFileDownloadProgressbar(@NonNull Context context) {
        super(context);
        initialize();
    }

    public MessageFileDownloadProgressbar(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public MessageFileDownloadProgressbar(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public MessageFileDownloadProgressbar(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initialize();
    }

    private void initialize() {
        LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutInflater.inflate(R.layout.content_progress_download, null);
        binding = ContentProgressDownloadBinding.inflate(layoutInflater, this, true);
    }

    public void setUp() {
        //String directory = JsSdcardUtils.CreateFolder(JsSdcardUtils.PUTILS_IMAGE_IMAGES);
        //File file = new File(directory, fileName);


        File filedir = new File(Environment.getExternalStorageDirectory(), "ParentalControll");
        if (!filedir.exists()) {
            filedir.mkdirs();
        }

        File fileDecrypted = new File(filedir, fileName);


        if ((fileDecrypted.exists() || fileType.equals("document")) && fileDecrypted.exists()) {
            Log.e("FILENAME", fileDecrypted.getName());
            String mimeType = URLConnection.guessContentTypeFromName(fileDecrypted.getAbsolutePath());
            if (fileType.equals("video")) {

            } else {
                binding.imgRowMessageSelfVideoMessage.setImageResource(0);
            }

        } else {
            binding.imgRowMessageSelfVideoMessage.setImageResource(R.drawable.ic_download);
        }


        binding.imgRowMessageSelfVideoClose.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                PRDownloader.cancel(downLoadingId);
            }
        });
        binding.imgRowMessageSelfVideoMessage.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // String directory = JsSdcardUtils.CreateFolder(JsSdcardUtils.PUTILS_IMAGE_IMAGES);
//                File file = new File(directory, fileName);

                File filedir = new File(Environment.getExternalStorageDirectory(), "ParentalControll");
                if (!filedir.exists()) {
                    filedir.mkdirs();
                }

                File fileDecrypted = new File(filedir, fileName);

                if ((fileDecrypted.exists() || fileType.equals("document")) && fileDecrypted.exists()) {
                    String mimeType = URLConnection.guessContentTypeFromName(fileDecrypted.getAbsolutePath());
                    if (fileType.equals("video")) {

                    } else if (fileType.equals("image")) {

                    }
                } else {
                    Dexter.withActivity(activity).withPermissions(
                            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            .withListener(new MultiplePermissionsListener() {
                                @Override
                                public void onPermissionsChecked(MultiplePermissionsReport report) {
                                    if (report.areAllPermissionsGranted()) {
                                        String url = file_url;
                                        Log.e("URL", url);
                                        downLoadingId = PRDownloader.download(url, fileDecrypted.getParent(), fileDecrypted.getName())
                                                .build()
                                                .setOnStartOrResumeListener(new OnStartOrResumeListener() {
                                                    @Override
                                                    public void onStartOrResume() {
                                                        binding.cardRowMessageSelfVideoDownload.setVisibility(VISIBLE);
                                                        binding.imgRowMessageSelfVideoMessage.setVisibility(GONE);
                                                    }
                                                })
                                                .setOnCancelListener(new OnCancelListener() {
                                                    @Override
                                                    public void onCancel() {
                                                        binding.cardRowMessageSelfVideoDownload.setVisibility(GONE);
                                                        binding.imgRowMessageSelfVideoMessage.setImageResource(R.drawable.ic_download);
                                                        binding.imgRowMessageSelfVideoMessage.setVisibility(VISIBLE);

                                                    }
                                                })
                                                .setOnProgressListener(new OnProgressListener() {
                                                    @Override
                                                    public void onProgress(Progress progress) {
                                                        Log.e("Progress", new Gson().toJson(progress));
                                                        int mProgressPersentage = (int) ((progress.currentBytes * 100) / progress.totalBytes);
                                                        binding.pBarRowMessageSelfVideoDownload.setProgress(mProgressPersentage);
                                                    }
                                                })
                                                .start(new OnDownloadListener() {
                                                    @Override
                                                    public void onDownloadComplete() {
                                                        binding.cardRowMessageSelfVideoDownload.setVisibility(GONE);
                                                        String mimeType = URLConnection.guessContentTypeFromName(fileDecrypted.getAbsolutePath());
                                                        if (mimeType.contains("video")) {

                                                        } else {
                                                            binding.imgRowMessageSelfVideoMessage.setImageResource(0);
                                                        }
                                                        binding.imgRowMessageSelfVideoMessage.setVisibility(VISIBLE);

                                                        File mDecryptedFile = new File(filedir.getAbsolutePath(), fileName);
                                                        Log.e("DECRYPTEDFILEPATH", mDecryptedFile.getAbsolutePath());
                                                        if (listener != null) {
                                                            listener.onDosnloadComplete();
                                                        }

                                                    }

                                                    @Override
                                                    public void onError(Error error) {
                                                        binding.cardRowMessageSelfVideoDownload.setVisibility(GONE);
                                                        binding.imgRowMessageSelfVideoMessage.setImageResource(R.drawable.ic_download);
                                                        binding.imgRowMessageSelfVideoMessage.setVisibility(VISIBLE);
                                                    }
                                                });
                                    } else {
                                        if (report.isAnyPermissionPermanentlyDenied()) {
//                                            new JsCommonDialog(
//                                                    activity,
//                                                    R.string.permission,
//                                                    activity.getString(R.string.allow_permission_from_setting),
//                                                    R.string.ok,
//                                                    0,
//                                                    new JsCommonDialog.OnButtonClickListener() {
//                                                        @Override
//                                                        public void onPositiveButtonClick() {
//                                                            Intent intent = new Intent();
//                                                            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//                                                            Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
//                                                            intent.setData(uri);
//                                                            activity.startActivity(intent);
//                                                        }
//
//                                                        @Override
//                                                        public void onNegatievButtonClick() {
//
//                                                        }
//                                                    }
//                                            ).show();
                                        }
                                    }

                                }

                                @Override
                                public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                                    token.continuePermissionRequest();
                                }
                            }).check();

                }
            }
        });
    }

    public void performDownload() {
        binding.imgRowMessageSelfVideoMessage.performClick();
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setKeyChar(String keyChar) {
        this.keyChar = keyChar;
    }

    public ContentProgressDownloadBinding getBinding() {
        return binding;
    }

    public void setBinding(ContentProgressDownloadBinding binding) {
        this.binding = binding;
    }

    public interface OnDownloadCompleteListener {
        void onDosnloadComplete();
    }

    public void setListener(OnDownloadCompleteListener listener) {
        this.listener = listener;
    }

    public void setFile_url(String file_url) {
        this.file_url = file_url;
    }
}
