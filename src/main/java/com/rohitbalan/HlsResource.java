package com.rohitbalan;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.io.IOUtils;

/**
 * Root resource (exposed at "myresource" path)
 */
@Path("/")
public class HlsResource {

    
	@GET
    @Path("getHlsAsMkv.ts")
    @Produces("video/ts")
	public Response getHlsAsMkv() {
		try {
			System.out.println("Calling getHlsAsMkv");
			StreamingOutput stream = new StreamingOutput() {
				@SuppressWarnings("unused")
				@Override
				public void write(OutputStream os) throws IOException,
						WebApplicationException {
					String fileName = "/tmp/hlsDownloader.txt";
					String lastPart = null;
					
					a:
					while(true) {
						BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
						String line;
						Set<String> parts = new LinkedHashSet<String>();
						while((line=reader.readLine())!=null) {
					    	if(!line.isEmpty()) {
					    		parts.add(line);
					    		
					    	}
					    }
						
						if(lastPart==null || !parts.contains(lastPart)) {
							for(String part: parts) {
								lastPart = part;
								System.out.println(part);
								File file = new File(part);
					    		if(file.exists()) {
					    			IOUtils.copy(new FileInputStream(file),os);
					    		}
							}
						} else {
							boolean skip = true;
							for(String part: parts) {
								if(skip) {
									if(part.equals(lastPart)) {
										skip = false;
									}
								} else {
									lastPart = part;
									File file = new File(part);
						    		if(file.exists()) {
						    			IOUtils.copy(new FileInputStream(file),os);
						    		}
								}
							}
							//break a;
							
						}
						
						reader.close();
					}
				}
			};
			return Response.ok(stream)
					.header("Content-Length", "1452930260")
					.header("X-Content-Duration", "00:01:00.046")
					.header("Content-Disposition", "attachment; filename=\"play me.mkv\"")
					.build();
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
     
}
