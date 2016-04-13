package org.theiner.kinoxscanner.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.widget.ImageView;

import org.theiner.kinoxscanner.async.GetImageTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * Created by TTheiner on 12.04.2016.
 */
public class ImageHelper {
    public static Bitmap getImage(String addr) {
        Bitmap result = null;

        File pictureFileDir = getDir();
        pictureFileDir.mkdirs();

        String imageFilename = pictureFileDir.getPath() + File.separator + addr + ".jpg";

        File imageFile = new File(imageFilename);

        if(imageFile.exists()) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            result = BitmapFactory.decodeFile(imageFilename, options);
        }

        return result;
    }

    public static Bitmap retrieveImage(String imageSubDir, String addr) {
        // Called from task
        // http://www.kinox.to/statics/thumbs/<imageSubDir>/<addr>.jpg
        return HTTPHelper.getBitmapFromURL("http://www.kinox.to/statics/thumbs/" + imageSubDir + "/" + addr + ".jpg");
    }

    public static void storeImageInCache(Bitmap image, String addr) {
        // save image bitmap to cache
        File pictureFileDir = getDir();
        pictureFileDir.mkdirs();

        String imageFilename = pictureFileDir.getPath() + File.separator + addr + ".jpg";

        File newFile = new File(imageFilename);

        if(!newFile.exists()) {
            try {
                FileOutputStream out = new FileOutputStream(newFile);
                image.compress(Bitmap.CompressFormat.JPEG, 90, out);
                out.flush();
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void removeImage(String addr) {
        File pictureFileDir = getDir();
        pictureFileDir.mkdirs();

        String imageFilename = pictureFileDir.getPath() + File.separator + addr + ".jpg";

        File imageFile = new File(imageFilename);

        if(imageFile.exists()) {
            imageFile.delete();
        }
    }

    private static File getDir() {
        File sdDir = Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return new File(sdDir, "KinoxScannerCache");
    }

    public static void startGetImageTask(final ImageView ivCoverArt, String imageSubDir, final String addr) {
        // Bild laden
        GetImageTask.CheckCompleteListener ccl = new GetImageTask.CheckCompleteListener() {
            @Override
            public void onCheckComplete(Bitmap result) {
                // Bild an der Serie speichern und auf Platte ablegen, dann im ImageView anzeigen
                ImageHelper.storeImageInCache(result, addr);
                ivCoverArt.setImageBitmap(result);
            }
        };

        GetImageTask myTask = new GetImageTask(ccl);
        myTask.execute(imageSubDir, addr);
    }

}
