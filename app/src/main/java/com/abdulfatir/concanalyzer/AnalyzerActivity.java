package com.abdulfatir.concanalyzer;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.abdulfatir.concanalyzer.models.IdAbscissaMap;
import com.abdulfatir.concanalyzer.models.SampleModel;
import com.abdulfatir.concanalyzer.util.Consts;
import com.abdulfatir.concanalyzer.util.ImageUtils;

import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Performs analysis on the image to detect ROIs.
 */
public class AnalyzerActivity extends AppCompatActivity {

    public static final String IMAGE_KEY = "IMAGE";
    public static final String DEMO_MODE_KEY = "demoMode";
    /**
     * The number of blobs to detect.
     */
    public static final int NB_BLOBS = 7;
    private static final String KNOWN_SAMPLES_KEY = "KNOWN_SAMPLES";
    private static final String UNKNOWN_SAMPLES_KEY = "UNKNOWN_SAMPLES";
    private static final String QC_SAMPLES_KEY = "QC_SAMPLES";
    private static final String SLOPE_KEY = "SLOPE";
    private static final String INTERCEPT_KEY = "INTERCEPT";
    private static final String DECIMAL_NO_REGEX = "^\\d{1,5}(\\.|\\.\\d+)?$";
    private static final int PERMISSION_STORAGE = 2345;

    static {
        OpenCVLoader.initDebug();
    }

    private final int IMAGE_CODE = 7323;
    private final int CAMERA_CODE = 3243;
    /**
     * The Samples.
     */
    HashMap<Integer, SampleModel> samples;
    private ImageView imageView;
    private String pictureFilePath;
    private int choice;
    private SharedPreferences prefs;
    private Uri cameraFileUri;
    private boolean analyzedCorrectly = false;
    private Bitmap changedBitmap;
    private ProgressDialog dlg;
    private Rect[] rects;

    /**
     * Opens settings for this application for allowing user to give permissions.
     *
     * @param context the context
     */
    public static void startInstalledAppDetailsActivity(final Activity context) {
        if (context == null) {
            return;
        }
        final Intent i = new Intent();
        i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        i.addCategory(Intent.CATEGORY_DEFAULT);
        i.setData(Uri.parse("package:" + context.getPackageName()));
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        context.startActivity(i);
    }

    /**
     * Save the bitmap as a jpeg file.
     *
     * @param bmp the bmp
     * @return the file
     * @throws IOException the io exception
     */
    public static File saveBitmap(Bitmap bmp) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = Environment.getExternalStorageDirectory()
                + File.separator + "ConcAnalyzer";
        File dir = new File(path);
        if (!dir.exists())
            dir.mkdirs();
        path += (File.separator + Long.toString(System.currentTimeMillis() / 1000) + ".jpg");
        File f = new File(path);
        f.createNewFile();
        FileOutputStream fo = new FileOutputStream(f);
        fo.write(bytes.toByteArray());
        fo.close();
        return f;
    }

    @Override
    protected void onRestoreInstanceState(Bundle saved) {
        String filename = saved.getString(IMAGE_KEY);
        if (filename != null) {
            cameraFileUri = Uri.fromFile(new File(filename));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analyzer);
        if (savedInstanceState != null) {
            String filename = savedInstanceState.getString(IMAGE_KEY);
            if (filename != null) {
                cameraFileUri = Uri.fromFile(new File(filename));
            }
        }
        choice = getIntent().getIntExtra(Consts.CHOICE_KEY, -1);
        prefs = getSharedPreferences(Consts.PREFS_NAME, MODE_PRIVATE);
        imageView = (ImageView) findViewById(R.id.imageView);
        final Dialog sampleData = new Dialog(this);
        sampleData.setContentView(R.layout.dialog_sample_data);
        sampleData.setCancelable(true);
        final EditText intensityET = (EditText) sampleData.findViewById(R.id.editText);
        final Spinner sampleTypeSp = (Spinner) sampleData.findViewById(R.id.spinner);
        final EditText concET = (EditText) sampleData.findViewById(R.id.editText3);
        final EditText idET = (EditText) sampleData.findViewById(R.id.editText4);
        Button save = (Button) sampleData.findViewById(R.id.button3);
        sampleTypeSp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == 2)
                    concET.setVisibility(View.GONE);
                else
                    concET.setVisibility(View.VISIBLE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = sampleTypeSp.getSelectedItemPosition();
                int idx = Integer.parseInt(idET.getText().toString());
                String conc = concET.getText().toString();
                if (pos == 2) {
                    samples.get(idx).setDataPointType(SampleModel.DataPointType.UNKNOWN);
                    samples.get(idx).setUpdated(true);
                } else if (pos == 0) {
                    samples.get(idx).setDataPointType(SampleModel.DataPointType.KNOWN);
                    if (conc.matches(DECIMAL_NO_REGEX)) {
                        double concen = Double.parseDouble(conc);
                        samples.get(idx).setConcentration(concen);
                        samples.get(idx).setUpdated(true);
                    } else {
                        Toast.makeText(AnalyzerActivity.this, "Enter valid concentration value.", Toast.LENGTH_SHORT).show();
                    }
                } else if (pos == 1) {
                    samples.get(idx).setDataPointType(SampleModel.DataPointType.QUALITY_CONTROL);
                    if (conc.matches(DECIMAL_NO_REGEX)) {
                        double concen = Double.parseDouble(conc);
                        samples.get(idx).setConcentration(concen);
                        samples.get(idx).setUpdated(true);
                    } else {
                        Toast.makeText(AnalyzerActivity.this, "Enter valid concentration value.", Toast.LENGTH_SHORT).show();
                    }
                }
                if (samples.get(idx).isUpdated())
                    updateBitmap();
                sampleData.dismiss();
            }
        });
        imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                if (action == MotionEvent.ACTION_UP) {
                    float ivX = event.getX();
                    float ivY = event.getY();
                    int[] imC = getProjection(ivX, ivY);
                    if (imC != null && analyzedCorrectly) {
                        for (int idx = 0; idx < rects.length; idx++) {
                            if (rects[idx].contains(new Point(imC[0], imC[1]))) {
                                SampleModel s = samples.get(idx);
                                intensityET.setText(String.format(Locale.getDefault(), "%.2f", s.getIntensity()));
                                concET.setText(String.format(Locale.getDefault(), "%.2f", s.getConcentration()));
                                if (s.getConcentration() == 0)
                                    concET.setText("");
                                idET.setText(String.format(Locale.getDefault(), "%d", idx));
                                int pos = 0;
                                switch (s.getDataPointType()) {
                                    case KNOWN:
                                        pos = 0;
                                        break;
                                    case QUALITY_CONTROL:
                                        pos = 1;
                                        break;
                                    case UNKNOWN:
                                        pos = 2;
                                }
                                sampleTypeSp.setSelection(pos);
                                sampleData.setTitle("Set Value");
                                sampleData.show();
                            }
                        }
                    }
                }
                return true;
            }
        });
        if (choice == Consts.CHOOSE_FROM_LIBRARY) {
            if (!hasPermission(Consts.STORAGE_PERMISSIONS[0])) {
                ActivityCompat.requestPermissions(this, Consts.STORAGE_PERMISSIONS, PERMISSION_STORAGE);
            } else {
                chooseFromLibrary();
            }
        } else if (choice == Consts.TAKE_PICTURE) {
            if (!hasPermission(Consts.STORAGE_PERMISSIONS[0])) {
                ActivityCompat.requestPermissions(this, Consts.STORAGE_PERMISSIONS, PERMISSION_STORAGE);
            } else {
                takePicture();
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle out) {
        if (cameraFileUri != null) {
            out.putString(IMAGE_KEY, cameraFileUri.getPath());
        }
        super.onSaveInstanceState(out);
    }

    private void chooseFromLibrary() {
        startActivityForResult(Intent.createChooser(new Intent(Intent.ACTION_GET_CONTENT)
                .setType("image/*"), "Choose an image"), IMAGE_CODE);
    }

    private void takePicture() {
        File mainDirectory = new File(Environment.getExternalStorageDirectory(), "ConcAnalyzer/Images");
        if (!mainDirectory.exists())
            mainDirectory.mkdirs();
        cameraFileUri = Uri.fromFile(new File(mainDirectory, "IMG_conc.jpg"));
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraFileUri);
        startActivityForResult(cameraIntent, CAMERA_CODE);
    }

    @Nullable
    private int[] getProjection(float x, float y) {
        if (x < 0 || y < 0 || x > imageView.getWidth() || y > imageView.getHeight()) {
            return null;
        } else {
            int projectedX = (int) ((double) x * ((double) changedBitmap.getWidth() / (double) imageView.getWidth()));
            int projectedY = (int) ((double) y * ((double) changedBitmap.getHeight() / (double) imageView.getHeight()));
            return new int[]{projectedX, projectedY};
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_CODE && resultCode == RESULT_OK && data != null) {
            analyzedCorrectly = false;
            String[] projection = {MediaStore.Images.Media.DATA, MediaStore.Images.Media.ORIENTATION};
            Uri selectedImage = data.getData();
            Cursor cursor = null;
            if (Build.VERSION.SDK_INT > 19) {
                String id = null;
                if (DocumentsContract.isDocumentUri(this, selectedImage)) {
                    String wholeID = DocumentsContract.getDocumentId(selectedImage);
                    id = wholeID.split(":")[1];
                } else {
                    String regex = "\\/(\\d+)$";
                    Pattern r = Pattern.compile(regex);
                    Log.d(getClass().getName(), selectedImage.toString());
                    Matcher m = r.matcher(selectedImage.toString());
                    if (m.find())
                        id = m.group(1);
                }
                String sel = MediaStore.Images.Media._ID + "=?";
                if (id != null)
                    cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            projection, sel, new String[]{id}, null);
            } else {
                cursor = getContentResolver().query(selectedImage,
                        projection, null, null, null);
            }
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndex(projection[0]);
                    pictureFilePath = cursor.getString(columnIndex);
                    int orientation = -1;
                    orientation = cursor.getInt(cursor.getColumnIndex(projection[1]));
                    Matrix matrix = new Matrix();
                    matrix.reset();
                    matrix.postRotate(orientation);
                    cursor.close();
                    new AsyncScaleImage().execute();
                } else {
                    if (selectedImage.toString().startsWith("file://")) {
                        pictureFilePath = selectedImage.getPath();
                        new AsyncScaleImage().execute();
                    }
                }
            } catch (NullPointerException e) {
                Toast.makeText(this, "Failed to load image. Try loading from a different app or folder instead.", Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == CAMERA_CODE && resultCode == RESULT_OK) {
            pictureFilePath = cameraFileUri.getPath();
            new AsyncScaleImage().execute();
        }
        if (resultCode == RESULT_CANCELED) {
            finish();
        }

    }

    @Override
    public void onPause() {
        if (cameraFileUri != null) {
            getContentResolver().notifyChange(cameraFileUri, null);
        }
        if (changedBitmap != null) {
            changedBitmap.recycle();
            changedBitmap = null;
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (pictureFilePath != null) {
            changedBitmap = ImageUtils.lessResolution(pictureFilePath, 600);
            imageView.setImageBitmap(changedBitmap);
        }
        ((Button) findViewById(R.id.analyze)).setText(R.string.analyzeText);
    }

    @Override
    public void onRequestPermissionsResult(int permsRequestCode, final String[] permissions, int[] grantResults) {
        switch (permsRequestCode) {
            case PERMISSION_STORAGE:
                boolean storage = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
                if (storage) {
                    if (choice == Consts.CHOOSE_FROM_LIBRARY)
                        chooseFromLibrary();
                    else if (choice == Consts.TAKE_PICTURE)
                        takePicture();

                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0])) {
                        Snackbar.make(findViewById(R.id.main_layout), "Access to Storage is required!",
                                Snackbar.LENGTH_INDEFINITE).setAction(android.R.string.ok, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                ActivityCompat.requestPermissions(AnalyzerActivity.this, permissions, PERMISSION_STORAGE);
                            }
                        }).show();
                    } else {
                        Snackbar.make(findViewById(R.id.main_layout), "Allow Access to Storage in Settings!",
                                Snackbar.LENGTH_INDEFINITE).setAction(android.R.string.ok, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                startInstalledAppDetailsActivity(AnalyzerActivity.this);
                                android.os.Process.killProcess(android.os.Process.myPid());
                                System.exit(0);
                            }
                        }).show();
                    }
                }
                break;
        }
    }

    private boolean hasPermission(String perm) {
        return ContextCompat.checkSelfPermission(this, perm) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Analyzes the image or shows the result if already analyzed.
     *
     * @param view the view
     */
    public void onButtonPress(View view) {
        if (((Button) view).getText().equals("Analyze")) {
            dlg = ProgressDialog.show(this, "Analyzing", "Please wait...", true, false);
            new AnalyzeImageTask().execute();

        } else if (((Button) view).getText().equals("Results")) {
            SimpleRegression reg = new SimpleRegression();
            boolean incomplete = false;
            for (int i = 0; i < NB_BLOBS; i++) {
                SampleModel s = samples.get(i);
                if (s.getDataPointType() == SampleModel.DataPointType.NONE || !s.isUpdated()) {
                    incomplete = true;
                    break;
                } else if (s.getDataPointType() == SampleModel.DataPointType.KNOWN) {
                    reg.addData(s.getIntensity(), s.getConcentration());
                }
            }
            if (incomplete) {
                Toast.makeText(this, "Incomplete Data!", Toast.LENGTH_SHORT).show();
                return;
            }
            double slope = reg.getSlope();
            double inter = reg.getIntercept();
            ArrayList<SampleModel> knownSamples = new ArrayList<>();
            ArrayList<SampleModel> unKnownSamples = new ArrayList<>();
            ArrayList<SampleModel> qcSamples = new ArrayList<>();
            for (int i = 0; i < samples.size(); i++) {
                SampleModel sampleModel = samples.get(i);
                if (sampleModel.getDataPointType() == SampleModel.DataPointType.KNOWN) {
                    knownSamples.add(sampleModel);
                } else if (sampleModel.getDataPointType() == SampleModel.DataPointType.QUALITY_CONTROL) {
                    qcSamples.add(sampleModel);
                } else if (sampleModel.getDataPointType() == SampleModel.DataPointType.UNKNOWN) {
                    unKnownSamples.add(sampleModel);
                }
            }
            final Intent resultIntent = new Intent(AnalyzerActivity.this, ResultActivity.class);
            resultIntent.putExtra(INTERCEPT_KEY, inter);
            resultIntent.putExtra(SLOPE_KEY, slope);
            resultIntent.putParcelableArrayListExtra(KNOWN_SAMPLES_KEY, knownSamples);
            resultIntent.putParcelableArrayListExtra(UNKNOWN_SAMPLES_KEY, unKnownSamples);
            resultIntent.putParcelableArrayListExtra(QC_SAMPLES_KEY, qcSamples);
            startActivity(resultIntent);

        }
    }

    /**
     * Updates bitmap to indicate data the that has been entered by highlighting it in red.
     */
    public void updateBitmap() {
        Mat updated = new Mat();
        org.opencv.android.Utils.bitmapToMat(changedBitmap, updated);
        for (int i = 0; i < samples.size(); i++) {
            if (samples.get(i).isUpdated()) {
                Rect r = rects[i];
                Imgproc.rectangle(updated, new Point(r.x, r.y), new Point(r.x + r.width, r.y + r.height), new Scalar(255, 0, 0), 2);
            }
        }
        org.opencv.android.Utils.matToBitmap(updated, changedBitmap);
        imageView.setImageBitmap(changedBitmap);
        updated.release();
        updated = null;
    }

    /**
     * Segment the image to detect samples. Returns true if the detection was successful.
     *
     * @return true if the detection was successful.
     */
    public boolean segmentImage() {
        if (changedBitmap != null) {
            try {
                saveBitmap(changedBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
            samples = new HashMap<>();
            rects = new Rect[NB_BLOBS];
            int rectIdx = 0;
            Mat raw = new Mat();
            Mat gray = new Mat();
            Mat th = new Mat();
            org.opencv.android.Utils.bitmapToMat(changedBitmap, raw);
            Imgproc.cvtColor(raw, gray, Imgproc.COLOR_RGB2GRAY);
            Imgproc.GaussianBlur(gray, gray, new Size(5, 5), 0);
            Imgproc.adaptiveThreshold(gray, th, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 85, 2);
            Imgproc.medianBlur(th, th, 5);
            Mat se = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(15, 15));
            Imgproc.morphologyEx(th, th, Imgproc.MORPH_CLOSE, se);
            List<MatOfPoint> contours = new ArrayList<>();
            Mat hieh = new Mat();
            Imgproc.findContours(th, contours, hieh, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
            hieh.release();
            HashMap<Integer, Integer> dict = new HashMap<>();
            for (int idx = 0; idx < contours.size(); idx++) {
                MatOfPoint contour = contours.get(idx);
                if (isValidBlob(contour)) {
                    Rect r = Imgproc.boundingRect(contour);
                    Point center = new Point(r.x + r.width / 2, r.y + r.height / 2);
                    addToDict(dict, center);
                }
            }
            boolean foundSixBlobs = false;
            int centerY = 0;
            Iterator it = dict.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                int value = (int) pair.getValue();
                if (value == 6) {
                    centerY = (int) pair.getKey();
                    foundSixBlobs = true;
                }
            }
            if (!foundSixBlobs)
                return false;
            int minX = raw.width();
            int maxX = 0;
            List<MatOfPoint> sampleCandidates = new ArrayList<>();
            for (int idx = 0; idx < contours.size(); idx++) {
                MatOfPoint contour = contours.get(idx);
                if (isValidBlob(contour)) {
                    Rect r = Imgproc.boundingRect(contour);
                    Point center = new Point(r.x + r.width / 2, r.y + r.height / 2);
                    if (centerY - center.y > 20) {
                        sampleCandidates.add(contour);
                    }
                    if (Math.abs(centerY - center.y) <= 15 && rectIdx < 6) {
                        if (r.x < minX)
                            minX = r.x;
                        if (r.x + r.width > maxX)
                            maxX = r.x + r.width;
                        rects[rectIdx] = new Rect(r.x, r.y, r.width, r.height);
                        Mat clip = new Mat(gray, r);
                        Mat clipThresh = new Mat();
                        Imgproc.threshold(clip, clipThresh, 0, 255, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);
                        double intensity_sum = 0;
                        int n = 0;
                        for (int y = 0; y < r.height; y++) {
                            for (int x = 0; x < r.width; x++) {
                                double B = clipThresh.get(y, x)[0];
                                if (B == 0) {
                                    double I = clip.get(y, x)[0];
                                    intensity_sum += I;
                                    n += 1;
                                }
                            }
                        }
                        double intensity = intensity_sum / n;
                        SampleModel sample = new SampleModel(intensity);
                        samples.put(rectIdx, sample);
                        rectIdx++;
                        Imgproc.rectangle(raw, new Point(r.x, r.y), new Point(r.x + r.width, r.y + r.height), new Scalar(0, 255, 0), 2);
                        clip.release();
                        clipThresh.release();

                    }

                }
            }
            double centerOfStandard = (minX + maxX) / 2;
            if (sampleCandidates.size() > 0) {
                MatOfPoint sample = sampleCandidates.get(0);
                Rect r = Imgproc.boundingRect(sample);
                Point center = new Point(r.x + r.width / 2, r.y + r.height / 2);
                double delta = Math.abs(center.x - centerOfStandard);
                for (int idx = 1; idx < sampleCandidates.size(); idx++) {
                    MatOfPoint contour = sampleCandidates.get(idx);
                    r = Imgproc.boundingRect(contour);
                    center = new Point(r.x + r.width / 2, r.y + r.height / 2);
                    double d = Math.abs(center.x - centerOfStandard);
                    if (d < delta) {
                        delta = d;
                        sample = contour;
                    }
                }
                r = Imgproc.boundingRect(sample);
                center = new Point(r.x + r.width / 2, r.y + r.height / 2);
                rects[rectIdx] = new Rect(r.x, r.y, r.width, r.height);
                Mat clip = new Mat(gray, r);
                Mat clipThresh = new Mat();
                Imgproc.threshold(clip, clipThresh, 0, 255, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);
                double intensity_sum = 0;
                int n = 0;
                for (int y = 0; y < r.height; y++) {
                    for (int x = 0; x < r.width; x++) {
                        double B = clipThresh.get(y, x)[0];
                        if (B == 0) {
                            double I = clip.get(y, x)[0];
                            intensity_sum += I;
                            n += 1;
                        }
                    }
                }
                double intensity = intensity_sum / n;
                SampleModel sampleModel = new SampleModel(intensity);
                samples.put(rectIdx, sampleModel);
                rectIdx++;
                Imgproc.rectangle(raw, new Point(r.x, r.y), new Point(r.x + r.width, r.y + r.height), new Scalar(0, 255, 0), 2);
                clip.release();
                clipThresh.release();
            }
            org.opencv.android.Utils.matToBitmap(raw, changedBitmap);
            raw.release();
            gray.release();
            th.release();
            if (rectIdx == 7)
                return true;
        }
        return false;
    }

    private void addToDict(HashMap<Integer, Integer> dict, Point center) {
        boolean found = false;
        Iterator it = dict.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            int Y = (int) pair.getKey();
            if (Math.abs(Y - center.y) <= 15) {
                found = true;
                Integer value = (Integer) pair.getValue();
                value += 1;
                pair.setValue(value);
            }
        }
        if (!found)
            dict.put((int) center.y, 1);
    }

    private boolean isValidBlob(MatOfPoint contour) {
        boolean flag = true;
        double area = Imgproc.contourArea(contour);
        if (area < 1000 || area > 16000)
            flag = false;
        Rect r = Imgproc.boundingRect(contour);
        float aspectRatio;
        int w = r.width, h = r.height;
        if (w > h)
            aspectRatio = ((float) w) / h;
        else
            aspectRatio = ((float) h) / w;
        if (aspectRatio >= 1.5)
            flag = false;
        return flag;
    }

    private boolean isDemoOn() {
        return prefs.getBoolean(DEMO_MODE_KEY, true);
    }

    private void setValuesAutomatically() {
        ArrayList<IdAbscissaMap> idXMap = new ArrayList<>();
        for (int idx = 0; idx < rects.length - 1; idx++) {
            IdAbscissaMap iam = new IdAbscissaMap(rects[idx].x, idx);
            idXMap.add(iam);
        }
        Collections.sort(idXMap);
        double[] concentrations = new double[]{100, 150, 200, 250, 300, 350};
        for (int idx = 0; idx < idXMap.size(); idx++) {
            int _id = idXMap.get(idx).getId();
            SampleModel sm = samples.get(_id);
            sm.setConcentration(concentrations[idx]);
            if (idx != 2)
                sm.setDataPointType(SampleModel.DataPointType.KNOWN);
            else
                sm.setDataPointType(SampleModel.DataPointType.QUALITY_CONTROL);
            sm.setUpdated(true);
        }
        samples.get(rects.length - 1).setDataPointType(SampleModel.DataPointType.UNKNOWN);
        samples.get(rects.length - 1).setUpdated(true);
        updateBitmap();
    }

    private class AsyncScaleImage extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... v) {
            changedBitmap = ImageUtils.lessResolution(pictureFilePath, 600);
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            imageView.setImageBitmap(changedBitmap);
        }
    }

    private class AnalyzeImageTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean result = segmentImage();
            return result;
        }

        @Override
        protected void onPostExecute(Boolean b) {
            if (dlg != null)
                dlg.dismiss();
            if (b) {
                imageView.setImageBitmap(changedBitmap);
                ((Button) AnalyzerActivity.this.findViewById(R.id.analyze)).setText(R.string.resultsText);
                analyzedCorrectly = true;
                if (isDemoOn())
                    setValuesAutomatically();
            } else {
                Toast.makeText(AnalyzerActivity.this, "Could not detect ROIs", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

