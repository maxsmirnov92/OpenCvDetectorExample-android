package net.maxsmr.opencv.androiddetector;


import android.content.Context;
import android.support.annotation.NonNull;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public final class OpenCvInit {

    private static final Logger logger = LoggerFactory.getLogger(OpenCvInit.class);

    public static final String OPENCV_APP_URL = "https://play.google.com/store/apps/details?id=org.opencv.engine";

    private static OpenCvInit mInstance;

    @NonNull
    private final Context mContext;


    private OpenCvInitListener initListener;

    private boolean isOpenCvManagerLoaded = false;

    private boolean isOpenCvManagerInitComplete = false;

    private OpenCvInit(@NonNull Context ctx, boolean allowInstall) {

        mContext = ctx;

        try {

            initOpenCvManager(allowInstall);

        } catch (RuntimeException e) {
            logger.error("a RuntimeException occured during initOpenCvManager(): {}", e.getMessage());

            isOpenCvManagerInitComplete = true;
            isOpenCvManagerLoaded = false;

            if (initListener != null) {
                initListener.onOpenCvInitFailure();
            }
        }

    }

    public static void initInstance(@NonNull Context context, boolean allowInstall) {
        if (mInstance == null) {
            logger.debug("initInstance()");
            synchronized (OpenCvInit.class) {
                mInstance = new OpenCvInit(context, allowInstall);
            }
        }
    }

    @NonNull
    public static OpenCvInit getInstance() {
        if (mInstance == null) {
            throw new IllegalStateException("initInstance() was not called");
        }
        return mInstance;
    }


    public void setOpenCvInitListener(OpenCvInitListener listener) {
        initListener = listener;
    }

    public boolean isOpenCvManagerLoaded() {
        return isOpenCvManagerLoaded;
    }

    public boolean isOpenCvManagerInitComplete() {
        return isOpenCvManagerInitComplete;
    }

    private boolean initOpenCvManager(boolean allowInstall) {
        logger.debug("initOpenCvManager()");

        if (!OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_9, mContext, new BaseLoaderCallback(mContext) {
            @Override
            protected boolean allowShowDialogs() {
                return true;
            }

            @Override
            public void onManagerConnected(int status) {
                super.onManagerConnected(status);

                switch (status) {
                    case LoaderCallbackInterface.SUCCESS: {
                        logger.info("OpenCV loaded successfully");
                        isOpenCvManagerInitComplete = true;
                        isOpenCvManagerLoaded = true;

                        if (initListener != null) {
                            initListener.onOpenCvInitSuccess();
                        }
                    }
                    break;

                    default: {
                        logger.error("OpenCV load failure");
                        isOpenCvManagerInitComplete = true;
                        isOpenCvManagerLoaded = false;

                        if (initListener != null) {
                            initListener.onOpenCvInitFailure();
                        }
                    }
                    break;
                }
            }
        })) {
            logger.error("Cannot connect to OpenCV Manager");
            isOpenCvManagerInitComplete = true;
            isOpenCvManagerLoaded = false;

            if (initListener != null) {
                initListener.onOpenCvInitFailure();
            }
            return false;
        }
        return true;
    }

    public interface OpenCvInitListener {

        void onOpenCvInitSuccess();

        void onOpenCvInitFailure();
    }

}
