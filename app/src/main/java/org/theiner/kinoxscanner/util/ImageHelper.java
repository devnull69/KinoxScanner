package org.theiner.kinoxscanner.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
}
