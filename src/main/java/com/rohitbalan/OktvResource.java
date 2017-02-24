package com.rohitbalan;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import com.google.common.io.Files;
import com.rohitbalan.core.OktvService;

/**
 * Root resource (exposed at "myresource" path)
 */
@Path("/")
public class OktvResource {

	@GET
	@Path("urlresolver")
	@Produces(MediaType.TEXT_PLAIN)
	public String getOklivetv(@QueryParam("id") String id) {
		return new OktvService().resolvedUrl(id);
	}

	@GET
	@Path("getMergedTs")
	@Produces("video/mp4")
	public Response getMergedTs(final @QueryParam("url") String url) throws URISyntaxException, InterruptedException {
		System.out.println("Calling getMergedTs: " + url);

		StreamingOutput stream = new StreamingOutput() {

			@Override
			public void write(OutputStream out) throws IOException, WebApplicationException {
				int SEGMENT_SIZE = 2048;
				try {
					byte[] subset = null;
					while (true) {
						long startTime = Calendar.getInstance().getTimeInMillis();
						byte[] bytes = new OktvService().getBinaryContentFromUrl(url);
						System.out.print("Length: " + bytes.length + "\t");
						if (bytes.length <= SEGMENT_SIZE) {
							System.out.println();
							Thread.sleep(3000);
							continue;
						}
						int percent;
						byte[] newData;
						if (subset == null) {
							newData = bytes;
						} else {
							int index = indexOf(bytes, subset);
							System.out.print("index: " + index + "\t");
							if (index == -1) {
								newData = bytes;
							} else {
								newData = Arrays.copyOfRange(bytes, index + SEGMENT_SIZE, bytes.length);
							}
						}

						percent = (newData.length) * 100 / bytes.length;
						System.out.print("Percent: " + percent + "%\t");
						out.write(newData);

						subset = Arrays.copyOfRange(bytes, bytes.length - SEGMENT_SIZE, bytes.length);
						long endTime = Calendar.getInstance().getTimeInMillis();

						/*
						 * long sleepTime = (bytes.length //- index -
						 * SEGMENT_SIZE )/229;
						 */

						long sleepTime = getVideoDurationInMilliSeconds(newData);
						System.out.print("sleepTime: " + sleepTime + "\t");
						long downloadTime = endTime - startTime;
						System.out.print("downloadTime: " + downloadTime + "\t");

						if (sleepTime > downloadTime) {
							long correctedSleepTime = sleepTime - downloadTime - 2000l;
							System.out.print("correctedSleepTime: " + correctedSleepTime + "\t");

							if (correctedSleepTime < 100l)
								correctedSleepTime = 100l;
							if (correctedSleepTime > 8000l)
								correctedSleepTime = 8000l;

							Thread.sleep(correctedSleepTime);
						}
						System.out.println();
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		return Response.ok(stream)
				// .header("Content-Length", "10000000000")
				.build();
	}

	private int getVideoDurationInMilliSeconds(byte[] bytes) {
		try {
			String videoDuration = "";
			File video = File.createTempFile("OkTv", ".ts");
			Files.write(bytes, video);
			Process process = new ProcessBuilder("ffprobe", "-show_streams", "-i", video.getCanonicalPath()).start();
			InputStream is = process.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String line;

			Pattern p = Pattern.compile("^duration=(.*)$");

			while ((line = br.readLine()) != null) {
				Matcher m = p.matcher(line);
				while (m.find()) {
					videoDuration = m.group(1);
				}
			}
			video.deleteOnExit();
			int videoDurationInMilliSeconds = (int) (Float.parseFloat(videoDuration) * 1000);
			System.out.print("videoDuration: " + videoDurationInMilliSeconds + "\t");
			return videoDurationInMilliSeconds;
		} catch (Exception e) {
			e.printStackTrace();
			return 10000;
		}
	}

	public int indexOf(byte[] outerArray, byte[] smallerArray) {
		for (int i = 0; i < outerArray.length - smallerArray.length + 1; ++i) {
			boolean found = true;
			for (int j = 0; j < smallerArray.length; ++j) {
				if (outerArray[i + j] != smallerArray[j]) {
					found = false;
					break;
				}
			}
			if (found)
				return i;
		}
		return -1;
	}

}
