package tukano.servers.java;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.logging.Logger;

import tukano.api.java.Blobs;
import tukano.api.java.Result;
import tukano.api.java.Result.ErrorCode;
import tukano.api.java.Shorts;
import tukano.clients.ClientFactory;

public class JavaBlobs implements Blobs {

    private static Logger Log = Logger.getLogger(JavaBlobs.class.getName());

    @Override
    public Result<Void> upload(String blobId, byte[] bytes) {
        Log.info("upload : blobId = " + blobId + "; bytes = " + bytes);

        Result<Void> checkBlobIdResult = checkBlobId(blobId);
        if(!checkBlobIdResult.isOK())return Result.error(checkBlobIdResult.error());

        File blob = new File("blob" + blobId);
        try {
            if (!blob.exists()) {
                FileOutputStream outputStream = new FileOutputStream(blob);
                outputStream.write(bytes);
                outputStream.close();
            } else {
                byte[] bytes2 = new byte[(int) blob.length()];
                FileInputStream fis = new FileInputStream(blob);
                fis.read(bytes2);
                fis.close();
                if (!Arrays.equals(bytes, bytes2)) {
                    Log.info("bytes do not match");
                    return Result.error(ErrorCode.CONFLICT);
                }
            }
        } catch (IOException e) {
            Log.info("IO exception");
            return Result.error(ErrorCode.BAD_REQUEST);
        }
        return Result.ok();
    }

    @Override
    public Result<byte[]> download(String blobId) {
        Log.info("download : blobId = " + blobId);
        
        File blob = new File("blob" + blobId);
        byte[] bytes = new byte[(int) blob.length()];

        try (FileInputStream fis = new FileInputStream(blob)) {
            fis.read(bytes);
        } catch (FileNotFoundException e) {
            Log.info("file wasn't found");
            return Result.error(ErrorCode.NOT_FOUND);
        } catch (IOException e) {
            Log.info("IO exception");
            return Result.error(ErrorCode.BAD_REQUEST);
        }
        return Result.ok(bytes);
    }

    @Override
    public Result<Void> deleteBlob(String blobId) {
        Log.info("deleteBlob : blobId = " + blobId);

        File blob = new File("blob" + blobId);
        try {
            if (!Files.deleteIfExists(blob.toPath())) {
                Log.info("File not found.");
                return Result.error(ErrorCode.NOT_FOUND);
            }
        } catch (IOException e) {
            Log.info("IO exception");
            return Result.error(ErrorCode.BAD_REQUEST);
        }

        Log.info("File deleted successfully.");

        return Result.ok();
    }

    private Result<Void> checkBlobId(String blobId) {
        Log.info("checking blobId : "+blobId);
        Result<Shorts> shortsClient = ClientFactory.getShortsClient();

        if (!shortsClient.isOK()) {
            Log.info("Server error");
            return Result.error(ErrorCode.BAD_REQUEST);
        }

        Result<Void> a = shortsClient.value().checkBlobId(blobId);

        return a;
    }

}