package net.maxsmr.opencv.androiddetector.object.cascade.classifiers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Context;

import net.maxsmr.commonutils.data.FileHelper;
import net.maxsmr.opencv.androiddetector.R;

public class ClassifierResInit {

    private static final Logger logger = LoggerFactory.getLogger(ClassifierResInit.class);

    public List<File> getCarMainClassifiers() {
        List<File> mainClassifiers = new ArrayList<File>();
        mainClassifiers.add(CarClassifier.CAR1.getDestFile(mContext));
        mainClassifiers.add(CarClassifier.CAR2.getDestFile(mContext));
        mainClassifiers.add(CarClassifier.CAR3.getDestFile(mContext));
        mainClassifiers.add(CarClassifier.CAR4.getDestFile(mContext));
        return mainClassifiers;
    }

    public File getCarCheckClassifier() {
        return CarClassifier.CAR_CHECK.getDestFile(mContext);
    }

    private static void copyCarClassifiers(Context context) throws IOException, InterruptedException {
        logger.debug("copyCarClassifiers()");

        FileHelper.copyRawFile(context, R.raw.lbpcascade_car_check, CarClassifier.CAR_CHECK.getDestFile(context),
                FileHelper.FILE_PERMISSIONS_ALL);
        FileHelper.copyRawFile(context, R.raw.lbpcascade_car_1, CarClassifier.CAR1.getDestFile(context),
                FileHelper.FILE_PERMISSIONS_ALL);
        FileHelper.copyRawFile(context, R.raw.lbpcascade_car_2, CarClassifier.CAR2.getDestFile(context),
                FileHelper.FILE_PERMISSIONS_ALL);
        FileHelper.copyRawFile(context, R.raw.lbpcascade_car_3, CarClassifier.CAR3.getDestFile(context),
                FileHelper.FILE_PERMISSIONS_ALL);
        FileHelper.copyRawFile(context, R.raw.lbpcascade_car_4, CarClassifier.CAR4.getDestFile(context),
                FileHelper.FILE_PERMISSIONS_ALL);
    }

    private ClassifierResInit(Context context) {
        mContext = context;
        try {
            copyCarClassifiers(mContext);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("an Exception occurred during copyRawFile()", e);
        }
    }

    private static ClassifierResInit mInstance;
    private final Context mContext;

    public static void initInstance(Context context) {
        if (mInstance == null) {
            logger.debug("initInstance()");
            synchronized (ClassifierResInit.class) {
                mInstance = new ClassifierResInit(context);
            }
        }
    }

    public static ClassifierResInit getInstance(Context ctx) {
        initInstance(ctx);
        return mInstance;
    }

}
