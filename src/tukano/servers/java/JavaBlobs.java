package tukano.servers.java;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Logger;

import tukano.api.java.Blobs;
import tukano.api.java.Result;
import tukano.api.java.Result.ErrorCode;

public class JavaBlobs implements Blobs{
	
	private static Logger Log = Logger.getLogger(JavaBlobs.class.getName());

	@Override
	public Result<Void> upload(String blobId, byte[] bytes){
		Log.info("upload : blobId = " + blobId + "; bytes = " + bytes);
		
		File blob = new File("blob" + blobId + ".txt");
		try {
			if (blob.createNewFile()) {
				FileOutputStream outputStream = new FileOutputStream(blob);
				outputStream.write(bytes);
				outputStream.close();
			  } else {
				  byte[] bytes2 = new byte[(int) blob.length()];
				  FileInputStream fis = new FileInputStream(blob);
				  fis.read(bytes2);
				  fis.close();
				  if(!Arrays.equals(bytes, bytes2)) {
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
		
		File blob = new File("blob" + blobId + ".txt");
		byte[] bytes = new byte[(int) blob.length()];
		
		try(FileInputStream fis = new FileInputStream(blob)) {
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
	
}