package com.luna.controller.helper;

import java.io.*;
import java.util.*; 

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.luna.controller.hadoop.HDFSUpload;

public class UploadFiles extends HttpServlet {  
   /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
   private boolean isMultipart;
   private String filePath;
   private long maxFileSize = Long.MAX_VALUE;
   private int maxMemSize = 100 * 1024;
   private File file ;
   final String CATALINA = System.getProperty("catalina.home");
   final String FILES = CATALINA+"/webapps/ROOT/Luna/Pending-Files/";
   public void init( ){
      // Get the file location where it would be stored.
      filePath = FILES; 
   }
   public void doPost(HttpServletRequest request, 
               HttpServletResponse response)
              throws ServletException, java.io.IOException {
      // Check that we have a file upload request
      isMultipart = ServletFileUpload.isMultipartContent(request);
      response.setContentType("application/json");
      java.io.PrintWriter out = response.getWriter( );
      if( !isMultipart ){
    	 out.println("{"); 
         out.println("\"error\" : \"Request is not in multipart form.\"");
         out.println("}");
         return;
      }
      DiskFileItemFactory factory = new DiskFileItemFactory();
      // maximum size that will be stored in memory
      factory.setSizeThreshold(maxMemSize);
      // Location to save data that is larger than maxMemSize.
      factory.setRepository(new File("/tmp"));
      // Create a new file upload handler
      ServletFileUpload upload = new ServletFileUpload(factory);
      // maximum file size to be uploaded.
      upload.setSizeMax( maxFileSize );
      try{ 
      // Parse the request to get file items.
      List<FileItem> fileItems = upload.parseRequest(request);
      // Process the uploaded file items
      Iterator<FileItem> i = fileItems.iterator();    
      out.println("{"); 
      while ( i.hasNext () ) 
      {
         FileItem fi = (FileItem)i.next();
         if ( !fi.isFormField () )  
         {
            // Get the uploaded file parameters
            //String fieldName = fi.getFieldName();
            String fileName = fi.getName();
            //String contentType = fi.getContentType();
            //boolean isInMemory = fi.isInMemory();
            //long sizeInBytes = fi.getSize();
            // Write the file
            if( fileName.lastIndexOf("/") >= 0 ){
               file = new File( filePath + 
               fileName.substring( fileName.lastIndexOf("/"))) ;
            }else{
               file = new File( filePath + 
               fileName.substring(fileName.lastIndexOf("/")+1)) ;
            }
            if(!file.exists()){
            	fi.write(file);
            	HDFSUpload hdfs = new HDFSUpload(file);
            	hdfs.copy2HDFS();
            	out.println("\"success\" : \"Uploaded Filename: " + fileName + "\""); 
            }else{
            	out.println("\"error\" : \"Upload Failed: File " + fileName + " already exists.\""); 
            }           
         }
      }
      out.println("}");
   }catch(Exception ex) {
       System.out.println(ex);
   }
   }
   public void doGet(HttpServletRequest request, 
                       HttpServletResponse response)
        throws ServletException, java.io.IOException {      
        throw new ServletException("GET method used with " +
                getClass( ).getName( )+": POST method required.");
   } 
}

