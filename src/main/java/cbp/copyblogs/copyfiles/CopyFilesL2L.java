package cbp.copyblogs.copyfiles;

import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;


/**
 * 
 * @author Christian Parker
 *
 * Class for copying files from one local folder to another
 */
public class CopyFilesL2L extends CopyFiles {
    
    private final String sourceAdditionalFilesPath;
    private final String destinationAdditionalFilesPath;
    private final Integer sourceBlogId;
    private final Integer destinationBlogId;

    

    public CopyFilesL2L(String sourceAdditionalFilesPath, String destinationAdditionalFilesPath, Integer sourceBlogId, Integer destinationBlogId) {
    	super();
    	this.sourceAdditionalFilesPath = sourceAdditionalFilesPath;
        this.destinationAdditionalFilesPath = destinationAdditionalFilesPath;
        this.sourceBlogId = sourceBlogId;
        this.destinationBlogId = destinationBlogId;
    }
    
    	
    	
    /**
     * Copy files locally
     * 
     * @throws IOException for FileUtils
     */
    public void copy() throws IOException {
		
		try {
			logger.info("Copying additional files. This may take a few minutes.");
			
			File sourceMediaDir = new File(sourceAdditionalFilesPath + sourceBlogId);
			File destinationMediaDir = new File(destinationAdditionalFilesPath + destinationBlogId);
			
			if(destinationMediaDir.exists())
				FileUtils.deleteDirectory(destinationMediaDir);
			if(sourceMediaDir.exists())
				FileUtils.copyDirectory(sourceMediaDir, destinationMediaDir);
			
		} catch (IOException e) {
			logger.error("There was an error in copying the Additional Files.");
			logger.error(e.getMessage());
			throw e;
		}
		
		logger.info("Additional files copied.");
    }
	
}
