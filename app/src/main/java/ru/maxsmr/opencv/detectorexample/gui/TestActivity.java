package ru.maxsmr.opencv.detectorexample.gui;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.ngweb.android.api.filedialog.FileDialog;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ru.maxsmr.android.recyclerview.adapters.BaseRecyclerViewAdapter;
import ru.maxsmr.commonutils.data.CompareUtils;
import ru.maxsmr.commonutils.data.FileHelper;
import ru.maxsmr.commonutils.graphic.GraphicUtils;
import ru.maxsmr.opencv.androiddetector.OpenCvInit;
import ru.maxsmr.opencv.androiddetector.motion.AbstractMotionDetector;
import ru.maxsmr.opencv.androiddetector.motion.BsMotionDetector;
import ru.maxsmr.opencv.androiddetector.object.AbstractObjectDetector;
import ru.maxsmr.opencv.androiddetector.object.cascade.ClassifierDetector;
import ru.maxsmr.opencv.commondetector.model.DETECTOR_SENSITIVITY;
import ru.maxsmr.opencv.commondetector.model.motion.info.MotionDetectVideoInfo;
import ru.maxsmr.opencv.commondetector.model.motion.settings.MotionDetectorSettings;
import ru.maxsmr.opencv.commondetector.model.object.info.ObjectDetectFrameInfo;
import ru.maxsmr.opencv.commondetector.model.object.info.ObjectDetectVideoInfo;
import ru.maxsmr.opencv.commondetector.model.object.settings.OBJECT_TYPE;
import ru.maxsmr.opencv.commondetector.model.object.settings.ObjectDetectorSettings;
import ru.maxsmr.opencv.commondetector.utils.OpenCvUtils;
import ru.maxsmr.opencv.detectorexample.R;
import ru.maxsmr.opencv.detectorexample.app.DefaultSettings;
import ru.maxsmr.opencv.detectorexample.app.Paths;
import ru.maxsmr.opencv.detectorexample.gui.adapters.FileAdapter;
import ru.maxsmr.opencv.detectorexample.gui.progressable.DialogProgressable;

public class TestActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener, BaseRecyclerViewAdapter.OnItemClickListener<File>, BaseRecyclerViewAdapter.OnItemRemovedListener<File>, DialogProgressable.OnBackPressedListener, DialogInterface.OnDismissListener {

    private static final Logger logger = LoggerFactory.getLogger(TestActivity.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        setStatusBarColor(ContextCompat.getColor(this, R.color.statusBarColor));
        init();
        invalidateStateByCurrentSpinnerPosition();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_test, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean handled = false;
        switch (item.getItemId()) {
            case R.id.action_start:
                if (canDetectionStart()) {
                    startDetectorTask();
                } else {
                    Toast.makeText(this, R.string.toast_cant_start, Toast.LENGTH_SHORT).show();
                }
                handled = true;
                break;
        }
        return handled || super.onOptionsItemSelected(item);
    }

    public void setStatusBarColor(@ColorInt int colorId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(colorId);
        }
    }

    private DetectorTaskRunnable detectorTaskRunnable;
    private ExecutorService detectorTask;

    private boolean isDetectorTaskRunning() {
        return detectorTask != null && detectorTaskRunnable != null && detectorTaskRunnable.isRunning;
    }

    private void restartDetectorTask() {
        (detectorTask = Executors.newSingleThreadExecutor()).submit(detectorTaskRunnable = new DetectorTaskRunnable(getCurrentSpinnerCategory()));
    }

    private void startDetectorTask() {
        if (!isDetectorTaskRunning()) {
            restartDetectorTask();
        }
    }

    private void stopDetectorTask() {
        if (isDetectorTaskRunning()) {
            detectorTask.shutdown();
            detectorTask = null;
            detectorTaskRunnable.isCancelled = true;
            detectorTaskRunnable = null;
        }
    }

    private boolean canDetectionStart() {
        boolean can = false;
        switch (getCurrentSpinnerCategory()) {
            case MOTION:
            case OBJECT:
                validateAdapterVideos();
                can = !fileAdapter.isEmpty();
                break;
            case FACE:
            case EDGE:
                can = GraphicUtils.canDecodeImage(lastPictureFile);
                break;
        }
        return can;
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkOpenCvInit();
        restoreAdapterVideosFromWorkingDir();
    }

    private Spinner navigationSpinner;

    private ImageView pictureView;
    private RecyclerView filesRecycler;
    private FileAdapter fileAdapter;
    private TextView placeholder;

    private File lastPictureFile;

    FloatingActionButton addFileButton;

    private void init() {
        initToolbar();
        initSpinner();
        initRecycler();
        placeholder = (TextView) findViewById(R.id.tvPlaceholder);
        pictureView = (ImageView) findViewById(R.id.ivPicture);
        pictureView.setOnClickListener(this);
        addFileButton = (FloatingActionButton) findViewById(R.id.fabAddFile);
        addFileButton.setOnClickListener(this);
    }

    private void initToolbar() {
        String title = getTitle().toString();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setNavigationIcon(null);
            toolbar.setTitle(title);
            setSupportActionBar(toolbar);
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setTitle(title);
                actionBar.setLogo(R.drawable.logo_opencv);
                actionBar.setDisplayUseLogoEnabled(false);
                actionBar.setDisplayShowTitleEnabled(true);
                actionBar.setDisplayHomeAsUpEnabled(false);
                actionBar.show();
            }
        }
    }

    private int getCurrentSpinnerPosition() {
        return navigationSpinner.getSelectedItemPosition();
    }

    private DetectorCategory getCurrentSpinnerCategory() {
        return DetectorCategory.fromPosition(navigationSpinner.getSelectedItemPosition());
    }

    private void initSpinner() {
        SpinnerAdapter spinnerAdapter = ArrayAdapter.createFromResource(getApplicationContext(), R.array.category, R.layout.spinner_dropdown_item);
        navigationSpinner = (Spinner) findViewById(R.id.spinnerCategory);
        navigationSpinner.setAdapter(spinnerAdapter);
        navigationSpinner.setOnItemSelectedListener(this);
    }

    private void initRecycler() {
        filesRecycler = (RecyclerView) findViewById(R.id.rvVideoFiles);
        fileAdapter = new FileAdapter(this, null);
        fileAdapter.setOnItemClickListener(this);
        fileAdapter.setOnItemRemovedListener(this);
        filesRecycler.setLayoutManager(new LinearLayoutManager(this));
        filesRecycler.setAdapter(fileAdapter);
    }

    private File lastSelectedDirectory;
    private Dialog fileDialog;

    private boolean isFileDialogShowing() {
        return fileDialog != null && fileDialog.isShowing();
    }

    private void showFolderSelectDialog() {
        if (!isFileDialogShowing()) {
            FileDialog fd = new FileDialog(this);
            fd.setShowDirectoryOnly(false);
            fd.setFileSortedBy(FileDialog.SORTED_BY_NAME);
            fd.initDirectory(lastSelectedDirectory != null && FileHelper.isDirExists(lastSelectedDirectory.getAbsolutePath()) ? lastSelectedDirectory.getAbsolutePath() : Environment.getExternalStorageDirectory().toString());
            fd.addDirectoryListener(new FileDialog.DirSelectedListener() {
                @Override
                public void directorySelected(File directory, String[] dirs, String[] files) {
                    if (directory != null && FileHelper.isDirExists(directory.getAbsolutePath())) {
                        lastSelectedDirectory = directory;
                    }
                }
            });
            fd.addFileListener(new FileDialog.FileSelectedListener() {
                @Override
                public void fileSelected(File file, String[] strings, String[] strings1) {
                    switch (getCurrentSpinnerCategory()) {
                        case MOTION:
                        case OBJECT:
                            if (GraphicUtils.canDecodeVideo(file)) {
                                File newFile = new File(Paths.getDefaultVideosDirPath().getAbsolutePath(), file.getName());
                                if (!FileHelper.isFileExists(newFile.getAbsolutePath())) {
                                    newFile = FileHelper.copyFile(file, Paths.getDefaultVideosDirPath().getAbsolutePath());
                                }
                                fileAdapter.addItem(newFile);
                                displayFilesList();
                            } else {
                                Toast.makeText(TestActivity.this, getString(R.string.toast_incorrect_video_file) + ": " + (file != null ? file.getAbsolutePath() : null), Toast.LENGTH_SHORT).show();
                            }
                            break;
                        case FACE:
                        case EDGE:
                            if (GraphicUtils.canDecodeImage(file)) {
                                displayPicture(lastPictureFile = file);
                            } else {
                                Toast.makeText(TestActivity.this, getString(R.string.toast_incorrect_picture_file) + ": " + (file != null ? file.getAbsolutePath() : null), Toast.LENGTH_SHORT).show();
                            }
                            break;

                    }
                }
            });

            fileDialog = fd.createFileDialog();
            fileDialog.show();
        }
    }

    private void hideFolderSelectDialog() {
        if (isFileDialogShowing()) {
            fileDialog.hide();
            fileDialog = null;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.fabAddFile:
                showFolderSelectDialog();
                break;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        invalidateStateBySpinnerPosition(position);
    }

    private void displayFilesList() {
        if (!fileAdapter.isEmpty()) {
            filesRecycler.setVisibility(View.VISIBLE);
            placeholder.setVisibility(View.GONE);
        } else {
            filesRecycler.setVisibility(View.GONE);
            placeholder.setVisibility(View.VISIBLE);
            placeholder.setText(R.string.placeholder_add_video_files);
        }
        pictureView.setVisibility(View.GONE);
    }

    private void displayPicture(File pictureFile) {
        displayPicture(GraphicUtils.createBitmapFromFile(pictureFile, 1));
    }

    private void displayPicture(Bitmap pictureBitmap) {
        if (GraphicUtils.isBitmapCorrect(pictureBitmap)) {
            pictureView.setImageBitmap(pictureBitmap);
            pictureView.setVisibility(View.VISIBLE);
            placeholder.setVisibility(View.GONE);
        } else {
            pictureView.setVisibility(View.GONE);
            placeholder.setVisibility(View.VISIBLE);
            placeholder.setText(R.string.placeholder_add_picture);
        }
        filesRecycler.setVisibility(View.GONE);
    }

    private void invalidateStateByCurrentSpinnerPosition() {
        invalidateStateBySpinnerPosition(getCurrentSpinnerPosition());
    }

    private void invalidateStateBySpinnerPosition(int position) {
        switch (DetectorCategory.fromPosition(position)) {
            case MOTION:
            case OBJECT:
                displayFilesList();
                break;
            case FACE:
            case EDGE:
                displayPicture(lastPictureFile);
                break;
        }
        addFileButton.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fab_show));
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onItemClick(File item) {

    }

    @Override
    public void onItemRemoved(int from, File item) {
        if (item != null) {
            if (item.getParentFile().equals(Paths.getDefaultVideosDirPath())) {
                FileHelper.deleteFile(item);
            }
        }
        invalidateStateByCurrentSpinnerPosition();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        hideFolderSelectDialog();
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    private void checkOpenCvInit() {

        final OpenCvInit openCvInit = OpenCvInit.getInstance();

        if (!openCvInit.isOpenCvManagerInitComplete()) {

            openCvInit.setOpenCvInitListener(new OpenCvInit.OpenCvInitListener() {

                @Override
                public void onOpenCvInitSuccess() {
                    logger.info("onOpenCvInitSuccess()");
                }

                @Override
                public void onOpenCvInitFailure() {
                    logger.error("onOpenCvInitFailure()");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showOkAlertDialog(getString(R.string.dialog_opencv_init_failure_title), String.format(getString(R.string.dialog_opencv_init_failure_message), OpenCVLoader.OPENCV_VERSION_2_4_9),
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.cancel();
                                            browseWebPageAndFinish(OpenCvInit.OPENCV_APP_URL);
                                        }
                                    });
                        }
                    });

                }
            });

        } else {

            if (!openCvInit.isOpenCvManagerLoaded()) {
                logger.error("");
                showOkAlertDialog(getString(R.string.dialog_opencv_init_failure_title), String.format(getString(R.string.dialog_opencv_init_failure_message), OpenCVLoader.OPENCV_VERSION_2_4_9),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                browseWebPageAndFinish(OpenCvInit.OPENCV_APP_URL);
                            }
                        });

            }
        }
    }

    private void showOkAlertDialog(String title, String message, DialogInterface.OnClickListener clickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title).setMessage(message).setIcon(android.R.drawable.ic_dialog_alert).setPositiveButton(android.R.string.ok, clickListener)
                .setCancelable(false).show();
    }

    private void browseWebPageAndFinish(String url) {
        Intent viewIntent = new Intent(Intent.ACTION_VIEW);
        viewIntent.setData(Uri.parse(url));
        viewIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(viewIntent);
        finish();
    }

    private List<MotionDetectVideoInfo> testMotionDetector(MotionDetectorSettings s) {
        try {
            List<MotionDetectVideoInfo> infos = AbstractMotionDetector.testMotionDetector(new BsMotionDetector(s.grayscale(), s.getMorphKernelSize(), s.getMixtures(), s.getHistory(), s.getBackgroundRatio(), s.getNoiseSigma(), s.getLearningRate(), s.getMinContourAreaRatio(), Paths.getDefaultSavedMotionFramesDirPath()),
                    Paths.getDefaultVideosDirPath().getAbsolutePath(),
                    Paths.INFO_MOTION_FILE_NAME,
                    Paths.getDefaultSavedMotionFramesDirPath().getAbsolutePath(), s);
            logger.debug("infos=" + infos);
            return infos;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("an Exception occurred", e);
            return null;
        }
    }

    private List<ObjectDetectVideoInfo> testObjectDetector(ObjectDetectorSettings s, File classifierFile) {
        try {
            List<ObjectDetectVideoInfo> infos = AbstractObjectDetector.testObjectDetector(new ClassifierDetector(this, s.getType(), classifierFile, s.grayscale(), Paths.getDefaultSavedObjectFramesDirPath()), Paths.getDefaultVideosDirPath().getAbsolutePath(), "info_object.txt", Paths.getDefaultSavedObjectFramesDirPath()
                    .getAbsolutePath(), s);
            logger.debug("infos=" + infos);
            return infos;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("an Exception occurred", e);
            return null;
        }
    }

    private void restoreAdapterVideosFromWorkingDir() {
        List<File> videos = FileHelper.getFiles(Paths.getDefaultVideosDirPath(), false, new Comparator<File>() {
            @Override
            public int compare(File lhs, File rhs) {
                return CompareUtils.compareStrings(lhs.getName(), rhs.getName(), true);
            }
        });
        if (!fileAdapter.isEmpty()) {
            fileAdapter.clearItems();
        }
        for (File video : videos) {
            if (GraphicUtils.canDecodeVideo(video)) {
                fileAdapter.addItem(video);
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void copyAdapterVideosToWorkingDir() {
        if (!fileAdapter.isEmpty()) {
            for (File videoFile : fileAdapter.getItems()) {
                if (!FileHelper.isFileExists(new File(Paths.getDefaultVideosDirPath().getAbsolutePath(), videoFile.getName()).getAbsolutePath())) {
                    FileHelper.copyFile(videoFile, Paths.getDefaultVideosDirPath().getAbsolutePath());
                }
            }
        }
    }

    private void validateAdapterVideos() {
        if (!fileAdapter.isEmpty()) {
            for (File file : fileAdapter.getItems()) {
                if (!GraphicUtils.canDecodeVideo(file)) {
                    fileAdapter.removeItem(file);
                }
            }
        }
    }

    private DialogProgressable dialogProgressable;

    private boolean isDialogProgressableShowing() {
        return dialogProgressable != null && dialogProgressable.isStarted();
    }

    private void onStartDetecting() {
        this.dialogProgressable = new DialogProgressable(TestActivity.this);
        this.dialogProgressable.setOnBackPressedListener(this);
        this.dialogProgressable.setOnDismissListener(this);
        this.dialogProgressable.setMessage(getString(R.string.processing));
        this.dialogProgressable.setIndeterminate(true);
        this.dialogProgressable.setCancelable(false);
        this.dialogProgressable.onStart();
    }

    private void onFinishDetecting() {
        if (isDialogProgressableShowing()) {
            this.dialogProgressable.onStop();
            this.dialogProgressable = null;
        }
    }

    private AlertDialog cancelConfirmDialog;

    private boolean isCancelConfirmDialogShowing() {
        return cancelConfirmDialog != null && cancelConfirmDialog.isShowing();
    }

    private void hideCancelConfirmDialog() {
        if (isCancelConfirmDialogShowing()) {
            cancelConfirmDialog.cancel();
            cancelConfirmDialog = null;
        }
    }

    private void showCancelConfirmDialog() {
        if (!isCancelConfirmDialogShowing()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setIcon(android.R.drawable.ic_dialog_alert);
            builder.setTitle(R.string.dialog_cancel_detection_title);
            builder.setMessage(R.string.dialog_cancel_detection_message);
            builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    stopDetectorTask();
                    onFinishDetecting();
                }
            });
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    cancelConfirmDialog = null;
                }
            });
            builder.setCancelable(true);
            cancelConfirmDialog = builder.show();
        }
    }


    @Override
    public void onBackPressed(DialogInterface dialog) {
        showCancelConfirmDialog();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        dialogProgressable = null;
    }


    private class DetectorTaskRunnable implements Runnable {

        long startTime = 0;
        boolean isRunning = false;

        boolean isCancelled = false;

        @NonNull
        private final DetectorCategory category;

        public DetectorTaskRunnable(@NonNull DetectorCategory category) {
            this.category = category;
        }

        void onStart() {
            if (!isCancelled) {
                startTime = System.currentTimeMillis();
                isRunning = true;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        onStartDetecting();
                    }
                });
            }
        }

        void onStop() {
            if (!isCancelled) {
                final float execTime = (float) (System.currentTimeMillis() - startTime) / 1000f;
                logger.debug("execTime=" + execTime);
                isRunning = false;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        onFinishDetecting();
                        Toast.makeText(TestActivity.this, String.format(getString(R.string.toast_execution_time), execTime + " (s)"), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }

        @Override
        public void run() {
            onStart();
            doJob();
            onStop();
        }

        private void doJob() {
            if (!isCancelled) {
                switch (category) {

                    case MOTION:
                        List<MotionDetectVideoInfo> motionDetectVideoInfos = testMotionDetector(DefaultSettings.generateDefaultMotionDetectorSettings());
                        displayResultMotion(motionDetectVideoInfos);
                        break;

                    case OBJECT:
                        File humanClassifierFile = FileHelper.createNewFile("hogcascade_pedestrians.xml", Paths.getDefaultWorkingDir().getAbsolutePath());
                        try {
                            FileHelper.copyRawFile(TestActivity.this, R.raw.hogcascade_pedestrians, humanClassifierFile, FileHelper.FILE_PERMISSIONS_ALL);
                        } catch (Exception e) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(TestActivity.this, R.string.toast_file_read_unsuccessful, Toast.LENGTH_SHORT).show();
                                }
                            });
                            return;
                        }
                        List<ObjectDetectVideoInfo> objectDetectVideoInfos = testObjectDetector(DefaultSettings.generateDefaultObjectDetectorSettings(), humanClassifierFile);
                        displayResultObject(objectDetectVideoInfos);
                        break;

                    case FACE:
                        if (GraphicUtils.canDecodeImage(lastPictureFile)) {

                            ClassifierDetector detector;
                            try {
                                File faceClassifierFile = FileHelper.createNewFile("haarcascade_frontalface_alt.xml", Paths.getDefaultWorkingDir().getAbsolutePath());
                                FileHelper.copyRawFile(TestActivity.this, R.raw.haarcascade_frontalface_alt, faceClassifierFile, FileHelper.FILE_PERMISSIONS_ALL);
                                detector = new ClassifierDetector(TestActivity.this, OBJECT_TYPE.FACE, faceClassifierFile, false, null);
                            } catch (Exception e) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(TestActivity.this, R.string.toast_file_read_unsuccessful, Toast.LENGTH_SHORT).show();
                                    }
                                });
                                return;
                            }

                            Bitmap sceneImageBitmap = GraphicUtils.createBitmapFromFile(lastPictureFile, 1);
                            if (sceneImageBitmap != null) {
                                byte[] sceneImage = GraphicUtils.getBitmapData(sceneImageBitmap);
                                ObjectDetectFrameInfo info = detector.detectObjectByByteArray(sceneImage, true, 0, sceneImageBitmap.getWidth(), sceneImageBitmap.getHeight(), DETECTOR_SENSITIVITY.HIGH, null);
                                logger.debug("info=" + info);
                                Mat resultMat = OpenCvUtils.convertByteArrayToMat(info.getSceneImage(), info.getWidth(), info.getHeight(), info.getType());
                                Bitmap resultBitmap = OpenCvUtils.convertMatToBitmap(resultMat, true);
                                displayResultBitmap(resultBitmap, info.detected());
                            }

                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(TestActivity.this, R.string.toast_file_read_unsuccessful, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        break;

                    case EDGE:
                        if (GraphicUtils.canDecodeImage(lastPictureFile)) {
                            Bitmap resultBitmap = OpenCvUtils.detectEdges(lastPictureFile, OpenCvUtils.CANNY_THRESHOLD_DEFAULT, OpenCvUtils.CANNY_RATIO_DEFAULT);
                            displayResultBitmap(resultBitmap, true);
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(TestActivity.this, R.string.toast_file_read_unsuccessful, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        break;
                }
            }
        }

        private void displayResultMotion(final List<MotionDetectVideoInfo> infos) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (infos != null && !infos.isEmpty()) {
                        Toast.makeText(TestActivity.this, R.string.toast_detection_successful, Toast.LENGTH_SHORT).show();
                        Toast.makeText(TestActivity.this, String.format(getString(R.string.toast_detection_results), Paths.getDefaultWorkingDir().getAbsolutePath()), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(TestActivity.this, R.string.toast_detection_unsuccessful, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        private void displayResultObject(final List<ObjectDetectVideoInfo> infos) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (infos != null && !infos.isEmpty()) {
                        Toast.makeText(TestActivity.this, R.string.toast_detection_successful, Toast.LENGTH_SHORT).show();
                        Toast.makeText(TestActivity.this, String.format(getString(R.string.toast_detection_results), Paths.getDefaultWorkingDir().getAbsolutePath()), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(TestActivity.this, R.string.toast_detection_unsuccessful, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        private void displayResultBitmap(final Bitmap bm, final boolean detected) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (detected && GraphicUtils.isBitmapCorrect(bm)) {
                        displayPicture(bm);
                    } else {
                        Toast.makeText(TestActivity.this, R.string.toast_detection_unsuccessful, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    protected enum DetectorCategory {
        MOTION, OBJECT, FACE, EDGE;

        public static DetectorCategory fromPosition(int position) {
            for (DetectorCategory category : DetectorCategory.values()) {
                if (category.ordinal() == position) {
                    return category;
                }
            }
            throw new IllegalArgumentException("incorrect position: " + position);
        }
    }

}
