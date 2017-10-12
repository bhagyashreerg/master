package clientWebservice;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Scanner;


public class FileManager {
	public void confirmDir(String dirName)throws IOException{		
		
		File folder = new File(dirName);				
		if(!folder.exists() && !folder.isDirectory()){
			folder.mkdir();
		} else {
			//System.out.println("Folder already exists");
		}
	}
	
	public String[] listFiles(String src)throws IOException{
		String[] listFiles =null;
		File afile =new File(src);
		listFiles = afile.list();
		Arrays.sort(listFiles);
		return listFiles;
	}	
	
	public void deleteFiles(String src)throws IOException{
		
		File file = new File(src);         
        String[] myFiles;       
            if(file.isDirectory()){   
                myFiles = file.list();   
                for (int i=0; i<myFiles.length; i++) {   
                    File myFile = new File(file, myFiles[i]);    
                    myFile.delete();   
                }   
             }   
	}
	
	public void replace(String oldFileName, String tmpFileName, String searchString, String replaceString) {
	
		/*	
		System.out.println("oldFileName: " + oldFileName);
		System.out.println("tmpFileName: " + tmpFileName);
		System.out.println("searchString: " + searchString);
		System.out.println("replaceString: " + replaceString);
		*/
		
	      BufferedReader br = null;
	      BufferedWriter bw = null;
	      try {
	         br = new BufferedReader(new FileReader(oldFileName));
	         bw = new BufferedWriter(new FileWriter(tmpFileName));
	         String line;
	         while ((line = br.readLine()) != null) {
	            if (line.contains(searchString))
	            	//System.out.println("......replacing ");
	               line = line.replace(searchString, replaceString);
	            bw.write(line+"\n");
	         }
	      } catch (Exception e) {
	         return;
	      } finally {
	         try {
	            if(br != null)
	               br.close();
	         } catch (IOException e) {
	            //
	        	 System.out.println("Error1" + e.getStackTrace());
	         }
	         try {
	            if(bw != null)
	               bw.close();
	         } catch (IOException e) {
	            //
	        	 System.out.println("Error2" + e.getStackTrace());
	         }
	      }
	      // Once everything is complete, delete old file..
	      File oldFile = new File(oldFileName);
	      oldFile.delete();

	      // And rename tmp file's name to old file name
	      File newFile = new File(tmpFileName);
	      newFile.renameTo(oldFile);

	   }
		
	public void copyFile(String source, String target,String[] list, MasterData md) throws IOException {
		
		/*String target_file = null;
		String tmp_target_file = null;*/
		
		for(int i=0;i<list.length;i++){
			//System.out.println("FileName: " + list[i]);
			File origin = new File(source +"//"+list[i]);
			File destination = new File(target +"//"+list[i]);
	    	InputStream in = new FileInputStream(origin);
	        OutputStream out = new FileOutputStream(destination);
	    
	        // Copy the bits from instream to outstream
	        byte[] buf = new byte[1024];
	        int len;

	       while ((len = in.read(buf)) > 0) {
	            out.write(buf, 0, len);
	        }

	       in.close();
	       out.close();
		}
    }
	
	
	public static boolean find(File f, String searchString) {
        boolean result = false;
        Scanner in = null;
        try {
            in = new Scanner(new FileReader(f));
            while(in.hasNextLine() && !result) {
                result = in.nextLine().indexOf(searchString) >= 0;
            }
        }
        catch(IOException e) {
            e.printStackTrace();      
        }
        finally {
            try { 
            	if(in != null)
            	in.close() ; } catch(Exception e) { /* ignore */ }  
        }
        return result;
    }
	
	public File createWorkDir(String filename, MasterData md, AcsLabels al){
		
	//	System.out.println(md.getAUTO_EPOCHTIME());
		File destination_file = new File(filename+"//"+md.getAUTO_EPOCHTIME());
		if(destination_file.exists()==false){ 
			destination_file.mkdir();
		} else {
		//	System.out.println("Already exists");
			md.getAUTO_NUMBER();
		//	System.out.println(md.getAUTO_EPOCHTIME());
			createWorkDir(filename, md, al);
		}
		return destination_file;
	}

}
